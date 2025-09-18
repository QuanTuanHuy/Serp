// EditCustomerPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { CustomerForm } from '../../components/forms';
import {
  useGetCustomerQuery,
  useUpdateCustomerMutation,
} from '../../api/crmApi';

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
  const {
    data: customerResponse,
    isLoading: isLoadingCustomer,
    error,
  } = useGetCustomerQuery(customerId);
  const [updateCustomer, { isLoading: isUpdating }] =
    useUpdateCustomerMutation();

  const customer = customerResponse?.data;

  const handleSubmit = async (data: any) => {
    try {
      await updateCustomer({ id: customerId, ...data }).unwrap();
      onSuccess?.();
    } catch (error) {
      console.error('Failed to update customer:', error);
    }
  };

  // Loading state
  if (isLoadingCustomer) {
    return (
      <div className={cn('p-6', className)}>
        <div className='animate-pulse space-y-6'>
          <div className='h-8 bg-gray-200 rounded w-1/3'></div>
          <div className='h-96 bg-gray-200 rounded'></div>
        </div>
      </div>
    );
  }

  // Error state
  if (error || !customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card className='border-red-200 bg-red-50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 mb-2'>
              Customer Not Found
            </h3>
            <p className='text-red-600 mb-4'>
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
            <h1 className='text-2xl font-bold text-gray-900'>Edit Customer</h1>
            <p className='text-gray-600'>
              Update {customer.name}'s information
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max-w-4xl'>
        <CustomerForm
          customer={customer}
          onSubmit={handleSubmit}
          onCancel={onCancel}
          isLoading={isUpdating}
        />
      </div>
    </div>
  );
};

export default EditCustomerPage;
