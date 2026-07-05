/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Post office to hub transport via handover manifests
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { ArrowRight, PackageCheck, Plus, RefreshCw, Truck } from 'lucide-react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Button,
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import {
  useCancelPostOfficeHandoverManifestMutation,
  useConfirmHandoverManifestInboundMutation,
  useCreatePostOfficeHandoverManifestMutation,
  useDispatchPostOfficeHandoverManifestMutation,
  useGetBagDistributionManifestsQuery,
  useGetFirstMileOrdersQuery,
  useGetHandoverManifestByIdQuery,
  useGetHandoverManifestsQuery,
  useGetHubsQuery,
  useGetPostOfficeHandoverManifestByIdQuery,
  useGetPostOfficeHandoverManifestsQuery,
  useGetPostOfficesQuery,
  useGetSecondMileRoutesQuery,
  useGetSecondMileVehiclesQuery,
  useScanOutPostOfficeHandoverOrderMutation,
} from '../../api';
import type {
  BagDistributionManifestStatus,
  HandoverManifest,
  HandoverManifestStatus,
  Hub,
  PostOffice,
  SecondMileRoute,
  SecondMileVehicle,
} from '../../types';
import {
  HandoverManifestCreateDialog,
  HandoverManifestDetailDialog,
  HandoverManifestDispatchDialog,
  HandoverManifestReceiveDialog,
  HandoverManifestScanOutDialog,
  HandoverManifestTableCard,
  HubTransferManifestTableCard,
} from './components';
import {
  canManagePostOfficeHandover,
  canReceiveHubHandover,
  getDefaultArrivalTime,
  getDefaultDepartureTime,
  type HandoverManagementDomain,
  isHandoverDriverOnly,
  isReadyForDispatch,
  MANIFEST_STATUS_OPTIONS,
  normalizeScanCode,
  PAGE_SIZE,
  SELECT_PAGE_SIZE,
  type ManifestMode,
} from './handoverManifestModels';

function HandoverManifestManagementPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canPostOfficeMode = canManagePostOfficeHandover(roles);
  const canHubMode = canReceiveHubHandover(roles);
  const canAccess = canPostOfficeMode || canHubMode;

  const [activeDomain, setActiveDomain] =
    React.useState<HandoverManagementDomain>('POST_OFFICE_HUB');
  const [mode, setMode] = React.useState<ManifestMode>('POST_OFFICE');
  const effectiveMode = React.useMemo<ManifestMode>(() => {
    if (mode === 'POST_OFFICE' && canPostOfficeMode) {
      return 'POST_OFFICE';
    }
    if (mode === 'HUB' && canHubMode) {
      return 'HUB';
    }
    return canPostOfficeMode ? 'POST_OFFICE' : 'HUB';
  }, [canHubMode, canPostOfficeMode, mode]);

  const [page, setPage] = React.useState(0);
  const [filterHubId, setFilterHubId] = React.useState('');
  const [filterPostOfficeId, setFilterPostOfficeId] = React.useState('');
  const [filterStatus, setFilterStatus] = React.useState<
    'ALL' | HandoverManifestStatus
  >('ALL');
  const [hubTransferPage, setHubTransferPage] = React.useState(0);
  const [hubTransferStatus, setHubTransferStatus] = React.useState<
    'ALL' | BagDistributionManifestStatus
  >('ALL');

  const [isCreateOpen, setIsCreateOpen] = React.useState(false);
  const [createPostOfficeId, setCreatePostOfficeId] = React.useState('');
  const [createNote, setCreateNote] = React.useState('');
  const [selectedOrderCodes, setSelectedOrderCodes] = React.useState<string[]>(
    []
  );

  const [scanManifest, setScanManifest] =
    React.useState<HandoverManifest | null>(null);
  const [scanOrderCode, setScanOrderCode] = React.useState('');

  const [dispatchManifest, setDispatchManifest] =
    React.useState<HandoverManifest | null>(null);
  const [dispatchRouteId, setDispatchRouteId] = React.useState('');
  const [dispatchVehicleId, setDispatchVehicleId] = React.useState('');
  const [dispatchDepartureAt, setDispatchDepartureAt] = React.useState('');
  const [dispatchArrivalAt, setDispatchArrivalAt] = React.useState('');
  const [dispatchSealCode, setDispatchSealCode] = React.useState('');
  const [dispatchNote, setDispatchNote] = React.useState('');

  const [receiveManifest, setReceiveManifest] =
    React.useState<HandoverManifest | null>(null);
  const [receiveOrderCode, setReceiveOrderCode] = React.useState('');
  const [receivedOrderCodes, setReceivedOrderCodes] = React.useState<string[]>(
    []
  );

  const [detailTarget, setDetailTarget] = React.useState<{
    manifest: HandoverManifest;
    mode: ManifestMode;
  } | null>(null);

  const targetHubId = filterHubId ? Number(filterHubId) : undefined;
  const filterPostOfficeNumericId = filterPostOfficeId
    ? Number(filterPostOfficeId)
    : undefined;

  const { data: postOfficesData } = useGetPostOfficesQuery(
    { page: 0, size: SELECT_PAGE_SIZE, status: 'ACTIVE' },
    { skip: !canPostOfficeMode }
  );

  const { data: hubsData } = useGetHubsQuery(
    { page: 0, size: SELECT_PAGE_SIZE },
    { skip: !canHubMode }
  );

  const postOfficeOptions = postOfficesData?.items ?? [];
  const hubOptions = hubsData?.items ?? [];
  const postOfficeFilterOptions = [
    { value: 'ALL', label: 'Tất cả bưu cục' },
    ...postOfficeOptions.map((postOffice) => ({
      value: String(postOffice.id),
      label: `${postOffice.code} - ${postOffice.name}`,
    })),
  ];
  const createPostOfficeOptions = postOfficeOptions.map((postOffice) => ({
    value: String(postOffice.id),
    label: `${postOffice.code} - ${postOffice.name}`,
  }));
  const hubFilterOptions = [
    { value: 'ALL', label: 'Tất cả hub' },
    ...hubOptions.map((hub) => ({
      value: String(hub.id),
      label: `${hub.code} - ${hub.name}`,
    })),
  ];
  const statusFilterOptions = [
    { value: 'ALL', label: 'Tất cả trạng thái' },
    ...MANIFEST_STATUS_OPTIONS,
  ];

  const filterPostOffice = postOfficeOptions.find(
    (postOffice) => postOffice.id === filterPostOfficeNumericId
  );

  const {
    data: postOfficeManifestsData,
    isFetching: isFetchingPostOfficeManifests,
    refetch: refetchPostOfficeManifests,
  } = useGetPostOfficeHandoverManifestsQuery(
    {
      page,
      size: PAGE_SIZE,
      ...(filterPostOfficeNumericId
        ? { postOfficeId: filterPostOfficeNumericId }
        : {}),
      ...(targetHubId ? { targetHubId } : {}),
      ...(filterStatus !== 'ALL' ? { status: filterStatus } : {}),
    },
    { skip: !canPostOfficeMode || effectiveMode !== 'POST_OFFICE' }
  );

  const {
    data: hubManifestsData,
    isFetching: isFetchingHubManifests,
    refetch: refetchHubManifests,
  } = useGetHandoverManifestsQuery(
    {
      page,
      size: PAGE_SIZE,
      ...(filterPostOffice?.code
        ? { originPostOfficeCode: filterPostOffice.code }
        : {}),
      ...(targetHubId ? { targetHubId } : {}),
      ...(filterStatus !== 'ALL' ? { status: filterStatus } : {}),
    },
    { skip: !canHubMode || effectiveMode !== 'HUB' }
  );

  const {
    data: hubTransferManifestsData,
    isFetching: isFetchingHubTransferManifests,
    refetch: refetchHubTransferManifests,
  } = useGetBagDistributionManifestsQuery(
    {
      page: hubTransferPage,
      size: PAGE_SIZE,
      ...(hubTransferStatus !== 'ALL' ? { status: hubTransferStatus } : {}),
    },
    { skip: !canHubMode || activeDomain !== 'HUB_TRANSFER' }
  );

  const selectedCreatePostOffice = postOfficeOptions.find(
    (postOffice) => postOffice.id === Number(createPostOfficeId)
  );
  const dispatchTargetHubId = dispatchManifest?.targetHubId;
  const dispatchOriginPostOfficeCode = dispatchManifest?.originPostOfficeCode;

  const { data: dispatchRoutesData, isFetching: isLoadingDispatchRoutes } =
    useGetSecondMileRoutesQuery(
      {
        page: 0,
        size: SELECT_PAGE_SIZE,
        originType: 'POST_OFFICE',
        ...(dispatchOriginPostOfficeCode
          ? { originPostOfficeCode: dispatchOriginPostOfficeCode }
          : {}),
        destinationType: 'HUB',
        ...(dispatchTargetHubId
          ? { destinationHubId: dispatchTargetHubId }
          : {}),
        status: 'ACTIVE',
      },
      {
        skip:
          !dispatchManifest ||
          !dispatchTargetHubId ||
          !dispatchOriginPostOfficeCode,
      }
    );

  const { data: dispatchVehiclesData, isFetching: isLoadingDispatchVehicles } =
    useGetSecondMileVehiclesQuery(
      {
        page: 0,
        size: SELECT_PAGE_SIZE,
        ...(dispatchTargetHubId ? { hubId: dispatchTargetHubId } : {}),
        status: 'ACTIVE',
      },
      { skip: !dispatchManifest || !dispatchTargetHubId }
    );

  const { data: readyOrdersData, isFetching: isLoadingReadyOrders } =
    useGetFirstMileOrdersQuery(
      {
        page: 0,
        size: SELECT_PAGE_SIZE,
        originPostOfficeCode: selectedCreatePostOffice?.code,
        status: 'AT_ORIGIN_POST_OFFICE',
      },
      {
        skip:
          !canPostOfficeMode ||
          !isCreateOpen ||
          !selectedCreatePostOffice?.code,
      }
    );

  const scanManifestId = scanManifest?.id;
  const { data: scanManifestDetail, isFetching: isFetchingScanManifest } =
    useGetPostOfficeHandoverManifestByIdQuery(scanManifestId ?? 0, {
      skip: !scanManifestId,
    });

  const receiveManifestId = receiveManifest?.id;
  const { data: receiveManifestDetail, isFetching: isFetchingReceiveManifest } =
    useGetHandoverManifestByIdQuery(receiveManifestId ?? 0, {
      skip: !receiveManifestId,
    });

  const detailManifestId = detailTarget?.manifest.id;
  const {
    data: postOfficeDetailManifest,
    isFetching: isFetchingPostOfficeDetail,
  } = useGetPostOfficeHandoverManifestByIdQuery(detailManifestId ?? 0, {
    skip: !detailManifestId || detailTarget?.mode !== 'POST_OFFICE',
  });
  const { data: hubDetailManifest, isFetching: isFetchingHubDetail } =
    useGetHandoverManifestByIdQuery(detailManifestId ?? 0, {
      skip: !detailManifestId || detailTarget?.mode !== 'HUB',
    });

  const [createManifest, { isLoading: isCreating }] =
    useCreatePostOfficeHandoverManifestMutation();
  const [scanOutOrder, { isLoading: isScanningOut }] =
    useScanOutPostOfficeHandoverOrderMutation();
  const [dispatchManifestRun, { isLoading: isDispatching }] =
    useDispatchPostOfficeHandoverManifestMutation();
  const [cancelManifestRun, { isLoading: isCancelling }] =
    useCancelPostOfficeHandoverManifestMutation();
  const [confirmInbound, { isLoading: isConfirmingInbound }] =
    useConfirmHandoverManifestInboundMutation();

  const manifestsData =
    effectiveMode === 'POST_OFFICE'
      ? postOfficeManifestsData
      : hubManifestsData;
  const isFetching =
    effectiveMode === 'POST_OFFICE'
      ? isFetchingPostOfficeManifests
      : isFetchingHubManifests;
  const manifests = manifestsData?.items ?? [];
  const hubTransferManifests = hubTransferManifestsData?.items ?? [];
  const readyOrders = readyOrdersData?.items ?? [];
  const dispatchRoutes = dispatchRoutesData?.items ?? [];
  const dispatchVehicles = dispatchVehiclesData?.items ?? [];
  const dispatchRouteOptions = dispatchRoutes.map((route: SecondMileRoute) => ({
    value: String(route.id),
    label: `${route.routeCode} - ${route.routeName}`,
  }));
  const dispatchVehicleOptions = dispatchVehicles.map(
    (vehicle: SecondMileVehicle) => ({
      value: String(vehicle.id),
      label: `${vehicle.licensePlate} (${vehicle.vehicleType})`,
    })
  );
  const selectedDispatchRoute = dispatchRoutes.find(
    (route) => route.id === Number(dispatchRouteId)
  );

  const activeScanManifest = scanManifestDetail ?? scanManifest;
  const scanOrders = activeScanManifest?.orders ?? [];

  const activeReceiveManifest = receiveManifestDetail ?? receiveManifest;
  const receiveOrders = activeReceiveManifest?.orders ?? [];

  const detailManifest =
    detailTarget?.mode === 'POST_OFFICE'
      ? (postOfficeDetailManifest ?? detailTarget?.manifest)
      : (hubDetailManifest ?? detailTarget?.manifest);
  const isFetchingDetail =
    detailTarget?.mode === 'POST_OFFICE'
      ? isFetchingPostOfficeDetail
      : isFetchingHubDetail;

  const readyOrderCodes = React.useMemo(
    () =>
      readyOrders
        .map((order) => order.orderCode)
        .filter((orderCode): orderCode is string => Boolean(orderCode)),
    [readyOrders]
  );

  const allReadyOrdersSelected =
    readyOrderCodes.length > 0 &&
    readyOrderCodes.every((orderCode) =>
      selectedOrderCodes.includes(orderCode)
    );

  React.useEffect(() => {
    setPage(0);
    setFilterHubId('');
    setFilterPostOfficeId('');
    setFilterStatus('ALL');
  }, [effectiveMode]);

  React.useEffect(() => {
    setHubTransferPage(0);
  }, [hubTransferStatus]);

  React.useEffect(() => {
    setSelectedOrderCodes([]);
  }, [createPostOfficeId]);

  React.useEffect(() => {
    if (selectedDispatchRoute?.vehicleId) {
      setDispatchVehicleId(String(selectedDispatchRoute.vehicleId));
    }
  }, [selectedDispatchRoute?.vehicleId]);

  const refetchActiveManifests = () => {
    if (activeDomain === 'HUB_TRANSFER') {
      void refetchHubTransferManifests();
      return;
    }

    if (effectiveMode === 'POST_OFFICE') {
      void refetchPostOfficeManifests();
      return;
    }
    void refetchHubManifests();
  };

  const resolveHubLabel = (hubId?: number): string => {
    if (!hubId) {
      return '--';
    }
    const hub = hubOptions.find((item: Hub) => item.id === hubId);
    if (!hub) {
      return `#${hubId}`;
    }
    return `${hub.code} - ${hub.name}`;
  };

  const resolvePostOfficeLabel = (
    postOfficeId?: number,
    postOfficeCode?: string
  ): string => {
    const postOffice = postOfficeOptions.find(
      (item: PostOffice) =>
        (postOfficeId && item.id === postOfficeId) ||
        (postOfficeCode && item.code === postOfficeCode)
    );
    if (postOffice) {
      return `${postOffice.code} - ${postOffice.name}`;
    }
    return postOfficeCode || (postOfficeId ? `#${postOfficeId}` : '--');
  };

  const resolveRouteLabel = (routeId?: number, routeCode?: string): string => {
    if (routeCode) {
      return routeCode;
    }
    if (!routeId) {
      return '--';
    }
    const route = dispatchRoutes.find((item) => item.id === routeId);
    return route ? `${route.routeCode} - ${route.routeName}` : `#${routeId}`;
  };

  const resolveVehicleLabel = (
    vehicleId?: number,
    licensePlate?: string
  ): string => {
    if (licensePlate) {
      return licensePlate;
    }
    if (!vehicleId) {
      return '--';
    }
    const vehicle = dispatchVehicles.find((item) => item.id === vehicleId);
    return vehicle ? vehicle.licensePlate : `#${vehicleId}`;
  };

  const resetCreateForm = () => {
    setCreatePostOfficeId('');
    setCreateNote('');
    setSelectedOrderCodes([]);
  };

  const resetDispatchForm = () => {
    setDispatchManifest(null);
    setDispatchRouteId('');
    setDispatchVehicleId('');
    setDispatchDepartureAt('');
    setDispatchArrivalAt('');
    setDispatchSealCode('');
    setDispatchNote('');
  };

  const resetReceiveForm = () => {
    setReceiveManifest(null);
    setReceiveOrderCode('');
    setReceivedOrderCodes([]);
  };

  const handleToggleOrder = (orderCode: string, checked?: boolean) => {
    setSelectedOrderCodes((current) => {
      const exists = current.includes(orderCode);
      const shouldSelect = checked ?? !exists;

      if (shouldSelect && !exists) {
        return [...current, orderCode];
      }
      if (!shouldSelect && exists) {
        return current.filter((code) => code !== orderCode);
      }
      return current;
    });
  };

  const handleToggleAllReadyOrders = (checked: boolean) => {
    setSelectedOrderCodes(checked ? readyOrderCodes : []);
  };

  const handleCreateManifest = async () => {
    const postOfficeId = Number(createPostOfficeId);
    if (!postOfficeId || !selectedCreatePostOffice) {
      notification.error('Vui lòng chọn bưu cục gửi.');
      return;
    }
    if (selectedOrderCodes.length === 0) {
      notification.error('Vui lòng chọn ít nhất một đơn để bàn giao.');
      return;
    }

    try {
      await createManifest({
        post_office_id: postOfficeId,
        ...(selectedCreatePostOffice.hubId
          ? { target_hub_id: selectedCreatePostOffice.hubId }
          : {}),
        order_codes: selectedOrderCodes,
        ...(createNote.trim() ? { note: createNote.trim() } : {}),
      }).unwrap();
      notification.success('Đã tạo phiếu bàn giao.');
      setIsCreateOpen(false);
      resetCreateForm();
      void refetchPostOfficeManifests();
    } catch (error) {
      notification.error('Không thể tạo phiếu bàn giao.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleOpenDispatch = (manifest: HandoverManifest) => {
    setDispatchManifest(manifest);
    setDispatchRouteId(manifest.routeId ? String(manifest.routeId) : '');
    setDispatchVehicleId(manifest.vehicleId ? String(manifest.vehicleId) : '');
    setDispatchDepartureAt(
      manifest.plannedDepartureAt
        ? manifest.plannedDepartureAt.slice(0, 16)
        : getDefaultDepartureTime()
    );
    setDispatchArrivalAt(
      manifest.plannedArrivalAt
        ? manifest.plannedArrivalAt.slice(0, 16)
        : getDefaultArrivalTime()
    );
    setDispatchSealCode(manifest.sealCode ?? '');
    setDispatchNote(manifest.note ?? '');
  };

  const handleScanOutOrder = async (orderCodeValue?: string) => {
    if (!activeScanManifest?.id) {
      return;
    }
    const orderCode = normalizeScanCode(orderCodeValue ?? scanOrderCode);
    if (!orderCode) {
      notification.error('Vui lòng nhập mã đơn hàng.');
      return;
    }

    const matchedOrder = scanOrders.find(
      (order) => order.orderCode === orderCode
    );
    if (scanOrders.length > 0 && !matchedOrder) {
      notification.error('Đơn hàng này không có trong phiếu đã chọn.');
      return;
    }
    if (matchedOrder?.scanOutTime) {
      notification.error('Đơn hàng này đã được quét xuất.');
      return;
    }

    try {
      await scanOutOrder({
        manifestId: activeScanManifest.id,
        body: { order_code: orderCode },
      }).unwrap();
      notification.success('Đã quét xuất đơn hàng.');
      setScanOrderCode('');
    } catch (error) {
      notification.error('Không thể quét xuất đơn hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDispatchManifest = async () => {
    if (!dispatchManifest?.id) {
      return;
    }
    if (!isReadyForDispatch(dispatchManifest)) {
      notification.error('Cần quét xuất tất cả đơn hàng trước khi xuất đi.');
      return;
    }
    const routeId = Number(dispatchRouteId);
    const vehicleId = Number(dispatchVehicleId);
    if (!routeId) {
      notification.error('Vui lòng chọn tuyến từ bưu cục đến hub.');
      return;
    }
    if (!vehicleId) {
      notification.error('Vui lòng chọn phương tiện vận chuyển.');
      return;
    }
    if (!dispatchDepartureAt || !dispatchArrivalAt) {
      notification.error('Vui lòng nhập thời gian đi và thời gian đến dự kiến.');
      return;
    }
    if (new Date(dispatchArrivalAt) <= new Date(dispatchDepartureAt)) {
      notification.error('Thời gian đến dự kiến phải sau thời gian đi.');
      return;
    }

    try {
      await dispatchManifestRun({
        manifestId: dispatchManifest.id,
        body: {
          vehicle_id: vehicleId,
          route_id: routeId,
          planned_departure_at: dispatchDepartureAt,
          planned_arrival_at: dispatchArrivalAt,
          ...(dispatchSealCode.trim()
            ? { seal_code: dispatchSealCode.trim() }
            : {}),
          ...(dispatchNote.trim() ? { note: dispatchNote.trim() } : {}),
        },
      }).unwrap();
      notification.success('Đã xuất phiếu bàn giao đi hub.');
      resetDispatchForm();
      void refetchPostOfficeManifests();
    } catch (error) {
      notification.error('Không thể xuất phiếu bàn giao.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleCancelManifest = async (manifest: HandoverManifest) => {
    if (!manifest.id) {
      return;
    }
    const confirmed = window.confirm(
      `Hủy phiếu bàn giao ${manifest.manifestCode || `#${manifest.id}`}?`
    );
    if (!confirmed) {
      return;
    }

    try {
      await cancelManifestRun(manifest.id).unwrap();
      notification.success('Đã hủy phiếu bàn giao.');
      void refetchPostOfficeManifests();
    } catch (error) {
      notification.error('Không thể hủy phiếu bàn giao.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleOpenReceive = (manifest: HandoverManifest) => {
    setReceiveManifest(manifest);
    setReceiveOrderCode('');
    setReceivedOrderCodes([]);
  };

  const handleScanInboundOrder = (orderCodeValue?: string) => {
    const orderCode = normalizeScanCode(orderCodeValue ?? receiveOrderCode);
    if (!orderCode) {
      notification.error('Vui lòng nhập mã đơn hàng.');
      return;
    }

    const matchedOrder = receiveOrders.find(
      (order) => order.orderCode === orderCode
    );
    if (receiveOrders.length > 0 && !matchedOrder) {
      notification.error('Đơn hàng này không có trong phiếu đã chọn.');
      return;
    }
    if (matchedOrder?.scanInTime || receivedOrderCodes.includes(orderCode)) {
      notification.error('Đơn hàng này đã được quét nhập.');
      return;
    }

    setReceivedOrderCodes((current) => [...current, orderCode]);
    setReceiveOrderCode('');
  };

  const handleConfirmInbound = async () => {
    if (!activeReceiveManifest?.id) {
      return;
    }
    if (receivedOrderCodes.length === 0) {
      notification.error('Vui lòng quét ít nhất một đơn trước khi xác nhận nhập.');
      return;
    }

    try {
      await confirmInbound({
        manifestId: activeReceiveManifest.id,
        orderCodes: receivedOrderCodes,
      }).unwrap();
      notification.success('Đã xác nhận nhập hàng tại hub.');
      resetReceiveForm();
      void refetchHubManifests();
    } catch (error) {
      notification.error('Không thể xác nhận nhập hàng tại hub.', {
        description: getErrorMessage(error),
      });
    }
  };

  const postOfficeHubManifestTable = (
    <HandoverManifestTableCard
      effectiveMode={effectiveMode}
      filterHubId={filterHubId}
      filterPostOfficeId={filterPostOfficeId}
      filterStatus={filterStatus}
      hubFilterOptions={hubFilterOptions}
      isCancelling={isCancelling}
      isDispatching={isDispatching}
      isFetching={isFetching}
      manifests={manifests}
      manifestsData={manifestsData}
      page={page}
      postOfficeFilterOptions={postOfficeFilterOptions}
      resolveHubLabel={resolveHubLabel}
      resolvePostOfficeLabel={resolvePostOfficeLabel}
      resolveRouteLabel={resolveRouteLabel}
      resolveVehicleLabel={resolveVehicleLabel}
      showHubFilter={hubOptions.length > 0}
      showPostOfficeFilter={postOfficeOptions.length > 0}
      statusFilterOptions={statusFilterOptions}
      onCancelManifest={(manifest) => void handleCancelManifest(manifest)}
      onFilterHubChange={(value) => {
        setFilterHubId(value);
        setPage(0);
      }}
      onFilterPostOfficeChange={(value) => {
        setFilterPostOfficeId(value);
        setPage(0);
      }}
      onFilterStatusChange={(value) => {
        setFilterStatus(value);
        setPage(0);
      }}
      onNextPage={() => setPage((current) => current + 1)}
      onOpenDetail={(manifest) =>
        setDetailTarget({
          manifest,
          mode: effectiveMode,
        })
      }
      onOpenDispatch={handleOpenDispatch}
      onOpenReceive={handleOpenReceive}
      onOpenScanOut={(manifest) => {
        setScanManifest(manifest);
        setScanOrderCode('');
      }}
      onPreviousPage={() => setPage((current) => Math.max(current - 1, 0))}
    />
  );

  if (!canAccess) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Không có quyền truy cập</CardTitle>
          <CardDescription>
            Chỉ quản lý bưu cục, nhân sự hub hoặc quản trị TMS có thể quản lý
            bàn giao từ bưu cục đến hub.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Quản lý biên bản bàn giao
          </h1>
          <p className='text-muted-foreground'>
            Theo dõi tập trung các biên bản nhập/xuất hàng tại bưu cục, hub và
            các chặng vận chuyển liên quan.
          </p>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button
            variant='outline'
            onClick={refetchActiveManifests}
          >
            <RefreshCw className='mr-2 h-4 w-4' />
            Làm mới
          </Button>
          {activeDomain === 'POST_OFFICE_HUB' &&
          effectiveMode === 'POST_OFFICE' ? (
            <Button onClick={() => setIsCreateOpen(true)}>
              <Plus className='mr-2 h-4 w-4' />
              Tạo phiếu bàn giao
            </Button>
          ) : null}
        </div>
      </div>

      <Tabs
        value={activeDomain}
        onValueChange={(value) =>
          setActiveDomain(value as HandoverManagementDomain)
        }
        className='space-y-4'
      >
        <TabsList className='grid w-full grid-cols-2'>
          <TabsTrigger value='POST_OFFICE_HUB'>
            <Truck className='mr-2 h-4 w-4' />
            Bưu cục ↔ Hub
          </TabsTrigger>
          <TabsTrigger value='HUB_TRANSFER' disabled={!canHubMode}>
            <PackageCheck className='mr-2 h-4 w-4' />
            Hub ↔ Hub
          </TabsTrigger>
        </TabsList>

        <TabsContent value='POST_OFFICE_HUB' className='mt-0 space-y-4'>
          {canPostOfficeMode && canHubMode ? (
            <Tabs
              value={effectiveMode}
              onValueChange={(value) => setMode(value as ManifestMode)}
              className='space-y-4'
            >
              <TabsList className='grid w-full grid-cols-2 lg:w-auto'>
                <TabsTrigger value='POST_OFFICE'>
                  Bưu cục xuất hàng
                </TabsTrigger>
                <TabsTrigger value='HUB'>Hub nhận hàng</TabsTrigger>
              </TabsList>
              <TabsContent value='POST_OFFICE' className='mt-0'>
                {postOfficeHubManifestTable}
              </TabsContent>
              <TabsContent value='HUB' className='mt-0'>
                {postOfficeHubManifestTable}
              </TabsContent>
            </Tabs>
          ) : (
            postOfficeHubManifestTable
          )}
        </TabsContent>

        <TabsContent value='HUB_TRANSFER' className='mt-0'>
          <HubTransferManifestTableCard
            filterStatus={hubTransferStatus}
            isFetching={isFetchingHubTransferManifests}
            manifests={hubTransferManifests}
            manifestsData={hubTransferManifestsData}
            page={hubTransferPage}
            statusFilterOptions={statusFilterOptions}
            onFilterStatusChange={setHubTransferStatus}
            onNextPage={() => setHubTransferPage((current) => current + 1)}
            onPreviousPage={() =>
              setHubTransferPage((current) => Math.max(current - 1, 0))
            }
          />
        </TabsContent>

      </Tabs>

      <HandoverManifestCreateDialog
        allReadyOrdersSelected={allReadyOrdersSelected}
        createNote={createNote}
        createPostOfficeId={createPostOfficeId}
        createPostOfficeOptions={createPostOfficeOptions}
        isCreating={isCreating}
        isLoadingReadyOrders={isLoadingReadyOrders}
        open={isCreateOpen}
        readyOrderCodes={readyOrderCodes}
        readyOrders={readyOrders}
        selectedCreatePostOffice={selectedCreatePostOffice}
        selectedOrderCodes={selectedOrderCodes}
        targetHubLabel={
          selectedCreatePostOffice?.hubId
            ? resolveHubLabel(selectedCreatePostOffice.hubId)
            : 'Chưa gán hub'
        }
        onCreateNoteChange={setCreateNote}
        onCreatePostOfficeChange={setCreatePostOfficeId}
        onOpenChange={(open) => {
          setIsCreateOpen(open);
          if (!open) {
            resetCreateForm();
          }
        }}
        onSubmit={() => void handleCreateManifest()}
        onToggleAllReadyOrders={handleToggleAllReadyOrders}
        onToggleOrder={handleToggleOrder}
      />

      <HandoverManifestScanOutDialog
        activeScanManifest={activeScanManifest}
        isFetchingScanManifest={isFetchingScanManifest}
        isScanningOut={isScanningOut}
        open={Boolean(scanManifest)}
        scanOrderCode={scanOrderCode}
        scanOrders={scanOrders}
        onOpenChange={(open) => {
          if (!open) {
            setScanManifest(null);
            setScanOrderCode('');
          }
        }}
        onScan={(orderCode) => void handleScanOutOrder(orderCode)}
        onScanOrderCodeChange={setScanOrderCode}
      />

      <HandoverManifestDispatchDialog
        dispatchArrivalAt={dispatchArrivalAt}
        dispatchDepartureAt={dispatchDepartureAt}
        dispatchManifest={dispatchManifest}
        dispatchNote={dispatchNote}
        dispatchRouteId={dispatchRouteId}
        dispatchRouteOptions={dispatchRouteOptions}
        dispatchSealCode={dispatchSealCode}
        dispatchVehicleId={dispatchVehicleId}
        dispatchVehicleOptions={dispatchVehicleOptions}
        isDispatching={isDispatching}
        isLoadingDispatchRoutes={isLoadingDispatchRoutes}
        isLoadingDispatchVehicles={isLoadingDispatchVehicles}
        open={Boolean(dispatchManifest)}
        onArrivalAtChange={setDispatchArrivalAt}
        onDepartureAtChange={setDispatchDepartureAt}
        onNoteChange={setDispatchNote}
        onOpenChange={(open) => {
          if (!open) {
            resetDispatchForm();
          }
        }}
        onRouteChange={setDispatchRouteId}
        onSealCodeChange={setDispatchSealCode}
        onSubmit={() => void handleDispatchManifest()}
        onVehicleChange={setDispatchVehicleId}
      />

      <HandoverManifestReceiveDialog
        activeReceiveManifest={activeReceiveManifest}
        isConfirmingInbound={isConfirmingInbound}
        isFetchingReceiveManifest={isFetchingReceiveManifest}
        open={Boolean(receiveManifest)}
        receiveOrderCode={receiveOrderCode}
        receiveOrders={receiveOrders}
        receivedOrderCodes={receivedOrderCodes}
        onConfirm={() => void handleConfirmInbound()}
        onOpenChange={(open) => {
          if (!open) {
            resetReceiveForm();
          }
        }}
        onReceiveOrderCodeChange={setReceiveOrderCode}
        onScanInboundOrder={handleScanInboundOrder}
      />

      <HandoverManifestDetailDialog
        detailManifest={detailManifest}
        isFetchingDetail={isFetchingDetail}
        open={Boolean(detailTarget)}
        resolveHubLabel={resolveHubLabel}
        resolvePostOfficeLabel={resolvePostOfficeLabel}
        resolveRouteLabel={resolveRouteLabel}
        resolveVehicleLabel={resolveVehicleLabel}
        onOpenChange={(open) => {
          if (!open) {
            setDetailTarget(null);
          }
        }}
      />
    </div>
  );
}

export function HandoverManifestListPage() {
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );

  if (isHandoverDriverOnly(roles)) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Check-in tài xế đã chuyển sang trang Trung chuyển</CardTitle>
          <CardDescription>
            Vui lòng thực hiện check-in các chặng trung chuyển tại trang Trung
            chuyển để tập trung cùng các chuyến hub.
          </CardDescription>
          <div className='pt-2'>
            <Button asChild>
              <Link href='/first-mile/pickup-and-delivery/transit'>
                Mở trang Trung chuyển
                <ArrowRight className='h-4 w-4' />
              </Link>
            </Button>
          </div>
        </CardHeader>
      </Card>
    );
  }

  return <HandoverManifestManagementPage />;
}
