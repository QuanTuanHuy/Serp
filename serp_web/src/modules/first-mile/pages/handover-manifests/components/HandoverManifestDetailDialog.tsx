/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Handover manifest detail dialog
 */

import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import type { HandoverManifest } from '../../../types';
import {
  formatDateTime,
  getScannedInOrders,
  getScannedOutOrders,
  getStatusBadgeVariant,
  getTotalOrders,
  MANIFEST_STATUS_LABELS,
} from '../handoverManifestModels';
import { DetailItem } from './DetailItem';
import { ManifestOrdersTable } from './ManifestOrdersTable';

interface HandoverManifestDetailDialogProps {
  detailManifest?: HandoverManifest | null;
  isFetchingDetail: boolean;
  onOpenChange: (open: boolean) => void;
  open: boolean;
  resolveHubLabel: (hubId?: number) => string;
  resolvePostOfficeLabel: (
    postOfficeId?: number,
    postOfficeCode?: string
  ) => string;
  resolveRouteLabel: (routeId?: number, routeCode?: string) => string;
  resolveVehicleLabel: (vehicleId?: number, licensePlate?: string) => string;
}

export function HandoverManifestDetailDialog({
  detailManifest,
  isFetchingDetail,
  onOpenChange,
  open,
  resolveHubLabel,
  resolvePostOfficeLabel,
  resolveRouteLabel,
  resolveVehicleLabel,
}: HandoverManifestDetailDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>Handover manifest detail</DialogTitle>
          <DialogDescription>
            Manifest summary, timestamps, and order scan history.
          </DialogDescription>
        </DialogHeader>

        {detailManifest ? (
          <div className='space-y-4'>
            <div className='grid gap-3 sm:grid-cols-3'>
              <DetailItem
                label='Manifest'
                value={detailManifest.manifestCode || `#${detailManifest.id}`}
              />
              <DetailItem
                label='Status'
                value={
                  detailManifest.status ? (
                    <Badge
                      variant={getStatusBadgeVariant(detailManifest.status)}
                    >
                      {MANIFEST_STATUS_LABELS[detailManifest.status]}
                    </Badge>
                  ) : (
                    '--'
                  )
                }
              />
              <DetailItem
                label='Target hub'
                value={resolveHubLabel(detailManifest.targetHubId)}
              />
              <DetailItem
                label='Route'
                value={resolveRouteLabel(
                  detailManifest.routeId,
                  detailManifest.routeCode
                )}
              />
              <DetailItem
                label='Vehicle'
                value={resolveVehicleLabel(
                  detailManifest.vehicleId,
                  detailManifest.vehicleLicensePlate
                )}
              />
              <DetailItem
                label='Origin post office'
                value={resolvePostOfficeLabel(
                  detailManifest.originPostOfficeId,
                  detailManifest.originPostOfficeCode
                )}
              />
              <DetailItem
                label='Scan out'
                value={`${getScannedOutOrders(detailManifest)}/${getTotalOrders(
                  detailManifest
                )}`}
              />
              <DetailItem
                label='Scan in'
                value={`${getScannedInOrders(detailManifest)}/${getTotalOrders(
                  detailManifest
                )}`}
              />
              <DetailItem
                label='Seal'
                value={detailManifest.sealCode || '--'}
              />
              <DetailItem
                label='Dispatched'
                value={formatDateTime(detailManifest.dispatchedAt)}
              />
              <DetailItem
                label='Planned departure'
                value={formatDateTime(detailManifest.plannedDepartureAt)}
              />
              <DetailItem
                label='Planned arrival'
                value={formatDateTime(detailManifest.plannedArrivalAt)}
              />
              <DetailItem
                label='Inbound confirmed'
                value={formatDateTime(detailManifest.inboundConfirmedAt)}
              />
              <DetailItem
                label='Departure check-in'
                value={
                  detailManifest.driverStartCheckinAt
                    ? `${formatDateTime(
                        detailManifest.driverStartCheckinAt
                      )} (${Math.round(
                        detailManifest.driverStartDistanceM ?? 0
                      )}m)`
                    : '--'
                }
              />
              <DetailItem
                label='Arrival check-in'
                value={
                  detailManifest.driverEndCheckinAt
                    ? `${formatDateTime(
                        detailManifest.driverEndCheckinAt
                      )} (${Math.round(detailManifest.driverEndDistanceM ?? 0)}m)`
                    : '--'
                }
              />
            </div>

            {detailManifest.note ? (
              <div className='space-y-1 rounded-md border p-3'>
                <p className='text-xs font-medium uppercase text-muted-foreground'>
                  Note
                </p>
                <p className='text-sm'>{detailManifest.note}</p>
              </div>
            ) : null}

            {isFetchingDetail ? (
              <p className='text-sm text-muted-foreground'>Loading detail...</p>
            ) : (
              <ManifestOrdersTable orders={detailManifest.orders ?? []} />
            )}
          </div>
        ) : null}

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
