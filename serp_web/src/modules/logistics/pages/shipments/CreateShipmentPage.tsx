/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create Shipment Page
*/

'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { InboundShipmentForm } from '../../components/forms/InboundShipmentForm';
import { useCreateShipmentMutation } from '../../api/logisticsApi';
import type { ShipmentCreationForm } from '../../types';

interface CreateShipmentPageProps {
  orderId?: string;
}

export const CreateShipmentPage: React.FC<CreateShipmentPageProps> = ({
  orderId: propsOrderId,
}) => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const orderIdFromParams = searchParams?.get('orderId');
  const orderId = propsOrderId || orderIdFromParams || '';

  const [createShipment] = useCreateShipmentMutation();

  const handleSubmit = async (data: ShipmentCreationForm) => {
    try {
      await createShipment(data).unwrap();
      router.push(`/logistics/purchase-orders/${orderId}`);
    } catch (error) {
      console.error('Không thể tạo phiếu kho:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    if (orderId) {
      router.push(`/logistics/purchase-orders/${orderId}`);
    } else {
      router.push('/logistics/purchase-orders');
    }
  };

  return (
    <InboundShipmentForm
      orderId={orderId}
      onSubmit={handleSubmit as any}
      onCancel={handleCancel}
    />
  );
};

export default CreateShipmentPage;
