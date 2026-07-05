/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Driver handover check-in dialog
 */

import { Camera, Loader2, MapPin } from 'lucide-react';

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
import type { HandoverManifest } from '../../../types';

interface DriverCheckinDialogProps {
  checkinLatitude: string;
  checkinLongitude: string;
  checkinManifest: HandoverManifest | null;
  checkinTitle: string;
  isDevMode: boolean;
  isSubmitting: boolean;
  onCancel: () => void;
  onDevModeChange: (checked: boolean) => void;
  onLatitudeChange: (value: string) => void;
  onLongitudeChange: (value: string) => void;
  onOpenChange: (open: boolean) => void;
  onPhotoChange: (photo: File | null) => void;
  onSubmit: () => void;
  onUseCurrentLocation: () => void;
}

export function DriverCheckinDialog({
  checkinLatitude,
  checkinLongitude,
  checkinManifest,
  checkinTitle,
  isDevMode,
  isSubmitting,
  onCancel,
  onDevModeChange,
  onLatitudeChange,
  onLongitudeChange,
  onOpenChange,
  onPhotoChange,
  onSubmit,
  onUseCurrentLocation,
}: DriverCheckinDialogProps) {
  return (
    <Dialog open={Boolean(checkinManifest)} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{checkinTitle}</DialogTitle>
          <DialogDescription>
            Tải ảnh bàn giao và cung cấp tọa độ GPS cho điểm check-in này.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='flex items-center justify-between rounded-md border p-3'>
            <div className='space-y-1'>
              <Label htmlFor='handover-dev-mode'>Chế độ phát triển</Label>
              <p className='text-xs text-muted-foreground'>
                Tự điền tọa độ hợp lệ theo thông tin trong phiếu.
              </p>
            </div>
            <Switch
              id='handover-dev-mode'
              checked={isDevMode}
              onCheckedChange={onDevModeChange}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='handover-photo'>Ảnh</Label>
            <Input
              id='handover-photo'
              type='file'
              accept='image/*'
              capture='environment'
              onChange={(event) =>
                onPhotoChange(event.target.files?.[0] ?? null)
              }
            />
          </div>

          <div className='grid gap-3 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='handover-latitude'>Vĩ độ</Label>
              <Input
                id='handover-latitude'
                inputMode='decimal'
                value={checkinLatitude}
                onChange={(event) => onLatitudeChange(event.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='handover-longitude'>Kinh độ</Label>
              <Input
                id='handover-longitude'
                inputMode='decimal'
                value={checkinLongitude}
                onChange={(event) => onLongitudeChange(event.target.value)}
              />
            </div>
          </div>

          <Button
            type='button'
            variant='outline'
            onClick={onUseCurrentLocation}
          >
            <MapPin className='mr-2 h-4 w-4' />
            Dùng vị trí hiện tại
          </Button>
        </div>

        <DialogFooter>
          <Button type='button' variant='outline' onClick={onCancel}>
            Hủy
          </Button>
          <Button type='button' disabled={isSubmitting} onClick={onSubmit}>
            {isSubmitting ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <Camera className='mr-2 h-4 w-4' />
            )}
            Gửi
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
