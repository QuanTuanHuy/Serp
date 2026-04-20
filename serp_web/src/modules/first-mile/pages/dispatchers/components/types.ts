/**
 * Author: GitHub Copilot
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

export type BooleanMode = 'default' | 'true' | 'false';

export type RoutingVehicleOption =
  | 'DEFAULT'
  | 'CAR'
  | 'BIKE'
  | 'TAXI'
  | 'TRUCK'
  | 'HD';

export type DispatchActionType = 'preview' | 'auto' | 'manual' | null;

export interface AdvancedDispatchSettings {
  vehicle?: string;
  average_speed_kmph?: number;
  service_minutes_per_stop?: number;
  max_iterations?: number;
  max_runtime_millis?: number;
  destroy_rate?: number;
  initial_temperature?: number;
  cooling_rate?: number;
  allow_lateness?: boolean;
  enforce_planning_end?: boolean;
  enforce_capacity?: boolean;
  distance_weight?: number;
  lateness_weight?: number;
  unassigned_penalty?: number;
  used_route_penalty?: number;
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

export interface DispatchSetupAdvancedValues {
  vehicleOption: RoutingVehicleOption;
  averageSpeedInput: string;
  serviceMinutesPerStopInput: string;
  maxIterationsInput: string;
  maxRuntimeMillisInput: string;
  destroyRateInput: string;
  initialTemperatureInput: string;
  coolingRateInput: string;
  distanceWeightInput: string;
  latenessWeightInput: string;
  unassignedPenaltyInput: string;
  usedRoutePenaltyInput: string;
  allowLatenessMode: BooleanMode;
  enforcePlanningEndMode: BooleanMode;
  enforceCapacityMode: BooleanMode;
}

export interface DispatchSetupAdvancedHandlers {
  onVehicleOptionChange: (value: RoutingVehicleOption) => void;
  onAverageSpeedInputChange: (value: string) => void;
  onServiceMinutesPerStopInputChange: (value: string) => void;
  onMaxIterationsInputChange: (value: string) => void;
  onMaxRuntimeMillisInputChange: (value: string) => void;
  onDestroyRateInputChange: (value: string) => void;
  onInitialTemperatureInputChange: (value: string) => void;
  onCoolingRateInputChange: (value: string) => void;
  onDistanceWeightInputChange: (value: string) => void;
  onLatenessWeightInputChange: (value: string) => void;
  onUnassignedPenaltyInputChange: (value: string) => void;
  onUsedRoutePenaltyInputChange: (value: string) => void;
  onAllowLatenessModeChange: (value: BooleanMode) => void;
  onEnforcePlanningEndModeChange: (value: BooleanMode) => void;
  onEnforceCapacityModeChange: (value: BooleanMode) => void;
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
  advancedValues: DispatchSetupAdvancedValues;
  advancedHandlers: DispatchSetupAdvancedHandlers;
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
