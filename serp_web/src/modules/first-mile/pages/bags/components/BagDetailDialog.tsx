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
          <DialogTitle>Chi tiết túi</DialogTitle>
          <DialogDescription>
            {bag?.bagCode ?? 'Đang tải thông tin túi'}
          </DialogDescription>
        </DialogHeader>

        {isLoading || !bag ? (
          <div className='py-8 text-center text-sm text-muted-foreground'>
            Đang tải chi tiết túi...
          </div>
        ) : (
          <div className='space-y-5'>
            <div className='grid gap-3 text-sm md:grid-cols-3'>
              <DetailItem
                label='Hub gốc'
                value={getHubLabel(hubs, bag.originHubId)}
              />
              <DetailItem
                label='Điểm đến'
                value={getDestinationLabel(bag, hubs)}
              />
              <DetailItem
                label='Xe'
                value={getVehicleLabel(vehicles, bag.vehicleId)}
              />
              <DetailItem
                label='Trạng thái'
                value={
                  <Badge variant={getBagStatusVariant(bag.status)}>
                    {getBagStatusLabel(bag.status)}
                  </Badge>
                }
              />
              <DetailItem
                label='Đơn hàng'
                value={`${formatNumber(bag.currentOrders, 0)} / ${formatNumber(
                  bag.maxOrders,
                  0
                )}`}
              />
              <DetailItem
                label='Khối lượng'
                value={`${formatNumber(bag.currentWeight)} / ${formatNumber(
                  bag.maxWeight
                )} kg`}
              />
              <DetailItem
                label='Thể tích'
                value={`${formatNumber(bag.currentVolume)} / ${formatNumber(
                  bag.maxVolume
                )} m3`}
              />
              <DetailItem
                label='Niêm phong lúc'
                value={formatDateTime(bag.sealedAt)}
              />
              <DetailItem
                label='Cập nhật lúc'
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
                    <TableHead>Mã đơn hàng</TableHead>
                    <TableHead>ID đơn hàng</TableHead>
                    <TableHead className='text-right'>Thao tác</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={3}
                        className='h-20 text-center text-muted-foreground'
                      >
                        Chưa có đơn hàng trong túi này.
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
                                    aria-label='Xóa đơn khỏi túi'
                                    onClick={() =>
                                      onRemoveOrder(order.orderCode!)
                                    }
                                  >
                                    <Trash2 className='h-4 w-4' />
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent>Xóa đơn khỏi túi</TooltipContent>
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
            Đóng
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
