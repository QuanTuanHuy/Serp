import { Button, Card, CardContent, Badge } from '@/shared/components/ui';
import {
  Clock,
  Truck,
  User,
  ArrowDownToLine,
  ArrowUpFromLine,
  Box,
  ReceiptText,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { Customer, Order, Shipment, Supplier } from '../../types';

const statusStyles = {
  CREATED: {
    label: 'Nháp',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
    icon: Clock,
  },
  IMPORTED: {
    label: 'Đã nhập kho',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: ArrowDownToLine,
  },
  EXPORTED: {
    label: 'Đã xuất kho',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: ArrowUpFromLine,
  },
};

const typeStyles = {
  INBOUND: {
    label: 'Phiếu nhập',
    bg: 'bg-green-100 dark:bg-green-900/30',
    text: 'text-green-700 dark:text-green-400',
    icon: ReceiptText,
  },
  OUTBOUND: {
    label: 'Phiếu xuất',
    bg: 'bg-orange-100 dark:bg-orange-900/30',
    text: 'text-orange-700 dark:text-orange-400',
    icon: ReceiptText,
  },
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN');
};

export const ShipmentCard = ({
  shipment,
  order,
  customer,
  supplier,
  onClick,
}: {
  shipment: Shipment;
  order?: Order;
  customer?: Customer;
  supplier?: Supplier;
  onClick?: () => void;
}) => {
  const status =
    statusStyles[shipment.statusId as keyof typeof statusStyles] ||
    statusStyles.CREATED;
  const type =
    typeStyles[shipment.shipmentTypeId as keyof typeof typeStyles] ||
    typeStyles.INBOUND;
  const StatusIcon = status.icon;
  const TypeIcon = type.icon;

  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer'
      )}
      onClick={onClick}
    >
      <div className='relative h-2 bg-gradient-to-r from-primary/60 via-primary/40 to-primary/20' />

      <CardContent className='p-5'>
        <div className='flex items-start justify-between mb-4'>
          <div className='flex-1 min-w-0'>
            <div className='flex items-center gap-2 mb-2'>
              <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 text-primary'>
                <Truck className='h-5 w-5' />
              </div>
              <div className='flex-1 min-w-0'>
                <h3 className='font-semibold text-foreground truncate'>
                  {shipment.shipmentName ||
                    `Phiếu #${shipment.id?.slice(0, 8)}...`}
                </h3>
                <p className='text-xs text-muted-foreground'>
                  ID: {shipment.id?.slice(0, 10) || 'N/A'}...
                </p>
              </div>
            </div>

            <div className='flex items-center gap-2 mb-2'>
              <Badge
                variant='outline'
                className={cn('gap-1', type.bg, type.text)}
              >
                <TypeIcon className='h-3 w-3' />
                {type.label}
              </Badge>
              <Badge
                variant='secondary'
                className={cn('gap-1', status.bg, status.text)}
              >
                <StatusIcon className='h-3 w-3' />
                <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
                {status.label}
              </Badge>
            </div>
          </div>
        </div>

        <div className='space-y-2 mb-4'>
          {order !== undefined && (
            <div className='flex items-center gap-2 text-sm'>
              <Box className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                Đơn hàng: {order.orderName || order.id.slice(0, 8) + '...'}
              </span>
            </div>
          )}

          {supplier !== undefined && (
            <div className='flex items-center gap-2 text-sm'>
              <User className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                NCC: {supplier.name}
              </span>
            </div>
          )}

          {customer !== undefined && (
            <div className='flex items-center gap-2 text-sm'>
              <User className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                Khách hàng: {customer.name}
              </span>
            </div>
          )}
        </div>

        <div className='flex items-center justify-between pt-3 border-t'>
          <div>
            <p className='text-xs text-muted-foreground'>Ngày giao</p>
            <p className='text-lg font-bold text-foreground'>
              {shipment.expectedDeliveryDate
                ? formatDate(shipment.expectedDeliveryDate)
                : formatDate(shipment.createdStamp)}
            </p>
          </div>

          <Button
            variant='ghost'
            size='sm'
            className='opacity-0 group-hover:opacity-100 transition-opacity'
            onClick={(e) => {
              e.stopPropagation();
              onClick?.();
            }}
          >
            Xem chi tiết →
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
