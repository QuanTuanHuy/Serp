/**
 * PTM v2 - Dashboard Stats Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Fetch and compute dashboard statistics
 */

import { useMemo } from 'react';
import { useGetTasksQuery } from '../services/taskApi';
import { useGetScheduleEventsQuery } from '../services/scheduleApi';

export interface DashboardStats {
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  completionRate: number;
  focusTimeToday: number; // in hours
  tasksToday: number;
  overdueTasks: number;
  trends: {
    completionRateTrend: number; // percentage change from last week
    focusTimeTrend: number;
  };
}

export function useDashboardStats() {
  const { data: tasks = [], isLoading: isLoadingTasks } = useGetTasksQuery({});

  // Get today's schedule events
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  const { data: todayEvents = [], isLoading: isLoadingSchedule } =
    useGetScheduleEventsQuery({
      startDateMs: today.getTime(),
      endDateMs: tomorrow.getTime(),
    });

  const stats = useMemo<DashboardStats>(() => {
    const totalTasks = tasks.length;
    const completedTasks = tasks.filter((t) => t.status === 'DONE').length;
    const inProgressTasks = tasks.filter(
      (t) => t.status === 'IN_PROGRESS'
    ).length;
    const todoTasks = tasks.filter((t) => t.status === 'TODO').length;
    const completionRate =
      totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0;

    // Calculate focus time today (sum of deep work task durations)
    const focusTimeToday = todayEvents
      .filter((event) => event.isDeepWork)
      .reduce((acc, event) => acc + event.durationMin / 60, 0);

    // Tasks scheduled for today
    const tasksToday = todayEvents.filter(
      (event) => event.status === 'scheduled'
    ).length;

    // Overdue tasks (deadline passed but not completed)
    const now = Date.now();
    const overdueTasks = tasks.filter(
      (t) => t.deadlineMs && t.deadlineMs < now && t.status !== 'DONE'
    ).length;

    // Mock trends (would come from historical data in real implementation)
    const completionRateTrend = 5.2; // +5.2% from last week
    const focusTimeTrend = -2.1; // -2.1% from last week

    return {
      totalTasks,
      completedTasks,
      inProgressTasks,
      todoTasks,
      completionRate,
      focusTimeToday,
      tasksToday,
      overdueTasks,
      trends: {
        completionRateTrend,
        focusTimeTrend,
      },
    };
  }, [tasks, todayEvents]);

  return {
    stats,
    isLoading: isLoadingTasks || isLoadingSchedule,
  };
}
