/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment creation dialog component
*/

'use client';

import React, { useState, useMemo } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
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
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2, Plus, Trash2 } from 'lucide-react';
import { useGetFacilitiesQuery } from '../../services/purchaseApi';
import type { OrderDetail, CreateShipmentRequest } from '../../types';

const shipmentItemSchema = z.object({
  productId: z.string().min(1, 'Vui lòng chọn sản phẩm'),
  quantity: z.number().min(1, 'Số lượng phải lớn hơn 0'),
  orderItemId: z.string().optional(),
  lotId: z.string().optional(),
  note: z.string().optional(),
  manufacturingDate: z.string().optional(),
  expirationDate: z.string().optional(),
});

const shipmentFormSchema = z.object({
  shipmentName: z.string().optional(),
  expectedDeliveryDate: z
    .string()
    .min(1, 'Vui lòng chọn ngày giao hàng dự kiến'),
  facilityId: z.string().min(1, 'Vui lòng chọn kho'),
  note: z.string().optional(),
  items: z.array(shipmentItemSchema).min(1, 'Phải có ít nhất 1 sản phẩm'),
});

type ShipmentFormData = z.infer<typeof shipmentFormSchema>;

interface ShipmentFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  order: OrderDetail;
  existingShipments: any[];
  onSubmit: (data: CreateShipmentRequest) => Promise<boolean>;
  isLoading?: boolean;
}

