import { ChangeDetectionStrategy, Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Parametricas } from '../../../services/parametricas';
import { Notification } from '../../../services/notification';

import {
  EntidadParametrica,
  EvaluacionParametricasDTO,
  ResultadoEvaluacionDTO,
  ParametricasVigentesDTO
} from '../../../models/parametricas';

@Component({
  selector: 'app-evaluador-parametricas',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTabsModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './evaluador-parametricas.html',
  styleUrl: './evaluador-parametricas.css'
})
export class EvaluadorParametricas implements OnInit {

  // Datos del requerimiento a evaluar
  evaluacion: EvaluacionParametricasDTO = {
    fechaEvaluacion: new Date(),
    montoContrato: 25000,
    anioVigencia: new Date().getFullYear()
  };

  // Resultados y estado
  ultimoResultado: ResultadoEvaluacionDTO | string | null = null;
  parametricasVigentes: ParametricasVigentesDTO | string | null = null;
  loading = false;
  loadingParametricas = false;

  // Datos para los selects
  tiposProceso: EntidadParametrica[] = [];
  objetosContratacion: EntidadParametrica[] = [];
  operadoresMonto: EntidadParametrica[] = [];

  // Configuración
  aniosDisponibles: number[] = [];
  fechaMinima = new Date(2020, 0, 1);
  fechaMaxima = new Date(2030, 11, 31);

  constructor(
    private parametricasService: Parametricas,
    private notificationService: Notification,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.inicializarAnios();
    this.cargarParametricasIniciales();
  }

  private inicializarAnios() {
    const anioActual = new Date().getFullYear();
    for (let i = anioActual - 5; i <= anioActual + 2; i++) {
      this.aniosDisponibles.push(i);
    }
    this.evaluacion.anioVigencia = anioActual;
  }

  private cargarParametricasIniciales() {
    this.loadingParametricas = true;

    // Cargar datos para los selects
    Promise.all([
      this.parametricasService.getEntidades('tipo-proceso').toPromise(),
      this.parametricasService.getEntidades('objeto_contratacion').toPromise(),
      this.parametricasService.getEntidades('operadores_monto').toPromise()
    ]).then(([tipos, objetos, operadores]) => {
      this.tiposProceso = Array.isArray(tipos) ? tipos : [];
      this.objetosContratacion = Array.isArray(objetos) ? objetos : [];
      this.operadoresMonto = Array.isArray(operadores) ? operadores : [];
      this.loadingParametricas = false;
      this.cdr.detectChanges();

      // Cargar paramétricas vigentes para la fecha actual
      this.cargarParametricasVigentes();
    }).catch(error => {
      console.error('Error loading initial data:', error);
      this.notificationService.error('Error al cargar datos iniciales');
      this.loadingParametricas = false;
      this.cdr.detectChanges();
    });
  }

  cargarParametricasVigentes() {
    if (!this.evaluacion.fechaEvaluacion) return;

    this.loadingParametricas = true;
    const fechaStr = this.evaluacion.fechaEvaluacion.toISOString().split('T')[0];

    this.parametricasService.getParametricasVigentes(fechaStr).subscribe({
      next: (parametricas) => {
      if (typeof parametricas === 'string') {
        // Es mensaje de error del backend
        this.notificationService.error(parametricas);
        this.parametricasVigentes = null; // Asignar null para indicar que no hay datos
      } else {
        // Son datos reales
        this.parametricasVigentes = parametricas;
        console.log('Paramétricas vigentes cargadas:', parametricas);
      }
      this.loadingParametricas = false;
      this.cdr.detectChanges();
    },
      error: (error) => {
        console.error('Error loading parametricas vigentes:', error);
        this.notificationService.warning('Error al cargar paramétricas vigentes');
        this.loadingParametricas = false;
        this.cdr.detectChanges();
      }
    });
  }

  onFechaChange() {
    if (this.evaluacion.fechaEvaluacion) {
      this.evaluacion.anioVigencia = this.evaluacion.fechaEvaluacion.getFullYear();
      this.cargarParametricasVigentes();
    }
  }

evaluarRequerimiento() {
  if (!this.validarFormulario()) {
    return;
  }

  this.loading = true;
  this.cdr.detectChanges();

  this.parametricasService.evaluarParametricas(this.evaluacion).subscribe({
    next: (resultado) => {
      this.loading = false;

      if (typeof resultado === 'string') {
        // Es mensaje de error del backend
        this.notificationService.error(resultado);
        this.ultimoResultado = null; // Asignar null, no el string
      } else {
        // Son datos reales
        this.ultimoResultado = resultado;
        if (resultado.evaluacionExitosa) {
          this.notificationService.success('Evaluación completada exitosamente');
        } else {
          this.notificationService.warning(resultado.mensaje);
        }
      }

      this.cdr.detectChanges();
    },
    error: (error) => {
      console.error('Error en evaluación:', error);
      this.loading = false;
      this.ultimoResultado = null;
      this.cdr.detectChanges();
      this.notificationService.error('Error al evaluar el requerimiento');
    }
  });
}

