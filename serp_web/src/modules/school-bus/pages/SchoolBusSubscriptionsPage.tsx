'use client';

import * as React from 'react';
import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  Calendar,
  Eye,
  GraduationCap,
  PauseCircle,
  PlayCircle,
  Repeat,
  Search,
  User,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetBasicSchoolDropdownOptionsQuery,
  useGetSchoolBusSubscriptionSummaryQuery,
  useGetSchoolBusSubscriptionsQuery,
} from '../api/schoolBusApi';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import {
  formatDate,
  getPageItems,
  SCHOOL_BUS_PAGE_QUERY_OPTIONS,
} from '../utils';
import { useSchoolBusAccess } from '../security/schoolBusAccess';
import {
  subscriptionStatusLabel,
  tripOptionLabel,
} from '../schoolBusLabels';

export function SchoolBusSubscriptionsPage() {
  const access = useSchoolBusAccess();
  const router = useRouter();
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  });
  const { data, isLoading } = useGetSchoolBusSubscriptionsQuery(
    pagination.params,
    SCHOOL_BUS_PAGE_QUERY_OPTIONS
  );
  const { data: summaryData } = useGetSchoolBusSubscriptionSummaryQuery(
    undefined,
    { refetchOnMountOrArgChange: true }
  );
  const { data: schoolsData } = useGetBasicSchoolDropdownOptionsQuery();
  const subscriptions = getPageItems(data?.data);
  const schools = schoolsData?.data || [];

  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);

  React.useEffect(() => {
    pagination.setKeyword(debouncedSearch || '');
  }, [debouncedSearch]);

  // Client-side filters
  const [filterSchool, setFilterSchool] = useState<string>('');
  const [filterStatus, setFilterStatus] = useState<string>('');
  const [filterTripOption, setFilterTripOption] = useState<string>('');

  const filteredSubscriptions = React.useMemo(() => {
    let result = subscriptions;
    if (filterSchool) {
      result = result.filter((s) => s.schoolId === Number(filterSchool));
    }
    if (filterStatus) {
      result = result.filter((s) => s.status === filterStatus);
    }
    if (filterTripOption) {
      result = result.filter((s) => s.tripOption === filterTripOption);
    }
    return result;
  }, [subscriptions, filterSchool, filterStatus, filterTripOption]);

  const schoolOptions = React.useMemo(() => {
    return [
      { label: 'Tất cả trường học', value: '' },
      ...schools.map((s) => ({
        label: s.name,
        value: String(s.id),
      })),
    ];
  }, [schools]);

  const statusOptions = [
    { label: 'Tất cả trạng thái', value: '' },
    { label: subscriptionStatusLabel.PENDING, value: 'PENDING', color: 'slate' as const },
    { label: subscriptionStatusLabel.ACTIVE, value: 'ACTIVE', color: 'green' as const },
    { label: subscriptionStatusLabel.PAUSED, value: 'PAUSED', color: 'orange' as const },
    { label: subscriptionStatusLabel.STOPPED, value: 'STOPPED', color: 'red' as const },
    { label: subscriptionStatusLabel.EXPIRED, value: 'EXPIRED', color: 'slate' as const },
  ];

  const tripOptionOptions = [
    { label: 'Tất cả loại chuyến', value: '' },
    { label: tripOptionLabel.ROUND_TRIP, value: 'ROUND_TRIP' },
    { label: tripOptionLabel.MORNING, value: 'MORNING' },
    { label: tripOptionLabel.AFTERNOON, value: 'AFTERNOON' },
  ];

  const subscriptionColumns: SchoolBusTableColumn<any>[] = [
    {
      key: 'code',
      header: 'Đăng ký dịch vụ',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (subscription) => (
        <div className='flex items-center gap-3 font-semibold text-slate-900'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-blue-50/70 text-blue-700 border border-blue-100/50'>
            <Repeat className='h-4 w-4' />
          </div>
          <div className='flex flex-col min-w-0'>
            <Link
              href={`/school-bus/subscriptions/${subscription.id}`}
              className='font-bold text-[#C81E3A] hover:underline truncate'
            >
              {subscription.subscriptionCode}
            </Link>
            {subscription.sourceRequestId && (
              <span className='text-[10px] text-slate-400 font-normal mt-0.5'>
                Từ yêu cầu #{subscription.sourceRequestId}
              </span>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'student',
      header: 'Học sinh',
      render: (subscription) => (
        <div className='flex items-center gap-2.5'>
          <div className='flex h-7.5 w-7.5 shrink-0 items-center justify-center rounded-full bg-violet-50 text-violet-600 border border-violet-100/40'>
            <User className='h-4 w-4' />
          </div>
          <span className='font-semibold text-slate-800 truncate'>
            {subscription.studentName}
          </span>
        </div>
      ),
    },
    {
      key: 'school',
      header: 'Trường học',
      render: (subscription) => (
        <div className='flex items-center gap-2'>
          <GraduationCap className='h-4 w-4 text-slate-400 shrink-0' />
          <span className='text-slate-700 font-medium truncate'>
            {subscription.schoolName}
          </span>
        </div>
      ),
    },
    {
      key: 'tripOption',
      header: 'Phương án đi xe',
      render: (subscription) => {
        const colors: Record<string, string> = {
          ROUND_TRIP: 'bg-blue-50/50 text-blue-700 border-blue-100/50',
          MORNING: 'bg-sky-50/50 text-sky-700 border-sky-100/50',
          AFTERNOON: 'bg-indigo-50/50 text-indigo-700 border-indigo-100/50',
        };
        const label =
          tripOptionLabel[subscription.tripOption] || subscription.tripOption;
        const colorClass =
          colors[subscription.tripOption] ||
          'bg-slate-50 text-slate-600 border-slate-100';
        return (
          <span
            className={cn(
              'inline-flex items-center rounded-lg border px-2 py-0.5 text-xs font-semibold',
              colorClass
            )}
          >
            {label}
          </span>
        );
      },
    },
    {
      key: 'pickupDropoff',
      header: 'Điểm đón / trả',
      render: (subscription) => (
        <div className='flex flex-col gap-1.5 text-xs'>
          <div className='flex items-center gap-1.5'>
            <span className='text-[10px] font-bold text-blue-600 uppercase tracking-wide bg-blue-50 px-1 py-0.5 rounded shrink-0'>
              P
            </span>
            {subscription.pickupPointName ? (
              <span
                className='font-medium text-slate-700 truncate max-w-[180px]'
                title={subscription.pickupPointName}
              >
                {subscription.pickupPointName}
              </span>
            ) : (
              <span className='font-medium text-amber-600 italic bg-amber-50 px-1 py-0.5 rounded shrink-0'>
                Thiếu cấu hình
              </span>
            )}
          </div>
          <div className='flex items-center gap-1.5'>
            <span className='text-[10px] font-bold text-emerald-600 uppercase tracking-wide bg-emerald-50 px-1 py-0.5 rounded shrink-0'>
              D
            </span>
            {subscription.dropoffPointName ? (
              <span
                className='font-medium text-slate-700 truncate max-w-[180px]'
                title={subscription.dropoffPointName}
              >
                {subscription.dropoffPointName}
              </span>
            ) : (
              <span className='font-medium text-amber-600 italic bg-amber-50 px-1 py-0.5 rounded shrink-0'>
                Thiếu cấu hình
              </span>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'effective',
      header: 'Hiệu lực',
      render: (subscription) => (
        <div className='flex flex-col text-xs text-slate-600 gap-0.5'>
          <span className='font-medium text-slate-700'>
            {formatDate(subscription.effectiveFrom)}
          </span>
          <span className='text-[10px] text-slate-400'>
            đến{' '}
            {subscription.effectiveTo
              ? formatDate(subscription.effectiveTo)
              : 'Đang áp dụng'}
          </span>
        </div>
      ),
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (subscription) => (
        <SchoolBusStatusBadge
          status={subscription.status}
          labelMap={subscriptionStatusLabel}
        />
      ),
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (subscription) => (
        <div
          className='flex justify-end gap-2'
          onClick={(e) => e.stopPropagation()}
        >
          <Link href={`/school-bus/subscriptions/${subscription.id}`}>
            <Button
              size='icon'
              variant='outline'
              className='h-8 w-8 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-slate-700'
            >
              <Eye className='h-3.5 w-3.5' />
            </Button>
          </Link>
        </div>
      ),
    },
  ];

  const subscriptionToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0 justify-between'>
      <div className='relative flex-1 min-w-[200px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Tìm theo mã đăng ký hoặc tên học sinh...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>

      <div className='flex flex-wrap items-center gap-2'>
        <SchoolBusSelect
          value={filterSchool}
          onChange={setFilterSchool}
          options={schoolOptions}
          placeholder='Tất cả trường học'
          searchable
          className='w-48'
        />
        <SchoolBusSelect
          value={filterStatus}
          onChange={setFilterStatus}
          options={statusOptions}
          placeholder='Tất cả trạng thái'
          className='w-40'
        />
        <SchoolBusSelect
          value={filterTripOption}
          onChange={setFilterTripOption}
          options={tripOptionOptions}
          placeholder='Tất cả phương án'
          className='w-44'
        />
      </div>
    </div>
  );

  return (
    <SchoolBusPageShell
      title='Đăng ký'
      description={
        access.isParentOnly
          ? 'Theo dõi các dịch vụ xe bus đang áp dụng cho học sinh.'
          : 'Quản lý nhu cầu sử dụng xe bus dài hạn. Yêu cầu đã duyệt được chuyển thành đăng ký để phục vụ lập tuyến.'
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Stats */}
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label='Đăng ký'
            value={summaryData?.data?.totalSubscriptions ?? 0}
            icon={Repeat}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Đang hoạt động'
            value={summaryData?.data?.activeSubscriptions ?? 0}
            icon={PlayCircle}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Tạm dừng hoặc đã dừng'
            value={summaryData?.data?.inactiveSubscriptions ?? 0}
            icon={PauseCircle}
            tone='warning'
          />
        </div>

        {/* Data Table */}
        <SchoolBusDataTable
          title='Danh sách đăng ký dịch vụ'
          description={
            access.isParentOnly
              ? 'Xem chi tiết đăng ký dịch vụ của học sinh.'
              : 'Theo dõi trạng thái đăng ký để xác định học sinh đủ điều kiện tham gia lập tuyến hằng ngày.'
          }
          toolbar={subscriptionToolbar}
          data={filteredSubscriptions}
          columns={subscriptionColumns}
          isLoading={isLoading}
          pagination={{ page: data?.data, onPageChange: pagination.setPage }}
          stickyFirstColumn
          stickyActionColumn
          onRowDoubleClick={(row) =>
            router.push(`/school-bus/subscriptions/${row.id}`)
          }
          emptyIcon={Repeat}
          emptyTitle={
            subscriptions.length === 0
              ? 'Chưa có đăng ký'
              : 'Không có đăng ký phù hợp với bộ lọc'
          }
          emptyDescription={
            subscriptions.length === 0
              ? 'Duyệt yêu cầu xe bus để hệ thống tự tạo đăng ký dịch vụ.'
              : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
          }
        />
      </div>
    </SchoolBusPageShell>
  );
}
