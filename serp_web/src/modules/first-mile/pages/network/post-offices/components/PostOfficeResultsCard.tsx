/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - Post office results card
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/shared/components/ui';
import { Eye, Loader2, Pencil, Trash2 } from 'lucide-react';
import type {
  FirstMilePaginatedData,
  PostOffice,
  PostOfficeStatus,
} from '../../../../types';

interface PostOfficeResultsCardProps {
  data?: FirstMilePaginatedData<PostOffice>;
  isLoading: boolean;
  isFetching: boolean;
  isTmsAdmin: boolean;
  isSaving: boolean;
  isDeleting: boolean;
  onViewDetails: (postOffice: PostOffice) => void;
  onEdit: (postOffice: PostOffice) => void;
  onDelete: (postOffice: PostOffice) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
  getProvinceLabel: (provinceCode?: string) => string;
  getWardLabel: (provinceCode?: string, wardCode?: string) => string;
  getStatusBadgeVariant: (
    status: PostOfficeStatus
  ) => 'default' | 'secondary' | 'outline' | 'destructive';
}

const ACTION_COLUMN_CLASS =
  'sticky right-0 z-10 w-[148px] min-w-[148px] border-l bg-card';
const ACTION_HEADER_CLASS = `${ACTION_COLUMN_CLASS} z-20`;

export const PostOfficeResultsCard: React.FC<PostOfficeResultsCardProps> = ({
  data,
  isLoading,
  isFetching,
  isTmsAdmin,
  isSaving,
  isDeleting,
  onViewDetails,
  onEdit,
  onDelete,
  onPreviousPage,
  onNextPage,
  getProvinceLabel,
  getWardLabel,
  getStatusBadgeVariant,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Post office table ({data?.totalItems ?? 0})</CardTitle>
      </CardHeader>
      <CardContent className='space-y-4'>
        {isLoading ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading post offices...
          </div>
        ) : data && data.items.length > 0 ? (
          <>
            <div className='rounded-md border'>
              <Table className='min-w-[1320px]'>
                <TableHeader>
                  <TableRow>
                    <TableHead className='w-[96px]'>Image</TableHead>
                    <TableHead className='w-[150px]'>Code</TableHead>
                    <TableHead className='w-[220px]'>Name</TableHead>
                    <TableHead className='w-[130px]'>Status</TableHead>
                    <TableHead className='w-[220px]'>Province / Ward</TableHead>
                    <TableHead className='w-[280px]'>Address</TableHead>
                    <TableHead className='w-[150px]'>Pickup load</TableHead>
                    <TableHead className='w-[150px]'>Delivery load</TableHead>
                    <TableHead className='w-[180px]'>Coordinates</TableHead>
                    <TableHead className={ACTION_HEADER_CLASS}>
                      <span className='flex justify-end'>Actions</span>
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.items.map((item) => {
                    const imageUrl = item.imageUrl?.trim();
                    const hasCoordinates =
                      item.latitude !== undefined &&
                      item.latitude !== null &&
                      item.longitude !== undefined &&
                      item.longitude !== null;

                    return (
                      <TableRow key={item.id}>
                        <TableCell>
                          <div className='relative h-12 w-16 overflow-hidden rounded-md border bg-muted'>
                            {imageUrl ? (
                              <Image
                                src={imageUrl}
                                alt={`Post office ${item.code}`}
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
                        <TableCell className='font-mono text-xs'>
                          {item.code}
                        </TableCell>
                        <TableCell className='font-medium'>
                          {item.name}
                        </TableCell>
                        <TableCell>
                          <Badge variant={getStatusBadgeVariant(item.status)}>
                            {item.status}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className='space-y-1'>
                            <p>{getProvinceLabel(item.provinceCode)}</p>
                            <p className='text-xs text-muted-foreground'>
                              {getWardLabel(item.provinceCode, item.wardCode)}
                            </p>
                          </div>
                        </TableCell>
                        <TableCell className='max-w-[280px] whitespace-normal'>
                          <span className='line-clamp-2'>
                            {item.addressDetail || '--'}
                          </span>
                        </TableCell>
                        <TableCell>
                          {item.currentLoad ?? '--'} /{' '}
                          {item.dailyCapacity ?? '--'}
                        </TableCell>
                        <TableCell>
                          {item.currentDeliveryLoad ?? '--'} /{' '}
                          {item.deliveryCapacity ?? '--'}
                        </TableCell>
                        <TableCell className='font-mono text-xs'>
                          {hasCoordinates
                            ? `${item.latitude}, ${item.longitude}`
                            : '--'}
                        </TableCell>
                        <TableCell className={ACTION_COLUMN_CLASS}>
                          <div className='flex justify-end gap-2'>
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Button
                                  type='button'
                                  variant='outline'
                                  size='icon'
                                  aria-label='View post office details'
                                  onClick={() => onViewDetails(item)}
                                  disabled={isSaving || isDeleting}
                                >
                                  <Eye className='h-4 w-4' />
                                </Button>
                              </TooltipTrigger>
                              <TooltipContent>View details</TooltipContent>
                            </Tooltip>

                            {isTmsAdmin ? (
                              <>
                                <Tooltip>
                                  <TooltipTrigger asChild>
                                    <Button
                                      type='button'
                                      variant='outline'
                                      size='icon'
                                      aria-label='Edit post office'
                                      onClick={() => onEdit(item)}
                                      disabled={isSaving || isDeleting}
                                    >
                                      <Pencil className='h-4 w-4' />
                                    </Button>
                                  </TooltipTrigger>
                                  <TooltipContent>Edit</TooltipContent>
                                </Tooltip>
                                <Tooltip>
                                  <TooltipTrigger asChild>
                                    <Button
                                      type='button'
                                      variant='destructive'
                                      size='icon'
                                      aria-label='Delete post office'
                                      onClick={() => onDelete(item)}
                                      disabled={isSaving || isDeleting}
                                    >
                                      <Trash2 className='h-4 w-4' />
                                    </Button>
                                  </TooltipTrigger>
                                  <TooltipContent>Delete</TooltipContent>
                                </Tooltip>
                              </>
                            ) : null}
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
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
          </>
        ) : (
          <p className='text-muted-foreground'>No post offices found.</p>
        )}
      </CardContent>
    </Card>
  );
};
