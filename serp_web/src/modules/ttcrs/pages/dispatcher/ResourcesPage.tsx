'use client';

import React, { useState, useMemo } from 'react';
import {
  Plus,
  Loader2,
  AlertCircle,
  Search,
  ChevronLeft,
  ChevronRight,
  ArrowUp,
  ArrowDown,
  ArrowUpDown,
  Package2,
} from 'lucide-react';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import { AccessDenied } from '@/modules/account/components';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Input,
  Skeleton,
  Tabs,
  TabsList,
  TabsTrigger,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetDispatcherContainersQuery,
  useGetDispatcherTrucksQuery,
  useGetDispatcherTrailersQuery,
  useGetDispatcherDriversQuery,
} from '../../api/ttcrsApi';
import { CreateResourceDialog, ResourceDetailSheet } from '../../components';
import type {
  ResourceKind,
  ResourceRow,
  ResourceContainerSize,
  ContainerItem,
  TruckItem,
  TrailerItem,
  DriverItem,
} from '../../types';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const PAGE_SIZE = 10;

const KIND_TABS: { label: string; value: ResourceKind | 'ALL' }[] = [
  { label: 'All', value: 'ALL' },
  { label: 'Container', value: 'CONTAINER' },
  { label: 'Truck', value: 'TRUCK' },
  { label: 'Trailer', value: 'TRAILER' },
  { label: 'Driver', value: 'DRIVER' },
];

const KIND_CLASS: Record<ResourceKind, string> = {
  CONTAINER: 'bg-blue-500 hover:bg-blue-500/90 text-white border-transparent',
  TRUCK:     'bg-green-500 hover:bg-green-500/90 text-white border-transparent',
  TRAILER:   'bg-amber-500 hover:bg-amber-500/90 text-white border-transparent',
  DRIVER:    'bg-purple-500 hover:bg-purple-500/90 text-white border-transparent',
};

const STATUS_CLASS: Record<string, string> = {
  AVAILABLE:   'bg-green-100 text-green-700 border-green-200 dark:bg-green-900/30 dark:text-green-400 dark:border-green-800',
  IN_USE:      'bg-blue-100 text-blue-700 border-blue-200 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-800',
  MAINTENANCE: 'bg-amber-100 text-amber-700 border-amber-200 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-800',
  OFF:         'bg-red-100 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800',
};

const STATUS_DOT: Record<string, string> = {
  AVAILABLE:   'bg-green-500',
  IN_USE:      'bg-blue-500',
  MAINTENANCE: 'bg-amber-500',
  OFF:         'bg-red-500',
};

const SIZE_LABEL: Record<ResourceContainerSize, string> = {
  TWENTY: '20 ft',
  FORTY: '40 ft',
};

type SortField = 'identifier' | 'kind' | 'size' | 'status' | 'currentLocationCode';

// -------------------------------------------------------------------------
// Helpers
// -------------------------------------------------------------------------

function normalize(
  containers: ContainerItem[],
  trucks: TruckItem[],
  trailers: TrailerItem[],
  drivers: DriverItem[]
): ResourceRow[] {
  return [
    ...containers.map((c) => ({
      id: c.id,
      kind: 'CONTAINER' as ResourceKind,
      identifier: c.code,
      status: c.status,
      size: c.size,
      currentLocationCode: c.currentLocationCode,
    })),
    ...trucks.map((t) => ({
      id: t.id,
      kind: 'TRUCK' as ResourceKind,
      identifier: t.code,
      status: t.status,
      size: null,
      currentLocationCode: t.currentLocationCode,
    })),
    ...trailers.map((t) => ({
      id: t.id,
      kind: 'TRAILER' as ResourceKind,
      identifier: t.code,
      status: t.status,
      size: null,
      currentLocationCode: t.currentLocationCode,
    })),
    ...drivers.map((d) => ({
      id: d.userId,
      kind: 'DRIVER' as ResourceKind,
      identifier: d.name,
      status: d.status,
      size: null,
      currentLocationCode: null,
    })),
  ];
}

// -------------------------------------------------------------------------
// Sub-components
// -------------------------------------------------------------------------

