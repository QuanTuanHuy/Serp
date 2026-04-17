'use client';

/**
 * Author: Tran Ngoc Hung
 * Description: Transport Plan creation wizard — Step 1 (select PENDING requests)
 *              and Step 2 (select resources + per-resource return depots).
 *
 * Flow:
 *   Step 1 → "Next"  : PATCH request statuses PENDING → PLANNED (lock for this dispatcher).
 *   Step 2 → "Back"  : PATCH request statuses PLANNED → PENDING (release the lock).
 *   Step 2 → "Create Plan": POST all selections to a single @Transactional endpoint;
 *                       rolls back atomically if anything fails server-side.
 *
 * Depot semantics:
 *   Each selected resource carries its own list of allowed return depot codes.
 *   When a resource is first selected, its current location is auto-added as the
 *   default return depot. The dispatcher can add more or remove any depot inline
 *   from the resource table row.
 */

import React, { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  ArrowRight,
  CheckSquare,
  ChevronLeft,
  ChevronRight,
  Loader2,
  MapPin,
  Package,
  Plus,
  Truck,
  Container,
  AlertCircle,
  BarChart3,
  Route,
  X,
} from 'lucide-react';
import { toast } from 'sonner';
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
  Checkbox,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
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
  useGetDispatcherRequestsQuery,
  useUpdateDispatcherRequestsStatusMutation,
  useGetDispatcherLocationsQuery,
  useGetDispatcherTrucksQuery,
  useGetDispatcherTrailersQuery,
  useGetDispatcherContainersQuery,
  useCreateDispatcherTransportPlanMutation,
} from '../../api/ttcrsApi';
import type {
  RequestType,
  TtcrsRequest,
  LocationItem,
  TruckItem,
  TrailerItem,
  ContainerItem,
  VehicleStatus,
  TransportPlanResult,
  AlgorithmTruckRoute,
} from '../../types';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const PAGE_SIZE = 20;

const TYPE_CLASS: Record<RequestType, string> = {
  OF: 'bg-green-500 hover:bg-green-500/90 text-white border-transparent',
  IF: 'bg-blue-500 hover:bg-blue-500/90 text-white border-transparent',
  OE: 'bg-amber-500 hover:bg-amber-500/90 text-white border-transparent',
  IE: 'bg-purple-500 hover:bg-purple-500/90 text-white border-transparent',
};

/** Status sort order for resources: AVAILABLE first, then IN_USE, then MAINTENANCE. */
const STATUS_ORDER: Record<VehicleStatus, number> = {
  AVAILABLE: 0,
  IN_USE: 1,
  MAINTENANCE: 2,
};

// -------------------------------------------------------------------------
// Helpers
// -------------------------------------------------------------------------

function formatId(id: number): string {
  return `REQ-${String(id).padStart(5, '0')}`;
}

function sortByStatus<T extends { status: VehicleStatus }>(items: T[]): T[] {
  return [...items].sort(
    (a, b) => (STATUS_ORDER[a.status] ?? 99) - (STATUS_ORDER[b.status] ?? 99)
  );
}

// -------------------------------------------------------------------------
// Sub-components
// -------------------------------------------------------------------------

function StepIndicator({ step }: { step: 1 | 2 }) {
  return (
    <div className='flex items-center gap-3 mb-6'>
      {/* Step 1 */}
      <div className='flex items-center gap-2'>
        <div
          className={cn(
            'flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold',
            step === 1
              ? 'bg-orange-600 text-white'
              : 'bg-orange-600/20 text-orange-600'
          )}
        >
          {step > 1 ? '✓' : '1'}
        </div>
        <span
          className={cn(
            'text-sm font-medium',
            step === 1 ? 'text-foreground' : 'text-muted-foreground'
          )}
        >
          Select Requests
        </span>
      </div>

      {/* Connector */}
      <div className='flex-1 h-px bg-border max-w-16' />

      {/* Step 2 */}
      <div className='flex items-center gap-2'>
        <div
          className={cn(
            'flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold',
            step === 2
              ? 'bg-orange-600 text-white'
              : 'bg-muted text-muted-foreground'
          )}
        >
          2
        </div>
        <span
          className={cn(
            'text-sm font-medium',
            step === 2 ? 'text-foreground' : 'text-muted-foreground'
          )}
        >
          Plan Setup
        </span>
      </div>
    </div>
  );
}

function TypeBadge({ type }: { type: RequestType }) {
  return (
    <Badge
      className={cn(
        'rounded px-2 py-0.5 text-xs font-bold tracking-wide',
        TYPE_CLASS[type] ?? 'bg-gray-400 text-white border-transparent'
      )}
    >
      {type}
    </Badge>
  );
}

