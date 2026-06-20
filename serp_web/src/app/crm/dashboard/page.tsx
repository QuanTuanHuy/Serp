/**
 * CRM Dashboard Page - Modern dashboard with live API data
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM main dashboard
 */

'use client';

import React, { useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Users,
  Target,
  TrendingUp,
  DollarSign,
  Building2,
  ArrowUpRight,
  BarChart3,
  CalendarClock,
} from 'lucide-react';
import {
  StatsCard,
  PipelineFunnel,
  RecentActivity,
  QuickActions,
  type ActivityItem,
  type PipelineStage,
} from '@/modules/crm/components/dashboard';
import {
  useGetCustomersQuery,
  useGetLeadsQuery,
  useGetOpportunitiesQuery,
  useGetOpportunityPipelineQuery,
  useGetUpcomingActivitiesQuery,
} from '@/modules/crm/api/crmApi';
import type {
  Activity,
  ActivityType,
  OpportunityStage,
} from '@/modules/crm/types';

const stageColors: Record<OpportunityStage, PipelineStage['color']> = {
  PROSPECTING: 'blue',
  QUALIFICATION: 'indigo',
  PROPOSAL: 'yellow',
  NEGOTIATION: 'orange',
  CLOSED_WON: 'green',
  CLOSED_LOST: 'red',
};

const activityTypeMap: Record<ActivityType, ActivityItem['type']> = {
  CALL: 'call_made',
  EMAIL: 'email_sent',
  MEETING: 'meeting_scheduled',
  TASK: 'task_completed',
  NOTE: 'note_added',
  DEMO: 'meeting_scheduled',
  PROPOSAL: 'email_sent',
  FOLLOW_UP: 'task_completed',
};

const formatCurrency = (value: number) =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(value);

const toLocalDateTimeParam = (date: Date) => date.toISOString().slice(0, 19);

const formatStageName = (stage: OpportunityStage) =>
  stage
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(' ');

const toActivityItem = (activity: Activity): ActivityItem => ({
  id: activity.id,
  type: activityTypeMap[activity.type] ?? 'task_completed',
  title: activity.subject,
  description:
    activity.description ||
    `${activity.relatedTo?.type ?? 'CRM'}${activity.relatedTo?.name ? `: ${activity.relatedTo.name}` : ''}`,
  timestamp: activity.scheduledDate || activity.createdAt,
  user: activity.assignedToName ? { name: activity.assignedToName } : undefined,
});

