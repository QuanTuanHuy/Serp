/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bagging page
 */

'use client';

import React from 'react';
import {
  Boxes,
  CheckCircle2,
  Clock3,
  Loader2,
  PackageCheck,
  Plus,
  RefreshCcw,
  ShieldAlert,
  SlidersHorizontal,
  WandSparkles,
} from 'lucide-react';

import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Card,
  CardContent,
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
  Textarea,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';

import {
  useAddSecondMileBagOrderMutation,
  useAutoPlanSecondMileBagsMutation,
  useCreateSecondMileBagMutation,
  useDeleteSecondMileBagMutation,
  useGetHubPostOfficesQuery,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useGetSecondMileBagByIdQuery,
  useGetSecondMileBagCapacitySettingsQuery,
  useGetSecondMileBaggingKpisQuery,
  useGetSecondMileBagsQuery,
  useGetSecondMileOrdersQuery,
  useGetSecondMileVehiclesQuery,
  useRemoveSecondMileBagOrderMutation,
  useReopenSecondMileBagMutation,
  useSealSecondMileBagMutation,
  useUpdateSecondMileBagCapacitySettingsMutation,
  useUpdateSecondMileBagMutation,
  useValidateSecondMileBaggingMutation,
} from '../../api/firstMileApi';
import { TmsCombobox } from '../../components/TmsCombobox';
import type {
  AutoSecondMileBaggingPlanRequest,
  AutoSecondMileBaggingPlan,
  HubPostOfficeMapping,
  PostOffice,
  SecondMileBag,
  SecondMileBagDestinationType,
  SecondMileBagListFilters,
  SecondMileBagStatus,
  SecondMileBaggingValidation,
  SecondMileOrder,
  UpdateSecondMileBagRequest,
} from '../../types';
import {
  AutoBaggingDialog,
  BagDetailDialog,
  BagFormDialog,
  BagKpiPanel,
  BagResultsTable,
  BagScanOrdersDialog,
} from './components';
import {
  BAG_DESTINATION_TYPE_OPTIONS,
  BAG_STATUS_OPTIONS,
  buildBagRequest,
  canManageBags,
  canOperateBagOrders,
  canViewBags,
  emptyAutoBaggingFormValues,
  emptyBagFormValues,
  formatNumber,
  normalizeOrderCode,
  toDefaultedBagFormValues,
  toBagFormValues,
  toLocalDateTimeInputValue,
  type AutoBaggingFormValues,
  type BagFormValues,
  validateAutoBaggingForm,
  validateBagForm,
} from './bagPageModels';

const PAGE_SIZE = 20;
const ALL_VALUE = '__ALL__';

type BagCapacitySettingsFormValues = {
  maxWeight: string;
  maxVolume: string;
  maxOrders: string;
};

