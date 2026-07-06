/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Post office inbound scan dialog after courier return
 */

'use client';

import React from 'react';
import { ScanLine } from 'lucide-react';
import { useNotification } from '@/shared/hooks';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
} from '@/shared/components';
import type {
  PickupTrackingOrder,
  PickupTrackingTrip,
} from '../../../../types';

const normalizeScanCode = (value: string): string => value.trim().toUpperCase();

interface PickupPostOfficeInboundDialogProps {
  open: boolean;
  trip: PickupTrackingTrip | null;
  orders: PickupTrackingOrder[];
  isConfirming: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: (orderCodes: string[]) => Promise<void>;
}

export function PickupPostOfficeInboundDialog({
  open,
  trip,
  orders,
  isConfirming,
  onOpenChange,
  onConfirm,
}: PickupPostOfficeInboundDialogProps) {
  const notification = useNotification();
  const [scanOrderCode, setScanOrderCode] = React.useState('');
  const [scannedOrderCodes, setScannedOrderCodes] = React.useState<string[]>(
    []
  );

  const pendingOrders = React.useMemo(
    () =>
      orders.filter(
        (order) =>
          order.tripId === trip?.tripId &&
          order.orderStatus === 'PENDING_ORIGIN_POST_OFFICE_INBOUND'
      ),
    [orders, trip?.tripId]
  );

  React.useEffect(() => {
    if (!open) {
      setScanOrderCode('');
      setScannedOrderCodes([]);
    }
  }, [open, trip?.tripId]);

  const handleScanOrder = (orderCodeValue?: string) => {
    const orderCode = normalizeScanCode(orderCodeValue ?? scanOrderCode);
    if (!orderCode) {
      notification.error('Vui lòng nhập mã đơn.');
      return;
    }

    const matchedOrder = pendingOrders.find(
      (order) => normalizeScanCode(order.orderCode ?? '') === orderCode
    );
    if (pendingOrders.length > 0 && !matchedOrder) {
      notification.error(
        'Đơn này không chờ nhập bưu cục trong chuyến đã chọn.'
      );
      return;
    }
    if (scannedOrderCodes.includes(orderCode)) {
      notification.error('Đơn này đã được quét.');
      return;
    }

    setScannedOrderCodes((current) => [...current, orderCode]);
    setScanOrderCode('');
  };

  const handleConfirm = async () => {
    if (scannedOrderCodes.length === 0) {
      return;
    }
    await onConfirm(scannedOrderCodes);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Xác nhận nhập bưu cục</DialogTitle>
          <DialogDescription>
            Quét các đơn nhân viên giao nhận mang về{' '}
            {trip?.tripCode ? `trong chuyến ${trip.tripCode}` : ''} trước khi
            chuyển sang bàn giao.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='flex flex-col gap-2 sm:flex-row'>
            <Input
              className='min-w-0'
              value={scanOrderCode}
              onChange={(event) => setScanOrderCode(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault();
                  handleScanOrder();
                }
              }}
              placeholder='Quét hoặc nhập mã đơn'
            />
            <Button
              className='shrink-0 whitespace-nowrap'
              variant='outline'
              onClick={() => handleScanOrder()}
            >
              <ScanLine className='mr-2 h-4 w-4' />
              Quét đơn
            </Button>
          </div>

          <div className='overflow-x-auto rounded-md border'>
            <table className='w-full min-w-[480px] text-sm'>
              <thead className='bg-muted/40 text-left'>
                <tr>
                  <th className='px-3 py-2 font-medium'>Đơn</th>
                  <th className='px-3 py-2 font-medium'>Người gửi</th>
                  <th className='px-3 py-2 font-medium'>Trạng thái quét</th>
                </tr>
              </thead>
              <tbody>
                {pendingOrders.length === 0 ? (
                  <tr className='border-t'>
                    <td className='px-3 py-4 text-muted-foreground' colSpan={3}>
                      Không có đơn đang chờ xác nhận nhập bưu cục.
                    </td>
                  </tr>
                ) : (
                  pendingOrders.map((order) => {
                    const orderCode = normalizeScanCode(order.orderCode ?? '');
                    const isScanned = scannedOrderCodes.includes(orderCode);

                    return (
                      <tr key={order.orderId} className='border-t'>
                        <td className='px-3 py-2 font-medium'>
                          {order.orderCode || '--'}
                        </td>
                        <td className='px-3 py-2'>
                          <div>{order.senderName || '--'}</div>
                          <div className='text-xs text-muted-foreground'>
                            {order.senderPhone || '--'}
                          </div>
                        </td>
                        <td className='px-3 py-2'>
                          <Badge variant={isScanned ? 'default' : 'outline'}>
                            {isScanned ? 'Đã quét' : 'Chờ quét'}
                          </Badge>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>

        <DialogFooter className='sm:flex-wrap'>
          <Button
            className='w-full sm:w-auto'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Hủy
          </Button>
          <Button
            className='w-full sm:w-auto'
            disabled={
              scannedOrderCodes.length === 0 ||
              isConfirming ||
              pendingOrders.length === 0
            }
            onClick={() => void handleConfirm()}
          >
            {isConfirming ? 'Đang xác nhận...' : 'Xác nhận nhập'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
