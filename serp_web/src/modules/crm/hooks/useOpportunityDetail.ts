// Author: QuanTuanHuy, Description: Part of Serp Project

import { useMemo } from 'react';
import {
  useGetOpportunityQuery,
  useGetOpportunityActivitiesQuery,
} from '../api/crmApi';
import {
  PIPELINE_STAGES,
  OPPORTUNITY_TYPES,
  calculateDaysInPipeline,
  calculateDaysUntilClose,
} from '../utils';

export const useOpportunityDetail = (opportunityId: string) => {
  const { data, isLoading } = useGetOpportunityQuery(opportunityId);
  const { data: activitiesData, isLoading: isActivitiesLoading } =
    useGetOpportunityActivitiesQuery({ opportunityId, page: 1, size: 20 });

  const opportunity = data?.data;
  const activities = activitiesData?.data?.data || [];

  const stageConfig = useMemo(
    () => PIPELINE_STAGES.find((stage) => stage.stage === opportunity?.stage),
    [opportunity?.stage]
  );

  const typeConfig = useMemo(
    () => OPPORTUNITY_TYPES[opportunity?.type || 'NEW_BUSINESS'],
    [opportunity?.type]
  );

  const isClosed = useMemo(
    () =>
      opportunity?.stage === 'CLOSED_WON' ||
      opportunity?.stage === 'CLOSED_LOST',
    [opportunity?.stage]
  );

  const canReopen = useMemo(
    () => opportunity?.stage === 'CLOSED_LOST',
    [opportunity?.stage]
  );

  const probability = useMemo(
    () => opportunity?.probability ?? stageConfig?.probability ?? 0,
    [opportunity?.probability, stageConfig?.probability]
  );

  const estimatedValue = useMemo(
    () => opportunity?.estimatedValue ?? opportunity?.value ?? 0,
    [opportunity?.estimatedValue, opportunity?.value]
  );

  const weightedValue = useMemo(
    () => (estimatedValue * probability) / 100,
    [estimatedValue, probability]
  );

  const daysInPipeline = useMemo(
    () => calculateDaysInPipeline(opportunity?.createdAt),
    [opportunity?.createdAt]
  );

  const daysUntilClose = useMemo(
    () => calculateDaysUntilClose(opportunity?.expectedCloseDate),
    [opportunity?.expectedCloseDate]
  );

  return {
    opportunity,
    activities,
    isLoading,
    isActivitiesLoading,
    stageConfig,
    typeConfig,
    isClosed,
    canReopen,
    probability,
    estimatedValue,
    weightedValue,
    daysInPipeline,
    daysUntilClose,
  };
};
