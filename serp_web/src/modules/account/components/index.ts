/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication components exports
 */

export { LoginForm } from './LoginForm';
export { RegisterForm } from './RegisterForm';
export { AuthLayout } from './AuthLayout';
export { ProtectedRoute, withAuth } from './ProtectedRoute';
export { UserProfile, UserProfileCompact } from './UserProfile';
export { RoleGuard, withRoleGuard, type RoleGuardProps } from './RoleGuard';
export {
  AccessDenied,
  AuthenticationRequired,
  InsufficientPermissions,
  InsufficientRole,
  OrganizationAccessDenied,
  FeatureUnavailable,
  MaintenanceMode,
  type AccessDeniedProps,
  type AccessDeniedReason,
} from './AccessDenied';
