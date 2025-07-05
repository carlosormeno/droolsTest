// rule-builder.component.ts
import { Component, EventEmitter, Input, Output, OnInit, ChangeDetectionStrategy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';

interface RuleCondition {
  field: string;
  operator: string;
  value: string;
  connector: 'AND' | 'OR';
}

interface RuleAction {
  type: string;
  value: string;
}

interface RuleTemplate {
  name: string;
  description: string;
  content: string;
  conditions: RuleCondition[];
  actions: RuleAction[];
}

@Component({
  selector: 'app-rule-builder',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTabsModule,
    MatChipsModule,
    MatDividerModule,
    MatTooltipModule,
    MatExpansionModule
  ],
  templateUrl: './rule-builder.html',
  styleUrls: ['./rule-builder.css']
})
export class RuleBuilder implements OnInit {
  @Input() initialRule?: any;
  @Output() ruleGenerated = new EventEmitter<{ name: string; description: string; content: string; template?: string }>();

  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  ruleForm: FormGroup;
  viewMode: 'visual' | 'code' = 'visual';
  conditions: RuleCondition[] = [];
  actions: RuleAction[] = [];

  readonly fields = [
    { value: 'age', label: 'Edad' },
    { value: 'totalPurchases', label: 'Total de Compras' },
    { value: 'loyaltyPoints', label: 'Puntos de Lealtad' },
    { value: 'customerSince', label: 'Cliente Desde' },
    { value: 'lastPurchase', label: 'Última Compra' }
  ];

  readonly operators = [
    { value: '>=', label: 'Mayor o igual a' },
    { value: '<=', label: 'Menor o igual a' },
    { value: '>', label: 'Mayor que' },
    { value: '<', label: 'Menor que' },
    { value: '==', label: 'Igual a' },
    { value: '!=', label: 'Diferente de' }
  ];

  readonly actionTypes = [
    { value: 'setDiscount', label: 'Aplicar Descuento' },
    { value: 'setVipCustomer', label: 'Marcar como VIP' },
    { value: 'setLoyalCustomer', label: 'Marcar como Leal' },
    { value: 'setYoungCustomer', label: 'Marcar como Joven' },
    { value: 'setNewCustomer', label: 'Marcar como Nuevo' },
    { value: 'setRecommendation', label: 'Añadir Recomendación' },
    { value: 'addLoyaltyPoints', label: 'Añadir Puntos' }
  ];

  readonly templates: Record<string, RuleTemplate> = {
    vip: {
      name: 'Cliente VIP',
      description: 'Regla para identificar y beneficiar clientes VIP',
      content: '',
      conditions: [
        { field: 'totalPurchases', operator: '>=', value: '1000', connector: 'AND' },
        { field: 'loyaltyPoints', operator: '>=', value: '500', connector: 'AND' }
      ],
      actions: [
        { type: 'setVipCustomer', value: 'true' },
        { type: 'setDiscount', value: '15%' },
        { type: 'setRecommendation', value: 'Productos Premium' }
      ]
    },
    loyal: {
      name: 'Cliente Leal',
      description: 'Regla para clientes con alta lealtad',
      content: '',
      conditions: [
        { field: 'loyaltyPoints', operator: '>=', value: '200', connector: 'AND' },
        { field: 'totalPurchases', operator: '>=', value: '300', connector: 'AND' }
      ],
      actions: [
        { type: 'setLoyalCustomer', value: 'true' },
        { type: 'setDiscount', value: '10%' },
        { type: 'setRecommendation', value: 'Ofertas Especiales' }
      ]
    },
    young: {
      name: 'Cliente Joven',
      description: 'Regla especial para clientes jóvenes',
      content: '',
      conditions: [
        { field: 'age', operator: '<', value: '30', connector: 'AND' }
      ],
      actions: [
        { type: 'setYoungCustomer', value: 'true' },
        { type: 'setDiscount', value: '5%' },
        { type: 'setRecommendation', value: 'Productos Trending' }
      ]
    },
    new: {
      name: 'Cliente Nuevo',
      description: 'Bienvenida para nuevos clientes',
      content: '',
      conditions: [
        { field: 'totalPurchases', operator: '<', value: '50', connector: 'AND' }
      ],
      actions: [
        { type: 'setNewCustomer', value: 'true' },
        { type: 'setDiscount', value: 'Welcome10' },
        { type: 'setRecommendation', value: 'Productos Populares' }
      ]
    }
  };

