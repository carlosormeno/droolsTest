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
  selector: 'app-uit-manager',
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
    FormularioDinamico // Agregar aquí
  ],
  templateUrl: './uit-manager.html',
  styleUrl: '../styles/parametricas-manager-shared.css'
})
export class UitManager implements OnInit {
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
        console.log('Metadata uit:', catalogo.parametricas?.['uit']);

        this.metadata = catalogo.parametricas?.['uit'] || null;

        if (this.metadata) {
          // Configurar columnas (excluir 'id' del display pero incluir acciones)
          this.columnas = this.metadata.fields.filter(f => f !== 'id');
          this.columnasConAcciones = [...this.columnas, 'acciones'];

          this.cargarUITs();
        } else {
          this.notificationService.error('No se encontró metadata para UITs');
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

  private cargarUITs() {
    this.parametricasService.getEntidades('uit').subscribe({
      next: (data) => {
        if (typeof data === 'string') {
          this.notificationService.error('Error al cargar UITs: ' + data);
          this.dataSource = [];
        } else {
          this.dataSource = Array.isArray(data) ? data : [];
          console.log('UITs cargadas:', this.dataSource.length);
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading UITs:', error);
        this.notificationService.error('Error al cargar UITs');
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('uit', this.metadata);
    this.camposFormulario.forEach(campo => {
      campo.etiqueta = campo.nombre;
    });
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('uit', this.metadata);

    // Prellenar los valores usando acceso dinámico
    this.camposFormulario.forEach(campo => {
      campo.valor = entidad[campo.nombre];
    });

    this.modoEdicion = true;
    this.entidadEditando = entidad;
    this.mostrandoFormulario = true;
    this.cdr.detectChanges();
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
      // Actualizar UIT existente
      this.parametricasService.updateEntidad('uit', this.entidadEditando.id, payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al actualizar: ' + response);
          } else {
            this.notificationService.success('UIT actualizada exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error updating UIT:', error);
          this.notificationService.error('Error al actualizar UIT');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      // Crear nueva UIT
      this.parametricasService.createEntidad('uit', payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al crear: ' + response);
          } else {
            this.notificationService.success('UIT creada exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating UIT:', error);
          this.notificationService.error('Error al crear UIT');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  onEliminar(entidad: EntidadParametrica) {
    // Validar que la entidad tenga ID
    if (!entidad.id) {
      this.notificationService.error('No se puede eliminar: ID no válido');
      return;
    }

    const confirmMessage = `¿Está seguro de eliminar la UIT ${entidad['anioVigencia'] || entidad.id}?`;

    if (confirm(confirmMessage)) {
      this.loading = true;
      this.cdr.detectChanges();

      this.parametricasService.deleteEntidad('uit', entidad.id).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al eliminar: ' + response);
          } else {
            this.notificationService.success('UIT eliminada exitosamente');
            this.cargarMetadataYDatos();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error deleting UIT:', error);
          this.notificationService.error('Error al eliminar UIT');
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
    if (columna === 'monto' && typeof valor === 'number') {
      return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: 'PEN',
        minimumFractionDigits: 0
      }).format(valor);
    }

    if (columna === 'fechaVigencia' && valor instanceof Date) {
      return valor.toLocaleDateString('es-PE');
    }

    return valor.toString();
  }

  obtenerDescripcionColumna(columna: string): string {
    return this.metadata?.fieldDescriptions?.[columna] || columna;
  }

  // Getters para el template
  get tieneDatos(): boolean {
    return this.dataSource.length > 0;
  }

  get tieneMetadata(): boolean {
    return !!this.metadata;
  }
}
