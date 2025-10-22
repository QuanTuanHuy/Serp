/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin dashboard page
 */

'use client';

import React from 'react';
import { AdminStatsCard, AdminStatusBadge } from '@/modules/admin';
import {
  Building2,
  CreditCard,
  Package,
  Users,
  TrendingUp,
  Activity,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';

export default function AdminDashboardPage() {
  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div>
        <h1 className='text-3xl font-bold tracking-tight'>System Dashboard</h1>
        <p className='text-muted-foreground mt-2'>
          Overview of system-wide statistics and recent activities
        </p>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <AdminStatsCard
          title='Total Organizations'
          value={128}
          description='Active organizations'
          icon={<Building2 className='h-4 w-4' />}
          trend={{ value: 12.5, label: 'vs last month' }}
        />

        <AdminStatsCard
          title='Active Subscriptions'
          value={245}
          description='Paid subscriptions'
          icon={<CreditCard className='h-4 w-4' />}
          trend={{ value: 8.2, label: 'vs last month' }}
        />

        <AdminStatsCard
          title='Total Users'
          value='3.2K'
          description='Across all organizations'
          icon={<Users className='h-4 w-4' />}
          trend={{ value: 15.8, label: 'vs last month' }}
        />

        <AdminStatsCard
          title='Revenue (MRR)'
          value='$24.5K'
          description='Monthly recurring revenue'
          icon={<TrendingUp className='h-4 w-4' />}
          trend={{ value: 5.7, label: 'vs last month' }}
        />
      </div>

      {/* Recent Activity & Quick Stats */}
      <div className='grid gap-4 md:grid-cols-2'>
        {/* Recent Organizations */}
        <Card>
          <CardHeader>
            <CardTitle className='text-base font-medium'>
              Recent Organizations
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className='space-y-3'>
              {[
                { name: 'Acme Corporation', status: 'ACTIVE', users: 45 },
                { name: 'TechStart Inc', status: 'TRIAL', users: 12 },
                { name: 'Global Solutions', status: 'ACTIVE', users: 78 },
                { name: 'Innovation Labs', status: 'SUSPENDED', users: 23 },
              ].map((org, index) => (
                <div
                  key={index}
                  className='flex items-center justify-between py-2 border-b last:border-0'
                >
                  <div className='flex-1'>
                    <p className='text-sm font-medium'>{org.name}</p>
                    <p className='text-xs text-muted-foreground'>
                      {org.users} users
                    </p>
                  </div>
                  <AdminStatusBadge status={org.status} />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Subscription Overview */}
        <Card>
          <CardHeader>
            <CardTitle className='text-base font-medium'>
              Subscription Status
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className='space-y-3'>
              {[
                { label: 'Active Subscriptions', count: 245, status: 'ACTIVE' },
                { label: 'Trial Period', count: 38, status: 'TRIAL' },
                { label: 'Pending Renewal', count: 12, status: 'PENDING' },
                { label: 'Expired', count: 5, status: 'EXPIRED' },
              ].map((item, index) => (
                <div
                  key={index}
                  className='flex items-center justify-between py-2 border-b last:border-0'
                >
                  <div className='flex-1'>
                    <p className='text-sm font-medium'>{item.label}</p>
                    <p className='text-xs text-muted-foreground'>
                      {item.count} subscriptions
                    </p>
                  </div>
                  <AdminStatusBadge status={item.status} />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* System Health */}
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Activity className='h-5 w-5' />
            System Health
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid gap-4 md:grid-cols-3'>
            <div className='space-y-2'>
              <p className='text-sm font-medium text-muted-foreground'>
                API Status
              </p>
              <div className='flex items-center gap-2'>
                <div className='h-2 w-2 rounded-full bg-green-500'></div>
                <p className='text-sm font-semibold'>Operational</p>
              </div>
            </div>

            <div className='space-y-2'>
              <p className='text-sm font-medium text-muted-foreground'>
                Database
              </p>
              <div className='flex items-center gap-2'>
                <div className='h-2 w-2 rounded-full bg-green-500'></div>
                <p className='text-sm font-semibold'>Healthy</p>
              </div>
            </div>

            <div className='space-y-2'>
              <p className='text-sm font-medium text-muted-foreground'>
                Background Jobs
              </p>
              <div className='flex items-center gap-2'>
                <div className='h-2 w-2 rounded-full bg-green-500'></div>
                <p className='text-sm font-semibold'>Running</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
