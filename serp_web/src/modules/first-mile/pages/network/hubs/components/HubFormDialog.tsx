/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub create/update form dialog
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
import { Loader2, LocateFixed } from 'lucide-react';
import { CoordinatePickerMap, TmsCombobox } from '../../../../components';
import type { Province, Ward } from '../../../../types';
import {
  HUB_FORM_STATUS_OPTIONS,
  normalizeLocationCode,
  type HubFormMode,
  type HubFormState,
  type UpdateHubFormField,
} from '../hubForm';

interface HubFormDialogProps {
  open: boolean;
  formMode: HubFormMode;
  isSaving: boolean;
  isGeocodingAddress: boolean;
  formValues: HubFormState;
  selectedProvinceCode: string;
  selectedWardCode: string;
  provinceSelectOptions: Province[];
  wardSelectOptions: Ward[];
  isFetchingWardsForForm: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onGeocodeAddress: () => Promise<void>;
  updateFormField: UpdateHubFormField;
}

export const HubFormDialog: React.FC<HubFormDialogProps> = ({
  open,
  formMode,
  isSaving,
  isGeocodingAddress,
  formValues,
  selectedProvinceCode,
  selectedWardCode,
  provinceSelectOptions,
  wardSelectOptions,
  isFetchingWardsForForm,
  onOpenChange,
  onSubmit,
  onGeocodeAddress,
  updateFormField,
}) => {
  const latNum = Number(formValues.latitude);
  const lngNum = Number(formValues.longitude);
  const mapLat = Number.isFinite(latNum) ? latNum : undefined;
  const mapLng = Number.isFinite(lngNum) ? lngNum : undefined;
  const provinceOptions = provinceSelectOptions.flatMap((province) => {
    const code = normalizeLocationCode(province.provinceCode);

    return code
      ? [
          {
            value: code,
            label: `${province.name} (${code})`,
          },
        ]
      : [];
  });
  const wardOptions = wardSelectOptions.flatMap((ward) => {
    const code = normalizeLocationCode(ward.wardCode);

    return code
      ? [
          {
            value: code,
            label: `${ward.name} (${code})`,
          },
        ]
      : [];
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl max-h-[85vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            {formMode === 'create' ? 'Tạo hub' : 'Cập nhật hub'}
          </DialogTitle>
          <DialogDescription>
            {formMode === 'create'
              ? 'Tạo hub trung chuyển mới.'
              : 'Cập nhật thông tin hub.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='hub-code'>Mã hub *</Label>
              <Input
                id='hub-code'
                value={formValues.code}
                onChange={(e) => updateFormField('code', e.target.value)}
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-name'>Tên hub *</Label>
              <Input
                id='hub-name'
                value={formValues.name}
                onChange={(e) => updateFormField('name', e.target.value)}
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-status'>Trạng thái *</Label>
              <TmsCombobox
                id='hub-status'
                value={formValues.status}
                onValueChange={(v) =>
                  updateFormField('status', v as HubFormState['status'])
                }
                options={HUB_FORM_STATUS_OPTIONS}
                placeholder='Chọn trạng thái'
                emptyText='Không tìm thấy trạng thái'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-province'>Tỉnh/Thành phố *</Label>
              <TmsCombobox
                id='hub-province'
                value={selectedProvinceCode}
                onValueChange={(value) => {
                  updateFormField('province_code', value);
                  updateFormField('ward_code', '');
                }}
                options={provinceOptions}
                placeholder='Chọn tỉnh/thành phố'
                emptyText='Không tìm thấy tỉnh/thành phố'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-ward'>Phường/Xã *</Label>
              <TmsCombobox
                id='hub-ward'
                value={selectedWardCode}
                onValueChange={(value) => updateFormField('ward_code', value)}
                options={wardOptions}
                placeholder={
                  selectedProvinceCode
                    ? isFetchingWardsForForm
                      ? 'Đang tải phường/xã...'
                      : 'Chọn phường/xã'
                    : 'Chọn tỉnh/thành phố trước'
                }
                emptyText='Không tìm thấy phường/xã'
                disabled={isSaving || !selectedProvinceCode}
                loading={isFetchingWardsForForm}
              />
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='hub-address'>Địa chỉ chi tiết *</Label>
              <Input
                id='hub-address'
                value={formValues.address_detail}
                onChange={(e) =>
                  updateFormField('address_detail', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-phone'>Số điện thoại</Label>
              <Input
                id='hub-phone'
                value={formValues.phone_number}
                onChange={(e) =>
                  updateFormField('phone_number', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-daily-capacity'>
                Sức chứa đơn tại hub *
              </Label>
              <Input
                id='hub-daily-capacity'
                type='number'
                min={0}
                step={1}
                value={formValues.daily_capacity}
                onChange={(e) =>
                  updateFormField('daily_capacity', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-current-load'>Tải hiện tại *</Label>
              <Input
                id='hub-current-load'
                type='number'
                min={0}
                step={1}
                value={formValues.current_load}
                onChange={(e) =>
                  updateFormField('current_load', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-working-start'>Giờ bắt đầu (HH:mm)</Label>
              <Input
                id='hub-working-start'
                type='time'
                value={formValues.working_start_time}
                onChange={(e) =>
                  updateFormField('working_start_time', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-working-end'>Giờ kết thúc (HH:mm)</Label>
              <Input
                id='hub-working-end'
                type='time'
                value={formValues.working_end_time}
                onChange={(e) =>
                  updateFormField('working_end_time', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-latitude'>Vĩ độ</Label>
              <Input
                id='hub-latitude'
                value={formValues.latitude}
                onChange={(e) => updateFormField('latitude', e.target.value)}
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-longitude'>Kinh độ</Label>
              <Input
                id='hub-longitude'
                value={formValues.longitude}
                onChange={(e) => updateFormField('longitude', e.target.value)}
                disabled={isSaving}
              />
            </div>
          </div>

          <div className='space-y-2'>
            <div className='flex items-center justify-between gap-2'>
              <Label>Bản đồ vị trí</Label>
              <Button
                type='button'
                variant='outline'
                size='sm'
                disabled={isSaving || isGeocodingAddress}
                onClick={() => void onGeocodeAddress()}
              >
                {isGeocodingAddress ? (
                  <>
                    <Loader2 className='h-4 w-4 mr-1 animate-spin' />
                    Đang định vị...
                  </>
                ) : (
                  <>
                    <LocateFixed className='h-4 w-4 mr-1' />
                    Lấy tọa độ từ địa chỉ
                  </>
                )}
              </Button>
            </div>
            <CoordinatePickerMap
              latitude={mapLat}
              longitude={mapLng}
              disabled={isSaving}
              onChange={(latitude, longitude) => {
                updateFormField('latitude', String(latitude));
                updateFormField('longitude', String(longitude));
              }}
            />
            <p className='text-xs text-muted-foreground'>
              Chọn một điểm trên bản đồ hoặc nhập tọa độ. Để trống cả hai nếu
              chưa xác định.
            </p>
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
              {isSaving ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Đang lưu...
                </>
              ) : formMode === 'create' ? (
                'Tạo mới'
              ) : (
                'Lưu thay đổi'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
