package com.example.droolsTest.service;

// Imports de Spring
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

// Imports de JPA (✅ CORRECTOS)
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

// Imports de tu proyecto
import com.example.droolsTest.annotation.ParametricaEntity;
import com.example.droolsTest.entity.DTO.CatalogoParametricasDTO;
import com.example.droolsTest.entity.DTO.ParametricaMetadata;

// Imports de Java
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoParametricasService {

    private final EntityManager entityManager;
    private final ApplicationContext applicationContext;

    private Map<String, Class<?>> entidadesParametricas = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Inicializando catálogo dinámico de paramétricas...");
        this.entidadesParametricas = escanearEntidadesParametricas();
        log.info("Catálogo inicializado con {} paramétricas: {}",
                entidadesParametricas.size(), entidadesParametricas.keySet());
    }

    /**
     * Escanea automáticamente todas las entidades marcadas con @ParametricaEntity
     */
    private Map<String, Class<?>> escanearEntidadesParametricas() {
        Map<String, Class<?>> entidades = new HashMap<>();

        // Obtener todas las entidades JPA del contexto
        Set<EntityType<?>> entityTypes = entityManager.getMetamodel().getEntities();

        for (EntityType<?> entityType : entityTypes) {
            Class<?> javaType = entityType.getJavaType();

            // Verificar si tiene la anotación @ParametricaEntity
            if (javaType.isAnnotationPresent(ParametricaEntity.class)) {
                ParametricaEntity annotation = javaType.getAnnotation(ParametricaEntity.class);

                // Solo incluir si está marcada para incluir en catálogo
                if (annotation.includeInCatalog()) {
                    String nombreClave = obtenerNombreClave(javaType, annotation);
                    entidades.put(nombreClave, javaType);

                    log.debug("Entidad paramétrica registrada: {} -> {}",
                            nombreClave, javaType.getSimpleName());
                }
            }
        }

        return entidades;
    }

    /**
     * Obtiene el nombre clave para la entidad
     */
    private String obtenerNombreClave(Class<?> clazz, ParametricaEntity annotation) {
        // Si la anotación tiene valor personalizado, usarlo
        if (!annotation.value().isEmpty()) {
            return annotation.value();
        }

        // Si no, convertir nombre de clase a snake_case
        return clazz.getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    /**
     * Genera catálogo completo dinámicamente
     */
    public CatalogoParametricasDTO obtenerCatalogoCompleto() {
        log.debug("Generando catálogo completo dinámicamente...");

        Map<String, ParametricaMetadata> catalogoDinamico = new HashMap<>();

        for (Map.Entry<String, Class<?>> entry : entidadesParametricas.entrySet()) {
            String nombreParametrica = entry.getKey();
            Class<?> claseEntidad = entry.getValue();

            try {
                ParametricaMetadata metadata = generarMetadataParametrica(nombreParametrica, claseEntidad);
                catalogoDinamico.put(nombreParametrica, metadata);

            } catch (Exception e) {
                log.error("Error generando metadata para {}: {}", nombreParametrica, e.getMessage());
                // Continuar con las demás paramétricas
            }
        }

        return CatalogoParametricasDTO.builder()
                .parametricas(catalogoDinamico)
                .totalParametricas(catalogoDinamico.size())
                .fechaGeneracion(LocalDateTime.now())
                .version("1.0")
                .build();
    }

    /**
     * Genera metadata detallada para una entidad específica
     */
    private ParametricaMetadata generarMetadataParametrica(String nombre, Class<?> clazz) {

        // Obtener información básica
        String tableName = obtenerNombreTabla(clazz);
        String descripcion = obtenerDescripcionEntidad(clazz);

        // Obtener campos usando reflexión
        List<String> campos = obtenerCamposEntidad(clazz);
        Map<String, String> descripcionesCampos = generarDescripcionesCampos(clazz);

        // Obtener estadísticas dinámicas
        Integer totalRegistros = contarRegistrosDinamicamente(tableName);
        List<Integer> aniosDisponibles = obtenerAniosDisponibles(tableName);

        // Metadata adicional
        Map<String, Object> metadataAdicional = new HashMap<>();
        metadataAdicional.put("hasAnioVigencia", tieneAnioVigencia(clazz));
        metadataAdicional.put("hasEstado", tieneEstado(clazz));
        metadataAdicional.put("auditEnabled", tieneAuditoria(clazz));

        return ParametricaMetadata.builder()
                .tableName(tableName)
                .className(clazz.getSimpleName())
                .packageName(clazz.getPackageName())
                .fields(campos)
                .description(descripcion)
                .totalRegistros(totalRegistros)
                .aniosDisponibles(aniosDisponibles)
                .fieldDescriptions(descripcionesCampos)
                .metadataAdicional(metadataAdicional)
                .build();
    }

    private String obtenerNombreTabla(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }

        // Convertir nombre de clase a snake_case por defecto
        return clazz.getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    private String obtenerDescripcionEntidad(Class<?> clazz) {
        ParametricaEntity annotation = clazz.getAnnotation(ParametricaEntity.class);

        if (annotation != null && !annotation.description().isEmpty()) {
            return annotation.description();
        }

        // Descripción por defecto
        return "Entidad paramétrica: " + clazz.getSimpleName();
    }

    private List<String> obtenerCamposEntidad(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !field.getName().equals("id")) // Excluir ID
                .filter(field -> !esCampoAuditoria(field.getName())) // Excluir auditoría
                .filter(field -> !esRelacionJPA(field)) // Excluir relaciones
                .filter(field -> !esCampoEstatico(field)) // Excluir campos estáticos
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    private boolean esCampoAuditoria(String nombreCampo) {
        return Arrays.asList("createdAt", "updatedAt", "createdBy", "updatedBy", "estadoRegistro")
                .contains(nombreCampo);
    }

    private boolean esRelacionJPA(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) ||
                field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToMany.class) ||
                field.isAnnotationPresent(OneToOne.class);
    }

    private boolean esCampoEstatico(Field field) {
        return java.lang.reflect.Modifier.isStatic(field.getModifiers());
    }

    private Map<String, String> generarDescripcionesCampos(Class<?> clazz) {
        Map<String, String> descripciones = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (!esCampoAuditoria(field.getName()) &&
                    !esRelacionJPA(field) &&
                    !esCampoEstatico(field) &&
                    !field.getName().equals("id")) {

                StringBuilder desc = new StringBuilder();
                desc.append("Tipo: ").append(field.getType().getSimpleName());

                // Obtener información de @Column si existe
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null) {
                    if (columnAnnotation.length() > 0 && columnAnnotation.length() != 255) {
                        desc.append(", Máx: ").append(columnAnnotation.length());
                    }
                    if (!columnAnnotation.nullable()) {
                        desc.append(", Requerido");
                    }
                    if (columnAnnotation.unique()) {
                        desc.append(", Único");
                    }
                }

                descripciones.put(field.getName(), desc.toString());
            }
        }

        return descripciones;
    }

    @SuppressWarnings("unchecked")
    private Integer contarRegistrosDinamicamente(String tableName) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM " + tableName + " WHERE estado_registro = true"
            );
            Number result = (Number) query.getSingleResult();
            return result.intValue();
        } catch (Exception e) {
            log.warn("No se pudo contar registros de tabla {}: {}", tableName, e.getMessage());
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Integer> obtenerAniosDisponibles(String tableName) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT DISTINCT anio_vigencia FROM " + tableName +
                            " WHERE estado_registro = true AND anio_vigencia IS NOT NULL ORDER BY anio_vigencia DESC"
            );
            List<Integer> resultList = query.getResultList();
            return resultList != null ? resultList : new ArrayList<>();
        } catch (Exception e) {
            // Si no tiene campo anio_vigencia, retornar lista vacía
            return new ArrayList<>();
        }
    }

    private boolean tieneAnioVigencia(Class<?> clazz) {
        try {
            clazz.getDeclaredField("anioVigencia");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private boolean tieneEstado(Class<?> clazz) {
        try {
            clazz.getDeclaredField("estado");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private boolean tieneAuditoria(Class<?> clazz) {
        try {
            clazz.getDeclaredField("createdAt");
            clazz.getDeclaredField("estadoRegistro");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * Obtener detalle específico de una paramétrica
     */
    public ParametricaMetadata obtenerDetalleParametrica(String nombre) {
        Class<?> clazz = entidadesParametricas.get(nombre);
        if (clazz == null) {
            throw new IllegalArgumentException("Paramétrica no encontrada: " + nombre);
        }

        return generarMetadataParametrica(nombre, clazz);
    }

    /**
     * Obtener lista de paramétricas disponibles
     */
    public Set<String> obtenerNombresParametricas() {
        return entidadesParametricas.keySet();
    }
}