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
        <CardTitle>Kết quả tính phí</CardTitle>
        <CardDescription>
          Chi tiết phí theo loại tuyến và các quy tắc tính cước đã cấu hình.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-6'>
        <div className='grid gap-4 md:grid-cols-3'>
          <div className='rounded-lg border p-4'>
            <p className='text-xs text-muted-foreground'>Tổng phí</p>
            <p className='mt-2 text-2xl font-semibold'>
              {formatMoneyVnd(result.totalFee)}
            </p>
          </div>
          <div className='rounded-lg border p-4'>
            <p className='text-xs text-muted-foreground'>Phí cơ bản</p>
            <p className='mt-2 text-lg font-semibold'>
              {formatMoneyVnd(result.baseFee)}
            </p>
          </div>
          <div className='rounded-lg border p-4'>
            <p className='text-xs text-muted-foreground'>Phụ phí</p>
            <p className='mt-2 text-lg font-semibold'>
              {formatMoneyVnd(result.surchargeFee)}
            </p>
          </div>
        </div>

        <div className='flex flex-wrap gap-2 text-sm'>
          <Badge variant='outline'>Dịch vụ: {result.serviceCode}</Badge>
          <Badge variant='secondary'>
            Tuyến: {getRouteTypeLabel(result.routeType)}
          </Badge>
          <Badge variant='outline'>
            Trọng lượng tính phí: {result.chargeableWeightGram}g
          </Badge>
        </div>

        <div>
          <h3 className='text-sm font-medium mb-3'>Chi tiết dòng phí</h3>
          {result.feeItems.length === 0 ? (
            <Alert>
              <AlertTitle>Không có dòng phí chi tiết</AlertTitle>
              <AlertDescription>
                Dịch vụ tính cước đã trả về tổng phí nhưng không có chi tiết
                từng dòng phí.
              </AlertDescription>
            </Alert>
          ) : (
            <div className='rounded-md border overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã</TableHead>
                    <TableHead>Tên phí</TableHead>
                    <TableHead>Nhóm</TableHead>
                    <TableHead className='text-right'>Số tiền</TableHead>
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
