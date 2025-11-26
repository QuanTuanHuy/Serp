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
import { Package, Plus, PackageCheck, Trash2 } from 'lucide-react';
import { formatDate } from '@/shared/utils';
import { useShipments } from '../../hooks/useShipments';
import { ShipmentFormDialog } from './ShipmentFormDialog';
import { ShipmentDetailDialog } from './ShipmentDetailDialog';
import type { OrderDetail } from '../../types';

interface OrderShipmentsTabProps {
  order: OrderDetail;
}

export const OrderShipmentsTab: React.FC<OrderShipmentsTabProps> = ({
  order,
}) => {
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedShipmentId, setSelectedShipmentId] = useState<string | null>(
    null
  );
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [shipmentToDelete, setShipmentToDelete] = useState<string | null>(null);

  const {
    shipments,
    isLoadingShipments,
    canEdit,
    handleCreateShipment,
    handleUpdateShipment,
    handleDeleteShipment,
    handleImportShipment,
    handleAddItem,
    handleUpdateItem,
    handleDeleteItem,
    handleUpdateFacility,
    isCreating,
    isDeleting,
    isImporting,
  } = useShipments(order.id);

  const showActions = canEdit && order.statusId === 'APPROVED';

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

  const handleDeleteClick = (shipmentId: string) => {
    setShipmentToDelete(shipmentId);
    setDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (shipmentToDelete) {
      await handleDeleteShipment(shipmentToDelete);
      setDeleteDialogOpen(false);
      setShipmentToDelete(null);
    }
  };

  const handleImportClick = async (shipmentId: string) => {
    await handleImportShipment(shipmentId);
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
          <div className='flex items-center justify-between'>
            <CardTitle>Danh sách phiếu nhập</CardTitle>
            {showActions && (
              <Button
                size='sm'
                onClick={() => setCreateDialogOpen(true)}
                disabled={isCreating}
              >
                <Plus className='h-4 w-4 mr-2' />
                Tạo phiếu nhập
              </Button>
            )}
          </div>
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
                Bắt đầu bằng cách tạo phiếu nhập đầu tiên cho đơn hàng này
              </p>
              {showActions && (
                <Button size='sm' onClick={() => setCreateDialogOpen(true)}>
                  <Plus className='h-4 w-4 mr-2' />
                  Tạo phiếu nhập
                </Button>
              )}
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
                    <TableHead className='text-right'>Thao tác</TableHead>
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
                      <TableCell className='text-right'>
                        <div className='flex items-center justify-end gap-2'>
                          {canEdit &&
                            order.statusId === 'READY_FOR_DELIVERY' &&
                            shipment.statusId === 'READY' && (
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={() => handleImportClick(shipment.id)}
                                disabled={isImporting}
                                title='Nhập hàng'
                              >
                                <PackageCheck className='h-4 w-4 text-green-600' />
                              </Button>
                            )}
                          {showActions && (
                            <Button
                              size='sm'
                              variant='ghost'
                              onClick={() => handleDeleteClick(shipment.id)}
                              disabled={isDeleting}
                              title='Xóa phiếu nhập'
                            >
                              <Trash2 className='h-4 w-4 text-destructive' />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create Shipment Dialog */}
      <ShipmentFormDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        order={order}
        existingShipments={shipments}
        onSubmit={handleCreateShipment}
        isLoading={isCreating}
      />

      {/* Shipment Detail Dialog */}
      <ShipmentDetailDialog
        open={detailDialogOpen}
        onOpenChange={setDetailDialogOpen}
        shipmentId={selectedShipmentId}
        order={order}
        canEdit={canEdit}
        existingShipments={shipments}
        onUpdateShipment={handleUpdateShipment}
        onUpdateFacility={handleUpdateFacility}
        onAddItem={handleAddItem}
        onUpdateItem={handleUpdateItem}
        onDeleteItem={handleDeleteItem}
      />

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Xác nhận xóa phiếu nhập</DialogTitle>
            <DialogDescription>
              Bạn có chắc chắn muốn xóa phiếu nhập này? Hành động này không thể
              hoàn tác.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setDeleteDialogOpen(false)}
            >
              Hủy
            </Button>
            <Button
              onClick={handleConfirmDelete}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              Xóa
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
};
