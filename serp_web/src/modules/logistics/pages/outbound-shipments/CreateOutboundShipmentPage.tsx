/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create outbound shipment page
*/

'use client';

import { useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { toast } from 'sonner';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { ArrowLeft, Package, Plus, Trash2, Truck } from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useCreateOutboundShipmentMutation,
  useGetFacilitiesQuery,
  useGetOrderQuery,
  useGetOrdersQuery,
} from '../../api/logisticsApi';
import type { OutboundShipmentItemForm } from '../../types';

interface FormState {
  orderId: string;
  facilityId: string;
  name: string;
}

interface DetailOption {
  inventoryItemDetailId: string;
  inventoryItemId: string;
  productId: string;
  productName: string;
  lotId: string;
  maxQuantity: number;
}

export const CreateOutboundShipmentPage: React.FC = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [createShipment, { isLoading: isCreating }] =
    useCreateOutboundShipmentMutation();

  const [form, setForm] = useState<FormState>({
    orderId: searchParams.get('orderId') || '',
    facilityId: '',
    name: '',
  });

  const [items, setItems] = useState<OutboundShipmentItemForm[]>([]);
  const [selectedDetailId, setSelectedDetailId] = useState('');
  const [selectedQuantity, setSelectedQuantity] = useState<number>(1);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: ordersResponse } = useGetOrdersQuery({
    filters: {
      orderTypeId: 'SALES',
      statusId: 'APPROVED',
    },
    pagination: {
      page: 0,
      size: 100,
    },
  });

  const { data: orderResponse, isFetching: isLoadingOrder } = useGetOrderQuery(
    form.orderId,
    {
      skip: !form.orderId,
    }
  );

  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    filters: { statusId: 'ACTIVE' },
    pagination: { page: 0, size: 100 },
  });

  const order = orderResponse?.data;

  const detailOptions = useMemo(() => {
    const map = new Map<string, DetailOption>();

    order?.items?.forEach((orderItem) => {
      const productName = orderItem.product?.name || orderItem.productId;

      (orderItem.allocatedInventoryItems || []).forEach((detail) => {
        const maxQuantity = detail.notYetOutboundQuantity ?? detail.quantity;

        if (maxQuantity <= 0) {
          return;
        }

        map.set(detail.id, {
          inventoryItemDetailId: detail.id,
          inventoryItemId: detail.inventoryItemId,
          productId: detail.productId,
          productName,
          lotId: detail.lotId,
          maxQuantity,
        });
      });
    });

    return Array.from(map.values());
  }, [order]);

  const optionMap = useMemo(() => {
    const map = new Map<string, DetailOption>();
    detailOptions.forEach((item) => {
      map.set(item.inventoryItemDetailId, item);
    });
    return map;
  }, [detailOptions]);

  const validateBeforeSubmit = () => {
    const nextErrors: Record<string, string> = {};

    if (!form.orderId) {
      nextErrors.orderId = 'Vui lòng chọn đơn hàng';
    }

    if (!form.facilityId) {
      nextErrors.facilityId = 'Vui lòng chọn kho xuất';
    }

    if (items.length === 0) {
      nextErrors.items = 'Vui lòng thêm ít nhất một dòng hàng';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleAddItem = () => {
    const option = optionMap.get(selectedDetailId);

    if (!option) {
      toast.error('Vui lòng chọn dòng tồn kho để xuất');
      return;
    }

    if (selectedQuantity <= 0) {
      toast.error('Số lượng phải lớn hơn 0');
      return;
    }

    const existingIndex = items.findIndex(
      (item) => item.inventoryItemDetailId === option.inventoryItemDetailId
    );

    if (existingIndex >= 0) {
      const existingItem = items[existingIndex];
      const nextQuantity = existingItem.quantity + selectedQuantity;

      if (nextQuantity > option.maxQuantity) {
        toast.error(`Số lượng vượt mức có thể xuất (${option.maxQuantity})`);
        return;
      }

      const nextItems = [...items];
      nextItems[existingIndex] = {
        ...nextItems[existingIndex],
        quantity: nextQuantity,
      };
      setItems(nextItems);
      setSelectedDetailId('');
      setSelectedQuantity(1);
      return;
    }

    if (selectedQuantity > option.maxQuantity) {
      toast.error(`Số lượng vượt mức có thể xuất (${option.maxQuantity})`);
      return;
    }

    setItems((prev) => [
      ...prev,
      {
        inventoryItemDetailId: option.inventoryItemDetailId,
        inventoryItemId: option.inventoryItemId,
        productId: option.productId,
        quantity: selectedQuantity,
      },
    ]);

    setSelectedDetailId('');
    setSelectedQuantity(1);
  };

  const updateItemQuantity = (index: number, quantity: number) => {
    const target = items[index];
    const option = optionMap.get(target.inventoryItemDetailId);
    const maxQuantity = option?.maxQuantity || quantity;

    const nextQuantity = Math.max(1, Math.min(quantity, maxQuantity));

    setItems((prev) => {
      const next = [...prev];
      next[index] = {
        ...next[index],
        quantity: nextQuantity,
      };
      return next;
    });
  };

  const removeItem = (index: number) => {
    setItems((prev) => prev.filter((_, itemIndex) => itemIndex !== index));
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!validateBeforeSubmit()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    try {
      await createShipment({
        orderId: form.orderId,
        facilityId: form.facilityId,
        name: form.name || undefined,
        items,
      }).unwrap();

      toast.success('Tạo phiếu xuất thành công');
      router.push('/logistics/outbound-shipments');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể tạo phiếu xuất');
    }
  };

  return (
    <form className='space-y-6' onSubmit={handleSubmit}>
      <div className='flex items-center gap-4'>
        <Button
          type='button'
          variant='ghost'
          size='icon'
          onClick={() => router.push('/logistics/outbound-shipments')}
        >
          <ArrowLeft className='h-5 w-5' />
        </Button>

        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Tạo phiếu xuất kho
          </h1>
          <p className='text-muted-foreground'>
            Tạo outbound shipment mới cho đơn hàng bán đã duyệt
          </p>
        </div>
      </div>

      <div className='grid gap-6 lg:grid-cols-3'>
        <div className='space-y-6 lg:col-span-2'>
          <Card>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Truck className='h-5 w-5' />
                Thông tin phiếu xuất
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='space-y-2'>
                <Label htmlFor='orderId'>Đơn hàng bán</Label>
                <select
                  id='orderId'
                  value={form.orderId}
                  onChange={(event) => {
                    setForm((prev) => ({
                      ...prev,
                      orderId: event.target.value,
                    }));
                    setItems([]);
                    setSelectedDetailId('');
                  }}
                  className={cn(
                    'w-full rounded-md border bg-background px-3 py-2 text-sm',
                    errors.orderId && 'border-destructive'
                  )}
                >
                  <option value=''>Chọn đơn hàng</option>
                  {(ordersResponse?.data?.items || []).map((orderItem) => (
                    <option key={orderItem.id} value={orderItem.id}>
                      {orderItem.orderName || orderItem.id} ({orderItem.id})
                    </option>
                  ))}
                </select>
                {errors.orderId && (
                  <p className='text-xs text-destructive'>{errors.orderId}</p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='facilityId'>Kho xuất</Label>
                <select
                  id='facilityId'
                  value={form.facilityId}
                  onChange={(event) =>
                    setForm((prev) => ({
                      ...prev,
                      facilityId: event.target.value,
                    }))
                  }
                  className={cn(
                    'w-full rounded-md border bg-background px-3 py-2 text-sm',
                    errors.facilityId && 'border-destructive'
                  )}
                >
                  <option value=''>Chọn kho xuất</option>
                  {(facilitiesResponse?.data?.items || []).map((facility) => (
                    <option key={facility.id} value={facility.id}>
                      {facility.name}
                    </option>
                  ))}
                </select>
                {errors.facilityId && (
                  <p className='text-xs text-destructive'>
                    {errors.facilityId}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='name'>Tên phiếu xuất</Label>
                <Input
                  id='name'
                  value={form.name}
                  onChange={(event) =>
                    setForm((prev) => ({ ...prev, name: event.target.value }))
                  }
                  placeholder='Để trống để backend tự sinh tên'
                />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <CardTitle className='flex items-center gap-2'>
                  <Package className='h-5 w-5' />
                  Danh sách dòng hàng xuất
                </CardTitle>
                <Badge variant='secondary'>{items.length} dòng</Badge>
              </div>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid gap-3 rounded-lg border p-3 md:grid-cols-[1fr_120px_auto]'>
                <select
                  value={selectedDetailId}
                  onChange={(event) => setSelectedDetailId(event.target.value)}
                  disabled={!form.orderId || isLoadingOrder}
                  className='w-full rounded-md border bg-background px-3 py-2 text-sm disabled:cursor-not-allowed disabled:opacity-60'
                >
                  <option value=''>Chọn dòng tồn kho đã allocate</option>
                  {detailOptions.map((option) => (
                    <option
                      key={option.inventoryItemDetailId}
                      value={option.inventoryItemDetailId}
                    >
                      {option.productName} | Lot {option.lotId} | Có thể xuất:{' '}
                      {option.maxQuantity}
                    </option>
                  ))}
                </select>

                <Input
                  type='number'
                  min={1}
                  value={selectedQuantity}
                  onChange={(event) =>
                    setSelectedQuantity(Number(event.target.value) || 1)
                  }
                />

                <Button type='button' onClick={handleAddItem}>
                  <Plus className='mr-2 h-4 w-4' />
                  Thêm
                </Button>
              </div>

              {errors.items && (
                <p className='text-xs text-destructive'>{errors.items}</p>
              )}

              {items.length > 0 ? (
                <div className='space-y-2'>
                  {items.map((item, index) => {
                    const option = optionMap.get(item.inventoryItemDetailId);

                    return (
                      <div
                        key={item.inventoryItemDetailId}
                        className='grid items-center gap-3 rounded-lg border p-3 md:grid-cols-[1fr_140px_auto]'
                      >
                        <div className='text-sm'>
                          <p className='font-medium'>
                            {option?.productName || item.productId}
                          </p>
                          <p className='text-muted-foreground'>
                            Detail: {item.inventoryItemDetailId} • Max:{' '}
                            {option?.maxQuantity || item.quantity}
                          </p>
                        </div>

                        <Input
                          type='number'
                          min={1}
                          max={option?.maxQuantity || item.quantity}
                          value={item.quantity}
                          onChange={(event) =>
                            updateItemQuantity(
                              index,
                              Number(event.target.value) || 1
                            )
                          }
                        />

                        <Button
                          type='button'
                          variant='ghost'
                          className='text-destructive hover:text-destructive'
                          onClick={() => removeItem(index)}
                        >
                          <Trash2 className='h-4 w-4' />
                        </Button>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className='rounded-lg border border-dashed py-10 text-center text-sm text-muted-foreground'>
                  Chưa có dòng hàng nào được chọn
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <Card className='h-fit'>
          <CardHeader>
            <CardTitle>Tóm tắt</CardTitle>
          </CardHeader>
          <CardContent className='space-y-3 text-sm'>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Đơn hàng</span>
              <span className='font-medium'>
                {order?.orderName || form.orderId || 'Chưa chọn'}
              </span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Kho xuất</span>
              <span className='font-medium'>
                {(facilitiesResponse?.data?.items || []).find(
                  (facility) => facility.id === form.facilityId
                )?.name || 'Chưa chọn'}
              </span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Tổng dòng hàng</span>
              <span className='font-medium'>{items.length}</span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Tổng số lượng</span>
              <span className='font-medium'>
                {items.reduce((sum, item) => sum + item.quantity, 0)}
              </span>
            </div>

            <div className='border-t pt-3'>
              <Button className='w-full' type='submit' disabled={isCreating}>
                {isCreating ? 'Đang tạo...' : 'Tạo phiếu xuất'}
              </Button>
              <Button
                type='button'
                variant='outline'
                className='mt-2 w-full'
                onClick={() => router.push('/logistics/outbound-shipments')}
              >
                Hủy
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </form>
  );
};

export default CreateOutboundShipmentPage;
