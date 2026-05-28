/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Surcharge rule admin form card
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
import type { SurchargeRuleAdminResponse } from '../../../types';
import {
  CALCULATION_TYPE_OPTIONS,
  SURCHARGE_RULE_CODE_OPTIONS,
  type SurchargeRuleFormState,
} from '../billingPageModels';

interface SurchargeRuleFormCardProps {
  form: SurchargeRuleFormState;
  onFormChange: React.Dispatch<React.SetStateAction<SurchargeRuleFormState>>;
  calculationTypeHelper?: string;
  lastSaved: SurchargeRuleAdminResponse | null;
  isLoading: boolean;
  onSubmit: (event: React.FormEvent) => void;
  onReset: () => void;
}

export const SurchargeRuleFormCard: React.FC<SurchargeRuleFormCardProps> = ({
  form,
  onFormChange,
  calculationTypeHelper,
  lastSaved,
  isLoading,
  onSubmit,
  onReset,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Surcharge Rule</CardTitle>
        <CardDescription>
          Configure additional fee logic for remote areas and special cargo.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='surchargeCode'>Rule Code</Label>
              <Select
                value={form.code}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    code: value as SurchargeRuleFormState['code'],
                    name: prev.name || value,
                  }))
                }
              >
                <SelectTrigger id='surchargeCode'>
                  <SelectValue placeholder='Select rule code' />
                </SelectTrigger>
                <SelectContent>
                  {SURCHARGE_RULE_CODE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeName'>Display Name</Label>
              <Input
                id='surchargeName'
                value={form.name}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    name: event.target.value,
                  }))
                }
                placeholder='Example: Remote area surcharge'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeCalculationType'>Calculation Type</Label>
              <Select
                value={form.calculationType}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    calculationType:
                      value as SurchargeRuleFormState['calculationType'],
                  }))
                }
              >
                <SelectTrigger id='surchargeCalculationType'>
                  <SelectValue placeholder='Select type' />
                </SelectTrigger>
                <SelectContent>
                  {CALCULATION_TYPE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {calculationTypeHelper ? (
                <p className='text-xs text-muted-foreground'>
                  {calculationTypeHelper}
                </p>
              ) : null}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeRatePercent'>Rate Percent</Label>
              <Input
                id='surchargeRatePercent'
                inputMode='decimal'
                value={form.ratePercent}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    ratePercent: event.target.value,
                  }))
                }
                placeholder='Optional'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeFixedAmount'>Fixed Amount</Label>
              <Input
                id='surchargeFixedAmount'
                inputMode='decimal'
                value={form.fixedAmount}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    fixedAmount: event.target.value,
                  }))
                }
                placeholder='Optional'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeMinAmount'>Min Amount</Label>
              <Input
                id='surchargeMinAmount'
                inputMode='decimal'
                value={form.minAmount}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    minAmount: event.target.value,
                  }))
                }
                placeholder='Optional'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeBaseWeight'>Base Weight</Label>
              <Input
                id='surchargeBaseWeight'
                inputMode='decimal'
                value={form.baseWeight}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    baseWeight: event.target.value,
                  }))
                }
                placeholder='Optional'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeBasePrice'>Base Price</Label>
              <Input
                id='surchargeBasePrice'
                inputMode='decimal'
                value={form.basePrice}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    basePrice: event.target.value,
                  }))
                }
                placeholder='Optional'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeStepWeight'>Step Weight</Label>
              <Input
                id='surchargeStepWeight'
                inputMode='decimal'
                value={form.stepWeight}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    stepWeight: event.target.value,
                  }))
                }
                placeholder='Optional'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeStepPrice'>Step Price</Label>
              <Input
                id='surchargeStepPrice'
                inputMode='decimal'
                value={form.stepPrice}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    stepPrice: event.target.value,
                  }))
                }
                placeholder='Optional'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='surchargeEffectiveDate'>Effective Date</Label>
              <Input
                id='surchargeEffectiveDate'
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
              <Label htmlFor='surchargeExpirationDate'>Expiration Date</Label>
              <Input
                id='surchargeExpirationDate'
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
                  Saving Surcharge...
                </>
              ) : (
                'Save Surcharge Rule'
              )}
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={onReset}
              disabled={isLoading}
            >
              Reset Surcharge Form
            </Button>
          </div>
        </form>

        {lastSaved ? (
          <Alert>
            <AlertTitle>Last saved surcharge</AlertTitle>
            <AlertDescription>
              #{lastSaved.id} - {lastSaved.code}
            </AlertDescription>
          </Alert>
        ) : null}
      </CardContent>
    </Card>
  );
};
