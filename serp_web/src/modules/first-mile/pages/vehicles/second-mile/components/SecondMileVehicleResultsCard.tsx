/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle results table
 */

import Image from 'next/image';
import React from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Eye, Loader2, Pencil, Trash2 } from 'lucide-react';
import type {
  FirstMilePaginatedData,
  Hub,
  SecondMileVehicle,
} from '../../../../types';
import {
  buildHubLabel,
  formatOptionalNumber,
  formatStatusLabel,
  formatVehicleType,
  getStatusBadgeVariant,
} from '../vehiclePageModels';

interface SecondMileVehicleResultsCardProps {
  canManage: boolean;
  data?: FirstMilePaginatedData<SecondMileVehicle>;
  driverLabelByStaffId: Record<number, string>;
  hubById: Record<number, Hub>;
  imageRefreshKey?: number;
  isLoading: boolean;
  isFetching: boolean;
  isSaving: boolean;
  isDeleting: boolean;
  onView: (id: number) => void;
  onEdit: (vehicle: SecondMileVehicle) => void;
  onDelete: (vehicle: SecondMileVehicle) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
}

export const SecondMileVehicleResultsCard: React.FC<
  SecondMileVehicleResultsCardProps
> = ({
  canManage,
  data,
  driverLabelByStaffId,
  hubById,
  imageRefreshKey,
  isLoading,
  isFetching,
  isSaving,
  isDeleting,
  onView,
  onEdit,
  onDelete,
  onPreviousPage,
  onNextPage,
}) => (
  <Card>
    <CardHeader>
      <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
    </CardHeader>
    <CardContent>
      {isLoading ? (
        <div className='flex items-center gap-2 text-muted-foreground'>
          <Loader2 className='h-4 w-4 animate-spin' />
          Loading vehicles...
        </div>
      ) : data && data.items.length > 0 ? (
        <div className='space-y-4'>
          <div className='overflow-hidden rounded-md border'>
            <div className='overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Image</TableHead>
                    <TableHead>License plate</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Hub</TableHead>
                    <TableHead>Driver</TableHead>
                    <TableHead>Capacity</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.items.map((vehicle) => {
                    const imageUrl = vehicle.imageUrl?.trim();
                    const imageSrc =
                      imageUrl && imageRefreshKey
                        ? `${imageUrl}${imageUrl.includes('?') ? '&' : '?'}v=${imageRefreshKey}`
                        : imageUrl;
                    const driverLabel = vehicle.assignedStaffId
                      ? (driverLabelByStaffId[vehicle.assignedStaffId] ??
                        `Driver #${vehicle.assignedStaffId}`)
                      : '—';
                    return (
                      <TableRow key={vehicle.id}>
                        <TableCell>
                          <div className='relative h-12 w-16 overflow-hidden rounded border bg-muted'>
                            {imageSrc ? (
                              <Image
                                src={imageSrc}
                                alt={vehicle.licensePlate}
                                fill
                                unoptimized
                                sizes='64px'
                                className='object-cover'
                              />
                            ) : (
                              <div className='flex h-full items-center justify-center text-[10px] text-muted-foreground'>
                                —
                              </div>
                            )}
                          </div>
                        </TableCell>
                        <TableCell className='font-medium'>
                          {vehicle.licensePlate}
                        </TableCell>
                        <TableCell>
                          <Badge variant='outline'>
                            {formatVehicleType(vehicle.vehicleType)}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={getStatusBadgeVariant(vehicle.status)}
                          >
                            {formatStatusLabel(vehicle.status)}
                          </Badge>
                        </TableCell>
                        <TableCell className='text-xs text-muted-foreground'>
                          {buildHubLabel(vehicle.hubId, hubById)}
                        </TableCell>
                        <TableCell className='text-xs text-muted-foreground'>
                          {driverLabel}
                        </TableCell>
                        <TableCell className='text-xs text-muted-foreground'>
                          bags: {vehicle.maxBags} |{' '}
                          {formatOptionalNumber(vehicle.maxWeight)} kg |{' '}
                          {formatOptionalNumber(vehicle.maxVolume)} m³
                        </TableCell>
                        <TableCell className='text-right'>
                          <div className='flex justify-end gap-1'>
                            <Button
                              type='button'
                              variant='outline'
                              size='sm'
                              onClick={() => onView(vehicle.id)}
                            >
                              <Eye className='mr-1 h-4 w-4' />
                              View
                            </Button>
                            {canManage && (
                              <>
                                <Button
                                  type='button'
                                  variant='outline'
                                  size='sm'
                                  onClick={() => onEdit(vehicle)}
                                  disabled={isSaving || isDeleting}
                                >
                                  <Pencil className='h-4 w-4' />
                                </Button>
                                <Button
                                  type='button'
                                  variant='destructive'
                                  size='sm'
                                  onClick={() => onDelete(vehicle)}
                                  disabled={isSaving || isDeleting}
                                >
                                  <Trash2 className='h-4 w-4' />
                                </Button>
                              </>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </div>
          </div>
          <div className='flex items-center justify-between'>
            <Button
              variant='outline'
              onClick={onPreviousPage}
              disabled={!data.hasPrevious || isFetching}
            >
              Previous
            </Button>
            <span className='text-sm text-muted-foreground'>
              Page {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
            </span>
            <Button
              variant='outline'
              onClick={onNextPage}
              disabled={!data.hasNext || isFetching}
            >
              Next
            </Button>
          </div>
        </div>
      ) : (
        <p className='text-muted-foreground'>No vehicles found.</p>
      )}
    </CardContent>
  </Card>
);
