/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher assignment result card
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import type { AssignmentResultCardProps } from './types';
import type { PickupShift } from '../../../../types';

const formatShiftLabel = (shift?: PickupShift): string => {
  if (shift === 'MORNING') {
    return 'Ca sáng';
  }

  if (shift === 'AFTERNOON') {
    return 'Ca chiều';
  }

  if (shift === 'EVENING') {
    return 'Ca tối';
  }

  return shift || '--';
};

export const AssignmentResultCard: React.FC<AssignmentResultCardProps> = ({
  assignmentResult,
  formatDateTime,
  formatNumber,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Kết quả phân công mới nhất</CardTitle>
        <CardDescription>
          {formatShiftLabel(assignmentResult.shift)} ngày{' '}
          {assignmentResult.tripDate || '--'}
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-3 text-sm'>
        <div className='grid gap-3 md:grid-cols-3'>
          <div>
            <p className='text-muted-foreground'>Đơn yêu cầu</p>
            <p className='font-medium'>
              {formatNumber(assignmentResult.totalRequestedOrders)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Đơn đã phân công</p>
            <p className='font-medium'>
              {formatNumber(assignmentResult.assignedOrders)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Đơn chưa phân công</p>
            <p className='font-medium'>
              {formatNumber(assignmentResult.unassignedOrders)}
            </p>
          </div>
        </div>

        {assignmentResult.trips && assignmentResult.trips.length > 0 ? (
          <div className='space-y-2'>
            {assignmentResult.trips.map((trip, index) => (
              <div
                key={`${trip.tripId ?? 'trip'}-${index}`}
                className='rounded-md border p-3'
              >
                <p className='font-medium'>
                  Chuyến: {trip.tripCode || '--'} | Nhân viên:{' '}
                  {trip.courierCode || '--'}
                  {trip.courierName ? ` - ${trip.courierName}` : ''}
                </p>
                <p className='text-xs text-muted-foreground'>
                  Kế hoạch: {formatDateTime(trip.plannedStartTime)} -{' '}
                  {formatDateTime(trip.plannedEndTime)} | Điểm dừng:{' '}
                  {formatNumber(trip.totalStops)}
                </p>
              </div>
            ))}
          </div>
        ) : (
          <p className='text-muted-foreground'>Chưa tạo chuyến nào.</p>
        )}
      </CardContent>
    </Card>
  );
};
