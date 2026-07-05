/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order create/update form dialog
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
  Textarea,
} from '@/shared/components/ui';
import { Loader2, Plus, Trash2 } from 'lucide-react';
import { CoordinatePickerMap, TmsCombobox } from '../../../../components';
import type {
  CreateOrderRequest,
  ProductType,
  Province,
  Ward,
} from '../../../../types';
import {
  DELIVERY_REQUEST_TIME_OPTIONS,
  FEE_PAYER_OPTIONS,
  ORDER_PRODUCT_CATEGORY_OPTIONS,
  ORDER_PICKUP_METHOD_OPTIONS,
  type CreateOrderFormState,
  type LocationTarget,
  type OrderFormMode,
  type UpdateOrderFormField,
} from '../orderPageModels';

type OrderProductFormItem = NonNullable<CreateOrderRequest['products']>[number];

interface OrderFormDialogProps {
  open: boolean;
  orderFormMode: OrderFormMode;
  isSubmittingOrder: boolean;
  createForm: CreateOrderFormState;
  selectedSenderProvinceCode: string;
  selectedSenderWardCode: string;
  selectedReceiverProvinceCode: string;
  selectedReceiverWardCode: string;
  provinceSelectOptions: Province[];
  senderWardSelectOptions: Ward[];
  receiverWardSelectOptions: Ward[];
  productTypeOptions: ProductType[];
  orderProducts: OrderProductFormItem[];
  isFetchingSenderWards: boolean;
  isFetchingReceiverWards: boolean;
  isFetchingProductTypes: boolean;
  geocodingTarget: LocationTarget | null;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
  onFormChange: UpdateOrderFormField;
  onProductsChange: (products: OrderProductFormItem[]) => void;
  onGeocodeFromAddress: (target: LocationTarget) => void;
  onMapCoordinateChange: (
    target: LocationTarget,
    latitude: number,
    longitude: number
  ) => void;
  normalizeLocationCode: (value?: string) => string;
  parseOptionalNumberInput: (value: string) => number | undefined;
}

