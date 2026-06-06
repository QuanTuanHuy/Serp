/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag detail dialog
 */

import type React from 'react';

import { Trash2 } from 'lucide-react';

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
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/shared/components';

import type { Hub, SecondMileBag, SecondMileVehicle } from '../../../types';
import {
  formatDateTime,
  formatNumber,
  getBagStatusLabel,
  getBagStatusVariant,
  getDestinationLabel,
  getHubLabel,
  getVehicleLabel,
} from '../bagPageModels';

interface BagDetailDialogProps {
  open: boolean;
  bag?: SecondMileBag;
  hubs: Hub[];
  vehicles: SecondMileVehicle[];
  isLoading?: boolean;
  canRemoveOrders: boolean;
  isRemovingOrder?: boolean;
  onOpenChange: (open: boolean) => void;
  onRemoveOrder: (orderCode: string) => void;
}

export function BagDetailDialog({
  open,
  bag,
  hubs,
  vehicles,
  isLoading,
  canRemoveOrders,
  isRemovingOrder,
  onOpenChange,
  onRemoveOrder,
}: BagDetailDialogProps) {
  const orders = bag?.orders ?? [];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>Bag details</DialogTitle>
          <DialogDescription>
            {bag?.bagCode ?? 'Loading bag information'}
          </DialogDescription>
        </DialogHeader>

        {isLoading || !bag ? (
          <div className='py-8 text-center text-sm text-muted-foreground'>
            Loading bag details...
          </div>
        ) : (
          <div className='space-y-5'>
            <div className='grid gap-3 text-sm md:grid-cols-3'>
              <DetailItem
                label='Origin hub'
                value={getHubLabel(hubs, bag.originHubId)}
              />
              <DetailItem
                label='Destination'
                value={getDestinationLabel(bag, hubs)}
              />
              <DetailItem
                label='Vehicle'
                value={getVehicleLabel(vehicles, bag.vehicleId)}
              />
              <DetailItem
                label='Status'
                value={
                  <Badge variant={getBagStatusVariant(bag.status)}>
                    {getBagStatusLabel(bag.status)}
                  </Badge>
                }
              />
              <DetailItem
                label='Orders'
                value={`${formatNumber(bag.currentOrders, 0)} / ${formatNumber(
                  bag.maxOrders,
                  0
                )}`}
              />
              <DetailItem
                label='Weight'
                value={`${formatNumber(bag.currentWeight)} / ${formatNumber(
                  bag.maxWeight
                )} kg`}
              />
              <DetailItem
                label='Volume'
                value={`${formatNumber(bag.currentVolume)} / ${formatNumber(
                  bag.maxVolume
                )} m3`}
              />
              <DetailItem
                label='Sealed at'
                value={formatDateTime(bag.sealedAt)}
              />
              <DetailItem
                label='Updated at'
                value={formatDateTime(bag.updatedAt)}
              />
            </div>

            {bag.note && (
              <div className='rounded-md border p-3 text-sm text-muted-foreground'>
                {bag.note}
              </div>
            )}

            <div className='overflow-x-auto rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Order code</TableHead>
                    <TableHead>Order ID</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={3}
                        className='h-20 text-center text-muted-foreground'
                      >
                        No orders in this bag.
                      </TableCell>
                    </TableRow>
                  ) : (
                    orders.map((order) => (
                      <TableRow key={`${order.id}-${order.orderCode}`}>
                        <TableCell className='font-medium'>
                          {order.orderCode ?? '-'}
                        </TableCell>
                        <TableCell>{order.orderId ?? '-'}</TableCell>
                        <TableCell className='text-right'>
                          {canRemoveOrders &&
                            bag.status === 'CREATED' &&
                            order.orderCode && (
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <Button
                                    type='button'
                                    size='icon'
                                    variant='ghost'
                                    disabled={isRemovingOrder}
                                    aria-label='Remove order'
                                    onClick={() =>
                                      onRemoveOrder(order.orderCode!)
                                    }
                                  >
                                    <Trash2 className='h-4 w-4' />
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent>Remove order</TooltipContent>
                              </Tooltip>
                            )}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          </div>
        )}

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

interface DetailItemProps {
  label: string;
  value: React.ReactNode;
}

function DetailItem({ label, value }: DetailItemProps) {
  return (
    <div className='rounded-md border p-3'>
      <div className='text-xs text-muted-foreground'>{label}</div>
      <div className='mt-1 font-medium'>{value}</div>
    </div>
  );
}
