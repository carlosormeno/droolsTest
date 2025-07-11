# Sistema de Gestión de Paramétricas - Backend

API REST desarrollada en Spring Boot para la gestión de parámetros de contratación y motor de reglas de negocio.

## Descripción

Backend robusto que proporciona servicios para la administración de parámetros de contratación, evaluación de reglas y cálculos automáticos de montos según normativas vigentes.

## Tecnologías

- **Spring Boot 3.5.3** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring Web** - API REST
- **Spring Security** - Seguridad (configurado)
- **Spring Boot Actuator** - Monitoreo
- **Hibernate** - ORM
- **H2/MySQL** - Base de datos
- **Maven** - Gestión de dependencias

## Arquitectura

### Entidades Principales
- **UIT** - Unidad Impositiva Tributaria por año
- **TipoProcesoSeleccion** - Modalidades de contratación
- **SubDescripcionContratacion** - Subcategorías
- **ObjetoContratacion** - Bienes, Servicios, Obras
- **OperadoresMonto** - Operadores matemáticos
- **Topes** - Límites configurables por combinación

### Servicios Principales
- **ParametricasService** - Lógica de evaluación
- **CatalogoParametricasService** - Metadatos dinámicos
- **Servicios específicos** - CRUD por entidad

## Funcionalidades

### API REST Completa
- **CRUD completo** para todas las entidades
- **Endpoints de evaluación** de parámetros
- **Catálogo dinámico** de metadatos
- **Validaciones** de negocio
- **Manejo de errores** estructurado

### Motor de Evaluación
- Evaluación de montos contra topes
- Cálculos automáticos con UIT
- Reglas de negocio configurable
- Resultados estructurados

### Características Técnicas
- Transacciones declarativas
- Auditoría automática
- Paginación y filtros
- Validaciones con Bean Validation
- Logging estructurado

## Configuración

### Base de Datos
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### Puertos y Profiles
```properties
server.port=8080
spring.profiles.active=dev
```

## Endpoints Principales

### Paramétricas
```
GET    /api/parametricas/vigentes?fecha={date}
POST   /api/parametricas/evaluar
GET    /api/parametricas/catalogo-completo
```

### Entidades CRUD
```
GET    /api/{entidad}
POST   /api/{entidad}
PUT    /api/{entidad}/{id}
DELETE /api/{entidad}/{id}
```

### Monitoreo
```
GET    /api/parametricas/health
GET    /actuator/health
```

## Ejecución

### Desarrollo
```bash
mvn spring-boot:run
```

### Construcción
```bash
mvn clean package
java -jar target/drools-backend.jar
```

### Pruebas
```bash
mvn test                    # Pruebas unitarias
mvn integration-test        # Pruebas de integración
```

## Estructura del Proyecto

```
src/main/java/
├── controller/             # Controladores REST
├── service/               # Lógica de negocio
├── repository/            # Acceso a datos
├── entity/                # Entidades JPA
│   └── DTO/              # Objetos de transferencia
├── annotation/            # Anotaciones custom
└── exception/             # Manejo de excepciones
```

## Documentación de Referencia

### Spring Boot
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/3.5.3/maven-plugin)
- [Spring Web](https://docs.spring.io/spring-boot/3.5.3/reference/web/servlet.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.3/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Spring Security](https://docs.spring.io/spring-boot/3.5.3/reference/web/spring-security.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.3/reference/actuator/index.html)

### Guías
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Validation](https://spring.io/guides/gs/validating-form-input/)
- [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
