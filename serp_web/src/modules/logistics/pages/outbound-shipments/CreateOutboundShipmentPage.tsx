'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { OutboundShipmentForm } from '../../components/forms/OutboundShipmentForm';
import { useCreateOutboundShipmentMutation } from '../../api/logisticsApi';
import type { OutboundShipmentCreationForm } from '../../types';

interface CreateOutboundShipmentPageProps {
  orderId?: string;
}

export const CreateOutboundShipmentPage: React.FC<
  CreateOutboundShipmentPageProps
> = ({ orderId: propsOrderId }) => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const orderIdFromParams = searchParams?.get('orderId');
  const orderId = propsOrderId || orderIdFromParams || '';

  const [createShipment] = useCreateOutboundShipmentMutation();

  const handleSubmit = async (data: OutboundShipmentCreationForm) => {
    try {
      await createShipment(data).unwrap();
      router.push(`/logistics/sale-orders/${orderId}`);
    } catch (error) {
      console.error('Không thể tạo phiếu xuất kho:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    if (orderId) {
      router.push(`/logistics/sale-orders/${orderId}`);
    } else {
      router.push('/logistics/outbound-shipments');
    }
  };

  return (
    <OutboundShipmentForm
      orderId={orderId}
      onSubmit={handleSubmit as any}
      onCancel={handleCancel}
    />
  );
};

export default CreateOutboundShipmentPage;
