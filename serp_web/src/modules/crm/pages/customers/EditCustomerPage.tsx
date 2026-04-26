// EditCustomerPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { getErrorMessage } from '@/lib/store/api';
import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import { AccountForm } from '../../components/forms';
import {
  useGetAccountQuery,
  useUpdateAccountMutation,
} from '../../api/crmApi';
import type { CreateAccountRequest, UpdateAccountRequest } from '../../types';

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
  const { data, isLoading: isFetching } = useGetAccountQuery(customerId);
  const [updateAccount, { isLoading: isUpdating }] = useUpdateAccountMutation();
  const customer = data?.data;

  const handleSubmit = async (
    data: CreateAccountRequest | UpdateAccountRequest
  ) => {
    try {
      await updateAccount({ id: customerId, data }).unwrap();
      toast.success('Update account successfully');
      onSuccess?.();
    } catch (error) {
      toast.error('Failed to update account', {
        description: getErrorMessage(error),
      });
    }
  };

  // Error state - customer not found
  if (!isFetching && !customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
               Account Not Found
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
               The account you're trying to edit doesn't exist or has been
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
              Edit Account
            </h1>
            <p className='text-muted-foreground'>
              Update {customer?.name}'s information
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max lg:max-w-4xl xl:max-w-5xl mx-auto'>
        <AccountForm
          customer={customer}
          onSubmit={handleSubmit}
          onCancel={onCancel}
          isLoading={isFetching || isUpdating}
        />
      </div>
    </div>
  );
};

export default EditCustomerPage;
