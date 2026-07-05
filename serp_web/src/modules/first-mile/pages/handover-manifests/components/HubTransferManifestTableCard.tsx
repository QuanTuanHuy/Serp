/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub transfer manifest table card
 */

import { ArrowRight, PackageCheck } from 'lucide-react';

import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Label,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { TmsCombobox, type TmsComboboxOption } from '../../../components';
import type {
  BagDistributionManifest,
  BagDistributionManifestStatus,
  FirstMilePaginatedData,
  SecondMileBagDestinationType,
} from '../../../types';
import {
  formatDateTime,
  getStatusBadgeVariant,
  MANIFEST_STATUS_LABELS,
} from '../handoverManifestModels';

interface HubTransferManifestTableCardProps {
  filterStatus: 'ALL' | BagDistributionManifestStatus;
  isFetching: boolean;
  manifests: BagDistributionManifest[];
  manifestsData?: FirstMilePaginatedData<BagDistributionManifest>;
  onFilterStatusChange: (
    value: 'ALL' | BagDistributionManifestStatus
  ) => void;
  onNextPage: () => void;
  onPreviousPage: () => void;
  page: number;
  statusFilterOptions: TmsComboboxOption[];
}

const formatDestinationLabel = (
  destinationType?: SecondMileBagDestinationType,
  destinationHubId?: number,
  destinationHubCode?: string,
  destinationPostOfficeCode?: string
): string => {
  if (destinationType === 'HUB') {
    return destinationHubCode || (destinationHubId ? `Hub #${destinationHubId}` : 'Hub');
  }

  if (destinationType === 'POST_OFFICE') {
    return destinationPostOfficeCode
      ? `Bưu cục ${destinationPostOfficeCode}`
      : 'Bưu cục';
  }

  return '--';
};

const getBagStats = (manifest: BagDistributionManifest) => {
  const bags = manifest.bags ?? [];

  return {
    scannedIn: bags.filter((bag) => Boolean(bag.scanInTime)).length,
    scannedOut: bags.filter((bag) => Boolean(bag.scanOutTime)).length,
    totalBags: bags.length,
    totalOrders: bags.reduce(
      (sum, bag) => sum + (bag.totalOrdersSnapshot ?? 0),
      0
    ),
  };
};

export function HubTransferManifestTableCard({
  filterStatus,
  isFetching,
  manifests,
  manifestsData,
  onFilterStatusChange,
  onNextPage,
  onPreviousPage,
  page,
  statusFilterOptions,
}: HubTransferManifestTableCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          <PackageCheck className='h-5 w-5' />
          Biên bản bàn giao chặng hub
        </CardTitle>
        <CardDescription>
          Theo dõi các phiếu phân phối túi từ hub xuất phát đến hub hoặc bưu cục
          đích.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-4 md:grid-cols-3'>
          <div className='space-y-2'>
            <Label htmlFor='hub-transfer-status'>Trạng thái</Label>
            <TmsCombobox
              id='hub-transfer-status'
              value={filterStatus}
              onValueChange={(value) =>
                onFilterStatusChange(
                  value as 'ALL' | BagDistributionManifestStatus
                )
              }
              options={statusFilterOptions}
              placeholder='Tất cả trạng thái'
              emptyText='Không tìm thấy trạng thái'
            />
          </div>
        </div>

        <div className='overflow-x-auto rounded-md border'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Phiếu</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead>Luồng bàn giao</TableHead>
                <TableHead>Vận chuyển</TableHead>
                <TableHead>Kế hoạch</TableHead>
                <TableHead>Túi / đơn</TableHead>
                <TableHead>Tiến độ</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {manifests.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    className='py-8 text-center text-muted-foreground'
                  >
                    {isFetching
                      ? 'Đang tải biên bản chặng hub...'
                      : 'Chưa có biên bản chặng hub phù hợp.'}
                  </TableCell>
                </TableRow>
              ) : (
                manifests.map((manifest) => {
                  const stats = getBagStats(manifest);
                  const originHubLabel =
                    manifest.originHubCode ||
                    (manifest.originHubId ? `Hub #${manifest.originHubId}` : '--');
                  const destinationLabel = formatDestinationLabel(
                    manifest.destinationType,
                    manifest.destinationHubId,
                    manifest.destinationHubCode,
                    manifest.destinationPostOfficeCode
                  );

                  return (
                    <TableRow key={manifest.id}>
                      <TableCell className='font-medium'>
                        {manifest.manifestCode || `#${manifest.id}`}
                      </TableCell>
                      <TableCell>
                        {manifest.status ? (
                          <Badge
                            variant={getStatusBadgeVariant(manifest.status)}
                          >
                            {MANIFEST_STATUS_LABELS[manifest.status]}
                          </Badge>
                        ) : (
                          '--'
                        )}
                      </TableCell>
                      <TableCell>
                        <div className='flex items-center gap-2 text-sm'>
                          <span>{originHubLabel}</span>
                          <ArrowRight className='h-4 w-4 text-muted-foreground' />
                          <span>{destinationLabel}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className='space-y-1 text-xs'>
                          <div>Tuyến: {manifest.routeCode || '--'}</div>
                          <div>
                            Phương tiện:{' '}
                            {manifest.vehicleLicensePlate || '--'}
                          </div>
                          <div>
                            Tài xế:{' '}
                            {manifest.assignedDriverId
                              ? `#${manifest.assignedDriverId}`
                              : '--'}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className='space-y-1 text-xs'>
                          <div>
                            Đi {formatDateTime(manifest.plannedDepartureAt)}
                          </div>
                          <div>
                            Đến {formatDateTime(manifest.plannedArrivalAt)}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div>{stats.totalBags} túi</div>
                        <div className='text-xs text-muted-foreground'>
                          {stats.totalOrders} đơn
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className='space-y-1 text-xs'>
                          <div>
                            Xuất: {stats.scannedOut}/{stats.totalBags}
                          </div>
                          <div>
                            Nhập: {stats.scannedIn}/{stats.totalBags}
                          </div>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>

        <div className='flex items-center justify-between'>
          <p className='text-sm text-muted-foreground'>
            Trang {(manifestsData?.currentPage ?? 0) + 1} /{' '}
            {Math.max(manifestsData?.totalPages ?? 1, 1)} -{' '}
            {manifestsData?.totalItems ?? 0} phiếu
          </p>
          <div className='flex gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={page <= 0 || isFetching}
              onClick={onPreviousPage}
            >
              Trước
            </Button>
            <Button
              variant='outline'
              size='sm'
              disabled={
                isFetching || (manifestsData?.totalPages ?? 1) <= page + 1
              }
              onClick={onNextPage}
            >
              Sau
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
