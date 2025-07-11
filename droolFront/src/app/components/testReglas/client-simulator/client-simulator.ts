import { ChangeDetectionStrategy, Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { SimulationResults } from '../simulation-results/simulation-results';
import { Drools } from '../../../services/drools';
import { Notification } from '../../../services/notification';
import { Customer } from '../../../models/customer';
import { ExecutionResult, Scenario } from '../../../models/rule';

@Component({
  selector: 'app-client-simulator',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTabsModule,
    SimulationResults],
  templateUrl: './client-simulator.html',
  styleUrl: './client-simulator.css'
})
export class ClientSimulator implements OnInit {
  testCustomer: Customer = {
    name: '',
    email: '',
    age: 30,
    totalPurchases: 5000,
    loyaltyPoints: 500,
    daysSinceRegistration: 30,
    isActive: true
  };

  lastResult: ExecutionResult | null = null;
  scenarios: Scenario[] = [];
  loading = false;
  scenarioName = '';

  constructor(
    private droolsService: Drools,
    private notificationService: Notification,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadScenarios();
  }

  loadScenarios() {
    this.droolsService.getScenarios().subscribe({
      next: (scenarios) => {
        this.scenarios = scenarios;
      },
      error: (error) => {
        console.error('Error loading scenarios:', error);
      }
    });
  }

  simulateCustomer() {
    if (!this.testCustomer.name.trim()) {
      this.notificationService.warning('El nombre del cliente es requerido');
      return;
    }

    if (!this.testCustomer.email.trim()) {
      this.notificationService.warning('El email del cliente es requerido');
      return;
    }

    this.loading = true;

    this.droolsService.executeRules(this.testCustomer).subscribe({
      next: (result) => {
        console.log('Setting lastResult to:', result);
        this.loading = false;
        this.lastResult = result;
        this.cdr.detectChanges();
        console.log('lastResult después de asignar:', this.lastResult);
        this.notificationService.success('Simulación ejecutada exitosamente');
      },
      error: (error) => {
        console.log('Error lastResult to:', error);
        this.loading = false;
        this.cdr.detectChanges();
        this.notificationService.error('Error en la simulación');
        console.error('Error executing rules:', error);
      }
    });
  }

  generateRandomCustomer() {
    this.testCustomer = this.droolsService.generateRandomCustomer();
    this.notificationService.info('Cliente aleatorio generado');
  }

  saveScenario() {
    if (!this.lastResult) {
      this.notificationService.warning('Primero ejecuta una simulación');
      return;
    }

    if (!this.scenarioName.trim()) {
      this.scenarioName = `Escenario ${this.testCustomer.name} - ${new Date().toLocaleString()}`;
    }

    const scenario = {
      name: this.scenarioName,
      customer: this.testCustomer,
      expectedResults: this.lastResult
    };

    this.droolsService.saveScenario(scenario).subscribe({
      next: (savedScenario) => {
        this.notificationService.success('Escenario guardado exitosamente');
        this.scenarioName = '';
        this.loadScenarios();
      },
      error: (error) => {
        this.notificationService.error('Error al guardar el escenario');
        console.error('Error saving scenario:', error);
      }
    });
  }

  loadScenario(scenario: Scenario) {
    this.testCustomer = { ...scenario.customer };
    this.simulateCustomer();
  }

  deleteScenario(scenarioId: number) {
    if (confirm('¿Estás seguro de que quieres eliminar este escenario?')) {
      this.droolsService.deleteScenario(scenarioId).subscribe({
        next: () => {
          this.notificationService.success('Escenario eliminado');
          this.loadScenarios();
        },
        error: (error) => {
          this.notificationService.error('Error al eliminar el escenario');
          console.error('Error deleting scenario:', error);
        }
      });
    }
  }

  resetForm() {
    this.testCustomer = {
      name: '',
      email: '',
      age: 30,
      totalPurchases: 5000,
      loyaltyPoints: 500,
      daysSinceRegistration: 30,
      isActive: true
    };
    this.lastResult = null;
    this.scenarioName = '';
  }

}
