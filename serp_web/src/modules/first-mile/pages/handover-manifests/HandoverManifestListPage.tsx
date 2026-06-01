/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Post office to hub transport via handover manifests
 */

'use client';

import React from 'react';
import {
  CheckCircle2,
  Eye,
  PackageCheck,
  Plus,
  RefreshCw,
  ScanLine,
  Send,
  Truck,
  XCircle,
} from 'lucide-react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import { TmsCombobox } from '../../components';
import {
  useCancelPostOfficeHandoverManifestMutation,
  useConfirmHandoverManifestInboundMutation,
  useCreatePostOfficeHandoverManifestMutation,
  useDispatchPostOfficeHandoverManifestMutation,
  useGetFirstMileOrdersQuery,
  useGetHandoverManifestByIdQuery,
  useGetHandoverManifestsQuery,
  useGetHubsQuery,
  useGetPostOfficeHandoverManifestByIdQuery,
  useGetPostOfficeHandoverManifestsQuery,
  useGetPostOfficesQuery,
  useScanOutPostOfficeHandoverOrderMutation,
} from '../../api';
import type {
  HandoverManifest,
  HandoverManifestOrderItem,
  HandoverManifestStatus,
  Hub,
  PostOffice,
} from '../../types';

const PAGE_SIZE = 20;
const SELECT_PAGE_SIZE = 200;

type ManifestMode = 'POST_OFFICE' | 'HUB';

