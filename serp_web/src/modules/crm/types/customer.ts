// Customer & Account Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type { BaseEntity } from './base';

export type CustomerType = 'PROSPECT' | 'CUSTOMER';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE';

/** Matches CRM `AccountTier` enum. */
export type AccountTier = 'STANDARD' | 'SILVER' | 'GOLD' | 'PLATINUM';

/** Matches CRM `PreferredTimeSlot` enum. */
export type PreferredTimeSlot = 'MORNING' | 'AFTERNOON';

/** Java `DayOfWeek` for account preferred contact days (same as team working hours). */
export type CrmDayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface Customer extends BaseEntity {
  name: string;
  email: string;
  phone?: string;
  address?: string;
  customerType: CustomerType;
  status: CustomerStatus;
  companyName?: string;
  taxNumber?: string;
  website?: string;
  notes?: string;
  assignedSalesRep?: string;
  tags: string[];
  customFields: Record<string, any>;
  totalValue: number;
  lastContactDate?: string;
  industry?: string;
  companySize?: string;
  tier?: AccountTier;
  preferredTimeSlots?: PreferredTimeSlot[];
  preferredDays?: CrmDayOfWeek[];
  language?: string;
  timezone?: string;
  creditLimit?: number;
  paymentTerms?: string;
  totalOpportunities?: number;
  wonOpportunities?: number;
}

export type AccountType = CustomerType;
export type AccountStatus = CustomerStatus;
export type Account = Customer;

// Filter types
export interface CustomerFilters {
  search?: string;
  status?: CustomerStatus[];
  type?: CustomerType[];
  assignedSalesRep?: string[];
  tags?: string[];
  createdDateFrom?: string;
  createdDateTo?: string;
  lastContactFrom?: string;
  lastContactTo?: string;
  minValue?: number;
  maxValue?: number;
}

export type AccountFilters = CustomerFilters;

// Request types
export type CreateCustomerRequest = Omit<
  Customer,
  | 'id'
  | 'createdAt'
  | 'updatedAt'
  | 'totalValue'
  | 'lastContactDate'
  | 'totalOpportunities'
  | 'wonOpportunities'
> & {
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
};
export type UpdateCustomerRequest = Partial<CreateCustomerRequest>;

export type CreateAccountRequest = CreateCustomerRequest;
export type UpdateAccountRequest = UpdateCustomerRequest;
