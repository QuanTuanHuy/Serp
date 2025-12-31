/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment Form Component
*/

'use client';

import { useState, useEffect } from 'react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Badge,
  Label,
  Textarea,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Truck,
  Package,
  Calendar,
  CheckCircle2,
  AlertTriangle,
  Plus,
  Trash2,
  Warehouse,
  Search,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import {
  useGetOrderQuery,
  useGetFacilitiesQuery,
} from '../../api/logisticsApi';
import type {
  Shipment,
  ShipmentCreationForm,
  ShipmentUpdateForm,
  ShipmentItemForm,
} from '../../types';

interface InboundShipmentFormProps {
  shipment?: Shipment;
  orderId: string;
  onSubmit: (data: ShipmentCreationForm | ShipmentUpdateForm) => Promise<void>;
  onCancel: () => void;
}

export const InboundShipmentForm: React.FC<InboundShipmentFormProps> = ({
  shipment,
  orderId,
  onSubmit,
  onCancel,
}) => {
  const isEditMode = !!shipment;

  // Fetch order data
  const {
    data: orderResponse,
    isLoading: loadingOrder,
    isError: orderError,
  } = useGetOrderQuery(orderId, { skip: !orderId });

  const order = orderResponse?.data;

  // Fetch facilities
  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    filters: {},
    pagination: { page: 0, size: 100 },
  });

  const facilities = facilitiesResponse?.data?.items || [];

  // Form state
  const [formData, setFormData] = useState<{
    shipmentName: string;
    note: string;
    expectedDeliveryDate: string;
  }>({
    shipmentName: shipment?.shipmentName || '',
    note: shipment?.note || '',
    expectedDeliveryDate: shipment?.expectedDeliveryDate
      ? new Date(shipment.expectedDeliveryDate).toISOString().split('T')[0]
      : '',
  });

  const [items, setItems] = useState<ShipmentItemForm[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Search states
  const [orderItemSearch, setOrderItemSearch] = useState('');
  const [showOrderItemDropdown, setShowOrderItemDropdown] = useState(false);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!isEditMode && items.length === 0) {
      newErrors.items = 'Vui lòng thêm ít nhất một sản phẩm';
    }

    if (!isEditMode) {
      items.forEach((item, index) => {
        if (!item.orderItemId) {
          newErrors[`item_${index}_orderItemId`] = 'Chưa chọn sản phẩm';
        }
        if (!item.lotId.trim()) {
          newErrors[`item_${index}_lotId`] = 'Vui lòng nhập lô hàng';
        }
        if (!item.facilityId) {
          newErrors[`item_${index}_facilityId`] = 'Vui lòng chọn kho';
        }
        if (item.quantity <= 0) {
          newErrors[`item_${index}_quantity`] = 'Số lượng phải > 0';
        }
        if (
          item.quantity >
          (order?.items?.find((oi) => oi.id === item.orderItemId)
            ?.quantityRemaining || 0)
        ) {
          newErrors[`item_${index}_quantity`] =
            'Số lượng vượt quá số lượng còn lại trong đơn hàng';
        }
      });
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    setIsSubmitting(true);

    try {
      if (isEditMode) {
        const updateData: ShipmentUpdateForm = {
          shipmentName: formData.shipmentName,
          note: formData.note || undefined,
          expectedDeliveryDate: formData.expectedDeliveryDate || undefined,
        };
        await onSubmit(updateData);
      } else {
        const createData: ShipmentCreationForm = {
          orderId: orderId,
          shipmentName: formData.shipmentName,
          note: formData.note || undefined,
          expectedDeliveryDate: formData.expectedDeliveryDate || undefined,
          items: items,
        };
        await onSubmit(createData);
      }
      toast.success(
        isEditMode
          ? 'Cập nhật phiếu nhập thành công'
          : 'Tạo phiếu nhập thành công'
      );
    } catch (error: any) {
      console.error('Form submission error:', error);
      toast.error(
        error?.data?.message || 'Không thể xử lý phiếu nhập. Vui lòng thử lại.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const addItem = (orderItemId: string) => {
    const orderItem = order?.items?.find((oi) => oi.id === orderItemId);

    if (!orderItem || orderItem.quantityRemaining <= 0) {
      toast.warning('Sản phẩm không khả dụng');
      return;
    }

    setItems([
      ...items,
      {
        orderItemId: orderItem.id,
        quantity: 1,
        lotId: '',
        facilityId: '',
        note: '',
        expirationDate: '',
        manufacturingDate: '',
      },
    ]);
    setOrderItemSearch('');
    setShowOrderItemDropdown(false);
  };

  const removeItem = (index: number) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const updateItem = (index: number, updates: Partial<ShipmentItemForm>) => {
    const newItems = [...items];
    newItems[index] = { ...newItems[index], ...updates };
    setItems(newItems);
  };

  if (!orderId) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertTriangle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Thiếu thông tin đơn hàng
          </h3>
          <p className='text-muted-foreground mb-4'>
            Không thể {isEditMode ? 'chỉnh sửa' : 'tạo'} phiếu nhập mà không có
            đơn hàng.
          </p>
          <Button onClick={onCancel}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Quay lại
          </Button>
        </CardContent>
      </Card>
    );
  }

  if (loadingOrder) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse'>
          <div className='h-8 bg-muted rounded w-1/3 mb-4' />
          <div className='h-64 bg-muted rounded' />
        </div>
      </div>
    );
  }

  if (orderError || !order) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertTriangle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Không tìm thấy đơn hàng
          </h3>
          <p className='text-muted-foreground mb-4'>
            Đơn hàng không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={onCancel}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Quay lại
          </Button>
        </CardContent>
      </Card>
    );
  }

  if (order.statusId !== 'APPROVED') {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertTriangle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Đơn hàng chưa được phê duyệt
          </h3>
          <p className='text-muted-foreground mb-4'>
            Chỉ có thể {isEditMode ? 'chỉnh sửa' : 'tạo'} phiếu nhập cho các đơn
            hàng đã được phê duyệt.
          </p>
          <Button onClick={onCancel}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Quay lại
          </Button>
        </CardContent>
      </Card>
    );
  }

  const availableOrderItems =
    order.items?.filter((item) => item.quantityRemaining > 0) || [];

  return (
    <form onSubmit={handleSubmit} className='space-y-6'>
      {/* Header */}
      <div className='flex items-center gap-4'>
        <Button type='button' variant='ghost' size='icon' onClick={onCancel}>
          <ArrowLeft className='h-5 w-5' />
        </Button>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            {isEditMode ? 'Chỉnh sửa phiếu nhập' : 'Tạo phiếu nhập mới'}
          </h1>
          <p className='text-muted-foreground'>
            {isEditMode
              ? 'Cập nhật thông tin phiếu nhập'
              : `${order.orderName || order.id.slice(0, 8) + '...'}`}
          </p>
        </div>
      </div>

      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
        {/* Main Form - 2/3 width */}
        <div className='lg:col-span-2 space-y-6'>
          {/* Shipment Information */}
          <Card>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Truck className='h-5 w-5' />
                Thông tin phiếu nhập
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='space-y-2'>
                <Label htmlFor='shipmentName'>Tên phiếu</Label>
                <Input
                  id='shipmentName'
                  placeholder='Nhập tên phiếu nhập...'
                  value={formData.shipmentName}
                  onChange={(e) =>
                    setFormData({ ...formData, shipmentName: e.target.value })
                  }
                  className={cn(errors.shipmentName && 'border-destructive')}
                />
                {errors.shipmentName && (
                  <p className='text-xs text-destructive mt-1'>
                    {errors.shipmentName}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='expectedDeliveryDate'>
                  Ngày giao hàng dự kiến
                </Label>
                <div className='relative'>
                  <Calendar className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none' />
                  <Input
                    id='expectedDeliveryDate'
                    type='date'
                    value={formData.expectedDeliveryDate}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        expectedDeliveryDate: e.target.value,
                      })
                    }
                    className='pl-10'
                  />
                </div>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='note'>Ghi chú</Label>
                <Textarea
                  id='note'
                  placeholder='Nhập ghi chú cho phiếu nhập...'
                  value={formData.note}
                  onChange={(e) =>
                    setFormData({ ...formData, note: e.target.value })
                  }
                  rows={3}
                />
              </div>
            </CardContent>
          </Card>

          {/* Items - Only show in create mode */}
          {!isEditMode && (
            <Card>
              <CardHeader>
                <div className='flex items-center justify-between'>
                  <CardTitle className='flex items-center gap-2'>
                    <Package className='h-5 w-5' />
                    Danh sách sản phẩm
                  </CardTitle>
                  <Badge variant='secondary'>{items.length} sản phẩm</Badge>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                {/* Add Product Search */}
                <div className='relative'>
                  <div className='relative'>
                    <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      placeholder='Tìm kiếm và thêm sản phẩm...'
                      value={orderItemSearch}
                      onChange={(e) => {
                        setOrderItemSearch(e.target.value);
                        setShowOrderItemDropdown(true);
                      }}
                      onFocus={() => setShowOrderItemDropdown(true)}
                      className='pl-10'
                    />
                  </div>

                  {showOrderItemDropdown && availableOrderItems.length > 0 && (
                    <div className='absolute z-10 w-full mt-1 bg-popover border rounded-lg shadow-lg max-h-60 overflow-auto'>
                      {availableOrderItems
                        .filter(
                          (orderItem) =>
                            !items.some(
                              (item) => item.orderItemId === orderItem.id
                            ) &&
                            (orderItem.product?.name
                              ?.toLowerCase()
                              .includes(orderItemSearch.toLowerCase()) ||
                              orderItem.product?.skuCode
                                ?.toLowerCase()
                                .includes(orderItemSearch.toLowerCase()) ||
                              orderItemSearch === '')
                        )
                        .map((orderItem) => (
                          <button
                            key={orderItem.id}
                            type='button'
                            onClick={() => addItem(orderItem.id)}
                            className='w-full px-4 py-2 text-left hover:bg-muted transition-colors flex items-center justify-between'
                          >
                            <div>
                              <p className='font-medium'>
                                {orderItem.product?.name || orderItem.productId}
                              </p>
                              <p className='text-sm text-muted-foreground'>
                                SKU: {orderItem.product?.skuCode || 'N/A'} • Còn
                                lại: {orderItem.quantityRemaining}{' '}
                                {orderItem.unit}
                              </p>
                            </div>
                            <Plus className='h-4 w-4' />
                          </button>
                        ))}
                    </div>
                  )}
                </div>

                {errors.items && (
                  <p className='text-sm text-destructive'>{errors.items}</p>
                )}

                {items.length === 0 ? (
                  <div className='py-8 text-center text-muted-foreground'>
                    <Package className='h-12 w-12 mx-auto mb-3 opacity-50' />
                    <p>
                      Chưa có sản phẩm nào. Tìm kiếm và thêm sản phẩm trong
                      thanh tìm kiếm.
                    </p>
                  </div>
                ) : (
                  items.map((item, index) => {
                    const orderItem = order.items?.find(
                      (oi) => oi.id === item.orderItemId
                    );

                    return (
                      <Card
                        key={index}
                        className='border-muted-foreground/20 relative'
                      >
                        <CardContent className='p-4 space-y-4'>
                          <div className='flex items-start justify-between'>
                            <div className='flex-1'>
                              <h4 className='font-semibold'>
                                {orderItem?.product?.name ||
                                  orderItem?.productId}
                              </h4>
                              <p className='text-sm text-muted-foreground'>
                                SKU: {orderItem?.product?.skuCode || 'N/A'} •
                                Còn lại: {orderItem?.quantityRemaining}{' '}
                                {orderItem?.unit}
                              </p>
                            </div>
                            <Button
                              type='button'
                              variant='ghost'
                              size='icon'
                              onClick={() => removeItem(index)}
                              className='text-destructive hover:text-destructive'
                            >
                              <Trash2 className='h-4 w-4' />
                            </Button>
                          </div>

                          <div className='grid grid-cols-2 gap-4'>
                            <div className='space-y-2'>
                              <Label htmlFor={`quantity_${index}`}>
                                Số lượng{' '}
                                <span className='text-destructive'>*</span>
                              </Label>
                              <Input
                                id={`quantity_${index}`}
                                type='number'
                                min='1'
                                max={orderItem?.quantityRemaining || 1}
                                value={item.quantity}
                                onChange={(e) =>
                                  updateItem(index, {
                                    quantity: parseInt(e.target.value) || 0,
                                  })
                                }
                                className={cn(
                                  errors[`item_${index}_quantity`] &&
                                    'border-destructive'
                                )}
                              />
                              {errors[`item_${index}_quantity`] && (
                                <p className='text-xs text-destructive mt-1'>
                                  {errors[`item_${index}_quantity`]}
                                </p>
                              )}
                              {orderItem?.quantityRemaining !== undefined && (
                                <Badge
                                  variant='secondary'
                                  className={
                                    item.quantity > orderItem?.quantityRemaining
                                      ? 'mt-2 text-red-700 dark:text-red-400'
                                      : 'mt-2 text-emerald-700 dark:text-emerald-400'
                                  }
                                >
                                  Số lượng còn lại:{' '}
                                  {orderItem?.quantityRemaining}
                                </Badge>
                              )}
                            </div>

                            <div className='space-y-2'>
                              <Label htmlFor={`lotId_${index}`}>
                                Lô hàng{' '}
                                <span className='text-destructive'>*</span>
                              </Label>
                              <Input
                                id={`lotId_${index}`}
                                placeholder='Nhập lô hàng...'
                                value={item.lotId}
                                onChange={(e) =>
                                  updateItem(index, { lotId: e.target.value })
                                }
                                className={cn(
                                  errors[`item_${index}_lotId`] &&
                                    'border-destructive'
                                )}
                              />
                              {errors[`item_${index}_lotId`] && (
                                <p className='text-xs text-destructive mt-1'>
                                  {errors[`item_${index}_lotId`]}
                                </p>
                              )}
                            </div>
                          </div>

                          <div className='space-y-2'>
                            <Label htmlFor={`facilityId_${index}`}>
                              Kho <span className='text-destructive'>*</span>
                            </Label>
                            <select
                              id={`facilityId_${index}`}
                              value={item.facilityId}
                              onChange={(e) =>
                                updateItem(index, {
                                  facilityId: e.target.value,
                                })
                              }
                              className={cn(
                                'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
                                errors[`item_${index}_facilityId`] &&
                                  'border-destructive'
                              )}
                            >
                              <option value=''>Chọn kho</option>
                              {facilities.map((facility) => (
                                <option key={facility.id} value={facility.id}>
                                  {facility.name}
                                </option>
                              ))}
                            </select>
                            {errors[`item_${index}_facilityId`] && (
                              <p className='text-xs text-destructive mt-1'>
                                {errors[`item_${index}_facilityId`]}
                              </p>
                            )}
                          </div>

                          <div className='grid grid-cols-2 gap-4'>
                            <div className='space-y-2'>
                              <Label htmlFor={`manufacturingDate_${index}`}>
                                Ngày sản xuất
                              </Label>
                              <Input
                                id={`manufacturingDate_${index}`}
                                type='date'
                                value={item.manufacturingDate}
                                onChange={(e) =>
                                  updateItem(index, {
                                    manufacturingDate: e.target.value,
                                  })
                                }
                              />
                            </div>

                            <div className='space-y-2'>
                              <Label htmlFor={`expirationDate_${index}`}>
                                Ngày hết hạn
                              </Label>
                              <Input
                                id={`expirationDate_${index}`}
                                type='date'
                                value={item.expirationDate}
                                onChange={(e) =>
                                  updateItem(index, {
                                    expirationDate: e.target.value,
                                  })
                                }
                              />
                            </div>
                          </div>

                          <div className='space-y-2'>
                            <Label htmlFor={`note_${index}`}>Ghi chú</Label>
                            <Input
                              id={`note_${index}`}
                              placeholder='Ghi chú cho sản phẩm này...'
                              value={item.note}
                              onChange={(e) =>
                                updateItem(index, { note: e.target.value })
                              }
                            />
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })
                )}
              </CardContent>
            </Card>
          )}
        </div>

        {/* Summary Sidebar - 1/3 width */}
        <div className='space-y-6'>
          <div className='sticky top-6 space-y-6'>
            <Card>
              <CardHeader>
                <CardTitle>{isEditMode ? 'Xem trước' : 'Tóm tắt'}</CardTitle>
              </CardHeader>
              <CardContent className='space-y-4'>
                {!isEditMode && (
                  <>
                    <div className='flex items-center justify-between'>
                      <span className='text-muted-foreground'>
                        Tổng sản phẩm
                      </span>
                      <span className='font-semibold'>{items.length}</span>
                    </div>
                    <div className='flex items-center justify-between'>
                      <span className='text-muted-foreground'>
                        Tổng số lượng
                      </span>
                      <span className='font-semibold'>
                        {items.reduce((sum, item) => sum + item.quantity, 0)}
                      </span>
                    </div>
                  </>
                )}

                {formData.expectedDeliveryDate && (
                  <div className='pt-4 border-t'>
                    <span className='text-sm text-muted-foreground'>
                      Giao hàng dự kiến
                    </span>
                    <p className='font-medium'>
                      {new Date(
                        formData.expectedDeliveryDate
                      ).toLocaleDateString('vi-VN')}
                    </p>
                  </div>
                )}

                <div className='border-t pt-4 space-y-3'>
                  <Button
                    type='submit'
                    className='w-full'
                    disabled={
                      isSubmitting || (!isEditMode && items.length === 0)
                    }
                  >
                    {isSubmitting ? (
                      <>
                        <div className='animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2' />
                        Đang xử lý...
                      </>
                    ) : (
                      <>
                        <CheckCircle2 className='h-4 w-4 mr-2' />
                        {isEditMode ? 'Cập nhật phiếu kho' : 'Tạo phiếu kho'}
                      </>
                    )}
                  </Button>
                  <Button
                    type='button'
                    variant='outline'
                    className='w-full'
                    onClick={onCancel}
                    disabled={isSubmitting}
                  >
                    Hủy
                  </Button>
                </div>
              </CardContent>
            </Card>

            {/* Order Info */}
            <Card>
              <CardHeader>
                <CardTitle className='text-base'>Thông tin đơn hàng</CardTitle>
              </CardHeader>
              <CardContent className='space-y-3 text-sm'>
                <div className='space-y-2'>
                  <Label className='text-muted-foreground'>Tên đơn hàng</Label>
                  <p className='font-medium'>
                    {order.orderName || order.id.slice(0, 12) + '...'}
                  </p>
                </div>
                {!isEditMode && (
                  <div className='space-y-2'>
                    <Label className='text-muted-foreground'>
                      Sản phẩm còn lại
                    </Label>
                    <div className='space-y-2'>
                      {availableOrderItems.length === 0 ? (
                        <p className='text-sm text-muted-foreground'>
                          Không có sản phẩm nào còn lại trong đơn hàng.
                        </p>
                      ) : (
                        availableOrderItems.map((orderItem) => (
                          <div
                            key={orderItem.id}
                            className='flex items-center justify-between p-2 rounded-md bg-muted/50'
                          >
                            <div className='flex-1 min-w-0'>
                              <p className='text-xs font-medium truncate'>
                                {orderItem.product?.name || orderItem.productId}
                              </p>
                            </div>
                            <Badge variant='secondary' className='ml-2'>
                              {orderItem.quantityRemaining}
                            </Badge>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </form>
  );
};

export default InboundShipmentForm;
