// topes-manager.ts
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
  selector: 'app-topes-manager',
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
  templateUrl: './topes-manager.html',
  styleUrl: '../styles/parametricas-manager-shared.css'
})
export class TopesManager implements OnInit {
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

  // Para los dropdowns (relaciones)
  tiposProceso: EntidadParametrica[] = [];
  objetosContratacion: EntidadParametrica[] = [];
  operadoresMonto: EntidadParametrica[] = [];
  
  // Maps para formateo rápido
  tiposProcesoMap: { [key: number]: string } = {};
  objetosContratacionMap: { [key: number]: string } = {};
  operadoresMontoMap: { [key: number]: string } = {};

  // Agregar propiedad para UITs
  uits: EntidadParametrica[] = [];
  uitsMap: { [key: number]: EntidadParametrica } = {}; // año -> UIT

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
        console.log('Metadata topes:', catalogo.parametricas?.['topes']);

        this.metadata = catalogo.parametricas?.['topes'] || null;

        if (this.metadata) {
          // Configurar columnas (excluir 'id' del display pero incluir acciones)
          this.columnas = this.metadata.fields.filter(f => f !== 'id');
          this.columnasConAcciones = [...this.columnas, 'acciones'];

          // Cargar todas las entidades relacionadas primero
          this.cargarEntidadesRelacionadas();
        } else {
          this.notificationService.error('No se encontró metadata para Topes');
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

  private cargarEntidadesRelacionadas() {
    // Cargar tipos de proceso, objetos de contratación y operadores de monto en paralelo
    const tiposProceso$ = this.parametricasService.getEntidades('tipo-proceso');
    const objetosContratacion$ = this.parametricasService.getEntidades('objeto-contratacion');
    const operadoresMonto$ = this.parametricasService.getEntidades('operadores-monto');
    const uits$ = this.parametricasService.getEntidades('uit');

    // Tipos de proceso
    tiposProceso$.subscribe({
      next: (data) => {
        if (Array.isArray(data)) {
          this.tiposProceso = data;
          this.tiposProcesoMap = {};
          this.tiposProceso.forEach(tipo => {
            if (tipo.id) {
              this.tiposProcesoMap[tipo.id] = `${tipo['codigo']} - ${tipo['nombre']}`;
            }
          });
          console.log('Tipos de proceso cargados:', this.tiposProceso.length);
        }
        this.verificarCargaCompleta();
      },
      error: (error) => {
        console.error('Error loading tipos proceso:', error);
        this.verificarCargaCompleta();
      }
    });

    // Objetos de contratación
    objetosContratacion$.subscribe({
      next: (data) => {
        if (Array.isArray(data)) {
          this.objetosContratacion = data;
          this.objetosContratacionMap = {};
          this.objetosContratacion.forEach(obj => {
            if (obj.id) {
              this.objetosContratacionMap[obj.id] = `${obj['codigo']} - ${obj['nombre']}`;
            }
          });
          console.log('Objetos de contratación cargados:', this.objetosContratacion.length);
        }
        this.verificarCargaCompleta();
      },
      error: (error) => {
        console.error('Error loading objetos contratacion:', error);
        this.verificarCargaCompleta();
      }
    });

    // Operadores de monto
    operadoresMonto$.subscribe({
      next: (data) => {
        if (Array.isArray(data)) {
          this.operadoresMonto = data;
          this.operadoresMontoMap = {};
          this.operadoresMonto.forEach(op => {
            if (op.id) {
              this.operadoresMontoMap[op.id] = `${op['simbolo']} (${op['nombre']})`;
            }
          });
          console.log('Operadores de monto cargados:', this.operadoresMonto.length);
        }
        this.verificarCargaCompleta();
      },
      error: (error) => {
        console.error('Error loading operadores monto:', error);
        this.verificarCargaCompleta();
      }
    });

    uits$.subscribe({
      next: (data) => {
        if (Array.isArray(data)) {
          this.uits = data;

          console.log('UITs recibidas del backend:', data);
      if (data.length > 0) {
        console.log('Primera UIT completa:', data[0]);
        console.log('Campos disponibles en UIT:', Object.keys(data[0]));
      }

          this.uitsMap = {};
          this.uits.forEach(uit => {
            console.log('Procesando UIT:', uit);
            if (uit['anioVigencia']) {
              this.uitsMap[uit['anioVigencia']] = uit;
              console.log(`UIT mapeada: ${uit['anioVigencia']} ->`, uit);
            }else {
              console.log('UIT sin anio_vigencia:', uit); // DEBUG problema
            }
          });
          console.log('UITs Map final:', this.uitsMap);
          console.log('UITs cargadas:', this.uits.length);
        }
        this.verificarCargaCompleta();
      },
      error: (error) => {
        console.error('Error loading UITs:', error);
        this.verificarCargaCompleta();
      }
    });

  }

  private contadorCarga = 0;
  private verificarCargaCompleta() {
    this.contadorCarga++;
    if (this.contadorCarga >= 4) { // Cuando las 3 cargas estén completas
      this.cargarTopes();
    }
  }

  private cargarTopes() {
    this.parametricasService.getEntidades('topes').subscribe({
      next: (data) => {
        if (typeof data === 'string') {
          this.notificationService.error('Error al cargar Topes: ' + data);
          this.dataSource = [];
        } else {
          this.dataSource = Array.isArray(data) ? data : [];
          console.log('Topes cargados:', this.dataSource.length);
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading Topes:', error);
        this.notificationService.error('Error al cargar Topes');
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('topes', this.metadata);
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

    this.camposFormulario = ParametricasHelper.generarCamposFormulario('topes', this.metadata);
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
    // Aplicar validaciones específicas para topes
    this.camposFormulario.forEach(campo => {
      
      // Dropdown de Tipo de Proceso
      if (campo.nombre === 'idTipoProcesoSeleccion') {
        campo.tipo = TipoCampo.SELECT;
        campo.requerido = true;
        campo.ayuda = 'Tipo de proceso de selección al que aplica este tope';
        campo.placeholder = 'Seleccionar tipo de proceso...';
        campo.opciones = this.tiposProceso.map(tipo => ({
          valor: tipo.id,
          etiqueta: `${tipo['codigo']} - ${tipo['nombre']}`
        }));
      }

      // Dropdown de Objeto de Contratación
      if (campo.nombre === 'idObjetoContratacion') {
        campo.tipo = TipoCampo.SELECT;
        campo.requerido = true;
        campo.ayuda = 'Objeto de contratación al que aplica este tope';
        campo.placeholder = 'Seleccionar objeto de contratación...';
        campo.opciones = this.objetosContratacion.map(obj => ({
          valor: obj.id,
          etiqueta: `${obj['codigo']} - ${obj['nombre']}`
        }));
      }

      // Dropdown de Operador de Monto
      if (campo.nombre === 'idOperadorMonto') {
        campo.tipo = TipoCampo.SELECT;
        campo.requerido = true;
        campo.ayuda = 'Operador matemático para evaluar el monto';
        campo.placeholder = 'Seleccionar operador...';
        campo.opciones = this.operadoresMonto.map(op => ({
          valor: op.id,
          etiqueta: `${op['simbolo']} (${op['nombre']})`
        }));
      }

      // Campo monto
      if (campo.nombre === 'monto') {
        campo.tipo = TipoCampo.NUMBER;
        campo.validaciones = {
          min: 0,
          step: 0.01
        };
        campo.placeholder = 'Ej: 150000.00';
        campo.ayuda = 'Monto límite en soles (debe ser mayor o igual a 0)';
        campo.sufijo = 'S/';
      }

      // Campo referencia UIT
      /*if (campo.nombre === 'referenciaUit') {
        campo.tipo = TipoCampo.NUMBER;
        campo.requerido = false;
        campo.validaciones = {
          min: 0,
          step: 0.0001
        };
        campo.placeholder = 'Ej: 3.5000';
        campo.ayuda = 'Referencia en UIT para este tope (opcional)';
        campo.sufijo = 'UIT';
      }*/

        if (campo.nombre === 'referenciaUit') {
          campo.tipo = TipoCampo.NUMBER;
          campo.soloLectura = false;
          campo.requerido = false;
          campo.ayuda = 'Se calcula automáticamente según el año seleccionado';
          campo.sufijo = 'UIT';
          campo.placeholder = 'Se completará automáticamente...';
          campo.validaciones = {
            min: 0,
            step: 0.0001
          };
          
          // Valor sugerido según año actual
          const añoActual = new Date().getFullYear();
          const uitActual = this.uitsMap[añoActual];
          if (uitActual) {
            campo.valor = uitActual['monto'];
          }
        }

      // Campo año de vigencia
      /*if (campo.nombre === 'anioVigencia') {
        campo.tipo = TipoCampo.NUMBER;
        campo.validaciones = {
          min: 2020,
          max: 2030
        };
        campo.placeholder = 'Ej: 2024';
        campo.ayuda = 'Año de vigencia del tope (2020-2030)';
        campo.valor = campo.valor || new Date().getFullYear(); // Default año actual
      }*/

        if (campo.nombre === 'anioVigencia') {
          campo.tipo = TipoCampo.SELECT;
          campo.requerido = true;
          campo.ayuda = 'Año fiscal para este tope (determina la UIT a usar)';
          campo.placeholder = 'Seleccionar año...';
          
          // Opciones: años disponibles en tabla UIT
          campo.opciones = this.uits.map(uit => ({
            valor: uit['anioVigencia'],
            etiqueta: `${uit['anioVigencia']} (UIT: S/ ${uit['monto']})`
          })).sort((a, b) => b.valor - a.valor); // Ordenar descendente
          
          // Listener para cambios de año
          //this.configurarListenerAnio(campo);
        }

      // Campo observaciones
      if (campo.nombre === 'observaciones') {
        campo.tipo = TipoCampo.TEXTAREA;
        campo.requerido = false;
        campo.validaciones = {
          maxLength: 500,
          filas: 3
        };
        campo.placeholder = 'Observaciones adicionales sobre este tope...';
        campo.ayuda = 'Comentarios o notas adicionales sobre este tope (opcional)';
      }

      // Estado
      if (campo.nombre === 'estado') {
        campo.valor = campo.valor || 'ACTIVO';
      }
    });

    this.camposFormulario = this.camposFormulario.filter(campo => 
      campo.nombre !== 'idUit'
    );

    console.log('Campos configurados para topes:', this.camposFormulario);
  }

  onGuardar(campos: CampoFormulario[]) {
    // Convertir campos a objeto plano
    const payload: EntidadParametrica = {};
    campos.forEach(campo => {
      payload[campo.nombre] = campo.valor;
    });

    // Calcular UIT automáticamente según el año
  const anioSeleccionado = payload['anioVigencia'];
  if (anioSeleccionado && this.uitsMap[anioSeleccionado]) {
    const uitDelAnio = this.uitsMap[anioSeleccionado];
    payload['idUit'] = uitDelAnio.id;
    payload['referenciaUit'] = uitDelAnio['monto'];
    
    console.log(`Año ${anioSeleccionado} -> UIT ID: ${uitDelAnio.id}, Monto: ${uitDelAnio['monto']}`);
  } else if (anioSeleccionado) {
    this.notificationService.error(`No se encontró UIT para el año ${anioSeleccionado}`);
    this.loading = false;
    this.cdr.detectChanges();
    return;
  }

    this.loading = true;
    this.cdr.detectChanges();

    if (this.modoEdicion && this.entidadEditando?.id) {
      // Actualizar Tope existente
      this.parametricasService.updateEntidad('topes', this.entidadEditando.id, payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al actualizar: ' + response);
          } else {
            this.notificationService.success('Tope actualizado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error updating Tope:', error);
          this.notificationService.error('Error al actualizar Tope');
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      // Crear nuevo Tope
      this.parametricasService.createEntidad('topes', payload).subscribe({
        next: (response) => {
          if (typeof response === 'string') {
            this.notificationService.error('Error al crear: ' + response);
          } else {
            this.notificationService.success('Tope creado exitosamente');
            this.cargarMetadataYDatos();
            this.cerrarFormulario();
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating Tope:', error);
          this.notificationService.error('Error al crear Tope');
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

    const año = entidad['anioVigencia'] || 'N/A';
    const monto = entidad['monto'] || 'N/A';
    const confirmMessage = `¿Está seguro de eliminar el Tope del año ${año} con monto ${monto}?`;

    if (confirm(confirmMessage)) {
      this.loading = true;
      this.cdr.detectChanges();

      this.parametricasService.deleteEntidad('topes', entidad.id).subscribe({
        next: (response) => {
          if (response) {
            this.notificationService.success('Tope eliminado exitosamente');
            this.cargarMetadataYDatos();
          } else {
            this.notificationService.error('Error al eliminar Tope');
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error deleting Tope:', error);
          this.notificationService.error('Error al eliminar Tope');
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

    // Formatear campos de relación
    if (columna === 'idTipoProcesoSeleccion') {
      return this.tiposProcesoMap[valor] || `ID: ${valor}`;
    }

    if (columna === 'idObjetoContratacion') {
      return this.objetosContratacionMap[valor] || `ID: ${valor}`;
    }

    if (columna === 'idOperadorMonto') {
      return this.operadoresMontoMap[valor] || `ID: ${valor}`;
    }

    // Formatear montos
    if (columna === 'monto') {
      return `S/ ${Number(valor).toLocaleString('es-PE', { minimumFractionDigits: 2 })}`;
    }

    if (columna === 'referenciaUit') {
      if (!valor) return '--';
      return `${Number(valor).toFixed(4)} UIT`;
    }

    // Formatear año
    /*if (columna === 'anioVigencia') {
      return `${valor}`;
    }*/

    if (columna === 'estado') {
      const estados: { [key: string]: string } = {
        'ACTIVO': '✅ Activo',
        'INACTIVO': '❌ Inactivo'
      };
      return estados[valor] || valor.toString();
    }

    if (columna === 'anioVigencia') {
      const uitDelAnio = this.uitsMap[valor];
      return uitDelAnio ? 
        `${valor} (UIT: S/ ${uitDelAnio['monto']})` : 
        `${valor}`;
    }

    return valor.toString();
  }

  obtenerDescripcionColumna(columna: string): string {
    const descripciones: { [key: string]: string } = {
      'idTipoProcesoSeleccion': 'Tipo de Proceso',
      'idObjetoContratacion': 'Objeto Contratación',
      'idOperadorMonto': 'Operador',
      'monto': 'Monto',
      'referenciaUit': 'Ref. UIT',
      'observaciones': 'Observaciones',
      'anioVigencia': 'Año',
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