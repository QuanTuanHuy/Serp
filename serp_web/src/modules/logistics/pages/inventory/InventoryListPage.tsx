/**
 * Inventory List Page - Logistics Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Inventory management with modern UI
 */

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Button,
  Card,
  CardContent,
  Input,
  Badge,
} from '@/shared/components/ui';
import {
  Search,
  Plus,
  SlidersHorizontal,
  Package,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  X,
  ChevronLeft,
  ChevronRight,
  Calendar,
  MapPin,
  Grid3X3,
  List,
  Box,
  Boxes,
  TriangleAlert,
  ShieldX,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetInventoryItemsQuery,
  useGetProductsQuery,
  useGetFacilitiesQuery,
} from '../../api/logisticsApi';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import {
  setInventoryItemPagination,
  setInventoryItemFilters,
} from '../../store';
import {
  selectInventoryItemPagination,
  selectInventoryItemFilters,
} from '../../store/selectors';
import type {
  Facility,
  InventoryItem,
  InventoryItemStatus,
  Product,
} from '../../types';

interface InventoryListPageProps {
  className?: string;
}

const statusStyles = {
  VALID: {
    label: 'Hợp lệ',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
  },
  EXPIRED: {
    label: 'Đã hết hạn',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
  },
  DAMAGED: {
    label: 'Hư hỏng',
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    dot: 'bg-amber-500',
    icon: AlertTriangle,
  },
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN');
};

// Enhanced StatsCard component
const StatsCard = ({
  title,
  value,
  icon: Icon,
  variant = 'default',
}: {
  title: string;
  value: number | string;
  icon: any;
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger';
}) => {
  const variantStyles = {
    default: {
      card: 'bg-card hover:bg-card/90 border-border',
      icon: 'bg-muted text-muted-foreground',
      iconRing: '',
    },
    primary: {
      card: 'bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/30 dark:to-blue-900/20 border-blue-200/50 dark:border-blue-800/30',
      icon: 'bg-blue-500 text-white shadow-blue-500/25',
      iconRing: 'ring-4 ring-blue-500/10',
    },
    success: {
      card: 'bg-gradient-to-br from-emerald-50 to-emerald-100/50 dark:from-emerald-950/30 dark:to-emerald-900/20 border-emerald-200/50 dark:border-emerald-800/30',
      icon: 'bg-emerald-500 text-white shadow-emerald-500/25',
      iconRing: 'ring-4 ring-emerald-500/10',
    },
    warning: {
      card: 'bg-gradient-to-br from-amber-50 to-amber-100/50 dark:from-amber-950/30 dark:to-amber-900/20 border-amber-200/50 dark:border-amber-800/30',
      icon: 'bg-amber-500 text-white shadow-amber-500/25',
      iconRing: 'ring-4 ring-amber-500/10',
    },
    danger: {
      card: 'bg-gradient-to-br from-rose-50 to-rose-100/50 dark:from-rose-950/30 dark:to-rose-900/20 border-rose-200/50 dark:border-rose-800/30',
      icon: 'bg-rose-500 text-white shadow-rose-500/25',
      iconRing: 'ring-4 ring-rose-500/10',
    },
  };

  const styles = variantStyles[variant];

  return (
    <Card
      className={cn(
        'group relative overflow-hidden p-5 shadow-sm transition-all duration-200 hover:shadow-md cursor-pointer',
        styles.card
      )}
    >
      {/* Background Pattern */}
      <div className='absolute inset-0 opacity-5 dark:opacity-10'>
        <svg
          className='absolute -right-8 -top-8 h-32 w-32 text-current'
          viewBox='0 0 100 100'
        >
          <circle
            cx='50'
            cy='50'
            r='40'
            fill='currentColor'
            fillOpacity='0.3'
          />
        </svg>
      </div>

      <div className='relative flex items-start justify-between gap-4'>
        <div className='space-y-1 min-w-0 flex-1'>
          <p className='text-sm font-medium text-muted-foreground truncate'>
            {title}
          </p>
          <p className='text-2xl font-bold tracking-tight truncate'>{value}</p>
        </div>

        <div
          className={cn(
            'flex h-12 w-12 shrink-0 items-center justify-center rounded-xl shadow-lg transition-transform duration-200 group-hover:scale-105',
            styles.icon,
            styles.iconRing
          )}
        >
          <Icon className='h-6 w-6' />
        </div>
      </div>
    </Card>
  );
};

