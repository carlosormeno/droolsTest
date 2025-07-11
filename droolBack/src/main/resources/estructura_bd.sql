--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13
-- Dumped by pg_dump version 16.9 (Ubuntu 16.9-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: evaluar_monto_contra_topes(numeric, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.evaluar_monto_contra_topes(p_monto numeric, p_anio integer DEFAULT EXTRACT(year FROM CURRENT_DATE)) RETURNS TABLE(tipo_proceso character varying, categoria character varying, sub_categoria character varying, cumple_condicion boolean, condicion_texto text, monto_limite numeric, operador_simbolo character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        tps.nombre as tipo_proceso,
        oc.nombre as categoria,
        COALESCE(sdc.nombre, '') as sub_categoria,
        CASE
            WHEN om.codigo = 'MAYOR_IGUAL' THEN p_monto >= t.monto
            WHEN om.codigo = 'MENOR_IGUAL' THEN p_monto <= t.monto
            WHEN om.codigo = 'MAYOR' THEN p_monto > t.monto
            WHEN om.codigo = 'MENOR' THEN p_monto < t.monto
        END as cumple_condicion,
        om.nombre || ' ' || TO_CHAR(t.monto, 'FM999,999,999') as condicion_texto,
        t.monto as monto_limite,
        om.simbolo as operador_simbolo
    FROM topes t
    JOIN tipo_proceso_seleccion tps ON t.id_tipo_proceso_seleccion = tps.id
    JOIN objeto_contratacion oc ON t.id_objeto_contratacion = oc.id
    JOIN operadores_monto om ON t.id_operador_monto = om.id
    LEFT JOIN sub_descripcion_contratacion sdc ON t.id_sub_descripcion_contratacion = sdc.id
    WHERE t.anio_vigencia = p_anio
      AND t.estado = 'ACTIVO'
      AND t.estado_registro = TRUE
      AND tps.estado = 'ACTIVO'
      AND tps.estado_registro = TRUE
      AND om.estado = 'ACTIVO'
      AND om.estado_registro = TRUE;
END;
$$;


--
-- Name: get_current_user(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.get_current_user() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN COALESCE(current_setting('app.current_user', true), 'SISTEMA');
END;
$$;


--
-- Name: handle_audit_insert(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.handle_audit_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.created_at = CURRENT_TIMESTAMP;
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.estado_registro = COALESCE(NEW.estado_registro, TRUE);

    -- Si no se especifica created_by, usar el usuario de la sesión o 'SISTEMA'
    IF NEW.created_by IS NULL THEN
        NEW.created_by = COALESCE(current_setting('app.current_user', true), 'SISTEMA');
    END IF;

    NEW.updated_by = NEW.created_by;

    RETURN NEW;
END;
$$;


--
-- Name: handle_audit_update(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.handle_audit_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Preservar fecha de creación y usuario creador
    NEW.created_at = OLD.created_at;
    NEW.created_by = OLD.created_by;

    -- Actualizar fecha y usuario de modificación
    NEW.updated_at = CURRENT_TIMESTAMP;

    -- Si no se especifica updated_by, usar el usuario de la sesión o 'SISTEMA'
    IF NEW.updated_by IS NULL OR NEW.updated_by = OLD.updated_by THEN
        NEW.updated_by = COALESCE(current_setting('app.current_user', true), 'SISTEMA');
    END IF;

    RETURN NEW;
END;
$$;


--
-- Name: handle_logical_delete(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.handle_logical_delete() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- En lugar de eliminar, marcar como eliminado
    UPDATE uit SET
        estado_registro = FALSE,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = COALESCE(current_setting('app.current_user', true), 'SISTEMA')
    WHERE id = OLD.id;

    -- Cancelar el DELETE físico
    RETURN NULL;
END;
$$;


--
-- Name: obtener_uit_vigente(date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.obtener_uit_vigente(p_fecha date) RETURNS TABLE(id bigint, monto numeric, anio_vigencia integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT u.id, u.monto, u.anio_vigencia
    FROM uit u
    WHERE u.anio_vigencia <= EXTRACT(YEAR FROM p_fecha)
      AND u.estado = 'ACTIVO'
      AND u.estado_registro = TRUE
    ORDER BY u.anio_vigencia DESC
    LIMIT 1;
END;
$$;


--
-- Name: restore_record(character varying, bigint, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.restore_record(p_table_name character varying, p_record_id bigint, p_user character varying DEFAULT NULL::character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_user VARCHAR;
    v_sql TEXT;
BEGIN
    v_user := COALESCE(p_user, get_current_user());

    v_sql := format('UPDATE %I SET
                     estado_registro = TRUE,
                     updated_at = CURRENT_TIMESTAMP,
                     updated_by = %L
                     WHERE id = %L AND estado_registro = FALSE',
                    p_table_name, v_user, p_record_id);

    EXECUTE v_sql;

    RETURN FOUND;
END;
$$;


--
-- Name: set_current_user(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.set_current_user(p_username character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM set_config('app.current_user', p_username, false);
END;
$$;


--
-- Name: soft_delete_record(character varying, bigint, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.soft_delete_record(p_table_name character varying, p_record_id bigint, p_user character varying DEFAULT NULL::character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_user VARCHAR;
    v_sql TEXT;
BEGIN
    v_user := COALESCE(p_user, get_current_user());

    v_sql := format('UPDATE %I SET
                     estado_registro = FALSE,
                     updated_at = CURRENT_TIMESTAMP,
                     updated_by = %L
                     WHERE id = %L AND estado_registro = TRUE',
                    p_table_name, v_user, p_record_id);

    EXECUTE v_sql;

    RETURN FOUND;
END;
$$;


--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: business_rules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.business_rules (
    id bigint NOT NULL,
    category character varying(100),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(100),
    description text,
    is_template boolean,
    name character varying(255) NOT NULL,
    priority integer,
    rule_content text NOT NULL,
    status character varying(20) NOT NULL,
    tags character varying(500),
    updated_at timestamp(6) without time zone,
    updated_by character varying(100),
    validation_errors text,
    version integer,
    CONSTRAINT business_rules_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ACTIVE'::character varying, 'INACTIVE'::character varying, 'TESTING'::character varying, 'ERROR'::character varying])::text[])))
);


--
-- Name: business_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.business_rules ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.business_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: objeto_contratacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.objeto_contratacion (
    id bigint NOT NULL,
    codigo character varying(20) NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion text,
    permite_sub_descripcion boolean DEFAULT false,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character varying(100),
    updated_by character varying(100),
    estado_registro boolean DEFAULT true,
    id_sub_descripcion_contratacion bigint,
    CONSTRAINT objeto_contratacion_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying])::text[])))
);


--
-- Name: objeto_contratacion_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.objeto_contratacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: objeto_contratacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.objeto_contratacion_id_seq OWNED BY public.objeto_contratacion.id;


--
-- Name: operadores_monto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.operadores_monto (
    id bigint NOT NULL,
    codigo character varying(20) NOT NULL,
    nombre character varying(50) NOT NULL,
    simbolo character varying(10) NOT NULL,
    descripcion text,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character varying(100),
    updated_by character varying(100),
    estado_registro boolean DEFAULT true,
    CONSTRAINT operadores_monto_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying])::text[])))
);


--
-- Name: operadores_monto_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.operadores_monto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: operadores_monto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.operadores_monto_id_seq OWNED BY public.operadores_monto.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying(50) NOT NULL,
    description text
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: rules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rules (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    drl_content text NOT NULL,
    rule_json jsonb,
    is_active boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character varying(100),
    anio_vigencia integer,
    uses_parametricas boolean DEFAULT false,
    parametricas_config jsonb,
    estado_registro boolean DEFAULT true
);


--
-- Name: rules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rules_id_seq OWNED BY public.rules.id;


--
-- Name: sub_descripcion_contratacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sub_descripcion_contratacion (
    id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character varying(100),
    updated_by character varying(100),
    estado_registro boolean DEFAULT true,
    CONSTRAINT sub_descripcion_contratacion_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying])::text[])))
);


