/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Manual dispatch risk evaluation
 */

import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  PickupShift,
  PostOffice,
} from '../../../types';
import type { DispatchCourierOption } from './components/types';

const CANDIDATE_STATUSES: FirstMileOrderStatus[] = ['CREATED', 'PICKUP_FAILED'];

export interface ManualAssignRisk {
  id: string;
  message: string;
  orderCodes?: string[];
}

export interface EvaluateManualAssignRisksParams {
  postOffice?: PostOffice;
  postOfficeCode?: string;
  courierStaffId: number;
  courierOptions: DispatchCourierOption[];
  selectedOrderIds: number[];
  ordersById: Map<number, FirstMileOrderDetail>;
  planningStartTime: string;
  planningEndTime: string;
}

const formatOrderLabel = (order: FirstMileOrderDetail): string => {
  return order.customerOrderCode
    ? `${order.orderCode} (${order.customerOrderCode})`
    : order.orderCode;
};

const hasValidSenderLocation = (order: FirstMileOrderDetail): boolean => {
  const latitude = order.senderLatitude;
  const longitude = order.senderLongitude;

  if (
    latitude === undefined ||
    longitude === undefined ||
    latitude === null ||
    longitude === null
  ) {
    return false;
  }

  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    return false;
  }

  if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
    return false;
  }

  if (Math.abs(latitude) < 0.000001 && Math.abs(longitude) < 0.000001) {
    return false;
  }

  return true;
};

const isPickupWindowOutsidePlanning = (
  order: FirstMileOrderDetail,
  planningStartTime: string,
  planningEndTime: string
): boolean => {
  const planningStart = new Date(planningStartTime);
  const planningEnd = new Date(planningEndTime);

  if (
    Number.isNaN(planningStart.getTime()) ||
    Number.isNaN(planningEnd.getTime())
  ) {
    return false;
  }

  if (order.pickupTimeEnd) {
    const pickupEnd = new Date(order.pickupTimeEnd);
    if (
      !Number.isNaN(pickupEnd.getTime()) &&
      pickupEnd.getTime() < planningStart.getTime()
    ) {
      return true;
    }
  }

  if (order.pickupTimeStart) {
    const pickupStart = new Date(order.pickupTimeStart);
    if (
      !Number.isNaN(pickupStart.getTime()) &&
      pickupStart.getTime() > planningEnd.getTime()
    ) {
      return true;
    }
  }

  return false;
};

const pushRisk = (
  risks: ManualAssignRisk[],
  id: string,
  message: string,
  orderCodes?: string[]
) => {
  const existing = risks.find((risk) => risk.id === id);
  if (existing) {
    if (orderCodes?.length) {
      const merged = new Set([...(existing.orderCodes ?? []), ...orderCodes]);
      existing.orderCodes = Array.from(merged);
    }
    return;
  }

  risks.push({
    id,
    message,
    ...(orderCodes?.length ? { orderCodes } : {}),
  });
};

