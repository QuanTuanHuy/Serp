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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import {
  DELIVERY_SERVICE_OPTIONS,
  ROUTE_TYPE_OPTIONS,
  type TariffFormState,
} from '../billingPageModels';

export type TariffRuleFormMode = 'create' | 'edit';

interface TariffRuleFormDialogProps {
  open: boolean;
  mode: TariffRuleFormMode;
  form: TariffFormState;
  onFormChange: React.Dispatch<React.SetStateAction<TariffFormState>>;
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
}

export const TariffRuleFormDialog: React.FC<TariffRuleFormDialogProps> = ({
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
      <DialogContent className='max-h-[85vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            {mode === 'create' ? 'Create Tariff Rule' : 'Edit Tariff Rule'}
          </DialogTitle>
          <DialogDescription>
            Configure base price and step price by service and route type.
          </DialogDescription>
        </DialogHeader>

        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='tariffServiceCode'>Service Code</Label>
              <Select
                value={form.serviceCode}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    serviceCode: value as TariffFormState['serviceCode'],
                  }))
                }
                disabled={isLoading}
              >
                <SelectTrigger id='tariffServiceCode'>
                  <SelectValue placeholder='Select service' />
                </SelectTrigger>
                <SelectContent>
                  {DELIVERY_SERVICE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='tariffRouteType'>Route Type</Label>
              <Select
                value={form.routeTypeCode}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    routeTypeCode: value as TariffFormState['routeTypeCode'],
                  }))
                }
                disabled={isLoading}
              >
                <SelectTrigger id='tariffRouteType'>
                  <SelectValue placeholder='Select route type' />
                </SelectTrigger>
                <SelectContent>
                  {ROUTE_TYPE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='baseWeight'>Base Weight</Label>
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
                placeholder='Example: 2000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='basePrice'>Base Price</Label>
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
                placeholder='Example: 25000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='stepWeight'>Step Weight</Label>
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
                placeholder='Example: 500'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='stepPrice'>Step Price</Label>
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
                placeholder='Example: 5000'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='effectiveDate'>Effective Date</Label>
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
              <Label htmlFor='expirationDate'>Expiration Date</Label>
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
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  Saving...
                </>
              ) : mode === 'create' ? (
                'Create Tariff Rule'
              ) : (
                'Save Changes'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
