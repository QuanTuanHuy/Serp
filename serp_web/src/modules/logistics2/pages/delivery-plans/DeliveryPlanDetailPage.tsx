/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 delivery plan detail page
*/

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  AlertCircle,
  ArrowLeft,
  CalendarDays,
  CheckCircle2,
  Loader2,
  MapPinned,
  PencilLine,
  Plus,
  Search,
  X,
  Trash2,
  Undo2,
} from 'lucide-react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  Input,
  Label,
  ScrollArea,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDateVN, formatDateTimeVN } from '@/shared/utils/format';
import { getErrorMessage } from '@/lib/store/api';
import { useNotification } from '@/shared/hooks';
import {
  useDeleteDeliveryPlanMutation,
  useGetFacilityDetailQuery,
  useGetDeliveryPlanDetailQuery,
  useGetDeliverySlipsQuery,
  useGetRoutesQuery,
  useGetVehicleShippersQuery,
  useOptimizeDeliveryPlanMutation,
  useRollbackDeliveryPlanMutation,
  useUpdateDeliveryPlanMutation,
} from '../../api/logistics2Api';
import { RouteCard } from '../../components/cards/RouteCard';
import { StatsCard } from '../../components/cards/StatsCard';
import type {
  DeliverySlipStatus,
  Facility,
  PlanOptimizationStatus,
  Route,
  UpdateDeliveryPlanRequestBody,
  VehicleShipperStatus,
} from '../../types';

const PLAN_STATUS_META: Record<
  PlanOptimizationStatus,
  { label: string; badgeClass: string; icon: React.ElementType }
> = {
  DRAFT: {
    label: 'Nháp',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-200',
    icon: PencilLine,
  },
  OPTIMIZING: {
    label: 'Đang tối ưu',
    badgeClass: 'bg-amber-100 text-amber-700 border-amber-200',
    icon: MapPinned,
  },
  COMPLETED: {
    label: 'Đã tối ưu',
    badgeClass: 'bg-emerald-100 text-emerald-700 border-emerald-200',
    icon: CheckCircle2,
  },
  FAILED: {
    label: 'Tối ưu thất bại',
    badgeClass: 'bg-rose-100 text-rose-700 border-rose-200',
    icon: AlertCircle,
  },
};

const SLIP_STATUS_META: Record<
  DeliverySlipStatus,
  { label: string; badgeClass: string }
> = {
  PENDING: {
    label: 'Chờ xử lý',
    badgeClass: 'bg-slate-100 text-slate-700 border-slate-200',
  },
  ASSIGNED: {
    label: 'Đã lên kế hoạch',
    badgeClass: 'bg-blue-100 text-blue-700 border-blue-200',
  },
  DELIVERING: {
    label: 'Đang giao',
    badgeClass: 'bg-amber-100 text-amber-700 border-amber-200',
  },
  DELIVERED: {
    label: 'Đã giao',
    badgeClass: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  },
  RECALLING: {
    label: 'Thu hồi',
    badgeClass: 'bg-rose-100 text-rose-700 border-rose-200',
  },
};

const VEHICLE_STATUS_META: Record<
  VehicleShipperStatus,
  { label: string; badgeClass: string }
> = {
  ACTIVE: {
    label: 'Đang hoạt động',
    badgeClass: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  },
  INACTIVATE_REQUESTED: {
    label: 'Chờ hủy',
    badgeClass: 'bg-amber-100 text-amber-700 border-amber-200',
  },
  CANCELED: {
    label: 'Đã hủy',
    badgeClass: 'bg-rose-100 text-rose-700 border-rose-200',
  },
};

const formatVehicleType = (vehicleType?: string) => {
  if (!vehicleType) return 'Chưa xác định';

  switch (vehicleType.toUpperCase()) {
    case 'MOTORBIKE':
      return 'Xe máy';
    case 'VAN_500KG':
      return 'Xe van 500kg';
    case 'VAN_1000KG':
      return 'Xe van 1 tấn';
    case 'LIGHT_TRUCK_1T':
      return 'Xe tải 1 tấn';
    case 'LIGHT_TRUCK_2T':
      return 'Xe tải 2 tấn';
    default:
      return vehicleType;
  }
};

const formatNumber = (value?: number) =>
  new Intl.NumberFormat('vi-VN').format(value ?? 0);

const buildFallbackFacility = (facilityId?: string): Facility => ({
  id: facilityId || 'UNKNOWN',
  name: facilityId || 'Không xác định',
  statusId: 'ACTIVE',
  currentAddressId: '',
  createdStamp: '',
  lastUpdatedStamp: '',
  isDefault: false,
  tenantId: 0,
});

