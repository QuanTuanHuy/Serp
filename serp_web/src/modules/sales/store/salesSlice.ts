// Sales Store Slice (authors: QuanTuanHuy, Description: Part of Serp Project)

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type {
  Customer,
  Product,
  Order,
  Category,
  Facility,
  InventoryItem,
  CustomerFilters,
  ProductFilters,
  OrderFilters,
  CategoryFilters,
  FacilityFilters,
  InventoryItemFilters,
  PaginationParams,
} from '../types';

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

// Sales UI state
export interface SalesUIState {
  activeModule:
    | 'dashboard'
    | 'customers'
    | 'products'
    | 'orders'
    | 'categories'
    | 'facilities'
    | 'inventory';
  selectedItems: string[];
  bulkActionMode: boolean;
  viewMode: 'list' | 'grid' | 'kanban';
  sidebarCollapsed: boolean;
  filterPanelOpen: boolean;
}

// Sales state
interface SalesState {
  ui: SalesUIState;
  customers: CustomerState;
  products: ProductState;
  orders: OrderState;
  categories: CategoryState;
  facilities: FacilityState;
  inventoryItems: InventoryItemState;
}

// Initial state
const initialPagination: PaginationParams = {
  page: 0,
  size: 10,
  sortBy: 'createdStamp',
  sortDirection: 'desc',
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

const initialProductState: ProductState = {
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

const initialCategoryState: CategoryState = {
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

const initialState: SalesState = {
  ui: {
    activeModule: 'dashboard',
    selectedItems: [],
    bulkActionMode: false,
    viewMode: 'list',
    sidebarCollapsed: false,
    filterPanelOpen: false,
  },
  customers: initialCustomerState,
  products: initialProductState,
  orders: initialOrderState,
  categories: initialCategoryState,
  facilities: initialFacilityState,
  inventoryItems: initialInventoryItemState,
};

// Sales slice
const salesSlice = createSlice({
  name: 'sales',
  initialState,
  reducers: {
    // UI actions
    setActiveModule: (
      state,
      action: PayloadAction<SalesUIState['activeModule']>
    ) => {
      state.ui.activeModule = action.payload;
      state.ui.selectedItems = [];
      state.ui.bulkActionMode = false;
    },

    setViewMode: (state, action: PayloadAction<SalesUIState['viewMode']>) => {
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

    // Reset actions
    resetCustomerState: (state) => {
      state.customers = initialCustomerState;
    },

    resetProductState: (state) => {
      state.products = initialProductState;
    },

    resetOrderState: (state) => {
      state.orders = initialOrderState;
    },

    resetCategoryState: (state) => {
      state.categories = initialCategoryState;
    },

    resetFacilityState: (state) => {
      state.facilities = initialFacilityState;
    },

    resetInventoryItemState: (state) => {
      state.inventoryItems = initialInventoryItemState;
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
  // Customer actions
  setCustomerFilters,
  setCustomerPagination,
  setCustomerSelectedItems,
  toggleCustomerSelection,
  clearCustomerSelection,
  // Product actions
  setProductFilters,
  setProductPagination,
  setProductSelectedItems,
  toggleProductSelection,
  clearProductSelection,
  // Order actions
  setOrderFilters,
  setOrderPagination,
  setOrderSelectedItems,
  toggleOrderSelection,
  clearOrderSelection,
  // Category actions
  setCategoryFilters,
  setCategoryPagination,
  // Facility actions
  setFacilityFilters,
  setFacilityPagination,
  // Inventory Item actions
  setInventoryItemFilters,
  setInventoryItemPagination,
  // Reset actions
  resetCustomerState,
  resetProductState,
  resetOrderState,
  resetCategoryState,
  resetFacilityState,
  resetInventoryItemState,
  resetAllState,
} = salesSlice.actions;

export default salesSlice.reducer;
