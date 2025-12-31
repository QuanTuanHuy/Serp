/**
 * Create Inventory Item Page - Logistics Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Form to add new inventory items
 */

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Badge,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Package,
  Warehouse,
  Calendar,
  CheckCircle2,
  AlertTriangle,
  Search,
  ChevronDown,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import {
  useCreateInventoryItemMutation,
  useGetProductsQuery,
  useGetFacilitiesQuery,
} from '../../api/logisticsApi';
import type { InventoryItemStatus } from '../../types';

interface CreateInventoryItemPageProps {
  className?: string;
}

const statusStyles = {
  VALID: {
    label: 'Hợp lệ',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    icon: CheckCircle2,
  },
  EXPIRED: {
    label: 'Đã hết hạn',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    icon: AlertTriangle,
  },
  DAMAGED: {
    label: 'Hư hỏng',
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    icon: AlertTriangle,
  },
};

const formatDateForInput = (dateString?: string) => {
  if (!dateString) return '';
  try {
    return new Date(dateString).toISOString().split('T')[0];
  } catch {
    return '';
  }
};

export const CreateInventoryItemPage: React.FC<
  CreateInventoryItemPageProps
> = ({ className }) => {
  const router = useRouter();

  const [createItem, { isLoading: isCreating }] =
    useCreateInventoryItemMutation();

  // Form state
  const [formData, setFormData] = useState<{
    productId: string;
    facilityId: string;
    lotId: string;
    quantity: number;
    statusId: InventoryItemStatus;
    manufacturingDate: string;
    expirationDate: string;
  }>({
    productId: '',
    facilityId: '',
    lotId: '',
    quantity: 0,
    statusId: 'VALID',
    manufacturingDate: '',
    expirationDate: '',
  });

  const [productSearch, setProductSearch] = useState('');
  const [facilitySearch, setFacilitySearch] = useState('');
  const [showProductDropdown, setShowProductDropdown] = useState(false);
  const [showFacilityDropdown, setShowFacilityDropdown] = useState(false);

  const [errors, setErrors] = useState<Record<string, string>>({});

  // Fetch products and facilities
  const { data: productsResponse, isLoading: loadingProducts } =
    useGetProductsQuery({
      filters: { query: productSearch },
      pagination: { page: 0, size: 20 },
    });

  const { data: facilitiesResponse, isLoading: loadingFacilities } =
    useGetFacilitiesQuery({
      filters: { query: facilitySearch },
      pagination: { page: 0, size: 20 },
    });

  const products = productsResponse?.data?.items || [];
  const facilities = facilitiesResponse?.data?.items || [];

  // Selected entities
  const selectedProduct = useMemo(
    () => products.find((p) => p.id === formData.productId),
    [products, formData.productId]
  );

  const selectedFacility = useMemo(
    () => facilities.find((f) => f.id === formData.facilityId),
    [facilities, formData.facilityId]
  );

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.productId) {
      newErrors.productId = 'Vui lòng chọn sản phẩm';
    }
    if (!formData.facilityId) {
      newErrors.facilityId = 'Vui lòng chọn kho';
    }
    if (!formData.lotId || formData.lotId.trim() === '') {
      newErrors.lotId = 'Vui lòng nhập mã lô hàng';
    }
    if (formData.quantity <= 0) {
      newErrors.quantity = 'Số lượng phải lớn hơn 0';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    try {
      await createItem(formData).unwrap();
      toast.success('Nhập kho thành công');
      router.push('/logistics/inventory');
    } catch (error) {
      toast.error('Không thể nhập kho. Vui lòng thử lại.');
      console.error('Create error:', error);
    }
  };

  const handleCancel = () => {
    router.push('/logistics/inventory');
  };

  const statusOptions = Object.entries(statusStyles).map(([key, value]) => ({
    value: key as InventoryItemStatus,
    label: value.label,
    Icon: value.icon,
    styles: value,
  }));

  const selectedStatus = statusOptions.find(
    (opt) => opt.value === formData.statusId
  );

  return (
    <div className={cn('space-y-6', className)}>
      {/* Header */}
      <div className='flex items-center gap-4'>
        <Button variant='ghost' size='icon' onClick={handleCancel}>
          <ArrowLeft className='h-5 w-5' />
        </Button>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Nhập kho</h1>
          <p className='text-muted-foreground'>Thêm mới mặt hàng vào tồn kho</p>
        </div>
      </div>

      <form
        onSubmit={handleSubmit}
        className='grid grid-cols-1 lg:grid-cols-3 gap-6'
      >
        {/* Main Form - 2/3 width */}
        <div className='lg:col-span-2 space-y-6'>
          {/* Product Selection */}
          <Card>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Package className='h-5 w-5' />
                Thông tin sản phẩm
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              {/* Product Search */}
              <div className='relative'>
                <label className='text-sm font-medium mb-1.5 block'>
                  Sản phẩm <span className='text-destructive'>*</span>
                </label>
                <div className='relative'>
                  <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                  <Input
                    placeholder='Tìm kiếm sản phẩm...'
                    value={selectedProduct?.name || productSearch}
                    onChange={(e) => {
                      setProductSearch(e.target.value);
                      setShowProductDropdown(true);
                      if (selectedProduct) {
                        setFormData({ ...formData, productId: '' });
                      }
                    }}
                    onFocus={() => setShowProductDropdown(true)}
                    className={cn(
                      'pl-10 pr-10',
                      errors.productId && 'border-destructive'
                    )}
                  />
                  <ChevronDown className='absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                </div>
                {errors.productId && (
                  <p className='text-xs text-destructive mt-1'>
                    {errors.productId}
                  </p>
                )}

                {/* Product Dropdown */}
                {showProductDropdown && (
                  <div className='absolute z-10 w-full mt-1 bg-card border rounded-lg shadow-lg max-h-60 overflow-auto'>
                    {loadingProducts ? (
                      <div className='p-4 text-center text-sm text-muted-foreground'>
                        Đang tải...
                      </div>
                    ) : products.length === 0 ? (
                      <div className='p-4 text-center text-sm text-muted-foreground'>
                        Không tìm thấy sản phẩm
                      </div>
                    ) : (
                      products.map((product) => (
                        <button
                          key={product.id}
                          type='button'
                          onClick={() => {
                            setFormData({ ...formData, productId: product.id });
                            setProductSearch('');
                            setShowProductDropdown(false);
                            setErrors({ ...errors, productId: '' });
                          }}
                          className='w-full text-left px-4 py-2 hover:bg-muted transition-colors'
                        >
                          <div className='font-medium'>{product.name}</div>
                          <div className='text-xs text-muted-foreground'>
                            Đơn vị: {product.unit}
                          </div>
                        </button>
                      ))
                    )}
                  </div>
                )}
              </div>

              {/* Selected Product Info */}
              {selectedProduct && (
                <Card className='bg-muted/50'>
                  <CardContent className='p-4'>
                    <div className='flex items-center gap-3'>
                      <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary'>
                        <Package className='h-5 w-5' />
                      </div>
                      <div className='flex-1'>
                        <p className='font-semibold'>{selectedProduct.name}</p>
                        <p className='text-xs text-muted-foreground'>
                          Đơn vị: {selectedProduct.unit}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Lot ID */}
              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Mã lô hàng <span className='text-destructive'>*</span>
                </label>
                <Input
                  placeholder='Nhập mã lô hàng'
                  value={formData.lotId}
                  onChange={(e) =>
                    setFormData({ ...formData, lotId: e.target.value })
                  }
                  className={errors.lotId ? 'border-destructive' : ''}
                />
                {errors.lotId && (
                  <p className='text-xs text-destructive mt-1'>
                    {errors.lotId}
                  </p>
                )}
              </div>

              {/* Quantity */}
              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Số lượng <span className='text-destructive'>*</span>
                </label>
                <Input
                  type='number'
                  min='0'
                  placeholder='0'
                  value={formData.quantity || ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      quantity: Number(e.target.value),
                    })
                  }
                  className={errors.quantity ? 'border-destructive' : ''}
                />
                {errors.quantity && (
                  <p className='text-xs text-destructive mt-1'>
                    {errors.quantity}
                  </p>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Facility Selection */}
          <Card>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Warehouse className='h-5 w-5' />
                Thông tin kho
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              {/* Facility Search */}
              <div className='relative'>
                <label className='text-sm font-medium mb-1.5 block'>
                  Kho <span className='text-destructive'>*</span>
                </label>
                <div className='relative'>
                  <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                  <Input
                    placeholder='Tìm kiếm kho...'
                    value={selectedFacility?.name || facilitySearch}
                    onChange={(e) => {
                      setFacilitySearch(e.target.value);
                      setShowFacilityDropdown(true);
                      if (selectedFacility) {
                        setFormData({ ...formData, facilityId: '' });
                      }
                    }}
                    onFocus={() => setShowFacilityDropdown(true)}
                    className={cn(
                      'pl-10 pr-10',
                      errors.facilityId && 'border-destructive'
                    )}
                  />
                  <ChevronDown className='absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                </div>
                {errors.facilityId && (
                  <p className='text-xs text-destructive mt-1'>
                    {errors.facilityId}
                  </p>
                )}

                {/* Facility Dropdown */}
                {showFacilityDropdown && (
                  <div className='absolute z-10 w-full mt-1 bg-card border rounded-lg shadow-lg max-h-60 overflow-auto'>
                    {loadingFacilities ? (
                      <div className='p-4 text-center text-sm text-muted-foreground'>
                        Đang tải...
                      </div>
                    ) : facilities.length === 0 ? (
                      <div className='p-4 text-center text-sm text-muted-foreground'>
                        Không tìm thấy kho
                      </div>
                    ) : (
                      facilities.map((facility) => (
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
                            setErrors({ ...errors, facilityId: '' });
                          }}
                          className='w-full text-left px-4 py-2 hover:bg-muted transition-colors'
                        >
                          <div className='font-medium'>{facility.name}</div>
                          <div className='text-xs text-muted-foreground'>
                            {facility.address?.fullAddress || 'N/A'}
                          </div>
                        </button>
                      ))
                    )}
                  </div>
                )}
              </div>

              {/* Selected Facility Info */}
              {selectedFacility && (
                <Card className='bg-muted/50'>
                  <CardContent className='p-4'>
                    <div className='flex items-center gap-3'>
                      <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary'>
                        <Warehouse className='h-5 w-5' />
                      </div>
                      <div className='flex-1'>
                        <p className='font-semibold'>{selectedFacility.name}</p>
                        <p className='text-xs text-muted-foreground'>
                          {selectedFacility.address?.fullAddress || 'N/A'}
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}
            </CardContent>
          </Card>

          {/* Dates & Status */}
          <Card>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Calendar className='h-5 w-5' />
                Thời gian & Trạng thái
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid grid-cols-1 sm:grid-cols-2 gap-4'>
                <div>
                  <label className='text-sm font-medium mb-1.5 block'>
                    Ngày sản xuất
                  </label>
                  <Input
                    type='date'
                    value={formData.manufacturingDate}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        manufacturingDate: e.target.value,
                      })
                    }
                  />
                </div>

                <div>
                  <label className='text-sm font-medium mb-1.5 block'>
                    Ngày hết hạn
                  </label>
                  <Input
                    type='date'
                    value={formData.expirationDate}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        expirationDate: e.target.value,
                      })
                    }
                  />
                </div>

                <div>
                  <label className='text-sm font-medium mb-1.5 block'>
                    Trạng thái
                  </label>
                  <select
                    value={formData.statusId}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        statusId: e.target.value as InventoryItemStatus,
                      })
                    }
                    className='w-full px-3 py-2 border rounded-lg bg-background'
                  >
                    {statusOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Summary Sidebar - 1/3 width */}
        <div className='lg:col-span-1'>
          <Card className='sticky top-6'>
            <CardHeader>
              <CardTitle>Tóm tắt</CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              {/* Product */}
              <div>
                <p className='text-sm text-muted-foreground mb-1'>Sản phẩm</p>
                <p className='font-medium'>
                  {selectedProduct?.name || (
                    <span className='text-muted-foreground'>Chưa chọn</span>
                  )}
                </p>
              </div>

              {/* Facility */}
              <div>
                <p className='text-sm text-muted-foreground mb-1'>Kho</p>
                <p className='font-medium'>
                  {selectedFacility?.name || (
                    <span className='text-muted-foreground'>Chưa chọn</span>
                  )}
                </p>
              </div>

              {/* Quantity */}
              <div>
                <p className='text-sm text-muted-foreground mb-1'>Số lượng</p>
                <p className='font-semibold text-lg text-primary'>
                  {formData.quantity || 0} {selectedProduct?.unit || 'đơn vị'}
                </p>
              </div>

              {/* Status */}
              <div>
                <p className='text-sm text-muted-foreground mb-1'>Trạng thái</p>
                {selectedStatus && (
                  <Badge
                    className={cn(
                      'gap-1',
                      selectedStatus.styles.bg,
                      selectedStatus.styles.text
                    )}
                  >
                    <selectedStatus.Icon className='h-3 w-3' />
                    {selectedStatus.label}
                  </Badge>
                )}
              </div>

              {/* Lot ID */}
              {formData.lotId && (
                <div>
                  <p className='text-sm text-muted-foreground mb-1'>Mã lô</p>
                  <p className='font-medium font-mono text-sm'>
                    {formData.lotId}
                  </p>
                </div>
              )}

              <div className='pt-4 border-t space-y-2'>
                <Button type='submit' className='w-full' disabled={isCreating}>
                  <CheckCircle2 className='h-4 w-4 mr-2' />
                  {isCreating ? 'Đang xử lý...' : 'Nhập kho'}
                </Button>
                <Button
                  type='button'
                  variant='outline'
                  className='w-full'
                  onClick={handleCancel}
                  disabled={isCreating}
                >
                  Hủy
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </form>
    </div>
  );
};
