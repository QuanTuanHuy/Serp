// CreateCustomerPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { AccountForm } from '../../components/forms';
import { useCreateAccountMutation } from '../../api/crmApi';
import type { CreateAccountRequest } from '../../types';

interface CreateCustomerPageProps {
  className?: string;
  onSuccess?: (customerId: string) => void;
  onCancel?: () => void;
}

export const CreateCustomerPage: React.FC<CreateCustomerPageProps> = ({
  className,
  onSuccess,
  onCancel,
}) => {
  const [createAccount, { isLoading }] = useCreateAccountMutation();

  const handleSubmit = async (
    data: CreateAccountRequest | Partial<CreateAccountRequest>
  ) => {
    try {
      const result = await createAccount(data as CreateAccountRequest).unwrap();
      onSuccess?.(result.data.id);
    } catch (error) {
      console.error('Failed to create account:', error);
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
              Create New Account
            </h1>
            <p className='text-muted-foreground'>
              Add a new account to your CRM
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max-w-4xl'>
        <AccountForm
          onSubmit={handleSubmit}
          onCancel={onCancel}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
};

export default CreateCustomerPage;
