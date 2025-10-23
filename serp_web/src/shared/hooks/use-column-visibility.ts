/*
Author: QuanTuanHuy
Description: Part of Serp Project - Hook for managing column visibility with localStorage persistence
*/

import { useState, useEffect, useCallback } from 'react';
import { ColumnDef, ColumnVisibility } from '../types/datatable.types';

export function useColumnVisibility(columns: ColumnDef[], storageKey?: string) {
  // Initialize visibility from columns default or localStorage
  const [visibility, setVisibility] = useState<ColumnVisibility>(() => {
    if (storageKey) {
      const saved = localStorage.getItem(storageKey);
      if (saved) {
        try {
          return JSON.parse(saved);
        } catch (e) {
          console.error(
            'Failed to parse column visibility from localStorage',
            e
          );
        }
      }
    }

    // Default visibility from column definitions
    return columns.reduce((acc, col) => {
      acc[col.id] = col.defaultVisible !== false; // Default to visible if not specified
      return acc;
    }, {} as ColumnVisibility);
  });

  // Save to localStorage whenever visibility changes
  useEffect(() => {
    if (storageKey) {
      localStorage.setItem(storageKey, JSON.stringify(visibility));
    }
  }, [visibility, storageKey]);

  const toggleVisibility = useCallback((columnId: string) => {
    setVisibility((prev) => ({
      ...prev,
      [columnId]: !prev[columnId],
    }));
  }, []);

  const setColumnVisibility = useCallback(
    (columnId: string, visible: boolean) => {
      setVisibility((prev) => ({
        ...prev,
        [columnId]: visible,
      }));
    },
    []
  );

  const resetVisibility = useCallback(() => {
    const defaultVisibility = columns.reduce((acc, col) => {
      acc[col.id] = col.defaultVisible !== false;
      return acc;
    }, {} as ColumnVisibility);
    setVisibility(defaultVisibility);
  }, [columns]);

  const getVisibleColumns = useCallback(() => {
    return columns.filter((col) => visibility[col.id] !== false);
  }, [columns, visibility]);

  const hasHiddenColumns = useCallback(() => {
    return columns.some((col) => visibility[col.id] === false);
  }, [columns, visibility]);

  return {
    visibility,
    toggleVisibility,
    setColumnVisibility,
    resetVisibility,
    getVisibleColumns,
    hasHiddenColumns,
  };
}
