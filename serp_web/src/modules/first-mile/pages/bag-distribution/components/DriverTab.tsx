/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution driver tab
 */

import { ArrowDownToLine, ArrowUpFromLine, Eye } from 'lucide-react';

import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';

import type { BagDistributionManifest } from '../../../types';
import type { CheckinMode } from '../bagDistributionModels';
import {
  destinationLabel,
  formatNumber,
  getManifestBagStats,
  STATUS_LABELS,
  statusVariant,
} from '../bagDistributionModels';
import { SummaryItem } from './SummaryItem';

interface DriverTabProps {
  manifests: BagDistributionManifest[];
  isFetching: boolean;
  canCheckin: boolean;
  onOpenCheckin: (manifest: BagDistributionManifest, mode: CheckinMode) => void;
  onViewManifest: (id: number) => void;
}

export function DriverTab({
  manifests,
  isFetching,
  canCheckin,
  onOpenCheckin,
  onViewManifest,
}: DriverTabProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Driver check-in</CardTitle>
        <CardDescription>
          Submit location and photo when leaving the origin hub and when
          arriving at the destination.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {!canCheckin && (
          <div className='mb-4 rounded-md border bg-muted/40 p-3 text-sm text-muted-foreground'>
            You need hub operation or driver access to submit check-ins.
          </div>
        )}
        <div className='grid gap-3 lg:grid-cols-2'>
          {manifests.length === 0 ? (
            <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground lg:col-span-2'>
              {isFetching
                ? 'Loading assigned manifests...'
                : 'No outbound manifests are currently waiting for driver check-in.'}
            </div>
          ) : (
            manifests.map((manifest) => {
              const stats = getManifestBagStats(manifest);
              return (
                <div
                  key={manifest.id}
                  className='space-y-4 rounded-md border p-4'
                >
                  <div className='flex items-start justify-between gap-3'>
                    <div>
                      <p className='font-medium'>
                        {manifest.manifestCode ?? `Manifest #${manifest.id}`}
                      </p>
                      <p className='text-sm text-muted-foreground'>
                        {manifest.originHubCode ?? manifest.originHubId} to{' '}
                        {destinationLabel(
                          manifest.destinationType,
                          manifest.destinationHubId,
                          manifest.destinationHubCode,
                          manifest.destinationPostOfficeCode
                        )}
                      </p>
                    </div>
                    <Badge variant={statusVariant(manifest.status)}>
                      {manifest.status
                        ? STATUS_LABELS[manifest.status]
                        : 'Unknown'}
                    </Badge>
                  </div>
                  <div className='grid gap-2 text-sm md:grid-cols-2'>
                    <SummaryItem
                      label='Vehicle'
                      value={manifest.vehicleLicensePlate ?? '-'}
                    />
                    <SummaryItem
                      label='Bags'
                      value={`${formatNumber(stats.totalBags, 0)} bags`}
                    />
                  </div>
                  <div className='flex flex-wrap gap-2'>
                    <Button
                      disabled={
                        !canCheckin || Boolean(manifest.driverStartPhotoUrl)
                      }
                      onClick={() => onOpenCheckin(manifest, 'start')}
                    >
                      <ArrowUpFromLine className='h-4 w-4' />
                      Start check-in
                    </Button>
                    <Button
                      variant='secondary'
                      disabled={
                        !canCheckin || Boolean(manifest.driverEndPhotoUrl)
                      }
                      onClick={() => onOpenCheckin(manifest, 'end')}
                    >
                      <ArrowDownToLine className='h-4 w-4' />
                      End check-in
                    </Button>
                    <Button
                      variant='outline'
                      onClick={() => onViewManifest(manifest.id)}
                    >
                      <Eye className='h-4 w-4' />
                      Detail
                    </Button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </CardContent>
    </Card>
  );
}
