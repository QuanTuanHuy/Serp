/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Scan handover orders out dialog
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
import { getScannedOutOrders, getTotalOrders } from '../handoverManifestModels';
import { DetailItem } from './DetailItem';
import { ManifestOrdersTable } from './ManifestOrdersTable';

interface HandoverManifestScanOutDialogProps {
  activeScanManifest?: HandoverManifest | null;
  isFetchingScanManifest: boolean;
  isScanningOut: boolean;
  onOpenChange: (open: boolean) => void;
  onScan: (orderCode?: string) => void;
  onScanOrderCodeChange: (value: string) => void;
  open: boolean;
  scanOrderCode: string;
  scanOrders: HandoverManifestOrderItem[];
}

export function HandoverManifestScanOutDialog({
  activeScanManifest,
  isFetchingScanManifest,
  isScanningOut,
  onOpenChange,
  onScan,
  onScanOrderCodeChange,
  open,
  scanOrderCode,
  scanOrders,
}: HandoverManifestScanOutDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>Quét xuất đơn hàng</DialogTitle>
          <DialogDescription>
            Quét từng đơn trước khi xuất phiếu bàn giao đi hub.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='grid gap-3 sm:grid-cols-3'>
            <DetailItem
              label='Phiếu'
              value={activeScanManifest?.manifestCode || '--'}
            />
            <DetailItem
              label='Đã quét xuất'
              value={`${getScannedOutOrders(activeScanManifest)}/${getTotalOrders(
                activeScanManifest
              )}`}
            />
            <DetailItem
              label='Trạng thái'
              value={activeScanManifest?.status || '--'}
            />
          </div>

          <div className='flex flex-col gap-2 sm:flex-row'>
            <Input
              value={scanOrderCode}
              onChange={(event) => onScanOrderCodeChange(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault();
                  onScan();
                }
              }}
              placeholder='Quét hoặc nhập mã đơn hàng'
            />
            <Button disabled={isScanningOut} onClick={() => onScan()}>
              <ScanLine className='mr-2 h-4 w-4' />
              Quét xuất
            </Button>
          </div>

          {isFetchingScanManifest ? (
            <p className='text-sm text-muted-foreground'>
              Đang tải đơn trong phiếu...
            </p>
          ) : (
            <ManifestOrdersTable
              actionMode='SCAN_OUT'
              isActionLoading={isScanningOut}
              orders={scanOrders}
              onScan={onScan}
            />
          )}
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Xong
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
