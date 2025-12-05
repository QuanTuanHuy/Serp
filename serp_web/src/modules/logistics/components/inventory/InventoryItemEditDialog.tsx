/*
Author: QuanTuanHuy
Description: Part of Serp Project - Inventory Item Edit Dialog
*/

'use client';

import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Input,
  Label,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type { InventoryItem, UpdateInventoryItemRequest } from '../../types';

const updateSchema = z.object({
  quantity: z.number().min(0, 'Số lượng phải >= 0'),
  lotId: z.string().optional(),
  expirationDate: z.string().optional(),
  manufacturingDate: z.string().optional(),
  receivedDate: z.string().optional(),
  statusId: z.string().optional(),
});

type UpdateFormData = z.infer<typeof updateSchema>;

interface InventoryItemEditDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  inventoryItem: InventoryItem | null;
  onSubmit: (itemId: string, data: UpdateInventoryItemRequest) => Promise<void>;
  isLoading?: boolean;
}

export const InventoryItemEditDialog: React.FC<
  InventoryItemEditDialogProps
> = ({ open, onOpenChange, inventoryItem, onSubmit, isLoading = false }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<UpdateFormData>({
    resolver: zodResolver(updateSchema),
  });

  useEffect(() => {
    if (inventoryItem) {
      reset({
        quantity: inventoryItem.quantity,
        lotId: inventoryItem.lotId || '',
        expirationDate: inventoryItem.expirationDate || '',
        manufacturingDate: inventoryItem.manufacturingDate || '',
        receivedDate: inventoryItem.receivedDate || '',
        statusId: inventoryItem.statusId || '',
      });
    }
  }, [inventoryItem, reset]);

  const handleFormSubmit = async (data: UpdateFormData) => {
    if (!inventoryItem) return;

    await onSubmit(inventoryItem.id, {
      quantity: data.quantity,
      lotId: data.lotId,
      expirationDate: data.expirationDate,
      manufacturingDate: data.manufacturingDate,
      receivedDate: data.receivedDate,
      statusId: data.statusId,
    });
  };

  if (!inventoryItem) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Chỉnh sửa tồn kho</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className='space-y-4'>
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='quantity'>
                Số lượng <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='quantity'
                type='number'
                {...register('quantity', { valueAsNumber: true })}
                disabled={isLoading}
              />
              {errors.quantity && (
                <p className='text-sm text-destructive'>
                  {errors.quantity.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='lotId'>Lô hàng</Label>
              <Input
                id='lotId'
                {...register('lotId')}
                disabled={isLoading}
                placeholder='Nhập mã lô hàng'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='receivedDate'>Ngày nhận</Label>
              <Input
                id='receivedDate'
                type='date'
                {...register('receivedDate')}
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='manufacturingDate'>Ngày sản xuất</Label>
              <Input
                id='manufacturingDate'
                type='date'
                {...register('manufacturingDate')}
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='expirationDate'>Ngày hết hạn</Label>
              <Input
                id='expirationDate'
                type='date'
                {...register('expirationDate')}
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='statusId'>Trạng thái</Label>
              <Input
                id='statusId'
                {...register('statusId')}
                disabled={isLoading}
                placeholder='AVAILABLE, RESERVED, etc.'
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
