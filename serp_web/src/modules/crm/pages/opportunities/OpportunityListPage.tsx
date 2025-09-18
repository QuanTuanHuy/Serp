// OpportunityListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
import { Button, Card, CardContent, CardHeader } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetOpportunitiesQuery } from '../../api/crmApi';
import { EntityCard, SearchInput, StatusBadge } from '../../components/shared';
import type {
  Opportunity,
  OpportunityFilters,
  OpportunityStage,
} from '../../types';

interface OpportunityListPageProps {
  className?: string;
}

export const OpportunityListPage: React.FC<OpportunityListPageProps> = ({
  className,
}) => {
  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [stageFilter, setStageFilter] = useState<OpportunityStage | ''>('');
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'value'>(
    'createdAt'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);

  const pageSize = 12;

  // Build filters
  const filters: OpportunityFilters = useMemo(
    () => ({
      search: searchQuery || undefined,
      stage: stageFilter ? [stageFilter] : undefined,
    }),
    [searchQuery, stageFilter]
  );

  // Fetch opportunities
  const {
    data: opportunitiesResponse,
    isLoading,
    error,
  } = useGetOpportunitiesQuery({
    filters,
    pagination: { page: currentPage, limit: pageSize, sortBy, sortOrder },
  });

  const opportunities = opportunitiesResponse?.data?.data || [];
  const totalPages = opportunitiesResponse?.data?.pagination?.totalPages || 0;
  const total = opportunitiesResponse?.data?.pagination?.total || 0;

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(1);
  };

  return (
    <div className={cn('p-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <div>
          <h1 className='text-2xl font-bold'>Opportunities</h1>
          <p className='text-muted-foreground'>Manage your sales pipeline</p>
        </div>
        <Button>Add Opportunity</Button>
      </div>

      {/* Pipeline Overview */}
      <div className='grid grid-cols-1 md:grid-cols-5 gap-4 mb-6'>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-blue-600'>{total}</div>
            <p className='text-sm text-muted-foreground'>Total</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-yellow-600'>
              {opportunities.filter((o) => o.stage === 'PROSPECTING').length}
            </div>
            <p className='text-sm text-muted-foreground'>Prospecting</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-orange-600'>
              {opportunities.filter((o) => o.stage === 'PROPOSAL').length}
            </div>
            <p className='text-sm text-muted-foreground'>Proposal</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-purple-600'>
              {opportunities.filter((o) => o.stage === 'NEGOTIATION').length}
            </div>
            <p className='text-sm text-muted-foreground'>Negotiation</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-green-600'>
              {opportunities.filter((o) => o.stage === 'CLOSED_WON').length}
            </div>
            <p className='text-sm text-muted-foreground'>Won</p>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card className='mb-6'>
        <CardHeader>
          <h3 className='text-lg font-semibold'>Filters</h3>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
            <div>
              <SearchInput
                placeholder='Search opportunities...'
                onSearch={handleSearch}
                value={searchQuery}
              />
            </div>

            <div>
              <select
                value={stageFilter}
                onChange={(e) => {
                  setStageFilter(e.target.value as OpportunityStage | '');
                  setCurrentPage(1);
                }}
                className='w-full px-3 py-2 border border-border rounded-md bg-background focus:outline-none focus:ring-2 focus:ring-ring'
              >
                <option value=''>All Stages</option>
                <option value='PROSPECTING'>Prospecting</option>
                <option value='QUALIFICATION'>Qualification</option>
                <option value='PROPOSAL'>Proposal</option>
                <option value='NEGOTIATION'>Negotiation</option>
                <option value='CLOSED_WON'>Closed Won</option>
                <option value='CLOSED_LOST'>Closed Lost</option>
              </select>
            </div>

            <div>
              <select
                value={`${sortBy}-${sortOrder}`}
                onChange={(e) => {
                  const [field, order] = e.target.value.split('-');
                  setSortBy(field as typeof sortBy);
                  setSortOrder(order as 'asc' | 'desc');
                  setCurrentPage(1);
                }}
                className='w-full px-3 py-2 border border-border rounded-md bg-background focus:outline-none focus:ring-2 focus:ring-ring'
              >
                <option value='createdAt-desc'>Newest First</option>
                <option value='createdAt-asc'>Oldest First</option>
                <option value='name-asc'>Name A-Z</option>
                <option value='name-desc'>Name Z-A</option>
                <option value='value-desc'>Highest Value</option>
                <option value='value-asc'>Lowest Value</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Opportunity Grid */}
      {!isLoading && opportunities.length > 0 && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {opportunities.map((opportunity) => (
            <EntityCard
              key={opportunity.id}
              type='opportunity'
              entity={opportunity}
              onClick={() => {
                console.log('View opportunity:', opportunity.id);
              }}
              className='h-full'
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && opportunities.length === 0 && !error && (
        <Card>
          <CardContent className='p-12 text-center'>
            <div className='mx-auto w-24 h-24 bg-muted rounded-full flex items-center justify-center mb-4'>
              <svg
                className='w-12 h-12 text-muted-foreground'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth='2'
                  d='M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z'
                />
              </svg>
            </div>
            <h3 className='text-lg font-semibold mb-2'>
              No opportunities found
            </h3>
            <p className='text-muted-foreground mb-4'>
              Get started by creating your first opportunity.
            </p>
            <Button>Create First Opportunity</Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default OpportunityListPage;