export const ShipmentFormDialog: React.FC<ShipmentFormDialogProps> = ({
  open,
  onOpenChange,
  order,
  existingShipments,
  onSubmit,
  isLoading = false,
}) => {
  // Fetch facilities for dropdown
  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    page: 1,
    size: 100,
  });

  const facilities = useMemo(
    () => facilitiesResponse?.data?.items || [],
    [facilitiesResponse]
  );

  // Calculate remaining quantities for each product
  const availableProducts = useMemo(() => {
    const productMap = new Map();

    // Add order items with their quantities
    order.orderItems?.forEach((item) => {
      productMap.set(item.productId, {
        ...item,
        remainingQuantity: item.quantity,
        orderItemId: item.id,
      });
    });

    // Subtract quantities already in shipments
    existingShipments.forEach((shipment) => {
      shipment.items?.forEach((item: any) => {
        const product = productMap.get(item.productId);
        if (product) {
          product.remainingQuantity -= item.quantity;
        }
      });
    });

    return Array.from(productMap.values()).filter(
      (p) => p.remainingQuantity > 0
    );
  }, [order, existingShipments]);

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    setValue,
    reset,
    control,
  } = useForm<ShipmentFormData>({
    resolver: zodResolver(shipmentFormSchema),
    defaultValues: {
      shipmentName: '',
      expectedDeliveryDate: '',
      facilityId: '',
      note: '',
      items: [
        {
          productId: '',
          quantity: 1,
          orderItemId: '',
          lotId: '',
          note: '',
          manufacturingDate: '',
          expirationDate: '',
        },
      ],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const handleFormSubmit = async (data: ShipmentFormData) => {
    const request: CreateShipmentRequest = {
      fromSupplierId: order.fromSupplierId || '',
      orderId: order.id,
      shipmentName: data.shipmentName || `Phiếu nhập ${order.orderName}`,
      note: data.note,
      expectedDeliveryDate: data.expectedDeliveryDate,
      facilityId: data.facilityId,
      items: data.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        orderItemId: item.orderItemId,
        note: item.note,
        lotId: item.lotId,
        expirationDate: item.expirationDate,
        manufacturingDate: item.manufacturingDate,
      })),
    };

    const success = await onSubmit(request);
    if (success) {
      reset();
      onOpenChange(false);
    }
  };

  // Get product info by ID
  const getProductById = (productId: string) => {
    return order.orderItems?.find((item) => item.productId === productId);
  };

  // Get max quantity for selected product
  const getMaxQuantity = (productId: string) => {
    const product = availableProducts.find((p) => p.productId === productId);
    return product?.remainingQuantity || 0;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-4xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Tạo phiếu nhập mới</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(handleFormSubmit)} className='space-y-6'>
          {/* Basic Information */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='shipmentName'>Tên phiếu nhập</Label>
              <Input
                id='shipmentName'
                placeholder='Để trống sẽ tự động tạo tên'
                {...register('shipmentName')}
                disabled={isLoading}
              />
              {errors.shipmentName && (
                <p className='text-sm text-destructive'>
                  {errors.shipmentName.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='expectedDeliveryDate'>
                Ngày giao dự kiến <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='expectedDeliveryDate'
                type='date'
                {...register('expectedDeliveryDate')}
                disabled={isLoading}
              />
              {errors.expectedDeliveryDate && (
                <p className='text-sm text-destructive'>
                  {errors.expectedDeliveryDate.message}
                </p>
              )}
            </div>
          </div>

          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='facilityId'>
                Kho <span className='text-red-500'>*</span>
              </Label>
              <Select
                value={watch('facilityId')}
                onValueChange={(value) => setValue('facilityId', value)}
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Chọn kho' />
                </SelectTrigger>
                <SelectContent>
                  {facilities.map((facility) => (
                    <SelectItem key={facility.id} value={facility.id}>
                      {facility.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.facilityId && (
                <p className='text-sm text-destructive'>
                  {errors.facilityId.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='note'>Ghi chú</Label>
              <Input
                id='note'
                placeholder='Ghi chú về phiếu nhập'
                {...register('note')}
                disabled={isLoading}
              />
            </div>
          </div>

          {/* Product Items */}
          <div className='space-y-4'>
            <div className='flex items-center justify-between'>
              <h3 className='text-lg font-medium'>Danh sách sản phẩm</h3>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={() =>
                  append({
                    productId: '',
                    quantity: 1,
                    orderItemId: '',
                    lotId: '',
                    note: '',
                    manufacturingDate: '',
                    expirationDate: '',
                  })
                }
                disabled={isLoading}
              >
                <Plus className='h-4 w-4 mr-2' />
                Thêm sản phẩm
              </Button>
            </div>

            {fields.map((field, index) => (
              <div
                key={field.id}
                className='p-4 border rounded-lg space-y-4 bg-muted/30'
              >
                <div className='flex items-center justify-between'>
                  <h4 className='font-medium'>Sản phẩm {index + 1}</h4>
                  {fields.length > 1 && (
                    <Button
                      type='button'
                      variant='ghost'
                      size='sm'
                      onClick={() => remove(index)}
                      disabled={isLoading}
                    >
                      <Trash2 className='h-4 w-4' />
                    </Button>
                  )}
                </div>

                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-2'>
                    <Label>
                      Sản phẩm <span className='text-red-500'>*</span>
                    </Label>
                    <Select
                      value={watch(`items.${index}.productId`)}
                      onValueChange={(value) => {
                        setValue(`items.${index}.productId`, value);
                        const product = getProductById(value);
                        if (product) {
                          setValue(`items.${index}.orderItemId`, product.id);
                        }
                      }}
                      disabled={isLoading}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder='Chọn sản phẩm' />
                      </SelectTrigger>
                      <SelectContent>
                        {availableProducts.map((product) => (
                          <SelectItem
                            key={product.productId}
                            value={product.productId}
                          >
                            {product.productId} (Còn:{' '}
                            {product.remainingQuantity})
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {errors.items?.[index]?.productId && (
                      <p className='text-sm text-destructive'>
                        {errors.items[index]?.productId?.message}
                      </p>
                    )}
                  </div>

                  <div className='space-y-2'>
                    <Label>
                      Số lượng <span className='text-red-500'>*</span>
                    </Label>
                    <Input
                      type='number'
                      min={1}
                      max={getMaxQuantity(watch(`items.${index}.productId`))}
                      {...register(`items.${index}.quantity`, {
                        valueAsNumber: true,
                      })}
                      disabled={isLoading}
                    />
                    {getMaxQuantity(watch(`items.${index}.productId`)) > 0 && (
                      <p className='text-xs text-muted-foreground'>
                        Tối đa:{' '}
                        {getMaxQuantity(watch(`items.${index}.productId`))}
                      </p>
                    )}
                    {errors.items?.[index]?.quantity && (
                      <p className='text-sm text-destructive'>
                        {errors.items[index]?.quantity?.message}
                      </p>
                    )}
                  </div>
                </div>

                <div className='grid grid-cols-3 gap-4'>
                  <div className='space-y-2'>
                    <Label>Lô hàng</Label>
                    <Input
                      placeholder='Mã lô hàng'
                      {...register(`items.${index}.lotId`)}
                      disabled={isLoading}
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label>Ngày sản xuất</Label>
                    <Input
                      type='date'
                      {...register(`items.${index}.manufacturingDate`)}
                      disabled={isLoading}
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label>Ngày hết hạn</Label>
                    <Input
                      type='date'
                      {...register(`items.${index}.expirationDate`)}
                      disabled={isLoading}
                    />
                  </div>
                </div>
              </div>
            ))}
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
              Tạo phiếu nhập
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
