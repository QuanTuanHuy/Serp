// EditCustomerPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useRouter } from 'next/navigation';
import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { CustomerForm } from '../../components/forms';
import {
  useGetCustomerQuery,
  useUpdateCustomerMutation,
} from '../../api/salesApi';
import type { CustomerUpdateForm } from '../../types';

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
  const router = useRouter();

  // Fetch customer data
  const {
    data: customerResponse,
    isLoading,
    isError,
  } = useGetCustomerQuery(customerId);
  const [updateCustomer, { isLoading: isUpdating }] =
    useUpdateCustomerMutation();

  const customer = customerResponse?.data;

  const handleSubmit = async (data: CustomerUpdateForm) => {
    try {
      await updateCustomer({ customerId: customerId, data }).unwrap();
      onSuccess?.();
    } catch (error) {
      console.error('Failed to update customer:', error);
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    } else {
      router.back();
    }
  };

  // Loading state
  if (isLoading) {
    return (
      <div className={cn('p-6', className)}>
        <div className='flex items-center justify-center min-h-[400px]'>
          <div className='text-center'>
            <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4'></div>
            <p className='text-muted-foreground'>
              Đang tải dữ liệu khách hàng...
            </p>
          </div>
        </div>
      </div>
    );
  }

  // Error state - customer not found
  if (isError || !customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
              Không tìm thấy khách hàng
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
              Khách hàng với ID "{customerId}" không tồn tại hoặc đã bị xóa.
            </p>
            <Button variant='outline' onClick={handleCancel}>
              Quay lại danh sách khách hàng
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
          <Button variant='outline' onClick={handleCancel}>
            ← Quay lại
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              Chỉnh sửa khách hàng
            </h1>
            <p className='text-muted-foreground'>
              Cập nhật thông tin của {customer.name}
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className='max-w-4xl mx-auto'>
        <CustomerForm
          customer={customer}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          isLoading={isUpdating}
        />
      </div>
    </div>
  );
};

export default EditCustomerPage;
