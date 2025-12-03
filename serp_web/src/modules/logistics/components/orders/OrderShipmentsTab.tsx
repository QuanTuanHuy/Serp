/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Shipments Tab
*/

'use client';

import React, { useMemo, useState } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Badge,
  Button,
} from '@/shared/components/ui';
import { DataTable } from '@/shared/components';
import { useShipments } from '../../hooks';
import { useAuth } from '@/modules/account/hooks';
import { useGetOrderQuery, useImportShipmentMutation } from '../../services';
import { useNotification } from '@/shared/hooks';
import { CreateShipmentDialog, ShipmentDetailDialog } from '../shipments';
import { Package, FileUp, Plus } from 'lucide-react';

interface OrderShipmentsTabProps {
  orderId: string;
}

export const OrderShipmentsTab: React.FC<OrderShipmentsTabProps> = ({
  orderId,
}) => {
  // Get shipments filtered by orderId
  const { shipments, isLoadingShipments } = useShipments();
  const { user } = useAuth();
  const notification = useNotification();
  const [importShipment] = useImportShipmentMutation();

  // State for dialogs
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedShipmentId, setSelectedShipmentId] = useState<string | null>(
    null
  );

  // Fetch order details for create dialog
  const { data: orderDetail } = useGetOrderQuery(orderId);

  // Filter shipments by orderId
  const orderShipments = useMemo(() => {
    return shipments.filter((shipment) => shipment.orderId === orderId);
  }, [shipments, orderId]);

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

  // Check if user has logistics role
  const hasLogisticsAccess = useMemo(() => {
    if (!user?.roles) return false;
    return user.roles.some(
      (role) => role === 'LOGISTICS_ADMIN' || role === 'LOGISTICS_EMPLOYEE'
    );
  }, [user]);

  const handleImport = async (shipmentId: string) => {
    try {
      await importShipment(shipmentId).unwrap();
      notification.success('Thành công', {
        description: 'Nhập hàng thành công',
      });
    } catch (error: any) {
      notification.error('Lỗi', {
        description: error?.data?.message || 'Có lỗi xảy ra khi nhập hàng',
      });
    }
  };

  const handleViewDetail = (shipmentId: string) => {
    setSelectedShipmentId(shipmentId);
    setDetailDialogOpen(true);
  };

  const getStatusBadgeVariant = (
    statusId?: string
  ): 'default' | 'destructive' | 'outline' | 'secondary' => {
    switch (statusId?.toUpperCase()) {
      case 'CREATED':
        return 'default';
      case 'IN_TRANSIT':
        return 'outline';
      case 'DELIVERED':
        return 'secondary';
      case 'CANCELLED':
        return 'destructive';
      default:
        return 'secondary';
    }
  };

  const columns = useMemo(
    () => [
      {
        id: 'id',
        header: 'Mã phiếu',
        accessor: 'id',
        cell: ({ row }: any) => (
          <div className='font-mono text-xs'>{row.id.substring(0, 12)}</div>
        ),
      },
      {
        id: 'shipmentName',
        header: 'Tên phiếu',
        accessor: 'shipmentName',
        cell: ({ row }: any) => (
          <button
            onClick={() => handleViewDetail(row.id)}
            className='text-sm font-medium text-primary hover:underline'
          >
            {row.shipmentName || row.id.substring(0, 12)}
          </button>
        ),
      },
      {
        id: 'statusId',
        header: 'Trạng thái',
        accessor: 'statusId',
        cell: ({ row }: any) => (
          <Badge
            variant={getStatusBadgeVariant(row.statusId)}
            className='text-xs'
          >
            {row.statusId || 'N/A'}
          </Badge>
        ),
      },
      {
        id: 'createdStamp',
        header: 'Ngày tạo',
        accessor: 'createdStamp',
        cell: ({ row }: any) => (
          <div className='text-sm'>{formatDateTime(row.createdStamp)}</div>
        ),
      },
      {
        id: 'expectedDeliveryDate',
        header: 'Ngày giao dự kiến',
        accessor: 'expectedDeliveryDate',
        cell: ({ row }: any) => (
          <div className='text-sm'>{formatDate(row.expectedDeliveryDate)}</div>
        ),
      },
      ...(hasLogisticsAccess
        ? [
            {
              id: 'actions',
              header: 'Thao tác',
              accessor: 'id',
              cell: ({ row }: any) =>
                row.statusId === 'CREATED' ? (
                  <Button
                    size='sm'
                    variant='outline'
                    onClick={() => handleImport(row.id)}
                    className='h-8 gap-1'
                  >
                    <FileUp className='h-3.5 w-3.5' />
                    Nhập hàng
                  </Button>
                ) : null,
            },
          ]
        : []),
    ],
    [hasLogisticsAccess]
  );

  return (
    <div className='space-y-6'>
      <Card>
        <CardHeader>
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-2'>
              <CardTitle className='text-lg flex items-center gap-2'>
                <Package className='h-5 w-5' />
                Danh sách phiếu nhập
              </CardTitle>
              <Badge variant='secondary'>
                {orderShipments.length} phiếu nhập
              </Badge>
            </div>
            {hasLogisticsAccess && (
              <Button
                size='sm'
                onClick={() => setCreateDialogOpen(true)}
                className='gap-1'
              >
                <Plus className='h-4 w-4' />
                Tạo phiếu nhập
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {orderShipments.length === 0 && !isLoadingShipments ? (
            <div className='text-center py-12 text-muted-foreground'>
              <Package className='h-12 w-12 mx-auto mb-4 opacity-50' />
              <p className='text-sm'>Chưa có phiếu nhập nào cho đơn hàng này</p>
            </div>
          ) : (
            <DataTable
              columns={columns}
              data={orderShipments}
              isLoading={isLoadingShipments}
              keyExtractor={(row) => row.id}
            />
          )}
        </CardContent>
      </Card>

      {/* Create Shipment Dialog */}
      {orderDetail && (
        <CreateShipmentDialog
          open={createDialogOpen}
          onOpenChange={setCreateDialogOpen}
          order={orderDetail}
        />
      )}

      {/* Shipment Detail Dialog */}
      {selectedShipmentId && (
        <ShipmentDetailDialog
          open={detailDialogOpen}
          onOpenChange={setDetailDialogOpen}
          shipmentId={selectedShipmentId}
        />
      )}
    </div>
  );
};
