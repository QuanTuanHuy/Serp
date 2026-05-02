/**
 * Delivery Slip Detail Page - Logistics2 Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Delivery slip details with item management
 */

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Clock3,
  Edit,
  ExternalLink,
  Loader2,
  Package,
  Plus,
  Redo2Icon,
  Trash2,
  Truck,
} from 'lucide-react';
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
import { getErrorMessage } from '@/lib/store/api';
import { cn } from '@/shared/utils';
import {
  useAddDeliverySlipItemMutation,
  useGetDeliverySlipDetailQuery,
  useGetOutboundShipmentDetailQuery,
  useRemoveDeliverySlipItemMutation,
  useReturnDeliverySlipMutation,
  useUpdateDeliverySlipItemMutation,
} from '../../api/logistics2Api';
import type {
  DeliveryItem,
  DeliverySlipStatus,
  InventoryItem,
  OutboundShipmentItem,
} from '../../types';
import { useGetUsersQuery } from '@/modules/admin/services/users/usersApi';

interface DeliverySlipDetailPageProps {
  slipId: string;
}

interface ShipmentItemOption {
  outboundShipmentItemId: string;
  inventoryItemId: string;
  productId: string;
  productName: string;
  lotId: string;
  maxQuantity: number;
  inventoryItem: InventoryItem;
}

const STATUS_CONFIG: Record<
  DeliverySlipStatus,
  { label: string; color: string; bgColor: string; icon: React.ElementType }
