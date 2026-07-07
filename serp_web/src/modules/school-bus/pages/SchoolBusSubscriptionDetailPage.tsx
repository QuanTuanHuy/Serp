'use client';

import Link from 'next/link';
import * as React from 'react';
import {
  ArrowLeft,
  Calendar,
  GraduationCap,
  PauseCircle,
  PlayCircle,
  StopCircle,
  User,
  AlertCircle,
  CheckCircle2,
  Info,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetSchoolBusSubscriptionByIdQuery,
  useActivateSchoolBusSubscriptionMutation,
  usePauseSchoolBusSubscriptionMutation,
  useStopSchoolBusSubscriptionMutation,
} from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { formatDate } from '../utils';
import { useSchoolBusAccess } from '../security/schoolBusAccess';
import { subscriptionStatusLabel, tripOptionLabel } from '../schoolBusLabels';

const TRIP_OPTION_LABELS: Record<string, string> = {
  MORNING: tripOptionLabel.MORNING,
  AFTERNOON: tripOptionLabel.AFTERNOON,
  ROUND_TRIP: tripOptionLabel.ROUND_TRIP,
};

const DAY_KEYS = [
  { key: 'monday', label: 'T2' },
  { key: 'tuesday', label: 'T3' },
  { key: 'wednesday', label: 'T4' },
  { key: 'thursday', label: 'T5' },
  { key: 'friday', label: 'T6' },
  { key: 'saturday', label: 'T7' },
  { key: 'sunday', label: 'CN' },
] as const;

interface SchoolBusSubscriptionDetailPageProps {
  subscriptionId: number;
}

