'use client';

import { useEffect, useMemo, useState } from 'react';
import {
  Bike,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  MoreVertical,
  PauseCircle,
  Pencil,
  PlayCircle,
  Plus,
  RefreshCcw,
  Search,
  SlidersHorizontal,
  Truck,
  Wrench,
  X,
  type LucideIcon,
} from 'lucide-react';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Card,
  CardContent,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  Input,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { useNotification } from '@/shared/hooks';
import { cn } from '@/shared/utils';
import { formatDateVN } from '@/shared/utils/format';
import {
  useActivateVehicleMutation,
  useCreateVehicleMutation,
  useDeactivateVehicleMutation,
  useGetVehiclesQuery,
  useUpdateVehicleMutation,
} from '../../api/logistics2Api';
import {
  setActiveModule,
  setVehicleFilters,
  setVehiclePagination,
} from '../../store';
import {
  selectVehicleFilters,
  selectVehiclePagination,
} from '../../store/selectors';
import type { Vehicle, VehicleStatus, VehicleType } from '../../types';
import { StatsCard } from '../../components/cards/StatsCard';
import {
  VehicleFormDialog,
  type VehicleFormErrors,
  type VehicleFormMode,
  type VehicleFormValues,
  type VehicleTypeOption,
} from './VehicleFormDialog';
import {
  VehicleStatusDialog,
  type VehicleStatusAction,
} from './VehicleStatusDialog';

const DEFAULT_FORM_VALUES: VehicleFormValues = {
  licensePlate: '',
  vehicleType: '',
  maxWeightKg: '',
  maxVolumeCbm: '',
};

const VEHICLE_TYPE_OPTIONS: VehicleTypeOption[] = [
  {
    value: 'MOTORBIKE',
    label: 'Xe máy',
    description: 'Phù hợp giao hàng nhanh, nhẹ',
  },
  {
    value: 'VAN_500KG',
    label: 'Xe van 500kg',
    description: 'Xe van nhỏ cho lô hàng vừa và nhẹ',
  },
  {
    value: 'VAN_1000KG',
    label: 'Xe van 1000kg',
    description: 'Xe van trung bình cho hàng nặng',
  },
  {
    value: 'LIGHT_TRUCK_1T',
    label: 'Xe tải 1 tấn',
    description: 'Xe tải nhẹ, linh hoạt',
  },
  {
    value: 'LIGHT_TRUCK_2T',
    label: 'Xe tải 2 tấn',
    description: 'Xe tải nhẹ cho hàng lớn',
  },
];

const VEHICLE_TYPE_META: Record<
  VehicleType,
  {
    label: string;
    badgeClass: string;
    icon: LucideIcon;
    iconClass: string;
  }
> = {
  MOTORBIKE: {
    label: 'Xe máy',
    badgeClass: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    icon: Bike,
    iconClass: 'bg-emerald-100 text-emerald-600',
  },
  VAN_500KG: {
    label: 'Xe van 500kg',
    badgeClass: 'border-sky-200 bg-sky-50 text-sky-700',
    icon: Truck,
    iconClass: 'bg-sky-100 text-sky-600',
  },
  VAN_1000KG: {
    label: 'Xe van 1000kg',
    badgeClass: 'border-blue-200 bg-blue-50 text-blue-700',
    icon: Truck,
    iconClass: 'bg-blue-100 text-blue-600',
  },
  LIGHT_TRUCK_1T: {
    label: 'Xe tải 1 tấn',
    badgeClass: 'border-amber-200 bg-amber-50 text-amber-700',
    icon: Truck,
    iconClass: 'bg-amber-100 text-amber-600',
  },
  LIGHT_TRUCK_2T: {
    label: 'Xe tải 2 tấn',
    badgeClass: 'border-rose-200 bg-rose-50 text-rose-700',
    icon: Truck,
    iconClass: 'bg-rose-100 text-rose-600',
  },
};

const VEHICLE_STATUS_META: Record<
  VehicleStatus,
  {
    label: string;
    badgeClass: string;
  }
> = {
  IN_USE: {
    label: 'Đang hoạt động',
    badgeClass: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  },
  MAINTENANCE: {
    label: 'Đang bảo trì',
    badgeClass: 'border-amber-200 bg-amber-50 text-amber-700',
  },
};

const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '--';
  return new Intl.NumberFormat('vi-VN').format(value);
};

