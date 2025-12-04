/*
Author: QuanTuanHuy
Description: Part of Serp Project - Edit Shipment Dialog Component
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
import { Button, Input, Label } from '@/shared/components/ui';
import { Textarea } from '@/shared/components/ui/textarea';
import { Loader2 } from 'lucide-react';
import { useUpdateShipmentMutation } from '../../services';
import { useNotification } from '@/shared/hooks';
import type { ShipmentDetail, UpdateShipmentRequest } from '../../types';

interface EditShipmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shipment: ShipmentDetail;
}

export const EditShipmentDialog: React.FC<EditShipmentDialogProps> = ({
  open,
  onOpenChange,
  shipment,
}) => {
  const notification = useNotification();
  const [updateShipment, { isLoading }] = useUpdateShipmentMutation();

  const [formData, setFormData] = useState<UpdateShipmentRequest>({
    shipmentName: '',
    note: '',
    expectedDeliveryDate: '',
  });

  useEffect(() => {
    if (open && shipment) {
      setFormData({
        shipmentName: shipment.shipmentName || '',
        note: shipment.note || '',
        expectedDeliveryDate: shipment.expectedDeliveryDate || '',
      });
    }
  }, [open, shipment]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.expectedDeliveryDate) {
      notification.error('Validation Error', {
        description: 'Vui lòng chọn ngày giao dự kiến',
      });
      return;
    }

    try {
      await updateShipment({
        shipmentId: shipment.id,
        data: formData,
      }).unwrap();
      notification.success('Thành công', {
        description: 'Cập nhật phiếu thành công',
      });
      onOpenChange(false);
    } catch (error: any) {
      notification.error('Lỗi', {
        description: error?.data?.message || 'Có lỗi xảy ra khi cập nhật phiếu',
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Sửa thông tin phiếu</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='shipmentName'>Tên phiếu</Label>
            <Input
              id='shipmentName'
              value={formData.shipmentName}
              onChange={(e) =>
                setFormData({ ...formData, shipmentName: e.target.value })
              }
              placeholder='Nhập tên phiếu'
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

          <div className='space-y-2'>
            <Label htmlFor='note'>Ghi chú</Label>
            <Textarea
              id='note'
              value={formData.note}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                setFormData({ ...formData, note: e.target.value })
              }
              placeholder='Nhập ghi chú...'
              rows={4}
            />
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