export function BagListPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );

  const canView = canViewBags(roles);
  const canManage = canManageBags(roles);
  const canOperate = canOperateBagOrders(roles);

  const [page, setPage] = React.useState(0);
  const [filters, setFilters] = React.useState<SecondMileBagListFilters>({});
  const [kpiFrom, setKpiFrom] = React.useState(() => {
    const date = new Date();
    date.setHours(0, 0, 0, 0);
    return toLocalDateTimeInputValue(date);
  });
  const [kpiTo, setKpiTo] = React.useState(() => {
    const date = new Date();
    date.setHours(23, 59, 0, 0);
    return toLocalDateTimeInputValue(date);
  });

  const [formOpen, setFormOpen] = React.useState(false);
  const [formMode, setFormMode] = React.useState<'create' | 'edit'>('create');
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [formValues, setFormValues] =
    React.useState<BagFormValues>(emptyBagFormValues);
  const [settingsOpen, setSettingsOpen] = React.useState(false);
  const [settingsValues, setSettingsValues] =
    React.useState<BagCapacitySettingsFormValues>({
      maxWeight: '',
      maxVolume: '',
      maxOrders: '',
    });

  const [selectedDetailId, setSelectedDetailId] = React.useState<number | null>(
    null
  );
  const [scanBag, setScanBag] = React.useState<SecondMileBag | null>(null);
  const [scanOrderCodes, setScanOrderCodes] = React.useState<string[]>([]);
  const [validationResult, setValidationResult] =
    React.useState<SecondMileBaggingValidation | null>(null);

  const [autoOpen, setAutoOpen] = React.useState(false);
  const [autoValues, setAutoValues] = React.useState<AutoBaggingFormValues>(
    emptyAutoBaggingFormValues
  );
  const [autoPlan, setAutoPlan] =
    React.useState<AutoSecondMileBaggingPlan | null>(null);
  const formOriginHubId = Number(formValues.originHubId);
  const autoOriginHubId = Number(autoValues.originHubId);
  const autoDestinationHubId = Number(autoValues.destinationHubId);
  const scanOriginHubId = scanBag?.originHubId ?? 0;
  const scanDestinationHubId = scanBag?.destinationHubId ?? 0;
  const shouldLoadFormDestinationMappings =
    formOpen &&
    formValues.destinationType === 'POST_OFFICE' &&
    Number.isFinite(formOriginHubId) &&
    formOriginHubId > 0;
  const shouldLoadAutoOriginMappings =
    autoOpen && Number.isFinite(autoOriginHubId) && autoOriginHubId > 0;
  const shouldLoadAutoDestinationMappings =
    autoOpen &&
    autoValues.destinationType === 'HUB' &&
    Number.isFinite(autoDestinationHubId) &&
    autoDestinationHubId > 0;
  const shouldLoadScanOriginMappings =
    Boolean(scanBag) && Number.isFinite(scanOriginHubId) && scanOriginHubId > 0;
  const shouldLoadScanDestinationMappings =
    Boolean(scanBag) &&
    scanBag?.destinationType === 'HUB' &&
    Number.isFinite(scanDestinationHubId) &&
    scanDestinationHubId > 0;

  const [deleteTarget, setDeleteTarget] = React.useState<SecondMileBag | null>(
    null
  );
  const [sealTarget, setSealTarget] = React.useState<SecondMileBag | null>(
    null
  );
  const [reopenTarget, setReopenTarget] = React.useState<SecondMileBag | null>(
    null
  );
  const [reopenReason, setReopenReason] = React.useState('');

  const { data: hubsData } = useGetHubsQuery(
    { page: 0, size: 500, status: 'ACTIVE' },
    { skip: !canView }
  );
  const { data: postOfficesData, isFetching: isFetchingPostOffices } =
    useGetPostOfficesQuery(
      { page: 0, size: 500, status: 'ACTIVE' },
      { skip: !canView }
    );
  const {
    data: formDestinationPostOfficesData,
    isFetching: isFetchingFormDestinationPostOffices,
  } = useGetHubPostOfficesQuery(
    { hubId: formOriginHubId, page: 0, size: 500 },
    { skip: !canView || !shouldLoadFormDestinationMappings }
  );
  const {
    data: autoOriginPostOfficesData,
    isFetching: isFetchingAutoOriginPostOffices,
  } = useGetHubPostOfficesQuery(
    { hubId: autoOriginHubId, page: 0, size: 500 },
    { skip: !canView || !shouldLoadAutoOriginMappings }
  );
  const {
    data: autoDestinationPostOfficesData,
    isFetching: isFetchingAutoDestinationPostOffices,
  } = useGetHubPostOfficesQuery(
    { hubId: autoDestinationHubId, page: 0, size: 500 },
    { skip: !canView || !shouldLoadAutoDestinationMappings }
  );
  const {
    data: scanOriginPostOfficesData,
    isFetching: isFetchingScanOriginPostOffices,
  } = useGetHubPostOfficesQuery(
    { hubId: scanOriginHubId, page: 0, size: 500 },
    { skip: !canView || !shouldLoadScanOriginMappings }
  );
  const {
    data: scanDestinationPostOfficesData,
    isFetching: isFetchingScanDestinationPostOffices,
  } = useGetHubPostOfficesQuery(
    { hubId: scanDestinationHubId, page: 0, size: 500 },
    { skip: !canView || !shouldLoadScanDestinationMappings }
  );
  const { data: ordersData, isFetching: isFetchingOrders } =
    useGetSecondMileOrdersQuery(
      {
        page: 0,
        size: 500,
        statuses: ['INBOUND_AT_ORIGIN_HUB', 'BAGGING_IN_PROGRESS'],
      },
      { skip: !canView || (!autoOpen && !scanBag) }
    );
  const { data: vehiclesData } = useGetSecondMileVehiclesQuery(
    { page: 0, size: 500, status: 'ACTIVE' },
    { skip: !canView }
  );
  const {
    data: bagCapacitySettings,
    isFetching: isFetchingBagCapacitySettings,
    refetch: refetchBagCapacitySettings,
  } = useGetSecondMileBagCapacitySettingsQuery(undefined, {
    skip: !canView,
  });
  const {
    data: bagsData,
    isFetching: isFetchingBags,
    refetch,
  } = useGetSecondMileBagsQuery(
    {
      page,
      size: PAGE_SIZE,
      ...filters,
    },
    { skip: !canView }
  );
  const {
    data: detailBag,
    isFetching: isFetchingDetail,
    refetch: refetchDetail,
  } = useGetSecondMileBagByIdQuery(selectedDetailId ?? 0, {
    skip: selectedDetailId === null || !canView,
  });
  const { data: kpiData, isFetching: isFetchingKpi } =
    useGetSecondMileBaggingKpisQuery(
      {
        originHubId: filters.originHubId ?? 0,
        from: kpiFrom,
        to: kpiTo,
      },
      {
        skip: !canView || !filters.originHubId || !kpiFrom || !kpiTo,
      }
    );
  const bagPageItems = bagsData?.items ?? [];
  const bagStats = [
    {
      label: 'Tổng kết quả',
      value: formatNumber(bagsData?.totalItems, 0),
      icon: Boxes,
    },
    {
      label: 'Trang hiện tại',
      value: formatNumber(bagPageItems.length, 0),
      icon: Clock3,
    },
    {
      label: 'Túi đang mở',
      value: formatNumber(
        bagPageItems.filter((bag) => bag.status === 'CREATED').length,
        0
      ),
      icon: PackageCheck,
    },
    {
      label: 'Túi đã niêm phong',
      value: formatNumber(
        bagPageItems.filter((bag) => bag.status === 'SEALED').length,
        0
      ),
      icon: CheckCircle2,
    },
  ];

  const [createBag, { isLoading: isCreating }] =
    useCreateSecondMileBagMutation();
  const [updateBag, { isLoading: isUpdating }] =
    useUpdateSecondMileBagMutation();
  const [deleteBag, { isLoading: isDeleting }] =
    useDeleteSecondMileBagMutation();
  const [addBagOrder, { isLoading: isAddingOrder }] =
    useAddSecondMileBagOrderMutation();
  const [removeBagOrder, { isLoading: isRemovingOrder }] =
    useRemoveSecondMileBagOrderMutation();
  const [sealBag, { isLoading: isSealing }] = useSealSecondMileBagMutation();
  const [reopenBag, { isLoading: isReopening }] =
    useReopenSecondMileBagMutation();
  const [
    updateBagCapacitySettings,
    { isLoading: isUpdatingBagCapacitySettings },
  ] = useUpdateSecondMileBagCapacitySettingsMutation();
  const [validateBagging, { isLoading: isValidating }] =
    useValidateSecondMileBaggingMutation();
  const [autoPlanBags, { isLoading: isAutoPlanning }] =
    useAutoPlanSecondMileBagsMutation();

  const hubs = hubsData?.items ?? [];
  const postOffices = postOfficesData?.items ?? [];
  const orders = ordersData?.items ?? [];
  const postOfficeByCode = React.useMemo(
    () =>
      postOffices.reduce<Record<string, PostOffice>>((acc, postOffice) => {
        acc[normalizeCode(postOffice.code)] = postOffice;
        return acc;
      }, {}),
    [postOffices]
  );
  const formDestinationPostOfficeOptions = React.useMemo(
    () =>
      toPostOfficeComboboxOptions(
        formDestinationPostOfficesData?.items ?? [],
        postOfficeByCode
      ),
    [formDestinationPostOfficesData?.items, postOfficeByCode]
  );
  const autoDestinationPostOfficeOptions = React.useMemo(
    () =>
      toPostOfficeComboboxOptions(
        autoOriginPostOfficesData?.items ?? [],
        postOfficeByCode
      ),
    [autoOriginPostOfficesData?.items, postOfficeByCode]
  );
  const autoOriginPostOfficeCodes = React.useMemo(
    () => toPostOfficeCodeSet(autoOriginPostOfficesData?.items ?? []),
    [autoOriginPostOfficesData?.items]
  );
  const autoDestinationPostOfficeCodes = React.useMemo(
    () => toPostOfficeCodeSet(autoDestinationPostOfficesData?.items ?? []),
    [autoDestinationPostOfficesData?.items]
  );
  const scanOriginPostOfficeCodes = React.useMemo(
    () => toPostOfficeCodeSet(scanOriginPostOfficesData?.items ?? []),
    [scanOriginPostOfficesData?.items]
  );
  const scanDestinationPostOfficeCodes = React.useMemo(
    () => toPostOfficeCodeSet(scanDestinationPostOfficesData?.items ?? []),
    [scanDestinationPostOfficesData?.items]
  );
  const autoCandidateOrders = React.useMemo(
    () =>
      filterCandidateOrdersByTarget(
        orders,
        autoOriginPostOfficeCodes,
        autoValues.destinationType,
        autoDestinationPostOfficeCodes,
        autoValues.destinationPostOfficeCode
      ),
    [
      autoDestinationPostOfficeCodes,
      autoOriginPostOfficeCodes,
      autoValues.destinationPostOfficeCode,
      autoValues.destinationType,
      orders,
    ]
  );
  const scanCandidateOrders = React.useMemo(
    () =>
      filterCandidateOrdersByTarget(
        orders,
        scanOriginPostOfficeCodes,
        scanBag?.destinationType,
        scanDestinationPostOfficeCodes,
        scanBag?.destinationPostOfficeCode
      ),
    [
      orders,
      scanBag?.destinationPostOfficeCode,
      scanBag?.destinationType,
      scanDestinationPostOfficeCodes,
      scanOriginPostOfficeCodes,
    ]
  );
  const vehicles = vehiclesData?.items ?? [];
  const isSavingForm = isCreating || isUpdating;

  React.useEffect(() => {
    if (!autoOpen) {
      return;
    }

    const candidateOrderCodes = new Set(
      autoCandidateOrders
        .map((order) => order.orderCode)
        .filter((orderCode): orderCode is string => Boolean(orderCode))
    );
    const nextOrderCodes = autoValues.orderCodes.filter((orderCode) =>
      candidateOrderCodes.has(orderCode)
    );

    if (nextOrderCodes.length !== autoValues.orderCodes.length) {
      setAutoValues((current) => ({
        ...current,
        orderCodes: current.orderCodes.filter((orderCode) =>
          candidateOrderCodes.has(orderCode)
        ),
      }));
      setAutoPlan(null);
    }
  }, [autoCandidateOrders, autoOpen, autoValues.orderCodes]);

  React.useEffect(() => {
    if (!scanBag) {
      return;
    }

    const candidateOrderCodes = new Set(
      scanCandidateOrders
        .map((order) => order.orderCode)
        .filter((orderCode): orderCode is string => Boolean(orderCode))
    );
    const nextOrderCodes = scanOrderCodes.filter((orderCode) =>
      candidateOrderCodes.has(orderCode)
    );

    if (nextOrderCodes.length !== scanOrderCodes.length) {
      setScanOrderCodes(nextOrderCodes);
      setValidationResult(null);
    }
  }, [scanBag, scanCandidateOrders, scanOrderCodes]);

  const hubOptions = [
    { value: ALL_VALUE, label: 'Tất cả hub' },
    ...hubs.map((hub) => ({
      value: String(hub.id),
      label: `${hub.code} - ${hub.name}`,
    })),
  ];
  const vehicleOptions = [
    { value: ALL_VALUE, label: 'Tất cả xe' },
    ...vehicles.map((vehicle) => ({
      value: String(vehicle.id),
      label: vehicle.licensePlate,
    })),
  ];
  const postOfficeFilterOptions = [
    { value: ALL_VALUE, label: 'Tất cả bưu cục' },
    ...postOffices.map((postOffice) => ({
      value: postOffice.code,
      label: `${postOffice.code} - ${postOffice.name}`,
    })),
  ];
  const destinationTypeOptions = [
    { value: ALL_VALUE, label: 'Tất cả điểm đến' },
    ...BAG_DESTINATION_TYPE_OPTIONS,
  ];
  const statusOptions = [
    { value: ALL_VALUE, label: 'Tất cả trạng thái' },
    ...BAG_STATUS_OPTIONS,
  ];
  const updateFilter = <K extends keyof SecondMileBagListFilters>(
    field: K,
    value: SecondMileBagListFilters[K] | undefined
  ) => {
    setFilters((current) => ({ ...current, [field]: value }));
    setPage(0);
  };

  const resetFilters = () => {
    setFilters({});
    setPage(0);
  };

  const handleCreate = () => {
    if (!canManage) {
      notification.error('Cần quyền quản lý hub để tạo túi.');
      return;
    }
    setFormMode('create');
    setEditingId(null);
    setFormValues(toDefaultedBagFormValues(bagCapacitySettings));
    setFormOpen(true);
  };

  const openSettingsDialog = () => {
    if (!canManage) {
      notification.error('Cần quyền quản lý hub để cấu hình túi.');
      return;
    }

    setSettingsValues({
      maxWeight: bagCapacitySettings?.maxWeight
        ? String(bagCapacitySettings.maxWeight)
        : '',
      maxVolume: bagCapacitySettings?.maxVolume
        ? String(bagCapacitySettings.maxVolume)
        : '',
      maxOrders: bagCapacitySettings?.maxOrders
        ? String(bagCapacitySettings.maxOrders)
        : '',
    });
    setSettingsOpen(true);
  };

  const handleEdit = (bag: SecondMileBag) => {
    if (!canManage || bag.status !== 'CREATED') {
      notification.error('Chỉ có thể sửa túi đang mở.');
      return;
    }
    setFormMode('edit');
    setEditingId(bag.id);
    setFormValues(toBagFormValues(bag));
    setFormOpen(true);
  };

  const handleSubmitForm = async (event: React.FormEvent) => {
    event.preventDefault();
    const error = validateBagForm(formValues);
    if (error) {
      notification.error(error);
      return;
    }

    const request = buildBagRequest(formValues);
    try {
      if (formMode === 'create') {
        await createBag(request).unwrap();
        notification.success('Đã tạo túi thành công.');
      } else if (editingId !== null) {
        await updateBag({
          id: editingId,
          body: { ...request, status: 'CREATED' } as UpdateSecondMileBagRequest,
        }).unwrap();
        notification.success('Đã cập nhật túi thành công.');
      }
      setFormOpen(false);
      setEditingId(null);
      void refetch();
    } catch (err) {
      notification.error('Không thể lưu túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleSubmitSettings = async (event: React.FormEvent) => {
    event.preventDefault();
    const maxWeight = Number(settingsValues.maxWeight);
    const maxVolume = Number(settingsValues.maxVolume);
    const maxOrders = Number(settingsValues.maxOrders);

    if (!Number.isFinite(maxWeight) || maxWeight <= 0) {
      notification.error('Khối lượng tối đa phải lớn hơn 0.');
      return;
    }
    if (!Number.isFinite(maxVolume) || maxVolume <= 0) {
      notification.error('Thể tích tối đa phải lớn hơn 0.');
      return;
    }
    if (!Number.isInteger(maxOrders) || maxOrders <= 0) {
      notification.error('Số đơn tối đa phải là số nguyên dương.');
      return;
    }

    try {
      await updateBagCapacitySettings({
        max_weight: maxWeight,
        max_volume: maxVolume,
        max_orders: maxOrders,
      }).unwrap();
      notification.success('Đã cập nhật mặc định túi.');
      setSettingsOpen(false);
      void refetchBagCapacitySettings();
      void refetch();
    } catch (err) {
      notification.error('Không thể cập nhật mặc định túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleValidateScans = async () => {
    if (!scanBag) {
      return;
    }
    const orderCodes = Array.from(
      new Set(
        scanOrderCodes.map(normalizeOrderCode).filter((code) => code.length > 0)
      )
    );
    if (orderCodes.length === 0) {
      notification.error('Vui lòng chọn ít nhất một đơn hàng.');
      return;
    }

    try {
      const result = await validateBagging({
        bag_id: scanBag.id,
        order_codes: orderCodes,
      }).unwrap();
      setValidationResult(result);
      notification.success(
        `Đã kiểm tra: ${result.acceptedCount} đơn hợp lệ và ${result.rejectedCount} đơn bị từ chối.`
      );
    } catch (err) {
      notification.error('Không thể kiểm tra đóng túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleAddAcceptedOrders = async () => {
    if (!scanBag || !validationResult) {
      return;
    }
    const acceptedCodes = validationResult.items
      .filter((item) => item.accepted)
      .map((item) => item.orderCode);
    if (acceptedCodes.length === 0) {
      return;
    }

    try {
      for (const orderCode of acceptedCodes) {
        await addBagOrder({
          id: scanBag.id,
          body: { order_code: orderCode },
        }).unwrap();
      }
      notification.success('Đã thêm đơn hàng vào túi.');
      setScanOrderCodes([]);
      setValidationResult(null);
      setScanBag(null);
      void refetch();
      if (selectedDetailId === scanBag.id) {
        void refetchDetail();
      }
    } catch (err) {
      notification.error('Không thể thêm đơn hàng vào túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleAutoPreview = async (event: React.FormEvent) => {
    event.preventDefault();
    await runAutoPlan(false);
  };

  const handleAutoExecute = async () => {
    await runAutoPlan(true);
  };

  const runAutoPlan = async (execute: boolean) => {
    const error = validateAutoBaggingForm(autoValues);
    if (error) {
      notification.error(error);
      return;
    }

    const request = buildAutoPlanRequest(autoValues, execute);
    try {
      const result = await autoPlanBags(request).unwrap();
      setAutoPlan(result);
      notification.success(
        execute
          ? 'Đã thực hiện đóng túi tự động.'
          : 'Đã tạo phương án đóng túi.'
      );
      if (execute) {
        setAutoOpen(false);
        setAutoValues({ ...emptyAutoBaggingFormValues });
        void refetch();
      }
    } catch (err) {
      notification.error('Không thể lập túi tự động.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleRemoveOrder = async (orderCode: string) => {
    if (!detailBag) {
      return;
    }
    try {
      await removeBagOrder({ id: detailBag.id, orderCode }).unwrap();
      notification.success('Đã xóa đơn hàng khỏi túi.');
      void refetch();
      void refetchDetail();
    } catch (err) {
      notification.error('Không thể xóa đơn hàng khỏi túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) {
      return;
    }
    try {
      await deleteBag(deleteTarget.id).unwrap();
      notification.success('Đã xóa túi thành công.');
      setDeleteTarget(null);
      void refetch();
    } catch (err) {
      notification.error('Không thể xóa túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleSeal = async () => {
    if (!sealTarget) {
      return;
    }
    try {
      await sealBag(sealTarget.id).unwrap();
      notification.success('Đã niêm phong túi thành công.');
      setSealTarget(null);
      void refetch();
    } catch (err) {
      notification.error('Không thể niêm phong túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleReopen = async () => {
    if (!reopenTarget) {
      return;
    }
    if (!reopenReason.trim()) {
      notification.error('Vui lòng nhập lý do mở lại túi.');
      return;
    }
    try {
      await reopenBag({
        id: reopenTarget.id,
        body: { reason: reopenReason.trim() },
      }).unwrap();
      notification.success('Đã mở lại túi thành công.');
      setReopenTarget(null);
      setReopenReason('');
      void refetch();
    } catch (err) {
      notification.error('Không thể mở lại túi.', {
        description: getErrorMessage(err),
      });
    }
  };

  if (!canView) {
    return (
      <Alert>
        <ShieldAlert className='h-4 w-4' />
        <AlertTitle>Không có quyền truy cập</AlertTitle>
        <AlertDescription>
          Chức năng túi hàng yêu cầu quyền vận hành hub.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-2xl font-semibold tracking-tight'>Túi hàng</h1>
          <p className='text-sm text-muted-foreground'>
            Tạo túi, quét đơn đã về hub, niêm phong túi và theo dõi tỷ lệ lấp
            đầy.
          </p>
        </div>
        <div className='flex flex-wrap gap-2'>
          {canManage && (
            <Button
              variant='outline'
              onClick={() => {
                setAutoValues({ ...emptyAutoBaggingFormValues });
                setAutoPlan(null);
                setAutoOpen(true);
              }}
            >
              <WandSparkles className='h-4 w-4' />
              Tự động lập túi
            </Button>
          )}
          {canManage && (
            <Button
              variant='outline'
              disabled={isFetchingBagCapacitySettings}
              onClick={openSettingsDialog}
            >
              <SlidersHorizontal className='h-4 w-4' />
              Mặc định túi
            </Button>
          )}
          <Button variant='outline' onClick={() => void refetch()}>
            <RefreshCcw className='h-4 w-4' />
            Làm mới
          </Button>
          {canManage && (
            <Button onClick={handleCreate}>
              <Plus className='h-4 w-4' />
              Tạo túi
            </Button>
          )}
        </div>
      </div>

      {/*
        <Card className='gap-3 py-5'>
        <CardHeader className='px-5 py-0'>
          <div className='flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
            <CardTitle className='text-base'>Tìm kiếm và bộ lọc</CardTitle>
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={() => setAdvancedFiltersOpen(true)}
              className='w-full justify-center sm:w-auto'
            >
              <SlidersHorizontal className='h-4 w-4' />
              Nâng cao
              {advancedFilterCount > 0 ? (
                <Badge variant='secondary' className='ml-1.5 h-5 px-1.5'>
                  {advancedFilterCount}
                </Badge>
              ) : null}
            </Button>
          </div>
        </CardHeader>
        <CardContent className='grid gap-3 px-5 py-0 md:grid-cols-[minmax(260px,1fr)_220px_220px_auto]'>
          <div className='space-y-2'>
            <Label htmlFor='bag-keyword'>Từ khóa</Label>
            <div className='relative'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                id='bag-keyword'
                className='pl-10'
                value={filters.keyword ?? ''}
                onChange={(event) =>
                  updateFilter('keyword', event.target.value || undefined)
                }
                placeholder='Tìm túi hàng'
              />
            </div>
          </div>
          <div className='space-y-2'>
            <Label htmlFor='bag-origin-filter'>Hub gốc</Label>
            <TmsCombobox
              id='bag-origin-filter'
              value={
                filters.originHubId ? String(filters.originHubId) : ALL_VALUE
              }
              onValueChange={(value) =>
                updateFilter(
                  'originHubId',
                  value === ALL_VALUE ? undefined : Number(value)
                )
              }
              options={hubOptions}
              placeholder='Tất cả hub'
              emptyText='Không tìm thấy hub'
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='bag-status-filter'>Trạng thái</Label>
            <TmsCombobox
              id='bag-status-filter'
              value={filters.status ?? ALL_VALUE}
              onValueChange={(value) =>
                updateFilter(
                  'status',
                  value === ALL_VALUE
                    ? undefined
                    : (value as SecondMileBagStatus)
                )
              }
              options={statusOptions}
              placeholder='Tất cả trạng thái'
              emptyText='Không tìm thấy trạng thái'
            />
          </div>
          <div className='flex items-end'>
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={resetFilters}
              className='w-full md:w-auto'
            >
              <FilterX className='h-4 w-4' />
              Xóa
            </Button>
          </div>
        </CardContent>
      </Card>

      <Dialog open={advancedFiltersOpen} onOpenChange={setAdvancedFiltersOpen}>
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
          <DialogHeader>
            <DialogTitle>Bộ lọc nâng cao</DialogTitle>
            <DialogDescription>
              Lọc túi theo điểm đến, xe, mã túi chính xác và khung thời gian KPI.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-3 sm:grid-cols-2 lg:grid-cols-3'>
            <div className='space-y-2'>
              <Label htmlFor='bag-code-filter'>Mã túi</Label>
              <Input
                id='bag-code-filter'
                value={filters.bagCode ?? ''}
                onChange={(event) =>
                  updateFilter('bagCode', event.target.value || undefined)
                }
                placeholder='BAG-001'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='bag-destination-type-filter'>
                Loại điểm đến
              </Label>
              <TmsCombobox
                id='bag-destination-type-filter'
                value={filters.destinationType ?? ALL_VALUE}
                onValueChange={(value) => {
                  updateFilter(
                    'destinationType',
                    value === ALL_VALUE
                      ? undefined
                      : (value as SecondMileBagDestinationType)
                  );
                  updateFilter('destinationHubId', undefined);
                  updateFilter('destinationPostOfficeCode', undefined);
                }}
                options={destinationTypeOptions}
                placeholder='Tất cả điểm đến'
                emptyText='Không tìm thấy loại điểm đến'
              />
            </div>
            {filters.destinationType === 'HUB' && (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-hub-filter'>
                  Hub đích
                </Label>
                <TmsCombobox
                  id='bag-destination-hub-filter'
                  value={
                    filters.destinationHubId
                      ? String(filters.destinationHubId)
                      : ALL_VALUE
                  }
                  onValueChange={(value) =>
                    updateFilter(
                      'destinationHubId',
                      value === ALL_VALUE ? undefined : Number(value)
                    )
                  }
                  options={hubOptions}
                  placeholder='Tất cả hub đích'
                  emptyText='Không tìm thấy hub'
                />
              </div>
            )}
            {filters.destinationType === 'POST_OFFICE' && (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-post-office-filter'>
                  Bưu cục đích
                </Label>
                <TmsCombobox
                  id='bag-destination-post-office-filter'
                  value={filters.destinationPostOfficeCode ?? ALL_VALUE}
                  onValueChange={(value) =>
                    updateFilter(
                      'destinationPostOfficeCode',
                      value === ALL_VALUE ? undefined : value
                    )
                  }
                  options={postOfficeFilterOptions}
                  placeholder='Tất cả bưu cục'
                  emptyText='Không tìm thấy bưu cục'
                  loading={isFetchingPostOffices}
                />
              </div>
            )}
            <div className='space-y-2'>
              <Label htmlFor='bag-vehicle-filter'>Xe</Label>
              <TmsCombobox
                id='bag-vehicle-filter'
                value={
                  filters.vehicleId ? String(filters.vehicleId) : ALL_VALUE
                }
                onValueChange={(value) =>
                  updateFilter(
                    'vehicleId',
                    value === ALL_VALUE ? undefined : Number(value)
                  )
                }
                options={vehicleOptions}
                placeholder='Tất cả xe'
                emptyText='Không tìm thấy xe'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='bag-kpi-from'>KPI từ</Label>
              <Input
                id='bag-kpi-from'
                type='datetime-local'
                value={kpiFrom}
                onChange={(event) => setKpiFrom(event.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='bag-kpi-to'>KPI đến</Label>
              <Input
                id='bag-kpi-to'
                type='datetime-local'
                value={kpiTo}
                onChange={(event) => setKpiTo(event.target.value)}
              />
            </div>
          </div>

          <DialogFooter className='gap-2 sm:gap-0'>
            <Button type='button' variant='outline' onClick={resetFilters}>
              Xóa tất cả
            </Button>
            <Button type='button' onClick={() => setAdvancedFiltersOpen(false)}>
              Xong
            </Button>
          </DialogFooter>
        </DialogContent>
        </Dialog>
      */}

      <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-4'>
        {bagStats.map((stat) => {
          const Icon = stat.icon;

          return (
            <Card key={stat.label} className='gap-2 py-3'>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 px-4 py-0'>
                <CardTitle className='text-sm font-medium'>
                  {stat.label}
                </CardTitle>
                <Icon className='h-4 w-4 text-muted-foreground' />
              </CardHeader>
              <CardContent className='px-4 py-0'>
                <div className='text-xl font-semibold'>
                  {isFetchingBags ? '-' : stat.value}
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {filters.originHubId && (
        <BagKpiPanel data={kpiData} isFetching={isFetchingKpi} />
      )}

      <BagResultsTable
        data={bagsData}
        hubs={hubs}
        vehicles={vehicles}
        filters={filters}
        hubOptions={hubOptions}
        vehicleOptions={vehicleOptions}
        destinationTypeOptions={destinationTypeOptions}
        postOfficeOptions={postOfficeFilterOptions}
        statusOptions={statusOptions}
        isLoadingPostOffices={isFetchingPostOffices}
        isFetching={isFetchingBags}
        canManage={canManage}
        canOperate={canOperate}
        page={page}
        onPageChange={setPage}
        onFilterChange={updateFilter}
        onClearFilters={resetFilters}
        onView={(bag) => setSelectedDetailId(bag.id)}
        onEdit={handleEdit}
        onDelete={setDeleteTarget}
        onScan={(bag) => {
          setScanBag(bag);
          setScanOrderCodes([]);
          setValidationResult(null);
        }}
        onSeal={setSealTarget}
        onReopen={(bag) => {
          setReopenTarget(bag);
          setReopenReason('');
        }}
      />

      <BagFormDialog
        open={formOpen}
        mode={formMode}
        values={formValues}
        hubs={hubs}
        destinationPostOfficeOptions={formDestinationPostOfficeOptions}
        isLoadingDestinationPostOffices={isFetchingFormDestinationPostOffices}
        isSaving={isSavingForm}
        onOpenChange={setFormOpen}
        onSubmit={handleSubmitForm}
        onUpdateField={(field, value) =>
          setFormValues((current) => ({ ...current, [field]: value }))
        }
      />

      <BagDetailDialog
        open={selectedDetailId !== null}
        bag={detailBag}
        hubs={hubs}
        vehicles={vehicles}
        isLoading={isFetchingDetail}
        canRemoveOrders={canOperate}
        isRemovingOrder={isRemovingOrder}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedDetailId(null);
          }
        }}
        onRemoveOrder={handleRemoveOrder}
      />

      <BagScanOrdersDialog
        open={Boolean(scanBag)}
        bag={scanBag ?? undefined}
        orders={scanCandidateOrders}
        selectedOrderCodes={scanOrderCodes}
        validation={validationResult}
        isValidating={isValidating}
        isAdding={isAddingOrder}
        isOrdersLoading={
          isFetchingOrders ||
          isFetchingScanOriginPostOffices ||
          isFetchingScanDestinationPostOffices
        }
        onOpenChange={(open) => {
          if (!open) {
            setScanBag(null);
            setScanOrderCodes([]);
            setValidationResult(null);
          }
        }}
        onOrderCodesChange={(value) => {
          setScanOrderCodes(value);
          setValidationResult(null);
        }}
        onValidate={handleValidateScans}
        onAddAccepted={handleAddAcceptedOrders}
      />

      <AutoBaggingDialog
        open={autoOpen}
        values={autoValues}
        hubs={hubs}
        orders={autoCandidateOrders}
        destinationPostOfficeOptions={autoDestinationPostOfficeOptions}
        plan={autoPlan}
        isPlanning={isAutoPlanning}
        isExecuting={isAutoPlanning}
        isOrdersLoading={
          isFetchingOrders ||
          isFetchingAutoOriginPostOffices ||
          isFetchingAutoDestinationPostOffices
        }
        isLoadingDestinationPostOffices={isFetchingAutoOriginPostOffices}
        onOpenChange={(open) => {
          setAutoOpen(open);
          if (!open) {
            setAutoPlan(null);
          }
        }}
        onSubmitPreview={handleAutoPreview}
        onExecute={handleAutoExecute}
        onUpdateField={(field, value) => {
          setAutoValues((current) => ({ ...current, [field]: value }));
          setAutoPlan(null);
        }}
      />

      <Dialog open={settingsOpen} onOpenChange={setSettingsOpen}>
        <DialogContent className='sm:max-w-lg'>
          <DialogHeader>
            <DialogTitle>Mặc định túi</DialogTitle>
            <DialogDescription>
              Thiết lập sức chứa mặc định dùng khi tạo túi mới và lập túi tự
              động.
            </DialogDescription>
          </DialogHeader>
          <form className='space-y-4' onSubmit={handleSubmitSettings}>
            <div className='grid gap-3 sm:grid-cols-3'>
              <div className='space-y-2'>
                <Label htmlFor='bag-default-max-weight'>
                  Khối lượng tối đa (kg)
                </Label>
                <Input
                  id='bag-default-max-weight'
                  type='number'
                  min='0'
                  step='0.01'
                  value={settingsValues.maxWeight}
                  onChange={(event) =>
                    setSettingsValues((current) => ({
                      ...current,
                      maxWeight: event.target.value,
                    }))
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-default-max-volume'>
                  Thể tích tối đa (m3)
                </Label>
                <Input
                  id='bag-default-max-volume'
                  type='number'
                  min='0'
                  step='0.001'
                  value={settingsValues.maxVolume}
                  onChange={(event) =>
                    setSettingsValues((current) => ({
                      ...current,
                      maxVolume: event.target.value,
                    }))
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-default-max-orders'>Số đơn tối đa</Label>
                <Input
                  id='bag-default-max-orders'
                  type='number'
                  min='1'
                  step='1'
                  value={settingsValues.maxOrders}
                  onChange={(event) =>
                    setSettingsValues((current) => ({
                      ...current,
                      maxOrders: event.target.value,
                    }))
                  }
                />
              </div>
            </div>
            <DialogFooter>
              <Button
                type='button'
                variant='outline'
                disabled={isUpdatingBagCapacitySettings}
                onClick={() => setSettingsOpen(false)}
              >
                Hủy
              </Button>
              <Button type='submit' disabled={isUpdatingBagCapacitySettings}>
                {isUpdatingBagCapacitySettings && (
                  <Loader2 className='h-4 w-4 animate-spin' />
                )}
                Lưu mặc định
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null);
          }
        }}
        title='Xóa túi'
        description={`Xóa ${deleteTarget?.bagCode ?? 'túi này'}?`}
        confirmText='Xóa'
        variant='destructive'
        isLoading={isDeleting}
        onConfirm={handleDelete}
      />

      <ConfirmDialog
        open={Boolean(sealTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setSealTarget(null);
          }
        }}
        title='Niêm phong túi'
        description={`Niêm phong ${sealTarget?.bagCode ?? 'túi này'}?`}
        confirmText='Niêm phong'
        isLoading={isSealing}
        onConfirm={handleSeal}
      />

      <Dialog
        open={Boolean(reopenTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setReopenTarget(null);
            setReopenReason('');
          }
        }}
      >
        <DialogContent className='sm:max-w-lg'>
          <DialogHeader>
            <DialogTitle>Mở lại túi</DialogTitle>
            <DialogDescription>
              Mở lại {reopenTarget?.bagCode ?? 'túi này'} để điều chỉnh đơn
              hàng.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-2'>
            <Label htmlFor='reopen-reason'>Lý do *</Label>
            <Textarea
              id='reopen-reason'
              value={reopenReason}
              onChange={(event) => setReopenReason(event.target.value)}
              rows={4}
              placeholder='Nhập lý do mở lại'
            />
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              disabled={isReopening}
              onClick={() => {
                setReopenTarget(null);
                setReopenReason('');
              }}
            >
              Hủy
            </Button>
            <Button disabled={isReopening} onClick={handleReopen}>
              {isReopening && <Loader2 className='h-4 w-4 animate-spin' />}
              Mở lại
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

const normalizeCode = (value?: string) => value?.trim().toUpperCase() ?? '';

const toPostOfficeCodeSet = (mappings: HubPostOfficeMapping[]) =>
  new Set(
    mappings
      .map((mapping) => normalizeCode(mapping.postOfficeCode))
      .filter(Boolean)
  );

const toPostOfficeComboboxOptions = (
  mappings: HubPostOfficeMapping[],
  postOfficeByCode: Record<string, PostOffice>
) =>
  mappings
    .map((mapping) => normalizeCode(mapping.postOfficeCode))
    .filter(
      (code, index, codes) => Boolean(code) && codes.indexOf(code) === index
    )
    .map((code) => {
      const postOffice = postOfficeByCode[code];
      return {
        value: code,
        label: postOffice ? `${postOffice.code} - ${postOffice.name}` : code,
      };
    });

const filterCandidateOrdersByTarget = (
  orders: SecondMileOrder[],
  originPostOfficeCodes: Set<string>,
  destinationType?: SecondMileBagDestinationType,
  destinationHubPostOfficeCodes: Set<string> = new Set<string>(),
  destinationPostOfficeCode?: string
) => {
  const selectedDestinationPostOfficeCode = normalizeCode(
    destinationPostOfficeCode
  );

  return orders.filter((order) => {
    const originPostOfficeCode = normalizeCode(order.originPostOfficeCode);
    const orderDestinationPostOfficeCode = normalizeCode(
      order.destinationPostOfficeCode
    );

    if (
      !originPostOfficeCode ||
      !originPostOfficeCodes.has(originPostOfficeCode)
    ) {
      return false;
    }

    if (destinationType === 'POST_OFFICE') {
      return (
        Boolean(selectedDestinationPostOfficeCode) &&
        orderDestinationPostOfficeCode === selectedDestinationPostOfficeCode
      );
    }

    if (destinationType === 'HUB') {
      return (
        Boolean(orderDestinationPostOfficeCode) &&
        destinationHubPostOfficeCodes.has(orderDestinationPostOfficeCode)
      );
    }

    return false;
  });
};

const buildAutoPlanRequest = (
  values: AutoBaggingFormValues,
  execute: boolean
): AutoSecondMileBaggingPlanRequest => {
  const request: AutoSecondMileBaggingPlanRequest = {
    origin_hub_id: Number(values.originHubId),
    destination_type: values.destinationType,
    order_codes: values.orderCodes
      .map(normalizeOrderCode)
      .filter((code) => code.length > 0),
    execute,
  };

  if (values.destinationType === 'HUB') {
    request.destination_hub_id = Number(values.destinationHubId);
  } else {
    request.destination_post_office_code = values.destinationPostOfficeCode
      .trim()
      .toUpperCase();
  }

  return request;
};
