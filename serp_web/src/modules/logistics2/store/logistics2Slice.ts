import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type {
  DeliveryPlanFilters,
  DeliverySlipFilters,
  OutboundShipmentFilters,
  PaginationParams,
  RouteFilters,
  VehicleFilters,
  VehicleShipperFilters,
} from '../types';

export type Logistics2ActiveModule =
  | 'delivery-plans'
  | 'delivery-slips'
  | 'outbound-shipments'
  | 'routes'
  | 'vehicles'
  | 'vehicle-shippers'
  | 'vehicle-registration';

interface ListViewState<TFilters extends object> {
  filters: TFilters;
  pagination: PaginationParams;
}

interface Logistics2UIState {
  activeModule: Logistics2ActiveModule;
  sidebarCollapsed: boolean;
  filterPanelOpen: boolean;
}

interface Logistics2State {
  ui: Logistics2UIState;
  deliveryPlans: ListViewState<DeliveryPlanFilters>;
  deliverySlips: ListViewState<DeliverySlipFilters>;
  outboundShipments: ListViewState<OutboundShipmentFilters>;
  routes: ListViewState<RouteFilters>;
  vehicles: ListViewState<VehicleFilters>;
  vehicleShippers: ListViewState<VehicleShipperFilters>;
}

const defaultPagination: PaginationParams = {
  page: 0,
  size: 10,
  sortBy: 'createdStamp',
  sortDirection: 'desc',
};

const createListViewState = <
  TFilters extends object,
>(): ListViewState<TFilters> => ({
  filters: {} as TFilters,
  pagination: { ...defaultPagination },
});

const initialState: Logistics2State = {
  ui: {
    activeModule: 'delivery-plans',
    sidebarCollapsed: false,
    filterPanelOpen: false,
  },
  deliveryPlans: createListViewState<DeliveryPlanFilters>(),
  deliverySlips: createListViewState<DeliverySlipFilters>(),
  outboundShipments: createListViewState<OutboundShipmentFilters>(),
  routes: createListViewState<RouteFilters>(),
  vehicles: createListViewState<VehicleFilters>(),
  vehicleShippers: createListViewState<VehicleShipperFilters>(),
};

const logistics2Slice = createSlice({
  name: 'logistics2',
  initialState,
  reducers: {
    setActiveModule: (state, action: PayloadAction<Logistics2ActiveModule>) => {
      state.ui.activeModule = action.payload;
    },

    toggleSidebar: (state) => {
      state.ui.sidebarCollapsed = !state.ui.sidebarCollapsed;
    },

    toggleFilterPanel: (state) => {
      state.ui.filterPanelOpen = !state.ui.filterPanelOpen;
    },

    setDeliveryPlanFilters: (
      state,
      action: PayloadAction<DeliveryPlanFilters>
    ) => {
      state.deliveryPlans.filters = action.payload;
      state.deliveryPlans.pagination.page = 0;
    },

    setDeliveryPlanPagination: (
      state,
      action: PayloadAction<PaginationParams>
    ) => {
      state.deliveryPlans.pagination = {
        ...state.deliveryPlans.pagination,
        ...action.payload,
      };
    },

    setDeliverySlipFilters: (
      state,
      action: PayloadAction<DeliverySlipFilters>
    ) => {
      state.deliverySlips.filters = action.payload;
      state.deliverySlips.pagination.page = 0;
    },

    setDeliverySlipPagination: (
      state,
      action: PayloadAction<PaginationParams>
    ) => {
      state.deliverySlips.pagination = {
        ...state.deliverySlips.pagination,
        ...action.payload,
      };
    },

    setOutboundShipmentFilters: (
      state,
      action: PayloadAction<OutboundShipmentFilters>
    ) => {
      state.outboundShipments.filters = action.payload;
      state.outboundShipments.pagination.page = 0;
    },

    setOutboundShipmentPagination: (
      state,
      action: PayloadAction<PaginationParams>
    ) => {
      state.outboundShipments.pagination = {
        ...state.outboundShipments.pagination,
        ...action.payload,
      };
    },

    setRouteFilters: (state, action: PayloadAction<RouteFilters>) => {
      state.routes.filters = action.payload;
      state.routes.pagination.page = 0;
    },

    setRoutePagination: (state, action: PayloadAction<PaginationParams>) => {
      state.routes.pagination = {
        ...state.routes.pagination,
        ...action.payload,
      };
    },

    setVehicleFilters: (state, action: PayloadAction<VehicleFilters>) => {
      state.vehicles.filters = action.payload;
      state.vehicles.pagination.page = 0;
    },

    setVehiclePagination: (state, action: PayloadAction<PaginationParams>) => {
      state.vehicles.pagination = {
        ...state.vehicles.pagination,
        ...action.payload,
      };
    },

    setVehicleShipperFilters: (
      state,
      action: PayloadAction<VehicleShipperFilters>
    ) => {
      state.vehicleShippers.filters = action.payload;
      state.vehicleShippers.pagination.page = 0;
    },

    setVehicleShipperPagination: (
      state,
      action: PayloadAction<PaginationParams>
    ) => {
      state.vehicleShippers.pagination = {
        ...state.vehicleShippers.pagination,
        ...action.payload,
      };
    },

    resetLogistics2Filters: (state) => {
      state.deliveryPlans = createListViewState<DeliveryPlanFilters>();
      state.deliverySlips = createListViewState<DeliverySlipFilters>();
      state.outboundShipments = createListViewState<OutboundShipmentFilters>();
      state.routes = createListViewState<RouteFilters>();
      state.vehicles = createListViewState<VehicleFilters>();
      state.vehicleShippers = createListViewState<VehicleShipperFilters>();
    },
  },
});

export const {
  setActiveModule,
  toggleSidebar,
  toggleFilterPanel,
  setDeliveryPlanFilters,
  setDeliveryPlanPagination,
  setDeliverySlipFilters,
  setDeliverySlipPagination,
  setOutboundShipmentFilters,
  setOutboundShipmentPagination,
  setRouteFilters,
  setRoutePagination,
  setVehicleFilters,
  setVehiclePagination,
  setVehicleShipperFilters,
  setVehicleShipperPagination,
  resetLogistics2Filters,
} = logistics2Slice.actions;

export default logistics2Slice.reducer;
