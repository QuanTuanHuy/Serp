// EditCustomerPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useMemo } from 'react';
import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { CustomerForm } from '../../components/forms';
import { MOCK_CUSTOMERS } from '../../mocks';

interface EditCustomerPageProps {
  customerId: string;
  className?: string;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export const EditCustomerPage: React.FC<EditCustomerPageProps> = ({
  customerId,
  className,
  onSuccess,
  onCancel,
}) => {
  // Find customer from mock data
  const customer = useMemo(() => {
    return MOCK_CUSTOMERS.find((c) => c.id === customerId);
  }, [customerId]);

  const handleSubmit = async (data: any) => {
    console.log('Updating customer:', data);
    // TODO: Implement API call when backend is ready
    onSuccess?.();
  };

  // Error state - customer not found
  if (!customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
              Customer Not Found
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
              The customer you're trying to edit doesn't exist or has been
              deleted.
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
            <h1 className='text-2xl font-bold text-foreground'>
              Edit Customer
            </h1>
            <p className='text-muted-foreground'>
              Update {customer.name}'s information
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max lg:max-w-4xl xl:max-w-5xl mx-auto'>
        <CustomerForm
          customer={customer}
          onSubmit={handleSubmit}
          onCancel={onCancel}
        />
      </div>
    </div>
  );
};

export default EditCustomerPage;
