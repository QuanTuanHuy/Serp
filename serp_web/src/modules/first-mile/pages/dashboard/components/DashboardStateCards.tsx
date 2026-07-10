/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard states
 */

import { AlertCircle, RefreshCw } from 'lucide-react';

import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Card,
  CardContent,
  Skeleton,
} from '@/shared/components/ui';

export function DashboardLoadingState() {
  return (
    <div className='space-y-4'>
      <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className='h-36 rounded-lg' />
        ))}
      </div>
      <div className='grid gap-4 xl:grid-cols-[1.15fr_0.85fr]'>
        <Skeleton className='h-80 rounded-lg' />
        <Skeleton className='h-80 rounded-lg' />
      </div>
      <Skeleton className='h-72 rounded-lg' />
    </div>
  );
}

export function DashboardErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <Alert variant='destructive' className='rounded-lg'>
      <AlertCircle className='h-4 w-4' />
      <AlertTitle>Không thể tải dữ liệu tổng quan</AlertTitle>
      <AlertDescription className='mt-2 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        <span>
          Bạn không có quyền xem dữ liệu này hoặc dịch vụ đang gặp sự cố.
        </span>
        <Button variant='secondary' size='sm' onClick={onRetry}>
          <RefreshCw className='mr-2 h-4 w-4' />
          Thử lại
        </Button>
      </AlertDescription>
    </Alert>
  );
}

export function DashboardEmptyState() {
  return (
    <Card className='rounded-lg'>
      <CardContent className='flex min-h-48 flex-col items-center justify-center gap-2 text-center'>
        <div className='text-sm font-medium'>Chưa có dữ liệu tổng quan</div>
        <div className='max-w-md text-sm text-muted-foreground'>
          Không có đơn hàng phù hợp với khoảng thời gian và phạm vi đã chọn.
        </div>
      </CardContent>
    </Card>
  );
}
