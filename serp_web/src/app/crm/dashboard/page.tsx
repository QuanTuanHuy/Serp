/**
 * CRM Dashboard Page - Modern dashboard with analytics
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
} from 'lucide-react';
import {
  StatsCard,
  PipelineFunnel,
  SalesTrendChart,
  RecentActivity,
  QuickActions,
} from '@/modules/crm/components/dashboard';
import {
  MOCK_CUSTOMERS,
  MOCK_LEADS,
  MOCK_OPPORTUNITIES,
  MOCK_PIPELINE_DATA,
  MOCK_SALES_TREND,
  MOCK_DASHBOARD_ACTIVITIES,
} from '@/modules/crm/mocks';

export default function CRMDashboard() {
  const router = useRouter();

  // Calculate dashboard metrics from mock data
  const dashboardMetrics = useMemo(() => {
    const totalCustomers = MOCK_CUSTOMERS.length;
    const activeCustomers = MOCK_CUSTOMERS.filter(
      (c) => c.status === 'ACTIVE'
    ).length;

    const totalLeads = MOCK_LEADS.length;
    const newLeads = MOCK_LEADS.filter((l) => l.status === 'NEW').length;

    const totalOpportunities = MOCK_OPPORTUNITIES.length;
    const pipelineValue = MOCK_OPPORTUNITIES.filter(
      (o) => o.stage !== 'CLOSED_WON' && o.stage !== 'CLOSED_LOST'
    ).reduce((sum, o) => sum + (o.value || 0), 0);

    const wonDeals = MOCK_OPPORTUNITIES.filter((o) => o.stage === 'CLOSED_WON');
    const closedDeals = MOCK_OPPORTUNITIES.filter(
      (o) => o.stage === 'CLOSED_WON' || o.stage === 'CLOSED_LOST'
    ).length;
    const winRate =
      closedDeals > 0 ? Math.round((wonDeals.length / closedDeals) * 100) : 0;

    const monthlyRevenue = wonDeals.reduce((sum, o) => sum + (o.value || 0), 0);
    const avgDealSize =
      totalOpportunities > 0
        ? Math.round(pipelineValue / totalOpportunities)
        : 0;
    const conversionRate =
      totalLeads > 0
        ? Math.round((wonDeals.length / totalLeads) * 100 * 10) / 10
        : 0;

    return {
      totalCustomers,
      activeCustomers,
      totalLeads,
      newLeads,
      totalOpportunities,
      monthlyRevenue,
      conversionRate,
      avgDealSize,
      winRate,
      pipelineValue,
    };
  }, []);

  const quickActions = [
    {
      id: 'add-customer',
      label: 'Add Customer',
      description: 'Create new customer',
      icon: Users,
      variant: 'primary' as const,
      onClick: () => router.push('/crm/customers/create'),
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
  ];

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Dashboard</h1>
          <p className='text-muted-foreground'>
            Overview of your CRM performance
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

      {/* Key Metrics */}
      <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4'>
        <StatsCard
          title='Total Customers'
          value={dashboardMetrics.totalCustomers}
          icon={Users}
          variant='primary'
          trend={{ value: 12.5, isPositive: true, label: 'vs last month' }}
        />
        <StatsCard
          title='Active Leads'
          value={dashboardMetrics.totalLeads}
          icon={Target}
          variant='warning'
          trend={{ value: 8.2, isPositive: true, label: 'vs last month' }}
        />
        <StatsCard
          title='Open Opportunities'
          value={dashboardMetrics.totalOpportunities}
          icon={Building2}
          variant='default'
          trend={{ value: 3.1, isPositive: false, label: 'vs last month' }}
        />
        <StatsCard
          title='Monthly Revenue'
          value={`$${dashboardMetrics.monthlyRevenue.toLocaleString()}`}
          icon={DollarSign}
          variant='success'
          trend={{ value: 15.3, isPositive: true, label: 'vs last month' }}
        />
      </div>

      {/* Secondary Metrics */}
      <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4'>
        <StatsCard
          title='Conversion Rate'
          value={`${dashboardMetrics.conversionRate}%`}
          icon={TrendingUp}
          subtitle='Lead to Customer'
        />
        <StatsCard
          title='Avg. Deal Size'
          value={`$${dashboardMetrics.avgDealSize.toLocaleString()}`}
          icon={BarChart3}
          subtitle='Across all deals'
        />
        <StatsCard
          title='Win Rate'
          value={`${dashboardMetrics.winRate}%`}
          icon={ArrowUpRight}
          subtitle='Opportunities won'
          trend={{ value: 5.2, isPositive: true }}
        />
      </div>

      {/* Charts Section */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        {/* Sales Pipeline */}
        <PipelineFunnel
          stages={MOCK_PIPELINE_DATA}
          title='Sales Pipeline'
          subtitle='Current quarter performance'
        />

        {/* Sales Trend */}
        <SalesTrendChart
          data={MOCK_SALES_TREND}
          title='Sales Trend'
          subtitle='Monthly revenue over time'
        />
      </div>

      {/* Activity & Quick Actions */}
      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
        {/* Recent Activity - Takes 2 columns */}
        <div className='lg:col-span-2'>
          <RecentActivity
            activities={MOCK_DASHBOARD_ACTIVITIES}
            title='Recent Activity'
            maxItems={6}
            onViewAll={() => router.push('/crm/activities')}
          />
        </div>

        {/* Quick Actions */}
        <QuickActions
          actions={quickActions}
          title='Quick Actions'
          columns={2}
        />
      </div>

      {/* Top Performers Section */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        {/* Top Customers */}
        <div className='rounded-xl border bg-card p-6'>
          <h3 className='text-lg font-semibold mb-4'>Top Customers</h3>
          <div className='space-y-3'>
            {MOCK_CUSTOMERS.slice(0, 5).map((customer) => (
              <div
                key={customer.id}
                className='flex items-center justify-between p-3 rounded-lg bg-muted/30 hover:bg-muted/50 transition-colors cursor-pointer'
                onClick={() => router.push(`/crm/customers/${customer.id}`)}
              >
                <div className='flex items-center gap-3'>
                  <div className='flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary font-semibold text-sm'>
                    {customer.name.charAt(0)}
                  </div>
                  <div>
                    <p className='font-medium text-sm'>{customer.name}</p>
                    <p className='text-xs text-muted-foreground'>
                      {customer.customerType === 'COMPANY'
                        ? 'Business'
                        : 'Individual'}
                    </p>
                  </div>
                </div>
                <div className='text-right'>
                  <p className='font-semibold text-sm text-primary'>
                    {customer.status === 'ACTIVE' ? 'Active' : customer.status}
                  </p>
                  <p className='text-xs text-muted-foreground'>Status</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Upcoming Tasks */}
        <div className='rounded-xl border bg-card p-6'>
          <h3 className='text-lg font-semibold mb-4'>Upcoming Tasks</h3>
          <div className='space-y-3'>
            {[
              {
                title: 'Follow up with TechStart',
                type: 'Call',
                dueDate: 'Today, 2:00 PM',
                priority: 'high',
              },
              {
                title: 'Send proposal to DataFlow',
                type: 'Email',
                dueDate: 'Tomorrow, 10:00 AM',
                priority: 'medium',
              },
              {
                title: 'Demo meeting with CloudServe',
                type: 'Meeting',
                dueDate: 'Dec 5, 3:00 PM',
                priority: 'high',
              },
              {
                title: 'Review contract terms',
                type: 'Task',
                dueDate: 'Dec 6, 5:00 PM',
                priority: 'low',
              },
              {
                title: 'Quarterly review prep',
                type: 'Task',
                dueDate: 'Dec 8, 9:00 AM',
                priority: 'medium',
              },
            ].map((task, index) => (
              <div
                key={index}
                className='flex items-center justify-between p-3 rounded-lg border border-border/50 hover:border-border transition-colors cursor-pointer'
              >
                <div className='flex items-center gap-3'>
                  <div
                    className={`h-2 w-2 rounded-full ${
                      task.priority === 'high'
                        ? 'bg-rose-500'
                        : task.priority === 'medium'
                          ? 'bg-amber-500'
                          : 'bg-emerald-500'
                    }`}
                  />
                  <div>
                    <p className='font-medium text-sm'>{task.title}</p>
                    <p className='text-xs text-muted-foreground'>{task.type}</p>
                  </div>
                </div>
                <div className='text-right'>
                  <p className='text-xs font-medium text-muted-foreground'>
                    {task.dueDate}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
