// API Response Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type {
  Customer,
  Lead,
  Opportunity,
  Activity,
  CRMMetrics,
  PipelineMetrics,
  SalesMetrics,
  LeadSourceMetrics,
  PaginatedResponse,
} from './index';

// Standard API Response wrapper
export interface APIResponse<T = any> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export interface APIError {
  success: false;
  error: {
    code: string;
    message: string;
    details?: Record<string, any>;
  };
  timestamp: string;
}

// Specific API Response types
export type CustomerResponse = APIResponse<Customer>;
export type CustomersResponse = APIResponse<PaginatedResponse<Customer>>;
export type CreateCustomerResponse = APIResponse<Customer>;
export type UpdateCustomerResponse = APIResponse<Customer>;
export type DeleteCustomerResponse = APIResponse<{ deleted: boolean }>;

export type LeadResponse = APIResponse<Lead>;
export type LeadsResponse = APIResponse<PaginatedResponse<Lead>>;
export type CreateLeadResponse = APIResponse<Lead>;
export type UpdateLeadResponse = APIResponse<Lead>;
export type ConvertLeadResponse = APIResponse<{
  customer: Customer;
  opportunity?: Opportunity;
}>;
export type DeleteLeadResponse = APIResponse<{ deleted: boolean }>;

export type OpportunityResponse = APIResponse<Opportunity>;
export type OpportunitiesResponse = APIResponse<PaginatedResponse<Opportunity>>;
export type CreateOpportunityResponse = APIResponse<Opportunity>;
export type UpdateOpportunityResponse = APIResponse<Opportunity>;
export type DeleteOpportunityResponse = APIResponse<{ deleted: boolean }>;

export type ActivityResponse = APIResponse<Activity>;
export type ActivitiesResponse = APIResponse<PaginatedResponse<Activity>>;
export type CreateActivityResponse = APIResponse<Activity>;
export type UpdateActivityResponse = APIResponse<Activity>;
export type DeleteActivityResponse = APIResponse<{ deleted: boolean }>;

// Analytics API responses
export type CRMMetricsResponse = APIResponse<CRMMetrics>;
export type PipelineMetricsResponse = APIResponse<PipelineMetrics[]>;
export type SalesMetricsResponse = APIResponse<SalesMetrics[]>;
export type LeadSourceMetricsResponse = APIResponse<LeadSourceMetrics[]>;

// Bulk operations
export interface BulkOperation {
  action: 'delete' | 'update' | 'export';
  ids: string[];
  updateData?: Record<string, any>;
}

export interface BulkOperationResult {
  successCount: number;
  failureCount: number;
  errors: Array<{
    id: string;
    error: string;
  }>;
}

export type BulkOperationResponse = APIResponse<BulkOperationResult>;

// Import/Export types
export interface ImportRequest {
  file: File;
  mapping: Record<string, string>; // CSV column -> entity field mapping
  skipDuplicates: boolean;
  updateExisting: boolean;
}

export interface ImportResult {
  totalRows: number;
  successCount: number;
  failureCount: number;
  skippedCount: number;
  errors: Array<{
    row: number;
    error: string;
  }>;
}

export type ImportResponse = APIResponse<ImportResult>;

export interface ExportRequest {
  format: 'csv' | 'xlsx' | 'pdf';
  filters?: Record<string, any>;
  fields?: string[];
}

export interface ExportResult {
  downloadUrl: string;
  filename: string;
  size: number;
}

export type ExportResponse = APIResponse<ExportResult>;
