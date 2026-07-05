/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Handover manifest table card
 */

import {
  CheckCircle2,
  Eye,
  PackageCheck,
  ScanLine,
  Send,
  Truck,
  XCircle,
} from 'lucide-react';

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
  FirstMilePaginatedData,
  HandoverManifest,
  HandoverManifestStatus,
} from '../../../types';
import {
  formatDateTime,
  getScannedInOrders,
  getScannedOutOrders,
  getStatusBadgeVariant,
  getTotalOrders,
  isReadyForDispatch,
  MANIFEST_STATUS_LABELS,
  type ManifestMode,
} from '../handoverManifestModels';

interface HandoverManifestTableCardProps {
  effectiveMode: ManifestMode;
  filterHubId: string;
  filterPostOfficeId: string;
  filterStatus: 'ALL' | HandoverManifestStatus;
  hubFilterOptions: TmsComboboxOption[];
  isCancelling: boolean;
  isDispatching: boolean;
  isFetching: boolean;
  manifests: HandoverManifest[];
  manifestsData?: FirstMilePaginatedData<HandoverManifest>;
  onCancelManifest: (manifest: HandoverManifest) => void;
  onFilterHubChange: (value: string) => void;
  onFilterPostOfficeChange: (value: string) => void;
  onFilterStatusChange: (value: 'ALL' | HandoverManifestStatus) => void;
  onNextPage: () => void;
  onOpenDetail: (manifest: HandoverManifest) => void;
  onOpenDispatch: (manifest: HandoverManifest) => void;
  onOpenReceive: (manifest: HandoverManifest) => void;
  onOpenScanOut: (manifest: HandoverManifest) => void;
  onPreviousPage: () => void;
  page: number;
  postOfficeFilterOptions: TmsComboboxOption[];
  resolveHubLabel: (hubId?: number) => string;
  resolvePostOfficeLabel: (
    postOfficeId?: number,
    postOfficeCode?: string
  ) => string;
  resolveRouteLabel: (routeId?: number, routeCode?: string) => string;
  resolveVehicleLabel: (vehicleId?: number, licensePlate?: string) => string;
  showHubFilter: boolean;
  showPostOfficeFilter: boolean;
  statusFilterOptions: TmsComboboxOption[];
}

