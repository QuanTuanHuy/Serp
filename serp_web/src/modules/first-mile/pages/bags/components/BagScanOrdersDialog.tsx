/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag scan orders dialog
 */

import { CheckCircle2, Loader2, XCircle } from 'lucide-react';

import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea,
} from '@/shared/components';

import type {
  SecondMileBag,
  SecondMileBaggingValidation,
} from '../../../types';

interface BagScanOrdersDialogProps {
  open: boolean;
  bag?: SecondMileBag;
  orderCodes: string;
  validation?: SecondMileBaggingValidation | null;
  isValidating: boolean;
  isAdding: boolean;
  onOpenChange: (open: boolean) => void;
  onOrderCodesChange: (value: string) => void;
  onValidate: () => void;
  onAddAccepted: () => void;
}

export function BagScanOrdersDialog({
  open,
  bag,
  orderCodes,
  validation,
  isValidating,
  isAdding,
  onOpenChange,
  onOrderCodesChange,
  onValidate,
  onAddAccepted,
}: BagScanOrdersDialogProps) {
  const acceptedCount = validation?.acceptedCount ?? 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>Add orders</DialogTitle>
          <DialogDescription>
            {bag?.bagCode
              ? `Scan or paste order codes for ${bag.bagCode}.`
              : 'Select an open bag before adding orders.'}
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <Textarea
            value={orderCodes}
            onChange={(event) => onOrderCodesChange(event.target.value)}
            rows={7}
            placeholder={'ORD-001\nORD-002'}
            disabled={isValidating || isAdding}
            autoFocus
          />

          {validation && (
            <div className='rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Order code</TableHead>
                    <TableHead>Result</TableHead>
                    <TableHead>Reason</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {validation.items.map((item) => (
                    <TableRow key={item.orderCode}>
                      <TableCell className='font-medium'>
                        {item.orderCode}
                      </TableCell>
                      <TableCell>
                        {item.accepted ? (
                          <Badge className='gap-1'>
                            <CheckCircle2 className='h-3 w-3' />
                            Accepted
                          </Badge>
                        ) : (
                          <Badge variant='destructive' className='gap-1'>
                            <XCircle className='h-3 w-3' />
                            Rejected
                          </Badge>
                        )}
                      </TableCell>
                      <TableCell className='text-sm text-muted-foreground'>
                        {item.reason ?? '-'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            disabled={isValidating || isAdding}
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button
            type='button'
            variant='outline'
            disabled={!bag || isValidating || isAdding || !orderCodes.trim()}
            onClick={onValidate}
          >
            {isValidating && <Loader2 className='h-4 w-4 animate-spin' />}
            Validate
          </Button>
          <Button
            type='button'
            disabled={
              !validation || acceptedCount === 0 || isValidating || isAdding
            }
            onClick={onAddAccepted}
          >
            {isAdding && <Loader2 className='h-4 w-4 animate-spin' />}
            Add accepted
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
