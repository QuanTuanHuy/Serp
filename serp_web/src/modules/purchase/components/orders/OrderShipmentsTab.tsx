/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Shipments Tab Component
*/

'use client';

import React, { useState } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Button,
  Badge,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Package } from 'lucide-react';
import { formatDate } from '@/shared/utils';
import { useShipments } from '../../hooks/useShipments';
import { ShipmentDetailDialog } from './ShipmentDetailDialog';
import type { OrderDetail } from '../../types';

interface OrderShipmentsTabProps {
  order: OrderDetail;
}

export const OrderShipmentsTab: React.FC<OrderShipmentsTabProps> = ({
  order,
}) => {
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedShipmentId, setSelectedShipmentId] = useState<string | null>(
    null
  );

  const { shipments, isLoadingShipments } = useShipments(order.id);

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

  const handleViewDetail = (shipmentId: string) => {
    setSelectedShipmentId(shipmentId);
    setDetailDialogOpen(true);
  };

  // Show placeholder if order is not approved
  if (order.statusId !== 'APPROVED' && shipments.length === 0) {
    return (
      <Card>
        <CardContent className='p-8'>
          <div className='flex flex-col items-center justify-center space-y-4 text-center'>
            <div className='rounded-full bg-muted p-4'>
              <Package className='h-8 w-8 text-muted-foreground' />
            </div>
            <div>
              <h3 className='text-lg font-semibold'>Phiếu nhập hàng</h3>
              <p className='text-sm text-muted-foreground mt-2'>
                Đơn hàng cần được phê duyệt trước khi tạo phiếu nhập
              </p>
            </div>
            <p className='text-xs text-muted-foreground italic'>
              Trạng thái hiện tại: {order.statusId}
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Danh sách phiếu nhập</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoadingShipments ? (
            <div className='flex items-center justify-center p-8'>
              <div className='text-center space-y-2'>
                <Package className='h-8 w-8 mx-auto animate-pulse text-muted-foreground' />
                <p className='text-sm text-muted-foreground'>Đang tải...</p>
              </div>
            </div>
          ) : shipments.length === 0 ? (
            <div className='flex flex-col items-center justify-center p-8 text-center'>
              <div className='rounded-full bg-muted p-4 mb-4'>
                <Package className='h-8 w-8 text-muted-foreground' />
              </div>
              <h3 className='text-lg font-semibold mb-2'>Chưa có phiếu nhập</h3>
              <p className='text-sm text-muted-foreground mb-4'>
                Chờ phiếu nhập được tạo từ lãnh đạo
              </p>
            </div>
          ) : (
            <div className='border rounded-lg overflow-hidden'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã phiếu</TableHead>
                    <TableHead>Tên phiếu</TableHead>
                    <TableHead>Trạng thái</TableHead>
                    <TableHead>Ngày tạo</TableHead>
                    <TableHead>Ngày giao dự kiến</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {shipments.map((shipment) => (
                    <TableRow key={shipment.id}>
                      <TableCell className='font-mono text-sm'>
                        {shipment.id}
                      </TableCell>
                      <TableCell>
                        <button
                          onClick={() => handleViewDetail(shipment.id)}
                          className='text-primary hover:underline font-medium'
                        >
                          {shipment.shipmentName}
                        </button>
                      </TableCell>
                      <TableCell>{getStatusBadge(shipment.statusId)}</TableCell>
                      <TableCell>{formatDate(shipment.createdStamp)}</TableCell>
                      <TableCell>
                        {shipment.expectedDeliveryDate
                          ? formatDate(shipment.expectedDeliveryDate)
                          : '-'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Shipment Detail Dialog */}
      <ShipmentDetailDialog
        open={detailDialogOpen}
        onOpenChange={setDetailDialogOpen}
        shipmentId={selectedShipmentId}
        order={order}
      />
    </>
  );
};
