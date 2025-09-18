// CRM Selectors (authors: QuanTuanHuy, Description: Part of Serp Project)

import { createSelector } from '@reduxjs/toolkit';
import type { RootState } from '../../../lib/store';
import type { Customer, Lead, Opportunity, Activity } from '../types';

// Base selectors
export const selectCRM = (state: RootState) => state.crm;
export const selectCRMUI = (state: RootState) => state.crm.ui;
export const selectCustomers = (state: RootState) => state.crm.customers;
export const selectLeads = (state: RootState) => state.crm.leads;
export const selectOpportunities = (state: RootState) =>
  state.crm.opportunities;
export const selectActivities = (state: RootState) => state.crm.activities;

// UI selectors
export const selectActiveModule = createSelector(
  selectCRMUI,
  (ui) => ui.activeModule
);

export const selectViewMode = createSelector(selectCRMUI, (ui) => ui.viewMode);

export const selectIsSidebarCollapsed = createSelector(
  selectCRMUI,
  (ui) => ui.sidebarCollapsed
);

export const selectIsFilterPanelOpen = createSelector(
  selectCRMUI,
  (ui) => ui.filterPanelOpen
);

export const selectIsBulkActionMode = createSelector(
  selectCRMUI,
  (ui) => ui.bulkActionMode
);

export const selectGlobalSelectedItems = createSelector(
  selectCRMUI,
  (ui) => ui.selectedItems
);

// Customer selectors
export const selectCustomerItems = createSelector(
  selectCustomers,
  (customers) => customers.items
);

export const selectCustomersLoading = createSelector(
  selectCustomers,
  (customers) => customers.loading
);

export const selectCustomersError = createSelector(
  selectCustomers,
  (customers) => customers.error
);

export const selectCustomerFilters = createSelector(
  selectCustomers,
  (customers) => customers.filters
);

export const selectCustomerPagination = createSelector(
  selectCustomers,
  (customers) => customers.pagination
);

export const selectCustomerSelectedItems = createSelector(
  selectCustomers,
  (customers) => customers.selectedItems
);

export const selectCustomerTotal = createSelector(
  selectCustomers,
  (customers) => customers.total
);

export const selectCustomerById = createSelector(
  [selectCustomerItems, (state: RootState, id: string) => id],
  (customers: Customer[], id: string) =>
    customers.find((customer: Customer) => customer.id === id)
);

export const selectActiveCustomers = createSelector(
  selectCustomerItems,
  (customers: Customer[]) =>
    customers.filter((customer: Customer) => customer.status === 'ACTIVE')
);

export const selectCustomersByType = createSelector(
  [
    selectCustomerItems,
    (state: RootState, type: Customer['customerType']) => type,
  ],
  (customers: Customer[], type: Customer['customerType']) =>
    customers.filter((customer: Customer) => customer.customerType === type)
);

// Lead selectors
export const selectLeadItems = createSelector(
  selectLeads,
  (leads) => leads.items
);

export const selectLeadsLoading = createSelector(
  selectLeads,
  (leads) => leads.loading
);

export const selectLeadsError = createSelector(
  selectLeads,
  (leads) => leads.error
);

export const selectLeadFilters = createSelector(
  selectLeads,
  (leads) => leads.filters
);

export const selectLeadPagination = createSelector(
  selectLeads,
  (leads) => leads.pagination
);

export const selectLeadSelectedItems = createSelector(
  selectLeads,
  (leads) => leads.selectedItems
);

export const selectLeadTotal = createSelector(
  selectLeads,
  (leads) => leads.total
);

export const selectLeadById = createSelector(
  [selectLeadItems, (state: RootState, id: string) => id],
  (leads: Lead[], id: string) => leads.find((lead: Lead) => lead.id === id)
);

export const selectLeadsByStatus = createSelector(
  [selectLeadItems, (state: RootState, status: Lead['status']) => status],
  (leads: Lead[], status: Lead['status']) =>
    leads.filter((lead: Lead) => lead.status === status)
);

export const selectQualifiedLeads = createSelector(
  selectLeadItems,
  (leads: Lead[]) => leads.filter((lead: Lead) => lead.status === 'QUALIFIED')
);

export const selectLeadsBySource = createSelector(
  [selectLeadItems, (state: RootState, source: Lead['source']) => source],
  (leads: Lead[], source: Lead['source']) =>
    leads.filter((lead: Lead) => lead.source === source)
);

