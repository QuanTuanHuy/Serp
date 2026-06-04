/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin dashboard page
 */

'use client';

import Link from 'next/link';
import React from 'react';
import {
  AlertTriangle,
  Building2,
  CheckCircle2,
  Clock,
  CreditCard,
  Package,
  Puzzle,
  RefreshCw,
  ShieldAlert,
  Users,
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import {
  AdminStatsCard,
  AdminStatusBadge,
  useGetAdminDashboardQuery,
} from '@/modules/admin';
import type { AdminDashboardStatusCount } from '@/modules/admin';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';

const formatNumber = (value: number | undefined) =>
  new Intl.NumberFormat('en-US').format(value ?? 0);

const formatGeneratedAt = (value: number | undefined) => {
  if (!value) return '';

  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
};

const filterVisibleStatuses = (items: AdminDashboardStatusCount[]) =>
  items.filter((item) => item.count > 0);

export default function AdminDashboardPage() {
  const {
    data: dashboard,
    isError,
    isLoading,
    refetch,
  } = useGetAdminDashboardQuery();

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='space-y-2'>
          <Skeleton className='h-8 w-64' />
          <Skeleton className='h-4 w-96 max-w-full' />
        </div>
        <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
          {Array.from({ length: 4 }).map((_, index) => (
            <Skeleton key={index} className='h-32 rounded-lg' />
          ))}
        </div>
        <div className='grid gap-4 xl:grid-cols-[1.1fr_0.9fr]'>
          <Skeleton className='h-80 rounded-lg' />
          <Skeleton className='h-80 rounded-lg' />
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <Card className='max-w-xl'>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <AlertTriangle className='h-5 w-5 text-destructive' />
            Dashboard unavailable
          </CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <p className='text-sm text-muted-foreground'>
            The account service did not return dashboard data.
          </p>
          <Button onClick={() => refetch()} className='gap-2'>
            <RefreshCw className='h-4 w-4' />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  if (!dashboard) {
    return (
      <Card className='max-w-xl'>
        <CardHeader>
          <CardTitle className='text-base'>No dashboard data</CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-sm text-muted-foreground'>
            No account-service operations data is available yet.
          </p>
        </CardContent>
      </Card>
    );
  }

  const metrics = dashboard.metrics;
  const actionQueue = dashboard.actionQueue;
  const coverage = dashboard.configurationCoverage;
  const subscriptionStatusData = filterVisibleStatuses(
    dashboard.subscriptionStatuses
  );
  const generatedAt = formatGeneratedAt(dashboard.generatedAt);

  const actionItems = [
    {
      label: 'Pending subscription approvals',
      value: actionQueue.pendingSubscriptions,
      icon: Clock,
      href: '/admin/subscriptions',
    },
    {
      label: 'Subscriptions ending soon',
      value: actionQueue.subscriptionsEndingSoon,
      icon: AlertTriangle,
      href: '/admin/subscriptions',
    },
    {
      label: 'Trials ending soon',
      value: actionQueue.trialsEndingSoon,
      icon: CreditCard,
      href: '/admin/subscriptions',
    },
    {
      label: 'Suspended organizations',
      value: actionQueue.suspendedOrganizations,
      icon: ShieldAlert,
      href: '/admin/organizations',
    },
    {
      label: 'Expired organizations',
      value: actionQueue.expiredOrganizations,
      icon: Building2,
      href: '/admin/organizations',
    },
  ];

  const coverageItems = [
    {
      label: 'Plans',
      activeLabel: 'active',
      inactiveLabel: 'inactive',
      total: coverage.totalPlans,
      active: coverage.activePlans,
      inactive: coverage.inactivePlans,
      icon: Package,
      href: '/admin/plans',
    },
    {
      label: 'Modules',
      activeLabel: 'available',
      inactiveLabel: 'unavailable',
      total: coverage.totalModules,
      active: coverage.availableModules,
      inactive: coverage.unavailableModules,
      icon: Puzzle,
      href: '/admin/modules',
    },
    {
      label: 'Menu displays',
      activeLabel: 'visible',
      inactiveLabel: 'hidden',
      total: coverage.totalMenuDisplays,
      active: coverage.visibleMenuDisplays,
      inactive: coverage.hiddenMenuDisplays,
      icon: CheckCircle2,
      href: '/admin/menu-displays',
    },
  ];

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2 md:flex-row md:items-end md:justify-between'>
        <div>
          <h1 className='text-2xl font-semibold tracking-tight'>
            System Operations
          </h1>
          <p className='text-sm text-muted-foreground'>
            Account-service overview for organizations, users, subscriptions,
            and admin configuration.
          </p>
        </div>
        {generatedAt && (
          <p className='text-xs text-muted-foreground'>
            Generated {generatedAt}
          </p>
        )}
      </div>

      <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
        <AdminStatsCard
          title='Organizations'
          value={formatNumber(metrics.totalOrganizations)}
          description={`${formatNumber(metrics.activeOrganizations)} active`}
          icon={<Building2 className='h-4 w-4' />}
        />
        <AdminStatsCard
          title='Users'
          value={formatNumber(metrics.totalUsers)}
          description={`${formatNumber(metrics.suspendedUsers)} suspended`}
          icon={<Users className='h-4 w-4' />}
        />
        <AdminStatsCard
          title='Subscriptions'
          value={formatNumber(metrics.totalSubscriptions)}
          description={`${formatNumber(metrics.activeSubscriptions)} active`}
          icon={<CreditCard className='h-4 w-4' />}
        />
        <AdminStatsCard
          title='Pending Work'
          value={formatNumber(actionQueue.pendingSubscriptions)}
          description='subscription approvals'
          icon={<Clock className='h-4 w-4' />}
        />
      </div>

      <div className='grid gap-4 xl:grid-cols-[1.1fr_0.9fr]'>
        <Card>
          <CardHeader className='pb-3'>
            <CardTitle className='text-base'>Action Queue</CardTitle>
          </CardHeader>
          <CardContent className='space-y-2'>
            {actionItems.map((item) => {
              const Icon = item.icon;

              return (
                <Link
                  key={item.label}
                  href={item.href}
                  className='flex items-center justify-between rounded-md border px-3 py-2 transition-colors hover:bg-muted'
                >
                  <span className='flex min-w-0 items-center gap-3'>
                    <Icon className='h-4 w-4 shrink-0 text-muted-foreground' />
                    <span className='truncate text-sm font-medium'>
                      {item.label}
                    </span>
                  </span>
                  <span className='text-sm font-semibold'>
                    {formatNumber(item.value)}
                  </span>
                </Link>
              );
            })}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='pb-3'>
            <CardTitle className='text-base'>Subscription Status</CardTitle>
          </CardHeader>
          <CardContent>
            {subscriptionStatusData.length > 0 ? (
              <div className='h-72'>
                <ResponsiveContainer width='100%' height='100%'>
                  <BarChart data={subscriptionStatusData}>
                    <CartesianGrid strokeDasharray='3 3' vertical={false} />
                    <XAxis
                      dataKey='status'
                      tick={{ fontSize: 11 }}
                      tickLine={false}
                      axisLine={false}
                    />
                    <YAxis
                      allowDecimals={false}
                      tick={{ fontSize: 11 }}
                      tickLine={false}
                      axisLine={false}
                    />
                    <Tooltip />
                    <Bar
                      dataKey='count'
                      fill='hsl(var(--primary))'
                      radius={4}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <p className='text-sm text-muted-foreground'>
                No subscription records found.
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      <div className='grid gap-4 xl:grid-cols-[1.1fr_0.9fr]'>
        <Card>
          <CardHeader className='flex flex-row items-center justify-between pb-3'>
            <CardTitle className='text-base'>Recent Organizations</CardTitle>
            <Button asChild variant='ghost' size='sm'>
              <Link href='/admin/organizations'>View all</Link>
            </Button>
          </CardHeader>
          <CardContent>
            {dashboard.recentOrganizations.length > 0 ? (
              <div className='space-y-2'>
                {dashboard.recentOrganizations.map((organization) => (
                  <Link
                    key={organization.id}
                    href='/admin/organizations'
                    className='grid gap-3 rounded-md border p-3 transition-colors hover:bg-muted md:grid-cols-[1fr_auto]'
                  >
                    <div className='min-w-0 space-y-1'>
                      <p className='truncate text-sm font-medium'>
                        {organization.name}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        {organization.code} ·{' '}
                        {formatNumber(organization.userCount)} users
                      </p>
                    </div>
                    <div className='flex flex-wrap items-center gap-2 md:justify-end'>
                      <AdminStatusBadge
                        status={organization.status ?? 'UNKNOWN'}
                      />
                      <AdminStatusBadge
                        status={organization.subscriptionStatus}
                      />
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <p className='text-sm text-muted-foreground'>
                No organizations have been created yet.
              </p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className='pb-3'>
            <CardTitle className='text-base'>Configuration Coverage</CardTitle>
          </CardHeader>
          <CardContent className='space-y-3'>
            {coverageItems.map((item) => {
              const Icon = item.icon;

              return (
                <Link
                  key={item.label}
                  href={item.href}
                  className='flex items-center justify-between rounded-md border p-3 transition-colors hover:bg-muted'
                >
                  <span className='flex min-w-0 items-center gap-3'>
                    <Icon className='h-4 w-4 shrink-0 text-muted-foreground' />
                    <span className='min-w-0'>
                      <span className='block truncate text-sm font-medium'>
                        {item.label}
                      </span>
                      <span className='block text-xs text-muted-foreground'>
                        {formatNumber(item.active)} {item.activeLabel} ·{' '}
                        {formatNumber(item.inactive)} {item.inactiveLabel}
                      </span>
                    </span>
                  </span>
                  <span className='text-sm font-semibold'>
                    {formatNumber(item.total)}
                  </span>
                </Link>
              );
            })}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
