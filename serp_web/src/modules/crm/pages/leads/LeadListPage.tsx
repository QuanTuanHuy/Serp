// LeadListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import { useDebounce } from '@/shared/hooks/use-debounce';
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
} from 'lucide-react';
import { toast } from 'sonner';
import { cn } from '@/shared/utils';
import { LeadCard } from '../../components/cards';
import { StatsCard } from '../../components/dashboard';
import { QuickAddLeadDialog } from '../../components/dialogs';
import { ExportDropdown } from '../../components/shared';
import {
  useCreateLeadMutation,
  useDeleteLeadMutation,
  useGetLeadsQuery,
} from '../../api/crmApi';
import { LEAD_EXPORT_COLUMNS } from '../../utils/export';
import type { CreateLeadRequest, LeadSource, LeadStatus } from '../../types';

const LEAD_STATUSES: { status: LeadStatus; label: string; color: string }[] = [
  { status: 'NEW', label: 'New', color: 'bg-blue-500' },
  { status: 'CONTACTED', label: 'Contacted', color: 'bg-yellow-500' },
  { status: 'NURTURING', label: 'Nurturing', color: 'bg-indigo-500' },
  { status: 'QUALIFIED', label: 'Qualified', color: 'bg-green-500' },
  { status: 'DISQUALIFIED', label: 'Disqualified', color: 'bg-red-500' },
  { status: 'CONVERTED', label: 'Converted', color: 'bg-purple-500' },
];

interface LeadListPageProps {
  className?: string;
}

