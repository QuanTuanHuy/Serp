/**
 * PTM v2 - Task Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task domain types
 */

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
export type TaskSource = 'manual' | 'template' | 'imported' | 'ai_generated';

export interface Task {
  id: number;
  userId: number;
  tenantId: number;
  projectId?: number;
  parentTaskId?: number;

  title: string;
  description?: string;
  priority: TaskPriority;
  status: TaskStatus;

  estimatedDurationHours: number;
  actualDurationHours?: number;

  preferredStartDateMs?: number;
  deadlineMs?: number;
  completedAt?: string;

  isDeepWork: boolean;
  category?: string;
  tags: string[];

  dependentTaskIds: number[];
  repeatConfig?: RepeatConfig;
  source: TaskSource;
  externalId?: string;

  progressPercentage: number;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

export interface RepeatConfig {
  frequency: 'daily' | 'weekly' | 'monthly';
  interval: number;
  endDate?: string;
  daysOfWeek?: number[];
}

export interface TaskTemplate {
  id: number;
  userId: number;
  tenantId: number;

  templateName: string;
  titleTemplate: string;
  descriptionTemplate?: string;

  estimatedDurationHours: number;
  priority: TaskPriority;
  category?: string;
  tags: string[];
  isDeepWork: boolean;

  isFavorite: boolean;
  usageCount: number;
  lastUsedAt?: string;

  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority?: TaskPriority;
  estimatedDurationHours?: number;
  deadlineMs?: number;
  projectId?: number;
  parentTaskId?: number;
  category?: string;
  tags?: string[];
  isDeepWork?: boolean;
  repeatConfig?: RepeatConfig;
}

export interface UpdateTaskRequest {
  id: number;
  title?: string;
  description?: string;
  priority?: TaskPriority;
  status?: TaskStatus;
  estimatedDurationHours?: number;
  deadlineMs?: number;
  progressPercentage?: number;
  isDeepWork?: boolean;
  category?: string;
  tags?: string[];
}
