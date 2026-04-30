// CreateOpportunityPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { OpportunityForm } from '../../components/forms';
import { useCreateOpportunityMutation } from '../../api/crmApi';
import type { CreateOpportunityRequest } from '../../types';

interface CreateOpportunityPageProps {
  className?: string;
  onSuccess?: (opportunityId: string) => void;
  onCancel?: () => void;
}

export const CreateOpportunityPage: React.FC<CreateOpportunityPageProps> = ({
  className,
  onSuccess,
  onCancel,
}) => {
  const router = useRouter();
  const [createOpportunity, { isLoading }] = useCreateOpportunityMutation();

  const handleSubmit = async (
    data: CreateOpportunityRequest | Partial<CreateOpportunityRequest>
  ) => {
    try {
      const result = await createOpportunity(
        data as CreateOpportunityRequest
      ).unwrap();
      toast.success('Create opportunity successfully');
      onSuccess?.(result.data.id);
      router.push(`/crm/opportunities/${result.data.id}`);
    } catch (error) {
      toast.error('Failed to create opportunity', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className={cn('p-6', className)}>
      <div className='mb-6 flex items-center justify-between'>
        <div className='flex items-center space-x-4'>
          <Button
            variant='outline'
            onClick={onCancel ?? (() => router.push('/crm/opportunities'))}
          >
            ← Back
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              Create New Opportunity
            </h1>
            <p className='text-muted-foreground'>
              Add a new business opportunity to your pipeline.
            </p>
          </div>
        </div>
      </div>

      <div className='mx-auto max-w-5xl'>
        <OpportunityForm
          onSubmit={handleSubmit}
          onCancel={onCancel ?? (() => router.push('/crm/opportunities'))}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
};

export default CreateOpportunityPage;
