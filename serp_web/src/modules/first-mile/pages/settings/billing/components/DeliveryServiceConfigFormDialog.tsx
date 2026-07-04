/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Delivery service config admin dialog
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
  Switch,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type { DeliveryServiceConfigFormState } from '../billingPageModels';

export type DeliveryServiceConfigFormMode = 'create' | 'edit';

interface DeliveryServiceConfigFormDialogProps {
  open: boolean;
  mode: DeliveryServiceConfigFormMode;
  form: DeliveryServiceConfigFormState;
  onFormChange: React.Dispatch<
    React.SetStateAction<DeliveryServiceConfigFormState>
  >;
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
}

export const DeliveryServiceConfigFormDialog: React.FC<
  DeliveryServiceConfigFormDialogProps
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
      <DialogContent className='max-h-[85vh] overflow-y-auto sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>
            {mode === 'create'
              ? 'Tạo hình thức vận chuyển'
              : 'Sửa hình thức vận chuyển'}
          </DialogTitle>
          <DialogDescription>
            Cấu hình mã dịch vụ, tên hiển thị và trạng thái sử dụng trong công
            thức tính phí.
          </DialogDescription>
        </DialogHeader>

        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='deliveryServiceCode'>Mã dịch vụ</Label>
              <Input
                id='deliveryServiceCode'
                value={form.serviceCode}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    serviceCode: event.target.value.toUpperCase(),
                  }))
                }
                placeholder='Ví dụ: HOA_TOC'
                disabled={isLoading || mode === 'edit'}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='deliveryServiceName'>Tên hiển thị</Label>
              <Input
                id='deliveryServiceName'
                value={form.name}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    name: event.target.value,
                  }))
                }
                placeholder='Ví dụ: Hỏa tốc'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='deliveryServiceDescription'>Mô tả</Label>
              <Input
                id='deliveryServiceDescription'
                value={form.description}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    description: event.target.value,
                  }))
                }
                placeholder='Mô tả ngắn cho người vận hành'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='deliveryServiceSortOrder'>Thứ tự hiển thị</Label>
              <Input
                id='deliveryServiceSortOrder'
                inputMode='numeric'
                value={form.sortOrder}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    sortOrder: event.target.value,
                  }))
                }
                placeholder='Ví dụ: 10'
                disabled={isLoading}
              />
            </div>

            <div className='flex items-center justify-between gap-4 rounded-md border p-3'>
              <div className='space-y-1'>
                <Label htmlFor='deliveryServiceActive'>Đang hoạt động</Label>
                <p className='text-xs text-muted-foreground'>
                  Chỉ dịch vụ đang hoạt động mới xuất hiện khi tính phí.
                </p>
              </div>
              <Switch
                id='deliveryServiceActive'
                checked={form.active}
                onCheckedChange={(checked) =>
                  onFormChange((prev) => ({
                    ...prev,
                    active: checked,
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
                'Tạo hình thức'
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
