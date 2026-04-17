'use client';

import React, { useState, useMemo } from 'react';
import {
  Plus,
  Loader2,
  AlertCircle,
  MapPin,
  Search,
  ChevronLeft,
  ChevronRight,
  ArrowUp,
  ArrowDown,
  ArrowUpDown,
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
import { useGetDispatcherLocationsQuery } from '../../api/ttcrsApi';
import { CreateLocationDialog, LocationDetailSheet } from '../../components';
import type { LocationItem, LocationType } from '../../types';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const LOCATION_PAGE_SIZE = 10;

const TYPE_TABS: { label: string; value: LocationType | 'ALL' }[] = [
  { label: 'All', value: 'ALL' },
  { label: 'Port', value: 'PORT' },
  { label: 'Warehouse', value: 'WAREHOUSE' },
  { label: 'Depot – Container', value: 'DEPOT_CONTAINER' },
  { label: 'Depot – Truck', value: 'DEPOT_TRUCK' },
  { label: 'Depot – Trailer', value: 'DEPOT_TRAILER' },
];

const TYPE_CLASS: Record<LocationType, string> = {
  PORT:            'bg-blue-500 hover:bg-blue-500/90 text-white border-transparent',
  WAREHOUSE:       'bg-green-500 hover:bg-green-500/90 text-white border-transparent',
  DEPOT_CONTAINER: 'bg-amber-500 hover:bg-amber-500/90 text-white border-transparent',
  DEPOT_TRUCK:     'bg-purple-500 hover:bg-purple-500/90 text-white border-transparent',
  DEPOT_TRAILER:   'bg-rose-500 hover:bg-rose-500/90 text-white border-transparent',
};

const TYPE_LABEL: Record<LocationType, string> = {
  PORT:            'Port',
  WAREHOUSE:       'Warehouse',
  DEPOT_CONTAINER: 'Depot – Container',
  DEPOT_TRUCK:     'Depot – Truck',
  DEPOT_TRAILER:   'Depot – Trailer',
};

type SortableLocationField = 'locationCode' | 'type' | 'lat' | 'lng';

// -------------------------------------------------------------------------
// Sub-components
// -------------------------------------------------------------------------

function TypeBadge({ type }: { type: LocationType }) {
  return (
    <Badge
      className={cn(
        'rounded px-2 py-0.5 text-xs font-bold tracking-wide',
        TYPE_CLASS[type] ?? 'bg-gray-400 text-white border-transparent'
      )}
    >
      {TYPE_LABEL[type] ?? type}
    </Badge>
  );
}

function SortIcon({
  field,
  sortBy,
  sortDirection,
}: {
  field: SortableLocationField;
  sortBy: SortableLocationField;
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

function SkeletonRow() {
  return (
    <TableRow>
      {[...Array(5)].map((_, i) => (
        <TableCell key={i}>
          <Skeleton className='h-4 w-full' />
        </TableCell>
      ))}
    </TableRow>
  );
}

function EmptyState() {
  return (
    <TableRow>
      <TableCell colSpan={5}>
        <div className='flex flex-col items-center justify-center gap-3 py-16 text-center'>
          <MapPin className='h-12 w-12 text-muted-foreground/40' />
          <p className='text-sm font-medium text-muted-foreground'>
            No locations found
          </p>
          <p className='text-xs text-muted-foreground/70'>
            Create your first location using the button above.
          </p>
        </div>
      </TableCell>
    </TableRow>
  );
}

// -------------------------------------------------------------------------
// Main Page
// -------------------------------------------------------------------------

export function LocationsPage() {
  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const [searchQuery, setSearchQuery] = useState('');
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState<LocationItem | null>(null);
  const [activeTypeTab, setActiveTypeTab] = useState<LocationType | 'ALL'>('ALL');
  const [sortBy, setSortBy] = useState<SortableLocationField>('locationCode');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [page, setPage] = useState(0);

  const handleSort = (field: SortableLocationField) => {
    if (sortBy === field) {
      setSortDirection((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDirection('asc');
    }
    setPage(0);
  };

  const handleTypeTabChange = (tab: string) => {
    setActiveTypeTab(tab as LocationType | 'ALL');
    setPage(0);
  };

  const { data, isLoading, isError, isFetching, refetch } =
    useGetDispatcherLocationsQuery(undefined, { skip: !isDispatcher });

  const items: LocationItem[] = data?.data ?? [];

  const filteredItems = useMemo(() => {
    let result = items;

    if (activeTypeTab !== 'ALL') {
      result = result.filter((loc) => loc.type === activeTypeTab);
    }

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (loc) =>
          loc.locationCode.toLowerCase().includes(q) ||
          loc.type.toLowerCase().includes(q)
      );
    }

    return [...result].sort((a, b) => {
      const aVal = a[sortBy] ?? '';
      const bVal = b[sortBy] ?? '';
      if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }, [items, searchQuery, activeTypeTab, sortBy, sortDirection]);

  const totalElements = filteredItems.length;
  const totalPages = Math.ceil(totalElements / LOCATION_PAGE_SIZE);
  const pagedItems = filteredItems.slice(
    page * LOCATION_PAGE_SIZE,
    (page + 1) * LOCATION_PAGE_SIZE
  );

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

  const sortableHeadClass =
    'cursor-pointer select-none px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground hover:text-foreground transition-colors';

  return (
    <div className='flex min-h-screen flex-col bg-background px-12 py-6'>
      {/* ---- Page heading ---- */}
      <div className='mb-6 flex flex-col gap-1'>
        <h1 className='text-2xl font-bold tracking-tight text-foreground'>
          Locations
        </h1>
        <p className='text-sm text-muted-foreground'>
          Manage ports, warehouses, and depots used in transport requests
        </p>
      </div>

      {/* ---- Toolbar ---- */}
      <div className='mb-4 flex items-center justify-between gap-4'>
        <div className='relative w-72'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            id='locations-search'
            type='text'
            placeholder='Search by code or type…'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className='pl-9'
          />
        </div>

        <Button
          id='locations-create-btn'
          variant='outline'
          size='sm'
          onClick={() => setIsCreateOpen(true)}
        >
          <Plus className='h-4 w-4' />
          Create a new location
        </Button>
      </div>

      {/* ---- Main card ---- */}
      <Card className='flex flex-1 flex-col overflow-hidden gap-0 py-0'>
        {/* Type filter tabs */}
        <div className='border-b border-border px-4 pt-3'>
          <Tabs value={activeTypeTab} onValueChange={handleTypeTabChange}>
            <TabsList className='h-auto gap-1 rounded-none border-0 bg-transparent p-0 flex w-full'>
              {TYPE_TABS.map((tab) => (
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
                Failed to load locations. Please try again.
              </p>
              <Button variant='outline' size='sm' onClick={refetch}>
                Retry
              </Button>
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
                    onClick={() => handleSort('locationCode')}
                  >
                    Location Code
                    <SortIcon field='locationCode' sortBy={sortBy} sortDirection={sortDirection} />
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('type')}
                  >
                    Type
                    <SortIcon field='type' sortBy={sortBy} sortDirection={sortDirection} />
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('lat')}
                  >
                    Latitude
                    <SortIcon field='lat' sortBy={sortBy} sortDirection={sortDirection} />
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('lng')}
                  >
                    Longitude
                    <SortIcon field='lng' sortBy={sortBy} sortDirection={sortDirection} />
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading || isFetching ? (
                  [...Array(5)].map((_, i) => <SkeletonRow key={i} />)
                ) : pagedItems.length === 0 ? (
                  <EmptyState />
                ) : (
                  pagedItems.map((loc, idx) => (
                    <TableRow
                      key={loc.id}
                      id={`location-row-${loc.id}`}
                      className='cursor-pointer hover:bg-muted/50 transition-colors'
                      onClick={() => setSelectedLocation(loc)}
                    >
                      <TableCell className='px-4 py-3 text-muted-foreground'>
                        {page * LOCATION_PAGE_SIZE + idx + 1}
                      </TableCell>
                      <TableCell className='px-4 py-3 font-medium text-foreground'>
                        <div className='flex items-center gap-1.5'>
                          <MapPin className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                          {loc.locationCode}
                        </div>
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <TypeBadge type={loc.type} />
                      </TableCell>
                      <TableCell className='px-4 py-3 font-mono text-sm text-foreground'>
                        {loc.lat !== null ? loc.lat : (
                          <span className='text-muted-foreground'>—</span>
                        )}
                      </TableCell>
                      <TableCell className='px-4 py-3 font-mono text-sm text-foreground'>
                        {loc.lng !== null ? loc.lng : (
                          <span className='text-muted-foreground'>—</span>
                        )}
                      </TableCell>
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
              {`Showing ${page * LOCATION_PAGE_SIZE + 1}–${Math.min(
                (page + 1) * LOCATION_PAGE_SIZE,
                totalElements
              )} of ${totalElements} location${totalElements !== 1 ? 's' : ''}`}
            </p>
            <div className='flex items-center gap-2'>
              <Button
                id='locations-pagination-prev'
                variant='outline'
                size='icon'
                className='h-8 w-8'
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
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
                id='locations-pagination-next'
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

      {/* ---- Create Location Dialog ---- */}
      <CreateLocationDialog
        open={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSuccess={() => setIsCreateOpen(false)}
      />

      {/* ---- Location Detail Sheet ---- */}
      <LocationDetailSheet
        location={selectedLocation}
        open={selectedLocation !== null}
        onClose={() => setSelectedLocation(null)}
      />
    </div>
  );
}
