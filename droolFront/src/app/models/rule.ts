export interface Rule {
  id: number;
  name: string;
  description: string;
  template: string;
  content: string;
  active: boolean;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface Scenario {
  id: number;
  name: string;
  customer: import('./customer').Customer;
  expectedResults?: any;
  createdAt: Date;
}

export interface ExecutionResult {
  // Campos del cliente directo (como viene del backend)
  id?: number;
  name: string;
  email: string;
  age: number;
  totalPurchases: number;
  loyaltyPoints: number;
  daysSinceRegistration: number;
  isActive: boolean;

  // Campos de clasificación
  vipCustomer?: boolean;
  loyalCustomer?: boolean;
  youngCustomer?: boolean;
  newCustomer?: boolean;

  // Campos adicionales del backend
  category?: string;
  discount?: string;
  recommendation?: string;
  registrationDate?: string;

  // Campos de ejecución
  executedRules?: string[];
  executionTime?: number;
  success?: boolean;
  errors?: string[];

  //customer: import('./customer').Customer;
  //executedRules: string[];
  //executionTime: number;
  //success: boolean;
  //errors?: string[];
}
