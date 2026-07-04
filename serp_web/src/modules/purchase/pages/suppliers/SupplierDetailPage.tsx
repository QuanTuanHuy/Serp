'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
  Avatar,
  AvatarFallback,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  MoreHorizontal,
  Edit,
  Trash2,
  Phone,
  Mail,
  MapPin,
  Calendar,
  User,
  ShoppingCart,
  DollarSign,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetSupplierQuery,
  useDeleteSupplierMutation,
  useGetOrdersQuery,
} from '../../api/purchaseApi';
import { formatDate, formatCurrency } from '@/shared/utils/format';
import { toast } from 'sonner';
import { UpdateAddressDialog } from '../../components/dialogs/UpdateAddressDialog';
import type { Order } from '../../types';
import { useUser } from '@/modules/account';

interface SupplierDetailPageProps {
  supplierId: string;
}

const STATUS_CONFIG = {
  ACTIVE: {
    label: 'Hoạt động',
    color: 'text-green-700 dark:text-green-300',
    bgColor: 'bg-green-100 dark:bg-green-900/50',
  },
  INACTIVE: {
    label: 'Không hoạt động',
    color: 'text-gray-700 dark:text-gray-300',
    bgColor: 'bg-gray-100 dark:bg-gray-800',
  },
};

const ORDER_STATUS_LABELS: Record<string, string> = {
  CREATED: 'Chờ phê duyệt',
  APPROVED: 'Đã duyệt',
  CANCELLED: 'Đã hủy',
  FULLY_DELIVERED: 'Đã giao',
};

const ORDER_STATUS_STYLES: Record<
  string,
  { bg: string; text: string; dot: string }
> = {
  CREATED: {
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
  },
  APPROVED: {
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
  },
  CANCELLED: {
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
  },
  FULLY_DELIVERED: {
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
  },
};

const OrderCard = ({
  order,
  onClick,
}: {
  order: Order;
  onClick?: () => void;
}) => {
  const statusStyle =
    ORDER_STATUS_STYLES[order.statusId] || ORDER_STATUS_STYLES.CREATED;

  return (
    <Card
      className='group hover:shadow-md hover:border-primary/20 transition-all duration-200 cursor-pointer'
      onClick={onClick}
    >
      <CardContent className='p-4'>
        <div className='flex items-start justify-between mb-3'>
          <div className='min-w-0'>
            <p className='font-semibold text-foreground truncate'>
              {order.orderName || `Đơn hàng #${order.id.slice(0, 8)}`}
            </p>
            <div className='flex items-center gap-1 mt-1 text-xs text-muted-foreground'>
              <Calendar className='h-3 w-3' />
              <span>{formatDate(order.orderDate)}</span>
            </div>
          </div>
          <Badge
            variant='secondary'
            className={cn('gap-1 shrink-0', statusStyle.bg, statusStyle.text)}
          >
            <span className={cn('h-1.5 w-1.5 rounded-full', statusStyle.dot)} />
            {ORDER_STATUS_LABELS[order.statusId] || order.statusId}
          </Badge>
        </div>

        <div className='flex items-center justify-between text-sm'>
          <div className='flex items-center gap-1 text-muted-foreground'>
            <ShoppingCart className='h-3.5 w-3.5' />
            <span>{order.totalQuantity ?? 0} sản phẩm</span>
          </div>
          <span className='font-semibold text-foreground'>
            {formatCurrency(order.totalAmount)}
          </span>
        </div>
      </CardContent>
    </Card>
  );
};

