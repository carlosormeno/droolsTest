// rule-form.component.ts
import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectionStrategy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { Drools } from '../../../services/drools';
import { Notification } from '../../../services/notification';
import { Rule } from '../../../models/rule';
import { RuleBuilder } from '../rule-builder/rule-builder'; // ✅ Corregido
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-rule-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule, // ✅ Necesario para ngModel en modo código
    MatFormFieldModule, // ✅ Necesario para campos en modo código
    MatInputModule, // ✅ Necesario para inputs en modo código
    MatSelectModule, // ✅ Necesario para selects en modo código
    MatCheckboxModule, // ✅ Necesario para checkbox en modo código
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule, // ✅ Necesario para spinner en modo código
    MatButtonToggleModule, // ✅ Para el selector de modo
    MatCardModule,
    RuleBuilder // ✅ Tu nombre preferido
  ],
  templateUrl: './rule-form.html', // ✅ Nombre consistente
  styleUrls: ['./rule-form.css'] // ✅ styleUrls (plural) y .css
})
export class RuleForm implements OnInit { // ✅ Tu nombre preferido
  @Input() editingRule?: Rule | null; // ✅ Permitir null
  @Input() rule?: Rule | null; // ✅ Permitir null
  // ❌ ELIMINAR @Input() isEditing para evitar duplicado
  @Output() ruleCreated = new EventEmitter<Rule>();
  @Output() ruleUpdated = new EventEmitter<Rule>();
  @Output() cancelled = new EventEmitter<void>();

  private droolsService = inject(Drools);
  private notificationService = inject(Notification);
  private cdr = inject(ChangeDetectorRef);

  // ✅ Asegurar que todas las propiedades estén definidas
  currentRule: Partial<Rule> = {
    name: '',
    description: '',
    content: '',
    active: true,
    template: ''
  };

  templateOptions = [
    { value: '', label: 'Seleccionar plantilla...' },
    { value: 'vip', label: 'Cliente VIP' },
    { value: 'loyal', label: 'Cliente Leal' },
    { value: 'young', label: 'Cliente Joven' },
    { value: 'new', label: 'Cliente Nuevo' }
  ];

  isEditing = false;
  loading = false; // ✅ Cambiar de isLoading a loading
  successMessage = ''; // ✅ Debe estar definido
  errorMessage = ''; // ✅ Debe estar definido
  editMode = 'visual'; // ✅ Para modo dual si lo usas

  ngOnInit() {
    // ✅ Manejar ambos inputs por compatibilidad (con null checks)
    if (this.rule && !this.editingRule) {
      this.editingRule = this.rule;
    }

    // ✅ Determinar si está editando basado solo en editingRule
    this.isEditing = !!(this.editingRule && this.editingRule !== null);

    if (this.editingRule && this.editingRule !== null) {
      // ✅ Cargar datos de la regla a editar
      this.currentRule = { ...this.editingRule };
    } else {
      // ✅ Limpiar datos para nueva regla
      this.currentRule = {
        name: '',
        description: '',
        content: '',
        active: true,
        template: ''
      };
    }

    this.clearMessages();
  }

  onTemplateChange() {
    // ✅ Implementar lógica de plantillas
    const selectedTemplate = this.currentRule.template;
    if (selectedTemplate) {
      const templates: Record<string, string> = {
        vip: `rule "Cliente VIP"
when
    customer: Customer(totalPurchases >= 1000, loyaltyPoints >= 500)
then
    customer.setVipCustomer(true);
    customer.setDiscount("15%");
end`,
        loyal: `rule "Cliente Leal"
when
    customer: Customer(loyaltyPoints >= 200, totalPurchases >= 300)
then
    customer.setLoyalCustomer(true);
    customer.setDiscount("10%");
end`,
        young: `rule "Cliente Joven"
when
    customer: Customer(age < 30)
then
    customer.setYoungCustomer(true);
    customer.setDiscount("5%");
end`,
        new: `rule "Cliente Nuevo"
when
    customer: Customer(totalPurchases < 50)
then
    customer.setNewCustomer(true);
    customer.setDiscount("Welcome10");
end`
      };

      if (templates[selectedTemplate]) {
        this.currentRule.content = templates[selectedTemplate];
        this.cdr.detectChanges();
      }
    }
  }

  onModeChange() {
    // ✅ Método para cambio de modo (si usas modo dual)
    this.cdr.detectChanges();
  }

  onSubmit() {
    if (!this.currentRule.name || !this.currentRule.content) {
      this.notificationService.error('Por favor completa todos los campos requeridos');
      return;
    }

    const ruleData = {
      name: this.currentRule.name!,
      description: this.currentRule.description || '',
      content: this.currentRule.content!,
      template: this.currentRule.template || 'custom'
    };

    this.onRuleGenerated(ruleData);
  }

  testRule() {
    // ✅ Implementar test de regla
    this.notificationService.info('Función de prueba en desarrollo');
  }

  onRuleGenerated(ruleData: { name: string; description: string; content: string; template?: string }) {
    this.clearMessages();
    this.loading = true; // ✅ Usar loading consistentemente
    this.cdr.detectChanges();

    if (this.isEditing && this.editingRule && this.editingRule !== null) {
      // ✅ Verificar que la regla tenga ID y no sea null
      if (!this.editingRule.id) {
        this.errorMessage = 'Error: La regla no tiene un ID válido para actualizar';
        this.notificationService.error('Error: La regla no tiene un ID válido');
        this.loading = false;
        this.cdr.detectChanges();
        return;
      }

      // Actualizar regla existente
      const updatedRule: Rule = {
        ...this.editingRule,
        name: ruleData.name,
        description: ruleData.description,
        content: ruleData.content,
        active: this.currentRule.active ?? true,
        template: ruleData.template || this.currentRule.template || 'custom' // ✅ Usar template del RuleBuilder
      };

      console.log('Updating rule:', updatedRule); // ✅ Debug log

      this.droolsService.updateRule(updatedRule).subscribe({
        next: (rule) => {
          this.loading = false;
          this.successMessage = `Regla "${rule.name}" actualizada correctamente`;
          this.notificationService.success('Regla actualizada correctamente');
          this.ruleUpdated.emit(rule);
          this.cdr.detectChanges();

          setTimeout(() => {
            this.clearMessages();
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = 'Error al actualizar la regla: ' + (error.message || 'Error desconocido');
          this.notificationService.error('Error al actualizar la regla');
          console.error('Error updating rule:', error);
          this.cdr.detectChanges();
        }
      });
    } else {
      // Crear nueva regla
      const newRule: Omit<Rule, 'id'> = {
        name: ruleData.name,
        description: ruleData.description,
        content: ruleData.content,
        active: this.currentRule.active ?? true,
        template: ruleData.template || 'custom' // ✅ Usar template del RuleBuilder
      };

      console.log('Creating new rule:', newRule); // ✅ Debug log

      this.droolsService.createRule(newRule).subscribe({
        next: (rule) => {
          this.loading = false;
          this.successMessage = `Regla "${rule.name}" creada correctamente`;
          this.notificationService.success('Regla creada correctamente');
          this.ruleCreated.emit(rule);
          this.cdr.detectChanges();

          setTimeout(() => {
            this.clearMessages();
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = 'Error al crear la regla: ' + (error.message || 'Error desconocido');
          this.notificationService.error('Error al crear la regla');
          console.error('Error creating rule:', error);
          this.cdr.detectChanges();
        }
      });
    }
  }

  onCancel() {
    this.clearMessages();
    this.cancelled.emit();
  }

  private clearMessages() {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
