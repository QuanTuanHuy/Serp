// OpportunityListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Button,
  Card,
  CardContent,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Label,
} from '@/shared/components/ui';
import {
  Search,
  Plus,
  Grid3X3,
  List,
  Columns3,
  SlidersHorizontal,
  TrendingUp,
  DollarSign,
  Target,
  Trophy,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { OpportunityCard } from '../../components/cards';
import { StatsCard } from '../../components/dashboard';
import { ExportDropdown } from '../../components/shared';
import { QuickAddOpportunityDialog } from '../../components/dialogs';
import { OPPORTUNITY_EXPORT_COLUMNS } from '../../utils/export';
import { MOCK_OPPORTUNITIES } from '../../mocks';
import type { Opportunity, OpportunityStage } from '../../types';

// Pipeline stage configuration
const PIPELINE_STAGES: {
  stage: OpportunityStage;
  label: string;
  color: string;
}[] = [
  { stage: 'PROSPECTING', label: 'Prospecting', color: 'bg-blue-500' },
  { stage: 'QUALIFICATION', label: 'Qualification', color: 'bg-cyan-500' },
  { stage: 'PROPOSAL', label: 'Proposal', color: 'bg-yellow-500' },
  { stage: 'NEGOTIATION', label: 'Negotiation', color: 'bg-orange-500' },
  { stage: 'CLOSED_WON', label: 'Won', color: 'bg-green-500' },
  { stage: 'CLOSED_LOST', label: 'Lost', color: 'bg-red-500' },
];

interface OpportunityListPageProps {
  className?: string;
}

export const OpportunityListPage: React.FC<OpportunityListPageProps> = ({
  className,
}) => {
  const router = useRouter();

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [stageFilter, setStageFilter] = useState<OpportunityStage | ''>('');
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'value'>(
    'createdAt'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);
  const [viewMode, setViewMode] = useState<'grid' | 'list' | 'pipeline'>(
    'pipeline'
  );
  const [showFilters, setShowFilters] = useState(false);
  const [showQuickAdd, setShowQuickAdd] = useState(false);

  const pageSize = viewMode === 'pipeline' ? 100 : 12;

  // Filter and sort opportunities from mock data
  const filteredOpportunities = useMemo(() => {
    let result = [...MOCK_OPPORTUNITIES];

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(
        (opp) =>
          opp.name.toLowerCase().includes(query) ||
          opp.customerName?.toLowerCase().includes(query)
      );
    }

    // Stage filter
    if (stageFilter) {
      result = result.filter((opp) => opp.stage === stageFilter);
    }

    // Sorting
    result.sort((a, b) => {
      let comparison = 0;
      if (sortBy === 'name') {
        comparison = a.name.localeCompare(b.name);
      } else if (sortBy === 'value') {
        comparison = (a.value || 0) - (b.value || 0);
      } else if (sortBy === 'createdAt') {
        comparison =
          new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      }
      return sortOrder === 'desc' ? -comparison : comparison;
    });

    return result;
  }, [searchQuery, stageFilter, sortBy, sortOrder]);

  // Paginate for grid/list view
  const opportunities = useMemo(() => {
    if (viewMode === 'pipeline') {
      return filteredOpportunities;
    }
    const start = (currentPage - 1) * pageSize;
    return filteredOpportunities.slice(start, start + pageSize);
  }, [filteredOpportunities, currentPage, pageSize, viewMode]);

  const total = filteredOpportunities.length;
  const totalPages = Math.ceil(total / pageSize);

  // Calculate stats from all mock data (not filtered)
  const stats = useMemo(() => {
    const allOpportunities = MOCK_OPPORTUNITIES;
    const totalValue = allOpportunities.reduce(
      (sum, o) => sum + (o.value || 0),
      0
    );
    const weightedValue = allOpportunities.reduce(
      (sum, o) => sum + (o.value || 0) * ((o.probability || 0) / 100),
      0
    );
    const wonDeals = allOpportunities.filter((o) => o.stage === 'CLOSED_WON');
    const wonValue = wonDeals.reduce((sum, o) => sum + (o.value || 0), 0);
    const totalCount = allOpportunities.length;

    return {
      total: totalCount,
      totalValue,
      weightedValue,
      wonCount: wonDeals.length,
      wonValue,
      avgDealSize: totalCount > 0 ? totalValue / totalCount : 0,
    };
  }, []);

  // Group opportunities by stage for pipeline view (use filtered data)
  const opportunitiesByStage = useMemo(() => {
    const grouped: Record<OpportunityStage, Opportunity[]> = {
      PROSPECTING: [],
      QUALIFICATION: [],
      PROPOSAL: [],
      NEGOTIATION: [],
      CLOSED_WON: [],
      CLOSED_LOST: [],
    };
    filteredOpportunities.forEach((opp) => {
      if (grouped[opp.stage]) {
        grouped[opp.stage].push(opp);
      }
    });
    return grouped;
  }, [filteredOpportunities]);

  // Calculate stage values from filtered data
  const stageValues = useMemo(() => {
    const values: Record<OpportunityStage, number> = {
      PROSPECTING: 0,
      QUALIFICATION: 0,
      PROPOSAL: 0,
      NEGOTIATION: 0,
      CLOSED_WON: 0,
      CLOSED_LOST: 0,
    };
    filteredOpportunities.forEach((opp) => {
      values[opp.stage] += opp.value || 0;
    });
    return values;
  }, [filteredOpportunities]);

  const handleViewOpportunity = (opportunityId: string) => {
    router.push(`/crm/opportunities/${opportunityId}`);
  };

  const handleQuickAddOpportunity = async (data: unknown) => {
    console.log('Quick adding opportunity:', data);
    setShowQuickAdd(false);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStageFilter('');
    setCurrentPage(1);
  };

  const hasActiveFilters = searchQuery || stageFilter;

  return (
    <div className={cn('space-y-6', className)}>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Opportunities</h1>
          <p className='text-muted-foreground'>
            Track and manage your sales pipeline
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <ExportDropdown
            data={filteredOpportunities}
            columns={OPPORTUNITY_EXPORT_COLUMNS}
            filename='opportunities'
            onExportComplete={(format, count) => {
              console.log(`Exported ${count} opportunities as ${format}`);
            }}
          />
          <Button onClick={() => setShowQuickAdd(true)} className='gap-2'>
            <Plus className='h-4 w-4' />
            Add Opportunity
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Total Pipeline'
          value={`$${stats.totalValue.toLocaleString()}`}
          icon={TrendingUp}
          variant='primary'
        />
        <StatsCard
          title='Weighted Value'
          value={`$${Math.round(stats.weightedValue).toLocaleString()}`}
          icon={DollarSign}
          variant='warning'
        />
        <StatsCard
          title='Won Deals'
          value={stats.wonCount}
          icon={Trophy}
          variant='success'
        />
        <StatsCard
          title='Avg. Deal Size'
          value={`$${Math.round(stats.avgDealSize).toLocaleString()}`}
          icon={Target}
          variant='default'
        />
      </div>

      {/* Search & Filters Bar */}
      <div className='flex flex-col sm:flex-row gap-3'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search opportunities by name or customer...'
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setCurrentPage(1);
            }}
            className='pl-10 pr-10'
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
            >
              <X className='h-4 w-4' />
            </button>
          )}
        </div>

        {/* Filter Toggle */}
        <Button
          variant={showFilters ? 'secondary' : 'outline'}
          onClick={() => setShowFilters(!showFilters)}
          className='gap-2'
        >
          <SlidersHorizontal className='h-4 w-4' />
          Filters
          {hasActiveFilters && (
            <span className='h-2 w-2 rounded-full bg-primary' />
          )}
        </Button>

        {/* View Toggle */}
        <div className='flex rounded-lg border bg-muted p-1'>
          <button
            onClick={() => setViewMode('pipeline')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'pipeline'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
            title='Pipeline view'
          >
            <Columns3 className='h-4 w-4' />
          </button>
          <button
            onClick={() => setViewMode('grid')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'grid'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
            title='Grid view'
          >
            <Grid3X3 className='h-4 w-4' />
          </button>
          <button
            onClick={() => setViewMode('list')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'list'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
            title='List view'
          >
            <List className='h-4 w-4' />
          </button>
        </div>
      </div>

      {/* Expanded Filters */}
      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 sm:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label>Stage</Label>
                <Select
                  value={stageFilter}
                  onValueChange={(value) => {
                    setStageFilter(value as OpportunityStage | '');
                    setCurrentPage(1);
                  }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='All Stages' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value=''>All Stages</SelectItem>
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
                    setSortBy(field as typeof sortBy);
                    setSortOrder(order as 'asc' | 'desc');
                    setCurrentPage(1);
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
              <div className='mt-4 pt-4 border-t flex items-center justify-between'>
                <p className='text-sm text-muted-foreground'>
                  {total} results found
                </p>
                <Button variant='ghost' size='sm' onClick={clearFilters}>
                  Clear all filters
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pipeline Board View */}
      {viewMode === 'pipeline' && opportunities.length > 0 && (
        <div className='overflow-x-auto pb-4'>
          <div className='grid grid-cols-6 gap-4 min-w-[1200px]'>
            {PIPELINE_STAGES.map(({ stage, label, color }) => (
              <div
                key={stage}
                className='bg-muted/30 rounded-xl p-4 min-h-[500px]'
              >
                {/* Column Header */}
                <div className='mb-4'>
                  <div className='flex items-center justify-between mb-2'>
                    <div className='flex items-center gap-2'>
                      <div className={cn('w-3 h-3 rounded-full', color)} />
                      <h3 className='font-semibold text-sm'>{label}</h3>
                    </div>
                    <span className='text-xs text-muted-foreground px-2 py-1 bg-background rounded-full'>
                      {opportunitiesByStage[stage]?.length || 0}
                    </span>
                  </div>
                  <p className='text-sm font-medium text-muted-foreground'>
                    ${stageValues[stage].toLocaleString()}
                  </p>
                </div>

                {/* Cards */}
                <div className='space-y-3'>
                  {opportunitiesByStage[stage]?.map((opportunity) => (
                    <OpportunityCard
                      key={opportunity.id}
                      opportunity={opportunity}
                      variant='pipeline'
                      onClick={() => handleViewOpportunity(opportunity.id)}
                    />
                  ))}
                  {(!opportunitiesByStage[stage] ||
                    opportunitiesByStage[stage].length === 0) && (
                    <p className='text-xs text-muted-foreground text-center py-8'>
                      No deals
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Grid/List View */}
      {viewMode !== 'pipeline' && opportunities.length > 0 && (
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
              onClick={() => handleViewOpportunity(opportunity.id)}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {opportunities.length === 0 && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <TrendingUp className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>
              No opportunities found
            </h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by creating your first opportunity.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button>
                <Plus className='h-4 w-4 mr-2' />
                Create First Opportunity
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pagination (not for pipeline) */}
      {viewMode !== 'pipeline' && total > pageSize && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {(currentPage - 1) * pageSize + 1} to{' '}
            {Math.min(currentPage * pageSize, total)} of {total} opportunities
          </p>
          <div className='flex items-center gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(currentPage - 1)}
            >
              <ChevronLeft className='h-4 w-4' />
              Previous
            </Button>
            <div className='flex items-center gap-1'>
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                const pageNum = i + 1;
                return (
                  <button
                    key={pageNum}
                    onClick={() => setCurrentPage(pageNum)}
                    className={cn(
                      'h-8 w-8 rounded-md text-sm font-medium transition-colors',
                      currentPage === pageNum
                        ? 'bg-primary text-primary-foreground'
                        : 'hover:bg-muted'
                    )}
                  >
                    {pageNum}
                  </button>
                );
              })}
            </div>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage(currentPage + 1)}
            >
              Next
              <ChevronRight className='h-4 w-4' />
            </Button>
          </div>
        </div>
      )}

      {/* Quick Add Dialog */}
      <QuickAddOpportunityDialog
        open={showQuickAdd}
        onOpenChange={setShowQuickAdd}
        onSubmit={handleQuickAddOpportunity}
      />
    </div>
  );
};

export default OpportunityListPage;
