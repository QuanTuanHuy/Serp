/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution manifest tab
 */

import {
  ArrowDownToLine,
  ArrowUpFromLine,
  Eye,
  Loader2,
  XCircle,
} from 'lucide-react';

import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { cn } from '@/shared/utils';

import type { BagDistributionManifest } from '../../../types';
import type { StatusFilter } from '../bagDistributionModels';
import {
  destinationLabel,
  formatDateTime,
  formatNumber,
  getManifestBagStats,
  STATUS_HELP,
  STATUS_LABELS,
  STATUS_OPTIONS,
  statusVariant,
} from '../bagDistributionModels';

interface ManifestTabProps {
  manifests: BagDistributionManifest[];
  selectedManifestId: number | null;
  manifestStatus: StatusFilter;
  isFetching: boolean;
  canManage: boolean;
  canOperate: boolean;
  isConfirmingOutbound: boolean;
  isConfirmingInbound: boolean;
  isCancelling: boolean;
  onStatusChange: (value: StatusFilter) => void;
  onSelectManifest: (id: number | null) => void;
  onViewManifest: (id: number) => void;
  onConfirmOutbound: (manifest: BagDistributionManifest) => void;
  onConfirmInbound: (manifest: BagDistributionManifest) => void;
  onCancel: (manifest: BagDistributionManifest) => void;
}

