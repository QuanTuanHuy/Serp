/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 delivery slips list page
*/

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAppDispatch } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Input,
} from '@/shared/components/ui';
import {
  Calendar,
  ChevronLeft,
  ChevronRight,
  Clock3,
  ExternalLink,
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
  useGetDeliverySlipsQuery,
  useGetFacilitiesQuery,
  useGetOutboundShipmentsQuery,
} from '../../api/logistics2Api';
import type {
  Customer,
  DeliverySlip,
  DeliverySlipFilters,
  DeliverySlipStatus,
  Facility,
  OutboundShipment,
  PaginationParams,
} from '../../types';
import { StatsCard } from '../../components/cards/StatsCard';
import { setActiveModule } from '../../store';
import { DeliverySlipCard } from '../../components/cards/DeliverySlipCard';

interface DeliverySlipListPageProps {
  className?: string;
}

const STATUS_OPTIONS: Array<{ value: DeliverySlipStatus; label: string }> = [
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'ASSIGNED', label: 'Đã lên kế hoạch giao' },
  { value: 'DELIVERING', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'RECALLING', label: 'Đang thu hồi' },
];

const STATUS_CONFIG: Record<
  DeliverySlipStatus,
  {
    label: string;
    badgeClass: string;
  }
> = {
  PENDING: {
    label: 'Chờ xử lý',
    badgeClass:
      'bg-slate-100 text-slate-700 dark:bg-slate-900/30 dark:text-slate-300',
  },
  ASSIGNED: {
    label: 'Đã lên kế hoạch giao',
    badgeClass:
      'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300',
  },
  DELIVERING: {
    label: 'Đang giao',
    badgeClass:
      'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
  },
  DELIVERED: {
    label: 'Đã giao',
    badgeClass:
      'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300',
  },
  RECALLING: {
    label: 'Đang thu hồi',
    badgeClass:
      'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-300',
  },
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatNumber = (value?: number) => {
  if (!value) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

export const DeliverySlipListPage: React.FC<DeliverySlipListPageProps> = ({
  className,
}) => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const dispatch = useAppDispatch();

  const initialOutboundShipmentId =
    searchParams.get('outboundShipmentId') || undefined;

  const [filters, setFilters] = useState<DeliverySlipFilters>({
    ...(initialOutboundShipmentId
      ? { outboundShipmentId: initialOutboundShipmentId }
      : {}),
  });
  const [pagination, setPagination] = useState<PaginationParams>({
    page: 0,
    size: 9,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  });

  const [searchValue, setSearchValue] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  useEffect(() => {
    dispatch(setActiveModule('delivery-slips'));
  }, [dispatch]);

  useEffect(() => {
    if (!initialOutboundShipmentId) return;

    setFilters((prev) => ({
      ...prev,
      outboundShipmentId: initialOutboundShipmentId,
    }));
  }, [initialOutboundShipmentId]);

  const { data, isLoading, error, refetch } = useGetDeliverySlipsQuery({
    filters,
    pagination,
  });

  const slips = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  const { data: shipmentsResponse } = useGetOutboundShipmentsQuery({
    filters: {},
    pagination: { page: 0, size: 200 },
  });

  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    filters: {},
    pagination: { page: 0, size: 200 },
  });

  const stats = useMemo(() => {
    const pending = slips.filter((item) => item.status === 'PENDING').length;
    const delivering = slips.filter(
      (item) => item.status === 'DELIVERING'
    ).length;
    const delivered = slips.filter(
      (item) => item.status === 'DELIVERED'
    ).length;

    return {
      pending,
      delivering,
      delivered,
    };
  }, [slips]);

  const hasActiveFilters = Boolean(
    filters.query ||
      filters.status ||
      filters.facilityId ||
      filters.outboundShipmentId
  );

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

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Đơn giao hàng</h1>
          <p className='text-muted-foreground'>Danh sách đơn giao hàng</p>
        </div>

        <Button variant='outline' onClick={() => refetch()}>
          Làm mới
        </Button>
      </div>

      <div className='grid grid-cols-1 gap-4 sm:grid-cols-3'>
        <StatsCard
          title='Chờ xử lý'
          value={stats.pending}
          icon={Clock3}
          variant='primary'
        />

        <StatsCard
          title='Đang giao'
          value={stats.delivering}
          icon={Truck}
          variant='warning'
        />

        <StatsCard
          title='Đã giao'
          value={stats.delivered}
          icon={Package}
          variant='success'
        />
      </div>

      <div className='flex flex-col gap-3 sm:flex-row'>
        <div className='relative flex-1'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={searchValue}
            placeholder='Tìm theo mã đơn giao, khách hàng...'
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
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
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
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
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
          <CardContent className='grid gap-4 p-4 sm:grid-cols-3'>
            <div>
              <label className='mb-1.5 block text-sm font-medium'>
                Trạng thái
              </label>
              <select
                value={filters.status || ''}
                onChange={(event) => {
                  const nextStatus =
                    (event.target.value as DeliverySlipStatus) || undefined;
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
                Kho giao
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

            <div>
              <label className='mb-1.5 block text-sm font-medium'>
                Phiếu xuất
              </label>
              <select
                value={filters.outboundShipmentId || ''}
                onChange={(event) => {
                  const nextShipmentId = event.target.value || undefined;
                  setFilters((prev) => ({
                    ...prev,
                    outboundShipmentId: nextShipmentId,
                  }));
                  setPagination((prev) => ({ ...prev, page: 0 }));
                }}
                className='w-full rounded-lg border bg-background px-3 py-2'
              >
                <option value=''>Tất cả phiếu xuất</option>
                {(shipmentsResponse?.data?.items || []).map((shipment) => (
                  <option key={shipment.id} value={shipment.id}>
                    {shipment.name || shipment.id}
                  </option>
                ))}
              </select>
            </div>

            {hasActiveFilters && (
              <div className='flex items-center justify-between border-t pt-4 sm:col-span-3'>
                <p className='text-sm text-muted-foreground'>
                  Tìm thấy {totalItems} đơn phù hợp
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
            Đã xảy ra lỗi khi tải danh sách đơn giao hàng.
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

      {!isLoading && slips.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {slips.map((slip: DeliverySlip) => (
            <DeliverySlipCard
              key={slip.id}
              slip={slip}
              viewMode={viewMode}
              onClick={() =>
                router.push(`/logistics2/delivery-slips/${slip.id}`)
              }
            />
          ))}
        </div>
      )}

      {!isLoading && slips.length === 0 && !error && (
        <Card>
          <CardContent className='py-14 text-center'>
            <div className='mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted'>
              <Truck className='h-10 w-10 text-muted-foreground' />
            </div>
            <h3 className='mb-2 text-lg font-semibold'>
              Chưa có đơn giao hàng nào
            </h3>
            <p className='mx-auto mb-6 max-w-sm text-muted-foreground'>
              {hasActiveFilters
                ? 'Thử điều chỉnh bộ lọc để xem thêm dữ liệu.'
                : 'Dữ liệu đơn giao hàng sẽ hiển thị khi hệ thống tạo mới.'}
            </p>
            <Button onClick={() => refetch()}>
              <Truck className='mr-2 h-4 w-4' />
              Tải lại dữ liệu
            </Button>
          </CardContent>
        </Card>
      )}

      {totalItems > (pagination.size || 10) && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Hiển thị {currentPage * (pagination.size || 10) + 1} đến{' '}
            {Math.min((currentPage + 1) * (pagination.size || 10), totalItems)}{' '}
            trong tổng số {totalItems} đơn giao hàng
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
