'use client';

import React, { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Search,
  Plus,
  Route,
  Loader2,
  AlertCircle,
  Package,
  MapPin,
  ChevronLeft,
  ChevronRight,
  ArrowUp,
  ArrowDown,
  ArrowUpDown,
  RotateCcw,
} from 'lucide-react';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings';
import { AccessDenied } from '@/modules/account/components';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Checkbox,
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
import { toast } from 'sonner';
import {
  useGetDispatcherRequestsQuery,
  useUpdateDispatcherRequestsStatusMutation,
} from '../../api/ttcrsApi';
import { CreateRequestDialog } from '../../components';
import type { RequestStatus, RequestType, TtcrsRequest } from '../../types';

const REQUEST_TYPES: { label: string; value: RequestType | '' }[] = [
  { label: 'All Types', value: '' },
  { label: 'OF - Outbound Full', value: 'OF' },
  { label: 'IF - Inbound Full', value: 'IF' },
  { label: 'OE - Outbound Empty', value: 'OE' },
  { label: 'IE - Inbound Empty', value: 'IE' },
];

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const STATUS_TABS: { label: string; value: RequestStatus | 'ALL' }[] = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'Planned', value: 'PLANNED' },
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
  { label: 'All', value: 'ALL' },
];

const TYPE_CLASS: Record<RequestType, string> = {
  OF: 'bg-green-500 hover:bg-green-500/90 text-white border-transparent',
  IF: 'bg-blue-500 hover:bg-blue-500/90 text-white border-transparent',
  OE: 'bg-amber-500 hover:bg-amber-500/90 text-white border-transparent',
  IE: 'bg-purple-500 hover:bg-purple-500/90 text-white border-transparent',
};

const STATUS_CLASS: Record<RequestStatus, string> = {
  PENDING:
    'bg-amber-100 text-amber-700 border-amber-200 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-800',
  PLANNED:
    'bg-blue-100 text-blue-700 border-blue-200 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-800',
  IN_PROGRESS:
    'bg-indigo-100 text-indigo-700 border-indigo-200 dark:bg-indigo-900/30 dark:text-indigo-400 dark:border-indigo-800',
  COMPLETED:
    'bg-green-100 text-green-700 border-green-200 dark:bg-green-900/30 dark:text-green-400 dark:border-green-800',
  CANCELLED:
    'bg-red-100 text-red-700 border-red-200 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800',
};

const STATUS_DOT: Record<RequestStatus, string> = {
  PENDING: 'bg-amber-500',
  PLANNED: 'bg-blue-500',
  IN_PROGRESS: 'bg-indigo-500',
  COMPLETED: 'bg-green-500',
  CANCELLED: 'bg-red-500',
};

const PAGE_SIZE = 10;

type SortableRequestField =
  | 'id'
  | 'customerId'
  | 'srcLocationCode'
  | 'destLocationCode'
  | 'status'
  | 'type'
  | 'createdAt';

// -------------------------------------------------------------------------
// Helpers
// -------------------------------------------------------------------------

function formatId(id: number): string {
  return `REQ-${String(id).padStart(5, '0')}`;
}

function formatDateTime(dt: string | null) {
  if (!dt) return '—';
  const [datePart, timePart] = dt.replace('T', ' ').split(' ');
  if (!datePart || !timePart) return dt.replace('T', ' ').slice(0, 16);

  const [year, month, day] = datePart.split('-');
  if (!year || !month || !day) return dt.replace('T', ' ').slice(0, 16);

  return `${day}-${month}-${year} ${timePart.slice(0, 5)}`;
}

// -------------------------------------------------------------------------
// Sub-components
// -------------------------------------------------------------------------

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

function StatusBadge({ status }: { status: RequestStatus }) {
  const label = status.replace('_', ' ');
  return (
    <Badge
      className={cn(
        'gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium',
        STATUS_CLASS[status]
      )}
    >
      <span className={cn('h-1.5 w-1.5 rounded-full', STATUS_DOT[status])} />
      {label}
    </Badge>
  );
}

