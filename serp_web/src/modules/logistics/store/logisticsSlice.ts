/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Store Slice
*/

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type {
  Category,
  Customer,
  Facility,
  InventoryItem,
  Order,
  Product,
  Shipment,
  Supplier,
  CategoryFilters,
  CustomerFilters,
  FacilityFilters,
  InventoryItemFilters,
  OrderFilters,
  ProductFilters,
  ShipmentFilters,
  SupplierFilters,
  PaginationParams,
} from '../types';

// Category slice state
interface CategoryState {
  items: Category[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: CategoryFilters;
  pagination: PaginationParams;
  total: number;
}

// Customer slice state
interface CustomerState {
  items: Customer[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: CustomerFilters;
  pagination: PaginationParams;
  total: number;
}

// Facility slice state
interface FacilityState {
  items: Facility[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: FacilityFilters;
  pagination: PaginationParams;
  total: number;
}

// Inventory Item slice state
interface InventoryItemState {
  items: InventoryItem[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: InventoryItemFilters;
  pagination: PaginationParams;
  total: number;
}

// Order slice state
interface OrderState {
  items: Order[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: OrderFilters;
  pagination: PaginationParams;
  total: number;
}

// Product slice state
interface ProductState {
  items: Product[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: ProductFilters;
  pagination: PaginationParams;
  total: number;
}

// Shipment slice state
interface ShipmentState {
  items: Shipment[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: ShipmentFilters;
  pagination: PaginationParams;
  total: number;
}

// Supplier slice state
interface SupplierState {
  items: Supplier[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: SupplierFilters;
  pagination: PaginationParams;
  total: number;
}

// Logistics UI state
export interface LogisticsUIState {
  activeModule:
    | 'dashboard'
    | 'inventory'
    | 'shipments'
    | 'orders'
    | 'products'
    | 'facilities'
    | 'suppliers';
  selectedItems: string[];
  bulkActionMode: boolean;
  viewMode: 'list' | 'grid' | 'kanban';
  sidebarCollapsed: boolean;
  filterPanelOpen: boolean;
}

// Logistics state
interface LogisticsState {
  ui: LogisticsUIState;
  categories: CategoryState;
  customers: CustomerState;
  facilities: FacilityState;
  inventoryItems: InventoryItemState;
  orders: OrderState;
  products: ProductState;
  shipments: ShipmentState;
  suppliers: SupplierState;
}

// Initial state
const initialPagination: PaginationParams = {
  page: 0,
  size: 10,
  sortBy: 'createdStamp',
  sortDirection: 'desc',
};

const initialCategoryState: CategoryState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialCustomerState: CustomerState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialFacilityState: FacilityState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialInventoryItemState: InventoryItemState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialOrderState: OrderState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialProductState: ProductState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialShipmentState: ShipmentState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialSupplierState: SupplierState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialState: LogisticsState = {
  ui: {
    activeModule: 'dashboard',
    selectedItems: [],
    bulkActionMode: false,
    viewMode: 'list',
    sidebarCollapsed: false,
    filterPanelOpen: false,
  },
  categories: initialCategoryState,
  customers: initialCustomerState,
  facilities: initialFacilityState,
  inventoryItems: initialInventoryItemState,
  orders: initialOrderState,
  products: initialProductState,
  shipments: initialShipmentState,
  suppliers: initialSupplierState,
};

// Logistics slice
const logisticsSlice = createSlice({
  name: 'logistics',
  initialState,
  reducers: {
    // UI actions
    setActiveModule: (
      state,
      action: PayloadAction<LogisticsUIState['activeModule']>
    ) => {
      state.ui.activeModule = action.payload;
      state.ui.selectedItems = [];
      state.ui.bulkActionMode = false;
    },

    setViewMode: (
      state,
      action: PayloadAction<LogisticsUIState['viewMode']>
    ) => {
      state.ui.viewMode = action.payload;
    },

    toggleSidebar: (state) => {
      state.ui.sidebarCollapsed = !state.ui.sidebarCollapsed;
    },

    toggleFilterPanel: (state) => {
      state.ui.filterPanelOpen = !state.ui.filterPanelOpen;
    },

    toggleBulkActionMode: (state) => {
      state.ui.bulkActionMode = !state.ui.bulkActionMode;
      if (!state.ui.bulkActionMode) {
        state.ui.selectedItems = [];
      }
    },

    setGlobalSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.ui.selectedItems = action.payload;
    },

    addGlobalSelectedItem: (state, action: PayloadAction<string>) => {
      if (!state.ui.selectedItems.includes(action.payload)) {
        state.ui.selectedItems.push(action.payload);
      }
    },

    removeGlobalSelectedItem: (state, action: PayloadAction<string>) => {
      state.ui.selectedItems = state.ui.selectedItems.filter(
        (id) => id !== action.payload
      );
    },

    clearGlobalSelection: (state) => {
      state.ui.selectedItems = [];
    },

    // Category actions
    setCategoryFilters: (state, action: PayloadAction<CategoryFilters>) => {
      state.categories.filters = action.payload;
      state.categories.pagination.page = 0;
    },

    setCategoryPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.categories.pagination = {
        ...state.categories.pagination,
        ...action.payload,
      };
    },

    setCategorySelectedItems: (state, action: PayloadAction<string[]>) => {
      state.categories.selectedItems = action.payload;
    },

    toggleCategorySelection: (state, action: PayloadAction<string>) => {
      const index = state.categories.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.categories.selectedItems.splice(index, 1);
      } else {
        state.categories.selectedItems.push(action.payload);
      }
    },

