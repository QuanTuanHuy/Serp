/*
Author: QuanTuanHuy
Description: Part of Serp Project - Outbound Shipment card
*/

import { Badge, Button, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { CheckCircle2, Clock3, Package, Truck } from 'lucide-react';
import type { OutboundShipment } from '../../types';

const statusStyles = {
  CREATED: {
    label: 'Nháp',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    icon: Clock3,
  },
  READY_TO_EXPORT: {
    label: 'Sẵn sàng xuất',
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    icon: Truck,
  },
  DELIVERED: {
    label: 'Đã giao',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    icon: CheckCircle2,
  },
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN');
};

interface OutboundShipmentCardProps {
  shipment: OutboundShipment;
  orderName?: string;
  facilityName?: string;
  onClick?: () => void;
}

export const OutboundShipmentCard: React.FC<OutboundShipmentCardProps> = ({
  shipment,
  orderName,
  facilityName,
  onClick,
}) => {
  const status =
    statusStyles[shipment.status as keyof typeof statusStyles] ||
    statusStyles.CREATED;
  const StatusIcon = status.icon;

  return (
    <Card
      className='group cursor-pointer overflow-hidden transition-all duration-200 hover:border-primary/30 hover:shadow-md'
      onClick={onClick}
    >
      <div className='h-1.5 bg-gradient-to-r from-primary/60 via-primary/30 to-primary/5' />

      <CardContent className='space-y-4 p-5'>
        <div className='flex items-start justify-between gap-3'>
          <div className='min-w-0 flex-1'>
            <div className='mb-2 flex items-center gap-2'>
              <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary'>
                <Package className='h-5 w-5' />
              </div>
              <div className='min-w-0'>
                <h3 className='truncate font-semibold text-foreground'>
                  {shipment.name || `Phiếu xuất #${shipment.id.slice(0, 8)}...`}
                </h3>
                <p className='text-xs text-muted-foreground'>
                  ID: {shipment.id.slice(0, 10)}...
                </p>
              </div>
            </div>

            <Badge
              variant='secondary'
              className={cn('gap-1', status.bg, status.text)}
            >
              <StatusIcon className='h-3 w-3' />
              {status.label}
            </Badge>
          </div>
        </div>

        <div className='space-y-1 text-sm text-muted-foreground'>
          <p>Đơn hàng: {orderName || shipment.orderId}</p>
          <p>Kho xuất: {facilityName || shipment.facilityId}</p>
          <p>Ngày tạo: {formatDate(shipment.createdStamp)}</p>
          <p>Số dòng hàng: {shipment.items?.length || 0}</p>
        </div>

        <div className='flex justify-end border-t pt-3'>
          <Button
            variant='ghost'
            size='sm'
            className='opacity-0 transition-opacity group-hover:opacity-100'
            onClick={(event) => {
              event.stopPropagation();
              onClick?.();
            }}
          >
            Xem chi tiết
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
