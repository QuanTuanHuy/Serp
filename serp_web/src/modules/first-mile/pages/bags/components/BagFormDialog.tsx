/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag form dialog
 */

import type React from 'react';

import { Loader2 } from 'lucide-react';

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
  Textarea,
} from '@/shared/components';

import { TmsCombobox } from '../../../components/TmsCombobox';
import type { Hub } from '../../../types';
import {
  BAG_DESTINATION_TYPE_OPTIONS,
  type BagFormValues,
} from '../bagPageModels';

interface BagFormDialogProps {
  open: boolean;
  mode: 'create' | 'edit';
  values: BagFormValues;
  hubs: Hub[];
  destinationPostOfficeOptions: Array<{ value: string; label: string }>;
  isLoadingDestinationPostOffices: boolean;
  isSaving: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onUpdateField: <K extends keyof BagFormValues>(
    field: K,
    value: BagFormValues[K]
  ) => void;
}

export function BagFormDialog({
  open,
  mode,
  values,
  hubs,
  destinationPostOfficeOptions,
  isLoadingDestinationPostOffices,
  isSaving,
  onOpenChange,
  onSubmit,
  onUpdateField,
}: BagFormDialogProps) {
  const hubOptions = hubs.map((hub) => ({
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  }));

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            {mode === 'create' ? 'Tạo túi mới' : 'Sửa túi'}
          </DialogTitle>
          <DialogDescription>
            Cấu hình điểm đến và sức chứa trước khi quét đơn vào túi.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='bag-code'>Mã túi *</Label>
              <Input
                id='bag-code'
                value={values.bagCode}
                onChange={(event) =>
                  onUpdateField('bagCode', event.target.value)
                }
                disabled={isSaving}
                placeholder='BAG-001'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='bag-origin-hub'>Hub gốc *</Label>
              <TmsCombobox
                id='bag-origin-hub'
                value={values.originHubId}
                onValueChange={(value) => {
                  onUpdateField('originHubId', value);
                  if (values.destinationHubId === value) {
                    onUpdateField('destinationHubId', '');
                  }
                  onUpdateField('destinationPostOfficeCode', '');
                }}
                options={hubOptions}
                placeholder='Chọn hub gốc'
                emptyText='Không tìm thấy hub'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='bag-destination-type'>Loại điểm đến *</Label>
              <TmsCombobox
                id='bag-destination-type'
                value={values.destinationType}
                onValueChange={(value) => {
                  onUpdateField(
                    'destinationType',
                    value as BagFormValues['destinationType']
                  );
                  onUpdateField('destinationHubId', '');
                  onUpdateField('destinationPostOfficeCode', '');
                }}
                options={BAG_DESTINATION_TYPE_OPTIONS}
                placeholder='Chọn loại điểm đến'
                emptyText='Không tìm thấy loại điểm đến'
                disabled={isSaving}
              />
            </div>

            {values.destinationType === 'HUB' ? (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-hub'>Hub đích *</Label>
                <TmsCombobox
                  id='bag-destination-hub'
                  value={values.destinationHubId}
                  onValueChange={(value) => {
                    onUpdateField('destinationHubId', value);
                  }}
                  options={hubOptions.filter(
                    (hub) => hub.value !== values.originHubId
                  )}
                  placeholder='Chọn hub đích'
                  emptyText='Không tìm thấy hub đích'
                  disabled={isSaving}
                />
              </div>
            ) : (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-post-office'>
                  Bưu cục đích *
                </Label>
                <TmsCombobox
                  id='bag-destination-post-office'
                  value={values.destinationPostOfficeCode}
                  onValueChange={(value) => {
                    onUpdateField('destinationPostOfficeCode', value);
                  }}
                  options={destinationPostOfficeOptions}
                  disabled={isSaving || isLoadingDestinationPostOffices}
                  loading={isLoadingDestinationPostOffices}
                  placeholder={
                    values.originHubId
                      ? 'Chọn bưu cục đích'
                      : 'Chọn hub gốc trước'
                  }
                  emptyText='Không tìm thấy bưu cục được liên kết'
                />
              </div>
            )}

            <div className='grid grid-cols-3 gap-3 sm:col-span-2'>
              <div className='space-y-2'>
                <Label htmlFor='bag-max-weight'>Khối lượng tối đa (kg)</Label>
                <Input
                  id='bag-max-weight'
                  type='number'
                  min='0'
                  step='0.01'
                  value={values.maxWeight}
                  onChange={(event) =>
                    onUpdateField('maxWeight', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-max-volume'>Thể tích tối đa (m3)</Label>
                <Input
                  id='bag-max-volume'
                  type='number'
                  min='0'
                  step='0.01'
                  value={values.maxVolume}
                  onChange={(event) =>
                    onUpdateField('maxVolume', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-max-orders'>Số đơn tối đa</Label>
                <Input
                  id='bag-max-orders'
                  type='number'
                  min='1'
                  step='1'
                  value={values.maxOrders}
                  onChange={(event) =>
                    onUpdateField('maxOrders', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='bag-note'>Ghi chú</Label>
              <Textarea
                id='bag-note'
                value={values.note}
                onChange={(event) => onUpdateField('note', event.target.value)}
                disabled={isSaving}
                rows={3}
                placeholder='Ghi chú vận hành'
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              disabled={isSaving}
              onClick={() => onOpenChange(false)}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isSaving}>
              {isSaving && <Loader2 className='h-4 w-4 animate-spin' />}
              {mode === 'create' ? 'Tạo túi' : 'Lưu thay đổi'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