function KindBadge({ kind }: { kind: ResourceKind }) {
  const labels: Record<ResourceKind, string> = {
    CONTAINER: 'Container',
    TRUCK: 'Truck',
    TRAILER: 'Trailer',
    DRIVER: 'Driver',
  };
  return (
    <Badge className={cn('rounded px-2 py-0.5 text-xs font-bold tracking-wide', KIND_CLASS[kind])}>
      {labels[kind]}
    </Badge>
  );
}

function StatusBadge({ status }: { status: string }) {
  const label = status.replace('_', ' ');
  return (
    <Badge className={cn('gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium', STATUS_CLASS[status] ?? '')}>
      <span className={cn('h-1.5 w-1.5 rounded-full', STATUS_DOT[status] ?? 'bg-gray-400')} />
      {label}
    </Badge>
  );
}

function SortIcon({
  field,
  sortBy,
  sortDirection,
}: {
  field: SortField;
  sortBy: SortField;
  sortDirection: 'asc' | 'desc';
}) {
  if (sortBy !== field)
    return <ArrowUpDown className='ml-1 inline h-3 w-3 opacity-40' />;
  return sortDirection === 'asc' ? (
    <ArrowUp className='ml-1 inline h-3 w-3' />
  ) : (
    <ArrowDown className='ml-1 inline h-3 w-3' />
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

function EmptyState({ colSpan }: { colSpan: number }) {
  return (
    <TableRow>
      <TableCell colSpan={colSpan}>
        <div className='flex flex-col items-center justify-center gap-3 py-16 text-center'>
          <Package2 className='h-12 w-12 text-muted-foreground/40' />
          <p className='text-sm font-medium text-muted-foreground'>No resources found</p>
          <p className='text-xs text-muted-foreground/70'>
            Create your first resource using the button above.
          </p>
        </div>
      </TableCell>
    </TableRow>
  );
}

// -------------------------------------------------------------------------
// Table config per tab
// -------------------------------------------------------------------------

interface TableConfig {
  showKind: boolean;
  showSize: boolean;
  showLocation: boolean;
  identifierLabel: string;
}

function getTableConfig(tab: ResourceKind | 'ALL'): TableConfig {
  switch (tab) {
    case 'CONTAINER': return { showKind: false, showSize: true,  showLocation: true,  identifierLabel: 'Code' };
    case 'TRUCK':     return { showKind: false, showSize: false, showLocation: true,  identifierLabel: 'Code' };
    case 'TRAILER':   return { showKind: false, showSize: false, showLocation: true,  identifierLabel: 'Code' };
    case 'DRIVER':    return { showKind: false, showSize: false, showLocation: false, identifierLabel: 'Name' };
    default:          return { showKind: true,  showSize: true,  showLocation: true,  identifierLabel: 'Code / Name' };
  }
}

// -------------------------------------------------------------------------
// Main Page
// -------------------------------------------------------------------------

export function ResourcesPage() {
  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const [activeTab, setActiveTab] = useState<ResourceKind | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<SortField>('identifier');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [page, setPage] = useState(0);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedResource, setSelectedResource] = useState<ResourceRow | null>(null);

  const skip = !isDispatcher;
  const { data: containersData, isLoading: loadingContainers, isError: errorContainers } =
    useGetDispatcherContainersQuery(undefined, { skip });
  const { data: trucksData, isLoading: loadingTrucks, isError: errorTrucks } =
    useGetDispatcherTrucksQuery(undefined, { skip });
  const { data: trailersData, isLoading: loadingTrailers, isError: errorTrailers } =
    useGetDispatcherTrailersQuery(undefined, { skip });
  const { data: driversData, isLoading: loadingDrivers, isError: errorDrivers } =
    useGetDispatcherDriversQuery(undefined, { skip });

  const isLoading = loadingContainers || loadingTrucks || loadingTrailers || loadingDrivers;
  const isError = errorContainers || errorTrucks || errorTrailers || errorDrivers;

  const allRows = useMemo(
    () =>
      normalize(
        containersData?.data ?? [],
        trucksData?.data ?? [],
        trailersData?.data ?? [],
        driversData?.data ?? []
      ),
    [containersData, trucksData, trailersData, driversData]
  );

  const filteredRows = useMemo(() => {
    let result = allRows;

    if (activeTab !== 'ALL') {
      result = result.filter((r) => r.kind === activeTab);
    }

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (r) =>
          r.identifier.toLowerCase().includes(q) ||
          r.kind.toLowerCase().includes(q) ||
          (r.currentLocationCode?.toLowerCase().includes(q) ?? false)
      );
    }

    return [...result].sort((a, b) => {
      const aVal = (a[sortBy] ?? '') as string;
      const bVal = (b[sortBy] ?? '') as string;
      if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }, [allRows, activeTab, searchQuery, sortBy, sortDirection]);

  const totalElements = filteredRows.length;
  const totalPages = Math.ceil(totalElements / PAGE_SIZE);
  const pagedRows = filteredRows.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  const handleTabChange = (tab: string) => {
    setActiveTab(tab as ResourceKind | 'ALL');
    setPage(0);
  };

  const handleSort = (field: SortField) => {
    if (sortBy === field) {
      setSortDirection((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDirection('asc');
    }
    setPage(0);
  };

  const defaultKind: ResourceKind =
    activeTab === 'ALL' ? 'CONTAINER' : activeTab;

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

  const cfg = getTableConfig(activeTab);
  const colCount = 2 + (cfg.showKind ? 1 : 0) + (cfg.showSize ? 1 : 0) + 1 + (cfg.showLocation ? 1 : 0);

  const sortableHeadClass =
    'cursor-pointer select-none px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground hover:text-foreground transition-colors';

  return (
    <div className='flex min-h-screen flex-col bg-background px-12 py-6'>
      {/* ---- Page heading ---- */}
      <div className='mb-6 flex flex-col gap-1'>
        <h1 className='text-2xl font-bold tracking-tight text-foreground'>Resources</h1>
        <p className='text-sm text-muted-foreground'>
          Manage containers, trucks, trailers, and drivers
        </p>
      </div>

      {/* ---- Toolbar ---- */}
      <div className='mb-4 flex items-center justify-between gap-4'>
        <div className='relative w-72'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            id='resources-search'
            type='text'
            placeholder='Search by code, name, or location…'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className='pl-9'
          />
        </div>

        {activeTab !== 'DRIVER' && (
          <Button
            id='resources-create-btn'
            variant='outline'
            size='sm'
            onClick={() => setIsCreateOpen(true)}
          >
            <Plus className='h-4 w-4' />
            Create a new resource
          </Button>
        )}
      </div>

      {/* ---- Main card ---- */}
      <Card className='flex flex-1 flex-col overflow-hidden gap-0 py-0'>
        {/* Kind filter tabs */}
        <div className='border-b border-border px-4 pt-3'>
          <Tabs value={activeTab} onValueChange={handleTabChange}>
            <TabsList className='h-auto gap-1 rounded-none border-0 bg-transparent p-0 flex w-full'>
              {KIND_TABS.map((tab) => (
                <TabsTrigger
                  key={tab.value}
                  value={tab.value}
                  className='flex-1 text-center rounded-t-md hover:bg-gray-100 dark:hover:bg-gray-800 rounded-b-none border border-b-0 border-transparent transition-colors px-4 py-2.5 text-sm data-[state=active]:border-border data-[state=active]:bg-gray-200 dark:data-[state=active]:bg-gray-700 data-[state=active]:shadow-none'
                >
                  {tab.label}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>
        </div>

        <CardContent className='flex-1 overflow-auto p-0'>
          {isError ? (
            <div className='flex flex-col items-center justify-center gap-3 py-20'>
              <AlertCircle className='h-12 w-12 text-destructive/60' />
              <p className='text-sm font-medium text-muted-foreground'>
                Failed to load resources. Please try again.
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow className='bg-muted/20 hover:bg-muted/20'>
                  <TableHead className='w-12 px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                    No
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('identifier')}
                  >
                    {cfg.identifierLabel}
                    <SortIcon field='identifier' sortBy={sortBy} sortDirection={sortDirection} />
                  </TableHead>
                  {cfg.showKind && (
                    <TableHead
                      className={sortableHeadClass}
                      onClick={() => handleSort('kind')}
                    >
                      Kind
                      <SortIcon field='kind' sortBy={sortBy} sortDirection={sortDirection} />
                    </TableHead>
                  )}
                  {cfg.showSize && (
                    <TableHead
                      className={sortableHeadClass}
                      onClick={() => handleSort('size')}
                    >
                      Size
                      <SortIcon field='size' sortBy={sortBy} sortDirection={sortDirection} />
                    </TableHead>
                  )}
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('status')}
                  >
                    Status
                    <SortIcon field='status' sortBy={sortBy} sortDirection={sortDirection} />
                  </TableHead>
                  {cfg.showLocation && (
                    <TableHead
                      className={sortableHeadClass}
                      onClick={() => handleSort('currentLocationCode')}
                    >
                      Current Location
                      <SortIcon field='currentLocationCode' sortBy={sortBy} sortDirection={sortDirection} />
                    </TableHead>
                  )}
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  [...Array(6)].map((_, i) => <SkeletonRow key={i} cols={colCount} />)
                ) : pagedRows.length === 0 ? (
                  <EmptyState colSpan={colCount} />
                ) : (
                  pagedRows.map((row, idx) => (
                    <TableRow
                      key={`${row.kind}-${row.id}`}
                      id={`resource-row-${row.kind}-${row.id}`}
                      className='cursor-pointer hover:bg-muted/50 transition-colors'
                      onClick={() => setSelectedResource(row)}
                    >
                      <TableCell className='px-4 py-3 text-muted-foreground'>
                        {page * PAGE_SIZE + idx + 1}
                      </TableCell>
                      <TableCell className='px-4 py-3 font-medium text-foreground'>
                        {row.identifier}
                      </TableCell>
                      {cfg.showKind && (
                        <TableCell className='px-4 py-3'>
                          <KindBadge kind={row.kind} />
                        </TableCell>
                      )}
                      {cfg.showSize && (
                        <TableCell className='px-4 py-3 text-foreground'>
                          {row.size ? SIZE_LABEL[row.size] ?? row.size : (
                            <span className='text-muted-foreground'>—</span>
                          )}
                        </TableCell>
                      )}
                      <TableCell className='px-4 py-3'>
                        <StatusBadge status={row.status} />
                      </TableCell>
                      {cfg.showLocation && (
                        <TableCell className='px-4 py-3 text-foreground'>
                          {row.currentLocationCode ?? (
                            <span className='text-muted-foreground'>—</span>
                          )}
                        </TableCell>
                      )}
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>

        {/* Pagination footer */}
        {!isLoading && !isError && totalElements > 0 && (
          <div className='flex items-center justify-between border-t border-border px-4 py-3'>
            <p className='text-xs text-muted-foreground'>
              {`Showing ${page * PAGE_SIZE + 1}–${Math.min(
                (page + 1) * PAGE_SIZE,
                totalElements
              )} of ${totalElements} resource${totalElements !== 1 ? 's' : ''}`}
            </p>
            <div className='flex items-center gap-2'>
              <Button
                id='resources-pagination-prev'
                variant='outline'
                size='icon'
                className='h-8 w-8'
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                <ChevronLeft className='h-4 w-4' />
              </Button>

              <span className='min-w-20 text-center text-xs text-muted-foreground'>
                {isLoading ? (
                  <Loader2 className='mx-auto h-4 w-4 animate-spin' />
                ) : (
                  `Page ${page + 1} / ${totalPages}`
                )}
              </span>

              <Button
                id='resources-pagination-next'
                variant='outline'
                size='icon'
                className='h-8 w-8'
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                <ChevronRight className='h-4 w-4' />
              </Button>
            </div>
          </div>
        )}
      </Card>

      {/* ---- Create Resource Dialog ---- */}
      <CreateResourceDialog
        open={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSuccess={() => setIsCreateOpen(false)}
        defaultKind={defaultKind}
      />

      {/* ---- Resource Detail Sheet ---- */}
      <ResourceDetailSheet
        resource={selectedResource}
        open={selectedResource !== null}
        onClose={() => setSelectedResource(null)}
      />
    </div>
  );
}