> = {
  PENDING: {
    label: 'Chờ xử lý',
    color: 'text-slate-700 dark:text-slate-300',
    bgColor: 'bg-slate-100 dark:bg-slate-900/40',
    icon: Clock3,
  },
  ASSIGNED: {
    label: 'Đã lên kế hoạch giao',
    color: 'text-blue-700 dark:text-blue-300',
    bgColor: 'bg-blue-100 dark:bg-blue-900/40',
    icon: Truck,
  },
  DELIVERING: {
    label: 'Đang giao',
    color: 'text-amber-700 dark:text-amber-300',
    bgColor: 'bg-amber-100 dark:bg-amber-900/40',
    icon: Truck,
  },
  DELIVERED: {
    label: 'Đã giao',
    color: 'text-purple-700 dark:text-purple-300',
    bgColor: 'bg-purple-100 dark:bg-purple-900/40',
    icon: CheckCircle2,
  },
  RECALLING: {
    label: 'Đang thu hồi',
    color: 'text-rose-700 dark:text-rose-300',
    bgColor: 'bg-rose-100 dark:bg-rose-900/40',
    icon: AlertCircle,
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

const formatNumber = (value?: number) => {
  if (!value) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

const formatFloat = (value?: number, decimalPlaces: number = 2) => {
  if (!value) return '0';
  return value.toLocaleString('vi-VN', {
    minimumFractionDigits: decimalPlaces,
    maximumFractionDigits: decimalPlaces,
  });
};

const formatFullname = (
  user: { firstName?: string; lastName?: string } | undefined
) => {
  if (!user) return 'N/A';
  return `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'N/A';
};

export const DeliverySlipDetailPage: React.FC<DeliverySlipDetailPageProps> = ({
  slipId,
}) => {
  const router = useRouter();

  const [isProcessing, setIsProcessing] = useState(false);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const [selectedItem, setSelectedItem] = useState<DeliveryItem | null>(null);
  const [selectedOutboundItemId, setSelectedOutboundItemId] = useState('');
  const [addQuantity, setAddQuantity] = useState(1);
  const [editQuantity, setEditQuantity] = useState(1);

  const {
    data: slipResponse,
    isLoading,
    isError,
  } = useGetDeliverySlipDetailQuery(slipId);

  const slip = slipResponse?.data;

  const { data: shipmentResponse } = useGetOutboundShipmentDetailQuery(
    slip?.outboundShipmentId || '',
    {
      skip: !slip?.outboundShipmentId,
    }
  );

  const shipment = shipmentResponse?.data;

  const [recallSlip, { isLoading: isRecallingSlip }] =
    useReturnDeliverySlipMutation();
  const [addItem, { isLoading: isAddingItem }] =
    useAddDeliverySlipItemMutation();
  const [updateItem, { isLoading: isUpdatingItem }] =
    useUpdateDeliverySlipItemMutation();
  const [removeItem, { isLoading: isRemovingItem }] =
    useRemoveDeliverySlipItemMutation();

  const userIds = slip?.createdByUserId ? [slip.createdByUserId] : [];
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

  const statusConfig = slip
    ? STATUS_CONFIG[slip.status as DeliverySlipStatus] || STATUS_CONFIG.PENDING
    : STATUS_CONFIG.PENDING;

  const StatusIcon = statusConfig.icon;

  const canMutateItems = slip?.status === 'PENDING';

  const goBack = () => {
    if (slip?.outboundShipmentId) {
      router.push(
        `/logistics2/outbound-shipments/${slip.outboundShipmentId}/delivery-slips`
      );
      return;
    }

    router.push('/logistics2/delivery-slips');
  };

  const shipmentItemMap = useMemo(() => {
    const map = new Map<string, OutboundShipmentItem>();

    (shipment?.items || []).forEach((item) => {
      map.set(item.id, item);
    });

    return map;
  }, [shipment]);

  const addItemOptions = useMemo<ShipmentItemOption[]>(() => {
    if (!shipment?.items || !slip) {
      return [];
    }

    const existingOutboundItemIds = new Set(
      (slip.items || []).map((item) => item.outboundShipmentItemId)
    );

    return shipment.items
      .map((item) => {
        const maxQuantity = item.quantityRemaining ?? 0;

        if (maxQuantity <= 0 || existingOutboundItemIds.has(item.id)) {
          return null;
        }

        return {
          outboundShipmentItemId: item.id,
          inventoryItemId: item.inventoryItemId,
          productId: item.productId,
          productName: item.product?.name || item.productId,
          lotId: item.inventoryItem?.lotId || 'N/A',
          maxQuantity,
          inventoryItem: item.inventoryItem,
        };
      })
      .filter((option): option is ShipmentItemOption => option !== null);
  }, [shipment, slip]);

  const addItemOptionMap = useMemo(() => {
    const map = new Map<string, ShipmentItemOption>();

    addItemOptions.forEach((option) => {
      map.set(option.outboundShipmentItemId, option);
    });

    return map;
  }, [addItemOptions]);

  const totalItems = slip?.items?.length || 0;
  const totalQuantity = (slip?.items || []).reduce(
    (sum, item) => sum + item.quantity,
    0
  );

  const getMaxEditableQuantity = (item: DeliveryItem | null) => {
    if (!item) return 1;

    const linkedShipmentItem = shipmentItemMap.get(item.outboundShipmentItemId);
    const remainingQuantity = linkedShipmentItem?.quantityRemaining ?? 0;

    return item.quantity + remainingQuantity;
  };

  const openAddDialog = () => {
    if (!canMutateItems) {
      toast.error(
        'Chỉ có thể thêm mặt hàng khi đơn giao hàng ở trạng thái PENDING.'
      );
      return;
    }

    setSelectedOutboundItemId('');
    setAddQuantity(1);
    setShowAddDialog(true);
  };

  const openEditDialog = (item: DeliveryItem) => {
    if (!canMutateItems) {
      toast.error(
        'Chỉ có thể sửa mặt hàng khi đơn giao hàng ở trạng thái PENDING.'
      );
      return;
    }

    setSelectedItem(item);
    setEditQuantity(item.quantity);
    setShowEditDialog(true);
  };

  const openDeleteDialog = (item: DeliveryItem) => {
    if (!canMutateItems) {
      toast.error(
        'Chỉ có thể xóa mặt hàng khi đơn giao hàng ở trạng thái PENDING.'
      );
      return;
    }

    setSelectedItem(item);
    setShowDeleteDialog(true);
  };

  const handleRecall = async () => {
    if (!shipment) return;

    setIsProcessing(true);
    try {
      await recallSlip(slipId).unwrap();
      toast.success('Đã trả đơn hàng về kho thành công.');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể trả đơn hàng về kho');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleAddItem = async () => {
    if (!slip || !canMutateItems) {
      return;
    }

    const option = addItemOptionMap.get(selectedOutboundItemId);

    if (!option) {
      toast.error('Vui lòng chọn mặt hàng cần thêm.');
      return;
    }

    if (addQuantity <= 0 || addQuantity > option.maxQuantity) {
      toast.error(`Số lượng phải từ 1 đến ${option.maxQuantity}.`);
      return;
    }

    try {
      await addItem({
        slipId: slip.id,
        data: {
          outboundShipmentItemId: option.outboundShipmentItemId,
          inventoryItemId: option.inventoryItemId,
          productId: option.productId,
          quantity: addQuantity,
        },
      }).unwrap();

      toast.success('Đã thêm mặt hàng vào đơn giao hàng.');
      setShowAddDialog(false);
      setSelectedOutboundItemId('');
      setAddQuantity(1);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  const handleUpdateItem = async () => {
    if (!slip || !selectedItem || !canMutateItems) {
      return;
    }

    const maxQuantity = getMaxEditableQuantity(selectedItem);

    if (editQuantity <= 0 || editQuantity > maxQuantity) {
      toast.error(`Số lượng phải từ 1 đến ${maxQuantity}.`);
      return;
    }

    try {
      await updateItem({
        slipId: slip.id,
        itemId: selectedItem.id,
        data: {
          quantity: editQuantity,
        },
      }).unwrap();

      toast.success('Cập nhật mặt hàng thành công.');
      setShowEditDialog(false);
      setSelectedItem(null);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  const handleDeleteItem = async () => {
    if (!slip || !selectedItem || !canMutateItems) {
      return;
    }

    try {
      await removeItem({
        slipId: slip.id,
        itemId: selectedItem.id,
      }).unwrap();

      toast.success('Đã xóa mặt hàng khỏi đơn giao hàng.');
      setShowDeleteDialog(false);
      setSelectedItem(null);
    } catch (error) {
      toast.error(getErrorMessage(error));
    }
  };

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse space-y-4'>
          <div className='h-8 w-1/3 rounded bg-muted' />
          <div className='h-64 rounded bg-muted' />
        </div>
      </div>
    );
  }

  if (isError || !slip) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='space-y-4 p-8 text-center'>
          <AlertCircle className='mx-auto h-12 w-12 text-destructive' />
          <h3 className='text-lg font-semibold'>
            Không tìm thấy đơn giao hàng
          </h3>
          <p className='text-muted-foreground'>
            Đơn giao hàng không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/logistics2/delivery-slips')}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 md:flex-row md:items-start md:justify-between'>
        <div className='flex items-start gap-3'>
          <Button variant='ghost' size='icon' onClick={goBack}>
            <ArrowLeft className='h-4 w-4' />
          </Button>

          <div>
            <div className='mb-2 flex flex-wrap items-center gap-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {slip.code || `Đơn giao hàng ${slip.id}`}
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
              ID: {slip.id} • Tạo ngày: {formatDate(slip.createdStamp)}
            </p>
          </div>
        </div>

        {slip.status === 'RECALLING' && (
          <Button
            onClick={handleRecall}
            disabled={isProcessing || isRecallingSlip}
          >
            {isRecallingSlip ? (
              <>
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                Đang xử lý...
              </>
            ) : (
              <>
                <Redo2Icon className='mr-2 h-4 w-4' />
                Trả kho
              </>
            )}
          </Button>
        )}
      </div>

      <div className='grid gap-6 lg:grid-cols-3'>
        <div className='space-y-6 lg:col-span-2'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Thông tin đơn giao hàng</h3>
            </CardHeader>
            <CardContent className='grid gap-4 sm:grid-cols-2'>
              <div>
                <Label className='text-muted-foreground'>Phiếu xuất</Label>
                <p className='font-medium'>
                  {shipment?.name || slip.outboundShipmentId}
                </p>
              </div>
              <div>
                <Label className='text-muted-foreground'>Người tạo</Label>
                <p>
                  {formatFullname(
                    slip.createdByUserId
                      ? userMap.get(slip.createdByUserId)
                      : undefined
                  )}
                </p>
              </div>

              <div>
                <Label className='text-muted-foreground'>Khách hàng</Label>
                <p className='font-medium'>
                  {slip.customerName || slip.customerId || 'N/A'}
                </p>
              </div>

              <div>
                <Label className='text-muted-foreground'>
                  Địa chỉ khách hàng
                </Label>
                <p>{slip.customerAddress?.fullAddress || 'N/A'}</p>
              </div>
              <div>
                <Label className='text-muted-foreground'>Kho giao</Label>
                <p className='font-medium'>
                  {slip.facility?.name || slip.facilityId || 'N/A'}
                </p>
              </div>
              <div>
                <Label className='text-muted-foreground'>
                  Địa chỉ kho giao
                </Label>
                <p>{slip.facilityAddress?.fullAddress || 'N/A'}</p>
              </div>
              <div>
                <Label className='text-muted-foreground'>Ngày tạo</Label>
                <p>{formatDate(slip.createdStamp)}</p>
              </div>
              <div>
                <Label className='text-muted-foreground'>
                  Cập nhật lần cuối
                </Label>
                <p>{formatDate(slip.lastUpdatedStamp)}</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
                <div>
                  <h3 className='font-semibold'>Danh sách mặt hàng giao</h3>
                  {!canMutateItems && (
                    <p className='text-sm text-muted-foreground'>
                      Thao tác thêm, sửa, xóa chỉ khả dụng khi trạng thái là
                      PENDING.
                    </p>
                  )}
                </div>

                <div className='flex items-center gap-2'>
                  <Badge variant='secondary'>{totalItems} mặt hàng</Badge>
                  {canMutateItems && (
                    <Button size='sm' onClick={openAddDialog}>
                      <Plus className='mr-2 h-4 w-4' />
                      Thêm mặt hàng
                    </Button>
                  )}
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {(slip.items || []).length > 0 ? (
                <div className='space-y-3'>
                  {(slip.items || []).map((item) => {
                    const maxEditableQuantity = getMaxEditableQuantity(item);

                    return (
                      <div key={item.id} className='rounded-lg border p-4'>
                        <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
                          <div>
                            <p className='font-medium'>
                              {item.productName || item.productId}
                            </p>
                            <div className='mt-1 grid gap-4 text-sm text-muted-foreground sm:grid-cols-4'>
                              <p>Số lượng: {formatNumber(item.quantity)}</p>
                              <p>Khối lượng: {item.weightKg}kg</p>
                              <p>
                                Kích thước:{' '}
                                {formatFloat(
                                  item.heightM * item.widthM * item.lengthM
                                )}
                                m3
                              </p>
                              <p>
                                Số lượng tối đa:{' '}
                                {formatNumber(maxEditableQuantity)}
                              </p>
                            </div>
                          </div>

                          {canMutateItems && (
                            <div className='flex items-center gap-2'>
                              <Button
                                variant='ghost'
                                size='icon'
                                onClick={() => openEditDialog(item)}
                              >
                                <Edit className='h-4 w-4' />
                              </Button>
                              <Button
                                variant='ghost'
                                size='icon'
                                className='text-destructive hover:text-destructive'
                                onClick={() => openDeleteDialog(item)}
                              >
                                <Trash2 className='h-4 w-4' />
                              </Button>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className='rounded-lg border border-dashed py-12 text-center text-muted-foreground'>
                  Đơn giao hàng chưa có mặt hàng nào.
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <Card className='h-fit'>
          <CardHeader>
            <div className='flex items-center gap-2'>
              <Package className='h-5 w-5 text-primary' />
              <h3 className='font-semibold'>Thống kê</h3>
            </div>
          </CardHeader>
          <CardContent className='space-y-3 text-sm'>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Số mặt hàng</span>
              <span className='font-medium'>{totalItems}</span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Tổng số lượng</span>
              <span className='font-medium'>{formatNumber(totalQuantity)}</span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Tổng khối lượng</span>
              <span className='font-medium'>
                {formatNumber(slip.totalWeightKg)} kg
              </span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-muted-foreground'>Tổng thể tích</span>
              <span className='font-medium'>
                {formatNumber(slip.totalVolumeCbm)} m3
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      <Dialog open={showAddDialog} onOpenChange={setShowAddDialog}>
        <DialogContent className='sm:max-w-[640px]'>
          <DialogHeader>
            <DialogTitle>Thêm mặt hàng vào đơn giao hàng</DialogTitle>
            <DialogDescription>
              Chỉ các mặt hàng còn lại của phiếu xuất mới có thể được thêm.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4 py-2'>
            <div className='space-y-2'>
              <Label htmlFor='add-item'>Mặt hàng</Label>
              <select
                id='add-item'
                value={selectedOutboundItemId}
                onChange={(event) =>
                  setSelectedOutboundItemId(event.target.value)
                }
                className='h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>Chọn mặt hàng</option>
                {addItemOptions.map((option) => (
                  <option
                    key={option.outboundShipmentItemId}
                    value={option.outboundShipmentItemId}
                  >
                    {option.productName} | Lô: {option.lotId} | Còn lại:{' '}
                    {formatNumber(option.maxQuantity)}
                  </option>
                ))}
              </select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='add-quantity'>Số lượng</Label>
              <Input
                id='add-quantity'
                type='number'
                min={1}
                max={
                  selectedOutboundItemId
                    ? addItemOptionMap.get(selectedOutboundItemId)
                        ?.maxQuantity || 1
                    : 1
                }
                value={addQuantity}
                onChange={(event) =>
                  setAddQuantity(Number(event.target.value) || 1)
                }
              />
            </div>

            {addItemOptions.length === 0 && (
              <p className='rounded-md bg-muted p-3 text-sm text-muted-foreground'>
                Không còn mặt hàng khả dụng để thêm.
              </p>
            )}
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowAddDialog(false)}
              disabled={isAddingItem}
            >
              Hủy
            </Button>
            <Button
              onClick={handleAddItem}
              disabled={isAddingItem || addItemOptions.length === 0}
            >
              {isAddingItem ? (
                <>
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  Đang thêm...
                </>
              ) : (
                'Thêm mặt hàng'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showEditDialog} onOpenChange={setShowEditDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật số lượng</DialogTitle>
            <DialogDescription>
              Điều chỉnh số lượng cho mặt hàng đã chọn.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-2 py-2'>
            <Label htmlFor='edit-quantity'>Số lượng mới</Label>
            <Input
              id='edit-quantity'
              type='number'
              min={1}
              max={getMaxEditableQuantity(selectedItem)}
              value={editQuantity}
              onChange={(event) =>
                setEditQuantity(Number(event.target.value) || 1)
              }
            />
            <p className='text-xs text-muted-foreground'>
              Tối đa: {formatNumber(getMaxEditableQuantity(selectedItem))}
            </p>
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setShowEditDialog(false)}
              disabled={isUpdatingItem}
            >
              Hủy
            </Button>
            <Button onClick={handleUpdateItem} disabled={isUpdatingItem}>
              {isUpdatingItem ? (
                <>
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  Đang lưu...
                </>
              ) : (
                'Lưu thay đổi'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa mặt hàng khỏi đơn giao hàng</AlertDialogTitle>
            <AlertDialogDescription>
              Hành động này sẽ xóa mặt hàng khỏi đơn giao hàng hiện tại.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel disabled={isRemovingItem}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteItem}
              disabled={isRemovingItem}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isRemovingItem ? (
                <>
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  Đang xóa...
                </>
              ) : (
                'Xóa mặt hàng'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default DeliverySlipDetailPage;
