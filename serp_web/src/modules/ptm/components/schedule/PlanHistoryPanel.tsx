/*
Author: QuanTuanHuy
Description: Part of Serp Project
Plan History Panel - Shows active, proposed, and archived schedule plans
*/

'use client';

import { useState } from 'react';
import {
  useGetPlanHistoryQuery,
  useApplyProposedPlanMutation,
  useDiscardProposedPlanMutation,
  useRevertToPlanMutation,
} from '../../api';
import type { SchedulePlan } from '../../types';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  CheckCircle2,
  Clock,
  TrendingUp,
  Archive,
  ChevronRight,
  ChevronDown,
  Calendar,
  Trash2,
  RotateCcw,
} from 'lucide-react';
import { formatDate as formatDateUtil } from '@/shared/utils';

// Helper functions
const formatDate = (timestampMs: number | string | undefined) => {
  if (!timestampMs) return 'N/A';
  return formatDateUtil(new Date(timestampMs));
};

const formatTime = (timestampMs: number | string) => {
  const date = new Date(timestampMs);
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

const STATUS_CONFIG = {
  ACTIVE: {
    label: 'Active',
    icon: CheckCircle2,
    color: 'text-green-600',
    bgColor: 'bg-green-50',
    borderColor: 'border-green-200',
  },
  PROPOSED: {
    label: 'Proposed',
    icon: TrendingUp,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50',
    borderColor: 'border-blue-200',
  },
  PROCESSING: {
    label: 'Processing',
    icon: Clock,
    color: 'text-yellow-600',
    bgColor: 'bg-yellow-50',
    borderColor: 'border-yellow-200',
  },
  ARCHIVED: {
    label: 'Archived',
    icon: Archive,
    color: 'text-gray-600',
    bgColor: 'bg-gray-50',
    borderColor: 'border-gray-200',
  },
  COMPLETED: {
    label: 'Completed',
    icon: CheckCircle2,
    color: 'text-green-600',
    bgColor: 'bg-green-50',
    borderColor: 'border-green-200',
  },
  DISCARDED: {
    label: 'Discarded',
    icon: Trash2,
    color: 'text-red-600',
    bgColor: 'bg-red-50',
    borderColor: 'border-red-200',
  },
  FAILED: {
    label: 'Failed',
    icon: Trash2,
    color: 'text-red-600',
    bgColor: 'bg-red-50',
    borderColor: 'border-red-200',
  },
  DRAFT: {
    label: 'Draft',
    icon: Clock,
    color: 'text-gray-600',
    bgColor: 'bg-gray-50',
    borderColor: 'border-gray-200',
  },
} as const;

interface PlanCardProps {
  plan: SchedulePlan;
  isActive?: boolean;
  isProposed?: boolean;
  onApply?: () => void;
  onDiscard?: () => void;
  onRevert?: () => void;
}

function PlanCard({
  plan,
  isActive,
  isProposed,
  onApply,
  onDiscard,
  onRevert,
}: PlanCardProps) {
  const config = STATUS_CONFIG[plan.status];
  const Icon = config.icon;

  return (
    <Card
      className={`${isActive ? 'ring-2 ring-green-500' : ''} ${config.borderColor} border-2 transition-all hover:shadow-md`}
    >
      <CardHeader className='pb-3'>
        <div className='flex items-start justify-between'>
          <div className='space-y-1'>
            <CardTitle className='text-base font-semibold'>
              {plan.name || `Plan #${plan.id}`}
            </CardTitle>
            <CardDescription className='text-xs'>
              {formatDate(plan.startDateMs)} - {formatDate(plan.endDateMs)}
            </CardDescription>
          </div>
          <Badge
            variant='outline'
            className={`${config.bgColor} ${config.color} border-current`}
          >
            <Icon className='mr-1 h-3 w-3' />
            {config.label}
          </Badge>
        </div>
      </CardHeader>

      <CardContent className='space-y-3'>
        {/* Stats */}
        <div className='grid grid-cols-2 gap-2 text-xs'>
          <div className='space-y-1'>
            <div className='text-muted-foreground'>Total Tasks</div>
            <div className='font-semibold'>
              {plan.totalScheduledTasks || plan.tasksScheduled || 0}/
              {plan.totalTasks || 0}
            </div>
          </div>
          {plan.optimizationScore !== undefined && (
            <div className='space-y-1'>
              <div className='text-muted-foreground'>Score</div>
              <div className='font-semibold'>
                {plan.optimizationScore.toFixed(2)}
              </div>
            </div>
          )}
        </div>

        {/* Metadata */}
        <div className='space-y-1 border-t pt-2 text-xs text-muted-foreground'>
          <div className='flex items-center justify-between'>
            <span>Created</span>
            <span>{formatTime(plan.createdAt)}</span>
          </div>
          {plan.optimizationDurationMs && (
            <div className='flex items-center justify-between'>
              <span>Optimized in</span>
              <span>{plan.optimizationDurationMs}ms</span>
            </div>
          )}
        </div>

        {/* Actions */}
        {isProposed && (
          <div className='flex gap-2 border-t pt-3'>
            <Button
              size='sm'
              className='flex-1'
              onClick={onApply}
              variant='default'
            >
              <CheckCircle2 className='mr-1 h-4 w-4' />
              Apply
            </Button>
            <Button
              size='sm'
              variant='outline'
              onClick={onDiscard}
              className='flex-1'
            >
              <Trash2 className='mr-1 h-4 w-4' />
              Discard
            </Button>
          </div>
        )}

        {!isActive && !isProposed && plan.status === 'ARCHIVED' && (
          <Button
            size='sm'
            variant='outline'
            onClick={onRevert}
            className='w-full'
          >
            <RotateCcw className='mr-1 h-4 w-4' />
            Revert to this plan
          </Button>
        )}
      </CardContent>
    </Card>
  );
}

export function PlanHistoryPanel() {
  const [showArchived, setShowArchived] = useState(false);
  const { data: historyData, isLoading } = useGetPlanHistoryQuery({
    page: 1,
    pageSize: 20,
  });

  const [applyPlan, { isLoading: isApplying }] = useApplyProposedPlanMutation();
  const [discardPlan, { isLoading: isDiscarding }] =
    useDiscardProposedPlanMutation();
  const [revertPlan, { isLoading: isReverting }] = useRevertToPlanMutation();

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center'>
            <Calendar className='mr-2 h-5 w-5' />
            Plan History
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className='flex items-center justify-center py-8'>
            <Clock className='h-6 w-6 animate-spin text-muted-foreground' />
          </div>
        </CardContent>
      </Card>
    );
  }

  const plans = historyData?.plans || [];
  const activePlan = plans.find((p: SchedulePlan) => p.status === 'ACTIVE');
  const proposedPlan = plans.find((p: SchedulePlan) => p.status === 'PROPOSED');
  const archivedPlans = plans.filter(
    (p: SchedulePlan) => p.status === 'ARCHIVED' || p.status === 'COMPLETED'
  );

  const handleApply = async (planId: number) => {
    try {
      await applyPlan(planId).unwrap();
    } catch (error) {
      console.error('Failed to apply plan:', error);
    }
  };

  const handleDiscard = async (planId: number) => {
    try {
      await discardPlan(planId).unwrap();
    } catch (error) {
      console.error('Failed to discard plan:', error);
    }
  };

  const handleRevert = async (planId: number) => {
    try {
      await revertPlan(planId).unwrap();
    } catch (error) {
      console.error('Failed to revert plan:', error);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center'>
          <Calendar className='mr-2 h-5 w-5' />
          Plan History
        </CardTitle>
        <CardDescription>
          View and manage your schedule optimization history
        </CardDescription>
      </CardHeader>

      <CardContent className='space-y-4'>
        {/* Active Plan */}
        {activePlan && (
          <div className='space-y-2'>
            <h3 className='text-sm font-semibold text-muted-foreground'>
              Current Active Plan
            </h3>
            <PlanCard plan={activePlan} isActive />
          </div>
        )}

        {/* Proposed Plan */}
        {proposedPlan && (
          <div className='space-y-2'>
            <h3 className='text-sm font-semibold text-muted-foreground'>
              Pending Proposal
            </h3>
            <PlanCard
              plan={proposedPlan}
              isProposed
              onApply={() => handleApply(proposedPlan.id)}
              onDiscard={() => handleDiscard(proposedPlan.id)}
            />
          </div>
        )}

        {/* Archived Plans */}
        {archivedPlans.length > 0 && (
          <div className='space-y-2'>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => setShowArchived(!showArchived)}
              className='w-full justify-start p-0 hover:bg-transparent'
            >
              {showArchived ? (
                <ChevronDown className='mr-1 h-4 w-4' />
              ) : (
                <ChevronRight className='mr-1 h-4 w-4' />
              )}
              <h3 className='text-sm font-semibold text-muted-foreground'>
                Archived Plans ({archivedPlans.length})
              </h3>
            </Button>

            {showArchived && (
              <ScrollArea className='h-[400px] pr-4'>
                <div className='space-y-3'>
                  {archivedPlans.map((plan) => (
                    <PlanCard
                      key={plan.id}
                      plan={plan}
                      onRevert={() => handleRevert(plan.id)}
                    />
                  ))}
                </div>
              </ScrollArea>
            )}
          </div>
        )}

        {/* Empty State */}
        {!activePlan && !proposedPlan && archivedPlans.length === 0 && (
          <div className='flex flex-col items-center justify-center py-8 text-center'>
            <Archive className='mb-2 h-12 w-12 text-muted-foreground/50' />
            <p className='text-sm text-muted-foreground'>
              No plans found. Create your first schedule plan to get started.
            </p>
          </div>
        )}

        {/* Loading Overlay */}
        {(isApplying || isDiscarding || isReverting) && (
          <div className='absolute inset-0 z-10 flex items-center justify-center bg-background/80'>
            <Clock className='h-6 w-6 animate-spin' />
          </div>
        )}
      </CardContent>
    </Card>
  );
}
