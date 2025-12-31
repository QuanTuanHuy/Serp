/**
 * Shipment Detail Page - Logistics Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shipment detail view
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
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Label,
  DropdownMenuItem,
  DropdownMenuContent,
  DropdownMenuTrigger,
  DropdownMenu,
  DropdownMenuSeparator,
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Textarea,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Edit,
  Phone,
  MapPin,
  Calendar,
  User,
  Package,
  Clock,
  CheckCircle2,
  AlertCircle,
  FileText,
  TrendingUp,
  Building2,
  Truck,
  ArrowDownToLine,
  ArrowUpFromLine,
  Weight,
  ReceiptText,
  Mail,
  XCircle,
  MoreHorizontal,
  Trash2,
  Save,
  Plus,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetFacilitiesQuery,
  useGetProductsQuery,
  useGetShipmentQuery,
  useGetSupplierQuery,
  useImportShipmentMutation,
  useDeleteShipmentMutation,
  useAddItemToShipmentMutation,
  useUpdateItemInShipmentMutation,
  useDeleteItemFromShipmentMutation,
  useGetOrderQuery,
} from '../../api/logisticsApi';
import type { ShipmentItemForm, InventoryItemDetail } from '../../types';
import { useGetUsersQuery } from '@/modules/admin/services/users/usersApi';
import { toast } from 'sonner';

interface ShipmentDetailPageProps {
  shipmentId: string;
}

const STATUS_CONFIG = {
  CREATED: {
    label: 'Nháp',
    color: 'text-blue-700 dark:text-blue-400',
    bgColor: 'bg-blue-100 dark:bg-blue-900/30',
    icon: Clock,
  },
  IMPORTED: {
    label: 'Đã nhập kho',
    color: 'text-purple-700 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
    icon: ArrowDownToLine,
  },
  EXPORTED: {
    label: 'Đã xuất kho',
    color: 'text-purple-700 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
    icon: ArrowUpFromLine,
  },
};

const TYPE_CONFIG = {
  INBOUND: {
    label: 'Phiếu nhập',
    color: 'text-green-700 dark:text-green-400',
    bgColor: 'bg-green-100 dark:bg-green-900/30',
    icon: ReceiptText,
  },
  OUTBOUND: {
    label: 'Phiếu xuất',
    color: 'text-orange-700 dark:text-orange-400',
    bgColor: 'bg-orange-100 dark:bg-orange-900/30',
    icon: ReceiptText,
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

export const ShipmentDetailPage: React.FC<ShipmentDetailPageProps> = ({
  shipmentId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  // Item management states
  const [showAddItemDialog, setShowAddItemDialog] = useState(false);
  const [showEditItemDialog, setShowEditItemDialog] = useState(false);
  const [showDeleteItemDialog, setShowDeleteItemDialog] = useState(false);
  const [selectedItem, setSelectedItem] = useState<InventoryItemDetail | null>(
    null
  );
  const [itemFormData, setItemFormData] = useState<ShipmentItemForm>({
    orderItemId: '',
    quantity: 1,
    lotId: '',
    facilityId: '',
    note: '',
    expirationDate: '',
    manufacturingDate: '',
  });
  const [itemErrors, setItemErrors] = useState<Record<string, string>>({});

  const {
    data: shipmentResponse,
    isLoading,
    isError,
    refetch: refetchShipment,
  } = useGetShipmentQuery(shipmentId);

  const shipment = shipmentResponse?.data;

  // Mutations
  const [importShipment, { isLoading: isImporting }] =
    useImportShipmentMutation();
  const [deleteShipment, { isLoading: isDeleting }] =
    useDeleteShipmentMutation();
  const [addItemToShipment, { isLoading: isAddingItem }] =
    useAddItemToShipmentMutation();
  const [updateItemInShipment, { isLoading: isUpdatingItem }] =
    useUpdateItemInShipmentMutation();
  const [deleteItemFromShipment, { isLoading: isDeletingItem }] =
    useDeleteItemFromShipmentMutation();

  // Fetch order data to get available items
  const { data: orderResponse, refetch: refetchOrder } = useGetOrderQuery(
    shipment?.orderId || '',
    {
      skip: !shipment?.orderId,
    }
  );
  const order = orderResponse?.data;

  // Fetch supplier data
  const { data: supplierResponse } = useGetSupplierQuery(
    shipment?.fromSupplierId || '',
    { skip: !shipment?.fromSupplierId }
  );

  const supplier = supplierResponse?.data;

  // Fetch customer data
  const { data: customerResponse } = useGetSupplierQuery(
    shipment?.toCustomerId || '',
    { skip: !shipment?.toCustomerId }
  );

  const customer = customerResponse?.data;

  // Collect unique user IDs from order
  const userIds = [
    shipment?.createdByUserId,
    shipment?.handledByUserId,
    shipment?.userCancelledId,
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

  // Collect unique product IDs
  const productIds = useMemo(() => {
    return (
      shipment?.items
        ?.map((item) => item.productId)
        .filter((id): id is string => !!id) || []
    );
  }, [shipment]);

  // Fetch products data
  const { data: productsResponse } = useGetProductsQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: productIds.length === 0 }
  );

  // Create product map for quick lookup
  const productMap = useMemo(() => {
    const map = new Map();
    productsResponse?.data?.items?.forEach((product) => {
      map.set(product.id, product);
    });
    return map;
  }, [productsResponse]);

  // Collect facility IDs
  const facilityIds = useMemo(() => {
    return (
      shipment?.items
        ?.map((item) => item.facilityId)
        .filter((id): id is string => !!id) || []
    );
  }, [shipment]);

  // Fetch facilities data
  const { data: facilitiesResponse } = useGetFacilitiesQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: facilityIds.length === 0 }
  );

  // Create facility map for quick lookup
  const facilityMap = useMemo(() => {
    const map = new Map();
    facilitiesResponse?.data?.items?.forEach((facility) => {
      map.set(facility.id, facility);
    });
    return map;
  }, [facilitiesResponse]);

  const statusConfig = shipment
    ? STATUS_CONFIG[shipment.statusId as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.CREATED
    : STATUS_CONFIG.CREATED;

  const typeConfig = shipment
    ? TYPE_CONFIG[shipment.shipmentTypeId as keyof typeof TYPE_CONFIG] ||
      TYPE_CONFIG.INBOUND
    : TYPE_CONFIG.INBOUND;

  const StatusIcon = statusConfig.icon;
  const TypeIcon = typeConfig.icon;

  // Handlers
  const handleImportExport = async () => {
    if (!shipment) return;

    setIsProcessing(true);
    try {
      await importShipment({ shipmentId: shipment.id }).unwrap();
      toast.success(
        shipment.shipmentTypeId === 'INBOUND'
          ? 'Nhập kho thành công'
          : 'Xuất kho thành công'
      );
    } catch (error: any) {
      console.error('Import/Export error:', error);
      toast.error(
        error?.data?.message ||
          `Không thể ${shipment.shipmentTypeId === 'INBOUND' ? 'nhập' : 'xuất'} kho`
      );
    } finally {
      setIsProcessing(false);
    }
  };

  const handleEdit = () => {
    router.push(`/logistics/shipments/${shipmentId}/edit`);
  };

  const handleDelete = async () => {
    setIsProcessing(true);
    try {
      await deleteShipment(shipmentId).unwrap();
      toast.success('Xóa phiếu thành công');
      router.push('/logistics/shipments');
    } catch (error: any) {
      console.error('Delete error:', error);
      toast.error(error?.data?.message || 'Không thể xóa phiếu');
      setIsProcessing(false);
    }
  };

  // Item handlers
  const resetItemForm = () => {
    setItemFormData({
      orderItemId: '',
      quantity: 1,
      lotId: '',
      facilityId: '',
      note: '',
      expirationDate: '',
      manufacturingDate: '',
    });
    setItemErrors({});
    setSelectedItem(null);
  };

  const validateItemForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!itemFormData.orderItemId) {
      errors.orderItemId = 'Vui lòng chọn sản phẩm';
    }
    if (!itemFormData.lotId.trim()) {
      errors.lotId = 'Vui lòng nhập lô hàng';
    }
    if (!itemFormData.facilityId) {
      errors.facilityId = 'Vui lòng chọn kho';
    }
    if (itemFormData.quantity <= 0) {
      errors.quantity = 'Số lượng phải > 0';
    }
    if (
      selectedItem &&
      itemFormData.quantity >
        (order?.items?.find((item) => item.id === selectedItem?.orderItemId)
          ?.quantityRemaining ?? 0) +
          (selectedItem?.quantity || 0)
    ) {
      errors.quantity = 'Số lượng vượt quá số lượng còn lại trong đơn hàng';
    }
    if (
      !selectedItem &&
      itemFormData.quantity >
        (order?.items?.find((item) => item.id === itemFormData.orderItemId)
          ?.quantityRemaining ?? 0)
    ) {
      errors.quantity = 'Số lượng vượt quá số lượng còn lại trong đơn hàng';
    }

    setItemErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleAddItem = async () => {
    if (!validateItemForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    try {
      await addItemToShipment({
        shipmentId,
        data: itemFormData,
      }).unwrap();
      toast.success('Thêm sản phẩm thành công');
      await Promise.all([refetchShipment(), refetchOrder()]);
      setShowAddItemDialog(false);
      resetItemForm();
    } catch (error: any) {
      console.error('Add item error:', error);
      toast.error(error?.data?.message || 'Không thể thêm sản phẩm');
    }
  };

  const handleEditItem = async () => {
    if (!selectedItem || !validateItemForm()) {
      toast.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    try {
      await updateItemInShipment({
        shipmentId,
        itemId: selectedItem.id,
        data: itemFormData,
      }).unwrap();
      toast.success('Cập nhật sản phẩm thành công');
      await Promise.all([refetchShipment(), refetchOrder()]);
      setShowEditItemDialog(false);
      resetItemForm();
    } catch (error: any) {
      console.error('Update item error:', error);
      toast.error(error?.data?.message || 'Không thể cập nhật sản phẩm');
    }
  };

  const handleDeleteItem = async () => {
    if (!selectedItem) return;

    try {
      await deleteItemFromShipment({
        shipmentId,
        itemId: selectedItem.id,
      }).unwrap();
      toast.success('Xóa sản phẩm thành công');
      await Promise.all([refetchShipment(), refetchOrder()]);
      setShowDeleteItemDialog(false);
      resetItemForm();
    } catch (error: any) {
      console.error('Delete item error:', error);
      toast.error(error?.data?.message || 'Không thể xóa sản phẩm');
    }
  };

  const openAddItemDialog = () => {
    resetItemForm();
    setShowAddItemDialog(true);
  };

  const openEditItemDialog = (item: InventoryItemDetail) => {
    setSelectedItem(item);
    setItemFormData({
      orderItemId: item.productId || '',
      quantity: item.quantity || 1,
      lotId: item.lotId || '',
      facilityId: item.facilityId || '',
      note: item.note || '',
      expirationDate: item.expirationDate
        ? new Date(item.expirationDate).toISOString().split('T')[0]
        : '',
      manufacturingDate: item.manufacturingDate
        ? new Date(item.manufacturingDate).toISOString().split('T')[0]
        : '',
    });
    setShowEditItemDialog(true);
  };

  const openDeleteItemDialog = (item: InventoryItemDetail) => {
    setSelectedItem(item);
    setShowDeleteItemDialog(true);
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

  if (isError || !shipment) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>Không tìm thấy lô hàng</h3>
          <p className='text-muted-foreground mb-4'>
            Lô hàng không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/logistics/shipments')}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Quay lại danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Header Section */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-4'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics/shipments')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <div className='flex items-center gap-3 mb-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {shipment.shipmentName ||
                  `Phiếu #${shipment.id?.slice(0, 8)}...`}
              </h1>
              <Badge
                variant='outline'
                className={cn('gap-1.5', typeConfig.bgColor)}
              >
                <TypeIcon className={cn('h-3 w-3', typeConfig.color)} />
                <span className={typeConfig.color}>{typeConfig.label}</span>
              </Badge>
              <Badge
                variant='secondary'
                className={cn('gap-1.5', statusConfig.bgColor)}
              >
                <StatusIcon className={cn('h-3 w-3', statusConfig.color)} />
                <span className={statusConfig.color}>{statusConfig.label}</span>
              </Badge>
            </div>
            <p className='text-muted-foreground text-sm'>
              ID: {shipment.id} • Tạo lúc: {formatDate(shipment.createdStamp)}
            </p>
          </div>
        </div>

        {shipment.statusId === 'CREATED' && (
          <div className='flex items-center gap-2'>
            <Button
              onClick={handleImportExport}
              disabled={isProcessing || isImporting}
              className='gap-2'
            >
              {isImporting ? (
                <>
                  <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin' />
                  Đang xử lý...
                </>
              ) : (
                <>
                  <CheckCircle2 className='h-4 w-4' />
                  {shipment.shipmentTypeId === 'INBOUND'
                    ? 'Nhập kho'
                    : 'Xuất kho'}
                </>
              )}
            </Button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant='outline' size='icon' disabled={isProcessing}>
                  <MoreHorizontal className='h-4 w-4' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end'>
                <DropdownMenuItem onClick={handleEdit}>
                  <Edit className='mr-2 h-4 w-4' />
                  Chỉnh sửa phiếu
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onClick={() => setShowDeleteDialog(true)}
                  disabled={isProcessing}
                  className='text-destructive'
                >
                  <Trash2 className='mr-2 h-4 w-4' />
                  Xóa phiếu
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        )}
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
          <TabsTrigger value='items'>
            Sản phẩm ({shipment.items?.length || 0})
          </TabsTrigger>
        </TabsList>

        <TabsContent value='overview' className='space-y-6 mt-6'>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            <div className='lg:col-span-2 space-y-6'>
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <FileText className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Thông tin chung</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-4'>
                  <div className='grid grid-cols-2 gap-4'>
                    <div>
                      <Label className='text-muted-foreground'>
                        Mã đơn hàng
                      </Label>
                      <p className='font-medium'>{shipment.orderId}</p>
                    </div>
                    {shipment.expectedDeliveryDate && (
                      <div>
                        <Label className='text-muted-foreground'>
                          Ngày dự kiến
                        </Label>
                        <div className='flex items-center gap-2'>
                          <Calendar className='h-4 w-4 text-muted-foreground' />
                          <span>
                            {formatDate(shipment.expectedDeliveryDate)}
                          </span>
                        </div>
                      </div>
                    )}
                  </div>

                  {shipment.note && (
                    <div>
                      <Label className='text-muted-foreground'>Ghi chú</Label>
                      <p className='text-sm whitespace-pre-wrap'>
                        {shipment.note}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>

              {shipment.fromSupplierId && (
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
              )}

              {shipment.toCustomerId && (
                <Card>
                  <CardHeader>
                    <div className='flex items-center gap-2'>
                      <User className='h-5 w-5 text-primary' />
                      <h3 className='font-semibold'>Khách hàng</h3>
                    </div>
                  </CardHeader>
                  <CardContent className='space-y-4'>
                    {customer ? (
                      <>
                        <div>
                          <Label className='text-muted-foreground'>
                            Tên khách hàng
                          </Label>
                          <p className='font-medium'>{customer.name}</p>
                        </div>
                        {customer.email && (
                          <div className='flex items-center gap-2'>
                            <Mail className='h-4 w-4 text-muted-foreground' />
                            <span>{customer.email}</span>
                          </div>
                        )}
                        {customer.phone && (
                          <div className='flex items-center gap-2'>
                            <Phone className='h-4 w-4 text-muted-foreground' />
                            <span>{customer.phone}</span>
                          </div>
                        )}
                        {customer.address?.fullAddress && (
                          <div className='flex items-start gap-2'>
                            <MapPin className='h-4 w-4 text-muted-foreground mt-1' />
                            <span className='text-sm'>
                              {customer.address.fullAddress}
                            </span>
                          </div>
                        )}
                      </>
                    ) : (
                      <p className='text-muted-foreground'>
                        Không có thông tin khách hàng
                      </p>
                    )}
                  </CardContent>
                </Card>
              )}
            </div>

            <div className='space-y-6'>
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <Package className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Thống kê</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-4'>
                  {shipment.totalQuantity !== undefined && (
                    <div className='flex items-center justify-between'>
                      <span className='text-muted-foreground'>
                        Tổng số lượng
                      </span>
                      <span className='font-medium'>
                        {shipment.items?.reduce(
                          (sum, item) => sum + (item.quantity || 0),
                          0
                        ) || 0}
                      </span>
                    </div>
                  )}

                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>Số sản phẩm</span>
                    <span className='font-medium'>
                      {shipment.items?.length || 0}
                    </span>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Thông tin chi tiết</h3>
                </CardHeader>
                <CardContent className='space-y-3 text-sm'>
                  <div>
                    <Label className='text-muted-foreground'>Ngày tạo</Label>
                    <p>{formatDate(shipment.createdStamp)}</p>
                  </div>

                  {shipment.lastUpdatedStamp && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Cập nhật lần cuối
                      </Label>
                      <p>{formatDate(shipment.lastUpdatedStamp)}</p>
                    </div>
                  )}

                  <div>
                    <Label className='text-muted-foreground'>Người tạo</Label>
                    <p>
                      {formatFullname(
                        userMap.get(shipment.createdByUserId || 0)
                      )}
                    </p>
                  </div>

                  {shipment.handledByUserId && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Người nhập kho
                      </Label>
                      <p>
                        {formatFullname(
                          userMap.get(shipment.handledByUserId || 0)
                        )}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        <TabsContent value='items' className='space-y-6 mt-6'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Danh sách sản phẩm</h3>
                </div>
                {shipment.statusId === 'CREATED' && (
                  <Button onClick={openAddItemDialog} size='sm'>
                    <Plus className='h-4 w-4 mr-2' />
                    Thêm sản phẩm
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {shipment.items && shipment.items.length > 0 ? (
                <div className='space-y-3'>
                  {shipment.items.map((item, index) => (
                    <div
                      key={item.id || index}
                      className='flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors'
                    >
                      <div className='flex-1'>
                        <p className='font-medium'>
                          {productMap.get(item.productId)?.name || 'N/A'}
                        </p>
                        <div className='flex items-center gap-4 mt-1 text-sm text-muted-foreground'>
                          <span>ID: {item.id?.slice(0, 8)}...</span>
                          <span>Lô: {item.lotId || 'N/A'}</span>
                          {facilityMap.get(item.facilityId) && (
                            <span>
                              Kho:{' '}
                              {facilityMap.get(item.facilityId)?.name || 'N/A'}
                              ...
                            </span>
                          )}
                        </div>
                      </div>
                      <div className='flex items-center gap-8 text-sm'>
                        <div>
                          <Label className='text-muted-foreground'>
                            Số lượng
                          </Label>
                          <p className='font-medium'>{item.quantity}</p>
                        </div>
                        {item.manufacturingDate && (
                          <div>
                            <Label className='text-muted-foreground'>
                              Ngày sản xuất
                            </Label>
                            <p className='text-sm'>
                              {formatDate(item.manufacturingDate)}
                            </p>
                          </div>
                        )}
                        {item.expirationDate && (
                          <div>
                            <Label className='text-muted-foreground'>
                              Hạn sử dụng
                            </Label>
                            <p className='text-sm'>
                              {formatDate(item.expirationDate)}
                            </p>
                          </div>
                        )}
                        {shipment.statusId === 'CREATED' && (
                          <div className='flex items-center gap-2'>
                            <Button
                              variant='ghost'
                              size='icon'
                              onClick={() => openEditItemDialog(item)}
                            >
                              <Edit className='h-4 w-4' />
                            </Button>
                            <Button
                              variant='ghost'
                              size='icon'
                              onClick={() => openDeleteItemDialog(item)}
                              className='text-destructive hover:text-destructive'
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
                  <p>Chưa có sản phẩm nào trong phiếu</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Delete Shipment Confirmation Dialog */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận xóa phiếu</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn xóa phiếu "
              {shipment?.shipmentName || shipment?.id.slice(0, 8) + '...'}"?
              Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isProcessing}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              disabled={isProcessing || isDeleting}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isDeleting ? (
                <>
                  <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin mr-2' />
                  Đang xóa...
                </>
              ) : (
                'Xóa phiếu'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Add Item Dialog */}
      <Dialog
        open={showAddItemDialog}
        onOpenChange={(open) => {
          setShowAddItemDialog(open);
          if (!open) resetItemForm();
        }}
      >
        <DialogContent className='sm:max-w-[600px]'>
          <DialogHeader>
            <DialogTitle>Thêm sản phẩm mới</DialogTitle>
            <DialogDescription>
              Thêm sản phẩm vào phiếu{' '}
              {shipment.shipmentTypeId === 'INBOUND' ? 'nhập' : 'xuất'}
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4 py-4'>
            <div className='space-y-2'>
              <Label htmlFor='add-orderItemId'>
                Sản phẩm <span className='text-destructive'>*</span>
              </Label>
              <select
                id='add-orderItemId'
                value={itemFormData.orderItemId}
                onChange={(e) =>
                  setItemFormData({
                    ...itemFormData,
                    orderItemId: e.target.value,
                  })
                }
                className={cn(
                  'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background',
                  itemErrors.orderItemId && 'border-destructive'
                )}
              >
                <option value=''>Chọn sản phẩm</option>
                {order?.items
                  ?.filter((orderItem) => orderItem.quantityRemaining > 0)
                  .map((orderItem) => (
                    <option key={orderItem.id} value={orderItem.id}>
                      {orderItem.product?.name || orderItem.productId} (Còn lại:{' '}
                      {orderItem.quantityRemaining})
                    </option>
                  ))}
              </select>
              {itemErrors.orderItemId && (
                <p className='text-xs text-destructive'>
                  {itemErrors.orderItemId}
                </p>
              )}
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='add-quantity'>
                  Số lượng <span className='text-destructive'>*</span>
                </Label>
                <Input
                  id='add-quantity'
                  type='number'
                  min='1'
                  value={itemFormData.quantity}
                  onChange={(e) =>
                    setItemFormData({
                      ...itemFormData,
                      quantity: parseInt(e.target.value) || 0,
                    })
                  }
                  className={cn(itemErrors.quantity && 'border-destructive')}
                />
                {itemErrors.quantity && (
                  <p className='text-xs text-destructive'>
                    {itemErrors.quantity}
                  </p>
                )}
                {order?.items?.find(
                  (item) => item.id === itemFormData.orderItemId
                )?.quantityRemaining !== undefined && (
                  <Badge
                    variant='secondary'
                    className={
                      itemFormData.quantity >
                      (order?.items?.find(
                        (item) => item.id === itemFormData.orderItemId
                      )?.quantityRemaining ?? 0)
                        ? 'mt-2 text-red-700 dark:text-red-400'
                        : 'mt-2 text-emerald-700 dark:text-emerald-400'
                    }
                  >
                    Số lượng có sẵn:{' '}
                    {
                      order?.items?.find(
                        (item) => item.id === itemFormData.orderItemId
                      )?.quantityRemaining
                    }
                  </Badge>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='add-lotId'>
                  Lô hàng <span className='text-destructive'>*</span>
                </Label>
                <Input
                  id='add-lotId'
                  value={itemFormData.lotId}
                  onChange={(e) =>
                    setItemFormData({ ...itemFormData, lotId: e.target.value })
                  }
                  className={cn(itemErrors.lotId && 'border-destructive')}
                />
                {itemErrors.lotId && (
                  <p className='text-xs text-destructive'>{itemErrors.lotId}</p>
                )}
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='add-facilityId'>
                Kho <span className='text-destructive'>*</span>
              </Label>
              <select
                id='add-facilityId'
                value={itemFormData.facilityId}
                onChange={(e) =>
                  setItemFormData({
                    ...itemFormData,
                    facilityId: e.target.value,
                  })
                }
                className={cn(
                  'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background',
                  itemErrors.facilityId && 'border-destructive'
                )}
              >
                <option value=''>Chọn kho</option>
                {Array.from(facilityMap.values()).map((facility) => (
                  <option key={facility.id} value={facility.id}>
                    {facility.name}
                  </option>
                ))}
              </select>
              {itemErrors.facilityId && (
                <p className='text-xs text-destructive'>
                  {itemErrors.facilityId}
                </p>
              )}
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='add-manufacturingDate'>Ngày sản xuất</Label>
                <Input
                  id='add-manufacturingDate'
                  type='date'
                  value={itemFormData.manufacturingDate}
                  onChange={(e) =>
                    setItemFormData({
                      ...itemFormData,
                      manufacturingDate: e.target.value,
                    })
                  }
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='add-expirationDate'>Ngày hết hạn</Label>
                <Input
                  id='add-expirationDate'
                  type='date'
                  value={itemFormData.expirationDate}
                  onChange={(e) =>
                    setItemFormData({
                      ...itemFormData,
                      expirationDate: e.target.value,
                    })
                  }
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='add-note'>Ghi chú</Label>
              <Textarea
                id='add-note'
                value={itemFormData.note}
                onChange={(e) =>
                  setItemFormData({ ...itemFormData, note: e.target.value })
                }
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => {
                setShowAddItemDialog(false);
                resetItemForm();
              }}
              disabled={isAddingItem}
            >
              Hủy
            </Button>
            <Button onClick={handleAddItem} disabled={isAddingItem}>
              {isAddingItem ? (
                <>
                  <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin mr-2' />
                  Đang thêm...
                </>
              ) : (
                'Thêm sản phẩm'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Item Dialog */}
      <Dialog
        open={showEditItemDialog}
        onOpenChange={(open) => {
          setShowEditItemDialog(open);
          if (!open) resetItemForm();
        }}
      >
        <DialogContent className='sm:max-w-[600px]'>
          <DialogHeader>
            <DialogTitle>Chỉnh sửa sản phẩm</DialogTitle>
            <DialogDescription>
              Cập nhật thông tin sản phẩm trong phiếu
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4 py-4'>
            <div className='space-y-2'>
              <Label>Sản phẩm</Label>
              <Input
                value={
                  productMap.get(selectedItem?.productId || '')?.name ||
                  selectedItem?.productId ||
                  'N/A'
                }
                disabled
              />
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='edit-quantity'>
                  Số lượng <span className='text-destructive'>*</span>
                </Label>
                <Input
                  id='edit-quantity'
                  type='number'
                  min='1'
                  value={itemFormData.quantity}
                  onChange={(e) =>
                    setItemFormData({
                      ...itemFormData,
                      quantity: parseInt(e.target.value) || 0,
                    })
                  }
                  className={cn(itemErrors.quantity && 'border-destructive')}
                />
                {itemErrors.quantity && (
                  <p className='text-xs text-destructive'>
                    {itemErrors.quantity}
                  </p>
                )}
                {order?.items?.find(
                  (item) => item.id === selectedItem?.orderItemId
                )?.quantityRemaining !== undefined && (
                  <Badge
                    variant='secondary'
                    className={
                      itemFormData.quantity >
                      (selectedItem?.quantity ?? 0) +
                        (order?.items?.find(
                          (item) => item.id === selectedItem?.orderItemId
                        )?.quantityRemaining ?? 0)
                        ? 'mt-2 text-red-700 dark:text-red-400'
                        : 'mt-2 text-emerald-700 dark:text-emerald-400'
                    }
                  >
                    Số lượng có sẵn:{' '}
                    {(order?.items?.find(
                      (item) => item.id === selectedItem?.orderItemId
                    )?.quantityRemaining ?? 0) + (selectedItem?.quantity || 0)}
                  </Badge>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='edit-lotId'>
                  Lô hàng <span className='text-destructive'>*</span>
                </Label>
                <Input
                  id='edit-lotId'
                  value={itemFormData.lotId}
                  onChange={(e) =>
                    setItemFormData({ ...itemFormData, lotId: e.target.value })
                  }
                  className={cn(itemErrors.lotId && 'border-destructive')}
                />
                {itemErrors.lotId && (
                  <p className='text-xs text-destructive'>{itemErrors.lotId}</p>
                )}
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='edit-facilityId'>
                Kho <span className='text-destructive'>*</span>
              </Label>
              <select
                id='edit-facilityId'
                value={itemFormData.facilityId}
                onChange={(e) =>
                  setItemFormData({
                    ...itemFormData,
                    facilityId: e.target.value,
                  })
                }
                className={cn(
                  'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background',
                  itemErrors.facilityId && 'border-destructive'
                )}
              >
                <option value=''>Chọn kho</option>
                {Array.from(facilityMap.values()).map((facility) => (
                  <option key={facility.id} value={facility.id}>
                    {facility.name}
                  </option>
                ))}
              </select>
              {itemErrors.facilityId && (
                <p className='text-xs text-destructive'>
                  {itemErrors.facilityId}
                </p>
              )}
            </div>

            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='edit-manufacturingDate'>Ngày sản xuất</Label>
                <Input
                  id='edit-manufacturingDate'
                  type='date'
                  value={itemFormData.manufacturingDate}
                  onChange={(e) =>
                    setItemFormData({
                      ...itemFormData,
                      manufacturingDate: e.target.value,
                    })
                  }
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='edit-expirationDate'>Ngày hết hạn</Label>
                <Input
                  id='edit-expirationDate'
                  type='date'
                  value={itemFormData.expirationDate}
                  onChange={(e) =>
                    setItemFormData({
                      ...itemFormData,
                      expirationDate: e.target.value,
                    })
                  }
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='edit-note'>Ghi chú</Label>
              <Textarea
                id='edit-note'
                value={itemFormData.note}
                onChange={(e) =>
                  setItemFormData({ ...itemFormData, note: e.target.value })
                }
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => {
                setShowEditItemDialog(false);
                resetItemForm();
              }}
              disabled={isUpdatingItem}
            >
              Hủy
            </Button>
            <Button onClick={handleEditItem} disabled={isUpdatingItem}>
              {isUpdatingItem ? (
                <>
                  <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin mr-2' />
                  Đang cập nhật...
                </>
              ) : (
                <>
                  <Save className='h-4 w-4 mr-2' />
                  Cập nhật
                </>
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Item Dialog */}
      <AlertDialog
        open={showDeleteItemDialog}
        onOpenChange={setShowDeleteItemDialog}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận xóa sản phẩm</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn xóa sản phẩm "
              {productMap.get(selectedItem?.productId || '')?.name ||
                selectedItem?.productId}
              " khỏi phiếu? Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeletingItem}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteItem}
              disabled={isDeletingItem}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isDeletingItem ? (
                <>
                  <div className='h-4 w-4 border-2 border-background border-t-transparent rounded-full animate-spin mr-2' />
                  Đang xóa...
                </>
              ) : (
                'Xóa sản phẩm'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};
