/**
 * PTM v2 - UI State Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UIState {
  sidebarOpen: boolean;
  sidebarCollapsed: boolean;
  activeView: 'dashboard' | 'tasks' | 'projects' | 'schedule' | 'analytics';
  commandPaletteOpen: boolean;
  quickAddOpen: boolean;
  selectedTaskId: string | null;
  selectedProjectId: string | null;
}

const initialState: UIState = {
  sidebarOpen: true,
  sidebarCollapsed: false,
  activeView: 'dashboard',
  commandPaletteOpen: false,
  quickAddOpen: false,
  selectedTaskId: null,
  selectedProjectId: null,
};

const uiSlice = createSlice({
  name: 'ptmUI',
  initialState,
  reducers: {
    toggleSidebar: (state) => {
      state.sidebarOpen = !state.sidebarOpen;
    },
    collapseSidebar: (state, action: PayloadAction<boolean>) => {
      state.sidebarCollapsed = action.payload;
    },
    setActiveView: (state, action: PayloadAction<UIState['activeView']>) => {
      state.activeView = action.payload;
    },
    openCommandPalette: (state) => {
      state.commandPaletteOpen = true;
    },
    closeCommandPalette: (state) => {
      state.commandPaletteOpen = false;
    },
    toggleQuickAdd: (state) => {
      state.quickAddOpen = !state.quickAddOpen;
    },
    selectTask: (state, action: PayloadAction<string | null>) => {
      state.selectedTaskId = action.payload;
    },
    selectProject: (state, action: PayloadAction<string | null>) => {
      state.selectedProjectId = action.payload;
    },
  },
});

export const {
  toggleSidebar,
  collapseSidebar,
  setActiveView,
  openCommandPalette,
  closeCommandPalette,
  toggleQuickAdd,
  selectTask,
  selectProject,
} = uiSlice.actions;

// Selectors
export const selectUI = (state: { ptm: { ui: UIState } }) => state.ptm.ui;
export const selectActiveView = (state: { ptm: { ui: UIState } }) =>
  state.ptm.ui.activeView;
export const selectSidebarState = (state: { ptm: { ui: UIState } }) => ({
  open: state.ptm.ui.sidebarOpen,
  collapsed: state.ptm.ui.sidebarCollapsed,
});

export { uiSlice };
export default uiSlice.reducer;
