/**
 * Order List Page - Purchase Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Purchase order management with modern UI
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
  ShoppingCart,
  Clock,
  CheckCircle2,
  XCircle,
  DollarSign,
  X,
  ChevronLeft,
  ChevronRight,
  Calendar,
  User,
  Grid3X3,
  List,
  Truck,
  FileText,
  TrendingUp,
  PackageCheck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useGetOrdersQuery, useGetSuppliersQuery } from '../../api/purchaseApi';
import type { Supplier, Order, OrderStatus, SaleChannel } from '../../types';
import { OrderCard } from '../../components/cards/OrderCard';

interface OrderListPageProps {
  className?: string;
}

const formatCurrency = (value?: number) => {
  if (!value) return 'đ0';
  if (value >= 1000000) return `đ${(value / 1000000).toFixed(1)}M`;
  if (value >= 1000) return `đ${(value / 1000).toFixed(1)}K`;
  if (value >= 1000000000) return `đ${(value / 1000000000).toFixed(1)}B`;
  return `đ${value.toLocaleString()}`;
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN');
};

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

export const OrderListPage: React.FC<OrderListPageProps> = ({ className }) => {
  const router = useRouter();
  const SEARCH_PAGE_SIZE = 9;

  // Local state
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>('');
  const [supplierFilter, setSupplierFilter] = useState('');
  const [saleChannelFilter, setSaleChannelFilter] = useState<SaleChannel | ''>(
    ''
  );
  const [orderDateAfter, setOrderDateAfter] = useState('');
  const [orderDateBefore, setOrderDateBefore] = useState('');
  const [deliveryAfter, setDeliveryAfter] = useState('');
  const [deliveryBefore, setDeliveryBefore] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [pagination, setPagination] = useState({
    page: 0,
    size: SEARCH_PAGE_SIZE,
  });

  const { data, isLoading, error } = useGetOrdersQuery({
    filters: {
      query: searchQuery || undefined,
      statusId: statusFilter || undefined,
      fromSupplierId: supplierFilter || undefined,
      saleChannelId: saleChannelFilter || undefined,
      orderDateAfter: orderDateAfter || undefined,
      orderDateBefore: orderDateBefore || undefined,
      deliveryAfter: deliveryAfter || undefined,
      deliveryBefore: deliveryBefore || undefined,
    },
    pagination: {
      ...pagination,
      size: SEARCH_PAGE_SIZE,
    },
  });

  const orders = data?.data?.items || [];

  // Collect unique supplier IDs
  const supplierIds = useMemo(() => {
    return Array.from(
      new Set(orders.map((o: Order) => o.fromSupplierId).filter(Boolean))
    );
  }, [orders]);

  // Fetch suppliers
  const { data: suppliersResponse } = useGetSuppliersQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: supplierIds.length === 0 }
  );

  // Create supplier map for quick lookup
  const supplierMap = useMemo(() => {
    const map = new Map();
    suppliersResponse?.data?.items?.forEach((supplier) => {
      map.set(supplier.id, supplier);
    });
    return map;
  }, [suppliersResponse]);

  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  // Handle actions
  const handleSearch = () => {
    setPagination({ ...pagination, page: 0, size: SEARCH_PAGE_SIZE });
  };

  const handlePageChange = (newPage: number) => {
    setPagination({ ...pagination, page: newPage, size: SEARCH_PAGE_SIZE });
  };

  const handleViewOrder = (orderId: string) => {
    router.push(`/purchase/purchase-orders/${orderId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    setSupplierFilter('');
    setSaleChannelFilter('');
    setOrderDateAfter('');
    setOrderDateBefore('');
    setDeliveryAfter('');
    setDeliveryBefore('');
    setPagination({ ...pagination, page: 0, size: SEARCH_PAGE_SIZE });
  };

  const hasActiveFilters =
    !!searchQuery ||
    !!statusFilter ||
    !!supplierFilter ||
    !!saleChannelFilter ||
    !!orderDateAfter ||
    !!orderDateBefore ||
    !!deliveryAfter ||
    !!deliveryBefore;

  const activeFilterCount =
    (searchQuery ? 1 : 0) +
    (statusFilter ? 1 : 0) +
    (supplierFilter ? 1 : 0) +
    (saleChannelFilter ? 1 : 0) +
    (orderDateAfter ? 1 : 0) +
    (orderDateBefore ? 1 : 0) +
    (deliveryAfter ? 1 : 0) +
    (deliveryBefore ? 1 : 0);

  // Calculate stats
  const stats = useMemo(() => {
    return {
      total: totalItems,
      approved: orders.filter((o: Order) => o.statusId === 'APPROVED').length,
      delivered: orders.filter((o: Order) => o.statusId === 'FULLY_DELIVERED')
        .length,
      totalValue: orders
        .filter(
          (o: Order) =>
            o.statusId === 'APPROVED' || o.statusId === 'FULLY_DELIVERED'
        )
        .reduce((sum: number, o: Order) => sum + (o.totalAmount || 0), 0),
    };
  }, [totalItems, orders]);

  return (
    <div className={cn('space-y-6', className)}>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Đơn đặt hàng</h1>
          <p className='text-muted-foreground'>
            Quản lý đơn đặt hàng từ nhà cung cấp
          </p>
        </div>
        <Button onClick={() => router.push('/purchase/purchase-orders/new')}>
          <Plus className='h-4 w-4 mr-2' />
          Tạo đơn hàng mới
        </Button>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Tổng đơn hàng'
          value={stats.total}
          icon={FileText}
          variant='primary'
        />
        <StatsCard
          title='Đã duyệt'
          value={stats.approved}
          icon={CheckCircle2}
          variant='success'
        />
        <StatsCard
          title='Đã giao hàng'
          value={stats.delivered}
          icon={PackageCheck}
          variant='warning'
        />
        <StatsCard
          title='Tổng giá trị'
          value={formatCurrency(stats.totalValue)}
          icon={DollarSign}
          variant='success'
        />
      </div>

      {/* Search & Filters Bar */}
      <div className='flex flex-col sm:flex-row gap-3'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Tìm kiếm đơn hàng, NCC...'
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
            <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4'>
              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Trạng thái đơn hàng
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => {
                    const value = e.target.value as OrderStatus | '';
                    setStatusFilter(value);
                  }}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả trạng thái</option>
                  <option value='CREATED'>Chờ phê duyệt</option>
                  <option value='APPROVED'>Đã duyệt</option>
                  <option value='CANCELLED'>Đã hủy</option>
                  <option value='FULLY_DELIVERED'>Đã giao hàng</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Kênh bán
                </label>
                <select
                  value={saleChannelFilter}
                  onChange={(e) =>
                    setSaleChannelFilter(e.target.value as SaleChannel | '')
                  }
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả kênh</option>
                  <option value='ONLINE'>Online</option>
                  <option value='PARTNER'>Partner</option>
                  <option value='RETAIL'>Retail</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Nhà cung cấp
                </label>
                <select
                  value={supplierFilter}
                  onChange={(e) => setSupplierFilter(e.target.value)}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả nhà cung cấp</option>
                  {(suppliersResponse?.data?.items || []).map((supplier) => (
                    <option key={supplier.id} value={supplier.id}>
                      {supplier.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Ngày đặt
                </label>
                <div className='grid grid-cols-2 gap-2'>
                  <div>
                    <p className='mb-1 text-xs text-muted-foreground'>
                      Từ ngày
                    </p>
                    <Input
                      type='date'
                      value={orderDateAfter}
                      onChange={(e) => setOrderDateAfter(e.target.value)}
                    />
                  </div>

                  <div>
                    <p className='mb-1 text-xs text-muted-foreground'>
                      Đến ngày
                    </p>
                    <Input
                      type='date'
                      value={orderDateBefore}
                      onChange={(e) => setOrderDateBefore(e.target.value)}
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Ngày giao
                </label>
                <div className='grid grid-cols-2 gap-2'>
                  <div>
                    <p className='mb-1 text-xs text-muted-foreground'>
                      Từ ngày
                    </p>
                    <Input
                      type='date'
                      value={deliveryAfter}
                      onChange={(e) => setDeliveryAfter(e.target.value)}
                    />
                  </div>

                  <div>
                    <p className='mb-1 text-xs text-muted-foreground'>
                      Đến ngày
                    </p>
                    <Input
                      type='date'
                      value={deliveryBefore}
                      onChange={(e) => setDeliveryBefore(e.target.value)}
                    />
                  </div>
                </div>
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

            {hasActiveFilters && (
              <div className='mt-2'>
                <p className='text-sm text-muted-foreground'>
                  Đang bật {activeFilterCount} bộ lọc
                </p>
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
              Đã xảy ra lỗi khi tải dữ liệu đơn hàng. Vui lòng thử lại sau.
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

      {/* Order Grid/List */}
      {!isLoading && orders.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {orders.map((order: Order) => (
            <OrderCard
              key={order.id}
              order={order}
              onClick={() => handleViewOrder(order.id)}
              supplier={supplierMap.get(order.fromSupplierId)}
              viewMode={viewMode}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && orders.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <ShoppingCart className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>
              Không tìm thấy đơn hàng
            </h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Thử điều chỉnh bộ lọc để xem thêm kết quả.'
                : 'Bắt đầu bằng cách tạo đơn đặt hàng mới.'}
            </p>
            {hasActiveFilters && (
              <Button variant='outline' onClick={clearFilters}>
                Xóa bộ lọc
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pagination */}
      {totalItems > SEARCH_PAGE_SIZE && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Hiển thị {currentPage * SEARCH_PAGE_SIZE + 1} đến{' '}
            {Math.min((currentPage + 1) * SEARCH_PAGE_SIZE, totalItems)} trong
            tổng số {totalItems} đơn hàng
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
