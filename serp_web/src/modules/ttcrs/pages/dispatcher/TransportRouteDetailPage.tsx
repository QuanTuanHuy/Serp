'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  AlertCircle,
  ArrowLeft,
  ChevronRight,
  Clock,
  Loader2,
  Map,
  Navigation,
  RotateCcw,
  Truck,
  User,
  XCircle,
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
import {
  useGetTransportPlanDetailQuery,
  useCancelRouteMutation,
  useRestoreRouteMutation,
} from '../../api/ttcrsApi';
import type { StopAction, TransportPlanStatus } from '../../types';
import { RouteProgressOverview } from './components/RouteProgressOverview';
import { RouteManifestTimeline } from './components/RouteManifestTimeline';
import { RouteMapModal } from './components/RouteMapModal';
import { CancelRouteDialog } from './components/CancelRouteDialog';
import { RestoreRouteConfirmDialog } from './components/RestoreRouteConfirmDialog';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const STATUS_BADGE: Record<TransportPlanStatus, string> = {
  CREATED: 'bg-blue-100 text-blue-700 border-blue-200',
  EXECUTING: 'bg-orange-100 text-orange-700 border-orange-200',
  COMPLETED: 'bg-green-100 text-green-700 border-green-200',
  CANCELLED: 'bg-red-100 text-red-700 border-red-200',
};

const ACTION_BADGE: Record<StopAction, string> = {
  DEPOT_START: 'bg-gray-100 text-gray-700 border-gray-200',
  DEPOT_END: 'bg-gray-100 text-gray-700 border-gray-200',
  PICKUP_TRAILER: 'bg-purple-100 text-purple-700 border-purple-200',
  DROP_TRAILER: 'bg-purple-100 text-purple-700 border-purple-200',
  PICKUP_CONTAINER: 'bg-blue-100 text-blue-700 border-blue-200',
  DELIVERY_CONTAINER: 'bg-green-100 text-green-700 border-green-200',
};

const ACTION_LABEL: Record<StopAction, string> = {
  DEPOT_START: 'Depot Start',
  DEPOT_END: 'Depot End',
  PICKUP_TRAILER: 'Pickup Trailer',
  DROP_TRAILER: 'Drop Trailer',
  PICKUP_CONTAINER: 'Pickup Container',
  DELIVERY_CONTAINER: 'Delivery Container',
};

// -------------------------------------------------------------------------
// Helpers
// -------------------------------------------------------------------------

function fmt(dt: string | null) {
  if (!dt) return '—';
  return dt.replace('T', ' ').slice(0, 16);
}

