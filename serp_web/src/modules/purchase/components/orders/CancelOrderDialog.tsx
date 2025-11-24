/*
Author: QuanTuanHuy
Description: Part of Serp Project - Cancel Order Dialog Component
*/

'use client';

import React from 'react';
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
  Label,
} from '@/shared/components/ui';
import { Textarea } from '@/shared/components/ui/textarea';
import { Loader2 } from 'lucide-react';

const cancelOrderSchema = z.object({
  note: z.string().min(1, 'Vui lòng nhập lý do hủy'),
});

type CancelOrderFormData = z.infer<typeof cancelOrderSchema>;

interface CancelOrderDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: (note: string) => void;
  isLoading?: boolean;
}

export const CancelOrderDialog: React.FC<CancelOrderDialogProps> = ({
  open,
  onOpenChange,
  onConfirm,
  isLoading = false,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CancelOrderFormData>({
    resolver: zodResolver(cancelOrderSchema),
  });

  const onSubmit = async (data: CancelOrderFormData) => {
    onConfirm(data.note);
    reset();
  };

  const handleClose = () => {
    reset();
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-md'>
        <DialogHeader>
          <DialogTitle>Hủy đơn hàng</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='note'>
              Lý do hủy <span className='text-red-500'>*</span>
            </Label>
            <Textarea
              id='note'
              {...register('note')}
              placeholder='Nhập lý do hủy đơn hàng'
              rows={4}
              disabled={isLoading}
            />
            {errors.note && (
              <p className='text-sm text-destructive'>{errors.note.message}</p>
            )}
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={handleClose}
              disabled={isLoading}
            >
              Hủy bỏ
            </Button>
            <Button type='submit' variant='destructive' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Xác nhận hủy
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