function SkeletonRow({ cols }: { cols: number }) {
  return (
    <TableRow>
      {[...Array(cols)].map((_, i) => (
        <TableCell key={i}>
          <Skeleton className='h-4 w-full' />
        </TableCell>
      ))}
    </TableRow>
  );
}

function VehicleStatusBadge({ status }: { status: string }) {
  const cls =
    status === 'AVAILABLE'
      ? 'bg-green-100 text-green-700 border-green-200'
      : status === 'IN_USE'
      ? 'bg-blue-100 text-blue-700 border-blue-200'
      : 'bg-amber-100 text-amber-700 border-amber-200';
  return (
    <Badge className={cn('text-xs rounded-full', cls)}>
      {status.replace('_', ' ')}
    </Badge>
  );
}

// -------------------------------------------------------------------------
// ReturnDepotCell — inline depot chip list with add/remove per resource row
// -------------------------------------------------------------------------

interface ReturnDepotCellProps {
  resourceId: number;
  isSelected: boolean;
  depotCodes: string[];
  availableDepots: LocationItem[];
  isDepotsLoading: boolean;
  onAdd: (resourceId: number, code: string) => void;
  onRemove: (resourceId: number, code: string) => void;
}

function ReturnDepotCell({
  resourceId,
  isSelected,
  depotCodes,
  availableDepots,
  isDepotsLoading,
  onAdd,
  onRemove,
}: ReturnDepotCellProps) {
  if (!isSelected) {
    return (
      <TableCell className='px-3 py-2 text-xs text-muted-foreground'>
        —
      </TableCell>
    );
  }

  const remaining = availableDepots.filter(
    (d) => !depotCodes.includes(d.locationCode)
  );

  return (
    <TableCell className='px-3 py-2' onClick={(e) => e.stopPropagation()}>
      <div className='flex flex-wrap items-center gap-1'>
        {depotCodes.map((code) => (
          <Badge
            key={code}
            variant='secondary'
            className='text-xs flex items-center gap-0.5 py-0 pl-1.5 pr-0.5 font-mono'
          >
            {code}
            <button
              type='button'
              className='ml-0.5 rounded hover:text-destructive focus:outline-none'
              onClick={() => onRemove(resourceId, code)}
            >
              <X className='h-3 w-3' />
            </button>
          </Badge>
        ))}

        {remaining.length > 0 && (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                type='button'
                variant='outline'
                size='sm'
                className='h-6 px-2 text-xs border-dashed gap-1'
                disabled={isDepotsLoading}
              >
                <Plus className='h-3 w-3' />
                Add
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='start' className='max-h-48 overflow-y-auto'>
              {remaining.map((d) => (
                <DropdownMenuItem
                  key={d.locationCode}
                  className='text-xs font-mono'
                  onSelect={() => onAdd(resourceId, d.locationCode)}
                >
                  {d.locationCode}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        )}

        {depotCodes.length === 0 && remaining.length === 0 && (
          <span className='text-xs text-muted-foreground italic'>
            No depots available
          </span>
        )}
      </div>
    </TableCell>
  );
}

// -------------------------------------------------------------------------
// Step 1 — Select PENDING requests
// -------------------------------------------------------------------------

interface Step1Props {
  selectedIds: Set<number>;
  onToggle: (id: number) => void;
  onToggleAll: (ids: number[]) => void;
  onNext: () => void;
  isNextLoading: boolean;
}

function Step1({
  selectedIds,
  onToggle,
  onToggleAll,
  onNext,
  isNextLoading,
}: Step1Props) {
  const router = useRouter();
  const [page, setPage] = useState(0);

  const { data, isLoading, isError, isFetching } = useGetDispatcherRequestsQuery({
    statuses: ['PENDING'],
    page,
    size: PAGE_SIZE,
    sortBy: 'id',
    sortDirection: 'desc',
  });

  const items: TtcrsRequest[] = data?.data?.items ?? [];
  const totalPages = data?.data?.totalPages ?? 0;
  const totalElements = data?.data?.totalElements ?? 0;
  const pageIds = items.map((r) => r.id);
  const allPageSelected =
    pageIds.length > 0 && pageIds.every((id) => selectedIds.has(id));

  return (
    <div className='flex flex-col gap-4'>
      {/* Summary bar */}
      <div className='flex items-center justify-between'>
        <p className='text-sm text-muted-foreground'>
          {selectedIds.size === 0
            ? 'Select one or more requests to include in the transport plan.'
            : `${selectedIds.size} request${selectedIds.size > 1 ? 's' : ''} selected`}
        </p>
        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            size='sm'
            onClick={() => router.back()}
            disabled={isNextLoading}
          >
            Cancel
          </Button>
          <Button
            size='sm'
            className='bg-orange-600 hover:bg-orange-700 text-white'
            onClick={onNext}
            disabled={selectedIds.size === 0 || isNextLoading}
          >
            {isNextLoading ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <>
                Next
                <ArrowRight className='ml-1 h-4 w-4' />
              </>
            )}
          </Button>
        </div>
      </div>

      {/* Table */}
      <Card className='flex flex-col overflow-hidden gap-0 py-0'>
        <CardContent className='overflow-auto p-0'>
          {isError ? (
            <div className='flex flex-col items-center justify-center gap-3 py-20'>
              <AlertCircle className='h-12 w-12 text-destructive/60' />
              <p className='text-sm font-medium text-muted-foreground'>
                Failed to load requests. Please try again.
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow className='bg-muted/20 hover:bg-muted/20'>
                  <TableHead className='w-12 px-4 py-3'>
                    <Checkbox
                      checked={allPageSelected}
                      onCheckedChange={() => onToggleAll(pageIds)}
                      disabled={isLoading || isFetching || pageIds.length === 0}
                    />
                  </TableHead>
                  <TableHead className='px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                    Request ID
                  </TableHead>
                  <TableHead className='px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                    Origin
                  </TableHead>
                  <TableHead className='px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                    Destination
                  </TableHead>
                  <TableHead className='px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                    Type
                  </TableHead>
                  <TableHead className='px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                    Container Size
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading || isFetching ? (
                  [...Array(6)].map((_, i) => <SkeletonRow key={i} cols={6} />)
                ) : items.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <div className='flex flex-col items-center justify-center gap-3 py-16 text-center'>
                        <Package className='h-12 w-12 text-muted-foreground/40' />
                        <p className='text-sm font-medium text-muted-foreground'>
                          No pending requests found
                        </p>
                        <p className='text-xs text-muted-foreground/70'>
                          All transport requests are either planned, in progress, or completed.
                        </p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  items.map((req) => (
                    <TableRow
                      key={req.id}
                      className={cn(
                        'cursor-pointer',
                        selectedIds.has(req.id) && 'bg-orange-50 dark:bg-orange-950/20'
                      )}
                      onClick={() => onToggle(req.id)}
                    >
                      <TableCell className='px-4 py-3' onClick={(e) => e.stopPropagation()}>
                        <Checkbox
                          checked={selectedIds.has(req.id)}
                          onCheckedChange={() => onToggle(req.id)}
                        />
                      </TableCell>
                      <TableCell className='px-4 py-3 font-medium text-foreground'>
                        {formatId(req.id)}
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <div className='flex items-center gap-1.5'>
                          <MapPin className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                          <span className='text-foreground'>{req.srcLocationCode}</span>
                        </div>
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <div className='flex items-center gap-1.5'>
                          <MapPin className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                          <span className='text-foreground'>{req.destLocationCode}</span>
                        </div>
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <TypeBadge type={req.type} />
                      </TableCell>
                      <TableCell className='px-4 py-3 text-sm text-muted-foreground'>
                        {req.containerSize ?? '-'}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>

        {/* Pagination */}
        {!isLoading && !isError && totalPages > 0 && (
          <div className='flex items-center justify-between border-t border-border px-4 py-3'>
            <p className='text-xs text-muted-foreground'>
              {totalElements === 0
                ? 'No results'
                : `Showing ${page * PAGE_SIZE + 1}–${Math.min(
                    (page + 1) * PAGE_SIZE,
                    totalElements
                  )} of ${totalElements} pending requests`}
            </p>
            <div className='flex items-center gap-2'>
              <Button
                variant='outline'
                size='icon'
                className='h-8 w-8'
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || isFetching}
              >
                <ChevronLeft className='h-4 w-4' />
              </Button>
              <span className='min-w-20 text-center text-xs text-muted-foreground'>
                {isFetching ? (
                  <Loader2 className='mx-auto h-4 w-4 animate-spin' />
                ) : (
                  `Page ${page + 1} / ${totalPages}`
                )}
              </span>
              <Button
                variant='outline'
                size='icon'
                className='h-8 w-8'
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || isFetching}
              >
                <ChevronRight className='h-4 w-4' />
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}

// -------------------------------------------------------------------------
// PlanResultDialog — shows algorithm output after "Create Plan"
// -------------------------------------------------------------------------

interface PlanResultDialogProps {
  result: TransportPlanResult | null;
  onClose: () => void;
}

function PlanResultDialog({ result, onClose }: PlanResultDialogProps) {
  const [expandedRoute, setExpandedRoute] = useState<number | null>(null);

  if (!result) return null;

  const { statisticInformation: stats, truckRoutes } = result;

  const totalUnscheduled =
    (result.unscheduledExEmptyRequests?.length ?? 0) +
    (result.unscheduledExLadenRequests?.length ?? 0) +
    (result.unscheduledImEmptyRequests?.length ?? 0) +
    (result.unscheduledImLadenRequests?.length ?? 0);

  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className='max-w-4xl max-h-[85vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2 text-lg font-bold'>
            <BarChart3 className='h-5 w-5 text-orange-600' />
            Transport Plan Result
          </DialogTitle>
        </DialogHeader>

        {/* Statistics */}
        <div className='grid grid-cols-2 gap-3 sm:grid-cols-4'>
          {[
            { label: 'Total Requests', value: stats?.totalRequests ?? 0 },
            {
              label: 'Rejected',
              value: stats?.totalRejectedRequests ?? 0,
              highlight: (stats?.totalRejectedRequests ?? 0) > 0,
            },
            { label: 'Trucks Used', value: stats?.numberTrucks ?? 0 },
            {
              label: 'Total Distance',
              value: stats
                ? `${(stats.totalDistance / 1000).toFixed(1)} km`
                : '—',
            },
          ].map(({ label, value, highlight }) => (
            <Card key={label} className='py-3 gap-1'>
              <CardContent className='px-4 py-0'>
                <p className='text-xs text-muted-foreground'>{label}</p>
                <p
                  className={cn(
                    'text-2xl font-bold',
                    highlight ? 'text-destructive' : 'text-foreground'
                  )}
                >
                  {value}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Truck Routes */}
        <div className='flex flex-col gap-2'>
          <h3 className='flex items-center gap-2 text-sm font-semibold'>
            <Route className='h-4 w-4 text-orange-600' />
            Truck Routes
            <Badge className='ml-1 bg-orange-100 text-orange-700 border-orange-200 text-xs'>
              {truckRoutes?.length ?? 0}
            </Badge>
          </h3>

          {!truckRoutes || truckRoutes.length === 0 ? (
            <p className='text-sm text-muted-foreground italic'>
              No routes generated.
            </p>
          ) : (
            <div className='flex flex-col gap-2'>
              {truckRoutes.map((route: AlgorithmTruckRoute, idx: number) => (
                <Card key={idx} className='gap-0 py-0 overflow-hidden'>
                  <button
                    type='button'
                    className='flex w-full items-center justify-between px-4 py-3 text-left hover:bg-muted/40 transition-colors'
                    onClick={() =>
                      setExpandedRoute(expandedRoute === idx ? null : idx)
                    }
                  >
                    <div className='flex items-center gap-3'>
                      <Truck className='h-4 w-4 text-orange-600 shrink-0' />
                      <div>
                        <span className='font-mono font-medium text-sm'>
                          {route.truck?.code ?? `Truck ${idx + 1}`}
                        </span>
                        <span className='ml-3 text-xs text-muted-foreground'>
                          {route.nbStops} stop{route.nbStops !== 1 ? 's' : ''}{' '}
                          · {Math.round(route.travelTime / 60)} min
                        </span>
                      </div>
                    </div>
                    <ChevronRight
                      className={cn(
                        'h-4 w-4 text-muted-foreground transition-transform',
                        expandedRoute === idx && 'rotate-90'
                      )}
                    />
                  </button>

                  {expandedRoute === idx && (
                    <div className='border-t'>
                      <Table>
                        <TableHeader>
                          <TableRow className='bg-muted/20 hover:bg-muted/20'>
                            <TableHead className='px-4 py-2 text-xs'>#</TableHead>
                            <TableHead className='px-4 py-2 text-xs'>Location</TableHead>
                            <TableHead className='px-4 py-2 text-xs'>Action</TableHead>
                            <TableHead className='px-4 py-2 text-xs'>Arrival</TableHead>
                            <TableHead className='px-4 py-2 text-xs'>Departure</TableHead>
                            <TableHead className='px-4 py-2 text-xs'>Travel (min)</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {(route.nodes ?? []).map((node, nIdx) => (
                            <TableRow key={nIdx}>
                              <TableCell className='px-4 py-2 text-xs text-muted-foreground'>
                                {nIdx + 1}
                              </TableCell>
                              <TableCell className='px-4 py-2 font-mono text-xs font-medium'>
                                {node.locationCode}
                              </TableCell>
                              <TableCell className='px-4 py-2 text-xs'>
                                <Badge
                                  variant='outline'
                                  className='text-xs font-mono px-1.5'
                                >
                                  {node.action}
                                </Badge>
                              </TableCell>
                              <TableCell className='px-4 py-2 text-xs text-muted-foreground'>
                                {node.arrivalTime || '—'}
                              </TableCell>
                              <TableCell className='px-4 py-2 text-xs text-muted-foreground'>
                                {node.departureTime || '—'}
                              </TableCell>
                              <TableCell className='px-4 py-2 text-xs text-muted-foreground'>
                                {Math.round(node.travelTime / 60)}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </div>
                  )}
                </Card>
              ))}
            </div>
          )}
        </div>

        {/* Unscheduled requests */}
        {totalUnscheduled > 0 && (
          <div className='flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/5 p-4'>
            <AlertCircle className='h-5 w-5 text-destructive shrink-0 mt-0.5' />
            <div>
              <p className='text-sm font-semibold text-destructive'>
                {totalUnscheduled} unscheduled request
                {totalUnscheduled > 1 ? 's' : ''}
              </p>
              <p className='text-xs text-muted-foreground mt-0.5'>
                These requests could not be assigned to any truck with the current resources. Consider adding more trucks or trailers.
              </p>
            </div>
          </div>
        )}

        {/* Close */}
        <div className='flex justify-end pt-2'>
          <Button onClick={onClose}>
            <X className='mr-1.5 h-4 w-4' />
            Close
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

// -------------------------------------------------------------------------
// ResourceTable — generic selectable resource table with optional depot column
// -------------------------------------------------------------------------

interface DepotColumnConfig<T> {
  header: string;
  renderCell: (item: T, isSelected: boolean) => React.ReactNode;
}

interface ResourceTableProps<T extends { id: number }> {
  title: string;
  icon: React.ReactNode;
  items: T[];
  isLoading: boolean;
  selectedIds: Set<number>;
  /** Receives the full item so the parent can read currentLocationCode on first select. */
  onToggle: (item: T) => void;
  renderRow: (item: T) => React.ReactNode;
  headers: string[];
  depotColumn?: DepotColumnConfig<T>;
}

function ResourceTable<T extends { id: number }>({
  title,
  icon,
  items,
  isLoading,
  selectedIds,
  onToggle,
  renderRow,
  headers,
  depotColumn,
}: ResourceTableProps<T>) {
  const allSelected =
    items.length > 0 && items.every((i) => selectedIds.has(i.id));

  const toggleAll = () => {
    if (allSelected) {
      items.forEach((i) => selectedIds.has(i.id) && onToggle(i));
    } else {
      items.forEach((i) => !selectedIds.has(i.id) && onToggle(i));
    }
  };

  const totalHeaders = headers.length + (depotColumn ? 1 : 0);

  return (
    <Card className='flex flex-col gap-0 py-0'>
      <CardHeader className='px-4 py-3 border-b'>
        <CardTitle className='text-sm font-semibold flex items-center gap-2'>
          {icon}
          {title}
          {selectedIds.size > 0 && (
            <Badge className='ml-auto bg-orange-100 text-orange-700 border-orange-200 text-xs'>
              {selectedIds.size} selected
            </Badge>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent className='p-0 max-h-72 overflow-y-auto'>
        <Table>
          <TableHeader>
            <TableRow className='bg-muted/20 hover:bg-muted/20'>
              <TableHead className='w-10 px-3 py-2'>
                <Checkbox
                  checked={allSelected}
                  onCheckedChange={toggleAll}
                  disabled={isLoading || items.length === 0}
                />
              </TableHead>
              {headers.map((h) => (
                <TableHead
                  key={h}
                  className='px-3 py-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground'
                >
                  {h}
                </TableHead>
              ))}
              {depotColumn && (
                <TableHead className='px-3 py-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                  {depotColumn.header}
                </TableHead>
              )}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              [...Array(3)].map((_, i) => (
                <SkeletonRow key={i} cols={totalHeaders + 1} />
              ))
            ) : items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={totalHeaders + 1}>
                  <p className='py-6 text-center text-xs text-muted-foreground'>
                    No resources available
                  </p>
                </TableCell>
              </TableRow>
            ) : (
              items.map((item) => (
                <TableRow
                  key={item.id}
                  className={cn(
                    'cursor-pointer',
                    selectedIds.has(item.id) && 'bg-orange-50 dark:bg-orange-950/20'
                  )}
                  onClick={() => onToggle(item)}
                >
                  <TableCell
                    className='px-3 py-2'
                    onClick={(e) => e.stopPropagation()}
                  >
                    <Checkbox
                      checked={selectedIds.has(item.id)}
                      onCheckedChange={() => onToggle(item)}
                    />
                  </TableCell>
                  {renderRow(item)}
                  {depotColumn && depotColumn.renderCell(item, selectedIds.has(item.id))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}

// -------------------------------------------------------------------------
// Step 2 — Plan Setup (resource selection + per-resource return depots)
// -------------------------------------------------------------------------

interface Step2Props {
  selectedRequestIds: Set<number>;
  onBack: () => void;
  isBackLoading: boolean;
}

function Step2({ selectedRequestIds, onBack, isBackLoading }: Step2Props) {
  // Per-resource return depot maps: resourceId → list of allowed return depot codes
  const [truckReturnDepots, setTruckReturnDepots] = useState<Record<number, string[]>>({});
  const [trailerReturnDepots, setTrailerReturnDepots] = useState<Record<number, string[]>>({});
  const [containerReturnDepots, setContainerReturnDepots] = useState<Record<number, string[]>>({});

  // Algorithm result
  const [planResult, setPlanResult] = useState<TransportPlanResult | null>(null);

  // Resources (multi-select)
  const [selectedTruckIds, setSelectedTruckIds] = useState<Set<number>>(new Set());
  const [selectedTrailerIds, setSelectedTrailerIds] = useState<Set<number>>(new Set());
  const [selectedContainerIds, setSelectedContainerIds] = useState<Set<number>>(new Set());

  const { data: locationsData, isLoading: isLocationsLoading } =
    useGetDispatcherLocationsQuery();
  const { data: trucksData, isLoading: isTrucksLoading } =
    useGetDispatcherTrucksQuery();
  const { data: trailersData, isLoading: isTrailersLoading } =
    useGetDispatcherTrailersQuery();
  const { data: containersData, isLoading: isContainersLoading } =
    useGetDispatcherContainersQuery();

  const [createTransportPlan, { isLoading: isCreating }] =
    useCreateDispatcherTransportPlanMutation();

  const locations: LocationItem[] = locationsData?.data ?? [];

  // Resources sorted by status: AVAILABLE → IN_USE → MAINTENANCE
  const trucks = useMemo(() => sortByStatus(trucksData?.data ?? []), [trucksData]);
  const trailers = useMemo(() => sortByStatus(trailersData?.data ?? []), [trailersData]);
  const containers = useMemo(() => sortByStatus(containersData?.data ?? []), [containersData]);

  // Depot lists filtered by type — used for the "Add" dropdown in each resource table
  const truckDepotLocations = useMemo(
    () => locations.filter((l) => l.type === 'DEPOT_TRUCK'),
    [locations]
  );
  const trailerDepotLocations = useMemo(
    () => locations.filter((l) => l.type === 'DEPOT_TRAILER'),
    [locations]
  );
  const containerDepotLocations = useMemo(
    () => locations.filter((l) => l.type === 'DEPOT_CONTAINER'),
    [locations]
  );

  // -----------------------------------------------------------------------
  // Toggle handlers — on first selection, auto-seed return depots from
  // the resource's current location (dispatcher can adjust afterwards).
  // -----------------------------------------------------------------------

  const handleTruckToggle = (truck: TruckItem) => {
    const isAdding = !selectedTruckIds.has(truck.id);
    setSelectedTruckIds((prev) => {
      const next = new Set(prev);
      if (next.has(truck.id)) next.delete(truck.id);
      else next.add(truck.id);
      return next;
    });
    if (isAdding) {
      setTruckReturnDepots((prev) => ({
        ...prev,
        [truck.id]:
          prev[truck.id] ??
          (truck.currentLocationCode ? [truck.currentLocationCode] : []),
      }));
    }
  };

  const handleTrailerToggle = (trailer: TrailerItem) => {
    const isAdding = !selectedTrailerIds.has(trailer.id);
    setSelectedTrailerIds((prev) => {
      const next = new Set(prev);
      if (next.has(trailer.id)) next.delete(trailer.id);
      else next.add(trailer.id);
      return next;
    });
    if (isAdding) {
      setTrailerReturnDepots((prev) => ({
        ...prev,
        [trailer.id]:
          prev[trailer.id] ??
          (trailer.currentLocationCode ? [trailer.currentLocationCode] : []),
      }));
    }
  };

  const handleContainerToggle = (container: ContainerItem) => {
    const isAdding = !selectedContainerIds.has(container.id);
    setSelectedContainerIds((prev) => {
      const next = new Set(prev);
      if (next.has(container.id)) next.delete(container.id);
      else next.add(container.id);
      return next;
    });
    if (isAdding) {
      setContainerReturnDepots((prev) => ({
        ...prev,
        [container.id]:
          prev[container.id] ??
          (container.currentLocationCode ? [container.currentLocationCode] : []),
      }));
    }
  };

  // -----------------------------------------------------------------------
  // Depot add / remove helpers
  // -----------------------------------------------------------------------

  const addDepot = (
    setFn: React.Dispatch<React.SetStateAction<Record<number, string[]>>>,
    id: number,
    code: string
  ) => {
    setFn((prev) => ({ ...prev, [id]: [...(prev[id] ?? []), code] }));
  };

  const removeDepot = (
    setFn: React.Dispatch<React.SetStateAction<Record<number, string[]>>>,
    id: number,
    code: string
  ) => {
    setFn((prev) => ({
      ...prev,
      [id]: (prev[id] ?? []).filter((c) => c !== code),
    }));
  };

  // -----------------------------------------------------------------------
  // Depot column renderers passed to each ResourceTable
  // -----------------------------------------------------------------------

  const truckDepotColumn: DepotColumnConfig<TruckItem> = {
    header: 'Return Depots',
    renderCell: (truck, isSelected) => (
      <ReturnDepotCell
        resourceId={truck.id}
        isSelected={isSelected}
        depotCodes={truckReturnDepots[truck.id] ?? []}
        availableDepots={truckDepotLocations}
        isDepotsLoading={isLocationsLoading}
        onAdd={(id, code) => addDepot(setTruckReturnDepots, id, code)}
        onRemove={(id, code) => removeDepot(setTruckReturnDepots, id, code)}
      />
    ),
  };

  const trailerDepotColumn: DepotColumnConfig<TrailerItem> = {
    header: 'Return Depots',
    renderCell: (trailer, isSelected) => (
      <ReturnDepotCell
        resourceId={trailer.id}
        isSelected={isSelected}
        depotCodes={trailerReturnDepots[trailer.id] ?? []}
        availableDepots={trailerDepotLocations}
        isDepotsLoading={isLocationsLoading}
        onAdd={(id, code) => addDepot(setTrailerReturnDepots, id, code)}
        onRemove={(id, code) => removeDepot(setTrailerReturnDepots, id, code)}
      />
    ),
  };

  const containerDepotColumn: DepotColumnConfig<ContainerItem> = {
    header: 'Return Depots',
    renderCell: (container, isSelected) => (
      <ReturnDepotCell
        resourceId={container.id}
        isSelected={isSelected}
        depotCodes={containerReturnDepots[container.id] ?? []}
        availableDepots={containerDepotLocations}
        isDepotsLoading={isLocationsLoading}
        onAdd={(id, code) => addDepot(setContainerReturnDepots, id, code)}
        onRemove={(id, code) => removeDepot(setContainerReturnDepots, id, code)}
      />
    ),
  };

  // -----------------------------------------------------------------------
  // Form validation — at least one truck required (enforced by backend too)
  // -----------------------------------------------------------------------
  const isFormValid = selectedTruckIds.size > 0;

  // -----------------------------------------------------------------------
  // Submit
  // -----------------------------------------------------------------------
  const handleCreatePlan = async () => {
    try {
      const response = await createTransportPlan({
        requestIds: Array.from(selectedRequestIds),
        truckReturnDepots: Array.from(selectedTruckIds).map((id) => ({
          resourceId: id,
          returnDepotCodes: truckReturnDepots[id] ?? [],
        })),
        trailerReturnDepots: Array.from(selectedTrailerIds).map((id) => ({
          resourceId: id,
          returnDepotCodes: trailerReturnDepots[id] ?? [],
        })),
        containerReturnDepots: Array.from(selectedContainerIds).map((id) => ({
          resourceId: id,
          returnDepotCodes: containerReturnDepots[id] ?? [],
        })),
        truckIds: Array.from(selectedTruckIds),
        trailerIds: Array.from(selectedTrailerIds),
        containerIds: Array.from(selectedContainerIds),
      }).unwrap();

      setPlanResult(response.data);
      toast.success('Transport plan computed successfully.');
    } catch {
      toast.error('Failed to create transport plan. Please try again.');
    }
  };

  return (
    <div className='flex flex-col gap-6'>
      {/* Result dialog */}
      <PlanResultDialog result={planResult} onClose={() => setPlanResult(null)} />

      {/* Action bar */}
      <div className='flex items-center justify-between'>
        <p className='text-sm text-muted-foreground'>
          {selectedRequestIds.size} request{selectedRequestIds.size > 1 ? 's' : ''} selected
          {' '}— select resources and configure return depots per row.
        </p>
        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            size='sm'
            onClick={onBack}
            disabled={isBackLoading || isCreating}
          >
            {isBackLoading ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <>
                <ArrowLeft className='mr-1 h-4 w-4' />
                Back
              </>
            )}
          </Button>
          <Button
            size='sm'
            className='bg-orange-600 hover:bg-orange-700 text-white'
            onClick={handleCreatePlan}
            disabled={!isFormValid || isCreating || isBackLoading}
          >
            {isCreating ? (
              <Loader2 className='mr-1 h-4 w-4 animate-spin' />
            ) : (
              <CheckSquare className='mr-1 h-4 w-4' />
            )}
            Create Plan
          </Button>
        </div>
      </div>

      {/* Resource Tables — each has an inline Return Depots column */}
      <div className='flex flex-col gap-4'>
        {/* Trucks */}
        <ResourceTable<TruckItem>
          title='Trucks'
          icon={<Truck className='h-4 w-4 text-orange-600' />}
          items={trucks}
          isLoading={isTrucksLoading}
          selectedIds={selectedTruckIds}
          onToggle={handleTruckToggle}
          headers={['Code', 'Status', 'Location']}
          depotColumn={truckDepotColumn}
          renderRow={(truck) => (
            <>
              <TableCell className='px-3 py-2 font-mono text-sm font-medium'>
                {truck.code}
              </TableCell>
              <TableCell className='px-3 py-2'>
                <VehicleStatusBadge status={truck.status} />
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {truck.currentLocationCode ?? '-'}
              </TableCell>
            </>
          )}
        />

        {/* Trailers */}
        <ResourceTable<TrailerItem>
          title='Trailers'
          icon={<Package className='h-4 w-4 text-orange-600' />}
          items={trailers}
          isLoading={isTrailersLoading}
          selectedIds={selectedTrailerIds}
          onToggle={handleTrailerToggle}
          headers={['Code', 'Status', 'Location']}
          depotColumn={trailerDepotColumn}
          renderRow={(trailer) => (
            <>
              <TableCell className='px-3 py-2 font-mono text-sm font-medium'>
                {trailer.code}
              </TableCell>
              <TableCell className='px-3 py-2'>
                <VehicleStatusBadge status={trailer.status} />
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {trailer.currentLocationCode ?? '-'}
              </TableCell>
            </>
          )}
        />

        {/* Containers */}
        <ResourceTable<ContainerItem>
          title='Containers'
          icon={<Container className='h-4 w-4 text-orange-600' />}
          items={containers}
          isLoading={isContainersLoading}
          selectedIds={selectedContainerIds}
          onToggle={handleContainerToggle}
          headers={['Code', 'Size', 'Status', 'Location']}
          depotColumn={containerDepotColumn}
          renderRow={(container) => (
            <>
              <TableCell className='px-3 py-2 font-mono text-sm font-medium'>
                {container.code}
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {container.size ?? '-'}
              </TableCell>
              <TableCell className='px-3 py-2'>
                <VehicleStatusBadge status={container.status} />
              </TableCell>
              <TableCell className='px-3 py-2 text-sm text-muted-foreground'>
                {container.currentLocationCode ?? '-'}
              </TableCell>
            </>
          )}
        />
      </div>
    </div>
  );
}

// -------------------------------------------------------------------------
// Main Page
// -------------------------------------------------------------------------

export function CreateTransportPlanPage() {
  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const [step, setStep] = useState<1 | 2>(1);
  const [selectedRequestIds, setSelectedRequestIds] = useState<Set<number>>(
    new Set()
  );

  const [updateStatus, { isLoading: isUpdating }] =
    useUpdateDispatcherRequestsStatusMutation();

  // Role guard
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

  const toggleRequest = (id: number) => {
    setSelectedRequestIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleAllRequests = (ids: number[]) => {
    setSelectedRequestIds((prev) => {
      const allSelected = ids.every((id) => prev.has(id));
      const next = new Set(prev);
      if (allSelected) {
        ids.forEach((id) => next.delete(id));
      } else {
        ids.forEach((id) => next.add(id));
      }
      return next;
    });
  };

  /**
   * "Next": immediately set selected requests to PLANNED so no other dispatcher
   * can claim them while this wizard is in progress.
   */
  const handleNext = async () => {
    if (selectedRequestIds.size === 0) return;
    try {
      await updateStatus({
        requestIds: Array.from(selectedRequestIds),
        status: 'PLANNED',
      }).unwrap();
      setStep(2);
    } catch {
      toast.error('Failed to lock requests. Please try again.');
    }
  };

  /**
   * "Back": release the lock by reverting PLANNED → PENDING.
   */
  const handleBack = async () => {
    try {
      await updateStatus({
        requestIds: Array.from(selectedRequestIds),
        status: 'PENDING',
      }).unwrap();
      setStep(1);
    } catch {
      toast.error('Failed to release request lock. Please try again.');
    }
  };

  return (
    <div className='flex min-h-screen flex-col bg-background px-12 py-6'>
      {/* Page heading */}
      <div className='mb-6 flex flex-col gap-1'>
        <h1 className='text-2xl font-bold tracking-tight text-foreground'>
          Create Transport Plan
        </h1>
        <p className='text-sm text-muted-foreground'>
          Select pending requests and assign resources to create a transport plan.
        </p>
      </div>

      {/* Step indicator */}
      <StepIndicator step={step} />

      {/* Step content */}
      {step === 1 ? (
        <Step1
          selectedIds={selectedRequestIds}
          onToggle={toggleRequest}
          onToggleAll={toggleAllRequests}
          onNext={handleNext}
          isNextLoading={isUpdating}
        />
      ) : (
        <Step2
          selectedRequestIds={selectedRequestIds}
          onBack={handleBack}
          isBackLoading={isUpdating}
        />
      )}
    </div>
  );
}