  private validarFormulario(): boolean {
    if (!this.evaluacion.fechaEvaluacion) {
      this.notificationService.warning('La fecha de evaluación es requerida');
      return false;
    }

    if (!this.evaluacion.montoContrato || this.evaluacion.montoContrato <= 0) {
      this.notificationService.warning('El monto del contrato debe ser mayor a cero');
      return false;
    }

    return true;
  }

  generarEjemploAleatorio() {
    const ejemplos = [
      { monto: 15000, descripcion: 'Compra de equipos de oficina' },
      { monto: 75000, descripcion: 'Contratación de servicios de limpieza' },
      { monto: 250000, descripcion: 'Adquisición de vehículos' },
      { monto: 800000, descripcion: 'Construcción de infraestructura' },
      { monto: 35000, descripcion: 'Servicios de consultoría' },
      { monto: 120000, descripcion: 'Equipamiento tecnológico' }
    ];

    const ejemplo = ejemplos[Math.floor(Math.random() * ejemplos.length)];
    this.evaluacion.montoContrato = ejemplo.monto;

    // Fecha aleatoria en los últimos 2 años
    const fechaBase = new Date();
    const diasAleatorios = Math.floor(Math.random() * 730); // 2 años
    fechaBase.setDate(fechaBase.getDate() - diasAleatorios);
    this.evaluacion.fechaEvaluacion = fechaBase;
    this.onFechaChange();

    this.notificationService.info(`Ejemplo generado: ${ejemplo.descripcion}`);
  }

  limpiarFormulario() {
    this.evaluacion = {
      fechaEvaluacion: new Date(),
      montoContrato: 0,
      anioVigencia: new Date().getFullYear()
    };
    this.ultimoResultado = null;
    this.cargarParametricasVigentes();
    this.notificationService.info('Formulario limpiado');
  }

  // Helpers para el template - USANDO ACCESO DINÁMICO
  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 0
    }).format(monto);
  }

  obtenerNombreEntidad(entidades: EntidadParametrica[], id: number | undefined): string {
    if (!id || !entidades) return 'No especificado';
    const entidad = entidades.find(e => e.id === id);
    return entidad?.['nombre'] || entidad?.['codigo'] || `ID: ${id}`;
  }

  obtenerIconoTipoProceso(nombre: string): string {
    if (nombre.includes('Adjudicación')) return 'assignment_turned_in';
    if (nombre.includes('Comparación')) return 'compare_arrows';
    if (nombre.includes('Licitación')) return 'gavel';
    return 'rule';
  }

  obtenerColorTipoProceso(nombre: string): string {
    if (nombre.includes('Adjudicación')) return 'primary';
    if (nombre.includes('Comparación')) return 'accent';
    if (nombre.includes('Licitación')) return 'warn';
    return '';
  }

obtenerDescripcionUIT(): string {
  // Verificar que parametricasVigentes sea un objeto, no un string
  if (!this.parametricasVigentes || typeof this.parametricasVigentes === 'string') {
    return 'UIT no disponible';
  }

  if (!this.parametricasVigentes.uitVigente) {
    return 'UIT no disponible';
  }

  const uit = this.parametricasVigentes.uitVigente;
  return `UIT ${uit['anioVigencia']}: ${this.formatearMonto(uit['monto'])}`;
}

calcularMultiploUIT(monto: number): string {
  // Verificar que parametricasVigentes sea un objeto, no un string
  if (!this.parametricasVigentes || typeof this.parametricasVigentes === 'string') {
    return '-- UIT';
  }

  if (!this.parametricasVigentes.uitVigente?.['monto']) {
    return '-- UIT';
  }

  const multiplo = monto / this.parametricasVigentes.uitVigente['monto'];
  return `${multiplo.toFixed(2)} UIT`;
}

  // Getters para el template
  get tieneDatosMinimos(): boolean {
    return !!(this.evaluacion.fechaEvaluacion && this.evaluacion.montoContrato > 0);
  }

  get tieneParametricasVigentes(): boolean {
    return !!(this.parametricasVigentes && typeof this.parametricasVigentes !== 'string');
  }

  get tieneResultado(): boolean {
    return !!this.ultimoResultado;
  }

  // En evaluador-parametricas.ts, agregar:
get cantidadTiposProceso(): number {
  return (this.parametricasVigentes && typeof this.parametricasVigentes !== 'string')
    ? (this.parametricasVigentes.tiposProceso?.length || 0)
    : 0;
}

get cantidadObjetosContratacion(): number {
  return (this.parametricasVigentes && typeof this.parametricasVigentes !== 'string')
    ? (this.parametricasVigentes.objetosContratacion?.length || 0)
    : 0;
}

get cantidadTopesVigentes(): number {
  return (this.parametricasVigentes && typeof this.parametricasVigentes !== 'string')
    ? (this.parametricasVigentes.topesVigentes?.length || 0)
    : 0;
}

}
