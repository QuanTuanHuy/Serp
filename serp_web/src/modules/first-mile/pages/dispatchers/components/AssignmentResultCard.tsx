/**
 * Author: GitHub Copilot
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

export const AssignmentResultCard: React.FC<AssignmentResultCardProps> = ({
  assignmentResult,
  formatDateTime,
  formatNumber,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Latest Assignment Result</CardTitle>
        <CardDescription>
          Shift {assignmentResult.shift} on {assignmentResult.tripDate || '--'}
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-3 text-sm'>
        <div className='grid gap-3 md:grid-cols-3'>
          <div>
            <p className='text-muted-foreground'>Requested orders</p>
            <p className='font-medium'>
              {formatNumber(assignmentResult.totalRequestedOrders)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Assigned orders</p>
            <p className='font-medium'>
              {formatNumber(assignmentResult.assignedOrders)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Unassigned orders</p>
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
                  Trip: {trip.tripCode || '--'} | Courier:{' '}
                  {trip.courierCode || '--'}
                  {trip.courierName ? ` - ${trip.courierName}` : ''}
                </p>
                <p className='text-xs text-muted-foreground'>
                  Planned: {formatDateTime(trip.plannedStartTime)} -{' '}
                  {formatDateTime(trip.plannedEndTime)} | Stops:{' '}
                  {formatNumber(trip.totalStops)}
                </p>
              </div>
            ))}
          </div>
        ) : (
          <p className='text-muted-foreground'>No trips created.</p>
        )}
      </CardContent>
    </Card>
  );
};
