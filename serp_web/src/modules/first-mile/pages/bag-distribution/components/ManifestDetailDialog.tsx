/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution manifest detail dialog
 */

import * as React from 'react';
import {
  ArrowDownToLine,
  ArrowUpFromLine,
  Loader2,
  XCircle,
} from 'lucide-react';

import { Button } from '@/shared/components/ui/button';
import { Checkbox } from '@/shared/components/ui/checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

import type { BagDistributionManifest } from '../../../types';
import {
  formatDateTime,
  formatNumber,
  getManifestBagStats,
  STATUS_LABELS,
} from '../bagDistributionModels';
import { SummaryItem } from './SummaryItem';

interface ManifestDetailDialogProps {
  open: boolean;
  manifest?: BagDistributionManifest;
  isFetching: boolean;
  canOperate: boolean;
  canManage: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirmOutbound: (manifest: BagDistributionManifest) => void;
  onConfirmInbound: (
    manifest: BagDistributionManifest,
    bagIds?: number[]
  ) => void;
  onCancel: (manifest: BagDistributionManifest) => void;
}

export function ManifestDetailDialog({
  open,
  manifest,
  isFetching,
  canOperate,
  canManage,
  onOpenChange,
  onConfirmOutbound,
  onConfirmInbound,
  onCancel,
}: ManifestDetailDialogProps) {
  const [selectedInboundBagIds, setSelectedInboundBagIds] = React.useState<
    number[]
  >([]);

  React.useEffect(() => {
    setSelectedInboundBagIds([]);
  }, [manifest?.id]);

  const stats = manifest ? getManifestBagStats(manifest) : null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>Manifest details</DialogTitle>
          <DialogDescription>
            Review assigned bags and scan progress before lifecycle actions.
          </DialogDescription>
        </DialogHeader>
        {isFetching || !manifest ? (
          <div className='flex h-32 items-center justify-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading manifest...
          </div>
        ) : (
          <div className='space-y-4'>
            <div className='grid gap-3 md:grid-cols-3'>
              <SummaryItem
                label='Manifest'
                value={manifest.manifestCode ?? `#${manifest.id}`}
              />
              <SummaryItem
                label='Status'
                value={
                  manifest.status ? STATUS_LABELS[manifest.status] : 'Unknown'
                }
              />
              <SummaryItem
                label='Bags'
                value={`${formatNumber(stats?.totalBags, 0)} bags`}
              />
              <SummaryItem label='Route' value={manifest.routeCode ?? '-'} />
              <SummaryItem
                label='Vehicle'
                value={manifest.vehicleLicensePlate ?? '-'}
              />
              <SummaryItem
                label='Driver'
                value={
                  manifest.assignedDriverId
                    ? `Driver #${manifest.assignedDriverId}`
                    : '-'
                }
              />
            </div>

            <div className='overflow-x-auto rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className='w-10'>Receive</TableHead>
                    <TableHead>Bag code</TableHead>
                    <TableHead>Orders</TableHead>
                    <TableHead>Weight</TableHead>
                    <TableHead>Scan out</TableHead>
                    <TableHead>Scan in</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {(manifest.bags ?? []).map((bag) => (
                    <TableRow key={bag.id}>
                      <TableCell>
                        <Checkbox
                          checked={selectedInboundBagIds.includes(
                            bag.bagId ?? 0
                          )}
                          disabled={
                            manifest.status !== 'OUTBOUND_CONFIRMED' ||
                            Boolean(bag.scanInTime)
                          }
                          onCheckedChange={(checked) => {
                            const bagId = bag.bagId ?? 0;
                            setSelectedInboundBagIds((current) =>
                              checked
                                ? Array.from(new Set([...current, bagId]))
                                : current.filter((id) => id !== bagId)
                            );
                          }}
                          aria-label={`Select ${bag.bagCode ?? 'bag'} for inbound`}
                        />
                      </TableCell>
                      <TableCell className='font-medium'>
                        {bag.bagCode ?? `Bag #${bag.bagId}`}
                      </TableCell>
                      <TableCell>
                        {formatNumber(bag.totalOrdersSnapshot, 0)}
                      </TableCell>
                      <TableCell>
                        {formatNumber(bag.totalWeightSnapshot)} kg
                      </TableCell>
                      <TableCell>{formatDateTime(bag.scanOutTime)}</TableCell>
                      <TableCell>{formatDateTime(bag.scanInTime)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {manifest.note && (
              <div className='rounded-md border bg-muted/30 p-3 text-sm'>
                <p className='font-medium'>Note</p>
                <p className='mt-1 text-muted-foreground'>{manifest.note}</p>
              </div>
            )}
          </div>
        )}
        <DialogFooter className='gap-2 sm:gap-2'>
          {manifest?.status === 'CREATED' && (
            <>
              <Button
                disabled={!canOperate}
                onClick={() => onConfirmOutbound(manifest)}
              >
                <ArrowUpFromLine className='h-4 w-4' />
                Confirm outbound
              </Button>
              <Button
                variant='outline'
                disabled={!canManage}
                onClick={() => onCancel(manifest)}
              >
                <XCircle className='h-4 w-4' />
                Cancel
              </Button>
            </>
          )}
          {manifest?.status === 'OUTBOUND_CONFIRMED' && (
            <Button
              disabled={!canOperate}
              onClick={() =>
                onConfirmInbound(
                  manifest,
                  selectedInboundBagIds.length
                    ? selectedInboundBagIds
                    : undefined
                )
              }
            >
              <ArrowDownToLine className='h-4 w-4' />
              Confirm inbound
            </Button>
          )}
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
