// objetos-contratacion-manager.ts (ACTUALIZADO)
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
  ParametricasHelper,
  TipoCampo
} from '../../../models/parametricas';

import { FormularioDinamico } from '../formulario-dinamico/formulario-dinamico';

@Component({
  selector: 'app-objetos-contratacion-manager',
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
  templateUrl: './objetos-contratacion-manager.html',
  styleUrl: '../styles/parametricas-manager-shared.css'
})
export class ObjetosContratacionManager implements OnInit {
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

  // NUEVO: Para las sub-descripciones
  subDescripciones: EntidadParametrica[] = [];
  subDescripcionesMap: { [key: number]: string } = {};

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
        console.log('Metadata objeto_contratacion:', catalogo.parametricas?.['objeto_contratacion']);

        this.metadata = catalogo.parametricas?.['objeto_contratacion'] || null;

        if (this.metadata) {
          // Configurar columnas (excluir 'id' del display pero incluir acciones)
          this.columnas = this.metadata.fields.filter(f => f !== 'id');
          this.columnasConAcciones = [...this.columnas, 'acciones'];

          // NUEVO: Cargar sub-descripciones primero, luego objetos
          this.cargarSubDescripciones();
        } else {
          this.notificationService.error('No se encontró metadata para Objetos de Contratación');
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

  // NUEVO: Cargar sub-descripciones para el dropdown
  private cargarSubDescripciones() {
    this.parametricasService.getEntidades('sub-descripcion').subscribe({
      next: (data) => {
        if (typeof data === 'string') {
          console.warn('Error al cargar sub-descripciones:', data);
          this.subDescripciones = [];
        } else {
          this.subDescripciones = Array.isArray(data) ? data : [];
          console.log('Sub-descripciones cargadas:', this.subDescripciones.length);
          
          // Crear map para formateo rápido
          this.subDescripcionesMap = {};
          this.subDescripciones.forEach(sub => {
            if (sub.id) {
              this.subDescripcionesMap[sub.id] = sub['nombre'] || sub['codigo'] || `ID: ${sub.id}`;
            }
          });
        }
        
        // Ahora cargar objetos de contratación
        this.cargarObjetosContratacion();
      },
      error: (error) => {
        console.error('Error loading sub-descripciones:', error);
        this.subDescripciones = [];
        this.cargarObjetosContratacion(); // Continuar aunque fallen las sub-descripciones
      }
    });
  }

  private cargarObjetosContratacion() {
    this.parametricasService.getEntidades('objeto-contratacion').subscribe({
      next: (data) => {
        if (typeof data === 'string') {
          this.notificationService.error('Error al cargar Objetos de Contratación: ' + data);
          this.dataSource = [];
        } else {
          this.dataSource = Array.isArray(data) ? data : [];
          console.log('Objetos de Contratación cargados:', this.dataSource.length);
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading Objetos de Contratación:', error);
        this.notificationService.error('Error al cargar Objetos de Contratación');
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('objeto-contratacion', this.metadata);
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('objeto-contratacion', this.metadata);
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
    // Aplicar validaciones específicas para objeto-contratacion
    this.camposFormulario.forEach(campo => {
      if (campo.nombre === 'codigo') {
        campo.validaciones = {
          minLength: 1,
          maxLength: 20,
          pattern: '^[A-Z0-9]+$',
          patternMessage: 'Solo letras mayúsculas y números'
        };
        campo.placeholder = 'Ej: BIEN, SERV, OBRA';
        campo.ayuda = 'Código único identificador (1-20 caracteres, solo letras mayúsculas y números)';
      }
      
      if (campo.nombre === 'nombre') {
        campo.validaciones = {
          minLength: 3,
          maxLength: 50
        };
        campo.placeholder = 'Ej: Bienes, Servicios, Obras';
        campo.ayuda = 'Nombre descriptivo del objeto de contratación (3-50 caracteres)';
      }

      if (campo.nombre === 'descripcion') {
        campo.requerido = false; // Descripción es opcional
        campo.ayuda = 'Descripción detallada del objeto de contratación (opcional)';
      }

      // NUEVO: Campo id_sub_descripcion_contratacion como SELECT
      //if (campo.nombre === 'id_sub_descripcion_contratacion') {
      if (campo.nombre === 'idSubDescripcionContratacion') {
        campo.tipo = TipoCampo.SELECT;
        campo.requerido = false; // Opcional según el refactoring
        campo.ayuda = 'Sub-descripción asociada (opcional)';
        //campo.placeholder = 'Seleccionar sub-descripción...';
        campo.placeholder = this.subDescripciones.length > 0 ? 'Seleccionar sub-descripción...' : 'No hay sub-descripciones disponibles';
        
        // Configurar opciones del SELECT con las sub-descripciones cargadas
        /*campo.opciones = [
          { valor: null, etiqueta: '-- Sin sub-descripción --' },
          ...this.subDescripciones.map(sub => ({
            valor: sub.id,
            etiqueta: `${sub['codigo']} - ${sub['nombre']}`
          }))
        ];*/

        // Configurar opciones del SELECT con las sub-descripciones cargadas
        if (this.subDescripciones.length > 0) {
          // Si hay registros, solo mostrar los registros + opción "sin selección"
          campo.opciones = [
            //{ valor: null, etiqueta: '-- Sin sub-descripción --' },
            ...this.subDescripciones.map(sub => ({
              valor: sub.id,
              etiqueta: `${sub['codigo']} - ${sub['nombre']}`
            }))
          ];
        } else {
          // Si no hay registros, mostrar mensaje informativo
          campo.opciones = [
            { valor: null, etiqueta: 'No se cuenta con registros de sub-descripciones' }
          ];
        }
        
        console.log('Opciones para sub-descripción:', campo.opciones);
      }

      // VERIFICAR: ¿Este campo aún existe después del refactoring?
      //if (campo.nombre === 'permite_sub_descripcion') {
        if (campo.nombre === 'permiteSubDescripcion') {
          campo.ayuda = 'Marcar si este objeto permite subdivisiones (ej: Servicios tienen subcategorías)';
          campo.valor = campo.valor !== undefined ? campo.valor : false; // Default false
      }

      if (campo.nombre === 'estado') {
        campo.valor = campo.valor || 'ACTIVO'; // Default ACTIVO
      }
    });

    // Filtrar campos que no queremos mostrar en el formulario
    this.camposFormulario = this.camposFormulario.filter(campo => 
      campo.nombre !== 'permiteSubDescripcion'
    );
  }

  onGuardar(campos: CampoFormulario[]) {
    // Convertir campos a objeto plano
    const payload: EntidadParametrica = {};
    campos.forEach(campo => {
      payload[campo.nombre] = campo.valor;
    });

    // AGREGAR: Calcular automáticamente permiteSubDescripcion
    const tieneSubDescripcion = payload['idSubDescripcionContratacion'] !== null && 
    payload['idSubDescripcionContratacion'] !== undefined;
    payload['permiteSubDescripcion'] = tieneSubDescripcion;

    console.log('Payload a enviar:', payload); // Para verificar

    this.loading = true;
    this.cdr.detectChanges();

    if (this.modoEdicion && this.entidadEditando?.id) {
      // Actualizar Objeto de Contratación existente
      this.parametricasService.updateEntidad('objeto-contratacion', this.entidadEditando.id, payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al actualizar: ' + response);
          } else {
            this.notificationService.success('Objeto de Contratación actualizado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error updating Objeto de Contratación:', error);
          this.notificationService.error('Error al actualizar Objeto de Contratación');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      // Crear nuevo Objeto de Contratación
      this.parametricasService.createEntidad('objeto-contratacion', payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al crear: ' + response);
          } else {
            this.notificationService.success('Objeto de Contratación creado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating Objeto de Contratación:', error);
          this.notificationService.error('Error al crear Objeto de Contratación');
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
    const confirmMessage = `¿Está seguro de eliminar el Objeto de Contratación "${descripcion}"?\n\nEsto puede afectar las relaciones con Sub-Descripciones y Topes.`;

    if (confirm(confirmMessage)) {
      this.loading = true;
      this.cdr.detectChanges();

      this.parametricasService.deleteEntidad('objeto-contratacion', entidad.id).subscribe({
        next: (response) => {
          if (response) {
            this.notificationService.success('Objeto de Contratación eliminado exitosamente');
            this.cargarMetadataYDatos();
          } else {
            this.notificationService.error('Error al eliminar Objeto de Contratación');
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error deleting Objeto de Contratación:', error);
          this.notificationService.error('Error al eliminar Objeto de Contratación. Puede tener relaciones activas.');
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

    // NUEVO: Formatear el campo de sub-descripción
    //if (columna === 'id_sub_descripcion_contratacion') {
    if (columna === 'idSubDescripcionContratacion') {
      if (!valor) return '--';
      return this.subDescripcionesMap[valor] || `ID: ${valor}`;
    }

    //if (columna === 'permite_sub_descripcion') {
    if (columna === 'permiteSubDescripcion') {
      return valor ? 'Sí' : 'No';
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
      //'id_sub_descripcion_contratacion': 'Sub-Descripción',
      //'permite_sub_descripcion': 'Permite Sub-Desc.',
      'idSubDescripcionContratacion': 'Sub-Descripción',
      'permiteSubDescripcion': 'Permite Sub-Desc.',
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