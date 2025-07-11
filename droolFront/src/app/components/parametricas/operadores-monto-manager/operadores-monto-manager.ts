// operadores-monto-manager.ts
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
  selector: 'app-operadores-monto-manager',
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
  templateUrl: './operadores-monto-manager.html',
  styleUrl: '../styles/parametricas-manager-shared.css'
})
export class OperadoresMontoManager implements OnInit {
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
        console.log('Metadata operadores_monto:', catalogo.parametricas?.['operadores_monto']);

        this.metadata = catalogo.parametricas?.['operadores_monto'] || null;

        if (this.metadata) {
          // Configurar columnas (excluir 'id' del display pero incluir acciones)
          this.columnas = this.metadata.fields.filter(f => f !== 'id');
          this.columnasConAcciones = [...this.columnas, 'acciones'];

          this.cargarOperadoresMonto();
        } else {
          this.notificationService.error('No se encontró metadata para Operadores de Monto');
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

  private cargarOperadoresMonto() {
    this.parametricasService.getEntidades('operadores-monto').subscribe({
      next: (data) => {
        if (typeof data === 'string') {
          this.notificationService.error('Error al cargar Operadores de Monto: ' + data);
          this.dataSource = [];
        } else {
          this.dataSource = Array.isArray(data) ? data : [];
          console.log('Operadores de Monto cargados:', this.dataSource.length);
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading Operadores de Monto:', error);
        this.notificationService.error('Error al cargar Operadores de Monto');
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('operadores-monto', this.metadata);
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('operadores-monto', this.metadata);
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
    // Aplicar validaciones específicas para operadores-monto
    this.camposFormulario.forEach(campo => {
      if (campo.nombre === 'codigo') {
        campo.validaciones = {
          minLength: 2,
          maxLength: 20,
          pattern: '^[A-Z0-9_]+$',
          patternMessage: 'Solo letras mayúsculas, números y guión bajo'
        };
        campo.placeholder = 'Ej: MAYOR, MENOR, IGUAL';
        campo.ayuda = 'Código único del operador (2-20 caracteres, solo A-Z, 0-9 y _)';
      }
      
      if (campo.nombre === 'nombre') {
        campo.validaciones = {
          minLength: 3,
          maxLength: 50
        };
        campo.placeholder = 'Ej: Mayor que, Menor que, Igual a';
        campo.ayuda = 'Nombre descriptivo del operador matemático (3-50 caracteres)';
      }

      if (campo.nombre === 'simbolo') {
        campo.validaciones = {
          minLength: 1,
          maxLength: 10,
          pattern: '^[><=≥≤≠±+\\-*/^()\\s]+$',
          patternMessage: 'Solo símbolos matemáticos válidos'
        };
        campo.placeholder = 'Ej: >, <, =, >=, <=';
        campo.ayuda = 'Símbolo matemático del operador (1-10 caracteres)';
      }

      if (campo.nombre === 'descripcion') {
        campo.requerido = false; // Descripción es opcional
        campo.ayuda = 'Descripción detallada del uso del operador (opcional)';
        campo.validaciones = {
          maxLength: 500
        };
        campo.placeholder = 'Ej: Utilizado para evaluar si un monto es mayor que el límite establecido';
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
      // Actualizar Operador de Monto existente
      this.parametricasService.updateEntidad('operadores-monto', this.entidadEditando.id, payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al actualizar: ' + response);
          } else {
            this.notificationService.success('Operador de Monto actualizado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error updating Operador de Monto:', error);
          this.notificationService.error('Error al actualizar Operador de Monto');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      // Crear nuevo Operador de Monto
      this.parametricasService.createEntidad('operadores-monto', payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al crear: ' + response);
          } else {
            this.notificationService.success('Operador de Monto creado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating Operador de Monto:', error);
          this.notificationService.error('Error al crear Operador de Monto');
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
    const confirmMessage = `¿Está seguro de eliminar el Operador de Monto "${descripcion}"?\n\nEsto puede afectar las relaciones con Topes.`;

    if (confirm(confirmMessage)) {
      this.loading = true;
      this.cdr.detectChanges();

      this.parametricasService.deleteEntidad('operadores-monto', entidad.id).subscribe({
        next: (response) => {
          if (response) {
            this.notificationService.success('Operador de Monto eliminado exitosamente');
            this.cargarMetadataYDatos();
          } else {
            this.notificationService.error('Error al eliminar Operador de Monto');
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error deleting Operador de Monto:', error);
          this.notificationService.error('Error al eliminar Operador de Monto. Puede tener relaciones activas.');
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

    if (columna === 'simbolo') {
      return `[${valor}]`; // Mostrar símbolos entre corchetes para destacar
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
      'simbolo': 'Símbolo',
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