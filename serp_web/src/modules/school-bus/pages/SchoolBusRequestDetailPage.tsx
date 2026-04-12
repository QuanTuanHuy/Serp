'use client';

import Link from 'next/link';
import * as React from 'react';
import { CheckCircle2, Pencil, XCircle } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useApproveTransportRequestMutation,
  useGetTransportRequestByIdQuery,
  useRejectTransportRequestMutation,
} from '../api/schoolBusApi';
import { RejectTransportRequestDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { OperationsMap } from '../components/map/OperationsMap';
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
        actions={
          <>
            {request.status === 'PENDING' ? (
              <Button variant='outline' className='rounded-full' asChild>
                <Link href={`/school-bus/requests/${request.id}/edit`}>
                  <Pencil className='h-4 w-4' />
                  Edit
                </Link>
              </Button>
            ) : null}
            {request.status === 'PENDING' ? (
              <Button
                variant='outline'
                className='rounded-full'
                onClick={() => setRejectOpen(true)}
              >
                <XCircle className='h-4 w-4' />
                Reject
              </Button>
            ) : null}
            {request.status === 'PENDING' ? (
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
          title='Request map'
          description='Dispatcher review context for the school hub and all pickup points included in this request.'
        >
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
            selectedSchoolId={request.schoolId}
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
