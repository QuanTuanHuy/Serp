// Author: QuanTuanHuy, Description: Part of Serp Project

import { useMemo } from 'react';
import type { Opportunity, OpportunityStage } from '../types';

export const usePipelineGrouping = (opportunities: Opportunity[]) => {
  const opportunitiesByStage = useMemo(() => {
    const grouped: Record<OpportunityStage, Opportunity[]> = {
      PROSPECTING: [],
      QUALIFICATION: [],
      PROPOSAL: [],
      NEGOTIATION: [],
      CLOSED_WON: [],
      CLOSED_LOST: [],
    };

    opportunities.forEach((opportunity) => {
      grouped[opportunity.stage].push(opportunity);
    });

    return grouped;
  }, [opportunities]);

  const stageValues = useMemo(() => {
    const values: Record<OpportunityStage, number> = {
      PROSPECTING: 0,
      QUALIFICATION: 0,
      PROPOSAL: 0,
      NEGOTIATION: 0,
      CLOSED_WON: 0,
      CLOSED_LOST: 0,
    };

    opportunities.forEach((opportunity) => {
      values[opportunity.stage] +=
        opportunity.estimatedValue ?? opportunity.value ?? 0;
    });

    return values;
  }, [opportunities]);

  const opportunityMap = useMemo(
    () =>
      new Map(
        opportunities.map((opportunity) => [opportunity.id, opportunity])
      ),
    [opportunities]
  );

  return {
    opportunitiesByStage,
    stageValues,
    opportunityMap,
  };
};
