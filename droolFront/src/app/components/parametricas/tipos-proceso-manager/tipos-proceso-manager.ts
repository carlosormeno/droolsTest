import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Parametricas } from '../../../services/parametricas';
import { Notification } from '../../../services/notification';
import {
  EntidadParametrica,
  ParametricaMetadata,
  CampoFormulario,
  ParametricasHelper
} from '../../../models/parametricas';

import { FormularioDinamico } from '../formulario-dinamico/formulario-dinamico';

@Component({
  selector: 'app-tipos-proceso-manager',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTooltipModule,
    FormularioDinamico
  ],
  templateUrl: './tipos-proceso-manager.html',
  styleUrl: '../styles/parametricas-manager-shared.css'
})
export class TiposProcesoManager implements OnInit {
  // Datos de la tabla
  columnas: string[] = [];
  columnasConAcciones: string[] = [];
  dataSource: EntidadParametrica[] = [];
  metadata: ParametricaMetadata | null = null;
  loading = false;

  // Para formulario
  camposFormulario: CampoFormulario[] = [];
  modoEdicion = false;
  entidadEditando: EntidadParametrica | null = null;
  mostrandoFormulario = false;

  constructor(
    private parametricasService: Parametricas,
    private notificationService: Notification,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarMetadataYDatos();
  }

