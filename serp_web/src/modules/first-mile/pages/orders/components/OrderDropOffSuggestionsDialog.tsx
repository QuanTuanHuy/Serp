/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Drop-off post office suggestions dialog
 */

import React from 'react';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { Loader2, RefreshCw } from 'lucide-react';
import type {
  FirstMileOrderDetail,
  OrderDropOffPostOfficeSuggestion,
} from '../../../types';

interface OrderDropOffSuggestionsDialogProps {
  open: boolean;
  order: FirstMileOrderDetail | null;
  suggestions: OrderDropOffPostOfficeSuggestion[];
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onRefresh: () => void;
}

export const OrderDropOffSuggestionsDialog: React.FC<
  OrderDropOffSuggestionsDialogProps
> = ({ open, order, suggestions, isLoading, onOpenChange, onRefresh }) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[85vh] overflow-y-auto sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Suggested Post Offices</DialogTitle>
          <DialogDescription>
            {order
              ? `Order ${order.orderCode} - drop-off post offices that can still accept more orders.`
              : 'Suggested drop-off post offices.'}
          </DialogDescription>
        </DialogHeader>

        <div className='flex justify-end'>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={onRefresh}
            disabled={isLoading}
          >
            {isLoading ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <RefreshCw className='mr-2 h-4 w-4' />
            )}
            Refresh suggestions
          </Button>
        </div>

        {isLoading ? (
          <div className='flex items-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading suggested post offices...
          </div>
        ) : suggestions.length > 0 ? (
          <div className='space-y-3'>
            {suggestions.map((suggestion) => (
              <div
                key={`${suggestion.id}-${suggestion.code}`}
                className='rounded-md border p-3'
              >
                <div className='flex flex-wrap items-center justify-between gap-2'>
                  <div>
                    <p className='font-medium'>
                      {suggestion.code} - {suggestion.name}
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      {suggestion.addressDetail || '--'}
                    </p>
                  </div>
                  <div className='flex flex-wrap gap-1'>
                    <Badge variant='secondary'>
                      Pickup remaining: {suggestion.remainingCapacity ?? '--'}
                    </Badge>
                    <Badge variant='outline'>
                      Distance: {suggestion.distanceMeters?.toFixed(0) ?? '--'}{' '}
                      m
                    </Badge>
                  </div>
                </div>
                <p className='mt-2 text-xs text-muted-foreground'>
                  Pickup load: {suggestion.currentLoad ?? '--'} /
                  {suggestion.dailyCapacity ?? '--'} | Priority:{' '}
                  {suggestion.priority ?? '--'}
                </p>
              </div>
            ))}
          </div>
        ) : (
          <p className='text-sm text-muted-foreground'>
            No suitable post office suggestions for this order right now.
          </p>
        )}
      </DialogContent>
    </Dialog>
  );
};