function SortIcon({
  field,
  sortBy,
  sortDirection,
}: {
  field: SortableRequestField;
  sortBy: SortableRequestField;
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

function EmptyState({ tab, cols }: { tab: string; cols: number }) {
  return (
    <TableRow>
      <TableCell colSpan={cols}>
        <div className='flex flex-col items-center justify-center gap-3 py-16 text-center'>
          <Package className='h-12 w-12 text-muted-foreground/40' />
          <p className='text-sm font-medium text-muted-foreground'>
            No requests found
          </p>
          <p className='text-xs text-muted-foreground/70'>
            {tab === 'ALL'
              ? 'No transport requests exist yet.'
              : `No requests with status "${tab.replace('_', ' ')}" found.`}
          </p>
        </div>
      </TableCell>
    </TableRow>
  );
}

// -------------------------------------------------------------------------
// Main Page
// -------------------------------------------------------------------------

export function DispatcherDashboardPage() {
  const user = useAppSelector(selectUserProfile);
  const organizationId = user?.organizationId;
  const router = useRouter();

  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const [activeTab, setActiveTab] = useState<RequestStatus | 'ALL'>('PENDING');
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<RequestType | ''>('');
  const [page, setPage] = useState(0);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [sortBy, setSortBy] = useState<SortableRequestField>('id');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  // Multi-select for status revert actions
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [updateStatus, { isLoading: isReverting }] =
    useUpdateDispatcherRequestsStatusMutation();

  const handleSort = (field: SortableRequestField) => {
    if (sortBy === field) {
      setSortDirection((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDirection('asc');
    }
    setPage(0);
  };

  const queryParams = useMemo(
    () => ({
      ...(activeTab !== 'ALL' ? { statuses: [activeTab] } : {}),
      ...(typeFilter !== '' ? { type: typeFilter } : {}),
      page,
      size: PAGE_SIZE,
      sortBy,
      sortDirection,
    }),
    [activeTab, typeFilter, page, sortBy, sortDirection]
  );

  const { data, isLoading, isError, isFetching } =
    useGetDispatcherRequestsQuery(queryParams, { skip: !isDispatcher });

  const { data: usersData } = useGetOrganizationUsersQuery(
    { organizationId: organizationId!, pageSize: 100, page: 0 },
    { skip: !isDispatcher || !organizationId }
  );

  const items: TtcrsRequest[] = data?.data?.items ?? [];
  const totalPages = data?.data?.totalPages ?? 0;
  const totalElements = data?.data?.totalElements ?? 0;

  const customerMap = useMemo(() => {
    const map = new Map<number, string>();
    const users = usersData?.data?.items ?? [];

    users.forEach((u) => {
      if (!u?.id) return;
      const fullName = `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim();
      const display = fullName || u.email || `Customer #${u.id}`;
      map.set(u.id, display);
    });

    return map;
  }, [usersData]);

  const filteredItems = useMemo(() => {
    if (!searchQuery.trim()) return items;
    const q = searchQuery.toLowerCase();
    return items.filter(
      (r) =>
        (r.srcLocationCode ?? '').toLowerCase().includes(q) ||
        r.destLocationCode.toLowerCase().includes(q) ||
        formatId(r.id).toLowerCase().includes(q) ||
        r.type.toLowerCase().includes(q)
    );
  }, [items, searchQuery]);

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

  const handleTabChange = (tab: string) => {
    setActiveTab(tab as RequestStatus | 'ALL');
    setPage(0);
    setSelectedIds(new Set());
    setTypeFilter('');
  };

  const isPlannedTab = activeTab === 'PLANNED';
  const isInProgressTab = activeTab === 'IN_PROGRESS';
  const showCheckbox = isPlannedTab || isInProgressTab;
  const colCount = showCheckbox ? 9 : 8;

  const toggleRow = (id: number) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleAll = () => {
    const pageIds = filteredItems.map((r) => r.id);
    const allSelected = pageIds.every((id) => selectedIds.has(id));
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (allSelected) {
        pageIds.forEach((id) => next.delete(id));
      } else {
        pageIds.forEach((id) => next.add(id));
      }
      return next;
    });
  };

  const handleRevertToPending = async () => {
    if (selectedIds.size === 0) return;
    try {
      await updateStatus({
        requestIds: Array.from(selectedIds),
        status: 'PENDING',
      }).unwrap();
      toast.success(
        `${selectedIds.size} request${selectedIds.size > 1 ? 's' : ''} reverted to Pending.`
      );
      setSelectedIds(new Set());
    } catch {
      toast.error('Failed to revert requests. Please try again.');
    }
  };

  const handleRevertToPlanned = async () => {
    if (selectedIds.size === 0) return;
    try {
      await updateStatus({
        requestIds: Array.from(selectedIds),
        status: 'PLANNED',
      }).unwrap();
      toast.success(
        `${selectedIds.size} request${selectedIds.size > 1 ? 's' : ''} reverted to Planned.`
      );
      setSelectedIds(new Set());
    } catch {
      toast.error('Failed to revert requests. Please try again.');
    }
  };

  const sortableHeadClass =
    'cursor-pointer select-none px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground hover:text-foreground transition-colors';

  return (
    <div className='flex min-h-screen flex-col bg-background px-12 py-6'>
      {/* ---- Page heading ---- */}
      <div className='mb-6 flex flex-col gap-1'>
        <h1 className='text-2xl font-bold tracking-tight text-foreground'>
          Dispatcher Dashboard
        </h1>
        <p className='text-sm text-muted-foreground'>
          Manage and monitor transport requests
        </p>
      </div>

      {/* ---- Toolbar ---- */}
      <div className='mb-4 flex items-center justify-between gap-4'>
        {/* Search — shadcn Input with icon overlay */}
        <div className='relative w-72'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            id='dispatcher-search'
            type='text'
            placeholder='Search by ID, origin, destination…'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className='pl-9'
          />
        </div>

        {/* <select
          id='dispatcher-type-filter'
          value={typeFilter}
          onChange={(e) => {
            setTypeFilter(e.target.value as RequestType | '');
            setPage(0);
          }}
          className='rounded-lg border border-border bg-background px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary'
        >
          {REQUEST_TYPES.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select> */}

        {/* Action buttons */}
        <div className='flex items-center gap-2'>
          {/* Revert to Pending — PLANNED tab */}
          {isPlannedTab && selectedIds.size > 0 && (
            <Button
              id='dispatcher-revert-to-pending-btn'
              variant='outline'
              size='sm'
              onClick={handleRevertToPending}
              disabled={isReverting}
              className='border-amber-300 text-amber-700 hover:bg-amber-50 hover:text-amber-800 dark:border-amber-700 dark:text-amber-400 dark:hover:bg-amber-950'
            >
              {isReverting ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : (
                <RotateCcw className='h-4 w-4' />
              )}
              Revert to Pending
              <span className='ml-1 rounded-full bg-amber-100 px-1.5 py-0.5 text-xs font-bold text-amber-700 dark:bg-amber-900 dark:text-amber-300'>
                {selectedIds.size}
              </span>
            </Button>
          )}
          {/* Revert to Planned — IN_PROGRESS tab */}
          {isInProgressTab && selectedIds.size > 0 && (
            <Button
              id='dispatcher-revert-to-planned-btn'
              variant='outline'
              size='sm'
              onClick={handleRevertToPlanned}
              disabled={isReverting}
              className='border-blue-300 text-blue-700 hover:bg-blue-50 hover:text-blue-800 dark:border-blue-700 dark:text-blue-400 dark:hover:bg-blue-950'
            >
              {isReverting ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : (
                <RotateCcw className='h-4 w-4' />
              )}
              Revert to Planned
              <span className='ml-1 rounded-full bg-blue-100 px-1.5 py-0.5 text-xs font-bold text-blue-700 dark:bg-blue-900 dark:text-blue-300'>
                {selectedIds.size}
              </span>
            </Button>
          )}
          <Button
            id='dispatcher-create-transport-plan-btn'
            variant='outline'
            size='sm'
            style={{ borderColor: '#3b82f6', color: '#3b82f6' }}
            onClick={() => router.push('/ttcrs/dispatcher/plans/create')}
          >
            <Route className='h-4 w-4' />
            Generate Transport Plan
          </Button>
          <Button
            id='dispatcher-create-manual-route-btn'
            variant='outline'
            size='sm'
            style={{ borderColor: '#22c55e', color: '#22c55e' }}
            onClick={() => router.push('/ttcrs/dispatcher/routes/manual/create')}
          >
            <MapPin className='h-4 w-4' />
            Create Manual Route
          </Button>
          <Button
            id='dispatcher-create-request-btn'
            variant='outline'
            size='sm'
            style={{ borderColor: '#8b5cf6', color: '#8b5cf6' }}
            onClick={() => setIsCreateOpen(true)}
          >
            <Plus className='h-4 w-4' />
            Create a new request
          </Button>
        </div>
      </div>

      {/* ---- Main card — shadcn Card ---- */}
      <Card className='flex flex-1 flex-col overflow-hidden gap-0 py-0'>
        {/* Status tabs — shadcn Tabs */}
        <div className='border-b border-border px-4 pt-3'>
          <Tabs value={activeTab} onValueChange={handleTabChange}>
            <TabsList className='h-auto gap-1 rounded-none border-0 bg-transparent p-0 flex w-full'>
              {STATUS_TABS.map((tab) => (
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

        {/* Table — shadcn Table */}
        <CardContent className='flex-1 overflow-auto p-0'>
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
                  {showCheckbox && (
                    <TableHead className='w-10 px-4 py-3'>
                      <Checkbox
                        checked={
                          filteredItems.length > 0 &&
                          filteredItems.every((r) => selectedIds.has(r.id))
                        }
                        onCheckedChange={toggleAll}
                        disabled={
                          isLoading || isFetching || filteredItems.length === 0
                        }
                      />
                    </TableHead>
                  )}
                  <TableHead className='w-12 px-4 py-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                    No
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('id')}
                  >
                    Request ID
                    <SortIcon
                      field='id'
                      sortBy={sortBy}
                      sortDirection={sortDirection}
                    />
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('customerId')}
                  >
                    Customer
                    <SortIcon
                      field='customerId'
                      sortBy={sortBy}
                      sortDirection={sortDirection}
                    />
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('srcLocationCode')}
                  >
                    Origin
                    <SortIcon
                      field='srcLocationCode'
                      sortBy={sortBy}
                      sortDirection={sortDirection}
                    />
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('destLocationCode')}
                  >
                    Destination
                    <SortIcon
                      field='destLocationCode'
                      sortBy={sortBy}
                      sortDirection={sortDirection}
                    />
                  </TableHead>
                  <TableHead
                    className={sortableHeadClass}
                    onClick={() => handleSort('status')}
                  >
                    Status
                    <SortIcon
                      field='status'
                      sortBy={sortBy}
                      sortDirection={sortDirection}
                    />
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
                    onClick={() => handleSort('createdAt')}
                  >
                    Created At
                    <SortIcon field='createdAt' sortBy={sortBy} sortDirection={sortDirection} />
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading || isFetching ? (
                  [...Array(6)].map((_, i) => (
                    <SkeletonRow key={i} cols={colCount} />
                  ))
                ) : filteredItems.length === 0 ? (
                  <EmptyState tab={activeTab} cols={colCount} />
                ) : (
                  filteredItems.map((req, idx) => (
                    <TableRow
                      key={req.id}
                      id={`dispatcher-request-row-${req.id}`}
                      className={cn(
                        'cursor-pointer hover:bg-muted/50 transition-colors',
                        isPlannedTab &&
                          selectedIds.has(req.id) &&
                          'bg-amber-50 dark:bg-amber-950/20',
                        isInProgressTab &&
                          selectedIds.has(req.id) &&
                          'bg-blue-50 dark:bg-blue-950/20'
                      )}
                      onClick={() => router.push(`/ttcrs/dispatcher/requests/${req.id}`)}
                    >
                      {showCheckbox && (
                        <TableCell
                          className='px-4 py-3'
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleRow(req.id);
                          }}
                        >
                          <Checkbox
                            checked={selectedIds.has(req.id)}
                            onCheckedChange={() => toggleRow(req.id)}
                            disabled={isReverting}
                          />
                        </TableCell>
                      )}
                      <TableCell className='px-4 py-3 text-muted-foreground'>
                        {page * PAGE_SIZE + idx + 1}
                      </TableCell>
                      <TableCell className='px-4 py-3 font-medium text-foreground'>
                        {formatId(req.id)}
                      </TableCell>
                      <TableCell className='px-4 py-3 text-foreground'>
                        {req.customerId
                          ? customerMap.get(req.customerId) ||
                            `Customer #${req.customerId}`
                          : '-'}
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <div className='flex items-center gap-1.5'>
                          <MapPin className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                          <span className='text-foreground'>
                            {req.srcLocationCode ?? '—'}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <div className='flex items-center gap-1.5'>
                          <MapPin className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                          <span className='text-foreground'>
                            {req.destLocationCode}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <StatusBadge status={req.status} />
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <TypeBadge type={req.type} />
                      </TableCell>
                      <TableCell className='px-4 py-3 text-muted-foreground'>
                        {formatDateTime(req.createdAt)}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>

        {/* Pagination footer */}
        {!isLoading && !isError && totalPages > 0 && (
          <div className='flex items-center justify-between border-t border-border px-4 py-3'>
            <p className='text-xs text-muted-foreground'>
              {totalElements === 0
                ? 'No results'
                : `Showing ${page * PAGE_SIZE + 1}–${Math.min(
                    (page + 1) * PAGE_SIZE,
                    totalElements
                  )} of ${totalElements} requests`}
            </p>
            <div className='flex items-center gap-2'>
              <Button
                id='dispatcher-pagination-prev'
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
                id='dispatcher-pagination-next'
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

      {/* ---- Create Request Dialog ---- */}
      <CreateRequestDialog
        open={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSuccess={() => {
          setPage(0);
          setActiveTab('PENDING');
        }}
      />

    </div>
  );
}