export function ManifestTab({
  manifests,
  selectedManifestId,
  manifestStatus,
  isFetching,
  canManage,
  canOperate,
  isConfirmingOutbound,
  isConfirmingInbound,
  isCancelling,
  onStatusChange,
  onSelectManifest,
  onViewManifest,
  onConfirmOutbound,
  onConfirmInbound,
  onCancel,
}: ManifestTabProps) {
  return (
    <Card>
      <CardHeader>
        <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
          <div>
            <CardTitle>Distribution manifests</CardTitle>
            <CardDescription>
              Track manifest state and move bags from created to in transit to
              arrived.
            </CardDescription>
          </div>
          <Select
            value={manifestStatus}
            onValueChange={(value) => onStatusChange(value as StatusFilter)}
          >
            <SelectTrigger className='w-full lg:w-[220px]'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ALL'>All statuses</SelectItem>
              {STATUS_OPTIONS.map((status) => (
                <SelectItem key={status} value={status}>
                  {STATUS_LABELS[status]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardHeader>
      <CardContent>
        <ManifestTable
          manifests={manifests}
          selectedManifestId={selectedManifestId}
          isFetching={isFetching}
          canManage={canManage}
          canOperate={canOperate}
          isConfirmingOutbound={isConfirmingOutbound}
          isConfirmingInbound={isConfirmingInbound}
          isCancelling={isCancelling}
          onSelectManifest={onSelectManifest}
          onViewManifest={onViewManifest}
          onConfirmOutbound={onConfirmOutbound}
          onConfirmInbound={onConfirmInbound}
          onCancel={onCancel}
        />
      </CardContent>
    </Card>
  );
}

interface ManifestTableProps {
  manifests: BagDistributionManifest[];
  selectedManifestId: number | null;
  isFetching: boolean;
  canManage: boolean;
  canOperate: boolean;
  isConfirmingOutbound: boolean;
  isConfirmingInbound: boolean;
  isCancelling: boolean;
  onSelectManifest: (id: number | null) => void;
  onViewManifest: (id: number) => void;
  onConfirmOutbound: (manifest: BagDistributionManifest) => void;
  onConfirmInbound: (manifest: BagDistributionManifest) => void;
  onCancel: (manifest: BagDistributionManifest) => void;
}

function ManifestTable({
  manifests,
  selectedManifestId,
  isFetching,
  canManage,
  canOperate,
  isConfirmingOutbound,
  isConfirmingInbound,
  isCancelling,
  onSelectManifest,
  onViewManifest,
  onConfirmOutbound,
  onConfirmInbound,
  onCancel,
}: ManifestTableProps) {
  return (
    <div className='overflow-x-auto rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Manifest</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Lane</TableHead>
            <TableHead>Resource</TableHead>
            <TableHead>Schedule</TableHead>
            <TableHead>Bags</TableHead>
            <TableHead className='text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {manifests.length === 0 ? (
            <TableRow>
              <TableCell colSpan={7} className='h-24 text-center'>
                {isFetching ? (
                  <span className='inline-flex items-center gap-2 text-muted-foreground'>
                    <Loader2 className='h-4 w-4 animate-spin' />
                    Loading manifests...
                  </span>
                ) : (
                  <span className='text-muted-foreground'>
                    No manifests match the current filters.
                  </span>
                )}
              </TableCell>
            </TableRow>
          ) : (
            manifests.map((manifest) => {
              const stats = getManifestBagStats(manifest);
              return (
                <TableRow
                  key={manifest.id}
                  className={cn(
                    'cursor-pointer',
                    selectedManifestId === manifest.id && 'bg-muted/50'
                  )}
                  onClick={() =>
                    onSelectManifest(
                      selectedManifestId === manifest.id ? null : manifest.id
                    )
                  }
                >
                  <TableCell>
                    <div className='font-medium'>
                      {manifest.manifestCode ?? `Manifest #${manifest.id}`}
                    </div>
                    <div className='text-xs text-muted-foreground'>
                      Driver #{manifest.assignedDriverId ?? '-'}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant={statusVariant(manifest.status)}>
                      {manifest.status
                        ? STATUS_LABELS[manifest.status]
                        : 'Unknown'}
                    </Badge>
                    <div className='mt-1 text-xs text-muted-foreground'>
                      {manifest.status ? STATUS_HELP[manifest.status] : '-'}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>{manifest.originHubCode ?? manifest.originHubId}</div>
                    <div className='text-xs text-muted-foreground'>
                      to{' '}
                      {destinationLabel(
                        manifest.destinationType,
                        manifest.destinationHubId,
                        manifest.destinationHubCode,
                        manifest.destinationPostOfficeCode
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>{manifest.routeCode ?? '-'}</div>
                    <div className='text-xs text-muted-foreground'>
                      {manifest.vehicleLicensePlate ?? '-'}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>{formatDateTime(manifest.plannedDepartureAt)}</div>
                    <div className='text-xs text-muted-foreground'>
                      {formatDateTime(manifest.plannedArrivalAt)}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>{formatNumber(stats.totalBags, 0)} bags</div>
                    <div className='text-xs text-muted-foreground'>
                      {stats.scannedOut}/{stats.totalBags} out |{' '}
                      {stats.scannedIn}/{stats.totalBags} in
                    </div>
                  </TableCell>
                  <TableCell
                    className='text-right'
                    onClick={(event) => event.stopPropagation()}
                  >
                    <div className='flex justify-end gap-2'>
                      <Button
                        variant='ghost'
                        size='sm'
                        onClick={() => onViewManifest(manifest.id)}
                      >
                        <Eye className='h-4 w-4' />
                      </Button>
                      {manifest.status === 'CREATED' && (
                        <Button
                          size='sm'
                          disabled={!canOperate || isConfirmingOutbound}
                          onClick={() => onConfirmOutbound(manifest)}
                        >
                          <ArrowUpFromLine className='h-4 w-4' />
                          Outbound
                        </Button>
                      )}
                      {manifest.status === 'OUTBOUND_CONFIRMED' && (
                        <Button
                          size='sm'
                          variant='secondary'
                          disabled={!canOperate || isConfirmingInbound}
                          onClick={() => onConfirmInbound(manifest)}
                        >
                          <ArrowDownToLine className='h-4 w-4' />
                          Inbound
                        </Button>
                      )}
                      {manifest.status === 'CREATED' && (
                        <Button
                          size='sm'
                          variant='outline'
                          disabled={!canManage || isCancelling}
                          onClick={() => onCancel(manifest)}
                        >
                          <XCircle className='h-4 w-4' />
                          Cancel
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
}
