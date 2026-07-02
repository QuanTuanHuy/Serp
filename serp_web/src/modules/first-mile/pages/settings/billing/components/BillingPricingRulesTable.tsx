/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Pricing rules list table for admin
 */

'use client';

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { Loader2, Pencil, Plus } from 'lucide-react';
import type { BillingDeliveryService } from '../../../../types';
import {
  DELIVERY_SERVICE_OPTIONS,
  formatMoneyVnd,
  getRouteTypeLabel,
  ROUTE_TYPE_OPTIONS,
} from '../billingPageModels';

interface BillingPricingRulesTableProps<T> {
  title: string;
  description: string;
  columns: Array<{ key: string; label: string }>;
  rows: T[];
  isLoading: boolean;
  isError: boolean;
  emptyMessage: string;
  getRowKey: (row: T) => string | number;
  renderCells: (row: T) => React.ReactNode;
  onEdit: (row: T) => void;
  onCreate?: () => void;
  createLabel?: string;
  serviceFilter?: {
    value: BillingDeliveryService | 'ALL';
    onChange: (value: BillingDeliveryService | 'ALL') => void;
  };
}

export function BillingPricingRulesTable<T>({
  title,
  description,
  columns,
  rows,
  isLoading,
  isError,
  emptyMessage,
  getRowKey,
  renderCells,
  onEdit,
  onCreate,
  createLabel = 'Add rule',
  serviceFilter,
}: BillingPricingRulesTableProps<T>) {
  return (
    <Card>
      <CardHeader className='flex flex-row flex-wrap items-start justify-between gap-4'>
        <div className='space-y-1'>
          <CardTitle>{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </div>
        <div className='flex flex-wrap items-center gap-2'>
          {onCreate ? (
            <Button type='button' size='sm' onClick={onCreate}>
              <Plus className='mr-1 h-3.5 w-3.5' />
              {createLabel}
            </Button>
          ) : null}
          {serviceFilter ? (
            <>
              <Button
                type='button'
                size='sm'
                variant={serviceFilter.value === 'ALL' ? 'default' : 'outline'}
                onClick={() => serviceFilter.onChange('ALL')}
              >
                All services
              </Button>
              {DELIVERY_SERVICE_OPTIONS.map((option) => (
                <Button
                  key={option.value}
                  type='button'
                  size='sm'
                  variant={
                    serviceFilter.value === option.value ? 'default' : 'outline'
                  }
                  onClick={() => serviceFilter.onChange(option.value)}
                >
                  {option.label}
                </Button>
              ))}
            </>
          ) : null}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className='flex items-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading rules...
          </div>
        ) : isError ? (
          <p className='text-sm text-destructive'>
            Unable to load rules. Check that tms-billing-service is running.
          </p>
        ) : rows.length === 0 ? (
          <p className='text-sm text-muted-foreground'>{emptyMessage}</p>
        ) : (
          <div className='overflow-x-auto rounded-md border'>
            <Table>
              <TableHeader>
                <TableRow>
                  {columns.map((column) => (
                    <TableHead key={column.key}>{column.label}</TableHead>
                  ))}
                  <TableHead className='w-[100px] text-right'>
                    Actions
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((row) => (
                  <TableRow key={getRowKey(row)}>
                    {renderCells(row)}
                    <TableCell className='text-right'>
                      <Button
                        type='button'
                        size='sm'
                        variant='outline'
                        onClick={() => onEdit(row)}
                      >
                        <Pencil className='mr-1 h-3.5 w-3.5' />
                        Edit
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export const getTariffRouteLabel = (routeTypeCode: string): string => {
  const match = ROUTE_TYPE_OPTIONS.find((item) => item.value === routeTypeCode);
  return match?.label ?? getRouteTypeLabel(routeTypeCode as never);
};

export const formatTariffMoney = formatMoneyVnd;
