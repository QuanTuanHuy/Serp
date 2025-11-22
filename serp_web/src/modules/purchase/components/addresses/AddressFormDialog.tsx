/*
Author: QuanTuanHuy
Description: Part of Serp Project - Address form dialog wrapper
*/

'use client';

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { AddressForm } from './AddressForm';
import type { Address } from '../../types';

interface AddressFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  address?: Address;
  onSubmit: (data: any) => void;
  isSubmitting?: boolean;
  mode: 'create' | 'edit' | 'view';
}

export function AddressFormDialog({
  open,
  onOpenChange,
  address,
  onSubmit,
  isSubmitting = false,
  mode,
}: AddressFormDialogProps) {
  const title =
    mode === 'create'
      ? 'Tạo địa chỉ mới'
      : mode === 'edit'
        ? 'Chỉnh sửa địa chỉ'
        : 'Xem thông tin địa chỉ';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-3xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        <AddressForm
          address={address}
          onSubmit={onSubmit}
          onCancel={() => onOpenChange(false)}
          isSubmitting={isSubmitting}
          mode={mode}
        />
      </DialogContent>
    </Dialog>
  );
}
