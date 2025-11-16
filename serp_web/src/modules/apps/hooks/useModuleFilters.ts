/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module filters hook
 */

import { useState, useMemo, useCallback } from 'react';
import type { Module, ModuleFilterState } from '../types';

export const useModuleFilters = (modules: Module[] = []) => {
  const [filters, setFilters] = useState<ModuleFilterState>({
    search: '',
  });

  const filteredModules = useMemo(() => {
    return modules.filter((module) => {
      if (filters.search) {
        const searchLower = filters.search.toLowerCase();
        const matchesSearch =
          module.moduleName.toLowerCase().includes(searchLower) ||
          module.code.toLowerCase().includes(searchLower) ||
          module.description?.toLowerCase().includes(searchLower);

        if (!matchesSearch) return false;
      }

      if (filters.category && module.category !== filters.category) {
        return false;
      }

      if (
        filters.pricingModel &&
        module.pricingModel !== filters.pricingModel
      ) {
        return false;
      }

      if (filters.status && module.status !== filters.status) {
        return false;
      }

      return true;
    });
  }, [modules, filters]);

  const updateFilter = useCallback(
    (key: keyof ModuleFilterState, value: string | undefined) => {
      setFilters((prev) => ({
        ...prev,
        [key]: value,
      }));
    },
    []
  );

  const clearFilters = useCallback(() => {
    setFilters({
      search: '',
    });
  }, []);

  const categories = useMemo(() => {
    const categorySet = new Set(
      modules.filter((m) => m.category).map((m) => m.category!)
    );
    return Array.from(categorySet).sort();
  }, [modules]);

  return {
    filters,
    filteredModules,
    updateFilter,
    clearFilters,
    categories,
    hasActiveFilters:
      !!filters.search ||
      !!filters.category ||
      !!filters.pricingModel ||
      !!filters.status,
  };
};
