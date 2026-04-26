// EditLeadPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { LeadForm } from '../../components/forms';
import { useGetLeadQuery, useUpdateLeadMutation } from '../../api/crmApi';
import type { CreateLeadRequest, UpdateLeadRequest } from '../../types';

interface EditLeadPageProps {
  leadId: string;
  className?: string;
}

export const EditLeadPage: React.FC<EditLeadPageProps> = ({
  leadId,
  className,
}) => {
  const router = useRouter();
  const { data, isLoading: isFetching } = useGetLeadQuery(leadId);
  const [updateLead, { isLoading: isUpdating }] = useUpdateLeadMutation();
  const lead = data?.data;

  const handleSubmit = async (data: CreateLeadRequest | UpdateLeadRequest) => {
    try {
      await updateLead({ id: leadId, data }).unwrap();
      toast.success('Update lead successfully');
      router.push(`/crm/leads/${leadId}`);
    } catch (error) {
      toast.error('Failed to update lead', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!isFetching && !lead) {
    return (
      <div className={cn('p-6', className)}>
        <div className='flex h-[60vh] flex-col items-center justify-center'>
          <AlertCircle className='mb-4 h-16 w-16 text-muted-foreground' />
          <h2 className='mb-2 text-xl font-semibold text-foreground'>
            Lead not found
          </h2>
          <p className='mb-4 text-muted-foreground'>
            This lead does not exist or has been deleted.
          </p>
          <Button asChild>
            <Link href='/crm/leads'>
              <ArrowLeft className='mr-2 h-4 w-4' />
              Back to lead list
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
          <Button variant='outline' onClick={() => router.push(`/crm/leads/${leadId}`)}>
            ← Back
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>Edit Lead</h1>
            <p className='text-muted-foreground'>Update {lead?.name}'s information</p>
          </div>
        </div>
      </div>

      <div className='mx-auto max-w-5xl'>
        <LeadForm
          lead={lead}
          onSubmit={handleSubmit}
          onCancel={() => router.push(`/crm/leads/${leadId}`)}
          isLoading={isFetching || isUpdating}
        />
      </div>
    </div>
  );
};

export default EditLeadPage;
