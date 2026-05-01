/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 delivery plans list page
*/

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  ClipboardList,
  Grid3X3,
  List,
  Plus,
  RefreshCcw,
  Search,
  SlidersHorizontal,
  X,
} from 'lucide-react';
import { useAppDispatch } from '@/lib/store';
import { Button, Card, CardContent, Input } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { getErrorMessage } from '@/lib/store/api';
import {
  useGetDeliveryPlansQuery,
  useGetFacilitiesQuery,
} from '../../api/logistics2Api';
import { DeliveryPlanCard } from '../../components/cards/DeliveryPlanCard';
import { StatsCard } from '../../components/cards/StatsCard';
import { setActiveModule } from '../../store';
import type {
  DeliveryPlan,
  DeliveryPlanFilters,
  Facility,
  PaginationParams,
  PlanOptimizationStatus,
} from '../../types';

const STATUS_OPTIONS: Array<{ value: PlanOptimizationStatus; label: string }> =
  [
    { value: 'DRAFT', label: 'Nháp' },
    { value: 'OPTIMIZING', label: 'Đang tối ưu' },
    { value: 'COMPLETED', label: 'Đã tối ưu' },
    { value: 'FAILED', label: 'Tối ưu thất bại' },
  ];

const buildFallbackFacility = (facilityId?: string): Facility => ({
  id: facilityId || 'UNKNOWN',
  name: facilityId || 'Không xác định',
  statusId: 'ACTIVE',
  currentAddressId: '',
  createdStamp: '',
  lastUpdatedStamp: '',
  isDefault: false,
  tenantId: 0,
});

