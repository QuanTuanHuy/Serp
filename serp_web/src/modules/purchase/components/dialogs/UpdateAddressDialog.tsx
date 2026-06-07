// Purchase UpdateAddressDialog Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useEffect, useState } from 'react';
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
import { MapLocationPicker } from '@/shared/components';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import {
  useUpdateAddressMutation,
  useCreateAddressMutation,
} from '../../api/purchaseApi';
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

const DEFAULT_MAP_CENTER = {
  lat: 16.047079,
  lng: 108.20623,
};

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
  entityType = 'FACILITY',
  currentAddress,
}) => {
  const [fullAddress, setFullAddress] = useState('');
  const [addressType, setAddressType] = useState<AddressType>('SHIPPING');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [mapCenter, setMapCenter] = useState(DEFAULT_MAP_CENTER);

  const [updateAddress, { isLoading: isUpdating }] = useUpdateAddressMutation();
  const [createAddress, { isLoading: isCreating }] = useCreateAddressMutation();

  const isLoading = isUpdating || isCreating;
  const hasLocationError = !latitude.trim() || !longitude.trim();

  useEffect(() => {
    if (!open) {
      return;
    }

    const initialLatitude = currentAddress?.latitude ?? DEFAULT_MAP_CENTER.lat;
    const initialLongitude =
      currentAddress?.longitude ?? DEFAULT_MAP_CENTER.lng;

    setFullAddress(currentAddress?.fullAddress ?? '');
    setAddressType(currentAddress?.addressType ?? 'SHIPPING');
    setLatitude(currentAddress?.latitude?.toString() ?? '');
    setLongitude(currentAddress?.longitude?.toString() ?? '');
    setMapCenter({ lat: initialLatitude, lng: initialLongitude });
  }, [open, currentAddress]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!fullAddress.trim()) {
      toast.error('Vui lòng nhập địa chỉ.');
      return;
    }

    if (hasLocationError) {
      toast.error('Vui lòng chọn vị trí trên bản đồ.');
      return;
    }

    const lat = parseFloat(latitude);
    const lng = parseFloat(longitude);

    try {
      if (currentAddress) {
        const data: AddressUpdateForm = {
          fullAddress: fullAddress.trim(),
          addressType,
          latitude: lat,
          longitude: lng,
          default: currentAddress.default,
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
          default: true,
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

          <div className='space-y-2'>
            <Label>Chọn vị trí trên bản đồ để cập nhật kinh độ và vĩ độ</Label>
            <MapLocationPicker
              initialLat={mapCenter.lat}
              initialLng={mapCenter.lng}
              className={cn(hasLocationError && 'border-destructive')}
              disabled={isLoading}
              onLocationSelect={(lat, lng) => {
                setLatitude(lat.toString());
                setLongitude(lng.toString());
                setMapCenter({ lat, lng });
              }}
            />
            {hasLocationError && (
              <p className='text-sm text-destructive'>
                Vui lòng chọn vị trí trên bản đồ.
              </p>
            )}
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
