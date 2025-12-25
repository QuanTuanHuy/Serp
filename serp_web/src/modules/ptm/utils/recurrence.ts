/**
 * PTM - Recurrence Utilities
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Helper functions for recurrence display and parsing
 */

import type { Task } from '../types';

export interface RecurrenceInfo {
  displayText: string;
  endDate?: string;
  interval: number;
  frequency: string;
  daysOfWeek?: number[];
}

/**
 * Parse and format recurrence configuration for display
 */
export function getRecurrenceDisplay(task: Task): RecurrenceInfo | null {
  if (!task.isRecurring) return null;

  try {
    const pattern = task.recurrencePattern || '';
    const config = task.recurrenceConfig
      ? JSON.parse(task.recurrenceConfig)
      : {};
    const interval = config.interval || 1;
    const frequency = pattern.toLowerCase();

    let displayText = '';

    if (frequency === 'daily') {
      displayText = interval === 1 ? 'Daily' : `Every ${interval} days`;
    } else if (frequency === 'weekly') {
      const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
      const days = config.daysOfWeek || [];
      const daysText = days.map((d: number) => dayNames[d]).join(', ');

      displayText =
        interval === 1
          ? `Weekly${daysText ? ` on ${daysText}` : ''}`
          : `Every ${interval} weeks${daysText ? ` on ${daysText}` : ''}`;
    } else if (frequency === 'monthly') {
      displayText = interval === 1 ? 'Monthly' : `Every ${interval} months`;
    }

    return {
      displayText,
      endDate: config.endDate,
      interval,
      frequency,
      daysOfWeek: config.daysOfWeek,
    };
  } catch {
    return null;
  }
}

/**
 * Format duration from minutes to human-readable format
 */
export function formatDuration(minutes: number | undefined): string {
  if (!minutes || minutes === 0) return 'Not set';

  if (minutes >= 60) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
  }

  return `${minutes}m`;
}

/**
 * Parse RepeatConfig (UI format) to backend format
 */
export function repeatConfigToBackend(repeatConfig: any): {
  isRecurring: boolean;
  recurrencePattern?: string;
  recurrenceConfig?: string;
} {
  if (!repeatConfig) {
    return {
      isRecurring: false,
      recurrencePattern: undefined,
      recurrenceConfig: undefined,
    };
  }

  return {
    isRecurring: true,
    recurrencePattern: repeatConfig.frequency.toUpperCase(),
    recurrenceConfig: JSON.stringify({
      interval: repeatConfig.interval,
      endDate: repeatConfig.endDate,
      daysOfWeek: repeatConfig.daysOfWeek,
    }),
  };
}

/**
 * Parse backend recurrence format to UI RepeatConfig
 */
export function backendToRepeatConfig(task: Partial<Task>): any | null {
  if (!task?.isRecurring || !task.recurrencePattern) return null;

  try {
    const config = task.recurrenceConfig
      ? JSON.parse(task.recurrenceConfig)
      : {};

    return {
      frequency: task.recurrencePattern.toLowerCase() as
        | 'daily'
        | 'weekly'
        | 'monthly',
      interval: config.interval || 1,
      endDate: config.endDate,
      daysOfWeek: config.daysOfWeek,
    };
  } catch {
    return null;
  }
}
