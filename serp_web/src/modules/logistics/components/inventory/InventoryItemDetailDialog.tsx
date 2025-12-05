/*
Author: QuanTuanHuy
Description: Part of Serp Project - Inventory Item Detail Dialog
*/

'use client';

import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Badge,
} from '@/shared/components/ui';
import { Separator } from '@/shared/components/ui/separator';
import { useGetInventoryItemQuery } from '../../services/inventoryItemApi';
import { useProducts, useFacilities, useInventoryItems } from '../../hooks';
import { InventoryItemEditDialog } from './InventoryItemEditDialog';
import { formatDate } from '@/shared/utils';
import { toast } from 'sonner';
import type { UpdateInventoryItemRequest } from '../../types';

interface InventoryItemDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  inventoryItemId: string | null;
}

export const InventoryItemDetailDialog: React.FC<
  InventoryItemDetailDialogProps
> = ({ open, onOpenChange, inventoryItemId }) => {
  const [editDialogOpen, setEditDialogOpen] = useState(false);

  const { data: inventoryItem, isLoading } = useGetInventoryItemQuery(
    inventoryItemId || '',
    { skip: !inventoryItemId }
  );

  const { products } = useProducts();
  const { facilities } = useFacilities();
  const { update, isUpdating } = useInventoryItems();

  const getProductName = (productId: string) => {
    const product = products?.find((p) => p.id === productId);
    return product?.name || productId;
  };

  const getFacilityName = (facilityId: string) => {
    const facility = facilities?.find((f) => f.id === facilityId);
    return facility?.name || facilityId;
  };

  const handleEditSubmit = async (
    itemId: string,
    data: UpdateInventoryItemRequest
  ) => {
    try {
      const result = await update(itemId, data);
      if (result.success) {
        toast.success('Cập nhật tồn kho thành công');
        setEditDialogOpen(false);
      } else {
        toast.error('Cập nhật tồn kho thất bại');
      }
    } catch (error) {
      toast.error('Cập nhật tồn kho thất bại');
    }
  };

  const getStatusBadge = (statusId: string) => {
    const statusMap: Record<string, { label: string; variant: any }> = {
      AVAILABLE: { label: 'Có sẵn', variant: 'success' },
      RESERVED: { label: 'Đã đặt', variant: 'warning' },
      IN_TRANSIT: { label: 'Đang vận chuyển', variant: 'default' },
      DAMAGED: { label: 'Hỏng', variant: 'destructive' },
      EXPIRED: { label: 'Hết hạn', variant: 'destructive' },
    };

    const status = statusMap[statusId] || {
      label: statusId,
      variant: 'secondary',
    };
    return <Badge variant={status.variant}>{status.label}</Badge>;
  };

  if (isLoading) {
    return (
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className='w-full max-w-[90vw] md:max-w-[85vw] lg:max-w-[80vw]'>
          <DialogHeader>
            <DialogTitle>Chi tiết tồn kho</DialogTitle>
          </DialogHeader>
          <div className='flex items-center justify-center py-8'>
            <p className='text-sm text-muted-foreground'>Đang tải...</p>
          </div>
        </DialogContent>
      </Dialog>
    );
  }

  if (!inventoryItem) return null;

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className='w-full max-w-[90vw] md:max-w-[85vw] lg:max-w-[80vw] max-h-[90vh] overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>Chi tiết tồn kho</DialogTitle>
          </DialogHeader>

          {/* Basic Information */}
          <div className='space-y-4'>
            <div className='grid grid-cols-3 gap-4 text-sm'>
              <div>
                <span className='text-muted-foreground'>Mã tồn kho:</span>
                <p className='font-mono font-medium mt-1'>{inventoryItem.id}</p>
              </div>
              <div>
                <span className='text-muted-foreground'>Trạng thái:</span>
                <div className='mt-1'>
                  {getStatusBadge(inventoryItem.statusId)}
                </div>
              </div>
              <div>
                <span className='text-muted-foreground'>Sản phẩm:</span>
                <p className='font-medium mt-1'>
                  {getProductName(inventoryItem.productId)}
                </p>
              </div>
              <div>
                <span className='text-muted-foreground'>Số lượng:</span>
                <p className='font-semibold text-lg mt-1'>
                  {inventoryItem.quantity}
                </p>
              </div>
              <div>
                <span className='text-muted-foreground'>Kho:</span>
                <p className='font-medium mt-1'>
                  {getFacilityName(inventoryItem.facilityId)}
                </p>
              </div>
              <div>
                <span className='text-muted-foreground'>Lô hàng:</span>
                <p className='font-medium mt-1'>
                  {inventoryItem.lotId || 'Không có'}
                </p>
              </div>
            </div>

            <Separator />

            {/* Dates Information */}
            <div>
              <h3 className='text-sm font-semibold mb-3'>
                Thông tin ngày tháng
              </h3>
              <div className='grid grid-cols-4 gap-4 text-sm'>
                <div>
                  <span className='text-muted-foreground'>Ngày nhận:</span>
                  <p className='font-medium mt-1'>
                    {inventoryItem.receivedDate
                      ? formatDate(inventoryItem.receivedDate)
                      : 'Chưa có'}
                  </p>
                </div>
                <div>
                  <span className='text-muted-foreground'>Ngày sản xuất:</span>
                  <p className='font-medium mt-1'>
                    {inventoryItem.manufacturingDate
                      ? formatDate(inventoryItem.manufacturingDate)
                      : 'Chưa có'}
                  </p>
                </div>
                <div>
                  <span className='text-muted-foreground'>Ngày hết hạn:</span>
                  <p className='font-medium mt-1'>
                    {inventoryItem.expirationDate
                      ? formatDate(inventoryItem.expirationDate)
                      : 'Chưa có'}
                  </p>
                </div>
                <div>
                  <span className='text-muted-foreground'>Ngày tạo:</span>
                  <p className='font-medium mt-1'>
                    {formatDate(inventoryItem.createdStamp)}
                  </p>
                </div>
              </div>
            </div>

            <Separator />

            {/* System Information */}
            <div>
              <h3 className='text-sm font-semibold mb-3'>Thông tin hệ thống</h3>
              <div className='grid grid-cols-2 gap-4 text-sm'>
                <div>
                  <span className='text-muted-foreground'>
                    Cập nhật lần cuối:
                  </span>
                  <p className='font-medium mt-1'>
                    {formatDate(inventoryItem.lastUpdatedStamp)}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={() => onOpenChange(false)}>
              Đóng
            </Button>
            <Button onClick={() => setEditDialogOpen(true)}>Chỉnh sửa</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <InventoryItemEditDialog
        open={editDialogOpen}
        onOpenChange={setEditDialogOpen}
        inventoryItem={inventoryItem}
        onSubmit={handleEditSubmit}
        isLoading={isUpdating}
      />
    </>
  );
};
