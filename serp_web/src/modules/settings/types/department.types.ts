/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Department types
 */

export interface Department {
  id: string;
  name: string;
  description?: string;
  parentDepartmentId?: string;
  parentDepartmentName?: string;
  managerId?: string;
  managerName?: string;
  memberCount: number;
  status: DepartmentStatus;
  createdAt: string;
  updatedAt: string;
}

export type DepartmentStatus = 'ACTIVE' | 'INACTIVE';

export interface DepartmentMember {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  avatarUrl?: string;
  position?: string;
  joinedAt: string;
}

export interface CreateDepartmentRequest {
  name: string;
  description?: string;
  parentDepartmentId?: string;
  managerId?: string;
}

export interface UpdateDepartmentRequest {
  name?: string;
  description?: string;
  parentDepartmentId?: string;
  managerId?: string;
  status?: DepartmentStatus;
}

export interface AddDepartmentMemberRequest {
  userId: string;
}

export interface DepartmentFilters {
  search?: string;
  status?: DepartmentStatus;
  parentDepartmentId?: string;
}
