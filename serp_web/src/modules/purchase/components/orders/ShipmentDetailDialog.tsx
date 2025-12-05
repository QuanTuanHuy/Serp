/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment detail dialog (read-only)
*/

'use client';

import React, { useMemo } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Badge,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Separator } from '@/shared/components/ui/separator';
import {
  useGetShipmentByIdQuery,
  useGetProductsQuery,
} from '../../services/purchaseApi';
import { formatDate } from '@/shared/utils';
import type { OrderDetail } from '../../types';

interface ShipmentDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shipmentId: string | null;
  order: OrderDetail;
}

export const ShipmentDetailDialog: React.FC<ShipmentDetailDialogProps> = ({
  open,
  onOpenChange,
  shipmentId,
  order,
}) => {
  // Fetch shipment detail
  const { data: shipmentResponse } = useGetShipmentByIdQuery(shipmentId || '', {
    skip: !shipmentId,
  });

  const shipment = shipmentResponse?.data;

  // Fetch products for display names
  const { data: productsResponse } = useGetProductsQuery({
    page: 1,
    size: 1000,
  });

  const products = useMemo(
    () => productsResponse?.data?.items || [],
    [productsResponse]
  );

  const getProductName = (productId: string) => {
    const product = products.find((p) => p.id === productId);
    return product?.name || productId;
  };

  const getProductUnit = (productId: string) => {
    const product = order.orderItems?.find(
      (item) => item.productId === productId
    );
    return product?.unit || '';
  };

  const getStatusBadge = (statusId: string) => {
    const statusMap: Record<string, { label: string; variant: any }> = {
      CREATED: { label: 'Đã tạo', variant: 'secondary' },
      READY: { label: 'Sẵn sàng', variant: 'default' },
      IMPORTED: { label: 'Đã nhập', variant: 'success' },
      EXPORTED: { label: 'Đã xuất', variant: 'outline' },
    };

    const status = statusMap[statusId] || {
      label: statusId,
      variant: 'secondary',
    };
    return <Badge variant={status.variant}>{status.label}</Badge>;
  };

  if (!shipment) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-[calc(100%-2rem)] sm:max-w-7xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>{shipment.shipmentName}</DialogTitle>
        </DialogHeader>

        {/* Shipment Information */}
        <div className='grid grid-cols-2 gap-4 text-sm'>
          <div>
            <span className='text-muted-foreground'>Mã phiếu:</span>
            <p className='font-medium'>{shipment.id}</p>
          </div>
          <div>
            <span className='text-muted-foreground'>Trạng thái:</span>
            <div className='mt-1'>{getStatusBadge(shipment.statusId)}</div>
          </div>
          <div>
            <span className='text-muted-foreground'>Ngày tạo:</span>
            <p className='font-medium'>{formatDate(shipment.createdStamp)}</p>
          </div>
          <div>
            <span className='text-muted-foreground'>Ngày giao dự kiến:</span>
            <p className='font-medium'>
              {shipment.expectedDeliveryDate
                ? formatDate(shipment.expectedDeliveryDate)
                : 'Chưa có'}
            </p>
          </div>
          <div>
            <span className='text-muted-foreground'>Kho:</span>
            <p className='font-medium'>{shipment.facilityId || 'Chưa có'}</p>
          </div>
          {shipment.note && (
            <div className='col-span-2'>
              <span className='text-muted-foreground'>Ghi chú:</span>
              <p className='font-medium'>{shipment.note}</p>
            </div>
          )}
        </div>

        <Separator />

        {/* Products List */}
        <div className='space-y-4'>
          <h3 className='text-lg font-medium'>Danh sách sản phẩm</h3>

          <div className='border rounded-lg overflow-hidden'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sản phẩm</TableHead>
                  <TableHead>Số lượng</TableHead>
                  <TableHead>Đơn vị</TableHead>
                  <TableHead>Lô hàng</TableHead>
                  <TableHead>NSX</TableHead>
                  <TableHead>HSD</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {shipment.items?.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{getProductName(item.productId)}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{getProductUnit(item.productId)}</TableCell>
                    <TableCell>{item.lotId || '-'}</TableCell>
                    <TableCell>
                      {item.manufacturingDate
                        ? formatDate(item.manufacturingDate)
                        : '-'}
                    </TableCell>
                    <TableCell>
                      {item.expirationDate
                        ? formatDate(item.expirationDate)
                        : '-'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Đóng
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
