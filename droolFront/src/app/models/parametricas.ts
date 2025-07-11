// models/parametricas.ts - Versión consolidada y corregida

// =============================================================
// INTERFACES BASE
// =============================================================

export interface EntidadParametrica {
  id?: number;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
  estadoRegistro?: boolean;
  estado?: string;
  [key: string]: any;
}

// =============================================================
// DTOs PARA EVALUACIÓN
// =============================================================

export interface ParametricasVigentesDTO {
  fechaEvaluacion: Date;
  anioVigencia: number;
  uitVigente: EntidadParametrica | null;
  tiposProceso: EntidadParametrica[];
  objetosContratacion: EntidadParametrica[];
  operadoresMonto: EntidadParametrica[];
  topesVigentes: EntidadParametrica[];
}

export interface EvaluacionParametricasDTO {
  fechaEvaluacion: Date;
  tipoProcesoId?: number;
  objetoContratacionId?: number;
  subDescripcionId?: number;
  operadorMontoId?: number;
  montoContrato: number;
  anioVigencia?: number;
  parametrosAdicionales?: Record<string, any>;
}

export interface ResultadoEvaluacionDTO {
  evaluacionExitosa: boolean;
  mensaje: string;
  parametricasUtilizadas: ParametricasVigentesDTO;
  reglasAplicadas: string[];
  resultadosDetalle: Record<string, any>;
  fechaEvaluacion: Date;
}

// =============================================================
// DTOs DEL CATÁLOGO DINÁMICO
// =============================================================

export interface CatalogoParametricasDTO {
  parametricas: Record<string, ParametricaMetadata>;
  totalParametricas: number;
  fechaGeneracion: Date;
  version?: string;
}

export interface ParametricaMetadata {
  tableName: string;
  className: string;
  packageName: string;
  description: string;
  fields: string[];
  fieldDescriptions: Record<string, string>;
  fieldTypes: Record<string, TipoCampo>;
  fieldValidations: Record<string, ValidacionesCampo>;
  fieldOptions: Record<string, OpcionSelect[]>;
  sections: Record<string, string[]>;
  requiredFields: string[];
  readOnlyFields: string[];
  totalRegistros: number;
  aniosDisponibles: number[];
  metadataAdicional: Record<string, any>;
}

// =============================================================
// ENUMS Y TIPOS PARA FORMULARIOS
// =============================================================

export enum TipoCampo {
  TEXT = 'text',
  NUMBER = 'number',
  EMAIL = 'email',
  DATE = 'date',
  TEXTAREA = 'textarea',
  SELECT = 'select',
  BOOLEAN = 'boolean',
  PASSWORD = 'password',
  YEAR = 'year',
  CURRENCY = 'currency'
}

export enum AnchoCampo {
  COMPLETO = 'completo',
  MEDIO = 'medio',
  TERCIO = 'tercio',
  CUARTO = 'cuarto',
  AUTO = 'auto'
}

export type EstadoParametrica = 'ACTIVO' | 'INACTIVO' | 'PENDIENTE' | 'ARCHIVADO';

// =============================================================
// INTERFACES PARA FORMULARIOS DINÁMICOS
// =============================================================

export interface OpcionSelect {
  valor: any;
  etiqueta: string;
  descripcion?: string;
  deshabilitado?: boolean;
}

export interface ValidacionesCampo {
  min?: number;
  max?: number;
  minLength?: number;
  maxLength?: number;
  pattern?: string;
  patternMessage?: string;
  step?: number;
  fechaMin?: Date;
  fechaMax?: Date;
  filas?: number; // Para textarea
}

export interface CampoFormulario {
  nombre: string;
  etiqueta: string;
  tipo: TipoCampo;
  valor?: any;
  requerido?: boolean;
  soloLectura?: boolean;
  ancho?: AnchoCampo;
  seccion?: string;
  orden?: number;
  placeholder?: string;
  ayuda?: string;
  icono?: string;
  prefijo?: string;
  sufijo?: string;
  opciones?: OpcionSelect[];
  validaciones?: ValidacionesCampo;
  dependeDe?: string;
  condicionMostrar?: (valores: any) => boolean;
}

export interface FormularioDinamico {
  entidad: string;
  campos: CampoFormulario[];
  datosIniciales?: EntidadParametrica;
  modoEdicion: boolean;
}

// =============================================================
// INTERFACES PARA FILTROS Y BÚSQUEDAS
// =============================================================

export interface FiltroParametricas {
  anio?: number;
  estado?: EstadoParametrica;
  fechaDesde?: Date;
  fechaHasta?: Date;
  busqueda?: string;
  filtrosEspecificos?: Record<string, any>;
}

