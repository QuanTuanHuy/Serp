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
            Upload a handover photo and provide the GPS location for this driver
            checkpoint.
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='flex items-center justify-between rounded-md border p-3'>
            <div className='space-y-1'>
              <Label htmlFor='handover-dev-mode'>Development mode</Label>
              <p className='text-xs text-muted-foreground'>
                Fill valid checkpoint coordinates from the manifest.
              </p>
            </div>
            <Switch
              id='handover-dev-mode'
              checked={isDevMode}
              onCheckedChange={onDevModeChange}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='handover-photo'>Photo</Label>
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
              <Label htmlFor='handover-latitude'>Latitude</Label>
              <Input
                id='handover-latitude'
                inputMode='decimal'
                value={checkinLatitude}
                onChange={(event) => onLatitudeChange(event.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='handover-longitude'>Longitude</Label>
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
            Use current location
          </Button>
        </div>

        <DialogFooter>
          <Button type='button' variant='outline' onClick={onCancel}>
            Cancel
          </Button>
          <Button type='button' disabled={isSubmitting} onClick={onSubmit}>
            {isSubmitting ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <Camera className='mr-2 h-4 w-4' />
            )}
            Submit
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
