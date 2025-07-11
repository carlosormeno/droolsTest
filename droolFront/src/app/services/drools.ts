import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { catchError, tap, timeout } from 'rxjs/operators';
import { Customer } from '../models/customer';
import { Rule, Scenario, ExecutionResult } from '../models/rule';

@Injectable({
  providedIn: 'root'
})
export class Drools {
  //private baseUrl = '/api'; // Usa el proxy de Nginx en producción
  //private baseUrl = 'http://localhost:8080/api'; // URL directa para desarrollo
  private baseUrl = 'http://172.28.138.56:8080/api'; // IP de WSL desde Windows
  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  // Estado local para escenarios (no hay endpoint en backend)
  private scenariosSubject = new BehaviorSubject<Scenario[]>([]);

  constructor(private http: HttpClient) {}

  // Getter para escenarios
  get scenarios$(): Observable<Scenario[]> {
    return this.scenariosSubject.asObservable();
  }

  // =============================================================
  // MÉTODOS DE REGLAS - CONSUMEN DEL BACKEND REAL
  // =============================================================

  getRules(): Observable<Rule[]> {
    console.log('Fetching rules from backend...');
    return this.http.get<Rule[]>(`${this.baseUrl}/rules`).pipe(
      catchError(error => {
        //console.error('Error fetching rules:', error);
        console.warn('Backend not available for rules, using defaults:', error);
        // Fallback a reglas por defecto solo en caso de error
        return of(this.getDefaultRules());
      })
    );
  }

  createRule(rule: Omit<Rule, 'id'>): Observable<Rule> {
    console.log('Entramos a crear una regla en el backend...');
    return this.http.post<Rule>(`${this.baseUrl}/rules`, rule, this.httpOptions).pipe(
      catchError(error => {
        console.error('Error creating rule:', error);
        // Fallback para desarrollo
        const newRule: Rule = {
          ...rule,
          id: Date.now(),
          createdAt: new Date(),
          updatedAt: new Date()
        };
        return of(newRule);
      })
    );
  }

  updateRule(rule: Rule): Observable<Rule> {
    console.log('Entramos a actualizar una regla en el backend...');
    return this.http.put<Rule>(`${this.baseUrl}/rules/${rule.id}`, rule, this.httpOptions).pipe(
      catchError(error => {
        console.error('Error updating rule:', error);
        rule.updatedAt = new Date();
        return of(rule);
      })
    );
  }

  deleteRule(id: number): Observable<void> {
    console.log('Entramos a borrar una regla en el backend...');
    return this.http.delete<void>(`${this.baseUrl}/rules/${id}`).pipe(
      catchError(error => {
        console.error('Error deleting rule:', error);
        return of();
      })
    );
  }

  toggleRule(id: number): Observable<Rule> {
    return this.http.patch<Rule>(`${this.baseUrl}/rules/${id}/toggle`, {}, this.httpOptions).pipe(
      catchError(error => {
        console.error('Error toggling rule:', error);
        // Fallback para desarrollo
        const mockRule: Rule = {
          id,
          name: 'Mock Rule',
          description: 'Mock Description',
          template: 'custom',
          content: 'mock content',
          active: true,
          updatedAt: new Date()
        };
        return of(mockRule);
      })
    );
  }

  // =============================================================
  // MÉTODOS DE SIMULACIÓN - CONSUMEN DEL BACKEND REAL
  // =============================================================

  executeRules(customer: Customer): Observable<ExecutionResult> {
    console.log('Executing rules for customer:', customer);
    return this.http.post<ExecutionResult>(`${this.baseUrl}/rules/execute`, customer, this.httpOptions).pipe(
      timeout(8000), // 8 segundos de timeout
      tap(result => console.log('Backend response received:', result)),
      catchError(error => {
        console.warn('Backend error or timeout, using fallback simulation:', error);
        const fallbackResult = this.simulateRulesExecution(customer);
        console.log('Fallback result:', fallbackResult);
        // Fallback para desarrollo
        //return of(this.simulateRulesExecution(customer));
        return of(fallbackResult);
      })
    );
  }

  // =============================================================
  // MÉTODOS DE ESCENARIOS - LOCALES (NO HAY ENDPOINT EN BACKEND)
  // =============================================================

  getScenarios(): Observable<Scenario[]> {
    return this.scenarios$;
  }

  saveScenario(scenario: Omit<Scenario, 'id' | 'createdAt'>): Observable<Scenario> {
    const newScenario: Scenario = {
      ...scenario,
      id: Date.now(),
      createdAt: new Date()
    };

    const currentScenarios = this.scenariosSubject.value;
    this.scenariosSubject.next([...currentScenarios, newScenario]);

    return of(newScenario);
  }

  deleteScenario(id: number): Observable<void> {
    const currentScenarios = this.scenariosSubject.value;
    const filteredScenarios = currentScenarios.filter(s => s.id !== id);
    this.scenariosSubject.next(filteredScenarios);

    return of();
  }

  // =============================================================
  // PLANTILLAS Y UTILIDADES
  // =============================================================

