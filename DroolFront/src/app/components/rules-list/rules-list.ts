import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Rule } from '../../models/rule';

@Component({
  selector: 'app-rules-list',
  standalone: true,
  imports: [CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSlideToggleModule,
    MatTooltipModule],
  templateUrl: './rules-list.html',
  styleUrl: './rules-list.css'
})
export class RulesList {

  @Input() rules: Rule[] = [];
  @Output() editRule = new EventEmitter<Rule>();
  @Output() deleteRule = new EventEmitter<number>();
  @Output() toggleRule = new EventEmitter<number>();

  onEditRule(rule: Rule) {
    this.editRule.emit(rule);
  }

  onDeleteRule(ruleId: number) {
    this.deleteRule.emit(ruleId);
  }

  onToggleRule(ruleId: number) {
    this.toggleRule.emit(ruleId);
  }

  getTemplateIcon(template: string): string {
    switch (template) {
      case 'vip': return 'star';
      case 'loyal': return 'favorite';
      case 'young': return 'emoji_people';
      case 'new': return 'new_releases';
      default: return 'code';
    }
  }

  getTemplateColor(template: string): string {
    switch (template) {
      case 'vip': return 'primary';
      case 'loyal': return 'accent';
      case 'young': return 'warn';
      case 'new': return 'primary';
      default: return '';
    }
  }

  formatDate(date: Date | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}
