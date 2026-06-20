// LeadListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  DragDropContext,
  Draggable,
  Droppable,
  type DropResult,
} from '@hello-pangea/dnd';
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
import { getErrorMessage } from '@/lib/store/api';
import { useDebounce } from '@/shared/hooks/use-debounce';
import {
  Button,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Textarea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { LeadCard } from '../../components/cards';
import { formatCurrency } from '../../utils';
import { StatsCard } from '../../components/dashboard';
import { QuickAddLeadDialog } from '../../components/dialogs';
import { ExportDropdown } from '../../components/shared';
import {
  useCreateLeadMutation,
  useDeleteLeadMutation,
  useGetLeadsQuery,
  useUpdateLeadStatusMutation,
} from '../../api/crmApi';
import { LEAD_EXPORT_COLUMNS } from '../../utils/export';
import type {
  CreateLeadRequest,
  Lead,
  LeadSource,
  LeadStatus,
} from '../../types';

type BoardLeadStatus = Exclude<LeadStatus, 'LOST'>;
type PendingStatusDialog = {
  leadId: string;
  leadName: string;
  fromStatus: BoardLeadStatus;
  toStatus: Extract<BoardLeadStatus, 'QUALIFIED' | 'DISQUALIFIED'>;
};

const LEAD_STATUSES: {
  status: BoardLeadStatus;
  label: string;
  color: string;
  description?: string;
  dropDisabled?: boolean;
}[] = [
  { status: 'NEW', label: 'New', color: 'bg-blue-500' },
  { status: 'CONTACTED', label: 'Contacted', color: 'bg-yellow-500' },
  { status: 'NURTURING', label: 'Nurturing', color: 'bg-indigo-500' },
  {
    status: 'QUALIFIED',
    label: 'Qualified',
    color: 'bg-green-500',
    description: 'Dropping here asks for qualification notes.',
  },
  {
    status: 'DISQUALIFIED',
    label: 'Disqualified',
    color: 'bg-red-500',
    description: 'Dropping here asks for disqualification notes.',
  },
  {
    status: 'CONVERTED',
    label: 'Converted',
    color: 'bg-purple-500',
    description: 'Use Convert on a qualified lead to finish conversion.',
    dropDisabled: true,
  },
];

const KANBAN_COLUMN_PAGE_SIZE = 20;

const getLeadStatus = (lead: Lead): BoardLeadStatus =>
  (lead.leadStatus || lead.status || 'NEW') as BoardLeadStatus;

const getLeadDisplayName = (lead: Lead): string => {
  const fullName = `${lead.firstName || ''} ${lead.lastName || ''}`.trim();
  return lead.name?.trim() || fullName || 'Unnamed Lead';
};

const getStatusLabel = (status: BoardLeadStatus): string =>
  LEAD_STATUSES.find((item) => item.status === status)?.label || status;

interface LeadListPageProps {
  className?: string;
}

export const LeadListPage: React.FC<LeadListPageProps> = ({ className }) => {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<LeadStatus | ''>('');
  const [sourceFilter, setSourceFilter] = useState<LeadSource | ''>('');
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'estimatedValue'>(
    'createdAt'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);
  const [viewMode, setViewMode] = useState<'grid' | 'list' | 'kanban'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const [pendingStatusDialog, setPendingStatusDialog] =
    useState<PendingStatusDialog | null>(null);
  const [statusNotes, setStatusNotes] = useState('');

  const debouncedSearchQuery = useDebounce(searchQuery, 400);
  const isKanbanView = viewMode === 'kanban';
  const listPageSize = 12;

  const [createLead, { isLoading: isCreating }] = useCreateLeadMutation();
  const [deleteLead, { isLoading: isDeleting }] = useDeleteLeadMutation();
  const [updateLeadStatus, { isLoading: isUpdatingStatus }] =
    useUpdateLeadStatusMutation();

  const listQueryArgs = useMemo(
    () => ({
      filters: {
        search: debouncedSearchQuery || undefined,
        status: statusFilter ? [statusFilter] : undefined,
        source: sourceFilter ? [sourceFilter] : undefined,
      },
      pagination: {
        page: currentPage,
        limit: listPageSize,
        sortBy,
        sortOrder,
      },
    }),
    [
      currentPage,
      debouncedSearchQuery,
      listPageSize,
      sortBy,
      sortOrder,
      sourceFilter,
      statusFilter,
    ]
  );

  const kanbanBaseFilters = useMemo(
    () => ({
      search: debouncedSearchQuery || undefined,
      source: sourceFilter ? [sourceFilter] : undefined,
    }),
    [debouncedSearchQuery, sourceFilter]
  );

  const kanbanPagination = useMemo(
    () => ({
      page: 1,
      limit: KANBAN_COLUMN_PAGE_SIZE,
      sortBy,
      sortOrder,
    }),
    [sortBy, sortOrder]
  );

  const listQuery = useGetLeadsQuery(listQueryArgs, {
    skip: isKanbanView,
  });

  const newLeadsQuery = useGetLeadsQuery(
    {
      filters: { ...kanbanBaseFilters, status: ['NEW'] },
      pagination: kanbanPagination,
    },
    { skip: !isKanbanView }
  );
  const contactedLeadsQuery = useGetLeadsQuery(
    {
      filters: { ...kanbanBaseFilters, status: ['CONTACTED'] },
      pagination: kanbanPagination,
    },
    { skip: !isKanbanView }
  );
  const nurturingLeadsQuery = useGetLeadsQuery(
    {
      filters: { ...kanbanBaseFilters, status: ['NURTURING'] },
      pagination: kanbanPagination,
    },
    { skip: !isKanbanView }
  );
  const qualifiedLeadsQuery = useGetLeadsQuery(
    {
      filters: { ...kanbanBaseFilters, status: ['QUALIFIED'] },
      pagination: kanbanPagination,
    },
    { skip: !isKanbanView }
  );
  const disqualifiedLeadsQuery = useGetLeadsQuery(
    {
      filters: { ...kanbanBaseFilters, status: ['DISQUALIFIED'] },
      pagination: kanbanPagination,
    },
    { skip: !isKanbanView }
  );
  const convertedLeadsQuery = useGetLeadsQuery(
    {
      filters: { ...kanbanBaseFilters, status: ['CONVERTED'] },
      pagination: kanbanPagination,
    },
    { skip: !isKanbanView }
  );

  const kanbanQueries = [
    newLeadsQuery,
    contactedLeadsQuery,
    nurturingLeadsQuery,
    qualifiedLeadsQuery,
    disqualifiedLeadsQuery,
    convertedLeadsQuery,
  ];

  const leads = listQuery.data?.data.data || [];
  const pagination = listQuery.data?.data.pagination;
  const kanbanColumns = useMemo(
    () => [
      {
        ...LEAD_STATUSES[0],
        leads: newLeadsQuery.data?.data.data || [],
        total: newLeadsQuery.data?.data.pagination.total || 0,
      },
      {
        ...LEAD_STATUSES[1],
        leads: contactedLeadsQuery.data?.data.data || [],
        total: contactedLeadsQuery.data?.data.pagination.total || 0,
      },
      {
        ...LEAD_STATUSES[2],
        leads: nurturingLeadsQuery.data?.data.data || [],
        total: nurturingLeadsQuery.data?.data.pagination.total || 0,
      },
      {
        ...LEAD_STATUSES[3],
        leads: qualifiedLeadsQuery.data?.data.data || [],
        total: qualifiedLeadsQuery.data?.data.pagination.total || 0,
      },
      {
        ...LEAD_STATUSES[4],
        leads: disqualifiedLeadsQuery.data?.data.data || [],
        total: disqualifiedLeadsQuery.data?.data.pagination.total || 0,
      },
      {
        ...LEAD_STATUSES[5],
        leads: convertedLeadsQuery.data?.data.data || [],
        total: convertedLeadsQuery.data?.data.pagination.total || 0,
      },
    ],
    [
      contactedLeadsQuery.data,
      convertedLeadsQuery.data,
      disqualifiedLeadsQuery.data,
      newLeadsQuery.data,
      nurturingLeadsQuery.data,
      qualifiedLeadsQuery.data,
    ]
  );

  const kanbanLeads = useMemo(
    () => kanbanColumns.flatMap((column) => column.leads),
    [kanbanColumns]
  );
  const kanbanLeadMap = useMemo(
    () => new Map(kanbanLeads.map((lead) => [lead.id, lead])),
    [kanbanLeads]
  );
  const exportLeads = isKanbanView ? kanbanLeads : leads;
  const total = isKanbanView
    ? kanbanColumns.reduce((sum, column) => sum + column.total, 0)
    : pagination?.total || 0;
  const totalPages = pagination?.totalPages || 1;

  const stats = useMemo(() => {
    const statusTotals = isKanbanView
      ? Object.fromEntries(
          kanbanColumns.map((column) => [column.status, column.total])
        )
      : {
          NEW: leads.filter((lead) => getLeadStatus(lead) === 'NEW').length,
          CONTACTED: leads.filter((lead) => getLeadStatus(lead) === 'CONTACTED')
            .length,
          NURTURING: leads.filter((lead) => getLeadStatus(lead) === 'NURTURING')
            .length,
          QUALIFIED: leads.filter((lead) => getLeadStatus(lead) === 'QUALIFIED')
            .length,
          DISQUALIFIED: leads.filter(
            (lead) => getLeadStatus(lead) === 'DISQUALIFIED'
          ).length,
          CONVERTED: leads.filter((lead) => getLeadStatus(lead) === 'CONVERTED')
            .length,
        };
    const valueSource = isKanbanView ? kanbanLeads : leads;

    return {
      total,
      new: Number(statusTotals.NEW || 0),
      qualified: Number(statusTotals.QUALIFIED || 0),
      contacted: Number(statusTotals.CONTACTED || 0),
      converted: Number(statusTotals.CONVERTED || 0),
      avgValue: valueSource.length
        ? Math.round(
            valueSource.reduce(
              (sum, lead) => sum + (lead.estimatedValue || 0),
              0
            ) / valueSource.length
          )
        : 0,
    };
  }, [isKanbanView, kanbanColumns, kanbanLeads, leads, total]);

  const isListLoading =
    !isKanbanView && (listQuery.isLoading || listQuery.isFetching);
  const isKanbanLoading =
    isKanbanView &&
    kanbanColumns.every((column) => column.leads.length === 0) &&
    kanbanQueries.some((query) => query.isLoading || query.isFetching);
  const hasError = isKanbanView
    ? kanbanQueries.some((query) => Boolean(query.error))
    : Boolean(listQuery.error);
  const hasActiveFilters =
    !!searchQuery || !!sourceFilter || (!isKanbanView && !!statusFilter);
  const visibleLeadCount = isKanbanView ? total : leads.length;
  const isSubmittingStatusAction = isUpdatingStatus;

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

  const closePendingStatusDialog = () => {
    setPendingStatusDialog(null);
    setStatusNotes('');
  };

  const moveLeadToWorkingStatus = async (
    leadId: string,
    fromStatus: BoardLeadStatus,
    toStatus: BoardLeadStatus
  ) => {
    const result = await updateLeadStatus({
      id: leadId,
      data: {
        fromStatus,
        toStatus,
      },
    }).unwrap();

    toast.success(
      result.data.message || `Moved lead to ${getStatusLabel(toStatus)}`
    );
  };

  const handleSubmitPendingStatusDialog = async () => {
    if (!pendingStatusDialog) return;

    const notes = statusNotes.trim();
    if (!notes) return;

    try {
      const result = await updateLeadStatus({
        id: pendingStatusDialog.leadId,
        data: {
          fromStatus: pendingStatusDialog.fromStatus,
          toStatus: pendingStatusDialog.toStatus,
          notes,
        },
      }).unwrap();

      toast.success(
        result.data.message ||
          `Moved lead to ${getStatusLabel(pendingStatusDialog.toStatus)}`
      );

      closePendingStatusDialog();
    } catch (error) {
      toast.error('Failed to update lead status', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleKanbanDragEnd = async ({
    destination,
    draggableId,
    source,
  }: DropResult) => {
    if (!destination) return;

    const fromStatus = source.droppableId as BoardLeadStatus;
    const toStatus = destination.droppableId as BoardLeadStatus;

    if (fromStatus === toStatus) return;

    const leadId = draggableId.replace('lead-', '');
    const lead = kanbanLeadMap.get(leadId);
    if (!lead) return;

    const currentStatus = getLeadStatus(lead);
    if (currentStatus !== fromStatus) {
      toast.error('Lead status is outdated. Please refresh and try again.');
      return;
    }

    if (currentStatus === 'CONVERTED') {
      toast.error('Converted leads cannot be moved.');
      return;
    }

    if (toStatus === 'CONVERTED') {
      if (currentStatus !== 'QUALIFIED') {
        toast.error('Only qualified leads can be converted.');
        return;
      }

      toast.info('Complete conversion from the lead details page.');
      router.push(`/crm/leads/${leadId}`);
      return;
    }

    if (toStatus === 'QUALIFIED' || toStatus === 'DISQUALIFIED') {
      setPendingStatusDialog({
        leadId,
        leadName: getLeadDisplayName(lead),
        fromStatus,
        toStatus,
      });
      setStatusNotes('');
      return;
    }

    try {
      await moveLeadToWorkingStatus(leadId, fromStatus, toStatus);
    } catch (error) {
      toast.error('Failed to move lead', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <>
      <div className={cn('space-y-6', className)}>
        <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>Leads</h1>
            <p className='text-muted-foreground'>
              Manage and convert your sales prospects
            </p>
          </div>
          <div className='flex items-center gap-2'>
            <ExportDropdown
              data={exportLeads}
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
            value={formatCurrency(stats.avgValue)}
            icon={Target}
            variant='danger'
          />
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
            {hasActiveFilters && (
              <span className='h-2 w-2 rounded-full bg-primary' />
            )}
          </Button>

          <div className='flex rounded-lg border bg-muted p-1'>
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
            <button
              onClick={() => setViewMode('kanban')}
              className={cn(
                'flex h-8 w-8 items-center justify-center rounded-md transition-colors',
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

        {showFilters && (
          <Card>
            <CardContent className='p-4'>
              <div className='grid grid-cols-1 gap-4 sm:grid-cols-3'>
                <div>
                  <label className='mb-1.5 block text-sm font-medium'>
                    Status
                  </label>
                  <select
                    value={statusFilter}
                    disabled={isKanbanView}
                    onChange={(e) => {
                      setStatusFilter(e.target.value as LeadStatus | '');
                      setCurrentPage(1);
                    }}
                    className='w-full rounded-lg border border-border bg-background px-3 py-2 disabled:cursor-not-allowed disabled:bg-muted'
                  >
                    <option value=''>All Statuses</option>
                    {LEAD_STATUSES.map((option) => (
                      <option key={option.status} value={option.status}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  {isKanbanView && (
                    <p className='mt-1 text-xs text-muted-foreground'>
                      Kanban groups leads by status, so this filter is disabled
                      in board view.
                    </p>
                  )}
                </div>

                <div>
                  <label className='mb-1.5 block text-sm font-medium'>
                    Source
                  </label>
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
                  <label className='mb-1.5 block text-sm font-medium'>
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

        {hasError && (
          <Card className='border-destructive/50 bg-destructive/5'>
            <CardContent className='p-4'>
              <p className='text-destructive'>
                Error loading leads. Please try again.
              </p>
            </CardContent>
          </Card>
        )}

        {(isListLoading || isKanbanLoading) && (
          <div
            className={cn(
              'gap-4',
              isKanbanView
                ? 'grid min-w-[1320px] grid-cols-6'
                : viewMode === 'grid'
                  ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
                  : 'flex flex-col'
            )}
          >
            {Array.from({ length: isKanbanView ? 6 : 6 }).map((_, index) => (
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

        {!isKanbanLoading && isKanbanView && visibleLeadCount > 0 && (
          <DragDropContext onDragEnd={handleKanbanDragEnd}>
            <div className='overflow-x-auto pb-2'>
              <div className='grid min-w-[1320px] grid-cols-6 gap-4'>
                {kanbanColumns.map(
                  ({
                    status,
                    label,
                    color,
                    description,
                    dropDisabled,
                    leads: columnLeads,
                    total: columnTotal,
                  }) => (
                    <div
                      key={status}
                      className='rounded-xl border bg-muted/20 p-4'
                    >
                      <div className='mb-4 flex items-start justify-between gap-3'>
                        <div>
                          <div className='flex items-center gap-2'>
                            <div
                              className={cn('h-3 w-3 rounded-full', color)}
                            />
                            <h3 className='font-semibold'>{label}</h3>
                          </div>
                          {description && (
                            <p className='mt-1 text-xs text-muted-foreground'>
                              {description}
                            </p>
                          )}
                        </div>
                        <span className='rounded-full bg-background px-2 py-1 text-sm text-muted-foreground'>
                          {columnTotal}
                        </span>
                      </div>

                      <Droppable
                        droppableId={status}
                        isDropDisabled={dropDisabled}
                      >
                        {(provided, snapshot) => (
                          <div
                            ref={provided.innerRef}
                            {...provided.droppableProps}
                            className={cn(
                              'min-h-[420px] space-y-3 rounded-lg border border-dashed border-transparent transition-colors',
                              snapshot.isDraggingOver &&
                                !dropDisabled &&
                                'border-primary bg-primary/5',
                              dropDisabled && 'opacity-80'
                            )}
                          >
                            {columnLeads.map((lead, index) => (
                              <Draggable
                                key={lead.id}
                                draggableId={`lead-${lead.id}`}
                                index={index}
                              >
                                {(dragProvided, dragSnapshot) => (
                                  <div
                                    ref={dragProvided.innerRef}
                                    {...dragProvided.draggableProps}
                                    {...dragProvided.dragHandleProps}
                                    className={cn(
                                      dragSnapshot.isDragging &&
                                        'rotate-1 opacity-95'
                                    )}
                                  >
                                    <LeadCard
                                      lead={lead}
                                      variant='kanban'
                                      onClick={() =>
                                        router.push(`/crm/leads/${lead.id}`)
                                      }
                                      onConvert={
                                        getLeadStatus(lead) === 'QUALIFIED'
                                          ? () => handleConvertLead(lead.id)
                                          : undefined
                                      }
                                    />
                                  </div>
                                )}
                              </Draggable>
                            ))}
                            {provided.placeholder}
                            {columnLeads.length === 0 && (
                              <p className='rounded-lg border border-dashed py-8 text-center text-sm text-muted-foreground'>
                                No leads
                              </p>
                            )}
                            {columnTotal > columnLeads.length && (
                              <p className='text-center text-xs text-muted-foreground'>
                                Showing {columnLeads.length} of {columnTotal}{' '}
                                leads
                              </p>
                            )}
                          </div>
                        )}
                      </Droppable>
                    </div>
                  )
                )}
              </div>
            </div>
          </DragDropContext>
        )}

        {!isListLoading && viewMode !== 'kanban' && leads.length > 0 && (
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
                  getLeadStatus(lead) === 'QUALIFIED'
                    ? () => handleConvertLead(lead.id)
                    : undefined
                }
              />
            ))}
          </div>
        )}

        {!isListLoading &&
          !isKanbanLoading &&
          visibleLeadCount === 0 &&
          !hasError && (
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

        {viewMode !== 'kanban' && total > listPageSize && (
          <div className='flex items-center justify-between pt-4'>
            <p className='text-sm text-muted-foreground'>
              Showing {(currentPage - 1) * listPageSize + 1} to{' '}
              {Math.min(currentPage * listPageSize, total)} of {total} leads
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

      <Dialog
        open={!!pendingStatusDialog}
        onOpenChange={(open) => {
          if (!open) {
            closePendingStatusDialog();
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {pendingStatusDialog?.toStatus === 'QUALIFIED'
                ? 'Qualify lead'
                : 'Disqualify lead'}
            </DialogTitle>
            <DialogDescription>
              {pendingStatusDialog
                ? `Add a note before moving ${pendingStatusDialog.leadName} to ${getStatusLabel(
                    pendingStatusDialog.toStatus
                  )}.`
                : 'Add a note before changing this lead status.'}
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-2'>
            <label className='text-sm font-medium'>Notes</label>
            <Textarea
              value={statusNotes}
              onChange={(event) => setStatusNotes(event.target.value)}
              placeholder='Explain why this status change is happening...'
              rows={5}
            />
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={closePendingStatusDialog}>
              Cancel
            </Button>
            <Button
              onClick={handleSubmitPendingStatusDialog}
              disabled={!statusNotes.trim() || isSubmittingStatusAction}
            >
              {pendingStatusDialog?.toStatus === 'QUALIFIED'
                ? 'Confirm Qualification'
                : 'Confirm Disqualification'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
};

export default LeadListPage;
