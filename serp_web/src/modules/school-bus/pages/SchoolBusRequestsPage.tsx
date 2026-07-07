'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CheckCircle2,
  Clock3,
  Eye,
  FileText,
  Pencil,
  Plus,
  Search,
  XCircle,
  GraduationCap,
  User,
  Calendar,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useApproveTransportRequestMutation,
  useGetTransportRequestSummaryQuery,
  useGetTransportRequestsQuery,
  useRejectTransportRequestMutation,
  useGetSchoolDropdownOptionsQuery,
} from '../api/schoolBusApi';
import { RejectTransportRequestDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { schoolBusUi } from '../theme';
import type { SchoolBusTransportRequest } from '../types';
import { REQUEST_TYPE_OPTIONS } from '../constants';
import { requestStatusLabel, requestTypeLabel } from '../schoolBusLabels';
import {
  formatDate,
  formatDateTime,
  getPageItems,
  SCHOOL_BUS_OPTION_QUERY,
  SCHOOL_BUS_PAGE_QUERY_OPTIONS,
} from '../utils';
import { useSchoolBusAccess } from '../security/schoolBusAccess';

export function SchoolBusRequestsPage() {
  const access = useSchoolBusAccess();
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  });
  const { setKeyword } = pagination;
  const { data, isLoading } = useGetTransportRequestsQuery(
    pagination.params,
    SCHOOL_BUS_PAGE_QUERY_OPTIONS
  );
  const { data: summaryData } = useGetTransportRequestSummaryQuery(undefined, {
    refetchOnMountOrArgChange: true,
  });
  const { data: schoolsData } = useGetSchoolDropdownOptionsQuery();

  const [approveTransportRequest, { isLoading: approving }] =
    useApproveTransportRequestMutation();
  const [rejectTransportRequest, { isLoading: rejecting }] =
    useRejectTransportRequestMutation();
  const [processingId, setProcessingId] = React.useState<number | null>(null);
  const [rejectingRequest, setRejectingRequest] =
    React.useState<SchoolBusTransportRequest | null>(null);

  const requests = getPageItems(data?.data);
  const schools = schoolsData?.data || [];

  // Stats (derived from unfiltered requests in current page for dashboard metrics)
  const summary = summaryData?.data;
  const totalRequestsCount = summary?.totalRequests ?? 0;
  const pendingRequestsCount = summary?.submittedRequests ?? 0;
  const approvedRequestsCount = summary?.approvedRequests ?? 0;
  const rejectedRequestsCount = summary?.rejectedRequests ?? 0;

  // Search and Filter states
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filterSchool, setFilterSchool] = React.useState<string>('');
  const [filterRequestType, setFilterRequestType] = React.useState<string>('');
  const [filterStatus, setFilterStatus] = React.useState<string>('');

  // Debounced search text updates the pagination keyword parameter for backend search
  const [debouncedSearch, setDebouncedSearch] = React.useState('');
  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);

  React.useEffect(() => {
    setKeyword(debouncedSearch || '');
  }, [debouncedSearch, setKeyword]);

  // Client-side filters
  const filteredRequests = React.useMemo(() => {
    let result = requests;
    if (filterSchool) {
      result = result.filter((r) => r.schoolName === filterSchool);
    }
    if (filterRequestType) {
      result = result.filter((r) => r.requestType === filterRequestType);
    }
    if (filterStatus) {
      result = result.filter((r) => r.status === filterStatus);
    }
    return result;
  }, [requests, filterSchool, filterRequestType, filterStatus]);

  // School options derived from actual list
  const schoolOptions = React.useMemo(() => {
    return schools.map((s) => ({
      label: s.label,
      value: s.label,
    }));
  }, [schools]);

  // Pending queue for the "Approval focus" panel
  const pendingRequests = React.useMemo(() => {
    return requests.filter((request) => request.status === 'SUBMITTED');
  }, [requests]);

  const handleApprove = async (id: number) => {
    try {
      setProcessingId(id);
      const response = await approveTransportRequest(id).unwrap();
      toast.success(response.message || 'Đã phê duyệt yêu cầu di chuyển');
    } catch (error: any) {
      toast.error(
        error?.data?.message || 'Không thể phê duyệt yêu cầu di chuyển'
      );
    } finally {
      setProcessingId(null);
    }
  };

  const handleReject = async (reason: { reason: string }) => {
    if (!rejectingRequest) {
      return;
    }

    try {
      setProcessingId(rejectingRequest.id);
      const response = await rejectTransportRequest({
        id: rejectingRequest.id,
        body: reason,
      }).unwrap();
      toast.success(response.message || 'Đã từ chối yêu cầu di chuyển');
      setRejectingRequest(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể từ chối yêu cầu di chuyển');
    } finally {
      setProcessingId(null);
    }
  };

  const getRequestTypeDetails = (type: string) => {
    switch (type) {
      case 'NEW_SERVICE':
        return {
          label: requestTypeLabel.NEW_SERVICE,
          iconColor: 'bg-emerald-50 text-emerald-700 border-emerald-100/50',
          badgeColor: 'bg-emerald-50 text-emerald-700 border-emerald-100/50',
        };
      case 'CHANGE_SERVICE':
        return {
          label: requestTypeLabel.CHANGE_SERVICE,
          iconColor: 'bg-blue-50 text-blue-700 border-blue-100/50',
          badgeColor: 'bg-blue-50 text-blue-700 border-blue-100/50',
        };
      case 'STOP_SERVICE':
        return {
          label: requestTypeLabel.STOP_SERVICE,
          iconColor: 'bg-rose-50 text-rose-700 border-rose-100/50',
          badgeColor: 'bg-rose-50 text-rose-700 border-rose-100/50',
        };
      default:
        return {
          label: requestTypeLabel[type] || type,
          iconColor: 'bg-slate-50 text-slate-700 border-slate-100/50',
          badgeColor: 'bg-slate-50 text-slate-700 border-slate-100/50',
        };
    }
  };

  const requestColumns: SchoolBusTableColumn<SchoolBusTransportRequest>[] =
    React.useMemo(() => {
      const cols: SchoolBusTableColumn<SchoolBusTransportRequest>[] = [
        {
          key: 'request',
          header: 'Yêu cầu',
          className: 'pl-6',
          headerClassName: 'pl-6',
          render: (request) => {
            const typeDetails = getRequestTypeDetails(request.requestType);
            return (
              <div className='flex items-center gap-3'>
                <div
                  className={cn(
                    'flex h-8 w-8 shrink-0 items-center justify-center rounded-xl border',
                    typeDetails.iconColor
                  )}
                >
                  <FileText className='h-4.5 w-4.5' />
                </div>
                <div>
                  <p className='font-bold text-xs text-slate-800 font-mono leading-none'>
                    #{request.requestCode || request.id}
                  </p>
                  <span
                    className={cn(
                      'inline-flex items-center rounded-md border px-2 py-0.5 text-[9px] font-semibold mt-1.5 leading-none',
                      typeDetails.badgeColor
                    )}
                  >
                    {typeDetails.label}
                  </span>
                </div>
              </div>
            );
          },
        },
      ];

      // Parent column - only for Admin/Dispatcher
      if (!access.isParentOnly) {
        cols.push({
          key: 'parent',
          header: 'Phụ huynh',
          render: (request) => (
            <div className='flex items-center gap-2'>
              <User className='h-4 w-4 text-slate-400 shrink-0' />
              <span className='font-semibold text-xs text-slate-700'>
                {request.parentProfileName}
              </span>
            </div>
          ),
        });
      }

      cols.push(
        {
          key: 'school',
          header: 'Trường học',
          render: (request) => (
            <div className='flex items-center gap-2'>
              <GraduationCap className='h-4 w-4 text-slate-400 shrink-0' />
              <span
                className='text-xs text-slate-700 truncate max-w-[160px]'
                title={request.schoolName}
              >
                {request.schoolName}
              </span>
            </div>
          ),
        },
        {
          key: 'students',
          header: 'Học sinh',
          render: (request: any) => (
            <div className='flex items-center gap-1.5'>
              <span className='font-medium text-xs text-slate-600 bg-slate-50 px-2 py-0.5 rounded-lg border border-slate-100'>
                {request.studentCount != null
                  ? `${request.studentCount} học sinh`
                  : '-'}
              </span>
            </div>
          ),
        },
        {
          key: 'approvalState',
          header: 'Trạng thái',
          render: (request) => (
            <div className='space-y-1'>
              <SchoolBusStatusBadge
                status={request.status}
                labelMap={requestStatusLabel}
              />
              {request.approvedAt ? (
                <p className='text-[10px] text-slate-400'>
                  Đã duyệt: {formatDateTime(request.approvedAt)}
                </p>
              ) : request.rejectionReason ? (
                <p
                  className='text-[10px] text-rose-500 truncate max-w-[150px]'
                  title={request.rejectionReason}
                >
                  Lý do: {request.rejectionReason}
                </p>
              ) : null}
            </div>
          ),
        },
        {
          key: 'window',
          header: 'Thời gian hiệu lực',
          render: (request) => (
            <div className='text-xs text-slate-700 flex items-start gap-1.5'>
              <Calendar className='h-3.5 w-3.5 text-slate-400 mt-0.5 shrink-0' />
              <div>
                <p className='font-semibold text-slate-800 leading-none'>
                  {formatDate(request.effectiveFrom)}
                </p>
                <p className='text-[10px] text-slate-400 mt-1 leading-none'>
                  {request.effectiveTo
                    ? formatDate(request.effectiveTo)
                    : 'Không xác định'}
                </p>
              </div>
            </div>
          ),
        },
        {
          key: 'actions',
          header: 'Thao tác',
          className: 'pr-6 text-right',
          headerClassName: 'pr-6 text-right',
          render: (request) => {
            const canEdit =
              request.status === 'SUBMITTED' || request.status === 'REJECTED';
            const canCancel = request.status === 'SUBMITTED';

            return (
              <div className='flex justify-end gap-2 items-center'>
                <Button
                  size='icon'
                  variant='outline'
                  className='h-8 w-8 text-slate-500 border-slate-200 hover:bg-slate-50 hover:text-slate-700'
                  asChild
                >
                  <Link
                    href={`/school-bus/requests/${request.id}`}
                    title='Xem chi tiết'
                  >
                    <Eye className='h-4 w-4 text-slate-600' />
                  </Link>
                </Button>
                {access.isParentOnly ? (
                  <>
                    {canEdit && (
                      <Button
                        size='icon'
                        variant='outline'
                        className='h-8 w-8 text-slate-500 border-slate-200 hover:bg-slate-50 hover:text-slate-700'
                        asChild
                      >
                        <Link
                          href={`/school-bus/requests/${request.id}/edit`}
                          title='Sửa yêu cầu'
                        >
                          <Pencil className='h-4 w-4 text-slate-600' />
                        </Link>
                      </Button>
                    )}
                    {!canEdit && (
                      <span className='inline-flex items-center rounded-md bg-slate-50 px-2 py-0.5 text-[10px] font-medium text-slate-400 border border-slate-200/50'>
                        Đã khóa
                      </span>
                    )}
                  </>
                ) : request.status === 'SUBMITTED' &&
                  access.canApproveRequests ? (
                  <>
                    <Button
                      size='icon'
                      variant='outline'
                      className='h-8 w-8 text-slate-500 border-slate-200 hover:bg-slate-50 hover:text-slate-700'
                      asChild
                    >
                      <Link
                        href={`/school-bus/requests/${request.id}/edit`}
                        title='Sửa yêu cầu'
                      >
                        <Pencil className='h-4 w-4 text-slate-600' />
                      </Link>
                    </Button>
                    <Button
                      size='sm'
                      variant='outline'
                      className='h-8 text-red-700 hover:bg-red-50 hover:text-red-800 border-red-200 hover:border-red-300 rounded-lg text-xs font-semibold px-2.5'
                      disabled={rejecting && processingId === request.id}
                      onClick={() => setRejectingRequest(request)}
                    >
                      Từ chối
                    </Button>
                    <Button
                      size='sm'
                      className='h-8 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-lg text-xs font-semibold px-2.5'
                      disabled={approving && processingId === request.id}
                      onClick={() => handleApprove(request.id)}
                    >
                      {approving && processingId === request.id
                        ? '...'
                        : 'Duyệt'}
                    </Button>
                  </>
                ) : (
                  <span className='inline-flex items-center rounded-md bg-slate-50 px-2 py-0.5 text-[10px] font-medium text-slate-400 border border-slate-200/50'>
                    {request.status === 'SUBMITTED' ? 'Đang chờ' : 'Đã khóa'}
                  </span>
                )}
              </div>
            );
          },
        }
      );

      return cols;
    }, [
      access.isParentOnly,
      access.canApproveRequests,
      approving,
      rejecting,
      processingId,
    ]);

  const requestToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[200px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Tìm theo phụ huynh, trường học hoặc mã yêu cầu...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>

      <SchoolBusSelect
        value={filterSchool}
        onChange={setFilterSchool}
        placeholder='Tất cả trường học'
        options={schoolOptions}
        clearable
        searchable
      />

      <SchoolBusSelect
        value={filterRequestType}
        onChange={setFilterRequestType}
        placeholder='Tất cả loại yêu cầu'
        options={REQUEST_TYPE_OPTIONS.map((opt) => ({
          label: opt.label,
          value: opt.value,
        }))}
        clearable
      />

      <SchoolBusSelect
        value={filterStatus}
        onChange={setFilterStatus}
        placeholder='Tất cả trạng thái'
        options={[
          { label: 'Chờ duyệt', value: 'SUBMITTED' },
          { label: 'Đã duyệt', value: 'APPROVED' },
          { label: 'Từ chối', value: 'REJECTED' },
        ]}
        clearable
      />
    </div>
  );

  return (
    <>
      <SchoolBusPageShell
        title='Yêu cầu xe bus'
        description={
          access.isParentOnly
            ? 'Theo dõi trạng thái yêu cầu xe bus và tạo yêu cầu dịch vụ mới.'
            : 'Quản lý hàng đợi phê duyệt và chuyển nhu cầu hợp lệ sang lập tuyến.'
        }
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
              { label: 'Yêu cầu xe bus', current: true },
            ]}
          />
        }
        actions={
          access.canWriteRequest ? (
            <Button
              asChild
              className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-medium'
            >
              <Link href='/school-bus/requests/new'>
                <Plus className='h-4 w-4' />
                Tạo yêu cầu
              </Link>
            </Button>
          ) : undefined
        }
      >
        <div className='flex flex-col gap-6'>
          {/* Metrics grid */}
          <div className='grid gap-4 md:grid-cols-4'>
            <SchoolBusMetricCard
              label='Tổng yêu cầu'
              value={totalRequestsCount}
              icon={FileText}
              tone='info'
            />
            <SchoolBusMetricCard
              label={
                access.isParentOnly ? 'Yêu cầu chờ xử lý' : 'Yêu cầu chờ duyệt'
              }
              value={pendingRequestsCount}
              icon={Clock3}
              tone='warning'
            />
            <SchoolBusMetricCard
              label='Yêu cầu đã duyệt'
              value={approvedRequestsCount}
              icon={CheckCircle2}
              tone='success'
            />
            <SchoolBusMetricCard
              label='Yêu cầu bị từ chối'
              value={rejectedRequestsCount}
              icon={XCircle}
              tone='default'
            />
          </div>

          {/* Conditional Layout for Approval Focus */}
          <div
            className={cn(
              'grid gap-6',
              pendingRequests.length > 0 && !access.isParentOnly
                ? 'xl:grid-cols-[400px_1fr]'
                : 'grid-cols-1'
            )}
          >
            {pendingRequests.length > 0 &&
              access.canApproveRequests &&
              !access.isParentOnly && (
                <SchoolBusSection
                  title='Yêu cầu cần phê duyệt'
                  description='Danh sách yêu cầu chờ duyệt cần được xử lý trước.'
                >
                  <div className='space-y-3 max-h-[600px] overflow-y-auto pr-1'>
                    {pendingRequests.map((request) => {
                      const typeDetails = getRequestTypeDetails(
                        request.requestType
                      );
                      return (
                        <div
                          key={request.id}
                          className={`${schoolBusUi.interactiveCard} p-4 flex flex-col gap-3 border border-slate-100 hover:shadow-md transition-all shadow-sm rounded-2xl bg-white`}
                        >
                          <div className='flex items-start justify-between gap-4'>
                            <div className='space-y-1.5'>
                              <div className='flex items-center gap-2 flex-wrap'>
                                <span
                                  className={cn(
                                    'inline-flex items-center rounded-md border px-2 py-0.5 text-[9px] font-semibold leading-none',
                                    typeDetails.badgeColor
                                  )}
                                >
                                  {typeDetails.label}
                                </span>
                                <span className='text-[11px] text-muted-foreground font-mono font-semibold'>
                                  #{request.requestCode || request.id}
                                </span>
                              </div>
                              <p className='font-bold text-slate-800 text-sm flex items-center gap-1.5'>
                                <User className='h-4 w-4 text-slate-400 shrink-0' />
                                {request.parentProfileName}
                              </p>
                            </div>

                            <div className='flex gap-1 shrink-0'>
                              <Button
                                size='sm'
                                variant='outline'
                                asChild
                                className='rounded-full text-xs h-7 px-2.5 hover:bg-slate-50'
                              >
                                <Link
                                  href={`/school-bus/requests/${request.id}`}
                                >
                                  Xem xét
                                </Link>
                              </Button>
                              <Button
                                size='sm'
                                variant='outline'
                                className='rounded-full text-xs h-7 px-2.5 border-red-200 text-red-700 hover:bg-red-50 hover:text-red-800 hover:border-red-300'
                                disabled={
                                  rejecting && processingId === request.id
                                }
                                onClick={() => setRejectingRequest(request)}
                              >
                                Từ chối
                              </Button>
                              <Button
                                size='sm'
                                className='rounded-full text-xs h-7 px-2.5 bg-[#C81E3A] hover:bg-[#B31B34] text-white'
                                disabled={
                                  approving && processingId === request.id
                                }
                                onClick={() => handleApprove(request.id)}
                              >
                                {approving && processingId === request.id
                                  ? '...'
                                  : 'Duyệt'}
                              </Button>
                            </div>
                          </div>

                          <div className='grid grid-cols-2 gap-2 border-t border-slate-50 pt-2.5 text-[11px] text-muted-foreground'>
                            <div>
                              <span className='font-bold text-slate-500 uppercase tracking-wider text-[9px] block'>
                                Trường học
                              </span>
                              <span
                                className='font-medium text-slate-700 truncate block max-w-[155px]'
                                title={request.schoolName}
                              >
                                {request.schoolName}
                              </span>
                            </div>
                            <div>
                              <span className='font-bold text-slate-500 uppercase tracking-wider text-[9px] block'>
                                Thời gian hiệu lực
                              </span>
                              <span className='font-medium text-slate-700 flex items-center gap-1'>
                                <Calendar className='h-3 w-3 text-slate-400 shrink-0' />
                                {formatDate(request.effectiveFrom)}
                                {request.effectiveTo
                                  ? ` - ${formatDate(request.effectiveTo)}`
                                  : ' (không giới hạn)'}
                              </span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </SchoolBusSection>
              )}

            <div className={pendingRequests.length === 0 ? 'w-full' : ''}>
              <SchoolBusSection
                title='Tất cả yêu cầu'
                description='Xem xét, phê duyệt, từ chối và chỉnh sửa nhu cầu xe bus.'
              >
                {pendingRequests.length === 0 && !access.isParentOnly && (
                  <div className='mb-4 flex items-center justify-between p-4 bg-emerald-50/50 border border-emerald-100/50 rounded-2xl text-emerald-800 text-xs shadow-sm'>
                    <div className='flex items-center gap-2.5'>
                      <div className='flex h-6 w-6 shrink-0 items-center justify-center rounded-lg bg-emerald-100 text-emerald-700 border border-emerald-200/50'>
                        <CheckCircle2 className='h-3.5 w-3.5' />
                      </div>
                      <div>
                        <span className='font-bold text-emerald-900'>
                          Không có yêu cầu chờ duyệt
                        </span>
                        <span className='text-slate-500 ml-1.5'>
                          Hàng đợi phê duyệt hiện đang trống.
                        </span>
                      </div>
                    </div>
                  </div>
                )}
                <SchoolBusDataTable
                  toolbar={requestToolbar}
                  data={filteredRequests}
                  columns={requestColumns}
                  isLoading={isLoading}
                  pagination={{
                    page: data?.data,
                    onPageChange: pagination.setPage,
                  }}
                  stickyFirstColumn
                  stickyActionColumn
                  emptyIcon={FileText}
                  emptyTitle={
                    requests.length === 0
                      ? 'Không tìm thấy yêu cầu xe bus'
                      : 'Không có yêu cầu phù hợp với bộ lọc'
                  }
                  emptyDescription={
                    requests.length === 0
                      ? 'Không có yêu cầu phù hợp. Hãy điều chỉnh tiêu chí hoặc tạo yêu cầu mới.'
                      : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
                  }
                  className='border border-slate-200/80 shadow-sm'
                />
              </SchoolBusSection>
            </div>
          </div>
        </div>
      </SchoolBusPageShell>

      <RejectTransportRequestDialog
        open={Boolean(rejectingRequest)}
        onOpenChange={(open) => {
          if (!open) {
            setRejectingRequest(null);
          }
        }}
        onSubmit={handleReject}
        isLoading={rejecting}
      />
    </>
  );
}

