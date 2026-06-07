/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Delivery Manifest Detail Dialog
 */

'use client';

import React, { useState } from 'react';
import {
  X,
  Truck,
  CheckCircle2,
  XCircle,
  RotateCcw,
  Play,
  Package,
  MapPin,
  Phone,
  Banknote,
  Calendar,
  Clock,
  Camera,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Badge } from '@/shared/components/ui/badge';
import {
  useStartDeliveryMutation,
  useConfirmDeliveredMutation,
  useConfirmDeliveryFailedMutation,
  useConfirmReturnMutation,
  useGetDeliveryManifestDetailQuery,
} from '../../../api/lastMileApi';
import type {
  DeliveryManifestResponse,
  DeliveryManifestOrderResponse,
  DeliveryOrderStatus,
} from '../../../types';

const ORDER_STATUS_CONFIG: Record<
  DeliveryOrderStatus,
  { label: string; color: string }
> = {
  PENDING: { label: 'Pending', color: 'bg-slate-100 text-slate-700' },
  OUT_FOR_DELIVERY: {
    label: 'Out for Delivery',
    color: 'bg-blue-100 text-blue-700',
  },
  DELIVERED: { label: 'Delivered', color: 'bg-green-100 text-green-700' },
  FAILED: { label: 'Failed', color: 'bg-red-100 text-red-700' },
  RESCHEDULED: { label: 'Rescheduled', color: 'bg-amber-100 text-amber-700' },
  RETURNED: { label: 'Returned', color: 'bg-purple-100 text-purple-700' },
};

interface Props {
  manifest: DeliveryManifestResponse;
  onClose: () => void;
  onUpdated: () => void;
}

