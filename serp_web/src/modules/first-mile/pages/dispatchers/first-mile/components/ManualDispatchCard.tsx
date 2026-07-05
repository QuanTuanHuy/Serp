/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher manual dispatch card
 */

import React from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { CheckCircle2, Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import type { PickupShift, Province, Ward } from '../../../../types';
import { CandidateOrdersPanel } from './CandidateOrdersPanel';
import { OrderMultiSelect } from './OrderMultiSelect';
import type { ManualDispatchCardProps } from './types';

const MANUAL_SHIFT_OPTIONS: Array<{ value: PickupShift; label: string }> = [
  { value: 'MORNING', label: 'Ca sáng (07:30 - 12:00)' },
  { value: 'AFTERNOON', label: 'Ca chiều (13:30 - 18:00)' },
  { value: 'EVENING', label: 'Ca tối (18:30 - 22:00)' },
];

export const ManualDispatchCard: React.FC<ManualDispatchCardProps> = ({
  provinceOptions,
  wardOptions,
  selectedProvinceCode,
  onProvinceChange,
  selectedWardCode,
  onWardChange,
  isLoadingProvinces,
  isLoadingWards,
  postOfficeOptions,
  selectedPostOfficeId,
  onPostOfficeChange,
  isLoadingPostOffices,
  shift,
  onShiftChange,
  tripDate,
  onTripDateChange,
  courierOptions,
  selectedManualCourierId,
  onManualCourierIdChange,
  isLoadingCouriers,
  suggestedCouriers,
  onQuickPickCourier,
  isLoadingOrders,
  candidateOrders,
  selectedOrderIds,
  onOrderSelectionChange,
  onClearSelectedOrders,
  onManualAssign,
  isManualAssigning,
  activeAction,
}) => {
  const canSelectPostOffice = Boolean(selectedProvinceCode);
  const provinceComboboxOptions = provinceOptions.flatMap(
    (province: Province) =>
      province.provinceCode
        ? [
            {
              value: province.provinceCode,
              label: `${province.name} (${province.provinceCode})`,
            },
          ]
        : []
  );
  const wardComboboxOptions = [
    { value: 'ALL', label: 'Tất cả phường/xã trong tỉnh' },
    ...wardOptions.flatMap((ward: Ward) =>
      ward.wardCode
        ? [
            {
              value: ward.wardCode,
              label: `${ward.name} (${ward.wardCode})`,
            },
          ]
        : []
    ),
  ];
  const postOfficeComboboxOptions = postOfficeOptions.map((postOffice) => ({
    value: String(postOffice.id),
    label: `${postOffice.code} - ${postOffice.name}`,
  }));
  const courierComboboxOptions = courierOptions.map((courier) => ({
    value: String(courier.id),
    label: `${courier.code} - ${courier.fullName}`,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Điều phối thủ công</CardTitle>
        <CardDescription>
          Lọc bưu cục theo địa bàn, sau đó chọn đơn hàng và gán cho nhân viên
          giao nhận. Khu vực này dùng phạm vi bưu cục riêng, tách biệt với phần
          thiết lập điều phối.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-3'>
          <div className='space-y-2'>
            <Label htmlFor='manual-province'>Tỉnh/Thành phố</Label>
            <TmsCombobox
              id='manual-province'
              value={selectedProvinceCode}
              onValueChange={onProvinceChange}
              options={provinceComboboxOptions}
              placeholder='Chọn tỉnh/thành phố'
              emptyText='Không tìm thấy tỉnh/thành phố'
              disabled={isLoadingProvinces}
              loading={isLoadingProvinces}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-ward'>Phường/Xã</Label>
            <TmsCombobox
              id='manual-ward'
              value={selectedWardCode}
              onValueChange={onWardChange}
              options={wardComboboxOptions}
              placeholder={
                selectedProvinceCode
                  ? 'Chọn phường/xã (tùy chọn)'
                  : 'Chọn tỉnh/thành phố trước'
              }
              emptyText={
                isLoadingWards
                  ? 'Đang tải phường/xã...'
                  : 'Không tìm thấy phường/xã'
              }
              disabled={!selectedProvinceCode || isLoadingWards}
              loading={isLoadingWards}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-post-office'>Bưu cục</Label>
            <TmsCombobox
              id='manual-post-office'
              value={selectedPostOfficeId}
              onValueChange={onPostOfficeChange}
              options={postOfficeComboboxOptions}
              placeholder='Chọn bưu cục'
              emptyText='Không tìm thấy bưu cục'
              disabled={!canSelectPostOffice || isLoadingPostOffices}
              loading={isLoadingPostOffices}
            />
            {isLoadingPostOffices ? (
              <p className='text-xs text-muted-foreground'>
                Đang tải bưu cục...
              </p>
            ) : null}
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-shift'>Ca làm</Label>
            <TmsCombobox
              id='manual-shift'
              value={shift}
              onValueChange={(value) => onShiftChange(value as PickupShift)}
              options={MANUAL_SHIFT_OPTIONS}
              placeholder='Chọn ca làm'
              emptyText='Không tìm thấy ca làm'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-trip-date'>Ngày chuyến</Label>
            <Input
              id='manual-trip-date'
              type='date'
              value={tripDate}
              onChange={(event) => onTripDateChange(event.target.value)}
            />
          </div>
        </div>

        <div className='grid gap-3 md:grid-cols-2'>
          <div className='space-y-2'>
            <Label htmlFor='manual-courier-select'>Nhân viên giao nhận</Label>
            <TmsCombobox
              id='manual-courier-select'
              value={selectedManualCourierId}
              onValueChange={onManualCourierIdChange}
              options={courierComboboxOptions}
              placeholder='Chọn nhân viên để phân công thủ công'
              emptyText='Không tìm thấy nhân viên'
              disabled={!selectedPostOfficeId || isLoadingCouriers}
              loading={isLoadingCouriers}
            />
            {isLoadingCouriers ? (
              <p className='text-xs text-muted-foreground'>
                Đang tải nhân viên...
              </p>
            ) : null}
          </div>

          <div className='space-y-2'>
            <Label>Chọn nhanh nhân viên</Label>
            {suggestedCouriers.length > 0 ? (
              <div className='flex flex-wrap gap-2'>
                {suggestedCouriers.map((courier) => (
                  <Button
                    key={courier.id}
                    type='button'
                    variant='outline'
                    size='sm'
                    onClick={() => onQuickPickCourier(courier.id)}
                  >
                    {courier.label}
                  </Button>
                ))}
              </div>
            ) : (
              <p className='text-xs text-muted-foreground'>
                Chạy xem trước hoặc phân công tự động trong phần thiết lập để
                nhận gợi ý nhân viên.
              </p>
            )}
          </div>
        </div>

        <div className='space-y-2'>
          <div className='flex flex-wrap items-center justify-between gap-2'>
            <Label htmlFor='manual-orders-select'>Đơn hàng</Label>
            <div className='flex items-center gap-2'>
              <Badge variant='secondary'>
                Đã chọn: {selectedOrderIds.length}
              </Badge>
              {selectedOrderIds.length > 0 ? (
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  onClick={onClearSelectedOrders}
                >
                  Xóa lựa chọn
                </Button>
              ) : null}
            </div>
          </div>
          <OrderMultiSelect
            orders={candidateOrders}
            selectedOrderIds={selectedOrderIds}
            onSelectionChange={onOrderSelectionChange}
            disabled={!selectedPostOfficeId}
            loading={isLoadingOrders}
            placeholder={
              selectedPostOfficeId
                ? 'Chọn đơn hàng để phân công...'
                : 'Chọn bưu cục trước'
            }
          />
          {!isLoadingOrders && selectedPostOfficeId ? (
            <p className='text-xs text-muted-foreground'>
              {candidateOrders.length} đơn hàng ứng viên ở trạng thái Mới tạo
              hoặc Lấy hàng thất bại.
            </p>
          ) : null}
        </div>

        <CandidateOrdersPanel
          title='Đơn chờ điều phối thủ công'
          orders={candidateOrders}
          loading={isLoadingOrders}
          selectedOrderIds={selectedOrderIds}
          onOrderToggle={(orderId, checked) => {
            if (checked) {
              onOrderSelectionChange(
                selectedOrderIds.includes(orderId)
                  ? selectedOrderIds
                  : [...selectedOrderIds, orderId]
              );
              return;
            }

            onOrderSelectionChange(
              selectedOrderIds.filter((selectedId) => selectedId !== orderId)
            );
          }}
          emptyText='Không có đơn có thể điều phối cho bưu cục này.'
        />

        <div className='pt-1'>
          <Button
            type='button'
            onClick={onManualAssign}
            disabled={
              isManualAssigning ||
              selectedOrderIds.length === 0 ||
              !selectedPostOfficeId
            }
          >
            {activeAction === 'manual' ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <CheckCircle2 className='mr-2 h-4 w-4' />
            )}
            Phân công các đơn đã chọn
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
