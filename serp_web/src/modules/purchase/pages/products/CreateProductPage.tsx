'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Input,
  Label,
  Badge,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Save,
  Package,
  Tag,
  DollarSign,
  AlertCircle,
  Search,
  Plus,
} from 'lucide-react';
import {
  useCreateProductMutation,
  useGetCategoriesQuery,
  useCreateCategoryMutation,
} from '../../api/purchaseApi';
import type { ProductCreationForm, ProductStatus } from '../../types';

export const CreateProductPage: React.FC = () => {
  const router = useRouter();
  const [createProduct, { isLoading: isCreating }] = useCreateProductMutation();
  const [createCategory, { isLoading: isCreatingCategory }] =
    useCreateCategoryMutation();

  const [formData, setFormData] = useState<ProductCreationForm>({
    name: '',
    skuCode: '',
    unit: '',
    categoryId: '',
    statusId: 'ACTIVE',
    costPrice: undefined,
    wholeSalePrice: undefined,
    retailPrice: undefined,
    vatRate: undefined,
    weight: undefined,
    height: undefined,
  });

  // Category search state
  const [categorySearch, setCategorySearch] = useState('');
  const [showCategoryDropdown, setShowCategoryDropdown] = useState(false);
  const [selectedCategoryName, setSelectedCategoryName] = useState('');

  // Create category dialog state
  const [showCreateCategoryDialog, setShowCreateCategoryDialog] =
    useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');

  const { data: categoriesResponse } = useGetCategoriesQuery({
    filters: { query: categorySearch },
    pagination: { page: 0, size: 20 },
  });

  const categories = categoriesResponse?.data?.items || [];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      toast.error('Vui lòng nhập tên sản phẩm');
      return;
    }
    if (!formData.skuCode.trim()) {
      toast.error('Vui lòng nhập mã SKU');
      return;
    }
    if (!formData.unit.trim()) {
      toast.error('Vui lòng nhập đơn vị tính');
      return;
    }
    if (!formData.categoryId) {
      toast.error('Vui lòng chọn danh mục sản phẩm');
      return;
    }

    try {
      const submitData = { ...formData };
      if (submitData.costPrice === undefined) delete submitData.costPrice;
      if (submitData.wholeSalePrice === undefined)
        delete submitData.wholeSalePrice;
      if (submitData.retailPrice === undefined) delete submitData.retailPrice;
      if (submitData.vatRate === undefined) delete submitData.vatRate;
      if (submitData.weight === undefined) delete submitData.weight;
      if (submitData.height === undefined) delete submitData.height;

      await createProduct(submitData).unwrap();
      toast.success('Tạo sản phẩm thành công');
      router.push(`/purchase/products`);
    } catch (error: any) {
      const errorMessage =
        error?.data?.message || 'Không thể tạo sản phẩm. Vui lòng thử lại.';
      toast.error(errorMessage);
    }
  };

  const handleCreateCategory = async () => {
    if (!newCategoryName.trim()) {
      toast.error('Vui lòng nhập tên danh mục');
      return;
    }

    try {
      const result = await createCategory({
        name: newCategoryName.trim(),
      }).unwrap();
      const newCategory = result.data;
      if (newCategory) {
        setFormData({ ...formData, categoryId: newCategory.id });
        setSelectedCategoryName(newCategory.name);
        setCategorySearch('');
      }
      setShowCreateCategoryDialog(false);
      setNewCategoryName('');
      toast.success('Tạo danh mục thành công');
    } catch (error: any) {
      const errorMessage =
        error?.data?.message || 'Không thể tạo danh mục. Vui lòng thử lại.';
      toast.error(errorMessage);
    }
  };

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center gap-4'>
        <Button
          variant='outline'
          size='icon'
          onClick={() => router.back()}
          className='h-9 w-9'
        >
          <ArrowLeft className='h-4 w-4' />
        </Button>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Thêm sản phẩm mới
          </h1>
          <p className='text-muted-foreground'>
            Tạo sản phẩm mới trong danh mục
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className='grid gap-6 lg:grid-cols-3'>
          {/* Main Form */}
          <div className='lg:col-span-2 space-y-6'>
            {/* Basic Info */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Thông tin cơ bản</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='space-y-2'>
                  <Label htmlFor='name'>
                    Tên sản phẩm <span className='text-destructive'>*</span>
                  </Label>
                  <Input
                    id='name'
                    value={formData.name}
                    onChange={(e) =>
                      setFormData({ ...formData, name: e.target.value })
                    }
                    placeholder='Nhập tên sản phẩm'
                  />
                </div>

                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='skuCode'>
                      Mã SKU <span className='text-destructive'>*</span>
                    </Label>
                    <Input
                      id='skuCode'
                      value={formData.skuCode}
                      onChange={(e) =>
                        setFormData({ ...formData, skuCode: e.target.value })
                      }
                      placeholder='Nhập mã SKU'
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='unit'>
                      Đơn vị tính <span className='text-destructive'>*</span>
                    </Label>
                    <Input
                      id='unit'
                      value={formData.unit}
                      onChange={(e) =>
                        setFormData({ ...formData, unit: e.target.value })
                      }
                      placeholder='VD: cái, kg, hộp...'
                    />
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Category */}
            <Card>
              <CardHeader>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2'>
                    <Tag className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Danh mục</h3>
                  </div>
                  <Button
                    type='button'
                    variant='outline'
                    size='sm'
                    onClick={() => setShowCreateCategoryDialog(true)}
                    className='gap-1'
                  >
                    <Plus className='h-3.5 w-3.5' />
                    Tạo danh mục mới
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className='space-y-2'>
                  <Label htmlFor='category'>
                    Danh mục sản phẩm{' '}
                    <span className='text-destructive'>*</span>
                  </Label>
                  <div className='relative'>
                    <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      id='category'
                      placeholder='Tìm kiếm danh mục...'
                      value={selectedCategoryName || categorySearch}
                      onChange={(e) => {
                        setCategorySearch(e.target.value);
                        setShowCategoryDropdown(true);
                        if (formData.categoryId) {
                          setFormData({ ...formData, categoryId: '' });
                          setSelectedCategoryName('');
                        }
                      }}
                      onFocus={() => setShowCategoryDropdown(true)}
                      className='pl-10'
                    />

                    {showCategoryDropdown && (
                      <div className='absolute z-10 w-full mt-1 bg-background border rounded-lg shadow-lg max-h-60 overflow-y-auto'>
                        {categories.length > 0 ? (
                          categories.map((category) => (
                            <button
                              key={category.id}
                              type='button'
                              onClick={() => {
                                setFormData({
                                  ...formData,
                                  categoryId: category.id,
                                });
                                setSelectedCategoryName(category.name);
                                setCategorySearch('');
                                setShowCategoryDropdown(false);
                              }}
                              className='w-full px-4 py-3 text-left hover:bg-muted transition-colors border-b last:border-0'
                            >
                              <p className='font-medium'>{category.name}</p>
                            </button>
                          ))
                        ) : (
                          <div className='px-4 py-3 text-sm text-muted-foreground text-center'>
                            Không tìm thấy danh mục.{' '}
                            <button
                              type='button'
                              className='text-primary underline'
                              onClick={() => {
                                setShowCategoryDropdown(false);
                                setShowCreateCategoryDialog(true);
                                setNewCategoryName(categorySearch);
                              }}
                            >
                              Tạo mới
                            </button>
                          </div>
                        )}
                      </div>
                    )}
                  </div>

                  {formData.categoryId && (
                    <div className='p-3 bg-muted rounded-lg'>
                      <p className='text-sm font-medium'>
                        {selectedCategoryName}
                      </p>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Pricing */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <DollarSign className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Giá bán</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='costPrice'>Giá vốn (đ)</Label>
                    <Input
                      id='costPrice'
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
                    <Label htmlFor='wholeSalePrice'>Giá bán sỉ (đ)</Label>
                    <Input
                      id='wholeSalePrice'
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
                    <Label htmlFor='retailPrice'>Giá bán lẻ (đ)</Label>
                    <Input
                      id='retailPrice'
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
                    <Label htmlFor='vatRate'>Thuế VAT (%)</Label>
                    <Input
                      id='vatRate'
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
              </CardContent>
            </Card>

            {/* Dimensions */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Thông số kỹ thuật</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='weight'>Khối lượng (kg)</Label>
                    <Input
                      id='weight'
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
                    <Label htmlFor='height'>Chiều cao (cm)</Label>
                    <Input
                      id='height'
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
              </CardContent>
            </Card>
          </div>

          {/* Sidebar */}
          <div className='space-y-6'>
            {/* Status */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <AlertCircle className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Trạng thái</h3>
                </div>
              </CardHeader>
              <CardContent>
                <div className='space-y-2'>
                  <Label htmlFor='status'>Trạng thái sản phẩm</Label>
                  <select
                    id='status'
                    value={formData.statusId}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        statusId: e.target.value as ProductStatus,
                      })
                    }
                    className='w-full px-3 py-2 border rounded-lg bg-background'
                  >
                    <option value='ACTIVE'>Đang bán</option>
                    <option value='INACTIVE'>Ngừng bán</option>
                  </select>
                </div>
              </CardContent>
            </Card>

            {/* Summary */}
            <Card className='bg-gradient-to-br from-primary/5 to-primary/10'>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Tóm tắt</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Tên:</span>
                  <span className='font-medium truncate max-w-32'>
                    {formData.name || '-'}
                  </span>
                </div>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>SKU:</span>
                  <span className='font-medium'>{formData.skuCode || '-'}</span>
                </div>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Đơn vị:</span>
                  <span className='font-medium'>{formData.unit || '-'}</span>
                </div>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Danh mục:</span>
                  <span className='font-medium truncate max-w-32'>
                    {selectedCategoryName || '-'}
                  </span>
                </div>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Giá bán lẻ:</span>
                  <span className='font-medium'>
                    {formData.retailPrice
                      ? `đ${formData.retailPrice.toLocaleString()}`
                      : '-'}
                  </span>
                </div>
                <div className='flex justify-between text-sm pt-3 border-t'>
                  <span className='text-muted-foreground'>Trạng thái:</span>
                  <Badge
                    variant='secondary'
                    className={
                      formData.statusId === 'ACTIVE'
                        ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30'
                        : 'bg-slate-100 text-slate-700 dark:bg-slate-900/30'
                    }
                  >
                    {formData.statusId === 'ACTIVE' ? 'Đang bán' : 'Ngừng bán'}
                  </Badge>
                </div>
              </CardContent>
            </Card>

            {/* Actions */}
            <div className='flex flex-col gap-3'>
              <Button
                type='submit'
                disabled={isCreating}
                className='w-full gap-2'
              >
                {isCreating ? (
                  <>
                    <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin' />
                    Đang xử lý...
                  </>
                ) : (
                  <>
                    <Save className='h-4 w-4' />
                    Tạo sản phẩm
                  </>
                )}
              </Button>
              <Button
                type='button'
                variant='outline'
                onClick={() => router.back()}
                disabled={isCreating}
                className='w-full'
              >
                Hủy
              </Button>
            </div>
          </div>
        </div>
      </form>

      {/* Create Category Dialog */}
      <Dialog
        open={showCreateCategoryDialog}
        onOpenChange={setShowCreateCategoryDialog}
      >
        <DialogContent className='sm:max-w-md'>
          <DialogHeader>
            <DialogTitle>Tạo danh mục mới</DialogTitle>
          </DialogHeader>
          <div className='space-y-4 py-2'>
            <div className='space-y-2'>
              <Label htmlFor='newCategoryName'>
                Tên danh mục <span className='text-destructive'>*</span>
              </Label>
              <Input
                id='newCategoryName'
                value={newCategoryName}
                onChange={(e) => setNewCategoryName(e.target.value)}
                placeholder='Nhập tên danh mục'
                onKeyDown={(e) => e.key === 'Enter' && handleCreateCategory()}
                autoFocus
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => {
                setShowCreateCategoryDialog(false);
                setNewCategoryName('');
              }}
              disabled={isCreatingCategory}
            >
              Hủy
            </Button>
            <Button
              type='button'
              onClick={handleCreateCategory}
              disabled={isCreatingCategory}
              className='gap-2'
            >
              {isCreatingCategory ? (
                <>
                  <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin' />
                  Đang tạo...
                </>
              ) : (
                'Tạo danh mục'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};
