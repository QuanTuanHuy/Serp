// EditLeadPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { LeadForm } from '../../components/forms';
import { useGetLeadQuery, useUpdateLeadMutation } from '../../api/crmApi';

interface EditLeadPageProps {
  leadId: string;
  className?: string;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export const EditLeadPage: React.FC<EditLeadPageProps> = ({
  leadId,
  className,
  onSuccess,
  onCancel,
}) => {
  const {
    data: leadResponse,
    isLoading: isLoadingLead,
    error,
  } = useGetLeadQuery(leadId);
  const [updateLead, { isLoading: isUpdating }] = useUpdateLeadMutation();

  const lead = leadResponse?.data;

  const handleSubmit = async (data: any) => {
    try {
      await updateLead({ id: leadId, ...data }).unwrap();
      onSuccess?.();
    } catch (error) {
      console.error('Failed to update lead:', error);
    }
  };

  // Loading state
  if (isLoadingLead) {
    return (
      <div className={cn('p-6', className)}>
        <div className='animate-pulse space-y-6'>
          <div className='h-8 bg-muted rounded w-1/3'></div>
          <div className='h-96 bg-muted rounded'></div>
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
              The lead you're trying to edit doesn't exist or has been deleted.
            </p>
            <Button variant='outline' onClick={onCancel}>
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
          <Button variant='outline' onClick={onCancel}>
            ← Back
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>Edit Lead</h1>
            <p className='text-muted-foreground'>
              Update {lead.firstName} {lead.lastName}'s information
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max-w-4xl'>
        <LeadForm
          lead={lead}
          onSubmit={handleSubmit}
          onCancel={onCancel}
          isLoading={isUpdating}
        />
      </div>
    </div>
  );
};

export default EditLeadPage;