export function SchoolBusSubscriptionDetailPage({
  subscriptionId,
}: SchoolBusSubscriptionDetailPageProps) {
  const access = useSchoolBusAccess();
  const { data, isLoading } =
    useGetSchoolBusSubscriptionByIdQuery(subscriptionId);
  const [activateSubscription, { isLoading: activating }] =
    useActivateSchoolBusSubscriptionMutation();
  const [pauseSubscription, { isLoading: pausing }] =
    usePauseSchoolBusSubscriptionMutation();
  const [stopSubscription, { isLoading: stopping }] =
    useStopSchoolBusSubscriptionMutation();

  const sub = data?.data;

  const handleAction = async (action: 'activate' | 'pause' | 'stop') => {
    try {
      const response =
        action === 'activate'
          ? await activateSubscription(subscriptionId).unwrap()
          : action === 'pause'
            ? await pauseSubscription(subscriptionId).unwrap()
            : await stopSubscription(subscriptionId).unwrap();
      toast.success(response.message || 'Đã cập nhật đăng ký dịch vụ');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể cập nhật đăng ký');
    }
  };

  if (isLoading) {
    return (
      <SchoolBusPageShell
        title='Chi tiết đăng ký dịch vụ'
        description='Đang tải chi tiết đăng ký xe bus...'
      >
        <div className='flex h-[400px] items-center justify-center'>
          <div className='flex flex-col items-center gap-3'>
            <div className='h-8 w-8 animate-spin rounded-full border-2 border-[#C81E3A] border-t-transparent' />
            <p className='text-xs text-slate-500 font-medium'>
              Đang tải dữ liệu đăng ký dịch vụ...
            </p>
          </div>
        </div>
      </SchoolBusPageShell>
    );
  }

  if (!sub) {
    return (
      <SchoolBusPageShell
        title='Chi tiết đăng ký dịch vụ'
        description='Không thể xem đăng ký dịch vụ xe bus'
      >
        <div className='py-12 flex flex-col items-center gap-4'>
          <SchoolBusEmptyState
            title='Không tìm thấy đăng ký dịch vụ'
            description='Đăng ký này có thể đã bị xóa hoặc bạn không có quyền truy cập.'
            icon={AlertCircle}
          />
          <Link href='/school-bus/subscriptions'>
            <Button variant='outline' className='gap-2 rounded-xl'>
              <ArrowLeft className='h-4 w-4' />
              Quay lại danh sách đăng ký
            </Button>
          </Link>
        </div>
      </SchoolBusPageShell>
    );
  }

  // derive eligibility checklist
  const isStatusActive = sub.status === 'ACTIVE';
  const isDateValid = (() => {
    const now = new Date();
    const today = new Date(
      now.getFullYear(),
      now.getMonth(),
      now.getDate()
    ).getTime();
    const fromTime = sub.effectiveFrom
      ? new Date(sub.effectiveFrom).getTime()
      : 0;
    const toTime = sub.effectiveTo
      ? new Date(sub.effectiveTo).getTime()
      : Infinity;
    return today >= fromTime && today <= toTime;
  })();

  const hasDays =
    sub.monday ||
    sub.tuesday ||
    sub.wednesday ||
    sub.thursday ||
    sub.friday ||
    sub.saturday ||
    sub.sunday;

  const requiresPickup =
    sub.tripOption === 'MORNING' || sub.tripOption === 'ROUND_TRIP';
  const requiresDropoff =
    sub.tripOption === 'AFTERNOON' || sub.tripOption === 'ROUND_TRIP';

  const pickupPointConfigured = !requiresPickup || !!sub.pickupPointId;
  const dropoffPointConfigured = !requiresDropoff || !!sub.dropoffPointId;

  const isEligible =
    isStatusActive &&
    isDateValid &&
    hasDays &&
    pickupPointConfigured &&
    dropoffPointConfigured;

  const isConfigurationIncomplete =
    !pickupPointConfigured || !dropoffPointConfigured;

  const descriptionNode = (
    <div className='space-y-1.5 mt-1'>
      <p className='text-sm text-slate-500 font-medium'>
        Dịch vụ xe bus dài hạn của học sinh{' '}
        <span className='font-bold text-slate-800'>{sub.studentName}</span>
      </p>
      {/* Metadata row */}
      <div className='flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-slate-400 font-semibold'>
        <SchoolBusStatusBadge
          status={sub.status}
          labelMap={subscriptionStatusLabel}
        />
        <span>-</span>
        <span>{TRIP_OPTION_LABELS[sub.tripOption] || sub.tripOption}</span>
        <span>-</span>
        <span>
          Hiệu lực: {formatDate(sub.effectiveFrom)} -{' '}
          {sub.effectiveTo ? formatDate(sub.effectiveTo) : 'Đang áp dụng'}
        </span>
      </div>
    </div>
  );

  const actionsNode = (
    <div className='flex flex-wrap items-center gap-2 md:justify-end shrink-0'>
      <Link href='/school-bus/subscriptions'>
        <Button
          variant='outline'
          className='h-9 rounded-xl gap-2 font-bold text-slate-600 border-slate-200 hover:bg-slate-50'
        >
          <ArrowLeft className='h-4 w-4' />
          Quay lại
        </Button>
      </Link>

      {/* Admin/Dispatcher: lifecycle actions - Parent cannot write subscriptions directly */}
      {!access.isParentOnly && (
        <>
          {sub.status === 'ACTIVE' && (
            <>
              <Button
                variant='outline'
                onClick={() => handleAction('pause')}
                disabled={pausing}
                className='h-9 rounded-xl border-amber-200 hover:bg-amber-50 text-amber-700 font-bold gap-1.5'
              >
                <PauseCircle className='h-4 w-4' />
                Tạm dừng
              </Button>
              <Button
                variant='outline'
                onClick={() => handleAction('stop')}
                disabled={stopping}
                className='h-9 rounded-xl border-red-200 hover:bg-red-50 text-red-600 font-bold gap-1.5'
              >
                <StopCircle className='h-4 w-4' />
                Dừng
              </Button>
            </>
          )}

          {sub.status === 'PAUSED' && (
            <>
              <Button
                variant='outline'
                onClick={() => handleAction('activate')}
                disabled={activating}
                className='h-9 rounded-xl border-emerald-200 hover:bg-emerald-50 text-emerald-700 font-bold gap-1.5'
              >
                <PlayCircle className='h-4 w-4' />
                Kích hoạt lại
              </Button>
              <Button
                variant='outline'
                onClick={() => handleAction('stop')}
                disabled={stopping}
                className='h-9 rounded-xl border-red-200 hover:bg-red-50 text-red-600 font-bold gap-1.5'
              >
                <StopCircle className='h-4 w-4' />
                Dừng
              </Button>
            </>
          )}
        </>
      )}
    </div>
  );

  return (
    <SchoolBusPageShell
      title={`Đăng ký ${sub.subscriptionCode}`}
      description={descriptionNode}
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
            { label: 'Đăng ký', href: '/school-bus/subscriptions' },
            { label: sub.subscriptionCode || 'Chi tiết', current: true },
          ]}
        />
      }
      actions={actionsNode}
    >
      <div className='flex flex-col gap-6 mt-4'>
        {/* Status / Eligibility Banners */}
        <div className='space-y-3'>
          {sub.status === 'ACTIVE' && (
            <div className='flex items-start gap-3 rounded-2xl border border-emerald-200 bg-emerald-50/60 p-4 text-emerald-800 shadow-sm'>
              <CheckCircle2 className='mt-0.5 h-5 w-5 text-emerald-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-emerald-950'>
                  {subscriptionStatusLabel.ACTIVE}
                </h4>
                <p className='text-xs text-emerald-850 mt-0.5 leading-relaxed font-medium'>
                  Học sinh đủ điều kiện tham gia lập tuyến khi ngày phục vụ,
                  ngày hoạt động và điểm đón/trả phù hợp.
                </p>
              </div>
            </div>
          )}

          {sub.status === 'PAUSED' && (
            <div className='flex items-start gap-3 rounded-2xl border border-amber-200 bg-amber-50/60 p-4 text-amber-800 shadow-sm'>
              <PauseCircle className='mt-0.5 h-5 w-5 text-amber-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-amber-950'>
                  {subscriptionStatusLabel.PAUSED}
                </h4>
                <p className='text-xs text-amber-850 mt-0.5 leading-relaxed font-medium'>
                  Đăng ký này không tham gia lập tuyến trong thời gian tạm dừng.
                </p>
              </div>
            </div>
          )}

          {sub.status === 'STOPPED' && (
            <div className='flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50/60 p-4 text-red-800 shadow-sm'>
              <StopCircle className='mt-0.5 h-5 w-5 text-red-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-red-950'>
                  {subscriptionStatusLabel.STOPPED}
                </h4>
                <p className='text-xs text-red-850 mt-0.5 leading-relaxed font-medium'>
                  Đăng ký này không còn được sử dụng cho các phiên lập tuyến mới.
                </p>
              </div>
            </div>
          )}

          {sub.status === 'EXPIRED' && (
            <div className='flex items-start gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-slate-800 shadow-sm'>
              <AlertCircle className='mt-0.5 h-5 w-5 text-slate-500 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-slate-950'>
                  {subscriptionStatusLabel.EXPIRED}
                </h4>
                <p className='text-xs text-slate-700 mt-0.5 leading-relaxed font-medium'>
                  Đăng ký đã đến ngày kết thúc hiệu lực.
                </p>
              </div>
            </div>
          )}

          {isConfigurationIncomplete && (
            <div className='flex items-start gap-3 rounded-2xl border border-amber-200 bg-amber-50/60 p-4 text-amber-800 shadow-sm'>
              <Info className='mt-0.5 h-5 w-5 text-amber-600 shrink-0' />
              <div>
                <h4 className='font-bold text-sm text-amber-950'>
                  Cần bổ sung cấu hình
                </h4>
                <p className='text-xs text-amber-850 mt-0.5 leading-relaxed font-medium'>
                  Cần cấu hình đầy đủ điểm đón/trả hoặc tọa độ trước khi lập tuyến.
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Main Grid Layout */}
        <div className='grid grid-cols-1 lg:grid-cols-12 gap-6 items-start'>
          {/* Main Column */}
          <div className='lg:col-span-8 space-y-6'>
            {/* Service Overview */}
            <SchoolBusSection title='Tổng quan dịch vụ'>
              <div className='p-5 bg-white rounded-2xl border border-slate-200 shadow-sm'>
                <div className='grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4'>
                  <div className='space-y-1'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>
                      Học sinh
                    </span>
                    <div className='flex items-center gap-2'>
                      <div className='flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-violet-50 text-violet-600 border border-violet-100'>
                        <User className='h-3.5 w-3.5' />
                      </div>
                      <span className='text-sm font-bold text-slate-900'>
                        {sub.studentName}
                      </span>
                    </div>
                  </div>
                  <div className='space-y-1'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>
                      Mã học sinh
                    </span>
                    <span className='text-sm font-semibold text-slate-700 font-mono'>
                      {sub.studentCode || 'Chưa có'}
                    </span>
                  </div>
                  {!access.isParentOnly && (
                    <div className='space-y-1'>
                      <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>
                        Phụ huynh
                      </span>
                      <span className='text-sm font-semibold text-slate-700'>
                        {sub.parentName || 'Chưa có'}
                      </span>
                    </div>
                  )}
                  <div className='space-y-1'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>
                      Trường học
                    </span>
                    <div className='flex items-center gap-2'>
                      <div className='flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-blue-50 text-blue-600 border border-blue-100'>
                        <GraduationCap className='h-3.5 w-3.5' />
                      </div>
                      <span className='text-sm font-semibold text-slate-700'>
                        {sub.schoolName}
                      </span>
                    </div>
                  </div>
                  <div className='space-y-1 md:col-span-2'>
                    <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block'>
                      Mã trường
                    </span>
                    <span className='text-sm font-semibold text-slate-700 font-mono'>
                      {sub.schoolCode || 'Chưa có'}
                    </span>
                  </div>
                </div>
              </div>
            </SchoolBusSection>

            {/* Transport Configuration */}
            <SchoolBusSection title='Cấu hình đưa đón'>
              <div className='p-5 bg-white rounded-2xl border border-slate-200 shadow-sm space-y-4'>
                <div>
                  <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1'>
                    Phương án đi xe
                  </span>
                  <span className='inline-flex items-center rounded-lg border border-blue-100 bg-blue-50/50 px-2.5 py-1 text-xs font-bold text-blue-700'>
                    {TRIP_OPTION_LABELS[sub.tripOption] || sub.tripOption}
                  </span>
                </div>

                <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                  {/* Pickup Point */}
                  <div className='rounded-xl border border-slate-200 bg-slate-50/50 p-4'>
                    <div className='flex items-center gap-1.5 mb-2'>
                      <span className='text-[9px] font-extrabold text-blue-600 uppercase tracking-widest bg-blue-100/60 px-1.5 py-0.5 rounded'>
                        P
                      </span>
                      <h4 className='text-xs font-bold text-slate-500 uppercase tracking-wider'>
                        Điểm đón
                      </h4>
                    </div>
                    {sub.pickupPointName ? (
                      <div className='min-w-0'>
                        <p className='text-sm font-bold text-slate-900 truncate'>
                          {sub.pickupPointName}
                        </p>
                        <p className='text-[10px] text-slate-400 font-mono mt-1'>
                          Mã: {sub.pickupPointCode || 'Chưa có'}
                        </p>
                      </div>
                    ) : (
                      <p className='text-sm font-semibold text-amber-600 italic bg-amber-50/60 px-2.5 py-1.5 rounded-lg border border-amber-100/30 inline-block'>
                        Thiếu cấu hình
                      </p>
                    )}
                  </div>

                  {/* Drop-off Point */}
                  <div className='rounded-xl border border-slate-200 bg-slate-50/50 p-4'>
                    <div className='flex items-center gap-1.5 mb-2'>
                      <span className='text-[9px] font-extrabold text-emerald-600 uppercase tracking-widest bg-emerald-100/60 px-1.5 py-0.5 rounded'>
                        D
                      </span>
                      <h4 className='text-xs font-bold text-slate-500 uppercase tracking-wider'>
                        Điểm trả
                      </h4>
                    </div>
                    {sub.dropoffPointName ? (
                      <div className='min-w-0'>
                        <p className='text-sm font-bold text-slate-900 truncate'>
                          {sub.dropoffPointName}
                        </p>
                        <p className='text-[10px] text-slate-400 font-mono mt-1'>
                          Mã: {sub.dropoffPointCode || 'Chưa có'}
                        </p>
                      </div>
                    ) : (
                      <p className='text-sm font-semibold text-amber-600 italic bg-amber-50/60 px-2.5 py-1.5 rounded-lg border border-amber-100/30 inline-block'>
                        Thiếu cấu hình
                      </p>
                    )}
                  </div>
                </div>
              </div>
            </SchoolBusSection>

            {/* Active days */}
            <SchoolBusSection title='Ngày hoạt động'>
              <div className='p-5 bg-white rounded-2xl border border-slate-200 shadow-sm space-y-4'>
                <div>
                  <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-2'>
                    Ngày hoạt động
                  </span>
                  <div className='flex flex-wrap gap-1.5'>
                    {DAY_KEYS.map(({ key, label }) => {
                      const isActive = sub[key];
                      return (
                        <span
                          key={key}
                          className={cn(
                            'rounded-lg px-2.5 py-1 text-xs font-bold tracking-wide border shadow-sm transition-all',
                            isActive
                              ? 'bg-[#FDECEF] text-[#C81E3A] border-[#F6CDD5]'
                              : 'bg-slate-50 text-slate-400 border-slate-200/60 line-through'
                          )}
                        >
                          {label}
                        </span>
                      );
                    })}
                  </div>
                </div>
              </div>
            </SchoolBusSection>

            {/* Planning Eligibility Checklist - Admin/Dispatcher only */}
            {!access.isParentOnly && (
              <SchoolBusSection title='Điều kiện tham gia lập tuyến'>
                <div
                  className={cn(
                    'p-5 rounded-2xl border shadow-sm space-y-4 transition-all',
                    isEligible
                      ? 'bg-emerald-50/10 border-emerald-200'
                      : 'bg-amber-50/10 border-amber-200'
                  )}
                >
                  <div className='flex items-center justify-between pb-3 border-b border-slate-200/60'>
                    <span className='text-sm text-slate-500 font-semibold'>
                      Hiển thị trong lập tuyến:
                    </span>
                    <span
                      className={cn(
                        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold shadow-sm border',
                        isEligible
                          ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                          : 'bg-amber-50 text-amber-700 border-amber-200'
                      )}
                    >
                      {isEligible ? 'Có (đủ điều kiện)' : 'Không (chưa đủ điều kiện)'}
                    </span>
                  </div>

                  <div className='space-y-2.5 text-xs font-medium'>
                    {/* 1. Status Check */}
                    <div className='flex items-center gap-2'>
                      {isStatusActive ? (
                        <span className='text-emerald-600 font-bold'>Đạt</span>
                      ) : (
                        <span className='text-amber-500 font-bold'>Cảnh báo:</span>
                      )}
                      <span className='text-slate-500 font-medium'>
                        Trạng thái đăng ký đang hoạt động
                      </span>
                      <span className='ml-auto text-slate-700'>
                        {isStatusActive ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                      </span>
                    </div>

                    {/* 2. Effective date check */}
                    <div className='flex items-center gap-2'>
                      {isDateValid ? (
                        <span className='text-emerald-600 font-bold'>Đạt</span>
                      ) : (
                        <span className='text-amber-500 font-bold'>Cảnh báo:</span>
                      )}
                      <span className='text-slate-500 font-medium'>
                        Thời hạn hiệu lực hợp lệ
                      </span>
                      <span className='ml-auto text-slate-700'>
                        {isDateValid
                          ? 'Trong thời gian phục vụ'
                          : 'Ngoài thời gian phục vụ'}
                      </span>
                    </div>

                    {/* 3. Days selected check */}
                    <div className='flex items-center gap-2'>
                      {hasDays ? (
                        <span className='text-emerald-600 font-bold'>Đạt</span>
                      ) : (
                        <span className='text-amber-500 font-bold'>Cảnh báo:</span>
                      )}
                      <span className='text-slate-500 font-medium'>
                        Đã khai báo ngày chạy tuyến
                      </span>
                      <span className='ml-auto text-slate-700'>
                        {hasDays ? 'Có' : 'Chưa chọn ngày'}
                      </span>
                    </div>

                    {/* 4. Points configured check */}
                    <div className='flex items-center gap-2'>
                      {pickupPointConfigured && dropoffPointConfigured ? (
                        <span className='text-emerald-600 font-bold'>Đạt</span>
                      ) : (
                        <span className='text-amber-500 font-bold'>Cảnh báo:</span>
                      )}
                      <span className='text-slate-500 font-medium'>
                        Đã cấu hình điểm đón/trả
                      </span>
                      <span className='ml-auto text-slate-700'>
                        {pickupPointConfigured && dropoffPointConfigured
                          ? 'Đã cấu hình'
                          : 'Thiếu điểm đón/trả'}
                      </span>
                    </div>
                  </div>

                  <div className='rounded-xl bg-slate-50 p-3 text-slate-500 text-[11px] font-semibold leading-relaxed border border-slate-200/60'>
                    Lưu ý: khả năng tham gia lập tuyến chi tiết, như tọa độ và liên kết
                    với trường học, sẽ được kiểm tra trong giai đoạn lập tuyến.
                  </div>
                </div>
              </SchoolBusSection>
            )}
          </div>

          {/* Sidebar Column */}
          <div className='lg:col-span-4 space-y-6'>
            {/* Quick Summary Card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <h3 className='text-xs font-bold uppercase tracking-wider text-slate-500 pb-2 border-b border-slate-100'>
                Tóm tắt nhanh
              </h3>
              <div className='space-y-3.5 text-sm'>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>
                    Mã đăng ký
                  </span>
                  <span className='font-mono font-bold text-slate-900'>
                    {sub.subscriptionCode}
                  </span>
                </div>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>
                    Trạng thái
                  </span>
                  <SchoolBusStatusBadge
                    status={sub.status}
                    labelMap={subscriptionStatusLabel}
                  />
                </div>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>
                    Hiệu lực từ
                  </span>
                  <span className='font-semibold text-slate-700'>
                    {formatDate(sub.effectiveFrom)}
                  </span>
                </div>
                <div>
                  <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>
                    Hiệu lực đến
                  </span>
                  <span className='font-semibold text-slate-700'>
                    {sub.effectiveTo ? formatDate(sub.effectiveTo) : 'Đang áp dụng'}
                  </span>
                </div>
              </div>
            </div>

            {/* Source Request */}
            {sub.sourceRequestId && (
              <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-3'>
                <h3 className='text-xs font-bold uppercase tracking-wider text-slate-500 pb-2 border-b border-slate-100'>
                  Yêu cầu nguồn
                </h3>
                <div className='space-y-3'>
                  <div>
                    <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>
                      Mã yêu cầu
                    </span>
                    <span className='font-semibold text-slate-900'>
                      {sub.sourceRequestCode || `REQ-${sub.sourceRequestId}`}
                    </span>
                  </div>
                  <div>
                    <span className='text-[10px] text-slate-400 font-bold block mb-0.5 uppercase tracking-wide'>
                      ID yêu cầu
                    </span>
                    <span className='font-semibold text-slate-700 font-mono text-xs'>
                      #{sub.sourceRequestId}
                    </span>
                  </div>
                  <div className='pt-1'>
                    <Link href={`/school-bus/requests/${sub.sourceRequestId}`}>
                      <Button
                        variant='outline'
                        className='h-8 text-xs rounded-xl font-bold border-slate-200 text-slate-700 hover:bg-slate-50 w-full'
                      >
                        Xem yêu cầu nguồn
                      </Button>
                    </Link>
                  </div>
                </div>
              </div>
            )}

          </div>
        </div>
      </div>
    </SchoolBusPageShell>
  );
}

