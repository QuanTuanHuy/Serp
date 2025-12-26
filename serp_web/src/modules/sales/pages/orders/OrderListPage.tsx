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
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useGetOrdersQuery } from '../../api/salesApi';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { setOrderPagination, setOrderFilters } from '../../store';
import {
  selectOrderPagination,
  selectOrderFilters,
} from '../../store/selectors';
import type { Order, OrderStatus } from '../../types';
import { id } from 'date-fns/locale';

interface OrderListPageProps {
  className?: string;
}

const statusStyles = {
  CREATED: {
    label: 'Đã tạo',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
    icon: Clock,
  },
  APPROVED: {
    label: 'Đã duyệt',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
  },
  CANCELLED: {
    label: 'Đã hủy',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
  },
  FULLY_DELIVERED: {
    label: 'Đã giao hàng',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: Package,
  },
};

const formatCurrency = (value?: number) => {
  if (!value) return 'đ0';
  if (value >= 1000000) return `đ${(value / 1000000).toFixed(1)}M`;
  if (value >= 1000) return `đ${(value / 1000).toFixed(1)}K`;
  if (value >= 1000000000) return `đ${(value / 1000000000).toFixed(1)}B`;
  return `đ${value.toLocaleString()}`;
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

// Enhanced OrderCard component
const OrderCard = ({
  order,
  onClick,
}: {
  order: Order;
  onClick?: () => void;
}) => {
  const status =
    statusStyles[order.statusId as keyof typeof statusStyles] ||
    statusStyles.CREATED;
  const StatusIcon = status.icon;

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
                <ShoppingCart className='h-5 w-5' />
              </div>
              <div className='flex-1 min-w-0'>
                <h3 className='font-semibold text-foreground truncate'>
                  {order.orderName || `Order #${order.id?.slice(0, 8)}`}
                </h3>
                <p className='text-xs text-muted-foreground'>
                  {order.orderTypeId || 'Sales Order'}
                </p>
              </div>
            </div>

            <Badge
              variant='secondary'
              className={cn('gap-1', status.bg, status.text)}
            >
              <StatusIcon className='h-3 w-3' />
              <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
              {status.label}
            </Badge>
          </div>
        </div>

        {/* Order Info */}
        <div className='space-y-2 mb-4'>
          {order.orderDate && (
            <div className='flex items-center gap-2 text-sm'>
              <Calendar className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                {new Date(order.orderDate).toLocaleDateString()}
              </span>
            </div>
          )}
          {order.toCustomerId && (
            <div className='flex items-center gap-2 text-sm'>
              <User className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                Khách hàng: {order.toCustomerId.slice(0, 8)}...
              </span>
            </div>
          )}
          <div className='flex items-center gap-2 text-sm'>
            <Package className='h-4 w-4 text-muted-foreground shrink-0' />
            <span className='text-muted-foreground'>
              {order.totalQuantity || 0} sản phẩm
            </span>
          </div>
        </div>

        {/* Footer - Total */}
        <div className='flex items-center justify-between pt-3 border-t'>
          <div>
            <p className='text-xs text-muted-foreground'>Tổng thành tiền</p>
            <p className='text-lg font-bold text-foreground'>
              {formatCurrency(order.totalAmount)}
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

export const OrderListPage: React.FC<OrderListPageProps> = ({ className }) => {
  const router = useRouter();
  const dispatch = useAppDispatch();

  const pagination = useAppSelector(selectOrderPagination);
  const filters = useAppSelector(selectOrderFilters);

  const { data, isLoading, error } = useGetOrdersQuery({
    filters,
    pagination,
  });

  // Local state
  const [searchQuery, setSearchQuery] = useState(filters.query || '');
  const [statusFilter, setStatusFilter] = useState<OrderStatus | ''>('');
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  const orders = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  // Handle actions
  const handleSearch = () => {
    dispatch(setOrderFilters({ ...filters, query: searchQuery }));
    dispatch(setOrderPagination({ ...pagination, page: 0 }));
  };

  const handlePageChange = (newPage: number) => {
    dispatch(setOrderPagination({ ...pagination, page: newPage }));
  };

  const handleViewOrder = (orderId: string) => {
    router.push(`/sales/orders/${orderId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    dispatch(setOrderFilters({}));
    dispatch(setOrderPagination({ ...pagination, page: 0 }));
  };

  const hasActiveFilters = searchQuery || statusFilter;

  // Calculate stats
  const stats = useMemo(() => {
    return {
      total: totalItems,
      approved: orders.filter((o: Order) => o.statusId === 'APPROVED').length,
      delivered: orders.filter((o: Order) => o.statusId === 'FULLY_DELIVERED')
        .length,
      totalRevenue: orders
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
          <h1 className='text-2xl font-bold tracking-tight'>
            Danh sách đơn hàng
          </h1>
          <p className='text-muted-foreground'>
            Theo dõi và quản lý tất cả các đơn hàng của bạn
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <Button
            onClick={() => router.push('/sales/orders/new')}
            className='gap-2'
          >
            <Plus className='h-4 w-4' />
            Tạo đơn hàng
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Tổng đơn hàng'
          value={stats.total}
          icon={ShoppingCart}
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
          icon={Package}
          variant='success'
        />
        <StatsCard
          title='Doanh thu'
          value={formatCurrency(stats.totalRevenue)}
          icon={HandCoins}
          variant='warning'
        />
      </div>

      {/* Search & Filters Bar */}
      <div className='flex flex-col sm:flex-row gap-3'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Tìm kiếm đơn hàng theo tên, khách hàng...'
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
                  Trạng thái đơn hàng
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => {
                    const value = e.target.value as OrderStatus | '';
                    setStatusFilter(value);
                    dispatch(
                      setOrderFilters({
                        ...filters,
                        statusId: value || undefined,
                      })
                    );
                  }}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả trạng thái</option>
                  <option value='CREATED'>Nháp</option>
                  <option value='APPROVED'>Đã duyệt</option>
                  <option value='CANCELLED'>Đã hủy</option>
                  <option value='FULLY_DELIVERED'>Đã giao hàng</option>
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
              Đã xảy ra lỗi khi tải đơn hàng. Vui lòng thử lại sau.
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
                : 'Bắt đầu bằng cách tạo đơn hàng đầu tiên của bạn.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Xóa bộ lọc
              </Button>
            ) : (
              <Button onClick={() => router.push('/sales/orders/new')}>
                <Plus className='h-4 w-4 mr-2' />
                Tạo đơn hàng đầu tiên
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
            trong tổng số {totalItems} đơn hàng
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
