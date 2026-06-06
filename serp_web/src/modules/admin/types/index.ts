/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin types barrel export
 */

// Organization types
export type {
  OrganizationStatus,
  OrganizationType,
  BillingCycle,
  Organization,
  OrganizationStatusUpdateResponse,
  OrganizationFilters,
  OrganizationsResponse,
  OrganizationResponse,
} from './organization.types';

// Subscription types
export type {
  SubscriptionStatus,
  OrganizationSubscription,
  SubscriptionsResponse,
  SubscriptionResponse,
  SubscriptionFilters,
} from './subscriptions.types';

// Plan types
export type {
  LicenseType,
  PlanModule,
  SubscriptionPlan,
  AddModuleToPlanRequest,
  PlansResponse,
  PlanResponse,
  PlanModulesResponse,
} from './plans.types';

// Module types
export type {
  ModuleStatus,
  ModuleType,
  PricingModel,
  Module,
  ModuleFilters,
  ModulesResponse,
  ModuleResponse,
} from './module.types';

// User types
export type {
  UserStatus,
  UserType,
  UserProfile,
  UserStats,
  UserDetailResponse,
  UserDetailRole,
  UserDetailDepartment,
  UserDetailModuleAccess,
  DepartmentOption,
  UserFilters,
  UsersResponse,
  UserResponse,
  UserStatsResponse,
  UserDetailApiResponse,
  UpdateUserInfoRequest,
  CreateUserForOrganizationRequest,
} from './user.types';

// Role types
export type {
  RoleScope,
  RoleType,
  Permission,
  MenuDisplay,
  Role,
  CreateRoleRequest,
  UpdateRoleRequest,
  AddPermissionToRoleRequest,
  RoleFilters,
  RolesResponse,
  RoleResponse,
} from './role.types';

// Stats types
export type { AdminStats } from './stats.types';

// Dashboard types
export type {
  AdminDashboard,
  AdminDashboardActionQueue,
  AdminDashboardConfigurationCoverage,
  AdminDashboardMetrics,
  AdminDashboardRecentOrganization,
  AdminDashboardStatusCount,
} from './dashboard.types';

// Menu Display types
export type {
  MenuType,
  RoleInfo as MenuRoleInfo,
  MenuDisplayDetail,
  MenuDisplayTreeNode,
  CreateMenuDisplayRequest,
  UpdateMenuDisplayRequest,
  AssignMenuDisplayToRoleRequest,
  GetMenuDisplayParams,
  MenuDisplayResponse,
  AssignRoleResponse,
  MenuDisplayFilters,
  MenuDisplayStats,
} from './menu-display.types';
