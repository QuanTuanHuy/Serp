/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Integration types
 */

export interface Integration {
  id: string;
  name: string;
  provider: IntegrationProvider;
  description?: string;
  logoUrl?: string;
  category: IntegrationCategory;
  status: IntegrationStatus;
  isEnabled: boolean;
  config?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export type IntegrationProvider =
  | 'SLACK'
  | 'GOOGLE_WORKSPACE'
  | 'MICROSOFT_365'
  | 'SALESFORCE'
  | 'HUBSPOT'
  | 'ZENDESK'
  | 'JIRA'
  | 'GITHUB'
  | 'GITLAB'
  | 'STRIPE'
  | 'PAYPAL'
  | 'CUSTOM';

export type IntegrationCategory =
  | 'COMMUNICATION'
  | 'PRODUCTIVITY'
  | 'CRM'
  | 'SUPPORT'
  | 'DEV_TOOLS'
  | 'PAYMENT'
  | 'OTHER';

export type IntegrationStatus = 'ACTIVE' | 'INACTIVE' | 'ERROR';

export interface AvailableIntegration {
  provider: IntegrationProvider;
  name: string;
  description: string;
  logoUrl: string;
  category: IntegrationCategory;
  features: string[];
  isConfigured: boolean;
}

export interface ConfigureIntegrationRequest {
  provider: IntegrationProvider;
  config: Record<string, any>;
}

export interface UpdateIntegrationRequest {
  config?: Record<string, any>;
  isEnabled?: boolean;
}
