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

  title: string;
  description?: string;
  priority: TaskPriority;
  status: TaskStatus;

  // Duration fields (in minutes, matching backend)
  estimatedDurationMin?: number;
  actualDurationMin?: number;
  isDurationLearned: boolean;

  // Priority scoring
  priorityScore?: number;

  // Date fields (timestamps in milliseconds)
  preferredStartDateMs?: number;
  deadlineMs?: number;
  earliestStartMs?: number;
  completedAt?: string;

  // Categorization
  category?: string;
  tags: string[];

  // Subtask hierarchy (matches backend)
  parentTaskId?: number;
  hasSubtasks: boolean;
  totalSubtaskCount: number;
  completedSubtaskCount: number;

  // Recurrence fields
  isRecurring: boolean;
  recurrencePattern?: string;
  recurrenceConfig?: string;
  parentRecurringTaskId?: number;

  // Task attributes
  isDeepWork: boolean;
  isMeeting: boolean;
  isFlexible: boolean;

  // External integration
  source: TaskSource;
  externalId?: string;

  // Status
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;

  // Recursive subtask tree (from backend SubTasks field)
  subTasks?: Task[];

  // Computed fields for hierarchy and dependencies
  depth?: number; // 0=root, 1=subtask, 2=sub-subtask
  isBlocked?: boolean; // Has incomplete dependencies
  blockingTasksCount?: number; // Number of tasks waiting for this
  dependentTaskIds: number[]; // Computed from dependencies
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
  | 'FINISH_TO_START' // Default: B starts after A finishes
  | 'START_TO_START' // B starts when A starts
  | 'FINISH_TO_FINISH'; // B finishes when A finishes

export interface DependencyGraph {
  nodes: Task[];
  edges: TaskDependency[];
  criticalPath?: number[]; // IDs of tasks in critical path
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
  children: Task[]; // Direct subtasks
  descendants: Task[]; // All nested subtasks
  ancestors: Task[]; // Parent chain to root
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

  estimatedDurationMin?: number;
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
  estimatedDurationMin?: number;
  preferredStartDateMs?: number;
  deadlineMs?: number;
  earliestStartMs?: number;
  category?: string;
  tags?: string[];
  parentTaskId?: number;
  projectId?: number;
  isRecurring?: boolean;
  recurrencePattern?: string;
  recurrenceConfig?: string;
  isDeepWork?: boolean;
  isMeeting?: boolean;
  isFlexible?: boolean;
  externalId?: string;
  source?: TaskSource;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  priority?: TaskPriority;
  priorityScore?: number;
  estimatedDurationMin?: number;
  actualDurationMin?: number;
  preferredStartDateMs?: number;
  deadlineMs?: number;
  earliestStartMs?: number;
  category?: string;
  tags?: string[];
  parentTaskId?: number;
  projectId?: number;
  isRecurring?: boolean;
  recurrencePattern?: string;
  recurrenceConfig?: string;
  isDeepWork?: boolean;
  isMeeting?: boolean;
  isFlexible?: boolean;
  status?: TaskStatus;
}

export interface TaskFilterParams {
  status?: string;
  priority?: string;
  projectId?: number;
  parentTaskId?: number;
  category?: string;
  tags?: string[];
  isDeepWork?: boolean;
  isMeeting?: boolean;
  isRecurring?: boolean;
  deadlineFrom?: number;
  deadlineTo?: number;
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}
