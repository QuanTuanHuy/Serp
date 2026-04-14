/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile order list page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import {
  CheckCircle2,
  Download,
  Eye,
  FileUp,
  Loader2,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Shield,
  Trash2,
  XCircle,
} from 'lucide-react';
import {
  useCancelOrderMutation,
  useConfirmOrderMutation,
  useCreateOrderMutation,
  useGeocodeAddressMutation,
  useImportOrdersMutation,
  useLazyExportOrderTemplateQuery,
  useLazyGetOrderByIdQuery,
  useGetOrdersQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useUpdateOrderMutation,
  useValidateOrderImportMutation,
} from '../../api';
import { CoordinatePickerMap } from '../../components';
import type {
  CancelOrderRequest,
  CreateOrderRequest,
  FirstMileDeliveryRequestTime,
  FirstMileFeePayer,
  FirstMileOrderDetail,
  ImportHistory,
  OrderImportItem,
  FirstMileOrderProductItem,
  FirstMileOrderProductCategory,
  FirstMileOrderStatus,
  FirstMileOrderType,
  ValidateImportFileResponse,
  UpdateOrderRequest,
  Province,
  Ward,
} from '../../types';

const PAGE_SIZE = 20;
const IMPORT_PREVIEW_LIMIT = 5;

type OrderAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_POST_OFFICE'
  | 'COURIER_ASSIGNED'
  | 'CUSTOMER_CREATED'
  | 'NO_ACCESS';
type LocationTarget = 'sender' | 'receiver';
type OrderFormMode = 'create' | 'edit';

type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline';
type OrderStatusFilter = 'ALL' | FirstMileOrderStatus;

const ORDER_STATUS_OPTIONS: FirstMileOrderStatus[] = [
  'CREATED',
  'ASSIGNED_TO_PICKUP',
  'PICKING_UP',
  'PICKUP_FAILED',
  'PICKED_UP',
  'AT_ORIGIN_POST_OFFICE',
  'CANCELLED',
  'LOST_OR_DAMAGED',
];

const normalizeLocationCode = (value?: string): string => value?.trim() ?? '';

const buildWardSelectOptions = (
  wards: Ward[] | undefined,
  selectedWardCode: string,
  provinceCode: string
): Ward[] => {
  const options = [...(wards ?? [])];

  if (
    selectedWardCode &&
    !options.some(
      (ward) => normalizeLocationCode(ward.wardCode) === selectedWardCode
    )
  ) {
    options.unshift({
      wardCode: selectedWardCode,
      name: selectedWardCode,
      provinceCode,
    });
  }

  return options;
};

interface CreateOrderFormState {
  customerOrderCode: string;
  senderName: string;
  senderPhone: string;
  senderProvinceCode: string;
  senderWardCode: string;
  senderAddressDetail: string;
  senderLatitude: string;
  senderLongitude: string;
  receiverName: string;
  receiverPhone: string;
  receiverProvinceCode: string;
  receiverWardCode: string;
  receiverAddressDetail: string;
  receiverLatitude: string;
  receiverLongitude: string;
  pickupTimeStart: string;
  pickupTimeEnd: string;
  deliveryRequestTime: FirstMileDeliveryRequestTime;
  orderProductCategory: 'NONE' | FirstMileOrderProductCategory;
  orderType: FirstMileOrderType;
  feePayer: FirstMileFeePayer;
  isCod: 'true' | 'false';
  dimensionLengthCm: string;
  dimensionWidthCm: string;
  dimensionHeightCm: string;
  totalVolumeM3: string;
  note: string;
}

const DEFAULT_CREATE_ORDER_FORM: CreateOrderFormState = {
  customerOrderCode: '',
  senderName: '',
  senderPhone: '',
  senderProvinceCode: '',
  senderWardCode: '',
  senderAddressDetail: '',
  senderLatitude: '',
  senderLongitude: '',
  receiverName: '',
  receiverPhone: '',
  receiverProvinceCode: '',
  receiverWardCode: '',
  receiverAddressDetail: '',
  receiverLatitude: '',
  receiverLongitude: '',
  pickupTimeStart: '',
  pickupTimeEnd: '',
  deliveryRequestTime: 'BUSINESS_HOURS',
  orderProductCategory: 'NONE',
  orderType: 'STANDARD_ORDER',
  feePayer: 'SENDER',
  isCod: 'false',
  dimensionLengthCm: '',
  dimensionWidthCm: '',
  dimensionHeightCm: '',
  totalVolumeM3: '',
  note: '',
};

const DELIVERY_REQUEST_TIME_OPTIONS: Array<{
  value: FirstMileDeliveryRequestTime;
  label: string;
}> = [
  { value: 'BUSINESS_HOURS', label: 'Business hours' },
  { value: 'FULL_DAY', label: 'Full day' },
  { value: 'MORNING', label: 'Morning' },
  { value: 'AFTERNOON', label: 'Afternoon' },
  { value: 'SUNDAY', label: 'Sunday' },
  { value: 'HOLIDAY', label: 'Holiday' },
];

const ORDER_TYPE_OPTIONS: Array<{ value: FirstMileOrderType; label: string }> =
  [
    { value: 'STANDARD_ORDER', label: 'Standard' },
    { value: 'EXPRESS_ORDER', label: 'Express' },
  ];

const FEE_PAYER_OPTIONS: Array<{ value: FirstMileFeePayer; label: string }> = [
  { value: 'SENDER', label: 'Sender' },
  { value: 'RECEIVER', label: 'Receiver' },
];

const ORDER_PRODUCT_CATEGORY_OPTIONS: Array<{
  value: FirstMileOrderProductCategory;
  label: string;
}> = [
  { value: 'SOLID', label: 'Solid' },
  { value: 'FRAGILE', label: 'Fragile' },
  { value: 'HIGH_VALUE', label: 'High value' },
  { value: 'OVERSIZED', label: 'Oversized' },
  { value: 'LIQUID', label: 'Liquid' },
  { value: 'MAGNETIC_BATTERY', label: 'Magnetic battery' },
];

const resolveOrderAccessScope = (roles: string[]): OrderAccessScope => {
  if (roles.includes('TMS_ADMIN')) {
    return 'ADMIN_ALL';
  }

  if (roles.includes('TMS_POSTOFFICER_MANAGER')) {
    return 'MANAGER_POST_OFFICE';
  }

  if (roles.includes('TMS_POSTOFFICER')) {
    return 'COURIER_ASSIGNED';
  }

  if (roles.includes('TMS_CUSTOMER')) {
    return 'CUSTOMER_CREATED';
  }

  return 'NO_ACCESS';
};

const getScopeBadgeLabel = (scope: OrderAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'TMS admin';
    case 'MANAGER_POST_OFFICE':
      return 'Post office manager';
    case 'COURIER_ASSIGNED':
      return 'Courier';
    case 'CUSTOMER_CREATED':
      return 'Customer';
    default:
      return 'No access';
  }
};

const getScopeDescription = (scope: OrderAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'You can view all orders in the system.';
    case 'MANAGER_POST_OFFICE':
      return 'You can view orders assigned to the post office you manage.';
    case 'COURIER_ASSIGNED':
      return 'You can view orders assigned to you.';
    case 'CUSTOMER_CREATED':
      return 'You can only view orders created by you.';
    default:
      return 'Your current account does not have permission to access the order list.';
  }
};

const formatStatusLabel = (status: FirstMileOrderStatus): string =>
  status.replaceAll('_', ' ');

const getStatusBadgeVariant = (status: FirstMileOrderStatus): BadgeVariant => {
  switch (status) {
    case 'CREATED':
    case 'ASSIGNED_TO_PICKUP':
      return 'secondary';
    case 'PICKING_UP':
    case 'PICKED_UP':
    case 'AT_ORIGIN_POST_OFFICE':
      return 'default';
    case 'PICKUP_FAILED':
      return 'outline';
    case 'CANCELLED':
    case 'LOST_OR_DAMAGED':
      return 'destructive';
    default:
      return 'outline';
  }
};

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('en-US');
};