export const OrderFormDialog: React.FC<OrderFormDialogProps> = ({
  open,
  orderFormMode,
  isSubmittingOrder,
  createForm,
  selectedSenderProvinceCode,
  selectedSenderWardCode,
  selectedReceiverProvinceCode,
  selectedReceiverWardCode,
  provinceSelectOptions,
  senderWardSelectOptions,
  receiverWardSelectOptions,
  productTypeOptions,
  orderProducts,
  isFetchingSenderWards,
  isFetchingReceiverWards,
  isFetchingProductTypes,
  geocodingTarget,
  onOpenChange,
  onSubmit,
  onFormChange,
  onProductsChange,
  onGeocodeFromAddress,
  onMapCoordinateChange,
  normalizeLocationCode,
  parseOptionalNumberInput,
}) => {
  const defaultProductTypeId = productTypeOptions[0]?.id ?? 0;
  const provinceOptions = provinceSelectOptions.flatMap((province) => {
    const provinceCode = normalizeLocationCode(province.provinceCode);

    return provinceCode
      ? [
          {
            value: provinceCode,
            label: `${province.name} (${provinceCode})`,
          },
        ]
      : [];
  });
  const senderWardOptions = senderWardSelectOptions.flatMap((ward) => {
    const wardCode = normalizeLocationCode(ward.wardCode);

    return wardCode
      ? [
          {
            value: wardCode,
            label: `${ward.name} (${wardCode})`,
          },
        ]
      : [];
  });
  const receiverWardOptions = receiverWardSelectOptions.flatMap((ward) => {
    const wardCode = normalizeLocationCode(ward.wardCode);

    return wardCode
      ? [
          {
            value: wardCode,
            label: `${ward.name} (${wardCode})`,
          },
        ]
      : [];
  });
  const codOptions = [
    { value: 'false', label: 'No COD' },
    { value: 'true', label: 'COD' },
  ];
  const productTypeComboboxOptions = productTypeOptions.map((productType) => ({
    value: String(productType.id),
    label: `${productType.name} (${productType.code})`,
  }));

  const handleAddProduct = () => {
    onProductsChange([
      ...orderProducts,
      {
        name: '',
        value: 0,
        quantity: 1,
        weight_gram: 1,
        product_type_id: defaultProductTypeId,
      },
    ]);
  };

  const handleProductChange = <K extends keyof OrderProductFormItem>(
    index: number,
    field: K,
    value: OrderProductFormItem[K]
  ) => {
    onProductsChange(
      orderProducts.map((product, productIndex) =>
        productIndex === index ? { ...product, [field]: value } : product
      )
    );
  };

  const handleRemoveProduct = (index: number) => {
    onProductsChange(
      orderProducts.filter((_, productIndex) => productIndex !== index)
    );
  };

  const parseProductNumber = (value: string): number => {
    const parsedValue = Number(value);
    return Number.isFinite(parsedValue) ? parsedValue : 0;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>
            {orderFormMode === 'create' ? 'Tạo đơn hàng' : 'Sửa đơn hàng'}
          </DialogTitle>
          <DialogDescription>
            {orderFormMode === 'create'
              ? 'Tạo đơn hàng.'
              : 'Cập nhật một đơn hàng đã tồn tại. Chỉ các đơn hàng mới tạo và chưa xác nhận mới có thể chỉnh sửa.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-5'>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='customerOrderCode'>Mã đơn của khách hàng *</Label>
              <Input
                id='customerOrderCode'
                value={createForm.customerOrderCode}
                onChange={(event) =>
                  onFormChange('customerOrderCode', event.target.value)
                }
                placeholder='KH-DON-001'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='note'>Ghi chú</Label>
              <Textarea
                id='note'
                value={createForm.note}
                onChange={(event) => onFormChange('note', event.target.value)}
                placeholder='Ghi chú tùy chọn cho lấy/giao hàng'
                rows={2}
              />
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Thông tin người gửi</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='senderName'>Tên người gửi *</Label>
                <Input
                  id='senderName'
                  value={createForm.senderName}
                  onChange={(event) =>
                    onFormChange('senderName', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='senderPhone'>Số điện thoại người gửi *</Label>
                <Input
                  id='senderPhone'
                  value={createForm.senderPhone}
                  onChange={(event) =>
                    onFormChange('senderPhone', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='senderProvinceCode'>Tỉnh người gửi *</Label>
                <TmsCombobox
                  id='senderProvinceCode'
                  value={selectedSenderProvinceCode}
                  onValueChange={(value) => {
                    onFormChange('senderProvinceCode', value);
                    onFormChange('senderWardCode', '');
                  }}
                  options={provinceOptions}
                  placeholder='Chọn tỉnh/thành'
                  emptyText='Không tìm thấy tỉnh/thành'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='senderWardCode'>Phường/xã người gửi *</Label>
                <TmsCombobox
                  id='senderWardCode'
                  value={selectedSenderWardCode}
                  onValueChange={(value) =>
                    onFormChange('senderWardCode', value)
                  }
                  options={senderWardOptions}
                  placeholder={
                    selectedSenderProvinceCode
                      ? 'Chọn phường/xã'
                      : 'Chọn tỉnh/thành trước'
                  }
                  emptyText={
                    selectedSenderProvinceCode && isFetchingSenderWards
                      ? 'Đang tải phường/xã...'
                      : 'Không có phường/xã phù hợp.'
                  }
                  disabled={!selectedSenderProvinceCode}
                  loading={isFetchingSenderWards}
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='senderAddressDetail'>Địa chỉ người gửi *</Label>
                <Input
                  id='senderAddressDetail'
                  value={createForm.senderAddressDetail}
                  onChange={(event) =>
                    onFormChange('senderAddressDetail', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label>Tọa độ người gửi *</Label>
                <div className='grid gap-2 md:grid-cols-2'>
                  <Input
                    value={createForm.senderLatitude}
                    placeholder='Vĩ độ'
                    readOnly
                  />
                  <Input
                    value={createForm.senderLongitude}
                    placeholder='Kinh độ'
                    readOnly
                  />
                </div>
                <div className='flex flex-wrap gap-2'>
                  <Button
                    type='button'
                    variant='outline'
                    onClick={() => onGeocodeFromAddress('sender')}
                    disabled={geocodingTarget === 'sender'}
                  >
                    {geocodingTarget === 'sender' ? (
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    ) : null}
                    Lấy tọa độ từ địa chỉ
                  </Button>
                </div>
                <CoordinatePickerMap
                  latitude={parseOptionalNumberInput(createForm.senderLatitude)}
                  longitude={parseOptionalNumberInput(
                    createForm.senderLongitude
                  )}
                  onChange={(latitude, longitude) =>
                    onMapCoordinateChange('sender', latitude, longitude)
                  }
                  className='h-56'
                />
                <p className='text-xs text-muted-foreground'>
                  Bấm vào bản đồ để chọn tọa độ người gửi.
                </p>
              </div>
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Thông tin người nhận</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='receiverName'>Tên người nhận *</Label>
                <Input
                  id='receiverName'
                  value={createForm.receiverName}
                  onChange={(event) =>
                    onFormChange('receiverName', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='receiverPhone'>
                  Số điện thoại người nhận *
                </Label>
                <Input
                  id='receiverPhone'
                  value={createForm.receiverPhone}
                  onChange={(event) =>
                    onFormChange('receiverPhone', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='receiverProvinceCode'>Tỉnh người nhận *</Label>
                <TmsCombobox
                  id='receiverProvinceCode'
                  value={selectedReceiverProvinceCode}
                  onValueChange={(value) => {
                    onFormChange('receiverProvinceCode', value);
                    onFormChange('receiverWardCode', '');
                  }}
                  options={provinceOptions}
                  placeholder='Chọn tỉnh/thành'
                  emptyText='Không tìm thấy tỉnh/thành'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='receiverWardCode'>Phường/xã người nhận *</Label>
                <TmsCombobox
                  id='receiverWardCode'
                  value={selectedReceiverWardCode}
                  onValueChange={(value) =>
                    onFormChange('receiverWardCode', value)
                  }
                  options={receiverWardOptions}
                  placeholder={
                    selectedReceiverProvinceCode
                      ? 'Chọn phường/xã'
                      : 'Chọn tỉnh/thành trước'
                  }
                  emptyText={
                    selectedReceiverProvinceCode && isFetchingReceiverWards
                      ? 'Đang tải phường/xã...'
                      : 'Không có phường/xã phù hợp.'
                  }
                  disabled={!selectedReceiverProvinceCode}
                  loading={isFetchingReceiverWards}
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='receiverAddressDetail'>
                  Địa chỉ người nhận *
                </Label>
                <Input
                  id='receiverAddressDetail'
                  value={createForm.receiverAddressDetail}
                  onChange={(event) =>
                    onFormChange('receiverAddressDetail', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label>Tọa độ người nhận *</Label>
                <div className='grid gap-2 md:grid-cols-2'>
                  <Input
                    value={createForm.receiverLatitude}
                    placeholder='Vĩ độ'
                    readOnly
                  />
                  <Input
                    value={createForm.receiverLongitude}
                    placeholder='Kinh độ'
                    readOnly
                  />
                </div>
                <div className='flex flex-wrap gap-2'>
                  <Button
                    type='button'
                    variant='outline'
                    onClick={() => onGeocodeFromAddress('receiver')}
                    disabled={geocodingTarget === 'receiver'}
                  >
                    {geocodingTarget === 'receiver' ? (
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    ) : null}
                    Lấy tọa độ từ địa chỉ
                  </Button>
                </div>
                <CoordinatePickerMap
                  latitude={parseOptionalNumberInput(
                    createForm.receiverLatitude
                  )}
                  longitude={parseOptionalNumberInput(
                    createForm.receiverLongitude
                  )}
                  onChange={(latitude, longitude) =>
                    onMapCoordinateChange('receiver', latitude, longitude)
                  }
                  className='h-56'
                />
                <p className='text-xs text-muted-foreground'>
                  Bấm vào bản đồ để chọn tọa độ người nhận.
                </p>
              </div>
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Tùy chọn đơn hàng</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='pickupMethod'>Phương thức lấy hàng *</Label>
                <TmsCombobox
                  id='pickupMethod'
                  value={createForm.pickupMethod}
                  onValueChange={(value) =>
                    onFormChange(
                      'pickupMethod',
                      value as CreateOrderFormState['pickupMethod']
                    )
                  }
                  options={ORDER_PICKUP_METHOD_OPTIONS}
                  placeholder='Chọn phương thức lấy hàng'
                  emptyText='Không tìm thấy phương thức lấy hàng'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='deliveryRequestTime'>
                  Thời gian yêu cầu giao hàng *
                </Label>
                <TmsCombobox
                  id='deliveryRequestTime'
                  value={createForm.deliveryRequestTime}
                  onValueChange={(value) =>
                    onFormChange(
                      'deliveryRequestTime',
                      value as CreateOrderFormState['deliveryRequestTime']
                    )
                  }
                  options={DELIVERY_REQUEST_TIME_OPTIONS}
                  placeholder='Chọn thời gian'
                  emptyText='Không tìm thấy thời gian phù hợp'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='feePayer'>Bên trả phí *</Label>
                <TmsCombobox
                  id='feePayer'
                  value={createForm.feePayer}
                  onValueChange={(value) =>
                    onFormChange(
                      'feePayer',
                      value as CreateOrderFormState['feePayer']
                    )
                  }
                  options={FEE_PAYER_OPTIONS}
                  placeholder='Chọn bên trả phí'
                  emptyText='Không tìm thấy lựa chọn phù hợp'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='orderProductCategory'>Phân loại hàng hóa</Label>
                <TmsCombobox
                  id='orderProductCategory'
                  value={createForm.orderProductCategory}
                  onValueChange={(value) =>
                    onFormChange(
                      'orderProductCategory',
                      value as CreateOrderFormState['orderProductCategory']
                    )
                  }
                  options={ORDER_PRODUCT_CATEGORY_OPTIONS}
                  placeholder='Chọn phân loại hàng hóa'
                  emptyText='Không có phân loại phù hợp'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='isCod'>COD</Label>
                <TmsCombobox
                  id='isCod'
                  value={createForm.isCod}
                  onValueChange={(value) =>
                    onFormChange('isCod', value as 'true' | 'false')
                  }
                  options={codOptions}
                  placeholder='Chọn tùy chọn COD'
                  emptyText='Không tìm thấy tùy chọn COD'
                />
              </div>
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Lấy hàng và kích thước</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='pickupTimeStart'>Bắt đầu lấy hàng</Label>
                <Input
                  id='pickupTimeStart'
                  type='datetime-local'
                  value={createForm.pickupTimeStart}
                  onChange={(event) =>
                    onFormChange('pickupTimeStart', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='pickupTimeEnd'>Kết thúc lấy hàng</Label>
                <Input
                  id='pickupTimeEnd'
                  type='datetime-local'
                  value={createForm.pickupTimeEnd}
                  onChange={(event) =>
                    onFormChange('pickupTimeEnd', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='dimensionLengthCm'>Dài (cm)</Label>
                <Input
                  id='dimensionLengthCm'
                  type='number'
                  step='any'
                  value={createForm.dimensionLengthCm}
                  onChange={(event) =>
                    onFormChange('dimensionLengthCm', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='dimensionWidthCm'>Rộng (cm)</Label>
                <Input
                  id='dimensionWidthCm'
                  type='number'
                  step='any'
                  value={createForm.dimensionWidthCm}
                  onChange={(event) =>
                    onFormChange('dimensionWidthCm', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='dimensionHeightCm'>Cao (cm)</Label>
                <Input
                  id='dimensionHeightCm'
                  type='number'
                  step='any'
                  value={createForm.dimensionHeightCm}
                  onChange={(event) =>
                    onFormChange('dimensionHeightCm', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='totalVolumeM3'>Tổng thể tích (m3)</Label>
                <Input
                  id='totalVolumeM3'
                  type='number'
                  step='any'
                  value={createForm.totalVolumeM3}
                  onChange={(event) =>
                    onFormChange('totalVolumeM3', event.target.value)
                  }
                />
              </div>
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <div className='flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
              <div>
                <h3 className='text-sm font-semibold'>Sản phẩm</h3>
                <p className='text-xs text-muted-foreground'>
                  Thêm các dòng sản phẩm sẽ được gửi cùng đơn hàng này.
                </p>
              </div>
              <Button
                type='button'
                variant='outline'
                onClick={handleAddProduct}
                disabled={
                  isFetchingProductTypes || productTypeOptions.length === 0
                }
              >
                <Plus className='mr-2 h-4 w-4' />
                Thêm sản phẩm
              </Button>
            </div>

            {isFetchingProductTypes ? (
              <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                <Loader2 className='h-4 w-4 animate-spin' />
                Đang tải loại sản phẩm...
              </div>
            ) : productTypeOptions.length === 0 ? (
              <p className='rounded-md border border-dashed p-3 text-sm text-muted-foreground'>
                Không có loại sản phẩm đang hoạt động. Hãy tạo loại sản phẩm
                trước khi thêm sản phẩm cho đơn hàng.
              </p>
            ) : orderProducts.length === 0 ? (
              <p className='rounded-md border border-dashed p-3 text-sm text-muted-foreground'>
                Chưa có sản phẩm nào được thêm.
              </p>
            ) : (
              <div className='space-y-3'>
                {orderProducts.map((product, index) => (
                  <div
                    key={`${index}-${product.product_type_id}`}
                    className='rounded-md border bg-muted/10 p-3'
                  >
                    <div className='mb-3 flex items-center justify-between gap-2'>
                      <p className='text-sm font-medium'>
                        Sản phẩm #{index + 1}
                      </p>
                      <Button
                        type='button'
                        variant='ghost'
                        size='icon'
                        onClick={() => handleRemoveProduct(index)}
                        aria-label={`Xóa sản phẩm ${index + 1}`}
                      >
                        <Trash2 className='h-4 w-4 text-destructive' />
                      </Button>
                    </div>
                    <div className='grid gap-3 md:grid-cols-2'>
                      <div className='space-y-2'>
                        <Label htmlFor={`product-name-${index}`}>
                          Tên sản phẩm *
                        </Label>
                        <Input
                          id={`product-name-${index}`}
                          value={product.name}
                          onChange={(event) =>
                            handleProductChange(
                              index,
                              'name',
                              event.target.value
                            )
                          }
                          placeholder='Tên sản phẩm'
                        />
                      </div>
                      <div className='space-y-2'>
                        <Label htmlFor={`product-type-${index}`}>
                          Loại sản phẩm *
                        </Label>
                        <TmsCombobox
                          id={`product-type-${index}`}
                          value={
                            product.product_type_id > 0
                              ? String(product.product_type_id)
                              : ''
                          }
                          onValueChange={(value) =>
                            handleProductChange(
                              index,
                              'product_type_id',
                              Number(value)
                            )
                          }
                          options={productTypeComboboxOptions}
                          placeholder='Chọn loại sản phẩm'
                          emptyText='Không tìm thấy loại sản phẩm'
                        />
                      </div>
                      <div className='space-y-2'>
                        <Label htmlFor={`product-value-${index}`}>
                          Giá trị *
                        </Label>
                        <Input
                          id={`product-value-${index}`}
                          type='number'
                          min={0}
                          step={1}
                          value={product.value}
                          onChange={(event) =>
                            handleProductChange(
                              index,
                              'value',
                              parseProductNumber(event.target.value)
                            )
                          }
                        />
                      </div>
                      <div className='space-y-2'>
                        <Label htmlFor={`product-quantity-${index}`}>
                          Số lượng *
                        </Label>
                        <Input
                          id={`product-quantity-${index}`}
                          type='number'
                          min={1}
                          step={1}
                          value={product.quantity}
                          onChange={(event) =>
                            handleProductChange(
                              index,
                              'quantity',
                              parseProductNumber(event.target.value)
                            )
                          }
                        />
                      </div>
                      <div className='space-y-2 md:col-span-2'>
                        <Label htmlFor={`product-weight-${index}`}>
                          Khối lượng (gram) *
                        </Label>
                        <Input
                          id={`product-weight-${index}`}
                          type='number'
                          min={0}
                          step='any'
                          value={product.weight_gram}
                          onChange={(event) =>
                            handleProductChange(
                              index,
                              'weight_gram',
                              parseProductNumber(event.target.value)
                            )
                          }
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmittingOrder}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isSubmittingOrder}>
              {isSubmittingOrder ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : null}
              {orderFormMode === 'create'
                ? 'Tạo đơn hàng'
                : 'Cập nhật đơn hàng'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
