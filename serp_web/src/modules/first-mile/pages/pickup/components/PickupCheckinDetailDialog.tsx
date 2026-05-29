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
import { useLazyGetPickupCheckinDetailQuery } from '../../../api';
import type { PickupCheckinDetailResponse } from '../../../types';
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

  return parsedDate.toLocaleString('en-US');
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
  const [detail, setDetail] = React.useState<PickupCheckinDetailResponse | null>(
    null
  );
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
          <DialogTitle>Pickup check-in detail</DialogTitle>
          <DialogDescription>
            Review check-in evidence and location for order {titleCode}.
          </DialogDescription>
        </DialogHeader>

        {isFetching ? (
          <div className='flex items-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading check-in detail...
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
              <Badge variant='default'>Checked in</Badge>
              {detail.orderStatus ? (
                <Badge variant='outline'>{detail.orderStatus}</Badge>
              ) : null}
            </div>

            {detail.photoUrl ? (
              <div className='space-y-2'>
                <p className='text-sm font-medium'>Check-in photo</p>
                <a
                  href={detail.photoUrl}
                  target='_blank'
                  rel='noreferrer'
                  className='block overflow-hidden rounded-md border'
                >
                  <img
                    src={detail.photoUrl}
                    alt={`Pickup check-in for ${titleCode}`}
                    className='max-h-80 w-full object-contain bg-muted/30'
                  />
                </a>
              </div>
            ) : (
              <p className='text-sm text-muted-foreground'>
                No check-in photo available.
              </p>
            )}

            <div className='grid gap-3 sm:grid-cols-2'>
              <DetailField
                label='Check-in time'
                value={formatDateTime(detail.checkinTime)}
              />
              <DetailField
                label='Distance from pickup'
                value={formatDistance(detail.distanceMeters)}
              />
              <DetailField
                label='Allowed radius'
                value={formatDistance(detail.allowedRadiusMeters)}
              />
              <DetailField label='Trip' value={detail.tripCode || '--'} />
              <DetailField
                label='Courier'
                value={
                  detail.courierName
                    ? `${detail.courierName}${detail.courierCode ? ` (${detail.courierCode})` : ''}`
                    : detail.courierCode || '--'
                }
              />
              <DetailField
                label='Post office'
                value={
                  detail.postOfficeName
                    ? `${detail.postOfficeName}${detail.postOfficeCode ? ` (${detail.postOfficeCode})` : ''}`
                    : detail.postOfficeCode || '--'
                }
              />
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='text-sm font-medium'>Sender</p>
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
                label='Pickup coordinates'
                value={`${formatCoordinate(detail.pickupLatitude)}, ${formatCoordinate(detail.pickupLongitude)}`}
              />
              <DetailField
                label='Check-in coordinates'
                value={`${formatCoordinate(detail.checkinLatitude)}, ${formatCoordinate(detail.checkinLongitude)}`}
              />
            </div>

            <div className='space-y-2'>
              <p className='text-sm font-medium'>Location map</p>
              <p className='text-xs text-muted-foreground'>
                Blue: pickup location. Green: check-in location.
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
