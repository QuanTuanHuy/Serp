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

  // NEW: Computed fields for hierarchy and dependencies
  depth?: number;                // 0=root, 1=subtask, 2=sub-subtask
  hasSubtasks?: boolean;         // Quick check
  isBlocked?: boolean;           // Has incomplete dependencies
  blockingTasksCount?: number;   // Number of tasks waiting for this
  completedSubtasksCount?: number;
  totalSubtasksCount?: number;
}

export interface RepeatConfig {
  frequency: 'daily' | 'weekly' | 'monthly';
  interval: number;
  endDate?: string;
  daysOfWeek?: number[];
}

// NEW: Dependency types
export interface TaskDependency {
  id: number;
  taskId: number;
  dependsOnTaskId: number;
  dependencyType: DependencyType;
  lagDays?: number;
  createdAt: number;
}

export type DependencyType = 
  | 'FINISH_TO_START'   // Default: B starts after A finishes
  | 'START_TO_START'    // B starts when A starts
  | 'FINISH_TO_FINISH'; // B finishes when A finishes

export interface DependencyGraph {
  nodes: Task[];
  edges: TaskDependency[];
  criticalPath?: number[];  // IDs of tasks in critical path
}

// NEW: API Request/Response types
export interface CreateDependencyRequest {
  taskId: number;
  dependsOnTaskId: number;
  dependencyType?: DependencyType;
  lagDays?: number;
}

export interface GetTaskTreeResponse {
  task: Task;
  children: Task[];      // Direct subtasks
  descendants: Task[];   // All nested subtasks
  ancestors: Task[];     // Parent chain to root
  dependencies: TaskDependency[];
}

export interface DependencyValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  wouldCreateCycle?: boolean;
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
