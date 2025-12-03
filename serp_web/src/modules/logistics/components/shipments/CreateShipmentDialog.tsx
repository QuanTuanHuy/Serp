/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create Shipment Dialog Component
*/

'use client';

import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui';
import {
  Button,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Textarea } from '@/shared/components/ui/textarea';
import { Plus, Trash2, Loader2 } from 'lucide-react';
import { useCreateShipmentMutation } from '../../services';
import { useProducts, useFacilities } from '../../hooks';
import { useNotification } from '@/shared/hooks';
import type {
  CreateShipmentRequest,
  InventoryItemDetailForm,
  OrderDetail,
} from '../../types';

interface CreateShipmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  order: OrderDetail;
}

export const CreateShipmentDialog: React.FC<CreateShipmentDialogProps> = ({
  open,
  onOpenChange,
  order,
}) => {
  const notification = useNotification();
  const { products } = useProducts();
  const { facilities } = useFacilities();

  const [createShipment, { isLoading }] = useCreateShipmentMutation();

  // Form state
  const [formData, setFormData] = useState({
    shipmentName: '',
    shipmentTypeId: order.orderTypeId === 'PURCHASE' ? 'INBOUND' : 'OUTBOUND',
    fromSupplierId: order.fromSupplierId || null,
    toCustomerId: order.toCustomerId || null,
    orderId: order.id,
    expectedDeliveryDate: '',
    note: '',
  });

  const [inventoryItems, setInventoryItems] = useState<
    InventoryItemDetailForm[]
  >([]);

  const handleAddItem = () => {
    setInventoryItems([
      ...inventoryItems,
      {
        productId: '',
        quantity: 0,
        orderItemId: '',
        lotId: '',
        expirationDate: '',
        manufacturingDate: '',
        facilityId: '',
      },
    ]);
  };

  const handleRemoveItem = (index: number) => {
    setInventoryItems(inventoryItems.filter((_, i) => i !== index));
  };

  const handleItemChange = (
    index: number,
    field: keyof InventoryItemDetailForm,
    value: any
  ) => {
    const newItems = [...inventoryItems];
    newItems[index] = { ...newItems[index], [field]: value };

    // Tự động lấy orderItemId khi chọn productId
    if (field === 'productId' && value) {
      const orderItem = order.orderItems?.find(
        (item) => item.productId === value
      );
      if (orderItem) {
        newItems[index].orderItemId = orderItem.id;
      }
    }

    setInventoryItems(newItems);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validation
    if (!formData.expectedDeliveryDate) {
      notification.error('Validation Error', {
        description: 'Vui lòng chọn ngày giao dự kiến',
      });
      return;
    }

    if (inventoryItems.length === 0) {
      notification.error('Validation Error', {
        description: 'Vui lòng thêm ít nhất một sản phẩm',
      });
      return;
    }

    // Validate inventory items
    for (let i = 0; i < inventoryItems.length; i++) {
      const item = inventoryItems[i];
      if (!item.productId) {
        notification.error('Validation Error', {
          description: `Sản phẩm ${i + 1}: Vui lòng chọn sản phẩm`,
        });
        return;
      }
      if (!item.orderItemId) {
        notification.error('Validation Error', {
          description: `Sản phẩm ${i + 1}: Không tìm thấy order item tương ứng`,
        });
        return;
      }
      if (item.quantity <= 0) {
        notification.error('Validation Error', {
          description: `Sản phẩm ${i + 1}: Số lượng phải lớn hơn 0`,
        });
        return;
      }
      if (!item.facilityId) {
        notification.error('Validation Error', {
          description: `Sản phẩm ${i + 1}: Vui lòng chọn kho`,
        });
        return;
      }
    }

    const request: CreateShipmentRequest = {
      shipmentTypeId: formData.shipmentTypeId,
      fromSupplierId: formData.fromSupplierId,
      toCustomerId: formData.toCustomerId,
      orderId: formData.orderId,
      ...(formData.shipmentName.trim() && {
        shipmentName: formData.shipmentName,
      }),
      note: formData.note,
      expectedDeliveryDate: formData.expectedDeliveryDate,
      items: inventoryItems,
    };

    try {
      await createShipment(request).unwrap();
      notification.success('Thành công', {
        description: 'Tạo phiếu nhập thành công',
      });
      onOpenChange(false);
      resetForm();
    } catch (error: any) {
      notification.error('Lỗi', {
        description: error?.data?.message || 'Có lỗi xảy ra khi tạo phiếu nhập',
      });
    }
  };

  const resetForm = () => {
    setFormData({
      shipmentName: '',
      shipmentTypeId: order.orderTypeId === 'PURCHASE' ? 'INBOUND' : 'OUTBOUND',
      fromSupplierId: order.fromSupplierId || null,
      toCustomerId: order.toCustomerId || null,
      orderId: order.id,
      expectedDeliveryDate: '',
      note: '',
    });
    setInventoryItems([]);
  };

  const handleClose = () => {
    if (!isLoading) {
      onOpenChange(false);
      resetForm();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='!max-w-5xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            Tạo phiếu nhập cho đơn hàng #{order.id.substring(0, 8)}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-6'>
          {/* Basic Information */}
          <div className='space-y-4'>
            <h3 className='text-sm font-semibold'>Thông tin cơ bản</h3>
            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='shipmentName'>
                  Tên phiếu{' '}
                  {formData.shipmentTypeId === 'INBOUND' ? 'nhập' : 'xuất'}
                </Label>
                <Input
                  id='shipmentName'
                  value={formData.shipmentName}
                  onChange={(e) =>
                    setFormData({ ...formData, shipmentName: e.target.value })
                  }
                  placeholder={`Nhập tên phiếu ${formData.shipmentTypeId === 'INBOUND' ? 'nhập' : 'xuất'} (tùy chọn)`}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='shipmentTypeId'>Loại phiếu</Label>
                <Input
                  id='shipmentTypeId'
                  value={
                    formData.shipmentTypeId === 'INBOUND'
                      ? 'Nhập hàng'
                      : 'Xuất hàng'
                  }
                  disabled
                  className='bg-muted'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='expectedDeliveryDate'>
                  Ngày giao dự kiến <span className='text-red-500'>*</span>
                </Label>
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
                />
              </div>

              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='note'>Ghi chú</Label>
                <Textarea
                  id='note'
                  value={formData.note}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                    setFormData({ ...formData, note: e.target.value })
                  }
                  placeholder='Nhập ghi chú...'
                  rows={3}
                />
              </div>
            </div>
          </div>

          {/* Inventory Items */}
          <div className='space-y-4'>
            <div className='flex items-center justify-between'>
              <h3 className='text-sm font-semibold'>
                Danh sách sản phẩm ({inventoryItems.length})
              </h3>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={handleAddItem}
              >
                <Plus className='h-4 w-4 mr-1' />
                Thêm sản phẩm
              </Button>
            </div>

            <div className='space-y-3 max-h-96 overflow-y-auto'>
              {inventoryItems.map((item, index) => (
                <div
                  key={index}
                  className='p-4 border rounded-lg space-y-3 bg-muted/30'
                >
                  <div className='flex items-center justify-between'>
                    <span className='text-sm font-medium'>
                      Sản phẩm #{index + 1}
                    </span>
                    <Button
                      type='button'
                      variant='ghost'
                      size='sm'
                      onClick={() => handleRemoveItem(index)}
                      className='h-8 w-8 p-0 text-destructive hover:text-destructive'
                    >
                      <Trash2 className='h-4 w-4' />
                    </Button>
                  </div>

                  <div className='grid grid-cols-1 md:grid-cols-3 gap-3'>
                    <div className='space-y-2'>
                      <Label>
                        Sản phẩm <span className='text-red-500'>*</span>
                      </Label>
                      <Select
                        value={item.productId}
                        onValueChange={(value) =>
                          handleItemChange(index, 'productId', value)
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder='Chọn sản phẩm' />
                        </SelectTrigger>
                        <SelectContent>
                          {order.orderItems?.map((orderItem) => {
                            const product = products?.find(
                              (p) => p.id === orderItem.productId
                            );
                            return product ? (
                              <SelectItem key={product.id} value={product.id}>
                                {product.name}
                              </SelectItem>
                            ) : null;
                          })}
                        </SelectContent>
                      </Select>
                    </div>

                    <div className='space-y-2'>
                      <Label>
                        Kho <span className='text-red-500'>*</span>
                      </Label>
                      <Select
                        value={item.facilityId}
                        onValueChange={(value) =>
                          handleItemChange(index, 'facilityId', value)
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder='Chọn kho' />
                        </SelectTrigger>
                        <SelectContent>
                          {facilities?.map((facility) => (
                            <SelectItem key={facility.id} value={facility.id}>
                              {facility.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div className='space-y-2'>
                      <Label>
                        Số lượng <span className='text-red-500'>*</span>
                      </Label>
                      <Input
                        type='number'
                        min='0'
                        value={item.quantity}
                        onChange={(e) =>
                          handleItemChange(
                            index,
                            'quantity',
                            parseFloat(e.target.value) || 0
                          )
                        }
                      />
                    </div>

                    <div className='space-y-2'>
                      <Label>Lot ID</Label>
                      <Input
                        value={item.lotId}
                        onChange={(e) =>
                          handleItemChange(index, 'lotId', e.target.value)
                        }
                        placeholder='Nhập mã lô'
                      />
                    </div>

                    <div className='space-y-2'>
                      <Label>Ngày sản xuất</Label>
                      <Input
                        type='date'
                        value={item.manufacturingDate}
                        onChange={(e) =>
                          handleItemChange(
                            index,
                            'manufacturingDate',
                            e.target.value
                          )
                        }
                      />
                    </div>

                    <div className='space-y-2'>
                      <Label>Ngày hết hạn</Label>
                      <Input
                        type='date'
                        value={item.expirationDate}
                        onChange={(e) =>
                          handleItemChange(
                            index,
                            'expirationDate',
                            e.target.value
                          )
                        }
                      />
                    </div>
                  </div>
                </div>
              ))}

              {inventoryItems.length === 0 && (
                <div className='text-center py-8 text-muted-foreground'>
                  <p className='text-sm'>
                    Chưa có sản phẩm nào. Nhấn "Thêm sản phẩm" để bắt đầu.
                  </p>
                </div>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={handleClose}
              disabled={isLoading}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Tạo phiếu nhập
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