export function HandoverManifestTableCard({
  effectiveMode,
  filterHubId,
  filterPostOfficeId,
  filterStatus,
  hubFilterOptions,
  isCancelling,
  isDispatching,
  isFetching,
  manifests,
  manifestsData,
  onCancelManifest,
  onFilterHubChange,
  onFilterPostOfficeChange,
  onFilterStatusChange,
  onNextPage,
  onOpenDetail,
  onOpenDispatch,
  onOpenReceive,
  onOpenScanOut,
  onPreviousPage,
  page,
  postOfficeFilterOptions,
  resolveHubLabel,
  resolvePostOfficeLabel,
  resolveRouteLabel,
  resolveVehicleLabel,
  showHubFilter,
  showPostOfficeFilter,
  statusFilterOptions,
}: HandoverManifestTableCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          {effectiveMode === 'POST_OFFICE' ? (
            <Truck className='h-5 w-5' />
          ) : (
            <PackageCheck className='h-5 w-5' />
          )}
          {effectiveMode === 'POST_OFFICE'
            ? 'Phiếu bàn giao tại bưu cục'
            : 'Phiếu nhập tại hub'}
        </CardTitle>
        <CardDescription>
          {effectiveMode === 'POST_OFFICE'
            ? 'Tạo phiếu bàn giao, quét xuất từng đơn rồi xuất hàng đến hub được gán.'
            : 'Nhận phiếu đã xuất từ bưu cục và xác nhận các đơn nhập tại hub.'}
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-4 md:grid-cols-3'>
          {showPostOfficeFilter ? (
            <div className='space-y-2'>
              <Label htmlFor='filter-post-office'>Bưu cục gửi</Label>
              <TmsCombobox
                id='filter-post-office'
                value={filterPostOfficeId || 'ALL'}
                onValueChange={(value) =>
                  onFilterPostOfficeChange(value === 'ALL' ? '' : value)
                }
                options={postOfficeFilterOptions}
                placeholder='Tất cả bưu cục'
                emptyText='Không tìm thấy bưu cục'
              />
            </div>
          ) : null}

          {showHubFilter ? (
            <div className='space-y-2'>
              <Label htmlFor='filter-hub'>Hub nhận</Label>
              <TmsCombobox
                id='filter-hub'
                value={filterHubId || 'ALL'}
                onValueChange={(value) =>
                  onFilterHubChange(value === 'ALL' ? '' : value)
                }
                options={hubFilterOptions}
                placeholder='Tất cả hub'
                emptyText='Không tìm thấy hub'
              />
            </div>
          ) : null}

          <div className='space-y-2'>
            <Label htmlFor='filter-status'>Trạng thái</Label>
            <TmsCombobox
              id='filter-status'
              value={filterStatus}
              onValueChange={(value) =>
                onFilterStatusChange(value as 'ALL' | HandoverManifestStatus)
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
                <TableHead>Bưu cục</TableHead>
                <TableHead>Hub</TableHead>
                <TableHead>Vận chuyển</TableHead>
                <TableHead>Kế hoạch</TableHead>
                <TableHead>Đơn</TableHead>
                <TableHead>Tiến độ</TableHead>
                <TableHead>Seal</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead>Cập nhật</TableHead>
                <TableHead>Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {manifests.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={11}
                    className='py-8 text-center text-muted-foreground'
                  >
                    {isFetching
                      ? 'Đang tải phiếu bàn giao...'
                      : 'Chưa có phiếu bàn giao.'}
                  </TableCell>
                </TableRow>
              ) : (
                manifests.map((manifest) => {
                  const totalOrders = getTotalOrders(manifest);
                  const scannedOutOrders = getScannedOutOrders(manifest);
                  const scannedInOrders = getScannedInOrders(manifest);

                  return (
                    <TableRow key={manifest.id}>
                      <TableCell className='font-medium'>
                        {manifest.manifestCode || `#${manifest.id}`}
                      </TableCell>
                      <TableCell>
                        {resolvePostOfficeLabel(
                          manifest.originPostOfficeId,
                          manifest.originPostOfficeCode
                        )}
                      </TableCell>
                      <TableCell>
                        {resolveHubLabel(manifest.targetHubId)}
                      </TableCell>
                      <TableCell>
                        <div className='space-y-1 text-xs'>
                          <div>
                            Tuyến:{' '}
                            {resolveRouteLabel(
                              manifest.routeId,
                              manifest.routeCode
                            )}
                          </div>
                          <div>
                            Phương tiện:{' '}
                            {resolveVehicleLabel(
                              manifest.vehicleId,
                              manifest.vehicleLicensePlate
                            )}
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
                      <TableCell>{totalOrders}</TableCell>
                      <TableCell>
                        {effectiveMode === 'POST_OFFICE'
                          ? `${scannedOutOrders}/${totalOrders} xuất`
                          : `${scannedInOrders}/${totalOrders} nhập`}
                      </TableCell>
                      <TableCell>{manifest.sealCode || '--'}</TableCell>
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
                        {formatDateTime(manifest.updatedAt)}
                      </TableCell>
                      <TableCell>
                        <div className='flex flex-wrap gap-2'>
                          <Button
                            size='sm'
                            variant='outline'
                            onClick={() => onOpenDetail(manifest)}
                          >
                            <Eye className='mr-1 h-3.5 w-3.5' />
                            Xem
                          </Button>

                          {effectiveMode === 'POST_OFFICE' &&
                          manifest.status === 'CREATED' ? (
                            <>
                              <Button
                                size='sm'
                                variant='outline'
                                onClick={() => onOpenScanOut(manifest)}
                              >
                                <ScanLine className='mr-1 h-3.5 w-3.5' />
                                Quét xuất
                              </Button>
                              <Button
                                size='sm'
                                disabled={
                                  !isReadyForDispatch(manifest) || isDispatching
                                }
                                onClick={() => onOpenDispatch(manifest)}
                              >
                                <Send className='mr-1 h-3.5 w-3.5' />
                                Xuất đi
                              </Button>
                              <Button
                                size='sm'
                                variant='outline'
                                disabled={isCancelling}
                                onClick={() => onCancelManifest(manifest)}
                              >
                                <XCircle className='mr-1 h-3.5 w-3.5' />
                                Hủy
                              </Button>
                            </>
                          ) : null}

                          {effectiveMode === 'HUB' &&
                          manifest.status === 'OUTBOUND_CONFIRMED' ? (
                            <Button
                              size='sm'
                              onClick={() => onOpenReceive(manifest)}
                            >
                              <CheckCircle2 className='mr-1 h-3.5 w-3.5' />
                              Nhận hàng
                            </Button>
                          ) : null}
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
