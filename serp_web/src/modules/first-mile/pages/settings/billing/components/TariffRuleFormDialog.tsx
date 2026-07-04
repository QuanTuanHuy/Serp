/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Tariff rule admin form dialog
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
  ROUTE_TYPE_OPTIONS,
  type BillingDeliveryServiceOption,
  type TariffFormState,
} from '../billingPageModels';

export type TariffRuleFormMode = 'create' | 'edit';

interface TariffRuleFormDialogProps {
  open: boolean;
  mode: TariffRuleFormMode;
  form: TariffFormState;
  deliveryServiceOptions: BillingDeliveryServiceOption[];
  onFormChange: React.Dispatch<React.SetStateAction<TariffFormState>>;
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
}

export const TariffRuleFormDialog: React.FC<TariffRuleFormDialogProps> = ({
  open,
  mode,
  form,
  deliveryServiceOptions,
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
              ? 'Tạo quy tắc biểu phí'
              : 'Sửa quy tắc biểu phí'}
          </DialogTitle>
          <DialogDescription>
            Cấu hình giá cơ bản và giá theo bước tăng cho từng dịch vụ, loại
            tuyến.
          </DialogDescription>
        </DialogHeader>

        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='tariffServiceCode'>Mã dịch vụ</Label>
              <TmsCombobox
                id='tariffServiceCode'
                value={form.serviceCode}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    serviceCode: value as TariffFormState['serviceCode'],
                  }))
                }
                options={deliveryServiceOptions}
                placeholder='Chọn dịch vụ'
                emptyText='Không tìm thấy dịch vụ'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='tariffRouteType'>Loại tuyến</Label>
              <TmsCombobox
                id='tariffRouteType'
                value={form.routeTypeCode}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    routeTypeCode: value as TariffFormState['routeTypeCode'],
                  }))
                }
                options={ROUTE_TYPE_OPTIONS}
                placeholder='Chọn loại tuyến'
                emptyText='Không tìm thấy loại tuyến'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='baseWeight'>Khối lượng cơ bản</Label>
              <Input
                id='baseWeight'
                inputMode='decimal'
                value={form.baseWeight}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    baseWeight: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 2000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='basePrice'>Giá cơ bản</Label>
              <Input
                id='basePrice'
                inputMode='decimal'
                value={form.basePrice}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    basePrice: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 25000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='stepWeight'>Khối lượng mỗi bước</Label>
              <Input
                id='stepWeight'
                inputMode='decimal'
                value={form.stepWeight}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    stepWeight: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 500'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='stepPrice'>Giá mỗi bước</Label>
              <Input
                id='stepPrice'
                inputMode='decimal'
                value={form.stepPrice}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    stepPrice: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 5000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='effectiveDate'>Ngày hiệu lực</Label>
              <Input
                id='effectiveDate'
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
              <Label htmlFor='expirationDate'>Ngày hết hạn</Label>
              <Input
                id='expirationDate'
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
                'Tạo quy tắc biểu phí'
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
