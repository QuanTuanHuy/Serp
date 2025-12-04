// EditLeadPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { LeadForm } from '../../components/forms';
import { MOCK_LEADS } from '../../mocks';

interface EditLeadPageProps {
  leadId: string;
  className?: string;
}

export const EditLeadPage: React.FC<EditLeadPageProps> = ({
  leadId,
  className,
}) => {
  const router = useRouter();

  // Find lead from mock data
  const lead = MOCK_LEADS.find((l) => l.id === leadId);

  const handleSubmit = async (data: any) => {
    console.log('Updating lead:', { id: leadId, ...data });
    // In real app, call API here
    router.push(`/crm/leads/${leadId}`);
  };

  const handleCancel = () => {
    router.push(`/crm/leads/${leadId}`);
  };

  // Error state - lead not found
  if (!lead) {
    return (
      <div className={cn('p-6', className)}>
        <div className='flex h-[60vh] flex-col items-center justify-center'>
          <AlertCircle className='mb-4 h-16 w-16 text-muted-foreground' />
          <h2 className='mb-2 text-xl font-semibold text-foreground'>
            Không tìm thấy lead
          </h2>
          <p className='mb-4 text-muted-foreground'>
            Lead này không tồn tại hoặc đã bị xóa
          </p>
          <Button asChild>
            <Link href='/crm/leads'>
              <ArrowLeft className='mr-2 h-4 w-4' />
              Quay lại danh sách
            </Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={cn('p-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <div className='flex items-center space-x-4'>
          <Button variant='outline' asChild>
            <Link href={`/crm/leads/${leadId}`}>
              <ArrowLeft className='mr-2 h-4 w-4' />
              Back
            </Link>
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
      <div className='max-w-4xl lg:max-w-5xl mx-auto'>
        <LeadForm lead={lead} onSubmit={handleSubmit} onCancel={handleCancel} />
      </div>
    </div>
  );
};

export default EditLeadPage;
