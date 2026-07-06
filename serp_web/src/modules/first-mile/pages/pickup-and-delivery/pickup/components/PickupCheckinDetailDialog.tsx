/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Pickup check-in detail dialog
 */

'use client';

import React from 'react';
import { getErrorMessage } from '@/lib/store';
import {
  Badge,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components';
import { Loader2 } from 'lucide-react';
import { useLazyGetPickupCheckinDetailQuery } from '../../../../api';
import type { PickupCheckinDetailResponse } from '../../../../types';
import { PickupCheckinLocationMap } from './PickupCheckinLocationMap';

interface PickupCheckinDetailDialogProps {
  open: boolean;
  orderId?: number;
  orderCode?: string;
  onOpenChange: (open: boolean) => void;
}

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('vi-VN');
};

const formatCoordinate = (value?: number): string => {
  if (value === undefined || value === null || !Number.isFinite(value)) {
    return '--';
  }

  return value.toFixed(6);
};

const formatDistance = (value?: number): string => {
  if (value === undefined || value === null || !Number.isFinite(value)) {
    return '--';
  }

  return `${value.toFixed(2)} m`;
};

const ORDER_STATUS_LABELS: Record<string, string> = {
  ASSIGNED_TO_PICKUP: 'Đã phân công lấy',
  PICKING_UP: 'Đang lấy hàng',
  PICKED_UP: 'Đã lấy hàng',
  PENDING_ORIGIN_POST_OFFICE_INBOUND: 'Chờ nhập bưu cục gốc',
};

const formatOrderStatus = (status?: string): string =>
  status ? (ORDER_STATUS_LABELS[status] ?? status) : '--';

const DetailField: React.FC<{
  label: string;
  value: React.ReactNode;
}> = ({ label, value }) => (
  <div>
    <p className='text-xs text-muted-foreground'>{label}</p>
    <p className='text-sm font-medium'>{value}</p>
  </div>
);

export const PickupCheckinDetailDialog: React.FC<
  PickupCheckinDetailDialogProps
> = ({ open, orderId, orderCode, onOpenChange }) => {
  const [detail, setDetail] =
    React.useState<PickupCheckinDetailResponse | null>(null);
  const [loadError, setLoadError] = React.useState<string | null>(null);

  const [loadPickupCheckinDetail, { isFetching }] =
    useLazyGetPickupCheckinDetailQuery();

  React.useEffect(() => {
    if (!open || orderId === undefined) {
      setDetail(null);
      setLoadError(null);
      return;
    }

    let isCancelled = false;

    const fetchDetail = async () => {
      setDetail(null);
      setLoadError(null);

      try {
        const result = await loadPickupCheckinDetail(orderId).unwrap();
        if (!isCancelled) {
          setDetail(result);
        }
      } catch (error) {
        if (!isCancelled) {
          setLoadError(getErrorMessage(error));
        }
      }
    };

    void fetchDetail();

    return () => {
      isCancelled = true;
    };
  }, [loadPickupCheckinDetail, open, orderId]);

  const titleCode =
    detail?.orderCode || orderCode || (orderId ? `#${orderId}` : '');

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Chi tiết check-in lấy hàng</DialogTitle>
          <DialogDescription>
            Xem bằng chứng check-in và vị trí của đơn {titleCode}.
          </DialogDescription>
        </DialogHeader>

        {isFetching ? (
          <div className='flex items-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải chi tiết check-in...
          </div>
        ) : null}

        {!isFetching && loadError ? (
          <div className='rounded-md border border-destructive/40 bg-destructive/5 p-3 text-sm text-destructive'>
            {loadError}
          </div>
        ) : null}

        {!isFetching && !loadError && detail ? (
          <div className='space-y-4'>
            <div className='flex flex-wrap items-center gap-2'>
              <Badge variant='default'>Đã check-in</Badge>
              {detail.orderStatus ? (
                <Badge variant='outline'>
                  {formatOrderStatus(detail.orderStatus)}
                </Badge>
              ) : null}
            </div>

            {detail.photoUrl ? (
              <div className='space-y-2'>
                <p className='text-sm font-medium'>Ảnh check-in</p>
                <a
                  href={detail.photoUrl}
                  target='_blank'
                  rel='noreferrer'
                  className='block overflow-hidden rounded-md border'
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={detail.photoUrl}
                    alt={`Check-in lấy hàng cho ${titleCode}`}
                    className='max-h-80 w-full object-contain bg-muted/30'
                  />
                </a>
              </div>
            ) : (
              <p className='text-sm text-muted-foreground'>
                Chưa có ảnh check-in.
              </p>
            )}

            <div className='grid gap-3 sm:grid-cols-2'>
              <DetailField
                label='Thời gian check-in'
                value={formatDateTime(detail.checkinTime)}
              />
              <DetailField
                label='Khoảng cách tới điểm lấy'
                value={formatDistance(detail.distanceMeters)}
              />
              <DetailField
                label='Bán kính cho phép'
                value={formatDistance(detail.allowedRadiusMeters)}
              />
              <DetailField label='Chuyến' value={detail.tripCode || '--'} />
              <DetailField
                label='Nhân viên giao nhận'
                value={
                  detail.courierName
                    ? `${detail.courierName}${detail.courierCode ? ` (${detail.courierCode})` : ''}`
                    : detail.courierCode || '--'
                }
              />
              <DetailField
                label='Bưu cục'
                value={
                  detail.postOfficeName
                    ? `${detail.postOfficeName}${detail.postOfficeCode ? ` (${detail.postOfficeCode})` : ''}`
                    : detail.postOfficeCode || '--'
                }
              />
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='text-sm font-medium'>Người gửi</p>
              <p className='text-sm'>{detail.senderName || '--'}</p>
              <p className='text-sm text-muted-foreground'>
                {detail.senderPhone || '--'}
              </p>
              <p className='text-sm text-muted-foreground'>
                {detail.senderAddressDetail || '--'}
              </p>
            </div>

            <div className='grid gap-3 sm:grid-cols-2'>
              <DetailField
                label='Tọa độ lấy hàng'
                value={`${formatCoordinate(detail.pickupLatitude)}, ${formatCoordinate(detail.pickupLongitude)}`}
              />
              <DetailField
                label='Tọa độ check-in'
                value={`${formatCoordinate(detail.checkinLatitude)}, ${formatCoordinate(detail.checkinLongitude)}`}
              />
            </div>

            <div className='space-y-2'>
              <p className='text-sm font-medium'>Bản đồ vị trí</p>
              <p className='text-xs text-muted-foreground'>
                Xanh dương: vị trí lấy hàng. Xanh lá: vị trí check-in.
              </p>
              <PickupCheckinLocationMap
                pickupLatitude={detail.pickupLatitude}
                pickupLongitude={detail.pickupLongitude}
                checkinLatitude={detail.checkinLatitude}
                checkinLongitude={detail.checkinLongitude}
              />
            </div>
          </div>
        ) : null}
      </DialogContent>
    </Dialog>
  );
};
