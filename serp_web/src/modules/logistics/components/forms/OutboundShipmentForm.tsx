/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment Form Component
*/

'use client';

import { useMemo, useState } from 'react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Badge,
  Label,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Truck,
  Package,
  CheckCircle2,
  AlertTriangle,
  Plus,
  Trash2,
  Search,
  PlusIcon,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import {
  useGetOrderQuery,
  useGetFacilitiesQuery,
} from '../../api/logisticsApi';
import type {
  OutboundShipment,
  OutboundShipmentCreationForm,
  OutboundShipmentUpdateForm,
  OutboundShipmentItemForm,
} from '../../types';

interface OutboundShipmentFormProps {
  shipment?: OutboundShipment;
  orderId: string;
  onSubmit: (
    data: OutboundShipmentCreationForm | OutboundShipmentUpdateForm
  ) => Promise<void>;
  onCancel: () => void;
}

interface InventoryDetailOption {
  id: string;
  productName: string;
  productSkuCode?: string;
  lotId: string;
  manufacturingDate?: string;
  expirationDate?: string;
  unit: string;
  facilityId: string;
  notYetOutboundQuantity: number;
}

export const OutboundShipmentForm: React.FC<OutboundShipmentFormProps> = ({
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
    name: string;
    facilityId: string;
  }>({
    name: shipment?.name || '',
    facilityId: shipment?.facilityId || '',
  });

  const [items, setItems] = useState<OutboundShipmentItemForm[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Search states
  const [itemSearch, setItemSearch] = useState('');
  const [showItemDropdown, setShowItemDropdown] = useState(false);

  const inventoryDetailOptions = useMemo<InventoryDetailOption[]>(() => {
    if (!order?.items) {
      return [];
    }

    return order.items.flatMap((orderItem) => {
      return (orderItem.allocatedInventoryItems || []).map((detail) => ({
        id: detail.id,
        productName: orderItem.product?.name || detail.productId,
        productSkuCode: orderItem.product?.skuCode,
        lotId: detail.lotId,
        manufacturingDate:
          detail.manufacturingDate || detail.inventoryItem?.manufacturingDate,
        expirationDate:
          detail.expirationDate || detail.inventoryItem?.expirationDate,
        unit: detail.unit || orderItem.unit,
        facilityId: detail.facilityId,
        notYetOutboundQuantity: detail.notYetOutboundQuantity ?? 0,
      }));
    });
  }, [order]);

  const detailById = useMemo(() => {
    const detailMap = new Map<string, InventoryDetailOption>();
    inventoryDetailOptions.forEach((detail) => {
      detailMap.set(detail.id, detail);
    });

    return detailMap;
  }, [inventoryDetailOptions]);

  const availableItems = useMemo(() => {
    if (!formData.facilityId) {
      return [];
    }

    return inventoryDetailOptions.filter(
      (detail) =>
        detail.notYetOutboundQuantity > 0 &&
        detail.facilityId === formData.facilityId
    );
  }, [inventoryDetailOptions, formData.facilityId]);

  const filteredAvailableItems = useMemo(() => {
    const keyword = itemSearch.trim().toLowerCase();

    return availableItems.filter((detail) => {
      const isAdded = items.some(
        (item) => item.inventoryItemDetailId === detail.id
      );

      if (isAdded) {
        return false;
      }

      if (!keyword) {
        return true;
      }

      return detail.productName.toLowerCase().includes(keyword);
    });
  }, [availableItems, itemSearch, items]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.shipmentName = 'Vui lòng nhập tên phiếu xuất';
    }

    if (!isEditMode && !formData.facilityId) {
      newErrors.facilityId = 'Vui lòng chọn kho xuất';
    }

    if (!isEditMode && items.length === 0) {
      newErrors.items = 'Vui lòng thêm ít nhất một mặt hàng vào phiếu xuất';
    }

    if (!isEditMode) {
      items.forEach((item, index) => {
        if (!item.inventoryItemDetailId) {
          newErrors[`item_${index}_inventoryItemDetailId`] =
            'Chưa chọn mặt hàng';
        }
        if (item.quantity <= 0) {
          newErrors[`item_${index}_quantity`] = 'Số lượng phải > 0';
        }

        const detail = detailById.get(item.inventoryItemDetailId);

        if (!detail) {
          newErrors[`item_${index}_inventoryItemDetailId`] =
            'Mặt hàng không tồn tại trong đơn hàng';
          return;
        }

        if (detail.facilityId !== formData.facilityId) {
          newErrors[`item_${index}_inventoryItemDetailId`] =
            'Mặt hàng không thuộc kho đã chọn';
        }

        if (item.quantity > detail.notYetOutboundQuantity) {
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
        const updateData: OutboundShipmentUpdateForm = {
          name: formData.name.trim(),
        };
        await onSubmit(updateData);
      } else {
        const createData: OutboundShipmentCreationForm = {
          orderId: orderId,
          facilityId: formData.facilityId,
          name: formData.name.trim(),
          items: items,
        };
        await onSubmit(createData);
      }
      toast.success(
        isEditMode
          ? 'Cập nhật phiếu xuất thành công'
          : 'Tạo phiếu xuất thành công'
      );
    } catch (error: any) {
      console.error('Form submission error:', error);
      toast.error(
        error?.data?.message || 'Không thể xử lý phiếu xuất. Vui lòng thử lại.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const addItem = (inventoryItemDetailId: string) => {
    if (!formData.facilityId) {
      toast.warning('Vui lòng chọn kho xuất trước');
      return;
    }

    const selectedDetail = detailById.get(inventoryItemDetailId);

    if (!selectedDetail) {
      toast.warning('Mặt hàng không khả dụng');
      return;
    }

    if (selectedDetail.facilityId !== formData.facilityId) {
      toast.warning('Mặt hàng không thuộc kho đã chọn');
      return;
    }

    if (
      !selectedDetail.notYetOutboundQuantity ||
      selectedDetail.notYetOutboundQuantity <= 0
    ) {
      toast.warning('Sản phẩm không khả dụng');
      return;
    }

    if (
      items.some((item) => item.inventoryItemDetailId === inventoryItemDetailId)
    ) {
      toast.warning('Mặt hàng đã được thêm');
      return;
    }

    setItems((prevItems) => [
      ...prevItems,
      {
        inventoryItemDetailId: inventoryItemDetailId,
        quantity: 1,
      },
    ]);
    setItemSearch('');
    setShowItemDropdown(false);
  };

  const removeItem = (index: number) => {
    setItems((prevItems) => prevItems.filter((_, i) => i !== index));
  };

  const updateItem = (
    index: number,
    updates: Partial<OutboundShipmentItemForm>
  ) => {
    setItems((prevItems) => {
      const newItems = [...prevItems];
      newItems[index] = { ...newItems[index], ...updates };
      return newItems;
    });
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
            Không thể {isEditMode ? 'chỉnh sửa' : 'tạo'} phiếu xuất mà không có
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
            Chỉ có thể {isEditMode ? 'chỉnh sửa' : 'tạo'} phiếu xuất cho các đơn
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

  return (
    <form onSubmit={handleSubmit} className='space-y-6'>
      {/* Header */}
      <div className='flex items-center gap-4'>
        <Button type='button' variant='ghost' size='icon' onClick={onCancel}>
          <ArrowLeft className='h-5 w-5' />
        </Button>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            {isEditMode ? 'Chỉnh sửa phiếu xuất' : 'Tạo phiếu xuất mới'}
          </h1>
          <p className='text-muted-foreground'>
            {isEditMode
              ? 'Cập nhật thông tin phiếu xuất'
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
                Thông tin phiếu xuất
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='space-y-2'>
                <Label htmlFor='shipmentName'>Tên phiếu</Label>
                <Input
                  id='shipmentName'
                  placeholder='Nhập tên phiếu xuất...'
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
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
                <Label htmlFor='facilityId'>
                  Kho xuất{' '}
                  {!isEditMode && <span className='text-destructive'>*</span>}
                </Label>
                <select
                  id='facilityId'
                  value={formData.facilityId}
                  disabled={isEditMode}
                  onChange={(e) => {
                    const nextFacilityId = e.target.value;

                    setFormData((prevFormData) => ({
                      ...prevFormData,
                      facilityId: nextFacilityId,
                    }));

                    if (!isEditMode && nextFacilityId !== formData.facilityId) {
                      setItems([]);
                      setItemSearch('');
                      setShowItemDropdown(false);
                    }
                  }}
                  className={cn(
                    'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
                    errors.facilityId && 'border-destructive'
                  )}
                >
                  <option value=''>Chọn kho xuất</option>
                  {facilities.map((facility) => (
                    <option key={facility.id} value={facility.id}>
                      {facility.name}
                    </option>
                  ))}
                </select>
                {errors.facilityId && (
                  <p className='text-xs text-destructive mt-1'>
                    {errors.facilityId}
                  </p>
                )}
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
                    Danh sách mặt hàng
                  </CardTitle>
                  <Badge variant='secondary'>{items.length} mặt hàng</Badge>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                {/* Add Item Search */}
                <div className='relative'>
                  <div className='relative'>
                    <PlusIcon className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      placeholder={
                        formData.facilityId
                          ? 'Thêm mặt hàng tồn kho...'
                          : 'Vui lòng chọn kho xuất để thêm mặt hàng'
                      }
                      value={itemSearch}
                      disabled={!formData.facilityId}
                      onChange={(e) => {
                        setItemSearch(e.target.value);
                        if (formData.facilityId) {
                          setShowItemDropdown(true);
                        }
                      }}
                      onFocus={() => {
                        if (formData.facilityId) {
                          setShowItemDropdown(true);
                        }
                      }}
                      className='pl-10'
                    />
                  </div>

                  {showItemDropdown && formData.facilityId && (
                    <div className='absolute z-10 w-full mt-1 bg-popover border rounded-lg shadow-lg max-h-60 overflow-auto'>
                      {filteredAvailableItems.length === 0 ? (
                        <p className='px-4 py-3 text-sm text-muted-foreground'>
                          Không còn mặt hàng nào để xuất kho
                        </p>
                      ) : (
                        filteredAvailableItems.map((item) => (
                          <button
                            key={item.id}
                            type='button'
                            onClick={() => addItem(item.id)}
                            className='w-full px-4 py-2 text-left hover:bg-muted transition-colors flex items-center justify-between'
                          >
                            <div>
                              <p className='font-medium'>{item.productName}</p>
                              <p className='text-sm text-muted-foreground'>
                                SKU: {item.productSkuCode || 'N/A'} • Lô{' '}
                                {item.lotId} • NSX: {item.manufacturingDate} •
                                HSD: {item.expirationDate} • Còn lại:{' '}
                                {item.notYetOutboundQuantity} {item.unit}
                              </p>
                            </div>
                            <Plus className='h-4 w-4' />
                          </button>
                        ))
                      )}
                    </div>
                  )}
                </div>

                {errors.items && (
                  <p className='text-sm text-destructive'>{errors.items}</p>
                )}

                {items.length === 0 ? (
                  <div className='py-8 text-center text-muted-foreground'>
                    <Package className='h-12 w-12 mx-auto mb-3 opacity-50' />
                    <p>Chưa thêm mặt hàng nào</p>
                  </div>
                ) : (
                  items.map((item, index) => {
                    const selectedDetail = detailById.get(
                      item.inventoryItemDetailId
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
                                {selectedDetail?.productName ||
                                  item.inventoryItemDetailId}
                              </h4>
                              <p className='text-sm text-muted-foreground'>
                                SKU: {selectedDetail?.productSkuCode || 'N/A'} •
                                Lô {selectedDetail?.lotId || '-'} • NSX:{' '}
                                {selectedDetail?.manufacturingDate || '-'} •
                                HSD: {selectedDetail?.expirationDate || '-'} •
                                Còn lại:{' '}
                                {selectedDetail?.notYetOutboundQuantity || 0}{' '}
                                {selectedDetail?.unit}
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

                          <div className='space-y-2'>
                            <Label htmlFor={`quantity_${index}`}>
                              Số lượng{' '}
                              <span className='text-destructive'>*</span>
                            </Label>
                            <Input
                              id={`quantity_${index}`}
                              type='number'
                              min='1'
                              max={selectedDetail?.notYetOutboundQuantity || 1}
                              value={item.quantity}
                              onChange={(e) =>
                                updateItem(index, {
                                  quantity: parseInt(e.target.value, 10) || 0,
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
                            {selectedDetail?.notYetOutboundQuantity !==
                              undefined && (
                              <Badge
                                variant='secondary'
                                className={
                                  item.quantity >
                                  selectedDetail.notYetOutboundQuantity
                                    ? 'mt-2 text-red-700 dark:text-red-400'
                                    : 'mt-2 text-emerald-700 dark:text-emerald-400'
                                }
                              >
                                Số lượng còn lại:{' '}
                                {selectedDetail.notYetOutboundQuantity}
                              </Badge>
                            )}
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
                      <span className='text-muted-foreground'>Kho xuất</span>
                      <span className='font-semibold truncate max-w-[180px] text-right'>
                        {facilities.find(
                          (facility) => facility.id === formData.facilityId
                        )?.name || 'Chưa chọn'}
                      </span>
                    </div>
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
                        {isEditMode
                          ? 'Cập nhật phiếu xuất kho'
                          : 'Tạo phiếu xuất kho'}
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
                      Mặt hàng còn tồn kho
                    </Label>
                    <div className='space-y-2'>
                      {!formData.facilityId ? (
                        <p className='text-sm text-muted-foreground'>
                          Chọn kho xem danh sách mặt hàng còn tồn.
                        </p>
                      ) : availableItems.length === 0 ? (
                        <p className='text-sm text-muted-foreground'>
                          Không có mặt hàng nào còn tồn trong đơn hàng.
                        </p>
                      ) : (
                        availableItems.map((availableItem) => (
                          <div
                            key={availableItem.id}
                            className='flex items-center justify-between p-2 rounded-md bg-muted/50'
                          >
                            <div className='flex-1 min-w-0'>
                              <p className='text-xs font-medium truncate'>
                                {availableItem.productName}
                              </p>
                            </div>
                            <Badge variant='secondary' className='ml-2'>
                              {availableItem.notYetOutboundQuantity}
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

export default OutboundShipmentForm;
