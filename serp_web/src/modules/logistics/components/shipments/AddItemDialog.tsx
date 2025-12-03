/*
Author: QuanTuanHuy
Description: Part of Serp Project - Add Item Dialog Component
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
import { Loader2 } from 'lucide-react';
import { useAddItemToShipmentMutation, useGetOrderQuery } from '../../services';
import { useProducts, useFacilities } from '../../hooks';
import { useNotification } from '@/shared/hooks';
import type { InventoryItemDetailForm } from '../../types';

interface AddItemDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shipmentId: string;
  orderId: string;
}

export const AddItemDialog: React.FC<AddItemDialogProps> = ({
  open,
  onOpenChange,
  shipmentId,
  orderId,
}) => {
  const notification = useNotification();
  const { products } = useProducts();
  const { facilities } = useFacilities();
  const { data: order } = useGetOrderQuery(orderId);
  const [addItem, { isLoading }] = useAddItemToShipmentMutation();

  const [formData, setFormData] = useState<InventoryItemDetailForm>({
    productId: '',
    quantity: 0,
    orderItemId: '',
    lotId: '',
    expirationDate: '',
    manufacturingDate: '',
    facilityId: '',
  });

  const handleProductChange = (productId: string) => {
    setFormData({ ...formData, productId });

    // Tự động lấy orderItemId
    const orderItem = order?.orderItems?.find(
      (item) => item.productId === productId
    );
    if (orderItem) {
      setFormData((prev) => ({
        ...prev,
        productId,
        orderItemId: orderItem.id,
      }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.productId) {
      notification.error('Validation Error', {
        description: 'Vui lòng chọn sản phẩm',
      });
      return;
    }

    if (!formData.orderItemId) {
      notification.error('Validation Error', {
        description: 'Không tìm thấy order item tương ứng',
      });
      return;
    }

    if (formData.quantity <= 0) {
      notification.error('Validation Error', {
        description: 'Số lượng phải lớn hơn 0',
      });
      return;
    }

    if (!formData.facilityId) {
      notification.error('Validation Error', {
        description: 'Vui lòng chọn kho',
      });
      return;
    }

    try {
      await addItem({ shipmentId, data: formData }).unwrap();
      notification.success('Thành công', {
        description: 'Thêm sản phẩm thành công',
      });
      onOpenChange(false);
      resetForm();
    } catch (error: any) {
      notification.error('Lỗi', {
        description: error?.data?.message || 'Có lỗi xảy ra khi thêm sản phẩm',
      });
    }
  };

  const resetForm = () => {
    setFormData({
      productId: '',
      quantity: 0,
      orderItemId: '',
      lotId: '',
      expirationDate: '',
      manufacturingDate: '',
      facilityId: '',
    });
  };

  const handleClose = () => {
    if (!isLoading) {
      onOpenChange(false);
      resetForm();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Thêm sản phẩm</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='productId'>
                Sản phẩm <span className='text-red-500'>*</span>
              </Label>
              <Select
                value={formData.productId}
                onValueChange={handleProductChange}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Chọn sản phẩm' />
                </SelectTrigger>
                <SelectContent>
                  {order?.orderItems?.map((orderItem) => {
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
              <Label htmlFor='facilityId'>
                Kho <span className='text-red-500'>*</span>
              </Label>
              <Select
                value={formData.facilityId}
                onValueChange={(value) =>
                  setFormData({ ...formData, facilityId: value })
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
              <Label htmlFor='quantity'>
                Số lượng <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='quantity'
                type='number'
                min='0'
                value={formData.quantity}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    quantity: parseFloat(e.target.value) || 0,
                  })
                }
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='lotId'>Lot ID</Label>
              <Input
                id='lotId'
                value={formData.lotId}
                onChange={(e) =>
                  setFormData({ ...formData, lotId: e.target.value })
                }
                placeholder='Nhập mã lô'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='manufacturingDate'>Ngày sản xuất</Label>
              <Input
                id='manufacturingDate'
                type='date'
                value={formData.manufacturingDate}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    manufacturingDate: e.target.value,
                  })
                }
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='expirationDate'>Ngày hết hạn</Label>
              <Input
                id='expirationDate'
                type='date'
                value={formData.expirationDate}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    expirationDate: e.target.value,
                  })
                }
              />
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
              Thêm sản phẩm
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
