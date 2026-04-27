'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Card, CardContent, Input } from '@/shared/components/ui';
import {
  ChevronLeft,
  ChevronRight,
  FileText,
  Grid3X3,
  List,
  Package,
  Search,
  SlidersHorizontal,
  Truck,
  X,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetCustomersQuery,
  useGetFacilitiesQuery,
  useGetOrdersQuery,
  useGetOutboundShipmentsQuery,
} from '../../api/logistics2Api';
import type {
  Customer,
  Order,
  OutboundShipment,
  OutboundShipmentFilters,
  OutboundShipmentStatus,
  PaginationParams,
} from '../../types';
import { OutboundShipmentCard } from '../../components/cards/OutboundShipmentCard';
import { StatsCard } from '../../components/cards/StatsCard';

interface OutboundShipmentListPageProps {
  className?: string;
}

const STATUS_OPTIONS: Array<{ value: OutboundShipmentStatus; label: string }> =
  [
    { value: 'CREATED', label: 'Nháp' },
    { value: 'READY_TO_EXPORT', label: 'Sẵn sàng xuất' },
    { value: 'DELIVERED', label: 'Đã xuất' },
  ];

export const OutboundShipmentListPage: React.FC<
  OutboundShipmentListPageProps
> = ({ className }) => {
  const router = useRouter();

  const [filters, setFilters] = useState<OutboundShipmentFilters>({});
  const [pagination, setPagination] = useState<PaginationParams>({
    page: 0,
    size: 9,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  });

  const [searchValue, setSearchValue] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  const { data, isLoading, error, refetch } = useGetOutboundShipmentsQuery({
    filters,
    pagination,
  });

  const shipments = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  const { data: ordersResponse } = useGetOrdersQuery({
    filters: {},
    pagination: { page: 0, size: 100 },
  });

  const { data: customersResponse } = useGetCustomersQuery({
    filters: {},
    pagination: { page: 0, size: 100 },
  });

  const orderMap = useMemo(() => {
    const map = new Map<string, Order>();
    ordersResponse?.data?.items?.forEach((order) => {
      map.set(order.id, order);
    });
    return map;
  }, [ordersResponse]);

  const customerMap = useMemo(() => {
    const map = new Map<string, Customer>();
    customersResponse?.data?.items?.forEach((customer) => {
      map.set(customer.id, customer);
    });
    return map;
  }, [customersResponse]);

  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    filters: {},
    pagination: { page: 0, size: 100 },
  });

  const stats = useMemo(() => {
    const created = shipments.filter(
      (item) => item.status === 'CREATED'
    ).length;
    const ready = shipments.filter(
      (item) => item.status === 'READY_TO_EXPORT'
    ).length;
    const delivered = shipments.filter(
      (item) => item.status === 'DELIVERED'
    ).length;

    return {
      created,
      ready,
      delivered,
    };
  }, [shipments]);

  const handleSearch = () => {
    setFilters((prev) => ({
      ...prev,
      query: searchValue.trim() || undefined,
    }));
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const handlePageChange = (page: number) => {
    setPagination((prev) => ({ ...prev, page }));
  };

  const clearFilters = () => {
    setSearchValue('');
    setFilters({});
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const hasActiveFilters = Boolean(
    filters.query || filters.status || filters.facilityId
  );

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Phiếu xuất kho</h1>
          <p className='text-muted-foreground'>Danh sách phiếu xuất kho</p>
        </div>

        <Button variant='outline' onClick={() => refetch()}>
          Làm mới
        </Button>
      </div>

      <div className='grid grid-cols-1 gap-4 sm:grid-cols-3'>
        <StatsCard
          title='Phiếu nháp'
          value={stats.created}
          icon={FileText}
          variant='primary'
        />

        <StatsCard
          title='Sẵn sàng xuất'
          value={stats.ready}
          icon={Truck}
          variant='success'
        />

        <StatsCard
          title='Đã giao'
          value={stats.delivered}
          icon={Package}
          variant='danger'
        />
      </div>

      <div className='flex flex-col gap-3 sm:flex-row'>
        <div className='relative flex-1'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={searchValue}
            placeholder='Tìm theo tên phiếu, orderId...'
            onChange={(event) => setSearchValue(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                handleSearch();
              }
            }}
            className='pl-10 pr-10'
          />
          {searchValue && (
            <button
              type='button'
              className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
              onClick={() => {
                setSearchValue('');
                setFilters((prev) => ({ ...prev, query: undefined }));
                setPagination((prev) => ({ ...prev, page: 0 }));
              }}
            >
              <X className='h-4 w-4' />
            </button>
          )}
        </div>

        <Button variant='secondary' onClick={handleSearch}>
          <Search className='mr-2 h-4 w-4' />
          Tìm kiếm
        </Button>

        <Button
          variant={showFilters ? 'secondary' : 'outline'}
          onClick={() => setShowFilters((prev) => !prev)}
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
            type='button'
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
            type='button'
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
          <CardContent className='grid gap-4 p-4 sm:grid-cols-2'>
            <div>
              <label className='mb-1.5 block text-sm font-medium'>
                Trạng thái
              </label>
              <select
                value={filters.status || ''}
                onChange={(event) => {
                  const nextStatus =
                    (event.target.value as OutboundShipmentStatus) || undefined;
                  setFilters((prev) => ({ ...prev, status: nextStatus }));
                  setPagination((prev) => ({ ...prev, page: 0 }));
                }}
                className='w-full rounded-lg border bg-background px-3 py-2'
              >
                <option value=''>Tất cả</option>
                {STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className='mb-1.5 block text-sm font-medium'>
                Kho xuất
              </label>
              <select
                value={filters.facilityId || ''}
                onChange={(event) => {
                  const nextFacilityId = event.target.value || undefined;
                  setFilters((prev) => ({
                    ...prev,
                    facilityId: nextFacilityId,
                  }));
                  setPagination((prev) => ({ ...prev, page: 0 }));
                }}
                className='w-full rounded-lg border bg-background px-3 py-2'
              >
                <option value=''>Tất cả kho</option>
                {(facilitiesResponse?.data?.items || []).map((facility) => (
                  <option key={facility.id} value={facility.id}>
                    {facility.name}
                  </option>
                ))}
              </select>
            </div>

            {hasActiveFilters && (
              <div className='sm:col-span-2 flex items-center justify-between border-t pt-4'>
                <p className='text-sm text-muted-foreground'>
                  Tìm thấy {totalItems} phiếu phù hợp
                </p>
                <Button variant='ghost' size='sm' onClick={clearFilters}>
                  Xóa bộ lọc
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4 text-destructive'>
            Đã xảy ra lỗi khi tải danh sách phiếu xuất.
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
                <div className='mb-4 h-4 w-2/3 rounded bg-muted' />
                <div className='space-y-2'>
                  <div className='h-3 w-full rounded bg-muted' />
                  <div className='h-3 w-3/4 rounded bg-muted' />
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
          {shipments.map((shipment: OutboundShipment) => (
            <OutboundShipmentCard
              key={shipment.id}
              shipment={shipment}
              viewMode={viewMode}
              order={orderMap.get(shipment.orderId)}
              customer={
                shipment.customerId
                  ? customerMap.get(shipment.customerId)
                  : undefined
              }
              onClick={() =>
                router.push(`/logistics2/outbound-shipments/${shipment.id}`)
              }
            />
          ))}
        </div>
      )}

      {!isLoading && shipments.length === 0 && !error && (
        <Card>
          <CardContent className='py-14 text-center'>
            <div className='mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted'>
              <Truck className='h-10 w-10 text-muted-foreground' />
            </div>
            <h3 className='mb-2 text-lg font-semibold'>
              Chưa có phiếu xuất nào
            </h3>
            <p className='mx-auto mb-6 max-w-sm text-muted-foreground'>
              {hasActiveFilters
                ? 'Thử điều chỉnh điều kiện lọc để xem thêm dữ liệu.'
                : 'Dữ liệu phiếu xuất sẽ hiển thị khi hệ thống phát sinh đơn.'}
            </p>
            <Button onClick={() => router.push('/logistics2/delivery-slips')}>
              <Truck className='mr-2 h-4 w-4' />
              Xem đơn giao hàng
            </Button>
          </CardContent>
        </Card>
      )}

      {totalItems > (pagination.size || 10) && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Hiển thị {currentPage * (pagination.size || 10) + 1} đến{' '}
            {Math.min((currentPage + 1) * (pagination.size || 10), totalItems)}{' '}
            trong tổng số {totalItems} phiếu nhập
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