function fmtMinutes(min: number | null) {
  if (min == null) return '—';
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
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

  const { data, isLoading, isError, refetch } = useGetTransportPlanDetailQuery(planId);
  const plan = data?.data;

  const [cancelRoute, { isLoading: isCancelling }] = useCancelRouteMutation();
  const [restoreRoute, { isLoading: isRestoring }] = useRestoreRouteMutation();

  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [restoreDialogOpen, setRestoreDialogOpen] = useState(false);
  const [mapOpen, setMapOpen] = useState(false);

  if (!isDispatcher && user !== null) {
    return (
      <AccessDenied
        reason="authorization"
        title="Access Denied"
        description="You don't have the TTCRS_DISPATCHER role required to access this page."
        variant="minimal"
        size="md"
        showBackButton
        showHomeButton
      />
    );
  }

  const handleCancel = async (reason: string) => {
    try {
      await cancelRoute({ id: planId, body: { reason } }).unwrap();
      setCancelDialogOpen(false);
      refetch();
    } catch (err: any) {
      alert(err?.data?.message ?? 'Failed to cancel route');
    }
  };

  const handleRestore = async () => {
    try {
      await restoreRoute(planId).unwrap();
      setRestoreDialogOpen(false);
      refetch();
    } catch (err: any) {
      alert(err?.data?.message ?? 'Failed to restore route');
    }
  };

  // -------------------------------------------------------------------------
  // Loading / error
  // -------------------------------------------------------------------------

  if (isLoading) {
    return (
      <div className="flex flex-col gap-4 px-12 py-8">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (isError || !plan) {
    return (
      <div className="flex items-center gap-3 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive mx-12 mt-8">
        <AlertCircle className="h-5 w-5 shrink-0" />
        <span className="text-sm">Failed to load transport plan detail.</span>
      </div>
    );
  }

  // -------------------------------------------------------------------------
  // Shared header
  // -------------------------------------------------------------------------
  const header = (
    <div className="flex items-center gap-3">
      <Button
        variant="ghost"
        size="icon"
        className="h-8 w-8"
        onClick={() => router.push('/ttcrs/dispatcher/routes')}
      >
        <ArrowLeft className="h-4 w-4" />
      </Button>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap">
          <h1 className="text-2xl font-bold tracking-tight">Route #{plan.id}</h1>
          <Badge className={cn('border text-xs', STATUS_BADGE[plan.status])}>
            {plan.status}
          </Badge>
        </div>
        <p className="mt-0.5 text-sm text-muted-foreground">
          Full stop sequence for this transport plan.
        </p>
      </div>
    </div>
  );

  // -------------------------------------------------------------------------
  // EXECUTING
  // -------------------------------------------------------------------------
  if (plan.status === 'EXECUTING') {
    return (
      <div className="flex flex-col gap-6 px-12 py-6">
        {header}

        {/* Basic info card: truck, driver, actual start, created at */}
        <Card>
          <CardHeader className="pb-3">
            <div className="flex items-start justify-between">
              <CardTitle className="text-base font-semibold">Plan #{plan.id}</CardTitle>
              <Badge className={cn('border text-xs', STATUS_BADGE[plan.status])}>
                {plan.status}
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm sm:grid-cols-4">
              <InfoItem icon={<Truck className="h-4 w-4" />} label="Truck" value={plan.truckCode ?? '—'} />
              <InfoItem icon={<User className="h-4 w-4" />} label="Driver" value={plan.driverName ?? '—'} />
              <InfoItem
                icon={<Clock className="h-4 w-4" />}
                label="Actual Start"
                value={fmt(plan.actualStartTime)}
              />
              <InfoItem
                icon={<Clock className="h-4 w-4" />}
                label="Created At"
                value={fmt(plan.createdStamp)}
              />
            </dl>
          </CardContent>
        </Card>

        {/* Progress overview */}
        <RouteProgressOverview plan={plan} />

        {/* Route manifest */}
        <div>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">
              Route Manifest
            </h2>
            <Button variant="outline" size="sm" onClick={() => setMapOpen(true)}>
              <Map className="mr-2 h-4 w-4" />
              View full route map
            </Button>
          </div>
          <RouteManifestTimeline stops={plan.stops} currentStopSeq={plan.currentStop} />
        </div>

        <RouteMapModal
          open={mapOpen}
          onClose={() => setMapOpen(false)}
          stops={plan.stops}
          currentStopSeq={plan.currentStop}
        />
      </div>
    );
  }

  // -------------------------------------------------------------------------
  // COMPLETED
  // -------------------------------------------------------------------------
  if (plan.status === 'COMPLETED') {
    return (
      <div className="flex flex-col gap-6 px-12 py-6">
        {header}

        {/* Info card with actual times */}
        <Card>
          <CardHeader className="pb-3">
            <div className="flex items-start justify-between">
              <CardTitle className="text-base font-semibold">Plan #{plan.id}</CardTitle>
              <Badge className={cn('border text-xs', STATUS_BADGE[plan.status])}>
                {plan.status}
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm sm:grid-cols-4">
              <InfoItem icon={<Truck className="h-4 w-4" />} label="Truck" value={plan.truckCode ?? '—'} />
              <InfoItem icon={<User className="h-4 w-4" />} label="Driver" value={plan.driverName ?? '—'} />
              <InfoItem
                icon={<Clock className="h-4 w-4" />}
                label="Actual Start"
                value={fmt(plan.actualStartTime)}
                highlight
              />
              <InfoItem
                icon={<Clock className="h-4 w-4" />}
                label="Actual End"
                value={fmt(plan.actualEndTime)}
                highlight
              />
              <InfoItem
                icon={<Clock className="h-4 w-4" />}
                label="Created At"
                value={fmt(plan.createdStamp)}
              />
              <InfoItem
                icon={<Navigation className="h-4 w-4" />}
                label="Total Distance"
                value={plan.totalDistance != null ? `${plan.totalDistance.toFixed(1)} km` : '—'}
                highlight
              />
              <InfoItem
                icon={<Clock className="h-4 w-4" />}
                label="Total Time"
                value={fmtMinutes(plan.totalTime)}
                highlight
              />
              <InfoItem
                icon={<Navigation className="h-4 w-4" />}
                label="Total Stops"
                value={String(plan.stops.length)}
              />
            </dl>
          </CardContent>
        </Card>

        {/* Route manifest (all stops completed) */}
        <div>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">
              Route Manifest
            </h2>
            <Button variant="outline" size="sm" onClick={() => setMapOpen(true)}>
              <Map className="mr-2 h-4 w-4" />
              View full route map
            </Button>
          </div>
          <RouteManifestTimeline stops={plan.stops} />
        </div>

        <RouteMapModal
          open={mapOpen}
          onClose={() => setMapOpen(false)}
          stops={plan.stops}
          currentStopSeq={null}
        />
      </div>
    );
  }

  // -------------------------------------------------------------------------
  // CREATED / CANCELLED — table layout
  // -------------------------------------------------------------------------
  return (
    <div className="flex flex-col gap-6 px-12 py-6">
      {header}

      {/* Plan info card */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <CardTitle className="text-base font-semibold">Plan #{plan.id}</CardTitle>
            <Badge className={cn('border text-xs', STATUS_BADGE[plan.status])}>
              {plan.status}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <dl className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm sm:grid-cols-4">
            <InfoItem icon={<Truck className="h-4 w-4" />} label="Truck" value={plan.truckCode ?? '—'} />
            <InfoItem icon={<User className="h-4 w-4" />} label="Driver" value={plan.driverName ?? '—'} />
            <InfoItem
              icon={<Clock className="h-4 w-4" />}
              label="Start Time"
              value={fmt(plan.startTime)}
            />
            <InfoItem
              icon={<Clock className="h-4 w-4" />}
              label="End Time"
              value={fmt(plan.endTime)}
            />
            <InfoItem
              icon={<Clock className="h-4 w-4" />}
              label="Created"
              value={fmt(plan.createdStamp)}
            />
            <InfoItem
              icon={<Navigation className="h-4 w-4" />}
              label="Total Stops"
              value={String(plan.stops.length)}
            />
          </dl>
        </CardContent>
      </Card>

      {/* CANCELLED: cancellation reason */}
      {plan.status === 'CANCELLED' && plan.cancelReason && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="p-4">
            <div className="flex items-start gap-2">
              <XCircle className="mt-0.5 h-5 w-5 shrink-0 text-red-500" />
              <div>
                <p className="text-sm font-semibold text-red-700">Cancellation Reason</p>
                <p className="mt-1 text-sm text-red-600">{plan.cancelReason}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Stops table */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base font-semibold">Stops</CardTitle>
            {plan.status === 'CREATED' && (
              <Button
                variant="destructive"
                size="sm"
                onClick={() => setCancelDialogOpen(true)}
                disabled={isCancelling}
              >
                <XCircle className="mr-2 h-4 w-4" />
                Cancel Route
              </Button>
            )}
            {plan.status === 'CANCELLED' && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setRestoreDialogOpen(true)}
                disabled={isRestoring}
              >
                <RotateCcw className="mr-2 h-4 w-4" />
                Restore Route
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-12 text-center">#</TableHead>
                <TableHead>Location</TableHead>
                <TableHead>Action</TableHead>
                <TableHead>Planned Arrival</TableHead>
                <TableHead>Actual Arrival</TableHead>
                <TableHead className="text-right">Request ID</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[...plan.stops]
                .sort((a, b) => a.sequence - b.sequence)
                .map((stop) => (
                  <TableRow key={stop.id}>
                    <TableCell className="text-center text-xs text-muted-foreground">
                      {stop.sequence}
                    </TableCell>
                    <TableCell className="font-mono text-sm">{stop.locationCode}</TableCell>
                    <TableCell>
                      <Badge
                        className={cn(
                          'border text-xs',
                          ACTION_BADGE[stop.action] ?? 'bg-gray-100 text-gray-700 border-gray-200',
                        )}
                      >
                        {ACTION_LABEL[stop.action] ?? stop.action}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm tabular-nums">
                      {fmt(stop.plannedArrivalTime)}
                    </TableCell>
                    <TableCell className="text-sm tabular-nums">
                      {fmt(stop.actualArrivalTime)}
                    </TableCell>
                    <TableCell className="text-right text-sm tabular-nums text-muted-foreground">
                      {stop.requestId != null ? (
                        <span className="flex items-center justify-end gap-1">
                          #{stop.requestId}
                          <ChevronRight className="h-3 w-3" />
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

      {/* Dialogs */}
      <CancelRouteDialog
        open={cancelDialogOpen}
        onOpenChange={setCancelDialogOpen}
        onConfirm={handleCancel}
        isPending={isCancelling}
      />
      <RestoreRouteConfirmDialog
        open={restoreDialogOpen}
        onOpenChange={setRestoreDialogOpen}
        onConfirm={handleRestore}
        isPending={isRestoring}
      />
    </div>
  );
}

// -------------------------------------------------------------------------
// Local helper component
// -------------------------------------------------------------------------

function InfoItem({
  icon,
  label,
  value,
  highlight,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  highlight?: boolean;
}) {
  return (
    <div>
      <dt className="flex items-center gap-1 text-xs font-medium uppercase text-muted-foreground">
        {icon}
        {label}
      </dt>
      <dd className={cn('mt-0.5 font-medium tabular-nums', highlight && 'font-semibold text-primary')}>
        {value}
      </dd>
    </div>
  );
}
