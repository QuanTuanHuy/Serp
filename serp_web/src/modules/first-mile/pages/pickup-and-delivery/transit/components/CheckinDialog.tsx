/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution check-in dialog
 */

import { CheckCircle2, Loader2 } from 'lucide-react';

import { Button } from '@/shared/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';

import type { BagDistributionManifest } from '../../../../types';
import type { CheckinMode, CheckinState } from '../transitModels';
import { destinationLabel } from '../transitModels';

interface CheckinDialogProps {
  target: { manifest: BagDistributionManifest; mode: CheckinMode } | null;
  state: CheckinState;
  isLoading: boolean;
  onStateChange: (state: CheckinState) => void;
  onOpenChange: (open: boolean) => void;
  onSubmit: () => void;
}

export function CheckinDialog({
  target,
  state,
  isLoading,
  onStateChange,
  onOpenChange,
  onSubmit,
}: CheckinDialogProps) {
  const isStart = target?.mode === 'start';

  return (
    <Dialog open={Boolean(target)} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {isStart ? 'Check-in lúc xuất phát' : 'Check-in lúc đến nơi'}
          </DialogTitle>
          <DialogDescription>
            Nhập vị trí ghi nhận và tải lên ảnh xác minh tại điểm check-in của
            tài xế.
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='rounded-md border bg-muted/30 p-3 text-sm'>
            <p className='font-medium'>
              {target?.manifest.manifestCode ??
                `Biên bản #${target?.manifest.id ?? '-'}`}
            </p>
            <p className='text-muted-foreground'>
              {target
                ? `${target.manifest.originHubCode ?? target.manifest.originHubId} đến ${destinationLabel(
                    target.manifest.destinationType,
                    target.manifest.destinationHubId,
                    target.manifest.destinationHubCode,
                    target.manifest.destinationPostOfficeCode
                  )}`
                : '-'}
            </p>
          </div>
          <div className='grid gap-3 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='checkin-latitude'>Vĩ độ *</Label>
              <Input
                id='checkin-latitude'
                value={state.latitude}
                onChange={(event) =>
                  onStateChange({ ...state, latitude: event.target.value })
                }
                placeholder='10.762622'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='checkin-longitude'>Kinh độ *</Label>
              <Input
                id='checkin-longitude'
                value={state.longitude}
                onChange={(event) =>
                  onStateChange({ ...state, longitude: event.target.value })
                }
                placeholder='106.660172'
              />
            </div>
          </div>
          <div className='space-y-2'>
            <Label htmlFor='checkin-label'>Tên vị trí</Label>
            <Input
              id='checkin-label'
              value={state.locationLabel}
              onChange={(event) =>
                onStateChange({ ...state, locationLabel: event.target.value })
              }
              placeholder='Cổng hub xuất phát'
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='checkin-photo'>Ảnh xác minh *</Label>
            <Input
              id='checkin-photo'
              type='file'
              accept='image/*'
              onChange={(event) =>
                onStateChange({
                  ...state,
                  photo: event.target.files?.[0],
                })
              }
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Hủy
          </Button>
          <Button disabled={isLoading} onClick={onSubmit}>
            {isLoading ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <CheckCircle2 className='h-4 w-4' />
            )}
            Gửi check-in
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
