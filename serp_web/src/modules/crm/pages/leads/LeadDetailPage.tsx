// LeadDetailPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
import { useGetLeadQuery } from '../../api/crmApi';
import { StatusBadge, ActionMenu, EntityCard } from '../../components/shared';
import { LeadForm } from '../../components/forms';
import type { Lead } from '../../types';

interface LeadDetailPageProps {
  leadId: string;
  className?: string;
}

export const LeadDetailPage: React.FC<LeadDetailPageProps> = ({
  leadId,
  className,
}) => {
  const [editMode, setEditMode] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');

  // Fetch lead data
  const { data: leadResponse, isLoading, error } = useGetLeadQuery(leadId);
  const lead = leadResponse?.data;

  const handleUpdateLead = async (data: any) => {
    console.log('Updating lead:', data);
    setEditMode(false);
  };

  const handleDeleteLead = async () => {
    console.log('Deleting lead:', leadId);
  };

  const handleConvertLead = async () => {
    console.log('Converting lead to customer:', leadId);
  };

  // Show edit form
  if (editMode && lead) {
    return (
      <div className={cn('p-6', className)}>
        <LeadForm
          lead={lead}
          onSubmit={handleUpdateLead}
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
  if (error || !lead) {
    return (
      <div className={cn('p-6', className)}>
        <Card className='border-red-200 bg-red-50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 mb-2'>
              Lead Not Found
            </h3>
            <p className='text-red-600 mb-4'>
              The lead you're looking for doesn't exist or has been deleted.
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
              {lead.firstName} {lead.lastName}
            </h1>
            <div className='flex items-center space-x-2 mt-1'>
              <StatusBadge status={lead.status} />
              <Badge variant='outline'>{lead.source}</Badge>
            </div>
          </div>
        </div>

        <div className='flex items-center space-x-2'>
          {lead.status === 'QUALIFIED' && (
            <Button
              onClick={handleConvertLead}
              className='bg-green-600 hover:bg-green-700'
            >
              Convert to Customer
            </Button>
          )}
          <Button variant='outline' onClick={() => setEditMode(true)}>
            Edit
          </Button>
          <ActionMenu
            items={[
              {
                label: 'Edit Lead',
                onClick: () => setEditMode(true),
              },
              {
                label: 'Convert to Customer',
                onClick: handleConvertLead,
                disabled: lead.status !== 'QUALIFIED',
              },
              {
                label: 'Delete Lead',
                onClick: handleDeleteLead,
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
          <TabsTrigger value='activities'>Activities</TabsTrigger>
          <TabsTrigger value='notes'>Notes</TabsTrigger>
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
                      <p className='text-sm'>{lead.email}</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Phone
                      </label>
                      <p className='text-sm'>{lead.phone || 'Not provided'}</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Company
                      </label>
                      <p className='text-sm'>
                        {lead.company || 'Not provided'}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Position
                      </label>
                      <p className='text-sm'>
                        {lead.jobTitle || 'Not provided'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Lead Details */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Lead Details</h3>
                </CardHeader>
                <CardContent>
                  <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Source
                      </label>
                      <p className='text-sm'>{lead.source}</p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Priority
                      </label>
                      <p className='text-sm'>
                        {lead.priority || 'Not specified'}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Estimated Value
                      </label>
                      <p className='text-sm'>
                        {lead.estimatedValue
                          ? `$${lead.estimatedValue.toLocaleString()}`
                          : 'Not specified'}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Expected Timeline
                      </label>
                      <p className='text-sm'>
                        {lead.expectedCloseDate || 'Not specified'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Notes */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Notes</h3>
                </CardHeader>
                <CardContent>
                  <p className='text-sm text-muted-foreground'>
                    {lead.notes || 'No notes available.'}
                  </p>
                </CardContent>
              </Card>
            </div>

            {/* Sidebar */}
            <div className='space-y-6'>
              {/* Priority */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Priority</h3>
                </CardHeader>
                <CardContent>
                  <div className='text-center'>
                    <Badge
                      variant={
                        lead.priority === 'URGENT'
                          ? 'destructive'
                          : lead.priority === 'HIGH'
                            ? 'default'
                            : 'secondary'
                      }
                    >
                      {lead.priority}
                    </Badge>
                  </div>
                </CardContent>
              </Card>

              {/* Timeline */}
              <Card>
                <CardHeader>
                  <h3 className='text-lg font-semibold'>Timeline</h3>
                </CardHeader>
                <CardContent>
                  <div className='space-y-4'>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Created
                      </label>
                      <p className='text-sm'>
                        {new Date(lead.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Last Updated
                      </label>
                      <p className='text-sm'>
                        {new Date(lead.updatedAt).toLocaleDateString()}
                      </p>
                    </div>
                    {lead.lastActivityDate && (
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Last Activity
                        </label>
                        <p className='text-sm'>
                          {new Date(lead.lastActivityDate).toLocaleDateString()}
                        </p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Tags */}
              {lead.tags && lead.tags.length > 0 && (
                <Card>
                  <CardHeader>
                    <h3 className='text-lg font-semibold'>Tags</h3>
                  </CardHeader>
                  <CardContent>
                    <div className='flex flex-wrap gap-2'>
                      {lead.tags.map((tag, index) => (
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
                      Log Activity
                    </Button>
                    <Button className='w-full' variant='outline'>
                      Send Email
                    </Button>
                    <Button className='w-full' variant='outline'>
                      Schedule Call
                    </Button>
                    <Button className='w-full' variant='outline'>
                      Create Opportunity
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
              <div className='text-center py-8'>
                <p className='text-muted-foreground mb-4'>
                  No activities recorded yet.
                </p>
                <Button>Log First Activity</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='notes'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <h3 className='text-lg font-semibold'>Notes & Comments</h3>
                <Button>Add Note</Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className='space-y-4'>
                <div className='bg-muted p-4 rounded-lg'>
                  <p className='text-sm'>
                    {lead.notes || 'No notes available.'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default LeadDetailPage;
