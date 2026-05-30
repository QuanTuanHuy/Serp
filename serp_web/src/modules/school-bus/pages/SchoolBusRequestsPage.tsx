'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CheckCircle2,
  ClipboardList,
  Clock3,
  Eye,
  Pencil,
  Plus,
  Search,
  XCircle,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useApproveTransportRequestMutation,
  useGetTransportRequestsQuery,
  useRejectTransportRequestMutation,
  useGetSchoolsQuery,
} from '../api/schoolBusApi';
import { RejectTransportRequestDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusFilterSelect } from '../components/SchoolBusFilterSelect';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { schoolBusUi } from '../theme';
import type { SchoolBusTransportRequest } from '../types';
import { REQUEST_TYPE_OPTIONS } from '../constants';
import { formatDate, formatDateTime, getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

export function SchoolBusRequestsPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  });
  const { setKeyword } = pagination;
  const { data, isLoading } = useGetTransportRequestsQuery(pagination.params);
  const { data: schoolsData } = useGetSchoolsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'name',
  });
  const [approveTransportRequest, { isLoading: approving }] =
    useApproveTransportRequestMutation();
  const [rejectTransportRequest, { isLoading: rejecting }] =
    useRejectTransportRequestMutation();
  const [processingId, setProcessingId] = React.useState<number | null>(null);
  const [rejectingRequest, setRejectingRequest] =
    React.useState<SchoolBusTransportRequest | null>(null);

  const requests = getPageItems(data?.data);
  const schools = getPageItems(schoolsData?.data);

  // Stats (derived from unfiltered requests in current page for dashboard metrics)
  const totalRequestsCount = data?.data?.totalElements || requests.length;
  const pendingRequestsCount = requests.filter((r) => r.status === 'SUBMITTED').length;
  const approvedRequestsCount = requests.filter((r) => r.status === 'APPROVED').length;
  const rejectedRequestsCount = requests.filter((r) => r.status === 'REJECTED').length;

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
      label: s.name,
      value: s.name,
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
      toast.success(response.message || 'Transport request approved');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to approve transport request');
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
      toast.success(response.message || 'Transport request rejected');
      setRejectingRequest(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to reject transport request');
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <>
      <SchoolBusPageShell
        title='Transport request queue'
        description='Manage the approval queue and move valid demand into planning without leaving the module.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Requests', current: true },
            ]}
          />
        }
        actions={
          <Button asChild className='rounded-full'>
            <Link href='/school-bus/requests/new'>
              <Plus className='h-4 w-4' />
              New request
            </Link>
          </Button>
        }
      >
        <div className='grid gap-4 md:grid-cols-4'>
          <SchoolBusMetricCard
            label='Total requests'
            value={totalRequestsCount}
            hint='Demand records currently stored in the tenant'
            icon={ClipboardList}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Pending approvals'
            value={pendingRequestsCount}
            hint='Requires dispatcher action'
            icon={Clock3}
            tone='warning'
          />
          <SchoolBusMetricCard
            label='Approved requests'
            value={approvedRequestsCount}
            hint='Ready to be included in planning'
            icon={CheckCircle2}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Rejected requests'
            value={rejectedRequestsCount}
            hint='Excluded from planning'
            icon={XCircle}
            tone='default'
          />
        </div>

        <div className='grid gap-6 xl:grid-cols-[0.8fr_1.2fr]'>
          <SchoolBusSection
            title='Approval focus'
            description='The pending subset remains the highest-value operational queue.'
          >
            {pendingRequests.length === 0 ? (
              <SchoolBusEmptyState
                title='No pending approvals'
                description='The queue is currently clear.'
                icon={Clock3}
                className='min-h-[220px]'
              />
            ) : (
              <div className='space-y-3 max-h-[600px] overflow-y-auto pr-1'>
                {pendingRequests.map((request) => (
                  <div
                    key={request.id}
                    className={`${schoolBusUi.interactiveCard} p-4 flex flex-col gap-3 border border-slate-100 hover:border-rose-200 hover:shadow-md transition-all shadow-sm rounded-2xl bg-white`}
                  >
                    <div className='flex items-start justify-between gap-4'>
                      <div className='space-y-1'>
                        <div className='flex items-center gap-2 flex-wrap'>
                          <span className='inline-flex items-center rounded-full bg-rose-50 px-2 py-0.5 text-[10px] font-semibold text-rose-700 ring-1 ring-rose-200/50'>
                            {request.requestType}
                          </span>
                          <span className='text-[11px] text-muted-foreground font-mono'>
                            #{request.requestCode || request.id}
                          </span>
                        </div>
                        <p className='font-semibold text-slate-900 text-sm mt-1'>
                          {request.parentProfileName}
                        </p>
                      </div>

                      <div className='flex gap-1.5 shrink-0'>
                        <Button
                          size='sm'
                          variant='outline'
                          asChild
                          className='rounded-full text-xs h-7 px-3'
                        >
                          <Link href={`/school-bus/requests/${request.id}`}>
                            Review
                          </Link>
                        </Button>
                        <Button
                          size='sm'
                          variant='outline'
                          className='rounded-full text-xs h-7 px-3 border-rose-200 text-rose-700 hover:bg-rose-50'
                          disabled={rejecting && processingId === request.id}
                          onClick={() => setRejectingRequest(request)}
                        >
                          Reject
                        </Button>
                        <Button
                          size='sm'
                          className='rounded-full text-xs h-7 px-3 bg-rose-600 hover:bg-rose-700 text-white'
                          disabled={approving && processingId === request.id}
                          onClick={() => handleApprove(request.id)}
                        >
                          {approving && processingId === request.id
                            ? '...'
                            : 'Approve'}
                        </Button>
                      </div>
                    </div>

                    <div className='grid grid-cols-2 gap-2 border-t border-slate-50 pt-2 text-[11px] text-muted-foreground'>
                      <div>
                        <span className='font-medium text-slate-700 block'>School</span>
                        <span className='truncate block max-w-[150px]' title={request.schoolName}>
                          {request.schoolName}
                        </span>
                      </div>
                      <div>
                        <span className='font-medium text-slate-700 block'>Effective window</span>
                        <span>
                          {formatDate(request.effectiveFrom)}
                          {request.effectiveTo ? ` — ${formatDate(request.effectiveTo)}` : ' (Open)'}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </SchoolBusSection>

          <SchoolBusSection
            title='All requests'
            description='Review, inspect, approve, reject, and edit transport demand.'
          >
            {/* Search & Filter Bar */}
            <div className='flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm mb-4'>
              <div className='relative flex-1 min-w-[200px]'>
                <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
                <input
                  type='text'
                  placeholder='Search by parent, school or code...'
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className='w-full rounded-lg border border-slate-200 bg-slate-50 py-1.5 pl-8 pr-3 text-xs outline-none focus:border-rose-300 focus:ring-1 focus:ring-rose-200'
                />
              </div>

              <SchoolBusFilterSelect
                value={filterSchool}
                onChange={setFilterSchool}
                placeholder='All schools'
                options={schoolOptions}
              />

              <SchoolBusFilterSelect
                value={filterRequestType}
                onChange={setFilterRequestType}
                placeholder='All types'
                options={REQUEST_TYPE_OPTIONS.map((opt) => ({
                  label: opt.label,
                  value: opt.value,
                }))}
              />

              <SchoolBusFilterSelect
                value={filterStatus}
                onChange={setFilterStatus}
                placeholder='All states'
                options={[
                  { label: 'Submitted (Pending)', value: 'SUBMITTED' },
                  { label: 'Approved', value: 'APPROVED' },
                  { label: 'Rejected', value: 'REJECTED' },
                ]}
              />
            </div>

            {isLoading ? (
              <p className='text-sm text-muted-foreground'>Loading requests...</p>
            ) : filteredRequests.length === 0 ? (
              <SchoolBusEmptyState
                title='No transport requests found'
                description='No requests match the current filters. Modify your search criteria or create a request.'
                icon={ClipboardList}
              />
            ) : (
              <SchoolBusScrollableTable
                footer={
                  <SchoolBusPaginationBar
                    page={data?.data}
                    onPageChange={pagination.setPage}
                  />
                }
              >
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Request</TableHead>
                      <TableHead>Parent</TableHead>
                      <TableHead>School</TableHead>
                      <TableHead>Approval state</TableHead>
                      <TableHead>Window</TableHead>
                      <TableHead className='text-right'>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredRequests.map((request) => (
                      <TableRow key={request.id}>
                        <TableCell>
                          <div>
                            <p className='font-semibold text-xs text-slate-800 font-mono'>
                              #{request.requestCode || request.id}
                            </p>
                            <span className='inline-flex items-center rounded-md bg-slate-100 px-2 py-0.5 text-[10px] font-medium text-slate-600 mt-1'>
                              {request.requestType}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <span className='font-medium text-sm'>{request.parentProfileName}</span>
                        </TableCell>
                        <TableCell>
                          <span className='text-sm'>{request.schoolName}</span>
                        </TableCell>
                        <TableCell>
                          <div className='space-y-1.5'>
                            <SchoolBusStatusBadge status={request.status} />
                            {request.approvedAt ? (
                              <p className='text-[10px] text-muted-foreground'>
                                Approved: {formatDateTime(request.approvedAt)}
                              </p>
                            ) : request.rejectionReason ? (
                              <p className='text-[10px] text-rose-600 truncate max-w-[150px]' title={request.rejectionReason}>
                                Reason: {request.rejectionReason}
                              </p>
                            ) : null}
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className='text-xs'>
                            <p className='font-medium'>{formatDate(request.effectiveFrom)}</p>
                            <p className='text-muted-foreground'>
                              {request.effectiveTo
                                ? formatDate(request.effectiveTo)
                                : 'Open ended'}
                            </p>
                          </div>
                        </TableCell>
                        <TableCell className='text-right'>
                          <div className='flex justify-end gap-2 items-center'>
                            <Button size='icon' variant='outline' className='h-8 w-8' asChild>
                              <Link href={`/school-bus/requests/${request.id}`} title='View Details'>
                                <Eye className='h-4 w-4 text-slate-600' />
                              </Link>
                            </Button>
                            {request.status === 'SUBMITTED' ? (
                              <>
                                <Button size='icon' variant='outline' className='h-8 w-8' asChild>
                                  <Link href={`/school-bus/requests/${request.id}/edit`} title='Edit Request'>
                                    <Pencil className='h-4 w-4 text-slate-600' />
                                  </Link>
                                </Button>
                                <Button
                                  size='sm'
                                  variant='outline'
                                  className='h-8 text-rose-700 hover:bg-rose-50 border-rose-200'
                                  disabled={rejecting && processingId === request.id}
                                  onClick={() => setRejectingRequest(request)}
                                >
                                  Reject
                                </Button>
                                <Button
                                  size='sm'
                                  className='h-8 bg-rose-600 text-white hover:bg-rose-700'
                                  disabled={approving && processingId === request.id}
                                  onClick={() => handleApprove(request.id)}
                                >
                                  {approving && processingId === request.id
                                    ? '...'
                                    : 'Approve'}
                                </Button>
                              </>
                            ) : (
                              <span className='text-[11px] text-muted-foreground italic px-2'>
                                Locked
                              </span>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </SchoolBusScrollableTable>
            )}
          </SchoolBusSection>
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
