// EditActivityPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { AlertCircle, ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { ActivityForm } from '../../components/forms';
import {
  useGetActivityQuery,
  useUpdateActivityMutation,
} from '../../api/crmApi';
import type { UpdateActivityRequest } from '../../types';

interface EditActivityPageProps {
  activityId: string;
  className?: string;
}

export const EditActivityPage: React.FC<EditActivityPageProps> = ({
  activityId,
  className,
}) => {
  const router = useRouter();
  const { data, isLoading } = useGetActivityQuery(activityId);
  const [updateActivity, { isLoading: isUpdating }] =
    useUpdateActivityMutation();

  const activity = data?.data;

  const handleSubmit = async (formData: UpdateActivityRequest) => {
    try {
      await updateActivity({ id: activityId, data: formData }).unwrap();
      toast.success('Update activity successfully');
      router.push(`/crm/activities/${activityId}`);
    } catch (error) {
      toast.error('Failed to update activity', {
        description: getErrorMessage(error),
      });
    }
  };

  if (isLoading) {
    return (
      <div
        className={cn('flex h-[60vh] items-center justify-center', className)}
      >
        <p className='text-muted-foreground'>Loading activity...</p>
      </div>
    );
  }

  if (!activity) {
    return (
      <div className={cn('p-6', className)}>
        <div className='flex h-[60vh] flex-col items-center justify-center'>
          <AlertCircle className='mb-4 h-16 w-16 text-muted-foreground' />
          <h2 className='mb-2 text-xl font-semibold text-foreground'>
            Activity not found
          </h2>
          <p className='mb-4 text-muted-foreground'>
            This activity does not exist or has been deleted.
          </p>
          <Button asChild>
            <Link href='/crm/activities'>
              <ArrowLeft className='mr-2 h-4 w-4' />
              Back to activity list
            </Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={cn('p-6', className)}>
      <div className='mb-6 flex items-center justify-between'>
        <div className='flex items-center space-x-4'>
          <Button variant='outline' asChild>
            <Link href={`/crm/activities/${activityId}`}>
              <ArrowLeft className='mr-2 h-4 w-4' />
              Back
            </Link>
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              Edit Activity
            </h1>
            <p className='text-muted-foreground'>Update {activity.subject}</p>
          </div>
        </div>
      </div>

      <div className='mx-auto max-w-5xl'>
        <ActivityForm
          activity={activity}
          onSubmit={handleSubmit}
          onCancel={() => router.push(`/crm/activities/${activityId}`)}
          isLoading={isUpdating}
        />
      </div>
    </div>
  );
};

export default EditActivityPage;
