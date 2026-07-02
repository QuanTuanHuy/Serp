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
  CreditCard,
  Loader2,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Badge } from '@/shared/components/ui/badge';
import { Switch } from '@/shared/components/ui/switch';
import { Label } from '@/shared/components/ui/label';
import { getErrorMessage } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import {
  useStartDeliveryMutation,
  useConfirmDeliveredMutation,
  useConfirmDeliveryFailedMutation,
  useConfirmDeliveryPaymentMutation,
  useConfirmReturnMutation,
  useGetDeliveryManifestDetailQuery,
  useInitiateDeliveryPaymentMutation,
} from '../../../../api/lastMileApi';
import type {
  DeliveryPaymentInitResponse,
  DeliveryManifestResponse,
  DeliveryManifestOrderResponse,
  DeliveryOrderStatus,
} from '../../../../types';

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

const DELIVERY_DEV_CHECKIN_STORAGE_KEY = 'serp.first-mile.delivery-dev-checkin';
const PAYMENT_RESULT_MESSAGE_TYPE = 'SERP_PAYMENT_RESULT';
const RECEIVER_FEE_PAYER = 'RECEIVER';
const currencyFormatter = new Intl.NumberFormat('en-US');

const formatCurrency = (value?: number | null): string => {
  if (value === undefined || value === null || !Number.isFinite(value)) {
    return '--';
  }
  return `${currencyFormatter.format(value)} VND`;
};

const getRequiredCodAmount = (
  order: DeliveryManifestOrderResponse | null
): number => Math.max(0, order?.codAmount ?? 0);

const getRequiredReceiverShippingFee = (
  order: DeliveryManifestOrderResponse | null
): number => {
  if (!order || order.feePayer !== RECEIVER_FEE_PAYER) {
    return 0;
  }
  return Math.max(0, order.shippingFee ?? 0);
};

const getRequiredCustomerPayment = (
  order: DeliveryManifestOrderResponse | null
): number =>
  getRequiredCodAmount(order) + getRequiredReceiverShippingFee(order);

const isCustomerPaymentConfirmed = (
  order: DeliveryManifestOrderResponse | null
): boolean =>
  getRequiredCustomerPayment(order) <= 0 ||
  order?.deliveryPaymentStatus === 'PAID';

const isDeliveryDevCheckinAvailable = (): boolean =>
  process.env.NODE_ENV === 'development';

const readDeliveryDevCheckinPreference = (): boolean => {
  if (!isDeliveryDevCheckinAvailable() || typeof window === 'undefined') {
    return false;
  }

  return sessionStorage.getItem(DELIVERY_DEV_CHECKIN_STORAGE_KEY) === 'true';
};

const writeDeliveryDevCheckinPreference = (enabled: boolean): void => {
  if (!isDeliveryDevCheckinAvailable() || typeof window === 'undefined') {
    return;
  }

  sessionStorage.setItem(
    DELIVERY_DEV_CHECKIN_STORAGE_KEY,
    enabled ? 'true' : 'false'
  );
};

interface Props {
  manifest: DeliveryManifestResponse;
  canOperate: boolean;
  onClose: () => void;
  onUpdated: () => void;
}

