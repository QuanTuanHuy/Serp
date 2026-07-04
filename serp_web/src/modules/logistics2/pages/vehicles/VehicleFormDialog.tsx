'use client';

import { Loader2, Truck } from 'lucide-react';
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import type { VehicleType } from '../../types';

export type VehicleFormMode = 'create' | 'edit';

export interface VehicleFormValues {
  licensePlate: string;
  vehicleType: VehicleType | '';
  maxWeightKg: string;
  maxVolumeCbm: string;
}

export interface VehicleFormErrors {
  licensePlate?: string;
  vehicleType?: string;
  maxWeightKg?: string;
  maxVolumeCbm?: string;
}

export interface VehicleTypeOption {
  value: VehicleType;
  label: string;
  description?: string;
}

interface VehicleFormDialogProps {
  open: boolean;
  mode: VehicleFormMode;
  values: VehicleFormValues;
  errors: VehicleFormErrors;
  isSubmitting: boolean;
  vehicleTypeOptions: VehicleTypeOption[];
  onOpenChange: (open: boolean) => void;
  onSubmit: () => void;
  onValueChange: <K extends keyof VehicleFormValues>(
    field: K,
    value: VehicleFormValues[K]
  ) => void;
}

export const VehicleFormDialog: React.FC<VehicleFormDialogProps> = ({
  open,
  mode,
  values,
  errors,
  isSubmitting,
  vehicleTypeOptions,
  onOpenChange,
  onSubmit,
  onValueChange,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <div className='flex items-start gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-500 text-white shadow-lg shadow-emerald-500/30'>
              <Truck className='h-5 w-5' />
            </div>
            <div>
              <DialogTitle>
                {mode === 'create' ? 'Tạo phương tiện' : 'Cập nhật phương tiện'}
              </DialogTitle>
              <DialogDescription className='mt-1'>
                {mode === 'create'
                  ? 'Điền thông tin để tạo phương tiện mới.'
                  : 'Cập nhật thông tin chính cho phương tiện.'}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='vehicle-license-plate'>Biển số</Label>
              <Input
                id='vehicle-license-plate'
                value={values.licensePlate}
                onChange={(event) =>
                  onValueChange('licensePlate', event.target.value)
                }
                disabled={isSubmitting}
                placeholder='VD: 51A-12345'
              />
              {errors.licensePlate && (
                <p className='text-xs text-destructive'>
                  {errors.licensePlate}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-type'>Loại xe</Label>
              <Select
                value={values.vehicleType || undefined}
                onValueChange={(value) =>
                  onValueChange('vehicleType', value as VehicleType)
                }
                disabled={isSubmitting}
              >
                <SelectTrigger id='vehicle-type' className='w-full'>
                  <SelectValue placeholder='Chọn loại xe' />
                </SelectTrigger>
                <SelectContent>
                  {vehicleTypeOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      <div className='flex flex-col'>
                        <span className='text-sm font-medium'>
                          {option.label}
                        </span>
                        {option.description && (
                          <span className='text-xs text-muted-foreground'>
                            {option.description}
                          </span>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.vehicleType && (
                <p className='text-xs text-destructive'>{errors.vehicleType}</p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-max-weight'>Tải trọng tối đa (kg)</Label>
              <Input
                id='vehicle-max-weight'
                type='number'
                min={0}
                step='0.1'
                value={values.maxWeightKg}
                onChange={(event) =>
                  onValueChange('maxWeightKg', event.target.value)
                }
                disabled={isSubmitting}
                placeholder='VD: 1200'
              />
              {errors.maxWeightKg && (
                <p className='text-xs text-destructive'>{errors.maxWeightKg}</p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-max-volume'>Thể tích tối đa (cbm)</Label>
              <Input
                id='vehicle-max-volume'
                type='number'
                min={0}
                step='0.1'
                value={values.maxVolumeCbm}
                onChange={(event) =>
                  onValueChange('maxVolumeCbm', event.target.value)
                }
                disabled={isSubmitting}
                placeholder='VD: 6.5'
              />
              {errors.maxVolumeCbm && (
                <p className='text-xs text-destructive'>
                  {errors.maxVolumeCbm}
                </p>
              )}
            </div>
          </div>

          <div className='rounded-xl border border-slate-200 bg-slate-50 p-3 text-xs text-slate-600'>
            Lưu ý: Nhập tải trọng theo kg và thể tích theo cbm. Hệ thống sẽ tính
            toán năng lực giao hàng dựa trên số liệu này.
          </div>
        </div>

        <DialogFooter className='gap-2 sm:gap-0'>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isSubmitting}
          >
            Hủy
          </Button>
          <Button type='button' onClick={onSubmit} disabled={isSubmitting}>
            {isSubmitting && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
            {mode === 'create' ? 'Tạo phương tiện' : 'Cập nhật phương tiện'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
