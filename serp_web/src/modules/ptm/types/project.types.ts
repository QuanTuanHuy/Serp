/**
 * PTM v2 - Project Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Project domain types
 */

export type ProjectStatus =
  | 'NEW'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'ARCHIVED'
  | 'ON_HOLD';

export type ProjectPriority = 'LOW' | 'MEDIUM' | 'HIGH';

/**
 * Project entity matching backend ProjectResponse
 */
export interface Project {
  id: number;
  userId: number;
  tenantId: number;

  title: string;
  description?: string;

  status: ProjectStatus;
  priority: ProjectPriority;

  startDateMs?: number;
  deadlineMs?: number;
  progressPercentage: number;

  color?: string;
  icon?: string;
  isFavorite: boolean;

  activeStatus: 'ACTIVE' | 'INACTIVE';

  createdAt: string;
  updatedAt: string;

  // Optional stats (populated when includeStats=true in backend)
  totalTasks?: number;
  completedTasks?: number;
  estimatedHours?: number;
  actualHours?: number;

  // Computed fields from backend (optional)
  isOverdue?: boolean;
  deadlineRemainingMs?: number;
}

/**
 * Request to create a new project
 * Matches backend CreateProjectRequest
 * Backend defaults status to NEW, so status is not in create request
 */
export interface CreateProjectRequest {
  title: string;
  description?: string;
  priority: ProjectPriority;
  startDateMs?: number;
  deadlineMs?: number;
  color?: string;
  icon?: string;
  isFavorite?: boolean;
}

/**
 * Request to update an existing project
 * Matches backend UpdateProjectRequest
 */
export interface UpdateProjectRequest {
  id: number;
  title?: string;
  description?: string;
  priority?: ProjectPriority;
  status?: ProjectStatus;
  startDateMs?: number;
  deadlineMs?: number;
  color?: string;
  icon?: string;
  isFavorite?: boolean;
}

/**
 * Filter params for project list API
 */
export interface ProjectFilterParams {
  status?: ProjectStatus;
  priority?: ProjectPriority;
  page?: number;
  pageSize?: number;
}
