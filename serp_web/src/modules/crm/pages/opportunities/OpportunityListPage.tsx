// OpportunityListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import type { DropResult } from '@hello-pangea/dnd';
import { getErrorMessage } from '@/lib/store/api';
import { Button, Card, CardContent, Input } from '@/shared/components/ui';
import { toast } from 'sonner';
import {
  Search,
  Plus,
  Grid3X3,
  List,
  Columns3,
  SlidersHorizontal,
  TrendingUp,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { QuickAddOpportunityDialog } from '../../components/dialogs';
import {
  WonOpportunityDialog,
  LostOpportunityDialog,
  ReopenOpportunityDialog,
} from '../../components/dialogs';
import {
  OpportunityStats,
  OpportunityFilters,
  OpportunityGridView,
  OpportunityPipelineView,
} from '../../components/opportunities';
import { ExportDropdown } from '../../components/shared';
import {
  useCreateOpportunityMutation,
  useDeleteOpportunityMutation,
  useChangeOpportunityStageMutation,
} from '../../api/crmApi';
import { OPPORTUNITY_EXPORT_COLUMNS } from '../../utils/export';
import {
  useOpportunityFilters,
  useOpportunityData,
  usePipelineGrouping,
  useStageTransition,
} from '../../hooks';
import type {
  CreateOpportunityRequest,
  Opportunity,
  OpportunityStage,
} from '../../types';

interface OpportunityListPageProps {
  className?: string;
}

export const OpportunityListPage: React.FC<OpportunityListPageProps> = ({
  className,
}) => {
  const router = useRouter();
  const [viewMode, setViewMode] = useState<'grid' | 'list' | 'pipeline'>(
    'pipeline'
  );
  const [showFilters, setShowFilters] = useState(false);
  const [showQuickAdd, setShowQuickAdd] = useState(false);

  const {
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
  } = useOpportunityFilters();

  const pageSize = viewMode === 'pipeline' ? 100 : 12;

  const {
    listOpportunities,
    pipelineOpportunities,
    total,
    totalPages,
    stats,
    isLoading,
    activeError,
    activeOpportunities,
  } = useOpportunityData({
    searchQuery,
    stageFilter,
    sortBy,
    sortOrder,
    currentPage,
    pageSize,
    viewMode,
  });

  const { opportunitiesByStage, stageValues, opportunityMap } =
    usePipelineGrouping(pipelineOpportunities);

  const {
    pendingStageTransition,
    setPendingStageTransition,
    wonActualValue,
    setWonActualValue,
    wonNotes,
    setWonNotes,
    lostReason,
    setLostReason,
    reopenReason,
    setReopenReason,
    isUpdatingOpportunityStage,
    resetPendingStageTransition,
    handleSubmitWonTransition,
    handleSubmitLostTransition,
    handleSubmitReopenTransition,
    isWonDialogOpen,
    isLostDialogOpen,
    isReopenDialogOpen,
  } = useStageTransition();

  const [createOpportunity, { isLoading: isCreatingOpportunity }] =
    useCreateOpportunityMutation();
  const [deleteOpportunity] = useDeleteOpportunityMutation();
  const [changeOpportunityStage] = useChangeOpportunityStageMutation();

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

  const handlePipelineDragEnd = async ({
    destination,
    draggableId,
    source,
  }: DropResult) => {
    if (!destination) return;

    const fromStage = source.droppableId as OpportunityStage;
    const toStage = destination.droppableId as OpportunityStage;

    if (fromStage === toStage) return;

    const opportunityId = draggableId.replace('opportunity-', '');
    const opportunity = opportunityMap.get(opportunityId);

    if (!opportunity) return;

    if (opportunity.stage !== fromStage) {
      toast.error(
        'Opportunity stage is outdated. Please refresh and try again.'
      );
      return;
    }

    if (fromStage === 'CLOSED_WON') {
      toast.error('Closed won opportunities cannot be moved.');
      return;
    }

    if (fromStage === 'CLOSED_LOST') {
      if (toStage === 'CLOSED_WON') {
        toast.error('Reopen lost opportunities to an active stage first.');
        return;
      }

      setPendingStageTransition({
        opportunityId,
        opportunityName: opportunity.name,
        fromStage,
        toStage,
      });
      return;
    }

    if (toStage === 'CLOSED_WON' || toStage === 'CLOSED_LOST') {
      setPendingStageTransition({
        opportunityId,
        opportunityName: opportunity.name,
        fromStage,
        toStage,
      });
      return;
    }

    // Normal stage transition (between active stages)
    try {
      await changeOpportunityStage({
        id: opportunityId,
        data: { stage: toStage },
      }).unwrap();
      toast.success(`Opportunity moved to ${toStage.replace('_', ' ')}`);
    } catch (error) {
      toast.error('Failed to move opportunity', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <>
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

        <OpportunityStats
          totalValue={stats.totalValue}
          weightedValue={stats.weightedValue}
          wonCount={stats.wonCount}
          avgDealSize={stats.avgDealSize}
        />

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
          <OpportunityFilters
            stageFilter={stageFilter}
            onStageFilterChange={(value) => {
              setStageFilter(value);
              setCurrentPage(1);
            }}
            sortBy={sortBy}
            sortOrder={sortOrder}
            onSortChange={(field, order) => {
              setSortBy(field as typeof sortBy);
              setSortOrder(order as 'asc' | 'desc');
              setCurrentPage(1);
            }}
            hasActiveFilters={hasActiveFilters}
            onClearFilters={clearFilters}
            resultCount={
              viewMode === 'pipeline' ? pipelineOpportunities.length : total
            }
          />
        )}

        {activeError && (
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
            <OpportunityPipelineView
              opportunitiesByStage={opportunitiesByStage}
              stageValues={stageValues}
              onDragEnd={handlePipelineDragEnd}
              onViewOpportunity={handleViewOpportunity}
              onDeleteOpportunity={handleDeleteOpportunity}
            />
          )}

        {!isLoading &&
          viewMode !== 'pipeline' &&
          listOpportunities.length > 0 && (
            <OpportunityGridView
              opportunities={listOpportunities}
              viewMode={viewMode}
              onViewOpportunity={handleViewOpportunity}
              onDeleteOpportunity={handleDeleteOpportunity}
            />
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
                <Button
                  onClick={() => router.push('/crm/opportunities/create')}
                >
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

        <WonOpportunityDialog
          open={isWonDialogOpen}
          onOpenChange={(open) => {
            if (!open) resetPendingStageTransition();
          }}
          onSubmit={handleSubmitWonTransition}
          isLoading={isUpdatingOpportunityStage}
          opportunityName={pendingStageTransition?.opportunityName}
          actualValue={wonActualValue}
          onActualValueChange={setWonActualValue}
          notes={wonNotes}
          onNotesChange={setWonNotes}
        />

        <LostOpportunityDialog
          open={isLostDialogOpen}
          onOpenChange={(open) => {
            if (!open) resetPendingStageTransition();
          }}
          onSubmit={handleSubmitLostTransition}
          isLoading={isUpdatingOpportunityStage}
          opportunityName={pendingStageTransition?.opportunityName}
          lostReason={lostReason}
          onLostReasonChange={setLostReason}
        />

        <ReopenOpportunityDialog
          open={isReopenDialogOpen}
          onOpenChange={(open) => {
            if (!open) resetPendingStageTransition();
          }}
          onSubmit={handleSubmitReopenTransition}
          isLoading={isUpdatingOpportunityStage}
          opportunityName={pendingStageTransition?.opportunityName}
          reopenStage={pendingStageTransition?.toStage || 'PROSPECTING'}
          onReopenStageChange={() => {}}
          reopenReason={reopenReason}
          onReopenReasonChange={setReopenReason}
        />
      </div>
    </>
  );
};

export default OpportunityListPage;
