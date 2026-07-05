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
} from '@/shared/components';

import type {
  SecondMileBag,
  SecondMileBaggingValidation,
  SecondMileOrder,
} from '../../../types';
import { AutoBaggingOrderMultiSelect } from './AutoBaggingOrderMultiSelect';

interface BagScanOrdersDialogProps {
  open: boolean;
  bag?: SecondMileBag;
  orders: SecondMileOrder[];
  selectedOrderCodes: string[];
  validation?: SecondMileBaggingValidation | null;
  isValidating: boolean;
  isAdding: boolean;
  isOrdersLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onOrderCodesChange: (value: string[]) => void;
  onValidate: () => void;
  onAddAccepted: () => void;
}

export function BagScanOrdersDialog({
  open,
  bag,
  orders,
  selectedOrderCodes,
  validation,
  isValidating,
  isAdding,
  isOrdersLoading,
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
          <DialogTitle>Thêm đơn hàng</DialogTitle>
          <DialogDescription>
            {bag?.bagCode
              ? `Chọn các đơn đủ điều kiện cho ${bag.bagCode}.`
              : 'Chọn một túi đang mở trước khi thêm đơn hàng.'}
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <AutoBaggingOrderMultiSelect
            id='bag-order-codes'
            orders={orders}
            selectedOrderCodes={selectedOrderCodes}
            onSelectionChange={onOrderCodesChange}
            disabled={!bag || isValidating || isAdding}
            loading={isOrdersLoading}
            placeholder='Chọn đơn đủ điều kiện'
          />

          {validation && (
            <div className='rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã đơn hàng</TableHead>
                    <TableHead>Kết quả</TableHead>
                    <TableHead>Lý do</TableHead>
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
                            Chấp nhận
                          </Badge>
                        ) : (
                          <Badge variant='destructive' className='gap-1'>
                            <XCircle className='h-3 w-3' />
                            Từ chối
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
            Hủy
          </Button>
          <Button
            type='button'
            variant='outline'
            disabled={
              !bag ||
              isValidating ||
              isAdding ||
              isOrdersLoading ||
              selectedOrderCodes.length === 0
            }
            onClick={onValidate}
          >
            {isValidating && <Loader2 className='h-4 w-4 animate-spin' />}
            Kiểm tra
          </Button>
          <Button
            type='button'
            disabled={
              !validation || acceptedCount === 0 || isValidating || isAdding
            }
            onClick={onAddAccepted}
          >
            {isAdding && <Loader2 className='h-4 w-4 animate-spin' />}
            Thêm đơn hợp lệ
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
