// CRM Dashboard Page (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { Card, CardContent, CardHeader, Button } from '@/shared/components/ui';
import { MetricsCard } from '@/modules/crm/components/analytics';
import Link from 'next/link';

import { LayoutDashboard, Users, Target, Building2, Coins } from 'lucide-react';

export default function CRMDashboard() {
  // Mock data for demo
  const dashboardMetrics = {
    totalCustomers: 248,
    totalLeads: 89,
    totalOpportunities: 34,
    monthlyRevenue: 125000,
  };

  const pipelineData = [
    { stage: 'Prospecting', count: 12, value: 45000 },
    { stage: 'Qualification', count: 8, value: 32000 },
    { stage: 'Proposal', count: 6, value: 28000 },
    { stage: 'Negotiation', count: 4, value: 18000 },
    { stage: 'Closed Won', count: 4, value: 22000 },
  ];

  const salesData = [
    { month: 'Jan', sales: 85000 },
    { month: 'Feb', sales: 92000 },
    { month: 'Mar', sales: 78000 },
    { month: 'Apr', sales: 105000 },
    { month: 'May', sales: 125000 },
    { month: 'Jun', sales: 110000 },
  ];

  return (
    <div className='space-y-6'>
      {/* Key Metrics */}
      <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6'>
        <MetricsCard
          title='Total Customers'
          value={dashboardMetrics.totalCustomers}
          icon={<Users />}
        />
        <MetricsCard
          title='Active Leads'
          value={dashboardMetrics.totalLeads}
          icon={<Target />}
        />
        <MetricsCard
          title='Opportunities'
          value={dashboardMetrics.totalOpportunities}
          icon={<Building2 />}
        />
        <MetricsCard
          title='Monthly Revenue'
          value={`$${dashboardMetrics.monthlyRevenue.toLocaleString()}`}
          icon={<Coins />}
        />
      </div>

      {/* Charts */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        <Card>
          <CardHeader>
            <h3 className='text-lg font-semibold'>Sales Pipeline Overview</h3>
          </CardHeader>
          <CardContent>
            <div className='space-y-3'>
              {pipelineData.map((stage, index) => (
                <div
                  key={index}
                  className='flex items-center justify-between p-3 bg-muted rounded'
                >
                  <span className='font-medium'>{stage.stage}</span>
                  <div className='text-right'>
                    <div className='font-semibold'>{stage.count} deals</div>
                    <div className='text-sm text-muted-foreground'>
                      ${stage.value.toLocaleString()}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <h3 className='text-lg font-semibold'>Sales Trend</h3>
          </CardHeader>
          <CardContent>
            <div className='space-y-3'>
              {salesData.map((month, index) => (
                <div
                  key={index}
                  className='flex items-center justify-between p-3 bg-muted rounded'
                >
                  <span className='font-medium'>{month.month}</span>
                  <span className='font-semibold'>
                    ${month.sales.toLocaleString()}
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
        <Card>
          <CardHeader>
            <h3 className='text-lg font-semibold'>Customers</h3>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Manage your customer relationships
            </p>
            <div className='space-y-2'>
              <Link href='/crm/customers' className='block'>
                <Button variant='outline' className='w-full'>
                  View All Customers
                </Button>
              </Link>
              <Link href='/crm/customers/create' className='block'>
                <Button variant='outline' className='w-full'>
                  Add New Customer
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <h3 className='text-lg font-semibold'>Leads</h3>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Track and convert prospects
            </p>
            <div className='space-y-2'>
              <Link href='/crm/leads' className='block'>
                <Button variant='outline' className='w-full'>
                  View All Leads
                </Button>
              </Link>
              <Link href='/crm/leads/create' className='block'>
                <Button variant='outline' className='w-full'>
                  Add New Lead
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <h3 className='text-lg font-semibold'>Opportunities</h3>
          </CardHeader>
          <CardContent>
            <p className='text-sm text-muted-foreground mb-4'>
              Manage your sales pipeline
            </p>
            <div className='space-y-2'>
              <Link href='/crm/opportunities' className='block'>
                <Button variant='outline' className='w-full'>
                  View Pipeline
                </Button>
              </Link>
              <Link href='/crm/opportunities/create' className='block'>
                <Button variant='outline' className='w-full'>
                  Add Opportunity
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity */}
      <Card>
        <CardHeader>
          <h3 className='text-lg font-semibold'>Recent Activity</h3>
        </CardHeader>
        <CardContent>
          <div className='space-y-4'>
            <div className='flex items-center space-x-3 p-3 bg-muted rounded-lg'>
              <div className='w-2 h-2 bg-green-500 rounded-full'></div>
              <div className='flex-1'>
                <p className='text-sm font-medium'>
                  New customer "Acme Corp" added
                </p>
                <p className='text-xs text-muted-foreground'>2 hours ago</p>
              </div>
            </div>
            <div className='flex items-center space-x-3 p-3 bg-muted rounded-lg'>
              <div className='w-2 h-2 bg-blue-500 rounded-full'></div>
              <div className='flex-1'>
                <p className='text-sm font-medium'>
                  Lead "John Doe" converted to customer
                </p>
                <p className='text-xs text-muted-foreground'>4 hours ago</p>
              </div>
            </div>
            <div className='flex items-center space-x-3 p-3 bg-muted rounded-lg'>
              <div className='w-2 h-2 bg-purple-500 rounded-full'></div>
              <div className='flex-1'>
                <p className='text-sm font-medium'>
                  Opportunity "Enterprise Deal" moved to Negotiation
                </p>
                <p className='text-xs text-muted-foreground'>6 hours ago</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
