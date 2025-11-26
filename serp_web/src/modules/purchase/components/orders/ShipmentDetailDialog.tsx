/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipment detail dialog with inline editing
*/

'use client';

import React, { useState, useMemo, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Badge,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Separator } from '@/shared/components/ui/separator';
import { Loader2, Plus, Edit2, Trash2, Save, X, Building2 } from 'lucide-react';
import {
  useGetShipmentByIdQuery,
  useGetFacilitiesQuery,
  useGetProductsQuery,
} from '../../services/purchaseApi';
import { formatDate } from '@/shared/utils';
import { useNotification } from '@/shared/hooks';
import type {
  ShipmentItemAddRequest,
  InventoryItemDetailUpdateRequest,
  UpdateShipmentRequest,
  ShipmentFacilityUpdateRequest,
  OrderDetail,
} from '../../types';

const updateShipmentSchema = z.object({
  shipmentName: z.string().min(1, 'Tên phiếu nhập không được để trống'),
  expectedDeliveryDate: z.string().min(1, 'Vui lòng chọn ngày'),
  note: z.string().optional(),
});

type UpdateShipmentFormData = z.infer<typeof updateShipmentSchema>;

interface ShipmentDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shipmentId: string | null;
  order: OrderDetail;
  canEdit: boolean;
  existingShipments: any[];
  onUpdateShipment: (
    shipmentId: string,
    data: UpdateShipmentRequest
  ) => Promise<boolean>;
  onUpdateFacility: (
    shipmentId: string,
    data: ShipmentFacilityUpdateRequest
  ) => Promise<boolean>;
  onAddItem: (
    shipmentId: string,
    data: ShipmentItemAddRequest
  ) => Promise<boolean>;
  onUpdateItem: (
    shipmentId: string,
    itemId: string,
    data: InventoryItemDetailUpdateRequest
  ) => Promise<boolean>;
  onDeleteItem: (shipmentId: string, itemId: string) => Promise<boolean>;
  isLoading?: boolean;
}

