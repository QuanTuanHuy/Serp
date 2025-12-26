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
} from '../../api/salesApi';
import { formatDate, formatCurrency } from '@/shared/utils/format';

interface CustomerDetailPageProps {
  customerId: string;
}

// Customer status configuration
const STATUS_CONFIG = {
  ACTIVE: {
    label: 'Active',
    color: 'text-green-700 dark:text-green-300',
    bgColor: 'bg-green-100 dark:bg-green-900/50',
  },
  INACTIVE: {
    label: 'Inactive',
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

  const handleEdit = () => {
    router.push(`/sales/customers/${customerId}/edit`);
  };

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this customer?')) return;

    try {
      await deleteCustomer(customerId).unwrap();
      router.push('/sales/customers');
    } catch (error) {
      console.error('Failed to delete customer:', error);
    }
  };

  if (isLoading) {
    return (
      <div className='flex items-center justify-center min-h-[400px]'>
        <div className='text-center'>
          <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4'></div>
          <p className='text-muted-foreground'>Loading customer details...</p>
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
              The customer you're looking for doesn't exist or has been deleted.
            </p>
            <Button variant='outline' onClick={() => router.back()}>
              Go Back
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
                  <span>Joined {formatDate(customer.createdStamp)}</span>
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
              Edit Customer
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleDelete}
              className='text-destructive focus:text-destructive'
            >
              <Trash2 className='mr-2 h-4 w-4' />
              Delete Customer
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
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='orders'>Orders</TabsTrigger>
          <TabsTrigger value='activity'>Activity</TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value='overview' className='space-y-4'>
          <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
            {/* Contact Information */}
            <Card>
              <CardHeader className='pb-3'>
                <h3 className='font-semibold text-base'>Contact Information</h3>
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
                <h3 className='font-semibold text-base'>Statistics</h3>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                    <ShoppingCart className='h-4 w-4' />
                    <span>Total Orders</span>
                  </div>
                  <span className='font-semibold'>0</span>
                </div>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                    <DollarSign className='h-4 w-4' />
                    <span>Total Revenue</span>
                  </div>
                  <span className='font-semibold'>{formatCurrency(0)}</span>
                </div>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                    <Package className='h-4 w-4' />
                    <span>Products Purchased</span>
                  </div>
                  <span className='font-semibold'>0</span>
                </div>
              </CardContent>
            </Card>

            {/* Additional Info */}
            <Card>
              <CardHeader className='pb-3'>
                <h3 className='font-semibold text-base'>Additional Info</h3>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>Tenant ID</span>
                  <span className='font-medium'>{customer.tenantId}</span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>Created</span>
                  <span className='font-medium'>
                    {formatDate(customer.createdStamp)}
                  </span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>Last Updated</span>
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
          <Card>
            <CardContent className='p-6'>
              <div className='text-center py-8'>
                <ShoppingCart className='h-12 w-12 text-muted-foreground mx-auto mb-4' />
                <h3 className='text-lg font-semibold mb-2'>No Orders Yet</h3>
                <p className='text-muted-foreground mb-4'>
                  This customer hasn't placed any orders yet.
                </p>
                <Button>Create Order</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Activity Tab */}
        <TabsContent value='activity' className='space-y-4'>
          <Card>
            <CardContent className='p-6'>
              <div className='text-center py-8'>
                <History className='h-12 w-12 text-muted-foreground mx-auto mb-4' />
                <h3 className='text-lg font-semibold mb-2'>No Activity</h3>
                <p className='text-muted-foreground'>
                  No activity recorded for this customer yet.
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default CustomerDetailPage;
