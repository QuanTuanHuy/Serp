/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Tariff rule admin form card
 */

import React from 'react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type { TariffAdminResponse } from '../../../types';
import {
  DELIVERY_SERVICE_OPTIONS,
  ROUTE_TYPE_OPTIONS,
  type TariffFormState,
} from '../billingPageModels';

interface TariffRuleFormCardProps {
  form: TariffFormState;
  onFormChange: React.Dispatch<React.SetStateAction<TariffFormState>>;
  lastSaved: TariffAdminResponse | null;
  isLoading: boolean;
  onSubmit: (event: React.FormEvent) => void;
  onReset: () => void;
}

export const TariffRuleFormCard: React.FC<TariffRuleFormCardProps> = ({
  form,
  onFormChange,
  lastSaved,
  isLoading,
  onSubmit,
  onReset,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Tariff Rule</CardTitle>
        <CardDescription>
          Configure base price and step price by service and route type.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
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
              />
            </div>
          </div>

          <div className='flex flex-wrap gap-2'>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Saving Tariff...
                </>
              ) : (
                'Save Tariff Rule'
              )}
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={onReset}
              disabled={isLoading}
            >
              Reset Tariff Form
            </Button>
          </div>
        </form>

        {lastSaved ? (
          <Alert>
            <AlertTitle>Last saved tariff</AlertTitle>
            <AlertDescription>
              #{lastSaved.id} - {lastSaved.serviceCode} /{' '}
              {lastSaved.routeTypeCode}
            </AlertDescription>
          </Alert>
        ) : null}
      </CardContent>
    </Card>
  );
};
