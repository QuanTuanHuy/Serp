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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import { Loader2, Plus, Trash2 } from 'lucide-react';
import { CoordinatePickerMap } from '../../../components';
import type {
  CreateOrderRequest,
  ProductType,
  Province,
  Ward,
} from '../../../types';
import {
  DELIVERY_REQUEST_TIME_OPTIONS,
  FEE_PAYER_OPTIONS,
  ORDER_PICKUP_METHOD_OPTIONS,
  ORDER_PRODUCT_CATEGORY_OPTIONS,
  ORDER_TYPE_OPTIONS,
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
            {orderFormMode === 'create' ? 'Create Order' : 'Edit Order'}
          </DialogTitle>
          <DialogDescription>
            {orderFormMode === 'create'
              ? 'Create a first-mile order as customer or admin. Required fields follow backend validation rules.'
              : 'Update an existing first-mile order. Only newly created and unconfirmed orders can be edited.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-5'>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='customerOrderCode'>Customer order code *</Label>
              <Input
                id='customerOrderCode'
                value={createForm.customerOrderCode}
                onChange={(event) =>
                  onFormChange('customerOrderCode', event.target.value)
                }
                placeholder='CUS-ORDER-001'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='note'>Note</Label>
              <Textarea
                id='note'
                value={createForm.note}
                onChange={(event) => onFormChange('note', event.target.value)}
                placeholder='Optional note for pickup/delivery'
                rows={2}
              />
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Sender information</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='senderName'>Sender name *</Label>
                <Input
                  id='senderName'
                  value={createForm.senderName}
                  onChange={(event) =>
                    onFormChange('senderName', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='senderPhone'>Sender phone *</Label>
                <Input
                  id='senderPhone'
                  value={createForm.senderPhone}
                  onChange={(event) =>
                    onFormChange('senderPhone', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='senderProvinceCode'>Sender province *</Label>
                <Select
                  value={selectedSenderProvinceCode || undefined}
                  onValueChange={(value) => {
                    onFormChange('senderProvinceCode', value);
                    onFormChange('senderWardCode', '');
                  }}
                >
                  <SelectTrigger id='senderProvinceCode'>
                    <SelectValue placeholder='Select province' />
                  </SelectTrigger>
                  <SelectContent>
                    {provinceSelectOptions.map((province) => {
                      const provinceCode = normalizeLocationCode(
                        province.provinceCode
                      );

                      if (!provinceCode) {
                        return null;
                      }

                      return (
                        <SelectItem key={provinceCode} value={provinceCode}>
                          {province.name} ({provinceCode})
                        </SelectItem>
                      );
                    })}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label htmlFor='senderWardCode'>Sender ward *</Label>
                <Select
                  value={selectedSenderWardCode || undefined}
                  onValueChange={(value) =>
                    onFormChange('senderWardCode', value)
                  }
                  disabled={!selectedSenderProvinceCode}
                >
                  <SelectTrigger id='senderWardCode'>
                    <SelectValue
                      placeholder={
                        selectedSenderProvinceCode
                          ? 'Select ward'
                          : 'Select province first'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {selectedSenderProvinceCode && isFetchingSenderWards ? (
                      <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                        Loading wards...
                      </p>
                    ) : senderWardSelectOptions.length > 0 ? (
                      senderWardSelectOptions.map((ward) => {
                        const wardCode = normalizeLocationCode(ward.wardCode);

                        if (!wardCode) {
                          return null;
                        }

                        return (
                          <SelectItem key={wardCode} value={wardCode}>
                            {ward.name} ({wardCode})
                          </SelectItem>
                        );
                      })
                    ) : (
                      <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                        No wards available.
                      </p>
                    )}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='senderAddressDetail'>Sender address *</Label>
                <Input
                  id='senderAddressDetail'
                  value={createForm.senderAddressDetail}
                  onChange={(event) =>
                    onFormChange('senderAddressDetail', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label>Sender coordinates *</Label>
                <div className='grid gap-2 md:grid-cols-2'>
                  <Input
                    value={createForm.senderLatitude}
                    placeholder='Latitude'
                    readOnly
                  />
                  <Input
                    value={createForm.senderLongitude}
                    placeholder='Longitude'
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
                    Geocode from address
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
                  Click on the map to pick sender coordinates.
                </p>
              </div>
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Receiver information</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='receiverName'>Receiver name *</Label>
                <Input
                  id='receiverName'
                  value={createForm.receiverName}
                  onChange={(event) =>
                    onFormChange('receiverName', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='receiverPhone'>Receiver phone *</Label>
                <Input
                  id='receiverPhone'
                  value={createForm.receiverPhone}
                  onChange={(event) =>
                    onFormChange('receiverPhone', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='receiverProvinceCode'>
                  Receiver province *
                </Label>
                <Select
                  value={selectedReceiverProvinceCode || undefined}
                  onValueChange={(value) => {
                    onFormChange('receiverProvinceCode', value);
                    onFormChange('receiverWardCode', '');
                  }}
                >
                  <SelectTrigger id='receiverProvinceCode'>
                    <SelectValue placeholder='Select province' />
                  </SelectTrigger>
                  <SelectContent>
                    {provinceSelectOptions.map((province) => {
                      const provinceCode = normalizeLocationCode(
                        province.provinceCode
                      );

                      if (!provinceCode) {
                        return null;
                      }

                      return (
                        <SelectItem key={provinceCode} value={provinceCode}>
                          {province.name} ({provinceCode})
                        </SelectItem>
                      );
                    })}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label htmlFor='receiverWardCode'>Receiver ward *</Label>
                <Select
                  value={selectedReceiverWardCode || undefined}
                  onValueChange={(value) =>
                    onFormChange('receiverWardCode', value)
                  }
                  disabled={!selectedReceiverProvinceCode}
                >
                  <SelectTrigger id='receiverWardCode'>
                    <SelectValue
                      placeholder={
                        selectedReceiverProvinceCode
                          ? 'Select ward'
                          : 'Select province first'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {selectedReceiverProvinceCode && isFetchingReceiverWards ? (
                      <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                        Loading wards...
                      </p>
                    ) : receiverWardSelectOptions.length > 0 ? (
                      receiverWardSelectOptions.map((ward) => {
                        const wardCode = normalizeLocationCode(ward.wardCode);

                        if (!wardCode) {
                          return null;
                        }

                        return (
                          <SelectItem key={wardCode} value={wardCode}>
                            {ward.name} ({wardCode})
                          </SelectItem>
                        );
                      })
                    ) : (
                      <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                        No wards available.
                      </p>
                    )}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='receiverAddressDetail'>
                  Receiver address *
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
                <Label>Receiver coordinates *</Label>
                <div className='grid gap-2 md:grid-cols-2'>
                  <Input
                    value={createForm.receiverLatitude}
                    placeholder='Latitude'
                    readOnly
                  />
                  <Input
                    value={createForm.receiverLongitude}
                    placeholder='Longitude'
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
                    Geocode from address
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
                  Click on the map to pick receiver coordinates.
                </p>
              </div>
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Order options</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label>Pickup method *</Label>
                <Select
                  value={createForm.pickupMethod}
                  onValueChange={(value) =>
                    onFormChange(
                      'pickupMethod',
                      value as CreateOrderFormState['pickupMethod']
                    )
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {ORDER_PICKUP_METHOD_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Delivery request time *</Label>
                <Select
                  value={createForm.deliveryRequestTime}
                  onValueChange={(value) =>
                    onFormChange(
                      'deliveryRequestTime',
                      value as CreateOrderFormState['deliveryRequestTime']
                    )
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {DELIVERY_REQUEST_TIME_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Order type *</Label>
                <Select
                  value={createForm.orderType}
                  onValueChange={(value) =>
                    onFormChange(
                      'orderType',
                      value as CreateOrderFormState['orderType']
                    )
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {ORDER_TYPE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Fee payer *</Label>
                <Select
                  value={createForm.feePayer}
                  onValueChange={(value) =>
                    onFormChange(
                      'feePayer',
                      value as CreateOrderFormState['feePayer']
                    )
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {FEE_PAYER_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>COD</Label>
                <Select
                  value={createForm.isCod}
                  onValueChange={(value) =>
                    onFormChange('isCod', value as 'true' | 'false')
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='false'>No COD</SelectItem>
                    <SelectItem value='true'>COD</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2 md:col-span-2'>
                <Label>Product category</Label>
                <Select
                  value={createForm.orderProductCategory}
                  onValueChange={(value) =>
                    onFormChange(
                      'orderProductCategory',
                      value as CreateOrderFormState['orderProductCategory']
                    )
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select category (optional)' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='NONE'>No category</SelectItem>
                    {ORDER_PRODUCT_CATEGORY_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <div className='space-y-3 rounded-md border p-3'>
            <h3 className='text-sm font-semibold'>Pickup and dimensions</h3>
            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='pickupTimeStart'>Pickup start</Label>
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
                <Label htmlFor='pickupTimeEnd'>Pickup end</Label>
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
                <Label htmlFor='dimensionLengthCm'>Length (cm)</Label>
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
                <Label htmlFor='dimensionWidthCm'>Width (cm)</Label>
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
                <Label htmlFor='dimensionHeightCm'>Height (cm)</Label>
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
                <Label htmlFor='totalVolumeM3'>Total volume (m3)</Label>
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
                <h3 className='text-sm font-semibold'>Products</h3>
                <p className='text-xs text-muted-foreground'>
                  Add product lines that will be sent with this order.
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
                Add product
              </Button>
            </div>

            {isFetchingProductTypes ? (
              <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                <Loader2 className='h-4 w-4 animate-spin' />
                Loading product types...
              </div>
            ) : productTypeOptions.length === 0 ? (
              <p className='rounded-md border border-dashed p-3 text-sm text-muted-foreground'>
                No active product types are available. Create a product type
                before adding order products.
              </p>
            ) : orderProducts.length === 0 ? (
              <p className='rounded-md border border-dashed p-3 text-sm text-muted-foreground'>
                No products added yet.
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
                        Product #{index + 1}
                      </p>
                      <Button
                        type='button'
                        variant='ghost'
                        size='icon'
                        onClick={() => handleRemoveProduct(index)}
                        aria-label={`Remove product ${index + 1}`}
                      >
                        <Trash2 className='h-4 w-4 text-destructive' />
                      </Button>
                    </div>
                    <div className='grid gap-3 md:grid-cols-2'>
                      <div className='space-y-2'>
                        <Label htmlFor={`product-name-${index}`}>
                          Product name *
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
                          placeholder='Product name'
                        />
                      </div>
                      <div className='space-y-2'>
                        <Label htmlFor={`product-type-${index}`}>
                          Product type *
                        </Label>
                        <Select
                          value={
                            product.product_type_id > 0
                              ? String(product.product_type_id)
                              : undefined
                          }
                          onValueChange={(value) =>
                            handleProductChange(
                              index,
                              'product_type_id',
                              Number(value)
                            )
                          }
                        >
                          <SelectTrigger id={`product-type-${index}`}>
                            <SelectValue placeholder='Select product type' />
                          </SelectTrigger>
                          <SelectContent>
                            {productTypeOptions.map((productType) => (
                              <SelectItem
                                key={productType.id}
                                value={String(productType.id)}
                              >
                                {productType.name} ({productType.code})
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                      <div className='space-y-2'>
                        <Label htmlFor={`product-value-${index}`}>
                          Value *
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
                          Quantity *
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
                          Weight (gram) *
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
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmittingOrder}>
              {isSubmittingOrder ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : null}
              {orderFormMode === 'create' ? 'Create order' : 'Update order'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
