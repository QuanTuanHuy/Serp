/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bagging page
 */

'use client';

import React from 'react';
import {
  FilterX,
  Loader2,
  PackageOpen,
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
  useGetSecondMileRoutesQuery,
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
  const formDestinationHubId = Number(formValues.destinationHubId);
  const autoOriginHubId = Number(autoValues.originHubId);
  const autoDestinationHubId = Number(autoValues.destinationHubId);
  const scanOriginHubId = scanBag?.originHubId ?? 0;
  const scanDestinationHubId = scanBag?.destinationHubId ?? 0;
  const shouldLoadFormDestinationMappings =
    formOpen &&
    formValues.destinationType === 'POST_OFFICE' &&
    Number.isFinite(formOriginHubId) &&
    formOriginHubId > 0;
  const shouldLoadFormRoutes =
    formOpen &&
    Number.isFinite(formOriginHubId) &&
    formOriginHubId > 0 &&
    (formValues.destinationType === 'HUB'
      ? Number.isFinite(formDestinationHubId) && formDestinationHubId > 0
      : normalizeCode(formValues.destinationPostOfficeCode).length > 0);
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
  const { data: formRoutesData, isFetching: isFetchingFormRoutes } =
    useGetSecondMileRoutesQuery(
      {
        page: 0,
        size: 500,
        originType: 'HUB',
        originHubId: formOriginHubId,
        destinationType: formValues.destinationType,
        ...(formValues.destinationType === 'HUB'
          ? { destinationHubId: formDestinationHubId }
          : {
              destinationPostOfficeCode: normalizeCode(
                formValues.destinationPostOfficeCode
              ),
            }),
        status: 'ACTIVE',
      },
      { skip: !canView || !shouldLoadFormRoutes }
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
  const formRoutes = formRoutesData?.items ?? [];
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
    if (!shouldLoadFormRoutes || isFetchingFormRoutes || !formValues.routeId) {
      return;
    }

    const selectedRoute = formRoutes.find(
      (route) => String(route.id) === formValues.routeId
    );

    if (!selectedRoute) {
      setFormValues((current) => ({ ...current, routeId: '', vehicleId: '' }));
      return;
    }

    const nextVehicleId = selectedRoute.vehicleId
      ? String(selectedRoute.vehicleId)
      : '';
    if (formValues.vehicleId !== nextVehicleId) {
      setFormValues((current) => ({ ...current, vehicleId: nextVehicleId }));
    }
  }, [
    formRoutes,
    formValues.routeId,
    formValues.vehicleId,
    isFetchingFormRoutes,
    shouldLoadFormRoutes,
  ]);

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
    { value: ALL_VALUE, label: 'All hubs' },
    ...hubs.map((hub) => ({
      value: String(hub.id),
      label: `${hub.code} - ${hub.name}`,
    })),
  ];
  const vehicleOptions = [
    { value: ALL_VALUE, label: 'All vehicles' },
    ...vehicles.map((vehicle) => ({
      value: String(vehicle.id),
      label: vehicle.licensePlate,
    })),
  ];
  const postOfficeFilterOptions = [
    { value: ALL_VALUE, label: 'All post offices' },
    ...postOffices.map((postOffice) => ({
      value: postOffice.code,
      label: `${postOffice.code} - ${postOffice.name}`,
    })),
  ];
  const destinationTypeOptions = [
    { value: ALL_VALUE, label: 'All destinations' },
    ...BAG_DESTINATION_TYPE_OPTIONS,
  ];
  const statusOptions = [
    { value: ALL_VALUE, label: 'All statuses' },
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
      notification.error('Bag creation requires hub manager access.');
      return;
    }
    setFormMode('create');
    setEditingId(null);
    setFormValues(toDefaultedBagFormValues(bagCapacitySettings));
    setFormOpen(true);
  };

  const openSettingsDialog = () => {
    if (!canManage) {
      notification.error('Bag settings require hub manager access.');
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
      notification.error('Only open bags can be edited.');
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
        notification.success('Bag created successfully.');
      } else if (editingId !== null) {
        await updateBag({
          id: editingId,
          body: { ...request, status: 'CREATED' } as UpdateSecondMileBagRequest,
        }).unwrap();
        notification.success('Bag updated successfully.');
      }
      setFormOpen(false);
      setEditingId(null);
      void refetch();
    } catch (err) {
      notification.error('Failed to save bag.', {
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
      notification.error('Max weight must be greater than 0.');
      return;
    }
    if (!Number.isFinite(maxVolume) || maxVolume <= 0) {
      notification.error('Max volume must be greater than 0.');
      return;
    }
    if (!Number.isInteger(maxOrders) || maxOrders <= 0) {
      notification.error('Max orders must be a positive whole number.');
      return;
    }

    try {
      await updateBagCapacitySettings({
        max_weight: maxWeight,
        max_volume: maxVolume,
        max_orders: maxOrders,
      }).unwrap();
      notification.success('Bag defaults updated successfully.');
      setSettingsOpen(false);
      void refetchBagCapacitySettings();
      void refetch();
    } catch (err) {
      notification.error('Failed to update bag defaults.', {
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
      notification.error('Select at least one order.');
      return;
    }

    try {
      const result = await validateBagging({
        bag_id: scanBag.id,
        order_codes: orderCodes,
      }).unwrap();
      setValidationResult(result);
      notification.success(
        `Validated ${result.acceptedCount} accepted and ${result.rejectedCount} rejected order(s).`
      );
    } catch (err) {
      notification.error('Failed to validate bagging.', {
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
      notification.success('Order added to bag.');
      setScanOrderCodes([]);
      setValidationResult(null);
      setScanBag(null);
      void refetch();
      if (selectedDetailId === scanBag.id) {
        void refetchDetail();
      }
    } catch (err) {
      notification.error('Failed to add order to bag.', {
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
          ? 'Auto bagging executed successfully.'
          : 'Auto bagging plan created.'
      );
      if (execute) {
        setAutoOpen(false);
        setAutoValues({ ...emptyAutoBaggingFormValues });
        void refetch();
      }
    } catch (err) {
      notification.error('Failed to auto plan bags.', {
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
      notification.success('Order removed from bag.');
      void refetch();
      void refetchDetail();
    } catch (err) {
      notification.error('Failed to remove order from bag.', {
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
      notification.success('Bag deleted successfully.');
      setDeleteTarget(null);
      void refetch();
    } catch (err) {
      notification.error('Failed to delete bag.', {
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
      notification.success('Bag sealed successfully.');
      setSealTarget(null);
      void refetch();
    } catch (err) {
      notification.error('Failed to seal bag.', {
        description: getErrorMessage(err),
      });
    }
  };

  const handleReopen = async () => {
    if (!reopenTarget) {
      return;
    }
    if (!reopenReason.trim()) {
      notification.error('Reopen reason is required.');
      return;
    }
    try {
      await reopenBag({
        id: reopenTarget.id,
        body: { reason: reopenReason.trim() },
      }).unwrap();
      notification.success('Bag reopened successfully.');
      setReopenTarget(null);
      setReopenReason('');
      void refetch();
    } catch (err) {
      notification.error('Failed to reopen bag.', {
        description: getErrorMessage(err),
      });
    }
  };

  if (!canView) {
    return (
      <Alert>
        <ShieldAlert className='h-4 w-4' />
        <AlertTitle>Access denied</AlertTitle>
        <AlertDescription>
          Bagging requires hub operation access.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-2xl font-semibold tracking-tight'>Bags</h1>
          <p className='text-sm text-muted-foreground'>
            Create open bags, scan inbound orders, seal bags, and review fill
            metrics.
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
              Auto plan
            </Button>
          )}
          {canManage && (
            <Button
              variant='outline'
              disabled={isFetchingBagCapacitySettings}
              onClick={openSettingsDialog}
            >
              <SlidersHorizontal className='h-4 w-4' />
              Bag defaults
            </Button>
          )}
          <Button variant='outline' onClick={() => void refetch()}>
            <RefreshCcw className='h-4 w-4' />
            Refresh
          </Button>
          {canManage && (
            <Button onClick={handleCreate}>
              <Plus className='h-4 w-4' />
              New bag
            </Button>
          )}
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <PackageOpen className='h-4 w-4' />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='grid gap-3 md:grid-cols-4'>
            <div className='space-y-2'>
              <Label htmlFor='bag-keyword'>Keyword</Label>
              <Input
                id='bag-keyword'
                value={filters.keyword ?? ''}
                onChange={(event) =>
                  updateFilter('keyword', event.target.value || undefined)
                }
                placeholder='Search bags'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='bag-code-filter'>Bag code</Label>
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
              <Label htmlFor='bag-origin-filter'>Origin hub</Label>
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
                placeholder='All hubs'
                emptyText='No hubs found'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='bag-status-filter'>Status</Label>
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
                placeholder='All statuses'
                emptyText='No statuses found'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='bag-destination-type-filter'>
                Destination type
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
                placeholder='All destinations'
                emptyText='No destination types found'
              />
            </div>
            {filters.destinationType === 'HUB' && (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-hub-filter'>
                  Destination hub
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
                  placeholder='All destination hubs'
                  emptyText='No hubs found'
                />
              </div>
            )}
            {filters.destinationType === 'POST_OFFICE' && (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-post-office-filter'>
                  Destination post office
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
                  placeholder='All post offices'
                  emptyText='No post offices found'
                  loading={isFetchingPostOffices}
                />
              </div>
            )}
            <div className='space-y-2'>
              <Label htmlFor='bag-vehicle-filter'>Vehicle</Label>
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
                placeholder='All vehicles'
                emptyText='No vehicles found'
              />
            </div>
          </div>

          <div className='flex flex-wrap items-end gap-3'>
            <div className='space-y-2'>
              <Label htmlFor='bag-kpi-from'>KPI from</Label>
              <Input
                id='bag-kpi-from'
                type='datetime-local'
                value={kpiFrom}
                onChange={(event) => setKpiFrom(event.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='bag-kpi-to'>KPI to</Label>
              <Input
                id='bag-kpi-to'
                type='datetime-local'
                value={kpiTo}
                onChange={(event) => setKpiTo(event.target.value)}
              />
            </div>
            <Button variant='outline' onClick={resetFilters}>
              <FilterX className='h-4 w-4' />
              Clear filters
            </Button>
          </div>
        </CardContent>
      </Card>

      {filters.originHubId && (
        <BagKpiPanel data={kpiData} isFetching={isFetchingKpi} />
      )}

      <BagResultsTable
        data={bagsData}
        hubs={hubs}
        vehicles={vehicles}
        isFetching={isFetchingBags}
        canManage={canManage}
        canOperate={canOperate}
        page={page}
        onPageChange={setPage}
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
        routes={formRoutes}
        vehicles={vehicles}
        destinationPostOfficeOptions={formDestinationPostOfficeOptions}
        isLoadingDestinationPostOffices={isFetchingFormDestinationPostOffices}
        isLoadingRoutes={isFetchingFormRoutes}
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
            <DialogTitle>Bag defaults</DialogTitle>
            <DialogDescription>
              Set the default capacity used for new bags and auto planning.
            </DialogDescription>
          </DialogHeader>
          <form className='space-y-4' onSubmit={handleSubmitSettings}>
            <div className='grid gap-3 sm:grid-cols-3'>
              <div className='space-y-2'>
                <Label htmlFor='bag-default-max-weight'>Max weight (kg)</Label>
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
                <Label htmlFor='bag-default-max-volume'>Max volume (m3)</Label>
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
                <Label htmlFor='bag-default-max-orders'>Max orders</Label>
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
                Cancel
              </Button>
              <Button type='submit' disabled={isUpdatingBagCapacitySettings}>
                {isUpdatingBagCapacitySettings && (
                  <Loader2 className='h-4 w-4 animate-spin' />
                )}
                Save defaults
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
        title='Delete bag'
        description={`Delete ${deleteTarget?.bagCode ?? 'this bag'}?`}
        confirmText='Delete'
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
        title='Seal bag'
        description={`Seal ${sealTarget?.bagCode ?? 'this bag'}?`}
        confirmText='Seal'
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
            <DialogTitle>Reopen bag</DialogTitle>
            <DialogDescription>
              Reopen {reopenTarget?.bagCode ?? 'this bag'} for order changes.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-2'>
            <Label htmlFor='reopen-reason'>Reason *</Label>
            <Textarea
              id='reopen-reason'
              value={reopenReason}
              onChange={(event) => setReopenReason(event.target.value)}
              rows={4}
              placeholder='Reason for reopening'
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
              Cancel
            </Button>
            <Button disabled={isReopening} onClick={handleReopen}>
              {isReopening && <Loader2 className='h-4 w-4 animate-spin' />}
              Reopen
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
