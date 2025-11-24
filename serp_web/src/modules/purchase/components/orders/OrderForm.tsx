/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Form Component
*/

'use client';

import React, { useState, useEffect } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Label,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Button,
} from '@/shared/components/ui';
import { Textarea } from '@/shared/components/ui/textarea';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Loader2, Plus, Trash2 } from 'lucide-react';
import type { OrderDetail, Product } from '../../types';
import { useGetSuppliersQuery } from '../../services';

const orderItemSchema = z.object({
  productId: z.string().min(1, 'Product is required'),
  orderItemSeqId: z.number(),
  quantity: z.number().min(1, 'Quantity must be at least 1'),
  tax: z.number().min(0, 'Tax must be positive'),
  discount: z.number().min(0, 'Discount must be positive'),
});

const orderFormSchema = z.object({
  fromSupplierId: z.string().min(1, 'Supplier is required'),
  deliveryBeforeDate: z.string().min(1, 'Delivery before date is required'),
  deliveryAfterDate: z.string().min(1, 'Delivery after date is required'),
  orderName: z.string().min(1, 'Order name is required'),
  note: z.string().optional(),
  priority: z.number().min(0, 'Priority must be positive'),
  saleChannelId: z.string().optional(),
  orderItems: z.array(orderItemSchema).min(1, 'At least one item is required'),
});

type OrderFormData = z.infer<typeof orderFormSchema>;

interface OrderFormProps {
  order?: OrderDetail;
  products?: Product[];
  onSubmit: (data: OrderFormData) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
}

