'use client';

import React, { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Search,
  Package,
  MapPin,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import { AccessDenied } from '@/modules/account/components';
import {
  Badge,
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
  Button,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetCustomerRequestsQuery } from '../../api/ttcrsApi';
import type { RequestStatus, RequestType, TtcrsRequest } from '../../types';

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
  OF: 'bg-green-500 text-white border-transparent',
  IF: 'bg-blue-500 text-white border-transparent',
  OE: 'bg-amber-500 text-white border-transparent',
  IE: 'bg-purple-500 text-white border-transparent',
};

const STATUS_CLASS: Record<RequestStatus, string> = {
  PENDING: 'bg-amber-100 text-amber-700 border-amber-200',
  PLANNED: 'bg-blue-100 text-blue-700 border-blue-200',
  IN_PROGRESS: 'bg-indigo-100 text-indigo-700 border-indigo-200',
  COMPLETED: 'bg-green-100 text-green-700 border-green-200',
  CANCELLED: 'bg-red-100 text-red-700 border-red-200',
};

const PAGE_SIZE = 10;

function formatId(id: number) {
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

function TypeBadge({ type }: { type: RequestType }) {
  return (
    <Badge className={cn('rounded px-2 py-0.5 text-xs font-bold', TYPE_CLASS[type])}>
      {type}
    </Badge>
  );
}

function StatusBadge({ status }: { status: RequestStatus }) {
  return (
    <Badge className={cn('rounded-full px-2.5 py-0.5 text-xs font-medium border', STATUS_CLASS[status])}>
      {status.replace('_', ' ')}
    </Badge>
  );
}

function SkeletonRow({ cols }: { cols: number }) {
  return (
    <TableRow>
      {[...Array(cols)].map((_, i) => (
        <TableCell key={i}><Skeleton className="h-4 w-full" /></TableCell>
      ))}
    </TableRow>
  );
}

function EmptyState({ cols }: { cols: number }) {
  return (
    <TableRow>
      <TableCell colSpan={cols}>
        <div className="flex flex-col items-center justify-center gap-3 py-16 text-center">
          <Package className="h-12 w-12 text-muted-foreground/40" />
          <p className="text-sm font-medium text-muted-foreground">No requests found</p>
        </div>
      </TableCell>
    </TableRow>
  );
}

// -------------------------------------------------------------------------
// Main Page
// -------------------------------------------------------------------------

export function CustomerDashboardPage() {
  const user = useAppSelector(selectUserProfile);
  const router = useRouter();

  const isCustomer = user?.roles?.includes('TTCRS_CUSTOMER') ?? false;

  const [activeTab, setActiveTab] = useState<RequestStatus | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);

  const queryParams = useMemo(
    () => ({
      ...(activeTab !== 'ALL' ? { statuses: [activeTab] } : {}),
      page,
      size: PAGE_SIZE,
      sortBy: 'id',
      sortDirection: 'desc' as const,
    }),
    [activeTab, page],
  );

  const { data, isLoading, isError, isFetching } = useGetCustomerRequestsQuery(queryParams, {
    skip: !isCustomer,
  });

  const items: TtcrsRequest[] = data?.data?.items ?? [];
  const totalPages = data?.data?.totalPages ?? 0;
  const totalElements = data?.data?.totalElements ?? 0;

  const filteredItems = useMemo(() => {
    if (!searchQuery.trim()) return items;
    const q = searchQuery.toLowerCase();
    return items.filter(
      (r) =>
        (r.srcLocationCode ?? '').toLowerCase().includes(q) ||
        r.destLocationCode.toLowerCase().includes(q) ||
        formatId(r.id).toLowerCase().includes(q),
    );
  }, [items, searchQuery]);

  if (!isCustomer && user !== null) {
    return (
      <AccessDenied
        reason="authorization"
        title="Access Denied"
        description="You don't have the TTCRS_CUSTOMER role required to access this page."
        variant="minimal"
        size="md"
        showBackButton
        showHomeButton
      />
    );
  }

  const colCount = 7;

  return (
    <div className="space-y-4 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">My Requests</h1>
          <p className="text-sm text-muted-foreground">Track your transport requests</p>
        </div>
      </div>

      {/* Status tabs */}
      <div className="border-b border-border px-0 pt-1">
        <Tabs value={activeTab} onValueChange={(v) => { setActiveTab(v as RequestStatus | 'ALL'); setPage(0); }}>
          <TabsList className="h-auto gap-1 rounded-none border-0 bg-transparent p-0 flex w-full">
            {STATUS_TABS.map((tab) => (
              <TabsTrigger
                key={tab.value}
                value={tab.value}
                className="flex-1 text-center rounded-t-md hover:bg-gray-100 dark:hover:bg-gray-800 rounded-b-none border border-b-0 border-transparent transition-colors px-4 py-2.5 text-sm data-[state=active]:border-border data-[state=active]:bg-gray-200 dark:data-[state=active]:bg-gray-700 data-[state=active]:shadow-none"
              >
                {tab.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
      </div>

      <Card>
        <CardContent className="p-0">
          {/* Search bar */}
          <div className="flex items-center gap-3 border-b border-border px-4 py-3">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search by ID, origin, destination…"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9 h-9"
              />
            </div>
          </div>

          {isError ? (
            <div className="flex items-center justify-center py-16 text-sm text-destructive">
              Failed to load requests.
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="px-4 py-3 w-10">#</TableHead>
                  <TableHead className="px-4 py-3">ID</TableHead>
                  <TableHead className="px-4 py-3">Origin</TableHead>
                  <TableHead className="px-4 py-3">Destination</TableHead>
                  <TableHead className="px-4 py-3">Status</TableHead>
                  <TableHead className="px-4 py-3">Type</TableHead>
                  <TableHead className="px-4 py-3">Created At</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading || isFetching ? (
                  [...Array(6)].map((_, i) => <SkeletonRow key={i} cols={colCount} />)
                ) : filteredItems.length === 0 ? (
                  <EmptyState cols={colCount} />
                ) : (
                  filteredItems.map((req, idx) => (
                    <TableRow
                      key={req.id}
                      className="cursor-pointer hover:bg-muted/50 transition-colors"
                      onClick={() => router.push(`/ttcrs/customer/requests/${req.id}`)}
                    >
                      <TableCell className="px-4 py-3 text-muted-foreground">
                        {page * PAGE_SIZE + idx + 1}
                      </TableCell>
                      <TableCell className="px-4 py-3 font-medium">{formatId(req.id)}</TableCell>
                      <TableCell className="px-4 py-3">
                        <div className="flex items-center gap-1.5">
                          <MapPin className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
                          {req.srcLocationCode ?? '—'}
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <div className="flex items-center gap-1.5">
                          <MapPin className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
                          {req.destLocationCode}
                        </div>
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <StatusBadge status={req.status} />
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <TypeBadge type={req.type} />
                      </TableCell>
                      <TableCell className="px-4 py-3">
                        <div className="text-sm tabular-nums">
                          {formatDateTime(req.createdAt)}
                        </div>
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
          <div className="flex items-center justify-between border-t border-border px-4 py-3">
            <p className="text-xs text-muted-foreground">
              {totalElements === 0
                ? 'No results'
                : `Showing ${page * PAGE_SIZE + 1}–${Math.min((page + 1) * PAGE_SIZE, totalElements)} of ${totalElements}`}
            </p>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || isFetching}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-xs text-muted-foreground">
                Page {page + 1} / {totalPages}
              </span>
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || isFetching}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
