// sub-descripcion-manager.ts
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
  selector: 'app-sub-descripcion-manager',
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
  templateUrl: './sub-descripcion-manager.html',
  styleUrl: '../styles/parametricas-manager-shared.css'
})
export class SubDescripcionManager implements OnInit {
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
        console.log('Metadata sub_descripcion:', catalogo.parametricas?.['sub_descripcion']);

        this.metadata = catalogo.parametricas?.['sub_descripcion'] || null;

        if (this.metadata) {
          // Configurar columnas (excluir 'id' del display pero incluir acciones)
          this.columnas = this.metadata.fields.filter(f => f !== 'id');
          this.columnasConAcciones = [...this.columnas, 'acciones'];

          this.cargarSubDescripciones();
        } else {
          this.notificationService.error('No se encontró metadata para Sub-Descripciones');
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

  private cargarSubDescripciones() {
    this.parametricasService.getEntidades('sub-descripcion').subscribe({
      next: (data) => {
        if (typeof data === 'string') {
          this.notificationService.error('Error al cargar Sub-Descripciones: ' + data);
          this.dataSource = [];
        } else {
          this.dataSource = Array.isArray(data) ? data : [];
          console.log('Sub-Descripciones cargadas:', this.dataSource.length);
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading Sub-Descripciones:', error);
        this.notificationService.error('Error al cargar Sub-Descripciones');
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('sub-descripcion', this.metadata);
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('sub-descripcion', this.metadata);
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
    // Aplicar validaciones específicas para sub-descripcion
    this.camposFormulario.forEach(campo => {
      if (campo.nombre === 'codigo') {
        campo.validaciones = {
          minLength: 2,
          maxLength: 15,
          pattern: '^[A-Z0-9_]+$',
          patternMessage: 'Solo letras mayúsculas, números y guión bajo'
        };
        campo.placeholder = 'Ej: SERV_CONSUL, BIEN_COMP';
        campo.ayuda = 'Código único identificador (2-15 caracteres, solo letras mayúsculas, números y _)';
      }
      
      if (campo.nombre === 'nombre') {
        campo.validaciones = {
          minLength: 5,
          maxLength: 100
        };
        campo.placeholder = 'Ej: Servicios de Consultoría, Bienes de Cómputo';
        campo.ayuda = 'Nombre descriptivo de la sub-descripción (5-100 caracteres)';
      }

      if (campo.nombre === 'descripcion') {
        campo.requerido = false; // Descripción es opcional
        campo.ayuda = 'Descripción detallada de la sub-descripción (opcional)';
        campo.validaciones = {
          maxLength: 500
        };
      }

      if (campo.nombre === 'estado') {
        campo.valor = campo.valor || 'ACTIVO'; // Default ACTIVO
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
      // Actualizar Sub-Descripción existente
      this.parametricasService.updateEntidad('sub-descripcion', this.entidadEditando.id, payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al actualizar: ' + response);
          } else {
            this.notificationService.success('Sub-Descripción actualizada exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error updating Sub-Descripción:', error);
          this.notificationService.error('Error al actualizar Sub-Descripción');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      // Crear nueva Sub-Descripción
      this.parametricasService.createEntidad('sub-descripcion', payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al crear: ' + response);
          } else {
            this.notificationService.success('Sub-Descripción creada exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating Sub-Descripción:', error);
          this.notificationService.error('Error al crear Sub-Descripción');
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
    const confirmMessage = `¿Está seguro de eliminar la Sub-Descripción "${descripcion}"?\n\nEsto puede afectar las relaciones con Objetos de Contratación.`;

    if (confirm(confirmMessage)) {
      this.loading = true;
      this.cdr.detectChanges();

      this.parametricasService.deleteEntidad('sub-descripcion', entidad.id).subscribe({
        next: (response) => {
          if (response) {
            this.notificationService.success('Sub-Descripción eliminada exitosamente');
            this.cargarMetadataYDatos();
          } else {
            this.notificationService.error('Error al eliminar Sub-Descripción');
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error deleting Sub-Descripción:', error);
          this.notificationService.error('Error al eliminar Sub-Descripción. Puede tener relaciones activas.');
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
    if (columna === 'codigo') {
      return valor.toString().toUpperCase();
    }

    if (columna === 'estado') {
      const estados: { [key: string]: string } = {
        'ACTIVO': '✅ Activo',
        'INACTIVO': '❌ Inactivo'
      };
      return estados[valor] || valor.toString();
    }

    return valor.toString();
  }

  obtenerDescripcionColumna(columna: string): string {
    const descripciones: { [key: string]: string } = {
      'codigo': 'Código',
      'nombre': 'Nombre',
      'descripcion': 'Descripción',
      'estado': 'Estado'
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