export const OrderForm: React.FC<OrderFormProps> = ({
  order,
  products = [],
  onSubmit,
  onCancel,
  isLoading = false,
}) => {
  // Fetch suppliers for dropdown
  const { data: suppliersData } = useGetSuppliersQuery({
    page: 1,
    size: 1000,
  });
  const suppliers = suppliersData?.data?.items || [];

  const {
    register,
    handleSubmit,
    formState: { errors },
    control,
    watch,
    setValue,
  } = useForm<OrderFormData>({
    resolver: zodResolver(orderFormSchema),
    defaultValues: order
      ? {
          fromSupplierId: order.fromSupplierId || '',
          deliveryBeforeDate: order.deliveryBeforeDate || '',
          deliveryAfterDate: order.deliveryAfterDate || '',
          orderName: order.orderName || '',
          note: order.note || '',
          priority: order.priority || 0,
          salesChannelId: order.salesChannelId || '',
          orderItems: order.orderItems.map((item, index) => ({
            productId: item.productId,
            orderItemSeqId: index + 1,
            quantity: item.quantity,
            tax: 0,
            discount: 0,
          })),
        }
      : {
          fromSupplierId: '',
          deliveryBeforeDate: '',
          deliveryAfterDate: '',
          orderName: '',
          note: '',
          priority: 1,
          saleChannelId: '',
          orderItems: [
            {
              productId: '',
              orderItemSeqId: 1,
              quantity: 1,
              tax: 0,
              discount: 0,
            },
          ],
        },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'orderItems',
  });

  const watchItems = watch('orderItems');

  // Auto-update orderItemSeqId when items change
  useEffect(() => {
    fields.forEach((_, index) => {
      setValue(`orderItems.${index}.orderItemSeqId`, index + 1);
    });
  }, [fields.length, setValue]);

  // Get available products for a specific item (exclude already selected products)
  const getAvailableProducts = (currentIndex: number) => {
    const selectedProductIds = watchItems
      .map((item, idx) => (idx !== currentIndex ? item.productId : null))
      .filter((id) => id && id !== '');

    return products.filter(
      (product) => !selectedProductIds.includes(product.id)
    );
  };

  // Handle product change with duplicate check
  const handleProductChange = (index: number, value: string) => {
    const isDuplicate = watchItems.some(
      (item, idx) => idx !== index && item.productId === value
    );

    if (isDuplicate) {
      // Show error or warning
      alert('This product has already been added to the order!');
      return;
    }

    setValue(`orderItems.${index}.productId`, value);
  };

  const onFormSubmit = handleSubmit(async (data: OrderFormData) => {
    await onSubmit(data);
  });

  return (
    <form onSubmit={onFormSubmit} className='space-y-4'>
      <Card>
        <CardHeader>
          <CardTitle>Order Information</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {/* Order Name */}
            <div className='space-y-2'>
              <Label htmlFor='orderName'>
                Order Name <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='orderName'
                {...register('orderName')}
                placeholder='Enter order name'
                disabled={isLoading}
              />
              {errors.orderName && (
                <p className='text-sm text-destructive'>
                  {errors.orderName.message}
                </p>
              )}
            </div>

            {/* Supplier */}
            <div className='space-y-2'>
              <Label htmlFor='fromSupplierId'>
                Supplier <span className='text-red-500'>*</span>
              </Label>
              <Select
                value={watch('fromSupplierId')}
                onValueChange={(value) => setValue('fromSupplierId', value)}
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select supplier' />
                </SelectTrigger>
                <SelectContent>
                  {suppliers.map((supplier) => (
                    <SelectItem key={supplier.id} value={supplier.id}>
                      {supplier.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.fromSupplierId && (
                <p className='text-sm text-destructive'>
                  {errors.fromSupplierId.message}
                </p>
              )}
            </div>

            {/* Delivery After Date */}
            <div className='space-y-2'>
              <Label htmlFor='deliveryAfterDate'>
                Delivery After Date <span className='text-red-500'>*</span>
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
                Delivery Before Date <span className='text-red-500'>*</span>
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
                Priority <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='priority'
                type='number'
                {...register('priority', { valueAsNumber: true })}
                min='0'
                placeholder='0'
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
              <Label htmlFor='saleChannelId'>Sales Channel</Label>
              <Select
                value={watch('saleChannelId')}
                onValueChange={(value) => setValue('saleChannelId', value)}
                disabled={isLoading}
              >
                <SelectTrigger>
                  <SelectValue placeholder='Select sales channel' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='PARTNER'>Partner</SelectItem>
                  <SelectItem value='ONLINE'>Online</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Note */}
          <div className='space-y-2'>
            <Label htmlFor='note'>Note</Label>
            <Textarea
              id='note'
              {...register('note')}
              placeholder='Enter order note'
              rows={3}
              disabled={isLoading}
            />
          </div>
        </CardContent>
      </Card>

      {/* Order Items */}
      <Card>
        <CardHeader className='flex flex-row items-center justify-between'>
          <CardTitle>Order Items</CardTitle>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={() => {
              const newSeqId = fields.length + 1;
              append({
                productId: '',
                orderItemSeqId: newSeqId,
                quantity: 1,
                tax: 0,
                discount: 0,
              });
            }}
            disabled={isLoading}
          >
            <Plus className='h-4 w-4 mr-2' />
            Add Item
          </Button>
        </CardHeader>
        <CardContent>
          <div className='rounded-md border overflow-x-auto'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='w-[60px]'>#</TableHead>
                  <TableHead className='min-w-[200px]'>Product</TableHead>
                  <TableHead className='min-w-[100px]'>Quantity</TableHead>
                  <TableHead className='min-w-[100px]'>Tax (%)</TableHead>
                  <TableHead className='min-w-[100px]'>Discount</TableHead>
                  <TableHead className='w-[50px]'></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {fields.map((field, index) => (
                  <TableRow key={field.id}>
                    <TableCell>
                      <div className='text-sm text-muted-foreground'>
                        {index + 1}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Select
                        value={watch(`orderItems.${index}.productId`)}
                        onValueChange={(value) =>
                          handleProductChange(index, value)
                        }
                        disabled={isLoading}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder='Select product' />
                        </SelectTrigger>
                        <SelectContent>
                          {getAvailableProducts(index).map((product) => (
                            <SelectItem key={product.id} value={product.id}>
                              {product.name}
                            </SelectItem>
                          ))}
                          {getAvailableProducts(index).length === 0 && (
                            <div className='px-2 py-1.5 text-sm text-muted-foreground'>
                              No available products
                            </div>
                          )}
                        </SelectContent>
                      </Select>
                      {errors.orderItems?.[index]?.productId && (
                        <p className='text-xs text-destructive mt-1'>
                          {errors.orderItems[index]?.productId?.message}
                        </p>
                      )}
                    </TableCell>
                    <TableCell>
                      <Input
                        type='number'
                        {...register(`orderItems.${index}.quantity`, {
                          valueAsNumber: true,
                        })}
                        min='1'
                        disabled={isLoading}
                      />
                      {errors.orderItems?.[index]?.quantity && (
                        <p className='text-xs text-destructive mt-1'>
                          {errors.orderItems[index]?.quantity?.message}
                        </p>
                      )}
                    </TableCell>
                    <TableCell>
                      <Input
                        type='number'
                        {...register(`orderItems.${index}.tax`, {
                          valueAsNumber: true,
                        })}
                        min='0'
                        step='0.01'
                        placeholder='0'
                        disabled={isLoading}
                      />
                      {errors.orderItems?.[index]?.tax && (
                        <p className='text-xs text-destructive mt-1'>
                          {errors.orderItems[index]?.tax?.message}
                        </p>
                      )}
                    </TableCell>
                    <TableCell>
                      <Input
                        type='number'
                        {...register(`orderItems.${index}.discount`, {
                          valueAsNumber: true,
                        })}
                        min='0'
                        step='0.01'
                        placeholder='0'
                        disabled={isLoading}
                      />
                      {errors.orderItems?.[index]?.discount && (
                        <p className='text-xs text-destructive mt-1'>
                          {errors.orderItems[index]?.discount?.message}
                        </p>
                      )}
                    </TableCell>
                    <TableCell>
                      {fields.length > 1 && (
                        <Button
                          type='button'
                          variant='ghost'
                          size='sm'
                          onClick={() => remove(index)}
                          disabled={isLoading}
                        >
                          <Trash2 className='h-4 w-4 text-destructive' />
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
                {fields.length === 0 && (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className='text-center text-muted-foreground'
                    >
                      No items added yet
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>
          {errors.orderItems &&
            typeof errors.orderItems === 'object' &&
            'message' in errors.orderItems && (
              <p className='text-sm text-destructive mt-2'>
                {errors.orderItems.message as string}
              </p>
            )}
        </CardContent>
      </Card>

      {/* Form Actions */}
      <div className='flex justify-end gap-3'>
        {onCancel && (
          <Button
            type='button'
            variant='outline'
            onClick={onCancel}
            disabled={isLoading}
          >
            Cancel
          </Button>
        )}
        <Button type='submit' disabled={isLoading}>
          {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
          {order ? 'Update Order' : 'Create Order'}
        </Button>
      </div>
    </form>
  );
};
