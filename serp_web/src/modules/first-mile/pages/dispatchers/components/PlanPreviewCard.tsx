/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - Dispatcher plan preview card
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import type { PlanPreviewCardProps } from './types';

export const PlanPreviewCard: React.FC<PlanPreviewCardProps> = ({
  optimizationResult,
  formatNumber,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Latest Plan Preview</CardTitle>
        <CardDescription>
          Post office {optimizationResult.postOfficeCode || '--'} -{' '}
          {optimizationResult.postOfficeName || '--'}
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-3 text-sm'>
        <div className='grid gap-3 md:grid-cols-3'>
          <div>
            <p className='text-muted-foreground'>Total orders</p>
            <p className='font-medium'>
              {formatNumber(optimizationResult.totalOrders)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Assigned orders</p>
            <p className='font-medium'>
              {formatNumber(optimizationResult.assignedOrders)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Unassigned orders</p>
            <p className='font-medium'>
              {formatNumber(optimizationResult.unassignedOrders)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Total routes</p>
            <p className='font-medium'>
              {formatNumber(optimizationResult.totalRoutes)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Used routes</p>
            <p className='font-medium'>
              {formatNumber(optimizationResult.usedRoutes)}
            </p>
          </div>
          <div>
            <p className='text-muted-foreground'>Objective score</p>
            <p className='font-medium'>
              {formatNumber(optimizationResult.objectiveScore)}
            </p>
          </div>
        </div>

        {optimizationResult.routes && optimizationResult.routes.length > 0 ? (
          <div className='space-y-2'>
            {optimizationResult.routes.map((routeItem, index) => (
              <div
                key={`${routeItem.courierStaffId ?? 'route'}-${index}`}
                className='rounded-md border p-3'
              >
                <p className='font-medium'>
                  Courier: {routeItem.courierCode || '--'}
                  {routeItem.courierName
                    ? ` - ${routeItem.courierName}`
                    : ''}{' '}
                  (ID: {routeItem.courierStaffId ?? '--'})
                </p>
                <p className='text-xs text-muted-foreground'>
                  Stops: {formatNumber(routeItem.totalStops)} | Distance (km):{' '}
                  {formatNumber(routeItem.totalDistanceKm)} | Lateness (min):{' '}
                  {formatNumber(routeItem.totalLatenessMinutes)}
                </p>
              </div>
            ))}
          </div>
        ) : (
          <p className='text-muted-foreground'>No routes in preview.</p>
        )}
      </CardContent>
    </Card>
  );
};