export const evaluateManualAssignRisks = ({
  postOffice,
  postOfficeCode,
  courierStaffId,
  courierOptions,
  selectedOrderIds,
  ordersById,
  planningStartTime,
  planningEndTime,
}: EvaluateManualAssignRisksParams): ManualAssignRisk[] => {
  const risks: ManualAssignRisk[] = [];
  const normalizedPostOfficeCode = postOfficeCode?.trim().toLowerCase();

  if (!postOffice) {
    pushRisk(
      risks,
      'post-office-missing',
      'The selected post office was not found.'
    );
  } else {
    if (postOffice.status !== 'ACTIVE') {
      pushRisk(
        risks,
        'post-office-inactive',
        `Post office ${postOffice.code} is ${postOffice.status}, not ACTIVE.`
      );
    }

    if (postOffice.latitude == null || postOffice.longitude == null) {
      pushRisk(
        risks,
        'post-office-no-geocode',
        `Post office ${postOffice.code} does not have coordinates yet. Optimized routing may fail.`
      );
    }
  }

  const courierActive = courierOptions.some(
    (courier) => courier.id === courierStaffId
  );
  if (!courierActive) {
    pushRisk(
      risks,
      'courier-inactive',
      'The selected courier is no longer active for the current post office or shift.'
    );
  }

  const missingOrders: string[] = [];
  const invalidStatusOrders: string[] = [];
  const wrongPostOfficeOrders: string[] = [];
  const missingLocationOrders: string[] = [];
  const pickupWindowOrders: string[] = [];
  const alreadyAssignedOrders: string[] = [];

  for (const orderId of selectedOrderIds) {
    const order = ordersById.get(orderId);
    if (!order) {
      missingOrders.push(`#${orderId}`);
      continue;
    }

    const label = formatOrderLabel(order);

    if (!CANDIDATE_STATUSES.includes(order.status)) {
      if (order.status === 'ASSIGNED_TO_PICKUP') {
        alreadyAssignedOrders.push(label);
      } else {
        invalidStatusOrders.push(`${label} (${order.status})`);
      }
    }

    const orderOrigin = order.originPostOfficeCode?.trim().toLowerCase();
    if (
      normalizedPostOfficeCode &&
      orderOrigin &&
      orderOrigin !== normalizedPostOfficeCode
    ) {
      wrongPostOfficeOrders.push(
        `${label} (origin post office: ${order.originPostOfficeCode})`
      );
    }

    if (!hasValidSenderLocation(order)) {
      missingLocationOrders.push(label);
    }

    if (
      isPickupWindowOutsidePlanning(order, planningStartTime, planningEndTime)
    ) {
      pickupWindowOrders.push(label);
    }
  }

  if (missingOrders.length > 0) {
    pushRisk(
      risks,
      'orders-not-loaded',
      `${missingOrders.length} order(s) could not be loaded from the current list.`,
      missingOrders
    );
  }

  if (invalidStatusOrders.length > 0) {
    pushRisk(
      risks,
      'orders-invalid-status',
      'Some orders are not in CREATED or PICKUP_FAILED status.',
      invalidStatusOrders
    );
  }

  if (alreadyAssignedOrders.length > 0) {
    pushRisk(
      risks,
      'orders-already-assigned',
      'Some orders are already ASSIGNED_TO_PICKUP and may belong to another trip.',
      alreadyAssignedOrders
    );
  }

  if (wrongPostOfficeOrders.length > 0) {
    pushRisk(
      risks,
      'orders-wrong-post-office',
      'Some orders do not belong to the selected origin post office.',
      wrongPostOfficeOrders
    );
  }

  if (missingLocationOrders.length > 0) {
    pushRisk(
      risks,
      'orders-missing-location',
      'Some orders do not have valid sender coordinates.',
      missingLocationOrders
    );
  }

  if (pickupWindowOrders.length > 0) {
    pushRisk(
      risks,
      'orders-pickup-window',
      'Some order pickup windows do not match the selected shift or date.',
      pickupWindowOrders
    );
  }

  return risks;
};

export const formatManualAssignRiskLines = (
  risks: ManualAssignRisk[]
): string[] => {
  return risks.flatMap((risk) => {
    if (!risk.orderCodes?.length) {
      return [risk.message];
    }

    const preview = risk.orderCodes.slice(0, 5).join(', ');
    const suffix =
      risk.orderCodes.length > 5
        ? ` ... (+${risk.orderCodes.length - 5} more)`
        : '';

    return [`${risk.message} ${preview}${suffix}`];
  });
};

export const getManualShiftLabel = (shift: PickupShift): string => {
  if (shift === 'MORNING') {
    return 'Morning (07:30 - 12:00)';
  }

  if (shift === 'AFTERNOON') {
    return 'Afternoon (13:30 - 18:00)';
  }

  return 'Evening (18:30 - 22:00)';
};
