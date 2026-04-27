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
  useConfirmOrderMutation,
  useCreateOrderMutation,
  useGeocodeAddressMutation,
  useImportOrdersMutation,
  useLazyExportOrderTemplateQuery,
  useLazyGetDropOffPostOfficeSuggestionsQuery,
  useLazyGetOrderByIdQuery,
  useGetOrdersQuery,
  useGetPostOfficesQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useUpdateOrderMutation,
  useValidateOrderImportMutation,
} from '../../api';
import type {
  CancelOrderRequest,
  CreateOrderRequest,
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  ImportHistory,
  OrderDropOffPostOfficeSuggestion,
  OrderImportItem,
  PostOffice,
  Province,
  UpdateOrderRequest,
  ValidateImportFileResponse,
} from '../../types';
import {
  OrderAccessScopeCard,
  OrderCancelDialog,
  OrderDetailDialog,
  OrderDropOffManagerConfirmCard,
  OrderDropOffSuggestionsDialog,
  OrderFiltersCard,
  OrderFormDialog,
  OrderImportCard,
  OrderPageHeader,
  OrderResultsCard,
} from './components';
import {
  buildOrderAddressLabel,
  buildPostOfficeAssignmentLabel,
  buildWardSelectOptions,
  DEFAULT_CREATE_ORDER_FORM,
  formatDateTime,
  formatPickupMethodLabel,
  formatOrderImportProductsPreview,
  formatStatusLabel,
  getProvinceNameByCode,
  getScopeBadgeLabel,
  getScopeDescription,
  getStatusBadgeVariant,
  IMPORT_PREVIEW_LIMIT,
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
  type OrderStatusFilter,
} from './orderPageModels';

export const OrderListPage: React.FC = () => {
  const dispatch = useAppDispatch();
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
      keyword,
      status,
    },
    {
      skip: !canViewOrders,
    }
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
      skip: !canConfirmDropOffAtPostOffice,
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
  const [confirmOrder] = useConfirmOrderMutation();
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
      pickup_method: createForm.pickupMethod,
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
            // Ignore suggestion loading failures here because order creation already succeeded.
          } finally {
            setLoadingDropOffSuggestionOrderId(null);
          }
        }
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

    if (isDropOffOrder(order)) {
      notification.error(
        'Drop-off orders must use post office suggestion flow and manager confirmation at post office.'
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
        'Only TMS_ADMIN or TMS_CUSTOMER can view drop-off post office suggestions.'
      );
      return;
    }

    if (!isDropOffOrder(order)) {
      notification.error(
        'This order is not configured for post office drop-off.'
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
      notification.error('Failed to load drop-off post office suggestions.', {
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
      notification.error('Failed to refresh post office suggestions.', {
        description: getErrorMessage(error),
      });
    } finally {
      setLoadingDropOffSuggestionOrderId(null);
    }
  };

  const handleOpenManagerDropOffConfirm = (order: FirstMileOrderDetail) => {
    if (!canConfirmDropOffAtPostOffice) {
      notification.error(
        'Only TMS_POSTOFFICER_MANAGER can confirm drop-off orders.'
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
        'Only TMS_POSTOFFICER_MANAGER can confirm drop-off orders.'
      );
      return;
    }

    const orderId = parsePositiveIntegerInput(managerDropOffOrderIdInput);
    if (orderId === null) {
      notification.error('Order ID must be a positive integer.');
      return;
    }

    const postOfficeId = parsePositiveIntegerInput(
      managerDropOffPostOfficeIdInput
    );
    if (postOfficeId === null) {
      notification.error('Please select a valid post office.');
      return;
    }

    try {
      const confirmationResult = await confirmDropOffOrderAtPostOffice({
        orderId,
        postOfficeId,
      }).unwrap();

      const originPostOffice = confirmationResult.originPostOffice;
      notification.success(
        'Drop-off order confirmed at post office.',
        originPostOffice
          ? {
              description: `Origin post office: ${originPostOffice.code} - ${originPostOffice.name}`,
            }
          : undefined
      );

      setManagerDropOffOrderIdInput('');
      setDropOffSuggestionTarget(null);
      setDropOffSuggestions([]);
      void refetch();
    } catch (error) {
      notification.error('Failed to confirm drop-off order at post office.', {
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
      <OrderPageHeader
        canMutateOrders={canMutateOrders}
        onCreateOrder={handleOpenCreateDialog}
      />

      <OrderAccessScopeCard
        canViewOrders={canViewOrders}
        badgeLabel={getScopeBadgeLabel(accessScope)}
        description={getScopeDescription(accessScope)}
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

      <OrderFiltersCard
        canViewOrders={canViewOrders}
        keywordInput={keywordInput}
        statusInput={statusInput}
        statusOptions={ORDER_STATUS_OPTIONS}
        isFetching={isFetching}
        onKeywordInputChange={setKeywordInput}
        onStatusInputChange={setStatusInput}
        onApplyFilters={handleApplyFilters}
        onRefresh={() => {
          void refetch();
        }}
        formatStatusLabel={formatStatusLabel}
      />

      <OrderImportCard
        canMutateOrders={canMutateOrders}
        isImportFlowBusy={isImportFlowBusy}
        isExportingTemplate={isExportingTemplate}
        isValidatingImport={isValidatingImport}
        isImportingOrders={isImportingOrders}
        importFileInputKey={importFileInputKey}
        selectedImportFile={selectedImportFile}
        validateImportResult={validateImportResult}
        validatedPreviewItems={validatedPreviewItems}
        importPreviewLimit={IMPORT_PREVIEW_LIMIT}
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
        formatProductsPreview={formatOrderImportProductsPreview}
      />

      <OrderResultsCard
        canViewOrders={canViewOrders}
        canMutateOrders={canMutateOrders}
        canConfirmDropOffAtPostOffice={canConfirmDropOffAtPostOffice}
        data={data}
        isLoading={isLoading}
        isFetching={isFetching}
        loadingOrderActionId={loadingOrderActionId}
        confirmingOrderId={confirmingOrderId}
        loadingDropOffSuggestionOrderId={loadingDropOffSuggestionOrderId}
        onViewDetail={(orderId) => {
          void handleOpenOrderDetail(orderId);
        }}
        onEdit={(order) => {
          void handleOpenEditOrder(order);
        }}
        onRequestCancel={handleRequestCancelOrder}
        onRequestDelete={handleRequestDeleteOrder}
        onConfirm={(order) => {
          void handleConfirmOrder(order);
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
        isFetchingSenderWards={isFetchingSenderWards}
        isFetchingReceiverWards={isFetchingReceiverWards}
        geocodingTarget={geocodingTarget}
        onOpenChange={setIsCreateDialogOpen}
        onSubmit={handleCreateOrder}
        onFormChange={updateCreateFormField}
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
        title='Delete Order'
        description={
          deleteTarget
            ? `Delete order ${deleteTarget.orderCode}? This action will be mapped to cancel status because backend does not expose hard-delete API.`
            : 'Delete this order?'
        }
        confirmText='Delete order'
        cancelText='Keep order'
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
