/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Transit check-in history list
 */

import { Filter, LocateFixed } from 'lucide-react';

import { Badge } from '@/shared/components/ui/badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';

import type {
  BagDistributionManifest,
  BagDistributionManifestStatus,
} from '../../../../types';
import {
  destinationLabel,
  formatDateTime,
  formatNumber,
  getManifestBagStats,
  STATUS_LABELS,
  statusVariant,
} from '../transitModels';

export type CheckinHistoryStageFilter = 'ALL' | 'START' | 'END' | 'COMPLETED';

export interface CheckinHistoryFilters {
  stage: CheckinHistoryStageFilter;
  status: 'ALL' | BagDistributionManifestStatus;
  driverId: string;
}

interface CheckinHistoryCardProps {
  manifests: BagDistributionManifest[];
  filters: CheckinHistoryFilters;
  isAdmin: boolean;
  isFetching: boolean;
  selectedManifestId?: number;
  onFiltersChange: (filters: CheckinHistoryFilters) => void;
  onSelectManifest: (manifestId: number) => void;
}

const stageLabels: Record<CheckinHistoryStageFilter, string> = {
  ALL: 'Tất cả check-in',
  START: 'Đã check-in xuất phát',
  END: 'Đã check-in đến nơi',
  COMPLETED: 'Đủ hai điểm',
};

const getLatestCheckinTime = (manifest: BagDistributionManifest) =>
  manifest.driverEndCheckinAt ??
  manifest.driverStartCheckinAt ??
  manifest.updatedAt ??
  manifest.createdAt;

export const filterCheckinHistory = (
  manifests: BagDistributionManifest[],
  filters: CheckinHistoryFilters
) =>
  manifests.filter((manifest) => {
    const hasStart = Boolean(
      manifest.driverStartCheckinId || manifest.driverStartCheckinAt
    );
    const hasEnd = Boolean(
      manifest.driverEndCheckinId || manifest.driverEndCheckinAt
    );

    if (!hasStart && !hasEnd) return false;
    if (filters.stage === 'START' && !hasStart) return false;
    if (filters.stage === 'END' && !hasEnd) return false;
    if (filters.stage === 'COMPLETED' && (!hasStart || !hasEnd)) return false;

    return true;
  });

export function CheckinHistoryCard({
  manifests,
  filters,
  isAdmin,
  isFetching,
  selectedManifestId,
  onFiltersChange,
  onSelectManifest,
}: CheckinHistoryCardProps) {
  return (
    <Card>
      <CardHeader className='space-y-4'>
        <div>
          <CardTitle>Chuyến đã check-in</CardTitle>
          <CardDescription>
            Tài xế chỉ xem các chuyến được phân công; admin có thể lọc để rà
            soát toàn bộ lịch sử check-in.
          </CardDescription>
        </div>

        {isAdmin ? (
          <div className='grid gap-3 md:grid-cols-[minmax(180px,1fr)_minmax(180px,1fr)_minmax(160px,0.8fr)]'>
            <Select
              value={filters.stage}
              onValueChange={(stage) =>
                onFiltersChange({
                  ...filters,
                  stage: stage as CheckinHistoryStageFilter,
                })
              }
            >
              <SelectTrigger>
                <Filter className='h-4 w-4' />
                <SelectValue placeholder='Trạng thái check-in' />
              </SelectTrigger>
              <SelectContent>
                {Object.entries(stageLabels).map(([value, label]) => (
                  <SelectItem key={value} value={value}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select
              value={filters.status}
              onValueChange={(status) =>
                onFiltersChange({
                  ...filters,
                  status: status as CheckinHistoryFilters['status'],
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder='Trạng thái chuyến' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>Tất cả trạng thái</SelectItem>
                {Object.entries(STATUS_LABELS).map(([value, label]) => (
                  <SelectItem key={value} value={value}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Input
              inputMode='numeric'
              placeholder='Mã tài xế'
              value={filters.driverId}
              onChange={(event) =>
                onFiltersChange({ ...filters, driverId: event.target.value })
              }
            />
          </div>
        ) : null}
      </CardHeader>
      <CardContent>
        {manifests.length === 0 ? (
          <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
            {isFetching
              ? 'Đang tải lịch sử check-in...'
              : 'Chưa có chuyến nào đã check-in.'}
          </div>
        ) : (
          <div className='space-y-3'>
            {manifests.map((manifest) => {
              const stats = getManifestBagStats(manifest);
              const isSelected = selectedManifestId === manifest.id;
              const hasStart = Boolean(
                manifest.driverStartCheckinId || manifest.driverStartCheckinAt
              );
              const hasEnd = Boolean(
                manifest.driverEndCheckinId || manifest.driverEndCheckinAt
              );

              return (
                <button
                  key={manifest.id}
                  type='button'
                  className={`w-full rounded-md border p-4 text-left transition-colors hover:bg-muted/40 ${
                    isSelected ? 'border-primary bg-primary/5' : ''
                  }`}
                  onClick={() => onSelectManifest(manifest.id)}
                >
                  <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
                    <div className='min-w-0 space-y-1'>
                      <div className='flex flex-wrap items-center gap-2'>
                        <p className='font-medium'>
                          {manifest.manifestCode ?? `Biên bản #${manifest.id}`}
                        </p>
                        <Badge variant={statusVariant(manifest.status)}>
                          {manifest.status
                            ? STATUS_LABELS[manifest.status]
                            : 'Chưa rõ'}
                        </Badge>
                        {hasStart ? (
                          <Badge variant='secondary'>Đã rời điểm đi</Badge>
                        ) : null}
                        {hasEnd ? <Badge>Đã đến điểm nhận</Badge> : null}
                      </div>
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
                    <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                      <LocateFixed className='h-4 w-4' />
                      {formatDateTime(getLatestCheckinTime(manifest))}
                    </div>
                  </div>

                  <div className='mt-3 grid gap-2 text-sm md:grid-cols-4'>
                    <span>Xe: {manifest.vehicleLicensePlate ?? '-'}</span>
                    <span>Tài xế: {manifest.assignedDriverId ?? '-'}</span>
                    <span>{formatNumber(stats.totalBags, 0)} túi</span>
                    <span>
                      {formatNumber(stats.totalOrders, 0)} đơn trong túi
                    </span>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