// =============================================================
// INTERFACES PARA EL CONSTRUCTOR VISUAL
// =============================================================

export interface VariableParametrica {
  nombre: string;
  entidad: string;
  campo: string;
  descripcion: string;
  tipo: 'number' | 'string' | 'boolean' | 'date';
  ejemplo?: string;
}

export interface CondicionParametrica {
  variable: VariableParametrica;
  operador: string;
  valor: any;
  descripcion: string;
}

// =============================================================
// CONFIGURACIÓN DE ENTIDADES
// =============================================================

export interface ConfiguracionEntidad {
  nombre: string;
  nombreAmigable: string;
  descripcion: string;
  icono: string;
  color: string;
  camposRequeridos: string[];
  camposOpcionales: string[];
  relacionesCon: string[];
  operacionesPermitidas: ('crear' | 'editar' | 'eliminar' | 'ver')[];
}

export const CONFIGURACION_ENTIDADES: Record<string, ConfiguracionEntidad> = {
  uit: {
    nombre: 'uit',
    nombreAmigable: 'UIT',
    descripcion: 'Unidad Impositiva Tributaria',
    icono: 'monetization_on',
    color: 'primary',
    camposRequeridos: ['monto', 'anioVigencia'],
    camposOpcionales: ['observaciones'],
    relacionesCon: ['topes'],
    operacionesPermitidas: ['crear', 'editar', 'eliminar', 'ver']
  },
  'tipo_proceso_seleccion': {
    nombre: 'tipo_proceso_seleccion',
    nombreAmigable: 'Tipos de Proceso',
    descripcion: 'Tipos de proceso de selección',
    icono: 'account_tree',
    color: 'accent',
    camposRequeridos: ['codigo', 'nombre', 'anio_vigencia'],
    camposOpcionales: ['descripcion', 'montoMinimo', 'montoMaximo'],
    relacionesCon: ['topes'],
    operacionesPermitidas: ['crear', 'editar', 'eliminar', 'ver']
  },
  'objeto_contratacion': {
    nombre: 'objeto_contratacion',
    nombreAmigable: 'Objetos de Contratación',
    descripcion: 'Objetos de contratación (Bienes, Servicios, Obras)',
    icono: 'category',
    color: 'warn',
    camposRequeridos: ['codigo', 'nombre'],
    camposOpcionales: ['descripcion'],
    relacionesCon: ['sub-descripcion-contratacion', 'topes'],
    operacionesPermitidas: ['crear', 'editar', 'eliminar', 'ver']
  },
  'operadores_monto': {
    nombre: 'operadores_monto',
    nombreAmigable: 'Operadores de Monto',
    descripcion: 'Operadores matemáticos para comparaciones',
    icono: 'functions',
    color: 'primary',
    camposRequeridos: ['operador', 'descripcion', 'simbolo'],
    camposOpcionales: [],
    relacionesCon: ['topes'],
    operacionesPermitidas: ['ver', 'crear']
  },
  topes: {
    nombre: 'topes',
    nombreAmigable: 'Topes',
    descripcion: 'Reglas de topes por combinación',
    icono: 'policy',
    color: 'accent',
    camposRequeridos: ['tipoProcesoId', 'objetoContratacionId', 'operadorMontoId', 'valorTope', 'anioVigencia'],
    camposOpcionales: ['subDescripcionId'],
    relacionesCon: ['tipo_proceso_seleccion', 'objeto_contratacion', 'operadores_monto'],
    operacionesPermitidas: ['crear', 'editar', 'eliminar', 'ver']
  }
};

// =============================================================
// CLASE HELPER CONSOLIDADA
// =============================================================

export class ParametricasHelper {

