# Sistema de Gestión de Paramétricas - Frontend

Sistema frontend desarrollado en Angular para la gestión integral de parámetros de contratación y evaluación de reglas de negocio.

## Descripción

Interfaz web moderna que permite administrar y evaluar parámetros de contratación, incluyendo UITs, tipos de proceso, objetos de contratación, operadores de monto y topes configurables.

## Tecnologías

- **Angular 20.0.3** - Framework principal
- **Angular Material** - Componentes UI
- **TypeScript** - Lenguaje de desarrollo
- **RxJS** - Programación reactiva

## Funcionalidades Principales

### Gestión de Paramétricas
- **UIT Manager** - Administración de Unidad Impositiva Tributaria
- **Tipos de Proceso** - Modalidades de selección
- **Sub-Descripciones** - Subcategorías de contratación
- **Objetos de Contratación** - Bienes, Servicios, Obras
- **Operadores de Monto** - Operadores matemáticos
- **Topes Manager** - Límites por combinación de parámetros

### Características Técnicas
- Formularios dinámicos con validaciones
- Relaciones entre entidades
- Dropdowns dependientes
- Sistema de notificaciones
- Interfaz responsive
- Menú de navegación organizado

### Evaluador de Paramétricas
- Simulador de evaluación de montos
- Análisis de reglas aplicables
- Visualización de resultados
- Cálculos automáticos con UIT

## Instalación y Ejecución

### Servidor de Desarrollo
```bash
ng serve
```
Navegar a `http://localhost:4200/`

### Construcción
```bash
ng build
```
Los artefactos se almacenarán en `dist/`

### Pruebas
```bash
ng test        # Pruebas unitarias
ng e2e         # Pruebas end-to-end
```

## Estructura del Proyecto

```
src/
├── app/
│   ├── components/
│   │   ├── managers/          # CRUDs de paramétricas
│   │   ├── formulario-dinamico/   # Formularios reutilizables
│   │   └── evaluador/         # Simulador de evaluación
│   ├── services/              # Servicios de datos
│   ├── models/                # Modelos TypeScript
│   └── shared/                # Componentes compartidos
```

## Configuración

### Variables de Entorno
Configurar la URL del backend en los servicios:
```typescript
private baseUrl = 'http://localhost:8080/api';
```

### Dependencias Principales
- Angular Material
- Angular Router
- Angular Forms (Reactive)
- Angular HTTP Client

## Recursos Adicionales

- [Angular CLI Overview](https://angular.dev/tools/cli)
- [Angular Material Documentation](https://material.angular.io/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)