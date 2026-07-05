/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle form dialog
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
import type {
  SecondMileVehicleStatus,
  SecondMileVehicleType,
} from '../../../../types';
import {
  VEHICLE_STATUS_OPTIONS,
  VEHICLE_TYPE_OPTIONS,
  type VehicleFormMode,
  type VehicleFormState,
} from '../secondMileVehiclePageModels';

type UpdateVehicleFormField = <K extends keyof VehicleFormState>(
  field: K,
  value: VehicleFormState[K]
) => void;

interface VehicleSelectOption {
  value: string;
  label: string;
}

const NONE_VALUE = '__NONE__';

interface SecondMileVehicleFormDialogProps {
  open: boolean;
  formMode: VehicleFormMode;
  formValues: VehicleFormState;
  isSaving: boolean;
  hubOptions: VehicleSelectOption[];
  isLoadingHubs: boolean;
  driverOptions: VehicleSelectOption[];
  isLoadingDrivers: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onUpdateField: UpdateVehicleFormField;
}

export const SecondMileVehicleFormDialog: React.FC<
  SecondMileVehicleFormDialogProps
> = ({
  open,
  formMode,
  formValues,
  isSaving,
  hubOptions,
  isLoadingHubs,
  driverOptions,
  isLoadingDrivers,
  onOpenChange,
  onSubmit,
  onUpdateField,
}) => {
  const driverComboboxOptions = [
    { value: NONE_VALUE, label: 'Chưa phân công' },
    ...driverOptions,
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
            Phương tiện chặng giữa được liên kết với hub và tài xế phụ trách.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-license-plate'>Biển số xe *</Label>
              <Input
                id='sm-vehicle-license-plate'
                value={formValues.licensePlate}
                onChange={(event) =>
                  onUpdateField('licensePlate', event.target.value)
                }
                disabled={isSaving}
                placeholder='17B6-72685'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-status'>Trạng thái *</Label>
              <TmsCombobox
                id='sm-vehicle-status'
                value={formValues.status}
                onValueChange={(value) =>
                  onUpdateField('status', value as SecondMileVehicleStatus)
                }
                options={VEHICLE_STATUS_OPTIONS}
                placeholder='Chọn trạng thái'
                emptyText='Không có trạng thái phù hợp'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-type'>Loại xe *</Label>
              <TmsCombobox
                id='sm-vehicle-type'
                value={formValues.vehicleType}
                onValueChange={(value) =>
                  onUpdateField('vehicleType', value as SecondMileVehicleType)
                }
                options={VEHICLE_TYPE_OPTIONS}
                placeholder='Chọn loại xe'
                emptyText='Không có loại xe phù hợp'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-max-bags'>Số bao tối đa *</Label>
              <Input
                id='sm-vehicle-max-bags'
                type='number'
                min={0}
                step={1}
                value={formValues.maxBags}
                onChange={(event) =>
                  onUpdateField('maxBags', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-max-weight'>
                Tải trọng tối đa (kg) *
              </Label>
              <Input
                id='sm-vehicle-max-weight'
                type='number'
                min={0}
                step='any'
                value={formValues.maxWeight}
                onChange={(event) =>
                  onUpdateField('maxWeight', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-max-volume'>
                Thể tích tối đa (m3) *
              </Label>
              <Input
                id='sm-vehicle-max-volume'
                type='number'
                min={0}
                step='any'
                value={formValues.maxVolume}
                onChange={(event) =>
                  onUpdateField('maxVolume', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='sm-vehicle-hub'>Hub *</Label>
              <TmsCombobox
                id='sm-vehicle-hub'
                value={formValues.hubId.trim()}
                onValueChange={(value) => {
                  const nextHubId = value === NONE_VALUE ? '' : value;

                  onUpdateField('hubId', nextHubId);

                  if (nextHubId !== formValues.hubId) {
                    onUpdateField('assignedStaffId', '');
                  }
                }}
                options={hubOptions}
                placeholder={isLoadingHubs ? 'Đang tải hub...' : 'Chọn hub'}
                emptyText={
                  isLoadingHubs ? 'Đang tải hub...' : 'Không tìm thấy hub'
                }
                disabled={isSaving || isLoadingHubs}
                loading={isLoadingHubs}
              />
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='sm-vehicle-driver'>Tài xế</Label>
              <TmsCombobox
                id='sm-vehicle-driver'
                value={formValues.assignedStaffId.trim() || NONE_VALUE}
                onValueChange={(value) =>
                  onUpdateField(
                    'assignedStaffId',
                    value === NONE_VALUE ? '' : value
                  )
                }
                options={driverComboboxOptions}
                placeholder={
                  !formValues.hubId.trim()
                    ? 'Chọn hub trước'
                    : isLoadingDrivers
                      ? 'Đang tải tài xế...'
                      : 'Chọn tài xế'
                }
                emptyText={
                  !formValues.hubId.trim()
                    ? 'Chọn hub trước.'
                    : isLoadingDrivers
                      ? 'Đang tải tài xế...'
                      : 'Không có tài xế được phân công cho hub này.'
                }
                disabled={
                  isSaving || !formValues.hubId.trim() || isLoadingDrivers
                }
                loading={isLoadingDrivers}
              />
            </div>
          </div>

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