  cargarMetadataYDatos() {
    this.loading = true;
    this.cdr.detectChanges();

    this.parametricasService.getCatalogoCompleto().subscribe({
      next: (catalogo) => {
        if (typeof catalogo === 'string') {
          this.notificationService.error('Error al cargar catálogo: ' + catalogo);
          this.loading = false;
          this.cdr.detectChanges();
          return;
        }

        console.log('Catálogo completo:', catalogo);
        console.log('Claves disponibles:', Object.keys(catalogo.parametricas || {}));
        console.log('Metadata tipo_proceso:', catalogo.parametricas?.['tipo_proceso']);

        this.metadata = catalogo.parametricas?.['tipo_proceso'] || null;

        if (this.metadata) {
          // Configurar columnas (excluir 'id' del display pero incluir acciones)
          this.columnas = this.metadata.fields.filter(f => f !== 'id');
          this.columnasConAcciones = [...this.columnas, 'acciones'];

          this.cargarTiposProceso();
        } else {
          this.notificationService.error('No se encontró metadata para Tipos de Proceso');
          this.loading = false;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error loading catalog:', error);
        this.notificationService.error('Error al cargar catálogo');
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private cargarTiposProceso() {
    this.parametricasService.getEntidades('tipo-proceso').subscribe({
      next: (data) => {
        if (typeof data === 'string') {
          this.notificationService.error('Error al cargar Tipos de Proceso: ' + data);
          this.dataSource = [];
        } else {
          this.dataSource = Array.isArray(data) ? data : [];
          console.log('Tipos de Proceso cargados:', this.dataSource.length);
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading Tipos de Proceso:', error);
        this.notificationService.error('Error al cargar Tipos de Proceso');
        this.dataSource = [];
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onCrear() {
    if (!this.metadata) {
      this.notificationService.warning('Metadata no disponible');
      return;
    }

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('tipo-proceso', this.metadata);
    this.aplicarValidacionesEspecificas();
    this.modoEdicion = false;
    this.entidadEditando = null;
    this.mostrandoFormulario = true;
    this.cdr.detectChanges();
  }

  onEditar(entidad: EntidadParametrica) {
    if (!this.metadata) {
      this.notificationService.warning('Metadata no disponible');
      return;
    }

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('tipo-proceso', this.metadata);
    this.aplicarValidacionesEspecificas();

    // Prellenar los valores usando acceso dinámico
    this.camposFormulario.forEach(campo => {
      campo.valor = entidad[campo.nombre];
    });

    this.modoEdicion = true;
    this.entidadEditando = entidad;
    this.mostrandoFormulario = true;
    this.cdr.detectChanges();
  }

  private aplicarValidacionesEspecificas() {
    // Aplicar validaciones específicas para tipo-proceso
    this.camposFormulario.forEach(campo => {
      if (campo.nombre === 'codigo') {
        campo.validaciones = {
          minLength: 2,
          maxLength: 10,
          pattern: '^[A-Z0-9]+$',
          patternMessage: 'Solo letras mayúsculas y números'
        };
        campo.placeholder = 'Ej: LP, CP, AS';
        campo.ayuda = 'Código único identificador (2-10 caracteres, solo letras mayúsculas y números)';
      }
      
      if (campo.nombre === 'anio_vigencia') {
        const currentYear = new Date().getFullYear();
        campo.validaciones = {
          min: 2020,
          max: 2030
        };
        campo.valor = campo.valor || currentYear;
      }
    });
  }

  onGuardar(campos: CampoFormulario[]) {
    // Convertir campos a objeto plano
    const payload: EntidadParametrica = {};
    campos.forEach(campo => {
      payload[campo.nombre] = campo.valor;
    });

    this.loading = true;
    this.cdr.detectChanges();

    if (this.modoEdicion && this.entidadEditando?.id) {
      // Actualizar Tipo de Proceso existente
      this.parametricasService.updateEntidad('tipo-proceso', this.entidadEditando.id, payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al actualizar: ' + response);
          } else {
            this.notificationService.success('Tipo de Proceso actualizado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error updating Tipo de Proceso:', error);
          this.notificationService.error('Error al actualizar Tipo de Proceso');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      // Crear nuevo Tipo de Proceso
      this.parametricasService.createEntidad('tipo-proceso', payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al crear: ' + response);
          } else {
            this.notificationService.success('Tipo de Proceso creado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating Tipo de Proceso:', error);
          this.notificationService.error('Error al crear Tipo de Proceso');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  onEliminar(entidad: EntidadParametrica) {
    if (!entidad.id) {
      this.notificationService.error('No se puede eliminar: ID no válido');
      return;
    }

    const descripcion = entidad['nombre'] || entidad['codigo'] || `ID: ${entidad.id}`;
    const confirmMessage = `¿Está seguro de eliminar el Tipo de Proceso "${descripcion}"?`;

    if (confirm(confirmMessage)) {
      this.loading = true;
      this.cdr.detectChanges();

      this.parametricasService.deleteEntidad('tipo-proceso', entidad.id).subscribe({
        next: (response) => {
          if (response) {
            this.notificationService.success('Tipo de Proceso eliminado exitosamente');
            this.cargarMetadataYDatos();
          } else {
            this.notificationService.error('Error al eliminar Tipo de Proceso');
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error deleting Tipo de Proceso:', error);
          this.notificationService.error('Error al eliminar Tipo de Proceso');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  cerrarFormulario() {
    this.mostrandoFormulario = false;
    this.entidadEditando = null;
    this.camposFormulario = [];
    this.cdr.detectChanges();
  }

  // Helpers para el template
  formatearValor(valor: any, columna: string): string {
    if (valor === null || valor === undefined) return '--';

    // Formatear según el tipo de columna
    if ((columna === 'montoMinimo' || columna === 'montoMaximo') && typeof valor === 'number') {
      return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: 'PEN',
        minimumFractionDigits: 0
      }).format(valor);
    }

    if (columna === 'codigo') {
      return valor.toString().toUpperCase();
    }

    return valor.toString();
  }

  obtenerDescripcionColumna(columna: string): string {
    const descripciones: { [key: string]: string } = {
      'codigo': 'Código',
      'nombre': 'Nombre',
      'descripcion': 'Descripción',
      'anioVigencia': 'Año',
      'montoMinimo': 'Monto Mínimo',
      'montoMaximo': 'Monto Máximo'
    };

    return descripciones[columna] || this.metadata?.fieldDescriptions?.[columna] || columna;
  }

  // Getters para el template
  get tieneDatos(): boolean {
    return this.dataSource.length > 0;
  }

  get tieneMetadata(): boolean {
    return !!this.metadata;
  }
}