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
  XCircle,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useApproveTransportRequestMutation,
  useGetTransportRequestsQuery,
  useRejectTransportRequestMutation,
} from '../api/schoolBusApi';
import { RejectTransportRequestDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { schoolBusUi } from '../theme';
import type { SchoolBusTransportRequest } from '../types';
import { formatDate, formatDateTime, getPageItems } from '../utils';
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
  const { data, isLoading } = useGetTransportRequestsQuery(pagination.params);
  const [approveTransportRequest, { isLoading: approving }] =
    useApproveTransportRequestMutation();
  const [rejectTransportRequest, { isLoading: rejecting }] =
    useRejectTransportRequestMutation();
  const [processingId, setProcessingId] = React.useState<number | null>(null);
  const [rejectingRequest, setRejectingRequest] =
    React.useState<SchoolBusTransportRequest | null>(null);

  const requests = getPageItems(data?.data);
  const pendingRequests = requests.filter((request) => request.status === 'SUBMITTED');
  const approvedRequests = requests.filter(
    (request) => request.status === 'APPROVED'
  ).length;
  const rejectedRequests = requests.filter(
    (request) => request.status === 'REJECTED'
  ).length;

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
            value={requests.length}
            hint='Demand records currently stored in the tenant'
            icon={ClipboardList}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Pending approvals'
            value={pendingRequests.length}
            hint='Requests waiting on dispatcher action'
            icon={Clock3}
            tone='warning'
          />
          <SchoolBusMetricCard
            label='Approved requests'
            value={approvedRequests}
            hint='Requests already released toward planning'
            icon={CheckCircle2}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Rejected requests'
            value={rejectedRequests}
            hint='Demand records closed without routing'
            icon={XCircle}
            tone='default'
          />
        </div>

        <div className='grid gap-6 xl:grid-cols-[0.9fr_1.1fr]'>
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
              <div className='space-y-3'>
                {pendingRequests.map((request) => (
                  <div key={request.id} className={schoolBusUi.interactiveCard}>
                    <div className='flex items-start justify-between gap-4'>
                      <div className='space-y-1'>
                        <p className='font-medium'>{request.parentProfileName}</p>
                        <p className='text-sm text-muted-foreground'>
                          {request.schoolName} - {request.requestType}
                        </p>
                        <p className='text-xs text-muted-foreground'>
                          Effective from {formatDate(request.effectiveFrom)}
                        </p>
                      </div>
                      <div className='flex flex-wrap gap-2'>
                        <Button
                          size='sm'
                          variant='outline'
                          asChild
                          className='rounded-full'
                        >
                          <Link href={`/school-bus/requests/${request.id}`}>
                            View
                          </Link>
                        </Button>
                        <Button
                          size='sm'
                          variant='outline'
                          className='rounded-full'
                          disabled={rejecting && processingId === request.id}
                          onClick={() => setRejectingRequest(request)}
                        >
                          Reject
                        </Button>
                        <Button
                          size='sm'
                          className='rounded-full'
                          disabled={approving && processingId === request.id}
                          onClick={() => handleApprove(request.id)}
                        >
                          {approving && processingId === request.id
                            ? 'Approving...'
                            : 'Approve'}
                        </Button>
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
            {isLoading ? (
              <p className='text-sm text-muted-foreground'>Loading requests...</p>
            ) : requests.length === 0 ? (
              <SchoolBusEmptyState
                title='No transport requests found'
                description='Create the first request to start the request-to-dispatch flow.'
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
                    <TableHead>School</TableHead>
                    <TableHead>Approval state</TableHead>
                    <TableHead>Window</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {requests.map((request) => (
                    <TableRow key={request.id}>
                      <TableCell>
                        <div>
                          <p className='font-medium'>{request.requestType}</p>
                          <p className='text-xs text-muted-foreground'>
                            {request.parentProfileName}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell>{request.schoolName}</TableCell>
                      <TableCell>
                        <div className='space-y-2'>
                          <SchoolBusStatusBadge status={request.status} />
                          {request.approvedAt ? (
                            <p className='text-xs text-muted-foreground'>
                              Approved at {formatDateTime(request.approvedAt)}
                            </p>
                          ) : request.rejectionReason ? (
                            <p className='text-xs text-muted-foreground'>
                              {request.rejectionReason}
                            </p>
                          ) : null}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className='text-sm'>
                          <p>{formatDate(request.effectiveFrom)}</p>
                          <p className='text-xs text-muted-foreground'>
                            {request.effectiveTo
                              ? formatDate(request.effectiveTo)
                              : 'Open ended'}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell className='text-right'>
                        <div className='flex justify-end gap-2'>
                          <Button size='icon' variant='outline' asChild>
                            <Link href={`/school-bus/requests/${request.id}`}>
                              <Eye className='h-4 w-4' />
                            </Link>
                          </Button>
                          {request.status === 'SUBMITTED' ? (
                            <>
                              <Button size='icon' variant='outline' asChild>
                                <Link href={`/school-bus/requests/${request.id}/edit`}>
                                  <Pencil className='h-4 w-4' />
                                </Link>
                              </Button>
                              <Button
                                size='sm'
                                variant='outline'
                                disabled={rejecting && processingId === request.id}
                                onClick={() => setRejectingRequest(request)}
                              >
                                Reject
                              </Button>
                              <Button
                                size='sm'
                                disabled={approving && processingId === request.id}
                                onClick={() => handleApprove(request.id)}
                              >
                                {approving && processingId === request.id
                                  ? 'Approving...'
                                  : 'Approve'}
                              </Button>
                            </>
                          ) : (
                            <span className='text-xs text-muted-foreground'>
                              No further action
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
