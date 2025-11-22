/*
Author: QuanTuanHuy
Description: Part of Serp Project - Product Form Dialog Component
*/

'use client';

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { ProductForm } from './ProductForm';
import type {
  Product,
  Category,
  CreateProductRequest,
  UpdateProductRequest,
} from '../../types';

interface ProductFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  product?: Product;
  categories?: Category[];
  onSubmit: (data: CreateProductRequest | UpdateProductRequest) => Promise<void>;
  isLoading?: boolean;
}

export const ProductFormDialog: React.FC<ProductFormDialogProps> = ({
  open,
  onOpenChange,
  product,
  categories,
  onSubmit,
  isLoading = false,
}) => {
  const handleCancel = () => {
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {product ? 'Edit Product' : 'Create New Product'}
          </DialogTitle>
        </DialogHeader>
        <ProductForm
          product={product}
          categories={categories}
          onSubmit={onSubmit}
          onCancel={handleCancel}
          isLoading={isLoading}
        />
      </DialogContent>
    </Dialog>
  );
};
