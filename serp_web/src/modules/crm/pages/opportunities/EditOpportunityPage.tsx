// EditOpportunityPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { OpportunityForm } from '../../components/forms';
import { MOCK_OPPORTUNITIES } from '../../mocks';

interface EditOpportunityPageProps {
  opportunityId: string;
  className?: string;
}

export const EditOpportunityPage: React.FC<EditOpportunityPageProps> = ({
  opportunityId,
  className,
}) => {
  const router = useRouter();

  // Find opportunity from mock data
  const opportunity = MOCK_OPPORTUNITIES.find((o) => o.id === opportunityId);

  const handleSubmit = async (data: any) => {
    console.log('Updating opportunity:', { id: opportunityId, ...data });
    // In real app, call API here
    router.push(`/crm/opportunities/${opportunityId}`);
  };

  const handleCancel = () => {
    router.push(`/crm/opportunities/${opportunityId}`);
  };

  // Error state - opportunity not found
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
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
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
            <p className='text-muted-foreground'>Update {opportunity.name}</p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max-w-4xl'>
        <OpportunityForm
          opportunity={opportunity}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
        />
      </div>
    </div>
  );
};

export default EditOpportunityPage;
