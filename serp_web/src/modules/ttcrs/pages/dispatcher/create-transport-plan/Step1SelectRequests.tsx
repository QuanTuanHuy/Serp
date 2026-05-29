import { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  AlertCircle,
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  Loader2,
  MapPin,
  Package,
} from 'lucide-react';
import {
  Button,
  Card,
  CardContent,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetDispatcherRequestsQuery } from '../../../api/ttcrsApi';
import type { TtcrsRequest } from '../../../types';
import { PAGE_SIZE } from './constants';
import { SkeletonRow, TypeBadge } from './common';
import { formatId } from './helpers';

interface Step1Props {
  selectedIds: Set<number>;
  onToggle: (id: number) => void;
  onToggleAll: (ids: number[]) => void;
  onNext: () => void;
  isNextLoading: boolean;
}

function formatDateTime(dt: string | null) {
  if (!dt) return '—';
  const [datePart, timePart] = dt.replace('T', ' ').split(' ');
  if (!datePart || !timePart) return dt.replace('T', ' ').slice(0, 16);

  const [year, month, day] = datePart.split('-');
  if (!year || !month || !day) return dt.replace('T', ' ').slice(0, 16);

  return `${day}-${month}-${year} ${timePart.slice(0, 5)}`;
}

export function Step1SelectRequests({
  selectedIds,
  onToggle,
  onToggleAll,
  onNext,
  isNextLoading,
}: Step1Props) {
  const router = useRouter();
  const [page, setPage] = useState(0);

  const { data, isLoading, isError, isFetching } =
    useGetDispatcherRequestsQuery({
      statuses: ['PENDING'],
      page,
      size: PAGE_SIZE,
      sortBy: 'id',
      sortDirection: 'desc',
    });

  const items: TtcrsRequest[] = data?.data?.items ?? [];
  const totalPages = data?.data?.totalPages ?? 0;
  const totalElements = data?.data?.totalElements ?? 0;
  const pageIds = items.map((request) => request.id);
  const allPageSelected =
    pageIds.length > 0 && pageIds.every((id) => selectedIds.has(id));

  return (
    <div className='flex flex-col gap-4'>
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
            className='bg-orange-600 text-white hover:bg-orange-700'
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

      <Card className='flex flex-col gap-0 overflow-hidden py-0'>
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
                    Created At
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading || isFetching ? (
                  [...Array(6)].map((_, index) => (
                    <SkeletonRow key={index} cols={6} />
                  ))
                ) : items.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <div className='flex flex-col items-center justify-center gap-3 py-16 text-center'>
                        <Package className='h-12 w-12 text-muted-foreground/40' />
                        <p className='text-sm font-medium text-muted-foreground'>
                          No pending requests found
                        </p>
                        <p className='text-xs text-muted-foreground/70'>
                          All transport requests are either planned, in
                          progress, or completed.
                        </p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  items.map((request) => (
                    <TableRow
                      key={request.id}
                      className={cn(
                        'cursor-pointer',
                        selectedIds.has(request.id) &&
                          'bg-orange-50 dark:bg-orange-950/20'
                      )}
                      onClick={() => onToggle(request.id)}
                    >
                      <TableCell
                        className='px-4 py-3'
                        onClick={(e) => e.stopPropagation()}
                      >
                        <Checkbox
                          checked={selectedIds.has(request.id)}
                          onCheckedChange={() => onToggle(request.id)}
                        />
                      </TableCell>
                      <TableCell className='px-4 py-3 font-medium text-foreground'>
                        {formatId(request.id)}
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <div className='flex items-center gap-1.5'>
                          <MapPin className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                          <span className='text-foreground'>{request.srcLocationCode ?? '—'}</span>
                        </div>
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <div className='flex items-center gap-1.5'>
                          <MapPin className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
                          <span className='text-foreground'>
                            {request.destLocationCode}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className='px-4 py-3'>
                        <TypeBadge type={request.type} />
                      </TableCell>
                      <TableCell className='px-4 py-3 text-sm text-muted-foreground'>
                        {formatDateTime(request.createdAt)}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>

        {!isLoading && !isError && totalPages > 0 && (
          <div className='flex items-center justify-between border-t border-border px-4 py-3'>
            <p className='text-xs text-muted-foreground'>
              {totalElements === 0
                ? 'No results'
                : `Showing ${page * PAGE_SIZE + 1}-${Math.min(
                    (page + 1) * PAGE_SIZE,
                    totalElements
                  )} of ${totalElements} pending requests`}
            </p>
            <div className='flex items-center gap-2'>
              <Button
                variant='outline'
                size='icon'
                className='h-8 w-8'
                onClick={() => setPage((prev) => Math.max(0, prev - 1))}
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
                onClick={() =>
                  setPage((prev) => Math.min(totalPages - 1, prev + 1))
                }
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
