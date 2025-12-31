/*
Author: QuanTuanHuy
Description: Part of Serp Project - Edit Shipment Page
*/

'use client';

import { useRouter } from 'next/navigation';
import { InboundShipmentForm } from '../../components/forms/InboundShipmentForm';
import {
  useGetShipmentQuery,
  useUpdateShipmentMutation,
} from '../../api/logisticsApi';
import { Card, CardContent, Button } from '@/shared/components/ui';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import type { ShipmentUpdateForm } from '../../types';

interface EditShipmentPageProps {
  shipmentId: string;
}

export const EditShipmentPage: React.FC<EditShipmentPageProps> = ({
  shipmentId,
}) => {
  const router = useRouter();
  const {
    data: shipmentResponse,
    isLoading,
    isError,
  } = useGetShipmentQuery(shipmentId);
  const [updateShipment] = useUpdateShipmentMutation();

  const shipment = shipmentResponse?.data;

  const handleSubmit = async (data: ShipmentUpdateForm) => {
    try {
      await updateShipment({ shipmentId, data }).unwrap();
      router.push(`/logistics/shipments/${shipmentId}`);
    } catch (error) {
      console.error('Không thể cập nhật phiếu kho:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    router.push(`/logistics/shipments/${shipmentId}`);
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

  if (isError || !shipment) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Không tìm thấy phiếu kho
          </h3>
          <p className='text-muted-foreground mb-4'>
            Phiếu kho bạn đang cố gắng chỉnh sửa không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/logistics/shipments')}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách phiếu kho
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <InboundShipmentForm
      shipment={shipment}
      orderId={shipment.orderId}
      onSubmit={handleSubmit as any}
      onCancel={handleCancel}
    />
  );
};

export default EditShipmentPage;
