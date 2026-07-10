/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard alerts table
 */

import Link from 'next/link';
import { ArrowRight, AlertTriangle } from 'lucide-react';

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
} from '@/shared/components/ui';

import type { TmsDashboardAlert } from '@/modules/first-mile/types';
import {
  formatDateTime,
  getSeverityBadgeVariant,
} from '../dashboardPageModels';

export function DashboardAlertsTable({
  alerts,
}: {
  alerts: TmsDashboardAlert[];
}) {
  return (
    <Card className='rounded-lg'>
      <CardHeader className='flex flex-row items-center justify-between gap-3 pb-2'>
        <CardTitle className='text-base'>Cảnh báo vận hành</CardTitle>
        <Badge variant={alerts.length > 0 ? 'destructive' : 'secondary'}>
          {alerts.length}
        </Badge>
      </CardHeader>
      <CardContent>
        {alerts.length === 0 ? (
          <div className='flex h-32 items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground'>
            Không có cảnh báo đang mở
          </div>
        ) : (
          <div className='overflow-x-auto'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Mức độ</TableHead>
                  <TableHead>Cảnh báo</TableHead>
                  <TableHead>Đối tượng</TableHead>
                  <TableHead>Chặng</TableHead>
                  <TableHead>Thời gian</TableHead>
                  <TableHead className='text-right'>Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {alerts.map((alert) => (
                  <TableRow key={alert.id}>
                    <TableCell>
                      <Badge variant={getSeverityBadgeVariant(alert.severity)}>
                        {getSeverityLabel(alert.severity)}
                      </Badge>
                    </TableCell>
                    <TableCell className='min-w-64'>
                      <div className='flex items-start gap-2'>
                        <AlertTriangle className='mt-0.5 h-4 w-4 text-muted-foreground' />
                        <div>
                          <div className='font-medium'>{alert.title}</div>
                          <div className='text-xs text-muted-foreground'>
                            {alert.description}
                          </div>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>{alert.entityCode ?? 'Chưa có'}</TableCell>
                    <TableCell>{getLegLabel(alert.leg)}</TableCell>
                    <TableCell>{formatDateTime(alert.occurredAt)}</TableCell>
                    <TableCell className='text-right'>
                      {alert.actionHref ? (
                        <Button asChild variant='ghost' size='sm'>
                          <Link href={alert.actionHref}>
                            Mở
                            <ArrowRight className='ml-2 h-4 w-4' />
                          </Link>
                        </Button>
                      ) : (
                        <span className='text-xs text-muted-foreground'>
                          Chưa có
                        </span>
                      )}
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

function getLegLabel(leg: TmsDashboardAlert['leg']) {
  if (leg === 'FIRST_MILE') return 'Lấy hàng';
  if (leg === 'MIDDLE_MILE') return 'Trung chuyển';
  if (leg === 'LAST_MILE') return 'Giao hàng';
  return 'Tất cả';
}

function getSeverityLabel(severity: TmsDashboardAlert['severity']) {
  if (severity === 'CRITICAL') return 'Khẩn cấp';
  if (severity === 'HIGH') return 'Cao';
  if (severity === 'MEDIUM') return 'Trung bình';
  return 'Thấp';
}
