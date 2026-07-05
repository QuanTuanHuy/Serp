/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Handover manifest orders table
 */

import {
  Badge,
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import type { HandoverManifestOrderItem } from '../../../types';
import {
  formatDateTime,
  formatHandoverOrderStatusLabel,
} from '../handoverManifestModels';

interface ManifestOrdersTableProps {
  actionMode?: 'SCAN_OUT' | 'RECEIVE';
  isActionLoading?: boolean;
  onScan?: (orderCode: string) => void;
  orders?: HandoverManifestOrderItem[];
  scannedOrderCodes?: string[];
}

export function ManifestOrdersTable({
  actionMode,
  isActionLoading,
  onScan,
  orders = [],
  scannedOrderCodes = [],
}: ManifestOrdersTableProps) {
  return (
    <div className='overflow-x-auto rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Đơn hàng</TableHead>
            <TableHead>Đơn khách hàng</TableHead>
            <TableHead>Trạng thái</TableHead>
            <TableHead>Quét xuất</TableHead>
            <TableHead>Quét nhập</TableHead>
            {actionMode ? <TableHead>Thao tác</TableHead> : null}
          </TableRow>
        </TableHeader>
        <TableBody>
          {orders.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={actionMode ? 6 : 5}
                className='py-8 text-center text-muted-foreground'
              >
                Chưa có đơn trong phiếu này.
              </TableCell>
            </TableRow>
          ) : (
            orders.map((order) => {
              const orderCode = order.orderCode ?? '';
              const isScanned =
                actionMode === 'SCAN_OUT'
                  ? Boolean(order.scanOutTime)
                  : Boolean(
                      order.scanInTime ||
                        (orderCode && scannedOrderCodes.includes(orderCode))
                    );
              const scannedLabel =
                actionMode === 'SCAN_OUT' ? 'Đã quét xuất' : 'Đã quét nhập';

              return (
                <TableRow key={order.id ?? order.orderCode}>
                  <TableCell className='font-medium'>
                    {order.orderCode || '--'}
                  </TableCell>
                  <TableCell>{order.customerOrderCode || '--'}</TableCell>
                  <TableCell>
                    {order.status ? (
                      <Badge variant='outline'>
                        {formatHandoverOrderStatusLabel(order.status)}
                      </Badge>
                    ) : (
                      '--'
                    )}
                  </TableCell>
                  <TableCell>{formatDateTime(order.scanOutTime)}</TableCell>
                  <TableCell>{formatDateTime(order.scanInTime)}</TableCell>
                  {actionMode ? (
                    <TableCell>
                      {isScanned ? (
                        <Badge variant='secondary'>{scannedLabel}</Badge>
                      ) : (
                        <Button
                          size='sm'
                          variant='outline'
                          disabled={!orderCode || isActionLoading}
                          onClick={() => onScan?.(orderCode)}
                        >
                          Quét
                        </Button>
                      )}
                    </TableCell>
                  ) : null}
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
}
