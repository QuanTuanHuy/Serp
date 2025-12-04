// LeadListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Card, CardContent, Input } from '@/shared/components/ui';
import {
  Search,
  Plus,
  Grid3X3,
  List,
  LayoutGrid,
  SlidersHorizontal,
  Target,
  UserCheck,
  Clock,
  Sparkles,
  ChevronLeft,
  ChevronRight,
  X,
  ArrowRight,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { LeadCard } from '../../components/cards';
import { LeadForm } from '../../components/forms';
import { StatsCard } from '../../components/dashboard';
import { MOCK_LEADS } from '../../mocks';
import type { Lead, LeadFilters, LeadStatus, LeadSource } from '../../types';

// Status configuration for kanban view
const LEAD_STATUSES: { status: LeadStatus; label: string; color: string }[] = [
  { status: 'NEW', label: 'New', color: 'bg-blue-500' },
  { status: 'CONTACTED', label: 'Contacted', color: 'bg-yellow-500' },
  { status: 'QUALIFIED', label: 'Qualified', color: 'bg-green-500' },
  { status: 'CONVERTED', label: 'Converted', color: 'bg-purple-500' },
  { status: 'LOST', label: 'Lost', color: 'bg-red-500' },
];

interface LeadListPageProps {
  className?: string;
}

export const LeadListPage: React.FC<LeadListPageProps> = ({ className }) => {
  const router = useRouter();

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<LeadStatus | ''>('');
  const [sourceFilter, setSourceFilter] = useState<LeadSource | ''>('');
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'estimatedValue'>(
    'createdAt'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingLead, setEditingLead] = useState<Lead | null>(null);
  const [viewMode, setViewMode] = useState<'grid' | 'list' | 'kanban'>('grid');
  const [showFilters, setShowFilters] = useState(false);

  const pageSize = viewMode === 'kanban' ? 100 : 12;

  // Filter and sort mock data
  const filteredLeads = useMemo(() => {
    let result = [...MOCK_LEADS];

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(
        (l) =>
          `${l.firstName} ${l.lastName}`.toLowerCase().includes(query) ||
          l.email.toLowerCase().includes(query) ||
          l.company?.toLowerCase().includes(query)
      );
    }

    // Apply status filter
    if (statusFilter) {
      result = result.filter((l) => l.status === statusFilter);
    }

    // Apply source filter
    if (sourceFilter) {
      result = result.filter((l) => l.source === sourceFilter);
    }

    // Apply sorting
    result.sort((a, b) => {
      let comparison = 0;
      if (sortBy === 'name') {
        comparison = `${a.firstName} ${a.lastName}`.localeCompare(
          `${b.firstName} ${b.lastName}`
        );
      } else if (sortBy === 'createdAt') {
        comparison =
          new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      } else if (sortBy === 'estimatedValue') {
        comparison = (a.estimatedValue || 0) - (b.estimatedValue || 0);
      }
      return sortOrder === 'asc' ? comparison : -comparison;
    });

    return result;
  }, [searchQuery, statusFilter, sourceFilter, sortBy, sortOrder]);

  // Pagination
  const total = filteredLeads.length;
  const totalPages = Math.ceil(total / pageSize);
  const leads =
    viewMode === 'kanban'
      ? filteredLeads
      : filteredLeads.slice(
          (currentPage - 1) * pageSize,
          currentPage * pageSize
        );
  const isLoading = false;
  const error = null;

  // Calculate stats
  const stats = useMemo(() => {
    return {
      total: MOCK_LEADS.length,
      new: MOCK_LEADS.filter((l) => l.status === 'NEW').length,
      qualified: MOCK_LEADS.filter((l) => l.status === 'QUALIFIED').length,
      contacted: MOCK_LEADS.filter((l) => l.status === 'CONTACTED').length,
      converted: MOCK_LEADS.filter((l) => l.status === 'CONVERTED').length,
      avgValue: MOCK_LEADS.length
        ? Math.round(
            MOCK_LEADS.reduce((sum, l) => sum + (l.estimatedValue || 0), 0) /
              MOCK_LEADS.length
          )
        : 0,
    };
  }, []);

  // Group leads by status for kanban view
  const leadsByStatus = useMemo(() => {
    const grouped: Record<LeadStatus, Lead[]> = {
      NEW: [],
      CONTACTED: [],
      QUALIFIED: [],
      CONVERTED: [],
      LOST: [],
    };
    filteredLeads.forEach((lead) => {
      if (grouped[lead.status]) {
        grouped[lead.status].push(lead);
      }
    });
    return grouped;
  }, [filteredLeads]);

  // Handle actions
  const handleCreateLead = async (data: any) => {
    console.log('Creating lead:', data);
    setShowCreateForm(false);
  };

  const handleEditLead = (lead: Lead) => {
    setEditingLead(lead);
  };

  const handleUpdateLead = async (data: any) => {
    console.log('Updating lead:', data);
    setEditingLead(null);
  };

  const handleDeleteLead = async (leadId: string) => {
    console.log('Deleting lead:', leadId);
  };

  const handleConvertLead = async (leadId: string) => {
    console.log('Converting lead to customer:', leadId);
  };

  const handleViewLead = (leadId: string) => {
    router.push(`/crm/leads/${leadId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    setSourceFilter('');
    setCurrentPage(1);
  };

  const hasActiveFilters = searchQuery || statusFilter || sourceFilter;

  // Show create/edit form
  if (showCreateForm || editingLead) {
    return (
      <div className={cn('', className)}>
        <LeadForm
          lead={editingLead || undefined}
          onSubmit={editingLead ? handleUpdateLead : handleCreateLead}
          onCancel={() => {
            setShowCreateForm(false);
            setEditingLead(null);
          }}
        />
      </div>
    );
  }

  return (
    <div className={cn('space-y-6', className)}>
      {/* Page Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Leads</h1>
          <p className='text-muted-foreground'>
            Manage and convert your sales prospects
          </p>
        </div>
        <Button onClick={() => setShowCreateForm(true)} className='gap-2'>
          <Plus className='h-4 w-4' />
          Add Lead
        </Button>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-5 gap-4'>
        <StatsCard
          title='Total Leads'
          value={stats.total}
          icon={Target}
          variant='primary'
        />
        <StatsCard
          title='New'
          value={stats.new}
          icon={Sparkles}
          variant='default'
        />
        <StatsCard
          title='Contacted'
          value={stats.contacted}
          icon={Clock}
          variant='warning'
        />
        <StatsCard
          title='Qualified'
          value={stats.qualified}
          icon={UserCheck}
          variant='success'
        />
        <StatsCard
          title='Avg. Value'
          value={`$${stats.avgValue.toLocaleString()}`}
          icon={Target}
          variant='danger'
        />
      </div>

      {/* Search & Filters Bar */}
      <div className='flex flex-col sm:flex-row gap-3'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search leads by name, email, or company...'
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
          <button
            onClick={() => setViewMode('kanban')}
            className={cn(
              'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
              viewMode === 'kanban'
                ? 'bg-background shadow-sm'
                : 'hover:bg-background/50'
            )}
            title='Kanban board'
          >
            <LayoutGrid className='h-4 w-4' />
          </button>
        </div>
      </div>

      {/* Expanded Filters */}
      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 sm:grid-cols-3 gap-4'>
              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Status
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => {
                    setStatusFilter(e.target.value as LeadStatus | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Statuses</option>
                  <option value='NEW'>New</option>
                  <option value='CONTACTED'>Contacted</option>
                  <option value='QUALIFIED'>Qualified</option>
                  <option value='CONVERTED'>Converted</option>
                  <option value='LOST'>Lost</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Source
                </label>
                <select
                  value={sourceFilter}
                  onChange={(e) => {
                    setSourceFilter(e.target.value as LeadSource | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Sources</option>
                  <option value='WEBSITE'>Website</option>
                  <option value='REFERRAL'>Referral</option>
                  <option value='SOCIAL_MEDIA'>Social Media</option>
                  <option value='ADVERTISEMENT'>Advertisement</option>
                  <option value='EVENT'>Event</option>
                  <option value='COLD_CALL'>Cold Call</option>
                  <option value='OTHER'>Other</option>
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
                  <option value='createdAt-desc'>Newest First</option>
                  <option value='createdAt-asc'>Oldest First</option>
                  <option value='name-asc'>Name A-Z</option>
                  <option value='name-desc'>Name Z-A</option>
                  <option value='score-desc'>Highest Score</option>
                  <option value='score-asc'>Lowest Score</option>
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

      {/* Error State */}
      {error && (
        <Card className='border-destructive/50 bg-destructive/5'>
          <CardContent className='p-4'>
            <p className='text-destructive'>
              Error loading leads. Please try again.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Loading State */}
      {isLoading && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'kanban'
              ? 'grid grid-cols-4'
              : viewMode === 'grid'
                ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
                : 'flex flex-col'
          )}
        >
          {Array.from({ length: viewMode === 'kanban' ? 4 : 6 }).map(
            (_, index) => (
              <Card key={index} className='animate-pulse'>
                <CardContent className='p-5'>
                  <div className='flex items-center gap-3 mb-4'>
                    <div className='h-10 w-10 bg-muted rounded-full' />
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
            )
          )}
        </div>
      )}

      {/* Kanban Board View */}
      {!isLoading && viewMode === 'kanban' && leads.length > 0 && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4'>
          {LEAD_STATUSES.map(({ status, label, color }) => (
            <div
              key={status}
              className='bg-muted/30 rounded-xl p-4 min-h-[400px]'
            >
              {/* Column Header */}
              <div className='flex items-center justify-between mb-4'>
                <div className='flex items-center gap-2'>
                  <div className={cn('w-3 h-3 rounded-full', color)} />
                  <h3 className='font-semibold'>{label}</h3>
                </div>
                <span className='text-sm text-muted-foreground px-2 py-1 bg-background rounded-full'>
                  {leadsByStatus[status]?.length || 0}
                </span>
              </div>

              {/* Cards */}
              <div className='space-y-3'>
                {leadsByStatus[status]?.map((lead) => (
                  <LeadCard
                    key={lead.id}
                    lead={lead}
                    variant='kanban'
                    onClick={() => handleViewLead(lead.id)}
                    onConvert={
                      status === 'QUALIFIED'
                        ? () => handleConvertLead(lead.id)
                        : undefined
                    }
                  />
                ))}
                {(!leadsByStatus[status] ||
                  leadsByStatus[status].length === 0) && (
                  <p className='text-sm text-muted-foreground text-center py-8'>
                    No leads
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Grid/List View */}
      {!isLoading && viewMode !== 'kanban' && leads.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {leads.map((lead) => (
            <LeadCard
              key={lead.id}
              lead={lead}
              variant={viewMode === 'list' ? 'compact' : 'default'}
              onClick={() => handleViewLead(lead.id)}
              onEdit={() => handleEditLead(lead)}
              onDelete={() => handleDeleteLead(lead.id)}
              onConvert={
                lead.status === 'QUALIFIED'
                  ? () => handleConvertLead(lead.id)
                  : undefined
              }
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && leads.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Target className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>No leads found</h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first lead.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => setShowCreateForm(true)}>
                <Plus className='h-4 w-4 mr-2' />
                Add First Lead
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pagination (not for kanban) */}
      {viewMode !== 'kanban' && total > pageSize && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {(currentPage - 1) * pageSize + 1} to{' '}
            {Math.min(currentPage * pageSize, total)} of {total} leads
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

export default LeadListPage;
