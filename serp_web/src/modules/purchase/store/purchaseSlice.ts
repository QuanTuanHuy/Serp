// Purchase Store Slice (authors: QuanTuanHuy, Description: Part of Serp Project)

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { Supplier, Product, Order, Category, Facility } from '../types';

interface PurchaseState {
  selectedSupplier: Supplier | null;
  selectedProduct: Product | null;
  selectedOrder: Order | null;
  selectedCategory: Category | null;
  selectedFacility: Facility | null;
  filters: {
    suppliers: {
      query?: string;
      statusId?: string;
    };
    products: {
      query?: string;
      categoryId?: string;
      statusId?: string;
    };
    orders: {
      query?: string;
      statusId?: string;
      orderTypeId?: string;
      fromSupplierId?: string;
      fromDate?: string;
      toDate?: string;
    };
    categories: {
      query?: string;
    };
    facilities: {
      query?: string;
      statusId?: string;
    };
  };
}

const initialState: PurchaseState = {
  selectedSupplier: null,
  selectedProduct: null,
  selectedOrder: null,
  selectedCategory: null,
  selectedFacility: null,
  filters: {
    suppliers: {},
    products: {},
    orders: {},
    categories: {},
    facilities: {},
  },
};

const purchaseSlice = createSlice({
  name: 'purchase',
  initialState,
  reducers: {
    setSelectedSupplier: (state, action: PayloadAction<Supplier | null>) => {
      state.selectedSupplier = action.payload;
    },
    setSelectedProduct: (state, action: PayloadAction<Product | null>) => {
      state.selectedProduct = action.payload;
    },
    setSelectedOrder: (state, action: PayloadAction<Order | null>) => {
      state.selectedOrder = action.payload;
    },
    setSelectedCategory: (state, action: PayloadAction<Category | null>) => {
      state.selectedCategory = action.payload;
    },
    setSelectedFacility: (state, action: PayloadAction<Facility | null>) => {
      state.selectedFacility = action.payload;
    },
    setSupplierFilters: (
      state,
      action: PayloadAction<PurchaseState['filters']['suppliers']>
    ) => {
      state.filters.suppliers = action.payload;
    },
    setProductFilters: (
      state,
      action: PayloadAction<PurchaseState['filters']['products']>
    ) => {
      state.filters.products = action.payload;
    },
    setOrderFilters: (
      state,
      action: PayloadAction<PurchaseState['filters']['orders']>
    ) => {
      state.filters.orders = action.payload;
    },
    setCategoryFilters: (
      state,
      action: PayloadAction<PurchaseState['filters']['categories']>
    ) => {
      state.filters.categories = action.payload;
    },
    setFacilityFilters: (
      state,
      action: PayloadAction<PurchaseState['filters']['facilities']>
    ) => {
      state.filters.facilities = action.payload;
    },
    clearFilters: (state) => {
      state.filters = initialState.filters;
    },
    resetPurchaseState: () => initialState,
  },
});

export const {
  setSelectedSupplier,
  setSelectedProduct,
  setSelectedOrder,
  setSelectedCategory,
  setSelectedFacility,
  setSupplierFilters,
  setProductFilters,
  setOrderFilters,
  setCategoryFilters,
  setFacilityFilters,
  clearFilters,
  resetPurchaseState,
} = purchaseSlice.actions;

export default purchaseSlice.reducer;
