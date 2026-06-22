'use client';

import React, { useState, useEffect } from 'react';
import { toast } from 'sonner';
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
import { Save } from 'lucide-react';
import { useUpdateProductMutation } from '../../api/purchaseApi';
import type { Product, ProductStatus, ProductUpdateForm } from '../../types';

interface EditProductDialogProps {
  product: Product | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const EditProductDialog: React.FC<EditProductDialogProps> = ({
  product,
  open,
  onOpenChange,
}) => {
  const [updateProduct, { isLoading }] = useUpdateProductMutation();

  const [formData, setFormData] = useState<ProductUpdateForm>({
    name: '',
    skuCode: '',
    unit: '',
    costPrice: undefined,
    wholeSalePrice: undefined,
    retailPrice: undefined,
    vatRate: undefined,
    weight: undefined,
    height: undefined,
    statusId: 'ACTIVE',
  });

  useEffect(() => {
    if (product) {
      setFormData({
        name: product.name,
        skuCode: product.skuCode,
        unit: product.unit,
        costPrice: product.costPrice || undefined,
        wholeSalePrice: product.wholeSalePrice || undefined,
        retailPrice: product.retailPrice || undefined,
        vatRate: product.vatRate || undefined,
        weight: product.weight || undefined,
        height: product.height || undefined,
        statusId: product.statusId,
      });
    }
  }, [product]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!product) return;

    if (!formData.name?.trim()) {
      toast.error('Vui lòng nhập tên sản phẩm');
      return;
    }

    const submitData: ProductUpdateForm = { ...formData };
    if (!submitData.costPrice) delete submitData.costPrice;
    if (!submitData.wholeSalePrice) delete submitData.wholeSalePrice;
    if (!submitData.retailPrice) delete submitData.retailPrice;
    if (!submitData.vatRate) delete submitData.vatRate;
    if (!submitData.weight) delete submitData.weight;
    if (!submitData.height) delete submitData.height;

    try {
      await updateProduct({ productId: product.id, data: submitData }).unwrap();
      toast.success('Cập nhật sản phẩm thành công');
      onOpenChange(false);
    } catch (error: any) {
      const errorMessage =
        error?.data?.message ||
        'Không thể cập nhật sản phẩm. Vui lòng thử lại.';
      toast.error(errorMessage);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Chỉnh sửa sản phẩm</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-6 py-2'>
          {/* Basic Info */}
          <div className='space-y-4'>
            <h4 className='text-sm font-semibold text-muted-foreground uppercase tracking-wide'>
              Thông tin cơ bản
            </h4>

            <div className='space-y-2'>
              <Label htmlFor='edit-name'>
                Tên sản phẩm <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='edit-name'
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                placeholder='Nhập tên sản phẩm'
              />
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='edit-sku'>Mã SKU</Label>
                <Input
                  id='edit-sku'
                  value={formData.skuCode ?? ''}
                  onChange={(e) =>
                    setFormData({ ...formData, skuCode: e.target.value })
                  }
                  placeholder='Nhập mã SKU'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='edit-unit'>Đơn vị tính</Label>
                <Input
                  id='edit-unit'
                  value={formData.unit ?? ''}
                  onChange={(e) =>
                    setFormData({ ...formData, unit: e.target.value })
                  }
                  placeholder='VD: cái, kg, hộp...'
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='edit-status'>Trạng thái</Label>
              <select
                id='edit-status'
                value={formData.statusId}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    statusId: e.target.value as ProductStatus,
                  })
                }
                className='w-full px-3 py-2 border rounded-lg bg-background text-sm'
              >
                <option value='ACTIVE'>Đang bán</option>
                <option value='INACTIVE'>Ngừng bán</option>
              </select>
            </div>
          </div>

          {/* Pricing */}
          <div className='space-y-4'>
            <h4 className='text-sm font-semibold text-muted-foreground uppercase tracking-wide'>
              Giá bán
            </h4>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='edit-cost'>Giá vốn (đ)</Label>
                <Input
                  id='edit-cost'
                  type='number'
                  min='0'
                  value={formData.costPrice ?? ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      costPrice: e.target.value
                        ? parseFloat(e.target.value)
                        : undefined,
                    })
                  }
                  placeholder='0'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='edit-wholesale'>Giá bán sỉ (đ)</Label>
                <Input
                  id='edit-wholesale'
                  type='number'
                  min='0'
                  value={formData.wholeSalePrice ?? ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      wholeSalePrice: e.target.value
                        ? parseFloat(e.target.value)
                        : undefined,
                    })
                  }
                  placeholder='0'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='edit-retail'>Giá bán lẻ (đ)</Label>
                <Input
                  id='edit-retail'
                  type='number'
                  min='0'
                  value={formData.retailPrice ?? ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      retailPrice: e.target.value
                        ? parseFloat(e.target.value)
                        : undefined,
                    })
                  }
                  placeholder='0'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='edit-vat'>Thuế VAT (%)</Label>
                <Input
                  id='edit-vat'
                  type='number'
                  min='0'
                  max='100'
                  value={formData.vatRate ?? ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      vatRate: e.target.value
                        ? parseFloat(e.target.value)
                        : undefined,
                    })
                  }
                  placeholder='0'
                />
              </div>
            </div>
          </div>

          {/* Dimensions */}
          <div className='space-y-4'>
            <h4 className='text-sm font-semibold text-muted-foreground uppercase tracking-wide'>
              Thông số kỹ thuật
            </h4>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='edit-weight'>Khối lượng (kg)</Label>
                <Input
                  id='edit-weight'
                  type='number'
                  min='0'
                  step='0.01'
                  value={formData.weight ?? ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      weight: e.target.value
                        ? parseFloat(e.target.value)
                        : undefined,
                    })
                  }
                  placeholder='0'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='edit-height'>Chiều cao (cm)</Label>
                <Input
                  id='edit-height'
                  type='number'
                  min='0'
                  step='0.01'
                  value={formData.height ?? ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      height: e.target.value
                        ? parseFloat(e.target.value)
                        : undefined,
                    })
                  }
                  placeholder='0'
                />
              </div>
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
            <Button type='submit' disabled={isLoading} className='gap-2'>
              {isLoading ? (
                <>
                  <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin' />
                  Đang lưu...
                </>
              ) : (
                <>
                  <Save className='h-4 w-4' />
                  Lưu thay đổi
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
