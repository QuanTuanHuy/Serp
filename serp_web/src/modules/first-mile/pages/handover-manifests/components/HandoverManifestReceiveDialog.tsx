/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Receive handover manifest dialog
 */

import { ScanLine } from 'lucide-react';

import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
} from '@/shared/components/ui';
import type {
  HandoverManifest,
  HandoverManifestOrderItem,
} from '../../../types';
import { getTotalOrders } from '../handoverManifestModels';
import { DetailItem } from './DetailItem';
import { ManifestOrdersTable } from './ManifestOrdersTable';

interface HandoverManifestReceiveDialogProps {
  activeReceiveManifest?: HandoverManifest | null;
  isConfirmingInbound: boolean;
  isFetchingReceiveManifest: boolean;
  onConfirm: () => void;
  onOpenChange: (open: boolean) => void;
  onReceiveOrderCodeChange: (value: string) => void;
  onScanInboundOrder: (orderCode?: string) => void;
  open: boolean;
  receiveOrderCode: string;
  receiveOrders: HandoverManifestOrderItem[];
  receivedOrderCodes: string[];
}

export function HandoverManifestReceiveDialog({
  activeReceiveManifest,
  isConfirmingInbound,
  isFetchingReceiveManifest,
  onConfirm,
  onOpenChange,
  onReceiveOrderCodeChange,
  onScanInboundOrder,
  open,
  receiveOrderCode,
  receiveOrders,
  receivedOrderCodes,
}: HandoverManifestReceiveDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>Nhận phiếu tại hub</DialogTitle>
          <DialogDescription>
            Quét mã đơn đã nhận, sau đó xác nhận nhập hàng cho các đơn đã quét.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='grid gap-3 sm:grid-cols-4'>
            <DetailItem
              label='Phiếu'
              value={activeReceiveManifest?.manifestCode || '--'}
            />
            <DetailItem
              label='Bưu cục'
              value={activeReceiveManifest?.originPostOfficeCode || '--'}
            />
            <DetailItem
              label='Seal'
              value={activeReceiveManifest?.sealCode || '--'}
            />
            <DetailItem
              label='Đã quét nhập'
              value={`${receivedOrderCodes.length}/${getTotalOrders(
                activeReceiveManifest
              )}`}
            />
          </div>

          <div className='flex flex-col gap-2 sm:flex-row'>
            <Input
              value={receiveOrderCode}
              onChange={(event) => onReceiveOrderCodeChange(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault();
                  onScanInboundOrder();
                }
              }}
              placeholder='Quét hoặc nhập mã đơn đã nhận'
            />
            <Button variant='outline' onClick={() => onScanInboundOrder()}>
              <ScanLine className='mr-2 h-4 w-4' />
              Quét nhập
            </Button>
          </div>

          {isFetchingReceiveManifest ? (
            <p className='text-sm text-muted-foreground'>
              Đang tải đơn trong phiếu...
            </p>
          ) : (
            <ManifestOrdersTable
              actionMode='RECEIVE'
              isActionLoading={isConfirmingInbound}
              orders={receiveOrders}
              scannedOrderCodes={receivedOrderCodes}
              onScan={onScanInboundOrder}
            />
          )}
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Hủy
          </Button>
          <Button
            disabled={receivedOrderCodes.length === 0 || isConfirmingInbound}
            onClick={onConfirm}
          >
            {isConfirmingInbound ? 'Đang xác nhận...' : 'Xác nhận nhập'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
