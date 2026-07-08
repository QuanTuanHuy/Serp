/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher setup card
 */

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Checkbox,
  Input,
  Label,
} from '@/shared/components/ui';
import { CheckCircle2, Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import type { PickupShift } from '../../../../types';
import type {
  DispatchOptimizationGoalOption,
  DispatchSetupCardProps,
  RoutingVehicleOption,
} from './types';

const SHIFT_OPTIONS: Array<{ value: PickupShift; label: string }> = [
  { value: 'MORNING', label: 'Ca sáng (07:30 - 12:00)' },
  { value: 'AFTERNOON', label: 'Ca chiều (13:30 - 18:00)' },
  { value: 'EVENING', label: 'Ca tối (18:30 - 22:00)' },
];

const ROUTING_VEHICLE_OPTIONS: Array<{
  value: RoutingVehicleOption;
  label: string;
}> = [
  { value: 'DEFAULT', label: 'Dùng thiết lập mặc định' },
  { value: 'CAR', label: 'Ô tô' },
  { value: 'BIKE', label: 'Xe máy' },
  { value: 'TAXI', label: 'Taxi' },
  { value: 'TRUCK', label: 'Xe tải' },
  { value: 'HD', label: 'Xe tải nặng' },
];

const OPTIMIZATION_GOAL_OPTIONS: Array<{
  value: DispatchOptimizationGoalOption;
  label: string;
}> = [
  { value: 'BALANCED', label: 'Cân bằng' },
  { value: 'ON_TIME_PRIORITY', label: 'Ưu tiên lấy đúng giờ' },
  { value: 'COST_EFFICIENCY', label: 'Giảm chi phí di chuyển' },
  { value: 'MAX_ASSIGNMENT', label: 'Tối đa số đơn được phân công' },
];

export const DispatchSetupCard: React.FC<DispatchSetupCardProps> = ({
  postOfficeOptions,
  courierOptions,
  selectedPostOfficeId,
  onPostOfficeChange,
  isLoadingPostOffices,
  isLoadingCouriers,
  shift,
  onShiftChange,
  tripDate,
  onTripDateChange,
  orderLimitInput,
  onOrderLimitInputChange,
  selectedAutoCourierIds,
  onToggleAutoCourier,
  onClearAutoCourierSelection,
  businessValues,
  businessHandlers,
  onPreviewPlan,
  onAutoAssign,
  isOptimizing,
  isAutoAssigning,
  activeAction,
  isTmsAdmin,
  hidePreviewButton = false,
}) => {
  const postOfficeComboboxOptions = postOfficeOptions.map((postOffice) => ({
    value: String(postOffice.id),
    label: `${postOffice.code} - ${postOffice.name}`,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Thiết lập điều phối</CardTitle>
        <CardDescription>
          Cấu hình bưu cục, ca làm và chiến lược nghiệp vụ trước khi điều phối.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
          <div className='space-y-2'>
            <Label htmlFor='dispatch-post-office'>Bưu cục</Label>
            <TmsCombobox
              id='dispatch-post-office'
              value={selectedPostOfficeId}
              onValueChange={onPostOfficeChange}
              options={postOfficeComboboxOptions}
              placeholder='Chọn bưu cục'
              emptyText='Không tìm thấy bưu cục'
              disabled={isLoadingPostOffices}
              loading={isLoadingPostOffices}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-shift'>Ca làm</Label>
            <TmsCombobox
              id='dispatch-shift'
              value={shift}
              onValueChange={(value) => onShiftChange(value as PickupShift)}
              options={SHIFT_OPTIONS}
              placeholder='Chọn ca làm'
              emptyText='Không tìm thấy ca làm'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-trip-date'>Ngày chuyến</Label>
            <Input
              id='dispatch-trip-date'
              type='date'
              value={tripDate}
              onChange={(event) => onTripDateChange(event.target.value)}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-order-limit'>Giới hạn đơn hàng</Label>
            <Input
              id='dispatch-order-limit'
              value={orderLimitInput}
              onChange={(event) => onOrderLimitInputChange(event.target.value)}
              placeholder='Tùy chọn, ví dụ 100'
            />
          </div>

          <div className='space-y-2 md:col-span-2 xl:col-span-4'>
            <div className='flex items-center justify-between gap-2'>
              <Label>Nhân viên cho bộ tối ưu (tùy chọn)</Label>
              {selectedAutoCourierIds.length > 0 ? (
                <Button
                  type='button'
                  size='sm'
                  variant='outline'
                  onClick={onClearAutoCourierSelection}
                >
                  Xóa lọc nhân viên
                </Button>
              ) : null}
            </div>

            {isLoadingCouriers ? (
              <p className='text-xs text-muted-foreground'>
                Đang tải nhân viên của bưu cục đã chọn...
              </p>
            ) : courierOptions.length > 0 ? (
              <div className='grid gap-2 md:grid-cols-2 xl:grid-cols-3'>
                {courierOptions.map((courier) => {
                  const checked = selectedAutoCourierIds.includes(courier.id);

                  return (
                    <label
                      key={courier.id}
                      className='flex items-start gap-2 rounded-md border p-2 text-sm'
                    >
                      <Checkbox
                        checked={checked}
                        onCheckedChange={(value) =>
                          onToggleAutoCourier(courier.id, Boolean(value))
                        }
                        aria-label={`Chọn nhân viên ${courier.code}`}
                      />
                      <span className='leading-5'>
                        {courier.code} - {courier.fullName}
                      </span>
                    </label>
                  );
                })}
              </div>
            ) : (
              <p className='text-xs text-muted-foreground'>
                Không có nhân viên đang hoạt động tại bưu cục này.
              </p>
            )}

            <p className='text-xs text-muted-foreground'>
              Bỏ trống để hệ thống tự chọn toàn bộ nhân viên đủ điều kiện.
            </p>
          </div>

          <div className='space-y-3 rounded-md border p-4 md:col-span-2 xl:col-span-4'>
            <div className='space-y-1'>
              <p className='text-sm font-semibold'>Chiến lược điều phối</p>
              <p className='text-xs text-muted-foreground'>
                Hệ thống sẽ tự tính toán lộ trình phù hợp. Tại đây chỉ cần chọn
                mục tiêu điều phối.
              </p>
            </div>

            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='dispatch-vehicle-profile'>
                  Hồ sơ phương tiện
                </Label>
                <TmsCombobox
                  id='dispatch-vehicle-profile'
                  value={businessValues.vehicleOption}
                  onValueChange={(value) =>
                    businessHandlers.onVehicleOptionChange(
                      value as RoutingVehicleOption
                    )
                  }
                  options={ROUTING_VEHICLE_OPTIONS}
                  placeholder='Chọn hồ sơ phương tiện'
                  emptyText='Không tìm thấy hồ sơ phương tiện'
                />
                <p className='text-xs text-muted-foreground'>
                  Tùy chọn. Hầu hết trường hợp nên giữ theo thiết lập mặc định.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-optimization-goal'>
                  Mục tiêu lập kế hoạch
                </Label>
                <TmsCombobox
                  id='dispatch-optimization-goal'
                  value={businessValues.optimizationGoal}
                  onValueChange={(value) =>
                    businessHandlers.onOptimizationGoalChange(
                      value as DispatchOptimizationGoalOption
                    )
                  }
                  options={OPTIMIZATION_GOAL_OPTIONS}
                  placeholder='Chọn mục tiêu lập kế hoạch'
                  emptyText='Không tìm thấy mục tiêu'
                />
                <p className='text-xs text-muted-foreground'>
                  Xác định ưu tiên nghiệp vụ: đúng giờ, chi phí hoặc tỷ lệ phân
                  công.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className='flex flex-wrap gap-2'>
          {!hidePreviewButton ? (
            <Button
              type='button'
              variant='outline'
              onClick={onPreviewPlan}
              disabled={isOptimizing || !selectedPostOfficeId}
            >
              {activeAction === 'preview' ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : null}
              Xem trước kế hoạch
            </Button>
          ) : null}
          <Button
            type='button'
            onClick={onAutoAssign}
            disabled={isAutoAssigning || !selectedPostOfficeId}
          >
            {activeAction === 'auto' ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <CheckCircle2 className='mr-2 h-4 w-4' />
            )}
            Phân công tự động
          </Button>
        </div>

        {!isTmsAdmin ? (
          <p className='text-xs text-muted-foreground'>
            Hệ thống sẽ áp dụng phạm vi quản lý. Bạn chỉ có thể điều phối cho
            các bưu cục được gán cho mình.
          </p>
        ) : null}
      </CardContent>
    </Card>
  );
};
