'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { toast } from 'sonner';
import {
  useUpdateAddressMutation,
  useCreateAddressMutation,
} from '../../api/logisticsApi';
import type {
  Address,
  AddressType,
  EntityType,
  AddressUpdateForm,
  AddressCreationForm,
} from '../../types';

const ADDRESS_TYPE_OPTIONS: { value: AddressType; label: string }[] = [
  { value: 'SHIPPING', label: 'Giao hàng' },
  { value: 'FACILITY', label: 'Kho' },
  { value: 'BUSINESS', label: 'Cơ sở' },
];

interface UpdateAddressDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  entityId: string;
  entityType?: EntityType;
  currentAddress?: Address;
}

export const UpdateAddressDialog: React.FC<UpdateAddressDialogProps> = ({
  open,
  onOpenChange,
  entityId,
  entityType = 'CUSTOMER',
  currentAddress,
}) => {
  const [fullAddress, setFullAddress] = useState('');
  const [addressType, setAddressType] = useState<AddressType>('SHIPPING');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');

  const [updateAddress, { isLoading: isUpdating }] = useUpdateAddressMutation();
  const [createAddress, { isLoading: isCreating }] = useCreateAddressMutation();

  const isLoading = isUpdating || isCreating;

  useEffect(() => {
    if (open) {
      setFullAddress(currentAddress?.fullAddress ?? '');
      setAddressType(currentAddress?.addressType ?? 'SHIPPING');
      setLatitude(currentAddress?.latitude?.toString() ?? '');
      setLongitude(currentAddress?.longitude?.toString() ?? '');
    }
  }, [open, currentAddress]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!fullAddress.trim()) {
      toast.error('Vui lòng nhập địa chỉ.');
      return;
    }

    const lat = latitude ? parseFloat(latitude) : 0;
    const lng = longitude ? parseFloat(longitude) : 0;

    try {
      if (currentAddress) {
        const data: AddressUpdateForm = {
          fullAddress: fullAddress.trim(),
          addressType,
          latitude: lat,
          longitude: lng,
        };
        await updateAddress({ addressId: currentAddress.id, data }).unwrap();
      } else {
        const data: AddressCreationForm = {
          entityId,
          entityType,
          addressType,
          fullAddress: fullAddress.trim(),
          latitude: lat,
          longitude: lng,
          isDefault: true,
        };
        await createAddress(data).unwrap();
      }

      toast.success('Địa chỉ đã được cập nhật.');
      onOpenChange(false);
    } catch {
      toast.error('Đã xảy ra lỗi khi cập nhật địa chỉ. Vui lòng thử lại.');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Thay đổi địa chỉ</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4 py-2'>
          <div className='space-y-2'>
            <Label htmlFor='addressType'>Loại địa chỉ</Label>
            <Select
              value={addressType}
              onValueChange={(v) => setAddressType(v as AddressType)}
              disabled={isLoading}
            >
              <SelectTrigger id='addressType'>
                <SelectValue placeholder='Chọn loại địa chỉ' />
              </SelectTrigger>
              <SelectContent>
                {ADDRESS_TYPE_OPTIONS.map((opt) => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='fullAddress'>Địa chỉ</Label>
            <Input
              id='fullAddress'
              placeholder='Nhập địa chỉ đầy đủ'
              value={fullAddress}
              onChange={(e) => setFullAddress(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='latitude'>Vĩ độ</Label>
              <Input
                id='latitude'
                type='number'
                step='any'
                placeholder='VD: 10.7769'
                value={latitude}
                onChange={(e) => setLatitude(e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='longitude'>Kinh độ</Label>
              <Input
                id='longitude'
                type='number'
                step='any'
                placeholder='VD: 106.7009'
                value={longitude}
                onChange={(e) => setLongitude(e.target.value)}
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
              {isLoading ? 'Đang lưu...' : 'Lưu'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
