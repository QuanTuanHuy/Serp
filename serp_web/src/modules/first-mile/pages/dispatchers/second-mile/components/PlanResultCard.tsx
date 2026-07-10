/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution plan result card
 */

import { Badge } from '@/shared/components/ui/badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

import type {
  BagDistributionPlan,
  BagDistributionPlanItem,
} from '../../../../types';
import { formatNumber, HINT_LABELS } from '../secondMileDispatchModels';

interface PlanResultCardProps {
  plan: BagDistributionPlan | null;
}

export function PlanResultCard({ plan }: PlanResultCardProps) {
  return (
    <Card className='min-w-0 overflow-hidden'>
      <CardHeader>
        <CardTitle>Xem trước kế hoạch tự động</CardTitle>
        <CardDescription>
          Gợi ý được gom theo chặng và chấm điểm theo tuyến, tải trọng xe và
          tình trạng tài xế.
        </CardDescription>
      </CardHeader>
      <CardContent className='min-w-0'>
        {!plan ? (
          <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
            Chạy xem trước kế hoạch để kiểm tra các biên bản gợi ý trước khi
            thực thi.
          </div>
        ) : plan.items.length === 0 ? (
          <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
            Không tìm thấy túi đã niêm phong phù hợp chặng và khung thời gian
            này.
          </div>
        ) : (
          <div className='space-y-3'>
            <div className='flex flex-wrap items-center gap-2 text-sm'>
              <Badge variant={plan.executed ? 'default' : 'outline'}>
                {plan.executed ? 'Đã thực thi' : 'Bản xem trước'}
              </Badge>
              <span className='text-muted-foreground'>
                {plan.manifestCount} biên bản
              </span>
            </div>
            <div className='max-w-full overflow-x-auto rounded-md border'>
              <Table className='min-w-[1020px] table-fixed'>
                <TableHeader>
                  <TableRow>
                    <TableHead className='w-[110px]'>Tuyến</TableHead>
                    <TableHead className='w-[120px]'>Xe</TableHead>
                    <TableHead className='w-[180px]'>Túi hàng</TableHead>
                    <TableHead className='w-[120px]'>Tải</TableHead>
                    <TableHead className='w-[80px]'>Điểm</TableHead>
                    <TableHead className='w-[270px]'>Gợi ý</TableHead>
                    <TableHead className='w-[160px]'>Đã tạo</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {plan.items.map((item, index) => (
                    <PlanItemRow key={`${item.routeId}-${index}`} item={item} />
                  ))}
                </TableBody>
              </Table>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function PlanItemRow({ item }: { item: BagDistributionPlanItem }) {
  return (
    <TableRow>
      <TableCell className='truncate'>{item.routeCode ?? '-'}</TableCell>
      <TableCell className='truncate'>
        {item.vehicleLicensePlate ?? '-'}
      </TableCell>
      <TableCell>
        <div className='font-medium'>{formatNumber(item.bagIds.length, 0)}</div>
        <div className='truncate text-xs text-muted-foreground'>
          {item.bagCodes.join(', ') || '-'}
        </div>
      </TableCell>
      <TableCell>
        {formatNumber(item.totalOrders, 0)} đơn
        <div className='text-xs text-muted-foreground'>
          {formatNumber(item.totalWeight)} kg | {formatNumber(item.totalVolume)}{' '}
          m3
        </div>
      </TableCell>
      <TableCell>{formatNumber(item.score)}</TableCell>
      <TableCell className='whitespace-normal'>
        <div className='flex flex-wrap gap-1'>
          {item.hints.length === 0 ? (
            <Badge variant='outline'>Không có gợi ý</Badge>
          ) : (
            item.hints.map((hint) => (
              <Badge
                key={hint}
                variant={
                  hint === 'NO_ROUTE' ||
                  hint === 'NO_DRIVER' ||
                  hint === 'SCHEDULE_CONFLICT'
                    ? 'destructive'
                    : 'secondary'
                }
              >
                {HINT_LABELS[hint] ?? hint}
              </Badge>
            ))
          )}
        </div>
      </TableCell>
      <TableCell className='truncate'>
        {item.createdManifestCode ?? '-'}
      </TableCell>
    </TableRow>
  );
}