    clearCategorySelection: (state) => {
      state.categories.selectedItems = [];
    },

    // Customer actions
    setCustomerFilters: (state, action: PayloadAction<CustomerFilters>) => {
      state.customers.filters = action.payload;
      state.customers.pagination.page = 0;
    },

    setCustomerPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.customers.pagination = {
        ...state.customers.pagination,
        ...action.payload,
      };
    },

    setCustomerSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.customers.selectedItems = action.payload;
    },

    toggleCustomerSelection: (state, action: PayloadAction<string>) => {
      const index = state.customers.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.customers.selectedItems.splice(index, 1);
      } else {
        state.customers.selectedItems.push(action.payload);
      }
    },

    clearCustomerSelection: (state) => {
      state.customers.selectedItems = [];
    },

    // Facility actions
    setFacilityFilters: (state, action: PayloadAction<FacilityFilters>) => {
      state.facilities.filters = action.payload;
      state.facilities.pagination.page = 0;
    },

    setFacilityPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.facilities.pagination = {
        ...state.facilities.pagination,
        ...action.payload,
      };
    },

    setFacilitySelectedItems: (state, action: PayloadAction<string[]>) => {
      state.facilities.selectedItems = action.payload;
    },

    toggleFacilitySelection: (state, action: PayloadAction<string>) => {
      const index = state.facilities.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.facilities.selectedItems.splice(index, 1);
      } else {
        state.facilities.selectedItems.push(action.payload);
      }
    },

    clearFacilitySelection: (state) => {
      state.facilities.selectedItems = [];
    },

    // Inventory Item actions
    setInventoryItemFilters: (
      state,
      action: PayloadAction<InventoryItemFilters>
    ) => {
      state.inventoryItems.filters = action.payload;
      state.inventoryItems.pagination.page = 0;
    },

    setInventoryItemPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.inventoryItems.pagination = {
        ...state.inventoryItems.pagination,
        ...action.payload,
      };
    },

    setInventoryItemSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.inventoryItems.selectedItems = action.payload;
    },