--
-- Name: sub_descripcion_contratacion_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sub_descripcion_contratacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sub_descripcion_contratacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sub_descripcion_contratacion_id_seq OWNED BY public.sub_descripcion_contratacion.id;


--
-- Name: tipo_proceso_seleccion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipo_proceso_seleccion (
    id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(150) NOT NULL,
    descripcion text,
    anio_vigencia integer NOT NULL,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    observaciones text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character varying(100),
    updated_by character varying(100),
    estado_registro boolean DEFAULT true,
    CONSTRAINT tipo_proceso_seleccion_anio_vigencia_check CHECK (((anio_vigencia >= 2020) AND (anio_vigencia <= 2030))),
    CONSTRAINT tipo_proceso_seleccion_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying, 'BORRADOR'::character varying])::text[])))
);


--
-- Name: tipo_proceso_seleccion_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tipo_proceso_seleccion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipo_proceso_seleccion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tipo_proceso_seleccion_id_seq OWNED BY public.tipo_proceso_seleccion.id;


--
-- Name: topes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.topes (
    id bigint NOT NULL,
    id_tipo_proceso_seleccion bigint NOT NULL,
    id_objeto_contratacion bigint NOT NULL,
    id_operador_monto bigint NOT NULL,
    monto numeric(12,2) NOT NULL,
    referencia_uit numeric(8,4),
    observaciones text,
    anio_vigencia integer NOT NULL,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character varying(100),
    updated_by character varying(100),
    estado_registro boolean DEFAULT true,
    CONSTRAINT topes_anio_vigencia_check CHECK (((anio_vigencia >= 2020) AND (anio_vigencia <= 2030))),
    CONSTRAINT topes_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying, 'BORRADOR'::character varying])::text[]))),
    CONSTRAINT topes_monto_check CHECK ((monto >= (0)::numeric))
);


