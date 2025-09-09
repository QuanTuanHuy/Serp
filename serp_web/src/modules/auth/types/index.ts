/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication related types
 */

// User entity
export interface User {
  id: number;
  email: string;
  fullName: string;
  role: string;
  organizationId?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Auth state
export interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

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

// User profile response
export interface UserProfileResponse {
  code: number;
  status: string;
  message: string;
  data: User;
}

// Form validation types
export interface LoginFormData {
  email: string;
  password: string;
}

export interface RegisterFormData {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  organizationId?: number;
}

// Auth error types
export interface AuthError {
  field?: string;
  message: string;
  code?: string;
}
