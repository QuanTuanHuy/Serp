/**
 * Order List Page - Enhanced
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Order management with modern UI
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
  Skeleton,
} from '@/shared/components/ui';
import {
  Search,
  Plus,
  SlidersHorizontal,
  ShoppingCart,
  Package,
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
  HandCoins,
  BanknoteArrowDown,
  TrendingUp,
  FileText,
  PackageCheck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useGetOrdersQuery, useGetCustomersQuery } from '../../api/salesApi';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { setOrderPagination, setOrderFilters } from '../../store';
import {
  selectOrderPagination,
  selectOrderFilters,
} from '../../store/selectors';
import type {
  Customer,
  Order,
  OrderFilters,
  OrderStatus,
  SaleChannel,
} from '../../types';
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

const StatsCard = ({
  title,
  value,
  icon: Icon,
  variant = 'default',
}: {
  title: string;
  value: number | string;
  icon: any;
  variant?:
    | 'default'
    | 'primary'
    | 'success'
    | 'warning'
    | 'danger'
    | 'completed';
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
    completed: {
      card: 'bg-gradient-to-br from-purple-50 to-purple-100/50 dark:from-purple-950/30 dark:to-purple-900/20 border-purple-200/50 dark:border-purple-800/30',
      icon: 'bg-purple-500 text-white shadow-purple-500/25',
      iconRing: 'ring-4 ring-purple-500/10',
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

interface OrderListPageProps {
  className?: string;
}

export const OrderListPage: React.FC<OrderListPageProps> = ({ className }) => {
  const SEARCH_PAGE_SIZE = 9;

  const router = useRouter();
  const dispatch = useAppDispatch();

  const pagination = useAppSelector(selectOrderPagination);
  const filters = useAppSelector(selectOrderFilters);

  // Local state
  const [searchQuery, setSearchQuery] = useState(filters.query || '');
  const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>(
    (filters.statusId as OrderStatus) || ''
  );
  const [customerFilter, setCustomerFilter] = useState(
    filters.toCustomerId || ''
  );
  const [saleChannelFilter, setSaleChannelFilter] = useState<SaleChannel | ''>(
    (filters.saleChannelId as SaleChannel) || ''
  );
  const [orderDateAfter, setOrderDateAfter] = useState(
    filters.orderDateAfter || ''
  );
  const [orderDateBefore, setOrderDateBefore] = useState(
    filters.orderDateBefore || ''
  );
  const [deliveryAfter, setDeliveryAfter] = useState(
    filters.deliveryAfter || ''
  );
  const [deliveryBefore, setDeliveryBefore] = useState(
    filters.deliveryBefore || ''
  );
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [hasSearched, setHasSearched] = useState(true);

  const { data, isLoading, error } = useGetOrdersQuery(
    {
      filters: {
        ...filters,
      },
      pagination: {
        ...pagination,
        size: SEARCH_PAGE_SIZE,
      },
    },
    {
      skip: !hasSearched,
    }
  );

  const orders = data?.data?.items || [];

  // Fetch customers
  const { data: customersResponse } = useGetCustomersQuery({
    filters: {},
    pagination: { page: 0, size: 100 },
  });

  // Create customer map for quick lookup
  const customerMap = useMemo(() => {
    const map = new Map();
    customersResponse?.data?.items?.forEach((customer) => {
      map.set(customer.id, customer);
    });
    return map;
  }, [customersResponse]);

  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  const buildOrderFilters = (
    overrides?: Partial<{
      query: string;
      statusId: OrderStatus | '';
      toCustomerId: string;
      saleChannelId: SaleChannel | '';
      orderDateAfter: string;
      orderDateBefore: string;
      deliveryAfter: string;
      deliveryBefore: string;
    }>
  ): OrderFilters => {
    const query = overrides?.query ?? searchQuery;
    const statusId = overrides?.statusId ?? statusFilter;
    const toCustomerId = overrides?.toCustomerId ?? customerFilter;
    const saleChannelId = overrides?.saleChannelId ?? saleChannelFilter;
    const nextOrderDateAfter = overrides?.orderDateAfter ?? orderDateAfter;
    const nextOrderDateBefore = overrides?.orderDateBefore ?? orderDateBefore;
    const nextDeliveryAfter = overrides?.deliveryAfter ?? deliveryAfter;
    const nextDeliveryBefore = overrides?.deliveryBefore ?? deliveryBefore;

    return {
      query: query || undefined,
      statusId: statusId || undefined,
      toCustomerId: toCustomerId || undefined,
      saleChannelId: saleChannelId || undefined,
      orderDateAfter: nextOrderDateAfter || undefined,
      orderDateBefore: nextOrderDateBefore || undefined,
      deliveryAfter: nextDeliveryAfter || undefined,
      deliveryBefore: nextDeliveryBefore || undefined,
    };
  };

  // Handle actions
  const handleSearch = () => {
    dispatch(setOrderFilters(buildOrderFilters()));
    dispatch(
      setOrderPagination({ ...pagination, page: 0, size: SEARCH_PAGE_SIZE })
    );
    setHasSearched(true);
  };

  const handlePageChange = (newPage: number) => {
    dispatch(
      setOrderPagination({
        ...pagination,
        page: newPage,
        size: SEARCH_PAGE_SIZE,
      })
    );
  };

  const handleViewOrder = (orderId: string) => {
    router.push(`/logistics/sale-orders/${orderId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    setCustomerFilter('');
    setSaleChannelFilter('');
    setOrderDateAfter('');
    setOrderDateBefore('');
    setDeliveryAfter('');
    setDeliveryBefore('');
  };

  const hasActiveFilters =
    !!searchQuery ||
    !!statusFilter ||
    !!customerFilter ||
    !!saleChannelFilter ||
    !!orderDateAfter ||
    !!orderDateBefore ||
    !!deliveryAfter ||
    !!deliveryBefore;

  const activeFilterCount =
    (searchQuery ? 1 : 0) +
    (statusFilter ? 1 : 0) +
    (customerFilter ? 1 : 0) +
    (saleChannelFilter ? 1 : 0) +
    (orderDateAfter ? 1 : 0) +
    (orderDateBefore ? 1 : 0) +
    (deliveryAfter ? 1 : 0) +
    (deliveryBefore ? 1 : 0);

  const shouldShowResults = hasSearched;

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

  if (isLoading) {
    return (
      <div className={className}>
        <div className='flex items-center justify-between mb-6'>
          <Skeleton className='h-10 w-48' />
          <Skeleton className='h-10 w-32' />
        </div>
        <Skeleton className='h-12 w-full mb-4' />
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className='h-64 w-full' />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className={cn('space-y-6', className)}>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Đơn bán hàng</h1>
          <p className='text-muted-foreground'>
            Quản lý đơn bán hàng đến các đại lý
          </p>
        </div>
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
            className='pl-10 pr-10'
          />
          {searchQuery && (
            <button
              onClick={() => {
                const nextQuery = '';
                setSearchQuery(nextQuery);
              }}
              className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
            >
              <X className='h-4 w-4' />
            </button>
          )}
        </div>

        <Button
          onClick={handleSearch}
          variant='secondary'
          className='transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md active:translate-y-0 active:scale-[0.98]'
        >
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
                  <option value='CREATED'>Chờ duyệt</option>
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
                  value={customerFilter}
                  onChange={(e) => setCustomerFilter(e.target.value)}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả khách hàng</option>
                  {(customersResponse?.data?.items || []).map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      {customer.name}
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

            <div className='mt-4 pt-4 border-t flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3'>
              <p className='text-sm text-muted-foreground'>
                Đã tìm thấy {totalItems} kết quả phù hợp
              </p>
              {hasActiveFilters && (
                <Button variant='ghost' size='sm' onClick={clearFilters}>
                  Xóa tất cả bộ lọc
                </Button>
              )}
            </div>

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

      {!shouldShowResults && (
        <Card>
          <CardContent className='py-10 text-center'>
            <p className='text-sm text-muted-foreground'>
              Bấm Tìm kiếm để tải dữ liệu đơn hàng.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Error State */}
      {shouldShowResults && error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4'>
            <p className='text-destructive'>
              Đã xảy ra lỗi khi tải dữ liệu đơn hàng. Vui lòng thử lại sau.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Loading State */}
      {shouldShowResults && isLoading && (
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
      {shouldShowResults && !isLoading && orders.length > 0 && (
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
              customer={customerMap.get(order.toCustomerId)}
              viewMode={viewMode}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {shouldShowResults && !isLoading && orders.length === 0 && !error && (
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
      {shouldShowResults && totalItems > SEARCH_PAGE_SIZE && (
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
