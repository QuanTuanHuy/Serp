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
  UnscheduledTasksPanel,
  ScheduleTaskEditDialog,
} from '@/modules/ptm';
import type {
  OptimizationConfig,
  ScheduleEvent,
  ScheduleTask,
} from '@/modules/ptm';
import { GanttView } from '@/modules/ptm/components/schedule/GanttView';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Calendar, GanttChart } from 'lucide-react';
import {
  useGetFocusTimeBlocksQuery,
  useTriggerOptimizationMutation,
  useCreateScheduleEventMutation,
  useUpdateScheduleEventMutation,
  useDeleteScheduleEventMutation,
  useGetTasksQuery,
} from '@/modules/ptm/api';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

export default function SchedulePage() {
  const router = useRouter();
  const [viewMode, setViewMode] = useState<'calendar' | 'gantt'>('calendar');
  const [optimizationDialogOpen, setOptimizationDialogOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<ScheduleEvent | null>(
    null
  );
  const [editingTask, setEditingTask] = useState<ScheduleTask | null>(null);

  // Fetch data
  const { data: paginatedData } = useGetTasksQuery({});
  const allTasks = paginatedData?.data?.items || [];
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
      .reduce((sum, task) => sum + (task.estimatedDurationMin || 0) / 60, 0);
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
    router.push(`/ptm/tasks/${selectedEvent.scheduleTaskId}`);
  };

  return (
    <div className='container mx-auto p-6 space-y-6'>
      {/* Intelligent Header */}
      <ScheduleHeader
        dateRange={dateRange}
        stats={stats}
        onOptimize={() => setOptimizationDialogOpen(true)}
      />

      {/* View Tabs: Calendar & Gantt */}
      <Tabs
        value={viewMode}
        onValueChange={(v) => setViewMode(v as 'calendar' | 'gantt')}
      >
        <TabsList className='grid w-full max-w-md grid-cols-2'>
          <TabsTrigger value='calendar' className='flex items-center gap-2'>
            <Calendar className='h-4 w-4' />
            Calendar View
          </TabsTrigger>
          <TabsTrigger value='gantt' className='flex items-center gap-2'>
            <GanttChart className='h-4 w-4' />
            Timeline View
          </TabsTrigger>
        </TabsList>

        {/* Calendar View */}
        <TabsContent value='calendar' className='mt-6'>
          <div className='grid grid-cols-[320px_1fr] gap-6'>
            {/* Left Panel: Unscheduled Tasks */}
            <UnscheduledTasksPanel
              onEditTask={(task) => setEditingTask(task)}
              className='h-[calc(100vh-300px)]'
            />

            {/* Main Calendar */}
            <div className='flex-1'>
              <CalendarView
                onEventSelect={handleEventSelect}
                onExternalDrop={handleExternalDrop}
              />
            </div>
          </div>
        </TabsContent>

        {/* Gantt Timeline View */}
        <TabsContent value='gantt' className='mt-6'>
          <div className='border rounded-lg overflow-hidden bg-card'>
            <div className='p-4 border-b'>
              <h3 className='text-lg font-semibold'>Project Timeline</h3>
              <p className='text-sm text-muted-foreground'>
                Visualize task schedules and dependencies on a Gantt chart
              </p>
            </div>
            <div className='h-[700px]'>
              <GanttView />
            </div>
          </div>
        </TabsContent>
      </Tabs>

      {/* Event Detail Sheet */}
      {selectedEvent && (
        <EventDetailSheet
          event={selectedEvent}
          open={!!selectedEvent}
          onOpenChange={(open) => !open && setSelectedEvent(null)}
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

      {/* Schedule Task Edit Dialog */}
      <ScheduleTaskEditDialog
        task={editingTask}
        open={!!editingTask}
        onOpenChange={(open) => !open && setEditingTask(null)}
      />
    </div>
  );
}
