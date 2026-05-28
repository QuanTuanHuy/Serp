/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - VAS rule admin form card
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
import type { VasRuleAdminResponse } from '../../../types';
import {
  CALCULATION_TYPE_OPTIONS,
  VAS_RULE_CODE_OPTIONS,
  type VasRuleFormState,
} from '../billingPageModels';

interface VasRuleFormCardProps {
  form: VasRuleFormState;
  onFormChange: React.Dispatch<React.SetStateAction<VasRuleFormState>>;
  calculationTypeHelper?: string;
  lastSaved: VasRuleAdminResponse | null;
  isLoading: boolean;
  onSubmit: (event: React.FormEvent) => void;
  onReset: () => void;
}

export const VasRuleFormCard: React.FC<VasRuleFormCardProps> = ({
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
        <CardTitle>VAS Rule</CardTitle>
        <CardDescription>
          Configure add-on service rules such as COD and insurance.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='vasCode'>VAS Code</Label>
              <Select
                value={form.code}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    code: value as VasRuleFormState['code'],
                    name: prev.name || value,
                  }))
                }
              >
                <SelectTrigger id='vasCode'>
                  <SelectValue placeholder='Select VAS code' />
                </SelectTrigger>
                <SelectContent>
                  {VAS_RULE_CODE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vasName'>Display Name</Label>
              <Input
                id='vasName'
                value={form.name}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    name: event.target.value,
                  }))
                }
                placeholder='Example: Insurance fee'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vasCalculationType'>Calculation Type</Label>
              <Select
                value={form.calculationType}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    calculationType:
                      value as VasRuleFormState['calculationType'],
                  }))
                }
              >
                <SelectTrigger id='vasCalculationType'>
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
              <Label htmlFor='vasRatePercent'>Rate Percent</Label>
              <Input
                id='vasRatePercent'
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
              <Label htmlFor='vasFixedAmount'>Fixed Amount</Label>
              <Input
                id='vasFixedAmount'
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
              <Label htmlFor='vasMinAmount'>Min Amount</Label>
              <Input
                id='vasMinAmount'
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
          </div>

          <div className='flex flex-wrap gap-2'>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Saving VAS...
                </>
              ) : (
                'Save VAS Rule'
              )}
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={onReset}
              disabled={isLoading}
            >
              Reset VAS Form
            </Button>
          </div>
        </form>

        {lastSaved ? (
          <Alert>
            <AlertTitle>Last saved VAS</AlertTitle>
            <AlertDescription>
              #{lastSaved.id} - {lastSaved.code}
            </AlertDescription>
          </Alert>
        ) : null}
      </CardContent>
    </Card>
  );
};
