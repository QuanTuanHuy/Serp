# DynamicSidebar Collapse State Persistence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist the expand/collapse state of the `DynamicSidebar` to browser `localStorage` with try-catch block wrapping for robust handling.

**Architecture:** We will modify the `SidebarProvider` React context to load the initial collapsed/expanded state from client-side `localStorage` via a `useEffect` hook, and wrap all read/write accesses to storage in `try-catch` blocks to protect against private browsing limitations.

**Tech Stack:** React, Next.js, localStorage API

---

### Task 1: Update SidebarContext.tsx

**Files:**
- Modify: `serp_web/src/shared/components/DynamicSidebar/SidebarContext.tsx`

- [ ] **Step 1: Replace implementation of SidebarContext.tsx**

Update `SidebarProvider` to use a persistent key `serp-dynamic-sidebar-collapsed`, load state on client-side mount using `useEffect`, and safely wrap `localStorage` actions in try-catch.

Replace the contents of `d:\User2\open_source\serp\serp_web\src\shared\components\DynamicSidebar\SidebarContext.tsx` with:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Sidebar Context for managing collapse state with localStorage persistence
 */

'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';

interface SidebarContextType {
  isCollapsed: boolean;
  setIsCollapsed: (value: boolean) => void;
  toggleSidebar: () => void;
}

const SidebarContext = createContext<SidebarContextType | undefined>(undefined);

export const useSidebarContext = () => {
  const context = useContext(SidebarContext);
  if (!context) {
    throw new Error('useSidebarContext must be used within SidebarProvider');
  }
  return context;
};

interface SidebarProviderProps {
  children: React.ReactNode;
}

const SIDEBAR_STORAGE_KEY = 'serp-dynamic-sidebar-collapsed';

export const SidebarProvider: React.FC<SidebarProviderProps> = ({
  children,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(false);

  // Restore sidebar state from localStorage on client mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem(SIDEBAR_STORAGE_KEY);
      if (stored !== null) {
        setIsCollapsed(stored === 'true');
      }
    } catch (error) {
      console.warn('Failed to read sidebar state from localStorage:', error);
    }
  }, []);

  const handleSetIsCollapsed = (value: boolean) => {
    setIsCollapsed(value);
    try {
      localStorage.setItem(SIDEBAR_STORAGE_KEY, String(value));
    } catch (error) {
      console.warn('Failed to save sidebar state to localStorage:', error);
    }
  };

  const toggleSidebar = () => {
    setIsCollapsed((prev) => {
      const newState = !prev;
      try {
        localStorage.setItem(SIDEBAR_STORAGE_KEY, String(newState));
      } catch (error) {
        console.warn('Failed to save sidebar state to localStorage:', error);
      }
      return newState;
    });
  };

  return (
    <SidebarContext.Provider
      value={{
        isCollapsed,
        setIsCollapsed: handleSetIsCollapsed,
        toggleSidebar,
      }}
    >
      {children}
    </SidebarContext.Provider>
  );
};
```

- [ ] **Step 2: Verify type correctness**

Run the typescript type-checking command to ensure there are no compilation or type errors.

Cwd: `d:\User2\open_source\serp\serp_web`
Run: `npm run type-check`
Expected output: No errors, command exits with code 0.

- [ ] **Step 3: Verify linting and formatting**

Run formatting and linting check on `serp_web`.

Cwd: `d:\User2\open_source\serp\serp_web`
Run: `npm run lint`
Expected output: No ESLint errors.

- [ ] **Step 4: Commit changes**

Cwd: `d:\User2\open_source\serp`
Run:
```bash
git add serp_web/src/shared/components/DynamicSidebar/SidebarContext.tsx
git commit -m "feat(sidebar): persist dynamic sidebar collapse state to localStorage with try-catch"
```
