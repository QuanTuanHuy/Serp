/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution driver tab
 */

import { ArrowDownToLine, ArrowUpFromLine } from 'lucide-react';

import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';

import type { BagDistributionManifest } from '../../../../types';
import type { CheckinMode } from '../transitModels';
import {
  destinationLabel,
  formatNumber,
  getManifestBagStats,
  STATUS_LABELS,
  statusVariant,
} from '../transitModels';
import { SummaryItem } from './SummaryItem';

interface DriverTabProps {
  manifests: BagDistributionManifest[];
  isFetching: boolean;
  canCheckin: boolean;
  onOpenCheckin: (manifest: BagDistributionManifest, mode: CheckinMode) => void;
}

export function DriverTab({
  manifests,
  isFetching,
  canCheckin,
  onOpenCheckin,
}: DriverTabProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Check-in tài xế</CardTitle>
        <CardDescription>
          Gửi vị trí và ảnh xác minh khi rời hub xuất phát và khi đến điểm
          nhận.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {!canCheckin && (
          <div className='mb-4 rounded-md border bg-muted/40 p-3 text-sm text-muted-foreground'>
            Bạn cần quyền vận hành hub hoặc quyền tài xế để gửi check-in.
          </div>
        )}
        <div className='grid gap-3 lg:grid-cols-2'>
          {manifests.length === 0 ? (
            <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground lg:col-span-2'>
              {isFetching
                ? 'Đang tải biên bản được phân công...'
                : 'Hiện không có biên bản xuất hàng đang chờ tài xế check-in.'}
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
                        {manifest.manifestCode ?? `Biên bản #${manifest.id}`}
                      </p>
                      <p className='text-sm text-muted-foreground'>
                        {manifest.originHubCode ?? manifest.originHubId} đến{' '}
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
                        : 'Chưa rõ'}
                    </Badge>
                  </div>
                  <div className='grid gap-2 text-sm md:grid-cols-2'>
                    <SummaryItem
                      label='Xe'
                      value={manifest.vehicleLicensePlate ?? '-'}
                    />
                    <SummaryItem
                      label='Túi hàng'
                      value={`${formatNumber(stats.totalBags, 0)} túi`}
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
                      Check-in xuất phát
                    </Button>
                    <Button
                      variant='secondary'
                      disabled={
                        !canCheckin || Boolean(manifest.driverEndPhotoUrl)
                      }
                      onClick={() => onOpenCheckin(manifest, 'end')}
                    >
                      <ArrowDownToLine className='h-4 w-4' />
                      Check-in đến nơi
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
