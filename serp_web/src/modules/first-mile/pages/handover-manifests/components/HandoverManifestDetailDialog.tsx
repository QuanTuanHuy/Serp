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
          <DialogTitle>Chi tiết phiếu bàn giao</DialogTitle>
          <DialogDescription>
            Tổng quan phiếu, mốc thời gian và lịch sử quét đơn hàng.
          </DialogDescription>
        </DialogHeader>

        {detailManifest ? (
          <div className='space-y-4'>
            <div className='grid gap-3 sm:grid-cols-3'>
              <DetailItem
                label='Phiếu'
                value={detailManifest.manifestCode || `#${detailManifest.id}`}
              />
              <DetailItem
                label='Trạng thái'
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
                label='Hub nhận'
                value={resolveHubLabel(detailManifest.targetHubId)}
              />
              <DetailItem
                label='Tuyến'
                value={resolveRouteLabel(
                  detailManifest.routeId,
                  detailManifest.routeCode
                )}
              />
              <DetailItem
                label='Phương tiện'
                value={resolveVehicleLabel(
                  detailManifest.vehicleId,
                  detailManifest.vehicleLicensePlate
                )}
              />
              <DetailItem
                label='Bưu cục gửi'
                value={resolvePostOfficeLabel(
                  detailManifest.originPostOfficeId,
                  detailManifest.originPostOfficeCode
                )}
              />
              <DetailItem
                label='Quét xuất'
                value={`${getScannedOutOrders(detailManifest)}/${getTotalOrders(
                  detailManifest
                )}`}
              />
              <DetailItem
                label='Quét nhập'
                value={`${getScannedInOrders(detailManifest)}/${getTotalOrders(
                  detailManifest
                )}`}
              />
              <DetailItem
                label='Seal'
                value={detailManifest.sealCode || '--'}
              />
              <DetailItem
                label='Đã xuất đi'
                value={formatDateTime(detailManifest.dispatchedAt)}
              />
              <DetailItem
                label='Dự kiến đi'
                value={formatDateTime(detailManifest.plannedDepartureAt)}
              />
              <DetailItem
                label='Dự kiến đến'
                value={formatDateTime(detailManifest.plannedArrivalAt)}
              />
              <DetailItem
                label='Đã xác nhận nhập'
                value={formatDateTime(detailManifest.inboundConfirmedAt)}
              />
              <DetailItem
                label='Check-in xuất phát'
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
                label='Check-in điểm đến'
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
                  Ghi chú
                </p>
                <p className='text-sm'>{detailManifest.note}</p>
              </div>
            ) : null}

            {isFetchingDetail ? (
              <p className='text-sm text-muted-foreground'>
                Đang tải chi tiết...
              </p>
            ) : (
              <ManifestOrdersTable orders={detailManifest.orders ?? []} />
            )}
          </div>
        ) : null}

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Đóng
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
