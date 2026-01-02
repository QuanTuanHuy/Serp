/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create New Purchase Order Page
*/

'use client';

import { useRouter } from 'next/navigation';
import { OrderForm } from '../../components/forms/OrderForm';
import { useCreateOrderMutation } from '../../api/purchaseApi';
import type { OrderCreationForm } from '../../types';

export const CreateOrderPage: React.FC<{ supplierId?: string }> = ({
  supplierId,
}) => {
  const router = useRouter();
  const [createOrder] = useCreateOrderMutation();

  const handleSubmit = async (data: OrderCreationForm) => {
    try {
      const result = await createOrder(data).unwrap();
      if (result.data?.id) {
        router.push(`/purchase/purchase-orders/${result.data.id}`);
      } else {
        router.push('/purchase/purchase-orders');
      }
    } catch (error) {
      console.error('Không thể tạo đơn hàng:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    router.push('/purchase/purchase-orders');
  };

  return (
    <OrderForm
      supplierId={supplierId}
      onSubmit={handleSubmit as any}
      onCancel={handleCancel}
    />
  );
};

export default CreateOrderPage;
