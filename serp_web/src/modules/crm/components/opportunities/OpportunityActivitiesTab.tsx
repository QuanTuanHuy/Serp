// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import { Card, CardContent, CardHeader, Badge } from '@/shared/components/ui';
import { Clock, User } from 'lucide-react';
import { formatDateTime, getActivityBadgeClass } from '../../utils';
import type { Activity } from '../../types';

interface OpportunityActivitiesTabProps {
  activities: Activity[];
  isLoading: boolean;
}

export const OpportunityActivitiesTab: React.FC<
  OpportunityActivitiesTabProps
> = ({ activities, isLoading }) => {
  return (
    <Card>
      <CardHeader>
        <h3 className='font-semibold'>Recent Activities</h3>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <p className='text-sm text-muted-foreground'>Loading activities...</p>
        ) : activities.length > 0 ? (
          <div className='space-y-4'>
            {activities.map((activity) => (
              <div key={activity.id} className='rounded-lg border p-4'>
                <div className='mb-2 flex items-start justify-between gap-3'>
                  <div>
                    <p className='font-medium'>{activity.subject}</p>
                    <p className='text-sm text-muted-foreground'>
                      {activity.description || 'No description provided.'}
                    </p>
                  </div>
                  <Badge className={getActivityBadgeClass(activity.status)}>
                    {activity.status}
                  </Badge>
                </div>
                <div className='flex flex-wrap gap-4 text-sm text-muted-foreground'>
                  <span className='flex items-center gap-1'>
                    <Clock className='h-4 w-4' />
                    {formatDateTime(
                      activity.scheduledDate || activity.createdAt
                    )}
                  </span>
                  <span className='flex items-center gap-1'>
                    <User className='h-4 w-4' />
                    {activity.assignedToName || 'Unassigned'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className='text-sm text-muted-foreground'>
            No activities available for this opportunity.
          </p>
        )}
      </CardContent>
    </Card>
  );
};
