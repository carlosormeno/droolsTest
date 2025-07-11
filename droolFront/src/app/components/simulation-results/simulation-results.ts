import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { ExecutionResult } from '../../models/rule';

@Component({
  selector: 'app-simulation-results',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule
  ],
  templateUrl: './simulation-results.html',
  styleUrl: './simulation-results.css'
})
export class SimulationResults implements OnInit {
  @Input() result: ExecutionResult | null = null;
  @Input() scenarioName: string = '';
  @Output() saveScenario = new EventEmitter<void>();
  @Output() scenarioNameChange = new EventEmitter<string>();

  ngOnInit() {
  console.log('SimulationResults received:', this.result);
  }

  onScenarioNameChange(name: string) {
    this.scenarioNameChange.emit(name);
  }

  ngOnChanges(changes: SimpleChanges) {
    console.log('SimulationResults ngOnChanges:', changes);
    if (changes['result']) {
      console.log('New result received:', changes['result'].currentValue);
      // Forzar detección de cambios
      this.result = changes['result'].currentValue;
    }
  }

  onSaveScenario() {
    this.saveScenario.emit();
  }

  getClassificationCount(): number {
    //if (!this.result?.customer) return 0;
    if (!this.result) return 0;

    let count = 0;
    //if (this.result.customer.vipCustomer) count++;
    //if (this.result.customer.loyalCustomer) count++;
    //if (this.result.customer.youngCustomer) count++;
    //if (this.result.customer.newCustomer) count++;

    if (this.result.vipCustomer) count++;
    if (this.result.loyalCustomer) count++;
    if (this.result.youngCustomer) count++;
    if (this.result.newCustomer) count++;

    return count;
  }

  getClassificationText(): string {
    const count = this.getClassificationCount();
    if (count === 0) return 'Sin clasificaciones';
    if (count === 1) return '1 clasificación';
    return `${count} clasificaciones`;
  }
}
