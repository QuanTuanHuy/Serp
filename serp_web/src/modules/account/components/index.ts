/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication components exports
 */

export { LoginForm } from './LoginForm';
export { RegisterForm } from './RegisterForm';
export { AuthLayout } from './AuthLayout';
export { ProtectedRoute, withAuth } from './ProtectedRoute';
export { UserProfile, UserProfileCompact } from './UserProfile';
export { EditProfileForm } from './EditProfileForm';
export { ChangePasswordForm } from './ChangePasswordForm';
export { ProfileSidebar } from './layout/ProfileSidebar';
export { ProfileLayout } from './layout/ProfileLayout';
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
export { ModuleCard } from './ModuleCard';
export { ModuleShowcase } from './ModuleShowcase';
