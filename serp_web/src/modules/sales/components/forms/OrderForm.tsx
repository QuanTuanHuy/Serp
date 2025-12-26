/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Form Component
*/

'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Input,
  Label,
  Badge,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Plus,
  Trash2,
  Search,
  Package,
  ShoppingCart,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useGetCustomersQuery, useGetProductsQuery } from '../../api/salesApi';
import type {
  Order,
  OrderCreationForm,
  OrderUpdateForm,
  OrderItem,
  SaleChannel,
} from '../../types';

interface OrderFormProps {
  order?: Order;
  onSubmit: (data: OrderCreationForm | OrderUpdateForm) => Promise<void>;
  onCancel: () => void;
}

export const OrderForm: React.FC<OrderFormProps> = ({
  order,
  onSubmit,
  onCancel,
}) => {
  const router = useRouter();
  const isEditMode = !!order;

  // Form state
  const [customerId, setCustomerId] = useState(order?.toCustomerId || '');
  const [orderName, setOrderName] = useState(order?.orderName || '');
  const [saleChannel, setSaleChannel] = useState<SaleChannel>(
    order?.saleChannelId || 'ONLINE'
  );
  const [priority, setPriority] = useState(order?.priority || 1);
  const [deliveryBeforeDate, setDeliveryBeforeDate] = useState(
    order?.deliveryBeforeDate
      ? new Date(order.deliveryBeforeDate).toISOString().split('T')[0]
      : ''
  );
  const [deliveryAfterDate, setDeliveryAfterDate] = useState(
    order?.deliveryAfterDate
      ? new Date(order.deliveryAfterDate).toISOString().split('T')[0]
      : ''
  );
  const [note, setNote] = useState(order?.note || '');
  const [items, setItems] = useState<OrderItem[]>(
    order?.items?.map((item) => ({
      productId: item.productId,
      orderItemSeqId: item.orderItemSeqId,
      quantity: item.quantity,
      tax: item.tax,
      discount: item.discount,
      expireAfter: undefined,
    })) || []
  );

  // Search states
  const [customerSearch, setCustomerSearch] = useState('');
  const [productSearch, setProductSearch] = useState('');
  const [showCustomerDropdown, setShowCustomerDropdown] = useState(false);
  const [showProductDropdown, setShowProductDropdown] = useState(false);

  // Submission state
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Fetch customers and products
  const { data: customersResponse } = useGetCustomersQuery({
    filters: { query: customerSearch },
    pagination: { page: 0, size: 10 },
  });

  const { data: productsResponse } = useGetProductsQuery({
    filters: { query: productSearch },
    pagination: { page: 0, size: 20 },
  });

  const customers = customersResponse?.data?.items || [];
  const products = productsResponse?.data?.items || [];

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!customerId) {
      newErrors.customerId = 'Customer is required';
    }

    if (!saleChannel) {
      newErrors.saleChannel = 'Sale channel is required';
    }

    if (!isEditMode && items.length === 0) {
      newErrors.items = 'At least one item is required';
    }

    if (deliveryBeforeDate && deliveryAfterDate) {
      const before = new Date(deliveryBeforeDate);
      const after = new Date(deliveryAfterDate);
      if (before < after) {
        newErrors.deliveryBeforeDate =
          'Delivery before date must be after delivery after date';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      if (isEditMode) {
        const updateData: OrderUpdateForm = {
          orderName: orderName || undefined,
          saleChannelId: saleChannel,
          priority,
          deliveryBeforeDate: deliveryBeforeDate || undefined,
          deliveryAfterDate: deliveryAfterDate || undefined,
          note: note || undefined,
        };
        await onSubmit(updateData);
      } else {
        const createData: OrderCreationForm = {
          toCustomerId: customerId,
          orderName: orderName || undefined,
          saleChannelId: saleChannel,
          priority,
          deliveryBeforeDate: deliveryBeforeDate || undefined,
          deliveryAfterDate: deliveryAfterDate || undefined,
          note: note || undefined,
          items,
        };
        await onSubmit(createData);
      }
    } catch (error) {
      console.error('Form submission error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const addItem = (productId: string) => {
    const maxSeqId = Math.max(0, ...items.map((i) => i.orderItemSeqId));
    setItems([
      ...items,
      {
        productId,
        orderItemSeqId: maxSeqId + 1,
        quantity: 1,
        tax: 0,
        discount: 0,
      },
    ]);
    setProductSearch('');
    setShowProductDropdown(false);
  };

  const updateItem = (index: number, field: keyof OrderItem, value: number) => {
    const newItems = [...items];
    newItems[index] = { ...newItems[index], [field]: value };
    setItems(newItems);
  };

  const removeItem = (index: number) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const selectedCustomer = customers.find((c) => c.id === customerId);
  const getProductById = (productId: string) =>
    products.find((p) => p.id === productId);

  return (
    <form onSubmit={handleSubmit} className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-4'>
          <Button type='button' variant='ghost' size='icon' onClick={onCancel}>
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>
              {isEditMode ? 'Edit Order' : 'Create New Order'}
            </h1>
            <p className='text-muted-foreground'>
              {isEditMode
                ? 'Update order information'
                : 'Fill in the details to create a new order'}
            </p>
          </div>
        </div>

        <div className='flex items-center gap-2'>
          <Button type='button' variant='outline' onClick={onCancel}>
            Cancel
          </Button>
          <Button type='submit' disabled={isSubmitting}>
            {isSubmitting
              ? 'Saving...'
              : isEditMode
                ? 'Update Order'
                : 'Create Order'}
          </Button>
        </div>
      </div>

      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
        {/* Main Form */}
        <div className='lg:col-span-2 space-y-6'>
          {/* Basic Information */}
          <Card>
            <CardHeader>
              <div className='flex items-center gap-2'>
                <ShoppingCart className='h-5 w-5 text-primary' />
                <h3 className='font-semibold'>Order Information</h3>
              </div>
            </CardHeader>
            <CardContent className='space-y-4'>
              {/* Customer Selection */}
              {!isEditMode && (
                <div className='space-y-2'>
                  <Label htmlFor='customer'>
                    Customer <span className='text-destructive'>*</span>
                  </Label>
                  <div className='relative'>
                    <div className='relative'>
                      <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                      <Input
                        id='customer'
                        placeholder='Search customer...'
                        value={selectedCustomer?.name || customerSearch}
                        onChange={(e) => {
                          setCustomerSearch(e.target.value);
                          setShowCustomerDropdown(true);
                          if (!e.target.value) setCustomerId('');
                        }}
                        onFocus={() => setShowCustomerDropdown(true)}
                        className={cn(
                          'pl-10',
                          errors.customerId && 'border-destructive'
                        )}
                      />
                    </div>

                    {showCustomerDropdown && customers.length > 0 && (
                      <div className='absolute z-10 w-full mt-1 bg-popover border rounded-lg shadow-lg max-h-60 overflow-auto'>
                        {customers.map((customer) => (
                          <button
                            key={customer.id}
                            type='button'
                            onClick={() => {
                              setCustomerId(customer.id);
                              setCustomerSearch('');
                              setShowCustomerDropdown(false);
                            }}
                            className='w-full px-4 py-2 text-left hover:bg-muted transition-colors'
                          >
                            <p className='font-medium'>{customer.name}</p>
                            <p className='text-sm text-muted-foreground'>
                              {customer.email}
                            </p>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                  {errors.customerId && (
                    <p className='text-sm text-destructive'>
                      {errors.customerId}
                    </p>
                  )}
                  {selectedCustomer && (
                    <Badge variant='secondary' className='mt-2'>
                      Selected: {selectedCustomer.name}
                    </Badge>
                  )}
                </div>
              )}

              {/* Order Name */}
              <div className='space-y-2'>
                <Label htmlFor='orderName'>Order Name</Label>
                <Input
                  id='orderName'
                  value={orderName}
                  onChange={(e) => setOrderName(e.target.value)}
                  placeholder='e.g., Monthly Supply Order'
                />
              </div>

              {/* Sale Channel */}
              <div className='space-y-2'>
                <Label htmlFor='saleChannel'>
                  Sale Channel <span className='text-destructive'>*</span>
                </Label>
                <select
                  id='saleChannel'
                  value={saleChannel}
                  onChange={(e) =>
                    setSaleChannel(e.target.value as SaleChannel)
                  }
                  className={cn(
                    'w-full px-3 py-2 border rounded-lg bg-background',
                    errors.saleChannel && 'border-destructive'
                  )}
                >
                  <option value='ONLINE'>Online</option>
                  <option value='PARTNER'>Partner</option>
                  <option value='RETAIL'>Retail</option>
                </select>
                {errors.saleChannel && (
                  <p className='text-sm text-destructive'>
                    {errors.saleChannel}
                  </p>
                )}
              </div>

              {/* Priority */}
              <div className='space-y-2'>
                <Label htmlFor='priority'>Priority</Label>
                <Input
                  id='priority'
                  type='number'
                  min='1'
                  max='10'
                  value={priority}
                  onChange={(e) => setPriority(parseInt(e.target.value) || 1)}
                />
                <p className='text-xs text-muted-foreground'>
                  1 (lowest) to 10 (highest)
                </p>
              </div>

              {/* Delivery Dates */}
              <div className='grid grid-cols-2 gap-4'>
                <div className='space-y-2'>
                  <Label htmlFor='deliveryAfter'>Deliver After</Label>
                  <Input
                    id='deliveryAfter'
                    type='date'
                    value={deliveryAfterDate}
                    onChange={(e) => setDeliveryAfterDate(e.target.value)}
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='deliveryBefore'>Deliver Before</Label>
                  <Input
                    id='deliveryBefore'
                    type='date'
                    value={deliveryBeforeDate}
                    onChange={(e) => setDeliveryBeforeDate(e.target.value)}
                    className={cn(
                      errors.deliveryBeforeDate && 'border-destructive'
                    )}
                  />
                  {errors.deliveryBeforeDate && (
                    <p className='text-xs text-destructive'>
                      {errors.deliveryBeforeDate}
                    </p>
                  )}
                </div>
              </div>

              {/* Note */}
              <div className='space-y-2'>
                <Label htmlFor='note'>Notes</Label>
                <textarea
                  id='note'
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder='Additional notes about this order...'
                  rows={4}
                  className='w-full px-3 py-2 border rounded-lg bg-background resize-none'
                />
              </div>
            </CardContent>
          </Card>

          {/* Order Items */}
          {!isEditMode && (
            <Card>
              <CardHeader>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2'>
                    <Package className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Order Items</h3>
                  </div>
                  <Badge variant='secondary'>{items.length} items</Badge>
                </div>
              </CardHeader>
              <CardContent className='space-y-4'>
                {/* Add Product Search */}
                <div className='relative'>
                  <div className='relative'>
                    <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
                    <Input
                      placeholder='Search and add products...'
                      value={productSearch}
                      onChange={(e) => {
                        setProductSearch(e.target.value);
                        setShowProductDropdown(true);
                      }}
                      onFocus={() => setShowProductDropdown(true)}
                      className='pl-10'
                    />
                  </div>

                  {showProductDropdown && products.length > 0 && (
                    <div className='absolute z-10 w-full mt-1 bg-popover border rounded-lg shadow-lg max-h-60 overflow-auto'>
                      {products.map((product) => (
                        <button
                          key={product.id}
                          type='button'
                          onClick={() => addItem(product.id)}
                          className='w-full px-4 py-2 text-left hover:bg-muted transition-colors flex items-center justify-between'
                        >
                          <div>
                            <p className='font-medium'>{product.name}</p>
                            <p className='text-sm text-muted-foreground'>
                              {product.skuCode} • ${product.retailPrice}
                            </p>
                          </div>
                          <Plus className='h-4 w-4' />
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {errors.items && (
                  <p className='text-sm text-destructive'>{errors.items}</p>
                )}

                {/* Items List */}
                <div className='space-y-3'>
                  {items.map((item, index) => {
                    const product = getProductById(item.productId);
                    return (
                      <div
                        key={index}
                        className='flex items-start gap-3 p-4 border rounded-lg'
                      >
                        <div className='flex-1 space-y-3'>
                          <div>
                            <p className='font-medium'>
                              {product?.name || 'Unknown Product'}
                            </p>
                            <p className='text-sm text-muted-foreground'>
                              {product?.skuCode}
                            </p>
                          </div>

                          <div className='grid grid-cols-3 gap-2'>
                            <div>
                              <Label className='text-xs'>Quantity</Label>
                              <Input
                                type='number'
                                min='1'
                                value={item.quantity}
                                onChange={(e) =>
                                  updateItem(
                                    index,
                                    'quantity',
                                    parseInt(e.target.value) || 1
                                  )
                                }
                                className='h-9'
                              />
                            </div>
                            <div>
                              <Label className='text-xs'>Tax (%)</Label>
                              <Input
                                type='number'
                                min='0'
                                max='100'
                                step='0.1'
                                value={item.tax || 0}
                                onChange={(e) =>
                                  updateItem(
                                    index,
                                    'tax',
                                    parseFloat(e.target.value) || 0
                                  )
                                }
                                className='h-9'
                              />
                            </div>
                            <div>
                              <Label className='text-xs'>Discount (%)</Label>
                              <Input
                                type='number'
                                min='0'
                                max='100'
                                step='0.1'
                                value={item.discount || 0}
                                onChange={(e) =>
                                  updateItem(
                                    index,
                                    'discount',
                                    parseFloat(e.target.value) || 0
                                  )
                                }
                                className='h-9'
                              />
                            </div>
                          </div>
                        </div>

                        <Button
                          type='button'
                          variant='ghost'
                          size='icon'
                          onClick={() => removeItem(index)}
                          className='text-destructive hover:text-destructive'
                        >
                          <Trash2 className='h-4 w-4' />
                        </Button>
                      </div>
                    );
                  })}

                  {items.length === 0 && (
                    <div className='text-center py-8 text-muted-foreground'>
                      <Package className='h-12 w-12 mx-auto mb-2 opacity-50' />
                      <p>No items added yet</p>
                      <p className='text-sm'>Search and add products above</p>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Summary Sidebar */}
        <div className='space-y-6'>
          <Card className='sticky top-6'>
            <CardHeader>
              <h3 className='font-semibold'>Order Summary</h3>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='space-y-2 text-sm'>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Customer</span>
                  <span className='font-medium'>
                    {selectedCustomer?.name || 'Not selected'}
                  </span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Sale Channel</span>
                  <Badge variant='secondary'>{saleChannel}</Badge>
                </div>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Priority</span>
                  <span className='font-medium'>{priority}</span>
                </div>
                {!isEditMode && (
                  <>
                    <div className='flex justify-between'>
                      <span className='text-muted-foreground'>Total Items</span>
                      <span className='font-medium'>{items.length}</span>
                    </div>
                    <div className='flex justify-between'>
                      <span className='text-muted-foreground'>
                        Total Quantity
                      </span>
                      <span className='font-medium'>
                        {items.reduce((sum, item) => sum + item.quantity, 0)}
                      </span>
                    </div>
                  </>
                )}
              </div>

              {deliveryBeforeDate && (
                <div className='pt-4 border-t'>
                  <p className='text-xs text-muted-foreground mb-1'>
                    Deliver Before
                  </p>
                  <p className='text-sm font-medium'>
                    {new Date(deliveryBeforeDate).toLocaleDateString()}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </form>
  );
};

export default OrderForm;
