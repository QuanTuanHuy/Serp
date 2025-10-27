/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM sidebar state context
 */

'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';

interface CRMSidebarContextType {
  isCollapsed: boolean;
  toggleSidebar: () => void;
  collapseSidebar: () => void;
  expandSidebar: () => void;
}

const CRMSidebarContext = createContext<CRMSidebarContextType | undefined>(
  undefined
);

const SIDEBAR_STORAGE_KEY = 'crm-sidebar-collapsed';

export const CRMSidebarProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
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
    <CRMSidebarContext.Provider
      value={{ isCollapsed, toggleSidebar, collapseSidebar, expandSidebar }}
    >
      {children}
    </CRMSidebarContext.Provider>
  );
};

export const useCRMSidebar = () => {
  const context = useContext(CRMSidebarContext);
  if (!context) {
    throw new Error('useCRMSidebar must be used within CRMSidebarProvider');
  }
  return context;
};
