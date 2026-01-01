/*
Author: QuanTuanHuy
Description: Part of Serp Project - Sales Customer Detail Page
*/

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
  Package,
  Receipt,
  DollarSign,
  TrendingUp,
  History,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetCustomerQuery,
  useDeleteCustomerMutation,
  useGetOrdersQuery,
} from '../../api/salesApi';
import { formatDate, formatCurrency } from '@/shared/utils/format';
import { toast } from 'sonner';
import { OrderCard } from '../../components/cards/OrderCard';
import type { Order } from '../../types';

interface CustomerDetailPageProps {
  customerId: string;
}

// Customer status configuration
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

export const CustomerDetailPage: React.FC<CustomerDetailPageProps> = ({
  customerId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');

  // Fetch customer data
  const {
    data: customerResponse,
    isLoading,
    isError,
  } = useGetCustomerQuery(customerId);
  const [deleteCustomer, { isLoading: isDeleting }] =
    useDeleteCustomerMutation();

  const customer = customerResponse?.data;

  const {
    data: ordersResponse,
    isLoading: isLoadingOrders,
    error: ordersError,
  } = useGetOrdersQuery({
    filters: { toCustomerId: customerId },
    pagination: { page: 0, size: 100 },
  });

  const orders = ordersResponse?.data?.items || [];

  const handleEdit = () => {
    router.push(`/sales/customers/${customerId}/edit`);
  };

  const handleDelete = async () => {
    if (!confirm('Bạn có chắc chắn muốn xóa khách hàng này không?')) return;

    try {
      await deleteCustomer(customerId).unwrap();
      router.push('/sales/customers');
    } catch (error) {
      console.error('Lỗi khi xóa khách hàng:', error);
      toast.error('Đã xảy ra lỗi khi xóa khách hàng. Vui lòng thử lại.');
    }
  };

  if (isLoading) {
    return (
      <div className='flex items-center justify-center min-h-[400px]'>
        <div className='text-center'>
          <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4'></div>
          <p className='text-muted-foreground'>
            Đang tải thông tin khách hàng...
          </p>
        </div>
      </div>
    );
  }

  if (isError || !customer) {
    return (
      <div className='p-6'>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
              Customer Not Found
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
              Khách hàng bạn đang tìm không tồn tại hoặc đã bị xóa.
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
    STATUS_CONFIG[customer.statusId as keyof typeof STATUS_CONFIG] ||
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
                {customer.name.charAt(0).toUpperCase()}
              </AvatarFallback>
            </Avatar>

            <div>
              <div className='flex items-center gap-2 mb-2'>
                <h1 className='text-2xl font-bold text-foreground'>
                  {customer.name}
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
                  <span>ID: {customer.id}</span>
                </div>
                <div className='flex items-center gap-1'>
                  <Calendar className='h-4 w-4' />
                  <span>Tham gia ngày {formatDate(customer.createdStamp)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant='outline' size='icon' disabled={isDeleting}>
              <MoreHorizontal className='h-4 w-4' />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuItem onClick={handleEdit}>
              <Edit className='mr-2 h-4 w-4' />
              Chỉnh sửa khách hàng
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleDelete}
              className='text-destructive focus:text-destructive'
            >
              <Trash2 className='mr-2 h-4 w-4' />
              Xóa khách hàng
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
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
                {customer.email && (
                  <div className='flex items-center gap-2 text-sm'>
                    <Mail className='h-4 w-4 text-muted-foreground' />
                    <span className='text-foreground'>{customer.email}</span>
                  </div>
                )}
                {customer.phone && (
                  <div className='flex items-center gap-2 text-sm'>
                    <Phone className='h-4 w-4 text-muted-foreground' />
                    <span className='text-foreground'>{customer.phone}</span>
                  </div>
                )}
                {customer.address && (
                  <div className='flex items-start gap-2 text-sm'>
                    <MapPin className='h-4 w-4 text-muted-foreground mt-0.5' />
                    <span className='text-foreground'>
                      {customer.address.fullAddress}
                    </span>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Customer Stats - Placeholder */}
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
                    <span>Tổng doanh thu</span>
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
                    {formatDate(customer.createdStamp)}
                  </span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>
                    Cập nhật lần cuối
                  </span>
                  <span className='font-medium'>
                    {formatDate(customer.lastUpdatedStamp)}
                  </span>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Orders Tab */}
        <TabsContent value='orders' className='space-y-4'>
          {/* Loading State */}
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

          {/* Error State */}
          {ordersError && (
            <Card className='border-destructive/50 bg-destructive/5'>
              <CardContent className='p-6 text-center'>
                <p className='text-destructive'>
                  Đã xảy ra lỗi khi tải đơn hàng. Vui lòng thử lại sau.
                </p>
              </CardContent>
            </Card>
          )}

          {/* Orders List */}
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
                    router.push(`/sales/orders/new?customerId=${customerId}`)
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
                    onClick={() => router.push(`/sales/orders/${order.id}`)}
                    customer={customer}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Empty State */}
          {!isLoadingOrders && !ordersError && orders.length === 0 && (
            <Card>
              <CardContent className='py-16 text-center'>
                <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
                  <ShoppingCart className='w-10 h-10 text-muted-foreground' />
                </div>
                <h3 className='text-lg font-semibold mb-2'>Chưa có đơn hàng</h3>
                <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
                  Khách hàng này chưa đặt đơn hàng nào. Tạo đơn hàng đầu tiên
                  ngay bây giờ.
                </p>
                <Button
                  onClick={() =>
                    router.push(`/sales/orders/new?customerId=${customerId}`)
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
    </div>
  );
};

export default CustomerDetailPage;