export const SupplierDetailPage: React.FC<SupplierDetailPageProps> = ({
  supplierId,
}) => {
  const router = useRouter();

  const { user } = useUser();
  const hasEditPermission =
    user?.roles?.includes('PURCHASE_MANAGER') ||
    user?.roles?.includes('PURCHASE_ADMIN');

  const [activeTab, setActiveTab] = useState('overview');
  const [isAddressDialogOpen, setIsAddressDialogOpen] = useState(false);

  const {
    data: supplierResponse,
    isLoading,
    isError,
  } = useGetSupplierQuery(supplierId);
  const [deleteSupplier, { isLoading: isDeleting }] =
    useDeleteSupplierMutation();

  const supplier = supplierResponse?.data;

  const {
    data: ordersResponse,
    isLoading: isLoadingOrders,
    error: ordersError,
  } = useGetOrdersQuery({
    filters: { fromSupplierId: supplierId },
    pagination: { page: 0, size: 100 },
  });

  const orders = ordersResponse?.data?.items || [];

  const handleEdit = () => {
    router.push(`/purchase/suppliers/${supplierId}/edit`);
  };

  const handleDelete = async () => {
    if (!confirm('Bạn có chắc chắn muốn xóa nhà cung cấp này không?')) return;

    try {
      await deleteSupplier(supplierId).unwrap();
      router.push('/purchase/suppliers');
    } catch (error) {
      console.error('Lỗi khi xóa nhà cung cấp:', error);
      toast.error('Đã xảy ra lỗi khi xóa nhà cung cấp. Vui lòng thử lại.');
    }
  };

  if (isLoading) {
    return (
      <div className='flex items-center justify-center min-h-[400px]'>
        <div className='text-center'>
          <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4'></div>
          <p className='text-muted-foreground'>
            Đang tải thông tin nhà cung cấp...
          </p>
        </div>
      </div>
    );
  }

  if (isError || !supplier) {
    return (
      <div className='p-6'>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
              Không tìm thấy nhà cung cấp
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
              Nhà cung cấp bạn đang tìm không tồn tại hoặc đã bị xóa.
            </p>
            <Button variant='outline' onClick={() => router.back()}>
              Quay lại
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const statusConfig =
    STATUS_CONFIG[supplier.statusId as keyof typeof STATUS_CONFIG] ||
    STATUS_CONFIG.INACTIVE;

  return (
    <div className='p-6 space-y-6'>
      {/* Header */}
      <div className='flex items-start justify-between'>
        <div className='flex items-start gap-4'>
          <Button variant='outline' size='icon' onClick={() => router.back()}>
            <ArrowLeft className='h-4 w-4' />
          </Button>

          <div className='flex items-start gap-4'>
            <Avatar className='h-16 w-16'>
              <AvatarFallback className='text-xl'>
                {supplier.name.charAt(0).toUpperCase()}
              </AvatarFallback>
            </Avatar>

            <div>
              <div className='flex items-center gap-2 mb-2'>
                <h1 className='text-2xl font-bold text-foreground'>
                  {supplier.name}
                </h1>
                <Badge
                  className={cn(
                    statusConfig.bgColor,
                    statusConfig.color,
                    'border-0'
                  )}
                >
                  {statusConfig.label}
                </Badge>
              </div>

              <div className='flex items-center gap-4 text-sm text-muted-foreground'>
                <div className='flex items-center gap-1'>
                  <User className='h-4 w-4' />
                  <span>ID: {supplier.id}</span>
                </div>
                <div className='flex items-center gap-1'>
                  <Calendar className='h-4 w-4' />
                  <span>Tham gia ngày {formatDate(supplier.createdStamp)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {hasEditPermission && (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon' disabled={isDeleting}>
                <MoreHorizontal className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem onClick={handleEdit}>
                <Edit className='mr-2 h-4 w-4' />
                Chỉnh sửa nhà cung cấp
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setIsAddressDialogOpen(true)}>
                <MapPin className='mr-2 h-4 w-4' />
                Thay đổi địa chỉ
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={handleDelete}
                className='text-destructive focus:text-destructive'
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Xóa nhà cung cấp
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )}
      </div>

      {/* Tabs */}
      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList>
          <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
          <TabsTrigger value='orders'>Đơn hàng</TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value='overview' className='space-y-4'>
          <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
            {/* Contact Information */}
            <Card>
              <CardHeader className='pb-3'>
                <h3 className='font-semibold text-base'>Thông tin liên hệ</h3>
              </CardHeader>
              <CardContent className='space-y-3'>
                {supplier.email && (
                  <div className='flex items-center gap-2 text-sm'>
                    <Mail className='h-4 w-4 text-muted-foreground' />
                    <span className='text-foreground'>{supplier.email}</span>
                  </div>
                )}
                {supplier.phone && (
                  <div className='flex items-center gap-2 text-sm'>
                    <Phone className='h-4 w-4 text-muted-foreground' />
                    <span className='text-foreground'>{supplier.phone}</span>
                  </div>
                )}
                {supplier.address && (
                  <div className='flex items-start gap-2 text-sm'>
                    <MapPin className='h-4 w-4 text-muted-foreground mt-0.5' />
                    <span className='text-foreground'>
                      {supplier.address.fullAddress}
                    </span>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Supplier Stats */}
            <Card>
              <CardHeader className='pb-3'>
                <h3 className='font-semibold text-base'>Thống kê</h3>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                    <ShoppingCart className='h-4 w-4' />
                    <span>Tổng số đơn hàng</span>
                  </div>
                  <span className='font-semibold'>{orders.length}</span>
                </div>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                    <DollarSign className='h-4 w-4' />
                    <span>Tổng chi phí</span>
                  </div>
                  <span className='font-semibold'>
                    {formatCurrency(
                      orders.reduce(
                        (total, order) => total + order.totalAmount,
                        0
                      )
                    )}
                  </span>
                </div>
              </CardContent>
            </Card>

            {/* Additional Info */}
            <Card>
              <CardHeader className='pb-3'>
                <h3 className='font-semibold text-base'>Thông tin bổ sung</h3>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>Tham gia vào</span>
                  <span className='font-medium'>
                    {formatDate(supplier.createdStamp)}
                  </span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>
                    Cập nhật lần cuối
                  </span>
                  <span className='font-medium'>
                    {formatDate(supplier.lastUpdatedStamp)}
                  </span>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Orders Tab */}
        <TabsContent value='orders' className='space-y-4'>
          {isLoadingOrders && (
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

          {ordersError && (
            <Card className='border-destructive/50 bg-destructive/5'>
              <CardContent className='p-6 text-center'>
                <p className='text-destructive'>
                  Đã xảy ra lỗi khi tải đơn hàng. Vui lòng thử lại sau.
                </p>
              </CardContent>
            </Card>
          )}

          {!isLoadingOrders && !ordersError && orders.length > 0 && (
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <div>
                  <h3 className='text-lg font-semibold'>Danh sách đơn hàng</h3>
                  <p className='text-sm text-muted-foreground'>
                    {orders.length} đơn hàng
                  </p>
                </div>
                <Button
                  onClick={() =>
                    router.push(
                      `/purchase/purchase-orders/new?supplierId=${supplierId}`
                    )
                  }
                >
                  <ShoppingCart className='h-4 w-4 mr-2' />
                  Tạo đơn hàng mới
                </Button>
              </div>

              <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
                {orders.map((order: Order) => (
                  <OrderCard
                    key={order.id}
                    order={order}
                    onClick={() =>
                      router.push(`/purchase/purchase-orders/${order.id}`)
                    }
                  />
                ))}
              </div>
            </div>
          )}

          {!isLoadingOrders && !ordersError && orders.length === 0 && (
            <Card>
              <CardContent className='py-16 text-center'>
                <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
                  <ShoppingCart className='w-10 h-10 text-muted-foreground' />
                </div>
                <h3 className='text-lg font-semibold mb-2'>Chưa có đơn hàng</h3>
                <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
                  Nhà cung cấp này chưa có đơn hàng nào. Tạo đơn hàng đầu tiên
                  ngay bây giờ.
                </p>
                <Button
                  onClick={() =>
                    router.push(
                      `/purchase/purchase-orders/new?supplierId=${supplierId}`
                    )
                  }
                >
                  <ShoppingCart className='h-4 w-4 mr-2' />
                  Tạo đơn hàng đầu tiên
                </Button>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>

      <UpdateAddressDialog
        open={isAddressDialogOpen}
        onOpenChange={setIsAddressDialogOpen}
        entityId={supplierId}
        entityType='SUPPLIER'
        currentAddress={supplier.address}
      />
    </div>
  );
};

export default SupplierDetailPage;