  constructor() {
    this.ruleForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      content: ['']
    });
  }

  ngOnInit() {
    this.addCondition();
    this.addAction();

    if (this.initialRule) {
      this.loadRule(this.initialRule);
    }
  }

  toggleViewMode() {
    this.viewMode = this.viewMode === 'visual' ? 'code' : 'visual';
    this.cdr.detectChanges();
  }

  addCondition() {
    this.conditions.push({
      field: 'age',
      operator: '>=',
      value: '',
      connector: 'AND'
    });
    this.updateDRL();
  }

  removeCondition(index: number) {
    this.conditions.splice(index, 1);
    this.updateDRL();
  }

  addAction() {
    this.actions.push({
      type: 'setDiscount',
      value: ''
    });
    this.updateDRL();
  }

  removeAction(index: number) {
    this.actions.splice(index, 1);
    this.updateDRL();
  }

  applyTemplate(templateKey: string) {
    const template = this.templates[templateKey];
    this.ruleForm.patchValue({
      name: template.name,
      description: template.description
    });

    this.conditions = [...template.conditions];
    this.actions = [...template.actions];
    this.updateDRL();
    this.cdr.detectChanges();
  }

  getTemplateKeys(): string[] {
    return Object.keys(this.templates);
  }

  getActionPlaceholder(actionType: string): string {
    const placeholders: Record<string, string> = {
      'setDiscount': 'Ej: 10% o Welcome10',
      'setVipCustomer': 'true',
      'setLoyalCustomer': 'true',
      'setYoungCustomer': 'true',
      'setNewCustomer': 'true',
      'setRecommendation': 'Ej: Productos Premium',
      'addLoyaltyPoints': 'Ej: 100'
    };
    return placeholders[actionType] || 'Ingresa el valor';
  }

  generateDRL(): string {
    const ruleName = this.ruleForm.get('name')?.value || 'Nueva Regla';

    if (this.conditions.length === 0) {
      return `rule "${ruleName}"
    when
        // Añade condiciones
    then
        // Añade acciones
end`;
    }

    const conditionsStr = this.conditions
      .filter(cond => cond.value.trim() !== '')
      .map((cond, index) => {
        const connector = index > 0 ? ` ${cond.connector.toLowerCase()} ` : '';
        return `${connector}${cond.field} ${cond.operator} ${this.formatValue(cond.value)}`;
      }).join('');

    const actionsStr = this.actions
      .filter(action => action.value.trim() !== '')
      .map(action => this.generateActionCode(action))
      .join('\n');

    return `rule "${ruleName}"
    when
        customer: Customer(${conditionsStr || 'true'})
    then
${actionsStr || '        // Añade acciones'}
end`;
  }

  private formatValue(value: string): string {
    // Si es un número, no añadir comillas
    if (!isNaN(Number(value))) {
      return value;
    }
    // Si ya tiene comillas, no añadir más
    if (value.startsWith('"') && value.endsWith('"')) {
      return value;
    }
    // Para strings, añadir comillas
    return `"${value}"`;
  }

  private generateActionCode(action: RuleAction): string {
    switch (action.type) {
      case 'setDiscount':
        return `        customer.setDiscount("${action.value}");`;
      case 'setVipCustomer':
        return `        customer.setVipCustomer(true);`;
      case 'setLoyalCustomer':
        return `        customer.setLoyalCustomer(true);`;
      case 'setYoungCustomer':
        return `        customer.setYoungCustomer(true);`;
      case 'setNewCustomer':
        return `        customer.setNewCustomer(true);`;
      case 'setRecommendation':
        return `        customer.setRecommendation("${action.value}");`;
      case 'addLoyaltyPoints':
        return `        customer.addLoyaltyPoints(${action.value});`;
      default:
        return `        // Acción: ${action.type} = ${action.value}`;
    }
  }

  updateDRL() {
    const drlContent = this.generateDRL();
    this.ruleForm.patchValue({ content: drlContent });
    this.cdr.detectChanges();
  }

  getValidationErrors(): string[] {
    const errors: string[] = [];
    const drlContent = this.generateDRL();

    if (!this.ruleForm.get('name')?.value) {
      errors.push('Falta el nombre de la regla');
    }

    if (!drlContent.includes('rule "')) {
      errors.push('Falta la declaración de regla');
    }

    if (this.conditions.filter(c => c.value.trim() !== '').length === 0) {
      errors.push('Añade al menos una condición');
    }

    if (this.actions.filter(a => a.value.trim() !== '').length === 0) {
      errors.push('Añade al menos una acción');
    }

    return errors;
  }

  isFormValid(): boolean {
    return this.ruleForm.valid && this.getValidationErrors().length === 0;
  }

  resetForm() {
    this.ruleForm.reset();
    this.conditions = [{ field: 'age', operator: '>=', value: '', connector: 'AND' }];
    this.actions = [{ type: 'setDiscount', value: '' }];
    this.updateDRL();
  }

  onSave() {
    if (this.isFormValid()) {
      const rule = {
        name: this.ruleForm.get('name')?.value,
        description: this.ruleForm.get('description')?.value,
        content: this.generateDRL(),
        template: 'custom' // ✅ Añadir template por defecto
      };
      this.ruleGenerated.emit(rule);
    }
  }

  loadRule(rule: any) {
    // Implementar lógica para cargar una regla existente
    this.ruleForm.patchValue({
      name: rule.name,
      description: rule.description
    });
    // TODO: Parsear el contenido DRL y llenar conditions y actions
    // Esta es una funcionalidad avanzada para implementar más tarde
  }
}
