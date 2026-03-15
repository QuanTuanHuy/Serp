// Sales FacilityListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
  Warehouse,
  X,
  ChevronLeft,
  ChevronRight,
  Phone,
  Edit,
  Trash2,
  ExternalLink,
  MapPin,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetFacilitiesQuery,
  useDeleteFacilityMutation,
} from '../../api/salesApi';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { setFacilityPagination, setFacilityFilters } from '../../store';
import {
  selectFacilityPagination,
  selectFacilityFilters,
} from '../../store/selectors';
import type { Facility, FacilityStatus } from '../../types';
import { formatPhoneNumber } from '@/shared/utils/format';

interface FacilityListPageProps {
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
  variant?: 'default' | 'primary' | 'success' | 'warning';
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

const FacilityCard = ({
  facility,
  onClick,
  onEdit,
  onDelete,
}: {
  facility: Facility;
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
}) => {
  const getInitials = (name: string) =>
    name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);

  const statusStyles = {
    ACTIVE: {
      bg: 'bg-emerald-100 dark:bg-emerald-900/30',
      text: 'text-emerald-700 dark:text-emerald-400',
      dot: 'bg-emerald-500',
    },
    INACTIVE: {
      bg: 'bg-slate-100 dark:bg-slate-800/50',
      text: 'text-slate-600 dark:text-slate-400',
      dot: 'bg-slate-400',
    },
  };