// Opportunity selectors
export const selectOpportunityItems = createSelector(
  selectOpportunities,
  (opportunities) => opportunities.items
);

export const selectOpportunitiesLoading = createSelector(
  selectOpportunities,
  (opportunities) => opportunities.loading
);

export const selectOpportunitiesError = createSelector(
  selectOpportunities,
  (opportunities) => opportunities.error
);

export const selectOpportunityFilters = createSelector(
  selectOpportunities,
  (opportunities) => opportunities.filters
);

export const selectOpportunityPagination = createSelector(
  selectOpportunities,
  (opportunities) => opportunities.pagination
);

export const selectOpportunitySelectedItems = createSelector(
  selectOpportunities,
  (opportunities) => opportunities.selectedItems
);

export const selectOpportunityTotal = createSelector(
  selectOpportunities,
  (opportunities) => opportunities.total
);

export const selectOpportunityById = createSelector(
  [selectOpportunityItems, (state: RootState, id: string) => id],
  (opportunities: Opportunity[], id: string) =>
    opportunities.find((opportunity: Opportunity) => opportunity.id === id)
);

export const selectOpportunitiesByStage = createSelector(
  [
    selectOpportunityItems,
    (state: RootState, stage: Opportunity['stage']) => stage,
  ],
  (opportunities: Opportunity[], stage: Opportunity['stage']) =>
    opportunities.filter(
      (opportunity: Opportunity) => opportunity.stage === stage
    )
);

export const selectOpenOpportunities = createSelector(
  selectOpportunityItems,
  (opportunities: Opportunity[]) =>
    opportunities.filter(
      (opportunity: Opportunity) =>
        !['CLOSED_WON', 'CLOSED_LOST'].includes(opportunity.stage)
    )
);

export const selectClosedWonOpportunities = createSelector(
  selectOpportunityItems,
  (opportunities: Opportunity[]) =>
    opportunities.filter(
      (opportunity: Opportunity) => opportunity.stage === 'CLOSED_WON'
    )
);

export const selectOpportunitiesByCustomer = createSelector(
  [
    selectOpportunityItems,
    (state: RootState, customerId: string) => customerId,
  ],
  (opportunities: Opportunity[], customerId: string) =>
    opportunities.filter(
      (opportunity: Opportunity) => opportunity.customerId === customerId
    )
);

export const selectOpportunityPipelineValue = createSelector(
  selectOpenOpportunities,
  (opportunities: Opportunity[]) =>
    opportunities.reduce(
      (total: number, opp: Opportunity) => total + opp.value,
      0
    )
);

// Activity selectors
export const selectActivityItems = createSelector(
  selectActivities,
  (activities) => activities.items
);

export const selectActivitiesLoading = createSelector(
  selectActivities,
  (activities) => activities.loading
);

export const selectActivitiesError = createSelector(
  selectActivities,
  (activities) => activities.error
);

export const selectActivityFilters = createSelector(
  selectActivities,
  (activities) => activities.filters
);

export const selectActivityPagination = createSelector(
  selectActivities,
  (activities) => activities.pagination
);

export const selectActivitySelectedItems = createSelector(
  selectActivities,
  (activities) => activities.selectedItems
);

export const selectActivityTotal = createSelector(
  selectActivities,
  (activities) => activities.total
);

export const selectActivityById = createSelector(
  [selectActivityItems, (state: RootState, id: string) => id],
  (activities: Activity[], id: string) =>
    activities.find((activity: Activity) => activity.id === id)
);

export const selectActivitiesByType = createSelector(
  [selectActivityItems, (state: RootState, type: Activity['type']) => type],
  (activities: Activity[], type: Activity['type']) =>
    activities.filter((activity: Activity) => activity.type === type)
);

export const selectActivitiesByStatus = createSelector(
  [
    selectActivityItems,
    (state: RootState, status: Activity['status']) => status,
  ],
  (activities: Activity[], status: Activity['status']) =>
    activities.filter((activity: Activity) => activity.status === status)
);

export const selectOverdueActivities = createSelector(
  selectActivityItems,
  (activities: Activity[]) =>
    activities.filter(
      (activity: Activity) =>
        activity.status !== 'COMPLETED' &&
        activity.scheduledDate &&
        new Date(activity.scheduledDate) < new Date()
    )
);

