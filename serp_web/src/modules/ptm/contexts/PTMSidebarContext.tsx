/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PTM sidebar state context
 */

'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';

interface PTMSidebarContextType {
  isCollapsed: boolean;
  toggleSidebar: () => void;
  collapseSidebar: () => void;
  expandSidebar: () => void;
}

const PTMSidebarContext = createContext<PTMSidebarContextType | undefined>(
  undefined
);

const SIDEBAR_STORAGE_KEY = 'ptm-sidebar-collapsed';

export const PTMSidebarProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(false);

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
    <PTMSidebarContext.Provider
      value={{ isCollapsed, toggleSidebar, collapseSidebar, expandSidebar }}
    >
      {children}
    </PTMSidebarContext.Provider>
  );
};

export const usePTMSidebar = () => {
  const context = useContext(PTMSidebarContext);
  if (!context) {
    throw new Error('usePTMSidebar must be used within PTMSidebarProvider');
  }
  return context;
};
