/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Shipping fee calculator form
 */

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Separator,
} from '@/shared/components/ui';
import { TmsCombobox } from '@/modules/first-mile/components';
import { Calculator, Loader2 } from 'lucide-react';
import {
  DELIVERY_SERVICE_OPTIONS,
  type BillingFormState,
  type BillingSelectOption,
} from '../billingPageModels';
import { BillingLocationFields } from './BillingLocationFields';

interface BillingCalculatorFormProps {
  formValues: BillingFormState;
  onFormChange: React.Dispatch<React.SetStateAction<BillingFormState>>;
  provinceOptions: BillingSelectOption[];
  senderWardOptions: BillingSelectOption[];
  receiverWardOptions: BillingSelectOption[];
  isFetchingProvinces: boolean;
  isFetchingSenderWards: boolean;
  isFetchingReceiverWards: boolean;
  isLoading: boolean;
  onSubmit: (event: React.FormEvent) => void;
  onReset: () => void;
}

export const BillingCalculatorForm: React.FC<BillingCalculatorFormProps> = ({
  formValues,
  onFormChange,
  provinceOptions,
  senderWardOptions,
  receiverWardOptions,
  isFetchingProvinces,
  isFetchingSenderWards,
  isFetchingReceiverWards,
  isLoading,
  onSubmit,
  onReset,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className='flex items-center gap-2'>
          <Calculator className='h-5 w-5' />
          Shipping Fee Calculator
        </CardTitle>
        <CardDescription>
          Calculate delivery fee with live route and weight logic from billing
          service.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className='space-y-6' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='serviceCode'>Service</Label>
              <TmsCombobox
                id='serviceCode'
                value={formValues.serviceCode}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    serviceCode: value as BillingFormState['serviceCode'],
                  }))
                }
                options={DELIVERY_SERVICE_OPTIONS}
                placeholder='Select a service'
                emptyText='No services found'
              />
              <p className='text-xs text-muted-foreground'>
                {
                  DELIVERY_SERVICE_OPTIONS.find(
                    (option) => option.value === formValues.serviceCode
                  )?.description
                }
              </p>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='actualWeightGram'>Actual Weight (grams)</Label>
              <Input
                id='actualWeightGram'
                inputMode='numeric'
                value={formValues.actualWeightGram}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    actualWeightGram: event.target.value,
                  }))
                }
                placeholder='Example: 1500'
              />
            </div>

            <BillingLocationFields
              label='Sender Location'
              provinceId='senderProvinceCode'
              wardId='senderWardCode'
              provinceCode={formValues.senderProvinceCode}
              wardCode={formValues.senderWardCode}
              provinceOptions={provinceOptions}
              wardOptions={senderWardOptions}
              isFetchingProvinces={isFetchingProvinces}
              isFetchingWards={isFetchingSenderWards}
              onProvinceChange={(value) =>
                onFormChange((prev) => ({
                  ...prev,
                  senderProvinceCode: value,
                  senderWardCode: '',
                }))
              }
              onWardChange={(value) =>
                onFormChange((prev) => ({
                  ...prev,
                  senderWardCode: value,
                }))
              }
            />

            <BillingLocationFields
              label='Receiver Location'
              provinceId='receiverProvinceCode'
              wardId='receiverWardCode'
              provinceCode={formValues.receiverProvinceCode}
              wardCode={formValues.receiverWardCode}
              provinceOptions={provinceOptions}
              wardOptions={receiverWardOptions}
              isFetchingProvinces={isFetchingProvinces}
              isFetchingWards={isFetchingReceiverWards}
              onProvinceChange={(value) =>
                onFormChange((prev) => ({
                  ...prev,
                  receiverProvinceCode: value,
                  receiverWardCode: '',
                }))
              }
              onWardChange={(value) =>
                onFormChange((prev) => ({
                  ...prev,
                  receiverWardCode: value,
                }))
              }
            />
          </div>

          <Separator />

          <div className='space-y-3'>
            <h3 className='text-sm font-medium'>Package Dimensions (cm)</h3>
            <div className='grid gap-4 md:grid-cols-3'>
              <div className='space-y-2'>
                <Label htmlFor='lengthCm'>Length</Label>
                <Input
                  id='lengthCm'
                  inputMode='numeric'
                  value={formValues.lengthCm}
                  onChange={(event) =>
                    onFormChange((prev) => ({
                      ...prev,
                      lengthCm: event.target.value,
                    }))
                  }
                  placeholder='Length'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='widthCm'>Width</Label>
                <Input
                  id='widthCm'
                  inputMode='numeric'
                  value={formValues.widthCm}
                  onChange={(event) =>
                    onFormChange((prev) => ({
                      ...prev,
                      widthCm: event.target.value,
                    }))
                  }
                  placeholder='Width'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='heightCm'>Height</Label>
                <Input
                  id='heightCm'
                  inputMode='numeric'
                  value={formValues.heightCm}
                  onChange={(event) =>
                    onFormChange((prev) => ({
                      ...prev,
                      heightCm: event.target.value,
                    }))
                  }
                  placeholder='Height'
                />
              </div>
            </div>
          </div>

          <Separator />

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='codAmount'>COD (VND)</Label>
              <Input
                id='codAmount'
                inputMode='numeric'
                value={formValues.codAmount}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    codAmount: event.target.value,
                  }))
                }
                placeholder='Leave empty if not COD'
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='declaredValue'>Declared Value (VND)</Label>
              <Input
                id='declaredValue'
                inputMode='numeric'
                value={formValues.declaredValue}
                onChange={(event) =>
                  onFormChange((prev) => ({
                    ...prev,
                    declaredValue: event.target.value,
                  }))
                }
                placeholder='Leave empty if not declared'
              />
            </div>
          </div>

          <div className='flex flex-wrap gap-2'>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Calculating...
                </>
              ) : (
                'Calculate Fee'
              )}
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={onReset}
              disabled={isLoading}
            >
              Reset
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};