  /**
   * Generar campos de formulario basado en metadata
   */
  static generarCamposFormulario(entidad: string, metadata: ParametricaMetadata): CampoFormulario[] {
    const campos: CampoFormulario[] = [];

    metadata.fields.forEach((fieldName, index) => {
      if (fieldName === 'id') return; // No incluir ID en formularios

      const campo: CampoFormulario = {
        nombre: fieldName,
        //etiqueta: metadata.fieldDescriptions[fieldName] || this.formatearNombre(fieldName),
        etiqueta: this.formatearNombre(fieldName),
        tipo: metadata.fieldTypes?.[fieldName] || TipoCampo.TEXT,
        requerido: metadata.requiredFields?.includes(fieldName) || false,
        soloLectura: metadata.readOnlyFields?.includes(fieldName) || false,
        ancho: this.determinarAnchoCampo(fieldName, metadata.fieldTypes?.[fieldName]),
        seccion: this.determinarSeccion(fieldName, entidad),
        orden: index,
        validaciones: metadata.fieldValidations?.[fieldName],
        opciones: metadata.fieldOptions?.[fieldName],
        placeholder: this.generarPlaceholder(fieldName, metadata.fieldTypes?.[fieldName]),
        ayuda: this.generarAyuda(fieldName, entidad),
        icono: this.determinarIcono(fieldName, metadata.fieldTypes?.[fieldName])
      };

      campos.push(campo);
    });

    // Ordenar por sección y orden
    return campos.sort((a, b) => {
      if (a.seccion !== b.seccion) {
        return (a.seccion || '').localeCompare(b.seccion || '');
      }
      return (a.orden || 0) - (b.orden || 0);
    });
  }

  /**
   * Obtener configuración de una entidad específica
   */
  static obtenerConfiguracion(nombreEntidad: string): ConfiguracionEntidad | null {
    return CONFIGURACION_ENTIDADES[nombreEntidad] || null;
  }

  /**
   * Verificar si una entidad es conocida
   */
  static esEntidadConocida(nombreEntidad: string): boolean {
    return nombreEntidad in CONFIGURACION_ENTIDADES;
  }

  /**
   * Obtener todas las entidades configuradas
   */
  static obtenerTodasLasEntidades(): ConfiguracionEntidad[] {
    return Object.values(CONFIGURACION_ENTIDADES);
  }

  /**
   * Formatear valor para mostrar en interfaz
   */
  static formatearValor(valor: any, tipoCampo: TipoCampo): string {
    if (valor === null || valor === undefined) return '';

    switch (tipoCampo) {
      case TipoCampo.CURRENCY:
      case TipoCampo.NUMBER:
        return typeof valor === 'number' ? valor.toLocaleString() : valor.toString();
      case TipoCampo.DATE:
        return valor instanceof Date ? valor.toLocaleDateString() : valor.toString();
      default:
        return valor.toString();
    }
  }

  /**
   * Obtener opciones para campos de tipo select
   */
  static obtenerOpcionesSelect(campo: string): OpcionSelect[] {
    const opciones: Record<string, OpcionSelect[]> = {
      estado: [
        { valor: 'ACTIVO', etiqueta: 'Activo' },
        { valor: 'INACTIVO', etiqueta: 'Inactivo' },
        { valor: 'PENDIENTE', etiqueta: 'Pendiente' },
        { valor: 'ARCHIVADO', etiqueta: 'Archivado' }
      ]
    };

    return opciones[campo] || [];
  }

  // =============================================================
  // MÉTODOS PRIVADOS DE HELPER
  // =============================================================

  private static formatearNombre(fieldName: string): string {
    return fieldName
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .replace(/_/g, ' ')
      .trim();
  }

  private static determinarAnchoCampo(fieldName: string, tipo?: TipoCampo): AnchoCampo {
    const camposCompletos = ['descripcion', 'observaciones', 'comentarios', 'notas'];
    if (camposCompletos.some(campo => fieldName.toLowerCase().includes(campo))) {
      return AnchoCampo.COMPLETO;
    }

    if (tipo === TipoCampo.TEXTAREA) {
      return AnchoCampo.COMPLETO;
    }

    const camposPequenos = ['codigo', 'sigla', 'anio', 'orden'];
    if (camposPequenos.some(campo => fieldName.toLowerCase().includes(campo))) {
      return AnchoCampo.CUARTO;
    }

    return AnchoCampo.MEDIO;
  }

  private static determinarSeccion(fieldName: string, entidad: string): string {
    const camposConfiguracion = ['activo', 'vigente', 'orden', 'estado'];
    if (camposConfiguracion.some(campo => fieldName.toLowerCase().includes(campo))) {
      return 'Configuración';
    }

    const camposFechas = ['fecha', 'vigencia', 'inicio', 'fin'];
    if (camposFechas.some(campo => fieldName.toLowerCase().includes(campo))) {
      return 'Fechas y Vigencia';
    }

    const camposMontos = ['monto', 'valor', 'precio', 'costo'];
    if (camposMontos.some(campo => fieldName.toLowerCase().includes(campo))) {
      return 'Valores Monetarios';
    }

    switch (entidad.toLowerCase()) {
      case 'uit':
        return 'Información de la UIT';
      case 'tipo_proceso_seleccion':
        return 'Información del Proceso';
      case 'objeto_contratacion':
        return 'Información del Objeto';
      default:
        return 'Información General';
    }
  }

