/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order cancel dialog
 */

import React from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  Textarea,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type { FirstMileOrderDetail } from '../../../../types';

interface OrderCancelDialogProps {
  cancelTarget: FirstMileOrderDetail | null;
  cancelReason: string;
  isCancellingOrder: boolean;
  onOpenChange: (open: boolean) => void;
  onCancelReasonChange: (value: string) => void;
  onKeepOrder: () => void;
  onConfirmCancel: () => void;
}

export const OrderCancelDialog: React.FC<OrderCancelDialogProps> = ({
  cancelTarget,
  cancelReason,
  isCancellingOrder,
  onOpenChange,
  onCancelReasonChange,
  onKeepOrder,
  onConfirmCancel,
}) => {
  return (
    <Dialog open={Boolean(cancelTarget)} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-lg'>
        <DialogHeader>
          <DialogTitle>Cancel Order</DialogTitle>
          <DialogDescription>
            Cancel order {cancelTarget?.orderCode}. Provide a reason if needed.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-2'>
          <Label htmlFor='cancelReason'>Cancel reason</Label>
          <Textarea
            id='cancelReason'
            value={cancelReason}
            onChange={(event) => onCancelReasonChange(event.target.value)}
            placeholder='Optional reason for cancellation'
            rows={3}
          />
        </div>

        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={onKeepOrder}
            disabled={isCancellingOrder}
          >
            Keep order
          </Button>
          <Button
            type='button'
            variant='destructive'
            onClick={onConfirmCancel}
            disabled={isCancellingOrder}
          >
            {isCancellingOrder ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : null}
            Confirm cancel
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
