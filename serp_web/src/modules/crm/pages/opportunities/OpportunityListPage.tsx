// OpportunityListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import { useDebounce } from '@/shared/hooks/use-debounce';
import {
  Button,
  Card,
  CardContent,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { toast } from 'sonner';
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
import { QuickAddOpportunityDialog } from '../../components/dialogs';
import { ExportDropdown } from '../../components/shared';
import {
  useCreateOpportunityMutation,
  useDeleteOpportunityMutation,
  useGetOpportunitiesQuery,
  useGetOpportunityPipelineQuery,
} from '../../api/crmApi';
import { OPPORTUNITY_EXPORT_COLUMNS } from '../../utils/export';
import type {
  CreateOpportunityRequest,
  Opportunity,
  OpportunityStage,
} from '../../types';

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

  const [searchQuery, setSearchQuery] = useState('');
  const [stageFilter, setStageFilter] = useState<OpportunityStage | 'ALL'>(
    'ALL'
  );
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
  const debouncedSearchQuery = useDebounce(searchQuery, 400);

  const pageSize = viewMode === 'pipeline' ? 100 : 12;

  const [createOpportunity, { isLoading: isCreatingOpportunity }] =
    useCreateOpportunityMutation();
  const [deleteOpportunity] = useDeleteOpportunityMutation();

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

  const { data: pipelineResponse, isLoading: isLoadingPipeline } =
    useGetOpportunityPipelineQuery({});

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

  const opportunitiesByStage = useMemo(() => {
    const grouped: Record<OpportunityStage, Opportunity[]> = {
      PROSPECTING: [],
      QUALIFICATION: [],
      PROPOSAL: [],
      NEGOTIATION: [],
      CLOSED_WON: [],
      CLOSED_LOST: [],
    };

    pipelineOpportunities.forEach((opportunity) => {
      grouped[opportunity.stage].push(opportunity);
    });

    return grouped;
  }, [pipelineOpportunities]);

  const stageValues = useMemo(() => {
    const values: Record<OpportunityStage, number> = {
      PROSPECTING: 0,
      QUALIFICATION: 0,
      PROPOSAL: 0,
      NEGOTIATION: 0,
      CLOSED_WON: 0,
      CLOSED_LOST: 0,
    };

    pipelineOpportunities.forEach((opportunity) => {
      values[opportunity.stage] +=
        opportunity.estimatedValue ?? opportunity.value ?? 0;
    });

    return values;
  }, [pipelineOpportunities]);

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

  const handleViewOpportunity = (opportunityId: string) => {
    router.push(`/crm/opportunities/${opportunityId}`);
  };

  const handleQuickAddOpportunity = async (data: CreateOpportunityRequest) => {
    try {
      await createOpportunity(data).unwrap();
      toast.success('Create opportunity successfully');
      setShowQuickAdd(false);
    } catch (error) {
      toast.error('Failed to create opportunity', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeleteOpportunity = async (opportunityId: string) => {
    try {
      await deleteOpportunity(opportunityId).unwrap();
      toast.success('Delete opportunity successfully');
    } catch (error) {
      toast.error('Failed to delete opportunity', {
        description: getErrorMessage(error),
      });
    }
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStageFilter('ALL');
    setCurrentPage(1);
  };

  const hasActiveFilters =
    searchQuery || (stageFilter && stageFilter !== 'ALL');
  const isLoading =
    viewMode === 'pipeline' ? isLoadingPipeline : isLoadingOpportunities;
  const activeOpportunities =
    viewMode === 'pipeline' ? pipelineOpportunities : listOpportunities;

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Opportunities</h1>
          <p className='text-muted-foreground'>
            Track and manage your sales pipeline
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <ExportDropdown
            data={activeOpportunities}
            columns={OPPORTUNITY_EXPORT_COLUMNS}
            filename='opportunities'
            onExportComplete={(format, count) => {
              toast.success(`Exported ${count} opportunities as ${format}`);
            }}
          />
          <Button onClick={() => setShowQuickAdd(true)} className='gap-2'>
            <Plus className='h-4 w-4' />
            Add Opportunity
          </Button>
        </div>
      </div>

      <div className='grid grid-cols-2 gap-4 sm:grid-cols-4'>
        <StatsCard
          title='Total Pipeline'
          value={`$${Math.round(stats.totalValue).toLocaleString()}`}
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

      <div className='flex flex-col gap-3 sm:flex-row'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            placeholder='Search opportunities by name or account...'
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

        <div className='flex rounded-lg border bg-muted p-1'>
          <button
            onClick={() => setViewMode('pipeline')}
            className={cn(
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
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
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
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
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
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

      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label>Stage</Label>
                <Select
                  value={stageFilter}
                  onValueChange={(value) => {
                    setStageFilter(value as OpportunityStage | 'ALL');
                    setCurrentPage(1);
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
              <div className='mt-4 flex items-center justify-between border-t pt-4'>
                <p className='text-sm text-muted-foreground'>
                  {viewMode === 'pipeline'
                    ? pipelineOpportunities.length
                    : total}{' '}
                  results found
                </p>
                <Button variant='ghost' size='sm' onClick={clearFilters}>
                  Clear all filters
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {error && (
        <Card>
          <CardContent className='py-10 text-center text-sm text-muted-foreground'>
            Failed to load opportunities.
          </CardContent>
        </Card>
      )}

      {isLoading && (
        <Card>
          <CardContent className='py-16 text-center text-muted-foreground'>
            Loading opportunities...
          </CardContent>
        </Card>
      )}

      {!isLoading &&
        viewMode === 'pipeline' &&
        pipelineOpportunities.length > 0 && (
          <div className='overflow-x-auto pb-4'>
            <div className='grid min-w-[1200px] grid-cols-6 gap-4'>
              {PIPELINE_STAGES.map(({ stage, label, color }) => (
                <div
                  key={stage}
                  className='min-h-[500px] rounded-xl bg-muted/30 p-4'
                >
                  <div className='mb-4'>
                    <div className='mb-2 flex items-center justify-between'>
                      <div className='flex items-center gap-2'>
                        <div className={cn('h-3 w-3 rounded-full', color)} />
                        <h3 className='text-sm font-semibold'>{label}</h3>
                      </div>
                      <span className='rounded-full bg-background px-2 py-1 text-xs text-muted-foreground'>
                        {opportunitiesByStage[stage]?.length || 0}
                      </span>
                    </div>
                    <p className='text-sm font-medium text-muted-foreground'>
                      ${stageValues[stage].toLocaleString()}
                    </p>
                  </div>

                  <div className='space-y-3'>
                    {opportunitiesByStage[stage]?.map((opportunity) => (
                      <OpportunityCard
                        key={opportunity.id}
                        opportunity={opportunity}
                        variant='pipeline'
                        onClick={() => handleViewOpportunity(opportunity.id)}
                        onDelete={() => handleDeleteOpportunity(opportunity.id)}
                      />
                    ))}
                    {opportunitiesByStage[stage].length === 0 && (
                      <p className='py-8 text-center text-xs text-muted-foreground'>
                        No deals
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

      {!isLoading &&
        viewMode !== 'pipeline' &&
        listOpportunities.length > 0 && (
          <div
            className={cn(
              'gap-4',
              viewMode === 'grid'
                ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
                : 'flex flex-col'
            )}
          >
            {listOpportunities.map((opportunity) => (
              <OpportunityCard
                key={opportunity.id}
                opportunity={opportunity}
                variant={viewMode === 'list' ? 'compact' : 'default'}
                onClick={() => handleViewOpportunity(opportunity.id)}
                onDelete={() => handleDeleteOpportunity(opportunity.id)}
              />
            ))}
          </div>
        )}

      {!isLoading && activeOpportunities.length === 0 && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted'>
              <TrendingUp className='h-10 w-10 text-muted-foreground' />
            </div>
            <h3 className='mb-2 text-lg font-semibold'>
              No opportunities found
            </h3>
            <p className='mx-auto mb-6 max-w-sm text-muted-foreground'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by creating your first opportunity.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => router.push('/crm/opportunities/create')}>
                <Plus className='mr-2 h-4 w-4' />
                Create First Opportunity
              </Button>
            )}
          </CardContent>
        </Card>
      )}

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

      <QuickAddOpportunityDialog
        open={showQuickAdd}
        onOpenChange={setShowQuickAdd}
        onSubmit={handleQuickAddOpportunity}
        isLoading={isCreatingOpportunity}
      />
    </div>
  );
};

export default OpportunityListPage;
