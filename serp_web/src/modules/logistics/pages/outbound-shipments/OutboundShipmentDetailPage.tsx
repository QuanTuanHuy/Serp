/**
 * Outbound Shipment Detail Page - Logistics Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Outbound shipment detail and item management
 */

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  CheckCircle2,
  Clock3,
  Edit,
  Loader2,
  Plus,
  Trash2,
  Truck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useAddItemToOutboundShipmentMutation,
  useDeleteItemFromOutboundShipmentMutation,
  useDeleteOutboundShipmentMutation,
  useGetFacilityQuery,
  useGetOrderQuery,
  useGetOutboundShipmentQuery,
  useGetProductsQuery,
  useReadyToExportOutboundShipmentMutation,
  useUpdateItemInOutboundShipmentMutation,
} from '../../api/logisticsApi';
import type {
  OutboundShipmentItem,
  OutboundShipmentItemForm,
} from '../../types';
import { useGetUsersQuery } from '@/modules/admin/services/users/usersApi';

interface OutboundShipmentDetailPageProps {
  shipmentId: string;
}

interface DetailOption {
  inventoryItemDetailId: string;
  inventoryItemId: string;
  productId: string;
  productName: string;
  lotId: string;
  maxQuantity: number;
}

const STATUS_CONFIG = {
  CREATED: {
    label: 'Nháp',
    color: 'text-blue-700 dark:text-blue-400',
    bgColor: 'bg-blue-100 dark:bg-blue-900/30',
    icon: Clock3,
  },
  READY_TO_EXPORT: {
    label: 'Sẵn sàng xuất',
    color: 'text-amber-700 dark:text-amber-400',
    bgColor: 'bg-amber-100 dark:bg-amber-900/30',
    icon: Truck,
  },
  DELIVERED: {
    label: 'Đã giao',
    color: 'text-emerald-700 dark:text-emerald-400',
    bgColor: 'bg-emerald-100 dark:bg-emerald-900/30',
    icon: CheckCircle2,
  },
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatFullname = (
  user: { firstName?: string; lastName?: string } | undefined
) => {
  if (!user) return 'N/A';
  return `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'N/A';
};

export const OutboundShipmentDetailPage: React.FC<
  OutboundShipmentDetailPageProps
> = ({ shipmentId }) => {
  const router = useRouter();

  const [isProcessing, setIsProcessing] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const [showAddItemDialog, setShowAddItemDialog] = useState(false);
  const [showEditItemDialog, setShowEditItemDialog] = useState(false);
  const [showDeleteItemDialog, setShowDeleteItemDialog] = useState(false);
  const [selectedItem, setSelectedItem] = useState<OutboundShipmentItem | null>(
    null
  );

  const [selectedDetailId, setSelectedDetailId] = useState('');
  const [addQuantity, setAddQuantity] = useState(1);

  const [editQuantity, setEditQuantity] = useState(1);

  const {
    data: shipmentResponse,
    isLoading,
    isError,
  } = useGetOutboundShipmentQuery(shipmentId);

  const shipment = shipmentResponse?.data;

  const [readyToExport, { isLoading: isReadyLoading }] =
    useReadyToExportOutboundShipmentMutation();
  const [deleteShipment, { isLoading: isDeleteLoading }] =
    useDeleteOutboundShipmentMutation();
  const [addItem, { isLoading: isAddItemLoading }] =
    useAddItemToOutboundShipmentMutation();
  const [updateItem, { isLoading: isUpdateItemLoading }] =
    useUpdateItemInOutboundShipmentMutation();
  const [deleteItem, { isLoading: isDeleteItemLoading }] =
    useDeleteItemFromOutboundShipmentMutation();

  const { data: orderResponse } = useGetOrderQuery(shipment?.orderId || '', {
    skip: !shipment?.orderId,
  });

  const { data: facilityResponse } = useGetFacilityQuery(
    shipment?.facilityId || '',
    {
      skip: !shipment?.facilityId,
    }
  );

  const productIds = useMemo(
    () => shipment?.items?.map((item) => item.productId) || [],
    [shipment]
  );

  const { data: productsResponse } = useGetProductsQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    {
      skip: productIds.length === 0,
    }
  );

  const productMap = useMemo(() => {
    const map = new Map<string, string>();
    productsResponse?.data?.items?.forEach((product) => {
      map.set(product.id, product.name);
    });
    return map;
  }, [productsResponse]);

  const userIds = shipment?.createdByUserId ? [shipment.createdByUserId] : [];

  const { data: usersResponse } = useGetUsersQuery(
    {
      page: 0,
      pageSize: 100,
    },
    {
      skip: userIds.length === 0,
    }
  );

  const userMap = useMemo(
    () => new Map(usersResponse?.data?.items?.map((user) => [user.id, user])),
    [usersResponse]
  );

  const order = orderResponse?.data;
  const facility = facilityResponse?.data;

  const detailOptions = useMemo(() => {
    const map = new Map<string, DetailOption>();

    order?.items?.forEach((orderItem) => {
      const productName = orderItem.product?.name || orderItem.productId;

      (orderItem.allocatedInventoryItems || []).forEach((detail) => {
        const maxQuantity = detail.notYetOutboundQuantity ?? detail.quantity;
        if (maxQuantity <= 0) return;

        map.set(detail.id, {
          inventoryItemDetailId: detail.id,
          inventoryItemId: detail.inventoryItemId,
          productId: detail.productId,
          productName,
          lotId: detail.lotId,
          maxQuantity,
        });
      });
    });

    return Array.from(map.values());
  }, [order]);

  const detailMap = useMemo(() => {
    const map = new Map<string, DetailOption>();
    detailOptions.forEach((item) => {
      map.set(item.inventoryItemDetailId, item);
    });
    return map;
  }, [detailOptions]);

  const statusConfig = shipment
    ? STATUS_CONFIG[shipment.status as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.CREATED
    : STATUS_CONFIG.CREATED;

  const StatusIcon = statusConfig.icon;

  const openAddItemDialog = () => {
    setSelectedDetailId('');
    setAddQuantity(1);
    setShowAddItemDialog(true);
  };

  const openEditItemDialog = (item: OutboundShipmentItem) => {
    setSelectedItem(item);
    setEditQuantity(item.quantity);
    setShowEditItemDialog(true);
  };

  const openDeleteItemDialog = (item: OutboundShipmentItem) => {
    setSelectedItem(item);
    setShowDeleteItemDialog(true);
  };

  const handleReadyToExport = async () => {
    if (!shipment) return;

    setIsProcessing(true);
    try {
      await readyToExport({ shipmentId: shipment.id }).unwrap();
      toast.success('Đã chuyển phiếu sang trạng thái sẵn sàng giao');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể chuyển trạng thái phiếu');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDeleteShipment = async () => {
    if (!shipment) return;

    setIsProcessing(true);
    try {
      await deleteShipment(shipment.id).unwrap();
      toast.success('Đã xóa phiếu xuất');
      router.push('/logistics/outbound-shipments');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể xóa phiếu xuất');
      setIsProcessing(false);
    }
  };

  const handleAddItem = async () => {
    if (!shipment) return;

    const detail = detailMap.get(selectedDetailId);

    if (!detail) {
      toast.error('Vui lòng chọn dòng tồn kho');
      return;
    }

    if (addQuantity <= 0 || addQuantity > detail.maxQuantity) {
      toast.error(`Số lượng phải từ 1 đến ${detail.maxQuantity}`);
      return;
    }

    const payload: OutboundShipmentItemForm = {
      inventoryItemDetailId: detail.inventoryItemDetailId,
      quantity: addQuantity,
    };

    try {
      await addItem({ shipmentId: shipment.id, data: payload }).unwrap();
      toast.success('Đã thêm mặt hàng vào phiếu xuất');
      setShowAddItemDialog(false);
      setSelectedDetailId('');
      setAddQuantity(1);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể thêm mặt hàng');
    }
  };

  const handleUpdateItem = async () => {
    if (!shipment || !selectedItem) return;

    const remaining =
      detailMap.get(selectedItem.inventoryItemDetailId)?.maxQuantity || 0;
    const maxQuantity = selectedItem.quantity + remaining;

    if (editQuantity <= 0 || editQuantity > maxQuantity) {
      toast.error(`Số lượng phải từ 1 đến ${maxQuantity}`);
      return;
    }

    try {
      await updateItem({
        shipmentId: shipment.id,
        itemId: selectedItem.id,
        data: { quantity: editQuantity },
      }).unwrap();
      toast.success('Đã cập nhật mặt hàng');
      setShowEditItemDialog(false);
      setSelectedItem(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể cập nhật mặt hàng');
    }
  };

  const handleDeleteItem = async () => {
    if (!shipment || !selectedItem) return;

    try {
      await deleteItem({
        shipmentId: shipment.id,
        itemId: selectedItem.id,
      }).unwrap();
      toast.success('Đã xóa mặt hàng');
      setShowDeleteItemDialog(false);
      setSelectedItem(null);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể xóa mặt hàng');
    }
  };

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <Card className='animate-pulse'>
          <CardContent className='p-8'>
            <div className='mb-4 h-8 w-1/3 rounded bg-muted' />
            <div className='h-5 w-2/3 rounded bg-muted' />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isError || !shipment) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='space-y-4 p-8 text-center'>
          <h3 className='text-lg font-semibold'>Không tìm thấy phiếu xuất</h3>
          <p className='text-muted-foreground'>
            Phiếu xuất không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/logistics/outbound-shipments')}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div className='flex items-start gap-3'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics/outbound-shipments')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>

          <div>
            <div className='mb-2 flex flex-wrap items-center gap-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {shipment.name}
              </h1>
              <Badge
                variant='secondary'
                className={cn('gap-1.5', statusConfig.bgColor)}
              >
                <StatusIcon className={cn('h-3 w-3', statusConfig.color)} />
                <span className={statusConfig.color}>{statusConfig.label}</span>
              </Badge>
            </div>
            <p className='text-sm text-muted-foreground'>
              ID: {shipment.id} • Tạo lúc: {formatDate(shipment.createdStamp)}
            </p>
          </div>
        </div>

        <div className='flex flex-wrap items-center gap-2'>
          {shipment.status === 'CREATED' && (
            <Button
              onClick={handleReadyToExport}
              disabled={isProcessing || isReadyLoading}
            >
              {isReadyLoading ? (
                <>
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  Đang xử lý...
                </>
              ) : (
                <>
                  <Truck className='mr-2 h-4 w-4' />
                  Xuất kho
                </>
              )}
            </Button>
          )}

          <Button
            variant='outline'
            onClick={() =>
              router.push(`/logistics/outbound-shipments/${shipment.id}/edit`)
            }
            disabled={shipment.status !== 'CREATED'}
          >
            <Edit className='mr-2 h-4 w-4' />
            Đổi tên phiếu
          </Button>

          <Button
            variant='outline'
            className='text-destructive hover:text-destructive'
            onClick={() => setShowDeleteDialog(true)}
            disabled={shipment.status !== 'CREATED' || isProcessing}
          >
            <Trash2 className='mr-2 h-4 w-4' />
            Xóa phiếu
          </Button>
        </div>
      </div>

      <div className='grid gap-6 lg:grid-cols-3'>
        <div className='space-y-6 lg:col-span-2'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Thông tin tổng quan</h3>
            </CardHeader>
            <CardContent className='grid gap-4 sm:grid-cols-2'>
              <div>
                <Label className='text-muted-foreground'>Đơn hàng</Label>
                <p className='font-medium'>
                  {order?.orderName || shipment.orderId}
                </p>
              </div>
              <div>
                <Label className='text-muted-foreground'>Kho xuất</Label>
                <p className='font-medium'>
                  {facility?.name ||
                    shipment.facility?.name ||
                    shipment.facilityId}
                </p>
              </div>
              <div>
                <Label className='text-muted-foreground'>Người tạo</Label>
                <p>{formatFullname(userMap.get(shipment.createdByUserId))}</p>
              </div>
              <div>
                <Label className='text-muted-foreground'>
                  Cập nhật lần cuối
                </Label>
                <p>{formatDate(shipment.lastUpdatedStamp)}</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <h3 className='font-semibold'>Danh sách mặt hàng</h3>
                {shipment.status === 'CREATED' && (
                  <Button size='sm' onClick={openAddItemDialog}>
                    <Plus className='mr-2 h-4 w-4' />
                    Thêm mặt hàng
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {(shipment.items || []).length > 0 ? (
                <div className='space-y-3'>
                  {(shipment.items || []).map((item) => {
                    const detail = detailMap.get(item.inventoryItemDetailId);

                    return (
                      <div
                        key={item.id}
                        className='flex flex-col gap-3 rounded-lg border p-4 md:flex-row md:items-center md:justify-between'
                      >
                        <div>
                          <p className='font-medium'>
                            {productMap.get(item.productId) || item.productId}
                          </p>
                          <p className='text-sm text-muted-foreground'>
                            Số lượng: {item.quantity}
                          </p>
                        </div>

                        {shipment.status === 'CREATED' && (
                          <div className='flex items-center gap-2'>
                            <Button
                              size='sm'
                              variant='ghost'
                              onClick={() => openEditItemDialog(item)}
                            >
                              <Edit className='h-4 w-4' />
                            </Button>
                            <Button
                              size='sm'
                              variant='ghost'
                              className='text-destructive hover:text-destructive'
                              onClick={() => openDeleteItemDialog(item)}
                            >
                              <Trash2 className='h-4 w-4' />
                            </Button>
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className='rounded-lg border border-dashed py-12 text-center text-muted-foreground'>
                  Chưa có mặt hàng trong phiếu xuất
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <Card className='h-fit'>
          <CardHeader>
            <h3 className='font-semibold'>Thống kê</h3>
          </CardHeader>
          <CardContent className='space-y-3 text-sm'>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Số mặt hàng</span>
              <span className='font-medium'>{shipment.items?.length || 0}</span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Tổng số lượng</span>
              <span className='font-medium'>
                {(shipment.items || []).reduce(
                  (sum, item) => sum + item.quantity,
                  0
                )}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận xóa phiếu xuất</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa phiếu {shipment.name}. Hành động này không thể hoàn
              tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleteLoading}>
              Hủy
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteShipment}
              disabled={isDeleteLoading}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isDeleteLoading ? 'Đang xóa...' : 'Xóa phiếu'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <Dialog open={showAddItemDialog} onOpenChange={setShowAddItemDialog}>
        <DialogContent className='sm:max-w-[620px]'>
          <DialogHeader>
            <DialogTitle>Thêm mặt hàng</DialogTitle>
            <DialogDescription>
              Chọn dòng tồn kho đã allocate từ đơn hàng để thêm vào phiếu xuất.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4 py-2'>
            <div className='space-y-2'>
              <Label>Dòng tồn kho</Label>
              <select
                value={selectedDetailId}
                onChange={(event) => setSelectedDetailId(event.target.value)}
                className='w-full rounded-md border bg-background px-3 py-2 text-sm'
              >
                <option value=''>Chọn dòng tồn kho</option>
                {detailOptions.map((option) => (
                  <option
                    key={option.inventoryItemDetailId}
                    value={option.inventoryItemDetailId}
                  >
                    {option.productName} | Lot {option.lotId} | Có thể xuất:{' '}
                    {option.maxQuantity}
                  </option>
                ))}
              </select>
            </div>

            <div className='space-y-2'>
              <Label>Số lượng</Label>
              <Input
                type='number'
                min={1}
                max={detailMap.get(selectedDetailId)?.maxQuantity || 1}
                value={addQuantity}
                onChange={(event) =>
                  setAddQuantity(Number(event.target.value) || 1)
                }
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowAddItemDialog(false)}
              disabled={isAddItemLoading}
            >
              Hủy
            </Button>
            <Button onClick={handleAddItem} disabled={isAddItemLoading}>
              {isAddItemLoading ? 'Đang thêm...' : 'Thêm mặt hàng'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showEditItemDialog} onOpenChange={setShowEditItemDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật số lượng</DialogTitle>
            <DialogDescription>
              Điều chỉnh số lượng cho mặt hàng đã chọn.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-2 py-2'>
            <Label>Số lượng mới</Label>
            <Input
              type='number'
              min={1}
              max={
                selectedItem
                  ? selectedItem.quantity +
                    (detailMap.get(selectedItem.inventoryItemDetailId)
                      ?.maxQuantity || 0)
                  : 1
              }
              value={editQuantity}
              onChange={(event) =>
                setEditQuantity(Number(event.target.value) || 1)
              }
            />
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowEditItemDialog(false)}
              disabled={isUpdateItemLoading}
            >
              Hủy
            </Button>
            <Button onClick={handleUpdateItem} disabled={isUpdateItemLoading}>
              {isUpdateItemLoading ? 'Đang cập nhật...' : 'Lưu thay đổi'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog
        open={showDeleteItemDialog}
        onOpenChange={setShowDeleteItemDialog}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa mặt hàng</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn xóa mặt hàng này khỏi phiếu xuất?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleteItemLoading}>
              Hủy
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteItem}
              disabled={isDeleteItemLoading}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isDeleteItemLoading ? 'Đang xóa...' : 'Xóa mặt hàng'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default OutboundShipmentDetailPage;
