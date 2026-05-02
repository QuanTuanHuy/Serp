// Author: QuanTuanHuy, Description: Part of Serp Project

import { useState, useCallback } from 'react';
import type { OpportunityStage } from '../types';

interface UseOpportunityFiltersReturn {
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  stageFilter: OpportunityStage | 'ALL';
  setStageFilter: (stage: OpportunityStage | 'ALL') => void;
  sortBy: 'name' | 'createdAt' | 'value';
  setSortBy: (sort: 'name' | 'createdAt' | 'value') => void;
  sortOrder: 'asc' | 'desc';
  setSortOrder: (order: 'asc' | 'desc') => void;
  currentPage: number;
  setCurrentPage: (page: number) => void;
  clearFilters: () => void;
  hasActiveFilters: boolean;
}

export const useOpportunityFilters = (): UseOpportunityFiltersReturn => {
  const [searchQuery, setSearchQuery] = useState('');
  const [stageFilter, setStageFilter] = useState<OpportunityStage | 'ALL'>(
    'ALL'
  );
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'value'>(
    'createdAt'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);

  const clearFilters = useCallback(() => {
    setSearchQuery('');
    setStageFilter('ALL');
    setCurrentPage(1);
  }, []);

  const hasActiveFilters = Boolean(searchQuery) || stageFilter !== 'ALL';

  return {
    searchQuery,
    setSearchQuery,
    stageFilter,
    setStageFilter,
    sortBy,
    setSortBy,
    sortOrder,
    setSortOrder,
    currentPage,
    setCurrentPage,
    clearFilters,
    hasActiveFilters,
  };
};
