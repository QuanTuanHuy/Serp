import { createSelector } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';

export const selectLogistics2 = (state: RootState) => state.logistics2;
export const selectLogistics2UI = (state: RootState) => state.logistics2.ui;

export const selectDeliveryPlansState = (state: RootState) =>
  state.logistics2.deliveryPlans;
export const selectDeliverySlipsState = (state: RootState) =>
  state.logistics2.deliverySlips;
export const selectOutboundShipmentsState = (state: RootState) =>
  state.logistics2.outboundShipments;
export const selectRoutesState = (state: RootState) => state.logistics2.routes;
export const selectVehiclesState = (state: RootState) =>
  state.logistics2.vehicles;
export const selectVehicleShippersState = (state: RootState) =>
  state.logistics2.vehicleShippers;

export const selectLogistics2ActiveModule = createSelector(
  selectLogistics2UI,
  (ui) => ui.activeModule
);

export const selectDeliveryPlanFilters = createSelector(
  selectDeliveryPlansState,
  (state) => state.filters
);

export const selectDeliveryPlanPagination = createSelector(
  selectDeliveryPlansState,
  (state) => state.pagination
);

export const selectDeliverySlipFilters = createSelector(
  selectDeliverySlipsState,
  (state) => state.filters
);

export const selectDeliverySlipPagination = createSelector(
  selectDeliverySlipsState,
  (state) => state.pagination
);

export const selectOutboundShipmentFilters = createSelector(
  selectOutboundShipmentsState,
  (state) => state.filters
);

export const selectOutboundShipmentPagination = createSelector(
  selectOutboundShipmentsState,
  (state) => state.pagination
);

export const selectRouteFilters = createSelector(
  selectRoutesState,
  (state) => state.filters
);

export const selectRoutePagination = createSelector(
  selectRoutesState,
  (state) => state.pagination
);

export const selectVehicleFilters = createSelector(
  selectVehiclesState,
  (state) => state.filters
);

export const selectVehiclePagination = createSelector(
  selectVehiclesState,
  (state) => state.pagination
);

export const selectVehicleShipperFilters = createSelector(
  selectVehicleShippersState,
  (state) => state.filters
);

export const selectVehicleShipperPagination = createSelector(
  selectVehicleShippersState,
  (state) => state.pagination
);
