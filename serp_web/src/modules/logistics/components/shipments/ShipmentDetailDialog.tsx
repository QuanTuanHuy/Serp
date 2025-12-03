/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment Detail Dialog Component
*/

'use client';

import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui';
import {
  Button,
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { Package, Edit, Plus, Trash2, FileUp, Loader2 } from 'lucide-react';
import {
  useGetShipmentQuery,
  useImportShipmentMutation,
  useDeleteItemFromShipmentMutation,
} from '../../services';
import { useNotification } from '@/shared/hooks';
import { useProducts, useFacilities } from '../../hooks';
import { EditShipmentDialog } from './EditShipmentDialog';
import { AddItemDialog } from './AddItemDialog';
import { EditItemDialog } from './EditItemDialog';
import type { InventoryItemDetail } from '../../types';

interface ShipmentDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shipmentId: string;
}

export const ShipmentDetailDialog: React.FC<ShipmentDetailDialogProps> = ({
  open,
  onOpenChange,
  shipmentId,
}) => {
  const notification = useNotification();
  const { products } = useProducts();
  const { facilities } = useFacilities();
  const { data: shipment, isLoading } = useGetShipmentQuery(shipmentId, {
    skip: !open,
  });
  const [importShipment, { isLoading: isImporting }] =
    useImportShipmentMutation();
  const [deleteItem, { isLoading: isDeleting }] =
    useDeleteItemFromShipmentMutation();

  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [addItemDialogOpen, setAddItemDialogOpen] = useState(false);
  const [editItemDialogOpen, setEditItemDialogOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<InventoryItemDetail | null>(
    null
  );

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  const formatDateTime = (dateTimeString?: string) => {
    if (!dateTimeString) return '-';
    return new Date(dateTimeString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadgeVariant = (
    statusId?: string
  ): 'default' | 'destructive' | 'outline' | 'secondary' => {
    switch (statusId?.toUpperCase()) {
      case 'CREATED':
        return 'default';
      case 'IMPORTED':
        return 'secondary';
      default:
        return 'secondary';
    }
  };

  const handleImport = async () => {
    try {
      await importShipment(shipmentId).unwrap();
      notification.success('Thành công', {
        description: 'Nhập hàng thành công',
      });
      onOpenChange(false);
    } catch (error: any) {
      notification.error('Lỗi', {
        description: error?.data?.message || 'Có lỗi xảy ra khi nhập hàng',
      });
    }
  };

  const handleEditItem = (item: InventoryItemDetail) => {
    setSelectedItem(item);
    setEditItemDialogOpen(true);
  };

  const handleDeleteItem = async (itemId: string) => {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
      return;
    }

    try {
      await deleteItem({ shipmentId, itemId }).unwrap();
      notification.success('Thành công', {
        description: 'Xóa sản phẩm thành công',
      });
    } catch (error: any) {
      notification.error('Lỗi', {
        description: error?.data?.message || 'Có lỗi xảy ra khi xóa sản phẩm',
      });
    }
  };

  const canImport = shipment?.statusId === 'CREATED';

  const getProductName = (productId: string) => {
    const product = products?.find((p) => p.id === productId);
    return product?.name || productId.substring(0, 12);
  };

  const getFacilityName = (facilityId: string) => {
    const facility = facilities?.find((f) => f.id === facilityId);
    return facility?.name || facilityId.substring(0, 12);
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className='!max-w-6xl max-h-[90vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle className='flex items-center gap-2'>
              <Package className='h-5 w-5' />
              Chi tiết phiếu{' '}
              {shipment?.shipmentTypeId === 'INBOUND' ? 'nhập' : 'xuất'}
            </DialogTitle>
          </DialogHeader>

          {isLoading ? (
            <div className='flex items-center justify-center py-12'>
              <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
            </div>
          ) : shipment ? (
            <div className='space-y-6'>
              {/* Thông tin cơ bản */}
              <Card>
                <CardHeader>
                  <div className='flex items-center justify-between'>
                    <CardTitle className='text-base'>
                      Thông tin cơ bản
                    </CardTitle>
                    <Button
                      size='sm'
                      variant='outline'
                      onClick={() => setEditDialogOpen(true)}
                    >
                      <Edit className='h-3.5 w-3.5 mr-1' />
                      Sửa
                    </Button>
                  </div>
                </CardHeader>
                <CardContent className='grid grid-cols-2 gap-4'>
                  <div>
                    <p className='text-sm text-muted-foreground'>Mã phiếu</p>
                    <p className='font-mono text-sm'>
                      {shipment.id.substring(0, 16)}
                    </p>
                  </div>
                  <div>
                    <p className='text-sm text-muted-foreground'>Tên phiếu</p>
                    <p className='text-sm font-medium'>
                      {shipment.shipmentName || '-'}
                    </p>
                  </div>
                  <div>
                    <p className='text-sm text-muted-foreground'>Loại phiếu</p>
                    <p className='text-sm'>
                      {shipment.shipmentTypeId === 'INBOUND'
                        ? 'Nhập hàng'
                        : 'Xuất hàng'}
                    </p>
                  </div>
                  <div>
                    <p className='text-sm text-muted-foreground'>Trạng thái</p>
                    <Badge variant={getStatusBadgeVariant(shipment.statusId)}>
                      {shipment.statusId || 'N/A'}
                    </Badge>
                  </div>
                  <div>
                    <p className='text-sm text-muted-foreground'>
                      Ngày giao dự kiến
                    </p>
                    <p className='text-sm'>
                      {formatDate(shipment.expectedDeliveryDate)}
                    </p>
                  </div>
                  <div>
                    <p className='text-sm text-muted-foreground'>Ngày tạo</p>
                    <p className='text-sm'>
                      {formatDateTime(shipment.createdStamp)}
                    </p>
                  </div>
                  <div>
                    <p className='text-sm text-muted-foreground'>
                      Cập nhật lần cuối
                    </p>
                    <p className='text-sm'>
                      {formatDateTime(shipment.lastUpdatedStamp)}
                    </p>
                  </div>
                  {shipment.note && (
                    <div className='col-span-2'>
                      <p className='text-sm text-muted-foreground'>Ghi chú</p>
                      <p className='text-sm'>{shipment.note}</p>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Danh sách sản phẩm */}
              <Card>
                <CardHeader>
                  <div className='flex items-center justify-between'>
                    <CardTitle className='text-base'>
                      Danh sách sản phẩm ({shipment.items?.length || 0})
                    </CardTitle>
                    {canImport && (
                      <Button
                        size='sm'
                        variant='outline'
                        onClick={() => setAddItemDialogOpen(true)}
                      >
                        <Plus className='h-3.5 w-3.5 mr-1' />
                        Thêm sản phẩm
                      </Button>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  {!shipment.items || shipment.items.length === 0 ? (
                    <div className='text-center py-8 text-muted-foreground'>
                      Chưa có sản phẩm nào
                    </div>
                  ) : (
                    <div className='space-y-3'>
                      {shipment.items.map((item) => (
                        <div
                          key={item.id}
                          className='flex items-start gap-4 p-3 border rounded-lg'
                        >
                          <div className='flex-1 grid grid-cols-4 gap-4'>
                            <div>
                              <p className='text-xs text-muted-foreground'>
                                Sản phẩm
                              </p>
                              <p className='text-sm font-medium'>
                                {getProductName(item.productId)}
                              </p>
                            </div>
                            <div>
                              <p className='text-xs text-muted-foreground'>
                                Số lượng
                              </p>
                              <p className='text-sm'>
                                {item.quantity} {item.unit}
                              </p>
                            </div>
                            <div>
                              <p className='text-xs text-muted-foreground'>
                                Lot ID
                              </p>
                              <p className='text-sm'>{item.lotId || '-'}</p>
                            </div>
                            <div>
                              <p className='text-xs text-muted-foreground'>
                                Kho
                              </p>
                              <p className='text-sm font-medium'>
                                {getFacilityName(item.facilityId)}
                              </p>
                            </div>
                          </div>
                          {canImport && (
                            <div className='flex items-center gap-2 ml-4'>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={() => handleEditItem(item)}
                              >
                                <Edit className='h-3.5 w-3.5' />
                              </Button>
                              <Button
                                size='sm'
                                variant='ghost'
                                className='text-destructive hover:text-destructive'
                                onClick={() => handleDeleteItem(item.id)}
                                disabled={isDeleting}
                              >
                                <Trash2 className='h-3.5 w-3.5' />
                              </Button>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          ) : null}

          <DialogFooter>
            <Button variant='outline' onClick={() => onOpenChange(false)}>
              Đóng
            </Button>
            {canImport && (
              <Button onClick={handleImport} disabled={isImporting}>
                {isImporting && (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                )}
                <FileUp className='mr-2 h-4 w-4' />
                Nhập hàng
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Shipment Dialog */}
      {shipment && (
        <EditShipmentDialog
          open={editDialogOpen}
          onOpenChange={setEditDialogOpen}
          shipment={shipment}
        />
      )}

      {/* Add Item Dialog */}
      {shipment && (
        <AddItemDialog
          open={addItemDialogOpen}
          onOpenChange={setAddItemDialogOpen}
          shipmentId={shipmentId}
          orderId={shipment.orderId}
        />
      )}

      {/* Edit Item Dialog */}
      {selectedItem && (
        <EditItemDialog
          open={editItemDialogOpen}
          onOpenChange={setEditItemDialogOpen}
          shipmentId={shipmentId}
          item={selectedItem}
        />
      )}
    </>
  );
};
