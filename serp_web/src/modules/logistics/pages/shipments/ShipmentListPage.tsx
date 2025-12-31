/**
 * Shipment List Page - Logistics Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shipment management with modern UI
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
  SlidersHorizontal,
  Package,
  Clock,
  CheckCircle2,
  Truck,
  X,
  ChevronLeft,
  ChevronRight,
  Calendar,
  User,
  Grid3X3,
  List,
  FileText,
  TrendingUp,
  PackageCheck,
  ArrowDownToLine,
  ArrowUpFromLine,
  Box,
  ReceiptText,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetOrdersQuery,
  useGetShipmentsQuery,
  useGetSuppliersQuery,
} from '../../api/logisticsApi';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { setShipmentPagination, setShipmentFilters } from '../../store';
import {
  selectShipmentPagination,
  selectShipmentFilters,
} from '../../store/selectors';
import type {
  Customer,
  Order,
  Shipment,
  ShipmentStatus,
  ShipmentType,
  Supplier,
} from '../../types';
import { ShipmentCard } from '../../components/cards/ShipmentCard';

interface ShipmentListPageProps {
  className?: string;
}

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

export const ShipmentListPage: React.FC<ShipmentListPageProps> = ({
  className,
}) => {
  const router = useRouter();
  const dispatch = useAppDispatch();

  const pagination = useAppSelector(selectShipmentPagination);
  const filters = useAppSelector(selectShipmentFilters);

  const { data, isLoading, error } = useGetShipmentsQuery({
    filters,
    pagination,
  });

  const shipments = data?.data?.items || [];

  // Collect unique order IDs
  const orderIds = useMemo(() => {
    return Array.from(
      new Set(shipments.map((s: Shipment) => s.orderId).filter(Boolean))
    );
  }, [shipments]);

  // Fetch orders
  const { data: ordersResponse } = useGetOrdersQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: orderIds.length === 0 }
  );

  // Create order map for quick lookup
  const orderMap = useMemo(() => {
    const map = new Map();
    ordersResponse?.data?.items?.forEach((order) => {
      map.set(order.id, order);
    });
    return map;
  }, [ordersResponse]);

  // Collect unique supplier IDs
  const supplierIds = useMemo(() => {
    return Array.from(
      new Set(shipments.map((s: Shipment) => s.fromSupplierId).filter(Boolean))
    );
  }, [shipments]);

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

  // Collect unique customer IDs
  const customerIds = useMemo(() => {
    return Array.from(
      new Set(shipments.map((s: Shipment) => s.toCustomerId).filter(Boolean))
    );
  }, [shipments]);

  // Fetch customers
  const { data: customersResponse } = useGetSuppliersQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: customerIds.length === 0 }
  );

  // Create customer map for quick lookup
  const customerMap = useMemo(() => {
    const map = new Map();
    customersResponse?.data?.items?.forEach((customer) => {
      map.set(customer.id, customer);
    });
    return map;
  }, [customersResponse]);

  const [searchQuery, setSearchQuery] = useState(filters.query || '');
  const [statusFilter, setStatusFilter] = useState<ShipmentStatus | ''>('');
  const [typeFilter, setTypeFilter] = useState<ShipmentType | ''>('');
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  const handleSearch = () => {
    dispatch(setShipmentFilters({ ...filters, query: searchQuery }));
    dispatch(setShipmentPagination({ ...pagination, page: 0 }));
  };

  const handlePageChange = (newPage: number) => {
    dispatch(setShipmentPagination({ ...pagination, page: newPage }));
  };

  const handleViewShipment = (shipmentId: string) => {
    router.push(`/logistics/shipments/${shipmentId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    setTypeFilter('');
    dispatch(setShipmentFilters({}));
    dispatch(setShipmentPagination({ ...pagination, page: 0 }));
  };

  const hasActiveFilters = searchQuery || statusFilter || typeFilter;

  const stats = useMemo(() => {
    return {
      total: totalItems,
      imported: shipments.filter((s: Shipment) => s.statusId === 'IMPORTED')
        .length,
      exported: shipments.filter((s: Shipment) => s.statusId === 'EXPORTED')
        .length,
      inbound: shipments.filter((s: Shipment) => s.shipmentTypeId === 'INBOUND')
        .length,
    };
  }, [totalItems, shipments]);

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Phiếu nhập xuất</h1>
          <p className='text-muted-foreground'>Quản lý phiếu nhập xuất kho</p>
        </div>
      </div>

      <div className='grid grid-cols-2 sm:grid-cols-3 gap-4'>
        <StatsCard
          title='Tổng lô hàng'
          value={stats.total}
          icon={FileText}
          variant='primary'
        />
        <StatsCard
          title='Đã nhập kho'
          value={stats.imported}
          icon={ArrowDownToLine}
          variant='success'
        />
        <StatsCard
          title='Đã xuất kho'
          value={stats.exported}
          icon={ArrowUpFromLine}
          variant='warning'
        />
      </div>

      <div className='flex flex-col sm:flex-row gap-3'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Tìm kiếm phiếu nhập xuất...'
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
                    const value = e.target.value as ShipmentStatus | '';
                    setStatusFilter(value);
                    dispatch(
                      setShipmentFilters({
                        ...filters,
                        statusId: value || undefined,
                      })
                    );
                  }}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả trạng thái</option>
                  <option value='CREATED'>Đã tạo</option>
                  <option value='IMPORTED'>Đã nhập kho</option>
                  <option value='EXPORTED'>Đã xuất kho</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Loại phiếu
                </label>
                <select
                  value={typeFilter}
                  onChange={(e) => {
                    const value = e.target.value as ShipmentType | '';
                    setTypeFilter(value);
                    dispatch(
                      setShipmentFilters({
                        ...filters,
                        shipmentTypeId: value || undefined,
                      })
                    );
                  }}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả loại</option>
                  <option value='INBOUND'>Nhập kho</option>
                  <option value='OUTBOUND'>Xuất kho</option>
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

      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4'>
            <p className='text-destructive'>
              Đã xảy ra lỗi khi tải dữ liệu phiếu nhập xuất. Vui lòng thử lại
              sau.
            </p>
          </CardContent>
        </Card>
      )}

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

      {!isLoading && shipments.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {shipments.map((shipment: Shipment) => (
            <ShipmentCard
              key={shipment.id}
              shipment={shipment}
              onClick={() => handleViewShipment(shipment.id)}
              supplier={supplierMap.get(shipment.fromSupplierId || '')}
              customer={customerMap.get(shipment.toCustomerId || '')}
              order={orderMap.get(shipment.orderId)}
            />
          ))}
        </div>
      )}

      {!isLoading && shipments.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Truck className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>
              Không tìm thấy phiếu nhập xuất nào
            </h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Thử điều chỉnh bộ lọc để xem thêm kết quả.'
                : 'Chưa có phiếu nhập xuất nào được tạo.'}
            </p>
            {hasActiveFilters && (
              <Button variant='outline' onClick={clearFilters}>
                Xóa bộ lọc
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {totalItems > (pagination.size || 10) && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Hiển thị {currentPage * (pagination.size || 10) + 1} đến{' '}
            {Math.min((currentPage + 1) * (pagination.size || 10), totalItems)}{' '}
            trong tổng số {totalItems} phiếu nhập xuất
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