export default function CRMDashboard() {
  const router = useRouter();

  const pagination = useMemo(() => ({ page: 1, limit: 5 }), []);
  const upcomingRange = useMemo(() => {
    const start = new Date();
    const end = new Date();
    end.setDate(start.getDate() + 30);

    return {
      startDate: toLocalDateTimeParam(start),
      endDate: toLocalDateTimeParam(end),
    };
  }, []);

  const { data: customersResponse, isLoading: isCustomersLoading } =
    useGetCustomersQuery({ pagination });
  const { data: leadsResponse, isLoading: isLeadsLoading } = useGetLeadsQuery({
    pagination,
  });
  const { data: opportunitiesResponse, isLoading: isOpportunitiesLoading } =
    useGetOpportunitiesQuery({ pagination });
  const { data: pipelineResponse, isLoading: isPipelineLoading } =
    useGetOpportunityPipelineQuery({});
  const { data: activitiesResponse, isLoading: isActivitiesLoading } =
    useGetUpcomingActivitiesQuery(upcomingRange);

  const customers = customersResponse?.data?.data ?? [];
  const pipeline = pipelineResponse?.data;
  const totalCustomers = customersResponse?.data?.pagination.total ?? 0;
  const totalLeads = leadsResponse?.data?.pagination.total ?? 0;
  const totalOpportunities =
    pipeline?.summary.totalOpportunities ??
    opportunitiesResponse?.data?.pagination.total ??
    0;
  const pipelineValue = pipeline?.summary.totalPipelineValue ?? 0;
  const averageDealSize = pipeline?.summary.averageDealSize ?? 0;
  const weightedPipelineValue = pipeline?.summary.weightedPipelineValue ?? 0;

  const pipelineStages = useMemo<PipelineStage[]>(() => {
    return (pipeline?.stages ?? []).map((stage) => ({
      name: formatStageName(stage.stage),
      count: stage.count,
      value: stage.totalValue,
      conversionRate:
        totalOpportunities > 0
          ? Math.round((stage.count / totalOpportunities) * 100)
          : 0,
      color: stageColors[stage.stage] ?? 'blue',
    }));
  }, [pipeline?.stages, totalOpportunities]);

  const upcomingActivities = useMemo(
    () => (activitiesResponse?.data ?? []).map(toActivityItem),
    [activitiesResponse?.data]
  );

  const isSummaryLoading =
    isCustomersLoading || isLeadsLoading || isOpportunitiesLoading;

  const quickActions = [
    {
      id: 'add-customer',
      label: 'Add Account',
      description: 'Create new account',
      icon: Users,
      variant: 'primary' as const,
      onClick: () => router.push('/crm/accounts/create'),
    },
    {
      id: 'add-lead',
      label: 'Add Lead',
      description: 'Capture new prospect',
      icon: Target,
      variant: 'warning' as const,
      onClick: () => router.push('/crm/leads/create'),
    },
    {
      id: 'create-opportunity',
      label: 'New Opportunity',
      description: 'Create deal',
      icon: Building2,
      variant: 'success' as const,
      onClick: () => router.push('/crm/opportunities/create'),
    },
    {
      id: 'meeting-requests',
      label: 'Meeting requests',
      description: 'Scheduling queue',
      icon: CalendarClock,
      variant: 'default' as const,
      onClick: () => router.push('/crm/meeting-requests'),
    },
  ];

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Dashboard</h1>
          <p className='text-muted-foreground'>
            Overview from live CRM records
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <span className='text-sm text-muted-foreground'>Last updated:</span>
          <span className='text-sm font-medium'>
            {new Date().toLocaleDateString('en-US', {
              month: 'short',
              day: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </span>
        </div>
      </div>

      <div className='grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4'>
        <StatsCard
          title='Total Accounts'
          value={totalCustomers}
          icon={Users}
          variant='primary'
          isLoading={isCustomersLoading}
        />
        <StatsCard
          title='Total Leads'
          value={totalLeads}
          icon={Target}
          variant='warning'
          isLoading={isLeadsLoading}
        />
        <StatsCard
          title='Opportunities'
          value={totalOpportunities}
          icon={Building2}
          variant='default'
          isLoading={isOpportunitiesLoading || isPipelineLoading}
        />
        <StatsCard
          title='Pipeline Value'
          value={formatCurrency(pipelineValue)}
          icon={DollarSign}
          variant='success'
          isLoading={isPipelineLoading}
        />
      </div>

      <div className='grid grid-cols-1 gap-4 sm:grid-cols-2'>
        <StatsCard
          title='Weighted Pipeline'
          value={formatCurrency(weightedPipelineValue)}
          icon={TrendingUp}
          subtitle='Expected value from open stages'
          isLoading={isPipelineLoading}
        />
        <StatsCard
          title='Avg. Deal Size'
          value={formatCurrency(averageDealSize)}
          icon={BarChart3}
          subtitle='From pipeline summary'
          isLoading={isPipelineLoading}
        />
      </div>

      <div className='grid grid-cols-1 gap-6 lg:grid-cols-3'>
        <div className='lg:col-span-2'>
          <PipelineFunnel
            stages={pipelineStages}
            title='Sales Pipeline'
            subtitle='Current opportunity stages'
            isLoading={isPipelineLoading}
            onStageClick={() => router.push('/crm/opportunities')}
          />
        </div>
        <QuickActions
          actions={quickActions}
          title='Quick Actions'
          columns={2}
        />
      </div>

      <div className='grid grid-cols-1 gap-6 lg:grid-cols-2'>
        <RecentActivity
          activities={upcomingActivities}
          title='Upcoming Activities'
          isLoading={isActivitiesLoading}
          maxItems={6}
          onViewAll={() => router.push('/crm/activities')}
          onActivityClick={(activity) =>
            router.push(`/crm/activities/${activity.id}`)
          }
        />

        <div className='rounded-xl border bg-card p-6'>
          <h3 className='mb-4 text-lg font-semibold'>Recent Accounts</h3>
          {isSummaryLoading ? (
            <p className='text-sm text-muted-foreground'>Loading accounts...</p>
          ) : customers.length > 0 ? (
            <div className='space-y-3'>
              {customers.map((customer) => (
                <button
                  key={customer.id}
                  type='button'
                  className='flex w-full items-center justify-between rounded-lg bg-muted/30 p-3 text-left transition-colors hover:bg-muted/50'
                  onClick={() => router.push(`/crm/accounts/${customer.id}`)}
                >
                  <div className='flex min-w-0 items-center gap-3'>
                    <div className='flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-semibold text-primary'>
                      {customer.name.charAt(0)}
                    </div>
                    <div className='min-w-0'>
                      <p className='truncate text-sm font-medium'>
                        {customer.name}
                      </p>
                      <p className='truncate text-xs text-muted-foreground'>
                        {customer.email || customer.companyName || 'Account'}
                      </p>
                    </div>
                  </div>
                  <div className='text-right'>
                    <p className='text-sm font-semibold text-primary'>
                      {customer.status}
                    </p>
                    <p className='text-xs text-muted-foreground'>Status</p>
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className='py-8 text-center'>
              <ArrowUpRight className='mx-auto mb-3 h-10 w-10 text-muted-foreground/40' />
              <p className='text-sm text-muted-foreground'>
                No accounts found yet.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