    toggleInventoryItemSelection: (state, action: PayloadAction<string>) => {
      const index = state.inventoryItems.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.inventoryItems.selectedItems.splice(index, 1);
      } else {
        state.inventoryItems.selectedItems.push(action.payload);
      }
    },

    clearInventoryItemSelection: (state) => {
      state.inventoryItems.selectedItems = [];
    },

    // Order actions
    setOrderFilters: (state, action: PayloadAction<OrderFilters>) => {
      state.orders.filters = action.payload;
      state.orders.pagination.page = 0;
    },

    setOrderPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.orders.pagination = {
        ...state.orders.pagination,
        ...action.payload,
      };
    },

    setOrderSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.orders.selectedItems = action.payload;
    },

    toggleOrderSelection: (state, action: PayloadAction<string>) => {
      const index = state.orders.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.orders.selectedItems.splice(index, 1);
      } else {
        state.orders.selectedItems.push(action.payload);
      }
    },

    clearOrderSelection: (state) => {
      state.orders.selectedItems = [];
    },

    // Product actions
    setProductFilters: (state, action: PayloadAction<ProductFilters>) => {
      state.products.filters = action.payload;
      state.products.pagination.page = 0;
    },

    setProductPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.products.pagination = {
        ...state.products.pagination,
        ...action.payload,
      };
    },

    setProductSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.products.selectedItems = action.payload;
    },

    toggleProductSelection: (state, action: PayloadAction<string>) => {
      const index = state.products.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.products.selectedItems.splice(index, 1);
      } else {
        state.products.selectedItems.push(action.payload);
      }
    },

    clearProductSelection: (state) => {
      state.products.selectedItems = [];
    },

    // Shipment actions
    setShipmentFilters: (state, action: PayloadAction<ShipmentFilters>) => {
      state.shipments.filters = action.payload;
      state.shipments.pagination.page = 0;
    },

    setShipmentPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.shipments.pagination = {
        ...state.shipments.pagination,
        ...action.payload,
      };
    },

    setShipmentSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.shipments.selectedItems = action.payload;
    },

    toggleShipmentSelection: (state, action: PayloadAction<string>) => {
      const index = state.shipments.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.shipments.selectedItems.splice(index, 1);
      } else {
        state.shipments.selectedItems.push(action.payload);
      }
    },

    clearShipmentSelection: (state) => {
      state.shipments.selectedItems = [];
    },

    // Supplier actions
    setSupplierFilters: (state, action: PayloadAction<SupplierFilters>) => {
      state.suppliers.filters = action.payload;
      state.suppliers.pagination.page = 0;
    },

    setSupplierPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.suppliers.pagination = {
        ...state.suppliers.pagination,
        ...action.payload,
      };
    },

    setSupplierSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.suppliers.selectedItems = action.payload;
    },

    toggleSupplierSelection: (state, action: PayloadAction<string>) => {
      const index = state.suppliers.selectedItems.indexOf(action.payload);
      if (index > -1) {
        state.suppliers.selectedItems.splice(index, 1);
      } else {
        state.suppliers.selectedItems.push(action.payload);
      }
    },

    clearSupplierSelection: (state) => {
      state.suppliers.selectedItems = [];
    },

    // Reset actions
    resetCategoryState: (state) => {
      state.categories = initialCategoryState;
    },

    resetCustomerState: (state) => {
      state.customers = initialCustomerState;
    },

    resetFacilityState: (state) => {
      state.facilities = initialFacilityState;
    },

    resetInventoryItemState: (state) => {
      state.inventoryItems = initialInventoryItemState;
    },

    resetOrderState: (state) => {
      state.orders = initialOrderState;
    },

    resetProductState: (state) => {
      state.products = initialProductState;
    },

    resetShipmentState: (state) => {
      state.shipments = initialShipmentState;
    },

    resetSupplierState: (state) => {
      state.suppliers = initialSupplierState;
    },

    resetAllState: () => initialState,
  },
});

export const {
  // UI actions
  setActiveModule,
  setViewMode,
  toggleSidebar,
  toggleFilterPanel,
  toggleBulkActionMode,
  setGlobalSelectedItems,
  addGlobalSelectedItem,
  removeGlobalSelectedItem,
  clearGlobalSelection,
  // Category actions
  setCategoryFilters,
  setCategoryPagination,
  setCategorySelectedItems,
  toggleCategorySelection,
  clearCategorySelection,
  // Customer actions
  setCustomerFilters,
  setCustomerPagination,
  setCustomerSelectedItems,
  toggleCustomerSelection,
  clearCustomerSelection,
  // Facility actions
  setFacilityFilters,
  setFacilityPagination,
  setFacilitySelectedItems,
  toggleFacilitySelection,
  clearFacilitySelection,
  // Inventory Item actions
  setInventoryItemFilters,
  setInventoryItemPagination,
  setInventoryItemSelectedItems,
  toggleInventoryItemSelection,
  clearInventoryItemSelection,
  // Order actions
  setOrderFilters,
  setOrderPagination,
  setOrderSelectedItems,
  toggleOrderSelection,
  clearOrderSelection,
  // Product actions
  setProductFilters,
  setProductPagination,
  setProductSelectedItems,
  toggleProductSelection,
  clearProductSelection,
  // Shipment actions
  setShipmentFilters,
  setShipmentPagination,
  setShipmentSelectedItems,
  toggleShipmentSelection,
  clearShipmentSelection,
  // Supplier actions
  setSupplierFilters,
  setSupplierPagination,
  setSupplierSelectedItems,
  toggleSupplierSelection,
  clearSupplierSelection,
  // Reset actions
  resetCategoryState,
  resetCustomerState,
  resetFacilityState,
  resetInventoryItemState,
  resetOrderState,
  resetProductState,
  resetShipmentState,
  resetSupplierState,
  resetAllState,
} = logisticsSlice.actions;

export default logisticsSlice.reducer;
