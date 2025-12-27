/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create Inventory Item Page
*/

'use client';

import { useState } from 'react';
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
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Save,
  Package,
  Calendar,
  MapPin,
  Box,
  AlertCircle,
  Search,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useCreateInventoryItemMutation,
  useGetProductsQuery,
  useGetFacilitiesQuery,
} from '../../api/salesApi';
import type {
  InventoryItemCreationForm,
  InventoryItemStatus,
} from '../../types';

export const CreateInventoryItemPage: React.FC = () => {
  const router = useRouter();
  const [createInventoryItem, { isLoading: isCreating }] =
    useCreateInventoryItemMutation();

  // Form state
  const [formData, setFormData] = useState<InventoryItemCreationForm>({
    productId: '',
    quantity: 0,
    lotId: '',
    facilityId: '',
    expirationDate: '',
    manufacturingDate: '',
    statusId: 'VALID',
  });

  // Search states
  const [productSearch, setProductSearch] = useState('');
  const [facilitySearch, setFacilitySearch] = useState('');
  const [showProductDropdown, setShowProductDropdown] = useState(false);
  const [showFacilityDropdown, setShowFacilityDropdown] = useState(false);

  // Fetch products
  const { data: productsResponse } = useGetProductsQuery({
    filters: { query: productSearch },
    pagination: { page: 0, size: 20 },
  });

  // Fetch facilities
  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    filters: { query: facilitySearch },
    pagination: { page: 0, size: 20 },
  });

  const products = productsResponse?.data?.items || [];
  const facilities = facilitiesResponse?.data?.items || [];

  const selectedProduct = products.find((p) => p.id === formData.productId);
  const selectedFacility = facilities.find((f) => f.id === formData.facilityId);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validation
    if (!formData.productId) {
      toast.error('Vui lòng chọn sản phẩm');
      return;
    }
    if (!formData.facilityId) {
      toast.error('Vui lòng chọn kho');
      return;
    }
    if (!formData.lotId) {
      toast.error('Vui lòng nhập số lot');
      return;
    }
    if (formData.quantity <= 0) {
      toast.error('Số lượng phải lớn hơn 0');
      return;
    }

    try {
      // Remove empty date fields
      const submitData = { ...formData };
      if (!submitData.expirationDate) delete submitData.expirationDate;
      if (!submitData.manufacturingDate) delete submitData.manufacturingDate;

      await createInventoryItem(submitData).unwrap();
      toast.success('Nhập kho thành công');
      router.push('/sales/inventory');
    } catch (error: any) {
      const errorMessage =
        error?.data?.message || 'Không thể nhập kho. Vui lòng thử lại.';
      toast.error(errorMessage);
    }
  };

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
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
            <h1 className='text-2xl font-bold tracking-tight'>Nhập kho</h1>
            <p className='text-muted-foreground'>Thêm sản phẩm mới vào kho</p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className='grid gap-6 lg:grid-cols-3'>
          {/* Main Form */}
          <div className='lg:col-span-2 space-y-6'>
            {/* Product Selection */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Thông tin sản phẩm</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                {/* Product Search */}
                <div className='space-y-2'>
                  <Label htmlFor='product'>
                    Sản phẩm <span className='text-destructive'>*</span>
                  </Label>
                  <div className='relative'>
                    <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      id='product'
                      placeholder='Tìm kiếm sản phẩm...'
                      value={selectedProduct?.name || productSearch}
                      onChange={(e) => {
                        setProductSearch(e.target.value);
                        setShowProductDropdown(true);
                        if (formData.productId) {
                          setFormData({ ...formData, productId: '' });
                        }
                      }}
                      onFocus={() => setShowProductDropdown(true)}
                      className='pl-10'
                    />

                    {/* Product Dropdown */}
                    {showProductDropdown && products.length > 0 && (
                      <div className='absolute z-10 w-full mt-1 bg-background border rounded-lg shadow-lg max-h-60 overflow-y-auto'>
                        {products.map((product) => (
                          <button
                            key={product.id}
                            type='button'
                            onClick={() => {
                              setFormData({
                                ...formData,
                                productId: product.id,
                              });
                              setProductSearch('');
                              setShowProductDropdown(false);
                            }}
                            className='w-full px-4 py-3 text-left hover:bg-muted transition-colors border-b last:border-0'
                          >
                            <div className='flex items-center justify-between'>
                              <div>
                                <p className='font-medium'>{product.name}</p>
                                <p className='text-sm text-muted-foreground'>
                                  {product.unit} - đ
                                  {product.costPrice.toLocaleString()}
                                </p>
                              </div>
                              <Badge variant='secondary'>
                                {product.statusId}
                              </Badge>
                            </div>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>

                  {selectedProduct && (
                    <div className='p-3 bg-muted rounded-lg'>
                      <p className='text-sm font-medium'>
                        {selectedProduct.name}
                      </p>
                      <p className='text-xs text-muted-foreground'>
                        Đơn vị: {selectedProduct.unit} | Giá vốn: đ
                        {selectedProduct.costPrice.toLocaleString()}
                      </p>
                    </div>
                  )}
                </div>

                {/* Quantity */}
                <div className='space-y-2'>
                  <Label htmlFor='quantity'>
                    Số lượng <span className='text-destructive'>*</span>
                  </Label>
                  <Input
                    id='quantity'
                    type='number'
                    min='0'
                    step='1'
                    value={formData.quantity || ''}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        quantity: parseInt(e.target.value) || 0,
                      })
                    }
                    placeholder='Nhập số lượng'
                    required
                  />
                </div>

                {/* Lot ID */}
                <div className='space-y-2'>
                  <Label htmlFor='lotId'>
                    Số Lô <span className='text-destructive'>*</span>
                  </Label>
                  <Input
                    id='lotId'
                    value={formData.lotId}
                    onChange={(e) =>
                      setFormData({ ...formData, lotId: e.target.value })
                    }
                    placeholder='Nhập lô hàng'
                    required
                  />
                </div>
              </CardContent>
            </Card>

            {/* Facility & Dates */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <MapPin className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Kho & Thời gian</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                {/* Facility Search */}
                <div className='space-y-2'>
                  <Label htmlFor='facility'>
                    Kho hàng <span className='text-destructive'>*</span>
                  </Label>
                  <div className='relative'>
                    <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      id='facility'
                      placeholder='Tìm kiếm kho...'
                      value={selectedFacility?.name || facilitySearch}
                      onChange={(e) => {
                        setFacilitySearch(e.target.value);
                        setShowFacilityDropdown(true);
                        if (formData.facilityId) {
                          setFormData({ ...formData, facilityId: '' });
                        }
                      }}
                      onFocus={() => setShowFacilityDropdown(true)}
                      className='pl-10'
                    />

                    {/* Facility Dropdown */}
                    {showFacilityDropdown && facilities.length > 0 && (
                      <div className='absolute z-10 w-full mt-1 bg-background border rounded-lg shadow-lg max-h-60 overflow-y-auto'>
                        {facilities.map((facility) => (
                          <button
                            key={facility.id}
                            type='button'
                            onClick={() => {
                              setFormData({
                                ...formData,
                                facilityId: facility.id,
                              });
                              setFacilitySearch('');
                              setShowFacilityDropdown(false);
                            }}
                            className='w-full px-4 py-3 text-left hover:bg-muted transition-colors border-b last:border-0'
                          >
                            <div className='flex items-center justify-between'>
                              <div>
                                <p className='font-medium'>{facility.name}</p>
                                {facility.address && (
                                  <p className='text-sm text-muted-foreground'>
                                    Kho: {facility.name}
                                  </p>
                                )}
                              </div>
                              <Badge variant='secondary'>
                                {facility.statusId}
                              </Badge>
                            </div>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>

                  {selectedFacility && (
                    <div className='p-3 bg-muted rounded-lg'>
                      <p className='text-sm font-medium'>
                        {selectedFacility.name}
                      </p>
                      {selectedFacility.address && (
                        <p className='text-xs text-muted-foreground'>
                          {selectedFacility.address.fullAddress}
                        </p>
                      )}
                    </div>
                  )}
                </div>

                {/* Manufacturing Date */}
                <div className='space-y-2'>
                  <Label htmlFor='manufacturingDate'>Ngày sản xuất</Label>
                  <div className='relative'>
                    <Calendar className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      id='manufacturingDate'
                      type='date'
                      value={formData.manufacturingDate || ''}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          manufacturingDate: e.target.value,
                        })
                      }
                      className='pl-10'
                    />
                  </div>
                </div>

                {/* Expiration Date */}
                <div className='space-y-2'>
                  <Label htmlFor='expirationDate'>Ngày hết hạn</Label>
                  <div className='relative'>
                    <Calendar className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      id='expirationDate'
                      type='date'
                      value={formData.expirationDate || ''}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          expirationDate: e.target.value,
                        })
                      }
                      className='pl-10'
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
                  <Label htmlFor='status'>Trạng thái hàng hóa</Label>
                  <select
                    id='status'
                    value={formData.statusId}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        statusId: e.target.value as InventoryItemStatus,
                      })
                    }
                    className='w-full px-3 py-2 border rounded-lg bg-background'
                  >
                    <option value='VALID'>Hợp lệ</option>
                    <option value='EXPIRED'>Đã hết hạn</option>
                    <option value='DAMAGED'>Hư hỏng</option>
                  </select>
                </div>
              </CardContent>
            </Card>

            {/* Summary */}
            <Card className='bg-gradient-to-br from-primary/5 to-primary/10'>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <Box className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Tóm tắt</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Sản phẩm:</span>
                  <span className='font-medium'>
                    {selectedProduct?.name || '-'}
                  </span>
                </div>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Số lượng:</span>
                  <span className='font-medium'>
                    {formData.quantity || 0} {selectedProduct?.unit || ''}
                  </span>
                </div>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Kho:</span>
                  <span className='font-medium'>
                    {selectedFacility?.name || '-'}
                  </span>
                </div>
                <div className='flex justify-between text-sm'>
                  <span className='text-muted-foreground'>Lot:</span>
                  <span className='font-medium'>{formData.lotId || '-'}</span>
                </div>
                <div className='flex justify-between text-sm pt-3 border-t'>
                  <span className='text-muted-foreground'>Trạng thái:</span>
                  <Badge
                    variant='secondary'
                    className={cn(
                      formData.statusId === 'VALID' &&
                        'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30',
                      formData.statusId === 'EXPIRED' &&
                        'bg-rose-100 text-rose-700 dark:bg-rose-900/30',
                      formData.statusId === 'DAMAGED' &&
                        'bg-amber-100 text-amber-700 dark:bg-amber-900/30'
                    )}
                  >
                    {formData.statusId === 'VALID' && 'Hợp lệ'}
                    {formData.statusId === 'EXPIRED' && 'Đã hết hạn'}
                    {formData.statusId === 'DAMAGED' && 'Hư hỏng'}
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
                    Nhập kho
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
    </div>
  );
};