const mapVehicleToFormValues = (vehicle: Vehicle): VehicleFormValues => ({
  licensePlate: vehicle.licensePlate || '',
  vehicleType: vehicle.vehicleType || '',
  maxWeightKg:
    vehicle.maxWeightKg === undefined || vehicle.maxWeightKg === null
      ? ''
      : String(vehicle.maxWeightKg),
  maxVolumeCbm:
    vehicle.maxVolumeCbm === undefined || vehicle.maxVolumeCbm === null
      ? ''
      : String(vehicle.maxVolumeCbm),
});

export const VehicleListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const notification = useNotification();
  const filters = useAppSelector(selectVehicleFilters);
  const pagination = useAppSelector(selectVehiclePagination);

  const [searchValue, setSearchValue] = useState(filters.query || '');
  const [showFilters, setShowFilters] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<VehicleFormMode>('create');
  const [formValues, setFormValues] =
    useState<VehicleFormValues>(DEFAULT_FORM_VALUES);
  const [formErrors, setFormErrors] = useState<VehicleFormErrors>({});
  const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
  const [statusDialog, setStatusDialog] = useState<{
    open: boolean;
    action: VehicleStatusAction;
    vehicle: Vehicle | null;
  }>({
    open: false,
    action: 'activate',
    vehicle: null,
  });

  useEffect(() => {
    dispatch(setActiveModule('vehicles'));
  }, [dispatch]);

  useEffect(() => {
    setSearchValue(filters.query || '');
  }, [filters.query]);

  const { data, isLoading, error, refetch } = useGetVehiclesQuery({
    filters,
    pagination,
  });

  const [createVehicle, { isLoading: isCreating }] = useCreateVehicleMutation();
  const [updateVehicle, { isLoading: isUpdating }] = useUpdateVehicleMutation();
  const [activateVehicle, { isLoading: isActivating }] =
    useActivateVehicleMutation();
  const [deactivateVehicle, { isLoading: isDeactivating }] =
    useDeactivateVehicleMutation();

  const vehicles = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage ?? pagination.page ?? 0;
  const pageSize = pagination.size || 10;

  const stats = useMemo(() => {
    const inUse = vehicles.filter(
      (vehicle) => vehicle.status === 'IN_USE'
    ).length;
    const maintenance = vehicles.filter(
      (vehicle) => vehicle.status === 'MAINTENANCE'
    ).length;

    return {
      total: totalItems,
      inUse,
      maintenance,
    };
  }, [vehicles, totalItems]);

  const hasActiveFilters = Boolean(
    filters.query || filters.vehicleType || filters.vehicleStatus
  );

  const isSubmitting = isCreating || isUpdating;
  const isStatusUpdating = isActivating || isDeactivating;

  const handleSearch = () => {
    dispatch(
      setVehicleFilters({
        ...filters,
        query: searchValue.trim() || undefined,
      })
    );
  };

  const handleReset = () => {
    setSearchValue('');
    dispatch(setVehicleFilters({}));
  };

  const handlePageChange = (page: number) => {
    dispatch(setVehiclePagination({ ...pagination, page }));
  };

  const handleFormOpenChange = (open: boolean) => {
    setFormOpen(open);

    if (!open) {
      setFormErrors({});
      setFormValues(DEFAULT_FORM_VALUES);
      setEditingVehicle(null);
    }
  };

  const handleFormValueChange = <K extends keyof VehicleFormValues>(
    field: K,
    value: VehicleFormValues[K]
  ) => {
    setFormValues((prev) => ({
      ...prev,
      [field]: value,
    }));

    if (formErrors[field]) {
      setFormErrors((prev) => ({
        ...prev,
        [field]: undefined,
      }));
    }
  };

  const openCreateDialog = () => {
    setFormMode('create');
    setFormValues(DEFAULT_FORM_VALUES);
    setFormErrors({});
    setEditingVehicle(null);
    setFormOpen(true);
  };

  const openEditDialog = (vehicle: Vehicle) => {
    setFormMode('edit');
    setEditingVehicle(vehicle);
    setFormValues(mapVehicleToFormValues(vehicle));
    setFormErrors({});
    setFormOpen(true);
  };

  const validateForm = () => {
    const nextErrors: VehicleFormErrors = {};
    const licensePlate = formValues.licensePlate.trim();

    if (!licensePlate) {
      nextErrors.licensePlate = 'Vui long nhap bien so.';
    }

    if (!formValues.vehicleType) {
      nextErrors.vehicleType = 'Vui long chon loai xe.';
    }

    if (!formValues.maxWeightKg.trim()) {
      nextErrors.maxWeightKg = 'Vui long nhap tai trong.';
    } else {
      const parsedWeight = Number(formValues.maxWeightKg);
      if (!Number.isFinite(parsedWeight) || parsedWeight < 0) {
        nextErrors.maxWeightKg = 'Tai trong phai la so khong am.';
      }
    }

    if (!formValues.maxVolumeCbm.trim()) {
      nextErrors.maxVolumeCbm = 'Vui long nhap the tich.';
    } else {
      const parsedVolume = Number(formValues.maxVolumeCbm);
      if (!Number.isFinite(parsedVolume) || parsedVolume < 0) {
        nextErrors.maxVolumeCbm = 'The tich phai la so khong am.';
      }
    }

    setFormErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      return null;
    }

    return {
      licensePlate,
      vehicleType: formValues.vehicleType as VehicleType,
      maxWeightKg: Number(formValues.maxWeightKg),
      maxVolumeCbm: Number(formValues.maxVolumeCbm),
    };
  };

  const handleSubmitForm = async () => {
    const payload = validateForm();

    if (!payload) return;

    try {
      if (formMode === 'create') {
        await createVehicle(payload).unwrap();
        notification.success('Tạo phương tiện thành công', {
          description: `Biển số ${payload.licensePlate} đã được thêm.`,
        });
      } else if (editingVehicle) {
        await updateVehicle({
          vehicleId: editingVehicle.id,
          data: payload,
        }).unwrap();
        notification.success('Cập nhật phương tiện thành công', {
          description: `Biển số ${payload.licensePlate} đã được cập nhật.`,
        });
      }

      handleFormOpenChange(false);
    } catch (submitError) {
      notification.error('Không thể lưu phương tiện', {
        description: getErrorMessage(submitError),
      });
    }
  };

  const openStatusDialog = (vehicle: Vehicle) => {
    const action: VehicleStatusAction =
      vehicle.status === 'IN_USE' ? 'deactivate' : 'activate';
    setStatusDialog({
      open: true,
      action,
      vehicle,
    });
  };

  const handleStatusDialogChange = (open: boolean) => {
    setStatusDialog((prev) => ({
      ...prev,
      open,
      vehicle: open ? prev.vehicle : null,
    }));
  };

  const handleConfirmStatus = async () => {
    if (!statusDialog.vehicle) return;

    try {
      if (statusDialog.action === 'activate') {
        await activateVehicle(statusDialog.vehicle.id).unwrap();
        notification.success('Đã kích hoạt phương tiện', {
          description: `Biển số ${statusDialog.vehicle.licensePlate} đã sẵn sàng hoạt động.`,
        });
      } else {
        await deactivateVehicle(statusDialog.vehicle.id).unwrap();
        notification.success('Đã dừng hoạt động phương tiện', {
          description: `Biển số ${statusDialog.vehicle.licensePlate} đã dừng hoạt động.`,
        });
      }

      setStatusDialog({
        open: false,
        action: 'activate',
        vehicle: null,
      });
    } catch (submitError) {
      notification.error('Không thể cập nhật trạng thái', {
        description: getErrorMessage(submitError),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <Card className='relative overflow-hidden border-slate-200/80 bg-gradient-to-r from-slate-50 via-white to-emerald-50'>
        <CardContent className='p-6'>
          <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
            <div className='flex items-center gap-4'>
              <div className='flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-500 text-white shadow-lg shadow-emerald-500/30'>
                <Truck className='h-7 w-7' />
              </div>
              <div>
                <h1 className='text-2xl font-bold tracking-tight'>
                  Quản lý phương tiện
                </h1>
                <p className='text-muted-foreground'>
                  Theo dõi năng lực vận chuyển và trạng thái vận hành
                </p>
              </div>
            </div>

            <div className='flex flex-wrap items-center gap-2'>
              <Button
                variant='outline'
                onClick={() => refetch()}
                className='gap-2'
              >
                <RefreshCcw className='h-4 w-4' />
                Làm mới
              </Button>
              <Button onClick={openCreateDialog} className='gap-2'>
                <Plus className='h-4 w-4' />
                Tạo phương tiện
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className='grid grid-cols-1 gap-4 sm:grid-cols-3'>
        <StatsCard
          title='Tổng phương tiện'
          value={stats.total}
          icon={Truck}
          variant='default'
        />
        <StatsCard
          title='Đang hoạt động'
          value={stats.inUse}
          icon={CheckCircle2}
          variant='success'
        />
        <StatsCard
          title='Đang bảo trì'
          value={stats.maintenance}
          icon={Wrench}
          variant='warning'
        />
      </div>

      <div className='flex flex-col gap-3 lg:flex-row lg:items-center'>
        <div className='relative flex-1'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={searchValue}
            placeholder='Tìm theo biển số hoặc loại xe'
            onChange={(event) => setSearchValue(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                handleSearch();
              }
            }}
            className='pl-10 pr-10'
          />
          {searchValue && (
            <button
              type='button'
              className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
              onClick={() => {
                setSearchValue('');
                dispatch(
                  setVehicleFilters({
                    ...filters,
                    query: undefined,
                  })
                );
              }}
            >
              <X className='h-4 w-4' />
            </button>
          )}
        </div>

        <Button variant='secondary' onClick={handleSearch}>
          <Search className='mr-2 h-4 w-4' />
          Tìm kiếm
        </Button>

        <Button
          variant={showFilters ? 'secondary' : 'outline'}
          onClick={() => setShowFilters((prev) => !prev)}
          className='gap-2'
        >
          <SlidersHorizontal className='h-4 w-4' />
          Bộ lọc
          {hasActiveFilters && (
            <span className='h-2 w-2 rounded-full bg-primary' />
          )}
        </Button>
      </div>

      {showFilters && (
        <Card>
          <CardContent className='grid gap-4 p-4 sm:grid-cols-2'>
            <div>
              <label className='mb-1.5 block text-sm font-medium'>
                Loại xe
              </label>
              <select
                value={filters.vehicleType || ''}
                onChange={(event) => {
                  const nextType =
                    (event.target.value as VehicleType) || undefined;
                  dispatch(
                    setVehicleFilters({
                      ...filters,
                      vehicleType: nextType,
                    })
                  );
                }}
                className='w-full rounded-lg border bg-background px-3 py-2'
              >
                <option value=''>Tất cả loại</option>
                {VEHICLE_TYPE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className='mb-1.5 block text-sm font-medium'>
                Trạng thái
              </label>
              <select
                value={filters.vehicleStatus || ''}
                onChange={(event) => {
                  const nextStatus =
                    (event.target.value as VehicleStatus) || undefined;
                  dispatch(
                    setVehicleFilters({
                      ...filters,
                      vehicleStatus: nextStatus,
                    })
                  );
                }}
                className='w-full rounded-lg border bg-background px-3 py-2'
              >
                <option value=''>Tất cả trạng thái</option>
                {Object.entries(VEHICLE_STATUS_META).map(([value, meta]) => (
                  <option key={value} value={value}>
                    {meta.label}
                  </option>
                ))}
              </select>
            </div>

            {hasActiveFilters && (
              <div className='sm:col-span-2 flex items-center justify-between border-t pt-4'>
                <p className='text-sm text-muted-foreground'>
                  Tìm thấy {totalItems} phương tiện phù hợp
                </p>
                <Button variant='ghost' size='sm' onClick={handleReset}>
                  Xóa bộ lọc
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4 text-destructive'>
            Không thể tải danh sách: {getErrorMessage(error)}
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent className='p-0'>
          {isLoading ? (
            <div className='space-y-3 p-4'>
              {Array.from({ length: 6 }).map((_, index) => (
                <div
                  key={index}
                  className='rounded-xl border border-slate-200/80 bg-white p-4 shadow-sm animate-pulse'
                >
                  <div className='flex items-center gap-4'>
                    <div className='h-10 w-10 rounded-lg bg-muted' />
                    <div className='flex-1 space-y-2'>
                      <div className='h-4 w-1/3 rounded bg-muted' />
                      <div className='h-3 w-1/2 rounded bg-muted' />
                    </div>
                    <div className='h-8 w-20 rounded bg-muted' />
                  </div>
                </div>
              ))}
            </div>
          ) : vehicles.length > 0 ? (
            <>
              <div className='hidden md:block'>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className='min-w-[260px]'>
                        Phương tiện
                      </TableHead>
                      <TableHead className='min-w-[180px]'>Sức chứa</TableHead>
                      <TableHead className='min-w-[140px]'>
                        Trạng thái
                      </TableHead>
                      <TableHead className='min-w-[160px]'>Cập nhật</TableHead>
                      <TableHead className='text-right'>Hành động</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {vehicles.map((vehicle) => {
                      const typeMeta = VEHICLE_TYPE_META[vehicle.vehicleType];
                      const statusMeta = VEHICLE_STATUS_META[vehicle.status];
                      const TypeIcon = typeMeta.icon;

                      return (
                        <TableRow key={vehicle.id} className='group'>
                          <TableCell>
                            <div className='flex items-center gap-3'>
                              <div
                                className={cn(
                                  'flex h-10 w-10 items-center justify-center rounded-xl',
                                  typeMeta.iconClass
                                )}
                              >
                                <TypeIcon className='h-5 w-5' />
                              </div>
                              <div>
                                <p className='text-sm font-semibold text-slate-900'>
                                  {vehicle.licensePlate}
                                </p>
                                <div className='flex flex-wrap items-center gap-2 text-xs text-slate-500'>
                                  <Badge
                                    variant='outline'
                                    className={typeMeta.badgeClass}
                                  >
                                    {typeMeta.label}
                                  </Badge>
                                </div>
                              </div>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className='text-xs text-slate-600'>
                              <p className='font-medium text-slate-700'>
                                {formatNumber(vehicle.maxWeightKg)} kg
                              </p>
                              <p>{formatNumber(vehicle.maxVolumeCbm)} cbm</p>
                            </div>
                          </TableCell>
                          <TableCell>
                            <Badge
                              variant='outline'
                              className={statusMeta.badgeClass}
                            >
                              {statusMeta.label}
                            </Badge>
                          </TableCell>
                          <TableCell className='text-xs text-slate-600'>
                            {vehicle.lastUpdatedStamp
                              ? formatDateVN(vehicle.lastUpdatedStamp)
                              : '--'}
                          </TableCell>
                          <TableCell className='text-right'>
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button
                                  variant='ghost'
                                  size='icon'
                                  className='h-9 w-9 rounded-full'
                                  disabled={isSubmitting || isStatusUpdating}
                                >
                                  <MoreVertical className='h-4 w-4' />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align='end' className='w-48'>
                                <DropdownMenuItem
                                  className='flex items-center gap-2'
                                  onClick={() => openEditDialog(vehicle)}
                                >
                                  <Pencil className='h-4 w-4' />
                                  Chỉnh sửa
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                  className={cn(
                                    'flex items-center gap-2',
                                    vehicle.status === 'IN_USE'
                                      ? 'text-amber-600 focus:text-amber-700'
                                      : 'text-emerald-600 focus:text-emerald-700'
                                  )}
                                  onClick={() => openStatusDialog(vehicle)}
                                >
                                  {vehicle.status === 'IN_USE' ? (
                                    <PauseCircle className='h-4 w-4' />
                                  ) : (
                                    <PlayCircle className='h-4 w-4' />
                                  )}
                                  {vehicle.status === 'IN_USE'
                                    ? 'Dừng hoạt động'
                                    : 'Kích hoạt'}
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </div>

              <div className='space-y-3 p-4 md:hidden'>
                {vehicles.map((vehicle) => {
                  const typeMeta = VEHICLE_TYPE_META[vehicle.vehicleType];
                  const statusMeta = VEHICLE_STATUS_META[vehicle.status];
                  const TypeIcon = typeMeta.icon;

                  return (
                    <Card key={vehicle.id} className='border-slate-200/80'>
                      <CardContent className='space-y-4 p-4'>
                        <div className='flex items-start justify-between gap-3'>
                          <div className='flex items-center gap-3'>
                            <div
                              className={cn(
                                'flex h-10 w-10 items-center justify-center rounded-xl',
                                typeMeta.iconClass
                              )}
                            >
                              <TypeIcon className='h-5 w-5' />
                            </div>
                            <div>
                              <p className='text-sm font-semibold text-slate-900'>
                                {vehicle.licensePlate}
                              </p>
                              <p className='text-xs text-slate-500'>
                                {typeMeta.label}
                              </p>
                            </div>
                          </div>
                          <Badge
                            variant='outline'
                            className={statusMeta.badgeClass}
                          >
                            {statusMeta.label}
                          </Badge>
                        </div>

                        <div className='grid gap-2 text-xs text-slate-600 sm:grid-cols-2'>
                          <div>
                            <p className='font-medium text-slate-700'>
                              Tải trọng tối đa
                            </p>
                            <p>{formatNumber(vehicle.maxWeightKg)} kg</p>
                          </div>
                          <div>
                            <p className='font-medium text-slate-700'>
                              Thể tích tối đa
                            </p>
                            <p>{formatNumber(vehicle.maxVolumeCbm)} cbm</p>
                          </div>
                          <div>
                            <p className='font-medium text-slate-700'>
                              Cập nhật
                            </p>
                            <p>
                              {vehicle.lastUpdatedStamp
                                ? formatDateVN(vehicle.lastUpdatedStamp)
                                : '--'}
                            </p>
                          </div>
                          <div>
                            <p className='font-medium text-slate-700'>Ma xe</p>
                            <p>#{vehicle.id}</p>
                          </div>
                        </div>

                        <div className='flex flex-wrap items-center gap-2'>
                          <Button
                            variant='outline'
                            size='sm'
                            onClick={() => openEditDialog(vehicle)}
                            disabled={isSubmitting || isStatusUpdating}
                          >
                            <Pencil className='mr-2 h-4 w-4' />
                            Chỉnh sửa
                          </Button>
                          <Button
                            variant={
                              vehicle.status === 'IN_USE'
                                ? 'secondary'
                                : 'default'
                            }
                            size='sm'
                            onClick={() => openStatusDialog(vehicle)}
                            disabled={isSubmitting || isStatusUpdating}
                          >
                            {vehicle.status === 'IN_USE' ? (
                              <PauseCircle className='mr-2 h-4 w-4' />
                            ) : (
                              <PlayCircle className='mr-2 h-4 w-4' />
                            )}
                            {vehicle.status === 'IN_USE'
                              ? 'Dừng hoạt động'
                              : 'Kích hoạt'}
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </>
          ) : (
            <div className='flex flex-col items-center justify-center gap-3 px-6 py-14 text-center'>
              <div className='flex h-16 w-16 items-center justify-center rounded-full bg-muted'>
                <Truck className='h-8 w-8 text-muted-foreground' />
              </div>
              <h3 className='text-lg font-semibold'>Chua co phuong tien</h3>
              <p className='max-w-sm text-sm text-muted-foreground'>
                {hasActiveFilters
                  ? 'Thu dieu chinh bo loc de xem them ket qua.'
                  : 'Them phuong tien moi de bat dau quan ly danh sach.'}
              </p>
              {hasActiveFilters ? (
                <Button variant='outline' onClick={handleReset}>
                  Xoa bo loc
                </Button>
              ) : (
                <Button onClick={openCreateDialog}>
                  <Plus className='mr-2 h-4 w-4' />
                  Tao phuong tien
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {totalItems > pageSize && (
        <div className='flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between'>
          <p className='text-sm text-muted-foreground'>
            Hien thi {currentPage * pageSize + 1} den{' '}
            {Math.min((currentPage + 1) * pageSize, totalItems)} trong tong so{' '}
            {totalItems} phuong tien
          </p>
          <div className='flex items-center gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === 0}
              onClick={() => handlePageChange(currentPage - 1)}
            >
              <ChevronLeft className='h-4 w-4' />
              Truoc
            </Button>
            <div className='flex items-center gap-1'>
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                const pageNum = i;
                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={cn(
                      'h-8 w-8 rounded-md text-sm font-medium transition-colors',
                      currentPage === pageNum
                        ? 'bg-primary text-primary-foreground'
                        : 'hover:bg-muted'
                    )}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
            </div>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage >= totalPages - 1}
              onClick={() => handlePageChange(currentPage + 1)}
            >
              Tiep
              <ChevronRight className='h-4 w-4' />
            </Button>
          </div>
        </div>
      )}

      <VehicleFormDialog
        open={formOpen}
        mode={formMode}
        values={formValues}
        errors={formErrors}
        isSubmitting={isSubmitting}
        vehicleTypeOptions={VEHICLE_TYPE_OPTIONS}
        onOpenChange={handleFormOpenChange}
        onSubmit={handleSubmitForm}
        onValueChange={handleFormValueChange}
      />

      <VehicleStatusDialog
        open={statusDialog.open}
        action={statusDialog.action}
        vehicle={statusDialog.vehicle}
        isSubmitting={isStatusUpdating}
        onOpenChange={handleStatusDialogChange}
        onConfirm={handleConfirmStatus}
      />
    </div>
  );
};
