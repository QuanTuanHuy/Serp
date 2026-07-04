/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle form dialog
 */

import React from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import type { VehicleStatus, VehicleType } from '../../../../types';
import {
  VEHICLE_STATUS_OPTIONS,
  VEHICLE_TYPE_OPTIONS,
  type VehicleFormMode,
  type VehicleFormState,
} from '../firstMileVehiclePageModels';

type UpdateVehicleFormField = <K extends keyof VehicleFormState>(
  field: K,
  value: VehicleFormState[K]
) => void;

interface VehicleSelectOption {
  value: string;
  label: string;
}

const NONE_VALUE = '__NONE__';

interface VehicleFormDialogProps {
  open: boolean;
  formMode: VehicleFormMode;
  formValues: VehicleFormState;
  isSaving: boolean;
  postOfficeOptions: VehicleSelectOption[];
  courierOptions: VehicleSelectOption[];
  isLoadingPostOffices: boolean;
  isLoadingCouriers: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onUpdateField: UpdateVehicleFormField;
}

export const VehicleFormDialog: React.FC<VehicleFormDialogProps> = ({
  open,
  formMode,
  formValues,
  isSaving,
  postOfficeOptions,
  courierOptions,
  isLoadingPostOffices,
  isLoadingCouriers,
  onOpenChange,
  onSubmit,
  onUpdateField,
}) => {
  const postOfficeComboboxOptions = [
    { value: NONE_VALUE, label: 'Chưa phân công' },
    ...postOfficeOptions,
  ];
  const courierComboboxOptions = [
    { value: NONE_VALUE, label: 'Chưa phân công' },
    ...courierOptions,
  ];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>
            {formMode === 'create'
              ? 'Thêm phương tiện'
              : 'Cập nhật phương tiện'}
          </DialogTitle>
          <DialogDescription>
            Nhập các thông tin bắt buộc để lưu phương tiện.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='vehicle-license-plate'>Biển số xe *</Label>
              <Input
                id='vehicle-license-plate'
                value={formValues.licensePlate}
                onChange={(event) =>
                  onUpdateField('licensePlate', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-status'>Trạng thái *</Label>
              <TmsCombobox
                id='vehicle-status'
                value={formValues.status}
                onValueChange={(value) =>
                  onUpdateField('status', value as VehicleStatus)
                }
                options={VEHICLE_STATUS_OPTIONS}
                placeholder='Chọn trạng thái'
                emptyText='Không có trạng thái phù hợp'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-type'>Loại xe *</Label>
              <TmsCombobox
                id='vehicle-type'
                value={formValues.vehicleType}
                onValueChange={(value) =>
                  onUpdateField('vehicleType', value as VehicleType)
                }
                options={VEHICLE_TYPE_OPTIONS}
                placeholder='Chọn loại xe'
                emptyText='Không có loại xe phù hợp'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-max-weight'>Tải trọng tối đa (kg)</Label>
              <Input
                id='vehicle-max-weight'
                type='number'
                min={0}
                step='any'
                value={formValues.maxWeight}
                onChange={(event) =>
                  onUpdateField('maxWeight', event.target.value)
                }
                disabled={isSaving}
                placeholder='Ví dụ: 150'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-max-volume'>Thể tích tối đa (m3)</Label>
              <Input
                id='vehicle-max-volume'
                type='number'
                min={0}
                step='any'
                value={formValues.maxVolume}
                onChange={(event) =>
                  onUpdateField('maxVolume', event.target.value)
                }
                disabled={isSaving}
                placeholder='Ví dụ: 3.2'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-post-office-id'>Bưu cục</Label>
              <TmsCombobox
                id='vehicle-post-office-id'
                value={formValues.postOfficeId.trim() || NONE_VALUE}
                onValueChange={(value) => {
                  const nextPostOfficeId = value === NONE_VALUE ? '' : value;

                  onUpdateField('postOfficeId', nextPostOfficeId);

                  if (nextPostOfficeId !== formValues.postOfficeId) {
                    onUpdateField('postOfficeStaffId', '');
                  }
                }}
                options={postOfficeComboboxOptions}
                placeholder={
                  isLoadingPostOffices
                    ? 'Đang tải bưu cục...'
                    : 'Chọn bưu cục'
                }
                emptyText={
                  isLoadingPostOffices
                    ? 'Đang tải bưu cục...'
                    : 'Không có bưu cục khả dụng.'
                }
                disabled={isSaving || isLoadingPostOffices}
                loading={isLoadingPostOffices}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-post-office-staff-id'>
                Nhân viên giao nhận
              </Label>
              <TmsCombobox
                id='vehicle-post-office-staff-id'
                value={formValues.postOfficeStaffId.trim() || NONE_VALUE}
                onValueChange={(value) =>
                  onUpdateField(
                    'postOfficeStaffId',
                    value === NONE_VALUE ? '' : value
                  )
                }
                options={courierComboboxOptions}
                placeholder={
                  !formValues.postOfficeId.trim()
                    ? 'Chọn bưu cục trước'
                    : isLoadingCouriers
                      ? 'Đang tải nhân viên...'
                      : 'Chọn nhân viên'
                }
                emptyText={
                  !formValues.postOfficeId.trim()
                    ? 'Chọn bưu cục trước.'
                    : isLoadingCouriers
                      ? 'Đang tải nhân viên...'
                      : 'Không có nhân viên giao nhận cho bưu cục này.'
                }
                disabled={
                  isSaving ||
                  !formValues.postOfficeId.trim() ||
                  isLoadingCouriers
                }
                loading={isLoadingCouriers}
              />
            </div>
          </div>

          <p className='text-xs text-muted-foreground'>
            Với vai trò quản lý, danh sách bưu cục và nhân viên giao nhận được
            giới hạn theo phạm vi phụ trách.
          </p>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSaving}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isSaving}>
              {isSaving && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              {formMode === 'create' ? 'Tạo mới' : 'Lưu thay đổi'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
