// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import { cn } from '@/shared/utils';
import { OpportunityCard } from '../cards';
import type { Opportunity } from '../../types';

interface OpportunityGridViewProps {
  opportunities: Opportunity[];
  viewMode: 'grid' | 'list';
  onViewOpportunity: (opportunityId: string) => void;
  onDeleteOpportunity: (opportunityId: string) => void;
}

export const OpportunityGridView: React.FC<OpportunityGridViewProps> = ({
  opportunities,
  viewMode,
  onViewOpportunity,
  onDeleteOpportunity,
}) => {
  return (
    <div
      className={cn(
        'gap-4',
        viewMode === 'grid'
          ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
          : 'flex flex-col'
      )}
    >
      {opportunities.map((opportunity) => (
        <OpportunityCard
          key={opportunity.id}
          opportunity={opportunity}
          variant={viewMode === 'list' ? 'compact' : 'default'}
          onClick={() => onViewOpportunity(opportunity.id)}
          onDelete={() => onDeleteOpportunity(opportunity.id)}
        />
      ))}
    </div>
  );
};
