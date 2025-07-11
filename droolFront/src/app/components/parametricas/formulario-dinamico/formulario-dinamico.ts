// formulario-dinamico.ts
import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  SimpleChanges,
  ChangeDetectionStrategy,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

// Material imports
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { CampoFormulario, TipoCampo, OpcionSelect } from '../../../models/parametricas';
import { Notification } from '../../../services/notification';

@Component({
  selector: 'app-formulario-dinamico',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './formulario-dinamico.html',
  styleUrl: './formulario-dinamico.css'
})
export class FormularioDinamico implements OnInit, OnChanges {
  @Input() campos: CampoFormulario[] = [];
  @Input() modoEdicion = false;
  @Input() loading = false;
  @Input() titulo?: string;
  @Input() subtitulo?: string;

  @Output() guardar = new EventEmitter<CampoFormulario[]>();
  @Output() cancelar = new EventEmitter<void>();
  @Output() validacionCambio = new EventEmitter<boolean>();

  formulario: FormGroup;
  camposOrganizados: { [seccion: string]: CampoFormulario[] } = {};
  secciones: string[] = [];

  constructor(
    private fb: FormBuilder,
    private notificationService: Notification,
    private cdr: ChangeDetectorRef
  ) {
    this.formulario = this.fb.group({});
  }

  ngOnInit(): void {
    this.construirFormulario();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['campos']) {
      this.construirFormulario();
    }
  }

  private construirFormulario(): void {
    if (!this.campos || this.campos.length === 0) {
      return;
    }

    // Organizar campos por secciones
    this.organizarCamposPorSeccion();

    // Construir FormGroup
    const formControls: { [key: string]: any } = {};

    this.campos.forEach(campo => {
      const validators = this.construirValidadores(campo);
      const valorInicial = this.obtenerValorInicial(campo);

      formControls[campo.nombre] = [valorInicial, validators];
    });

    this.formulario = this.fb.group(formControls);

    // Suscribirse a cambios para validación en tiempo real
    this.formulario.valueChanges.subscribe(() => {
      this.validacionCambio.emit(this.formulario.valid);
      this.cdr.detectChanges();
    });

    this.cdr.detectChanges();
  }

  private organizarCamposPorSeccion(): void {
    this.camposOrganizados = {};
    this.secciones = [];

    this.campos.forEach(campo => {
      const seccion = campo.seccion || 'Información General';

      if (!this.camposOrganizados[seccion]) {
        this.camposOrganizados[seccion] = [];
        this.secciones.push(seccion);
      }

      this.camposOrganizados[seccion].push(campo);
    });

    // Ordenar secciones: 'Información General' primero
    this.secciones.sort((a, b) => {
      if (a === 'Información General') return -1;
      if (b === 'Información General') return 1;
      return a.localeCompare(b);
    });
  }

  private construirValidadores(campo: CampoFormulario): any[] {
    const validators: any[] = [];

    if (campo.requerido) {
      validators.push(Validators.required);
    }

    if (campo.tipo === TipoCampo.EMAIL) {
      validators.push(Validators.email);
    }

    if (campo.tipo === TipoCampo.NUMBER && campo.validaciones) {
      if (campo.validaciones.min !== undefined) {
        validators.push(Validators.min(campo.validaciones.min));
      }
      if (campo.validaciones.max !== undefined) {
        validators.push(Validators.max(campo.validaciones.max));
      }
    }

    if (campo.tipo === TipoCampo.TEXT && campo.validaciones) {
      if (campo.validaciones.minLength) {
        validators.push(Validators.minLength(campo.validaciones.minLength));
      }
      if (campo.validaciones.maxLength) {
        validators.push(Validators.maxLength(campo.validaciones.maxLength));
      }
      if (campo.validaciones.pattern) {
        validators.push(Validators.pattern(campo.validaciones.pattern));
      }
    }

    return validators;
  }

  private obtenerValorInicial(campo: CampoFormulario): any {
    if (campo.valor !== undefined && campo.valor !== null) {
      return campo.valor;
    }

    switch (campo.tipo) {
      case TipoCampo.BOOLEAN:
        return false;
      case TipoCampo.NUMBER:
        return 0;
      case TipoCampo.DATE:
        return null;
      case TipoCampo.SELECT:
        return null;
      default:
        return '';
    }
  }

  onSubmit(): void {
    if (this.formulario.invalid) {
      this.marcarCamposComoTocados();
      this.notificationService.warning('Por favor complete todos los campos requeridos');
      return;
    }

    // Actualizar valores en los campos
    const camposActualizados = this.campos.map(campo => ({
      ...campo,
      valor: this.formulario.get(campo.nombre)?.value
    }));

    this.guardar.emit(camposActualizados);
  }

  onCancelar(): void {
    this.cancelar.emit();
  }

  onLimpiar(): void {
    this.formulario.reset();
    this.campos.forEach(campo => {
      const valorInicial = this.obtenerValorInicial(campo);
      this.formulario.get(campo.nombre)?.setValue(valorInicial);
    });
  }

  private marcarCamposComoTocados(): void {
    Object.keys(this.formulario.controls).forEach(key => {
      this.formulario.get(key)?.markAsTouched();
    });
    this.cdr.detectChanges();
  }

  // Helpers para el template
  obtenerErrorMensaje(campo: CampoFormulario): string {
    const control = this.formulario.get(campo.nombre);

    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return `${campo.etiqueta} es requerido`;
    }

    if (control.errors['email']) {
      return 'Formato de email inválido';
    }

    if (control.errors['min']) {
      return `Valor mínimo: ${control.errors['min'].min}`;
    }

    if (control.errors['max']) {
      return `Valor máximo: ${control.errors['max'].max}`;
    }

    if (control.errors['minlength']) {
      return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
    }

    if (control.errors['maxlength']) {
      return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
    }

    if (control.errors['pattern']) {
      return campo.validaciones?.patternMessage || 'Formato inválido';
    }

    return 'Campo inválido';
  }

  tieneError(campo: CampoFormulario): boolean {
    const control = this.formulario.get(campo.nombre);
    return !!(control && control.invalid && control.touched);
  }

  obtenerPlaceholder(campo: CampoFormulario): string {
    if (campo.placeholder) {
      return campo.placeholder;
    }

    switch (campo.tipo) {
      case TipoCampo.EMAIL:
        return 'ejemplo@correo.com';
      case TipoCampo.NUMBER:
        return 'Ingrese un número';
      case TipoCampo.DATE:
        return 'Seleccione una fecha';
      case TipoCampo.SELECT:
        return 'Seleccione una opción';
      default:
        return `Ingrese ${campo.etiqueta.toLowerCase()}`;
    }
  }

  // Getters para el template
  get formularioValido(): boolean {
    return this.formulario.valid;
  }

  get tieneCambios(): boolean {
    return this.formulario.dirty;
  }

  get tituloFormulario(): string {
    if (this.titulo) {
      return this.titulo;
    }
    return this.modoEdicion ? 'Editar Registro' : 'Nuevo Registro';
  }

  // Enum para usar en template
  readonly TipoCampo = TipoCampo;

  // Métodos trackBy para mejorar rendimiento
  trackBySeccion = (index: number, seccion: string): string => seccion;

  trackByCampo = (index: number, campo: CampoFormulario): string => campo.nombre;

  trackByOpcion = (index: number, opcion: OpcionSelect): any => opcion.valor;
}