export const DeliveryPlanListPage: React.FC = () => {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const searchParams = useSearchParams();
  const searchParamValue = searchParams.get('search')?.trim() || '';

  const [filters, setFilters] = useState<DeliveryPlanFilters>({
    ...(searchParamValue ? { query: searchParamValue } : {}),
  });
  const [pagination, setPagination] = useState<PaginationParams>({
    page: 0,
    size: 9,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  });
  const [searchValue, setSearchValue] = useState(searchParamValue);
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  useEffect(() => {
    dispatch(setActiveModule('delivery-plans'));
  }, [dispatch]);

  useEffect(() => {
    setSearchValue(searchParamValue);
    setFilters((prev) => ({
      ...prev,
      query: searchParamValue || undefined,
    }));
    setPagination((prev) => ({ ...prev, page: 0 }));
  }, [searchParamValue]);

  const {
    data: plansResponse,
    isFetching,
    isLoading,
    error,
    refetch,
  } = useGetDeliveryPlansQuery({
    filters,
    pagination,
  });

  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    filters: {},
    pagination: { page: 0, size: 200 },
  });

  const plans = plansResponse?.data?.items || [];
  const totalItems = plansResponse?.data?.totalItems || 0;
  const totalPages = plansResponse?.data?.totalPages || 0;
  const currentPage = plansResponse?.data?.currentPage ?? pagination.page ?? 0;

  const facilityMap = useMemo(() => {
    const map = new Map<string, Facility>();

    (facilitiesResponse?.data?.items || []).forEach((facility) => {
      map.set(facility.id, facility);
    });

    return map;
  }, [facilitiesResponse]);

  const stats = useMemo(() => {
    const counts = {
      draft: 0,
      optimizing: 0,
      completed: 0,
      failed: 0,
    };

    plans.forEach((plan) => {
      switch (plan.optimizationStatus) {
        case 'DRAFT':
          counts.draft += 1;
          break;
        case 'OPTIMIZING':
          counts.optimizing += 1;
          break;
        case 'COMPLETED':
          counts.completed += 1;
          break;
        case 'FAILED':
          counts.failed += 1;
          break;
        default:
          break;
      }
    });

    return counts;
  }, [plans]);

  const hasActiveFilters = Boolean(
    filters.query ||
      filters.optimizationStatus ||
      filters.facilityId ||
      filters.deliveryDate
  );

  const handleRefreshData = () => {
    refetch();
  };

  const handleSearch = () => {
    setFilters((prev) => ({
      ...prev,
      query: searchValue.trim() || undefined,
    }));
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const clearFilters = () => {
    setSearchValue('');
    setFilters({});
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const handlePageChange = (page: number) => {
    setPagination((prev) => ({ ...prev, page }));
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Kế hoạch giao hàng
          </h1>
          <p className='text-muted-foreground'>
            Theo dõi và điều phối kế hoạch giao hàng
          </p>
        </div>

        <div className='flex flex-wrap items-center gap-2'>
          <Button
            variant='outline'
            className='border-slate-300 bg-white/90 text-slate-700 hover:bg-white'
            onClick={handleRefreshData}
            disabled={isFetching}
          >
            <RefreshCcw
              className={`mr-2 h-4 w-4 ${isFetching ? 'animate-spin text-emerald-600' : ''}`}
            />
            {isFetching ? 'Đang tải...' : 'Làm mới'}
          </Button>
          <Button
            onClick={() => router.push('/logistics2/delivery-plans/create')}
            className='gap-2'
          >
            <Plus className='h-4 w-4' />
            Tạo mới kế hoạch
          </Button>
        </div>
      </div>

      <div className='grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4'>
        <StatsCard
          title='Nháp'
          value={stats.draft}
          icon={ClipboardList}
          variant='default'
        />
        <StatsCard
          title='Đang tối ưu'
          value={stats.optimizing}
          icon={CalendarDays}
          variant='warning'
        />
        <StatsCard
          title='Đã tối ưu'
          value={stats.completed}
          icon={CalendarDays}
          variant='success'
        />
        <StatsCard
          title='Tối ưu thất bại'
          value={stats.failed}
          icon={CalendarDays}
          variant='danger'
        />
      </div>

      <div className='flex flex-col gap-3 sm:flex-row'>
        <div className='relative flex-1'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={searchValue}
            placeholder='Tìm theo mã kế hoạch, kho giao...'
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
                value={filters.optimizationStatus || ''}
                onChange={(event) => {
                  const nextStatus =
                    (event.target.value as PlanOptimizationStatus) || undefined;
                  setFilters((prev) => ({
                    ...prev,
                    optimizationStatus: nextStatus,
                  }));
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
                Ngày giao
              </label>
              <Input
                type='date'
                value={filters.deliveryDate || ''}
                onChange={(event) => {
                  const nextDate = event.target.value || undefined;
                  setFilters((prev) => ({
                    ...prev,
                    deliveryDate: nextDate,
                  }));
                  setPagination((prev) => ({ ...prev, page: 0 }));
                }}
              />
            </div>

            {hasActiveFilters && (
              <div className='flex items-center justify-between border-t pt-4 sm:col-span-3'>
                <p className='text-sm text-muted-foreground'>
                  Tìm thấy {totalItems} kế hoạch phù hợp
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
            {getErrorMessage(error) ||
              'Đã xảy ra lỗi khi tải danh sách kế hoạch.'}
          </CardContent>
        </Card>
      )}

      {isLoading && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3'
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

      {!isLoading && plans.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {plans.map((plan: DeliveryPlan) => (
            <DeliveryPlanCard
              key={plan.id}
              plan={plan}
              facility={
                facilityMap.get(plan.facilityId) ||
                buildFallbackFacility(plan.facilityId)
              }
              viewMode={viewMode}
              onClick={() =>
                router.push(`/logistics2/delivery-plans/${plan.id}`)
              }
            />
          ))}
        </div>
      )}

      {!isLoading && plans.length === 0 && !error && (
        <Card>
          <CardContent className='py-14 text-center'>
            <div className='mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted'>
              <CalendarDays className='h-10 w-10 text-muted-foreground' />
            </div>
            <h3 className='mb-2 text-lg font-semibold'>
              Chưa có kế hoạch giao nào
            </h3>
            <p className='mx-auto mb-6 max-w-sm text-muted-foreground'>
              {hasActiveFilters
                ? 'Thử điều chỉnh bộ lọc để xem thêm dữ liệu.'
                : 'Bắt đầu bằng cách tạo kế hoạch giao hàng đầu tiên.'}
            </p>
            <Button
              onClick={() => router.push('/logistics2/delivery-plans/create')}
            >
              <Plus className='mr-2 h-4 w-4' />
              Tạo kế hoạch mới
            </Button>
          </CardContent>
        </Card>
      )}

      {totalItems > (pagination.size || 10) && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Hiển thị {currentPage * (pagination.size || 10) + 1} đến{' '}
            {Math.min((currentPage + 1) * (pagination.size || 10), totalItems)}{' '}
            trong tổng số {totalItems} kế hoạch
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