// Enhanced InventoryCard component
const InventoryCard = ({
  item,
  onClick,
  product,
  facility,
}: {
  item: InventoryItem;
  onClick?: () => void;
  product?: Product;
  facility?: Facility;
}) => {
  const status =
    statusStyles[item.statusId as keyof typeof statusStyles] ||
    statusStyles.VALID;
  const StatusIcon = status.icon;

  const isExpiringSoon =
    item.expirationDate &&
    new Date(item.expirationDate) <
      new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) && // within 30 days
    new Date(item.expirationDate) >= new Date(); // not yet expired

  const isExpired =
    item.expirationDate && new Date(item.expirationDate) < new Date(); // already expired

  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer'
      )}
      onClick={onClick}
    >
      {/* Header with gradient accent */}
      <div className='relative h-2 bg-gradient-to-r from-primary/60 via-primary/40 to-primary/20' />

      <CardContent className='p-5'>
        {/* Header */}
        <div className='flex items-start justify-between mb-4'>
          <div className='flex-1 min-w-0'>
            <div className='flex items-center gap-2 mb-2'>
              <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 text-primary'>
                <Package className='h-5 w-5' />
              </div>
              <div className='flex-1 min-w-0'>
                <h3 className='font-semibold text-foreground truncate'>
                  {product?.name || item.productId.slice(0, 8)}
                </h3>
                <p className='text-xs text-muted-foreground'>
                  Lô hàng: {item.lotId}
                </p>
              </div>
            </div>

            <div className='flex items-center gap-2 flex-wrap'>
              <Badge
                variant='secondary'
                className={cn('gap-1', status.bg, status.text)}
              >
                <StatusIcon className='h-3 w-3' />
                <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
                {status.label}
              </Badge>
              {isExpiringSoon && (
                <Badge
                  variant='secondary'
                  className='gap-1 bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400'
                >
                  <AlertTriangle className='h-3 w-3' />
                  Sắp hết hạn
                </Badge>
              )}
              {isExpired && (
                <Badge
                  variant='secondary'
                  className='gap-1 bg-rose-100 dark:bg-rose-900/30 text-rose-700 dark:text-rose-400'
                >
                  <XCircle className='h-3 w-3' />
                  Đã hết hạn
                </Badge>
              )}
            </div>
          </div>
        </div>

        {/* Inventory Info */}
        <div className='space-y-2 mb-4'>
          <div className='flex items-center justify-between text-sm'>
            <span className='text-muted-foreground'>Tồn kho thực:</span>
            <span className='font-semibold text-purple-700 dark:text-purple-400'>
              {item.quantityOnHand} {product?.unit || ''}
            </span>
          </div>

          <div className='flex items-center gap-2 text-sm pt-2 border-t'>
            <MapPin className='h-4 w-4 text-muted-foreground shrink-0' />
            <span className='truncate text-muted-foreground'>
              {facility?.name || item.facilityId.slice(0, 8)}
            </span>
          </div>

          {item.manufacturingDate && (
            <div className='flex items-center gap-2 text-sm'>
              <Calendar className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                NSX: {formatDate(item.manufacturingDate)}
              </span>
            </div>
          )}

          {item.expirationDate && (
            <div className='flex items-center gap-2 text-sm'>
              <Calendar className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                HSD: {formatDate(item.expirationDate)}
              </span>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className='flex items-center justify-between pt-3 border-t'>
          <div>
            <p className='text-xs text-muted-foreground'>Khả dụng</p>
            <p className='text-lg font-bold text-emerald-600 dark:text-emerald-400'>
              {item.quantityOnHand -
                item.quantityCommitted -
                item.quantityReserved}{' '}
              {product?.unit || ''}
            </p>
          </div>

          <Button
            variant='ghost'
            size='sm'
            className='opacity-0 group-hover:opacity-100 transition-opacity'
            onClick={(e) => {
              e.stopPropagation();
              onClick?.();
            }}
          >
            Xem chi tiết →
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export const InventoryListPage: React.FC<InventoryListPageProps> = ({
  className,
}) => {
  const router = useRouter();
  const dispatch = useAppDispatch();

  const pagination = useAppSelector(selectInventoryItemPagination);
  const filters = useAppSelector(selectInventoryItemFilters);

  const { data, isLoading, error } = useGetInventoryItemsQuery({
    filters,
    pagination,
  });

  // Local state
  const [searchQuery, setSearchQuery] = useState(filters.query || '');
  const [statusFilter, setStatusFilter] = useState<InventoryItemStatus | ''>(
    ''
  );
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  const items = data?.data?.items || [];

  // Collect unique product and facility IDs
  const productIds = useMemo(() => {
    return Array.from(
      new Set(items.map((i: InventoryItem) => i.productId).filter(Boolean))
    );
  }, [items]);

  const facilityIds = useMemo(() => {
    return Array.from(
      new Set(items.map((i: InventoryItem) => i.facilityId).filter(Boolean))
    );
  }, [items]);

  // Fetch products and facilities
  const { data: productsResponse } = useGetProductsQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: productIds.length === 0 }
  );

  const { data: facilitiesResponse } = useGetFacilitiesQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: facilityIds.length === 0 }
  );

  // Create maps for quick lookup
  const productMap = useMemo(() => {
    const map = new Map();
    productsResponse?.data?.items?.forEach((product) => {
      map.set(product.id, product);
    });
    return map;
  }, [productsResponse]);

  const facilityMap = useMemo(() => {
    const map = new Map();
    facilitiesResponse?.data?.items?.forEach((facility) => {
      map.set(facility.id, facility);
    });
    return map;
  }, [facilitiesResponse]);

  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  // Handle actions
  const handleSearch = () => {
    dispatch(setInventoryItemFilters({ ...filters, query: searchQuery }));
    dispatch(setInventoryItemPagination({ ...pagination, page: 0 }));
  };

  const handlePageChange = (newPage: number) => {
    dispatch(setInventoryItemPagination({ ...pagination, page: newPage }));
  };

  const handleViewItem = (itemId: string) => {
    router.push(`/logistics/inventory/${itemId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    dispatch(setInventoryItemFilters({}));
    dispatch(setInventoryItemPagination({ ...pagination, page: 0 }));
  };

  const hasActiveFilters = searchQuery || statusFilter;

  // Calculate stats
  const stats = useMemo(() => {
    const totalOnHand = items.reduce(
      (sum: number, i: InventoryItem) => sum + (i.quantityOnHand || 0),
      0
    );
    const totalExpiringSoon = items.reduce(
      (sum: number, i: InventoryItem) =>
        sum +
        (i.expirationDate &&
        new Date(i.expirationDate) <
          new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) &&
        new Date(i.expirationDate) >= new Date()
          ? 1
          : 0),
      0
    );
    const totalExpired = items.reduce(
      (sum: number, i: InventoryItem) =>
        sum +
        (i.expirationDate && new Date(i.expirationDate) < new Date() ? 1 : 0),
      0
    );

    return {
      totalItems,
      totalOnHand,
      totalExpiringSoon,
      totalExpired,
    };
  }, [totalItems, items]);

  return (
    <div className={cn('space-y-6', className)}>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Quản lý tồn kho</h1>
          <p className='text-muted-foreground'>
            Theo dõi và quản lý tất cả các mặt hàng trong kho
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <Button
            onClick={() => router.push('/logistics/inventory/new')}
            className='gap-2'
          >
            <Plus className='h-4 w-4' />
            Nhập kho
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Tổng mặt hàng'
          value={stats.totalItems}
          icon={Boxes}
          variant='primary'
        />
        <StatsCard
          title='Số lượng tồn kho'
          value={stats.totalOnHand}
          icon={Package}
          variant='success'
        />
        <StatsCard
          title='Sắp hết hạn'
          value={stats.totalExpiringSoon}
          icon={TriangleAlert}
          variant='warning'
        />
        <StatsCard
          title='Đã hết hạn'
          value={stats.totalExpired}
          icon={ShieldX}
          variant='danger'
        />
      </div>

      {/* Search & Filters Bar */}
      <div className='flex flex-col sm:flex-row gap-3'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Tìm kiếm theo sản phẩm, lot...'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            className='pl-10 pr-10'
          />
          {searchQuery && (
            <button
              onClick={() => {
                setSearchQuery('');
                handleSearch();
              }}
              className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
            >
              <X className='h-4 w-4' />
            </button>
          )}
        </div>

        <Button onClick={handleSearch} variant='secondary'>
          <Search className='h-4 w-4 mr-2' />
          Tìm kiếm
        </Button>

        {/* Filter Toggle */}
        <Button
          variant={showFilters ? 'secondary' : 'outline'}
          onClick={() => setShowFilters(!showFilters)}
          className='gap-2'
        >
          <SlidersHorizontal className='h-4 w-4' />
          Bộ lọc
          {hasActiveFilters && (
            <span className='h-2 w-2 rounded-full bg-primary' />
          )}
        </Button>

        {/* View Toggle */}
        <div className='flex rounded-lg border bg-muted p-1'>
          <button
            onClick={() => setViewMode('grid')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'grid'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
          >
            <Grid3X3 className='h-4 w-4' />
          </button>
          <button
            onClick={() => setViewMode('list')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'list'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
          >
            <List className='h-4 w-4' />
          </button>
        </div>
      </div>

      {/* Expanded Filters */}
      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 sm:grid-cols-2 gap-4'>
              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Trạng thái
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => {
                    const value = e.target.value as InventoryItemStatus | '';
                    setStatusFilter(value);
                    dispatch(
                      setInventoryItemFilters({
                        ...filters,
                        statusId: value || undefined,
                      })
                    );
                  }}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả trạng thái</option>
                  <option value='VALID'>Hợp lệ</option>
                  <option value='EXPIRED'>Đã hết hạn</option>
                  <option value='DAMAGED'>Hư hỏng</option>
                </select>
              </div>
            </div>

            {hasActiveFilters && (
              <div className='mt-4 pt-4 border-t flex items-center justify-between'>
                <p className='text-sm text-muted-foreground'>
                  Đã tìm thấy {totalItems} kết quả phù hợp
                </p>
                <Button variant='ghost' size='sm' onClick={clearFilters}>
                  Xóa tất cả bộ lọc
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Error State */}
      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4'>
            <p className='text-destructive'>
              Đã xảy ra lỗi khi tải dữ liệu tồn kho. Vui lòng thử lại sau.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Loading State */}
      {isLoading && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {Array.from({ length: 6 }).map((_, index) => (
            <Card key={index} className='animate-pulse'>
              <CardContent className='p-5'>
                <div className='flex items-center gap-3 mb-4'>
                  <div className='h-10 w-10 bg-muted rounded-lg' />
                  <div className='flex-1'>
                    <div className='h-4 bg-muted rounded w-3/4 mb-2' />
                    <div className='h-3 bg-muted rounded w-1/2' />
                  </div>
                </div>
                <div className='space-y-2'>
                  <div className='h-3 bg-muted rounded w-full' />
                  <div className='h-3 bg-muted rounded w-2/3' />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Inventory Grid/List */}
      {!isLoading && items.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {items.map((item: InventoryItem) => (
            <InventoryCard
              key={item.id}
              item={item}
              onClick={() => handleViewItem(item.id)}
              product={productMap.get(item.productId)}
              facility={facilityMap.get(item.facilityId)}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && items.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Box className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>
              Không tìm thấy mặt hàng
            </h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Thử điều chỉnh bộ lọc để xem thêm kết quả.'
                : 'Bắt đầu bằng cách nhập hàng vào kho.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Xóa bộ lọc
              </Button>
            ) : (
              <Button onClick={() => router.push('/logistics/inventory/new')}>
                <Plus className='h-4 w-4 mr-2' />
                Nhập kho
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pagination */}
      {totalItems > (pagination.size || 10) && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Hiển thị {currentPage * (pagination.size || 10) + 1} đến{' '}
            {Math.min((currentPage + 1) * (pagination.size || 10), totalItems)}{' '}
            trong tổng số {totalItems} mặt hàng
          </p>
          <div className='flex items-center gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === 0}
              onClick={() => handlePageChange(currentPage - 1)}
            >
              <ChevronLeft className='h-4 w-4' />
              Trước
            </Button>
            <div className='flex items-center gap-1'>
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                const pageNum = i;
                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={cn(
                      'h-8 w-8 rounded-md text-sm font-medium transition-colors',
                      currentPage === pageNum
                        ? 'bg-primary text-primary-foreground'
                        : 'hover:bg-muted'
                    )}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
            </div>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage >= totalPages - 1}
              onClick={() => handlePageChange(currentPage + 1)}
            >
              Tiếp
              <ChevronRight className='h-4 w-4' />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
