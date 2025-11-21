/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Form validation types
 */

// Form data types
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
  organizationName: string;
}

// Profile update types
export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  avatarUrl?: string;
  timezone?: string;
  preferredLanguage?: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// Validation error types
export interface FormFieldError {
  field: string;
  message: string;
}

export interface FormValidationResult {
  isValid: boolean;
  errors: FormFieldError[];
}
