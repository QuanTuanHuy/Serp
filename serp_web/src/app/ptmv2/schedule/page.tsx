/**
 * PTM v2 - Schedule Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Modern intelligent schedule management
 */

'use client';

import { useState, useMemo, useEffect } from 'react';
import {
  CalendarView,
  ScheduleHeader,
  ScheduleSidebar,
  OptimizationDialog,
  EventDetailSheet,
} from '@/modules/ptmv2';
import type { OptimizationConfig, ScheduleEvent } from '@/modules/ptmv2';
import { useGetTasksQuery } from '@/modules/ptmv2/services/taskApi';
import {
  useGetFocusTimeBlocksQuery,
  useTriggerOptimizationMutation,
  useCreateScheduleEventMutation,
  useUpdateScheduleEventMutation,
  useDeleteScheduleEventMutation,
} from '@/modules/ptmv2/services/scheduleApi';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

export default function SchedulePage() {
  const router = useRouter();
  const [optimizationDialogOpen, setOptimizationDialogOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<ScheduleEvent | null>(
    null
  );

  // Fetch data
  const { data: allTasks = [] } = useGetTasksQuery({});
  const { data: focusBlocks = [] } = useGetFocusTimeBlocksQuery();
  const [triggerOptimization, { isLoading: isOptimizing }] =
    useTriggerOptimizationMutation();
  const [createScheduleEvent] = useCreateScheduleEventMutation();
  const [updateScheduleEvent] = useUpdateScheduleEventMutation();
  const [deleteScheduleEvent] = useDeleteScheduleEventMutation();

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Cmd/Ctrl + O to optimize
      if ((e.metaKey || e.ctrlKey) && e.key === 'o') {
        e.preventDefault();
        setOptimizationDialogOpen(true);
      }

      // Escape to close dialogs/sheets
      if (e.key === 'Escape') {
        if (optimizationDialogOpen) {
          setOptimizationDialogOpen(false);
        } else if (selectedEvent) {
          setSelectedEvent(null);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [optimizationDialogOpen, selectedEvent]);

  // Calculate date range for current week
  const dateRange = useMemo(() => {
    const now = new Date();
    const start = new Date(now);
    start.setDate(now.getDate() - now.getDay()); // Start of week (Sunday)
    start.setHours(0, 0, 0, 0);

    const end = new Date(start);
    end.setDate(start.getDate() + 7); // End of week

    return { start, end };
  }, []);

  // Calculate unscheduled tasks (tasks without schedule events)
  const unscheduledTasks = useMemo(() => {
    return allTasks.filter(
      (task) =>
        task.status !== 'DONE' &&
        task.status !== 'CANCELLED' &&
        task.activeStatus === 'ACTIVE'
    );
  }, [allTasks]);

  // Calculate stats
  const stats = useMemo(() => {
    const totalTasks = allTasks.filter(
      (t) => t.status !== 'CANCELLED' && t.activeStatus === 'ACTIVE'
    ).length;
    const scheduledTasks = totalTasks - unscheduledTasks.length;
    const plannedHours = allTasks
      .filter((t) => t.status !== 'DONE' && t.status !== 'CANCELLED')
      .reduce((sum, task) => sum + task.estimatedDurationHours, 0);
    const activeFocusBlocks = focusBlocks.filter((b) => b.isEnabled).length;

    // Mock optimization percentage (would come from backend)
    const optimizedPercentage =
      scheduledTasks > 0 ? Math.round((scheduledTasks / totalTasks) * 100) : 0;

    return {
      optimizedPercentage,
      tasksScheduled: scheduledTasks,
      totalTasks,
      plannedHours: Math.round(plannedHours),
      focusBlocks: activeFocusBlocks,
    };
  }, [allTasks, unscheduledTasks, focusBlocks]);

  const handleOptimize = async (config: OptimizationConfig) => {
    try {
      await triggerOptimization({
        planId: 'current', // Would be actual plan ID
        useQuickPlace: config.algorithmType === 'local_heuristic',
      }).unwrap();

      toast.success('Schedule optimized successfully! 🎉', {
        description: `Scheduled ${stats.tasksScheduled} tasks using ${config.algorithmType}`,
      });
      setOptimizationDialogOpen(false);
    } catch (error) {
      toast.error('Failed to optimize schedule', {
        description: 'Please try again or contact support',
      });
    }
  };

  const handleExternalDrop = async (
    taskId: number | string,
    start: Date,
    end: Date
  ) => {
    try {
      const numericTaskId =
        typeof taskId === 'string' ? parseInt(taskId, 10) : taskId;
      const task = allTasks.find((t) => t.id === numericTaskId);
      if (!task) {
        toast.error('Task not found');
        return;
      }

      const startMinutes = start.getHours() * 60 + start.getMinutes();
      const endMinutes = end.getHours() * 60 + end.getMinutes();
      const dateMs = new Date(start).setHours(0, 0, 0, 0);

      await createScheduleEvent({
        scheduleTaskId: numericTaskId,
        dateMs,
        startMin: startMinutes,
        endMin: endMinutes,
        isDeepWork: false,
        title: task.title,
      }).unwrap();

      toast.success(`Scheduled: ${task.title}`, {
        description: `${start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`,
      });
    } catch (error) {
      console.error('Failed to create schedule event:', error);
      toast.error('Failed to schedule task');
    }
  };

  const handleEventSelect = (event: ScheduleEvent) => {
    setSelectedEvent(event);
  };

  const handleMarkComplete = async () => {
    if (!selectedEvent?.scheduleTaskId) return;

    try {
      // This would call task update API to mark complete
      toast.success('Task marked as complete!');
      setSelectedEvent(null);
    } catch (error) {
      toast.error('Failed to mark task complete');
    }
  };

  const handleReschedule = async () => {
    if (!selectedEvent) return;

    try {
      // Trigger AI reschedule
      setSelectedEvent(null);
      setOptimizationDialogOpen(true);
      toast.info('AI will reschedule this task optimally');
    } catch (error) {
      toast.error('Failed to reschedule task');
    }
  };

  const handleRemoveFromSchedule = async () => {
    if (!selectedEvent) return;

    try {
      await deleteScheduleEvent(selectedEvent.id).unwrap();
      toast.success('Event removed from schedule');
      setSelectedEvent(null);
    } catch (error) {
      console.error('Failed to delete event:', error);
      toast.error('Failed to remove event');
    }
  };

  const handleViewTask = () => {
    if (!selectedEvent?.scheduleTaskId) return;
    setSelectedEvent(null);
    router.push(`/ptmv2/tasks/${selectedEvent.scheduleTaskId}`);
  };

  return (
    <div className='container mx-auto p-6 space-y-6'>
      {/* Intelligent Header */}
      <ScheduleHeader
        dateRange={dateRange}
        stats={stats}
        onOptimize={() => setOptimizationDialogOpen(true)}
      />

      {/* Main Content: Sidebar + Calendar */}
      <div className='flex gap-6'>
        <ScheduleSidebar
          unscheduledTasks={unscheduledTasks}
          focusBlocks={focusBlocks}
          onTaskDragStart={(task) => {
            console.log('Drag started:', task.title);
          }}
          onFocusBlockToggle={(blockId) => {
            console.log('Focus block toggled:', blockId);
          }}
        />

        <div className='flex-1'>
          <CalendarView
            onEventSelect={handleEventSelect}
            onExternalDrop={handleExternalDrop}
          />
        </div>
      </div>

      {/* Event Detail Sheet */}
      {selectedEvent && (
        <EventDetailSheet
          event={selectedEvent}
          open={!!selectedEvent}
          onOpenChange={(open) => !open && setSelectedEvent(null)}
          onMarkComplete={handleMarkComplete}
          onReschedule={handleReschedule}
          onRemove={handleRemoveFromSchedule}
          onViewTask={handleViewTask}
        />
      )}

      {/* Optimization Dialog */}
      <OptimizationDialog
        open={optimizationDialogOpen}
        onOpenChange={setOptimizationDialogOpen}
        onOptimize={handleOptimize}
        isOptimizing={isOptimizing}
      />
    </div>
  );
}
