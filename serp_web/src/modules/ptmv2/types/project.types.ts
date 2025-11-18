/**
 * PTM v2 - Project Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Project domain types
 */

export type ProjectStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED' | 'ON_HOLD';
export type ProjectPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Project {
  id: string;
  userId: string;
  tenantId: string;

  title: string;
  description?: string;
  status: ProjectStatus;
  priority: ProjectPriority;

  startDateMs?: number;
  deadlineMs?: number;
  progressPercentage: number;

  color: string;
  icon?: string;
  isFavorite: boolean;

  // Computed stats
  totalTasks: number;
  completedTasks: number;
  estimatedHours: number;
  actualHours: number;

  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  title: string;
  description?: string;
  status?: ProjectStatus;
  priority?: ProjectPriority;
  startDateMs?: number;
  deadlineMs?: number;
  estimatedHours?: number;
  color?: string;
  icon?: string;
}

export interface UpdateProjectRequest {
  id: string;
  title?: string;
  description?: string;
  status?: ProjectStatus;
  priority?: ProjectPriority;
  startDateMs?: number;
  deadlineMs?: number;
  estimatedHours?: number;
  color?: string;
  icon?: string;
  isFavorite?: boolean;
}
