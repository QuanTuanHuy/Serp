// UI State Types (authors: QuanTuanHuy, Description: Part of Serp Project)

export interface CRMUIState {
  activeModule:
    | 'customers'
    | 'leads'
    | 'opportunities'
    | 'activities'
    | 'analytics';
  selectedItems: string[];
  bulkActionMode: boolean;
  viewMode: 'list' | 'grid' | 'kanban' | 'calendar';
  sidebarCollapsed: boolean;
  filterPanelOpen: boolean;
}
