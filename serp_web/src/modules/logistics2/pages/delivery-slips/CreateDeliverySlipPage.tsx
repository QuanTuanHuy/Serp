'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  AlertCircle,
  ArrowLeft,
  Loader2,
  Package,
  Plus,
  Truck,
  X,
} from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  Input,
  Label,
} from '@/shared/components/ui';
import { getErrorMessage } from '@/lib/store/api';
import { cn } from '@/shared/utils';
import {
  useCreateDeliverySlipMutation,
  useGetOutboundShipmentDetailQuery,
} from '../../api/logistics2Api';
import type {
  DeliverySlipItemForm,
  InventoryItem,
  OutboundShipmentItem,
  OutboundShipmentStatus,
} from '../../types';

interface CreateDeliverySlipPageProps {
  shipmentId: string;
}

interface ShipmentItemOption {
  outboundShipmentItemId: string;
  inventoryItemId: string;
  productId: string;
  productName: string;
  lotId: string;
  maxQuantity: number;
  inventoryItem: InventoryItem;
}

const formatNumber = (value?: number) => {
  if (!value) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN');
};

export const CreateDeliverySlipPage: React.FC<CreateDeliverySlipPageProps> = ({
  shipmentId,
}) => {
  const router = useRouter();

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [searchValue, setSearchValue] = useState('');
  const [selectedShipmentItemId, setSelectedShipmentItemId] = useState('');
  const [items, setItems] = useState<DeliverySlipItemForm[]>([]);

  const {
    data: shipmentResponse,
    isLoading,
    isError,
  } = useGetOutboundShipmentDetailQuery(shipmentId);

  const shipment = shipmentResponse?.data;

  const [createDeliverySlip, { isLoading: isCreating }] =
    useCreateDeliverySlipMutation();

  const goBack = () => {
    router.push(`/logistics2/outbound-shipments/${shipmentId}/delivery-slips`);
  };

  const allShipmentItemOptions = useMemo<ShipmentItemOption[]>(() => {
    if (!shipment?.items) {
      return [];
    }

    return shipment.items
      .map((item: OutboundShipmentItem) => {
        const maxQuantity = item.quantityRemaining ?? 0;

        if (maxQuantity <= 0) {
          return null;
        }

        return {
          outboundShipmentItemId: item.id,
          inventoryItemId: item.inventoryItemId,
          productId: item.productId,
          productName: item.product?.name || item.productId,
          lotId: item.inventoryItem?.lotId || 'N/A',
          maxQuantity,
          inventoryItem: item.inventoryItem as InventoryItem,
        };
      })
      .filter((option): option is ShipmentItemOption => option !== null);
  }, [shipment]);

  const shipmentItemOptionMap = useMemo(() => {
    const map = new Map<string, ShipmentItemOption>();

    allShipmentItemOptions.forEach((option) => {
      map.set(option.outboundShipmentItemId, option);
    });

    return map;
  }, [allShipmentItemOptions]);

  const filteredAvailableOptions = useMemo(() => {
    const keyword = searchValue.trim().toLowerCase();

    return allShipmentItemOptions.filter((option) => {
      const isAdded = items.some(
        (item) => item.outboundShipmentItemId === option.outboundShipmentItemId
      );

      if (isAdded) {
        return false;
      }

      if (!keyword) {
        return true;
      }

      return (
        option.productName.toLowerCase().includes(keyword) ||
        option.lotId.toLowerCase().includes(keyword)
      );
    });
  }, [allShipmentItemOptions, items, searchValue]);

  const canCreateSlip = shipment?.status === 'READY_TO_EXPORT';

  const selectedItemsCount = items.length;

  const totalQuantity = useMemo(
    () => items.reduce((sum, item) => sum + item.quantity, 0),
    [items]
  );

  const validateForm = () => {
    const nextErrors: Record<string, string> = {};

    if (!shipment) {
      nextErrors.shipment = 'Không tìm thấy thông tin phiếu xuất.';
    }

    if (!canCreateSlip) {
      nextErrors.status =
        'Chỉ có thể tạo đơn giao hàng khi phiếu xuất ở trạng thái READY_TO_EXPORT.';
    }

    if (items.length === 0) {
      nextErrors.items = 'Vui lòng thêm ít nhất một mặt hàng.';
    }

    items.forEach((item, index) => {
      const option = shipmentItemOptionMap.get(item.outboundShipmentItemId);

      if (!option) {
        nextErrors[`item_${index}`] =
          'Mặt hàng không hợp lệ hoặc không còn khả dụng.';
        return;
      }

      if (item.quantity <= 0 || item.quantity > option.maxQuantity) {
        nextErrors[`item_quantity_${index}`] =
          'Số lượng vượt quá số lượng có thể tạo đơn giao hàng.';
      }
    });

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const addItem = () => {
    if (!selectedShipmentItemId) {
      setErrors((prev) => ({
        ...prev,
        selectedShipmentItemId: 'Vui lòng chọn một mặt hàng để thêm.',
      }));
      return;
    }

    const selectedOption = shipmentItemOptionMap.get(selectedShipmentItemId);

    if (!selectedOption) {
      toast.error('Mặt hàng không còn khả dụng.');
      return;
    }

    const isAlreadyAdded = items.some(
      (item) => item.outboundShipmentItemId === selectedShipmentItemId
    );

    if (isAlreadyAdded) {
      toast.error('Mặt hàng đã được thêm trước đó.');
      return;
    }

    setItems((prev) => [
      ...prev,
      {
        outboundShipmentItemId: selectedOption.outboundShipmentItemId,
        inventoryItemId: selectedOption.inventoryItemId,
        productId: selectedOption.productId,
        quantity: 1,
      },
    ]);

    setSelectedShipmentItemId('');
    setErrors((prev) => {
      const next = { ...prev };
      delete next.selectedShipmentItemId;
      delete next.items;
      return next;
    });
  };

  const removeItem = (index: number) => {
    setItems((prev) => prev.filter((_, itemIndex) => itemIndex !== index));
  };

  const updateItemQuantity = (index: number, quantity: number) => {
    setItems((prev) => {
      const next = [...prev];
      next[index] = {
        ...next[index],
        quantity,
      };
      return next;
    });
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!shipment) {
      toast.error('Không tìm thấy thông tin phiếu xuất.');
      return;
    }

    if (!validateForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin.');
      return;
    }

    try {
      await createDeliverySlip({
        outboundShipmentId: shipment.id,
        facilityId: shipment.facilityId,
        items,
      }).unwrap();

      toast.success('Tạo đơn giao hàng thành công.');
      router.push(
        `/logistics2/outbound-shipments/${shipment.id}/delivery-slips`
      );
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse space-y-4'>
          <div className='h-8 w-1/3 rounded bg-muted' />
          <div className='h-64 rounded bg-muted' />
        </div>
      </div>
    );
  }

  if (isError || !shipment) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='space-y-4 p-8 text-center'>
          <AlertCircle className='mx-auto h-12 w-12 text-destructive' />
          <h3 className='text-lg font-semibold'>Không tìm thấy phiếu xuất</h3>
          <p className='text-muted-foreground'>
            Phiếu xuất không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={goBack}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại
          </Button>
        </CardContent>
      </Card>
    );
  }

  if (!canCreateSlip) {
    return (
      <Card className='border-amber-300/60 bg-amber-50/50 dark:border-amber-900/50 dark:bg-amber-900/10'>
        <CardContent className='space-y-4 p-8 text-center'>
          <Truck className='mx-auto h-12 w-12 text-amber-600 dark:text-amber-300' />
          <h3 className='text-lg font-semibold'>Chưa thể tạo đơn giao hàng</h3>
          <p className='text-muted-foreground'>
            Đơn giao hàng chỉ được tạo khi phiếu xuất ở trạng thái{' '}
            <strong>READY_TO_EXPORT</strong>.
          </p>
          <Button onClick={goBack}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại phiếu xuất
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <form onSubmit={handleSubmit} className='space-y-6'>
      <div className='flex items-start gap-3'>
        <Button type='button' variant='ghost' size='icon' onClick={goBack}>
          <ArrowLeft className='h-4 w-4' />
        </Button>

        <div>
          <div className='mb-2 flex flex-wrap items-center gap-2'>
            <h1 className='text-2xl font-bold tracking-tight'>
              Tạo đơn giao hàng mới
            </h1>
          </div>
          <p className='text-sm text-muted-foreground'>
            Tạo đơn giao hàng từ phiếu xuất: {shipment.name || shipment.id}
          </p>
        </div>
      </div>

      <div className='grid gap-6 lg:grid-cols-3'>
        <div className='space-y-6 lg:col-span-2'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Thông tin phiếu xuất</h3>
            </CardHeader>
            <CardContent className='grid gap-4 sm:grid-cols-2'>
              <div>
                <Label className='text-muted-foreground'>Mã phiếu xuất</Label>
                <p className='font-medium'>{shipment.id}</p>
              </div>
              <div>
                <Label className='text-muted-foreground'>Kho xuất</Label>
                <p className='font-medium'>
                  {shipment.facility?.name || shipment.facilityId}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
                <div>
                  <h3 className='font-semibold'>Danh sách mặt hàng giao</h3>
                  <p className='text-sm text-muted-foreground'>
                    Chọn các mặt hàng chưa giao để tạo đơn giao hàng.
                  </p>
                </div>
                <Badge variant='secondary'>{selectedItemsCount} mặt hàng</Badge>
              </div>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid gap-3 sm:grid-cols-[1fr_auto]'>
                <div className='space-y-2'>
                  <Label htmlFor='shipment-item'>Mặt hàng tồn kho</Label>
                  <select
                    id='shipment-item'
                    value={selectedShipmentItemId}
                    onChange={(event) =>
                      setSelectedShipmentItemId(event.target.value)
                    }
                    className={cn(
                      'h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm',
                      errors.selectedShipmentItemId && 'border-destructive'
                    )}
                  >
                    <option value=''>Chọn mặt hàng</option>
                    {filteredAvailableOptions.map((option) => (
                      <option
                        key={option.outboundShipmentItemId}
                        value={option.outboundShipmentItemId}
                      >
                        {option.productName} • Lô: {option.lotId} • Còn lại:{' '}
                        {formatNumber(option.maxQuantity)} • NSX:{' '}
                        {formatDate(option.inventoryItem.manufacturingDate)} •
                        HSD: {formatDate(option.inventoryItem.expirationDate)}
                      </option>
                    ))}
                  </select>
                  {errors.selectedShipmentItemId && (
                    <p className='text-xs text-destructive'>
                      {errors.selectedShipmentItemId}
                    </p>
                  )}
                </div>

                <Button
                  type='button'
                  className='self-end'
                  onClick={addItem}
                  disabled={filteredAvailableOptions.length === 0}
                >
                  <Plus className='mr-2 h-4 w-4' />
                  Thêm
                </Button>
              </div>

              {errors.items && (
                <p className='text-sm text-destructive'>{errors.items}</p>
              )}

              {items.length > 0 ? (
                <div className='space-y-3'>
                  {items.map((item, index) => {
                    const option = shipmentItemOptionMap.get(
                      item.outboundShipmentItemId
                    );

                    if (!option) {
                      return null;
                    }

                    return (
                      <div
                        key={item.outboundShipmentItemId}
                        className='rounded-lg border p-4'
                      >
                        <div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
                          <div>
                            <p className='font-medium'>{option.productName}</p>
                            <div className='flex items-center gap-4'>
                              <p className='text-sm text-muted-foreground'>
                                Lô: {option.lotId}
                              </p>
                              <p className='text-sm text-muted-foreground'>
                                Số lượng tối đa:{' '}
                                {formatNumber(option.maxQuantity)}
                              </p>
                              <p className='text-sm text-muted-foreground'>
                                NSX:{' '}
                                {formatDate(
                                  option.inventoryItem.manufacturingDate
                                )}
                              </p>
                              <p className='text-sm text-muted-foreground'>
                                HSD:{' '}
                                {formatDate(
                                  option.inventoryItem.expirationDate
                                )}
                              </p>
                            </div>
                          </div>

                          <div className='flex items-center gap-2'>
                            <div className='space-y-1'>
                              <Label htmlFor={`quantity-${index}`}>
                                Số lượng
                              </Label>
                              <Input
                                id={`quantity-${index}`}
                                type='number'
                                min={1}
                                max={option.maxQuantity}
                                value={item.quantity}
                                onChange={(event) =>
                                  updateItemQuantity(
                                    index,
                                    Number(event.target.value) || 1
                                  )
                                }
                                className='w-28'
                              />
                              {errors[`item_quantity_${index}`] && (
                                <p className='text-xs text-destructive'>
                                  {errors[`item_quantity_${index}`]}
                                </p>
                              )}
                            </div>

                            <Button
                              type='button'
                              variant='ghost'
                              size='icon'
                              className='mt-6 text-destructive hover:text-destructive'
                              onClick={() => removeItem(index)}
                            >
                              <X className='h-4 w-4' />
                            </Button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className='rounded-lg border border-dashed py-10 text-center text-muted-foreground'>
                  Chưa có mặt hàng nào được chọn.
                </div>
              )}

              {filteredAvailableOptions.length === 0 && items.length === 0 && (
                <div className='rounded-lg border border-dashed p-4 text-sm text-muted-foreground'>
                  Tất cả mặt hàng trong phiếu xuất đã được tách đơn hoặc không
                  còn số lượng khả dụng.
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <Card className='h-fit'>
          <CardHeader>
            <div className='flex items-center gap-2'>
              <Package className='h-5 w-5 text-primary' />
              <h3 className='font-semibold'>Tóm tắt tạo đơn</h3>
            </div>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground'>Số mặt hàng</span>
              <span className='font-medium'>{selectedItemsCount}</span>
            </div>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground'>Tổng số lượng</span>
              <span className='font-medium'>{formatNumber(totalQuantity)}</span>
            </div>

            {errors.status && (
              <p className='rounded-md bg-destructive/10 p-2 text-xs text-destructive'>
                {errors.status}
              </p>
            )}

            <div className='space-y-2 pt-2'>
              <Button type='submit' className='w-full' disabled={isCreating}>
                {isCreating ? (
                  <>
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    Đang tạo...
                  </>
                ) : (
                  <>
                    <Truck className='mr-2 h-4 w-4' />
                    Tạo đơn giao hàng
                  </>
                )}
              </Button>

              <Button
                type='button'
                variant='outline'
                className='w-full'
                onClick={goBack}
                disabled={isCreating}
              >
                <ArrowLeft className='mr-2 h-4 w-4' />
                Hủy
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </form>
  );
};

export default CreateDeliverySlipPage;