  getTemplates(): { [key: string]: string } {
    return {
      vip: `rule "Cliente VIP"
when
    $customer : Customer(totalPurchases > 10000)
then
    $customer.setVipCustomer(true);
    $customer.setCategory("VIP");
    $customer.setDiscount("25% descuento VIP");
end`,
      loyal: `rule "Cliente Leal"
when
    $customer : Customer(loyaltyPoints >= 1000)
then
    $customer.setLoyalCustomer(true);
end`,
      young: `rule "Cliente Joven"
when
    $customer : Customer(age < 30)
then
    $customer.setYoungCustomer(true);
    $customer.setDiscount("10% descuento jóvenes");
end`,
      new: `rule "Cliente Nuevo"
when
    $customer : Customer(daysSinceRegistration == 0)
then
    $customer.setNewCustomer(true);
    $customer.setRecommendation("¡Bienvenido! Descubre nuestros productos");
end`,
      custom: `rule "Mi Regla Personalizada"
when
    $customer : Customer(/* condición */)
then
    /* acciones */
end`
    };
  }

  generateRandomCustomer(): Customer {
    const names = ['Ana García', 'Carlos López', 'María Rodríguez', 'Juan Pérez', 'Laura Martín'];
    const domains = ['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com'];

    const name = names[Math.floor(Math.random() * names.length)];
    const email = `${name.toLowerCase().replace(' ', '.')}@${domains[Math.floor(Math.random() * domains.length)]}`;

    return {
      name,
      email,
      age: Math.floor(Math.random() * 60) + 18,
      totalPurchases: Math.floor(Math.random() * 50000),
      loyaltyPoints: Math.floor(Math.random() * 5000),
      daysSinceRegistration: Math.floor(Math.random() * 365),
      isActive: Math.random() > 0.2
    };
  }

  // =============================================================
  // MÉTODOS PRIVADOS DE FALLBACK
  // =============================================================

  private getDefaultRules(): Rule[] {
    const templates = this.getTemplates();
    return [
      {
        id: 1,
        name: 'Cliente VIP',
        description: 'Clasifica clientes con compras superiores a $10,000',
        template: 'vip',
        content: templates['vip'],
        active: true
      },
      {
        id: 2,
        name: 'Cliente Leal',
        description: 'Identifica clientes con 1000+ puntos de lealtad',
        template: 'loyal',
        content: templates['loyal'],
        active: true
      },
      {
        id: 3,
        name: 'Cliente Joven',
        description: 'Segmenta clientes menores de 30 años',
        template: 'young',
        content: templates['young'],
        active: true
      },
      {
        id: 4,
        name: 'Cliente Nuevo',
        description: 'Identifica clientes recién registrados',
        template: 'new',
        content: templates['new'],
        active: false
      }
    ];
  }

  private simulateRulesExecution(customer: Customer): ExecutionResult {
    const startTime = Date.now();
    const executedRules: string[] = [];
    //const processedCustomer = { ...customer };

    // Simular ejecución de reglas por defecto
    let vipCustomer = false;
    let loyalCustomer = false;
    let youngCustomer = false;
    let newCustomer = false;
    let discount: string | undefined = undefined;
    let recommendation: string | undefined = undefined;

    /* Simular ejecución de reglas por defecto
    if (customer.totalPurchases > 10000) {
      processedCustomer.vipCustomer = true;
      processedCustomer.category = 'VIP';
      processedCustomer.discount = '25% descuento VIP';
      executedRules.push('Cliente VIP');
    }

    if (customer.loyaltyPoints >= 1000) {
      processedCustomer.loyalCustomer = true;
      executedRules.push('Cliente Leal');
    }

    if (customer.age < 30) {
      processedCustomer.youngCustomer = true;
      if (!processedCustomer.discount) {
        processedCustomer.discount = '10% descuento jóvenes';
      }
      executedRules.push('Cliente Joven');
    }

    if (customer.daysSinceRegistration === 0) {
      processedCustomer.newCustomer = true;
      processedCustomer.recommendation = '¡Bienvenido! Descubre nuestros productos';
      executedRules.push('Cliente Nuevo');
    }*/

        if (customer.totalPurchases > 10000) {
    vipCustomer = true;
    discount = '25% descuento VIP';
    executedRules.push('Cliente VIP');
  }

  if (customer.loyaltyPoints >= 1000) {
    loyalCustomer = true;
    executedRules.push('Cliente Leal');
  }

  if (customer.age < 30) {
    youngCustomer = true;
    if (!discount) {
      discount = '10% descuento jóvenes';
    }
    executedRules.push('Cliente Joven');
  }

  if (customer.daysSinceRegistration === 0) {
    newCustomer = true;
    recommendation = '¡Bienvenido! Descubre nuestros productos';
    executedRules.push('Cliente Nuevo');
  }

    return {
      //customer: processedCustomer,

      // Datos del cliente
    name: customer.name,
    email: customer.email,
    age: customer.age,
    totalPurchases: customer.totalPurchases,
    loyaltyPoints: customer.loyaltyPoints,
    daysSinceRegistration: customer.daysSinceRegistration,
    isActive: customer.isActive,

    // Clasificaciones
    vipCustomer,
    loyalCustomer,
    youngCustomer,
    newCustomer,

    // Beneficios
    discount,
    recommendation,

    // Metadatos
    executedRules,
    executionTime: Date.now() - startTime,
    success: true
    };
  }
}
