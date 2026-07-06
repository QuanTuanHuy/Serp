/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile order list page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppDispatch, useAppSelector } from '@/lib/store';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import {
  useConfirmDropOffOrderAtPostOfficeMutation,
  firstMileApi,
  useCancelOrderMutation,
  useCalculateShippingFeeMutation,
  useConfirmOrderPaymentMutation,
  useConfirmOrderMutation,
  useCreateOrderMutation,
  useGeocodeAddressMutation,
  useImportOrdersMutation,
  useInitiateOrderPaymentMutation,
  useLazyExportOrderTemplateQuery,
  useLazyGetDropOffPostOfficeSuggestionsQuery,
  useLazyGetOrderByIdQuery,
  useGetOrdersQuery,
  useGetPostOfficesQuery,
  useGetProductTypesQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useUpdateOrderMutation,
  useValidateOrderImportMutation,
} from '../../../api';
import type {
  CalculateShippingFeeRequest,
  CalculateShippingFeeResponse,
  CancelOrderRequest,
  CreateOrderRequest,
  FirstMileOrderDetail,
  FirstMileOrderListFilters,
  ImportHistory,
  OrderDropOffPostOfficeSuggestion,
  OrderConfirmationResponse,
  OrderImportItem,
  OrderPaymentInitResponse,
  PostOffice,
  ProductType,
  Province,
  UpdateOrderRequest,
  ValidateImportFileResponse,
} from '../../../types';
import {
  buildOrderListFilters,
  DEFAULT_ORDER_FILTER_FORM,
  type OrderFilterFormState,
} from './orderFilterModels';
import {
  OrderCancelDialog,
  OrderConfirmDialog,
  OrderDetailDialog,
  OrderDropOffManagerConfirmCard,
  OrderDropOffSuggestionsDialog,
  OrderFormDialog,
  OrderImportCard,
  OrderPageHeader,
  OrderResultsCard,
  OrderStatsCards,
} from './components';
import {
  buildOrderAddressLabel,
  buildPostOfficeAssignmentLabel,
  buildWardSelectOptions,
  DEFAULT_CREATE_ORDER_FORM,
  formatDateTime,
  formatPickupMethodLabel,
  formatStatusLabel,
  getProvinceNameByCode,
  getStatusBadgeVariant,
  isDropOffOrder,
  isConfirmableStatus,
  isDraftOrder,
  mapOrderProductsToRequest,
  mapOrderToFormState,
  normalizeLocationCode,
  ORDER_STATUS_OPTIONS,
  PAGE_SIZE,
  parseOptionalNumberInput,
  parseRequiredNumberInput,
  resolveOrderAccessScope,
  type CreateOrderFormState,
  type LocationTarget,
  type OrderFormMode,
} from './orderPageModels';

const PAYMENT_RESULT_MESSAGE_TYPE = 'SERP_PAYMENT_RESULT';

const formatConfirmationPostOffice = (
  label: string,
  postOffice: { code?: string | null; name?: string | null } | null | undefined
): string | null => {
  const code = postOffice?.code?.trim();
  if (!code) {
    return null;
  }

  const name = postOffice?.name?.trim();
  return name ? `${label}: ${code} - ${name}` : `${label}: ${code}`;
};

const buildConfirmationPostOfficeDescription = (
  confirmation: OrderConfirmationResponse
): string | undefined => {
  const details: string[] = [];

  const originDetail = formatConfirmationPostOffice(
    'Bưu cục gốc',
    confirmation.originPostOffice
  );
  if (originDetail) {
    details.push(originDetail);
  }

  const destinationDetail = formatConfirmationPostOffice(
    'Bưu cục đích',
    confirmation.destinationPostOffice
  );
  if (destinationDetail) {
    details.push(destinationDetail);
  }

  return details.length > 0 ? details.join(' | ') : undefined;
};

