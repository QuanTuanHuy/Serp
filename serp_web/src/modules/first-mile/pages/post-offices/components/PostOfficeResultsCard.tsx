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
} from '@/shared/components/ui';
import { Eye, LayoutGrid, List, Loader2, Pencil, Trash2 } from 'lucide-react';
import type {
  FirstMilePaginatedData,
  PostOffice,
  PostOfficeStatus,
} from '../../../types';
import type { PostOfficeViewMode } from '../postOfficeForm';

interface PostOfficeResultsCardProps {
  data?: FirstMilePaginatedData<PostOffice>;
  isLoading: boolean;
  isFetching: boolean;
  viewMode: PostOfficeViewMode;
  isTmsAdmin: boolean;
  isSaving: boolean;
  isDeleting: boolean;
  onViewModeChange: (mode: PostOfficeViewMode) => void;
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

export const PostOfficeResultsCard: React.FC<PostOfficeResultsCardProps> = ({
  data,
  isLoading,
  isFetching,
  viewMode,
  isTmsAdmin,
  isSaving,
  isDeleting,
  onViewModeChange,
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
      <CardHeader className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
        <div className='flex items-center gap-2'>
          <Button
            type='button'
            size='sm'
            variant={viewMode === 'list' ? 'default' : 'outline'}
            onClick={() => onViewModeChange('list')}
          >
            <List className='h-4 w-4 mr-1' />
            List
          </Button>
          <Button
            type='button'
            size='sm'
            variant={viewMode === 'grid' ? 'default' : 'outline'}
            onClick={() => onViewModeChange('grid')}
          >
            <LayoutGrid className='h-4 w-4 mr-1' />
            Grid
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading post offices...
          </div>
        ) : data && data.items.length > 0 ? (
          <div className='space-y-4'>
            {viewMode === 'list' ? (
              <div className='space-y-3'>
                {data.items.map((item) => {
                  const imageUrl = item.imageUrl?.trim();

                  return (
                    <div
                      key={item.id}
                      className='rounded-lg border p-3 flex flex-col gap-3'
                    >
                      <div className='flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between'>
                        <div className='flex gap-3 min-w-0'>
                          <div className='relative h-20 w-28 shrink-0 overflow-hidden rounded-md border bg-muted'>
                            {imageUrl ? (
                              <Image
                                src={imageUrl}
                                alt={`Post office ${item.code}`}
                                fill
                                unoptimized
                                sizes='112px'
                                className='object-cover'
                              />
                            ) : (
                              <div className='h-full w-full flex items-center justify-center text-[11px] text-muted-foreground'>
                                No image
                              </div>
                            )}
                          </div>

                          <div className='space-y-1 min-w-0'>
                            <div className='flex flex-wrap items-center gap-2'>
                              <p className='font-medium'>
                                {item.code} - {item.name}
                              </p>
                              <Badge
                                variant={getStatusBadgeVariant(item.status)}
                              >
                                {item.status}
                              </Badge>
                            </div>
                            <p className='text-sm text-muted-foreground'>
                              {item.addressDetail}
                            </p>
                            <p className='text-xs text-muted-foreground'>
                              Province/Ward:{' '}
                              {getProvinceLabel(item.provinceCode)} /{' '}
                              {getWardLabel(item.provinceCode, item.wardCode)}
                            </p>
                            {item.latitude !== undefined &&
                              item.latitude !== null &&
                              item.longitude !== undefined &&
                              item.longitude !== null && (
                                <p className='text-xs text-muted-foreground'>
                                  GPS: {item.latitude}, {item.longitude}
                                </p>
                              )}
                          </div>
                        </div>

                        <div className='flex items-center gap-2 self-end sm:self-start'>
                          <Button
                            type='button'
                            variant='outline'
                            size='sm'
                            onClick={() => onViewDetails(item)}
                            disabled={isSaving || isDeleting}
                          >
                            <Eye className='h-4 w-4 mr-1' />
                            View
                          </Button>

                          {isTmsAdmin ? (
                            <>
                              <Button
                                type='button'
                                variant='outline'
                                size='sm'
                                onClick={() => onEdit(item)}
                                disabled={isSaving || isDeleting}
                              >
                                <Pencil className='h-4 w-4 mr-1' />
                                Edit
                              </Button>
                              <Button
                                type='button'
                                variant='destructive'
                                size='sm'
                                onClick={() => onDelete(item)}
                                disabled={isSaving || isDeleting}
                              >
                                <Trash2 className='h-4 w-4 mr-1' />
                                Delete
                              </Button>
                            </>
                          ) : null}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-3'>
                {data.items.map((item) => {
                  const imageUrl = item.imageUrl?.trim();

                  return (
                    <div
                      key={item.id}
                      className='rounded-lg border overflow-hidden flex flex-col'
                    >
                      <div className='relative h-40 w-full bg-muted'>
                        {imageUrl ? (
                          <Image
                            src={imageUrl}
                            alt={`Post office ${item.code}`}
                            fill
                            unoptimized
                            sizes='(min-width: 1280px) 33vw, (min-width: 640px) 50vw, 100vw'
                            className='object-cover'
                          />
                        ) : (
                          <div className='h-full w-full flex items-center justify-center text-xs text-muted-foreground'>
                            No image
                          </div>
                        )}
                      </div>

                      <div className='p-3 space-y-1 flex-1'>
                        <div className='flex flex-wrap items-center gap-2'>
                          <p className='font-medium'>
                            {item.code} - {item.name}
                          </p>
                          <Badge variant={getStatusBadgeVariant(item.status)}>
                            {item.status}
                          </Badge>
                        </div>
                        <p className='text-sm text-muted-foreground line-clamp-2'>
                          {item.addressDetail}
                        </p>
                        <p className='text-xs text-muted-foreground'>
                          Province/Ward: {getProvinceLabel(item.provinceCode)} /{' '}
                          {getWardLabel(item.provinceCode, item.wardCode)}
                        </p>
                        {item.latitude !== undefined &&
                          item.latitude !== null &&
                          item.longitude !== undefined &&
                          item.longitude !== null && (
                            <p className='text-xs text-muted-foreground'>
                              GPS: {item.latitude}, {item.longitude}
                            </p>
                          )}
                      </div>

                      <div className='flex items-center gap-2 p-3 pt-0'>
                        <Button
                          type='button'
                          variant='outline'
                          size='sm'
                          onClick={() => onViewDetails(item)}
                          disabled={isSaving || isDeleting}
                          className={isTmsAdmin ? '' : 'flex-1'}
                        >
                          <Eye className='h-4 w-4 mr-1' />
                          View
                        </Button>

                        {isTmsAdmin ? (
                          <>
                            <Button
                              type='button'
                              variant='outline'
                              size='sm'
                              onClick={() => onEdit(item)}
                              disabled={isSaving || isDeleting}
                              className='flex-1'
                            >
                              <Pencil className='h-4 w-4 mr-1' />
                              Edit
                            </Button>
                            <Button
                              type='button'
                              variant='destructive'
                              size='sm'
                              onClick={() => onDelete(item)}
                              disabled={isSaving || isDeleting}
                              className='flex-1'
                            >
                              <Trash2 className='h-4 w-4 mr-1' />
                              Delete
                            </Button>
                          </>
                        ) : null}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

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
          <p className='text-muted-foreground'>No post offices found.</p>
        )}
      </CardContent>
    </Card>
  );
};
