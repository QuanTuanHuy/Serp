/**
 * Order Detail Page - Purchase Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Purchase order detail with item management
 */

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Input,
  Label,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Textarea,
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Phone,
  MapPin,
  Calendar,
  User,
  Package,
  DollarSign,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  FileText,
  TrendingUp,
  Building2,
  Truck,
  PanelsTopLeft,
  Mail,
  Plus,
  Edit,
  Trash2,
  MoreHorizontal,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetOrderQuery,
  useGetSupplierQuery,
  useGetShipmentsByOrderQuery,
  useAddProductToOrderMutation,
  useUpdateProductInOrderMutation,
  useDeleteProductFromOrderMutation,
  useGetProductsQuery,
  useApproveOrderMutation,
  useCancelOrderMutation,
  useDeleteOrderMutation,
} from '../../api/purchaseApi';
import { UserProfile, useGetUsersQuery } from '@/modules/admin';
import type { OrderItem, OrderItemUpdateForm, Shipment } from '../../types';

interface OrderDetailPageProps {
  orderId: string;
}

// Order status configuration
const STATUS_CONFIG = {
  CREATED: {
    label: 'Đã tạo',
    color: 'text-blue-700 dark:text-blue-400',
    bgColor: 'bg-blue-100 dark:bg-blue-900/30',
    icon: Clock,
  },
  APPROVED: {
    label: 'Đã phê duyệt',
    color: 'text-emerald-700 dark:text-emerald-400',
    bgColor: 'bg-emerald-100 dark:bg-emerald-900/30',
    icon: CheckCircle2,
  },
  CANCELLED: {
    label: 'Đã hủy',
    color: 'text-rose-700 dark:text-rose-400',
    bgColor: 'bg-rose-100 dark:bg-rose-900/30',
    icon: XCircle,
  },
  FULLY_DELIVERED: {
    label: 'Đã nhận hàng',
    color: 'text-purple-700 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
    icon: Package,
  },
};

const statusStyles = {
  CREATED: {
    label: 'Nháp',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    icon: Clock,
  },
  IMPORTED: {
    label: 'Đã nhập kho',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    icon: CheckCircle2,
  },
  EXPORTED: {
    label: 'Đã xuất kho',
    bg: 'bg-green-100 dark:bg-green-900/30',
    text: 'text-green-700 dark:text-green-400',
    icon: Truck,
  },
};

const formatCurrency = (value?: number) => {
  if (!value) return '0 đ';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value);
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatFullname = (user: UserProfile | undefined) => {
  if (!user) return 'N/A';
  return `${user.firstName || ''} ${user.lastName || ''}`.trim();
};

