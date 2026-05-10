'use client';

import { useRouter } from 'next/navigation';
import {
  AlertCircle,
  ArrowLeft,
  ChevronRight,
  Loader2,
} from 'lucide-react';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import { AccessDenied } from '@/modules/account/components';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetTransportPlanDetailQuery } from '../../api/ttcrsApi';
import type { StopAction, TransportPlanStatus } from '../../types';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const STATUS_BADGE: Record<TransportPlanStatus, string> = {
  CREATED:   'bg-blue-100 text-blue-700 border-blue-200',
  EXECUTING: 'bg-orange-100 text-orange-700 border-orange-200',
  COMPLETED: 'bg-green-100 text-green-700 border-green-200',
  CANCELLED: 'bg-red-100 text-red-700 border-red-200',
};

const ACTION_BADGE: Record<StopAction, string> = {
  DEPOT_START:          'bg-gray-100 text-gray-700 border-gray-200',
  DEPOT_END:            'bg-gray-100 text-gray-700 border-gray-200',
  PICKUP_TRAILER:       'bg-purple-100 text-purple-700 border-purple-200',
  DROP_TRAILER:         'bg-purple-100 text-purple-700 border-purple-200',
  PICKUP_CONTAINER:     'bg-blue-100 text-blue-700 border-blue-200',
  DELIVERY_CONTAINER:   'bg-green-100 text-green-700 border-green-200',
};

const ACTION_LABEL: Record<StopAction, string> = {
  DEPOT_START:          'Depot Start',
  DEPOT_END:            'Depot End',
  PICKUP_TRAILER:       'Pickup Trailer',
  DROP_TRAILER:         'Drop Trailer',
  PICKUP_CONTAINER:     'Pickup Container',
  DELIVERY_CONTAINER:   'Delivery Container',
};

// -------------------------------------------------------------------------
// Helpers
// -------------------------------------------------------------------------

function formatDateTime(dt: string | null) {
  if (!dt) return '—';
  return dt.replace('T', ' ').slice(0, 16);
}

// -------------------------------------------------------------------------
// Component
// -------------------------------------------------------------------------

interface Props {
  planId: number;
}

export function TransportRouteDetailPage({ planId }: Props) {
  const router = useRouter();
  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const { data, isLoading, isError } = useGetTransportPlanDetailQuery(planId);
  const plan = data?.data;

  if (!isDispatcher && user !== null) {
    return (
      <AccessDenied
        reason='authorization'
        title='Access Denied'
        description="You don't have the TTCRS_DISPATCHER role required to access this page."
        variant='minimal'
        size='md'
        showBackButton
        showHomeButton
      />
    );
  }

  return (
    <div className='flex flex-col gap-6 px-12 py-6'>
      {/* Header */}
      <div className='flex items-center gap-3'>
        <Button
          variant='ghost'
          size='icon'
          className='h-8 w-8'
          onClick={() => router.push('/ttcrs/dispatcher/routes')}
        >
          <ArrowLeft className='h-4 w-4' />
        </Button>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Route Detail</h1>
          <p className='mt-0.5 text-sm text-muted-foreground'>
            Full stop sequence for this transport plan.
          </p>
        </div>
      </div>

      {/* Loading / error states */}
      {isLoading && (
        <div className='space-y-3'>
          <Skeleton className='h-24 w-full' />
          <Skeleton className='h-64 w-full' />
        </div>
      )}

      {isError && (
        <div className='flex items-center gap-3 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive'>
          <AlertCircle className='h-5 w-5 shrink-0' />
          <span className='text-sm'>Failed to load transport plan detail.</span>
        </div>
      )}

      {isLoading && (
        <div className='flex items-center justify-center py-20'>
          <Loader2 className='h-6 w-6 animate-spin text-muted-foreground' />
        </div>
      )}

      {/* Plan info card */}
      {plan && (
        <>
          <Card>
            <CardHeader className='pb-3'>
              <div className='flex items-start justify-between'>
                <CardTitle className='text-base font-semibold'>
                  Plan #{plan.id}
                </CardTitle>
                <Badge className={cn('border text-xs', STATUS_BADGE[plan.status])}>
                  {plan.status}
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <dl className='grid grid-cols-2 gap-x-8 gap-y-3 text-sm sm:grid-cols-4'>
                <div>
                  <dt className='text-xs font-medium uppercase text-muted-foreground'>Truck</dt>
                  <dd className='mt-0.5 font-mono font-medium'>{plan.truckCode ?? '—'}</dd>
                </div>
                <div>
                  <dt className='text-xs font-medium uppercase text-muted-foreground'>Driver</dt>
                  <dd className='mt-0.5'>{plan.driverName ?? '—'}</dd>
                </div>
                <div>
                  <dt className='text-xs font-medium uppercase text-muted-foreground'>Start Time</dt>
                  <dd className='mt-0.5 tabular-nums'>{formatDateTime(plan.startTime)}</dd>
                </div>
                <div>
                  <dt className='text-xs font-medium uppercase text-muted-foreground'>End Time</dt>
                  <dd className='mt-0.5 tabular-nums'>{formatDateTime(plan.endTime)}</dd>
                </div>
                <div>
                  <dt className='text-xs font-medium uppercase text-muted-foreground'>Created</dt>
                  <dd className='mt-0.5 tabular-nums'>{formatDateTime(plan.createdStamp)}</dd>
                </div>
                <div>
                  <dt className='text-xs font-medium uppercase text-muted-foreground'>Total Stops</dt>
                  <dd className='mt-0.5'>{plan.stops.length}</dd>
                </div>
              </dl>
            </CardContent>
          </Card>

          {/* Stops table */}
          <Card>
            <CardHeader className='pb-3'>
              <CardTitle className='text-base font-semibold'>Stops</CardTitle>
            </CardHeader>
            <CardContent className='p-0'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className='w-12 text-center'>#</TableHead>
                    <TableHead>Location</TableHead>
                    <TableHead>Action</TableHead>
                    <TableHead>Planned Arrival</TableHead>
                    <TableHead>Actual Arrival</TableHead>
                    <TableHead className='text-right'>Request ID</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {plan.stops.map((stop) => (
                    <TableRow key={stop.id}>
                      <TableCell className='text-center text-xs text-muted-foreground'>
                        {stop.sequence}
                      </TableCell>
                      <TableCell className='font-mono text-sm'>{stop.locationCode}</TableCell>
                      <TableCell>
                        <Badge
                          className={cn(
                            'border text-xs',
                            ACTION_BADGE[stop.action] ?? 'bg-gray-100 text-gray-700 border-gray-200'
                          )}
                        >
                          {ACTION_LABEL[stop.action] ?? stop.action}
                        </Badge>
                      </TableCell>
                      <TableCell className='text-sm tabular-nums'>
                        {formatDateTime(stop.plannedArrivalTime)}
                      </TableCell>
                      <TableCell className='text-sm tabular-nums'>
                        {formatDateTime(stop.actualArrivalTime)}
                      </TableCell>
                      <TableCell className='text-right text-sm tabular-nums text-muted-foreground'>
                        {stop.requestId != null ? (
                          <span className='flex items-center justify-end gap-1'>
                            #{stop.requestId}
                            <ChevronRight className='h-3 w-3' />
                          </span>
                        ) : (
                          '—'
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}
