/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle results table card
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
import type { FirstMilePaginatedData, Vehicle } from '../../../../types';
import {
  buildPostOfficeLabel,
  formatOptionalNumber,
  formatStatusLabel,
  formatVehicleType,
  getStatusBadgeVariant,
} from '../firstMileVehiclePageModels';

interface VehicleResultsCardProps {
  canViewVehicles: boolean;
  canManageVehicles: boolean;
  data?: FirstMilePaginatedData<Vehicle>;
  isLoading: boolean;
  isFetching: boolean;
  isSaving: boolean;
  isDeleting: boolean;
  onViewDetails: (vehicleId: number) => void;
  onEdit: (vehicle: Vehicle) => void;
  onDelete: (vehicle: Vehicle) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
  resolveCourierLabel: (postOfficeStaffId?: number) => string;
}

export const VehicleResultsCard: React.FC<VehicleResultsCardProps> = ({
  canViewVehicles,
  canManageVehicles,
  data,
  isLoading,
  isFetching,
  isSaving,
  isDeleting,
  onViewDetails,
  onEdit,
  onDelete,
  onPreviousPage,
  onNextPage,
  resolveCourierLabel,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
      </CardHeader>
      <CardContent>
        {!canViewVehicles ? (
          <p className='text-muted-foreground'>
            You do not have permission to access vehicle data.
          </p>
        ) : isLoading ? (
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
                      <TableHead className='min-w-[120px]'>Image</TableHead>
                      <TableHead className='min-w-[160px]'>
                        License plate
                      </TableHead>
                      <TableHead className='min-w-[120px]'>Type</TableHead>
                      <TableHead className='min-w-[140px]'>Status</TableHead>
                      <TableHead className='min-w-[200px]'>
                        Post office
                      </TableHead>
                      <TableHead className='min-w-[140px]'>
                        Courier staff
                      </TableHead>
                      <TableHead className='min-w-[200px]'>Capacity</TableHead>
                      <TableHead className='sticky right-0 z-20 border-l bg-card text-right'>
                        Actions
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {data.items.map((vehicle) => {
                      const imageUrl = vehicle.imageUrl?.trim();

                      return (
                        <TableRow key={vehicle.id} className='group'>
                          <TableCell>
                            <div className='relative h-12 w-16 overflow-hidden rounded border bg-muted'>
                              {imageUrl ? (
                                <Image
                                  src={imageUrl}
                                  alt={`Vehicle ${vehicle.licensePlate}`}
                                  fill
                                  unoptimized
                                  sizes='64px'
                                  className='object-cover'
                                />
                              ) : (
                                <div className='flex h-full w-full items-center justify-center text-[10px] text-muted-foreground'>
                                  No image
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
                            {buildPostOfficeLabel(vehicle)}
                          </TableCell>
                          <TableCell className='text-xs text-muted-foreground'>
                            {resolveCourierLabel(vehicle.postOfficeStaffId)}
                          </TableCell>
                          <TableCell className='text-xs text-muted-foreground'>
                            {formatOptionalNumber(vehicle.maxWeight)} kg |{' '}
                            {formatOptionalNumber(vehicle.maxVolume)} m3
                          </TableCell>
                          <TableCell className='sticky right-0 z-10 border-l bg-background text-right group-hover:bg-muted/50'>
                            <div className='flex items-center justify-end gap-1'>
                              <Button
                                type='button'
                                variant='outline'
                                size='icon'
                                onClick={() => onViewDetails(vehicle.id)}
                                title='Details'
                                aria-label={`View details for ${vehicle.licensePlate}`}
                              >
                                <Eye className='h-4 w-4' />
                              </Button>
                              {canManageVehicles && (
                                <>
                                  <Button
                                    type='button'
                                    variant='outline'
                                    size='icon'
                                    onClick={() => onEdit(vehicle)}
                                    disabled={isSaving || isDeleting}
                                    title='Edit'
                                    aria-label={`Edit ${vehicle.licensePlate}`}
                                  >
                                    <Pencil className='h-4 w-4' />
                                  </Button>
                                  <Button
                                    type='button'
                                    variant='destructive'
                                    size='icon'
                                    onClick={() => onDelete(vehicle)}
                                    disabled={isSaving || isDeleting}
                                    title='Delete'
                                    aria-label={`Delete ${vehicle.licensePlate}`}
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

            <div className='flex items-center justify-between pt-2'>
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
};
