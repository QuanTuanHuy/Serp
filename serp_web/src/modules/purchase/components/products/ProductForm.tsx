/*
Author: QuanTuanHuy
Description: Part of Serp Project - Product Form Component
*/

'use client';

import React from 'react';
import { useForm } from 'react-hook-form';
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
import { Loader2 } from 'lucide-react';
import type { Product, Category } from '../../types';

const productFormSchema = z.object({
  name: z.string().min(3, 'Name must be at least 3 characters'),
  weight: z.number().min(0).optional(),
  height: z.number().min(0).optional(),
  unit: z.string().min(1, 'Unit is required'),
  costPrice: z.number().min(0, 'Cost price must be positive'),
  wholeSalePrice: z.number().min(0, 'Whole sale price must be positive'),
  retailPrice: z.number().min(0).optional(),
  categoryId: z.string().optional(),
  statusId: z.string().min(1, 'Status is required'),
  imageId: z.string().optional(),
});

type ProductFormData = z.infer<typeof productFormSchema>;

interface ProductFormProps {
  product?: Product;
  categories?: Category[];
  onSubmit: (data: ProductFormData) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
}

export const ProductForm: React.FC<ProductFormProps> = ({
  product,
  categories = [],
  onSubmit,
  onCancel,
  isLoading = false,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
  } = useForm<ProductFormData>({
    resolver: zodResolver(productFormSchema),
    defaultValues: product
      ? {
          name: product.name,
          weight: product.weight,
          height: product.height,
          unit: product.unit,
          costPrice: product.costPrice,
          wholeSalePrice: product.wholeSalePrice,
          retailPrice: product.retailPrice,
          categoryId: product.categoryId,
          statusId: product.statusId,
          imageId: product.imageId,
        }
      : {
          unit: 'Cái',
          statusId: 'ACTIVE',
          costPrice: 0,
          wholeSalePrice: 0,
          retailPrice: 0,
        },
  });

  const onFormSubmit = handleSubmit(async (data: ProductFormData) => {
    await onSubmit(data);
  });

  return (
    <form onSubmit={onFormSubmit} className='space-y-4'>
      <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
        {/* Product Name */}
        <div className='space-y-2'>
          <Label htmlFor='name'>
            Product Name <span className='text-red-500'>*</span>
          </Label>
          <Input
            id='name'
            {...register('name')}
            placeholder='Enter product name'
            disabled={isLoading}
          />
          {errors.name && (
            <p className='text-sm text-red-500'>{errors.name.message}</p>
          )}
        </div>

        {/* Unit */}
        <div className='space-y-2'>
          <Label htmlFor='unit'>
            Unit <span className='text-red-500'>*</span>
          </Label>
          <Input
            id='unit'
            {...register('unit')}
            placeholder='e.g., pcs, kg, box'
            disabled={isLoading}
          />
          {errors.unit && (
            <p className='text-sm text-red-500'>{errors.unit.message}</p>
          )}
        </div>

        {/* Cost Price */}
        <div className='space-y-2'>
          <Label htmlFor='costPrice'>
            Cost Price <span className='text-red-500'>*</span>
          </Label>
          <Input
            id='costPrice'
            type='number'
            step='0.01'
            {...register('costPrice', { valueAsNumber: true })}
            placeholder='0.00'
            disabled={isLoading}
          />
          {errors.costPrice && (
            <p className='text-sm text-red-500'>{errors.costPrice.message}</p>
          )}
        </div>

        {/* Wholesale Price */}
        <div className='space-y-2'>
          <Label htmlFor='wholeSalePrice'>Wholesale Price</Label>
          <Input
            id='wholeSalePrice'
            type='number'
            step='0.01'
            {...register('wholeSalePrice', { valueAsNumber: true })}
            placeholder='0.00'
            disabled={isLoading}
          />
          {errors.wholeSalePrice && (
            <p className='text-sm text-red-500'>
              {errors.wholeSalePrice.message}
            </p>
          )}
        </div>

        {/* Retail Price */}
        <div className='space-y-2'>
          <Label htmlFor='retailPrice'>Retail Price</Label>
          <Input
            id='retailPrice'
            type='number'
            step='0.01'
            {...register('retailPrice', { valueAsNumber: true })}
            placeholder='0.00'
            disabled={isLoading}
          />
          {errors.retailPrice && (
            <p className='text-sm text-red-500'>{errors.retailPrice.message}</p>
          )}
        </div>

        {/* Category */}
        <div className='space-y-2'>
          <Label htmlFor='categoryId'>Category</Label>
          <Select
            value={watch('categoryId') || 'NONE'}
            onValueChange={(value) =>
              setValue('categoryId', value === 'NONE' ? undefined : value)
            }
            disabled={isLoading}
          >
            <SelectTrigger>
              <SelectValue placeholder='Select category' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='NONE'>No category</SelectItem>
              {categories.map((category) => (
                <SelectItem key={category.id} value={category.id}>
                  {category.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.categoryId && (
            <p className='text-sm text-red-500'>{errors.categoryId.message}</p>
          )}
        </div>

        {/* Weight */}
        <div className='space-y-2'>
          <Label htmlFor='weight'>Weight (kg)</Label>
          <Input
            id='weight'
            type='number'
            step='0.01'
            {...register('weight', { valueAsNumber: true })}
            placeholder='0.00'
            disabled={isLoading}
          />
          {errors.weight && (
            <p className='text-sm text-red-500'>{errors.weight.message}</p>
          )}
        </div>

        {/* Height */}
        <div className='space-y-2'>
          <Label htmlFor='height'>Height (cm)</Label>
          <Input
            id='height'
            type='number'
            step='0.01'
            {...register('height', { valueAsNumber: true })}
            placeholder='0.00'
            disabled={isLoading}
          />
          {errors.height && (
            <p className='text-sm text-red-500'>{errors.height.message}</p>
          )}
        </div>

        {/* Status */}
        <div className='space-y-2'>
          <Label htmlFor='statusId'>
            Status <span className='text-red-500'>*</span>
          </Label>
          <Select
            value={watch('statusId')}
            onValueChange={(value) => setValue('statusId', value)}
            disabled={isLoading}
          >
            <SelectTrigger>
              <SelectValue placeholder='Select status' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ACTIVE'>Active</SelectItem>
              <SelectItem value='INACTIVE'>Inactive</SelectItem>
            </SelectContent>
          </Select>
          {errors.statusId && (
            <p className='text-sm text-red-500'>{errors.statusId.message}</p>
          )}
        </div>

        {/* Image ID (placeholder) */}
        <div className='space-y-2'>
          <Label htmlFor='imageId'>Image ID</Label>
          <Input
            id='imageId'
            {...register('imageId')}
            placeholder='Image identifier'
            disabled={isLoading}
          />
          {errors.imageId && (
            <p className='text-sm text-red-500'>{errors.imageId.message}</p>
          )}
        </div>
      </div>

      {/* Form Actions */}
      <div className='flex justify-end gap-2 pt-4'>
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
          {product ? 'Update Product' : 'Create Product'}
        </Button>
      </div>
    </form>
  );
};
