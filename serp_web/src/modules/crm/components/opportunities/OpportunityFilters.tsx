// Author: QuanTuanHuy, Description: Part of Serp Project

'use client';

import {
  Button,
  Card,
  CardContent,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import type { OpportunityStage } from '../../types';

interface OpportunityFiltersProps {
  stageFilter: OpportunityStage | 'ALL';
  onStageFilterChange: (value: OpportunityStage | 'ALL') => void;
  sortBy: 'name' | 'createdAt' | 'value';
  sortOrder: 'asc' | 'desc';
  onSortChange: (sortBy: string, sortOrder: string) => void;
  hasActiveFilters: boolean;
  onClearFilters: () => void;
  resultCount: number;
}

export const OpportunityFilters: React.FC<OpportunityFiltersProps> = ({
  stageFilter,
  onStageFilterChange,
  sortBy,
  sortOrder,
  onSortChange,
  hasActiveFilters,
  onClearFilters,
  resultCount,
}) => {
  return (
    <Card>
      <CardContent className='p-4'>
        <div className='grid grid-cols-1 gap-4 sm:grid-cols-2'>
          <div className='space-y-2'>
            <Label>Stage</Label>
            <Select
              value={stageFilter}
              onValueChange={(value) => {
                onStageFilterChange(value as OpportunityStage | 'ALL');
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder='All stages' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All Stages</SelectItem>
                <SelectItem value='PROSPECTING'>Prospecting</SelectItem>
                <SelectItem value='QUALIFICATION'>Qualification</SelectItem>
                <SelectItem value='PROPOSAL'>Proposal</SelectItem>
                <SelectItem value='NEGOTIATION'>Negotiation</SelectItem>
                <SelectItem value='CLOSED_WON'>Closed Won</SelectItem>
                <SelectItem value='CLOSED_LOST'>Closed Lost</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label>Sort By</Label>
            <Select
              value={`${sortBy}-${sortOrder}`}
              onValueChange={(value) => {
                const [field, order] = value.split('-');
                onSortChange(field, order);
              }}
            >
              <SelectTrigger>
                <SelectValue placeholder='Sort by' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='createdAt-desc'>Newest First</SelectItem>
                <SelectItem value='createdAt-asc'>Oldest First</SelectItem>
                <SelectItem value='name-asc'>Name A-Z</SelectItem>
                <SelectItem value='name-desc'>Name Z-A</SelectItem>
                <SelectItem value='value-desc'>Highest Value</SelectItem>
                <SelectItem value='value-asc'>Lowest Value</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {hasActiveFilters && (
          <div className='mt-4 flex items-center justify-between border-t pt-4'>
            <p className='text-sm text-muted-foreground'>
              {resultCount} results found
            </p>
            <Button variant='ghost' size='sm' onClick={onClearFilters}>
              Clear all filters
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
