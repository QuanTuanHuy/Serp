/**
 * Author: Tran Ngoc Hung
 * Description: Part of Serp Project - TTCRS sidebar collapse state context
 */

'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';

interface TtcrsSidebarContextType {
  isCollapsed: boolean;
  toggleSidebar: () => void;
  collapseSidebar: () => void;
  expandSidebar: () => void;
}

const TtcrsSidebarContext = createContext<TtcrsSidebarContextType | undefined>(
  undefined
);

const STORAGE_KEY = 'ttcrs-sidebar-collapsed';

export const TtcrsSidebarProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(false);

  // Restore from localStorage on mount
  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored !== null) setIsCollapsed(stored === 'true');
  }, []);

  const persist = (value: boolean) =>
    localStorage.setItem(STORAGE_KEY, String(value));

  const toggleSidebar = () =>
    setIsCollapsed((prev) => { persist(!prev); return !prev; });

  const collapseSidebar = () => { setIsCollapsed(true);  persist(true);  };
  const expandSidebar   = () => { setIsCollapsed(false); persist(false); };

  return (
    <TtcrsSidebarContext.Provider
      value={{ isCollapsed, toggleSidebar, collapseSidebar, expandSidebar }}
    >
      {children}
    </TtcrsSidebarContext.Provider>
  );
};

export const useTtcrsSidebar = () => {
  const ctx = useContext(TtcrsSidebarContext);
  if (!ctx) {
    throw new Error('useTtcrsSidebar must be used within TtcrsSidebarProvider');
  }
  return ctx;
};
