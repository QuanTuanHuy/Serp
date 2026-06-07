/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Facility Detail Page
*/

'use client';

import { useMemo, useState } from 'react';
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
  MapPin,
  Calendar,
  Warehouse,
  Ruler,
  Hash,
  Package,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetFacilityQuery,
  useDeleteFacilityMutation,
  useGetInventoryItemsQuery,
  useGetProductsQuery,
} from '../../api/logisticsApi';
import { formatDateStringVN, formatPhoneNumber } from '@/shared/utils/format';
import { toast } from 'sonner';
import { UpdateAddressDialog } from '../../components/dialogs/UpdateAddressDialog';
import { InventoryItemCard } from '../../components/cards/InventoryItemCard';
import type { InventoryItem } from '../../types';

interface FacilityDetailPageProps {
  facilityId: string;
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

export const FacilityDetailPage: React.FC<FacilityDetailPageProps> = ({
  facilityId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [isAddressDialogOpen, setIsAddressDialogOpen] = useState(false);
  const [inventoryPage, setInventoryPage] = useState(0);
  const INVENTORY_PAGE_SIZE = 9;

  const {
    data: facilityResponse,
    isLoading,
    isError,
  } = useGetFacilityQuery(facilityId);

  const [deleteFacility, { isLoading: isDeleting }] =
    useDeleteFacilityMutation();

  const facility = facilityResponse?.data;

  const {
    data: inventoryResponse,
    isLoading: isLoadingInventory,
    error: inventoryError,
  } = useGetInventoryItemsQuery({
    filters: { facilityId },
    pagination: {
      page: inventoryPage,
      size: INVENTORY_PAGE_SIZE,
      sortBy: 'expirationDate',
    },
  });

  const inventoryItems = inventoryResponse?.data?.items || [];
  const inventoryTotalItems = inventoryResponse?.data?.totalItems || 0;
  const inventoryTotalPages = inventoryResponse?.data?.totalPages || 0;
  const inventoryCurrentPage = inventoryResponse?.data?.currentPage || 0;

  const productIds = useMemo(() => {
    return Array.from(
      new Set(
        inventoryItems.map((i: InventoryItem) => i.productId).filter(Boolean)
      )
    );
  }, [inventoryItems]);

  const { data: productsResponse } = useGetProductsQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: productIds.length === 0 }
  );

  const productMap = useMemo(() => {
    const map = new Map();
    productsResponse?.data?.items?.forEach((product) => {
      map.set(product.id, product);
    });
    return map;
  }, [productsResponse]);

  const handleEdit = () => {
    router.push(`/sales/facility/${facilityId}/edit`);
  };

  const handleDelete = async () => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa kho hàng này không?'))
      return;

