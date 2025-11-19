/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity Tracking Types
 * Matches backend: ptm_task_v2/src/core/domain/entity/activity_event_entity.go
 */

import type { AlgorithmType, UtilityBreakdown } from './schedule.types';

export type ActivityEventType =
  // Task events
  | 'task_created'
  | 'task_updated'
  | 'task_completed'
  | 'task_deleted'
  | 'task_deadline_approaching'
  // Project events
  | 'project_created'
  | 'project_updated'
  | 'project_completed'
  | 'project_archived'
  // Schedule events
  | 'schedule_optimized'
  | 'schedule_conflict_detected'
  | 'auto_reschedule_triggered'
  // Algorithm events
  | 'algorithm_executed'
  | 'deadline_risk_detected'
  | 'optimization_failed';

export type EntityType =
  | 'task'
  | 'project'
  | 'schedule'
  | 'algorithm'
  | 'system';

export interface GoalWeights {
  priority: number;
  deadline: number;
  focusTime: number;
  contextSwitch: number;
}

export interface ActivityEvent {
  id: string;
  userId: string;
  tenantId: string;

  // Event classification
  eventType: ActivityEventType;
  entityType: EntityType;
  entityId?: number;

  // Content
  title: string;
  description?: string;
  metadata?: Record<string, any>;

  // Navigation support
  navigationUrl?: string;
  navigationParams?: Record<string, any>;

  // Algorithm transparency (re-using types from schedule.types)
  algorithmType?: AlgorithmType;
  utilityBreakdown?: UtilityBreakdown;
  executionTimeMs?: number;
  goalWeights?: GoalWeights;
  tasksAffected?: number;

  // Audit
  createdAt: number; // Unix timestamp ms
  ipAddress?: string;
  userAgent?: string;
}

export interface ActivityFeedResponse {
  activities: ActivityEvent[];
  totalCount: number;
  currentPage: number;
  pageSize: number;
  hasMore: boolean;
}

export interface ActivityFeedFilters {
  types?: ActivityEventType[];
  entity?: EntityType;
  fromDateMs?: number;
  toDateMs?: number;
  page?: number;
  size?: number;
}
