'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  AlertCircle,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  ChevronLeft,
  ChevronRight,
  Route,
  Search,
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tabs,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetTransportPlansQuery } from '../../api/ttcrsApi';
import type { TransportPlanListItem, TransportPlanStatus } from '../../types';

// -------------------------------------------------------------------------
// Constants
// -------------------------------------------------------------------------

const PAGE_SIZE = 10;

const STATUS_TABS: { label: string; value: TransportPlanStatus | 'ALL' }[] = [
  { label: 'All', value: 'ALL' },
  { label: 'Created', value: 'CREATED' },
  { label: 'Executing', value: 'EXECUTING' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
];

const STATUS_BADGE: Record<TransportPlanStatus, string> = {
  CREATED: 'bg-blue-100 text-blue-700 border-blue-200',
  EXECUTING: 'bg-orange-100 text-orange-700 border-orange-200',
  COMPLETED: 'bg-green-100 text-green-700 border-green-200',
  CANCELLED: 'bg-red-100 text-red-700 border-red-200',
};

type SortField =
  | 'truckCode'
  | 'driverName'
  | 'status'
  | 'startTime'
  | 'stopCount';

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

export function TransportRoutesPage() {
  const router = useRouter();
  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const [search, setSearch] = useState('');
  const [statusTab, setStatusTab] = useState<TransportPlanStatus | 'ALL'>(
    'ALL'
  );
  const [sortBy, setSortBy] = useState<SortField>('startTime');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');
  const [page, setPage] = useState(0);

  const { data, isLoading, isError } = useGetTransportPlansQuery();
  const plans = data?.data ?? [];

  const filtered = useMemo(() => {
    let result = plans;
    if (statusTab !== 'ALL')
      result = result.filter((p) => p.status === statusTab);
    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter(
        (p) =>
          p.truckCode?.toLowerCase().includes(q) ||
          p.driverName?.toLowerCase().includes(q)
      );
    }
    result = [...result].sort((a, b) => {
      let cmp = 0;
      if (sortBy === 'truckCode')
        cmp = (a.truckCode ?? '').localeCompare(b.truckCode ?? '');
      if (sortBy === 'driverName')
        cmp = (a.driverName ?? '').localeCompare(b.driverName ?? '');
      if (sortBy === 'status') cmp = a.status.localeCompare(b.status);
      if (sortBy === 'startTime')
        cmp = (a.startTime ?? '').localeCompare(b.startTime ?? '');
      if (sortBy === 'stopCount') cmp = a.stopCount - b.stopCount;
      return sortDir === 'asc' ? cmp : -cmp;
    });
    return result;
  }, [plans, statusTab, search, sortBy, sortDir]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  const toggleSort = (field: SortField) => {
    if (sortBy === field) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0);
  };

  const SortIcon = ({ field }: { field: SortField }) =>
    sortBy === field ? (
      sortDir === 'asc' ? (
        <ArrowUp className='ml-1 h-3 w-3' />
      ) : (
        <ArrowDown className='ml-1 h-3 w-3' />
      )
    ) : (
      <ArrowUpDown className='ml-1 h-3 w-3 opacity-40' />
    );

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
      <div>
        <h1 className='text-2xl font-bold tracking-tight'>Truck Routes</h1>
        <p className='mt-1 text-sm text-muted-foreground'>
          All transport plans generated by the dispatcher.
        </p>
      </div>

      {/* Filters */}
      <div className='flex flex-wrap items-center gap-3'>
        <div className='relative flex-1 min-w-48 max-w-xs'>
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            className='pl-9'
            placeholder='Search truck or driver…'
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>
      </div>

      {/* Table */}
      <Card className='overflow-hidden py-0'>
        <div className='border-b border-border px-4 pt-3'>
          <Tabs
            value={statusTab}
            onValueChange={(v) => {
              setStatusTab(v as TransportPlanStatus | 'ALL');
              setPage(0);
            }}
          >
            <TabsList className='h-auto gap-1 rounded-none border-0 bg-transparent p-0 flex w-full'>
              {STATUS_TABS.map((t) => (
                <TabsTrigger
                  key={t.value}
                  value={t.value}
                  className='flex-1 text-center rounded-t-md hover:bg-gray-100 dark:hover:bg-gray-800 rounded-b-none border border-b-0 border-transparent transition-colors px-4 py-2.5 text-sm data-[state=active]:border-border data-[state=active]:bg-gray-200 dark:data-[state=active]:bg-gray-700 data-[state=active]:shadow-none'
                >
                  {t.label}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>
        </div>
        <CardContent className='p-0'>
          {isLoading ? (
            <div className='space-y-2 p-4'>
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className='h-10 w-full' />
              ))}
            </div>
          ) : isError ? (
            <div className='flex items-center gap-3 p-8 text-destructive'>
              <AlertCircle className='h-5 w-5' />
              <span className='text-sm'>Failed to load transport plans.</span>
            </div>
          ) : filtered.length === 0 ? (
            <div className='flex flex-col items-center gap-3 py-20 text-muted-foreground'>
              <Route className='h-10 w-10 opacity-30' />
              <p className='text-sm'>No truck routes found.</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='w-8 text-center text-muted-foreground'>
                    #
                  </TableHead>
                  <TableHead>
                    <button
                      className='flex items-center text-xs font-medium uppercase'
                      onClick={() => toggleSort('truckCode')}
                    >
                      Truck <SortIcon field='truckCode' />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      className='flex items-center text-xs font-medium uppercase'
                      onClick={() => toggleSort('driverName')}
                    >
                      Driver <SortIcon field='driverName' />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      className='flex items-center text-xs font-medium uppercase'
                      onClick={() => toggleSort('status')}
                    >
                      Status <SortIcon field='status' />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      className='flex items-center text-xs font-medium uppercase'
                      onClick={() => toggleSort('startTime')}
                    >
                      Start Time <SortIcon field='startTime' />
                    </button>
                  </TableHead>
                  <TableHead>End Time</TableHead>
                  <TableHead>
                    <button
                      className='flex items-center text-xs font-medium uppercase'
                      onClick={() => toggleSort('stopCount')}
                    >
                      Stops <SortIcon field='stopCount' />
                    </button>
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {paginated.map((plan, idx) => (
                  <TableRow
                    key={plan.id}
                    className='cursor-pointer hover:bg-muted/50'
                    onClick={() =>
                      router.push(`/ttcrs/dispatcher/routes/${plan.id}`)
                    }
                  >
                    <TableCell className='text-center text-xs text-muted-foreground'>
                      {page * PAGE_SIZE + idx + 1}
                    </TableCell>
                    <TableCell className='font-mono text-sm font-medium'>
                      {plan.truckCode}
                    </TableCell>
                    <TableCell className='text-sm'>
                      {plan.driverName ?? '—'}
                    </TableCell>
                    <TableCell>
                      <Badge
                        className={cn(
                          'border text-xs',
                          STATUS_BADGE[plan.status]
                        )}
                      >
                        {plan.status}
                      </Badge>
                    </TableCell>
                    <TableCell className='text-sm tabular-nums'>
                      {formatDateTime(plan.startTime)}
                    </TableCell>
                    <TableCell className='text-sm tabular-nums'>
                      {formatDateTime(plan.endTime)}
                    </TableCell>
                    <TableCell className='text-sm tabular-nums'>
                      {plan.stopCount}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Pagination */}
      {filtered.length > PAGE_SIZE && (
        <div className='flex items-center justify-between text-sm text-muted-foreground'>
          <span>
            {page * PAGE_SIZE + 1}–
            {Math.min((page + 1) * PAGE_SIZE, filtered.length)} of{' '}
            {filtered.length}
          </span>
          <div className='flex items-center gap-1'>
            <Button
              variant='outline'
              size='icon'
              className='h-8 w-8'
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              <ChevronLeft className='h-4 w-4' />
            </Button>
            <span className='px-2'>
              {page + 1} / {totalPages}
            </span>
            <Button
              variant='outline'
              size='icon'
              className='h-8 w-8'
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              <ChevronRight className='h-4 w-4' />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
