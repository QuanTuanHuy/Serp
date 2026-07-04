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
            ? 'Post office manifests'
            : 'Hub inbound manifests'}
        </CardTitle>
        <CardDescription>
          {effectiveMode === 'POST_OFFICE'
            ? 'Build a handover manifest, scan every order out, then dispatch it to the mapped hub.'
            : 'Receive dispatched manifests from post offices and confirm inbound orders at the hub.'}
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-4 md:grid-cols-3'>
          {showPostOfficeFilter ? (
            <div className='space-y-2'>
              <Label htmlFor='filter-post-office'>Origin post office</Label>
              <TmsCombobox
                id='filter-post-office'
                value={filterPostOfficeId || 'ALL'}
                onValueChange={(value) =>
                  onFilterPostOfficeChange(value === 'ALL' ? '' : value)
                }
                options={postOfficeFilterOptions}
                placeholder='All post offices'
                emptyText='No post offices found'
              />
            </div>
          ) : null}

          {showHubFilter ? (
            <div className='space-y-2'>
              <Label htmlFor='filter-hub'>Target hub</Label>
              <TmsCombobox
                id='filter-hub'
                value={filterHubId || 'ALL'}
                onValueChange={(value) =>
                  onFilterHubChange(value === 'ALL' ? '' : value)
                }
                options={hubFilterOptions}
                placeholder='All hubs'
                emptyText='No hubs found'
              />
            </div>
          ) : null}

          <div className='space-y-2'>
            <Label htmlFor='filter-status'>Status</Label>
            <TmsCombobox
              id='filter-status'
              value={filterStatus}
              onValueChange={(value) =>
                onFilterStatusChange(value as 'ALL' | HandoverManifestStatus)
              }
              options={statusFilterOptions}
              placeholder='All statuses'
              emptyText='No statuses found'
            />
          </div>
        </div>

        <div className='overflow-x-auto rounded-md border'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Manifest</TableHead>
                <TableHead>Post office</TableHead>
                <TableHead>Hub</TableHead>
                <TableHead>Transport</TableHead>
                <TableHead>Planned</TableHead>
                <TableHead>Orders</TableHead>
                <TableHead>Progress</TableHead>
                <TableHead>Seal</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Updated</TableHead>
                <TableHead>Actions</TableHead>
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
                      ? 'Loading handover manifests...'
                      : 'No handover manifests found.'}
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
                            Route:{' '}
                            {resolveRouteLabel(
                              manifest.routeId,
                              manifest.routeCode
                            )}
                          </div>
                          <div>
                            Vehicle:{' '}
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
                            Depart {formatDateTime(manifest.plannedDepartureAt)}
                          </div>
                          <div>
                            Arrive {formatDateTime(manifest.plannedArrivalAt)}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>{totalOrders}</TableCell>
                      <TableCell>
                        {effectiveMode === 'POST_OFFICE'
                          ? `${scannedOutOrders}/${totalOrders} out`
                          : `${scannedInOrders}/${totalOrders} in`}
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
                            View
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
                                Scan out
                              </Button>
                              <Button
                                size='sm'
                                disabled={
                                  !isReadyForDispatch(manifest) || isDispatching
                                }
                                onClick={() => onOpenDispatch(manifest)}
                              >
                                <Send className='mr-1 h-3.5 w-3.5' />
                                Dispatch
                              </Button>
                              <Button
                                size='sm'
                                variant='outline'
                                disabled={isCancelling}
                                onClick={() => onCancelManifest(manifest)}
                              >
                                <XCircle className='mr-1 h-3.5 w-3.5' />
                                Cancel
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
                              Receive
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
            Page {(manifestsData?.currentPage ?? 0) + 1} of{' '}
            {Math.max(manifestsData?.totalPages ?? 1, 1)} -{' '}
            {manifestsData?.totalItems ?? 0} manifest(s)
          </p>
          <div className='flex gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={page <= 0 || isFetching}
              onClick={onPreviousPage}
            >
              Previous
            </Button>
            <Button
              variant='outline'
              size='sm'
              disabled={
                isFetching || (manifestsData?.totalPages ?? 1) <= page + 1
              }
              onClick={onNextPage}
            >
              Next
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
