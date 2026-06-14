/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Create handover manifest dialog
 */

import {
  Badge,
  Button,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  Textarea,
} from '@/shared/components/ui';
import { TmsCombobox, type TmsComboboxOption } from '../../../components';
import type { FirstMileOrderDetail, PostOffice } from '../../../types';
import { DetailItem } from './DetailItem';

interface HandoverManifestCreateDialogProps {
  allReadyOrdersSelected: boolean;
  createNote: string;
  createPostOfficeId: string;
  createPostOfficeOptions: TmsComboboxOption[];
  isCreating: boolean;
  isLoadingReadyOrders: boolean;
  onCreateNoteChange: (value: string) => void;
  onCreatePostOfficeChange: (value: string) => void;
  onOpenChange: (open: boolean) => void;
  onSubmit: () => void;
  onToggleAllReadyOrders: (checked: boolean) => void;
  onToggleOrder: (orderCode: string, checked?: boolean) => void;
  open: boolean;
  readyOrderCodes: string[];
  readyOrders: FirstMileOrderDetail[];
  selectedCreatePostOffice?: PostOffice;
  selectedOrderCodes: string[];
  targetHubLabel: string;
}

export function HandoverManifestCreateDialog({
  allReadyOrdersSelected,
  createNote,
  createPostOfficeId,
  createPostOfficeOptions,
  isCreating,
  isLoadingReadyOrders,
  onCreateNoteChange,
  onCreatePostOfficeChange,
  onOpenChange,
  onSubmit,
  onToggleAllReadyOrders,
  onToggleOrder,
  open,
  readyOrderCodes,
  readyOrders,
  selectedCreatePostOffice,
  selectedOrderCodes,
  targetHubLabel,
}: HandoverManifestCreateDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>New post office handover</DialogTitle>
          <DialogDescription>
            Select orders waiting at the post office. The target hub is read
            from the post office mapping.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='create-post-office'>Origin post office *</Label>
              <TmsCombobox
                id='create-post-office'
                value={createPostOfficeId}
                onValueChange={onCreatePostOfficeChange}
                options={createPostOfficeOptions}
                placeholder='Select post office'
                emptyText='No post offices found'
              />
            </div>

            <DetailItem label='Target hub' value={targetHubLabel} />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='create-note'>Note</Label>
            <Textarea
              id='create-note'
              value={createNote}
              onChange={(event) => onCreateNoteChange(event.target.value)}
              placeholder='Optional handover note'
            />
          </div>

          <div className='space-y-2'>
            <div className='flex flex-wrap items-center justify-between gap-2'>
              <Label>Orders ready at post office *</Label>
              {readyOrderCodes.length > 0 ? (
                <label className='flex items-center gap-2 text-sm'>
                  <Checkbox
                    checked={allReadyOrdersSelected}
                    onCheckedChange={(value) =>
                      onToggleAllReadyOrders(Boolean(value))
                    }
                  />
                  Select all
                </label>
              ) : null}
            </div>

            {!selectedCreatePostOffice ? (
              <p className='text-sm text-muted-foreground'>
                Select a post office to load ready orders.
              </p>
            ) : isLoadingReadyOrders ? (
              <p className='text-sm text-muted-foreground'>
                Loading ready orders...
              </p>
            ) : readyOrders.length === 0 ? (
              <p className='text-sm text-muted-foreground'>
                No orders with status AT_ORIGIN_POST_OFFICE at this post office.
              </p>
            ) : (
              <div className='max-h-72 space-y-2 overflow-y-auto rounded-md border p-3'>
                {readyOrders.map((order) => {
                  const orderCode = order.orderCode;
                  if (!orderCode) {
                    return null;
                  }
                  const checked = selectedOrderCodes.includes(orderCode);
                  return (
                    <div
                      key={order.id}
                      className='flex items-start gap-3 rounded-md border p-3'
                    >
                      <Checkbox
                        checked={checked}
                        onCheckedChange={(value) =>
                          onToggleOrder(orderCode, Boolean(value))
                        }
                        aria-label={`Select order ${orderCode}`}
                      />
                      <div className='min-w-0 flex-1 space-y-1 text-sm'>
                        <div className='flex flex-wrap items-center gap-2'>
                          <span className='font-medium'>{orderCode}</span>
                          <Badge variant='outline'>{order.status}</Badge>
                        </div>
                        <p className='text-xs text-muted-foreground'>
                          Customer order: {order.customerOrderCode || '--'}
                        </p>
                        <p className='text-xs text-muted-foreground'>
                          Receiver: {order.receiverName || '--'} (
                          {order.receiverPhone || '--'})
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button disabled={isCreating} onClick={onSubmit}>
            {isCreating ? 'Creating...' : 'Create manifest'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
