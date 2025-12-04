// Shared CRM Components Exports (authors: QuanTuanHuy, Description: Part of Serp Project)

export { StatusBadge } from './StatusBadge';
export { PriorityBadge } from './PriorityBadge';
export { EntityCard } from './EntityCard';
export { SearchInput } from './SearchInput';
export { ActionMenu, EditAction, DeleteAction } from './ActionMenu';
export { ExportDropdown } from './ExportDropdown';

// Re-export types for convenience
export type {
  Customer,
  Lead,
  Opportunity,
  Activity,
  Priority,
  LeadStatus,
  OpportunityStage,
  ActivityStatus,
  CustomerStatus,
} from '../../types';

// Re-export export types
export type { ExportDropdownProps } from './ExportDropdown';
