/*
Author: QuanTuanHuy
Description: Part of Serp Project - Edit Order Page
*/

'use client';

import { useRouter } from 'next/navigation';
import { OrderForm } from '../../components/forms/OrderForm';
import { useGetOrderQuery, useUpdateOrderMutation } from '../../api/salesApi';
import { Card, CardContent, Button } from '@/shared/components/ui';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import type { OrderUpdateForm } from '../../types';

interface EditOrderPageProps {
  orderId: string;
}

export const EditOrderPage: React.FC<EditOrderPageProps> = ({ orderId }) => {
  const router = useRouter();
  const { data: orderResponse, isLoading, isError } = useGetOrderQuery(orderId);
  const [updateOrder] = useUpdateOrderMutation();

  const order = orderResponse?.data;

  const handleSubmit = async (data: OrderUpdateForm) => {
    try {
      await updateOrder({ orderId, data }).unwrap();
      router.push(`/sales/orders/${orderId}`);
    } catch (error) {
      console.error('Không thể cập nhật đơn hàng:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    router.push(`/sales/orders/${orderId}`);
  };

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <Card className='animate-pulse'>
          <CardContent className='p-8'>
            <div className='h-8 bg-muted rounded w-1/3 mb-4' />
            <div className='h-4 bg-muted rounded w-1/2' />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isError || !order) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Không tìm thấy đơn hàng
          </h3>
          <p className='text-muted-foreground mb-4'>
            Đơn hàng bạn đang cố gắng chỉnh sửa không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/sales/orders')}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách đơn hàng
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <OrderForm
      order={order}
      onSubmit={handleSubmit as any}
      onCancel={handleCancel}
    />
  );
};

export default EditOrderPage;
