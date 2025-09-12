/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - API request and response DTOs
 */

// Request DTOs
export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  organizationId?: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RevokeTokenRequest {
  refreshToken: string;
}

// Response DTOs
export interface AuthResponse {
  code: number;
  status: string;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    refreshExpiresIn: number;
  };
}

export interface TokenResponse {
  code: number;
  status: string;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    refreshExpiresIn: number;
  };
}

export interface UserProfileResponse {
  code: number;
  status: string;
  message: string;
  data: User;
}

export interface PermissionsResponse {
  code: number;
  status: string;
  message: string;
  data: {
    roles: string[];
    permissions: string[];
    features: FeatureAccess[];
    organizationPermissions?: OrganizationPermission[];
  };
}

export interface MenusResponse {
  code: number;
  status: string;
  message: string;
  data: {
    menus: MenuAccess[];
    modules: ModuleAccess[];
  };
}

import type { User } from './auth';
import type {
  FeatureAccess,
  OrganizationPermission,
  MenuAccess,
  ModuleAccess,
} from './permissions';
