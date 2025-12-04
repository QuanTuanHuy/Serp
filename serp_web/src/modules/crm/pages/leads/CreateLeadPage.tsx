// CreateLeadPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { LeadForm } from '../../components/forms';
import { useCreateLeadMutation } from '../../api/crmApi';

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
  const [createLead, { isLoading }] = useCreateLeadMutation();

  const handleSubmit = async (data: any) => {
    try {
      const result = await createLead(data).unwrap();
      onSuccess?.(result.data.id);
    } catch (error) {
      console.error('Failed to create lead:', error);
    }
  };

  return (
    <div className={cn('p-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <div className='flex items-center space-x-4'>
          <Button variant='outline' onClick={onCancel}>
            ← Back
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              Create New Lead
            </h1>
            <p className='text-muted-foreground'>Add a new sales prospect</p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max-w-4xl'>
        <LeadForm
          onSubmit={handleSubmit}
          onCancel={onCancel}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
};

export default CreateLeadPage;
