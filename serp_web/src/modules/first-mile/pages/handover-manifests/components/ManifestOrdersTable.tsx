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
import { formatDateTime } from '../handoverManifestModels';

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
            <TableHead>Order</TableHead>
            <TableHead>Customer order</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Scan out</TableHead>
            <TableHead>Scan in</TableHead>
            {actionMode ? <TableHead>Action</TableHead> : null}
          </TableRow>
        </TableHeader>
        <TableBody>
          {orders.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={actionMode ? 6 : 5}
                className='py-8 text-center text-muted-foreground'
              >
                No orders in this manifest.
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
                actionMode === 'SCAN_OUT' ? 'Scanned out' : 'Scanned in';

              return (
                <TableRow key={order.id ?? order.orderCode}>
                  <TableCell className='font-medium'>
                    {order.orderCode || '--'}
                  </TableCell>
                  <TableCell>{order.customerOrderCode || '--'}</TableCell>
                  <TableCell>
                    {order.status ? (
                      <Badge variant='outline'>{order.status}</Badge>
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
                          Scan
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
