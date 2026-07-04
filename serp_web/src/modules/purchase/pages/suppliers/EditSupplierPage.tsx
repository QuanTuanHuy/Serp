'use client';

import { useRouter } from 'next/navigation';
import { Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SupplierForm } from '../../components/forms/SupplierForm';
import {
  useGetSupplierQuery,
  useUpdateSupplierMutation,
} from '../../api/purchaseApi';
import type { SupplierUpdateForm } from '../../types';

interface EditSupplierPageProps {
  supplierId: string;
  className?: string;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export const EditSupplierPage: React.FC<EditSupplierPageProps> = ({
  supplierId,
  className,
  onSuccess,
  onCancel,
}) => {
  const router = useRouter();

  const {
    data: supplierResponse,
    isLoading,
    isError,
  } = useGetSupplierQuery(supplierId);
  const [updateSupplier, { isLoading: isUpdating }] =
    useUpdateSupplierMutation();

  const supplier = supplierResponse?.data;

  const handleSubmit = async (data: SupplierUpdateForm) => {
    try {
      await updateSupplier({ supplierId, data }).unwrap();
      onSuccess?.();
    } catch (error) {
      console.error('Không thể cập nhật nhà cung cấp:', error);
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    } else {
      router.back();
    }
  };

  if (isLoading) {
    return (
      <div className={cn('p-6', className)}>
        <div className='flex items-center justify-center min-h-[400px]'>
          <div className='text-center'>
            <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4'></div>
            <p className='text-muted-foreground'>
              Đang tải dữ liệu nhà cung cấp...
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (isError || !supplier) {
    return (
      <div className={cn('p-6', className)}>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
              Không tìm thấy nhà cung cấp
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
              Nhà cung cấp với ID "{supplierId}" không tồn tại hoặc đã bị xóa.
            </p>
            <Button variant='outline' onClick={handleCancel}>
              Quay lại danh sách nhà cung cấp
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className={cn('p-6', className)}>
      <div className='flex items-center justify-between mb-6'>
        <div className='flex items-center space-x-4'>
          <Button variant='outline' onClick={handleCancel}>
            ← Quay lại
          </Button>
          <div>
            <h1 className='text-2xl font-bold text-foreground'>
              Chỉnh sửa nhà cung cấp
            </h1>
            <p className='text-muted-foreground'>
              Cập nhật thông tin của {supplier.name}
            </p>
          </div>
        </div>
      </div>

      <div className='max-w-4xl mx-auto'>
        <SupplierForm
          supplier={supplier}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          isLoading={isUpdating}
        />
      </div>
    </div>
  );
};

export default EditSupplierPage;
