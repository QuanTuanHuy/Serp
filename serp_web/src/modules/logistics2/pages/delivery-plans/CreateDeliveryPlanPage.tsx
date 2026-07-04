'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, CheckCircle2, Loader2, Search, X } from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  Checkbox,
  Input,
  Label,
  ScrollArea,
} from '@/shared/components/ui';
import { Combobox } from '@/shared/components/ui/combobox';
import { cn } from '@/shared/utils';
import { formatDateVN } from '@/shared/utils/format';
import { getErrorMessage } from '@/lib/store/api';
import { useNotification } from '@/shared/hooks';
import {
  useCreateDeliveryPlanMutation,
  useGetDeliverySlipsQuery,
  useGetFacilitiesQuery,
  useGetVehicleShippersQuery,
} from '../../api/logistics2Api';
import type {
  DeliverySlip,
  DeliverySlipStatus,
  Facility,
  VehicleShipper,
  VehicleShipperStatus,
} from '../../types';

const SLIP_STATUS_META: Record<
  DeliverySlipStatus,
  { label: string; badgeClass: string }
> = {
  PENDING: {
    label: 'Chờ lên kế hoạch',
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

export const CreateDeliveryPlanPage = () => {
  const router = useRouter();
  const notification = useNotification();

  const [facilityId, setFacilityId] = useState<string | undefined>(undefined);
  const [deliveryDate, setDeliveryDate] = useState('');
  const [searchSlipValue, setSearchSlipValue] = useState('');
  const [selectedSlipIds, setSelectedSlipIds] = useState<string[]>([]);
  const [selectedVehicleShipperIds, setSelectedVehicleShipperIds] = useState<
    string[]
  >([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: facilitiesResponse, isLoading: isLoadingFacilities } =
    useGetFacilitiesQuery({
      filters: {},
      pagination: { page: 0, size: 200 },
    });

  const {
    data: slipsResponse,
    currentData: currentSlipsResponse,
    isLoading: isLoadingSlips,
    isFetching: isFetchingSlips,
  } = useGetDeliverySlipsQuery(
    facilityId
      ? {
          filters: {
            facilityId,
            status: 'PENDING',
          },
          pagination: { page: 0, size: 200 },
        }
      : undefined,
    {
      skip: !facilityId,
    }
  );

  const {
    data: vehicleShippersResponse,
    currentData: currentVehicleShippersResponse,
    isLoading: isLoadingVehicleShippers,
    isFetching: isFetchingVehicleShippers,
  } = useGetVehicleShippersQuery(
    deliveryDate
      ? {
          filters: {
            workingDate: deliveryDate,
          },
          pagination: { page: 0, size: 200 },
        }
      : undefined,
    {
      skip: !deliveryDate,
    }
  );

  const [createDeliveryPlan, { isLoading: isCreating }] =
    useCreateDeliveryPlanMutation();

  useEffect(() => {
    setSelectedSlipIds([]);
  }, [facilityId]);

  useEffect(() => {
    setSelectedVehicleShipperIds([]);
  }, [deliveryDate]);

  const facilities = facilitiesResponse?.data?.items || [];
  const facilityItems = useMemo(
    () =>
      facilities.map((facility: Facility) => ({
        value: facility.id,
        label: facility.name,
      })),
    [facilities]
  );

  const slips =
    (isFetchingSlips ? currentSlipsResponse : slipsResponse)?.data?.items || [];
  const slipMap = useMemo(() => {
    const map = new Map<string, DeliverySlip>();

    slips.forEach((slip) => {
      map.set(slip.id, slip);
    });

    return map;
  }, [slips]);

  const availableSlips = useMemo(() => {
    const keyword = searchSlipValue.trim().toLowerCase();

    return slips.filter((slip) => {
      if (!keyword) {
        return true;
      }

      const slipCode = slip.code || '';
      const customerName = slip.customerName || '';

      return (
        slipCode.toLowerCase().includes(keyword) ||
        customerName.toLowerCase().includes(keyword)
      );
    });
  }, [searchSlipValue, slips]);

  const vehicleShippers = useMemo(() => {
    const allItems =
      (isFetchingVehicleShippers
        ? currentVehicleShippersResponse
        : vehicleShippersResponse
      )?.data?.items || [];
    return allItems.filter((shipper) => shipper.status === 'ACTIVE');
  }, [
    currentVehicleShippersResponse,
    isFetchingVehicleShippers,
    vehicleShippersResponse,
  ]);

  const vehicleShipperMap = useMemo(() => {
    const map = new Map<string, VehicleShipper>();

    vehicleShippers.forEach((shipper) => {
      map.set(shipper.id, shipper);
    });

    return map;
  }, [vehicleShippers]);

  const selectedSlips = useMemo(
    () =>
      selectedSlipIds
        .map((id) => slipMap.get(id))
        .filter((slip): slip is DeliverySlip => Boolean(slip)),
    [selectedSlipIds, slipMap]
  );

  const selectedVehicleShippers = useMemo(
    () =>
      selectedVehicleShipperIds
        .map((id) => vehicleShipperMap.get(id))
        .filter((shipper): shipper is VehicleShipper => Boolean(shipper)),
    [selectedVehicleShipperIds, vehicleShipperMap]
  );

  const canSubmit =
    facilityId &&
    deliveryDate &&
    selectedSlipIds.length > 0 &&
    selectedVehicleShipperIds.length > 0;

  const isFetchingData =
    isFetchingSlips || isFetchingVehicleShippers || isLoadingFacilities;

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

  const validateForm = () => {
    const nextErrors: Record<string, string> = {};

    if (!facilityId) {
      nextErrors.facilityId = 'Vui lòng chọn kho giao.';
    }

    if (!deliveryDate) {
      nextErrors.deliveryDate = 'Vui lòng chọn ngày giao.';
    }

    if (selectedSlipIds.length === 0) {
      nextErrors.slips = 'Vui lòng chọn ít nhất một phiếu giao.';
    }

    if (selectedVehicleShipperIds.length === 0) {
      nextErrors.vehicleShippers = 'Vui lòng chọn ít nhất một xe giao.';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm() || !facilityId) {
      return;
    }

    try {
      await createDeliveryPlan({
        facilityId,
        deliveryDate,
        deliverySlipIds: selectedSlipIds,
        vehicleShipperIds: selectedVehicleShipperIds,
      }).unwrap();

      notification.success('Tạo kế hoạch giao thành công', {
        description: 'Kế hoạch đã được lưu và sẵn sàng tối ưu lộ trình.',
      });

      router.push('/logistics2/delivery-plans');
    } catch (submitError) {
      notification.error('Không thể tạo kế hoạch', {
        description: getErrorMessage(submitError),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div className='flex items-start gap-3'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics2/delivery-plans')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>

          <div>
            <h1 className='text-2xl font-bold tracking-tight'>
              Tạo kế hoạch giao hàng
            </h1>
            <p className='text-muted-foreground'>
              Lựa chọn kho giao, phiếu giao và xe giao phù hợp
            </p>
          </div>
        </div>

        <Button
          onClick={handleSubmit}
          disabled={!canSubmit || isCreating}
          className='gap-2'
        >
          {isCreating ? (
            <Loader2 className='h-4 w-4 animate-spin' />
          ) : (
            <CheckCircle2 className='h-4 w-4' />
          )}
          Tạo kế hoạch
        </Button>
      </div>

      <div className='grid gap-6 lg:grid-cols-3'>
        <div className='space-y-6 lg:col-span-2'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Thông tin kế hoạch</h3>
            </CardHeader>
            <CardContent className='grid gap-4 sm:grid-cols-2'>
              <div>
                <Label>Kho giao</Label>
                <Combobox
                  value={facilityId}
                  onChange={(value) => {
                    setFacilityId(value ? String(value) : undefined);
                  }}
                  items={facilityItems}
                  placeholder='Chọn kho giao'
                  emptyText='Không tìm thấy kho'
                  loading={isLoadingFacilities}
                />
                {errors.facilityId && (
                  <p className='mt-1 text-xs text-destructive'>
                    {errors.facilityId}
                  </p>
                )}
              </div>

              <div>
                <Label>Ngày giao</Label>
                <Input
                  type='date'
                  value={deliveryDate}
                  onChange={(event) => setDeliveryDate(event.target.value)}
                />
                {errors.deliveryDate && (
                  <p className='mt-1 text-xs text-destructive'>
                    {errors.deliveryDate}
                  </p>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className='flex flex-wrap items-center justify-between gap-2'>
                <div>
                  <h3 className='font-semibold'>Phiếu giao hàng</h3>
                  <p className='text-sm text-muted-foreground'>
                    {facilityId
                      ? 'Chọn các phiếu giao thuộc kho đã chọn.'
                      : 'Hãy chọn kho giao trước.'}
                  </p>
                </div>
                <Badge variant='outline'>
                  Đã chọn: {selectedSlipIds.length}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='relative'>
                <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  value={searchSlipValue}
                  placeholder='Tìm phiếu theo mã, khách hàng...'
                  onChange={(event) => setSearchSlipValue(event.target.value)}
                  className='pl-10'
                  disabled={!facilityId}
                />
              </div>

              {errors.slips && (
                <p className='text-xs text-destructive'>{errors.slips}</p>
              )}

              <ScrollArea className='h-[280px] rounded-lg border'>
                <div className='space-y-2 p-3'>
                  {!facilityId && (
                    <div className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                      Vui lòng chọn kho giao để hiển thị các phiếu giao từ kho
                      đó.
                    </div>
                  )}

                  {facilityId && (isLoadingSlips || isFetchingSlips) && (
                    <div className='space-y-2'>
                      {Array.from({ length: 4 }).map((_, index) => (
                        <div
                          key={index}
                          className='h-16 rounded-lg bg-muted/60 animate-pulse'
                        />
                      ))}
                    </div>
                  )}

                  {facilityId &&
                    !(isLoadingSlips || isFetchingSlips) &&
                    availableSlips.length === 0 && (
                      <div className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                        Không có phiếu giao phù hợp.
                      </div>
                    )}

                  {facilityId &&
                    !(isLoadingSlips || isFetchingSlips) &&
                    availableSlips.map((slip) => {
                      const isSelected = selectedSlipIds.includes(slip.id);
                      const statusMeta =
                        SLIP_STATUS_META[slip.status] ||
                        SLIP_STATUS_META.PENDING;

                      return (
                        <div
                          role='button'
                          tabIndex={0}
                          key={slip.id}
                          onClick={() => toggleSlip(slip.id)}
                          onKeyDown={(e) => {
                            if (
                              e.key === 'Enter' ||
                              e.key === ' ' ||
                              e.key === 'Spacebar'
                            ) {
                              e.preventDefault();
                              toggleSlip(slip.id);
                            }
                          }}
                          className={cn(
                            'flex w-full items-start gap-3 rounded-lg border px-3 py-2 text-left transition',
                            isSelected
                              ? 'border-primary/60 bg-primary/5'
                              : 'hover:bg-muted/40'
                          )}
                        >
                          <Checkbox
                            checked={isSelected}
                            onCheckedChange={() => toggleSlip(slip.id)}
                            className='mt-1'
                          />
                          <div className='flex-1 space-y-1'>
                            <div className='flex flex-wrap items-center gap-2'>
                              <p className='text-sm font-semibold'>
                                {slip.code || `Phiếu #${slip.id.slice(0, 6)}`}
                              </p>
                              <Badge
                                variant='outline'
                                className={statusMeta.badgeClass}
                              >
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
                      );
                    })}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className='flex flex-wrap items-center justify-between gap-2'>
                <div>
                  <h3 className='font-semibold'>Xe giao hàng</h3>
                  <p className='text-sm text-muted-foreground'>
                    {deliveryDate
                      ? 'Hãy chọn xe hoạt động ngày giao chỉ định.'
                      : 'Hãy chọn ngày giao trước.'}
                  </p>
                </div>
                <Badge variant='outline'>
                  Đã chọn: {selectedVehicleShipperIds.length}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className='space-y-4'>
              {errors.vehicleShippers && (
                <p className='text-xs text-destructive'>
                  {errors.vehicleShippers}
                </p>
              )}

              <ScrollArea className='h-[240px] rounded-lg border'>
                <div className='space-y-2 p-3'>
                  {!deliveryDate && (
                    <div className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                      Vui lòng chọn ngày giao để hiển thị các xe hoạt động vào
                      ngày đó.
                    </div>
                  )}

                  {deliveryDate &&
                    (isLoadingVehicleShippers || isFetchingVehicleShippers) && (
                      <div className='space-y-2'>
                        {Array.from({ length: 4 }).map((_, index) => (
                          <div
                            key={index}
                            className='h-16 rounded-lg bg-muted/60 animate-pulse'
                          />
                        ))}
                      </div>
                    )}

                  {deliveryDate &&
                    !(isLoadingVehicleShippers || isFetchingVehicleShippers) &&
                    vehicleShippers.length === 0 && (
                      <div className='rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground'>
                        Không có xe giao phù hợp.
                      </div>
                    )}

                  {deliveryDate &&
                    !(isLoadingVehicleShippers || isFetchingVehicleShippers) &&
                    vehicleShippers.map((shipper) => {
                      const isSelected = selectedVehicleShipperIds.includes(
                        shipper.id
                      );
                      const statusMeta =
                        VEHICLE_STATUS_META[shipper.status] ||
                        VEHICLE_STATUS_META.ACTIVE;

                      return (
                        <div
                          role='button'
                          tabIndex={0}
                          key={shipper.id}
                          onClick={() => toggleVehicleShipper(shipper.id)}
                          onKeyDown={(e) => {
                            if (
                              e.key === 'Enter' ||
                              e.key === ' ' ||
                              e.key === 'Spacebar'
                            ) {
                              e.preventDefault();
                              toggleVehicleShipper(shipper.id);
                            }
                          }}
                          className={cn(
                            'flex w-full items-start gap-3 rounded-lg border px-3 py-2 text-left transition',
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
                              {formatVehicleType(shipper.vehicle?.vehicleType)}
                              {shipper.workingDate
                                ? ` · ${formatDateVN(shipper.workingDate)}`
                                : ''}
                            </p>
                          </div>
                        </div>
                      );
                    })}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </div>

        <div className='space-y-6'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Tóm tắt kế hoạch</h3>
            </CardHeader>
            <CardContent className='space-y-4 text-sm'>
              <div className='flex items-center justify-between'>
                <span className='text-muted-foreground'>Kho giao</span>
                <span className='font-medium'>
                  {facilityId
                    ? facilities.find((item) => item.id === facilityId)?.name ||
                      'Chưa chọn'
                    : 'Chưa chọn'}
                </span>
              </div>
              <div className='flex items-center justify-between'>
                <span className='text-muted-foreground'>Ngày giao</span>
                <span className='font-medium'>
                  {deliveryDate ? formatDateVN(deliveryDate) : 'Chưa chọn'}
                </span>
              </div>
              <div className='flex items-center justify-between'>
                <span className='text-muted-foreground'>Phiếu giao</span>
                <span className='font-medium'>
                  {formatNumber(selectedSlipIds.length)}
                </span>
              </div>
              <div className='flex items-center justify-between'>
                <span className='text-muted-foreground'>Xe giao</span>
                <span className='font-medium'>
                  {formatNumber(selectedVehicleShipperIds.length)}
                </span>
              </div>

              {isFetchingData && (
                <div className='flex items-center gap-2 text-xs text-muted-foreground'>
                  <Loader2 className='h-3.5 w-3.5 animate-spin' />
                  Đang tải dữ liệu...
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Phiếu giao đã chọn</h3>
            </CardHeader>
            <CardContent className='space-y-2'>
              {selectedSlips.length === 0 && (
                <p className='text-sm text-muted-foreground'>
                  Chưa chọn phiếu giao nào.
                </p>
              )}
              {selectedSlips.map((slip) => (
                <div
                  key={slip.id}
                  className='flex items-center justify-between gap-2 rounded-lg border px-3 py-2 text-sm'
                >
                  <div className='min-w-0'>
                    <p className='truncate font-medium'>
                      {slip.code || `Phiếu #${slip.id.slice(0, 6)}`}
                    </p>
                    <p className='truncate text-xs text-muted-foreground'>
                      {slip.customerName || 'Chưa có khách hàng'}
                    </p>
                  </div>
                  <Button
                    variant='ghost'
                    size='icon'
                    onClick={() => toggleSlip(slip.id)}
                  >
                    <X className='h-4 w-4' />
                  </Button>
                </div>
              ))}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Xe giao đã chọn</h3>
            </CardHeader>
            <CardContent className='space-y-2'>
              {selectedVehicleShippers.length === 0 && (
                <p className='text-sm text-muted-foreground'>
                  Chưa chọn xe giao nào.
                </p>
              )}
              {selectedVehicleShippers.map((shipper) => (
                <div
                  key={shipper.id}
                  className='flex items-center justify-between gap-2 rounded-lg border px-3 py-2 text-sm'
                >
                  <div className='min-w-0'>
                    <p className='truncate font-medium'>
                      {shipper.vehicle?.licensePlate || shipper.vehicleId}
                    </p>
                    <p className='truncate text-xs text-muted-foreground'>
                      {formatVehicleType(shipper.vehicle?.vehicleType)}
                    </p>
                  </div>
                  <Button
                    variant='ghost'
                    size='icon'
                    onClick={() => toggleVehicleShipper(shipper.id)}
                  >
                    <X className='h-4 w-4' />
                  </Button>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