export const OrderListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];
  const notification = useNotification();

  const [page, setPage] = React.useState(0);
  const [filterFormValues, setFilterFormValues] =
    React.useState<OrderFilterFormState>(DEFAULT_ORDER_FILTER_FORM);
  const [appliedFilters, setAppliedFilters] =
    React.useState<FirstMileOrderListFilters>({});
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
  const [confirmDialogOrder, setConfirmDialogOrder] =
    React.useState<FirstMileOrderDetail | null>(null);
  const [isConfirmDialogOpen, setIsConfirmDialogOpen] = React.useState(false);
  const [shippingFeeQuote, setShippingFeeQuote] =
    React.useState<CalculateShippingFeeResponse | null>(null);
  const [paymentInitResult, setPaymentInitResult] =
    React.useState<OrderPaymentInitResponse | null>(null);
  const [isAwaitingPaymentCompletion, setIsAwaitingPaymentCompletion] =
    React.useState(false);
  const [isProcessingPaymentWebhook, setIsProcessingPaymentWebhook] =
    React.useState(false);
  const paymentCompletionHandledRef = React.useRef(false);
  const paymentResultHandlingInProgressRef = React.useRef(false);
  const lastHandledPaymentMessageKeyRef = React.useRef<string | null>(null);
  const [dropOffSuggestionTarget, setDropOffSuggestionTarget] =
    React.useState<FirstMileOrderDetail | null>(null);
  const [dropOffSuggestions, setDropOffSuggestions] = React.useState<
    OrderDropOffPostOfficeSuggestion[]
  >([]);
  const [loadingDropOffSuggestionOrderId, setLoadingDropOffSuggestionOrderId] =
    React.useState<number | null>(null);
  const [managerDropOffOrderIdInput, setManagerDropOffOrderIdInput] =
    React.useState('');
  const [managerDropOffPostOfficeIdInput, setManagerDropOffPostOfficeIdInput] =
    React.useState('');
  const [geocodingTarget, setGeocodingTarget] =
    React.useState<LocationTarget | null>(null);
  const [selectedImportFile, setSelectedImportFile] =
    React.useState<File | null>(null);
  const [importFileInputKey, setImportFileInputKey] = React.useState(0);
  const [validateImportResult, setValidateImportResult] =
    React.useState<ValidateImportFileResponse<OrderImportItem> | null>(null);
  const [lastImportJob, setLastImportJob] =
    React.useState<ImportHistory | null>(null);
  const [wardNamesByProvinceCode, setWardNamesByProvinceCode] = React.useState<
    Record<string, Record<string, string>>
  >({});

  const updateCreateFormField = React.useCallback(
    <K extends keyof CreateOrderFormState>(
      field: K,
      value: CreateOrderFormState[K]
    ) => {
      setCreateForm((prev) => ({
        ...prev,
        [field]: value,
      }));
    },
    []
  );

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
  const canConfirmDropOffAtPostOffice = accessScope === 'MANAGER_POST_OFFICE';

  const { data, isLoading, isFetching, refetch } = useGetOrdersQuery(
    {
      page,
      size: PAGE_SIZE,
      ...appliedFilters,
    },
    {
      skip: !canViewOrders,
    }
  );

  const updateFilterField = React.useCallback(
    <K extends keyof OrderFilterFormState>(
      field: K,
      value: OrderFilterFormState[K]
    ) => {
      setFilterFormValues((prev) => ({
        ...prev,
        [field]: value,
      }));
    },
    []
  );

  const {
    data: managerPostOfficesData,
    isFetching: isFetchingManagerPostOffices,
  } = useGetPostOfficesQuery(
    {
      page: 0,
      size: 200,
    },
    {
      skip: !canViewOrders,
    }
  );

  const { data: provincesData } = useGetProvincesQuery({
    page: 0,
    size: 200,
  });
  const { data: productTypesData, isFetching: isFetchingProductTypes } =
    useGetProductTypesQuery({
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

  const productTypeOptions = React.useMemo<ProductType[]>(
    () => productTypesData?.items.filter((item) => item.isActive) ?? [],
    [productTypesData]
  );

  const managerPostOfficeOptions = React.useMemo<PostOffice[]>(
    () => managerPostOfficesData?.items ?? [],
    [managerPostOfficesData]
  );

  React.useEffect(() => {
    if (
      !canConfirmDropOffAtPostOffice ||
      managerDropOffPostOfficeIdInput ||
      managerPostOfficeOptions.length !== 1
    ) {
      return;
    }

    setManagerDropOffPostOfficeIdInput(String(managerPostOfficeOptions[0].id));
  }, [
    canConfirmDropOffAtPostOffice,
    managerDropOffPostOfficeIdInput,
    managerPostOfficeOptions,
  ]);

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

  const provinceNameByCode = React.useMemo(
    () => getProvinceNameByCode(provinceSelectOptions),
    [provinceSelectOptions]
  );

  const detailProvinceCodes = React.useMemo(() => {
    const senderProvinceCode = normalizeLocationCode(
      detailOrder?.senderProvinceCode
    );
    const receiverProvinceCode = normalizeLocationCode(
      detailOrder?.receiverProvinceCode
    );

    return Array.from(
      new Set(
        [senderProvinceCode, receiverProvinceCode].filter(
          (provinceCode): provinceCode is string => Boolean(provinceCode)
        )
      )
    );
  }, [detailOrder?.receiverProvinceCode, detailOrder?.senderProvinceCode]);

  React.useEffect(() => {
    const missingProvinceCodes = detailProvinceCodes.filter(
      (provinceCode) => !(provinceCode in wardNamesByProvinceCode)
    );

    if (missingProvinceCodes.length === 0) {
      return;
    }

    let isCancelled = false;

    const fetchMissingWardNames = async () => {
      await Promise.all(
        missingProvinceCodes.map(async (provinceCode) => {
          try {
            const wardPage = await dispatch(
              firstMileApi.endpoints.getWardsByProvinceCode.initiate(
                {
                  provinceCode,
                  page: 0,
                  size: 1000,
                },
                {
                  subscribe: false,
                }
              )
            ).unwrap();

            if (isCancelled) {
              return;
            }

            const wardNameByCode = wardPage.items.reduce<
              Record<string, string>
            >((accumulator, ward) => {
              const wardCode = normalizeLocationCode(ward.wardCode);

              if (wardCode) {
                accumulator[wardCode] = ward.name;
              }

              return accumulator;
            }, {});

            setWardNamesByProvinceCode((prev) => ({
              ...prev,
              [provinceCode]: wardNameByCode,
            }));
          } catch {
            if (isCancelled) {
              return;
            }

            setWardNamesByProvinceCode((prev) => ({
              ...prev,
              [provinceCode]: {},
            }));
          }
        })
      );
    };

    void fetchMissingWardNames();

    return () => {
      isCancelled = true;
    };
  }, [detailProvinceCodes, dispatch, wardNamesByProvinceCode]);

  const getProvinceLabel = React.useCallback(
    (provinceCode?: string) => {
      const normalizedProvinceCode = normalizeLocationCode(provinceCode);

      if (!normalizedProvinceCode) {
        return '--';
      }

      return (
        provinceNameByCode[normalizedProvinceCode] || normalizedProvinceCode
      );
    },
    [provinceNameByCode]
  );

  const getWardLabel = React.useCallback(
    (provinceCode?: string, wardCode?: string) => {
      const normalizedWardCode = normalizeLocationCode(wardCode);

      if (!normalizedWardCode) {
        return '--';
      }

      const normalizedProvinceCode = normalizeLocationCode(provinceCode);

      if (!normalizedProvinceCode) {
        return normalizedWardCode;
      }

      const wardNameByCode = wardNamesByProvinceCode[normalizedProvinceCode];

      return wardNameByCode?.[normalizedWardCode] || normalizedWardCode;
    },
    [wardNamesByProvinceCode]
  );

  const [createOrder, { isLoading: isCreatingOrder }] =
    useCreateOrderMutation();
  const [updateOrder, { isLoading: isUpdatingOrder }] =
    useUpdateOrderMutation();
  const [cancelOrder, { isLoading: isCancellingOrder }] =
    useCancelOrderMutation();
  const [confirmOrder, { isLoading: isConfirmingOrder }] =
    useConfirmOrderMutation();
  const [calculateShippingFee, { isLoading: isCalculatingShippingFee }] =
    useCalculateShippingFeeMutation();
  const [initiateOrderPayment, { isLoading: isInitiatingOrderPayment }] =
    useInitiateOrderPaymentMutation();
  const [confirmOrderPayment] = useConfirmOrderPaymentMutation();
  const [confirmDropOffOrderAtPostOffice, { isLoading: isConfirmingDropOff }] =
    useConfirmDropOffOrderAtPostOfficeMutation();
  const [
    loadDropOffPostOfficeSuggestions,
    { isFetching: isFetchingDropOffSuggestions },
  ] = useLazyGetDropOffPostOfficeSuggestionsQuery();
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

  const handleApplyFilters = (
    values: OrderFilterFormState = filterFormValues
  ) => {
    try {
      const nextFilters = buildOrderListFilters(values);
      setPage(0);
      setAppliedFilters(nextFilters);
    } catch (error) {
      notification.error(
        error instanceof Error ? error.message : 'Giá trị bộ lọc không hợp lệ.'
      );
    }
  };

  const handleClearFilters = () => {
    setFilterFormValues(DEFAULT_ORDER_FILTER_FORM);
    setAppliedFilters({});
    setPage(0);
  };

  const handleTableFilterSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    handleApplyFilters();
  };

  const handleOpenCreateDialog = () => {
    if (!canMutateOrders) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể tạo đơn hàng first-mile.'
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
      notification.error('Vui lòng chọn tỉnh/thành trước khi lấy tọa độ.');
      return;
    }

    if (!wardCode) {
      notification.error('Vui lòng chọn phường/xã trước khi lấy tọa độ.');
      return;
    }

    if (!addressDetail) {
      notification.error(
        'Vui lòng nhập chi tiết địa chỉ trước khi lấy tọa độ.'
      );
      return;
    }

    const provinceName = provinceNameByCode[provinceCode] || provinceCode;
    const wardName =
      wardOptions.find(
        (ward) => normalizeLocationCode(ward.wardCode) === wardCode
      )?.name || wardCode;
    const fullAddress = [addressDetail, wardName, provinceName, 'Việt Nam']
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

      notification.success('Đã cập nhật tọa độ từ địa chỉ.', {
        description: `${geocodeResult.latitude.toFixed(6)}, ${geocodeResult.longitude.toFixed(6)}`,
      });
    } catch (error) {
      notification.error('Không thể lấy tọa độ từ địa chỉ.', {
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
        notification.error('Không thể tải chi tiết đơn hàng.', {
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
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể cập nhật đơn hàng first-mile.'
      );
      return;
    }

    if (!isDraftOrder(order)) {
      notification.error(
        'Chỉ các đơn hàng mới tạo và chưa xác nhận mới có thể chỉnh sửa.'
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
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể hủy đơn hàng first-mile.'
      );
      return;
    }

    if (!isDraftOrder(order)) {
      notification.error(
        'Chỉ các đơn hàng mới tạo và chưa xác nhận mới có thể hủy.'
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

      notification.success('Đã hủy đơn hàng thành công.');
      setCancelTarget(null);
      setCancelReason('');
      refreshOrdersAfterMutation();
    } catch (error) {
      notification.error('Không thể hủy đơn hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRequestDeleteOrder = (order: FirstMileOrderDetail) => {
    if (!canMutateOrders) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể xóa đơn hàng first-mile.'
      );
      return;
    }

    if (!isDraftOrder(order)) {
      notification.error(
        'Chỉ các đơn hàng mới tạo và chưa xác nhận mới có thể xóa.'
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
          cancel_reason:
            'Đã xóa bởi người dùng từ danh sách đơn hàng trên giao diện.',
        },
      }).unwrap();

      notification.success('Đã xóa đơn hàng thành công.', {
        description: 'Hiện backend ánh xạ thao tác xóa sang trạng thái hủy.',
      });
      setDeleteTarget(null);
      refreshOrdersAfterMutation();
    } catch (error) {
      notification.error('Không thể xóa đơn hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  const buildCreateOrderPayload = (): CreateOrderRequest | null => {
    const requiredFields: Array<{ value: string; label: string }> = [
      {
        value: createForm.customerOrderCode,
        label: 'Mã đơn của khách hàng',
      },
      { value: createForm.senderName, label: 'Tên người gửi' },
      { value: createForm.senderPhone, label: 'Số điện thoại người gửi' },
      {
        value: createForm.senderProvinceCode,
        label: 'Mã tỉnh người gửi',
      },
      { value: createForm.senderWardCode, label: 'Mã phường/xã người gửi' },
      {
        value: createForm.senderAddressDetail,
        label: 'Chi tiết địa chỉ người gửi',
      },
      { value: createForm.receiverName, label: 'Tên người nhận' },
      { value: createForm.receiverPhone, label: 'Số điện thoại người nhận' },
      {
        value: createForm.receiverProvinceCode,
        label: 'Mã tỉnh người nhận',
      },
      { value: createForm.receiverWardCode, label: 'Mã phường/xã người nhận' },
      {
        value: createForm.receiverAddressDetail,
        label: 'Chi tiết địa chỉ người nhận',
      },
    ];

    const missingField = requiredFields.find((field) => !field.value.trim());
    if (missingField) {
      notification.error(`${missingField.label} là bắt buộc.`);
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
      notification.error('Tọa độ người gửi và người nhận phải hợp lệ.');
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
        'Vĩ độ phải nằm trong [-90, 90] và kinh độ phải nằm trong [-180, 180].'
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
      notification.error('Giá trị kích thước và thể tích phải là số hợp lệ.');
      return null;
    }

    const products = (orderProducts ?? []).map((product) => ({
      ...product,
      name: product.name.trim(),
      value: Math.round(product.value),
      quantity: Math.trunc(product.quantity),
    }));

    const invalidProductIndex = products.findIndex(
      (product) =>
        !product.name ||
        product.product_type_id <= 0 ||
        !Number.isFinite(product.value) ||
        product.value < 0 ||
        !Number.isFinite(product.quantity) ||
        product.quantity < 1 ||
        !Number.isFinite(product.weight_gram) ||
        product.weight_gram <= 0
    );

    if (invalidProductIndex >= 0) {
      notification.error(
        `Sản phẩm #${invalidProductIndex + 1} chưa đầy đủ hoặc không hợp lệ.`
      );
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
      pickup_method: createForm.pickupMethod,
      order_type: createForm.orderType,
      order_product_category: createForm.orderProductCategory,
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
      products,
    };
  };

  const handleCreateOrder = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!canMutateOrders) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể tạo đơn hàng first-mile.'
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

        notification.success('Đã tạo đơn hàng thành công.', {
          description: `Mã đơn hàng: ${createdOrder.orderCode}`,
        });

        if (
          createdOrder.id &&
          createdOrder.pickupMethod === 'DROP_OFF_AT_POST_OFFICE'
        ) {
          setLoadingDropOffSuggestionOrderId(createdOrder.id);

          try {
            const suggestions = await loadDropOffPostOfficeSuggestions({
              orderId: createdOrder.id,
              limit: 5,
            }).unwrap();

            setDropOffSuggestionTarget(createdOrder);
            setDropOffSuggestions(suggestions);
          } catch {
            // Bỏ qua lỗi tải gợi ý ở đây vì đơn hàng đã được tạo thành công.
          } finally {
            setLoadingDropOffSuggestionOrderId(null);
          }
        }
      } else {
        if (editingOrderId === null) {
          notification.error('Thiếu mã đơn hàng để cập nhật.');
          return;
        }

        await updateOrder({
          id: editingOrderId,
          body: payload as UpdateOrderRequest,
        }).unwrap();

        notification.success('Đã cập nhật đơn hàng thành công.');
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
          ? 'Không thể tạo đơn hàng.'
          : 'Không thể cập nhật đơn hàng.',
        {
          description: getErrorMessage(error),
        }
      );
    }
  };

  const buildShippingFeeRequestFromOrder = React.useCallback(
    (order: FirstMileOrderDetail): CalculateShippingFeeRequest => {
      if (!order.senderWardCode || !order.receiverWardCode) {
        throw new Error(
          'Đơn hàng thiếu mã phường/xã của người gửi hoặc người nhận để tính phí.'
        );
      }

      const actualWeightGram = Math.max(1, Math.round(order.totalWeight ?? 0));
      const lengthCm = Math.max(1, Math.round(order.dimensionLengthCm ?? 0));
      const widthCm = Math.max(1, Math.round(order.dimensionWidthCm ?? 0));
      const heightCm = Math.max(1, Math.round(order.dimensionHeightCm ?? 0));

      return {
        serviceCode: 'TIEU_CHUAN',
        senderWardCode: order.senderWardCode,
        receiverWardCode: order.receiverWardCode,
        actualWeightGram,
        lengthCm,
        widthCm,
        heightCm,
        ...(order.orderProductCategory
          ? { productCategory: order.orderProductCategory }
          : {}),
      };
    },
    []
  );

  const calculateOrderShippingFee = React.useCallback(
    async (
      order: FirstMileOrderDetail
    ): Promise<CalculateShippingFeeResponse | null> => {
      try {
        const payload = buildShippingFeeRequestFromOrder(order);
        const feeResult = await calculateShippingFee(payload).unwrap();
        setShippingFeeQuote(feeResult);
        return feeResult;
      } catch (error) {
        setShippingFeeQuote(null);
        notification.error(
          'Không thể tính phí vận chuyển từ dịch vụ billing.',
          {
            description: getErrorMessage(error),
          }
        );
        return null;
      }
    },
    [buildShippingFeeRequestFromOrder, calculateShippingFee, notification]
  );

  const confirmOrderAndRefresh = React.useCallback(
    async (order: FirstMileOrderDetail): Promise<boolean> => {
      const confirmationResult = await confirmOrder(order.id).unwrap();
      const confirmationDescription =
        buildConfirmationPostOfficeDescription(confirmationResult);

      notification.success(
        confirmationResult.alreadyConfirmed
          ? 'Đơn hàng đã được xác nhận trước đó.'
          : 'Đã xác nhận đơn hàng thành công.',
        confirmationDescription
          ? { description: confirmationDescription }
          : undefined
      );

      setIsConfirmDialogOpen(false);
      setConfirmDialogOrder(null);
      setPaymentInitResult(null);
      setShippingFeeQuote(null);
      setIsAwaitingPaymentCompletion(false);
      setIsProcessingPaymentWebhook(false);
      paymentCompletionHandledRef.current = false;
      paymentResultHandlingInProgressRef.current = false;
      lastHandledPaymentMessageKeyRef.current = null;
      void refetch();
      return true;
    },
    [confirmOrder, notification, refetch]
  );

  const confirmOrderAndRefreshRef = React.useRef(confirmOrderAndRefresh);
  confirmOrderAndRefreshRef.current = confirmOrderAndRefresh;

  React.useEffect(() => {
    paymentCompletionHandledRef.current = false;
    paymentResultHandlingInProgressRef.current = false;
    lastHandledPaymentMessageKeyRef.current = null;
  }, [confirmDialogOrder?.id, paymentInitResult?.appTransId]);

  const handlePaymentResultMessage = React.useCallback(
    async (messageOrderId?: number, messageAppTransId?: string) => {
      if (
        paymentCompletionHandledRef.current ||
        paymentResultHandlingInProgressRef.current ||
        !confirmDialogOrder
      ) {
        return;
      }

      if (
        confirmDialogOrder.feePayer !== 'SENDER' ||
        confirmDialogOrder.paymentStatus === 'PAID'
      ) {
        return;
      }

      const expectedAppTransId = paymentInitResult?.appTransId?.trim();
      if (!expectedAppTransId) {
        return;
      }

      if (messageOrderId && messageOrderId !== confirmDialogOrder.id) {
        return;
      }

      if (messageAppTransId && messageAppTransId !== expectedAppTransId) {
        return;
      }

      const messageKey = `${confirmDialogOrder.id}:${expectedAppTransId}`;
      if (lastHandledPaymentMessageKeyRef.current === messageKey) {
        return;
      }
      lastHandledPaymentMessageKeyRef.current = messageKey;
      paymentResultHandlingInProgressRef.current = true;

      setIsAwaitingPaymentCompletion(true);
      setIsProcessingPaymentWebhook(false);

      try {
        let latestOrder = await loadOrderById(confirmDialogOrder.id).unwrap();
        if (latestOrder.paymentStatus !== 'PAID') {
          const paymentConfirmResult = await confirmOrderPayment({
            orderId: confirmDialogOrder.id,
            body: { appTransId: expectedAppTransId },
          }).unwrap();

          latestOrder = {
            ...latestOrder,
            paymentStatus: paymentConfirmResult.paymentStatus,
          };
        }

        if (latestOrder.paymentStatus !== 'PAID') {
          setIsAwaitingPaymentCompletion(false);
          setIsProcessingPaymentWebhook(true);
          setConfirmDialogOrder(latestOrder);
          notification.info('Đang hoàn tất thanh toán.', {
            description:
              'Thanh toán tại cổng đã hoàn tất. Đang chờ webhook xác nhận từ dịch vụ thanh toán.',
          });
          return;
        }

        paymentCompletionHandledRef.current = true;
        setConfirmDialogOrder(latestOrder);
        setIsAwaitingPaymentCompletion(false);
        setIsProcessingPaymentWebhook(false);

        notification.success('Đã xác nhận thanh toán phí vận chuyển.', {
          description: 'Đang xác nhận đơn hàng...',
        });

        await confirmOrderAndRefreshRef.current(latestOrder);
      } catch (error) {
        setIsAwaitingPaymentCompletion(false);
        setIsProcessingPaymentWebhook(false);
        lastHandledPaymentMessageKeyRef.current = null;
        notification.error('Không thể hoàn tất xác nhận thanh toán.', {
          description: getErrorMessage(error),
        });
      } finally {
        paymentResultHandlingInProgressRef.current = false;
      }
    },
    [
      confirmDialogOrder,
      paymentInitResult?.appTransId,
      loadOrderById,
      confirmOrderPayment,
      notification,
    ]
  );

  React.useEffect(() => {
    const onPaymentResultMessage = (event: MessageEvent<unknown>) => {
      if (event.origin !== window.location.origin) {
        return;
      }

      if (
        !event.data ||
        typeof event.data !== 'object' ||
        !('type' in event.data) ||
        (event.data as { type?: string }).type !== PAYMENT_RESULT_MESSAGE_TYPE
      ) {
        return;
      }

      const payload = (
        event.data as {
          payload?: {
            orderId?: number | string;
            appTransId?: string;
            query?: Record<string, string>;
          };
        }
      ).payload;

      const rawOrderId = payload?.orderId ?? payload?.query?.orderId;
      const parsedOrderId = Number.parseInt(String(rawOrderId ?? ''), 10);
      const messageOrderId =
        Number.isInteger(parsedOrderId) && parsedOrderId > 0
          ? parsedOrderId
          : undefined;

      const messageAppTransId = (
        payload?.appTransId ??
        payload?.query?.appTransId ??
        payload?.query?.apptransid
      )?.trim();

      void handlePaymentResultMessage(messageOrderId, messageAppTransId);
    };

    window.addEventListener('message', onPaymentResultMessage);
    return () => {
      window.removeEventListener('message', onPaymentResultMessage);
    };
  }, [handlePaymentResultMessage]);

  const handleOpenConfirmOrder = async (order: FirstMileOrderDetail) => {
    if (!canMutateOrders) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể xác nhận đơn hàng first-mile.'
      );
      return;
    }

    if (isDropOffOrder(order)) {
      notification.error(
        'Đơn gửi tại bưu cục phải dùng luồng gợi ý bưu cục và xác nhận của quản lý bưu cục.'
      );
      return;
    }

    setConfirmingOrderId(order.id);

    try {
      const orderDetail = await loadOrderDetail(order.id);
      if (!orderDetail) {
        return;
      }

      setConfirmDialogOrder(orderDetail);
      setPaymentInitResult(null);
      setShippingFeeQuote(null);
      setIsAwaitingPaymentCompletion(false);
      setIsProcessingPaymentWebhook(false);
      setIsConfirmDialogOpen(true);
      await calculateOrderShippingFee(orderDetail);
    } finally {
      setConfirmingOrderId(null);
    }
  };

  const handleConfirmOrderFromDialog = async () => {
    if (!confirmDialogOrder) {
      return;
    }

    if (
      confirmDialogOrder.feePayer === 'SENDER' &&
      confirmDialogOrder.paymentStatus !== 'PAID'
    ) {
      notification.error(
        'Người gửi phải thanh toán trước khi xác nhận đơn hàng.'
      );
      return;
    }

    try {
      await confirmOrderAndRefresh(confirmDialogOrder);
    } catch (error) {
      notification.error('Không thể xác nhận đơn hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRecalculateConfirmDialogFee = async () => {
    if (!confirmDialogOrder) {
      return;
    }
    await calculateOrderShippingFee(confirmDialogOrder);
  };

  const handleInitiateConfirmDialogPayment = async () => {
    if (!confirmDialogOrder) {
      return;
    }

    if (confirmDialogOrder.feePayer !== 'SENDER') {
      notification.error(
        'Chỉ cần thanh toán khi người gửi trả phí vận chuyển.'
      );
      return;
    }

    if (!shippingFeeQuote || !Number.isFinite(shippingFeeQuote.totalFee)) {
      notification.error(
        'Cần có báo giá phí vận chuyển trước khi khởi tạo thanh toán.'
      );
      return;
    }

    try {
      const paymentResult = await initiateOrderPayment({
        orderId: confirmDialogOrder.id,
        body: {
          amount: Math.max(1, Math.round(shippingFeeQuote.totalFee)),
        },
      }).unwrap();

      setPaymentInitResult(paymentResult);
      setIsAwaitingPaymentCompletion(true);
      setIsProcessingPaymentWebhook(false);
      paymentCompletionHandledRef.current = false;
      paymentResultHandlingInProgressRef.current = false;
      lastHandledPaymentMessageKeyRef.current = null;

      if (paymentResult.paymentUrl) {
        const paymentPopup = window.open(
          paymentResult.paymentUrl,
          'serp-payment-window',
          'width=520,height=760,scrollbars=yes,resizable=yes'
        );

        if (!paymentPopup) {
          setIsAwaitingPaymentCompletion(false);
          setIsProcessingPaymentWebhook(false);
          notification.error('Trình duyệt đã chặn cửa sổ bật lên.', {
            description:
              'Hãy cho phép popup trên trang này để hoàn tất xác nhận thanh toán tự động.',
          });
          return;
        }
      }

      notification.success('Đã tạo yêu cầu thanh toán.', {
        description:
          'Hoàn tất thanh toán trong cửa sổ vừa mở. Hộp thoại này sẽ tự động xác nhận đơn hàng khi thanh toán thành công.',
      });
    } catch (error) {
      notification.error('Không thể khởi tạo thanh toán.', {
        description: getErrorMessage(error),
      });
    }
  };

  const parsePositiveIntegerInput = (value: string): number | null => {
    const trimmedValue = value.trim();
    if (!trimmedValue) {
      return null;
    }

    const parsedValue = Number(trimmedValue);
    if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
      return null;
    }

    return parsedValue;
  };

  const handleOpenDropOffSuggestions = async (order: FirstMileOrderDetail) => {
    if (!canMutateOrders) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể xem gợi ý bưu cục gửi hàng.'
      );
      return;
    }

    if (!isDropOffOrder(order)) {
      notification.error(
        'Đơn hàng này không được cấu hình để gửi tại bưu cục.'
      );
      return;
    }

    setLoadingDropOffSuggestionOrderId(order.id);

    try {
      const suggestions = await loadDropOffPostOfficeSuggestions({
        orderId: order.id,
        limit: 5,
      }).unwrap();

      setDropOffSuggestionTarget(order);
      setDropOffSuggestions(suggestions);
    } catch (error) {
      notification.error('Không thể tải gợi ý bưu cục gửi hàng.', {
        description: getErrorMessage(error),
      });
    } finally {
      setLoadingDropOffSuggestionOrderId(null);
    }
  };

  const handleRefreshDropOffSuggestions = async () => {
    if (!dropOffSuggestionTarget) {
      return;
    }

    setLoadingDropOffSuggestionOrderId(dropOffSuggestionTarget.id);

    try {
      const suggestions = await loadDropOffPostOfficeSuggestions({
        orderId: dropOffSuggestionTarget.id,
        limit: 5,
      }).unwrap();

      setDropOffSuggestions(suggestions);
    } catch (error) {
      notification.error('Không thể làm mới gợi ý bưu cục.', {
        description: getErrorMessage(error),
      });
    } finally {
      setLoadingDropOffSuggestionOrderId(null);
    }
  };

  const handleOpenManagerDropOffConfirm = (order: FirstMileOrderDetail) => {
    if (!canConfirmDropOffAtPostOffice) {
      notification.error(
        'Chỉ TMS_POSTOFFICER_MANAGER mới có thể xác nhận đơn gửi tại bưu cục.'
      );
      return;
    }

    setManagerDropOffOrderIdInput(String(order.id));

    if (order.originPostOfficeCode) {
      const matchedPostOffice = managerPostOfficeOptions.find(
        (postOffice) => postOffice.code === order.originPostOfficeCode
      );

      if (matchedPostOffice?.id) {
        setManagerDropOffPostOfficeIdInput(String(matchedPostOffice.id));
      }
    }
  };

  const handleManagerConfirmDropOffOrder = async () => {
    if (!canConfirmDropOffAtPostOffice) {
      notification.error(
        'Chỉ TMS_POSTOFFICER_MANAGER mới có thể xác nhận đơn gửi tại bưu cục.'
      );
      return;
    }

    const orderId = parsePositiveIntegerInput(managerDropOffOrderIdInput);
    if (orderId === null) {
      notification.error('Mã đơn hàng phải là số nguyên dương.');
      return;
    }

    const postOfficeId = parsePositiveIntegerInput(
      managerDropOffPostOfficeIdInput
    );
    if (postOfficeId === null) {
      notification.error('Vui lòng chọn một bưu cục hợp lệ.');
      return;
    }

    try {
      const confirmationResult = await confirmDropOffOrderAtPostOffice({
        orderId,
        postOfficeId,
      }).unwrap();

      const confirmationDescription =
        buildConfirmationPostOfficeDescription(confirmationResult);
      notification.success(
        'Đã xác nhận đơn gửi tại bưu cục.',
        confirmationDescription
          ? { description: confirmationDescription }
          : undefined
      );

      setManagerDropOffOrderIdInput('');
      setDropOffSuggestionTarget(null);
      setDropOffSuggestions([]);
      void refetch();
    } catch (error) {
      notification.error('Không thể xác nhận đơn gửi tại bưu cục.', {
        description: getErrorMessage(error),
      });
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
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể tải mẫu đơn hàng.'
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

      notification.success('Đã tải mẫu đơn hàng thành công.');
    } catch (error) {
      notification.error('Không thể tải mẫu đơn hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleValidateImportFile = async () => {
    if (!canMutateOrders) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể kiểm tra file nhập đơn hàng.'
      );
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Vui lòng chọn tệp Excel trước.');
      return;
    }

    try {
      const result = await validateOrderImport(formData).unwrap();
      setValidateImportResult(result);

      if (result.is_success) {
        notification.success('Đã kiểm tra tệp thành công.', {
          description: `${result.data.length} đơn hàng sẵn sàng nhập.`,
        });
      }
    } catch (error) {
      notification.error('Không thể kiểm tra file nhập đơn hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleImportFile = async () => {
    if (!canMutateOrders) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_CUSTOMER mới có thể nhập đơn hàng.'
      );
      return;
    }

    if (!validateImportResult) {
      notification.error('Vui lòng kiểm tra tệp đã chọn trước khi nhập.');
      return;
    }

    if (!validateImportResult.is_success) {
      return;
    }

    const formData = buildImportFormData();
    if (!formData) {
      notification.error('Vui lòng chọn tệp Excel trước.');
      return;
    }

    try {
      const importResult = await importOrderFile(formData).unwrap();
      setLastImportJob(importResult);
      resetImportFileSelection();

      notification.success('Đã tạo tác vụ nhập đơn hàng.', {
        description: `Tác vụ #${importResult.id} hiện ở trạng thái ${importResult.status}.`,
      });

      void refetch();
    } catch (error) {
      notification.error('Không thể nhập file đơn hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <OrderPageHeader
        canMutateOrders={canMutateOrders}
        onCreateOrder={handleOpenCreateDialog}
        importAction={
          canMutateOrders ? (
            <OrderImportCard
              canMutateOrders={canMutateOrders}
              isImportFlowBusy={isImportFlowBusy}
              isExportingTemplate={isExportingTemplate}
              isValidatingImport={isValidatingImport}
              isImportingOrders={isImportingOrders}
              importFileInputKey={importFileInputKey}
              selectedImportFile={selectedImportFile}
              validateImportResult={validateImportResult}
              lastImportJob={lastImportJob}
              onDownloadTemplate={() => {
                void handleDownloadTemplate();
              }}
              onSelectImportFile={handleSelectImportFile}
              onValidateImportFile={() => {
                void handleValidateImportFile();
              }}
              onImportFile={() => {
                void handleImportFile();
              }}
            />
          ) : null
        }
      />

      <OrderStatsCards
        canViewOrders={canViewOrders}
        data={data}
        isFetching={isFetching}
      />

      {canConfirmDropOffAtPostOffice ? (
        <OrderDropOffManagerConfirmCard
          orderIdInput={managerDropOffOrderIdInput}
          postOfficeIdInput={managerDropOffPostOfficeIdInput}
          postOffices={managerPostOfficeOptions}
          isLoadingPostOffices={isFetchingManagerPostOffices}
          isSubmitting={isConfirmingDropOff}
          onOrderIdInputChange={setManagerDropOffOrderIdInput}
          onPostOfficeIdInputChange={setManagerDropOffPostOfficeIdInput}
          onSubmit={() => {
            void handleManagerConfirmDropOffOrder();
          }}
        />
      ) : null}

      <OrderResultsCard
        canViewOrders={canViewOrders}
        canMutateOrders={canMutateOrders}
        canConfirmDropOffAtPostOffice={canConfirmDropOffAtPostOffice}
        data={data}
        isLoading={isLoading}
        isFetching={isFetching}
        filterFormValues={filterFormValues}
        statusOptions={ORDER_STATUS_OPTIONS}
        postOffices={managerPostOfficeOptions}
        isLoadingPostOffices={isFetchingManagerPostOffices}
        loadingOrderActionId={loadingOrderActionId}
        confirmingOrderId={confirmingOrderId}
        loadingDropOffSuggestionOrderId={loadingDropOffSuggestionOrderId}
        onFilterFieldChange={updateFilterField}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onRefresh={() => {
          void refetch();
        }}
        onViewDetail={(orderId) => {
          void handleOpenOrderDetail(orderId);
        }}
        onEdit={(order) => {
          void handleOpenEditOrder(order);
        }}
        onRequestCancel={handleRequestCancelOrder}
        onRequestDelete={handleRequestDeleteOrder}
        onConfirm={(order) => {
          void handleOpenConfirmOrder(order);
        }}
        onOpenDropOffSuggestions={(order) => {
          void handleOpenDropOffSuggestions(order);
        }}
        onOpenManagerDropOffConfirm={handleOpenManagerDropOffConfirm}
        onPreviousPage={() => setPage((prev) => Math.max(prev - 1, 0))}
        onNextPage={() => setPage((prev) => prev + 1)}
        formatStatusLabel={formatStatusLabel}
        formatPickupMethodLabel={formatPickupMethodLabel}
        getStatusBadgeVariant={getStatusBadgeVariant}
        isDraftOrder={isDraftOrder}
        isConfirmableStatus={isConfirmableStatus}
        isDropOffOrder={isDropOffOrder}
        buildOrderAddressLabel={buildOrderAddressLabel}
        buildPostOfficeAssignmentLabel={buildPostOfficeAssignmentLabel}
        formatDateTime={formatDateTime}
      />

      <OrderFormDialog
        open={isCreateDialogOpen}
        orderFormMode={orderFormMode}
        isSubmittingOrder={isSubmittingOrder}
        createForm={createForm}
        selectedSenderProvinceCode={selectedSenderProvinceCode}
        selectedSenderWardCode={selectedSenderWardCode}
        selectedReceiverProvinceCode={selectedReceiverProvinceCode}
        selectedReceiverWardCode={selectedReceiverWardCode}
        provinceSelectOptions={provinceSelectOptions}
        senderWardSelectOptions={senderWardSelectOptions}
        receiverWardSelectOptions={receiverWardSelectOptions}
        productTypeOptions={productTypeOptions}
        orderProducts={orderProducts ?? []}
        isFetchingSenderWards={isFetchingSenderWards}
        isFetchingReceiverWards={isFetchingReceiverWards}
        isFetchingProductTypes={isFetchingProductTypes}
        geocodingTarget={geocodingTarget}
        onOpenChange={setIsCreateDialogOpen}
        onSubmit={handleCreateOrder}
        onFormChange={updateCreateFormField}
        onProductsChange={setOrderProducts}
        onGeocodeFromAddress={(target) => {
          void handleGeocodeFromAddress(target);
        }}
        onMapCoordinateChange={handleMapCoordinateChange}
        normalizeLocationCode={normalizeLocationCode}
        parseOptionalNumberInput={parseOptionalNumberInput}
      />

      <OrderDetailDialog
        open={isDetailDialogOpen}
        detailOrder={detailOrder}
        onOpenChange={(open) => {
          setIsDetailDialogOpen(open);
          if (!open) {
            setDetailOrder(null);
          }
        }}
        formatStatusLabel={formatStatusLabel}
        formatPickupMethodLabel={formatPickupMethodLabel}
        buildOrderAddressLabel={buildOrderAddressLabel}
        getProvinceLabel={getProvinceLabel}
        getWardLabel={getWardLabel}
        formatDateTime={formatDateTime}
      />

      <OrderConfirmDialog
        open={isConfirmDialogOpen}
        order={confirmDialogOrder}
        shippingFee={shippingFeeQuote}
        paymentInitResult={paymentInitResult}
        isCalculatingFee={isCalculatingShippingFee}
        isConfirmingOrder={isConfirmingOrder}
        isInitiatingPayment={isInitiatingOrderPayment}
        isAwaitingPaymentCompletion={isAwaitingPaymentCompletion}
        isProcessingPaymentWebhook={isProcessingPaymentWebhook}
        onOpenChange={(open) => {
          setIsConfirmDialogOpen(open);
          if (!open) {
            setConfirmDialogOrder(null);
            setShippingFeeQuote(null);
            setPaymentInitResult(null);
            setIsAwaitingPaymentCompletion(false);
            setIsProcessingPaymentWebhook(false);
            paymentCompletionHandledRef.current = false;
            paymentResultHandlingInProgressRef.current = false;
            lastHandledPaymentMessageKeyRef.current = null;
          }
        }}
        onRecalculateFee={() => {
          void handleRecalculateConfirmDialogFee();
        }}
        onConfirmOrder={() => {
          void handleConfirmOrderFromDialog();
        }}
        onInitiatePayment={() => {
          void handleInitiateConfirmDialogPayment();
        }}
      />

      <OrderDropOffSuggestionsDialog
        open={Boolean(dropOffSuggestionTarget)}
        order={dropOffSuggestionTarget}
        suggestions={dropOffSuggestions}
        isLoading={isFetchingDropOffSuggestions}
        onOpenChange={(open) => {
          if (!open) {
            setDropOffSuggestionTarget(null);
            setDropOffSuggestions([]);
          }
        }}
        onRefresh={() => {
          void handleRefreshDropOffSuggestions();
        }}
      />

      <OrderCancelDialog
        cancelTarget={cancelTarget}
        cancelReason={cancelReason}
        isCancellingOrder={isCancellingOrder}
        onOpenChange={(open) => {
          if (!open) {
            setCancelTarget(null);
            setCancelReason('');
          }
        }}
        onCancelReasonChange={setCancelReason}
        onKeepOrder={() => {
          setCancelTarget(null);
          setCancelReason('');
        }}
        onConfirmCancel={() => {
          void handleCancelOrder();
        }}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null);
          }
        }}
        title='Xóa đơn hàng'
        description={
          deleteTarget
            ? `Xóa đơn hàng ${deleteTarget.orderCode}? Thao tác này sẽ được ánh xạ sang trạng thái hủy vì backend chưa cung cấp API xóa vĩnh viễn.`
            : 'Xóa đơn hàng này?'
        }
        confirmText='Xóa đơn hàng'
        cancelText='Giữ lại đơn'
        onConfirm={() => {
          void handleDeleteOrder();
        }}
        onCancel={() => setDeleteTarget(null)}
        isLoading={isCancellingOrder}
        variant='destructive'
      />
    </div>
  );
};
