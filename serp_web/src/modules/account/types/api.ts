/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - API request and response DTOs
 */

import type { ApiResponse } from '@/lib';

import type { User } from './auth';
import type {
  FeatureAccess,
  MenuAccess,
  ModuleAccess,
  OrganizationPermission,
} from './permissions';

// Request DTOs
export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  organization: {
    name: string;
  };
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

export interface ConfirmPasswordResetRequest {
  token: string;
  newPassword: string;
}

// Response Data DTOs
export interface TokenData {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  refreshExpiresIn: number;
}

export interface PermissionsData {
  roles: string[];
  permissions: string[];
  features: FeatureAccess[];
  organizationPermissions?: OrganizationPermission[];
}

export interface MenusData {
  menus: MenuAccess[];
  modules: ModuleAccess[];
}

export interface PasswordResetValidationData {
  valid: boolean;
  expiresAt: number | string;
  userId: number;
}

export type AuthResponse = ApiResponse<TokenData>;
export type TokenResponse = ApiResponse<TokenData>;
export type UserProfileResponse = ApiResponse<User>;
export type PermissionsResponse = ApiResponse<PermissionsData>;
export type MenusResponse = ApiResponse<MenusData>;
export type ValidatePasswordResetTokenResponse =
  ApiResponse<PasswordResetValidationData>;
export type ConfirmPasswordResetResponse = ApiResponse<string>;
