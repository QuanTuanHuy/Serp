// CreateCustomerPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { CustomerForm } from '../../components/forms';
import { useCreateCustomerMutation } from '../../api/crmApi';

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
  const [createCustomer, { isLoading }] = useCreateCustomerMutation();

  const handleSubmit = async (data: any) => {
    try {
      const result = await createCustomer(data).unwrap();
      onSuccess?.(result.data.id);
    } catch (error) {
      console.error('Failed to create customer:', error);
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
            <h1 className='text-2xl font-bold text-gray-900'>
              Create New Customer
            </h1>
            <p className='text-gray-600'>Add a new customer to your CRM</p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max-w-4xl'>
        <CustomerForm
          onSubmit={handleSubmit}
          onCancel={onCancel}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
};

export default CreateCustomerPage;
