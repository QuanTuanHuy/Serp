/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Chargeable weight config admin dialog
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
import {
  DELIVERY_SERVICE_OPTIONS,
  type ChargeableWeightConfigFormState,
} from '../billingPageModels';

export type ChargeableWeightConfigFormMode = 'create' | 'edit';

interface ChargeableWeightConfigFormDialogProps {
  open: boolean;
  mode: ChargeableWeightConfigFormMode;
  form: ChargeableWeightConfigFormState;
  onFormChange: React.Dispatch<
    React.SetStateAction<ChargeableWeightConfigFormState>
  >;
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
}

export const ChargeableWeightConfigFormDialog: React.FC<
  ChargeableWeightConfigFormDialogProps
> = ({
  open,
  mode,
  form,
  onFormChange,
  isLoading,
  onOpenChange,
  onSubmit,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[85vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            {mode === 'create'
              ? 'Tạo cấu hình khối lượng'
              : 'Sửa cấu hình khối lượng'}
          </DialogTitle>
          <DialogDescription>
            Thiết lập công thức quy đổi khối lượng tính cước cho từng hình thức
            vận chuyển.
          </DialogDescription>
        </DialogHeader>

        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='chargeableServiceCode'>Mã dịch vụ</Label>
              <TmsCombobox
                id='chargeableServiceCode'
                value={form.serviceCode}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    serviceCode:
                      value as ChargeableWeightConfigFormState['serviceCode'],
                  }))
                }
                options={DELIVERY_SERVICE_OPTIONS}
                placeholder='Chọn dịch vụ'
                emptyText='Không tìm thấy dịch vụ'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='minDimensionCm'>Kích thước tối thiểu (cm)</Label>
              <Input
                id='minDimensionCm'
                inputMode='decimal'
                value={form.minDimensionCm}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    minDimensionCm: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 10'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='smallBulkyThresholdCm'>
                Ngưỡng hàng cồng kềnh (cm)
              </Label>
              <Input
                id='smallBulkyThresholdCm'
                inputMode='decimal'
                value={form.smallBulkyThresholdCm}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    smallBulkyThresholdCm: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 100'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='baseWeightGram'>Khối lượng gốc (gram)</Label>
              <Input
                id='baseWeightGram'
                inputMode='decimal'
                value={form.baseWeightGram}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    baseWeightGram: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 2000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='stepWeightGram'>Nấc làm tròn (gram)</Label>
              <Input
                id='stepWeightGram'
                inputMode='decimal'
                value={form.stepWeightGram}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    stepWeightGram: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 500'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='maxWeightGram'>Khối lượng tối đa (gram)</Label>
              <Input
                id='maxWeightGram'
                inputMode='decimal'
                value={form.maxWeightGram}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    maxWeightGram: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 15000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='volumetricDivisor'>
                Hệ số quy đổi thể tích
              </Label>
              <Input
                id='volumetricDivisor'
                inputMode='decimal'
                value={form.volumetricDivisor}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    volumetricDivisor: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 5000'
                disabled={isLoading}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  Đang lưu...
                </>
              ) : mode === 'create' ? (
                'Tạo cấu hình'
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