const MANIFEST_STATUS_OPTIONS: Array<{
  value: HandoverManifestStatus;
  label: string;
}> = [
  { value: 'CREATED', label: 'Created' },
  { value: 'OUTBOUND_CONFIRMED', label: 'Dispatched to hub' },
  { value: 'INBOUND_CONFIRMED', label: 'Received at hub' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

const MANIFEST_STATUS_LABELS: Record<HandoverManifestStatus, string> = {
  CREATED: 'Created',
  OUTBOUND_CONFIRMED: 'Dispatched to hub',
  INBOUND_CONFIRMED: 'Received at hub',
  CANCELLED: 'Cancelled',
};

const canManagePostOfficeHandover = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') || roles.includes('TMS_POSTOFFICER_MANAGER');

const canReceiveHubHandover = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') ||
  roles.includes('TMS_HUB_MANAGER') ||
  roles.includes('TMS_HUB_EMPLOYEE');

const getStatusBadgeVariant = (
  status?: HandoverManifestStatus
): 'default' | 'secondary' | 'outline' | 'destructive' => {
  if (status === 'INBOUND_CONFIRMED') {
    return 'default';
  }
  if (status === 'OUTBOUND_CONFIRMED') {
    return 'outline';
  }
  if (status === 'CANCELLED') {
    return 'destructive';
  }
  return 'secondary';
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

const getTotalOrders = (manifest?: HandoverManifest | null): number =>
  manifest?.totalOrders ?? manifest?.orders?.length ?? 0;

const getScannedOutOrders = (manifest?: HandoverManifest | null): number =>
  manifest?.scannedOutOrders ??
  manifest?.orders?.filter((order) => Boolean(order.scanOutTime)).length ??
  0;

const getScannedInOrders = (manifest?: HandoverManifest | null): number =>
  manifest?.scannedInOrders ??
  manifest?.orders?.filter((order) => Boolean(order.scanInTime)).length ??
  0;

const isReadyForDispatch = (manifest: HandoverManifest): boolean => {
  const totalOrders = getTotalOrders(manifest);
  return (
    manifest.status === 'CREATED' &&
    totalOrders > 0 &&
    getScannedOutOrders(manifest) >= totalOrders
  );
};

const normalizeScanCode = (value: string): string => value.trim();

interface DetailItemProps {
  label: string;
  value: React.ReactNode;
}

function DetailItem({ label, value }: DetailItemProps) {
  return (
    <div className='space-y-1 rounded-md border p-3'>
      <p className='text-xs font-medium uppercase text-muted-foreground'>
        {label}
      </p>
      <div className='text-sm'>{value}</div>
    </div>
  );
}

interface ManifestOrdersTableProps {
  actionMode?: 'SCAN_OUT' | 'RECEIVE';
  isActionLoading?: boolean;
  onScan?: (orderCode: string) => void;
  orders?: HandoverManifestOrderItem[];
  scannedOrderCodes?: string[];
}

function ManifestOrdersTable({
  actionMode,
  isActionLoading,
  onScan,
  orders = [],
  scannedOrderCodes = [],
}: ManifestOrdersTableProps) {
  return (
    <div className='overflow-x-auto rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Order</TableHead>
            <TableHead>Customer order</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Scan out</TableHead>
            <TableHead>Scan in</TableHead>
            {actionMode ? <TableHead>Action</TableHead> : null}
          </TableRow>
        </TableHeader>
        <TableBody>
          {orders.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={actionMode ? 6 : 5}
                className='py-8 text-center text-muted-foreground'
              >
                No orders in this manifest.
              </TableCell>
            </TableRow>
          ) : (
            orders.map((order) => {
              const orderCode = order.orderCode ?? '';
              const isScanned =
                actionMode === 'SCAN_OUT'
                  ? Boolean(order.scanOutTime)
                  : Boolean(
                      order.scanInTime ||
                        (orderCode && scannedOrderCodes.includes(orderCode))
                    );
              const scannedLabel =
                actionMode === 'SCAN_OUT' ? 'Scanned out' : 'Scanned in';

              return (
                <TableRow key={order.id ?? order.orderCode}>
                  <TableCell className='font-medium'>
                    {order.orderCode || '--'}
                  </TableCell>
                  <TableCell>{order.customerOrderCode || '--'}</TableCell>
                  <TableCell>
                    {order.status ? (
                      <Badge variant='outline'>{order.status}</Badge>
                    ) : (
                      '--'
                    )}
                  </TableCell>
                  <TableCell>{formatDateTime(order.scanOutTime)}</TableCell>
                  <TableCell>{formatDateTime(order.scanInTime)}</TableCell>
                  {actionMode ? (
                    <TableCell>
                      {isScanned ? (
                        <Badge variant='secondary'>{scannedLabel}</Badge>
                      ) : (
                        <Button
                          size='sm'
                          variant='outline'
                          disabled={!orderCode || isActionLoading}
                          onClick={() => onScan?.(orderCode)}
                        >
                          Scan
                        </Button>
                      )}
                    </TableCell>
                  ) : null}
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
}

export function HandoverManifestListPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canPostOfficeMode = canManagePostOfficeHandover(roles);
  const canHubMode = canReceiveHubHandover(roles);
  const canAccess = canPostOfficeMode || canHubMode;

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
    { value: 'ALL', label: 'All post offices' },
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
    { value: 'ALL', label: 'All hubs' },
    ...hubOptions.map((hub) => ({
      value: String(hub.id),
      label: `${hub.code} - ${hub.name}`,
    })),
  ];
  const statusFilterOptions = [
    { value: 'ALL', label: 'All statuses' },
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

  const selectedCreatePostOffice = postOfficeOptions.find(
    (postOffice) => postOffice.id === Number(createPostOfficeId)
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
  const readyOrders = readyOrdersData?.items ?? [];

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
    setSelectedOrderCodes([]);
  }, [createPostOfficeId]);

  const refetchActiveManifests = () => {
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

  const resetCreateForm = () => {
    setCreatePostOfficeId('');
    setCreateNote('');
    setSelectedOrderCodes([]);
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
      notification.error('Origin post office is required.');
      return;
    }
    if (selectedOrderCodes.length === 0) {
      notification.error('Select at least one order to hand over.');
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
      notification.success('Handover manifest created successfully.');
      setIsCreateOpen(false);
      resetCreateForm();
      void refetchPostOfficeManifests();
    } catch (error) {
      notification.error('Failed to create handover manifest.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleOpenDispatch = (manifest: HandoverManifest) => {
    setDispatchManifest(manifest);
    setDispatchSealCode(manifest.sealCode ?? '');
    setDispatchNote(manifest.note ?? '');
  };

  const handleScanOutOrder = async (orderCodeValue?: string) => {
    if (!activeScanManifest?.id) {
      return;
    }
    const orderCode = normalizeScanCode(orderCodeValue ?? scanOrderCode);
    if (!orderCode) {
      notification.error('Order code is required.');
      return;
    }

    const matchedOrder = scanOrders.find(
      (order) => order.orderCode === orderCode
    );
    if (scanOrders.length > 0 && !matchedOrder) {
      notification.error('This order is not in the selected manifest.');
      return;
    }
    if (matchedOrder?.scanOutTime) {
      notification.error('This order has already been scanned out.');
      return;
    }

    try {
      await scanOutOrder({
        manifestId: activeScanManifest.id,
        body: { order_code: orderCode },
      }).unwrap();
      notification.success('Order scanned out successfully.');
      setScanOrderCode('');
    } catch (error) {
      notification.error('Failed to scan order out.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDispatchManifest = async () => {
    if (!dispatchManifest?.id) {
      return;
    }
    if (!isReadyForDispatch(dispatchManifest)) {
      notification.error('All orders must be scanned out before dispatch.');
      return;
    }

    try {
      await dispatchManifestRun({
        manifestId: dispatchManifest.id,
        body: {
          ...(dispatchSealCode.trim()
            ? { seal_code: dispatchSealCode.trim() }
            : {}),
          ...(dispatchNote.trim() ? { note: dispatchNote.trim() } : {}),
        },
      }).unwrap();
      notification.success('Manifest dispatched to hub successfully.');
      setDispatchManifest(null);
      setDispatchSealCode('');
      setDispatchNote('');
      void refetchPostOfficeManifests();
    } catch (error) {
      notification.error('Failed to dispatch manifest.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleCancelManifest = async (manifest: HandoverManifest) => {
    if (!manifest.id) {
      return;
    }
    const confirmed = window.confirm(
      `Cancel manifest ${manifest.manifestCode || `#${manifest.id}`}?`
    );
    if (!confirmed) {
      return;
    }

    try {
      await cancelManifestRun(manifest.id).unwrap();
      notification.success('Manifest cancelled successfully.');
      void refetchPostOfficeManifests();
    } catch (error) {
      notification.error('Failed to cancel manifest.', {
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
      notification.error('Order code is required.');
      return;
    }

    const matchedOrder = receiveOrders.find(
      (order) => order.orderCode === orderCode
    );
    if (receiveOrders.length > 0 && !matchedOrder) {
      notification.error('This order is not in the selected manifest.');
      return;
    }
    if (matchedOrder?.scanInTime || receivedOrderCodes.includes(orderCode)) {
      notification.error('This order has already been scanned in.');
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
      notification.error('Scan at least one order before confirming inbound.');
      return;
    }

    try {
      await confirmInbound({
        manifestId: activeReceiveManifest.id,
        orderCodes: receivedOrderCodes,
      }).unwrap();
      notification.success('Inbound receiving confirmed successfully.');
      setReceiveManifest(null);
      setReceiveOrderCode('');
      setReceivedOrderCodes([]);
      void refetchHubManifests();
    } catch (error) {
      notification.error('Failed to confirm inbound receiving.', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!canAccess) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Access denied</CardTitle>
          <CardDescription>
            Only post office managers, hub staff, or TMS admin can manage post
            office to hub handover.
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
            Post Office to Hub Handover
          </h1>
          <p className='text-muted-foreground'>
            Create manifests at post offices, scan orders out, dispatch them,
            and receive them at hubs.
          </p>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button variant='outline' onClick={refetchActiveManifests}>
            <RefreshCw className='mr-2 h-4 w-4' />
            Refresh
          </Button>
          {effectiveMode === 'POST_OFFICE' ? (
            <Button onClick={() => setIsCreateOpen(true)}>
              <Plus className='mr-2 h-4 w-4' />
              New handover
            </Button>
          ) : null}
        </div>
      </div>

      {canPostOfficeMode && canHubMode ? (
        <Card>
          <CardContent className='flex flex-col gap-3 pt-6 sm:flex-row'>
            <Button
              variant={effectiveMode === 'POST_OFFICE' ? 'default' : 'outline'}
              onClick={() => setMode('POST_OFFICE')}
            >
              <Truck className='mr-2 h-4 w-4' />
              Post office dispatch
            </Button>
            <Button
              variant={effectiveMode === 'HUB' ? 'default' : 'outline'}
              onClick={() => setMode('HUB')}
            >
              <PackageCheck className='mr-2 h-4 w-4' />
              Hub receiving
            </Button>
          </CardContent>
        </Card>
      ) : null}

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            {effectiveMode === 'POST_OFFICE' ? (
              <Truck className='h-5 w-5' />
            ) : (
              <PackageCheck className='h-5 w-5' />
            )}
            {effectiveMode === 'POST_OFFICE'
              ? 'Post office manifests'
              : 'Hub inbound manifests'}
          </CardTitle>
          <CardDescription>
            {effectiveMode === 'POST_OFFICE'
              ? 'Build a handover manifest, scan every order out, then dispatch it to the mapped hub.'
              : 'Receive dispatched manifests from post offices and confirm inbound orders at the hub.'}
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='grid gap-4 md:grid-cols-3'>
            {postOfficeOptions.length > 0 ? (
              <div className='space-y-2'>
                <Label htmlFor='filter-post-office'>Origin post office</Label>
                <TmsCombobox
                  id='filter-post-office'
                  value={filterPostOfficeId || 'ALL'}
                  onValueChange={(value) => {
                    setFilterPostOfficeId(value === 'ALL' ? '' : value);
                    setPage(0);
                  }}
                  options={postOfficeFilterOptions}
                  placeholder='All post offices'
                  emptyText='No post offices found'
                />
              </div>
            ) : null}

            {hubOptions.length > 0 ? (
              <div className='space-y-2'>
                <Label htmlFor='filter-hub'>Target hub</Label>
                <TmsCombobox
                  id='filter-hub'
                  value={filterHubId || 'ALL'}
                  onValueChange={(value) => {
                    setFilterHubId(value === 'ALL' ? '' : value);
                    setPage(0);
                  }}
                  options={hubFilterOptions}
                  placeholder='All hubs'
                  emptyText='No hubs found'
                />
              </div>
            ) : null}

            <div className='space-y-2'>
              <Label htmlFor='filter-status'>Status</Label>
              <TmsCombobox
                id='filter-status'
                value={filterStatus}
                onValueChange={(value) => {
                  setFilterStatus(value as 'ALL' | HandoverManifestStatus);
                  setPage(0);
                }}
                options={statusFilterOptions}
                placeholder='All statuses'
                emptyText='No statuses found'
              />
            </div>
          </div>

          <div className='overflow-x-auto rounded-md border'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Manifest</TableHead>
                  <TableHead>Post office</TableHead>
                  <TableHead>Hub</TableHead>
                  <TableHead>Orders</TableHead>
                  <TableHead>Progress</TableHead>
                  <TableHead>Seal</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Updated</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {manifests.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={9}
                      className='py-8 text-center text-muted-foreground'
                    >
                      {isFetching
                        ? 'Loading handover manifests...'
                        : 'No handover manifests found.'}
                    </TableCell>
                  </TableRow>
                ) : (
                  manifests.map((manifest) => {
                    const totalOrders = getTotalOrders(manifest);
                    const scannedOutOrders = getScannedOutOrders(manifest);
                    const scannedInOrders = getScannedInOrders(manifest);

                    return (
                      <TableRow key={manifest.id}>
                        <TableCell className='font-medium'>
                          {manifest.manifestCode || `#${manifest.id}`}
                        </TableCell>
                        <TableCell>
                          {resolvePostOfficeLabel(
                            manifest.originPostOfficeId,
                            manifest.originPostOfficeCode
                          )}
                        </TableCell>
                        <TableCell>
                          {resolveHubLabel(manifest.targetHubId)}
                        </TableCell>
                        <TableCell>{totalOrders}</TableCell>
                        <TableCell>
                          {effectiveMode === 'POST_OFFICE'
                            ? `${scannedOutOrders}/${totalOrders} out`
                            : `${scannedInOrders}/${totalOrders} in`}
                        </TableCell>
                        <TableCell>{manifest.sealCode || '--'}</TableCell>
                        <TableCell>
                          {manifest.status ? (
                            <Badge
                              variant={getStatusBadgeVariant(manifest.status)}
                            >
                              {MANIFEST_STATUS_LABELS[manifest.status]}
                            </Badge>
                          ) : (
                            '--'
                          )}
                        </TableCell>
                        <TableCell>
                          {formatDateTime(manifest.updatedAt)}
                        </TableCell>
                        <TableCell>
                          <div className='flex flex-wrap gap-2'>
                            <Button
                              size='sm'
                              variant='outline'
                              onClick={() =>
                                setDetailTarget({
                                  manifest,
                                  mode: effectiveMode,
                                })
                              }
                            >
                              <Eye className='mr-1 h-3.5 w-3.5' />
                              View
                            </Button>

                            {effectiveMode === 'POST_OFFICE' &&
                            manifest.status === 'CREATED' ? (
                              <>
                                <Button
                                  size='sm'
                                  variant='outline'
                                  onClick={() => {
                                    setScanManifest(manifest);
                                    setScanOrderCode('');
                                  }}
                                >
                                  <ScanLine className='mr-1 h-3.5 w-3.5' />
                                  Scan out
                                </Button>
                                <Button
                                  size='sm'
                                  disabled={
                                    !isReadyForDispatch(manifest) ||
                                    isDispatching
                                  }
                                  onClick={() => handleOpenDispatch(manifest)}
                                >
                                  <Send className='mr-1 h-3.5 w-3.5' />
                                  Dispatch
                                </Button>
                                <Button
                                  size='sm'
                                  variant='outline'
                                  disabled={isCancelling}
                                  onClick={() =>
                                    void handleCancelManifest(manifest)
                                  }
                                >
                                  <XCircle className='mr-1 h-3.5 w-3.5' />
                                  Cancel
                                </Button>
                              </>
                            ) : null}

                            {effectiveMode === 'HUB' &&
                            manifest.status === 'OUTBOUND_CONFIRMED' ? (
                              <Button
                                size='sm'
                                onClick={() => handleOpenReceive(manifest)}
                              >
                                <CheckCircle2 className='mr-1 h-3.5 w-3.5' />
                                Receive
                              </Button>
                            ) : null}
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })
                )}
              </TableBody>
            </Table>
          </div>

          <div className='flex items-center justify-between'>
            <p className='text-sm text-muted-foreground'>
              Page {(manifestsData?.currentPage ?? 0) + 1} of{' '}
              {Math.max(manifestsData?.totalPages ?? 1, 1)} ·{' '}
              {manifestsData?.totalItems ?? 0} manifest(s)
            </p>
            <div className='flex gap-2'>
              <Button
                variant='outline'
                size='sm'
                disabled={page <= 0 || isFetching}
                onClick={() => setPage((current) => Math.max(current - 1, 0))}
              >
                Previous
              </Button>
              <Button
                variant='outline'
                size='sm'
                disabled={
                  isFetching || (manifestsData?.totalPages ?? 1) <= page + 1
                }
                onClick={() => setPage((current) => current + 1)}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <Dialog
        open={isCreateOpen}
        onOpenChange={(open) => {
          setIsCreateOpen(open);
          if (!open) {
            resetCreateForm();
          }
        }}
      >
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>New post office handover</DialogTitle>
            <DialogDescription>
              Select orders waiting at the post office. The target hub is read
              from the post office mapping.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='create-post-office'>Origin post office *</Label>
                <TmsCombobox
                  id='create-post-office'
                  value={createPostOfficeId}
                  onValueChange={setCreatePostOfficeId}
                  options={createPostOfficeOptions}
                  placeholder='Select post office'
                  emptyText='No post offices found'
                />
              </div>

              <DetailItem
                label='Target hub'
                value={
                  selectedCreatePostOffice?.hubId
                    ? resolveHubLabel(selectedCreatePostOffice.hubId)
                    : 'No mapped hub'
                }
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='create-note'>Note</Label>
              <Textarea
                id='create-note'
                value={createNote}
                onChange={(event) => setCreateNote(event.target.value)}
                placeholder='Optional handover note'
              />
            </div>

            <div className='space-y-2'>
              <div className='flex flex-wrap items-center justify-between gap-2'>
                <Label>Orders ready at post office *</Label>
                {readyOrderCodes.length > 0 ? (
                  <label className='flex items-center gap-2 text-sm'>
                    <Checkbox
                      checked={allReadyOrdersSelected}
                      onCheckedChange={(value) =>
                        handleToggleAllReadyOrders(Boolean(value))
                      }
                    />
                    Select all
                  </label>
                ) : null}
              </div>

              {!selectedCreatePostOffice ? (
                <p className='text-sm text-muted-foreground'>
                  Select a post office to load ready orders.
                </p>
              ) : isLoadingReadyOrders ? (
                <p className='text-sm text-muted-foreground'>
                  Loading ready orders...
                </p>
              ) : readyOrders.length === 0 ? (
                <p className='text-sm text-muted-foreground'>
                  No orders with status AT_ORIGIN_POST_OFFICE at this post
                  office.
                </p>
              ) : (
                <div className='max-h-72 space-y-2 overflow-y-auto rounded-md border p-3'>
                  {readyOrders.map((order) => {
                    const orderCode = order.orderCode;
                    if (!orderCode) {
                      return null;
                    }
                    const checked = selectedOrderCodes.includes(orderCode);
                    return (
                      <div
                        key={order.id}
                        className='flex items-start gap-3 rounded-md border p-3'
                      >
                        <Checkbox
                          checked={checked}
                          onCheckedChange={(value) =>
                            handleToggleOrder(orderCode, Boolean(value))
                          }
                          aria-label={`Select order ${orderCode}`}
                        />
                        <div className='min-w-0 flex-1 space-y-1 text-sm'>
                          <div className='flex flex-wrap items-center gap-2'>
                            <span className='font-medium'>{orderCode}</span>
                            <Badge variant='outline'>{order.status}</Badge>
                          </div>
                          <p className='text-xs text-muted-foreground'>
                            Customer order: {order.customerOrderCode || '--'}
                          </p>
                          <p className='text-xs text-muted-foreground'>
                            Receiver: {order.receiverName || '--'} (
                            {order.receiverPhone || '--'})
                          </p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={() => setIsCreateOpen(false)}>
              Cancel
            </Button>
            <Button
              disabled={isCreating}
              onClick={() => void handleCreateManifest()}
            >
              {isCreating ? 'Creating...' : 'Create manifest'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={Boolean(scanManifest)}
        onOpenChange={(open) => {
          if (!open) {
            setScanManifest(null);
            setScanOrderCode('');
          }
        }}
      >
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
          <DialogHeader>
            <DialogTitle>Scan orders out</DialogTitle>
            <DialogDescription>
              Scan each order before dispatching the manifest to the hub.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='grid gap-3 sm:grid-cols-3'>
              <DetailItem
                label='Manifest'
                value={activeScanManifest?.manifestCode || '--'}
              />
              <DetailItem
                label='Scanned out'
                value={`${getScannedOutOrders(activeScanManifest)}/${getTotalOrders(
                  activeScanManifest
                )}`}
              />
              <DetailItem
                label='Status'
                value={activeScanManifest?.status || '--'}
              />
            </div>

            <div className='flex flex-col gap-2 sm:flex-row'>
              <Input
                value={scanOrderCode}
                onChange={(event) => setScanOrderCode(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault();
                    void handleScanOutOrder();
                  }
                }}
                placeholder='Scan or enter order code'
              />
              <Button
                disabled={isScanningOut}
                onClick={() => void handleScanOutOrder()}
              >
                <ScanLine className='mr-2 h-4 w-4' />
                Scan out
              </Button>
            </div>

            {isFetchingScanManifest ? (
              <p className='text-sm text-muted-foreground'>
                Loading manifest orders...
              </p>
            ) : (
              <ManifestOrdersTable
                actionMode='SCAN_OUT'
                isActionLoading={isScanningOut}
                orders={scanOrders}
                onScan={(orderCode) => void handleScanOutOrder(orderCode)}
              />
            )}
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={() => setScanManifest(null)}>
              Done
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={Boolean(dispatchManifest)}
        onOpenChange={(open) => {
          if (!open) {
            setDispatchManifest(null);
            setDispatchSealCode('');
            setDispatchNote('');
          }
        }}
      >
        <DialogContent className='sm:max-w-xl'>
          <DialogHeader>
            <DialogTitle>Dispatch manifest to hub</DialogTitle>
            <DialogDescription>
              Dispatch is allowed only after all orders have been scanned out.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='grid gap-3 sm:grid-cols-2'>
              <DetailItem
                label='Manifest'
                value={dispatchManifest?.manifestCode || '--'}
              />
              <DetailItem
                label='Scan progress'
                value={`${getScannedOutOrders(dispatchManifest)}/${getTotalOrders(
                  dispatchManifest
                )}`}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-seal'>Seal code</Label>
              <Input
                id='dispatch-seal'
                value={dispatchSealCode}
                onChange={(event) => setDispatchSealCode(event.target.value)}
                placeholder='Optional seal code'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='dispatch-note'>Dispatch note</Label>
              <Textarea
                id='dispatch-note'
                value={dispatchNote}
                onChange={(event) => setDispatchNote(event.target.value)}
                placeholder='Optional dispatch note'
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={() => setDispatchManifest(null)}>
              Cancel
            </Button>
            <Button
              disabled={
                !dispatchManifest ||
                !isReadyForDispatch(dispatchManifest) ||
                isDispatching
              }
              onClick={() => void handleDispatchManifest()}
            >
              {isDispatching ? 'Dispatching...' : 'Dispatch to hub'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={Boolean(receiveManifest)}
        onOpenChange={(open) => {
          if (!open) {
            setReceiveManifest(null);
            setReceiveOrderCode('');
            setReceivedOrderCodes([]);
          }
        }}
      >
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
          <DialogHeader>
            <DialogTitle>Receive manifest at hub</DialogTitle>
            <DialogDescription>
              Scan received order codes, then confirm inbound for the scanned
              orders.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='grid gap-3 sm:grid-cols-4'>
              <DetailItem
                label='Manifest'
                value={activeReceiveManifest?.manifestCode || '--'}
              />
              <DetailItem
                label='Post office'
                value={activeReceiveManifest?.originPostOfficeCode || '--'}
              />
              <DetailItem
                label='Seal'
                value={activeReceiveManifest?.sealCode || '--'}
              />
              <DetailItem
                label='Scanned in'
                value={`${receivedOrderCodes.length}/${getTotalOrders(
                  activeReceiveManifest
                )}`}
              />
            </div>

            <div className='flex flex-col gap-2 sm:flex-row'>
              <Input
                value={receiveOrderCode}
                onChange={(event) => setReceiveOrderCode(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault();
                    handleScanInboundOrder();
                  }
                }}
                placeholder='Scan or enter received order code'
              />
              <Button
                variant='outline'
                onClick={() => handleScanInboundOrder()}
              >
                <ScanLine className='mr-2 h-4 w-4' />
                Scan received
              </Button>
            </div>

            {isFetchingReceiveManifest ? (
              <p className='text-sm text-muted-foreground'>
                Loading manifest orders...
              </p>
            ) : (
              <ManifestOrdersTable
                actionMode='RECEIVE'
                isActionLoading={isConfirmingInbound}
                orders={receiveOrders}
                scannedOrderCodes={receivedOrderCodes}
                onScan={handleScanInboundOrder}
              />
            )}
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={() => setReceiveManifest(null)}>
              Cancel
            </Button>
            <Button
              disabled={receivedOrderCodes.length === 0 || isConfirmingInbound}
              onClick={() => void handleConfirmInbound()}
            >
              {isConfirmingInbound ? 'Confirming...' : 'Confirm inbound'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={Boolean(detailTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDetailTarget(null);
          }
        }}
      >
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
          <DialogHeader>
            <DialogTitle>Handover manifest detail</DialogTitle>
            <DialogDescription>
              Manifest summary, timestamps, and order scan history.
            </DialogDescription>
          </DialogHeader>

          {detailManifest ? (
            <div className='space-y-4'>
              <div className='grid gap-3 sm:grid-cols-3'>
                <DetailItem
                  label='Manifest'
                  value={detailManifest.manifestCode || `#${detailManifest.id}`}
                />
                <DetailItem
                  label='Status'
                  value={
                    detailManifest.status ? (
                      <Badge
                        variant={getStatusBadgeVariant(detailManifest.status)}
                      >
                        {MANIFEST_STATUS_LABELS[detailManifest.status]}
                      </Badge>
                    ) : (
                      '--'
                    )
                  }
                />
                <DetailItem
                  label='Target hub'
                  value={resolveHubLabel(detailManifest.targetHubId)}
                />
                <DetailItem
                  label='Origin post office'
                  value={resolvePostOfficeLabel(
                    detailManifest.originPostOfficeId,
                    detailManifest.originPostOfficeCode
                  )}
                />
                <DetailItem
                  label='Scan out'
                  value={`${getScannedOutOrders(detailManifest)}/${getTotalOrders(
                    detailManifest
                  )}`}
                />
                <DetailItem
                  label='Scan in'
                  value={`${getScannedInOrders(detailManifest)}/${getTotalOrders(
                    detailManifest
                  )}`}
                />
                <DetailItem
                  label='Seal'
                  value={detailManifest.sealCode || '--'}
                />
                <DetailItem
                  label='Dispatched'
                  value={formatDateTime(detailManifest.dispatchedAt)}
                />
                <DetailItem
                  label='Inbound confirmed'
                  value={formatDateTime(detailManifest.inboundConfirmedAt)}
                />
              </div>

              {detailManifest.note ? (
                <div className='space-y-1 rounded-md border p-3'>
                  <p className='text-xs font-medium uppercase text-muted-foreground'>
                    Note
                  </p>
                  <p className='text-sm'>{detailManifest.note}</p>
                </div>
              ) : null}

              {isFetchingDetail ? (
                <p className='text-sm text-muted-foreground'>
                  Loading detail...
                </p>
              ) : (
                <ManifestOrdersTable orders={detailManifest.orders ?? []} />
              )}
            </div>
          ) : null}

          <DialogFooter>
            <Button variant='outline' onClick={() => setDetailTarget(null)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
