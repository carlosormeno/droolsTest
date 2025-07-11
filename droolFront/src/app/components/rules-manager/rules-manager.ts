import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RuleForm } from '../rule-form/rule-form'; // ✅ Tu nombre preferido
import { RulesList } from '../rules-list/rules-list'; // ✅ Tu nombre preferido
import { Drools } from '../../services/drools';
import { Notification } from '../../services/notification';
import { Rule } from '../../models/rule';

@Component({
  selector: 'app-rules-manager',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTabsModule,
    MatIconModule,
    MatButtonModule,
    RuleForm, // ✅ Tu nombre preferido
    RulesList // ✅ Tu nombre preferido
  ],
  templateUrl: './rules-manager.html',
  styleUrls: ['./rules-manager.css'] // ✅ CORREGIDO: styleUrls (plural)
})
export class RulesManager implements OnInit { // ✅ Tu nombre preferido
  rules: Rule[] = [];
  showRuleForm = false;
  isCreatingRule = false;
  selectedRule: Rule | null = null;
  isEditing = false;

  constructor(
    private droolsService: Drools,
    private notificationService: Notification
  ) {}

  ngOnInit() {
    this.loadRules();
  }

  loadRules() {
    this.droolsService.getRules().subscribe({
      next: (rules) => {
        this.rules = rules;
      },
      error: (error) => {
        this.notificationService.error('Error al cargar las reglas');
        console.error('Error loading rules:', error);
      }
    });
  }

  onRuleCreated(rule: Rule) {
    console.log('Nueva regla creada:', rule);
    this.notificationService.success('Regla creada exitosamente');
    this.selectedRule = null; // ✅ Limpiar selección
    this.isEditing = false; // ✅ Salir del modo edición
    this.showRuleForm = false; // ✅ Ocultar formulario después de crear
    this.loadRules();
  }

  onRuleUpdated(rule: Rule) {
    console.log('Regla actualizada:', rule);
    this.notificationService.success('Regla actualizada exitosamente');
    this.selectedRule = null;
    this.isEditing = false;
    this.showRuleForm = false; // ✅ Ocultar formulario después de actualizar
    this.loadRules();
  }

  onEditRule(rule: Rule) {
    this.selectedRule = rule;
    this.isEditing = true;
    this.showRuleForm = true; // ✅ Mostrar formulario para editar
  }

  onRuleFormCancelled() {
    console.log('Formulario de regla cancelado');
    this.selectedRule = null; // ✅ Limpiar selección
    this.isEditing = false; // ✅ Salir del modo edición
    this.showRuleForm = false; // ✅ Ocultar formulario
  }

  // ✅ Método para crear nueva regla
  createNewRule() {
    this.selectedRule = null; // null = modo creación
    this.isEditing = false; // false = crear nueva
    this.showRuleForm = true; // ✅ Mostrar el formulario
  }

  onDeleteRule(ruleId: number) {
    if (confirm('¿Estás seguro de que quieres eliminar esta regla?')) {
      this.droolsService.deleteRule(ruleId).subscribe({
        next: () => {
          this.notificationService.success('Regla eliminada exitosamente');
          this.loadRules();
        },
        error: (error) => {
          this.notificationService.error('Error al eliminar la regla');
          console.error('Error deleting rule:', error);
        }
      });
    }
  }

  onToggleRule(ruleId: number) {
    this.droolsService.toggleRule(ruleId).subscribe({
      next: (rule) => {
        this.notificationService.success(`Regla ${rule.active ? 'activada' : 'desactivada'}`);
        this.loadRules();
      },
      error: (error) => {
        this.notificationService.error('Error al cambiar estado de la regla');
        console.error('Error toggling rule:', error);
      }
    });
  }

  onCancelEdit() {
    this.selectedRule = null;
    this.isEditing = false;
  }
}
