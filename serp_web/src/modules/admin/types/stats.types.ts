/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin stats types
 */

// Stats & Analytics
export interface AdminStats {
  totalOrganizations: number;
  activeOrganizations: number;
  suspendedOrganizations: number;
  totalSubscriptions: number;
  activeSubscriptions: number;
  trialSubscriptions: number;
  pendingSubscriptions: number;
  expiredSubscriptions: number;
  totalRevenue: number;
  mrr: number; // Monthly Recurring Revenue
  arr: number; // Annual Recurring Revenue
  totalUsers: number;
  activeUsers: number;
}
