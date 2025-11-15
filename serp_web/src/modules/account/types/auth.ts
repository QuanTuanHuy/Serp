/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User and authentication entities
 */

// User entity
export interface User {
  id: number;
  email: string;
  fullName: string;
  firstName: string;
  lastName: string;
  roles: string[];
  organizationId?: number;
  organizationName?: string;
  isActive?: boolean;
  createdAt: string;
  updatedAt: string;
}

// Auth state for Redux store - chỉ chứa authentication data
export interface AuthState {
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// Auth error types
export interface AuthError {
  field?: string;
  message: string;
  code?: string;
}
