/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Profile sidebar state context
 */

'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';

interface ProfileSidebarContextType {
  isCollapsed: boolean;
  toggleSidebar: () => void;
  collapseSidebar: () => void;
  expandSidebar: () => void;
}

const ProfileSidebarContext = createContext<
  ProfileSidebarContextType | undefined
>(undefined);

const SIDEBAR_STORAGE_KEY = 'profile-sidebar-collapsed';

export const ProfileSidebarProvider: React.FC<{
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
    <ProfileSidebarContext.Provider
      value={{ isCollapsed, toggleSidebar, collapseSidebar, expandSidebar }}
    >
      {children}
    </ProfileSidebarContext.Provider>
  );
};

export const useProfileSidebar = () => {
  const context = useContext(ProfileSidebarContext);
  if (!context) {
    throw new Error(
      'useProfileSidebar must be used within ProfileSidebarProvider'
    );
  }
  return context;
};
