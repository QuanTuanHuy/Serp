/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Surcharge rule admin form dialog
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
  CALCULATION_TYPE_OPTIONS,
  SURCHARGE_RULE_CODE_OPTIONS,
  type SurchargeRuleFormState,
} from '../billingPageModels';

export type SurchargeRuleFormMode = 'create' | 'edit';

interface SurchargeRuleFormDialogProps {
  open: boolean;
  mode: SurchargeRuleFormMode;
  form: SurchargeRuleFormState;
  onFormChange: React.Dispatch<React.SetStateAction<SurchargeRuleFormState>>;
  calculationTypeHelper?: string;
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
}

export const SurchargeRuleFormDialog: React.FC<
  SurchargeRuleFormDialogProps
> = ({
  open,
  mode,
  form,
  onFormChange,
  calculationTypeHelper,
  isLoading,
  onOpenChange,
  onSubmit,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[85vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            {mode === 'create' ? 'Tạo quy tắc phụ phí' : 'Sửa quy tắc phụ phí'}
          </DialogTitle>
          <DialogDescription>
            Cấu hình logic phí bổ sung cho các đơn giao khu vực xa.
          </DialogDescription>
        </DialogHeader>

        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='surchargeCode'>Mã quy tắc</Label>
              <TmsCombobox
                id='surchargeCode'
                value={form.code}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    code: value as SurchargeRuleFormState['code'],
                    name: prev.name || value,
                  }))
                }
                options={SURCHARGE_RULE_CODE_OPTIONS}
                placeholder='Chọn mã quy tắc'
                emptyText='Không tìm thấy mã quy tắc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeName'>Tên hiển thị</Label>
              <Input
                id='surchargeName'
                value={form.name}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    name: event.target.value,
                  }))
                }
                placeholder='Ví dụ: Phụ phí vùng xa'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeCalculationType'>Cách tính</Label>
              <TmsCombobox
                id='surchargeCalculationType'
                value={form.calculationType}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    calculationType:
                      value as SurchargeRuleFormState['calculationType'],
                  }))
                }
                options={CALCULATION_TYPE_OPTIONS}
                placeholder='Chọn cách tính'
                emptyText='Không tìm thấy cách tính'
                disabled={isLoading}
              />
              {calculationTypeHelper ? (
                <p className='text-xs text-muted-foreground'>
                  {calculationTypeHelper}
                </p>
              ) : null}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeRatePercent'>Tỷ lệ phần trăm</Label>
              <Input
                id='surchargeRatePercent'
                inputMode='decimal'
                value={form.ratePercent}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    ratePercent: event.target.value,
                  }))
                }
                placeholder='Không bắt buộc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeFixedAmount'>Số tiền cố định</Label>
              <Input
                id='surchargeFixedAmount'
                inputMode='decimal'
                value={form.fixedAmount}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    fixedAmount: event.target.value,
                  }))
                }
                placeholder='Không bắt buộc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeMinAmount'>Số tiền tối thiểu</Label>
              <Input
                id='surchargeMinAmount'
                inputMode='decimal'
                value={form.minAmount}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    minAmount: event.target.value,
                  }))
                }
                placeholder='Không bắt buộc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeBaseWeight'>Khối lượng cơ bản</Label>
              <Input
                id='surchargeBaseWeight'
                inputMode='decimal'
                value={form.baseWeight}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    baseWeight: event.target.value,
                  }))
                }
                placeholder='Không bắt buộc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeBasePrice'>Giá cơ bản</Label>
              <Input
                id='surchargeBasePrice'
                inputMode='decimal'
                value={form.basePrice}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    basePrice: event.target.value,
                  }))
                }
                placeholder='Không bắt buộc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeStepWeight'>Khối lượng mỗi bước</Label>
              <Input
                id='surchargeStepWeight'
                inputMode='decimal'
                value={form.stepWeight}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    stepWeight: event.target.value,
                  }))
                }
                placeholder='Không bắt buộc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeStepPrice'>Giá mỗi bước</Label>
              <Input
                id='surchargeStepPrice'
                inputMode='decimal'
                value={form.stepPrice}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    stepPrice: event.target.value,
                  }))
                }
                placeholder='Không bắt buộc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeEffectiveDate'>Ngày hiệu lực</Label>
              <Input
                id='surchargeEffectiveDate'
                type='date'
                value={form.effectiveDate}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    effectiveDate: event.target.value,
                  }))
                }
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeExpirationDate'>Ngày hết hạn</Label>
              <Input
                id='surchargeExpirationDate'
                type='date'
                value={form.expirationDate}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    expirationDate: event.target.value,
                  }))
                }
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
                'Tạo quy tắc phụ phí'
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