export const LeadListPage: React.FC<LeadListPageProps> = ({ className }) => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<LeadStatus | ''>('');
  const [sourceFilter, setSourceFilter] = useState<LeadSource | ''>('');
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'estimatedValue'>('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);
  const [viewMode, setViewMode] = useState<'grid' | 'list' | 'kanban'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [showQuickAdd, setShowQuickAdd] = useState(false);

  const debouncedSearchQuery = useDebounce(searchQuery, 400);
  const pageSize = viewMode === 'kanban' ? 100 : 12;

  const [createLead, { isLoading: isCreating }] = useCreateLeadMutation();
  const [deleteLead, { isLoading: isDeleting }] = useDeleteLeadMutation();

  const queryArgs = useMemo(
    () => ({
      filters: {
        search: debouncedSearchQuery || undefined,
        status: statusFilter ? [statusFilter] : undefined,
        source: sourceFilter ? [sourceFilter] : undefined,
      },
      pagination: {
        page: currentPage,
        limit: pageSize,
        sortBy,
        sortOrder,
      },
    }),
    [currentPage, debouncedSearchQuery, pageSize, sortBy, sortOrder, sourceFilter, statusFilter]
  );

  const { data, isLoading, isFetching } = useGetLeadsQuery(queryArgs);
  const leads = data?.data.data || [];
  const pagination = data?.data.pagination;
  const total = pagination?.total || 0;
  const totalPages = pagination?.totalPages || 1;
  const error = undefined;

  const stats = useMemo(
    () => ({
      total,
      new: leads.filter((lead) => lead.leadStatus === 'NEW').length,
      qualified: leads.filter((lead) => lead.leadStatus === 'QUALIFIED').length,
      contacted: leads.filter((lead) => lead.leadStatus === 'CONTACTED').length,
      converted: leads.filter((lead) => lead.leadStatus === 'CONVERTED').length,
      avgValue: leads.length
        ? Math.round(
            leads.reduce((sum, lead) => sum + (lead.estimatedValue || 0), 0) /
              leads.length
          )
        : 0,
    }),
    [leads, total]
  );

  const leadsByStatus = useMemo(() => {
    const grouped: Record<LeadStatus, typeof leads> = {
      NEW: [],
      CONTACTED: [],
      NURTURING: [],
      QUALIFIED: [],
      DISQUALIFIED: [],
      CONVERTED: [],
      LOST: [],
    };

    leads.forEach((lead) => {
      const status = lead.leadStatus;
      if (status && grouped[status]) {
        grouped[status].push(lead);
      }
    });

    return grouped;
  }, [leads]);

  const handleQuickAddLead = async (data: {
    name: string;
    email: string;
    phone?: string;
    company?: string;
    jobTitle?: string;
    leadSource: LeadSource;
    estimatedValue?: number;
    followUpDate?: string;
    notes?: string;
  }) => {
    try {
      const [firstName, ...rest] = data.name.trim().split(/\s+/);
      await createLead({
        firstName: firstName || data.name,
        lastName: rest.join(' '),
        source: data.leadSource,
        status: 'NEW',
        priority: 'MEDIUM',
        tags: [],
        customFields: {},
        isActive: true,
        ...data,
      } as CreateLeadRequest).unwrap();
      toast.success('Create lead successfully');
      setShowQuickAdd(false);
    } catch (error) {
      toast.error('Failed to create lead', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeleteLead = async (leadId: string) => {
    try {
      await deleteLead(leadId).unwrap();
      toast.success('Delete lead successfully');
    } catch (error) {
      toast.error('Failed to delete lead', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleConvertLead = (leadId: string) => {
    router.push(`/crm/leads/${leadId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    setSourceFilter('');
    setCurrentPage(1);
  };

  const hasActiveFilters = searchQuery || statusFilter || sourceFilter;

  return (
    <div className={cn('space-y-6', className)}>
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Leads</h1>
          <p className='text-muted-foreground'>Manage and convert your sales prospects</p>
        </div>
        <div className='flex items-center gap-2'>
          <ExportDropdown
            data={leads}
            columns={LEAD_EXPORT_COLUMNS}
            filename='leads'
            onExportComplete={(format, count) => {
              toast.success(`Exported ${count} leads as ${format}`);
            }}
          />
          <Button onClick={() => setShowQuickAdd(true)} className='gap-2'>
            <Plus className='h-4 w-4' />
            Add Lead
          </Button>
        </div>
      </div>

      <div className='grid grid-cols-2 gap-4 sm:grid-cols-5'>
        <StatsCard title='Total Leads' value={stats.total} icon={Target} variant='primary' />
        <StatsCard title='New' value={stats.new} icon={Sparkles} variant='default' />
        <StatsCard title='Contacted' value={stats.contacted} icon={Clock} variant='warning' />
        <StatsCard title='Qualified' value={stats.qualified} icon={UserCheck} variant='success' />
        <StatsCard title='Avg. Value' value={`$${stats.avgValue.toLocaleString()}`} icon={Target} variant='danger' />
      </div>

      <div className='flex flex-col gap-3 sm:flex-row'>
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
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

        <Button
          variant={showFilters ? 'secondary' : 'outline'}
          onClick={() => setShowFilters(!showFilters)}
          className='gap-2'
        >
          <SlidersHorizontal className='h-4 w-4' />
          Filters
          {hasActiveFilters && <span className='h-2 w-2 rounded-full bg-primary' />}
        </Button>

        <div className='flex rounded-lg border bg-muted p-1'>
          <button
            onClick={() => setViewMode('grid')}
            className={cn(
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
              viewMode === 'grid' ? 'bg-background shadow-sm' : 'hover:bg-background/50'
            )}
            title='Grid view'
          >
            <Grid3X3 className='h-4 w-4' />
          </button>
          <button
            onClick={() => setViewMode('list')}
            className={cn(
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
              viewMode === 'list' ? 'bg-background shadow-sm' : 'hover:bg-background/50'
            )}
            title='List view'
          >
            <List className='h-4 w-4' />
          </button>
          <button
            onClick={() => setViewMode('kanban')}
            className={cn(
              'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
              viewMode === 'kanban' ? 'bg-background shadow-sm' : 'hover:bg-background/50'
            )}
            title='Kanban board'
          >
            <LayoutGrid className='h-4 w-4' />
          </button>
        </div>
      </div>

      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 gap-4 sm:grid-cols-3'>
              <div>
                <label className='mb-1.5 block text-sm font-medium'>Status</label>
                <select
                  value={statusFilter}
                  onChange={(e) => {
                    setStatusFilter(e.target.value as LeadStatus | '');
                    setCurrentPage(1);
                  }}
                  className='w-full rounded-lg border border-border bg-background px-3 py-2'
                >
                  <option value=''>All Statuses</option>
                  {LEAD_STATUSES.map((option) => (
                    <option key={option.status} value={option.status}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className='mb-1.5 block text-sm font-medium'>Source</label>
                <select
                  value={sourceFilter}
                  onChange={(e) => {
                    setSourceFilter(e.target.value as LeadSource | '');
                    setCurrentPage(1);
                  }}
                  className='w-full rounded-lg border border-border bg-background px-3 py-2'
                >
                  <option value=''>All Sources</option>
                  <option value='WEBSITE'>Website</option>
                  <option value='REFERRAL'>Referral</option>
                  <option value='SOCIAL_MEDIA'>Social Media</option>
                  <option value='COLD_CALL'>Cold Call</option>
                  <option value='EMAIL_CAMPAIGN'>Email Campaign</option>
                </select>
              </div>

              <div>
                <label className='mb-1.5 block text-sm font-medium'>Sort By</label>
                <select
                  value={`${sortBy}-${sortOrder}`}
                  onChange={(e) => {
                    const [field, order] = e.target.value.split('-');
                    setSortBy(field as typeof sortBy);
                    setSortOrder(order as 'asc' | 'desc');
                    setCurrentPage(1);
                  }}
                  className='w-full rounded-lg border border-border bg-background px-3 py-2'
                >
                  <option value='createdAt-desc'>Newest First</option>
                  <option value='createdAt-asc'>Oldest First</option>
                  <option value='name-asc'>Name A-Z</option>
                  <option value='name-desc'>Name Z-A</option>
                  <option value='estimatedValue-desc'>Highest Value</option>
                  <option value='estimatedValue-asc'>Lowest Value</option>
                </select>
              </div>
            </div>

            {hasActiveFilters && (
              <div className='mt-4 flex items-center justify-between border-t pt-4'>
                <p className='text-sm text-muted-foreground'>{total} results found</p>
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
            <p className='text-destructive'>Error loading leads. Please try again.</p>
          </CardContent>
        </Card>
      )}

      {(isLoading || isFetching) && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'kanban'
              ? 'grid grid-cols-1 lg:grid-cols-4'
              : viewMode === 'grid'
                ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
                : 'flex flex-col'
          )}
        >
          {Array.from({ length: viewMode === 'kanban' ? 4 : 6 }).map((_, index) => (
            <Card key={index} className='animate-pulse'>
              <CardContent className='p-5'>
                <div className='mb-4 flex items-center gap-3'>
                  <div className='h-10 w-10 rounded-full bg-muted' />
                  <div className='flex-1'>
                    <div className='mb-2 h-4 w-3/4 rounded bg-muted' />
                    <div className='h-3 w-1/2 rounded bg-muted' />
                  </div>
                </div>
                <div className='space-y-2'>
                  <div className='h-3 w-full rounded bg-muted' />
                  <div className='h-3 w-2/3 rounded bg-muted' />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {!isLoading && viewMode === 'kanban' && leads.length > 0 && (
        <div className='grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4'>
          {LEAD_STATUSES.filter((item) => item.status !== 'DISQUALIFIED').map(({ status, label, color }) => (
            <div key={status} className='min-h-[400px] rounded-xl bg-muted/30 p-4'>
              <div className='mb-4 flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <div className={cn('h-3 w-3 rounded-full', color)} />
                  <h3 className='font-semibold'>{label}</h3>
                </div>
                <span className='rounded-full bg-background px-2 py-1 text-sm text-muted-foreground'>
                  {leadsByStatus[status]?.length || 0}
                </span>
              </div>

              <div className='space-y-3'>
                {leadsByStatus[status]?.map((lead) => (
                  <LeadCard
                    key={lead.id}
                    lead={lead}
                    variant='kanban'
                    onClick={() => router.push(`/crm/leads/${lead.id}`)}
                    onConvert={
                      status === 'QUALIFIED' ? () => handleConvertLead(lead.id) : undefined
                    }
                  />
                ))}
                {(!leadsByStatus[status] || leadsByStatus[status].length === 0) && (
                  <p className='py-8 text-center text-sm text-muted-foreground'>No leads</p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

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
              onClick={() => router.push(`/crm/leads/${lead.id}`)}
              onEdit={() => router.push(`/crm/leads/${lead.id}/edit`)}
              onDelete={() => handleDeleteLead(lead.id)}
              onConvert={
                lead.leadStatus === 'QUALIFIED'
                  ? () => handleConvertLead(lead.id)
                  : undefined
              }
            />
          ))}
        </div>
      )}

      {!isLoading && leads.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted'>
              <Target className='h-10 w-10 text-muted-foreground' />
            </div>
            <h3 className='mb-2 text-lg font-semibold'>No leads found</h3>
            <p className='mx-auto mb-6 max-w-sm text-muted-foreground'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first lead.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => setShowQuickAdd(true)}>
                <Plus className='mr-2 h-4 w-4' />
                Add First Lead
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {viewMode !== 'kanban' && total > pageSize && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {(currentPage - 1) * pageSize + 1} to {Math.min(currentPage * pageSize, total)} of {total} leads
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

      <QuickAddLeadDialog
        open={showQuickAdd}
        onOpenChange={setShowQuickAdd}
        onSubmit={handleQuickAddLead}
        isLoading={isCreating || isDeleting}
      />
    </div>
  );
};

export default LeadListPage;
