/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Driver handover trips card
 */

import { Loader2, Truck } from 'lucide-react';

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import type { HandoverManifest } from '../../../types';
import { DriverHandoverTripCard } from './DriverHandoverTripCard';

interface DriverHandoverTripsCardProps {
  activeManifestId: number | null;
  activeTrips: HandoverManifest[];
  isFetching: boolean;
  isSubmitting: boolean;
  manifestCount: number;
  onOpenCheckin: (manifest: HandoverManifest) => void;
}

export function DriverHandoverTripsCard({
  activeManifestId,
  activeTrips,
  isFetching,
  isSubmitting,
  manifestCount,
  onOpenCheckin,
}: DriverHandoverTripsCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          <Truck className='h-5 w-5' />
          Active trips
        </CardTitle>
        <CardDescription>
          Trips are available after the post office dispatches the handover
          manifest.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        {isFetching && manifestCount === 0 ? (
          <div className='flex items-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading driver handovers...
          </div>
        ) : activeTrips.length === 0 ? (
          <div className='rounded-md border border-dashed p-6 text-sm text-muted-foreground'>
            No dispatched handovers are waiting for driver action.
          </div>
        ) : (
          activeTrips.map((manifest) => (
            <DriverHandoverTripCard
              key={manifest.id}
              manifest={manifest}
              isSubmitting={isSubmitting}
              isCurrentSubmitting={
                isSubmitting && activeManifestId === manifest.id
              }
              onOpenCheckin={onOpenCheckin}
            />
          ))
        )}
      </CardContent>
    </Card>
  );
}