export const DeliveryManifestDetailDialog: React.FC<Props> = ({
  manifest: initialManifest,
  canOperate,
  onClose,
  onUpdated,
}) => {
  const notification = useNotification();
  const { data: manifest, refetch: refetchManifest } =
    useGetDeliveryManifestDetailQuery(
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
  const [confirmDelivered, { isLoading: isConfirmingDelivered }] =
    useConfirmDeliveredMutation();
  const [confirmFailed, { isLoading: isConfirmingFailed }] =
    useConfirmDeliveryFailedMutation();
  const [initiateDeliveryPayment, { isLoading: isInitiatingDeliveryPayment }] =
    useInitiateDeliveryPaymentMutation();
  const [confirmDeliveryPayment, { isLoading: isConfirmingDeliveryPayment }] =
    useConfirmDeliveryPaymentMutation();
  const [confirmReturn, { isLoading: isReturning }] =
    useConfirmReturnMutation();

  // Form state for delivery confirmation
  const [proofPhoto, setProofPhoto] = useState<File | null>(null);
  const [deliveryLatitude, setDeliveryLatitude] = useState('');
  const [deliveryLongitude, setDeliveryLongitude] = useState('');
  const [isResolvingLocation, setIsResolvingLocation] = useState(false);
  const isDevCheckinFeatureAvailable = isDeliveryDevCheckinAvailable();
  const [devCheckinMode, setDevCheckinMode] = useState(false);
  const [deliveryNote, setDeliveryNote] = useState('');
  const [failureReason, setFailureReason] = useState('');
  const [paymentInitResult, setPaymentInitResult] =
    useState<DeliveryPaymentInitResponse | null>(null);
  const [isAwaitingPaymentCompletion, setIsAwaitingPaymentCompletion] =
    useState(false);
  const paymentCompletionHandledRef = React.useRef(false);
  const paymentResultHandlingInProgressRef = React.useRef(false);
  const lastHandledPaymentMessageKeyRef = React.useRef<string | null>(null);

  React.useEffect(() => {
    if (isDevCheckinFeatureAvailable) {
      setDevCheckinMode(readDeliveryDevCheckinPreference());
    }
  }, [isDevCheckinFeatureAvailable]);

  const handleStartDelivery = async () => {
    try {
      await startDelivery({ id: current.id }).unwrap();
      onUpdated();
    } catch (error) {
      notification.error('Failed to start delivery.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleConfirmDelivered = async () => {
    if (!activeOrder) return;

    if (!proofPhoto) {
      notification.error('Please select a proof photo before check-in.');
      return;
    }

    const latitude = Number(deliveryLatitude);
    const longitude = Number(deliveryLongitude);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      notification.error('Please provide a valid delivery check-in location.');
      return;
    }

    if (!isCustomerPaymentConfirmed(activeOrder)) {
      notification.error(
        'Customer payment must be confirmed before delivery check-in.'
      );
      return;
    }

    try {
      await confirmDelivered({
        manifestId: current.id,
        orderCode: activeOrder.orderCode,
        body: {
          codCollected: getRequiredCodAmount(activeOrder),
          shippingFeeCollected: getRequiredReceiverShippingFee(activeOrder),
          latitude,
          longitude,
          photo: proofPhoto,
          note: deliveryNote || undefined,
        },
      }).unwrap();
      resetForm();
      onUpdated();
    } catch (error) {
      notification.error('Delivery check-in failed.', {
        description: getErrorMessage(error),
      });
    }
  };

  const markActiveOrderPaymentConfirmed = (
    appTransId: string,
    amount: number
  ) => {
    setActiveOrder((prev) =>
      prev
        ? {
            ...prev,
            deliveryPaymentStatus: 'PAID',
            deliveryPaymentAmount: amount,
            deliveryPaymentAppTransId: appTransId,
            deliveryPaymentConfirmedAt: new Date().toISOString(),
          }
        : prev
    );
  };

  const handleConfirmPayment = React.useCallback(
    async (appTransId?: string) => {
      if (!activeOrder) {
        return;
      }

      const resolvedAppTransId =
        appTransId?.trim() ||
        paymentInitResult?.appTransId?.trim() ||
        activeOrder.deliveryPaymentAppTransId?.trim();

      if (!resolvedAppTransId) {
        notification.error('Payment transaction is missing.');
        return;
      }

      const messageKey = `${current.id}:${activeOrder.orderCode}:${resolvedAppTransId}`;
      if (
        lastHandledPaymentMessageKeyRef.current === messageKey ||
        paymentResultHandlingInProgressRef.current
      ) {
        return;
      }

      lastHandledPaymentMessageKeyRef.current = messageKey;
      paymentResultHandlingInProgressRef.current = true;
      setIsAwaitingPaymentCompletion(true);

      try {
        const paymentResult = await confirmDeliveryPayment({
          manifestId: current.id,
          orderCode: activeOrder.orderCode,
          body: { appTransId: resolvedAppTransId },
        }).unwrap();

        paymentCompletionHandledRef.current = true;
        markActiveOrderPaymentConfirmed(
          paymentResult.appTransId,
          paymentResult.amount
        );
        setPaymentInitResult((prev) =>
          prev
            ? {
                ...prev,
                paymentStatus: paymentResult.paymentStatus,
                status: paymentResult.status,
                message: paymentResult.message,
              }
            : {
                manifestId: paymentResult.manifestId,
                orderCode: paymentResult.orderCode,
                amount: paymentResult.amount,
                paymentStatus: paymentResult.paymentStatus,
                appTransId: paymentResult.appTransId,
                status: paymentResult.status,
                message: paymentResult.message,
              }
        );
        await refetchManifest();

        notification.success('Customer payment confirmed successfully.', {
          description: 'You can now complete delivery check-in.',
        });
      } catch (error) {
        lastHandledPaymentMessageKeyRef.current = null;
        notification.error('Failed to confirm customer payment.', {
          description: getErrorMessage(error),
        });
      } finally {
        setIsAwaitingPaymentCompletion(false);
        paymentResultHandlingInProgressRef.current = false;
      }
    },
    [
      activeOrder,
      confirmDeliveryPayment,
      current.id,
      notification,
      paymentInitResult?.appTransId,
      refetchManifest,
    ]
  );

  const handleInitiatePayment = async () => {
    if (!activeOrder) {
      return;
    }

    if (getRequiredCustomerPayment(activeOrder) <= 0) {
      notification.error('This delivery does not require customer payment.');
      return;
    }

    try {
      const paymentResult = await initiateDeliveryPayment({
        manifestId: current.id,
        orderCode: activeOrder.orderCode,
      }).unwrap();

      setPaymentInitResult(paymentResult);
      setActiveOrder((prev) =>
        prev
          ? {
              ...prev,
              deliveryPaymentStatus: paymentResult.paymentStatus,
              deliveryPaymentAmount: paymentResult.amount,
              deliveryPaymentAppTransId:
                paymentResult.appTransId ?? prev.deliveryPaymentAppTransId,
            }
          : prev
      );
      setIsAwaitingPaymentCompletion(
        paymentResult.paymentStatus !== 'PAID' &&
          Boolean(paymentResult.appTransId)
      );
      paymentCompletionHandledRef.current =
        paymentResult.paymentStatus === 'PAID';
      paymentResultHandlingInProgressRef.current = false;
      lastHandledPaymentMessageKeyRef.current = null;

      if (paymentResult.paymentStatus === 'PAID') {
        notification.success('Customer payment is already confirmed.');
        return;
      }

      if (paymentResult.paymentUrl) {
        const paymentPopup = window.open(
          paymentResult.paymentUrl,
          'serp-delivery-payment-window',
          'width=520,height=760,scrollbars=yes,resizable=yes'
        );

        if (!paymentPopup) {
          setIsAwaitingPaymentCompletion(false);
          notification.error('Popup was blocked by the browser.', {
            description:
              'Allow popups for this site or use Verify payment after completing payment.',
          });
          return;
        }
      }

      notification.success('Payment request created.', {
        description:
          'Complete payment in the opened window. This panel will verify payment automatically when it succeeds.',
      });
    } catch (error) {
      setIsAwaitingPaymentCompletion(false);
      notification.error('Failed to initiate customer payment.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handlePaymentResultMessage = React.useCallback(
    async (payload?: {
      manifestId?: number;
      orderCode?: string;
      appTransId?: string;
      query?: Record<string, string>;
    }) => {
      if (
        paymentCompletionHandledRef.current ||
        !activeOrder ||
        getRequiredCustomerPayment(activeOrder) <= 0
      ) {
        return;
      }

      const messageManifestId = payload?.manifestId;
      const messageOrderCode = payload?.orderCode?.trim();
      if (messageManifestId && messageManifestId !== current.id) {
        return;
      }
      if (
        messageOrderCode &&
        messageOrderCode.toUpperCase() !== activeOrder.orderCode.toUpperCase()
      ) {
        return;
      }

      const messageAppTransId = (
        payload?.appTransId ??
        payload?.query?.appTransId ??
        payload?.query?.apptransid ??
        payload?.query?.app_trans_id
      )?.trim();

      const expectedAppTransId =
        paymentInitResult?.appTransId?.trim() ||
        activeOrder.deliveryPaymentAppTransId?.trim();
      if (!expectedAppTransId) {
        return;
      }
      if (messageAppTransId && messageAppTransId !== expectedAppTransId) {
        return;
      }

      await handleConfirmPayment(expectedAppTransId);
    },
    [
      activeOrder,
      current.id,
      handleConfirmPayment,
      paymentInitResult?.appTransId,
    ]
  );

  React.useEffect(() => {
    const onPaymentResultMessage = (event: MessageEvent<unknown>) => {
      if (event.origin !== window.location.origin) {
        return;
      }

      if (
        !event.data ||
        typeof event.data !== 'object' ||
        !('type' in event.data) ||
        (event.data as { type?: string }).type !== PAYMENT_RESULT_MESSAGE_TYPE
      ) {
        return;
      }

      const payload = (
        event.data as {
          payload?: {
            manifestId?: number | string;
            orderCode?: string;
            appTransId?: string;
            query?: Record<string, string>;
          };
        }
      ).payload;

      const rawManifestId = payload?.manifestId ?? payload?.query?.manifestId;
      const parsedManifestId = Number.parseInt(String(rawManifestId ?? ''), 10);

      void handlePaymentResultMessage({
        manifestId:
          Number.isInteger(parsedManifestId) && parsedManifestId > 0
            ? parsedManifestId
            : undefined,
        orderCode: payload?.orderCode ?? payload?.query?.orderCode,
        appTransId: payload?.appTransId,
        query: payload?.query,
      });
    };

    window.addEventListener('message', onPaymentResultMessage);
    return () => {
      window.removeEventListener('message', onPaymentResultMessage);
    };
  }, [handlePaymentResultMessage]);

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
    } catch (error) {
      notification.error('Failed to confirm delivery failure.', {
        description: getErrorMessage(error),
      });
    }
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
    } catch (error) {
      notification.error('Failed to return order to sender.', {
        description: getErrorMessage(error),
      });
    }
  };

  const resetForm = () => {
    setActiveOrder(null);
    setActionMode(null);
    setProofPhoto(null);
    setDeliveryLatitude('');
    setDeliveryLongitude('');
    setDeliveryNote('');
    setFailureReason('');
    setPaymentInitResult(null);
    setIsAwaitingPaymentCompletion(false);
    paymentCompletionHandledRef.current = false;
    paymentResultHandlingInProgressRef.current = false;
    lastHandledPaymentMessageKeyRef.current = null;
  };

  const fillReceiverLocation = (
    order: DeliveryManifestOrderResponse | null
  ): boolean => {
    if (
      order?.receiverLat === undefined ||
      order.receiverLat === null ||
      order.receiverLng === undefined ||
      order.receiverLng === null
    ) {
      notification.error('Receiver coordinates are missing for this order.');
      return false;
    }

    setDeliveryLatitude(order.receiverLat.toFixed(6));
    setDeliveryLongitude(order.receiverLng.toFixed(6));
    return true;
  };

  const handleDevCheckinModeChange = (enabled: boolean) => {
    setDevCheckinMode(enabled);
    writeDeliveryDevCheckinPreference(enabled);
    if (enabled) fillReceiverLocation(activeOrder);
  };

  const handleResolveCurrentLocation = async () => {
    if (devCheckinMode) {
      fillReceiverLocation(activeOrder);
      return;
    }

    if (typeof window === 'undefined' || !('geolocation' in navigator)) {
      notification.error('Geolocation is not supported in your browser.');
      return;
    }

    setIsResolvingLocation(true);
    try {
      const position = await new Promise<GeolocationPosition>(
        (resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 15000,
          });
        }
      );
      setDeliveryLatitude(position.coords.latitude.toFixed(6));
      setDeliveryLongitude(position.coords.longitude.toFixed(6));
    } catch (error) {
      notification.error('Failed to resolve current location.', {
        description: getErrorMessage(error),
      });
    } finally {
      setIsResolvingLocation(false);
    }
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
            value={`${current.collectedCodAmount.toLocaleString()} VND`}
            icon={Banknote}
            color='text-amber-600'
          />
        </div>

        {/* Start delivery action */}
        {canOperate && current.status === 'CREATED' && (
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
                canOperate &&
                current.status === 'IN_PROGRESS' &&
                order.status === 'OUT_FOR_DELIVERY'
              }
              canReturnToSender={canOperate && order.status === 'FAILED'}
              onDeliver={() => {
                setActiveOrder(order);
                setActionMode('deliver');
                setProofPhoto(null);
                setDeliveryLatitude('');
                setDeliveryLongitude('');
                setDeliveryNote('');
                setPaymentInitResult(
                  order.deliveryPaymentAppTransId
                    ? {
                        manifestId: current.id,
                        orderCode: order.orderCode,
                        amount:
                          order.deliveryPaymentAmount ??
                          getRequiredCustomerPayment(order),
                        paymentStatus: order.deliveryPaymentStatus ?? 'UNPAID',
                        appTransId: order.deliveryPaymentAppTransId,
                        status: order.deliveryPaymentStatus,
                      }
                    : null
                );
                setIsAwaitingPaymentCompletion(false);
                paymentCompletionHandledRef.current =
                  order.deliveryPaymentStatus === 'PAID';
                paymentResultHandlingInProgressRef.current = false;
                lastHandledPaymentMessageKeyRef.current = null;
                if (devCheckinMode) fillReceiverLocation(order);
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
                {actionMode === 'deliver' && 'Confirm Delivery'}
                {actionMode === 'fail' && 'Report Delivery Failure'}
                {actionMode === 'return' && 'Return to Sender'}
                <span className='text-muted-foreground ml-2'>
                  - {activeOrder.orderCode}
                </span>
              </h4>
              <Button variant='ghost' size='sm' onClick={resetForm}>
                <X className='h-4 w-4' />
              </Button>
            </div>

            {actionMode === 'deliver' && (
              <div className='space-y-4'>
                {getRequiredCustomerPayment(activeOrder) > 0 ? (
                  <div className='rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800 dark:bg-amber-950/30 dark:text-amber-200'>
                    Customer payment must be completed through payment service
                    before delivery check-in.
                  </div>
                ) : null}

                <div className='rounded-md border p-3 text-sm'>
                  <div className='flex flex-wrap items-center justify-between gap-2'>
                    <div>
                      <p className='font-semibold'>Customer payment</p>
                      <p className='text-xs text-muted-foreground'>
                        COD and receiver-paid shipping fee are verified by
                        payment service.
                      </p>
                    </div>
                    <Badge
                      variant={
                        isCustomerPaymentConfirmed(activeOrder)
                          ? 'default'
                          : 'secondary'
                      }
                    >
                      {isCustomerPaymentConfirmed(activeOrder)
                        ? 'Payment confirmed'
                        : 'Payment required'}
                    </Badge>
                  </div>

                  <div className='mt-3 grid gap-2 sm:grid-cols-3'>
                    <div>
                      <p className='text-xs text-muted-foreground'>COD</p>
                      <p className='font-medium'>
                        {formatCurrency(getRequiredCodAmount(activeOrder))}
                      </p>
                    </div>
                    <div>
                      <p className='text-xs text-muted-foreground'>
                        Receiver shipping fee
                      </p>
                      <p className='font-medium'>
                        {formatCurrency(
                          getRequiredReceiverShippingFee(activeOrder)
                        )}
                      </p>
                    </div>
                    <div>
                      <p className='text-xs text-muted-foreground'>Total</p>
                      <p className='font-semibold'>
                        {formatCurrency(
                          getRequiredCustomerPayment(activeOrder)
                        )}
                      </p>
                    </div>
                  </div>

                  {paymentInitResult ||
                  activeOrder.deliveryPaymentAppTransId ? (
                    <div className='mt-3 space-y-1 rounded-md bg-muted/50 p-2 text-xs text-muted-foreground'>
                      <p>
                        Transaction:{' '}
                        {paymentInitResult?.appTransId ||
                          activeOrder.deliveryPaymentAppTransId}
                      </p>
                      <p>
                        Status:{' '}
                        {paymentInitResult?.status ||
                          activeOrder.deliveryPaymentStatus ||
                          '--'}
                      </p>
                      {paymentInitResult?.message ? (
                        <p>Gateway message: {paymentInitResult.message}</p>
                      ) : null}
                    </div>
                  ) : null}

                  {isAwaitingPaymentCompletion ||
                  isConfirmingDeliveryPayment ? (
                    <div className='mt-3 flex items-center gap-2 text-xs text-muted-foreground'>
                      <Loader2 className='h-4 w-4 animate-spin' />
                      Waiting for payment confirmation...
                    </div>
                  ) : null}

                  {getRequiredCustomerPayment(activeOrder) > 0 &&
                  !isCustomerPaymentConfirmed(activeOrder) ? (
                    <div className='mt-3 flex flex-wrap gap-2'>
                      <Button
                        type='button'
                        variant='outline'
                        size='sm'
                        onClick={() => void handleInitiatePayment()}
                        disabled={
                          isInitiatingDeliveryPayment ||
                          isConfirmingDeliveryPayment ||
                          isAwaitingPaymentCompletion
                        }
                      >
                        {isInitiatingDeliveryPayment ? (
                          <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                        ) : (
                          <CreditCard className='mr-2 h-4 w-4' />
                        )}
                        Pay customer amount
                      </Button>
                      <Button
                        type='button'
                        variant='outline'
                        size='sm'
                        onClick={() => void handleConfirmPayment()}
                        disabled={
                          isInitiatingDeliveryPayment ||
                          isConfirmingDeliveryPayment ||
                          !(
                            paymentInitResult?.appTransId ||
                            activeOrder.deliveryPaymentAppTransId
                          )
                        }
                      >
                        {isConfirmingDeliveryPayment ? (
                          <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                        ) : null}
                        Verify payment
                      </Button>
                    </div>
                  ) : null}
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='delivery-proof-photo'>Proof Photo</Label>
                  <Input
                    id='delivery-proof-photo'
                    type='file'
                    accept='image/*'
                    onChange={(event) => {
                      const file = event.target.files?.[0] || null;
                      setProofPhoto(file);
                    }}
                  />
                </div>

                {isDevCheckinFeatureAvailable ? (
                  <div className='flex items-center justify-between gap-4 rounded-md border border-dashed border-amber-500/50 bg-amber-500/5 px-3 py-2'>
                    <div className='space-y-0.5'>
                      <Label htmlFor='delivery-dev-checkin-mode'>
                        Development mode
                      </Label>
                      <p className='text-xs text-muted-foreground'>
                        Fill check-in coordinates at receiver location instead
                        of current GPS.
                      </p>
                    </div>
                    <Switch
                      id='delivery-dev-checkin-mode'
                      checked={devCheckinMode}
                      onCheckedChange={handleDevCheckinModeChange}
                    />
                  </div>
                ) : null}

                <div className='grid gap-3 sm:grid-cols-2'>
                  <div className='space-y-1'>
                    <Label htmlFor='delivery-latitude'>Latitude</Label>
                    <Input
                      id='delivery-latitude'
                      value={deliveryLatitude}
                      readOnly
                      placeholder={
                        devCheckinMode
                          ? 'Auto-filled from receiver location'
                          : 'Resolve current location'
                      }
                    />
                  </div>
                  <div className='space-y-1'>
                    <Label htmlFor='delivery-longitude'>Longitude</Label>
                    <Input
                      id='delivery-longitude'
                      value={deliveryLongitude}
                      readOnly
                      placeholder={
                        devCheckinMode
                          ? 'Auto-filled from receiver location'
                          : 'Resolve current location'
                      }
                    />
                  </div>
                </div>

                <div className='space-y-1'>
                  <Label className='text-xs'>Note</Label>
                  <Input
                    value={deliveryNote}
                    onChange={(e) => setDeliveryNote(e.target.value)}
                    placeholder='Optional note'
                  />
                </div>

                <div className='flex flex-wrap justify-end gap-2'>
                  <Button
                    type='button'
                    variant='outline'
                    onClick={() => void handleResolveCurrentLocation()}
                    disabled={isResolvingLocation}
                  >
                    {isResolvingLocation
                      ? 'Resolving location...'
                      : devCheckinMode
                        ? 'Fill receiver location'
                        : 'Use current location'}
                  </Button>
                  <Button
                    onClick={handleConfirmDelivered}
                    disabled={
                      isConfirmingDelivered ||
                      isInitiatingDeliveryPayment ||
                      isConfirmingDeliveryPayment ||
                      isAwaitingPaymentCompletion ||
                      !isCustomerPaymentConfirmed(activeOrder)
                    }
                    className='bg-green-600 hover:bg-green-700'
                  >
                    <CheckCircle2 className='h-4 w-4 mr-2' />
                    {isConfirmingDelivered
                      ? 'Confirming...'
                      : 'Confirm Delivered'}
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
                  <Button
                    onClick={handleConfirmFailed}
                    variant='destructive'
                    disabled={isConfirmingFailed}
                  >
                    <XCircle className='h-4 w-4 mr-2' />
                    {isConfirmingFailed ? 'Confirming...' : 'Confirm Failed'}
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
                    disabled={isReturning}
                    className='border-purple-300 text-purple-700 hover:bg-purple-50'
                  >
                    <RotateCcw className='h-4 w-4 mr-2' />
                    {isReturning ? 'Returning...' : 'Return to Sender'}
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
              COD: {order.codAmount.toLocaleString()} VND
            </span>
          )}
          {order.shippingFee > 0 && (
            <span className='text-xs text-muted-foreground'>
              Fee: {order.shippingFee.toLocaleString()} VND
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
        <div className='mt-2 pt-2 border-t text-xs text-green-600 flex flex-wrap items-center gap-2'>
          <span className='flex items-center gap-1'>
            <Clock className='h-3 w-3' />
            Delivered at {new Date(order.deliveredAt).toLocaleString()}
          </span>
          {order.deliveryCheckinLat !== undefined &&
          order.deliveryCheckinLng !== undefined ? (
            <span className='flex items-center gap-1 text-muted-foreground'>
              <MapPin className='h-3 w-3' />
              {order.deliveryCheckinLat.toFixed(6)},{' '}
              {order.deliveryCheckinLng.toFixed(6)}
            </span>
          ) : null}
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
          {order.note && ` - ${order.note}`}
        </div>
      )}
    </div>
  );
};
