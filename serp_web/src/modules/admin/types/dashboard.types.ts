/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin dashboard types
 */

export interface AdminDashboardMetrics {
  totalOrganizations: number;
  activeOrganizations: number;
  suspendedOrganizations: number;
  expiredOrganizations: number;
  totalUsers: number;
  activeUsers: number;
  suspendedUsers: number;
  totalSubscriptions: number;
  activeSubscriptions: number;
  trialSubscriptions: number;
  pendingSubscriptions: number;
  expiredSubscriptions: number;
}

export interface AdminDashboardActionQueue {
  pendingSubscriptions: number;
  subscriptionsEndingSoon: number;
  trialsEndingSoon: number;
  suspendedOrganizations: number;
  expiredOrganizations: number;
}

export interface AdminDashboardRecentOrganization {
  id: number;
  name: string;
  code: string;
  status: string | null;
  userCount: number;
  subscriptionStatus: string;
  createdAt: number | null;
}

export interface AdminDashboardStatusCount {
  status: string;
  count: number;
}

export interface AdminDashboardConfigurationCoverage {
  totalPlans: number;
  activePlans: number;
  inactivePlans: number;
  totalModules: number;
  availableModules: number;
  unavailableModules: number;
  totalMenuDisplays: number;
  visibleMenuDisplays: number;
  hiddenMenuDisplays: number;
}

export interface AdminDashboard {
  metrics: AdminDashboardMetrics;
  actionQueue: AdminDashboardActionQueue;
  recentOrganizations: AdminDashboardRecentOrganization[];
  organizationStatuses: AdminDashboardStatusCount[];
  subscriptionStatuses: AdminDashboardStatusCount[];
  configurationCoverage: AdminDashboardConfigurationCoverage;
  generatedAt: number;
}
