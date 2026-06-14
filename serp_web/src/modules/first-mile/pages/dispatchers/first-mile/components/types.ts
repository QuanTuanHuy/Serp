/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher page local component types
 */

import type {
  FirstMileOrderDetail,
  PickupAssignmentResponse,
  PickupOptimizationResponse,
  PickupShift,
  PostOffice,
  Province,
  Ward,
} from '../../../../types';

export type DispatcherAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_SCOPED'
  | 'NO_ACCESS';

export type RoutingVehicleOption =
  | 'DEFAULT'
  | 'CAR'
  | 'BIKE'
  | 'TAXI'
  | 'TRUCK'
  | 'HD';

export type DispatchOptimizationGoalOption =
  | 'BALANCED'
  | 'ON_TIME_PRIORITY'
  | 'COST_EFFICIENCY'
  | 'MAX_ASSIGNMENT';

export type DispatchActionType = 'preview' | 'auto' | 'manual' | null;

export interface BusinessDispatchSettings {
  vehicle?: string;
  optimization_goal: DispatchOptimizationGoalOption;
  allow_lateness?: boolean;
}

export interface SuggestedCourier {
  id: number;
  label: string;
}

export interface DispatchCourierOption {
  id: number;
  code: string;
  fullName: string;
}

export interface DispatchSetupBusinessValues {
  vehicleOption: RoutingVehicleOption;
  optimizationGoal: DispatchOptimizationGoalOption;
}

export interface DispatchSetupBusinessHandlers {
  onVehicleOptionChange: (value: RoutingVehicleOption) => void;
  onOptimizationGoalChange: (value: DispatchOptimizationGoalOption) => void;
}

export interface DispatchSetupCardProps {
  postOfficeOptions: PostOffice[];
  courierOptions: DispatchCourierOption[];
  selectedPostOfficeId: string;
  onPostOfficeChange: (value: string) => void;
  isLoadingPostOffices: boolean;
  isLoadingCouriers: boolean;
  shift: PickupShift;
  onShiftChange: (value: PickupShift) => void;
  tripDate: string;
  onTripDateChange: (value: string) => void;
  orderLimitInput: string;
  onOrderLimitInputChange: (value: string) => void;
  selectedAutoCourierIds: number[];
  onToggleAutoCourier: (courierId: number, checked: boolean) => void;
  onClearAutoCourierSelection: () => void;
  businessValues: DispatchSetupBusinessValues;
  businessHandlers: DispatchSetupBusinessHandlers;
  onPreviewPlan: () => void;
  onAutoAssign: () => void;
  isOptimizing: boolean;
  isAutoAssigning: boolean;
  activeAction: DispatchActionType;
  isTmsAdmin: boolean;
}

export interface AccessScopeCardProps {
  canDispatch: boolean;
  scopeBadgeLabel: string;
  scopeDescription: string;
}

export interface NoAccessCardProps {
  message: string;
}

export interface CandidateOrderItemProps {
  order: FirstMileOrderDetail;
  checked?: boolean;
  onToggle?: (orderId: number, checked: boolean) => void;
  referenceTime?: Date;
}

export interface CandidateOrdersPanelProps {
  title: string;
  orders: FirstMileOrderDetail[];
  loading?: boolean;
  selectedOrderIds?: number[];
  onOrderToggle?: (orderId: number, checked: boolean) => void;
  maxVisibleOrders?: number;
  emptyText?: string;
  referenceTime?: Date;
}

export interface ManualDispatchCardProps {
  provinceOptions: Province[];
  wardOptions: Ward[];
  selectedProvinceCode: string;
  onProvinceChange: (value: string) => void;
  selectedWardCode: string;
  onWardChange: (value: string) => void;
  isLoadingProvinces: boolean;
  isLoadingWards: boolean;
  postOfficeOptions: PostOffice[];
  selectedPostOfficeId: string;
  onPostOfficeChange: (value: string) => void;
  isLoadingPostOffices: boolean;
  shift: PickupShift;
  onShiftChange: (value: PickupShift) => void;
  tripDate: string;
  onTripDateChange: (value: string) => void;
  courierOptions: DispatchCourierOption[];
  selectedManualCourierId: string;
  onManualCourierIdChange: (value: string) => void;
  isLoadingCouriers: boolean;
  suggestedCouriers: SuggestedCourier[];
  onQuickPickCourier: (courierId: number) => void;
  isLoadingOrders: boolean;
  candidateOrders: FirstMileOrderDetail[];
  selectedOrderIds: number[];
  onOrderSelectionChange: (orderIds: number[]) => void;
  onClearSelectedOrders: () => void;
  onManualAssign: () => void;
  isManualAssigning: boolean;
  activeAction: DispatchActionType;
}

export interface PlanPreviewCardProps {
  optimizationResult: PickupOptimizationResponse;
  formatDateTime: (value?: string) => string;
  formatNumber: (value?: number) => string;
}

export interface AssignmentResultCardProps {
  assignmentResult: PickupAssignmentResponse;
  formatDateTime: (value?: string) => string;
  formatNumber: (value?: number) => string;
}
