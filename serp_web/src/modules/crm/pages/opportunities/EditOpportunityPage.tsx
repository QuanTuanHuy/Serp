// EditOpportunityPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, AlertCircle, Loader2 } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { OpportunityForm } from '../../components/forms';
import {
  useGetOpportunityQuery,
  useUpdateOpportunityMutation,
} from '../../api/crmApi';
import type {
  CreateOpportunityRequest,
  UpdateOpportunityRequest,
} from '../../types';

interface EditOpportunityPageProps {
  opportunityId: string;
  className?: string;
}

export const EditOpportunityPage: React.FC<EditOpportunityPageProps> = ({
  opportunityId,
  className,
}) => {
  const router = useRouter();
  const { data, isLoading } = useGetOpportunityQuery(opportunityId);
  const [updateOpportunity, { isLoading: isUpdating }] =
    useUpdateOpportunityMutation();

  const opportunity = data?.data;

  const handleSubmit = async (
    data: CreateOpportunityRequest | UpdateOpportunityRequest
  ) => {
    try {
      await updateOpportunity({
        id: opportunityId,
        data: data as UpdateOpportunityRequest,
      }).unwrap();
      toast.success('Update opportunity successfully');
      router.push(`/crm/opportunities/${opportunityId}`);
    } catch (error) {
      toast.error('Failed to update opportunity', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleCancel = () => {
    router.push(`/crm/opportunities/${opportunityId}`);
  };

  if (isLoading) {
    return (
      <div
        className={cn(
          'flex min-h-[50vh] items-center justify-center p-6',
          className
        )}
      >
        <Loader2
          className='h-10 w-10 animate-spin text-muted-foreground'
          aria-label='Loading opportunity'
        />
      </div>
    );
  }

  if (!opportunity) {
    return (
      <div className={cn('p-6', className)}>
        <div className='flex h-[60vh] flex-col items-center justify-center'>
          <AlertCircle className='mb-4 h-16 w-16 text-muted-foreground' />
          <h2 className='mb-2 text-xl font-semibold text-foreground'>
            Opportunity Not Found
          </h2>
          <p className='mb-4 text-muted-foreground'>
            The opportunity you're trying to edit doesn't exist or has been
            deleted.
          </p>
          <Button asChild>
            <Link href='/crm/opportunities'>
              <ArrowLeft className='mr-2 h-4 w-4' />
              Back to Opportunities
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
            <Link href={`/crm/opportunities/${opportunityId}`}>
              <ArrowLeft className='mr-2 h-4 w-4' />
              Back
            </Link>
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              Edit Opportunity
            </h1>
            <p className='text-muted-foreground'>
              Update {opportunity.name || 'opportunity'}
            </p>
          </div>
        </div>
      </div>

      <div className='mx-auto max-w-5xl'>
        <OpportunityForm
          opportunity={opportunity}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          isLoading={isUpdating}
        />
      </div>
    </div>
  );
};

export default EditOpportunityPage;
