// Form Components Exports (authors: QuanTuanHuy, Description: Part of Serp Project)

export { CustomerForm } from './CustomerForm';
export { CustomerForm as AccountForm } from './CustomerForm';
export { LeadForm } from './LeadForm';
export { OpportunityForm } from './OpportunityForm';
export { ActivityForm } from './ActivityForm';

// Re-export types for convenience
export type {
  Customer,
  Account,
  Lead,
  Opportunity,
  Activity,
  CreateCustomerRequest,
  CreateAccountRequest,
  CreateLeadRequest,
  CreateOpportunityRequest,
  CreateActivityRequest,
  UpdateCustomerRequest,
  UpdateAccountRequest,
  UpdateLeadRequest,
  UpdateOpportunityRequest,
  UpdateActivityRequest,
} from '../../types';