export const DeliveryManifestDetailDialog: React.FC<Props> = ({
  manifest: initialManifest,
  onClose,
  onUpdated,
}) => {
  const { data: manifest } = useGetDeliveryManifestDetailQuery(
    { id: initialManifest.id },
    { refetchOnMountOrArgChange: true }
  );

  const current = manifest ?? initialManifest;
  const [activeOrder, setActiveOrder] =
    useState<DeliveryManifestOrderResponse | null>(null);
  const [actionMode, setActionMode] = useState<
    'deliver' | 'fail' | 'return' | null
  >(null);

  const [startDelivery, { isLoading: isStarting }] = useStartDeliveryMutation();
  const [confirmDelivered] = useConfirmDeliveredMutation();
  const [confirmFailed] = useConfirmDeliveryFailedMutation();
  const [confirmReturn] = useConfirmReturnMutation();

  // Form state for delivery confirmation
  const [codCollected, setCodCollected] = useState('');
  const [shippingFeeCollected, setShippingFeeCollected] = useState('');
  const [proofPhotoUrl, setProofPhotoUrl] = useState('');
  const [deliveryNote, setDeliveryNote] = useState('');
  const [failureReason, setFailureReason] = useState('');

  const handleStartDelivery = async () => {
    try {
      await startDelivery({ id: current.id }).unwrap();
      onUpdated();
    } catch {}
  };

  const handleConfirmDelivered = async () => {
    if (!activeOrder) return;
    try {
      await confirmDelivered({
        manifestId: current.id,
        orderCode: activeOrder.orderCode,
        body: {
          codCollected: codCollected ? Number(codCollected) : 0,
          shippingFeeCollected: shippingFeeCollected
            ? Number(shippingFeeCollected)
            : 0,
          proofPhotoUrl: proofPhotoUrl || undefined,
          note: deliveryNote || undefined,
        },
      }).unwrap();
      resetForm();
      onUpdated();
    } catch {}
  };

  const handleConfirmFailed = async () => {
    if (!activeOrder) return;
    try {
      await confirmFailed({
        manifestId: current.id,
        orderCode: activeOrder.orderCode,
        body: {
          failureReason: failureReason || 'UNKNOWN',
          note: deliveryNote || undefined,
        },
      }).unwrap();
      resetForm();
      onUpdated();
    } catch {}
  };

  const handleReturnToSender = async () => {
    if (!activeOrder) return;
    try {
      await confirmReturn({
        manifestId: current.id,
        orderCode: activeOrder.orderCode,
        body: { note: deliveryNote || undefined },
      }).unwrap();
      resetForm();
      onUpdated();
    } catch {}
  };

  const resetForm = () => {
    setActiveOrder(null);
    setActionMode(null);
    setCodCollected('');
    setShippingFeeCollected('');
    setProofPhotoUrl('');
    setDeliveryNote('');
    setFailureReason('');
  };

  const sortedOrders = [...(current.orders ?? [])].sort(
    (a, b) => a.sequence - b.sequence
  );

  return (
    <div className='fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4'>
      <Card className='w-full max-w-4xl max-h-[90vh] overflow-y-auto p-6'>
        {/* Header */}
        <div className='flex items-center justify-between mb-4'>
          <div>
            <div className='flex items-center gap-3'>
              <Truck className='h-5 w-5 text-primary' />
              <h2 className='text-lg font-semibold'>{current.manifestCode}</h2>
              <Badge className='text-xs'>
                {current.status.replace(/_/g, ' ')}
              </Badge>
            </div>
            <div className='flex gap-4 mt-1 text-sm text-muted-foreground'>
              <span className='flex items-center gap-1'>
                <Calendar className='h-3.5 w-3.5' />
                {current.plannedDate}
              </span>
              {current.courierName && (
                <span>Courier: {current.courierName}</span>
              )}
              {current.vehicleId && <span>Vehicle: {current.vehicleId}</span>}
            </div>
          </div>
          <Button variant='ghost' size='sm' onClick={onClose}>
            <X className='h-4 w-4' />
          </Button>
        </div>

        {/* Summary KPIs */}
        <div className='grid grid-cols-4 gap-3 mb-4'>
          <KpiCard
            label='Total Orders'
            value={current.totalOrders}
            icon={Package}
          />
          <KpiCard
            label='Delivered'
            value={current.deliveredCount}
            icon={CheckCircle2}
            color='text-green-600'
          />
          <KpiCard
            label='Failed'
            value={current.failedCount}
            icon={XCircle}
            color='text-red-600'
          />
          <KpiCard
            label='COD Collected'
            value={`${current.collectedCodAmount.toLocaleString()}đ`}
            icon={Banknote}
            color='text-amber-600'
          />
        </div>

        {/* Start delivery action */}
        {current.status === 'CREATED' && (
          <div className='bg-amber-50 dark:bg-amber-950/30 border border-amber-200 rounded-lg p-3 mb-4 flex items-center justify-between'>
            <div className='text-sm text-amber-700 dark:text-amber-300'>
              <p className='font-medium'>Ready to Start Delivery</p>
              <p className='text-xs mt-0.5'>
                Click start to mark the courier as departed.
              </p>
            </div>
            <Button
              size='sm'
              onClick={handleStartDelivery}
              disabled={isStarting}
              className='bg-amber-600 hover:bg-amber-700'
            >
              <Play className='h-4 w-4 mr-1' />
              Start Delivery
            </Button>
          </div>
        )}

        {/* Orders List */}
        <div className='space-y-2'>
          <h3 className='font-medium text-sm text-muted-foreground'>
            Delivery Route ({sortedOrders.length} stops)
          </h3>
          {sortedOrders.map((order) => (
            <DeliveryOrderCard
              key={order.id}
              order={order}
              isActive={activeOrder?.id === order.id}
              canUpdateDeliveryResult={
                current.status === 'IN_PROGRESS' &&
                order.status === 'OUT_FOR_DELIVERY'
              }
              canReturnToSender={order.status === 'FAILED'}
              onDeliver={() => {
                setActiveOrder(order);
                setActionMode('deliver');
                setCodCollected(String(order.codAmount ?? 0));
                setShippingFeeCollected(String(order.shippingFee ?? 0));
              }}
              onFail={() => {
                setActiveOrder(order);
                setActionMode('fail');
              }}
              onReturn={() => {
                setActiveOrder(order);
                setActionMode('return');
              }}
            />
          ))}
        </div>

        {/* Action Panel */}
        {actionMode && activeOrder && (
          <div className='mt-4 border rounded-lg p-4 bg-muted/30'>
            <div className='flex items-center justify-between mb-3'>
              <h4 className='font-medium text-sm'>
                {actionMode === 'deliver' && '✓ Confirm Delivery'}
                {actionMode === 'fail' && '✗ Report Delivery Failure'}
                {actionMode === 'return' && '↩ Return to Sender'}
                <span className='text-muted-foreground ml-2'>
                  — {activeOrder.orderCode}
                </span>
              </h4>
              <Button variant='ghost' size='sm' onClick={resetForm}>
                <X className='h-4 w-4' />
              </Button>
            </div>

            {actionMode === 'deliver' && (
              <div className='grid grid-cols-2 gap-3'>
                <div className='space-y-1'>
                  <label className='text-xs font-medium'>COD Collected</label>
                  <Input
                    type='number'
                    value={codCollected}
                    onChange={(e) => setCodCollected(e.target.value)}
                    placeholder='0'
                  />
                </div>
                <div className='space-y-1'>
                  <label className='text-xs font-medium'>
                    Shipping Fee Collected
                  </label>
                  <Input
                    type='number'
                    value={shippingFeeCollected}
                    onChange={(e) => setShippingFeeCollected(e.target.value)}
                    placeholder='0'
                  />
                </div>
                <div className='space-y-1'>
                  <label className='text-xs font-medium'>Proof Photo URL</label>
                  <Input
                    value={proofPhotoUrl}
                    onChange={(e) => setProofPhotoUrl(e.target.value)}
                    placeholder='https://...'
                  />
                </div>
                <div className='space-y-1'>
                  <label className='text-xs font-medium'>Note</label>
                  <Input
                    value={deliveryNote}
                    onChange={(e) => setDeliveryNote(e.target.value)}
                    placeholder='Optional note'
                  />
                </div>
                <div className='col-span-2 flex justify-end'>
                  <Button
                    onClick={handleConfirmDelivered}
                    className='bg-green-600 hover:bg-green-700'
                  >
                    <CheckCircle2 className='h-4 w-4 mr-2' />
                    Confirm Delivered
                  </Button>
                </div>
              </div>
            )}

            {actionMode === 'fail' && (
              <div className='space-y-3'>
                <div className='space-y-1'>
                  <label className='text-xs font-medium'>Failure Reason</label>
                  <select
                    className='w-full h-9 rounded-md border border-input bg-transparent px-3 py-1 text-sm'
                    value={failureReason}
                    onChange={(e) => setFailureReason(e.target.value)}
                  >
                    <option value=''>Select reason...</option>
                    <option value='RECIPIENT_NOT_HOME'>
                      Recipient not home
                    </option>
                    <option value='WRONG_ADDRESS'>Wrong address</option>
                    <option value='RECIPIENT_REFUSED'>Recipient refused</option>
                    <option value='CANNOT_CONTACT'>Cannot contact</option>
                    <option value='OTHER'>Other</option>
                  </select>
                </div>
                <div className='space-y-1'>
                  <label className='text-xs font-medium'>Note</label>
                  <Textarea
                    value={deliveryNote}
                    onChange={(e) => setDeliveryNote(e.target.value)}
                    placeholder='Additional details...'
                    rows={2}
                  />
                </div>
                <div className='flex justify-end'>
                  <Button onClick={handleConfirmFailed} variant='destructive'>
                    <XCircle className='h-4 w-4 mr-2' />
                    Confirm Failed
                  </Button>
                </div>
              </div>
            )}

            {actionMode === 'return' && (
              <div className='space-y-3'>
                <div className='space-y-1'>
                  <label className='text-xs font-medium'>Note</label>
                  <Textarea
                    value={deliveryNote}
                    onChange={(e) => setDeliveryNote(e.target.value)}
                    placeholder='Reason for return...'
                    rows={2}
                  />
                </div>
                <div className='flex justify-end'>
                  <Button
                    onClick={handleReturnToSender}
                    variant='outline'
                    className='border-purple-300 text-purple-700 hover:bg-purple-50'
                  >
                    <RotateCcw className='h-4 w-4 mr-2' />
                    Return to Sender
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </Card>
    </div>
  );
};

// ─── Sub-components ──────────────────────────────────────────────────────

interface KpiCardProps {
  label: string;
  value: string | number;
  icon: React.ElementType;
  color?: string;
}

const KpiCard: React.FC<KpiCardProps> = ({
  label,
  value,
  icon: Icon,
  color = 'text-foreground',
}) => (
  <div className='border rounded-lg p-3 text-center'>
    <Icon className={`h-4 w-4 mx-auto mb-1 ${color}`} />
    <div className={`text-lg font-semibold ${color}`}>{value}</div>
    <div className='text-xs text-muted-foreground'>{label}</div>
  </div>
);

interface DeliveryOrderCardProps {
  order: DeliveryManifestOrderResponse;
  isActive: boolean;
  canUpdateDeliveryResult: boolean;
  canReturnToSender: boolean;
  onDeliver: () => void;
  onFail: () => void;
  onReturn: () => void;
}

const DeliveryOrderCard: React.FC<DeliveryOrderCardProps> = ({
  order,
  isActive,
  canUpdateDeliveryResult,
  canReturnToSender,
  onDeliver,
  onFail,
  onReturn,
}) => {
  const statusConfig = ORDER_STATUS_CONFIG[order.status];

  return (
    <div
      className={`border rounded-lg p-3 transition-colors ${
        isActive ? 'border-primary bg-primary/5' : ''
      }`}
    >
      <div className='flex items-start justify-between'>
        <div className='flex items-start gap-3'>
          {/* Sequence number */}
          <div className='h-7 w-7 rounded-full bg-primary/10 flex items-center justify-center text-xs font-bold text-primary flex-shrink-0'>
            {order.sequence}
          </div>

          <div className='min-w-0'>
            <div className='flex items-center gap-2'>
              <span className='font-mono text-sm font-medium'>
                {order.orderCode}
              </span>
              <Badge className={`${statusConfig.color} text-xs`}>
                {statusConfig.label}
              </Badge>
              {order.deliveryAttemptCount > 1 && (
                <Badge variant='outline' className='text-xs'>
                  Attempt #{order.deliveryAttemptCount}
                </Badge>
              )}
            </div>
            <div className='flex flex-col gap-0.5 mt-1 text-xs text-muted-foreground'>
              <span className='flex items-center gap-1'>
                <MapPin className='h-3 w-3' />
                {order.receiverAddressDetail || 'No address'}
              </span>
              <span className='flex items-center gap-1'>
                <Phone className='h-3 w-3' />
                {order.receiverName} • {order.receiverPhone}
              </span>
            </div>
          </div>
        </div>

        <div className='flex flex-col items-end gap-1'>
          {order.codAmount > 0 && (
            <span className='text-xs font-medium text-amber-600'>
              COD: {order.codAmount.toLocaleString()}đ
            </span>
          )}
          {order.shippingFee > 0 && (
            <span className='text-xs text-muted-foreground'>
              Fee: {order.shippingFee.toLocaleString()}đ
            </span>
          )}
        </div>
      </div>

      {/* Action buttons */}
      {(canUpdateDeliveryResult || canReturnToSender) && (
        <div className='flex gap-2 mt-2 pt-2 border-t'>
          {canUpdateDeliveryResult && (
            <>
              <Button
                size='sm'
                variant='outline'
                className='text-green-600 border-green-200 hover:bg-green-50'
                onClick={onDeliver}
              >
                <CheckCircle2 className='h-3.5 w-3.5 mr-1' />
                Delivered
              </Button>
              <Button
                size='sm'
                variant='outline'
                className='text-red-600 border-red-200 hover:bg-red-50'
                onClick={onFail}
              >
                <XCircle className='h-3.5 w-3.5 mr-1' />
                Failed
              </Button>
            </>
          )}
          {canReturnToSender && (
            <Button
              size='sm'
              variant='outline'
              className='text-purple-600 border-purple-200 hover:bg-purple-50'
              onClick={onReturn}
            >
              <RotateCcw className='h-3.5 w-3.5 mr-1' />
              Return
            </Button>
          )}
        </div>
      )}

      {/* Delivery result info */}
      {order.status === 'DELIVERED' && order.deliveredAt && (
        <div className='mt-2 pt-2 border-t text-xs text-green-600 flex items-center gap-2'>
          <Clock className='h-3 w-3' />
          Delivered at {new Date(order.deliveredAt).toLocaleString()}
          {order.proofPhotoUrl && (
            <a
              href={order.proofPhotoUrl}
              target='_blank'
              rel='noopener noreferrer'
              className='flex items-center gap-1 text-blue-600 hover:underline'
            >
              <Camera className='h-3 w-3' />
              Proof
            </a>
          )}
        </div>
      )}
      {order.status === 'FAILED' && order.failureReason && (
        <div className='mt-2 pt-2 border-t text-xs text-red-600'>
          Reason: {order.failureReason}
          {order.note && ` — ${order.note}`}
        </div>
      )}
    </div>
  );
};