export const selectUpcomingActivities = createSelector(
  selectActivityItems,
  (activities: Activity[]) => {
    const now = new Date();
    const nextWeek = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);

    return activities.filter(
      (activity: Activity) =>
        activity.status === 'PLANNED' &&
        activity.scheduledDate &&
        new Date(activity.scheduledDate) >= now &&
        new Date(activity.scheduledDate) <= nextWeek
    );
  }
);

export const selectActivitiesByRelatedEntity = createSelector(
  [
    selectActivityItems,
    (
      state: RootState,
      entityType: Activity['relatedTo']['type'],
      entityId: string
    ) => ({ entityType, entityId }),
  ],
  (
    activities: Activity[],
    {
      entityType,
      entityId,
    }: { entityType: Activity['relatedTo']['type']; entityId: string }
  ) =>
    activities.filter(
      (activity: Activity) =>
        activity.relatedTo.type === entityType &&
        activity.relatedTo.id === entityId
    )
);

// Cross-entity selectors
export const selectRecentActivitiesByCustomer = createSelector(
  [selectActivityItems, (state: RootState, customerId: string) => customerId],
  (activities: Activity[], customerId: string) => {
    const customerActivities = activities.filter(
      (activity: Activity) =>
        (activity.relatedTo.type === 'CUSTOMER' &&
          activity.relatedTo.id === customerId) ||
        (activity.relatedTo.type === 'OPPORTUNITY' &&
          // We'd need to also check if opportunity belongs to customer
          // This would require joining with opportunities data
          false)
    );

    return customerActivities
      .sort(
        (a: Activity, b: Activity) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      )
      .slice(0, 5);
  }
);

// Dashboard metrics selectors
export const selectCRMDashboardMetrics = createSelector(
  [
    selectCustomerItems,
    selectLeadItems,
    selectOpportunityItems,
    selectActivityItems,
  ],
  (
    customers: Customer[],
    leads: Lead[],
    opportunities: Opportunity[],
    activities: Activity[]
  ) => {
    const activeCustomers = customers.filter(
      (c: Customer) => c.status === 'ACTIVE'
    );
    const activeLeads = leads.filter(
      (l: Lead) => l.status !== 'CONVERTED' && l.status !== 'LOST'
    );
    const openOpportunities = opportunities.filter(
      (o: Opportunity) => !['CLOSED_WON', 'CLOSED_LOST'].includes(o.stage)
    );
    const overdueActivities = activities.filter(
      (a: Activity) =>
        a.status !== 'COMPLETED' &&
        a.scheduledDate &&
        new Date(a.scheduledDate) < new Date()
    );

    return {
      totalCustomers: activeCustomers.length,
      activeLeads: activeLeads.length,
      openOpportunities: openOpportunities.length,
      totalPipelineValue: openOpportunities.reduce(
        (sum: number, o: Opportunity) => sum + o.value,
        0
      ),
      overdueActivities: overdueActivities.length,
    };
  }
);

// Search selectors
export const selectSearchResults = createSelector(
  [
    selectCustomerItems,
    selectLeadItems,
    selectOpportunityItems,
    selectActivityItems,
    (state: RootState, searchTerm: string) => searchTerm,
  ],
  (
    customers: Customer[],
    leads: Lead[],
    opportunities: Opportunity[],
    activities: Activity[],
    searchTerm: string
  ) => {
    if (!searchTerm.trim())
      return { customers: [], leads: [], opportunities: [], activities: [] };

    const term = searchTerm.toLowerCase();

    const filteredCustomers = customers.filter(
      (c: Customer) =>
        c.name.toLowerCase().includes(term) ||
        c.email.toLowerCase().includes(term)
    );

    const filteredLeads = leads.filter(
      (l: Lead) =>
        `${l.firstName} ${l.lastName}`.toLowerCase().includes(term) ||
        l.email.toLowerCase().includes(term) ||
        (l.company && l.company.toLowerCase().includes(term))
    );

    const filteredOpportunities = opportunities.filter(
      (o: Opportunity) =>
        o.name.toLowerCase().includes(term) ||
        o.customerName.toLowerCase().includes(term)
    );

    const filteredActivities = activities.filter(
      (a: Activity) =>
        a.subject.toLowerCase().includes(term) ||
        (a.description && a.description.toLowerCase().includes(term))
    );

    return {
      customers: filteredCustomers,
      leads: filteredLeads,
      opportunities: filteredOpportunities,
      activities: filteredActivities,
    };
  }
);
