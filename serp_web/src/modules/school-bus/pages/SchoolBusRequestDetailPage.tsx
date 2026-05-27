'use client';

import Link from 'next/link';
import * as React from 'react';
import { CheckCircle2, Pencil, XCircle } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useApproveTransportRequestMutation,
  useCancelTransportRequestMutation,
  useGetTransportRequestByIdQuery,
  useGetTransportRequestHistoryQuery,
  useRejectTransportRequestMutation,
} from '../api/schoolBusApi';
import { SchoolBusTimeline, mapRequestHistoryToTimeline } from '../components/history';
import { RejectTransportRequestDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { OperationsMap } from '../components/map/OperationsMap';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import type { StudentMapMarker } from '../components/map/OperationsMapClient';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

interface SchoolBusRequestDetailPageProps {
  requestId: number;
}

export function SchoolBusRequestDetailPage({
  requestId,
}: SchoolBusRequestDetailPageProps) {
  const { data, isLoading } = useGetTransportRequestByIdQuery(requestId);
  const [approveTransportRequest, { isLoading: approving }] =
    useApproveTransportRequestMutation();
  const [rejectTransportRequest, { isLoading: rejecting }] =
    useRejectTransportRequestMutation();
  const [cancelTransportRequest] = useCancelTransportRequestMutation();
  const { data: historyData, isLoading: historyLoading, isError: historyError } =
    useGetTransportRequestHistoryQuery(requestId);
  const [rejectOpen, setRejectOpen] = React.useState(false);
  const detail = data?.data;

  const handleApprove = async () => {
    try {
      const response = await approveTransportRequest(requestId).unwrap();
      toast.success(response.message || 'Transport request approved');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to approve transport request');
    }
  };

  const handleCancel = async () => {
    try {
      const response = await cancelTransportRequest(requestId).unwrap();
      toast.success(response.message || 'Transport request cancelled');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to cancel transport request');
    }
  };

  const handleReject = async (values: { reason: string }) => {
    try {
      const response = await rejectTransportRequest({
        id: requestId,
        body: values,
      }).unwrap();
      toast.success(response.message || 'Transport request rejected');
      setRejectOpen(false);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to reject transport request');
    }
  };

  if (isLoading || !detail) {
    return (
      <SchoolBusPageShell
        title='Transport request detail'
        description='Loading transport request...'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Requests', href: '/school-bus/requests' },
              { label: 'Detail', current: true },
            ]}
          />
        }
      >
        <SchoolBusEmptyState
          title='Loading request detail'
          description='Fetching request detail and linked students.'
        />
      </SchoolBusPageShell>
    );
  }

  const request = detail.request;

  return (
    <>
      <SchoolBusPageShell
        title={`Transport request #${request.id}`}
        description='Inspect the full request payload, review the student list, and take approval actions.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Requests', href: '/school-bus/requests' },
              { label: `#${request.id} — ${request.parentProfileName ?? 'Request'}`, current: true },
            ]}
          />
        }
        actions={
          <>
            {request.status === 'SUBMITTED' ? (
              <Button variant='outline' className='rounded-full' asChild>
                <Link href={`/school-bus/requests/${request.id}/edit`}>
                  <Pencil className='h-4 w-4' />
                  Edit
                </Link>
              </Button>
            ) : null}
            {request.status === 'SUBMITTED' || request.status === 'DRAFT' ? (
              <Button
                variant='outline'
                className='rounded-full'
                onClick={handleCancel}
              >
                Cancel
              </Button>
            ) : null}
            {request.status === 'SUBMITTED' ? (
              <Button
                variant='outline'
                className='rounded-full'
                onClick={() => setRejectOpen(true)}
              >
                <XCircle className='h-4 w-4' />
                Reject
              </Button>
            ) : null}
            {request.status === 'SUBMITTED' ? (
              <Button className='rounded-full' onClick={handleApprove}>
                <CheckCircle2 className='h-4 w-4' />
                {approving ? 'Approving...' : 'Approve'}
              </Button>
            ) : null}
          </>
        }
      >
        <div className='grid gap-6 xl:grid-cols-[0.9fr_1.1fr]'>
          <SchoolBusSection
            title='Request summary'
            description='Operational metadata and approval state.'
          >
            <div className='grid gap-4 md:grid-cols-2'>
              <InfoCard label='Parent' value={request.parentProfileName} />
              <InfoCard label='School' value={request.schoolName} />
              <InfoCard label='Request type' value={request.requestType} />
              <InfoCard label='Status' value={request.status} badge />
              <InfoCard label='Effective from' value={formatDate(request.effectiveFrom)} />
              <InfoCard
                label='Effective to'
                value={request.effectiveTo ? formatDate(request.effectiveTo) : 'Open ended'}
              />
              <InfoCard
                label='Approved at'
                value={request.approvedAt ? formatDateTime(request.approvedAt) : 'Not approved'}
              />
              <InfoCard
                label='Rejection reason'
                value={request.rejectionReason || 'N/A'}
              />
            </div>
          </SchoolBusSection>

          <SchoolBusSection
            title='Requested students'
            description='Students and pickup points included in this request.'
          >
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Student</TableHead>
                  <TableHead>Pickup point</TableHead>
                  <TableHead>Updated</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {detail.students.map((student) => (
                  <TableRow key={student.id}>
                    <TableCell>{student.studentName}</TableCell>
                    <TableCell>{student.pickupPointName || 'No pickup point'}</TableCell>
                    <TableCell>{formatDateTime(student.updatedAt)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SchoolBusSection>
        </div>

        <SchoolBusSection
          title='Lịch sử yêu cầu'
          description='Toàn bộ thay đổi trạng thái và ghi chú liên quan đến yêu cầu này.'
        >
          <SchoolBusTimeline
            events={mapRequestHistoryToTimeline(historyData?.data ?? [])}
            mode='compact'
            isLoading={historyLoading}
            isError={historyError}
            maxHeight='400px'
          />
        </SchoolBusSection>

        <SchoolBusSection
          title='Request map'
          description='Dispatcher review context for the school hub and all pickup points included in this request.'
        >
          <SchoolBusMapWorkspace
            defaultPreset='map-focus'
            map={
              <OperationsMap
                schools={[
                  {
                    id: request.schoolId,
                    name: request.schoolName,
                    latitude: request.schoolLatitude,
                    longitude: request.schoolLongitude,
                    address: undefined,
                  },
                ]}
                pickupPoints={detail.students
                  .filter(
                    (student) =>
                      student.pickupPointId &&
                      typeof student.pickupPointLatitude === 'number' &&
                      typeof student.pickupPointLongitude === 'number'
                  )
                  .map((student) => ({
                    id: student.pickupPointId as number,
                    schoolId: request.schoolId,
                    schoolName: request.schoolName,
                    name: student.pickupPointName || 'Pickup point',
                    address: student.pickupPointAddress || 'No address',
                    latitude: student.pickupPointLatitude,
                    longitude: student.pickupPointLongitude,
                  }))}
                studentMarkers={detail.students.reduce<StudentMapMarker[]>((acc, student) => {
                  if (student.pickupPointId && typeof student.pickupPointLatitude === 'number' && typeof student.pickupPointLongitude === 'number') {
                    acc.push({
                      key: `student-pickup-${student.id}`,
                      studentName: student.studentName,
                      pointName: student.pickupPointName || 'Pickup',
                      latitude: student.pickupPointLatitude,
                      longitude: student.pickupPointLongitude,
                      role: 'pickup',
                    });
                  }
                  if (student.dropoffPointId && typeof student.dropoffPointLatitude === 'number' && typeof student.dropoffPointLongitude === 'number'
                      && (student.dropoffPointId !== student.pickupPointId || student.dropoffPointLatitude !== student.pickupPointLatitude)) {
                    acc.push({
                      key: `student-dropoff-${student.id}`,
                      studentName: student.studentName,
                      pointName: student.dropoffPointName || 'Drop-off',
                      latitude: student.dropoffPointLatitude,
                      longitude: student.dropoffPointLongitude,
                      role: 'dropoff',
                    });
                  }
                  return acc;
                }, [])}
                selectedSchoolId={request.schoolId}
                className='h-full w-full'
              />
            }
            legend={<SchoolBusMapLegend />}
            panel={
              <div className='space-y-3'>
                <p className='text-sm font-semibold text-slate-950'>
                  Requested stop context
                </p>
                <p className='text-xs text-slate-500'>
                  Request type: {request.requestType}
                </p>
                <p className='text-xs text-slate-500'>
                  Students: {detail.students.length}
                </p>
                <div className='max-h-[320px] space-y-2 overflow-auto pr-1'>
                  {detail.students.map((student) => (
                    <div
                      key={student.id}
                      className='rounded-xl border border-slate-200 bg-white p-2'
                    >
                      <p className='text-xs font-semibold text-slate-900'>
                        {student.studentName}
                      </p>
                      <p className='text-xs text-slate-500'>
                        {student.pickupPointName || 'No pickup point'}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            }
          />
        </SchoolBusSection>
      </SchoolBusPageShell>

      <RejectTransportRequestDialog
        open={rejectOpen}
        onOpenChange={setRejectOpen}
        onSubmit={handleReject}
        isLoading={rejecting}
      />
    </>
  );
}

function InfoCard({
  label,
  value,
  badge = false,
}: {
  label: string;
  value: string;
  badge?: boolean;
}) {
  return (
    <div className={schoolBusUi.interactiveCard}>
      <p className='text-sm text-slate-500'>{label}</p>
      <div className='mt-2'>
        {badge ? (
          <SchoolBusStatusBadge status={value} />
        ) : (
          <p className='font-medium text-slate-950'>{value}</p>
        )}
      </div>
    </div>
  );
}