export const ShipmentDetailDialog: React.FC<ShipmentDetailDialogProps> = ({
  open,
  onOpenChange,
  shipmentId,
  order,
  canEdit,
  existingShipments,
  onUpdateShipment,
  onUpdateFacility,
  onAddItem,
  onUpdateItem,
  onDeleteItem,
  isLoading = false,
}) => {
  const notification = useNotification();
  const [editingItemId, setEditingItemId] = useState<string | null>(null);
  const [isAddingItem, setIsAddingItem] = useState(false);
  const [isEditingShipment, setIsEditingShipment] = useState(false);
  const [isChangingFacility, setIsChangingFacility] = useState(false);
  const [selectedFacility, setSelectedFacility] = useState('');

  // Fetch shipment detail
  const { data: shipmentResponse, refetch } = useGetShipmentByIdQuery(
    shipmentId || '',
    { skip: !shipmentId }
  );

  const shipment = shipmentResponse?.data;

  // Fetch facilities
  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    page: 1,
    size: 100,
  });

  const facilities = useMemo(
    () => facilitiesResponse?.data?.items || [],
    [facilitiesResponse]
  );

  // Fetch products
  const { data: productsResponse } = useGetProductsQuery({
    page: 1,
    size: 1000,
  });

  const products = useMemo(
    () => productsResponse?.data?.items || [],
    [productsResponse]
  );

  // Calculate remaining quantities for each product
  const productQuantities = useMemo(() => {
    if (!shipment) return new Map();

    const quantityMap = new Map();

    // Initialize with order quantities
    order.orderItems?.forEach((item) => {
      quantityMap.set(item.productId, {
        orderedQuantity: item.quantity,
        usedQuantity: 0,
        currentShipmentQuantity: 0,
        orderItemId: item.id,
      });
    });

    // Sum quantities from all shipments except current one
    existingShipments.forEach((s) => {
      if (s.id === shipmentId) return; // Skip current shipment
      s.items?.forEach((item: any) => {
        const data = quantityMap.get(item.productId);
        if (data) {
          data.usedQuantity += item.quantity;
        }
      });
    });

    // Track current shipment quantities
    shipment.items?.forEach((item) => {
      const data = quantityMap.get(item.productId);
      if (data) {
        data.currentShipmentQuantity = item.quantity;
      }
    });

    return quantityMap;
  }, [shipment, order, existingShipments, shipmentId]);

  // Get available products (not yet in current shipment)
  const availableProducts = useMemo(() => {
    if (!shipment) return [];

    const usedProductIds = new Set(
      shipment.items?.map((item) => item.productId) || []
    );

    return (
      order.orderItems?.filter((item) => {
        if (usedProductIds.has(item.productId)) return false;
        const data = productQuantities.get(item.productId);
        return data && data.usedQuantity < data.orderedQuantity;
      }) || []
    );
  }, [shipment, order, productQuantities]);

  // Update form for shipment
  const updateForm = useForm<UpdateShipmentFormData>({
    resolver: zodResolver(updateShipmentSchema),
    defaultValues: {
      shipmentName: shipment?.shipmentName || '',
      expectedDeliveryDate: shipment?.expectedDeliveryDate || '',
      note: shipment?.note || '',
    },
  });

  useEffect(() => {
    if (shipment) {
      updateForm.reset({
        shipmentName: shipment.shipmentName,
        expectedDeliveryDate: shipment.expectedDeliveryDate || '',
        note: shipment.note || '',
      });
      setSelectedFacility(shipment.facilityId || '');
    }
  }, [shipment, updateForm]);

  // Item editing state
  const [editingItem, setEditingItem] = useState<{
    quantity: number;
    lotId?: string;
    note?: string;
    manufacturingDate?: string;
    expirationDate?: string;
  } | null>(null);

  // New item state
  const [newItem, setNewItem] = useState<ShipmentItemAddRequest>({
    productId: '',
    quantity: 1,
    facilityId: shipment?.facilityId || '',
    orderItemId: '',
    lotId: '',
    note: '',
    manufacturingDate: '',
    expirationDate: '',
  });

  const handleEditItem = (item: any) => {
    setEditingItemId(item.id);
    setEditingItem({
      quantity: item.quantity,
      lotId: item.lotId || '',
      note: item.note || '',
      manufacturingDate: item.manufacturingDate || '',
      expirationDate: item.expirationDate || '',
    });
  };

  const handleSaveItem = async (itemId: string) => {
    if (!shipmentId || !editingItem) return;

    // Find the item being edited to get its productId
    const currentItem = shipment?.items?.find((item) => item.id === itemId);
    if (!currentItem) return;

    const maxQuantity = getMaxQuantityForProduct(currentItem.productId);
    if (editingItem.quantity > maxQuantity) {
      notification.error(`Số lượng vượt quá giới hạn. Tối đa: ${maxQuantity}`);
      return;
    }

    const success = await onUpdateItem(shipmentId, itemId, {
      quantity: editingItem.quantity,
      lotId: editingItem.lotId,
      note: editingItem.note,
      manufacturingDate: editingItem.manufacturingDate,
      expirationDate: editingItem.expirationDate,
    });

    if (success) {
      setEditingItemId(null);
      setEditingItem(null);
      refetch();
    }
  };

  const handleCancelEdit = () => {
    setEditingItemId(null);
    setEditingItem(null);
  };

  const handleDeleteItem = async (itemId: string) => {
    if (!shipmentId) return;

    const success = await onDeleteItem(shipmentId, itemId);
    if (success) {
      refetch();
    }
  };

  const handleAddNewItem = async () => {
    if (!shipmentId || !newItem.productId) return;

    const maxQuantity = getRemainingQuantityForNewItem(newItem.productId);
    if (newItem.quantity > maxQuantity) {
      notification.error(`Số lượng vượt quá giới hạn. Tối đa: ${maxQuantity}`);
      return;
    }

    const success = await onAddItem(shipmentId, {
      ...newItem,
      facilityId: shipment?.facilityId || '',
    });

    if (success) {
      setIsAddingItem(false);
      setNewItem({
        productId: '',
        quantity: 1,
        facilityId: shipment?.facilityId || '',
        orderItemId: '',
        lotId: '',
        note: '',
        manufacturingDate: '',
        expirationDate: '',
      });
      refetch();
    }
  };

  const handleUpdateShipment = async (data: UpdateShipmentFormData) => {
    if (!shipmentId) return;

    const success = await onUpdateShipment(shipmentId, {
      shipmentName: data.shipmentName,
      expectedDeliveryDate: data.expectedDeliveryDate,
      note: data.note,
    });

    if (success) {
      setIsEditingShipment(false);
      refetch();
    }
  };

  const handleChangeFacility = async () => {
    if (!shipmentId || !selectedFacility) return;

    const success = await onUpdateFacility(shipmentId, {
      facilityId: selectedFacility,
    });

    if (success) {
      setIsChangingFacility(false);
      refetch();
    }
  };

  const getProductName = (productId: string) => {
    const product = products.find((p) => p.id === productId);
    return product?.name || productId;
  };

  const getMaxQuantityForProduct = (productId: string) => {
    const data = productQuantities.get(productId);
    if (!data) return 0;
    // Max = ordered - used in other shipments + current quantity in this shipment
    return (
      data.orderedQuantity - data.usedQuantity + data.currentShipmentQuantity
    );
  };

  const getRemainingQuantityForNewItem = (productId: string) => {
    const data = productQuantities.get(productId);
    if (!data) return 0;
    // Remaining = ordered - used in other shipments
    return data.orderedQuantity - data.usedQuantity;
  };

  const getProductUnit = (productId: string) => {
    const product = order.orderItems?.find(
      (item) => item.productId === productId
    );
    return product?.unit || '';
  };

  const getFacilityName = (facilityId?: string) => {
    if (!facilityId) return '';
    const facility = facilities.find((f) => f.id === facilityId);
    return facility?.name || facilityId;
  };

  const getStatusBadge = (statusId: string) => {
    const statusMap: Record<string, { label: string; variant: any }> = {
      CREATED: { label: 'Đã tạo', variant: 'secondary' },
      READY: { label: 'Sẵn sàng', variant: 'default' },
      IMPORTED: { label: 'Đã nhập', variant: 'success' },
      EXPORTED: { label: 'Đã xuất', variant: 'outline' },
    };

    const status = statusMap[statusId] || {
      label: statusId,
      variant: 'secondary',
    };
    return <Badge variant={status.variant}>{status.label}</Badge>;
  };

  if (!shipment) return null;

  const showActions = canEdit && order.statusId === 'APPROVED';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-[calc(100%-2rem)] sm:max-w-7xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <div className='flex items-center justify-between'>
            <DialogTitle>{shipment.shipmentName}</DialogTitle>
            {showActions && !isEditingShipment && (
              <div className='flex gap-2'>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => setIsChangingFacility(true)}
                >
                  <Building2 className='h-4 w-4 mr-2' />
                  Đổi kho
                </Button>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => setIsEditingShipment(true)}
                >
                  <Edit2 className='h-4 w-4 mr-2' />
                  Chỉnh sửa
                </Button>
              </div>
            )}
          </div>
        </DialogHeader>

        {/* Facility Change Dialog */}
        {isChangingFacility && (
          <div className='p-4 border rounded-lg bg-muted/30'>
            <h4 className='font-medium mb-4'>Đổi kho</h4>
            <div className='flex gap-4 items-end'>
              <div className='flex-1'>
                <Label htmlFor='newFacility'>Chọn kho mới</Label>
                <Select
                  value={selectedFacility}
                  onValueChange={setSelectedFacility}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Chọn kho' />
                  </SelectTrigger>
                  <SelectContent>
                    {facilities.map((facility) => (
                      <SelectItem key={facility.id} value={facility.id}>
                        {facility.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button onClick={handleChangeFacility} disabled={isLoading}>
                Lưu
              </Button>
              <Button
                variant='outline'
                onClick={() => {
                  setIsChangingFacility(false);
                  setSelectedFacility(shipment.facilityId || '');
                }}
              >
                Hủy
              </Button>
            </div>
          </div>
        )}

        {/* Edit Shipment Form */}
        {isEditingShipment && (
          <form
            onSubmit={updateForm.handleSubmit(handleUpdateShipment)}
            className='p-4 border rounded-lg bg-muted/30 space-y-4'
          >
            <h4 className='font-medium'>Chỉnh sửa phiếu nhập</h4>
            <div className='grid grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='shipmentName'>
                  Tên phiếu nhập <span className='text-destructive'>*</span>
                </Label>
                <Input
                  id='shipmentName'
                  {...updateForm.register('shipmentName')}
                />
                {updateForm.formState.errors.shipmentName && (
                  <p className='text-sm text-destructive'>
                    {updateForm.formState.errors.shipmentName.message}
                  </p>
                )}
              </div>
              <div className='space-y-2'>
                <Label htmlFor='expectedDeliveryDate'>
                  Ngày giao dự kiến <span className='text-destructive'>*</span>
                </Label>
                <Input
                  id='expectedDeliveryDate'
                  type='date'
                  {...updateForm.register('expectedDeliveryDate')}
                />
                {updateForm.formState.errors.expectedDeliveryDate && (
                  <p className='text-sm text-destructive'>
                    {updateForm.formState.errors.expectedDeliveryDate.message}
                  </p>
                )}
              </div>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='note'>Ghi chú</Label>
              <Input id='note' {...updateForm.register('note')} />
              {updateForm.formState.errors.note && (
                <p className='text-sm text-destructive'>
                  {updateForm.formState.errors.note.message}
                </p>
              )}
            </div>
            <div className='flex gap-2'>
              <Button type='submit' disabled={isLoading}>
                Lưu
              </Button>
              <Button
                type='button'
                variant='outline'
                onClick={() => setIsEditingShipment(false)}
              >
                Hủy
              </Button>
            </div>
          </form>
        )}

        {/* Shipment Information */}
        {!isEditingShipment && (
          <div className='grid grid-cols-2 gap-4 text-sm'>
            <div>
              <span className='text-muted-foreground'>Mã phiếu:</span>
              <p className='font-medium'>{shipment.id}</p>
            </div>
            <div>
              <span className='text-muted-foreground'>Trạng thái:</span>
              <div className='mt-1'>{getStatusBadge(shipment.statusId)}</div>
            </div>
            <div>
              <span className='text-muted-foreground'>Ngày tạo:</span>
              <p className='font-medium'>{formatDate(shipment.createdStamp)}</p>
            </div>
            <div>
              <span className='text-muted-foreground'>Ngày giao dự kiến:</span>
              <p className='font-medium'>
                {shipment.expectedDeliveryDate
                  ? formatDate(shipment.expectedDeliveryDate)
                  : 'Chưa có'}
              </p>
            </div>
            <div>
              <span className='text-muted-foreground'>Kho:</span>
              <p className='font-medium'>
                {getFacilityName(shipment.facilityId)}
              </p>
            </div>
            {shipment.note && (
              <div className='col-span-2'>
                <span className='text-muted-foreground'>Ghi chú:</span>
                <p className='font-medium'>{shipment.note}</p>
              </div>
            )}
          </div>
        )}

        <Separator />

        {/* Products List */}
        <div className='space-y-4'>
          <div className='flex items-center justify-between'>
            <h3 className='text-lg font-medium'>Danh sách sản phẩm</h3>
            {showActions && !isAddingItem && (
              <Button
                variant='outline'
                size='sm'
                onClick={() => setIsAddingItem(true)}
                disabled={availableProducts.length === 0}
              >
                <Plus className='h-4 w-4 mr-2' />
                Thêm sản phẩm
              </Button>
            )}
          </div>

          <div className='border rounded-lg overflow-hidden'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sản phẩm</TableHead>
                  <TableHead>Số lượng</TableHead>
                  <TableHead>Đơn vị</TableHead>
                  <TableHead>Lô hàng</TableHead>
                  <TableHead>NSX</TableHead>
                  <TableHead>HSD</TableHead>
                  {showActions && (
                    <TableHead className='text-right'>Thao tác</TableHead>
                  )}
                </TableRow>
              </TableHeader>
              <TableBody>
                {shipment.items?.map((item) => {
                  const isEditing = editingItemId === item.id;

                  return (
                    <TableRow key={item.id}>
                      <TableCell>{getProductName(item.productId)}</TableCell>
                      <TableCell>
                        {isEditing ? (
                          <div className='space-y-1'>
                            <Input
                              type='number'
                              min={1}
                              max={getMaxQuantityForProduct(item.productId)}
                              value={editingItem?.quantity || 1}
                              onChange={(e) =>
                                setEditingItem((prev) => ({
                                  ...prev!,
                                  quantity: Number(e.target.value),
                                }))
                              }
                              className='w-20'
                            />
                            <p className='text-xs text-muted-foreground'>
                              Tối đa: {getMaxQuantityForProduct(item.productId)}
                            </p>
                          </div>
                        ) : (
                          item.quantity
                        )}
                      </TableCell>
                      <TableCell>{getProductUnit(item.productId)}</TableCell>
                      <TableCell>
                        {isEditing ? (
                          <Input
                            value={editingItem?.lotId || ''}
                            onChange={(e) =>
                              setEditingItem((prev) => ({
                                ...prev!,
                                lotId: e.target.value,
                              }))
                            }
                          />
                        ) : (
                          item.lotId || '-'
                        )}
                      </TableCell>
                      <TableCell>
                        {isEditing ? (
                          <Input
                            type='date'
                            value={editingItem?.manufacturingDate || ''}
                            onChange={(e) =>
                              setEditingItem((prev) => ({
                                ...prev!,
                                manufacturingDate: e.target.value,
                              }))
                            }
                          />
                        ) : item.manufacturingDate ? (
                          formatDate(item.manufacturingDate)
                        ) : (
                          '-'
                        )}
                      </TableCell>
                      <TableCell>
                        {isEditing ? (
                          <Input
                            type='date'
                            value={editingItem?.expirationDate || ''}
                            onChange={(e) =>
                              setEditingItem((prev) => ({
                                ...prev!,
                                expirationDate: e.target.value,
                              }))
                            }
                          />
                        ) : item.expirationDate ? (
                          formatDate(item.expirationDate)
                        ) : (
                          '-'
                        )}
                      </TableCell>
                      {showActions && (
                        <TableCell className='text-right'>
                          {isEditing ? (
                            <div className='flex justify-end gap-2'>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={() => handleSaveItem(item.id)}
                              >
                                <Save className='h-4 w-4' />
                              </Button>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={handleCancelEdit}
                              >
                                <X className='h-4 w-4' />
                              </Button>
                            </div>
                          ) : (
                            <div className='flex justify-end gap-2'>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={() => handleEditItem(item)}
                              >
                                <Edit2 className='h-4 w-4' />
                              </Button>
                              <Button
                                size='sm'
                                variant='ghost'
                                onClick={() => handleDeleteItem(item.id)}
                              >
                                <Trash2 className='h-4 w-4' />
                              </Button>
                            </div>
                          )}
                        </TableCell>
                      )}
                    </TableRow>
                  );
                })}

                {/* Add New Item Row */}
                {isAddingItem && (
                  <TableRow>
                    <TableCell>
                      <Select
                        value={newItem.productId}
                        onValueChange={(value) => {
                          const orderItem = order.orderItems?.find(
                            (item) => item.productId === value
                          );
                          setNewItem((prev) => ({
                            ...prev,
                            productId: value,
                            orderItemId: orderItem?.id || '',
                          }));
                        }}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder='Chọn sản phẩm' />
                        </SelectTrigger>
                        <SelectContent>
                          {availableProducts.map((product) => {
                            const productInfo = products.find(
                              (p) => p.id === product.productId
                            );
                            const remaining = getRemainingQuantityForNewItem(
                              product.productId
                            );
                            return (
                              <SelectItem
                                key={product.id}
                                value={product.productId}
                              >
                                {productInfo?.name || product.productId} (Còn:{' '}
                                {remaining})
                              </SelectItem>
                            );
                          })}
                        </SelectContent>
                      </Select>
                    </TableCell>
                    <TableCell>
                      <div className='space-y-1'>
                        <Input
                          type='number'
                          min={1}
                          max={
                            newItem.productId
                              ? getRemainingQuantityForNewItem(
                                  newItem.productId
                                )
                              : undefined
                          }
                          value={newItem.quantity}
                          onChange={(e) =>
                            setNewItem((prev) => ({
                              ...prev,
                              quantity: Number(e.target.value),
                            }))
                          }
                          className='w-20'
                        />
                        {newItem.productId && (
                          <p className='text-xs text-muted-foreground'>
                            Tối đa:{' '}
                            {getRemainingQuantityForNewItem(newItem.productId)}
                          </p>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      {newItem.productId
                        ? getProductUnit(newItem.productId)
                        : '-'}
                    </TableCell>
                    <TableCell>
                      <Input
                        value={newItem.lotId}
                        onChange={(e) =>
                          setNewItem((prev) => ({
                            ...prev,
                            lotId: e.target.value,
                          }))
                        }
                        placeholder='Lô hàng'
                      />
                    </TableCell>
                    <TableCell>
                      <Input
                        type='date'
                        value={newItem.manufacturingDate}
                        onChange={(e) =>
                          setNewItem((prev) => ({
                            ...prev,
                            manufacturingDate: e.target.value,
                          }))
                        }
                      />
                    </TableCell>
                    <TableCell>
                      <Input
                        type='date'
                        value={newItem.expirationDate}
                        onChange={(e) =>
                          setNewItem((prev) => ({
                            ...prev,
                            expirationDate: e.target.value,
                          }))
                        }
                      />
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex justify-end gap-2'>
                        <Button
                          size='sm'
                          variant='ghost'
                          onClick={handleAddNewItem}
                          disabled={!newItem.productId}
                        >
                          <Save className='h-4 w-4' />
                        </Button>
                        <Button
                          size='sm'
                          variant='ghost'
                          onClick={() => {
                            setIsAddingItem(false);
                            setNewItem({
                              productId: '',
                              quantity: 1,
                              facilityId: shipment?.facilityId || '',
                              orderItemId: '',
                              lotId: '',
                              note: '',
                              manufacturingDate: '',
                              expirationDate: '',
                            });
                          }}
                        >
                          <X className='h-4 w-4' />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Đóng
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
