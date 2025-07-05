export interface Customer {
  name: string;
  email: string;
  age: number;
  totalPurchases: number;
  loyaltyPoints: number;
  daysSinceRegistration: number;
  isActive: boolean;

  // Campos de clasificación (resultado de reglas)
  vipCustomer?: boolean;
  loyalCustomer?: boolean;
  youngCustomer?: boolean;
  newCustomer?: boolean;

  // Campos adicionales
  category?: string;
  discount?: string;
  recommendation?: string;
}
