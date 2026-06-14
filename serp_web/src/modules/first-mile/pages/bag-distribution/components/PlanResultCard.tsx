/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution plan result card
 */

import { Badge } from '@/shared/components/ui/badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

import type {
  BagDistributionPlan,
  BagDistributionPlanItem,
} from '../../../types';
import { formatNumber, HINT_LABELS } from '../bagDistributionModels';

interface PlanResultCardProps {
  plan: BagDistributionPlan | null;
}

export function PlanResultCard({ plan }: PlanResultCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Auto-plan preview</CardTitle>
        <CardDescription>
          Suggestions are grouped by lane and scored by route match, vehicle
          capacity, and driver availability.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {!plan ? (
          <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
            Run Preview auto plan to see suggested manifests before execution.
          </div>
        ) : plan.items.length === 0 ? (
          <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
            No eligible sealed bags were found for this lane and time window.
          </div>
        ) : (
          <div className='space-y-3'>
            <div className='flex flex-wrap items-center gap-2 text-sm'>
              <Badge variant={plan.executed ? 'default' : 'outline'}>
                {plan.executed ? 'Executed' : 'Preview'}
              </Badge>
              <span className='text-muted-foreground'>
                {plan.manifestCount} manifest
                {plan.manifestCount === 1 ? '' : 's'}
              </span>
            </div>
            <div className='overflow-x-auto rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Route</TableHead>
                    <TableHead>Vehicle</TableHead>
                    <TableHead>Bags</TableHead>
                    <TableHead>Load</TableHead>
                    <TableHead>Score</TableHead>
                    <TableHead>Hints</TableHead>
                    <TableHead>Created</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {plan.items.map((item, index) => (
                    <PlanItemRow key={`${item.routeId}-${index}`} item={item} />
                  ))}
                </TableBody>
              </Table>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function PlanItemRow({ item }: { item: BagDistributionPlanItem }) {
  return (
    <TableRow>
      <TableCell>{item.routeCode ?? '-'}</TableCell>
      <TableCell>{item.vehicleLicensePlate ?? '-'}</TableCell>
      <TableCell>
        <div className='font-medium'>{formatNumber(item.bagIds.length, 0)}</div>
        <div className='max-w-[220px] truncate text-xs text-muted-foreground'>
          {item.bagCodes.join(', ') || '-'}
        </div>
      </TableCell>
      <TableCell>
        {formatNumber(item.totalOrders, 0)} orders
        <div className='text-xs text-muted-foreground'>
          {formatNumber(item.totalWeight)} kg | {formatNumber(item.totalVolume)}{' '}
          m3
        </div>
      </TableCell>
      <TableCell>{formatNumber(item.score)}</TableCell>
      <TableCell>
        <div className='flex max-w-[260px] flex-wrap gap-1'>
          {item.hints.length === 0 ? (
            <Badge variant='outline'>No hints</Badge>
          ) : (
            item.hints.map((hint) => (
              <Badge
                key={hint}
                variant={
                  hint === 'NO_ROUTE' ||
                  hint === 'NO_DRIVER' ||
                  hint === 'SCHEDULE_CONFLICT'
                    ? 'destructive'
                    : 'secondary'
                }
              >
                {HINT_LABELS[hint] ?? hint}
              </Badge>
            ))
          )}
        </div>
      </TableCell>
      <TableCell>{item.createdManifestCode ?? '-'}</TableCell>
    </TableRow>
  );
}