  const status =
    statusStyles[facility.statusId as keyof typeof statusStyles] ||
    statusStyles.ACTIVE;

  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer'
      )}
      onClick={onClick}
    >
      <div className='relative h-2 bg-gradient-to-r from-primary/60 via-primary/40 to-primary/20' />

      <CardContent className='p-5'>
        {/* Top Row */}
        <div className='flex items-start justify-between mb-4'>
          <div className='flex items-center gap-3'>
            <div className='relative'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary font-semibold text-lg shadow-sm'>
                {getInitials(facility.name)}
              </div>
              <div
                className={cn(
                  'absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-full border-2 border-card',
                  status.dot
                )}
              />
            </div>
            <div className='min-w-0'>
              <h3 className='font-semibold text-foreground truncate'>
                {facility.name}
              </h3>
              <Badge
                variant='secondary'
                className={cn('gap-1 mt-1', status.bg, status.text)}
              >
                <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
                {facility.statusId === 'ACTIVE'
                  ? 'Hoạt động'
                  : 'Không hoạt động'}
              </Badge>
            </div>
          </div>

          <div className='flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8 hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-600 dark:text-blue-400'
              onClick={(e) => {
                e.stopPropagation();
                onEdit?.();
              }}
              title='Chỉnh sửa'
            >
              <Edit className='h-4 w-4' />
            </Button>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8 hover:bg-rose-100 dark:hover:bg-rose-900/30 text-rose-600 dark:text-rose-400'
              onClick={(e) => {
                e.stopPropagation();
                onDelete?.();
              }}
              title='Xóa'
            >
              <Trash2 className='h-4 w-4' />
            </Button>
          </div>
        </div>

        {/* Info */}
        <div className='space-y-2 mb-4'>
          {facility.phone && (
            <div className='flex items-center gap-2 text-sm'>
              <Phone className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                {formatPhoneNumber(facility.phone)}
              </span>
            </div>
          )}
          {facility.address && (
            <div className='flex items-center gap-2 text-sm'>
              <MapPin className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                {facility.address.fullAddress}
              </span>
            </div>
          )}
          {(facility.length || facility.width || facility.height) && (
            <div className='flex items-center gap-2 text-sm'>
              <Warehouse className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                {facility.length}m × {facility.width}m × {facility.height}m
              </span>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className='flex items-center justify-end gap-1 pt-3 border-t'>
          {facility.phone && (
            <Button
              variant='ghost'
              size='icon'
              className='h-9 w-9 hover:bg-emerald-100 dark:hover:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400'
              onClick={(e) => e.stopPropagation()}
              title='Gọi'
            >
              <Phone className='h-4 w-4' />
            </Button>
          )}
          <Button
            variant='ghost'
            size='icon'
            className='h-9 w-9'
            onClick={(e) => {
              e.stopPropagation();
              onClick?.();
            }}
            title='Xem chi tiết'
          >
            <ExternalLink className='h-4 w-4' />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export const FacilityListPage: React.FC<FacilityListPageProps> = ({
  className,
}) => {
  const router = useRouter();
  const dispatch = useAppDispatch();

  const pagination = useAppSelector(selectFacilityPagination);
  const filters = useAppSelector(selectFacilityFilters);

  const { data, isLoading, error } = useGetFacilitiesQuery({
    filters,
    pagination,
  });

  const [deleteFacility] = useDeleteFacilityMutation();

  const [searchQuery, setSearchQuery] = useState(filters.query || '');
  const [statusFilter, setStatusFilter] = useState<FacilityStatus | ''>('');
  const [showFilters, setShowFilters] = useState(false);

  const facilities = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  const handleSearch = () => {
    dispatch(setFacilityFilters({ ...filters, query: searchQuery }));
    dispatch(setFacilityPagination({ ...pagination, page: 0 }));
  };

  const handlePageChange = (newPage: number) => {
    dispatch(setFacilityPagination({ ...pagination, page: newPage }));
  };

  const handleDeleteFacility = async (facilityId: string) => {
    if (confirm('Bạn có chắc chắn muốn xóa kho hàng này không?')) {
      try {
        await deleteFacility(facilityId).unwrap();
      } catch (error) {
        console.error('Failed to delete facility:', error);
      }
    }
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    dispatch(setFacilityFilters({}));
    dispatch(setFacilityPagination({ ...pagination, page: 0 }));
  };

  const hasActiveFilters = searchQuery || statusFilter;

  const stats = useMemo(
    () => ({
      total: totalItems,
      active: facilities.filter((f: Facility) => f.statusId === 'ACTIVE')
        .length,
    }),
    [totalItems, facilities]
  );

  return (
    <div className={cn('space-y-6', className)}>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Kho hàng</h1>
          <p className='text-muted-foreground'>Quản lý kho hàng của bạn</p>
        </div>
        <Button
          onClick={() => router.push('/sales/facility/new')}
          className='gap-2'
        >
          <Plus className='h-4 w-4' />
          Thêm kho hàng mới
        </Button>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-3 gap-4'>
        <StatsCard
          title='Tổng số kho hàng'
          value={stats.total}
          icon={Warehouse}
        />
        <StatsCard
          title='Kho đang hoạt động'
          value={stats.active}
          icon={Warehouse}
        />
        <StatsCard
          title='Kho không hoạt động'
          value={stats.total - stats.active}
          icon={Warehouse}
        />
      </div>

      {/* Search & Filters */}
      <div className='flex flex-col sm:flex-row gap-3'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Tìm kiếm kho hàng theo tên...'
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
                    const value = e.target.value as FacilityStatus | '';
                    setStatusFilter(value);
                    dispatch(
                      setFacilityFilters({
                        ...filters,
                        statusId: value || undefined,
                      })
                    );
                  }}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
                >
                  <option value=''>Tất cả trạng thái</option>
                  <option value='ACTIVE'>Hoạt động</option>
                  <option value='INACTIVE'>Không hoạt động</option>
                </select>
              </div>
            </div>

            {hasActiveFilters && (
              <div className='mt-4 pt-4 border-t flex items-center justify-between'>
                <p className='text-sm text-muted-foreground'>
                  {totalItems} kết quả
                </p>
                <Button variant='ghost' size='sm' onClick={clearFilters}>
                  Xóa tất cả bộ lọc
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Error */}
      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4'>
            <p className='text-destructive'>
              Đã xảy ra lỗi khi tải danh sách kho hàng. Vui lòng thử lại.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Loading */}
      {isLoading && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {Array.from({ length: 6 }).map((_, index) => (
            <Card key={index} className='animate-pulse'>
              <CardContent className='p-5'>
                <div className='flex items-center gap-3 mb-4'>
                  <div className='h-12 w-12 bg-muted rounded-full' />
                  <div className='flex-1'>
                    <div className='h-4 bg-muted rounded w-3/4 mb-2' />
                    <div className='h-3 bg-muted rounded w-1/2' />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Facility Grid */}
      {!isLoading && facilities.length > 0 && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {facilities.map((facility: Facility) => (
            <FacilityCard
              key={facility.id}
              facility={facility}
              onClick={() => router.push(`/sales/facility/${facility.id}`)}
              onEdit={() => router.push(`/sales/facility/${facility.id}/edit`)}
              onDelete={() => handleDeleteFacility(facility.id)}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && facilities.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Warehouse className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>
              Không tìm thấy kho hàng
            </h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Hãy điều chỉnh bộ lọc để xem thêm kết quả.'
                : 'Bắt đầu bằng cách thêm kho hàng đầu tiên của bạn.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Xóa tất cả bộ lọc
              </Button>
            ) : (
              <Button onClick={() => router.push('/sales/facility/new')}>
                <Plus className='h-4 w-4 mr-2' />
                Thêm kho hàng đầu tiên
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
            trong tổng số {totalItems} kho hàng
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
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => (
                <button
                  key={i}
                  onClick={() => handlePageChange(i)}
                  className={cn(
                    'h-8 w-8 rounded-md text-sm font-medium transition-colors',
                    currentPage === i
                      ? 'bg-primary text-primary-foreground'
                      : 'hover:bg-muted'
                  )}
                >
                  {i + 1}
                </button>
              ))}
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

export default FacilityListPage;
