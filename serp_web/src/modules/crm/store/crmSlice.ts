// CRM Store Slice (authors: QuanTuanHuy, Description: Part of Serp Project)

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type {
  Customer,
  Lead,
  Opportunity,
  Activity,
  CRMUIState,
  CustomerFilters,
  LeadFilters,
  OpportunityFilters,
  ActivityFilters,
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

// Lead slice state
interface LeadState {
  items: Lead[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: LeadFilters;
  pagination: PaginationParams;
  total: number;
}

// Opportunity slice state
interface OpportunityState {
  items: Opportunity[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: OpportunityFilters;
  pagination: PaginationParams;
  total: number;
}

// Activity slice state
interface ActivityState {
  items: Activity[];
  selectedItems: string[];
  loading: boolean;
  error: string | null;
  filters: ActivityFilters;
  pagination: PaginationParams;
  total: number;
}

// CRM UI state
interface CRMState {
  ui: CRMUIState;
  customers: CustomerState;
  leads: LeadState;
  opportunities: OpportunityState;
  activities: ActivityState;
}

// Initial state
const initialPagination: PaginationParams = {
  page: 1,
  limit: 20,
  sortBy: 'updatedAt',
  sortOrder: 'desc',
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

const initialLeadState: LeadState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialOpportunityState: OpportunityState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialActivityState: ActivityState = {
  items: [],
  selectedItems: [],
  loading: false,
  error: null,
  filters: {},
  pagination: initialPagination,
  total: 0,
};

const initialState: CRMState = {
  ui: {
    activeModule: 'customers',
    selectedItems: [],
    bulkActionMode: false,
    viewMode: 'list',
    sidebarCollapsed: false,
    filterPanelOpen: false,
  },
  customers: initialCustomerState,
  leads: initialLeadState,
  opportunities: initialOpportunityState,
  activities: initialActivityState,
};

// CRM slice
const crmSlice = createSlice({
  name: 'crm',
  initialState,
  reducers: {
    // UI actions
    setActiveModule: (
      state,
      action: PayloadAction<CRMUIState['activeModule']>
    ) => {
      state.ui.activeModule = action.payload;
      // Clear selections when switching modules
      state.ui.selectedItems = [];
      state.ui.bulkActionMode = false;
    },

    setViewMode: (state, action: PayloadAction<CRMUIState['viewMode']>) => {
      state.ui.viewMode = action.payload;
    },

    toggleSidebar: (state) => {
      state.ui.sidebarCollapsed = !state.ui.sidebarCollapsed;
    },

    toggleFilterPanel: (state) => {
      state.ui.filterPanelOpen = !state.ui.filterPanelOpen;
    },

    setBulkActionMode: (state, action: PayloadAction<boolean>) => {
      state.ui.bulkActionMode = action.payload;
      if (!action.payload) {
        state.ui.selectedItems = [];
      }
    },

    setGlobalSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.ui.selectedItems = action.payload;
    },

    // Customer actions
    setCustomers: (
      state,
      action: PayloadAction<{ items: Customer[]; total: number }>
    ) => {
      state.customers.items = action.payload.items;
      state.customers.total = action.payload.total;
      state.customers.loading = false;
      state.customers.error = null;
    },

    setCustomersLoading: (state, action: PayloadAction<boolean>) => {
      state.customers.loading = action.payload;
      if (action.payload) {
        state.customers.error = null;
      }
    },

    setCustomersError: (state, action: PayloadAction<string>) => {
      state.customers.error = action.payload;
      state.customers.loading = false;
    },

    updateCustomer: (state, action: PayloadAction<Customer>) => {
      const index = state.customers.items.findIndex(
        (item) => item.id === action.payload.id
      );
      if (index !== -1) {
        state.customers.items[index] = action.payload;
      }
    },

    addCustomer: (state, action: PayloadAction<Customer>) => {
      state.customers.items.unshift(action.payload);
      state.customers.total += 1;
    },

    removeCustomer: (state, action: PayloadAction<string>) => {
      state.customers.items = state.customers.items.filter(
        (item) => item.id !== action.payload
      );
      state.customers.total -= 1;
    },

    setCustomerFilters: (state, action: PayloadAction<CustomerFilters>) => {
      state.customers.filters = action.payload;
      state.customers.pagination.page = 1; // Reset to first page when filtering
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

    // Lead actions
    setLeads: (
      state,
      action: PayloadAction<{ items: Lead[]; total: number }>
    ) => {
      state.leads.items = action.payload.items;
      state.leads.total = action.payload.total;
      state.leads.loading = false;
      state.leads.error = null;
    },

    setLeadsLoading: (state, action: PayloadAction<boolean>) => {
      state.leads.loading = action.payload;
      if (action.payload) {
        state.leads.error = null;
      }
    },

    setLeadsError: (state, action: PayloadAction<string>) => {
      state.leads.error = action.payload;
      state.leads.loading = false;
    },

    updateLead: (state, action: PayloadAction<Lead>) => {
      const index = state.leads.items.findIndex(
        (item) => item.id === action.payload.id
      );
      if (index !== -1) {
        state.leads.items[index] = action.payload;
      }
    },

    addLead: (state, action: PayloadAction<Lead>) => {
      state.leads.items.unshift(action.payload);
      state.leads.total += 1;
    },

    removeLead: (state, action: PayloadAction<string>) => {
      state.leads.items = state.leads.items.filter(
        (item) => item.id !== action.payload
      );
      state.leads.total -= 1;
    },

    setLeadFilters: (state, action: PayloadAction<LeadFilters>) => {
      state.leads.filters = action.payload;
      state.leads.pagination.page = 1;
    },

    setLeadPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.leads.pagination = { ...state.leads.pagination, ...action.payload };
    },

    setLeadSelectedItems: (state, action: PayloadAction<string[]>) => {
      state.leads.selectedItems = action.payload;
    },

    // Opportunity actions
    setOpportunities: (
      state,
      action: PayloadAction<{ items: Opportunity[]; total: number }>
    ) => {
      state.opportunities.items = action.payload.items;
      state.opportunities.total = action.payload.total;
      state.opportunities.loading = false;
      state.opportunities.error = null;
    },

    setOpportunitiesLoading: (state, action: PayloadAction<boolean>) => {
      state.opportunities.loading = action.payload;
      if (action.payload) {
        state.opportunities.error = null;
      }
    },

    setOpportunitiesError: (state, action: PayloadAction<string>) => {
      state.opportunities.error = action.payload;
      state.opportunities.loading = false;
    },

    updateOpportunity: (state, action: PayloadAction<Opportunity>) => {
      const index = state.opportunities.items.findIndex(
        (item) => item.id === action.payload.id
      );
      if (index !== -1) {
        state.opportunities.items[index] = action.payload;
      }
    },

    addOpportunity: (state, action: PayloadAction<Opportunity>) => {
      state.opportunities.items.unshift(action.payload);
      state.opportunities.total += 1;
    },

    removeOpportunity: (state, action: PayloadAction<string>) => {
      state.opportunities.items = state.opportunities.items.filter(
        (item) => item.id !== action.payload
      );
      state.opportunities.total -= 1;
    },

    setOpportunityFilters: (
      state,
      action: PayloadAction<OpportunityFilters>
    ) => {
      state.opportunities.filters = action.payload;
      state.opportunities.pagination.page = 1;
    },

    setOpportunityPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.opportunities.pagination = {
        ...state.opportunities.pagination,
        ...action.payload,
      };
    },

    setOpportunitySelectedItems: (state, action: PayloadAction<string[]>) => {
      state.opportunities.selectedItems = action.payload;
    },

    // Activity actions
    setActivities: (
      state,
      action: PayloadAction<{ items: Activity[]; total: number }>
    ) => {
      state.activities.items = action.payload.items;
      state.activities.total = action.payload.total;
      state.activities.loading = false;
      state.activities.error = null;
    },

    setActivitiesLoading: (state, action: PayloadAction<boolean>) => {
      state.activities.loading = action.payload;
      if (action.payload) {
        state.activities.error = null;
      }
    },

    setActivitiesError: (state, action: PayloadAction<string>) => {
      state.activities.error = action.payload;
      state.activities.loading = false;
    },

    updateActivity: (state, action: PayloadAction<Activity>) => {
      const index = state.activities.items.findIndex(
        (item) => item.id === action.payload.id
      );
      if (index !== -1) {
        state.activities.items[index] = action.payload;
      }
    },

    addActivity: (state, action: PayloadAction<Activity>) => {
      state.activities.items.unshift(action.payload);
      state.activities.total += 1;
    },

    removeActivity: (state, action: PayloadAction<string>) => {
      state.activities.items = state.activities.items.filter(
        (item) => item.id !== action.payload
      );
      state.activities.total -= 1;
    },

    setActivityFilters: (state, action: PayloadAction<ActivityFilters>) => {
      state.activities.filters = action.payload;
      state.activities.pagination.page = 1;
    },

    setActivityPagination: (
      state,
      action: PayloadAction<Partial<PaginationParams>>
    ) => {
      state.activities.pagination = {
        ...state.activities.pagination,
        ...action.payload,
      };
    },

    setActivitySelectedItems: (state, action: PayloadAction<string[]>) => {
      state.activities.selectedItems = action.payload;
    },

    // Reset actions
    resetFilters: (state) => {
      state.customers.filters = {};
      state.leads.filters = {};
      state.opportunities.filters = {};
      state.activities.filters = {};
      // Reset pagination to first page
      state.customers.pagination.page = 1;
      state.leads.pagination.page = 1;
      state.opportunities.pagination.page = 1;
      state.activities.pagination.page = 1;
    },

    clearSelections: (state) => {
      state.ui.selectedItems = [];
      state.customers.selectedItems = [];
      state.leads.selectedItems = [];
      state.opportunities.selectedItems = [];
      state.activities.selectedItems = [];
      state.ui.bulkActionMode = false;
    },
  },
});

// Export actions
export const {
  // UI actions
  setActiveModule,
  setViewMode,
  toggleSidebar,
  toggleFilterPanel,
  setBulkActionMode,
  setGlobalSelectedItems,

  // Customer actions
  setCustomers,
  setCustomersLoading,
  setCustomersError,
  updateCustomer,
  addCustomer,
  removeCustomer,
  setCustomerFilters,
  setCustomerPagination,
  setCustomerSelectedItems,

  // Lead actions
  setLeads,
  setLeadsLoading,
  setLeadsError,
  updateLead,
  addLead,
  removeLead,
  setLeadFilters,
  setLeadPagination,
  setLeadSelectedItems,

  // Opportunity actions
  setOpportunities,
  setOpportunitiesLoading,
  setOpportunitiesError,
  updateOpportunity,
  addOpportunity,
  removeOpportunity,
  setOpportunityFilters,
  setOpportunityPagination,
  setOpportunitySelectedItems,

  // Activity actions
  setActivities,
  setActivitiesLoading,
  setActivitiesError,
  updateActivity,
  addActivity,
  removeActivity,
  setActivityFilters,
  setActivityPagination,
  setActivitySelectedItems,

  // Reset actions
  resetFilters,
  clearSelections,
} = crmSlice.actions;

// Export reducer
export default crmSlice.reducer;

// Export types
export type {
  CRMState,
  CustomerState,
  LeadState,
  OpportunityState,
  ActivityState,
};
