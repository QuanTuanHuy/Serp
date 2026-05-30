/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub create/update form dialog
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2, LocateFixed } from 'lucide-react';
import { CoordinatePickerMap } from '../../../components';
import type { Province, Ward } from '../../../types';
import {
  HUB_FORM_STATUS_OPTIONS,
  HUB_TYPE_OPTIONS,
  normalizeLocationCode,
  type HubFormMode,
  type HubFormState,
  type UpdateHubFormField,
} from '../hubForm';

interface HubFormDialogProps {
  open: boolean;
  formMode: HubFormMode;
  isSaving: boolean;
  isGeocodingAddress: boolean;
  formValues: HubFormState;
  selectedProvinceCode: string;
  selectedWardCode: string;
  provinceSelectOptions: Province[];
  wardSelectOptions: Ward[];
  isFetchingWardsForForm: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onGeocodeAddress: () => Promise<void>;
  updateFormField: UpdateHubFormField;
}

export const HubFormDialog: React.FC<HubFormDialogProps> = ({
  open,
  formMode,
  isSaving,
  isGeocodingAddress,
  formValues,
  selectedProvinceCode,
  selectedWardCode,
  provinceSelectOptions,
  wardSelectOptions,
  isFetchingWardsForForm,
  onOpenChange,
  onSubmit,
  onGeocodeAddress,
  updateFormField,
}) => {
  const latNum = Number(formValues.latitude);
  const lngNum = Number(formValues.longitude);
  const mapLat = Number.isFinite(latNum) ? latNum : undefined;
  const mapLng = Number.isFinite(lngNum) ? lngNum : undefined;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl max-h-[85vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            {formMode === 'create' ? 'Create Hub' : 'Update Hub'}
          </DialogTitle>
          <DialogDescription>
            {formMode === 'create'
              ? 'Create a new distribution hub.'
              : 'Update hub details.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='hub-code'>Code *</Label>
              <Input
                id='hub-code'
                value={formValues.code}
                onChange={(e) => updateFormField('code', e.target.value)}
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-name'>Name *</Label>
              <Input
                id='hub-name'
                value={formValues.name}
                onChange={(e) => updateFormField('name', e.target.value)}
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-type'>Hub type *</Label>
              <Select
                value={formValues.hub_type}
                onValueChange={(v) =>
                  updateFormField('hub_type', v as HubFormState['hub_type'])
                }
                disabled={isSaving}
              >
                <SelectTrigger id='hub-type' className='w-full'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {HUB_TYPE_OPTIONS.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-status'>Status *</Label>
              <Select
                value={formValues.status}
                onValueChange={(v) =>
                  updateFormField('status', v as HubFormState['status'])
                }
                disabled={isSaving}
              >
                <SelectTrigger id='hub-status' className='w-full'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {HUB_FORM_STATUS_OPTIONS.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-province'>Province *</Label>
              <Select
                value={selectedProvinceCode || undefined}
                onValueChange={(value) => {
                  updateFormField('province_code', value);
                  updateFormField('ward_code', '');
                }}
                disabled={isSaving}
              >
                <SelectTrigger id='hub-province' className='w-full'>
                  <SelectValue placeholder='Select province' />
                </SelectTrigger>
                <SelectContent>
                  {provinceSelectOptions.map((province) => {
                    const code = normalizeLocationCode(province.provinceCode);
                    if (!code) {
                      return null;
                    }
                    return (
                      <SelectItem key={code} value={code}>
                        {province.name} ({code})
                      </SelectItem>
                    );
                  })}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-ward'>Ward *</Label>
              <Select
                value={selectedWardCode || undefined}
                onValueChange={(value) => updateFormField('ward_code', value)}
                disabled={isSaving || !selectedProvinceCode}
              >
                <SelectTrigger id='hub-ward' className='w-full'>
                  <SelectValue
                    placeholder={
                      selectedProvinceCode
                        ? isFetchingWardsForForm
                          ? 'Loading wards...'
                          : 'Select ward'
                        : 'Select province first'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {wardSelectOptions.map((ward) => {
                    const code = normalizeLocationCode(ward.wardCode);
                    if (!code) {
                      return null;
                    }
                    return (
                      <SelectItem key={code} value={code}>
                        {ward.name} ({code})
                      </SelectItem>
                    );
                  })}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='hub-address'>Address detail *</Label>
              <Input
                id='hub-address'
                value={formValues.address_detail}
                onChange={(e) =>
                  updateFormField('address_detail', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-phone'>Phone</Label>
              <Input
                id='hub-phone'
                value={formValues.phone_number}
                onChange={(e) =>
                  updateFormField('phone_number', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-daily-capacity'>Daily capacity *</Label>
              <Input
                id='hub-daily-capacity'
                type='number'
                min={0}
                step={1}
                value={formValues.daily_capacity}
                onChange={(e) =>
                  updateFormField('daily_capacity', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-current-load'>Current load *</Label>
              <Input
                id='hub-current-load'
                type='number'
                min={0}
                step={1}
                value={formValues.current_load}
                onChange={(e) =>
                  updateFormField('current_load', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-working-start'>Working start (HH:mm)</Label>
              <Input
                id='hub-working-start'
                type='time'
                value={formValues.working_start_time}
                onChange={(e) =>
                  updateFormField('working_start_time', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-working-end'>Working end (HH:mm)</Label>
              <Input
                id='hub-working-end'
                type='time'
                value={formValues.working_end_time}
                onChange={(e) =>
                  updateFormField('working_end_time', e.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-latitude'>Latitude</Label>
              <Input
                id='hub-latitude'
                value={formValues.latitude}
                onChange={(e) => updateFormField('latitude', e.target.value)}
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-longitude'>Longitude</Label>
              <Input
                id='hub-longitude'
                value={formValues.longitude}
                onChange={(e) => updateFormField('longitude', e.target.value)}
                disabled={isSaving}
              />
            </div>
          </div>

          <div className='space-y-2'>
            <div className='flex items-center justify-between gap-2'>
              <Label>Location map</Label>
              <Button
                type='button'
                variant='outline'
                size='sm'
                disabled={isSaving || isGeocodingAddress}
                onClick={() => void onGeocodeAddress()}
              >
                {isGeocodingAddress ? (
                  <>
                    <Loader2 className='h-4 w-4 mr-1 animate-spin' />
                    Geocoding...
                  </>
                ) : (
                  <>
                    <LocateFixed className='h-4 w-4 mr-1' />
                    Geocode from address
                  </>
                )}
              </Button>
            </div>
            <CoordinatePickerMap
              latitude={mapLat}
              longitude={mapLng}
              disabled={isSaving}
              onChange={(latitude, longitude) => {
                updateFormField('latitude', String(latitude));
                updateFormField('longitude', String(longitude));
              }}
            />
            <p className='text-xs text-muted-foreground'>
              Pick a point on the map or type coordinates. Leave both empty if
              unknown.
            </p>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSaving}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSaving}>
              {isSaving ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Saving...
                </>
              ) : formMode === 'create' ? (
                'Create'
              ) : (
                'Save changes'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
