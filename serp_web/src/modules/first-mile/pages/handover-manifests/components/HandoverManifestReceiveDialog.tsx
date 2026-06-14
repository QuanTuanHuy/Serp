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
          <DialogTitle>Receive manifest at hub</DialogTitle>
          <DialogDescription>
            Scan received order codes, then confirm inbound for the scanned
            orders.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='grid gap-3 sm:grid-cols-4'>
            <DetailItem
              label='Manifest'
              value={activeReceiveManifest?.manifestCode || '--'}
            />
            <DetailItem
              label='Post office'
              value={activeReceiveManifest?.originPostOfficeCode || '--'}
            />
            <DetailItem
              label='Seal'
              value={activeReceiveManifest?.sealCode || '--'}
            />
            <DetailItem
              label='Scanned in'
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
              placeholder='Scan or enter received order code'
            />
            <Button variant='outline' onClick={() => onScanInboundOrder()}>
              <ScanLine className='mr-2 h-4 w-4' />
              Scan received
            </Button>
          </div>

          {isFetchingReceiveManifest ? (
            <p className='text-sm text-muted-foreground'>
              Loading manifest orders...
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
            Cancel
          </Button>
          <Button
            disabled={receivedOrderCodes.length === 0 || isConfirmingInbound}
            onClick={onConfirm}
          >
            {isConfirmingInbound ? 'Confirming...' : 'Confirm inbound'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