const buildOrderAddressLabel = (
  name?: string,
  phone?: string,
  addressDetail?: string
): string => {
  const mainLabel = [name, phone].filter(Boolean).join(' - ');
  if (!mainLabel && !addressDetail) {
    return '--';
  }

  if (!addressDetail) {
    return mainLabel;
  }

  return `${mainLabel || '--'} | ${addressDetail}`;
};

const buildPostOfficeAssignmentLabel = (
  order: FirstMileOrderDetail
): string => {
  const origin = order.originPostOfficeCode || '--';
  const destination = order.destinationPostOfficeCode || '--';
  return `${origin} -> ${destination}`;
};

const parseOptionalNumberInput = (value: string): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isFinite(parsedValue)) {
    return undefined;
  }

  return parsedValue;
};

const parseRequiredNumberInput = (value: string): number | null => {
  const parsedValue = parseOptionalNumberInput(value);
  return parsedValue === undefined ? null : parsedValue;
};

const toDateTimeLocalValue = (value?: string): string => {
  if (!value) {
    return '';
  }

  const parsedDate = new Date(value);
  if (!Number.isNaN(parsedDate.getTime())) {
    const timezoneOffsetInMs = parsedDate.getTimezoneOffset() * 60 * 1000;
    return new Date(parsedDate.getTime() - timezoneOffsetInMs)
      .toISOString()
      .slice(0, 16);
  }

  return value.slice(0, 16);
};

const isDraftOrder = (order: FirstMileOrderDetail): boolean => {
  return order.status === 'CREATED' && !order.isConfirm;
};

const mapOrderToFormState = (
  order: FirstMileOrderDetail
): CreateOrderFormState => {
  return {
    customerOrderCode: order.customerOrderCode || '',
    senderName: order.senderName || '',
    senderPhone: order.senderPhone || '',
    senderProvinceCode: order.senderProvinceCode || '',
    senderWardCode: order.senderWardCode || '',
    senderAddressDetail: order.senderAddressDetail || '',
    senderLatitude:
      order.senderLatitude === undefined || order.senderLatitude === null
        ? ''
        : String(order.senderLatitude),
    senderLongitude:
      order.senderLongitude === undefined || order.senderLongitude === null
        ? ''
        : String(order.senderLongitude),
    receiverName: order.receiverName || '',
    receiverPhone: order.receiverPhone || '',
    receiverProvinceCode: order.receiverProvinceCode || '',
    receiverWardCode: order.receiverWardCode || '',
    receiverAddressDetail: order.receiverAddressDetail || '',
    receiverLatitude:
      order.receiverLatitude === undefined || order.receiverLatitude === null
        ? ''
        : String(order.receiverLatitude),
    receiverLongitude:
      order.receiverLongitude === undefined || order.receiverLongitude === null
        ? ''
        : String(order.receiverLongitude),
    pickupTimeStart: toDateTimeLocalValue(order.pickupTimeStart),
    pickupTimeEnd: toDateTimeLocalValue(order.pickupTimeEnd),
    deliveryRequestTime: order.deliveryRequestTime || 'BUSINESS_HOURS',
    orderProductCategory: order.orderProductCategory || 'NONE',
    orderType: order.orderType || 'STANDARD_ORDER',
    feePayer: order.feePayer || 'SENDER',
    isCod:
      order.codAmount !== undefined &&
      order.codAmount !== null &&
      order.codAmount > 0
        ? 'true'
        : 'false',
    dimensionLengthCm: '',
    dimensionWidthCm: '',
    dimensionHeightCm: '',
    totalVolumeM3:
      order.totalVolume === undefined || order.totalVolume === null
        ? ''
        : String(order.totalVolume),
    note: order.note || '',
  };
};

const mapOrderProductsToRequest = (
  products: FirstMileOrderProductItem[] | undefined
): CreateOrderRequest['products'] => {
  if (!products || products.length === 0) {
    return [];
  }

  return products
    .filter(
      (product) =>
        product.name &&
        product.productTypeId !== undefined &&
        product.value !== undefined &&
        product.quantity !== undefined &&
        product.weight !== undefined
    )
    .map((product) => ({
      name: product.name as string,
      product_type_id: product.productTypeId as number,
      value: Math.round(product.value as number),
      quantity: product.quantity as number,
      weight_gram: product.weight as number,
    }));
};

const isConfirmableStatus = (status: FirstMileOrderStatus): boolean => {
  return status === 'CREATED' || status === 'PICKUP_FAILED';
};

const formatOrderImportProductsPreview = (
  products?: OrderImportItem['products']
): string => {
  if (!products || products.length === 0) {
    return '-';
  }

  const previewText = products
    .slice(0, 2)
    .map((product) => {
      const productName = product.name?.trim() || '-';
      const quantity =
        product.quantity === undefined || product.quantity === null
          ? '-'
          : String(product.quantity);
      const productType =
        product.product_type_code || product.product_type_name;

      return productType
        ? `${productName} x${quantity} (${productType})`
        : `${productName} x${quantity}`;
    })
    .join('; ');

  return products.length > 2 ? `${previewText}; ...` : previewText;
};