type DetailTab = 'details' | 'routes';

export const DeliveryPlanDetailPage: React.FC<{ planId: string }> = ({
  planId,
}) => {
  const router = useRouter();
  const notification = useNotification();
  const [activeTab, setActiveTab] = useState<DetailTab>('details');
  const [searchSlipValue, setSearchSlipValue] = useState('');
  const [searchVehicleValue, setSearchVehicleValue] = useState('');
  const [selectedSlipIds, setSelectedSlipIds] = useState<string[]>([]);
  const [selectedVehicleShipperIds, setSelectedVehicleShipperIds] = useState<
    string[]
  >([]);
  const [showAddSlipDialog, setShowAddSlipDialog] = useState(false);
  const [showAddVehicleDialog, setShowAddVehicleDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const {
    data: planResponse,
    isLoading,
    isError,
    error,
    refetch,
  } = useGetDeliveryPlanDetailQuery(planId);

  const plan = planResponse?.data;

  const { data: facilityResponse } = useGetFacilityDetailQuery(
    plan?.facilityId ?? '',
    {
      skip: !plan?.facilityId,
    }
  );

  const isDraft = plan?.optimizationStatus === 'DRAFT';
  const isFailed = plan?.optimizationStatus === 'FAILED';

  const {
    data: slipsResponse,
    isLoading: isLoadingSlips,
    isFetching: isFetchingSlips,
  } = useGetDeliverySlipsQuery(
    isDraft && plan?.facilityId
      ? {
          filters: {
            facilityId: plan.facilityId,
          },
          pagination: { page: 0, size: 200 },
        }
      : undefined,
    {
      skip: !isDraft || !plan?.facilityId,
    }
  );

  const {
    data: vehicleShippersResponse,
    isLoading: isLoadingVehicleShippers,
    isFetching: isFetchingVehicleShippers,
  } = useGetVehicleShippersQuery(
    isDraft && plan?.deliveryDate
      ? {
          filters: {
            workingDate: plan.deliveryDate,
          },
          pagination: { page: 0, size: 200 },
        }
      : undefined,
    {
      skip: !isDraft || !plan?.deliveryDate,
    }
  );

  const {
    data: routesResponse,
    isLoading: isLoadingRoutes,
    isFetching: isFetchingRoutes,
  } = useGetRoutesQuery(
    plan?.optimizationStatus === 'COMPLETED'
      ? {
          filters: {
            deliveryPlanId: planId,
          },
          pagination: { page: 0, size: 50 },
        }
      : undefined,
    {
      skip: plan?.optimizationStatus !== 'COMPLETED',
    }
  );

  const [updateDeliveryPlan, { isLoading: isUpdating }] =
    useUpdateDeliveryPlanMutation();
  const [optimizeDeliveryPlan, { isLoading: isOptimizing }] =
    useOptimizeDeliveryPlanMutation();
  const [rollbackDeliveryPlan, { isLoading: isRollingBack }] =
    useRollbackDeliveryPlanMutation();
  const [deleteDeliveryPlan, { isLoading: isDeleting }] =
    useDeleteDeliveryPlanMutation();

  useEffect(() => {
    if (!plan) return;
    setSelectedSlipIds(plan.slips?.map((slip) => slip.id) || []);
    setSelectedVehicleShipperIds(
      plan.vehicleShippers?.map((shipper) => shipper.id) || []
    );
  }, [plan?.id]);

  const facilityData =
    facilityResponse?.data || buildFallbackFacility(plan?.facilityId);

  const allSlips = useMemo(() => {
    const planSlips = plan?.slips || [];
    const querySlips = slipsResponse?.data?.items || [];
    const slipMap = new Map<string, (typeof planSlips)[number]>();

    [...planSlips, ...querySlips].forEach((slip) => {
      slipMap.set(slip.id, slip);
    });

    return Array.from(slipMap.values());
  }, [plan?.slips, slipsResponse?.data?.items]);

  const selectedSlips = useMemo(
    () => allSlips.filter((slip) => selectedSlipIds.includes(slip.id)),
    [allSlips, selectedSlipIds]
  );

  const availableSlips = useMemo(() => {
    const keyword = searchSlipValue.trim().toLowerCase();

    return allSlips.filter((slip) => {
      if (slip.status !== 'PENDING') return false;
      if (selectedSlipIds.includes(slip.id)) return false;
      if (!keyword) return true;

      const slipCode = slip.code || '';
      const customerName = slip.customerName || '';

      return (
        slipCode.toLowerCase().includes(keyword) ||
        customerName.toLowerCase().includes(keyword)
      );
    });
  }, [allSlips, searchSlipValue, selectedSlipIds]);

  const allVehicleShippers = useMemo(() => {
    const planVehicleShippers = plan?.vehicleShippers || [];
    const queryVehicleShippers = vehicleShippersResponse?.data?.items || [];
    const shipperMap = new Map<string, (typeof planVehicleShippers)[number]>();

    [...planVehicleShippers, ...queryVehicleShippers].forEach((shipper) => {
      shipperMap.set(shipper.id, shipper);
    });

    return Array.from(shipperMap.values());
  }, [plan?.vehicleShippers, vehicleShippersResponse?.data?.items]);

  const selectedVehicleShippers = useMemo(
    () =>
      allVehicleShippers.filter((shipper) =>
        selectedVehicleShipperIds.includes(shipper.id)
      ),
    [allVehicleShippers, selectedVehicleShipperIds]
  );

  const availableVehicleShippers = useMemo(() => {
    const keyword = searchVehicleValue.trim().toLowerCase();

    return allVehicleShippers.filter((shipper) => {
      if (shipper.status !== 'ACTIVE') return false;
      if (selectedVehicleShipperIds.includes(shipper.id)) return false;
      if (!keyword) return true;

      const licensePlate = shipper.vehicle?.licensePlate || '';
      const vehicleId = shipper.vehicleId || '';

      return (
        licensePlate.toLowerCase().includes(keyword) ||
        vehicleId.toLowerCase().includes(keyword)
      );
    });
  }, [allVehicleShippers, searchVehicleValue, selectedVehicleShipperIds]);

  const initialSlipIds = useMemo(
    () => plan?.slips?.map((slip) => slip.id) || [],
    [plan?.slips]
  );

  const initialVehicleShipperIds = useMemo(
    () => plan?.vehicleShippers?.map((shipper) => shipper.id) || [],
    [plan?.vehicleShippers]
  );

  const addedSlipIds = useMemo(() => {
    const initialSet = new Set(initialSlipIds);
    return selectedSlipIds.filter((id) => !initialSet.has(id));
  }, [initialSlipIds, selectedSlipIds]);

  const removedSlipIds = useMemo(
    () => initialSlipIds.filter((id) => !selectedSlipIds.includes(id)),
    [initialSlipIds, selectedSlipIds]
  );

  const addedVehicleShipperIds = useMemo(() => {
    const initialSet = new Set(initialVehicleShipperIds);
    return selectedVehicleShipperIds.filter((id) => !initialSet.has(id));
  }, [initialVehicleShipperIds, selectedVehicleShipperIds]);

  const removedVehicleShipperIds = useMemo(
    () =>
      initialVehicleShipperIds.filter(
        (id) => !selectedVehicleShipperIds.includes(id)
      ),
    [initialVehicleShipperIds, selectedVehicleShipperIds]
  );

  const hasPendingChanges =
    addedSlipIds.length > 0 ||
    removedSlipIds.length > 0 ||
    addedVehicleShipperIds.length > 0 ||
    removedVehicleShipperIds.length > 0;

  const isFetchingData =
    isFetchingSlips || isFetchingVehicleShippers || isFetchingRoutes;

  const toggleSlip = (slipId: string) => {
    setSelectedSlipIds((prev) =>
      prev.includes(slipId)
        ? prev.filter((id) => id !== slipId)
        : [...prev, slipId]
    );
  };

  const toggleVehicleShipper = (shipperId: string) => {
    setSelectedVehicleShipperIds((prev) =>
      prev.includes(shipperId)
        ? prev.filter((id) => id !== shipperId)
        : [...prev, shipperId]
    );
  };

  const handleResetChanges = () => {
    setSelectedSlipIds(initialSlipIds);
    setSelectedVehicleShipperIds(initialVehicleShipperIds);
  };

  const handleUpdatePlan = async () => {
    if (!plan || !hasPendingChanges) return;

    const payload: UpdateDeliveryPlanRequestBody = {
      ...(addedSlipIds.length
        ? { additionalDeliverySlipIds: addedSlipIds }
        : {}),
      ...(removedSlipIds.length
        ? { removedDeliverySlipIds: removedSlipIds }
        : {}),
      ...(addedVehicleShipperIds.length
        ? { additionalVehicleShipperIds: addedVehicleShipperIds }
        : {}),
      ...(removedVehicleShipperIds.length
        ? { removedVehicleShipperIds: removedVehicleShipperIds }
        : {}),
    };

    try {
      await updateDeliveryPlan({ planId: plan.id, data: payload }).unwrap();
      notification.success('Đã cập nhật kế hoạch', {
        description: 'Danh sách phiếu giao và xe giao đã được cập nhật.',
      });
      await refetch();
    } catch (updateError) {
      notification.error('Không thể cập nhật kế hoạch', {
        description: getErrorMessage(updateError),
      });
    }
  };

  const handleOptimizePlan = async () => {
    if (!plan) return;

    try {
      await optimizeDeliveryPlan(plan.id).unwrap();
      notification.success('Đang tối ưu lộ trình', {
        description: 'Hệ thống đang tính toán lộ trình cho kế hoạch.',
      });
      await refetch();
    } catch (optimizeError) {
      notification.error('Không thể tối ưu lộ trình', {
        description: getErrorMessage(optimizeError),
      });
    }
  };

  const handleRollbackPlan = async () => {
    if (!plan) return;

    try {
      await rollbackDeliveryPlan(plan.id).unwrap();
      notification.success('Đang rollback lộ trình', {
        description: 'Hệ thống đang rollback lộ trình cho kế hoạch.',
      });
      await refetch();
    } catch (rollbackError) {
      notification.error('Không thể rollback lộ trình', {
        description: getErrorMessage(rollbackError),
      });
    }
  };

  const handleDeletePlan = async () => {
    if (!plan) return;

    try {
      await deleteDeliveryPlan(plan.id).unwrap();
      notification.success('Đã xóa kế hoạch', {
        description: 'Kế hoạch giao hàng đã được xóa khỏi hệ thống.',
      });
      router.push('/logistics2/delivery-plans');
    } catch (deleteError) {
      notification.error('Không thể xóa kế hoạch', {
        description: getErrorMessage(deleteError),
      });
    }
  };

  const handleOpenRouteDetail = (routeId: string) => {
    router.push(`/logistics2/routes/${routeId}`);
  };

  const renderSlipCard = (slip: (typeof allSlips)[number]) => {
    const statusMeta =
      SLIP_STATUS_META[slip.status] || SLIP_STATUS_META.PENDING;

    return (
      <div
        key={slip.id}
        role='button'
        tabIndex={0}
        onClick={() => router.push(`/logistics2/delivery-slips/${slip.id}`)}
        onKeyDown={(event) => {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            router.push(`/logistics2/delivery-slips/${slip.id}`);
          }
        }}
        className='group relative rounded-xl border bg-card p-4 pr-12 text-left transition hover:border-primary/40 hover:bg-muted/30'
      >
        {isDraft && (
          <Button
            type='button'
            variant='ghost'
            size='icon'
            className='absolute right-2 top-2 h-8 w-8 opacity-70 transition hover:opacity-100'
            onClick={(event) => {
              event.stopPropagation();
              toggleSlip(slip.id);
            }}
          >
            <X className='h-4 w-4' />
            <span className='sr-only'>Loại bỏ đơn hàng</span>
          </Button>
        )}

        <div className='flex flex-wrap items-start gap-3'>
          <div className='flex-1 space-y-1'>
            <div className='flex flex-wrap items-center gap-2'>
              <p className='text-sm font-semibold'>
                {slip.code || `Phiếu #${slip.id.slice(0, 6)}`}
              </p>
              <Badge variant='outline' className={statusMeta.badgeClass}>
                {statusMeta.label}
              </Badge>
            </div>
            <p className='text-xs text-muted-foreground'>
              {slip.customerName || 'Chưa có khách hàng'}
              {slip.totalWeightKg
                ? ` · ${formatNumber(slip.totalWeightKg)} kg`
                : ''}
              {slip.totalVolumeCbm
                ? ` · ${formatNumber(slip.totalVolumeCbm)} cbm`
                : ''}
            </p>
          </div>
        </div>
      </div>
    );
  };

  const renderVehicleShipperCard = (
    shipper: (typeof allVehicleShippers)[number]
  ) => {
    const statusMeta =
      VEHICLE_STATUS_META[shipper.status] || VEHICLE_STATUS_META.ACTIVE;

    return (
      <div
        key={shipper.id}
        className='group relative rounded-xl border bg-card p-4 pr-12 transition hover:border-primary/40 hover:bg-muted/30'
      >
        {isDraft && (
          <Button
            type='button'
            variant='ghost'
            size='icon'
            className='absolute right-2 top-2 h-8 w-8 opacity-70 transition hover:opacity-100'
            onClick={() => toggleVehicleShipper(shipper.id)}
          >
            <X className='h-4 w-4' />
            <span className='sr-only'>Loại bỏ xe giao</span>
          </Button>
        )}

        <div className='space-y-2'>
          <div className='flex flex-wrap items-center gap-2'>
            <p className='text-sm font-semibold'>
              {shipper.vehicle?.licensePlate || shipper.vehicleId}
            </p>
            <Badge variant='outline' className={statusMeta.badgeClass}>
              {statusMeta.label}
            </Badge>
          </div>
          <p className='text-xs text-muted-foreground'>
            {formatVehicleType(shipper.vehicle?.vehicleType)}
            {shipper.workingDate
              ? ` · ${formatDateVN(shipper.workingDate)}`
              : ''}
          </p>
        </div>
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse space-y-3'>
          <div className='h-12 w-1/2 rounded bg-muted' />
          <div className='h-40 rounded bg-muted' />
          <div className='h-64 rounded bg-muted' />
        </div>
      </div>
    );
  }

  if (isError || !plan) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='space-y-4 p-8 text-center'>
          <AlertCircle className='mx-auto h-12 w-12 text-destructive' />
          <h3 className='text-lg font-semibold'>
            Không tìm thấy kế hoạch giao
          </h3>
          <p className='text-muted-foreground'>
            {getErrorMessage(error) || 'Kế hoạch không tồn tại hoặc đã bị xóa.'}
          </p>
          <Button onClick={() => router.push('/logistics2/delivery-plans')}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lại danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  const statusMeta =
    PLAN_STATUS_META[plan.optimizationStatus] || PLAN_STATUS_META.DRAFT;
  const StatusIcon = statusMeta.icon;
  const facility = plan.facility || buildFallbackFacility(plan.facilityId);
  const routes = routesResponse?.data?.items || [];

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 md:flex-row md:items-start md:justify-between'>
        <div className='flex items-start gap-3'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics2/delivery-plans')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>

          <div>
            <div className='mb-2 flex flex-wrap items-center gap-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {plan.planCode || `Kế hoạch #${plan.id.slice(0, 8)}`}
              </h1>
              <Badge variant='outline' className={statusMeta.badgeClass}>
                <StatusIcon className='mr-1 h-3.5 w-3.5' />
                {statusMeta.label}
              </Badge>
            </div>
            <p className='text-sm text-muted-foreground'>
              ID: {plan.id} · Tạo ngày {formatDateTimeVN(plan.createdStamp)}
            </p>
          </div>
        </div>

        {isDraft && (
          <div className='flex flex-wrap items-center gap-2'>
            <Button
              variant='destructive'
              onClick={() => setShowDeleteDialog(true)}
              disabled={isDeleting}
              className='gap-2'
            >
              <Trash2 className='h-4 w-4' />
              Xóa kế hoạch
            </Button>
            <Button
              onClick={handleOptimizePlan}
              disabled={isOptimizing}
              className='gap-2'
            >
              {isOptimizing ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : (
                <MapPinned className='h-4 w-4' />
              )}
              Tính toán lộ trình
            </Button>
          </div>
        )}

        {isFailed && (
          <Button
            onClick={handleRollbackPlan}
            disabled={isRollingBack}
            className='gap-2'
          >
            {isRollingBack ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <Undo2 className='h-4 w-4' />
            )}
            Rollback
          </Button>
        )}
      </div>

      <div className='grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4'>
        <StatsCard
          title='Phiếu giao'
          value={plan.totalSlips}
          icon={CalendarDays}
          variant='primary'
        />
        <StatsCard
          title='Xe giao'
          value={plan.totalVehicles}
          icon={CalendarDays}
          variant='success'
        />
        <StatsCard
          title='Ngày giao'
          value={formatDateVN(plan.deliveryDate)}
          icon={CalendarDays}
          variant='warning'
        />
        <StatsCard
          title='Trạng thái'
          value={statusMeta.label}
          icon={StatusIcon}
          variant='default'
        />
      </div>

      <Tabs
        value={activeTab}
        onValueChange={(value) => setActiveTab(value as DetailTab)}
      >
        <TabsList>
          <TabsTrigger value='details'>Chi tiết kế hoạch</TabsTrigger>
          <TabsTrigger value='routes'>Lộ trình ({routes.length})</TabsTrigger>
        </TabsList>

        <TabsContent value='details' className='mt-6 space-y-6'>
          <div className='grid gap-6 lg:grid-cols-3'>
            <Card className='lg:col-span-2'>
              <CardHeader>
                <h3 className='font-semibold'>Thông tin tổng quan</h3>
              </CardHeader>
              <CardContent className='grid gap-4 sm:grid-cols-2'>
                <div>
                  <Label className='text-muted-foreground'>Kho giao</Label>
                  <p className='font-medium'>{facilityData.name}</p>
                </div>
                <div>
                  <Label className='text-muted-foreground'>Ngày giao</Label>
                  <p className='font-medium'>
                    {formatDateVN(plan.deliveryDate)}
                  </p>
                </div>
                <div>
                  <Label className='text-muted-foreground'>Địa chỉ kho</Label>
                  <p className='font-medium'>
                    {facilityData.address?.fullAddress || 'Chưa có địa chỉ'}
                  </p>
                </div>
                <div>
                  <Label className='text-muted-foreground'>
                    Cập nhật lần cuối
                  </Label>
                  <p className='font-medium'>
                    {formatDateTimeVN(plan.lastUpdatedStamp)}
                  </p>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <h3 className='font-semibold'>Tóm tắt kế hoạch</h3>
              </CardHeader>
              <CardContent className='space-y-3 text-sm'>
                <div className='flex items-center justify-between'>
                  <span className='text-muted-foreground'>Số đơn hàng</span>
                  <span className='font-medium'>
                    {formatNumber(plan.totalSlips)}
                  </span>
                </div>
                <div className='flex items-center justify-between'>
                  <span className='text-muted-foreground'>Số xe giao</span>
                  <span className='font-medium'>
                    {formatNumber(plan.totalVehicles)}
                  </span>
                </div>
                <div className='flex items-center justify-between'>
                  <span className='text-muted-foreground'>Số lộ trình</span>
                  <span className='font-medium'>{routes.length}</span>
                </div>
                <div className='flex items-center justify-between'>
                  <span className='text-muted-foreground'>Trạng thái</span>
                  <Badge variant='outline' className={statusMeta.badgeClass}>
                    {statusMeta.label}
                  </Badge>
                </div>
              </CardContent>
            </Card>
          </div>

          {isDraft && (
            <div className='flex flex-wrap items-center justify-between gap-2'>
              <div className='text-sm text-muted-foreground'>
                Cập nhật danh sách phiếu giao và xe giao trước khi tối ưu.
              </div>
              <div className='flex flex-wrap items-center gap-2'>
                <Button
                  variant='outline'
                  onClick={handleResetChanges}
                  disabled={!hasPendingChanges || isUpdating}
                >
                  Hoàn tác
                </Button>
                <Button
                  onClick={handleUpdatePlan}
                  disabled={!hasPendingChanges || isUpdating}
                  className='gap-2'
                >
                  {isUpdating ? (
                    <Loader2 className='h-4 w-4 animate-spin' />
                  ) : (
                    <PencilLine className='h-4 w-4' />
                  )}
                  Lưu thay đổi
                </Button>
              </div>
            </div>
          )}

          <div className='grid gap-6 lg:grid-cols-2'>
            <Card>
              <CardHeader>
                <div className='flex flex-wrap items-center justify-between gap-2'>
                  <div>
                    <h3 className='font-semibold'>Đơn hàng trong kế hoạch</h3>
                    <p className='text-sm text-muted-foreground'>
                      {isDraft
                        ? 'Rà soát danh sách đơn hàng trước khi tối ưu.'
                        : 'Danh sách đơn hàng của kế hoạch.'}
                    </p>
                  </div>
                  <div className='flex items-center gap-2'>
                    <Badge variant='outline'>
                      {selectedSlips.length} đơn hàng
                    </Badge>
                    {isDraft && (
                      <Dialog
                        open={showAddSlipDialog}
                        onOpenChange={setShowAddSlipDialog}
                      >
                        <DialogTrigger asChild>
                          <Button
                            type='button'
                            variant='outline'
                            className='gap-2'
                          >
                            <Plus className='h-4 w-4' />
                            Thêm đơn hàng
                          </Button>
                        </DialogTrigger>
                        <DialogContent className='max-h-[85vh] sm:max-w-3xl'>
                          <DialogHeader>
                            <DialogTitle>
                              Thêm đơn hàng vào kế hoạch
                            </DialogTitle>
                            <DialogDescription>
                              Chọn các phiếu giao ở trạng thái chờ để đưa vào kế
                              hoạch.
                            </DialogDescription>
                          </DialogHeader>

                          <div className='space-y-4'>
                            <div className='relative'>
                              <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                              <Input
                                value={searchSlipValue}
                                placeholder='Tìm theo mã phiếu, khách hàng...'
                                onChange={(event) =>
                                  setSearchSlipValue(event.target.value)
                                }
                                className='pl-10'
                              />
                            </div>

                            <ScrollArea className='h-[50vh] rounded-lg border'>
                              <div className='space-y-2 p-3'>
                                {isLoadingSlips && (
                                  <div className='space-y-2'>
                                    {Array.from({ length: 4 }).map(
                                      (_, index) => (
                                        <div
                                          key={index}
                                          className='h-16 rounded-lg bg-muted/60 animate-pulse'
                                        />
                                      )
                                    )}
                                  </div>
                                )}

                                {!isLoadingSlips &&
                                  availableSlips.length === 0 && (
                                    <div className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                                      Không có phiếu giao phù hợp.
                                    </div>
                                  )}

                                {!isLoadingSlips &&
                                  availableSlips.map((slip) => {
                                    const isSelected = selectedSlipIds.includes(
                                      slip.id
                                    );
                                    const statusMeta =
                                      SLIP_STATUS_META[slip.status] ||
                                      SLIP_STATUS_META.PENDING;

                                    return (
                                      <label
                                        key={slip.id}
                                        className={cn(
                                          'flex w-full items-start gap-3 rounded-lg border px-3 py-2 transition cursor-pointer',
                                          isSelected
                                            ? 'border-primary/60 bg-primary/5'
                                            : 'hover:bg-muted/40'
                                        )}
                                      >
                                        <Checkbox
                                          checked={isSelected}
                                          onCheckedChange={() =>
                                            toggleSlip(slip.id)
                                          }
                                          className='mt-1'
                                        />
                                        <div className='flex-1 space-y-1'>
                                          <div className='flex flex-wrap items-center gap-2'>
                                            <p className='text-sm font-semibold'>
                                              {slip.code ||
                                                `Phiếu #${slip.id.slice(0, 6)}`}
                                            </p>
                                            <Badge
                                              variant='outline'
                                              className={statusMeta.badgeClass}
                                            >
                                              {statusMeta.label}
                                            </Badge>
                                          </div>
                                          <p className='text-xs text-muted-foreground'>
                                            {slip.customerName ||
                                              'Chưa có khách hàng'}
                                            {slip.totalWeightKg
                                              ? ` · ${formatNumber(slip.totalWeightKg)} kg`
                                              : ''}
                                            {slip.totalVolumeCbm
                                              ? ` · ${formatNumber(slip.totalVolumeCbm)} cbm`
                                              : ''}
                                          </p>
                                        </div>
                                      </label>
                                    );
                                  })}
                              </div>
                            </ScrollArea>
                          </div>
                        </DialogContent>
                      </Dialog>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='space-y-3'>
                  {selectedSlips.length === 0 && (
                    <p className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                      Chưa có đơn hàng nào trong kế hoạch.
                    </p>
                  )}

                  {selectedSlips.map((slip) => renderSlipCard(slip))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <div className='flex flex-wrap items-center justify-between gap-2'>
                  <div>
                    <h3 className='font-semibold'>Xe giao trong kế hoạch</h3>
                    <p className='text-sm text-muted-foreground'>
                      {isDraft
                        ? 'Rà soát danh sách xe giao trước khi tối ưu.'
                        : 'Danh sách xe đã phân công.'}
                    </p>
                  </div>
                  <div className='flex items-center gap-2'>
                    <Badge variant='outline'>
                      {selectedVehicleShippers.length} xe
                    </Badge>
                    {isDraft && (
                      <Dialog
                        open={showAddVehicleDialog}
                        onOpenChange={setShowAddVehicleDialog}
                      >
                        <DialogTrigger asChild>
                          <Button
                            type='button'
                            variant='outline'
                            className='gap-2'
                          >
                            <Plus className='h-4 w-4' />
                            Thêm xe giao
                          </Button>
                        </DialogTrigger>
                        <DialogContent className='max-h-[85vh] sm:max-w-3xl'>
                          <DialogHeader>
                            <DialogTitle>Thêm xe giao vào kế hoạch</DialogTitle>
                            <DialogDescription>
                              Chọn các xe đang hoạt động để phân công cho ngày
                              giao.
                            </DialogDescription>
                          </DialogHeader>

                          <div className='space-y-4'>
                            <div className='relative'>
                              <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                              <Input
                                value={searchVehicleValue}
                                placeholder='Tìm theo biển số, xe...'
                                onChange={(event) =>
                                  setSearchVehicleValue(event.target.value)
                                }
                                className='pl-10'
                              />
                            </div>

                            <ScrollArea className='h-[50vh] rounded-lg border'>
                              <div className='space-y-2 p-3'>
                                {isLoadingVehicleShippers && (
                                  <div className='space-y-2'>
                                    {Array.from({ length: 4 }).map(
                                      (_, index) => (
                                        <div
                                          key={index}
                                          className='h-16 rounded-lg bg-muted/60 animate-pulse'
                                        />
                                      )
                                    )}
                                  </div>
                                )}

                                {!isLoadingVehicleShippers &&
                                  availableVehicleShippers.length === 0 && (
                                    <div className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                                      Không có xe giao phù hợp.
                                    </div>
                                  )}

                                {!isLoadingVehicleShippers &&
                                  availableVehicleShippers.map((shipper) => {
                                    const isSelected =
                                      selectedVehicleShipperIds.includes(
                                        shipper.id
                                      );
                                    const statusMeta =
                                      VEHICLE_STATUS_META[shipper.status] ||
                                      VEHICLE_STATUS_META.ACTIVE;

                                    return (
                                      <label
                                        key={shipper.id}
                                        className={cn(
                                          'flex w-full items-start gap-3 rounded-lg border px-3 py-2 transition cursor-pointer',
                                          isSelected
                                            ? 'border-primary/60 bg-primary/5'
                                            : 'hover:bg-muted/40'
                                        )}
                                      >
                                        <Checkbox
                                          checked={isSelected}
                                          onCheckedChange={() =>
                                            toggleVehicleShipper(shipper.id)
                                          }
                                          className='mt-1'
                                        />
                                        <div className='flex-1 space-y-1'>
                                          <div className='flex flex-wrap items-center gap-2'>
                                            <p className='text-sm font-semibold'>
                                              {shipper.vehicle?.licensePlate ||
                                                shipper.vehicleId}
                                            </p>
                                            <Badge
                                              variant='outline'
                                              className={statusMeta.badgeClass}
                                            >
                                              {statusMeta.label}
                                            </Badge>
                                          </div>
                                          <p className='text-xs text-muted-foreground'>
                                            {formatVehicleType(
                                              shipper.vehicle?.vehicleType
                                            )}
                                            {shipper.workingDate
                                              ? ` · ${formatDateVN(shipper.workingDate)}`
                                              : ''}
                                          </p>
                                        </div>
                                      </label>
                                    );
                                  })}
                              </div>
                            </ScrollArea>
                          </div>
                        </DialogContent>
                      </Dialog>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='space-y-3'>
                  {selectedVehicleShippers.length === 0 && (
                    <p className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                      Chưa có xe giao nào trong kế hoạch.
                    </p>
                  )}

                  {selectedVehicleShippers.map((shipper) =>
                    renderVehicleShipperCard(shipper)
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          {isFetchingData && (
            <div className='flex items-center gap-2 text-xs text-muted-foreground'>
              <Loader2 className='h-3.5 w-3.5 animate-spin' />
              Đang cập nhật dữ liệu...
            </div>
          )}
        </TabsContent>

        <TabsContent value='routes' className='mt-6 space-y-6'>
          {plan.optimizationStatus !== 'COMPLETED' && (
            <Card className='border-amber-200 bg-amber-50/60'>
              <CardContent className='p-6 text-sm text-amber-800'>
                Lộ trình chỉ được tạo khi kế hoạch ở trạng thái "Đã tối ưu".
              </CardContent>
            </Card>
          )}

          {plan.optimizationStatus === 'COMPLETED' && (
            <div className='space-y-4'>
              {isLoadingRoutes && (
                <div className='grid gap-4 md:grid-cols-2'>
                  {Array.from({ length: 4 }).map((_, index) => (
                    <Card key={index} className='h-36 animate-pulse' />
                  ))}
                </div>
              )}

              {!isLoadingRoutes && routes.length === 0 && (
                <Card>
                  <CardContent className='p-6 text-center text-sm text-muted-foreground'>
                    Chưa có lộ trình nào được tạo.
                  </CardContent>
                </Card>
              )}

              {!isLoadingRoutes && routes.length > 0 && (
                <div className='flex flex-col gap-4'>
                  {routes.map((route: Route) => (
                    <RouteCard
                      key={route.id}
                      route={route}
                      onOpenDetail={handleOpenRouteDetail}
                    />
                  ))}
                </div>
              )}
            </div>
          )}
        </TabsContent>
      </Tabs>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa kế hoạch giao</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn xóa kế hoạch giao này? Hành động không thể
              hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeletePlan}
              disabled={isDeleting}
              className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
            >
              {isDeleting ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : (
                <Trash2 className='mr-2 h-4 w-4' />
              )}
              Xóa kế hoạch
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};
