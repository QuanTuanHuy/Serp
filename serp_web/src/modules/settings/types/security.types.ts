/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Security settings types
 */

export interface SecuritySettings {
  twoFactorEnabled: boolean;
  twoFactorEnforced: boolean;
  passwordPolicy: PasswordPolicy;
  sessionTimeout: number;
  ipWhitelist: string[];
  allowedDomains: string[];
}

export interface PasswordPolicy {
  minLength: number;
  requireUppercase: boolean;
  requireLowercase: boolean;
  requireNumbers: boolean;
  requireSpecialChars: boolean;
  expiryDays?: number;
  preventReuse: number;
}

export interface UpdateSecuritySettingsRequest {
  twoFactorEnabled?: boolean;
  twoFactorEnforced?: boolean;
  passwordPolicy?: Partial<PasswordPolicy>;
  sessionTimeout?: number;
  ipWhitelist?: string[];
  allowedDomains?: string[];
}

export interface AuditLog {
  id: string;
  userId: string;
  userEmail: string;
  userName: string;
  action: string;
  resourceType: string;
  resourceId?: string;
  details?: string;
  ipAddress: string;
  userAgent: string;
  timestamp: string;
}

export interface AuditLogFilters {
  userId?: string;
  action?: string;
  resourceType?: string;
  startDate?: string;
  endDate?: string;
}

export interface ApiKey {
  id: string;
  name: string;
  key: string;
  prefix: string;
  permissions: string[];
  expiresAt?: string;
  lastUsedAt?: string;
  status: ApiKeyStatus;
  createdAt: string;
  createdBy: string;
}

export type ApiKeyStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED';

export interface CreateApiKeyRequest {
  name: string;
  permissions: string[];
  expiresAt?: string;
}

export interface UpdateApiKeyRequest {
  name?: string;
  permissions?: string[];
  status?: ApiKeyStatus;
}
