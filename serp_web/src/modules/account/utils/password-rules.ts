/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Shared password strength helpers for account flows
 */

export interface PasswordRules {
  minLength: boolean;
  hasUppercase: boolean;
  hasLowercase: boolean;
  hasNumber: boolean;
}

export const PASSWORD_REQUIREMENTS: ReadonlyArray<{
  key: keyof PasswordRules;
  label: string;
}> = [
  { key: 'minLength', label: 'At least 8 characters' },
  { key: 'hasUppercase', label: 'One uppercase letter' },
  { key: 'hasLowercase', label: 'One lowercase letter' },
  { key: 'hasNumber', label: 'One number' },
];

export function evaluatePassword(value: string): PasswordRules {
  return {
    minLength: value.length >= 8,
    hasUppercase: /[A-Z]/.test(value),
    hasLowercase: /[a-z]/.test(value),
    hasNumber: /\d/.test(value),
  };
}

export function getPasswordStrengthScore(rules: PasswordRules): number {
  return Object.values(rules).filter(Boolean).length;
}
