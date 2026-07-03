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
  type BillingFormState,
  type BillingDeliveryServiceOption,
  type BillingSelectOption,
} from '../billingPageModels';
import { BillingLocationFields } from './BillingLocationFields';

interface BillingCalculatorFormProps {
  formValues: BillingFormState;
  onFormChange: React.Dispatch<React.SetStateAction<BillingFormState>>;
  provinceOptions: BillingSelectOption[];
  senderWardOptions: BillingSelectOption[];
  receiverWardOptions: BillingSelectOption[];
  deliveryServiceOptions: BillingDeliveryServiceOption[];
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
  deliveryServiceOptions,
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
          Công cụ tính phí vận chuyển
        </CardTitle>
        <CardDescription>
          Tính phí giao hàng theo tuyến, trọng lượng và cấu hình hiện tại từ
          dịch vụ tính cước.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className='space-y-6' onSubmit={onSubmit}>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='serviceCode'>Dịch vụ</Label>
              <TmsCombobox
                id='serviceCode'
                value={formValues.serviceCode}
                onValueChange={(value) =>
                  onFormChange((prev) => ({
                    ...prev,
                    serviceCode: value as BillingFormState['serviceCode'],
                  }))
                }
                options={deliveryServiceOptions}
                placeholder='Chọn dịch vụ'
                emptyText='Không tìm thấy dịch vụ'
              />
              <p className='text-xs text-muted-foreground'>
                {
                  deliveryServiceOptions.find(
                    (option) => option.value === formValues.serviceCode
                  )?.description
                }
              </p>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='actualWeightGram'>Trọng lượng thực (gram)</Label>
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
                placeholder='Ví dụ: 1500'
              />
            </div>

            <BillingLocationFields
              label='Địa chỉ gửi'
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
              label='Địa chỉ nhận'
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
            <h3 className='text-sm font-medium'>Kích thước kiện hàng (cm)</h3>
            <div className='grid gap-4 md:grid-cols-3'>
              <div className='space-y-2'>
                <Label htmlFor='lengthCm'>Dài</Label>
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
                  placeholder='Dài'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='widthCm'>Rộng</Label>
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
                  placeholder='Rộng'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='heightCm'>Cao</Label>
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
                  placeholder='Cao'
                />
              </div>
            </div>
          </div>

          <div className='flex flex-wrap gap-2'>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Đang tính...
                </>
              ) : (
                'Tính phí'
              )}
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={onReset}
              disabled={isLoading}
            >
              Đặt lại
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};