  private static generarPlaceholder(fieldName: string, tipo?: TipoCampo): string {
    switch (tipo) {
      case TipoCampo.EMAIL:
        return 'ejemplo@correo.com';
      case TipoCampo.NUMBER:
      case TipoCampo.CURRENCY:
        if (fieldName.toLowerCase().includes('monto')) {
          return '0.00';
        }
        if (fieldName.toLowerCase().includes('anio')) {
          return new Date().getFullYear().toString();
        }
        return 'Ingrese un número';
      case TipoCampo.DATE:
      case TipoCampo.YEAR:
        return 'dd/mm/aaaa';
      case TipoCampo.SELECT:
        return 'Seleccione una opción';
      case TipoCampo.TEXTAREA:
        return 'Ingrese una descripción detallada...';
      default:
        return `Ingrese ${this.formatearNombre(fieldName).toLowerCase()}`;
    }
  }

  private static generarAyuda(fieldName: string, entidad: string): string {
    const ayudas: { [key: string]: string } = {
      'codigo': 'Código único identificador',
      'nombre': 'Nombre descriptivo del elemento',
      'descripcion': 'Descripción detallada (opcional)',
      'anio_vigencia': 'Año para el cual es válida esta configuración',
      'monto': 'Valor en soles peruanos (S/)',
      'fechaInicio': 'Fecha de inicio de vigencia',
      'fechaFin': 'Fecha de fin de vigencia (opcional)',
      'activo': 'Marcar si está actualmente activo',
      'orden': 'Orden de visualización en listas'
    };

    if (entidad === 'uit' && fieldName === 'monto') {
      return 'Valor de la UIT en soles para el año de vigencia';
    }

    return ayudas[fieldName] || '';
  }

  private static determinarIcono(fieldName: string, tipo?: TipoCampo): string {
    switch (tipo) {
      case TipoCampo.EMAIL:
        return 'email';
      case TipoCampo.DATE:
      case TipoCampo.YEAR:
        return 'calendar_today';
      case TipoCampo.NUMBER:
      case TipoCampo.CURRENCY:
        if (fieldName.toLowerCase().includes('monto')) {
          return 'monetization_on';
        }
        return 'numbers';
      case TipoCampo.BOOLEAN:
        return 'check_box';
      default:
        break;
    }

    const iconos: { [key: string]: string } = {
      'codigo': 'tag',
      'nombre': 'title',
      'descripcion': 'description',
      'anio': 'calendar_today',
      'fecha': 'event',
      'monto': 'monetization_on',
      'valor': 'paid',
      'orden': 'sort',
      'estado': 'info',
      'activo': 'toggle_on'
    };

    const fieldLower = fieldName.toLowerCase();
    for (const [key, icon] of Object.entries(iconos)) {
      if (fieldLower.includes(key)) {
        return icon;
      }
    }

    return 'edit';
  }

  /**
   * Validar dependencias entre campos
   */
  static validarDependencias(campos: CampoFormulario[], valores: any): boolean {
    return campos.every(campo => {
      if (!campo.dependeDe || !campo.condicionMostrar) {
        return true;
      }
      return campo.condicionMostrar(valores);
    });
  }

  /**
   * Obtener campos visibles según dependencias
   */
  static obtenerCamposVisibles(campos: CampoFormulario[], valores: any): CampoFormulario[] {
    return campos.filter(campo => {
      if (!campo.dependeDe || !campo.condicionMostrar) {
        return true;
      }
      return campo.condicionMostrar(valores);
    });
  }

  /**
   * Generar validaciones dinámicas por entidad
   */
  static generarValidacionesDinamicas(entidad: string): { [key: string]: ValidacionesCampo } {
    const validaciones: { [key: string]: ValidacionesCampo } = {};

    switch (entidad) {
      case 'uit':
        validaciones['monto'] = {
          min: 1,
          max: 10000,
          step: 0.01
        };
        validaciones['anio_vigencia'] = {
          min: 2020,
          max: 2030
        };
        break;

      case 'tipo_proceso_seleccion':
        validaciones['codigo'] = {
          minLength: 2,
          maxLength: 10,
          pattern: '^[A-Z0-9]+$',
          patternMessage: 'Solo letras mayúsculas y números'
        };
        validaciones['anio_vigencia'] = {
          min: 2020,
          max: 2030
        };
        break;

      case 'objeto_contratacion':
        validaciones['nombre'] = {
          minLength: 5,
          maxLength: 100
        };
        break;
    }

    return validaciones;
  }
}
