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
  Users,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { TeamCard } from '../../components/cards/TeamCard';
import { TeamForm } from '../../components/forms/TeamForm';
import { StatsCard } from '../../components/dashboard';
import {
  useGetTeamsQuery,
  useCreateTeamMutation,
  useUpdateTeamMutation,
  useDeleteTeamMutation,
} from '../../api/crmApi';
import type {
  Team,
  TeamStatus,
  CreateTeamRequest,
  UpdateTeamRequest,
} from '../../types';

interface TeamListPageProps {
  className?: string;
}

export const TeamListPage: React.FC<TeamListPageProps> = ({ className }) => {
  const router = useRouter();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<TeamStatus | ''>('');
  const [currentPage, setCurrentPage] = useState(1);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingTeam, setEditingTeam] = useState<Team | null>(null);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const debouncedSearchQuery = useDebounce(searchQuery, 400);

  const pageSize = 12;
  const [createTeam] = useCreateTeamMutation();
  const [updateTeam] = useUpdateTeamMutation();
  const [deleteTeam] = useDeleteTeamMutation();

  const { data, isLoading, error } = useGetTeamsQuery({
    filters: {
      status: statusFilter || undefined,
    },
    pagination: {
      page: currentPage,
      limit: pageSize,
    },
  });

  const apiTeams = data?.data?.items || [];
  const teams = debouncedSearchQuery
    ? apiTeams.filter((team) =>
        team.name.toLowerCase().includes(debouncedSearchQuery.toLowerCase())
      )
    : apiTeams;
  const total = debouncedSearchQuery
    ? teams.length
    : data?.data?.pagination?.totalItems || 0;
  const totalPages = data?.data?.pagination?.totalPages || 1;

  const stats = useMemo(() => {
    return {
      total,
      active: teams.filter((t) => t.status === 'ACTIVE').length,
      totalMembers: teams.reduce((sum, t) => sum + (t.memberCount ?? 0), 0),
    };
  }, [teams, total]);

  const handleCreateTeam = async (
    data: CreateTeamRequest | UpdateTeamRequest
  ) => {
    try {
      await createTeam(data as CreateTeamRequest).unwrap();
      toast.success('Team created successfully');
      setShowCreateForm(false);
    } catch (error) {
      toast.error('Failed to create team', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleUpdateTeam = async (
    data: CreateTeamRequest | UpdateTeamRequest
  ) => {
    if (!editingTeam) return;
    try {
      await updateTeam({ id: editingTeam.id, data }).unwrap();
      toast.success('Team updated successfully');
      setEditingTeam(null);
    } catch (error) {
      toast.error('Failed to update team', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeleteTeam = async (teamId: string) => {
    try {
      await deleteTeam(teamId).unwrap();
      toast.success('Team deleted successfully');
    } catch (error) {
      toast.error('Failed to delete team', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleViewTeam = (teamId: string) => {
    router.push(`/crm/teams/${teamId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    setCurrentPage(1);
  };

  const hasActiveFilters = searchQuery || statusFilter;

  if (showCreateForm || editingTeam) {
    return (
      <div className={cn('', className)}>
        <TeamForm
          team={editingTeam || undefined}
          onSubmit={editingTeam ? handleUpdateTeam : handleCreateTeam}
          onCancel={() => {
            setShowCreateForm(false);
            setEditingTeam(null);
          }}
        />
      </div>
    );
  }

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Teams</h1>
          <p className='text-muted-foreground'>
            Manage sales teams and territories
          </p>
        </div>
        <Button onClick={() => setShowCreateForm(true)} className='gap-2'>
          <Plus className='h-4 w-4' />
          Add Team
        </Button>
      </div>

      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Total Teams'
          value={stats.total}
          icon={Users}
          variant='primary'
        />
        <StatsCard
          title='Active Teams'
          value={stats.active}
          icon={Users}
          variant='success'
        />
        <StatsCard
          title='Total Members'
          value={stats.totalMembers}
          icon={Users}
          variant='default'
        />
      </div>

      <div className='flex flex-col sm:flex-row gap-3'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search teams by name...'
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
            }}
            className='pl-10'
          />
        </div>

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

      <Card>
        <CardContent className='p-4'>
          <div className='grid grid-cols-1 sm:grid-cols-3 gap-4'>
            <div>
              <label className='text-sm font-medium mb-1.5 block'>Status</label>
              <select
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value as TeamStatus | '');
                  setCurrentPage(1);
                }}
                className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
              >
                <option value=''>All Statuses</option>
                <option value='ACTIVE'>Active</option>
                <option value='INACTIVE'>Inactive</option>
              </select>
            </div>
          </div>

          {hasActiveFilters && (
            <div className='mt-4 pt-4 border-t flex items-center justify-between'>
              <p className='text-sm text-muted-foreground'>
                {total} results found
              </p>
              <Button variant='ghost' size='sm' onClick={clearFilters}>
                Clear filters
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4'>
            <p className='text-destructive'>
              Error loading teams. Please try again.
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

      {!isLoading && teams.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {teams.map((team) => (
            <TeamCard
              key={team.id}
              team={team}
              variant={viewMode === 'list' ? 'compact' : 'default'}
              onClick={() => handleViewTeam(team.id)}
              onEdit={() => setEditingTeam(team)}
              onDelete={() => handleDeleteTeam(team.id)}
            />
          ))}
        </div>
      )}

      {!isLoading && teams.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Users className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>No teams found</h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first team.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => setShowCreateForm(true)}>
                <Plus className='h-4 w-4 mr-2' />
                Add First Team
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {total > pageSize && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {(currentPage - 1) * pageSize + 1} to{' '}
            {Math.min(currentPage * pageSize, total)} of {total} teams
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

export default TeamListPage;
