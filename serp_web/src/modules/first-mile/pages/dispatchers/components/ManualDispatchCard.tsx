/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - Dispatcher manual dispatch card
 */

import React from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { CheckCircle2, Loader2, Search } from 'lucide-react';
import { CandidateOrderItem } from './CandidateOrderItem';
import type { ManualDispatchCardProps } from './types';

export const ManualDispatchCard: React.FC<ManualDispatchCardProps> = ({
  selectedPostOfficeId,
  orderKeywordInput,
  onOrderKeywordInputChange,
  onApplyOrderFilters,
  courierOptions,
  selectedManualCourierId,
  onManualCourierIdChange,
  isLoadingCouriers,
  suggestedCouriers,
  onQuickPickCourier,
  isLoadingOrders,
  candidateOrders,
  selectedOrderIds,
  selectedOrderIdSet,
  onToggleOrder,
  onSelectAllCurrentOrders,
  onClearSelectedOrders,
  currentPage,
  totalPages,
  hasPrevious,
  hasNext,
  isFetchingOrders,
  onPreviousPage,
  onNextPage,
  onManualAssign,
  isManualAssigning,
  activeAction,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Manual Dispatch</CardTitle>
        <CardDescription>
          Select candidate orders and assign them to a courier.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <form
          onSubmit={onApplyOrderFilters}
          className='flex flex-col gap-3 sm:flex-row'
        >
          <div className='relative flex-1'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              className='pl-10'
              value={orderKeywordInput}
              onChange={(event) =>
                onOrderKeywordInputChange(event.target.value)
              }
              placeholder='Search candidate orders...'
              disabled={!selectedPostOfficeId}
            />
          </div>
          <Button type='submit' disabled={!selectedPostOfficeId}>
            Apply
          </Button>
        </form>

        <div className='grid gap-3 md:grid-cols-2'>
          <div className='space-y-2'>
            <Label htmlFor='manual-courier-select'>Courier</Label>
            <Select
              value={selectedManualCourierId || undefined}
              onValueChange={onManualCourierIdChange}
              disabled={!selectedPostOfficeId || isLoadingCouriers}
            >
              <SelectTrigger id='manual-courier-select'>
                <SelectValue placeholder='Select courier for manual assignment' />
              </SelectTrigger>
              <SelectContent>
                {courierOptions.map((courier) => (
                  <SelectItem key={courier.id} value={String(courier.id)}>
                    {courier.code} - {courier.fullName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {isLoadingCouriers ? (
              <p className='text-xs text-muted-foreground'>
                Loading couriers...
              </p>
            ) : null}
          </div>
          <div className='space-y-2'>
            <Label>Quick pick courier</Label>
            {suggestedCouriers.length > 0 ? (
              <div className='flex flex-wrap gap-2'>
                {suggestedCouriers.map((courier) => (
                  <Button
                    key={courier.id}
                    type='button'
                    variant='outline'
                    size='sm'
                    onClick={() => onQuickPickCourier(courier.id)}
                  >
                    {courier.label}
                  </Button>
                ))}
              </div>
            ) : (
              <p className='text-xs text-muted-foreground'>
                No suggested couriers yet. Run preview or auto assign to get
                suggestions.
              </p>
            )}
          </div>
        </div>

        {isLoadingOrders ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading candidate orders...
          </div>
        ) : candidateOrders.length > 0 ? (
          <div className='space-y-3'>
            <div className='flex flex-wrap items-center gap-2'>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={onSelectAllCurrentOrders}
              >
                Select all current page
              </Button>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={onClearSelectedOrders}
              >
                Clear selection
              </Button>
              <Badge variant='secondary'>
                Selected: {selectedOrderIds.length}
              </Badge>
            </div>

            <div className='space-y-2'>
              {candidateOrders.map((order) => (
                <CandidateOrderItem
                  key={order.id}
                  order={order}
                  checked={selectedOrderIdSet.has(order.id)}
                  onToggle={onToggleOrder}
                />
              ))}
            </div>

            <div className='flex items-center justify-between'>
              <Button
                type='button'
                variant='outline'
                onClick={onPreviousPage}
                disabled={!hasPrevious || isFetchingOrders}
              >
                Previous
              </Button>
              <span className='text-sm text-muted-foreground'>
                Page {currentPage + 1} / {Math.max(totalPages, 1)}
              </span>
              <Button
                type='button'
                variant='outline'
                onClick={onNextPage}
                disabled={!hasNext || isFetchingOrders}
              >
                Next
              </Button>
            </div>
          </div>
        ) : (
          <p className='text-muted-foreground'>
            No candidate orders found with CREATED/PICKUP_FAILED status on this
            page.
          </p>
        )}

        <div className='pt-1'>
          <Button
            type='button'
            onClick={onManualAssign}
            disabled={isManualAssigning || selectedOrderIds.length === 0}
          >
            {activeAction === 'manual' ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <CheckCircle2 className='mr-2 h-4 w-4' />
            )}
            Manual assign selected orders
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
