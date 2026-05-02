/**
 * Meeting requests inbox — scheduling queue and detail with activity link.
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project
 */

'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';
import { toast } from 'sonner';
import {
  CalendarClock,
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  Plus,
  XCircle,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { cn } from '@/shared/utils';
import { RequestMeetingDialog } from '../../components/meeting-requests';
import {
  useCancelMeetingRequestMutation,
  useGetMeetingRequestQuery,
  useGetMeetingRequestsQuery,
} from '../../api/crmApi';
import type { MeetingRequestStatus } from '../../types';
import {
  MEETING_REQUEST_STATUS_LABELS,
  MEETING_REQUEST_TYPE_LABELS,
} from '../../types/meetingRequest';

const STATUS_OPTIONS: Array<MeetingRequestStatus | 'ALL'> = [
  'ALL',
  'PENDING',
  'SCHEDULED',
  'FAILED',
  'CANCELLED',
];

function formatTs(ms?: number) {
  if (ms == null || Number.isNaN(ms)) return '—';
  return new Date(ms).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function statusBadgeClass(status: MeetingRequestStatus) {
  switch (status) {
    case 'PENDING':
      return 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200';
    case 'SCHEDULED':
      return 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200';
    case 'FAILED':
      return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
    case 'CANCELLED':
      return 'bg-muted text-muted-foreground';
    default:
      return 'bg-muted';
  }
}

export interface MeetingRequestsPageProps {
  className?: string;
}

export function MeetingRequestsPage({ className }: MeetingRequestsPageProps) {
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<
    MeetingRequestStatus | 'ALL'
  >('ALL');
  const [createOpen, setCreateOpen] = useState(false);
  const [detailId, setDetailId] = useState<string | null>(null);

  const queryArgs = useMemo(
    () => ({
      pagination: { page, limit: 20 },
      ...(statusFilter !== 'ALL' ? { status: statusFilter } : {}),
    }),
    [page, statusFilter]
  );

  const { data, isLoading, isFetching, refetch } =
    useGetMeetingRequestsQuery(queryArgs);

  const {
    data: detailResponse,
    isError: detailError,
    isLoading: detailLoading,
  } = useGetMeetingRequestQuery(detailId!, {
    skip: !detailId,
  });

  const [cancelMeetingRequest, { isLoading: cancelling }] =
    useCancelMeetingRequestMutation();

  const rows = data?.data?.data ?? [];
  const pagination = data?.data?.pagination;

  const detail = detailResponse?.success ? detailResponse.data : undefined;

  const handleCancel = async (id: string) => {
    try {
      const res = await cancelMeetingRequest(id).unwrap();
      if (!res.success) {
        toast.error(res.message || 'Could not cancel');
        return;
      }
      toast.success(
        res.message ||
          'Meeting request cancelled. Any linked activity was cancelled.'
      );
      setDetailId(null);
      refetch();
    } catch (err) {
      toast.error(getErrorMessage(err) || 'Cancel failed');
    }
  };

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Meeting requests
          </h1>
          <p className='text-muted-foreground'>
            Queue for automatic rep assignment and slot matching. When
            scheduled, a{' '}
            <Link
              href='/crm/activities'
              className='text-primary underline-offset-4 hover:underline'
            >
              Meeting activity
            </Link>{' '}
            is created.
          </p>
        </div>
        <Button className='gap-2' onClick={() => setCreateOpen(true)}>
          <Plus className='h-4 w-4' />
          New request
        </Button>
      </div>

      <Card>
        <CardHeader className='flex flex-row flex-wrap items-center justify-between gap-3'>
          <CardTitle className='flex items-center gap-2 text-lg'>
            <CalendarClock className='h-5 w-5' />
            Inbox
          </CardTitle>
          <div className='flex items-center gap-2'>
            <span className='text-sm text-muted-foreground'>Status</span>
            <Select
              value={statusFilter}
              onValueChange={(v) => {
                setStatusFilter(v as MeetingRequestStatus | 'ALL');
                setPage(1);
              }}
            >
              <SelectTrigger className='w-[180px]'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {STATUS_OPTIONS.map((s) => (
                  <SelectItem key={s} value={s}>
                    {s === 'ALL'
                      ? 'All statuses'
                      : MEETING_REQUEST_STATUS_LABELS[s]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p className='text-sm text-muted-foreground'>Loading…</p>
          ) : rows.length === 0 ? (
            <p className='text-sm text-muted-foreground'>
              No meeting requests yet. Create one to enqueue scheduling.
            </p>
          ) : (
            <div className='rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Subject / type</TableHead>
                    <TableHead>Account</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Window</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {rows.map((row) => (
                    <TableRow key={row.id}>
                      <TableCell className='font-medium'>
                        <div className='flex flex-col'>
                          <span>
                            {row.subject?.trim() ||
                              MEETING_REQUEST_TYPE_LABELS[row.meetingType]}
                          </span>
                          <span className='text-xs text-muted-foreground'>
                            {MEETING_REQUEST_TYPE_LABELS[row.meetingType]}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Link
                          href={`/crm/accounts/${row.accountId}`}
                          className='text-primary underline-offset-4 hover:underline'
                        >
                          #{row.accountId}
                        </Link>
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant='secondary'
                          className={statusBadgeClass(row.status)}
                        >
                          {MEETING_REQUEST_STATUS_LABELS[row.status]}
                        </Badge>
                      </TableCell>
                      <TableCell className='text-sm text-muted-foreground'>
                        {formatTs(row.earliestStart)} →{' '}
                        {formatTs(row.latestStart)}
                      </TableCell>
                      <TableCell className='text-right'>
                        <Button
                          variant='outline'
                          size='sm'
                          onClick={() => setDetailId(row.id)}
                        >
                          View
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}

          {pagination && pagination.totalPages > 1 && (
            <div className='mt-4 flex items-center justify-between'>
              <p className='text-sm text-muted-foreground'>
                Page {pagination.page} of {pagination.totalPages}
                {isFetching ? ' · Refreshing…' : ''}
              </p>
              <div className='flex gap-2'>
                <Button
                  variant='outline'
                  size='sm'
                  disabled={!pagination.hasPrevious}
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                >
                  <ChevronLeft className='h-4 w-4' />
                </Button>
                <Button
                  variant='outline'
                  size='sm'
                  disabled={!pagination.hasNext}
                  onClick={() => setPage((p) => p + 1)}
                >
                  <ChevronRight className='h-4 w-4' />
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <RequestMeetingDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        onSuccess={() => refetch()}
      />

      <Dialog
        open={!!detailId}
        onOpenChange={(open) => {
          if (!open) setDetailId(null);
        }}
      >
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-lg'>
          <DialogHeader>
            <DialogTitle>Meeting request detail</DialogTitle>
            <DialogDescription>
              Scheduling attempts and linked CRM activity (when scheduled).
            </DialogDescription>
          </DialogHeader>

          {detail && (
            <div className='space-y-3 text-sm'>
              <div className='flex flex-wrap items-center gap-2'>
                <Badge
                  variant='secondary'
                  className={statusBadgeClass(detail.status)}
                >
                  {MEETING_REQUEST_STATUS_LABELS[detail.status]}
                </Badge>
                <span className='text-muted-foreground'>
                  {MEETING_REQUEST_TYPE_LABELS[detail.meetingType]}
                </span>
              </div>

              <div>
                <p className='text-xs font-medium text-muted-foreground'>
                  Subject
                </p>
                <p>{detail.subject?.trim() || '—'}</p>
              </div>

              <div className='grid gap-2 sm:grid-cols-2'>
                <div>
                  <p className='text-xs font-medium text-muted-foreground'>
                    Account
                  </p>
                  <Link
                    href={`/crm/accounts/${detail.accountId}`}
                    className='text-primary underline-offset-4 hover:underline'
                  >
                    #{detail.accountId}
                  </Link>
                </div>
                {detail.opportunityId && (
                  <div>
                    <p className='text-xs font-medium text-muted-foreground'>
                      Opportunity
                    </p>
                    <Link
                      href={`/crm/opportunities/${detail.opportunityId}`}
                      className='text-primary underline-offset-4 hover:underline'
                    >
                      #{detail.opportunityId}
                    </Link>
                  </div>
                )}
              </div>

              <div>
                <p className='text-xs font-medium text-muted-foreground'>
                  Time window
                </p>
                <p>
                  {formatTs(detail.earliestStart)} →{' '}
                  {formatTs(detail.latestStart)}
                </p>
                <p className='text-xs text-muted-foreground'>
                  Deadline: {formatTs(detail.requestedDeadline)}
                </p>
              </div>

              {detail.scheduledStartTime != null && (
                <div>
                  <p className='text-xs font-medium text-muted-foreground'>
                    Scheduled start
                  </p>
                  <p>{formatTs(detail.scheduledStartTime)}</p>
                </div>
              )}

              {detail.scheduledActivityId && (
                <div className='rounded-md border bg-muted/40 p-3'>
                  <p className='mb-2 text-xs font-medium text-muted-foreground'>
                    Linked activity
                  </p>
                  <Button variant='outline' size='sm' asChild className='gap-2'>
                    <Link
                      href={`/crm/activities/${detail.scheduledActivityId}`}
                    >
                      Open Meeting activity
                      <ExternalLink className='h-3.5 w-3.5' />
                    </Link>
                  </Button>
                  <p className='mt-2 text-xs text-muted-foreground'>
                    Edit time, notes, or outcome from the activity screen or
                    calendar.
                  </p>
                </div>
              )}

              {detail.failureReason && (
                <div className='rounded-md border border-red-200 bg-red-50 p-3 text-red-900 dark:border-red-900 dark:bg-red-950/40 dark:text-red-100'>
                  <p className='text-xs font-medium'>Failure reason</p>
                  <p className='mt-1'>{detail.failureReason}</p>
                </div>
              )}

              {detail.schedulingAttempts != null && (
                <p className='text-xs text-muted-foreground'>
                  Scheduling attempts: {detail.schedulingAttempts}
                </p>
              )}
            </div>
          )}

          {detailLoading && detailId && (
            <p className='text-sm text-muted-foreground'>Loading detail…</p>
          )}
          {detailError && detailId && (
            <p className='text-sm text-destructive'>
              Could not load this meeting request.
            </p>
          )}

          <DialogFooter className='flex-col gap-2 sm:flex-row'>
            {detail &&
              detail.status !== 'CANCELLED' &&
              detail.status !== 'FAILED' && (
                <Button
                  variant='destructive'
                  className='gap-2'
                  disabled={cancelling}
                  onClick={() => detailId && handleCancel(detailId)}
                >
                  <XCircle className='h-4 w-4' />
                  Cancel request
                </Button>
              )}
            <Button variant='outline' onClick={() => setDetailId(null)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