    try {
      await deleteFacility(facilityId).unwrap();
      router.push('/sales/facility');
    } catch (error) {
      console.error('Lỗi khi xóa kho hàng:', error);
      toast.error('Đã xảy ra lỗi khi xóa kho hàng. Vui lòng thử lại.');
    }
  };

  if (isLoading) {
    return (
      <div className='flex items-center justify-center min-h-[400px]'>
        <div className='text-center'>
          <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4'></div>
          <p className='text-muted-foreground'>
            Đang tải thông tin kho hàng...
          </p>
        </div>
      </div>
    );
  }

  if (isError || !facility) {
    return (
      <div className='p-6'>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
              Không tìm thấy kho hàng
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
              Kho hàng bạn đang tìm không tồn tại hoặc đã bị xóa.
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
    STATUS_CONFIG[facility.statusId as keyof typeof STATUS_CONFIG] ||
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
                {facility.name.charAt(0).toUpperCase()}
              </AvatarFallback>
            </Avatar>

            <div>
              <div className='flex items-center gap-2 mb-2'>
                <h1 className='text-2xl font-bold text-foreground'>
                  {facility.name}
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
                  <Warehouse className='h-4 w-4' />
                  <span>ID: {facility.id}</span>
                </div>
                <div className='flex items-center gap-1'>
                  <Calendar className='h-4 w-4' />
                  <span>
                    Tạo ngày {formatDateStringVN(facility.createdStamp)}
                  </span>
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
              Chỉnh sửa kho hàng
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
              Xóa kho hàng
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
          <TabsTrigger value='inventory'>Mặt hàng</TabsTrigger>
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
                {facility.phone && (
                  <div className='flex items-center gap-2 text-sm'>
                    <Phone className='h-4 w-4 text-muted-foreground' />
                    <span className='text-foreground'>
                      {formatPhoneNumber(facility.phone)}
                    </span>
                  </div>
                )}
                {facility.postalCode && (
                  <div className='flex items-center gap-2 text-sm'>
                    <Hash className='h-4 w-4 text-muted-foreground' />
                    <span className='text-foreground'>
                      Mã bưu chính: {facility.postalCode}
                    </span>
                  </div>
                )}
                {facility.address && (
                  <div className='flex items-start gap-2 text-sm'>
                    <MapPin className='h-4 w-4 text-muted-foreground mt-0.5' />
                    <span className='text-foreground'>
                      {facility.address.fullAddress}
                    </span>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Dimensions */}
            <Card>
              <CardHeader className='pb-3'>
                <h3 className='font-semibold text-base'>
                  Kích thước & Sức chứa
                </h3>
              </CardHeader>
              <CardContent className='space-y-3'>
                <div className='flex items-center justify-between text-sm'>
                  <div className='flex items-center gap-2 text-muted-foreground'>
                    <Ruler className='h-4 w-4' />
                    <span>Chiều dài</span>
                  </div>
                  <span className='font-semibold'>{facility.length} m</span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <div className='flex items-center gap-2 text-muted-foreground'>
                    <Ruler className='h-4 w-4' />
                    <span>Chiều rộng</span>
                  </div>
                  <span className='font-semibold'>{facility.width} m</span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <div className='flex items-center gap-2 text-muted-foreground'>
                    <Ruler className='h-4 w-4' />
                    <span>Chiều cao</span>
                  </div>
                  <span className='font-semibold'>{facility.height} m</span>
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
                  <span className='text-muted-foreground'>Tạo vào</span>
                  <span className='font-medium'>
                    {formatDateStringVN(facility.createdStamp)}
                  </span>
                </div>
                <div className='flex items-center justify-between text-sm'>
                  <span className='text-muted-foreground'>
                    Cập nhật lần cuối
                  </span>
                  <span className='font-medium'>
                    {formatDateStringVN(facility.lastUpdatedStamp)}
                  </span>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Inventory Tab */}
        <TabsContent value='inventory' className='space-y-4'>
          {/* Loading State */}
          {isLoadingInventory && (
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
          {inventoryError && (
            <Card className='border-destructive/50 bg-destructive/5'>
              <CardContent className='p-6 text-center'>
                <p className='text-destructive'>
                  Đã xảy ra lỗi khi tải mặt hàng. Vui lòng thử lại sau.
                </p>
              </CardContent>
            </Card>
          )}

          {/* Inventory List */}
          {!isLoadingInventory &&
            !inventoryError &&
            inventoryItems.length > 0 && (
              <div className='space-y-4'>
                <div className='flex items-center justify-between'>
                  <div>
                    <h3 className='text-lg font-semibold'>
                      Danh sách mặt hàng tồn kho
                    </h3>
                  </div>
                  <Button
                    onClick={() =>
                      router.push(
                        `/sales/inventory/new?facilityId=${facilityId}`
                      )
                    }
                  >
                    <Package className='h-4 w-4 mr-2' />
                    Tạo mặt hàng tồn kho
                  </Button>
                </div>

                <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
                  {inventoryItems.map((item: InventoryItem) => (
                    <InventoryItemCard
                      key={item.id}
                      item={item}
                      product={productMap.get(item.productId)}
                      facility={facility}
                      onClick={() => router.push(`/sales/inventory/${item.id}`)}
                    />
                  ))}
                </div>

                {inventoryTotalItems > INVENTORY_PAGE_SIZE && (
                  <div className='flex items-center justify-between pt-4'>
                    <p className='text-sm text-muted-foreground'>
                      Hiển thị {inventoryCurrentPage * INVENTORY_PAGE_SIZE + 1}{' '}
                      đến{' '}
                      {Math.min(
                        (inventoryCurrentPage + 1) * INVENTORY_PAGE_SIZE,
                        inventoryTotalItems
                      )}{' '}
                      trong tổng số {inventoryTotalItems} mặt hàng
                    </p>

                    <div className='flex items-center gap-2'>
                      <Button
                        variant='outline'
                        size='sm'
                        disabled={inventoryCurrentPage === 0}
                        onClick={() =>
                          setInventoryPage(inventoryCurrentPage - 1)
                        }
                      >
                        <ChevronLeft className='h-4 w-4' />
                        Trước
                      </Button>

                      <div className='flex items-center gap-1'>
                        {Array.from(
                          { length: Math.min(inventoryTotalPages, 5) },
                          (_, i) => (
                            <button
                              key={i}
                              onClick={() => setInventoryPage(i)}
                              className={cn(
                                'h-8 w-8 rounded-md text-sm font-medium transition-colors',
                                inventoryCurrentPage === i
                                  ? 'bg-primary text-primary-foreground'
                                  : 'hover:bg-muted'
                              )}
                            >
                              {i + 1}
                            </button>
                          )
                        )}
                      </div>

                      <Button
                        variant='outline'
                        size='sm'
                        disabled={
                          inventoryCurrentPage >= inventoryTotalPages - 1
                        }
                        onClick={() =>
                          setInventoryPage(inventoryCurrentPage + 1)
                        }
                      >
                        Tiếp
                        <ChevronRight className='h-4 w-4' />
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            )}

          {/* Empty State */}
          {!isLoadingInventory &&
            !inventoryError &&
            inventoryItems.length === 0 && (
              <Card>
                <CardContent className='py-16 text-center'>
                  <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
                    <Package className='w-10 h-10 text-muted-foreground' />
                  </div>
                  <h3 className='text-lg font-semibold mb-2'>
                    Chưa có mặt hàng
                  </h3>
                  <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
                    Kho hàng này chưa có mặt hàng nào trong hệ thống.
                  </p>
                  <Button
                    onClick={() =>
                      router.push(
                        `/sales/inventory/new?facilityId=${facilityId}`
                      )
                    }
                  >
                    <Package className='h-4 w-4 mr-2' />
                    Tạo mặt hàng tồn kho
                  </Button>
                </CardContent>
              </Card>
            )}
        </TabsContent>
      </Tabs>

      <UpdateAddressDialog
        open={isAddressDialogOpen}
        onOpenChange={setIsAddressDialogOpen}
        entityId={facilityId}
        entityType='FACILITY'
        currentAddress={facility.address}
      />
    </div>
  );
};

export default FacilityDetailPage;
