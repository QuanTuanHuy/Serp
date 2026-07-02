/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Shipping fee calculation result display
 */

import React from 'react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
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
import type { CalculateShippingFeeResponse } from '../../../../types';
import {
  formatMoneyVnd,
  getRouteTypeLabel,
  normalizeCategoryLabel,
} from '../billingPageModels';

interface BillingCalculationResultCardProps {
  result: CalculateShippingFeeResponse;
}

export const BillingCalculationResultCard: React.FC<
  BillingCalculationResultCardProps
> = ({ result }) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Calculation Result</CardTitle>
        <CardDescription>
          Fee breakdown by route type and configured billing rules.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-6'>
        <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
          <div className='rounded-lg border p-4'>
            <p className='text-xs text-muted-foreground'>Total Fee</p>
            <p className='mt-2 text-2xl font-semibold'>
              {formatMoneyVnd(result.totalFee)}
            </p>
          </div>
          <div className='rounded-lg border p-4'>
            <p className='text-xs text-muted-foreground'>Base Fee</p>
            <p className='mt-2 text-lg font-semibold'>
              {formatMoneyVnd(result.baseFee)}
            </p>
          </div>
          <div className='rounded-lg border p-4'>
            <p className='text-xs text-muted-foreground'>Surcharge Fee</p>
            <p className='mt-2 text-lg font-semibold'>
              {formatMoneyVnd(result.surchargeFee)}
            </p>
          </div>
          <div className='rounded-lg border p-4'>
            <p className='text-xs text-muted-foreground'>VAS Fee</p>
            <p className='mt-2 text-lg font-semibold'>
              {formatMoneyVnd(result.vasFee)}
            </p>
          </div>
        </div>

        <div className='flex flex-wrap gap-2 text-sm'>
          <Badge variant='outline'>Service: {result.serviceCode}</Badge>
          <Badge variant='secondary'>
            Route: {getRouteTypeLabel(result.routeType)}
          </Badge>
          <Badge variant='outline'>
            Chargeable weight: {result.chargeableWeightGram}g
          </Badge>
        </div>

        <div>
          <h3 className='text-sm font-medium mb-3'>Fee Line Items</h3>
          {result.feeItems.length === 0 ? (
            <Alert>
              <AlertTitle>No line items returned</AlertTitle>
              <AlertDescription>
                The billing service returned a total fee without detailed line
                items.
              </AlertDescription>
            </Alert>
          ) : (
            <div className='rounded-md border overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Code</TableHead>
                    <TableHead>Fee Name</TableHead>
                    <TableHead>Category</TableHead>
                    <TableHead className='text-right'>Amount</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {result.feeItems.map((item) => (
                    <TableRow key={`${item.code}-${item.category}`}>
                      <TableCell className='font-medium'>{item.code}</TableCell>
                      <TableCell>{item.name}</TableCell>
                      <TableCell>
                        <Badge variant='outline'>
                          {normalizeCategoryLabel(item.category)}
                        </Badge>
                      </TableCell>
                      <TableCell className='text-right'>
                        {formatMoneyVnd(item.amount)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};
