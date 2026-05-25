// Author: QuanTuanHuy, Description: Part of Serp Project

import { useMemo } from 'react';
import { useDebounce } from '@/shared/hooks/use-debounce';
import {
  useGetOpportunitiesQuery,
  useGetOpportunityPipelineQuery,
} from '../api/crmApi';
import type { OpportunityStage } from '../types';

interface UseOpportunityDataParams {
  searchQuery: string;
  stageFilter: OpportunityStage | 'ALL';
  sortBy: 'name' | 'createdAt' | 'value';
  sortOrder: 'asc' | 'desc';
  currentPage: number;
  pageSize: number;
  viewMode: 'grid' | 'list' | 'pipeline';
}

export const useOpportunityData = ({
  searchQuery,
  stageFilter,
  sortBy,
  sortOrder,
  currentPage,
  pageSize,
  viewMode,
}: UseOpportunityDataParams) => {
  const debouncedSearchQuery = useDebounce(searchQuery, 400);

  const {
    data: opportunitiesResponse,
    isLoading: isLoadingOpportunities,
    error,
  } = useGetOpportunitiesQuery({
    filters: {
      search: debouncedSearchQuery || undefined,
      stage: stageFilter !== 'ALL' ? [stageFilter] : undefined,
    },
    pagination: {
      page: currentPage,
      limit: pageSize,
      sortBy: sortBy === 'value' ? 'estimatedValue' : sortBy,
      sortOrder,
    },
  });

  const {
    data: pipelineResponse,
    isLoading: isLoadingPipeline,
    error: pipelineError,
  } = useGetOpportunityPipelineQuery({});

  const listOpportunities = opportunitiesResponse?.data?.data || [];
  const total = opportunitiesResponse?.data?.pagination?.total || 0;
  const totalPages = opportunitiesResponse?.data?.pagination?.totalPages || 1;

  const pipelineStages = pipelineResponse?.data?.stages || [];
  const pipelineSummary = pipelineResponse?.data?.summary;

  const pipelineOpportunities = useMemo(() => {
    let result = pipelineStages.flatMap((stage) => stage.opportunities || []);

    if (debouncedSearchQuery) {
      const query = debouncedSearchQuery.toLowerCase();
      result = result.filter(
        (opp) =>
          opp.name.toLowerCase().includes(query) ||
          (opp.customerName && opp.customerName.toLowerCase().includes(query))
      );
    }

    if (stageFilter !== 'ALL') {
      result = result.filter((opp) => opp.stage === stageFilter);
    }

    return result;
  }, [pipelineStages, debouncedSearchQuery, stageFilter]);

  const stats = useMemo(() => {
    if (!pipelineSummary) {
      return {
        total: total,
        totalValue: 0,
        weightedValue: 0,
        wonCount: 0,
        wonValue: 0,
        avgDealSize: 0,
      };
    }

    const wonStage = pipelineStages.find(
      (stage) => stage.stage === 'CLOSED_WON'
    );

    return {
      total: pipelineSummary.totalOpportunities,
      totalValue: pipelineSummary.totalPipelineValue,
      weightedValue: pipelineSummary.weightedPipelineValue,
      wonCount: wonStage?.count || 0,
      wonValue: wonStage?.totalValue || 0,
      avgDealSize: pipelineSummary.averageDealSize,
    };
  }, [pipelineSummary, pipelineStages, total]);

  const isLoading =
    viewMode === 'pipeline' ? isLoadingPipeline : isLoadingOpportunities;
  const activeError = viewMode === 'pipeline' ? pipelineError : error;
  const activeOpportunities =
    viewMode === 'pipeline' ? pipelineOpportunities : listOpportunities;

  return {
    listOpportunities,
    pipelineOpportunities,
    total,
    totalPages,
    stats,
    isLoading,
    activeError,
    activeOpportunities,
  };
};
