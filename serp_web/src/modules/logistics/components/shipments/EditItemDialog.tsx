/*
Author: QuanTuanHuy
Description: Part of Serp Project - Edit Item Dialog Component
*/

'use client';

import React, { useState, useEffect } from 'react';
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
import { useUpdateItemInShipmentMutation } from '../../services';
import { useFacilities } from '../../hooks';
import { useNotification } from '@/shared/hooks';
import type {
  InventoryItemDetail,
  UpdateInventoryItemDetailRequest,
} from '../../types';

interface EditItemDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shipmentId: string;
  item: InventoryItemDetail;
}

export const EditItemDialog: React.FC<EditItemDialogProps> = ({
  open,
  onOpenChange,
  shipmentId,
  item,
}) => {
  const notification = useNotification();
  const { facilities } = useFacilities();
  const [updateItem, { isLoading }] = useUpdateItemInShipmentMutation();

  const [formData, setFormData] = useState<UpdateInventoryItemDetailRequest>({
    quantity: 0,
    lotId: '',
    expirationDate: '',
    manufacturingDate: '',
    facilityId: '',
  });

  useEffect(() => {
    if (open && item) {
      setFormData({
        quantity: item.quantity,
        lotId: item.lotId || '',
        expirationDate: item.expirationDate || '',
        manufacturingDate: item.manufacturingDate || '',
        facilityId: item.facilityId || '',
      });
    }
  }, [open, item]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

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
      await updateItem({
        shipmentId,
        itemId: item.id,
        data: formData,
      }).unwrap();
      notification.success('Thành công', {
        description: 'Cập nhật sản phẩm thành công',
      });
      onOpenChange(false);
    } catch (error: any) {
      notification.error('Lỗi', {
        description:
          error?.data?.message || 'Có lỗi xảy ra khi cập nhật sản phẩm',
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Sửa thông tin sản phẩm</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='grid grid-cols-2 gap-4'>
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
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Lưu thay đổi
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
