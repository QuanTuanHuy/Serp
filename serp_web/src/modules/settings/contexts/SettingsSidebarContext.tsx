/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings sidebar state context
 */

'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';

interface SettingsSidebarContextType {
  isCollapsed: boolean;
  toggleSidebar: () => void;
  collapseSidebar: () => void;
  expandSidebar: () => void;
}

const SettingsSidebarContext = createContext<
  SettingsSidebarContextType | undefined
>(undefined);

const SIDEBAR_STORAGE_KEY = 'settings-sidebar-collapsed';

export const SettingsSidebarProvider: React.FC<{
  children: React.ReactNode;
}> = ({ children }) => {
  const [isCollapsed, setIsCollapsed] = useState(false);

  // Load sidebar state from localStorage on mount
  useEffect(() => {
    const stored = localStorage.getItem(SIDEBAR_STORAGE_KEY);
    if (stored !== null) {
      setIsCollapsed(stored === 'true');
    }
  }, []);

  const toggleSidebar = () => {
    setIsCollapsed((prev) => {
      const newState = !prev;
      localStorage.setItem(SIDEBAR_STORAGE_KEY, String(newState));
      return newState;
    });
  };

  const collapseSidebar = () => {
    setIsCollapsed(true);
    localStorage.setItem(SIDEBAR_STORAGE_KEY, 'true');
  };

  const expandSidebar = () => {
    setIsCollapsed(false);
    localStorage.setItem(SIDEBAR_STORAGE_KEY, 'false');
  };

  return (
    <SettingsSidebarContext.Provider
      value={{ isCollapsed, toggleSidebar, collapseSidebar, expandSidebar }}
    >
      {children}
    </SettingsSidebarContext.Provider>
  );
};

export const useSettingsSidebar = () => {
  const context = useContext(SettingsSidebarContext);
  if (!context) {
    throw new Error(
      'useSettingsSidebar must be used within SettingsSidebarProvider'
    );
  }
  return context;
};
