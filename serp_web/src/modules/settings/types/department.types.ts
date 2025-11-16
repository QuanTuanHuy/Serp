/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Department types
 */

export interface Department {
  id: number;
  organizationId: number;
  name: string;
  code?: string;
  description?: string;
  parentDepartmentId?: number;
  parentDepartmentName?: string;
  managerId?: number;
  managerName?: string;
  defaultModuleIds?: number[];
  defaultRoleIds?: number[];
  isActive: boolean;
  childrenCount?: number;
  memberCount?: number;
  createdAt: number;
  updatedAt: number;
}

export interface DepartmentMember {
  userId: number;
  userName: string;
  email: string;
  phoneNumber?: string;
  departmentId: number;
  isPrimary: boolean;
  jobTitle?: string;
  description?: string;
  isActive: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface DepartmentStatistics {
  totalDepartments: number;
  totalMembers: number;
  averageTeamSize: number;
  departmentsWithManagers: number;
  activeDepartments: number;
  inactiveDepartments: number;
}

// Request types
export interface CreateDepartmentRequest {
  name: string;
  description?: string;
  parentDepartmentId?: number;
  managerId?: number;
  defaultModuleIds?: number[];
  defaultRoleIds?: number[];
}

export interface UpdateDepartmentRequest {
  name?: string;
  description?: string;
  parentDepartmentId?: number;
  managerId?: number;
  defaultModuleIds?: number[];
  defaultRoleIds?: number[];
  isActive?: boolean;
}

export interface AssignUserToDepartmentRequest {
  userId: number;
  jobTitle?: string;
  isPrimary?: boolean;
}

export interface BulkAssignUsersToDepartmentRequest {
  userIds: number[];
  jobTitle?: string;
}

// Query params
export interface GetDepartmentParams {
  organizationId: number;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDir?: string;
  search?: string;
  parentDepartmentId?: number;
  isActive?: boolean;
  managerId?: number;
}

export type DepartmentStatus = 'ACTIVE' | 'INACTIVE';
