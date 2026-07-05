/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - Post office form dialog
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
import type { Province, Ward } from '../../../../types';
import {
  normalizeLocationCode,
  POST_OFFICE_STATUS_OPTIONS,
  type PostOfficeFormState,
  type PostOfficeFormMode,
  type UpdatePostOfficeFormField,
} from '../postOfficeForm';

interface PostOfficeFormDialogProps {
  open: boolean;
  formMode: PostOfficeFormMode;
  isSaving: boolean;
  formValues: PostOfficeFormState;
  selectedProvinceCode: string;
  selectedWardCode: string;
  provinceSelectOptions: Province[];
  wardSelectOptions: Ward[];
  isFetchingWardsForForm: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  updateFormField: UpdatePostOfficeFormField;
}

export const PostOfficeFormDialog: React.FC<PostOfficeFormDialogProps> = ({
  open,
  formMode,
  isSaving,
  formValues,
  selectedProvinceCode,
  selectedWardCode,
  provinceSelectOptions,
  wardSelectOptions,
  isFetchingWardsForForm,
  onOpenChange,
  onSubmit,
  updateFormField,
}) => {
  const provinceOptions = provinceSelectOptions.flatMap((province) => {
    const provinceCode = normalizeLocationCode(province.provinceCode);

    return provinceCode
      ? [
          {
            value: provinceCode,
            label: `${province.name} (${provinceCode})`,
          },
        ]
      : [];
  });
  const wardOptions = wardSelectOptions.flatMap((ward) => {
    const wardCode = normalizeLocationCode(ward.wardCode);

    return wardCode
      ? [
          {
            value: wardCode,
            label: `${ward.name} (${wardCode})`,
          },
        ]
      : [];
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl max-h-[85vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            {formMode === 'create' ? 'Tạo bưu cục' : 'Cập nhật bưu cục'}
          </DialogTitle>
          <DialogDescription>
            Nhập các thông tin bắt buộc để{' '}
            {formMode === 'create' ? 'tạo bưu cục mới' : 'cập nhật bưu cục'}.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='post-office-code'>Mã bưu cục *</Label>
              <Input
                id='post-office-code'
                value={formValues.code}
                onChange={(event) =>
                  updateFormField('code', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-name'>Tên bưu cục *</Label>
              <Input
                id='post-office-name'
                value={formValues.name}
                onChange={(event) =>
                  updateFormField('name', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-province'>Tỉnh/Thành phố *</Label>
              <TmsCombobox
                id='post-office-province'
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
              <Label htmlFor='post-office-ward'>Phường/Xã *</Label>
              <TmsCombobox
                id='post-office-ward'
                value={selectedWardCode}
                onValueChange={(value) => updateFormField('ward_code', value)}
                options={wardOptions}
                placeholder={
                  selectedProvinceCode
                    ? 'Chọn phường/xã'
                    : 'Chọn tỉnh/thành phố trước'
                }
                emptyText={
                  selectedProvinceCode && isFetchingWardsForForm
                    ? 'Đang tải phường/xã...'
                    : 'Không có phường/xã phù hợp.'
                }
                disabled={isSaving || !selectedProvinceCode}
                loading={isFetchingWardsForForm}
              />
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='post-office-address'>Địa chỉ chi tiết *</Label>
              <Input
                id='post-office-address'
                value={formValues.address_detail}
                onChange={(event) =>
                  updateFormField('address_detail', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-phone'>Số điện thoại</Label>
              <Input
                id='post-office-phone'
                value={formValues.phone_number}
                onChange={(event) =>
                  updateFormField('phone_number', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-status'>Trạng thái *</Label>
              <TmsCombobox
                id='post-office-status'
                value={formValues.status}
                onValueChange={(value) =>
                  updateFormField(
                    'status',
                    value as PostOfficeFormState['status']
                  )
                }
                options={POST_OFFICE_STATUS_OPTIONS}
                placeholder='Chọn trạng thái'
                emptyText='Không tìm thấy trạng thái'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-service-radius'>
                Bán kính phục vụ (m) *
              </Label>
              <Input
                id='post-office-service-radius'
                type='number'
                min={1}
                value={formValues.service_radius_m}
                onChange={(event) =>
                  updateFormField('service_radius_m', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-daily-capacity'>
                Sức chứa lấy hàng *
              </Label>
              <Input
                id='post-office-daily-capacity'
                type='number'
                min={0}
                value={formValues.daily_capacity}
                onChange={(event) =>
                  updateFormField('daily_capacity', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-current-load'>
                Tải lấy hàng hiện tại *
              </Label>
              <Input
                id='post-office-current-load'
                type='number'
                min={0}
                value={formValues.current_load}
                onChange={(event) =>
                  updateFormField('current_load', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-delivery-capacity'>
                Sức chứa giao hàng *
              </Label>
              <Input
                id='post-office-delivery-capacity'
                type='number'
                min={0}
                value={formValues.delivery_capacity}
                onChange={(event) =>
                  updateFormField('delivery_capacity', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-current-delivery-load'>
                Tải giao hàng hiện tại *
              </Label>
              <Input
                id='post-office-current-delivery-load'
                type='number'
                min={0}
                value={formValues.current_delivery_load}
                onChange={(event) =>
                  updateFormField('current_delivery_load', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-priority'>Độ ưu tiên *</Label>
              <Input
                id='post-office-priority'
                type='number'
                min={0}
                value={formValues.priority}
                onChange={(event) =>
                  updateFormField('priority', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-latitude'>Vĩ độ</Label>
              <Input
                id='post-office-latitude'
                type='number'
                step='any'
                min={-90}
                max={90}
                value={formValues.latitude}
                onChange={(event) =>
                  updateFormField('latitude', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-longitude'>Kinh độ</Label>
              <Input
                id='post-office-longitude'
                type='number'
                step='any'
                min={-180}
                max={180}
                value={formValues.longitude}
                onChange={(event) =>
                  updateFormField('longitude', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-start-date'>
                Ngày bắt đầu hoạt động
              </Label>
              <Input
                id='post-office-start-date'
                type='date'
                value={formValues.operational_start_date}
                onChange={(event) =>
                  updateFormField('operational_start_date', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-end-date'>
                Ngày kết thúc hoạt động
              </Label>
              <Input
                id='post-office-end-date'
                type='date'
                value={formValues.operational_end_date}
                onChange={(event) =>
                  updateFormField('operational_end_date', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-start-time'>
                Giờ bắt đầu làm việc
              </Label>
              <Input
                id='post-office-start-time'
                type='time'
                value={formValues.working_start_time}
                onChange={(event) =>
                  updateFormField('working_start_time', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='post-office-end-time'>
                Giờ kết thúc làm việc
              </Label>
              <Input
                id='post-office-end-time'
                type='time'
                value={formValues.working_end_time}
                onChange={(event) =>
                  updateFormField('working_end_time', event.target.value)
                }
                disabled={isSaving}
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
              {isSaving && <Loader2 className='h-4 w-4 mr-2 animate-spin' />}
              {formMode === 'create' ? 'Tạo mới' : 'Lưu thay đổi'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
