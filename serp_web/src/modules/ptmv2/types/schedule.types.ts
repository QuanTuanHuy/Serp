/**
 * PTM v2 - Schedule Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Schedule domain types
 */

export type ScheduleEventStatus =
  | 'scheduled'
  | 'completed'
  | 'skipped'
  | 'cancelled';
export type AlgorithmType = 'local_heuristic' | 'milp_optimized' | 'hybrid';

export interface SchedulePlan {
  id: string;
  userId: string;
  tenantId: string;

  startDateMs: number;
  endDateMs?: number;
  status: 'draft' | 'active' | 'completed' | 'archived';

  algorithmType: AlgorithmType;
  totalUtility: number;
  tasksScheduled: number;
  tasksUnscheduled: number;
  version: number;

  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

export interface ScheduleEvent {
  id: string;
  schedulePlanId: string;
  scheduleTaskId: string;

  dateMs: number;
  startMin: number; // Minutes from midnight (0-1439)
  endMin: number;
  durationMin: number;

  status: ScheduleEventStatus;
  taskPart: number;
  totalParts: number;

  utility: number;
  utilityBreakdown: UtilityBreakdown;

  isManualOverride: boolean;

  // For UI
  title?: string;
  priority?: string;
  isDeepWork?: boolean;
  projectColor?: string;

  createdAt: string;
  updatedAt: string;
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
  id: string;
  userId: string;
  tenantId: string;

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
  id: string;
  dateMs?: number;
  startMin?: number;
  endMin?: number;
  status?: ScheduleEventStatus;
}