export const OrderListPage: React.FC = () => {
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];
  const notification = useNotification();

  const [page, setPage] = React.useState(0);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [statusInput, setStatusInput] =
    React.useState<OrderStatusFilter>('ALL');
  const [keyword, setKeyword] = React.useState<string | undefined>(undefined);
  const [status, setStatus] = React.useState<FirstMileOrderStatus | undefined>(
    undefined
  );
  const [orderFormMode, setOrderFormMode] =
    React.useState<OrderFormMode>('create');
  const [editingOrderId, setEditingOrderId] = React.useState<number | null>(
    null
  );
  const [isCreateDialogOpen, setIsCreateDialogOpen] = React.useState(false);
  const [createForm, setCreateForm] = React.useState<CreateOrderFormState>({
    ...DEFAULT_CREATE_ORDER_FORM,
  });
  const [orderProducts, setOrderProducts] = React.useState<
    CreateOrderRequest['products']
  >([]);
  const [isDetailDialogOpen, setIsDetailDialogOpen] = React.useState(false);
  const [detailOrder, setDetailOrder] =
    React.useState<FirstMileOrderDetail | null>(null);
  const [cancelTarget, setCancelTarget] =
    React.useState<FirstMileOrderDetail | null>(null);
  const [cancelReason, setCancelReason] = React.useState('');
  const [deleteTarget, setDeleteTarget] =
    React.useState<FirstMileOrderDetail | null>(null);
  const [loadingOrderActionId, setLoadingOrderActionId] = React.useState<
    number | null
  >(null);
  const [confirmingOrderId, setConfirmingOrderId] = React.useState<
    number | null
  >(null);
  const [geocodingTarget, setGeocodingTarget] =
    React.useState<LocationTarget | null>(null);
  const [selectedImportFile, setSelectedImportFile] =
    React.useState<File | null>(null);
  const [importFileInputKey, setImportFileInputKey] = React.useState(0);
  const [validateImportResult, setValidateImportResult] =
    React.useState<ValidateImportFileResponse<OrderImportItem> | null>(null);
  const [lastImportJob, setLastImportJob] =
    React.useState<ImportHistory | null>(null);

  const selectedSenderProvinceCode = React.useMemo(
    () => normalizeLocationCode(createForm.senderProvinceCode),
    [createForm.senderProvinceCode]
  );
  const selectedSenderWardCode = React.useMemo(
    () => normalizeLocationCode(createForm.senderWardCode),
    [createForm.senderWardCode]
  );
  const selectedReceiverProvinceCode = React.useMemo(
    () => normalizeLocationCode(createForm.receiverProvinceCode),
    [createForm.receiverProvinceCode]
  );
  const selectedReceiverWardCode = React.useMemo(
    () => normalizeLocationCode(createForm.receiverWardCode),
    [createForm.receiverWardCode]
  );

  const accessScope = React.useMemo(
    () => resolveOrderAccessScope(roles),
    [roles]
  );
  const canViewOrders = accessScope !== 'NO_ACCESS';
  const canMutateOrders =
    accessScope === 'ADMIN_ALL' || accessScope === 'CUSTOMER_CREATED';

  const { data, isLoading, isFetching, refetch } = useGetOrdersQuery(
    {
      page,
      size: PAGE_SIZE,
      keyword,
      status,
    },
    {
      skip: !canViewOrders,
    }
  );

  const { data: provincesData } = useGetProvincesQuery({
    page: 0,
    size: 200,
  });

  const { data: senderWardsData, isFetching: isFetchingSenderWards } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: selectedSenderProvinceCode,
        page: 0,
        size: 1000,
      },
      {
        skip: !selectedSenderProvinceCode,
      }
    );

  const { data: receiverWardsData, isFetching: isFetchingReceiverWards } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: selectedReceiverProvinceCode,
        page: 0,
        size: 1000,
      },
      {
        skip: !selectedReceiverProvinceCode,
      }
    );

  const provinceSelectOptions = React.useMemo<Province[]>(
    () => provincesData?.items ?? [],
    [provincesData]
  );

  const senderWardSelectOptions = React.useMemo(
    () =>
      buildWardSelectOptions(
        senderWardsData?.items,
        selectedSenderWardCode,
        selectedSenderProvinceCode
      ),
    [senderWardsData, selectedSenderWardCode, selectedSenderProvinceCode]
  );

  const receiverWardSelectOptions = React.useMemo(
    () =>
      buildWardSelectOptions(
        receiverWardsData?.items,
        selectedReceiverWardCode,
        selectedReceiverProvinceCode
      ),
    [receiverWardsData, selectedReceiverWardCode, selectedReceiverProvinceCode]
  );

  const provinceNameByCode = React.useMemo(() => {
    return provinceSelectOptions.reduce<Record<string, string>>(
      (accumulator, province) => {
        const provinceCode = normalizeLocationCode(province.provinceCode);

        if (provinceCode) {
          accumulator[provinceCode] = province.name;
        }

        return accumulator;
      },
      {}
    );
  }, [provinceSelectOptions]);

  const [createOrder, { isLoading: isCreatingOrder }] =
    useCreateOrderMutation();
  const [updateOrder, { isLoading: isUpdatingOrder }] =
    useUpdateOrderMutation();
  const [cancelOrder, { isLoading: isCancellingOrder }] =
    useCancelOrderMutation();
  const [confirmOrder] = useConfirmOrderMutation();
  const [geocodeAddress] = useGeocodeAddressMutation();
  const [loadOrderById] = useLazyGetOrderByIdQuery();
  const [triggerExportOrderTemplate, { isFetching: isExportingTemplate }] =
    useLazyExportOrderTemplateQuery();
  const [validateOrderImport, { isLoading: isValidatingImport }] =
    useValidateOrderImportMutation();
  const [importOrderFile, { isLoading: isImportingOrders }] =
    useImportOrdersMutation();

  const isSubmittingOrder = isCreatingOrder || isUpdatingOrder;
  const isImportFlowBusy =
    isExportingTemplate || isValidatingImport || isImportingOrders;

  const validatedPreviewItems = React.useMemo(
    () => validateImportResult?.data?.slice(0, IMPORT_PREVIEW_LIMIT) ?? [],
    [validateImportResult]
  );

  const handleApplyFilters = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
    setStatus(statusInput === 'ALL' ? undefined : statusInput);
  };

  const handleOpenCreateDialog = () => {
    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can create first-mile orders.'
      );
      return;
    }

    setOrderFormMode('create');
    setEditingOrderId(null);
    setCreateForm({ ...DEFAULT_CREATE_ORDER_FORM });
    setOrderProducts([]);
    setIsCreateDialogOpen(true);
  };

  const handleMapCoordinateChange = React.useCallback(
    (target: LocationTarget, latitude: number, longitude: number) => {
      const latitudeValue = latitude.toFixed(6);
      const longitudeValue = longitude.toFixed(6);

      setCreateForm((prev) => {
        if (target === 'sender') {
          return {
            ...prev,
            senderLatitude: latitudeValue,
            senderLongitude: longitudeValue,
          };
        }

        return {
          ...prev,
          receiverLatitude: latitudeValue,
          receiverLongitude: longitudeValue,
        };
      });
    },
    []
  );

  const handleGeocodeFromAddress = async (target: LocationTarget) => {
    const isSender = target === 'sender';

    const provinceCode = isSender
      ? selectedSenderProvinceCode
      : selectedReceiverProvinceCode;
    const wardCode = isSender
      ? selectedSenderWardCode
      : selectedReceiverWardCode;
    const addressDetail = isSender
      ? createForm.senderAddressDetail.trim()
      : createForm.receiverAddressDetail.trim();
    const wardOptions = isSender
      ? senderWardSelectOptions
      : receiverWardSelectOptions;

    if (!provinceCode) {
      notification.error('Please select a province before geocoding.');
      return;
    }

    if (!wardCode) {
      notification.error('Please select a ward before geocoding.');
      return;
    }

    if (!addressDetail) {
      notification.error('Please enter address detail before geocoding.');
      return;
    }

    const provinceName = provinceNameByCode[provinceCode] || provinceCode;
    const wardName =
      wardOptions.find(
        (ward) => normalizeLocationCode(ward.wardCode) === wardCode
      )?.name || wardCode;
    const fullAddress = [addressDetail, wardName, provinceName, 'Vietnam']
      .filter(Boolean)
      .join(', ');

    setGeocodingTarget(target);

    try {
      const geocodeResult = await geocodeAddress({
        address: fullAddress,
      }).unwrap();

      handleMapCoordinateChange(
        target,
        geocodeResult.latitude,
        geocodeResult.longitude
      );

      notification.success('Coordinates updated from address.', {
        description: `${geocodeResult.latitude.toFixed(6)}, ${geocodeResult.longitude.toFixed(6)}`,
      });
    } catch (error) {
      notification.error('Failed to geocode address.', {
        description: getErrorMessage(error),
      });
    } finally {
      setGeocodingTarget(null);
    }
  };

  const refreshOrdersAfterMutation = React.useCallback(() => {
    if ((data?.items.length ?? 0) === 1 && page > 0) {
      setPage((prev) => Math.max(prev - 1, 0));
      return;
    }

    void refetch();
  }, [data?.items.length, page, refetch]);

  const loadOrderDetail = React.useCallback(
    async (orderId: number): Promise<FirstMileOrderDetail | null> => {
      try {
        const orderDetailResult = await loadOrderById(orderId).unwrap();
        return orderDetailResult;
      } catch (error) {
        notification.error('Failed to load order detail.', {
          description: getErrorMessage(error),
        });
        return null;
      }
    },
    [loadOrderById, notification]
  );

  const handleOpenOrderDetail = async (orderId: number) => {
    setLoadingOrderActionId(orderId);
    setIsDetailDialogOpen(true);
    setDetailOrder(null);

    const orderDetailResult = await loadOrderDetail(orderId);
    if (orderDetailResult) {
      setDetailOrder(orderDetailResult);
    } else {
      setIsDetailDialogOpen(false);
    }

    setLoadingOrderActionId(null);
  };

  const handleOpenEditOrder = async (order: FirstMileOrderDetail) => {
    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can update first-mile orders.'
      );
      return;
    }

    if (!isDraftOrder(order)) {
      notification.error(
        'Only newly created and unconfirmed orders can be edited.'
      );
      return;
    }

    setLoadingOrderActionId(order.id);
    const orderDetailResult = await loadOrderDetail(order.id);
    setLoadingOrderActionId(null);

    if (!orderDetailResult) {
      return;
    }

    setOrderFormMode('edit');
    setEditingOrderId(orderDetailResult.id);
    setCreateForm(mapOrderToFormState(orderDetailResult));
    setOrderProducts(mapOrderProductsToRequest(orderDetailResult.products));
    setIsCreateDialogOpen(true);
  };

  const handleRequestCancelOrder = (order: FirstMileOrderDetail) => {
    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can cancel first-mile orders.'
      );
      return;
    }

    if (!isDraftOrder(order)) {
      notification.error(
        'Only newly created and unconfirmed orders can be cancelled.'
      );
      return;
    }

    setCancelReason('');
    setCancelTarget(order);
  };

  const handleCancelOrder = async () => {
    if (!cancelTarget) {
      return;
    }

    const body: CancelOrderRequest = {
      ...(cancelReason.trim() ? { cancel_reason: cancelReason.trim() } : {}),
    };

    try {
      await cancelOrder({
        id: cancelTarget.id,
        body,
      }).unwrap();

      notification.success('Order cancelled successfully.');
      setCancelTarget(null);
      setCancelReason('');
      refreshOrdersAfterMutation();
    } catch (error) {
      notification.error('Failed to cancel order.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRequestDeleteOrder = (order: FirstMileOrderDetail) => {
    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can delete first-mile orders.'
      );
      return;
    }

    if (!isDraftOrder(order)) {
      notification.error(
        'Only newly created and unconfirmed orders can be deleted.'
      );
      return;
    }

    setDeleteTarget(order);
  };

  const handleDeleteOrder = async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      await cancelOrder({
        id: deleteTarget.id,
        body: {
          cancel_reason: 'Deleted by user from frontend order list.',
        },
      }).unwrap();

      notification.success('Order deleted successfully.', {
        description: 'Backend currently maps delete action to cancel status.',
      });
      setDeleteTarget(null);
      refreshOrdersAfterMutation();
    } catch (error) {
      notification.error('Failed to delete order.', {
        description: getErrorMessage(error),
      });
    }
  };

  const buildCreateOrderPayload = (): CreateOrderRequest | null => {
    const requiredFields: Array<{ value: string; label: string }> = [
      {
        value: createForm.customerOrderCode,
        label: 'Customer order code',
      },
      { value: createForm.senderName, label: 'Sender name' },
      { value: createForm.senderPhone, label: 'Sender phone' },
      {
        value: createForm.senderProvinceCode,
        label: 'Sender province code',
      },
      { value: createForm.senderWardCode, label: 'Sender ward code' },
      {
        value: createForm.senderAddressDetail,
        label: 'Sender address detail',
      },
      { value: createForm.receiverName, label: 'Receiver name' },
      { value: createForm.receiverPhone, label: 'Receiver phone' },
      {
        value: createForm.receiverProvinceCode,
        label: 'Receiver province code',
      },
      { value: createForm.receiverWardCode, label: 'Receiver ward code' },
      {
        value: createForm.receiverAddressDetail,
        label: 'Receiver address detail',
      },
    ];

    const missingField = requiredFields.find((field) => !field.value.trim());
    if (missingField) {
      notification.error(`${missingField.label} is required.`);
      return null;
    }

    const senderLatitude = parseRequiredNumberInput(createForm.senderLatitude);
    const senderLongitude = parseRequiredNumberInput(
      createForm.senderLongitude
    );
    const receiverLatitude = parseRequiredNumberInput(
      createForm.receiverLatitude
    );
    const receiverLongitude = parseRequiredNumberInput(
      createForm.receiverLongitude
    );

    if (
      senderLatitude === null ||
      senderLongitude === null ||
      receiverLatitude === null ||
      receiverLongitude === null
    ) {
      notification.error('Sender and receiver coordinates must be valid.');
      return null;
    }

    if (
      senderLatitude < -90 ||
      senderLatitude > 90 ||
      receiverLatitude < -90 ||
      receiverLatitude > 90 ||
      senderLongitude < -180 ||
      senderLongitude > 180 ||
      receiverLongitude < -180 ||
      receiverLongitude > 180
    ) {
      notification.error(
        'Latitude must be in [-90, 90] and longitude must be in [-180, 180].'
      );
      return null;
    }

    const dimensionLengthCm = parseOptionalNumberInput(
      createForm.dimensionLengthCm
    );
    const dimensionWidthCm = parseOptionalNumberInput(
      createForm.dimensionWidthCm
    );
    const dimensionHeightCm = parseOptionalNumberInput(
      createForm.dimensionHeightCm
    );
    const totalVolumeM3 = parseOptionalNumberInput(createForm.totalVolumeM3);

    const hasInvalidOptionalNumericField =
      (createForm.dimensionLengthCm.trim() &&
        dimensionLengthCm === undefined) ||
      (createForm.dimensionWidthCm.trim() && dimensionWidthCm === undefined) ||
      (createForm.dimensionHeightCm.trim() &&
        dimensionHeightCm === undefined) ||
      (createForm.totalVolumeM3.trim() && totalVolumeM3 === undefined);

    if (hasInvalidOptionalNumericField) {
      notification.error('Dimension and volume values must be valid numbers.');
      return null;
    }

    return {
      customer_order_code: createForm.customerOrderCode.trim(),
      sender_name: createForm.senderName.trim(),
      sender_phone: createForm.senderPhone.trim(),
      sender_province_code: createForm.senderProvinceCode.trim(),
      sender_ward_code: createForm.senderWardCode.trim(),
      sender_address_detail: createForm.senderAddressDetail.trim(),
      sender_latitude: senderLatitude,
      sender_longitude: senderLongitude,
      receiver_name: createForm.receiverName.trim(),
      receiver_phone: createForm.receiverPhone.trim(),
      receiver_province_code: createForm.receiverProvinceCode.trim(),
      receiver_ward_code: createForm.receiverWardCode.trim(),
      receiver_address_detail: createForm.receiverAddressDetail.trim(),
      receiver_latitude: receiverLatitude,
      receiver_longitude: receiverLongitude,
      ...(createForm.pickupTimeStart
        ? { pickup_time_start: createForm.pickupTimeStart }
        : {}),
      ...(createForm.pickupTimeEnd
        ? { pickup_time_end: createForm.pickupTimeEnd }
        : {}),
      delivery_request_time: createForm.deliveryRequestTime,
      ...(createForm.orderProductCategory !== 'NONE'
        ? { order_product_category: createForm.orderProductCategory }
        : {}),
      order_type: createForm.orderType,
      fee_payer: createForm.feePayer,
      is_cod: createForm.isCod === 'true',
      ...(dimensionLengthCm !== undefined
        ? { dimension_length_cm: dimensionLengthCm }
        : {}),
      ...(dimensionWidthCm !== undefined
        ? { dimension_width_cm: dimensionWidthCm }
        : {}),
      ...(dimensionHeightCm !== undefined
        ? { dimension_height_cm: dimensionHeightCm }
        : {}),
      ...(totalVolumeM3 !== undefined
        ? { total_volume_m3: totalVolumeM3 }
        : {}),
      ...(createForm.note.trim() ? { note: createForm.note.trim() } : {}),
      products: orderProducts ?? [],
    };
  };

  const handleCreateOrder = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can create first-mile orders.'
      );
      return;
    }

    const payload = buildCreateOrderPayload();
    if (!payload) {
      return;
    }

    try {
      if (orderFormMode === 'create') {
        const createdOrder = await createOrder(payload).unwrap();

        notification.success('Order created successfully.', {
          description: `Order code: ${createdOrder.orderCode}`,
        });
      } else {
        if (editingOrderId === null) {
          notification.error('Missing order id for update.');
          return;
        }

        await updateOrder({
          id: editingOrderId,
          body: payload as UpdateOrderRequest,
        }).unwrap();

        notification.success('Order updated successfully.');
      }

      setIsCreateDialogOpen(false);
      setOrderFormMode('create');
      setEditingOrderId(null);
      setCreateForm({ ...DEFAULT_CREATE_ORDER_FORM });
      setOrderProducts([]);

      if (orderFormMode === 'create' && page !== 0) {
        setPage(0);
      } else {
        refreshOrdersAfterMutation();
      }
    } catch (error) {
      notification.error(
        orderFormMode === 'create'
          ? 'Failed to create order.'
          : 'Failed to update order.',
        {
          description: getErrorMessage(error),
        }
      );
    }
  };

  const handleConfirmOrder = async (order: FirstMileOrderDetail) => {
    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can confirm first-mile orders.'
      );
      return;
    }

    setConfirmingOrderId(order.id);

    try {
      const confirmationResult = await confirmOrder(order.id).unwrap();
      const originPostOffice = confirmationResult.originPostOffice;

      notification.success(
        confirmationResult.alreadyConfirmed
          ? 'Order was already confirmed.'
          : 'Order confirmed successfully.',
        originPostOffice
          ? {
              description: `Origin post office: ${originPostOffice.code} - ${originPostOffice.name}`,
            }
          : undefined
      );

      void refetch();
    } catch (error) {
      notification.error('Failed to confirm order.', {
        description: getErrorMessage(error),
      });
    } finally {
      setConfirmingOrderId(null);
    }
  };

  const resetImportFileSelection = React.useCallback(() => {
    setSelectedImportFile(null);
    setValidateImportResult(null);
    setImportFileInputKey((prev) => prev + 1);
  }, []);

  const handleSelectImportFile = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = event.target.files?.[0] ?? null;
    setSelectedImportFile(file);
    setValidateImportResult(null);
    setLastImportJob(null);
  };

  const buildImportFormData = () => {
    if (!selectedImportFile) {
      return null;
    }

    const formData = new FormData();
    formData.append('file', selectedImportFile);
    return formData;
  };

  const handleDownloadTemplate = async () => {
    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can download order templates.'
      );
      return;
    }

    try {
      const blob = await triggerExportOrderTemplate(undefined).unwrap();
      const downloadUrl = URL.createObjectURL(blob);
      const link = document.createElement('a');

      link.href = downloadUrl;
      link.download = 'order_template.xlsx';
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(downloadUrl);

      notification.success('Order template downloaded successfully.');
    } catch (error) {
      notification.error('Failed to download order template.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleValidateImportFile = async () => {
    if (!canMutateOrders) {
      notification.error(
        'Only TMS_ADMIN or TMS_CUSTOMER can validate order imports.'
      );
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Please select an Excel file first.');
      return;
    }

    try {
      const result = await validateOrderImport(formData).unwrap();
      setValidateImportResult(result);

      if (result.is_success) {
        notification.success('File validated successfully.', {
          description: `${result.data.length} order(s) are ready to import.`,
        });
      } else {
        notification.error('Validation completed with errors.', {
          description:
            result.error_message ||
            'Please fix the Excel data before importing.',
        });
      }
    } catch (error) {
      notification.error('Failed to validate order import file.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleImportFile = async () => {
    if (!canMutateOrders) {
      notification.error('Only TMS_ADMIN or TMS_CUSTOMER can import orders.');
      return;
    }

    if (!validateImportResult) {
      notification.error('Please validate the selected file before importing.');
      return;
    }

    if (!validateImportResult.is_success) {
      notification.error(
        'Validation has errors. Please fix them before import.'
      );
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Please select an Excel file first.');
      return;
    }

    try {
      const importResult = await importOrderFile(formData).unwrap();
      setLastImportJob(importResult);
      resetImportFileSelection();

      notification.success('Order import job created.', {
        description: `Job #${importResult.id} is ${importResult.status}.`,
      });

      void refetch();
    } catch (error) {
      notification.error('Failed to import order file.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        <div className='flex flex-col gap-2'>
          <h1 className='text-2xl font-bold tracking-tight'>Orders</h1>
          <p className='text-muted-foreground'>
            Track first-mile orders based on your role and access scope.
          </p>
        </div>

        {canMutateOrders ? (
          <Button onClick={handleOpenCreateDialog}>
            <Plus className='mr-2 h-4 w-4' />
            New Order
          </Button>
        ) : null}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Shield className='h-5 w-5' />
            Access Scope
          </CardTitle>
          <CardDescription>
            The system automatically limits visible orders by your current role.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-2'>
          <Badge
            variant={canViewOrders ? 'secondary' : 'destructive'}
            className='w-fit'
          >
            {getScopeBadgeLabel(accessScope)}
          </Badge>
          <p className='text-sm text-muted-foreground'>
            {getScopeDescription(accessScope)}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Search & Filters</CardTitle>
          <CardDescription>
            Search by order code, customer order code, sender, or receiver
            name/phone.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleApplyFilters}
            className='flex flex-col gap-3 md:flex-row md:items-center'
          >
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                className='pl-10'
                value={keywordInput}
                onChange={(event) => setKeywordInput(event.target.value)}
                placeholder='Search orders...'
                disabled={!canViewOrders}
              />
            </div>

            <Select
              value={statusInput}
              onValueChange={(value) =>
                setStatusInput(value as OrderStatusFilter)
              }
              disabled={!canViewOrders}
            >
              <SelectTrigger className='w-full md:w-[220px]'>
                <SelectValue placeholder='Filter by status' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All statuses</SelectItem>
                {ORDER_STATUS_OPTIONS.map((statusOption) => (
                  <SelectItem key={statusOption} value={statusOption}>
                    {formatStatusLabel(statusOption)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Button type='submit' disabled={!canViewOrders}>
              Apply
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={() => refetch()}
              disabled={!canViewOrders || isFetching}
            >
              <RefreshCw className='mr-2 h-4 w-4' />
              Refresh
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Excel Import</CardTitle>
          <CardDescription>
            Download template, validate the completed file, then import orders.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='flex flex-col gap-2 lg:flex-row lg:items-center'>
            <Button
              type='button'
              variant='outline'
              onClick={handleDownloadTemplate}
              disabled={!canMutateOrders || isImportFlowBusy}
            >
              {isExportingTemplate ? (
                <Loader2 className='h-4 w-4 mr-2 animate-spin' />
              ) : (
                <Download className='h-4 w-4 mr-2' />
              )}
              Download template
            </Button>

            <Input
              key={importFileInputKey}
              type='file'
              accept='.xlsx,.xls'
              onChange={handleSelectImportFile}
              disabled={!canMutateOrders || isImportFlowBusy}
              className='lg:max-w-sm'
            />

            <Button
              type='button'
              variant='outline'
              onClick={handleValidateImportFile}
              disabled={
                !canMutateOrders || !selectedImportFile || isImportFlowBusy
              }
            >
              {isValidatingImport && (
                <Loader2 className='h-4 w-4 mr-2 animate-spin' />
              )}
              Validate file
            </Button>

            <Button
              type='button'
              onClick={handleImportFile}
              disabled={
                !canMutateOrders ||
                !selectedImportFile ||
                !validateImportResult?.is_success ||
                isImportFlowBusy
              }
            >
              {isImportingOrders ? (
                <Loader2 className='h-4 w-4 mr-2 animate-spin' />
              ) : (
                <FileUp className='h-4 w-4 mr-2' />
              )}
              Import file
            </Button>
          </div>

          {!canMutateOrders && (
            <p className='text-xs text-muted-foreground'>
              Import actions require TMS_ADMIN or TMS_CUSTOMER permission.
            </p>
          )}

          {selectedImportFile && (
            <p className='text-sm text-muted-foreground'>
              Selected file: {selectedImportFile.name}
            </p>
          )}

          {validateImportResult && (
            <div className='rounded-lg border p-3 space-y-3'>
              <div className='flex flex-wrap items-center gap-x-4 gap-y-1'>
                <p className='text-sm font-medium'>
                  Validation:{' '}
                  {validateImportResult.is_success ? 'Success' : 'Failed'}
                </p>
                <p className='text-xs text-muted-foreground'>
                  File ID: {validateImportResult.file_id}
                </p>
                <p className='text-xs text-muted-foreground'>
                  Parsed rows: {validateImportResult.data.length}
                </p>
              </div>

              {validateImportResult.error_message && (
                <pre className='whitespace-pre-wrap rounded-md bg-muted p-2 text-xs text-destructive'>
                  {validateImportResult.error_message}
                </pre>
              )}

              {validatedPreviewItems.length > 0 && (
                <div className='space-y-2'>
                  <p className='text-xs text-muted-foreground'>
                    Preview {validatedPreviewItems.length}/
                    {validateImportResult.data.length} validated order(s)
                  </p>

                  <div className='grid gap-2 sm:grid-cols-2'>
                    {validatedPreviewItems.map((item, index) => (
                      <div
                        key={`${item.customer_order_code || 'order'}-${index}`}
                        className='rounded-md border p-2 text-xs space-y-1'
                      >
                        <p className='font-medium'>
                          {item.customer_order_code || '-'} |{' '}
                          {item.order_type || '-'} | {item.fee_payer || '-'}
                        </p>
                        <p className='text-muted-foreground'>
                          Sender:{' '}
                          {item.sender_name
                            ? `${item.sender_name}${item.sender_phone ? ` (${item.sender_phone})` : ''}`
                            : '-'}
                        </p>
                        <p className='text-muted-foreground'>
                          Receiver:{' '}
                          {item.receiver_name
                            ? `${item.receiver_name}${item.receiver_phone ? ` (${item.receiver_phone})` : ''}`
                            : '-'}
                        </p>
                        <p className='text-muted-foreground'>
                          COD: {item.is_cod ? 'Yes' : 'No'} | Products:{' '}
                          {item.products?.length ?? 0}
                        </p>
                        <p className='text-muted-foreground'>
                          Product preview:{' '}
                          {formatOrderImportProductsPreview(item.products)}
                        </p>
                        <p className='text-muted-foreground'>
                          Source rows:{' '}
                          {item.source_rows?.length
                            ? item.source_rows.join(', ')
                            : '-'}
                        </p>
                      </div>
                    ))}
                  </div>

                  {validateImportResult.data.length > IMPORT_PREVIEW_LIMIT && (
                    <p className='text-xs text-muted-foreground'>
                      Showing only the first {IMPORT_PREVIEW_LIMIT} order(s).
                    </p>
                  )}
                </div>
              )}
            </div>
          )}

          {lastImportJob && (
            <div className='rounded-lg border p-3 space-y-1'>
              <p className='text-sm font-medium'>Latest import job</p>
              <p className='text-xs text-muted-foreground'>
                #{lastImportJob.id} - {lastImportJob.file_name}
              </p>
              <p className='text-xs text-muted-foreground'>
                Status: {lastImportJob.status} | Success/Failed:{' '}
                {lastImportJob.success_records}/{lastImportJob.failed_records}
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
        </CardHeader>
        <CardContent>
          {!canViewOrders ? (
            <p className='text-muted-foreground'>
              You do not have permission to access order data.
            </p>
          ) : isLoading ? (
            <div className='flex items-center gap-2 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Loading orders...
            </div>
          ) : data && data.items.length > 0 ? (
            <div className='space-y-3'>
              {data.items.map((order) => (
                <div
                  key={order.id}
                  className='rounded-lg border p-3 flex flex-col gap-2'
                >
                  <div className='flex flex-wrap items-center justify-between gap-2'>
                    <div>
                      <p className='font-medium'>{order.orderCode}</p>
                      <p className='text-xs text-muted-foreground'>
                        Customer order: {order.customerOrderCode || '--'}
                      </p>
                    </div>
                    <div className='flex items-center gap-2'>
                      <Badge variant={getStatusBadgeVariant(order.status)}>
                        {formatStatusLabel(order.status)}
                      </Badge>
                      <Badge variant='outline'>
                        {order.isConfirm ? 'Confirmed' : 'Pending confirm'}
                      </Badge>
                      <Button
                        size='sm'
                        variant='outline'
                        onClick={() => void handleOpenOrderDetail(order.id)}
                        disabled={loadingOrderActionId === order.id}
                      >
                        {loadingOrderActionId === order.id ? (
                          <Loader2 className='mr-2 h-3.5 w-3.5 animate-spin' />
                        ) : (
                          <Eye className='mr-2 h-3.5 w-3.5' />
                        )}
                        Details
                      </Button>
                      {canMutateOrders && isDraftOrder(order) ? (
                        <>
                          <Button
                            size='sm'
                            variant='outline'
                            onClick={() => void handleOpenEditOrder(order)}
                            disabled={loadingOrderActionId === order.id}
                          >
                            <Pencil className='mr-2 h-3.5 w-3.5' />
                            Edit
                          </Button>
                          <Button
                            size='sm'
                            variant='outline'
                            onClick={() => handleRequestCancelOrder(order)}
                          >
                            <XCircle className='mr-2 h-3.5 w-3.5' />
                            Cancel
                          </Button>
                          <Button
                            size='sm'
                            variant='destructive'
                            onClick={() => handleRequestDeleteOrder(order)}
                          >
                            <Trash2 className='mr-2 h-3.5 w-3.5' />
                            Delete
                          </Button>
                          {isConfirmableStatus(order.status) ? (
                            <Button
                              size='sm'
                              variant='outline'
                              onClick={() => void handleConfirmOrder(order)}
                              disabled={confirmingOrderId === order.id}
                            >
                              {confirmingOrderId === order.id ? (
                                <Loader2 className='mr-2 h-3.5 w-3.5 animate-spin' />
                              ) : (
                                <CheckCircle2 className='mr-2 h-3.5 w-3.5' />
                              )}
                              Confirm
                            </Button>
                          ) : null}
                        </>
                      ) : null}
                    </div>
                  </div>

                  <p className='text-xs text-muted-foreground'>
                    Sender:{' '}
                    {buildOrderAddressLabel(
                      order.senderName,
                      order.senderPhone,
                      order.senderAddressDetail
                    )}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Receiver:{' '}
                    {buildOrderAddressLabel(
                      order.receiverName,
                      order.receiverPhone,
                      order.receiverAddressDetail
                    )}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Assigned post office:{' '}
                    {buildPostOfficeAssignmentLabel(order)}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Pickup window: {formatDateTime(order.pickupTimeStart)} -{' '}
                    {formatDateTime(order.pickupTimeEnd)}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    Created by: {order.createdBy || '--'} | Updated:{' '}
                    {formatDateTime(order.updatedAt)}
                  </p>
                </div>
              ))}

              <div className='flex items-center justify-between pt-2'>
                <Button
                  variant='outline'
                  onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                  disabled={!data.hasPrevious || isFetching}
                >
                  Previous
                </Button>
                <span className='text-sm text-muted-foreground'>
                  Page {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
                </span>
                <Button
                  variant='outline'
                  onClick={() => setPage((prev) => prev + 1)}
                  disabled={!data.hasNext || isFetching}
                >
                  Next
                </Button>
              </div>
            </div>
          ) : (
            <p className='text-muted-foreground'>No orders found.</p>
          )}
        </CardContent>
      </Card>

      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
          <DialogHeader>
            <DialogTitle>
              {orderFormMode === 'create' ? 'Create Order' : 'Edit Order'}
            </DialogTitle>
            <DialogDescription>
              {orderFormMode === 'create'
                ? 'Create a first-mile order as customer or admin. Required fields follow backend validation rules.'
                : 'Update an existing first-mile order. Only newly created and unconfirmed orders can be edited.'}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleCreateOrder} className='space-y-5'>
            <div className='grid gap-4 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='customerOrderCode'>Customer order code *</Label>
                <Input
                  id='customerOrderCode'
                  value={createForm.customerOrderCode}
                  onChange={(event) =>
                    setCreateForm((prev) => ({
                      ...prev,
                      customerOrderCode: event.target.value,
                    }))
                  }
                  placeholder='CUS-ORDER-001'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='note'>Note</Label>
                <Textarea
                  id='note'
                  value={createForm.note}
                  onChange={(event) =>
                    setCreateForm((prev) => ({
                      ...prev,
                      note: event.target.value,
                    }))
                  }
                  placeholder='Optional note for pickup/delivery'
                  rows={2}
                />
              </div>
            </div>

            <div className='space-y-3 rounded-md border p-3'>
              <h3 className='text-sm font-semibold'>Sender information</h3>
              <div className='grid gap-3 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='senderName'>Sender name *</Label>
                  <Input
                    id='senderName'
                    value={createForm.senderName}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        senderName: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='senderPhone'>Sender phone *</Label>
                  <Input
                    id='senderPhone'
                    value={createForm.senderPhone}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        senderPhone: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='senderProvinceCode'>Sender province *</Label>
                  <Select
                    value={selectedSenderProvinceCode || undefined}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        senderProvinceCode: value,
                        senderWardCode: '',
                      }))
                    }
                  >
                    <SelectTrigger id='senderProvinceCode'>
                      <SelectValue placeholder='Select province' />
                    </SelectTrigger>
                    <SelectContent>
                      {provinceSelectOptions.map((province) => {
                        const provinceCode = normalizeLocationCode(
                          province.provinceCode
                        );

                        if (!provinceCode) {
                          return null;
                        }

                        return (
                          <SelectItem key={provinceCode} value={provinceCode}>
                            {province.name} ({provinceCode})
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='senderWardCode'>Sender ward *</Label>
                  <Select
                    value={selectedSenderWardCode || undefined}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        senderWardCode: value,
                      }))
                    }
                    disabled={!selectedSenderProvinceCode}
                  >
                    <SelectTrigger id='senderWardCode'>
                      <SelectValue
                        placeholder={
                          selectedSenderProvinceCode
                            ? 'Select ward'
                            : 'Select province first'
                        }
                      />
                    </SelectTrigger>
                    <SelectContent>
                      {selectedSenderProvinceCode && isFetchingSenderWards ? (
                        <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                          Loading wards...
                        </p>
                      ) : senderWardSelectOptions.length > 0 ? (
                        senderWardSelectOptions.map((ward) => {
                          const wardCode = normalizeLocationCode(ward.wardCode);

                          if (!wardCode) {
                            return null;
                          }

                          return (
                            <SelectItem key={wardCode} value={wardCode}>
                              {ward.name} ({wardCode})
                            </SelectItem>
                          );
                        })
                      ) : (
                        <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                          No wards available.
                        </p>
                      )}
                    </SelectContent>
                  </Select>
                </div>
                <div className='space-y-2 md:col-span-2'>
                  <Label htmlFor='senderAddressDetail'>Sender address *</Label>
                  <Input
                    id='senderAddressDetail'
                    value={createForm.senderAddressDetail}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        senderAddressDetail: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2 md:col-span-2'>
                  <Label>Sender coordinates *</Label>
                  <div className='grid gap-2 md:grid-cols-2'>
                    <Input
                      value={createForm.senderLatitude}
                      placeholder='Latitude'
                      readOnly
                    />
                    <Input
                      value={createForm.senderLongitude}
                      placeholder='Longitude'
                      readOnly
                    />
                  </div>
                  <div className='flex flex-wrap gap-2'>
                    <Button
                      type='button'
                      variant='outline'
                      onClick={() => void handleGeocodeFromAddress('sender')}
                      disabled={geocodingTarget === 'sender'}
                    >
                      {geocodingTarget === 'sender' ? (
                        <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      ) : null}
                      Geocode from address
                    </Button>
                  </div>
                  <CoordinatePickerMap
                    latitude={parseOptionalNumberInput(
                      createForm.senderLatitude
                    )}
                    longitude={parseOptionalNumberInput(
                      createForm.senderLongitude
                    )}
                    onChange={(latitude, longitude) =>
                      handleMapCoordinateChange('sender', latitude, longitude)
                    }
                    className='h-56'
                  />
                  <p className='text-xs text-muted-foreground'>
                    Click on the map to pick sender coordinates.
                  </p>
                </div>
              </div>
            </div>

            <div className='space-y-3 rounded-md border p-3'>
              <h3 className='text-sm font-semibold'>Receiver information</h3>
              <div className='grid gap-3 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='receiverName'>Receiver name *</Label>
                  <Input
                    id='receiverName'
                    value={createForm.receiverName}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        receiverName: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='receiverPhone'>Receiver phone *</Label>
                  <Input
                    id='receiverPhone'
                    value={createForm.receiverPhone}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        receiverPhone: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='receiverProvinceCode'>
                    Receiver province *
                  </Label>
                  <Select
                    value={selectedReceiverProvinceCode || undefined}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        receiverProvinceCode: value,
                        receiverWardCode: '',
                      }))
                    }
                  >
                    <SelectTrigger id='receiverProvinceCode'>
                      <SelectValue placeholder='Select province' />
                    </SelectTrigger>
                    <SelectContent>
                      {provinceSelectOptions.map((province) => {
                        const provinceCode = normalizeLocationCode(
                          province.provinceCode
                        );

                        if (!provinceCode) {
                          return null;
                        }

                        return (
                          <SelectItem key={provinceCode} value={provinceCode}>
                            {province.name} ({provinceCode})
                          </SelectItem>
                        );
                      })}
                    </SelectContent>
                  </Select>
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='receiverWardCode'>Receiver ward *</Label>
                  <Select
                    value={selectedReceiverWardCode || undefined}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        receiverWardCode: value,
                      }))
                    }
                    disabled={!selectedReceiverProvinceCode}
                  >
                    <SelectTrigger id='receiverWardCode'>
                      <SelectValue
                        placeholder={
                          selectedReceiverProvinceCode
                            ? 'Select ward'
                            : 'Select province first'
                        }
                      />
                    </SelectTrigger>
                    <SelectContent>
                      {selectedReceiverProvinceCode &&
                      isFetchingReceiverWards ? (
                        <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                          Loading wards...
                        </p>
                      ) : receiverWardSelectOptions.length > 0 ? (
                        receiverWardSelectOptions.map((ward) => {
                          const wardCode = normalizeLocationCode(ward.wardCode);

                          if (!wardCode) {
                            return null;
                          }

                          return (
                            <SelectItem key={wardCode} value={wardCode}>
                              {ward.name} ({wardCode})
                            </SelectItem>
                          );
                        })
                      ) : (
                        <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                          No wards available.
                        </p>
                      )}
                    </SelectContent>
                  </Select>
                </div>
                <div className='space-y-2 md:col-span-2'>
                  <Label htmlFor='receiverAddressDetail'>
                    Receiver address *
                  </Label>
                  <Input
                    id='receiverAddressDetail'
                    value={createForm.receiverAddressDetail}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        receiverAddressDetail: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2 md:col-span-2'>
                  <Label>Receiver coordinates *</Label>
                  <div className='grid gap-2 md:grid-cols-2'>
                    <Input
                      value={createForm.receiverLatitude}
                      placeholder='Latitude'
                      readOnly
                    />
                    <Input
                      value={createForm.receiverLongitude}
                      placeholder='Longitude'
                      readOnly
                    />
                  </div>
                  <div className='flex flex-wrap gap-2'>
                    <Button
                      type='button'
                      variant='outline'
                      onClick={() => void handleGeocodeFromAddress('receiver')}
                      disabled={geocodingTarget === 'receiver'}
                    >
                      {geocodingTarget === 'receiver' ? (
                        <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      ) : null}
                      Geocode from address
                    </Button>
                  </div>
                  <CoordinatePickerMap
                    latitude={parseOptionalNumberInput(
                      createForm.receiverLatitude
                    )}
                    longitude={parseOptionalNumberInput(
                      createForm.receiverLongitude
                    )}
                    onChange={(latitude, longitude) =>
                      handleMapCoordinateChange('receiver', latitude, longitude)
                    }
                    className='h-56'
                  />
                  <p className='text-xs text-muted-foreground'>
                    Click on the map to pick receiver coordinates.
                  </p>
                </div>
              </div>
            </div>

            <div className='space-y-3 rounded-md border p-3'>
              <h3 className='text-sm font-semibold'>Order options</h3>
              <div className='grid gap-3 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label>Delivery request time *</Label>
                  <Select
                    value={createForm.deliveryRequestTime}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        deliveryRequestTime:
                          value as FirstMileDeliveryRequestTime,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {DELIVERY_REQUEST_TIME_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label>Order type *</Label>
                  <Select
                    value={createForm.orderType}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        orderType: value as FirstMileOrderType,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {ORDER_TYPE_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label>Fee payer *</Label>
                  <Select
                    value={createForm.feePayer}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        feePayer: value as FirstMileFeePayer,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {FEE_PAYER_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label>COD</Label>
                  <Select
                    value={createForm.isCod}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        isCod: value as 'true' | 'false',
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='false'>No COD</SelectItem>
                      <SelectItem value='true'>COD</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2 md:col-span-2'>
                  <Label>Product category</Label>
                  <Select
                    value={createForm.orderProductCategory}
                    onValueChange={(value) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        orderProductCategory:
                          value as CreateOrderFormState['orderProductCategory'],
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select category (optional)' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='NONE'>No category</SelectItem>
                      {ORDER_PRODUCT_CATEGORY_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            <div className='space-y-3 rounded-md border p-3'>
              <h3 className='text-sm font-semibold'>Pickup and dimensions</h3>
              <div className='grid gap-3 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='pickupTimeStart'>Pickup start</Label>
                  <Input
                    id='pickupTimeStart'
                    type='datetime-local'
                    value={createForm.pickupTimeStart}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        pickupTimeStart: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='pickupTimeEnd'>Pickup end</Label>
                  <Input
                    id='pickupTimeEnd'
                    type='datetime-local'
                    value={createForm.pickupTimeEnd}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        pickupTimeEnd: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='dimensionLengthCm'>Length (cm)</Label>
                  <Input
                    id='dimensionLengthCm'
                    type='number'
                    step='any'
                    value={createForm.dimensionLengthCm}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        dimensionLengthCm: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='dimensionWidthCm'>Width (cm)</Label>
                  <Input
                    id='dimensionWidthCm'
                    type='number'
                    step='any'
                    value={createForm.dimensionWidthCm}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        dimensionWidthCm: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='dimensionHeightCm'>Height (cm)</Label>
                  <Input
                    id='dimensionHeightCm'
                    type='number'
                    step='any'
                    value={createForm.dimensionHeightCm}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        dimensionHeightCm: event.target.value,
                      }))
                    }
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='totalVolumeM3'>Total volume (m3)</Label>
                  <Input
                    id='totalVolumeM3'
                    type='number'
                    step='any'
                    value={createForm.totalVolumeM3}
                    onChange={(event) =>
                      setCreateForm((prev) => ({
                        ...prev,
                        totalVolumeM3: event.target.value,
                      }))
                    }
                  />
                </div>
              </div>
            </div>

            <DialogFooter>
              <Button
                type='button'
                variant='outline'
                onClick={() => setIsCreateDialogOpen(false)}
                disabled={isSubmittingOrder}
              >
                Cancel
              </Button>
              <Button type='submit' disabled={isSubmittingOrder}>
                {isSubmittingOrder ? (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                ) : null}
                {orderFormMode === 'create' ? 'Create order' : 'Update order'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog
        open={isDetailDialogOpen}
        onOpenChange={(open) => {
          setIsDetailDialogOpen(open);
          if (!open) {
            setDetailOrder(null);
          }
        }}
      >
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>Order Details</DialogTitle>
            <DialogDescription>
              View full information of a first-mile order.
            </DialogDescription>
          </DialogHeader>

          {!detailOrder ? (
            <div className='flex items-center gap-2 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Loading order details...
            </div>
          ) : (
            <div className='space-y-4 text-sm'>
              <div className='grid gap-3 md:grid-cols-2'>
                <div>
                  <p className='text-muted-foreground'>Order code</p>
                  <p className='font-medium'>{detailOrder.orderCode}</p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Customer order code</p>
                  <p className='font-medium'>
                    {detailOrder.customerOrderCode || '--'}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Status</p>
                  <p className='font-medium'>
                    {formatStatusLabel(detailOrder.status)}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Confirmation</p>
                  <p className='font-medium'>
                    {detailOrder.isConfirm ? 'Confirmed' : 'Pending confirm'}
                  </p>
                </div>
              </div>

              <div className='space-y-2 rounded-md border p-3'>
                <p className='font-semibold'>Sender</p>
                <p>
                  {buildOrderAddressLabel(
                    detailOrder.senderName,
                    detailOrder.senderPhone,
                    detailOrder.senderAddressDetail
                  )}
                </p>
                <p className='text-muted-foreground'>
                  {detailOrder.senderProvinceCode || '--'} /{' '}
                  {detailOrder.senderWardCode || '--'}
                </p>
                <p className='text-muted-foreground'>
                  Coordinates: {detailOrder.senderLatitude ?? '--'},
                  {detailOrder.senderLongitude ?? '--'}
                </p>
              </div>

              <div className='space-y-2 rounded-md border p-3'>
                <p className='font-semibold'>Receiver</p>
                <p>
                  {buildOrderAddressLabel(
                    detailOrder.receiverName,
                    detailOrder.receiverPhone,
                    detailOrder.receiverAddressDetail
                  )}
                </p>
                <p className='text-muted-foreground'>
                  {detailOrder.receiverProvinceCode || '--'} /{' '}
                  {detailOrder.receiverWardCode || '--'}
                </p>
                <p className='text-muted-foreground'>
                  Coordinates: {detailOrder.receiverLatitude ?? '--'},
                  {detailOrder.receiverLongitude ?? '--'}
                </p>
              </div>

              <div className='grid gap-3 md:grid-cols-2'>
                <div>
                  <p className='text-muted-foreground'>Pickup start</p>
                  <p className='font-medium'>
                    {formatDateTime(detailOrder.pickupTimeStart)}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Pickup end</p>
                  <p className='font-medium'>
                    {formatDateTime(detailOrder.pickupTimeEnd)}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Delivery request time</p>
                  <p className='font-medium'>
                    {detailOrder.deliveryRequestTime || '--'}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Order type</p>
                  <p className='font-medium'>{detailOrder.orderType || '--'}</p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Fee payer</p>
                  <p className='font-medium'>{detailOrder.feePayer || '--'}</p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Payment status</p>
                  <p className='font-medium'>
                    {detailOrder.paymentStatus || '--'}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Total weight</p>
                  <p className='font-medium'>
                    {detailOrder.totalWeight ?? '--'}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Total value</p>
                  <p className='font-medium'>
                    {detailOrder.totalValue ?? '--'}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>Total volume</p>
                  <p className='font-medium'>
                    {detailOrder.totalVolume ?? '--'}
                  </p>
                </div>
                <div>
                  <p className='text-muted-foreground'>COD amount</p>
                  <p className='font-medium'>{detailOrder.codAmount ?? '--'}</p>
                </div>
              </div>

              <div className='space-y-2'>
                <p className='text-muted-foreground'>Products</p>
                {detailOrder.products && detailOrder.products.length > 0 ? (
                  <div className='space-y-2'>
                    {detailOrder.products.map((product, index) => (
                      <div
                        key={`${detailOrder.id}-${product.id ?? index}`}
                        className='rounded-md border p-2'
                      >
                        <p className='font-medium'>{product.name || '--'}</p>
                        <p className='text-xs text-muted-foreground'>
                          Qty: {product.quantity ?? '--'} | Weight:{' '}
                          {product.weight ?? '--'}g | Value:{' '}
                          {product.value ?? '--'}
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className='text-muted-foreground'>No product details.</p>
                )}
              </div>

              {detailOrder.note ? (
                <div>
                  <p className='text-muted-foreground'>Note</p>
                  <p>{detailOrder.note}</p>
                </div>
              ) : null}

              <div className='text-xs text-muted-foreground'>
                Created: {formatDateTime(detailOrder.createdAt)} | Updated:{' '}
                {formatDateTime(detailOrder.updatedAt)} | Updated by:{' '}
                {detailOrder.updatedBy || '--'}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Dialog
        open={Boolean(cancelTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setCancelTarget(null);
            setCancelReason('');
          }
        }}
      >
        <DialogContent className='sm:max-w-lg'>
          <DialogHeader>
            <DialogTitle>Cancel Order</DialogTitle>
            <DialogDescription>
              Cancel order {cancelTarget?.orderCode}. Provide a reason if
              needed.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-2'>
            <Label htmlFor='cancelReason'>Cancel reason</Label>
            <Textarea
              id='cancelReason'
              value={cancelReason}
              onChange={(event) => setCancelReason(event.target.value)}
              placeholder='Optional reason for cancellation'
              rows={3}
            />
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => {
                setCancelTarget(null);
                setCancelReason('');
              }}
              disabled={isCancellingOrder}
            >
              Keep order
            </Button>
            <Button
              type='button'
              variant='destructive'
              onClick={() => void handleCancelOrder()}
              disabled={isCancellingOrder}
            >
              {isCancellingOrder ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : null}
              Confirm cancel
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null);
          }
        }}
        title='Delete Order'
        description={
          deleteTarget
            ? `Delete order ${deleteTarget.orderCode}? This action will be mapped to cancel status because backend does not expose hard-delete API.`
            : 'Delete this order?'
        }
        confirmText='Delete order'
        cancelText='Keep order'
        onConfirm={() => void handleDeleteOrder()}
        onCancel={() => setDeleteTarget(null)}
        isLoading={isCancellingOrder}
        variant='destructive'
      />
    </div>
  );
};
