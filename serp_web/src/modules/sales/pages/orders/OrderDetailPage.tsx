/*
Author: QuanTuanHuy
Description: Part of Serp Project - Sales Order Detail Page
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
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Input,
  Label,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  MoreHorizontal,
  Edit,
  Trash2,
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
  ShoppingCart,
  Plus,
  Search,
  Save,
  X as CloseIcon,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetOrderQuery,
  useDeleteOrderMutation,
  useApproveOrderMutation,
  useCancelOrderMutation,
  useAddProductToOrderMutation,
  useDeleteProductFromOrderMutation,
  useGetProductsQuery,
  useGetCustomerQuery,
} from '../../api/salesApi';
import { useGetUsersQuery, UserProfile } from '@/modules/admin';

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
    label: 'Đã giao hàng',
    color: 'text-purple-700 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
    icon: Package,
  },
};

const formatFullname = (user: UserProfile | undefined) => {
  if (!user) return 'N/A';
  return `${user.firstName || ''} ${user.lastName || ''}`.trim();
};

export const OrderDetailPage: React.FC<OrderDetailPageProps> = ({
  orderId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [cancellationNote, setCancellationNote] = useState('');

  // Order Items Management States
  const [showAddItemDialog, setShowAddItemDialog] = useState(false);
  const [editingItemId, setEditingItemId] = useState<string | null>(null);
  const [productSearch, setProductSearch] = useState('');
  const [showProductDropdown, setShowProductDropdown] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [itemQuantity, setItemQuantity] = useState(1);
  const [itemTax, setItemTax] = useState(0);
  const [itemDiscount, setItemDiscount] = useState(0);

  // Fetch order data
  const { data: orderResponse, isLoading, isError } = useGetOrderQuery(orderId);
  const [deleteOrder, { isLoading: isDeleting }] = useDeleteOrderMutation();
  const [approveOrder, { isLoading: isApproving }] = useApproveOrderMutation();
  const [cancelOrder, { isLoading: isCancelling }] = useCancelOrderMutation();
  const [addProductToOrder, { isLoading: isAddingProduct }] =
    useAddProductToOrderMutation();
  const [deleteProductFromOrder, { isLoading: isDeletingProduct }] =
    useDeleteProductFromOrderMutation();

  const order = orderResponse?.data;

  // Fetch customer data
  const { data: customerResponse } = useGetCustomerQuery(
    order?.toCustomerId || '',
    { skip: !order?.toCustomerId }
  );

  // Collect unique user IDs from order
  const userIds = [
    order?.createdByUserId,
    order?.userApprovedId,
    order?.userCancelledId,
  ].filter((id): id is number => !!id);

  // Fetch users data - get all users and filter by IDs
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

  // Fetch products for search
  const { data: productsResponse } = useGetProductsQuery({
    filters: { query: productSearch },
    pagination: { page: 0, size: 20 },
  });

  const customer = customerResponse?.data;
  const products = productsResponse?.data?.items || [];

  const statusConfig = order
    ? STATUS_CONFIG[order.statusId as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.CREATED
    : STATUS_CONFIG.CREATED;
  const StatusIcon = statusConfig.icon;

  const handleEdit = () => {
    router.push(`/sales/orders/${orderId}/edit`);
  };

  const handleDelete = async () => {
    if (!confirm('Bạn có chắc chắn muốn xóa đơn hàng này không?')) return;

    try {
      await deleteOrder(orderId).unwrap();
      toast.success('Xóa đơn hàng thành công');
      router.push('/sales/orders');
    } catch (error: any) {
      console.error('Không thể xóa đơn hàng:', error);
      toast.error(error?.data?.message || 'Không thể xóa đơn hàng');
    }
  };

  const handleApprove = async () => {
    if (!confirm('Bạn có chắc chắn muốn phê duyệt đơn hàng này không?')) return;

    try {
      await approveOrder(orderId).unwrap();
      toast.success('Phê duyệt đơn hàng thành công');
    } catch (error: any) {
      console.error('Không thể phê duyệt đơn hàng:', error);
      toast.error(error?.data?.message || 'Không thể phê duyệt đơn hàng');
    }
  };

  const handleCancel = async () => {
    if (!cancellationNote.trim()) {
      toast.error('Vui lòng cung cấp lý do hủy đơn hàng');
      return;
    }

    try {
      await cancelOrder({
        orderId,
        data: { cancellationNote },
      }).unwrap();
      toast.success('Hủy đơn hàng thành công');
      setShowCancelDialog(false);
      setCancellationNote('');
    } catch (error: any) {
      console.error('Không thể hủy đơn hàng:', error);
      toast.error(error?.data?.message || 'Không thể hủy đơn hàng');
    }
  };
  // Order Items Management Functions
  const handleAddItem = async () => {
    if (!selectedProductId) {
      toast.error('Vui lòng chọn sản phẩm');
      return;
    }

    if (itemQuantity <= 0) {
      toast.error('Số lượng phải lớn hơn 0');
      return;
    }

    // Validate quantity against available stock
    const selectedProduct = products.find((p) => p.id === selectedProductId);
    const maxQuantity = selectedProduct?.quantityAvailable || 0;

    if (itemQuantity > maxQuantity) {
      toast.error(`Số lượng đặt hàng không được vượt quá số lượng còn tồn`);
      return;
    }

    try {
      const maxSeqId = Math.max(
        0,
        ...(order?.items?.map((i) => i.orderItemSeqId) || [])
      );

      await addProductToOrder({
        orderId,
        data: {
          productId: selectedProductId,
          orderItemSeqId: maxSeqId + 1,
          quantity: itemQuantity,
          tax: itemTax,
          discount: itemDiscount,
        },
      }).unwrap();

      toast.success('Thêm sản phẩm thành công');

      // Reset form
      setShowAddItemDialog(false);
      setSelectedProductId('');
      setProductSearch('');
      setItemQuantity(1);
      setItemTax(0);
      setItemDiscount(0);
    } catch (error: any) {
      console.error('Failed to add product to order:', error);
      toast.error(
        error?.data?.message || 'Không thể thêm sản phẩm vào đơn hàng'
      );
    }
  };

  const handleDeleteItem = async (orderItemId: string) => {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;

    try {
      await deleteProductFromOrder({ orderId, orderItemId }).unwrap();
      toast.success('Xóa sản phẩm thành công');
    } catch (error: any) {
      console.error('Failed to delete product from order:', error);
      toast.error(
        error?.data?.message || 'Không thể xóa sản phẩm khỏi đơn hàng'
      );
    }
  };

  const resetItemForm = () => {
    setShowAddItemDialog(false);
    setEditingItemId(null);
    setSelectedProductId('');
    setProductSearch('');
    setItemQuantity(1);
    setItemTax(0);
    setItemDiscount(0);
  };
  const formatCurrency = (value?: number) => {
    if (!value) return '0đ';
    return `${value.toLocaleString('vi-VN', {
      style: 'currency',
      currency: 'VND',
    })}`;
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <Card className='animate-pulse'>
          <CardContent className='p-8'>
            <div className='h-8 bg-muted rounded w-1/3 mb-4' />
            <div className='h-4 bg-muted rounded w-1/2' />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isError || !order) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>Đơn hàng không tồn tại</h3>
          <p className='text-muted-foreground mb-4'>
            Đơn hàng bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/sales/orders')}>
            Quay lại danh sách đơn hàng
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-4'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/sales/orders')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>
              {order.orderName || `Order #${order.id.slice(0, 8)}`}
            </h1>
            <p className='text-muted-foreground'>
              Đã tạo ngày {formatDate(order.createdStamp)}
            </p>
          </div>
        </div>

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
      </div>

      {/* Status Banner */}
      <Card
        className={cn(
          'border-2',
          statusConfig.bgColor,
          'transition-all duration-200'
        )}
      >
        <CardContent className='p-4'>
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-3'>
              <div
                className={cn(
                  'flex h-12 w-12 items-center justify-center rounded-xl',
                  statusConfig.bgColor
                )}
              >
                <StatusIcon className={cn('h-6 w-6', statusConfig.color)} />
              </div>
              <div>
                <p className='text-sm text-muted-foreground'>
                  Trạng thái đơn hàng
                </p>
                <p className={cn('text-lg font-semibold', statusConfig.color)}>
                  {statusConfig.label}
                </p>
              </div>
            </div>

            <div className='text-right'>
              <p className='text-sm text-muted-foreground'>Tổng thành tiền</p>
              <p className='text-2xl font-bold'>
                {formatCurrency(order.totalAmount)}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className='grid w-full grid-cols-3'>
          <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
          <TabsTrigger value='items'>Sản Phẩm</TabsTrigger>
          <TabsTrigger value='history'>Lịch sử</TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value='overview' className='space-y-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {/* Order Information */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <ShoppingCart className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Thông tin đơn hàng</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='flex items-start justify-between'>
                  <div className='flex items-center gap-2 text-sm'>
                    <FileText className='h-4 w-4 text-muted-foreground' />
                    <span className='text-muted-foreground'>Loại đơn hàng</span>
                  </div>
                  <Badge variant='secondary'>
                    {order.orderTypeId || 'N/A'}
                  </Badge>
                </div>

                <div className='flex items-start justify-between'>
                  <div className='flex items-center gap-2 text-sm'>
                    <Calendar className='h-4 w-4 text-muted-foreground' />
                    <span className='text-muted-foreground'>Ngày đặt hàng</span>
                  </div>
                  <span className='font-medium'>
                    {formatDate(order.orderDate)}
                  </span>
                </div>

                <div className='flex items-start justify-between'>
                  <div className='flex items-center gap-2 text-sm'>
                    <TrendingUp className='h-4 w-4 text-muted-foreground' />
                    <span className='text-muted-foreground'>Độ ưu tiên</span>
                  </div>
                  <span className='font-medium'>{order.priority || 'N/A'}</span>
                </div>

                <div className='flex items-start justify-between'>
                  <div className='flex items-center gap-2 text-sm'>
                    <ShoppingCart className='h-4 w-4 text-muted-foreground' />
                    <span className='text-muted-foreground'>Kênh bán hàng</span>
                  </div>
                  <Badge variant='secondary'>
                    {order.saleChannelId || 'N/A'}
                  </Badge>
                </div>
              </CardContent>
            </Card>

            {/* Customer & Delivery */}
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <User className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Khách hàng & Giao hàng</h3>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='flex items-start justify-between'>
                  <div className='flex items-center gap-2 text-sm'>
                    <User className='h-4 w-4 text-muted-foreground' />
                    <span className='text-muted-foreground'>Khách hàng</span>
                  </div>
                  {customer ? (
                    <button
                      onClick={() =>
                        router.push(`/sales/customers/${order.toCustomerId}`)
                      }
                      className='font-medium text-primary hover:underline transition-colors'
                    >
                      {customer.name}
                    </button>
                  ) : (
                    <span className='font-medium text-xs text-muted-foreground'>
                      {order.toCustomerId.slice(0, 16)}...
                    </span>
                  )}
                </div>

                {order.deliveryPhone && (
                  <div className='flex items-start justify-between'>
                    <div className='flex items-center gap-2 text-sm'>
                      <Phone className='h-4 w-4 text-muted-foreground' />
                      <span className='text-muted-foreground'>
                        Số điện thoại
                      </span>
                    </div>
                    <span className='font-medium'>{order.deliveryPhone}</span>
                  </div>
                )}

                {order.deliveryFullAddress && (
                  <div>
                    <div className='flex items-center gap-2 text-sm mb-2'>
                      <MapPin className='h-4 w-4 text-muted-foreground' />
                      <span className='text-muted-foreground'>
                        Địa chỉ giao hàng
                      </span>
                    </div>
                    <p className='text-sm pl-6'>{order.deliveryFullAddress}</p>
                  </div>
                )}

                {order.deliveryBeforeDate && (
                  <div className='flex items-start justify-between'>
                    <div className='flex items-center gap-2 text-sm'>
                      <Calendar className='h-4 w-4 text-muted-foreground' />
                      <span className='text-muted-foreground'>
                        Giao trước ngày
                      </span>
                    </div>
                    <span className='font-medium'>
                      {formatDate(order.deliveryBeforeDate)}
                    </span>
                  </div>
                )}

                {order.deliveryAfterDate && (
                  <div className='flex items-start justify-between'>
                    <div className='flex items-center gap-2 text-sm'>
                      <Calendar className='h-4 w-4 text-muted-foreground' />
                      <span className='text-muted-foreground'>
                        Giao sau ngày
                      </span>
                    </div>
                    <span className='font-medium'>
                      {formatDate(order.deliveryAfterDate)}
                    </span>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Notes */}
          {order.note && (
            <Card>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <FileText className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Ghi chú đơn hàng</h3>
                </div>
              </CardHeader>
              <CardContent>
                <p className='text-sm text-muted-foreground'>{order.note}</p>
              </CardContent>
            </Card>
          )}

          {/* Cancellation Note */}
          {order.statusId === 'CANCELLED' && order.cancellationNote && (
            <Card className='border-destructive/50 bg-destructive/5'>
              <CardHeader>
                <div className='flex items-center gap-2'>
                  <XCircle className='h-5 w-5 text-destructive' />
                  <h3 className='font-semibold text-destructive'>
                    Lý do hủy đơn hàng
                  </h3>
                </div>
              </CardHeader>
              <CardContent>
                <p className='text-sm text-destructive/80'>
                  {order.cancellationNote}
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Order Items Tab */}
        <TabsContent value='items'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Danh sách sản phẩm</h3>
                </div>
                {order.statusId === 'CREATED' && (
                  <Button
                    size='sm'
                    onClick={() => setShowAddItemDialog(true)}
                    className='gap-2'
                  >
                    <Plus className='h-4 w-4' />
                    Thêm sản phẩm
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {order.items && order.items.length > 0 ? (
                <div className='space-y-3'>
                  {order.items.map((item) => (
                    <div
                      key={item.id}
                      className='group flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors'
                    >
                      <div className='flex-1'>
                        <p className='font-medium'>
                          {item.product?.name ||
                            `Product ${item.productId.slice(0, 8)}`}
                        </p>
                        <div className='flex items-center gap-4 mt-1 text-sm text-muted-foreground'>
                          <span>Số lượng: {item.quantity}</span>
                          <span>Đơn vị: {item.unit}</span>
                          <span>Giá: {formatCurrency(item.price)}</span>
                          {item.discount !== 0 && (
                            <span className='text-green-600'>
                              Giảm: {formatCurrency(item.discount)}
                            </span>
                          )}
                          {item.tax !== 0 && (
                            <span className='text-amber-600'>
                              Thuế: {item.tax}%
                            </span>
                          )}
                        </div>
                      </div>
                      <div className='flex items-center gap-3'>
                        <div className='text-right'>
                          <p className='font-bold text-lg'>
                            {formatCurrency(item.amount)}
                          </p>
                        </div>
                        {order.statusId === 'CREATED' && (
                          <Button
                            variant='ghost'
                            size='icon'
                            onClick={() => handleDeleteItem(item.id)}
                            disabled={isDeletingProduct}
                            className='opacity-0 group-hover:opacity-100 transition-opacity text-destructive hover:text-destructive hover:bg-destructive/10'
                          >
                            <Trash2 className='h-4 w-4' />
                          </Button>
                        )}
                      </div>
                    </div>
                  ))}

                  {/* Summary */}
                  <div className='border-t pt-4 mt-4'>
                    <div className='space-y-2 text-sm'>
                      <div className='flex items-center justify-between'>
                        <span className='text-muted-foreground'>
                          Số sản phẩm
                        </span>
                        <span className='font-medium'>
                          {order.items.length} sản phẩm
                        </span>
                      </div>
                      <div className='flex items-center justify-between text-lg font-bold pt-2 border-t'>
                        <span>Tổng thành tiền</span>
                        <span>{formatCurrency(order.totalAmount)}</span>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className='text-center py-12'>
                  <Package className='h-16 w-16 mx-auto mb-4 text-muted-foreground/50' />
                  <h3 className='text-lg font-semibold mb-2'>
                    Chưa có sản phẩm
                  </h3>
                  <p className='text-muted-foreground mb-4'>
                    Đơn hàng này chưa có sản phẩm nào
                  </p>
                  {order.statusId === 'CREATED' && (
                    <Button
                      onClick={() => setShowAddItemDialog(true)}
                      className='gap-2'
                    >
                      <Plus className='h-4 w-4' />
                      Thêm sản phẩm đầu tiên
                    </Button>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* History Tab */}
        <TabsContent value='history'>
          <Card>
            <CardHeader>
              <div className='flex items-center gap-2'>
                <Clock className='h-5 w-5 text-primary' />
                <h3 className='font-semibold'>Lịch sử đơn hàng</h3>
              </div>
            </CardHeader>
            <CardContent>
              <div className='space-y-4'>
                <div className='flex gap-4'>
                  <div className='flex flex-col items-center'>
                    <div className='flex h-10 w-10 items-center justify-center rounded-full bg-primary/10'>
                      <Clock className='h-5 w-5 text-primary' />
                    </div>
                    <div className='w-px h-full bg-border mt-2' />
                  </div>
                  <div className='flex-1 pb-8'>
                    <p className='font-medium'>Đơn hàng được tạo</p>
                    <p className='text-sm text-muted-foreground'>
                      Đơn hàng được tạo bởi{' '}
                      {order.createdByUserId
                        ? formatFullname(userMap.get(order.createdByUserId))
                        : 'N/A'}
                    </p>
                    <p className='text-sm text-muted-foreground'>
                      {formatDate(order.createdStamp)}
                    </p>
                  </div>
                </div>

                {order.statusId !== 'CREATED' && (
                  <div className='flex gap-4'>
                    <div className='flex flex-col items-center'>
                      <div className='flex h-10 w-10 items-center justify-center rounded-full bg-purple-500/10'>
                        <StatusIcon className='h-5 w-5 text-purple-500' />
                      </div>
                    </div>
                    <div className='flex-1'>
                      <p className='font-medium'>
                        {order.statusId === 'APPROVED'
                          ? 'Đơn hàng được phê duyệt'
                          : 'Đơn hàng bị hủy'}
                      </p>
                      <p className='text-sm text-muted-foreground'>
                        Trạng thái đơn hàng thay đổi thành{' '}
                        <i>{statusConfig.label}</i> bởi{' '}
                        {order.statusId === 'APPROVED'
                          ? order.userApprovedId
                            ? formatFullname(userMap.get(order.userApprovedId))
                            : 'N/A'
                          : order.userCancelledId
                            ? formatFullname(userMap.get(order.userCancelledId))
                            : 'N/A'}
                      </p>
                      <p className='text-xs text-muted-foreground mt-1'>
                        {formatDate(order.lastUpdatedStamp)}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Cancel Order Dialog */}
      {showCancelDialog && (
        <div className='fixed inset-0 z-50 bg-background/80 backdrop-blur-sm'>
          <div className='fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md'>
            <Card>
              <CardHeader>
                <h3 className='text-lg font-semibold'>Hủy đơn hàng</h3>
                <p className='text-sm text-muted-foreground'>
                  Vui lòng cung cấp lý do hủy
                </p>
              </CardHeader>
              <CardContent className='space-y-4'>
                <textarea
                  value={cancellationNote}
                  onChange={(e) => setCancellationNote(e.target.value)}
                  placeholder='Enter cancellation reason...'
                  className='w-full min-h-[100px] px-3 py-2 border rounded-lg bg-background resize-none'
                />
                <div className='flex justify-end gap-2'>
                  <Button
                    variant='outline'
                    onClick={() => setShowCancelDialog(false)}
                  >
                    Đóng
                  </Button>
                  <Button
                    variant='destructive'
                    onClick={handleCancel}
                    disabled={isCancelling}
                  >
                    Hủy đơn hàng
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      )}

      {/* Add Item Dialog */}
      {showAddItemDialog && (
        <div className='fixed inset-0 z-50 bg-background/80 backdrop-blur-sm'>
          <div className='fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl max-h-[90vh] overflow-auto'>
            <Card>
              <CardHeader>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2'>
                    <Plus className='h-5 w-5 text-primary' />
                    <h3 className='text-lg font-semibold'>
                      Thêm sản phẩm vào đơn hàng
                    </h3>
                  </div>
                  <Button variant='ghost' size='icon' onClick={resetItemForm}>
                    <CloseIcon className='h-4 w-4' />
                  </Button>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                {/* Product Search */}
                <div className='space-y-2'>
                  <Label htmlFor='productSearch'>
                    Tìm kiếm sản phẩm{' '}
                    <span className='text-destructive'>*</span>
                  </Label>
                  <div className='relative'>
                    <div className='relative'>
                      <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                      <Input
                        id='productSearch'
                        placeholder='Tìm kiếm theo tên, SKU...'
                        value={productSearch}
                        onChange={(e) => {
                          setProductSearch(e.target.value);
                          setShowProductDropdown(true);
                        }}
                        onFocus={() => setShowProductDropdown(true)}
                        className='pl-10'
                      />
                    </div>

                    {showProductDropdown && products.length > 0 && (
                      <div className='absolute z-10 w-full mt-1 bg-popover border rounded-lg shadow-lg max-h-60 overflow-auto'>
                        {products.map((product) => (
                          <button
                            key={product.id}
                            type='button'
                            onClick={() => {
                              setSelectedProductId(product.id);
                              setProductSearch(product.name);
                              setShowProductDropdown(false);
                            }}
                            className={cn(
                              'w-full px-4 py-3 text-left hover:bg-muted transition-colors',
                              selectedProductId === product.id && 'bg-muted'
                            )}
                          >
                            <div className='flex items-center justify-between'>
                              <div className='flex-1'>
                                <p className='font-medium'>{product.name}</p>
                                <p className='text-sm text-muted-foreground'>
                                  SKU: {product.skuCode} • Giá:{' '}
                                  {product.retailPrice?.toLocaleString() || 0}đ
                                </p>
                              </div>
                              {selectedProductId === product.id && (
                                <CheckCircle2 className='h-4 w-4 text-primary' />
                              )}
                            </div>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                  {selectedProductId && (
                    <Badge variant='secondary' className='mt-2'>
                      Đã chọn:{' '}
                      {products.find((p) => p.id === selectedProductId)?.name}
                    </Badge>
                  )}
                </div>

                {/* Quantity, Tax, Discount */}
                <div className='grid grid-cols-3 gap-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='quantity'>
                      Số lượng <span className='text-destructive'>*</span>
                    </Label>
                    <Input
                      id='quantity'
                      type='number'
                      min='1'
                      max={
                        products.find((p) => p.id === selectedProductId)
                          ?.quantityAvailable || 1
                      }
                      value={itemQuantity}
                      onChange={(e) =>
                        setItemQuantity(parseInt(e.target.value) || 1)
                      }
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='tax'>Thuế (%)</Label>
                    <Input
                      id='tax'
                      type='number'
                      min='0'
                      max='100'
                      step='0.1'
                      value={itemTax}
                      onChange={(e) =>
                        setItemTax(parseFloat(e.target.value) || 0)
                      }
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='discount'>Giảm giá (đ)</Label>
                    <Input
                      id='discount'
                      type='number'
                      min='0'
                      step='1000'
                      value={itemDiscount}
                      onChange={(e) =>
                        setItemDiscount(parseFloat(e.target.value) || 0)
                      }
                    />
                  </div>
                </div>

                {/* Preview */}
                {selectedProductId && (
                  <div className='p-4 bg-muted/50 rounded-lg space-y-2'>
                    <p className='text-sm font-medium text-muted-foreground'>
                      Xem trước
                    </p>
                    <div className='space-y-1 text-sm'>
                      <div className='flex justify-between font-bold'>
                        <span>Sản phẩm:</span>
                        <span>
                          {
                            products.find((p) => p.id === selectedProductId)
                              ?.name
                          }
                        </span>
                      </div>
                      <div className='flex justify-between font-bold'>
                        <span>Số lượng còn tồn:</span>
                        <span>
                          {
                            products.find((p) => p.id === selectedProductId)
                              ?.quantityAvailable
                          }
                        </span>
                      </div>
                      <div className='flex justify-between'>
                        <span>Số lượng đặt hàng:</span>
                        <span className='font-medium'>{itemQuantity}</span>
                      </div>
                      {itemTax > 0 && (
                        <div className='flex justify-between text-amber-600'>
                          <span>Thuế:</span>
                          <span className='font-medium'>{itemTax}%</span>
                        </div>
                      )}
                      {itemDiscount > 0 && (
                        <div className='flex justify-between text-green-600'>
                          <span>Giảm giá:</span>
                          <span className='font-medium'>
                            {formatCurrency(itemDiscount)}
                          </span>
                        </div>
                      )}
                    </div>
                    <div className='flex justify-between font-bold pt-2 border-t'>
                      <span>Thành tiền:</span>
                      <span>
                        {(() => {
                          const product = products.find(
                            (p) => p.id === selectedProductId
                          );
                          if (!product?.wholeSalePrice) return '0đ';
                          const subtotal =
                            product.wholeSalePrice * itemQuantity;
                          const afterDiscount = subtotal - itemDiscount;
                          const total = afterDiscount * (1 + itemTax / 100);
                          return formatCurrency(total);
                        })()}
                      </span>
                    </div>
                  </div>
                )}

                {/* Actions */}
                <div className='flex justify-end gap-2 pt-4'>
                  <Button
                    type='button'
                    variant='outline'
                    onClick={resetItemForm}
                  >
                    Hủy
                  </Button>
                  <Button
                    onClick={handleAddItem}
                    disabled={!selectedProductId || isAddingProduct}
                    className='gap-2'
                  >
                    {isAddingProduct ? (
                      <>Đang thêm...</>
                    ) : (
                      <>
                        <Plus className='h-4 w-4' />
                        Thêm sản phẩm
                      </>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderDetailPage;