--
-- Name: topes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.topes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: topes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.topes_id_seq OWNED BY public.topes.id;


--
-- Name: uit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.uit (
    id bigint NOT NULL,
    monto numeric(10,2) NOT NULL,
    anio_vigencia integer NOT NULL,
    estado character varying(20) DEFAULT 'ACTIVO'::character varying NOT NULL,
    observaciones text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character varying(100),
    updated_by character varying(100),
    estado_registro boolean DEFAULT true,
    CONSTRAINT uit_anio_vigencia_check CHECK (((anio_vigencia >= 2020) AND (anio_vigencia <= 2030))),
    CONSTRAINT uit_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVO'::character varying, 'INACTIVO'::character varying, 'BORRADOR'::character varying])::text[]))),
    CONSTRAINT uit_monto_check CHECK ((monto > (0)::numeric))
);


--
-- Name: uit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.uit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: uit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.uit_id_seq OWNED BY public.uit.id;


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    user_id integer NOT NULL,
    role_id integer NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    email character varying(100) NOT NULL,
    full_name character varying(100),
    enabled boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: objeto_contratacion id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objeto_contratacion ALTER COLUMN id SET DEFAULT nextval('public.objeto_contratacion_id_seq'::regclass);


--
-- Name: operadores_monto id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operadores_monto ALTER COLUMN id SET DEFAULT nextval('public.operadores_monto_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: rules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rules ALTER COLUMN id SET DEFAULT nextval('public.rules_id_seq'::regclass);


--
-- Name: sub_descripcion_contratacion id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sub_descripcion_contratacion ALTER COLUMN id SET DEFAULT nextval('public.sub_descripcion_contratacion_id_seq'::regclass);


--
-- Name: tipo_proceso_seleccion id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_proceso_seleccion ALTER COLUMN id SET DEFAULT nextval('public.tipo_proceso_seleccion_id_seq'::regclass);


--
-- Name: topes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topes ALTER COLUMN id SET DEFAULT nextval('public.topes_id_seq'::regclass);


--
-- Name: uit id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.uit ALTER COLUMN id SET DEFAULT nextval('public.uit_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: business_rules business_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.business_rules
    ADD CONSTRAINT business_rules_pkey PRIMARY KEY (id);


--
-- Name: objeto_contratacion objeto_contratacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objeto_contratacion
    ADD CONSTRAINT objeto_contratacion_pkey PRIMARY KEY (id);


--
-- Name: operadores_monto operadores_monto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operadores_monto
    ADD CONSTRAINT operadores_monto_pkey PRIMARY KEY (id);


--
-- Name: roles roles_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: rules rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rules
    ADD CONSTRAINT rules_pkey PRIMARY KEY (id);


--
-- Name: sub_descripcion_contratacion sub_descripcion_contratacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sub_descripcion_contratacion
    ADD CONSTRAINT sub_descripcion_contratacion_pkey PRIMARY KEY (id);


--
-- Name: tipo_proceso_seleccion tipo_proceso_seleccion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_proceso_seleccion
    ADD CONSTRAINT tipo_proceso_seleccion_pkey PRIMARY KEY (id);


--
-- Name: topes topes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topes
    ADD CONSTRAINT topes_pkey PRIMARY KEY (id);


--
-- Name: uit uit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.uit
    ADD CONSTRAINT uit_pkey PRIMARY KEY (id);


--
-- Name: objeto_contratacion uk_objeto_contratacion_codigo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objeto_contratacion
    ADD CONSTRAINT uk_objeto_contratacion_codigo UNIQUE (codigo, id_sub_descripcion_contratacion, estado_registro);


--
-- Name: objeto_contratacion uk_objeto_contratacion_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objeto_contratacion
    ADD CONSTRAINT uk_objeto_contratacion_nombre UNIQUE (nombre, estado_registro);


--
-- Name: operadores_monto uk_operadores_codigo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operadores_monto
    ADD CONSTRAINT uk_operadores_codigo UNIQUE (codigo, estado_registro);


--
-- Name: operadores_monto uk_operadores_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operadores_monto
    ADD CONSTRAINT uk_operadores_nombre UNIQUE (nombre, estado_registro);


--
-- Name: operadores_monto uk_operadores_simbolo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operadores_monto
    ADD CONSTRAINT uk_operadores_simbolo UNIQUE (simbolo, estado_registro);


--
-- Name: rules uk_rules_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rules
    ADD CONSTRAINT uk_rules_name UNIQUE (name);


--
-- Name: tipo_proceso_seleccion uk_tipo_proceso_codigo_anio; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_proceso_seleccion
    ADD CONSTRAINT uk_tipo_proceso_codigo_anio UNIQUE (codigo, anio_vigencia, estado_registro);


--
-- Name: tipo_proceso_seleccion uk_tipo_proceso_nombre_anio; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipo_proceso_seleccion
    ADD CONSTRAINT uk_tipo_proceso_nombre_anio UNIQUE (nombre, anio_vigencia, estado_registro);


--
-- Name: uit uk_uit_anio_estado_registro; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.uit
    ADD CONSTRAINT uk_uit_anio_estado_registro UNIQUE (anio_vigencia, estado_registro);


--
-- Name: business_rules ukr4coxb8que1xa7a2saw2w4cix; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.business_rules
    ADD CONSTRAINT ukr4coxb8que1xa7a2saw2w4cix UNIQUE (name);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: idx_objeto_contratacion_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_objeto_contratacion_estado ON public.objeto_contratacion USING btree (estado, estado_registro);


--
-- Name: idx_operadores_codigo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operadores_codigo ON public.operadores_monto USING btree (codigo);


--
-- Name: idx_operadores_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operadores_estado ON public.operadores_monto USING btree (estado, estado_registro);


--
-- Name: idx_rules_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_rules_active ON public.rules USING btree (is_active, estado_registro);


--
-- Name: idx_rules_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_rules_created_at ON public.rules USING btree (created_at);


--
-- Name: idx_rules_parametricas; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_rules_parametricas ON public.rules USING btree (uses_parametricas, anio_vigencia);


--
-- Name: idx_sub_desc_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sub_desc_estado ON public.sub_descripcion_contratacion USING btree (estado, estado_registro);


--
-- Name: idx_tipo_proceso_anio; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tipo_proceso_anio ON public.tipo_proceso_seleccion USING btree (anio_vigencia, estado);


--
-- Name: idx_tipo_proceso_codigo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tipo_proceso_codigo ON public.tipo_proceso_seleccion USING btree (codigo);


--
-- Name: idx_tipo_proceso_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tipo_proceso_estado ON public.tipo_proceso_seleccion USING btree (estado, estado_registro);


--
-- Name: idx_topes_anio_vigencia; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topes_anio_vigencia ON public.topes USING btree (anio_vigencia, estado, estado_registro);


--
-- Name: idx_topes_monto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topes_monto ON public.topes USING btree (monto);


--
-- Name: idx_topes_objeto_contratacion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topes_objeto_contratacion ON public.topes USING btree (id_objeto_contratacion, estado_registro);


--
-- Name: idx_topes_operador; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topes_operador ON public.topes USING btree (id_operador_monto, estado_registro);


--
-- Name: idx_topes_tipo_proceso; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topes_tipo_proceso ON public.topes USING btree (id_tipo_proceso_seleccion, estado_registro);


--
-- Name: idx_uit_anio_vigencia; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_uit_anio_vigencia ON public.uit USING btree (anio_vigencia, estado, estado_registro);


--
-- Name: idx_uit_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_uit_created_at ON public.uit USING btree (created_at);


--
-- Name: idx_uit_estado; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_uit_estado ON public.uit USING btree (estado, estado_registro);


--
-- Name: uk_topes_combinacion; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_topes_combinacion ON public.topes USING btree (id_tipo_proceso_seleccion, id_objeto_contratacion, id_operador_monto, anio_vigencia, estado_registro) WHERE (estado_registro = true);


--
-- Name: objeto_contratacion trigger_objeto_contratacion_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_objeto_contratacion_insert BEFORE INSERT ON public.objeto_contratacion FOR EACH ROW EXECUTE FUNCTION public.handle_audit_insert();


--
-- Name: objeto_contratacion trigger_objeto_contratacion_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_objeto_contratacion_update BEFORE UPDATE ON public.objeto_contratacion FOR EACH ROW EXECUTE FUNCTION public.handle_audit_update();


--
-- Name: operadores_monto trigger_operadores_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_operadores_insert BEFORE INSERT ON public.operadores_monto FOR EACH ROW EXECUTE FUNCTION public.handle_audit_insert();


--
-- Name: operadores_monto trigger_operadores_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_operadores_update BEFORE UPDATE ON public.operadores_monto FOR EACH ROW EXECUTE FUNCTION public.handle_audit_update();


--
-- Name: rules trigger_rules_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_rules_insert BEFORE INSERT ON public.rules FOR EACH ROW EXECUTE FUNCTION public.handle_audit_insert();


--
-- Name: rules trigger_rules_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_rules_update BEFORE UPDATE ON public.rules FOR EACH ROW EXECUTE FUNCTION public.handle_audit_update();


--
-- Name: sub_descripcion_contratacion trigger_sub_desc_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_sub_desc_insert BEFORE INSERT ON public.sub_descripcion_contratacion FOR EACH ROW EXECUTE FUNCTION public.handle_audit_insert();


--
-- Name: sub_descripcion_contratacion trigger_sub_desc_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_sub_desc_update BEFORE UPDATE ON public.sub_descripcion_contratacion FOR EACH ROW EXECUTE FUNCTION public.handle_audit_update();


--
-- Name: tipo_proceso_seleccion trigger_tipo_proceso_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_tipo_proceso_insert BEFORE INSERT ON public.tipo_proceso_seleccion FOR EACH ROW EXECUTE FUNCTION public.handle_audit_insert();


--
-- Name: tipo_proceso_seleccion trigger_tipo_proceso_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_tipo_proceso_update BEFORE UPDATE ON public.tipo_proceso_seleccion FOR EACH ROW EXECUTE FUNCTION public.handle_audit_update();


--
-- Name: topes trigger_topes_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_topes_insert BEFORE INSERT ON public.topes FOR EACH ROW EXECUTE FUNCTION public.handle_audit_insert();


--
-- Name: topes trigger_topes_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_topes_update BEFORE UPDATE ON public.topes FOR EACH ROW EXECUTE FUNCTION public.handle_audit_update();


--
-- Name: uit trigger_uit_delete; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_uit_delete BEFORE DELETE ON public.uit FOR EACH ROW EXECUTE FUNCTION public.handle_logical_delete();


--
-- Name: uit trigger_uit_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_uit_insert BEFORE INSERT ON public.uit FOR EACH ROW EXECUTE FUNCTION public.handle_audit_insert();


--
-- Name: uit trigger_uit_update; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trigger_uit_update BEFORE UPDATE ON public.uit FOR EACH ROW EXECUTE FUNCTION public.handle_audit_update();


--
-- Name: rules update_rules_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_rules_updated_at BEFORE UPDATE ON public.rules FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: uit update_uit_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_uit_updated_at BEFORE UPDATE ON public.uit FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: users update_users_updated_at; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: objeto_contratacion fk_objeto_sub_desc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.objeto_contratacion
    ADD CONSTRAINT fk_objeto_sub_desc FOREIGN KEY (id_sub_descripcion_contratacion) REFERENCES public.sub_descripcion_contratacion(id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: topes fk_topes_objeto_contratacion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topes
    ADD CONSTRAINT fk_topes_objeto_contratacion FOREIGN KEY (id_objeto_contratacion) REFERENCES public.objeto_contratacion(id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: topes fk_topes_operador_monto; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topes
    ADD CONSTRAINT fk_topes_operador_monto FOREIGN KEY (id_operador_monto) REFERENCES public.operadores_monto(id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: topes fk_topes_tipo_proceso; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topes
    ADD CONSTRAINT fk_topes_tipo_proceso FOREIGN KEY (id_tipo_proceso_seleccion) REFERENCES public.tipo_proceso_seleccion(id) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

