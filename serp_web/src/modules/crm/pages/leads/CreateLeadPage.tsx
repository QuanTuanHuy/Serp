// CreateLeadPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { LeadForm } from '../../components/forms';
import { useCreateLeadMutation } from '../../api/crmApi';
import type { CreateLeadRequest } from '../../types';

interface CreateLeadPageProps {
  className?: string;
  onSuccess?: (leadId: string) => void;
  onCancel?: () => void;
}

export const CreateLeadPage: React.FC<CreateLeadPageProps> = ({
  className,
  onSuccess,
  onCancel,
}) => {
  const router = useRouter();
  const [createLead, { isLoading }] = useCreateLeadMutation();

  const handleSubmit = async (data: CreateLeadRequest | Partial<CreateLeadRequest>) => {
    try {
      const result = await createLead(data as CreateLeadRequest).unwrap();
      toast.success('Create lead successfully');
      onSuccess?.(result.data.id);
      router.push(`/crm/leads/${result.data.id}`);
    } catch (error) {
      toast.error('Failed to create lead', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className={cn('p-6', className)}>
      <div className='mb-6 flex items-center justify-between'>
        <div className='flex items-center space-x-4'>
          <Button variant='outline' onClick={onCancel ?? (() => router.push('/crm/leads'))}>
            ← Back
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>Create New Lead</h1>
            <p className='text-muted-foreground'>Add a new sales prospect</p>
          </div>
        </div>
      </div>

      <div className='max-w-5xl'>
        <LeadForm
          onSubmit={handleSubmit}
          onCancel={onCancel ?? (() => router.push('/crm/leads'))}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
};

export default CreateLeadPage;
