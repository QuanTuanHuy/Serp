/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - VAS rule admin form dialog
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
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import {
  CALCULATION_TYPE_OPTIONS,
  VAS_RULE_CODE_OPTIONS,
  type VasRuleFormState,
} from '../billingPageModels';

export type VasRuleFormMode = 'create' | 'edit';

interface VasRuleFormDialogProps {
  open: boolean;
  mode: VasRuleFormMode;
  form: VasRuleFormState;
  onFormChange: React.Dispatch<React.SetStateAction<VasRuleFormState>>;
  calculationTypeHelper?: string;
  isLoading: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
}

export const VasRuleFormDialog: React.FC<VasRuleFormDialogProps> = ({
  open,
  mode,
  form,
  onFormChange,
  calculationTypeHelper,
  isLoading,
  onOpenChange,
  onSubmit,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[85vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            {mode === 'create' ? 'Create VAS Rule' : 'Edit VAS Rule'}
          </DialogTitle>
          <DialogDescription>
            Configure add-on service rules such as COD.
          </DialogDescription>
        </DialogHeader>

        <form className='space-y-4' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='vasCode'>VAS Code</Label>
              <TmsCombobox
                id='vasCode'
                value={form.code}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    code: value as VasRuleFormState['code'],
                    name: prev.name || value,
                  }))
                }
                options={VAS_RULE_CODE_OPTIONS}
                placeholder='Select VAS code'
                emptyText='No VAS codes found'
                disabled={isLoading}
              />
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
                placeholder='Example: COD fee'
                disabled={isLoading}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vasCalculationType'>Calculation Type</Label>
              <TmsCombobox
                id='vasCalculationType'
                value={form.calculationType}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    calculationType:
                      value as VasRuleFormState['calculationType'],
                  }))
                }
                options={CALCULATION_TYPE_OPTIONS}
                placeholder='Select type'
                emptyText='No calculation types found'
                disabled={isLoading}
              />
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
                disabled={isLoading}
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
                disabled={isLoading}
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
                'Create VAS Rule'
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
