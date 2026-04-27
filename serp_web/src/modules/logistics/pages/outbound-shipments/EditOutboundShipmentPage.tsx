/*
Author: QuanTuanHuy
Description: Part of Serp Project - Edit outbound shipment page
*/

'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import {
  useGetOutboundShipmentQuery,
  useUpdateOutboundShipmentMutation,
} from '../../api/logisticsApi';

interface EditOutboundShipmentPageProps {
  shipmentId: string;
}

export const EditOutboundShipmentPage: React.FC<
  EditOutboundShipmentPageProps
> = ({ shipmentId }) => {
  const router = useRouter();

  const {
    data: shipmentResponse,
    isLoading,
    isError,
  } = useGetOutboundShipmentQuery(shipmentId);

  const [updateShipment, { isLoading: isUpdating }] =
    useUpdateOutboundShipmentMutation();

  const shipment = shipmentResponse?.data;

  const [name, setName] = useState('');

  useEffect(() => {
    if (shipment?.name) {
      setName(shipment.name);
    }
  }, [shipment?.name]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!shipment) return;

    try {
      await updateShipment({
        shipmentId,
        data: {
          name: name.trim() || undefined,
        },
      }).unwrap();
      toast.success('Cập nhật phiếu xuất thành công');
      router.push(`/logistics/outbound-shipments/${shipmentId}`);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể cập nhật phiếu xuất');
    }
  };

  if (isLoading) {
    return (
      <Card className='animate-pulse'>
        <CardContent className='p-8'>
          <div className='mb-3 h-6 w-1/3 rounded bg-muted' />
          <div className='h-4 w-2/3 rounded bg-muted' />
        </CardContent>
      </Card>
    );
  }

  if (isError || !shipment) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='mx-auto mb-4 h-12 w-12 text-destructive' />
          <h3 className='mb-2 text-lg font-semibold'>
            Không tìm thấy phiếu xuất
          </h3>
          <p className='mb-4 text-muted-foreground'>
            Phiếu xuất không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/logistics/outbound-shipments')}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <form className='space-y-6' onSubmit={handleSubmit}>
      <div className='flex items-center gap-4'>
        <Button
          type='button'
          variant='ghost'
          size='icon'
          onClick={() =>
            router.push(`/logistics/outbound-shipments/${shipmentId}`)
          }
        >
          <ArrowLeft className='h-5 w-5' />
        </Button>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Đổi tên phiếu xuất
          </h1>
          <p className='text-muted-foreground'>
            Chỉ cho phép chỉnh sửa khi phiếu đang ở trạng thái CREATED
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Thông tin cập nhật</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='shipment-name'>Tên phiếu xuất</Label>
            <Input
              id='shipment-name'
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder='Nhập tên mới'
              disabled={shipment.status !== 'CREATED'}
            />
          </div>

          <div className='flex items-center gap-2'>
            <Button
              type='submit'
              disabled={shipment.status !== 'CREATED' || isUpdating}
            >
              {isUpdating ? 'Đang lưu...' : 'Lưu thay đổi'}
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={() =>
                router.push(`/logistics/outbound-shipments/${shipmentId}`)
              }
            >
              Hủy
            </Button>
          </div>
        </CardContent>
      </Card>
    </form>
  );
};

export default EditOutboundShipmentPage;
