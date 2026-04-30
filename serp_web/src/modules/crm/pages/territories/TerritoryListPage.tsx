'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import { useDebounce } from '@/shared/hooks/use-debounce';
import { Button, Card, CardContent, Input } from '@/shared/components/ui';
import { toast } from 'sonner';
import {
  Search,
  Plus,
  Grid3X3,
  List,
  SlidersHorizontal,
  MapPin,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { TerritoryCard } from '../../components/cards/TerritoryCard';
import { TerritoryForm } from '../../components/forms/TerritoryForm';
import { StatsCard } from '../../components/dashboard';
import {
  useGetTerritoriesQuery,
  useCreateTerritoryMutation,
  useUpdateTerritoryMutation,
  useActivateTerritoryMutation,
  useDeactivateTerritoryMutation,
} from '../../api/crmApi';
import type {
  Territory,
  CreateTerritoryRequest,
  UpdateTerritoryRequest,
} from '../../types';

interface TerritoryListPageProps {
  className?: string;
}

export const TerritoryListPage: React.FC<TerritoryListPageProps> = ({
  className,
}) => {
  const router = useRouter();

  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState<'true' | 'false' | ''>('');
  const [assignedFilter, setAssignedFilter] = useState<'true' | 'false' | ''>(
    ''
  );
  const [sortBy, setSortBy] = useState<'territoryName' | 'createdAt'>(
    'territoryName'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [currentPage, setCurrentPage] = useState(1);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingTerritory, setEditingTerritory] = useState<Territory | null>(
    null
  );
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const debouncedSearchQuery = useDebounce(searchQuery, 400);

  const pageSize = 12;
  const [createTerritory] = useCreateTerritoryMutation();
  const [updateTerritory] = useUpdateTerritoryMutation();
  const [activateTerritory] = useActivateTerritoryMutation();
  const [deactivateTerritory] = useDeactivateTerritoryMutation();

  const { data, isLoading, error } = useGetTerritoriesQuery({
    filters: {
      search: debouncedSearchQuery || undefined,
      active: activeFilter === '' ? undefined : activeFilter === 'true',
      assignedOnly: assignedFilter === 'true' ? true : undefined,
      unassignedOnly: assignedFilter === 'false' ? true : undefined,
    },
    pagination: {
      page: currentPage,
      limit: pageSize,
      sortBy,
      sortOrder,
    },
  });

  const territories = data?.data?.data || [];
  const total = data?.data?.pagination?.total || 0;
  const totalPages = data?.data?.pagination?.totalPages || 1;

  const stats = useMemo(() => {
    return {
      total,
      active: territories.filter((t) => t.active).length,
      assigned: territories.filter((t) => t.assignedTeamId).length,
      unassigned: territories.filter((t) => !t.assignedTeamId).length,
    };
  }, [territories, total]);

  const handleCreateTerritory = async (
    data: CreateTerritoryRequest | UpdateTerritoryRequest
  ) => {
    try {
      await createTerritory(data as CreateTerritoryRequest).unwrap();
      toast.success('Territory created successfully');
      setShowCreateForm(false);
    } catch (error) {
      toast.error('Failed to create territory', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleUpdateTerritory = async (
    data: CreateTerritoryRequest | UpdateTerritoryRequest
  ) => {
    if (!editingTerritory) return;
    try {
      await updateTerritory({
        territoryCode: editingTerritory.territoryCode,
        data,
      }).unwrap();
      toast.success('Territory updated successfully');
      setEditingTerritory(null);
    } catch (error) {
      toast.error('Failed to update territory', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleActivateTerritory = async (territoryCode: string) => {
    try {
      await activateTerritory(territoryCode).unwrap();
      toast.success('Territory activated successfully');
    } catch (error) {
      toast.error('Failed to activate territory', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeactivateTerritory = async (territoryCode: string) => {
    try {
      await deactivateTerritory(territoryCode).unwrap();
      toast.success('Territory deactivated successfully');
    } catch (error) {
      toast.error('Failed to deactivate territory', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleViewTerritory = (territoryCode: string) => {
    router.push(`/crm/territories/${territoryCode}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setActiveFilter('');
    setAssignedFilter('');
    setCurrentPage(1);
  };

  const hasActiveFilters = searchQuery || activeFilter || assignedFilter;

  if (showCreateForm || editingTerritory) {
    return (
      <div className={cn('', className)}>
        <TerritoryForm
          territory={editingTerritory || undefined}
          onSubmit={
            editingTerritory ? handleUpdateTerritory : handleCreateTerritory
          }
          onCancel={() => {
            setShowCreateForm(false);
            setEditingTerritory(null);
          }}
        />
      </div>
    );
  }

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Territories</h1>
          <p className='text-muted-foreground'>
            Manage geographic territories for lead routing
          </p>
        </div>
        <Button onClick={() => setShowCreateForm(true)} className='gap-2'>
          <Plus className='h-4 w-4' />
          Add Territory
        </Button>
      </div>

      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Total Territories'
          value={stats.total}
          icon={MapPin}
          variant='primary'
        />
        <StatsCard
          title='Active'
          value={stats.active}
          icon={MapPin}
          variant='success'
        />
        <StatsCard
          title='Assigned'
          value={stats.assigned}
          icon={MapPin}
          variant='default'
        />
        <StatsCard
          title='Unassigned'
          value={stats.unassigned}
          icon={MapPin}
          variant='warning'
        />
      </div>

      <div className='flex flex-col sm:flex-row gap-3'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search territories by name or code...'
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
            onClick={() => setViewMode('grid')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'grid'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
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
          >
            <List className='h-4 w-4' />
          </button>
        </div>
      </div>

      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 sm:grid-cols-3 gap-4'>
              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Active Status
                </label>
                <select
                  value={activeFilter}
                  onChange={(e) => {
                    setActiveFilter(e.target.value as 'true' | 'false' | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All</option>
                  <option value='true'>Active</option>
                  <option value='false'>Inactive</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Assignment Status
                </label>
                <select
                  value={assignedFilter}
                  onChange={(e) => {
                    setAssignedFilter(e.target.value as 'true' | 'false' | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All</option>
                  <option value='true'>Assigned</option>
                  <option value='false'>Unassigned</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Sort By
                </label>
                <select
                  value={`${sortBy}-${sortOrder}`}
                  onChange={(e) => {
                    const [field, order] = e.target.value.split('-');
                    setSortBy(field as typeof sortBy);
                    setSortOrder(order as 'asc' | 'desc');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value='territoryName-asc'>Name A-Z</option>
                  <option value='territoryName-desc'>Name Z-A</option>
                  <option value='createdAt-desc'>Newest First</option>
                  <option value='createdAt-asc'>Oldest First</option>
                </select>
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

      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4'>
            <p className='text-destructive'>
              Error loading territories. Please try again.
            </p>
          </CardContent>
        </Card>
      )}

      {isLoading && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {Array.from({ length: 6 }).map((_, index) => (
            <Card key={index} className='animate-pulse'>
              <CardContent className='p-5'>
                <div className='flex items-center gap-3 mb-4'>
                  <div className='h-12 w-12 bg-muted rounded-full' />
                  <div className='flex-1'>
                    <div className='h-4 bg-muted rounded w-3/4 mb-2' />
                    <div className='h-3 bg-muted rounded w-1/2' />
                  </div>
                </div>
                <div className='space-y-2'>
                  <div className='h-3 bg-muted rounded w-full' />
                  <div className='h-3 bg-muted rounded w-2/3' />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {!isLoading && territories.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {territories.map((territory) => (
            <TerritoryCard
              key={territory.territoryCode}
              territory={territory}
              variant={viewMode === 'list' ? 'compact' : 'default'}
              onClick={() => handleViewTerritory(territory.territoryCode)}
              onEdit={() => setEditingTerritory(territory)}
              onActivate={() =>
                handleActivateTerritory(territory.territoryCode)
              }
              onDeactivate={() =>
                handleDeactivateTerritory(territory.territoryCode)
              }
            />
          ))}
        </div>
      )}

      {!isLoading && territories.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <MapPin className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>No territories found</h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first territory.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => setShowCreateForm(true)}>
                <Plus className='h-4 w-4 mr-2' />
                Add First Territory
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {total > pageSize && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {(currentPage - 1) * pageSize + 1} to{' '}
            {Math.min(currentPage * pageSize, total)} of {total} territories
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
    </div>
  );
};

export default TerritoryListPage;