// Shipment Card Component
const ShipmentCard = ({
  shipment,
  onClick,
}: {
  shipment: Shipment;
  onClick?: () => void;
}) => {
  const status =
    statusStyles[shipment.statusId as keyof typeof statusStyles] ||
    statusStyles.CREATED;
  const StatusIcon = status.icon;

  return (
    <Card
      className={cn(
        'hover:shadow-md transition-all duration-200',
        onClick && 'cursor-pointer hover:border-primary/50'
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
            <Badge variant='secondary' className={cn('gap-1', status.bg)}>
              <StatusIcon className={cn('h-3 w-3', status.text)} />
              <span className={status.text}>{status.label}</span>
            </Badge>
          </div>
        </div>

        <div className='space-y-2 text-sm'>
          <div className='flex items-center gap-2'>
            <Calendar className='h-4 w-4 text-muted-foreground' />
            <span className='text-muted-foreground'>
              Ngày tạo: {formatDate(shipment.createdStamp)}
            </span>
          </div>
          {shipment.expectedDeliveryDate && (
            <div className='flex items-center gap-2'>
              <Truck className='h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground'>
                Giao hàng: {formatDate(shipment.expectedDeliveryDate)}
              </span>
            </div>
          )}
          {shipment.totalQuantity && (
            <div className='flex items-center gap-2'>
              <Package className='h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground'>
                Số lượng: {shipment.totalQuantity}
              </span>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

// Order Item Management Dialog
interface OrderItemDialogProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: any) => void;
  item?: OrderItem;
  orderId: string;
}

const OrderItemDialog: React.FC<OrderItemDialogProps> = ({
  open,
  onClose,
  onSave,
  item,
  orderId,
}) => {
  const [formData, setFormData] = useState({
    productId: item?.productId || '',
    orderItemSeqId: item?.orderItemSeqId || 1,
    quantity: item?.quantity || 1,
    tax: item?.tax || 0,
    discount: item?.discount || 0,
  });

  const { data: productsResponse } = useGetProductsQuery({
    filters: {},
    pagination: { page: 0, size: 100 },
  });

  const products = productsResponse?.data?.items || [];

  const handleSubmit = () => {
    onSave(formData);
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {item ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm'}
          </DialogTitle>
          <DialogDescription>
            {item
              ? 'Cập nhật thông tin sản phẩm trong đơn hàng'
              : 'Thêm sản phẩm mới vào đơn hàng'}
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          {!item && (
            <div>
              <Label>Sản phẩm</Label>
              <select
                value={formData.productId}
                onChange={(e) =>
                  setFormData({ ...formData, productId: e.target.value })
                }
                className='w-full px-3 py-2 border rounded-lg bg-background'
                disabled={!!item}
              >
                <option value=''>Chọn sản phẩm</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} - {product.skuCode}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <Label>Số lượng</Label>
            <Input
              type='number'
              value={formData.quantity}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  quantity: parseInt(e.target.value) || 1,
                })
              }
              min='1'
            />
          </div>

          <div className='grid grid-cols-2 gap-4'>
            <div>
              <Label>Thuế (%)</Label>
              <Input
                type='number'
                value={formData.tax}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    tax: parseFloat(e.target.value) || 0,
                  })
                }
                min='0'
                step='0.1'
              />
            </div>

            <div>
              <Label>Giảm giá</Label>
              <Input
                type='number'
                value={formData.discount}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    discount: parseFloat(e.target.value) || 0,
                  })
                }
                min='0'
              />
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={onClose}>
            Hủy
          </Button>
          <Button onClick={handleSubmit}>{item ? 'Cập nhật' : 'Thêm'}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export const OrderDetailPage: React.FC<OrderDetailPageProps> = ({
  orderId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [itemDialogOpen, setItemDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<OrderItem | undefined>();
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [cancellationNote, setCancellationNote] = useState('');

  // Fetch order data
  const { data: orderResponse, isLoading, isError } = useGetOrderQuery(orderId);
  const order = orderResponse?.data;

  // Fetch supplier data
  const { data: supplierResponse } = useGetSupplierQuery(
    order?.fromSupplierId || '',
    { skip: !order?.fromSupplierId }
  );
  const supplier = supplierResponse?.data;

  // Fetch shipments related to this order
  const {
    data: shipmentsResponse,
    isLoading: isLoadingShipments,
    error: shipmentsError,
  } = useGetShipmentsByOrderQuery(orderId);
  const shipments = shipmentsResponse?.data || [];

  // Mutations for order items
  const [addProduct] = useAddProductToOrderMutation();
  const [updateProduct] = useUpdateProductInOrderMutation();
  const [deleteProduct] = useDeleteProductFromOrderMutation();

  // Mutations for order actions
  const [approveOrder, { isLoading: isApproving }] = useApproveOrderMutation();
  const [cancelOrder, { isLoading: isCancelling }] = useCancelOrderMutation();
  const [deleteOrder, { isLoading: isDeleting }] = useDeleteOrderMutation();

  // Collect unique user IDs from order
  const userIds = [
    order?.createdByUserId,
    order?.userApprovedId,
    order?.userCancelledId,
  ].filter((id): id is number => !!id);

  // Fetch users data
  const { data: usersResponse } = useGetUsersQuery(
    {
      page: 0,
      pageSize: 100,
    },
    { skip: userIds.length === 0 }
  );

  // Create user map for quick lookup
  const userMap = new Map(
    usersResponse?.data?.items?.map((user) => [user.id, user]) || []
  );

  const statusConfig = order
    ? STATUS_CONFIG[order.statusId as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.CREATED
    : STATUS_CONFIG.CREATED;
  const StatusIcon = statusConfig.icon;

  // Handle order item actions
  const handleAddItem = () => {
    setEditingItem(undefined);
    setItemDialogOpen(true);
  };

  const handleEditItem = (item: OrderItem) => {
    setEditingItem(item);
    setItemDialogOpen(true);
  };

  const handleDeleteItem = async (itemId: string) => {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;

    try {
      await deleteProduct({ orderId, orderItemId: itemId }).unwrap();
      toast.success('Đã xóa sản phẩm khỏi đơn hàng');
    } catch (error) {
      toast.error('Không thể xóa sản phẩm');
    }
  };

  const handleSaveItem = async (data: any) => {
    try {
      if (editingItem) {
        // Update existing item
        await updateProduct({
          orderId,
          orderItemId: editingItem.id,
          data: {
            orderItemSeqId: data.orderItemSeqId,
            quantity: data.quantity,
            tax: data.tax,
            discount: data.discount,
          } as OrderItemUpdateForm,
        }).unwrap();
        toast.success('Đã cập nhật sản phẩm');
      } else {
        // Add new item
        await addProduct({
          orderId,
          data: data as OrderItem,
        }).unwrap();
        toast.success('Đã thêm sản phẩm vào đơn hàng');
      }
    } catch (error) {
      toast.error(
        editingItem ? 'Không thể cập nhật sản phẩm' : 'Không thể thêm sản phẩm'
      );
    }
  };

  // Handle order actions
  const handleApprove = async () => {
    if (!confirm('Bạn có chắc muốn phê duyệt đơn hàng này?')) return;

    try {
      await approveOrder(orderId).unwrap();
      toast.success('Đã phê duyệt đơn hàng');
    } catch (error) {
      toast.error('Không thể phê duyệt đơn hàng');
    }
  };

  const handleEdit = () => {
    router.push(`/purchase/purchase-orders/${orderId}/edit`);
  };

  const handleCancel = async () => {
    if (!cancellationNote.trim()) {
      toast.error('Vui lòng nhập lý do hủy đơn hàng');
      return;
    }

    try {
      await cancelOrder({ orderId, data: { note: cancellationNote } }).unwrap();
      toast.success('Đã hủy đơn hàng');
      setShowCancelDialog(false);
      setCancellationNote('');
    } catch (error) {
      toast.error('Không thể hủy đơn hàng');
    }
  };

  const handleDelete = async () => {
    if (
      !confirm(
        'Bạn có chắc muốn xóa đơn hàng này? Hành động này không thể hoàn tác.'
      )
    )
      return;

    try {
      await deleteOrder(orderId).unwrap();
      toast.success('Đã xóa đơn hàng');
      router.push('/purchase/purchase-orders');
    } catch (error) {
      toast.error('Không thể xóa đơn hàng');
    }
  };

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse'>
          <div className='h-8 bg-muted rounded w-1/3 mb-4' />
          <div className='h-64 bg-muted rounded' />
        </div>
      </div>
    );
  }

  if (isError || !order) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Không tìm thấy đơn hàng
          </h3>
          <p className='text-muted-foreground mb-4'>
            Đơn hàng không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/purchase/purchase-orders')}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Quay lại danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div className='flex items-center gap-4'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/purchase/purchase-orders')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <div className='flex items-center gap-3 mb-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {order.orderName || `Đơn hàng #${order.id?.slice(0, 8)}...`}
              </h1>
              <Badge
                variant='secondary'
                className={cn('gap-1.5', statusConfig.bgColor)}
              >
                <StatusIcon className={cn('h-3 w-3', statusConfig.color)} />
                <span className={statusConfig.color}>{statusConfig.label}</span>
              </Badge>
            </div>
            <p className='text-muted-foreground text-sm'>
              ID: {order.id} • Ngày đặt: {formatDate(order.orderDate)}
            </p>
          </div>
        </div>
        {(order.statusId === 'CREATED' || order.statusId === 'CANCELLED') && (
          <div className='flex items-center gap-2'>
            {order.statusId === 'CREATED' && (
              <Button
                onClick={handleApprove}
                disabled={isApproving}
                className='gap-2'
              >
                <CheckCircle2 className='h-4 w-4' />
                Phê duyệt đơn hàng
              </Button>
            )}

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant='outline' size='icon'>
                  <MoreHorizontal className='h-4 w-4' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end'>
                <DropdownMenuItem onClick={handleEdit}>
                  <Edit className='mr-2 h-4 w-4' />
                  Chỉnh sửa đơn hàng
                </DropdownMenuItem>
                {order.statusId === 'CREATED' && (
                  <DropdownMenuItem onClick={() => setShowCancelDialog(true)}>
                    <XCircle className='mr-2 h-4 w-4' />
                    Hủy đơn hàng
                  </DropdownMenuItem>
                )}
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onClick={handleDelete}
                  disabled={isDeleting}
                  className='text-destructive'
                >
                  <Trash2 className='mr-2 h-4 w-4' />
                  Xóa đơn hàng
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        )}
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
          <TabsTrigger value='items'>
            Sản phẩm ({order.items?.length || 0})
          </TabsTrigger>
          <TabsTrigger value='shipments'>
            Phiếu kho ({shipments.length})
          </TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value='overview' className='space-y-6 mt-6'>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            {/* Left Column - Order Details */}
            <div className='lg:col-span-2 space-y-6'>
              {/* Supplier Information */}
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <Building2 className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Nhà cung cấp</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-4'>
                  {supplier ? (
                    <>
                      <div>
                        <Label className='text-muted-foreground'>
                          Tên nhà cung cấp
                        </Label>
                        <p className='font-medium'>{supplier.name}</p>
                      </div>
                      {supplier.email && (
                        <div className='flex items-center gap-2'>
                          <Mail className='h-4 w-4 text-muted-foreground' />
                          <span>{supplier.email}</span>
                        </div>
                      )}
                      {supplier.phone && (
                        <div className='flex items-center gap-2'>
                          <Phone className='h-4 w-4 text-muted-foreground' />
                          <span>{supplier.phone}</span>
                        </div>
                      )}
                      {supplier.address?.fullAddress && (
                        <div className='flex items-start gap-2'>
                          <MapPin className='h-4 w-4 text-muted-foreground mt-1' />
                          <span className='text-sm'>
                            {supplier.address.fullAddress}
                          </span>
                        </div>
                      )}
                    </>
                  ) : (
                    <p className='text-muted-foreground'>
                      Không có thông tin nhà cung cấp
                    </p>
                  )}
                </CardContent>
              </Card>

              {/* Delivery Information */}
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <Truck className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Thông tin giao nhận</h3>
                  </div>
                </CardHeader>
                <CardContent className='grid gap-4'>
                  <div className='grid grid-cols-2 gap-4'>
                    <div>
                      <Label className='text-muted-foreground'>
                        Giao sau ngày
                      </Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <Calendar className='h-4 w-4 text-muted-foreground' />
                        <span>{formatDate(order.deliveryAfterDate)}</span>
                      </div>
                    </div>
                    <div>
                      <Label className='text-muted-foreground'>
                        Giao trước ngày
                      </Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <Calendar className='h-4 w-4 text-muted-foreground' />
                        <span>{formatDate(order.deliveryBeforeDate)}</span>
                      </div>
                    </div>
                  </div>

                  {order.deliveryFullAddress && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Địa chỉ giao hàng
                      </Label>
                      <div className='flex items-start gap-2 mt-1'>
                        <MapPin className='h-4 w-4 text-muted-foreground mt-1' />
                        <span>{order.deliveryFullAddress}</span>
                      </div>
                    </div>
                  )}

                  {order.deliveryPhone && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Số điện thoại liên hệ
                      </Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <Phone className='h-4 w-4 text-muted-foreground' />
                        <span>{order.deliveryPhone}</span>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Additional Information */}
              {order.note && (
                <Card>
                  <CardHeader>
                    <div className='flex items-center gap-2'>
                      <FileText className='h-5 w-5 text-primary' />
                      <h3 className='font-semibold'>Ghi chú</h3>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className='text-muted-foreground whitespace-pre-wrap'>
                      {order.note}
                    </p>
                  </CardContent>
                </Card>
              )}
            </div>

            {/* Right Column - Summary */}
            <div className='space-y-6'>
              {/* Order Summary */}
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <PanelsTopLeft className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Tổng quan</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-4'>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>Mức ưu tiên</span>
                    <Badge variant='outline'>
                      <TrendingUp className='h-3 w-3 mr-1' />
                      {order.priority}
                    </Badge>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>
                      Số lượng sản phẩm
                    </span>
                    <span className='font-medium'>
                      {order.items?.length || 0}
                    </span>
                  </div>

                  <div className='border-t pt-4'>
                    <div className='flex items-center justify-between text-lg font-semibold'>
                      <span>Tổng thành tiền</span>
                      <span className='text-primary'>
                        {formatCurrency(order.totalAmount)}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Order Metadata */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Thông tin chi tiết</h3>
                </CardHeader>
                <CardContent className='space-y-3 text-sm'>
                  <div>
                    <Label className='text-muted-foreground'>Ngày tạo</Label>
                    <p>{formatDate(String(order.createdStamp))}</p>
                  </div>

                  {order.lastUpdatedStamp && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Cập nhật lần cuối
                      </Label>
                      <p>{formatDate(String(order.lastUpdatedStamp))}</p>
                    </div>
                  )}

                  {order.createdByUserId && (
                    <div>
                      <Label className='text-muted-foreground'>Người tạo</Label>
                      <p className='font-bold'>
                        {formatFullname(userMap.get(order.createdByUserId))}
                      </p>
                    </div>
                  )}

                  {order.userApprovedId && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Người phê duyệt
                      </Label>
                      <p className='font-bold text-emerald-700 dark:text-emerald-400'>
                        {formatFullname(userMap.get(order.userApprovedId))}
                      </p>
                    </div>
                  )}

                  {order.userCancelledId && (
                    <div>
                      <Label className='text-muted-foreground'>Người hủy</Label>
                      <p className=' font-bold text-destructive'>
                        {formatFullname(userMap.get(order.userCancelledId))}
                      </p>
                    </div>
                  )}

                  {order.cancellationNote && (
                    <div>
                      <Label className='text-muted-foreground'>Lý do hủy</Label>
                      <p className='text-destructive'>
                        {order.cancellationNote}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        {/* Items Tab */}
        <TabsContent value='items' className='space-y-6 mt-6'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Danh sách sản phẩm</h3>
                </div>
                {order.statusId === 'CREATED' && (
                  <Button onClick={handleAddItem}>
                    <Plus className='h-4 w-4 mr-2' />
                    Thêm sản phẩm
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {order.items && order.items.length > 0 ? (
                <div className='space-y-3'>
                  {order.items.map((item, index) => (
                    <div
                      key={item.id || index}
                      className='flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors'
                    >
                      <div className='flex-1'>
                        <p className='font-medium'>
                          {item.product?.name || item.productId}
                        </p>
                        <p className='text-sm text-muted-foreground'>
                          SKU: {item.product?.skuCode || 'N/A'}
                        </p>
                      </div>
                      <div className='flex items-center gap-8 text-sm'>
                        <div>
                          <Label className='text-muted-foreground'>
                            Số lượng
                          </Label>
                          <p className='font-medium'>
                            {item.quantity} {item.product?.unit || ''}
                          </p>
                        </div>
                        <div>
                          <Label className='text-muted-foreground'>
                            Còn thiếu
                          </Label>
                          <p className='font-medium'>
                            {item.quantityRemaining} {item.product?.unit || ''}
                          </p>
                        </div>
                        <div>
                          <Label className='text-muted-foreground'>
                            Đơn giá
                          </Label>
                          <p className='font-medium'>
                            {formatCurrency(item.price)}
                          </p>
                        </div>
                        <div>
                          <Label className='text-muted-foreground'>Thuế</Label>
                          <p className='font-medium'>{item.tax}%</p>
                        </div>
                        <div>
                          <Label className='text-muted-foreground'>
                            Giảm giá
                          </Label>
                          <p className='font-medium'>
                            {formatCurrency(item.discount)}
                          </p>
                        </div>
                        <div>
                          <Label className='text-muted-foreground'>
                            Thành tiền
                          </Label>
                          <p className='font-semibold text-primary'>
                            {formatCurrency(item.amount)}
                          </p>
                        </div>
                        {order.statusId === 'CREATED' && (
                          <div className='flex gap-2'>
                            <Button
                              variant='ghost'
                              size='icon'
                              onClick={() => handleEditItem(item)}
                            >
                              <Edit className='h-4 w-4' />
                            </Button>
                            <Button
                              variant='ghost'
                              size='icon'
                              onClick={() => handleDeleteItem(item.id)}
                            >
                              <Trash2 className='h-4 w-4' />
                            </Button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='py-12 text-center text-muted-foreground'>
                  <Package className='h-12 w-12 mx-auto mb-4 opacity-50' />
                  <p>Chưa có sản phẩm nào trong đơn hàng</p>
                  {order.statusId === 'CREATED' && (
                    <Button
                      variant='outline'
                      className='mt-4'
                      onClick={handleAddItem}
                    >
                      <Plus className='h-4 w-4 mr-2' />
                      Thêm sản phẩm đầu tiên
                    </Button>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Shipments Tab */}
        <TabsContent value='shipments' className='space-y-6 mt-6'>
          {/* Loading State */}
          {isLoadingShipments && (
            <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
              {Array.from({ length: 3 }).map((_, index) => (
                <Card key={index} className='animate-pulse'>
                  <CardContent className='p-5'>
                    <div className='flex items-center gap-3 mb-4'>
                      <div className='h-10 w-10 bg-muted rounded-lg' />
                      <div className='flex-1'>
                        <div className='h-4 bg-muted rounded w-3/4 mb-2' />
                        <div className='h-3 bg-muted rounded w-1/2' />
                      </div>
                    </div>
                    <div className='space-y-2'>
                      <div className='h-3 bg-muted rounded w-full' />
                      <div className='h-3 bg-muted rounded w-2/3' />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {/* Error State */}
          {shipmentsError && (
            <Card className='border-destructive/50 bg-destructive/5'>
              <CardContent className='p-6 text-center'>
                <p className='text-destructive'>
                  Đã xảy ra lỗi khi tải phiếu kho. Vui lòng thử lại sau.
                </p>
              </CardContent>
            </Card>
          )}

          {/* Shipments List (Read-only) */}
          {!isLoadingShipments && !shipmentsError && shipments.length > 0 && (
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <div>
                  <h3 className='text-lg font-semibold'>Danh sách phiếu kho</h3>
                  <p className='text-sm text-muted-foreground'>
                    {shipments.length} phiếu kho (chỉ xem)
                  </p>
                </div>
              </div>

              <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
                {shipments.map((shipment: Shipment) => (
                  <ShipmentCard
                    key={shipment.id}
                    shipment={shipment}
                    onClick={() =>
                      router.push(`/purchase/shipments/${shipment.id}`)
                    }
                  />
                ))}
              </div>
            </div>
          )}

          {/* Empty State */}
          {!isLoadingShipments && !shipmentsError && shipments.length === 0 && (
            <Card>
              <CardContent className='py-16 text-center'>
                <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
                  <Truck className='w-10 h-10 text-muted-foreground' />
                </div>
                <h3 className='text-lg font-semibold mb-2'>
                  Chưa có phiếu kho
                </h3>
                <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
                  Phiếu kho sẽ được tạo bởi bộ phận logistics.
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>

      {/* Order Item Dialog */}
      <OrderItemDialog
        open={itemDialogOpen}
        onClose={() => setItemDialogOpen(false)}
        onSave={handleSaveItem}
        item={editingItem}
        orderId={orderId}
      />

      {/* Cancel Order Dialog */}
      <Dialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Hủy đơn hàng</DialogTitle>
            <DialogDescription>
              Vui lòng nhập lý do hủy đơn hàng. Hành động này không thể hoàn
              tác.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div>
              <Label htmlFor='cancellationNote'>Lý do hủy *</Label>
              <Textarea
                id='cancellationNote'
                value={cancellationNote}
                onChange={(e) => setCancellationNote(e.target.value)}
                placeholder='Nhập lý do hủy đơn hàng...'
                rows={4}
                className='resize-none'
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => {
                setShowCancelDialog(false);
                setCancellationNote('');
              }}
            >
              Đóng
            </Button>
            <Button
              variant='destructive'
              onClick={handleCancel}
              disabled={isCancelling || !cancellationNote.trim()}
            >
              {isCancelling ? 'Đang hủy...' : 'Xác nhận hủy'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};
