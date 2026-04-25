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
} from '../../../types';

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

export type DispatchOptimizationEffortOption = 'FAST' | 'STANDARD' | 'THOROUGH';

export type DispatchActionType = 'preview' | 'auto' | 'manual' | null;

export interface BusinessDispatchSettings {
  vehicle?: string;
  optimization_goal: DispatchOptimizationGoalOption;
  optimization_effort: DispatchOptimizationEffortOption;
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
  optimizationEffort: DispatchOptimizationEffortOption;
}

export interface DispatchSetupBusinessHandlers {
  onVehicleOptionChange: (value: RoutingVehicleOption) => void;
  onOptimizationGoalChange: (value: DispatchOptimizationGoalOption) => void;
  onOptimizationEffortChange: (value: DispatchOptimizationEffortOption) => void;
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
  onRefreshCandidateOrders: () => void;
  isOptimizing: boolean;
  isAutoAssigning: boolean;
  isFetchingOrders: boolean;
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
  checked: boolean;
  onToggle: (orderId: number, checked: boolean) => void;
}

export interface ManualDispatchCardProps {
  selectedPostOfficeId: string;
  orderKeywordInput: string;
  onOrderKeywordInputChange: (value: string) => void;
  onApplyOrderFilters: (event: React.FormEvent) => void;
  courierOptions: DispatchCourierOption[];
  selectedManualCourierId: string;
  onManualCourierIdChange: (value: string) => void;
  isLoadingCouriers: boolean;
  suggestedCouriers: SuggestedCourier[];
  onQuickPickCourier: (courierId: number) => void;
  isLoadingOrders: boolean;
  candidateOrders: FirstMileOrderDetail[];
  selectedOrderIds: number[];
  selectedOrderIdSet: Set<number>;
  onToggleOrder: (orderId: number, checked: boolean) => void;
  onSelectAllCurrentOrders: () => void;
  onClearSelectedOrders: () => void;
  currentPage: number;
  totalPages: number;
  hasPrevious: boolean;
  hasNext: boolean;
  isFetchingOrders: boolean;
  onPreviousPage: () => void;
  onNextPage: () => void;
  onManualAssign: () => void;
  isManualAssigning: boolean;
  activeAction: DispatchActionType;
}

export interface PlanPreviewCardProps {
  optimizationResult: PickupOptimizationResponse;
  formatNumber: (value?: number) => string;
}

export interface AssignmentResultCardProps {
  assignmentResult: PickupAssignmentResponse;
  formatDateTime: (value?: string) => string;
  formatNumber: (value?: number) => string;
}
