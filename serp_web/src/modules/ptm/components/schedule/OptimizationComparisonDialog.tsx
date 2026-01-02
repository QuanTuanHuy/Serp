/*
Author: QuanTuanHuy
Description: Part of Serp Project
Optimization Comparison Dialog - Compare active plan vs proposed plan
*/

'use client';

import { useState, useMemo } from 'react';
import {
  useGetPlanWithEventsQuery,
  useApplyProposedPlanMutation,
  useDiscardProposedPlanMutation,
} from '../../api';
import type { SchedulePlan, ScheduleEvent } from '../../types';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Badge } from '@/shared/components/ui/badge';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  ArrowRight,
  CheckCircle2,
  Clock,
  TrendingUp,
  X,
  Calendar,
  Plus,
  Minus,
  MoveHorizontal,
} from 'lucide-react';
import { cn } from '@/shared/utils';

// Helper functions
const formatDate = (timestampMs: number, options?: { format?: string }) => {
  const date = new Date(timestampMs);
  if (options?.format === 'MMM d') {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const formatTime = (timestampMs: number) => {
  const date = new Date(timestampMs);
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

interface OptimizationComparisonDialogProps {
  activePlan: SchedulePlan;
  proposedPlan: SchedulePlan;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

interface EventDiff {
  type: 'added' | 'removed' | 'moved' | 'unchanged';
  event: ScheduleEvent;
  oldEvent?: ScheduleEvent;
}

function MiniCalendarView({
  events,
  startDateMs,
  endDateMs,
  title,
}: {
  events: ScheduleEvent[];
  startDateMs: number;
  endDateMs: number;
  title: string;
}) {
  const days = useMemo(() => {
    const result: { date: Date; dateMs: number; events: ScheduleEvent[] }[] =
      [];
    const start = new Date(startDateMs);
    const end = new Date(endDateMs);

    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      const dayStart = new Date(d).setHours(0, 0, 0, 0);
      const dayEnd = new Date(d).setHours(23, 59, 59, 999);
      const dayEvents = events.filter((e) => {
        const eventDateMs = e.startDateMs || e.dateMs;
        return eventDateMs >= dayStart && eventDateMs <= dayEnd;
      });
      result.push({
        date: new Date(d),
        dateMs: dayStart,
        events: dayEvents,
      });
    }
    return result;
  }, [events, startDateMs, endDateMs]);

  return (
    <div className='space-y-2'>
      <h3 className='text-sm font-semibold'>{title}</h3>
      <div className='grid grid-cols-7 gap-1'>
        {days.map((day) => (
          <div
            key={day.dateMs}
            className='min-h-[60px] rounded border bg-card p-1 text-xs'
          >
            <div className='mb-1 font-semibold text-muted-foreground'>
              {formatDate(day.dateMs, { format: 'MMM d' })}
            </div>
            <div className='space-y-0.5'>
              {day.events.slice(0, 3).map((event) => (
                <div
                  key={event.id}
                  className='truncate rounded bg-primary/10 px-1 py-0.5 text-[10px]'
                  title={event.title || 'Untitled'}
                >
                  {event.title || 'Untitled'}
                </div>
              ))}
              {day.events.length > 3 && (
                <div className='text-[10px] text-muted-foreground'>
                  +{day.events.length - 3} more
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function DiffList({ diffs }: { diffs: EventDiff[] }) {
  const diffIcons = {
    added: Plus,
    removed: Minus,
    moved: MoveHorizontal,
    unchanged: CheckCircle2,
  };

  const diffColors = {
    added: 'text-green-600 bg-green-50',
    removed: 'text-red-600 bg-red-50',
    moved: 'text-blue-600 bg-blue-50',
    unchanged: 'text-gray-600 bg-gray-50',
  };

  return (
    <ScrollArea className='h-[400px]'>
      <div className='space-y-2 pr-4'>
        {diffs.map((diff) => {
          const Icon = diffIcons[diff.type];
          const colorClass = diffColors[diff.type];

          return (
            <div
              key={`${diff.event.id}-${diff.type}`}
              className={`rounded-lg border p-3 ${colorClass}`}
            >
              <div className='flex items-start gap-2'>
                <Icon className='mt-0.5 h-4 w-4 flex-shrink-0' />
                <div className='flex-1 space-y-1'>
                  <div className='font-medium'>
                    {diff.event.title || 'Untitled'}
                  </div>
                  <div className='text-xs text-muted-foreground'>
                    {formatDate(diff.event.startDateMs || diff.event.dateMs)} at{' '}
                    {formatTime(
                      (diff.event.startDateMs || diff.event.dateMs) +
                        diff.event.startMin * 60000
                    )}
                    {diff.oldEvent && diff.type === 'moved' && (
                      <>
                        {' '}
                        <ArrowRight className='inline h-3 w-3' />{' '}
                        {formatDate(
                          diff.oldEvent.startDateMs || diff.oldEvent.dateMs
                        )}{' '}
                        at{' '}
                        {formatTime(
                          (diff.oldEvent.startDateMs || diff.oldEvent.dateMs) +
                            diff.oldEvent.startMin * 60000
                        )}
                      </>
                    )}
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </ScrollArea>
  );
}

function StatsComparison({
  activePlan,
  proposedPlan,
}: {
  activePlan: SchedulePlan;
  proposedPlan: SchedulePlan;
}) {
  const stats = [
    {
      label: 'Total Tasks',
      active: activePlan.totalTasks || activePlan.tasksScheduled || 0,
      proposed: proposedPlan.totalTasks || proposedPlan.tasksScheduled || 0,
    },
    {
      label: 'Scheduled Tasks',
      active: activePlan.totalScheduledTasks || activePlan.tasksScheduled || 0,
      proposed:
        proposedPlan.totalScheduledTasks || proposedPlan.tasksScheduled || 0,
    },
    {
      label: 'Optimization Score',
      active: activePlan.optimizationScore?.toFixed(2) || 'N/A',
      proposed: proposedPlan.optimizationScore?.toFixed(2) || 'N/A',
    },
  ];

  return (
    <div className='grid gap-4'>
      {stats.map((stat) => (
        <div
          key={stat.label}
          className='flex items-center justify-between rounded-lg border bg-card p-4'
        >
          <span className='text-sm font-medium text-muted-foreground'>
            {stat.label}
          </span>
          <div className='flex items-center gap-4'>
            <div className='text-right'>
              <div className='text-xs text-muted-foreground'>Active</div>
              <div className='text-lg font-semibold'>{stat.active}</div>
            </div>
            <ArrowRight className='h-4 w-4 text-muted-foreground' />
            <div className='text-right'>
              <div className='text-xs text-muted-foreground'>Proposed</div>
              <div className='text-lg font-semibold text-blue-600'>
                {stat.proposed}
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export function OptimizationComparisonDialog({
  activePlan,
  proposedPlan,
  open,
  onOpenChange,
}: OptimizationComparisonDialogProps) {
  const [activeTab, setActiveTab] = useState<'stats' | 'calendar' | 'diff'>(
    'stats'
  );

  const { data: activeData } = useGetPlanWithEventsQuery(
    {
      id: activePlan.id,
      fromDateMs: activePlan.startDateMs || 0,
      toDateMs:
        activePlan.endDateMs ||
        activePlan.startDateMs + 7 * 24 * 60 * 60 * 1000 ||
        0,
    },
    { skip: !open }
  );

  const { data: proposedData } = useGetPlanWithEventsQuery(
    {
      id: proposedPlan.id,
      fromDateMs: proposedPlan.startDateMs || 0,
      toDateMs:
        proposedPlan.endDateMs ||
        proposedPlan.startDateMs + 7 * 24 * 60 * 60 * 1000 ||
        0,
    },
    { skip: !open }
  );

  const [applyPlan, { isLoading: isApplying }] = useApplyProposedPlanMutation();
  const [discardPlan, { isLoading: isDiscarding }] =
    useDiscardProposedPlanMutation();

  const diffs = useMemo<EventDiff[]>(() => {
    if (!activeData || !proposedData) return [];

    const activeEvents = activeData.events || [];
    const proposedEvents = proposedData.events || [];

    const result: EventDiff[] = [];
    const activeMap = new Map(
      activeEvents.map((e) => [e.taskId || e.scheduleTaskId, e])
    );
    const proposedMap = new Map(
      proposedEvents.map((e) => [e.taskId || e.scheduleTaskId, e])
    );

    // Find added and moved
    proposedEvents.forEach((proposedEvent) => {
      const taskKey = proposedEvent.taskId || proposedEvent.scheduleTaskId;
      const activeEvent = activeMap.get(taskKey);
      if (!activeEvent) {
        result.push({ type: 'added', event: proposedEvent });
      } else {
        const proposedStart = proposedEvent.startDateMs || proposedEvent.dateMs;
        const activeStart = activeEvent.startDateMs || activeEvent.dateMs;
        if (
          proposedStart !== activeStart ||
          proposedEvent.startMin !== activeEvent.startMin ||
          proposedEvent.endMin !== activeEvent.endMin
        ) {
          result.push({
            type: 'moved',
            event: proposedEvent,
            oldEvent: activeEvent,
          });
        } else {
          result.push({ type: 'unchanged', event: proposedEvent });
        }
      }
    });

    // Find removed
    activeEvents.forEach((activeEvent) => {
      const taskKey = activeEvent.taskId || activeEvent.scheduleTaskId;
      if (!proposedMap.has(taskKey)) {
        result.push({ type: 'removed', event: activeEvent });
      }
    });

    return result;
  }, [activeData, proposedData]);

  const handleApply = async () => {
    try {
      await applyPlan(proposedPlan.id).unwrap();
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to apply plan:', error);
    }
  };

  const handleDiscard = async () => {
    try {
      await discardPlan(proposedPlan.id).unwrap();
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to discard plan:', error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-4xl'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <TrendingUp className='h-5 w-5 text-blue-600' />
            Optimization Comparison
          </DialogTitle>
          <DialogDescription>
            Review changes between your current schedule and the proposed
            optimization
          </DialogDescription>
        </DialogHeader>

        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as any)}>
          <TabsList className='grid w-full grid-cols-3'>
            <TabsTrigger value='stats'>Stats</TabsTrigger>
            <TabsTrigger value='calendar'>Calendar</TabsTrigger>
            <TabsTrigger value='diff'>Changes</TabsTrigger>
          </TabsList>

          <TabsContent value='stats' className='space-y-4'>
            <StatsComparison
              activePlan={activePlan}
              proposedPlan={proposedPlan}
            />
          </TabsContent>

          <TabsContent value='calendar' className='space-y-4'>
            {activeData && (
              <MiniCalendarView
                events={activeData.events || []}
                startDateMs={activePlan.startDateMs ?? Date.now()}
                endDateMs={activePlan.endDateMs ?? Date.now()}
                title='Current Active Plan'
              />
            )}
            {proposedData && (
              <MiniCalendarView
                events={proposedData.events || []}
                startDateMs={proposedPlan.startDateMs ?? Date.now()}
                endDateMs={proposedPlan.endDateMs ?? Date.now()}
                title='Proposed Plan'
              />
            )}
          </TabsContent>

          <TabsContent value='diff'>
            <div className='space-y-2'>
              <div className='flex gap-2'>
                <Badge variant='outline' className='bg-green-50 text-green-600'>
                  <Plus className='mr-1 h-3 w-3' />
                  {diffs.filter((d) => d.type === 'added').length} Added
                </Badge>
                <Badge variant='outline' className='bg-red-50 text-red-600'>
                  <Minus className='mr-1 h-3 w-3' />
                  {diffs.filter((d) => d.type === 'removed').length} Removed
                </Badge>
                <Badge variant='outline' className='bg-blue-50 text-blue-600'>
                  <MoveHorizontal className='mr-1 h-3 w-3' />
                  {diffs.filter((d) => d.type === 'moved').length} Moved
                </Badge>
              </div>
              <DiffList diffs={diffs} />
            </div>
          </TabsContent>
        </Tabs>

        <DialogFooter>
          <Button
            variant='outline'
            onClick={handleDiscard}
            disabled={isApplying || isDiscarding}
          >
            <X className='mr-2 h-4 w-4' />
            Keep Current
          </Button>
          <Button
            onClick={handleApply}
            disabled={isApplying || isDiscarding}
            className='bg-blue-600 hover:bg-blue-700'
          >
            {isApplying ? (
              <Clock className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <CheckCircle2 className='mr-2 h-4 w-4' />
            )}
            Apply Proposed Plan
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
