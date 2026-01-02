/**
 * PTM v2 - Schedule Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Schedule domain types
 */

export type ScheduleEventStatus = 'PLANNED' | 'DONE' | 'SKIPPED' | 'CANCELLED';
export type AlgorithmType = 'local_heuristic' | 'milp_optimized' | 'hybrid';
export type PlanStatus =
  | 'DRAFT'
  | 'PROCESSING'
  | 'PROPOSED'
  | 'ACTIVE'
  | 'COMPLETED'
  | 'ARCHIVED'
  | 'DISCARDED'
  | 'FAILED';
export type RescheduleStrategy = 'ripple' | 'insertion' | 'full_replan';
export type ScheduleTaskStatus =
  | 'PENDING'
  | 'SCHEDULED'
  | 'EXCLUDED'
  | 'COMPLETED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface SchedulePlan {
  id: number;
  userId: number;
  tenantId: number;

  name?: string; // Plan name
  startDateMs: number;
  endDateMs?: number;
  status: PlanStatus;

  algorithmType?: AlgorithmType;
  totalUtility?: number;
  totalTasks?: number; // Add total tasks in plan
  tasksScheduled?: number;
  tasksUnscheduled?: number;
  totalScheduledTasks?: number; // Alias for tasksScheduled
  version: number;

  // Optimization metrics
  optimizationDurationMs?: number;
  optimizationScore?: number;

  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

export interface ScheduleEvent {
  id: number;
  schedulePlanId: number;
  scheduleTaskId: number;
  taskId?: number; // Reference to original task

  dateMs: number;
  startDateMs?: number; // Add for convenience (same as dateMs + startMin)
  endDateMs?: number; // Add for convenience
  startMin: number; // Minutes from midnight (0-1439)
  endMin: number;
  durationMin?: number; // Add computed duration (for compatibility)

  status: ScheduleEventStatus;
  partIndex: number;
  totalParts: number;
  linkedEventId?: number;

  isPinned: boolean;
  isManualOverride?: boolean; // Add manual override flag
  utilityScore?: number;

  actualStartMin?: number;
  actualEndMin?: number;

  // For UI
  title?: string;
  priority?: string;
  isDeepWork?: boolean;
  projectColor?: string;
  taskPart?: number; // Alias for partIndex + 1 (for UI compatibility)

  createdAt: string;
  updatedAt: string;
}

export interface ScheduleTask {
  id: number;
  userId: number;
  tenantId: number;
  schedulePlanId: number;
  taskId: number; // Reference to PTM Task

  title: string;
  durationMin: number;
  priority: Priority;
  category?: string;
  isDeepWork: boolean;

  // Constraints
  deadlineMs?: number;
  earliestStartMs?: number;
  preferredStartMs?: number;

  // Task structure
  parentTaskId?: number;
  hasSubtasks: boolean;
  totalSubtaskCount: number;
  completedSubtaskCount: number;

  // Splitting
  allowSplit: boolean;
  minSplitDurationMin: number;
  maxSplitCount: number;

  // Buffers
  bufferBeforeMin: number;
  bufferAfterMin: number;

  // Status
  scheduleStatus: ScheduleTaskStatus;
  priorityScore: number;
  taskSnapshotHash: string;

  createdAt: string;
  updatedAt: string;
}

export interface UpdateScheduleTaskRequest {
  durationMin?: number;
  priority?: Priority;
  deadlineMs?: number;
  earliestStartMs?: number;
  preferredStartMs?: number;
  isDeepWork?: boolean;
  allowSplit?: boolean;
  minSplitDurationMin?: number;
  maxSplitCount?: number;
  bufferBeforeMin?: number;
  bufferAfterMin?: number;
}

export interface GetScheduleTasksParams {
  planId?: number; // Default to active plan if not provided
  status?: ScheduleTaskStatus;
  includeEvents?: boolean;
}

export interface GetScheduleTasksResponse {
  tasks: ScheduleTask[];
  plan: SchedulePlan; // Include plan info for context
}

export interface UtilityBreakdown {
  priorityScore: number;
  deadlineScore: number;
  contextSwitchPenalty: number;
  focusTimeBonus: number;
  totalUtility: number;
  reason: string;
}

export interface FocusTimeBlock {
  id: number;
  userId: number;
  tenantId: number;

  blockName: string;
  dayOfWeek: number; // 0=Sunday, 6=Saturday
  startMin: number;
  endMin: number;

  allowMeetings: boolean;
  allowRegularTasks: boolean;
  flexibilityLevel: number; // 0-100

  isEnabled: boolean;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

export interface CreateSchedulePlanRequest {
  startDateMs: number;
  endDateMs?: number;
  algorithmType?: AlgorithmType;
}

export interface UpdateScheduleEventRequest {
  id: number;
  dateMs?: number;
  startMin?: number;
  endMin?: number;
  status?: ScheduleEventStatus;
  isPinned?: boolean;
}

export interface TriggerRescheduleRequest {
  strategy?: RescheduleStrategy;
}

export interface PlanDetailResponse {
  plan: SchedulePlan;
  events: ScheduleEvent[];
  tasks: any[]; // ScheduleTask from backend
  stats: PlanStats;
}

export interface PlanStats {
  totalTasks: number;
  scheduledTasks: number;
  unscheduledTasks: number;
  totalDurationMin: number;
  usedDurationMin: number;
  utilizationPct: number;
}

export interface PlanHistoryResponse {
  plans: SchedulePlan[];
  total: number;
  page: number;
  pageSize: number;
}

// Availability Calendar Types
export type AvailabilitySlotType = 'focus' | 'regular' | 'flexible';
export type ActiveStatus = 'ACTIVE' | 'INACTIVE';

export interface AvailabilityCalendar {
  id?: number;
  userId?: string;
  dayOfWeek: number; // 0-6 (Sunday-Saturday)
  startMin: number; // Minutes from midnight (0-1439)
  endMin: number; // Minutes from midnight (0-1439)
  slotType: AvailabilitySlotType; // 🆕 focus | regular | flexible
  activeStatus: ActiveStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface AvailabilitySlot {
  id?: number;
  dayOfWeek: number;
  startTime: string; // "09:00" format
  endTime: string; // "17:00" format
  slotType: AvailabilitySlotType;
  isEnabled: boolean;
}

export interface CreateAvailabilityRequest {
  items: AvailabilityCalendar[];
}

export interface UpdateAvailabilityRequest {
  items: AvailabilityCalendar[];
}
