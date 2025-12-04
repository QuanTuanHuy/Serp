// CustomerDetailPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState } from 'react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  Badge,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetCustomerQuery,
  useGetActivitiesQuery,
  useGetOpportunitiesQuery,
} from '../../api/crmApi';
import { StatusBadge, ActionMenu, EntityCard } from '../../components/shared';
import { CustomerForm } from '../../components/forms';
import type { Customer } from '../../types';

interface CustomerDetailPageProps {
  customerId: string;
  className?: string;
}

export const CustomerDetailPage: React.FC<CustomerDetailPageProps> = ({
  customerId,
  className,
}) => {
  const [editMode, setEditMode] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');

  // Fetch customer data
  const {
    data: customerResponse,
    isLoading,
    error,
  } = useGetCustomerQuery(customerId);
  // Mock activities and opportunities for now
  const activities: any[] = [];
  const opportunities: any[] = [];

  const customer = customerResponse?.data;

  const handleUpdateCustomer = async (data: any) => {
    // Implementation will use updateCustomerMutation
    console.log('Updating customer:', data);
    setEditMode(false);
  };

  const handleDeleteCustomer = async () => {
    // Implementation will use deleteCustomerMutation
    console.log('Deleting customer:', customerId);
  };

  // Show edit form
  if (editMode && customer) {
    return (
      <div className={cn('p-6', className)}>
        <CustomerForm
          customer={customer}
          onSubmit={handleUpdateCustomer}
          onCancel={() => setEditMode(false)}
        />
      </div>
    );
  }

  // Loading state
  if (isLoading) {
    return (
      <div className={cn('p-6', className)}>
        <div className='animate-pulse space-y-6'>
          <div className='h-8 bg-muted rounded w-1/3'></div>
          <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
            <div className='md:col-span-2'>
              <div className='h-64 bg-muted rounded'></div>
            </div>
            <div className='h-64 bg-muted rounded'></div>
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error || !customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card className='border-red-200 bg-red-50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 mb-2'>
              Customer Not Found
            </h3>
            <p className='text-red-600 mb-4'>
              The customer you're looking for doesn't exist or has been deleted.
            </p>
            <Button variant='outline' onClick={() => window.history.back()}>
              Go Back
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className={cn('p-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <div className='flex items-center space-x-4'>
          <Button variant='outline' onClick={() => window.history.back()}>
            ← Back
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              {customer.name}
            </h1>
            <div className='flex items-center space-x-2 mt-1'>
              <StatusBadge status={customer.status} />
              <Badge
                variant={
                  customer.customerType === 'COMPANY' ? 'default' : 'secondary'
                }
              >
                {customer.customerType}
              </Badge>
            </div>
          </div>
        </div>

        <div className='flex items-center space-x-2'>
          <Button variant='outline' onClick={() => setEditMode(true)}>
            Edit
          </Button>
          <ActionMenu
            items={[
              {
                label: 'Edit Customer',
                onClick: () => setEditMode(true),
              },
              {
                label: 'Delete Customer',
                onClick: handleDeleteCustomer,
                variant: 'destructive',
              },
            ]}
          />
        </div>
      </div>

      {/* Main Content */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className='mb-6'>
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='activities'>
            Activities ({activities.length})
          </TabsTrigger>
          <TabsTrigger value='opportunities'>
            Opportunities ({opportunities.length})
          </TabsTrigger>
          <TabsTrigger value='documents'>Documents</TabsTrigger>
        </TabsList>

        <TabsContent value='overview'>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            {/* Main Information */}
            <div className='lg:col-span-2 space-y-6'>
              {/* Contact Information */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Contact Information</h3>
                </CardHeader>
                <CardContent>
                  <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Email
                      </label>
                      <p className='text-sm'>{customer.email}</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Phone
                      </label>
                      <p className='text-sm'>
                        {customer.phone || 'Not provided'}
                      </p>
                    </div>
                    <div className='md:col-span-2'>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Address
                      </label>
                      <p className='text-sm'>
                        {customer.address || 'Not provided'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Company Information (if applicable) */}
              {customer.customerType === 'COMPANY' && (
                <Card>
                  <CardHeader>
                    <h3 className='text-lg font-semibold'>
                      Company Information
                    </h3>
                  </CardHeader>
                  <CardContent>
                    <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Company Name
                        </label>
                        <p className='text-sm'>
                          {customer.companyName || 'Not specified'}
                        </p>
                      </div>
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Business Type
                        </label>
                        <p className='text-sm'>{'Not specified'}</p>
                      </div>
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Website
                        </label>
                        <p className='text-sm'>
                          {customer.website ? (
                            <a
                              href={customer.website}
                              target='_blank'
                              rel='noopener noreferrer'
                              className='text-blue-600 hover:underline'
                            >
                              {customer.website}
                            </a>
                          ) : (
                            'Not provided'
                          )}
                        </p>
                      </div>
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Tax Number
                        </label>
                        <p className='text-sm'>
                          {customer.taxNumber || 'Not provided'}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Notes */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Notes</h3>
                </CardHeader>
                <CardContent>
                  <p className='text-sm text-muted-foreground'>
                    {customer.notes || 'No notes available.'}
                  </p>
                </CardContent>
              </Card>
            </div>

            {/* Sidebar */}
            <div className='space-y-6'>
              {/* Key Metrics */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Key Metrics</h3>
                </CardHeader>
                <CardContent>
                  <div className='space-y-4'>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Total Value
                      </label>
                      <p className='text-lg font-semibold'>
                        ${customer.totalValue?.toLocaleString() || '0'}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Last Contact
                      </label>
                      <p className='text-sm'>
                        {customer.lastContactDate
                          ? new Date(
                              customer.lastContactDate
                            ).toLocaleDateString()
                          : 'Never'}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Customer Since
                      </label>
                      <p className='text-sm'>
                        {new Date(customer.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Sales Representative */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>
                    Sales Representative
                  </h3>
                </CardHeader>
                <CardContent>
                  <p className='text-sm'>
                    {customer.assignedSalesRep || 'Unassigned'}
                  </p>
                </CardContent>
              </Card>

              {/* Tags */}
              {customer.tags && customer.tags.length > 0 && (
                <Card>
                  <CardHeader>
                    <h3 className='text-lg font-semibold'>Tags</h3>
                  </CardHeader>
                  <CardContent>
                    <div className='flex flex-wrap gap-2'>
                      {customer.tags.map((tag, index) => (
                        <Badge key={index} variant='outline'>
                          {tag}
                        </Badge>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Quick Actions */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Quick Actions</h3>
                </CardHeader>
                <CardContent>
                  <div className='space-y-2'>
                    <Button className='w-full' variant='outline'>
                      Create Opportunity
                    </Button>
                    <Button className='w-full' variant='outline'>
                      Log Activity
                    </Button>
                    <Button className='w-full' variant='outline'>
                      Send Email
                    </Button>
                    <Button className='w-full' variant='outline'>
                      Schedule Call
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        <TabsContent value='activities'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <h3 className='text-lg font-semibold'>Recent Activities</h3>
                <Button>Log New Activity</Button>
              </div>
            </CardHeader>
            <CardContent>
              {activities.length > 0 ? (
                <div className='space-y-4'>
                  {activities.map((activity) => (
                    <div
                      key={activity.id}
                      className='border-l-4 border-blue-500 pl-4 py-2'
                    >
                      <div className='flex items-center justify-between'>
                        <h4 className='font-medium'>{activity.title}</h4>
                        <span className='text-sm text-muted-foreground'>
                          {new Date(activity.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                      <p className='text-sm text-muted-foreground mt-1'>
                        {activity.description}
                      </p>
                      <Badge variant='outline' className='mt-2'>
                        {activity.type}
                      </Badge>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='text-center py-8'>
                  <p className='text-muted-foreground mb-4'>
                    No activities recorded yet.
                  </p>
                  <Button>Log First Activity</Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='opportunities'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <h3 className='text-lg font-semibold'>Opportunities</h3>
                <Button>Create Opportunity</Button>
              </div>
            </CardHeader>
            <CardContent>
              {opportunities.length > 0 ? (
                <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
                  {opportunities.map((opportunity) => (
                    <EntityCard
                      key={opportunity.id}
                      type='opportunity'
                      entity={opportunity}
                      onClick={() => {
                        // Navigate to opportunity detail
                        console.log('View opportunity:', opportunity.id);
                      }}
                    />
                  ))}
                </div>
              ) : (
                <div className='text-center py-8'>
                  <p className='text-muted-foreground mb-4'>
                    No opportunities yet.
                  </p>
                  <Button>Create First Opportunity</Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='documents'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <h3 className='text-lg font-semibold'>Documents</h3>
                <Button>Upload Document</Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className='text-center py-8'>
                <p className='text-muted-foreground mb-4'>
                  No documents uploaded yet.
                </p>
                <Button variant='outline'>Upload First Document</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default CustomerDetailPage;
