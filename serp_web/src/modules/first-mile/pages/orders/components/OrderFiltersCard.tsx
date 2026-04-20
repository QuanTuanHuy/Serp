/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order filters card
 */

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { RefreshCw, Search } from 'lucide-react';
import type { FirstMileOrderStatus } from '../../../types';
import type { OrderStatusFilter } from '../orderPageModels';

interface OrderFiltersCardProps {
  canViewOrders: boolean;
  keywordInput: string;
  statusInput: OrderStatusFilter;
  statusOptions: FirstMileOrderStatus[];
  isFetching: boolean;
  onKeywordInputChange: (value: string) => void;
  onStatusInputChange: (value: OrderStatusFilter) => void;
  onApplyFilters: (event: React.FormEvent) => void;
  onRefresh: () => void;
  formatStatusLabel: (status: FirstMileOrderStatus) => string;
}

export const OrderFiltersCard: React.FC<OrderFiltersCardProps> = ({
  canViewOrders,
  keywordInput,
  statusInput,
  statusOptions,
  isFetching,
  onKeywordInputChange,
  onStatusInputChange,
  onApplyFilters,
  onRefresh,
  formatStatusLabel,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Search & Filters</CardTitle>
        <CardDescription>
          Search by order code, customer order code, sender, or receiver
          name/phone.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form
          onSubmit={onApplyFilters}
          className='flex flex-col gap-3 md:flex-row md:items-center'
        >
          <div className='relative flex-1'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              className='pl-10'
              value={keywordInput}
              onChange={(event) => onKeywordInputChange(event.target.value)}
              placeholder='Search orders...'
              disabled={!canViewOrders}
            />
          </div>

          <Select
            value={statusInput}
            onValueChange={(value) =>
              onStatusInputChange(value as OrderStatusFilter)
            }
            disabled={!canViewOrders}
          >
            <SelectTrigger className='w-full md:w-[220px]'>
              <SelectValue placeholder='Filter by status' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ALL'>All statuses</SelectItem>
              {statusOptions.map((statusOption) => (
                <SelectItem key={statusOption} value={statusOption}>
                  {formatStatusLabel(statusOption)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Button type='submit' disabled={!canViewOrders}>
            Apply
          </Button>
          <Button
            type='button'
            variant='outline'
            onClick={onRefresh}
            disabled={!canViewOrders || isFetching}
          >
            <RefreshCw className='mr-2 h-4 w-4' />
            Refresh
          </Button>
        </form>
      </CardContent>
    </Card>
  );
};
