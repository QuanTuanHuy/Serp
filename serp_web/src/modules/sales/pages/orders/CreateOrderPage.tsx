/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create New Order Page
*/

'use client';

import { useRouter } from 'next/navigation';
import { OrderForm } from '../../components/forms/OrderForm';
import { useCreateOrderMutation } from '../../api/salesApi';
import type { OrderCreationForm } from '../../types';

export const CreateOrderPage: React.FC<{ customerId?: string }> = ({
  customerId,
}) => {
  const router = useRouter();
  const [createOrder] = useCreateOrderMutation();

  const handleSubmit = async (data: OrderCreationForm) => {
    try {
      const result = await createOrder(data).unwrap();
      if (result.data?.id) {
        router.push(`/sales/orders/${result.data.id}`);
      } else {
        router.push('/sales/orders');
      }
    } catch (error) {
      console.error('Không thể tạo đơn hàng:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    router.push('/sales/orders');
  };

  return (
    <OrderForm
      customerId={customerId}
      onSubmit={handleSubmit as any}
      onCancel={handleCancel}
    />
  );
};

export default CreateOrderPage;
