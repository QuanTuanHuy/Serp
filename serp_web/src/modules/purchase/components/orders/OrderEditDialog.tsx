/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Edit Dialog Component
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
  Input,
  Label,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from '@/shared/components/ui';
import { Textarea } from '@/shared/components/ui/textarea';
import { Loader2 } from 'lucide-react';
import { useUpdateOrderMutation } from '../../services';
import { useNotification } from '@/shared/hooks/use-notification';
import type { OrderDetail, UpdateOrderRequest } from '../../types';

const editOrderSchema = z.object({
  deliveryBeforeDate: z.string().min(1, 'Delivery before date is required'),
  deliveryAfterDate: z.string().min(1, 'Delivery after date is required'),
  orderName: z.string().min(1, 'Order name is required'),
  note: z.string().optional(),
  priority: z.number().min(0, 'Priority must be positive'),
  saleChannelId: z.string().optional(),
});

type EditOrderFormData = z.infer<typeof editOrderSchema>;

interface OrderEditDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  order: OrderDetail;
  onSuccess?: () => void;
}

export const OrderEditDialog: React.FC<OrderEditDialogProps> = ({
  open,
  onOpenChange,
  order,
  onSuccess,
}) => {
  const { success, error: showError } = useNotification();
  const [updateOrder, { isLoading }] = useUpdateOrderMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
  } = useForm<EditOrderFormData>({
    resolver: zodResolver(editOrderSchema),
    defaultValues: {
      deliveryBeforeDate: order.deliveryBeforeDate?.split('T')[0] || '',
      deliveryAfterDate: order.deliveryAfterDate?.split('T')[0] || '',
      orderName: order.orderName,
      note: order.note || '',
      priority: order.priority,
      saleChannelId: order.saleChannelId || '',
    },
  });

  const onSubmit = async (data: EditOrderFormData) => {
    try {
      const result = await updateOrder({
        orderId: order.id,
        data: data as UpdateOrderRequest,
      }).unwrap();

      if (result.code === 200 && result.status.toLowerCase() === 'success') {
        success('Cập nhật đơn hàng thành công', {
          description: result.message,
        });
        onSuccess?.();
      } else {
        showError('Cập nhật thất bại', {
          description: result.message,
        });
      }
    } catch (err: any) {
      showError('Lỗi khi cập nhật', {
        description: err?.data?.message || 'Đã xảy ra lỗi không mong muốn',
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Chỉnh sửa đơn hàng</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className='space-y-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {/* Order Name */}
            <div className='md:col-span-2 space-y-2'>
              <Label htmlFor='orderName'>
                Tên đơn hàng <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='orderName'
                {...register('orderName')}
                disabled={isLoading}
              />
              {errors.orderName && (
                <p className='text-sm text-destructive'>
                  {errors.orderName.message}
                </p>
              )}
            </div>

            {/* Delivery After Date */}
            <div className='space-y-2'>
              <Label htmlFor='deliveryAfterDate'>
                Giao sau ngày <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='deliveryAfterDate'
                type='date'
                {...register('deliveryAfterDate')}
                disabled={isLoading}
              />
              {errors.deliveryAfterDate && (
                <p className='text-sm text-destructive'>
                  {errors.deliveryAfterDate.message}
                </p>
              )}
            </div>

            {/* Delivery Before Date */}
            <div className='space-y-2'>
              <Label htmlFor='deliveryBeforeDate'>
                Giao trước ngày <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='deliveryBeforeDate'
                type='date'
                {...register('deliveryBeforeDate')}
                disabled={isLoading}
              />
              {errors.deliveryBeforeDate && (
                <p className='text-sm text-destructive'>
                  {errors.deliveryBeforeDate.message}
                </p>
              )}
            </div>

            {/* Priority */}
            <div className='space-y-2'>
              <Label htmlFor='priority'>
                Độ ưu tiên <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='priority'
                type='number'
                {...register('priority', { valueAsNumber: true })}
                min='0'
                disabled={isLoading}
              />
              {errors.priority && (
                <p className='text-sm text-destructive'>
                  {errors.priority.message}
                </p>
              )}
            </div>

            {/* Sales Channel */}
            <div className='space-y-2'>
              <Label htmlFor='saleChannelId'>Kênh bán</Label>
              <Select
                value={watch('saleChannelId')}
                onValueChange={(value) => setValue('saleChannelId', value)}
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Chọn kênh bán' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='PARTNER'>Partner</SelectItem>
                  <SelectItem value='ONLINE'>Online</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Note */}
            <div className='md:col-span-2 space-y-2'>
              <Label htmlFor='note'>Ghi chú</Label>
              <Textarea
                id='note'
                {...register('note')}
                placeholder='Nhập ghi chú'
                rows={3}
                disabled={isLoading}
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
              Lưu
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
