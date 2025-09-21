// LeadListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
import { Button, Card, CardContent, CardHeader } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetLeadsQuery } from '../../api/crmApi';
import {
  EntityCard,
  SearchInput,
  StatusBadge,
  ActionMenu,
} from '../../components/shared';
import { LeadForm } from '../../components/forms';
import type { Lead, LeadFilters, LeadStatus, LeadSource } from '../../types';

interface LeadListPageProps {
  className?: string;
}

export const LeadListPage: React.FC<LeadListPageProps> = ({ className }) => {
  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<LeadStatus | ''>('');
  const [sourceFilter, setSourceFilter] = useState<LeadSource | ''>('');
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'score'>(
    'createdAt'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingLead, setEditingLead] = useState<Lead | null>(null);

  const pageSize = 12;

  // Build filters
  const filters: LeadFilters = useMemo(
    () => ({
      search: searchQuery || undefined,
      status: statusFilter ? [statusFilter] : undefined,
      source: sourceFilter ? [sourceFilter] : undefined,
    }),
    [searchQuery, statusFilter, sourceFilter]
  );

  // Fetch leads
  const {
    data: leadsResponse,
    isLoading,
    error,
  } = useGetLeadsQuery({
    filters,
    pagination: { page: currentPage, limit: pageSize, sortBy, sortOrder },
  });

  const leads = leadsResponse?.data?.data || [];
  const totalPages = leadsResponse?.data?.pagination?.totalPages || 0;
  const total = leadsResponse?.data?.pagination?.total || 0;

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

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(1);
  };

  // Show create/edit form
  if (showCreateForm || editingLead) {
    return (
      <div className={cn('p-6', className)}>
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
    <div className={cn('p-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <div>
          <h1 className='text-2xl font-bold'>Leads</h1>
          <p className='text-muted-foreground'>Manage your sales prospects</p>
        </div>
        <Button onClick={() => setShowCreateForm(true)}>Add Lead</Button>
      </div>

      {/* Filters and Search */}
      <Card className='mb-6'>
        <CardHeader>
          <h3 className='text-lg font-semibold'>Filters</h3>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
            <div>
              <SearchInput
                placeholder='Search leads...'
                onSearch={handleSearch}
                value={searchQuery}
              />
            </div>

            <div>
              <select
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value as LeadStatus | '');
                  setCurrentPage(1);
                }}
                className='w-full px-3 py-2 border border-border rounded-md bg-background focus:outline-none focus:ring-2 focus:ring-ring'
              >
                <option value=''>All Statuses</option>
                <option value='NEW'>New</option>
                <option value='CONTACTED'>Contacted</option>
                <option value='QUALIFIED'>Qualified</option>
                <option value='UNQUALIFIED'>Unqualified</option>
                <option value='CONVERTED'>Converted</option>
              </select>
            </div>

            <div>
              <select
                value={sourceFilter}
                onChange={(e) => {
                  setSourceFilter(e.target.value as LeadSource | '');
                  setCurrentPage(1);
                }}
                className='w-full px-3 py-2 border border-border rounded-md bg-background focus:outline-none focus:ring-2 focus:ring-ring'
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
                <option value='score-desc'>Highest Score</option>
                <option value='score-asc'>Lowest Score</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Quick Stats */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4 mb-6'>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-blue-600'>{total}</div>
            <p className='text-sm text-muted-foreground'>Total Leads</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-green-600'>
              {leads.filter((l) => l.status === 'QUALIFIED').length}
            </div>
            <p className='text-sm text-muted-foreground'>Qualified</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-yellow-600'>
              {leads.filter((l) => l.status === 'CONTACTED').length}
            </div>
            <p className='text-sm text-muted-foreground'>In Progress</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className='p-4'>
            <div className='text-2xl font-bold text-purple-600'>
              {leads.filter((l) => l.status === 'CONVERTED').length}
            </div>
            <p className='text-sm text-muted-foreground'>Converted</p>
          </CardContent>
        </Card>
      </div>

      {/* Results Summary */}
      <div className='flex items-center justify-between mb-4'>
        <p className='text-sm text-muted-foreground'>
          {isLoading ? 'Loading...' : `${total} leads found`}
        </p>

        {total > pageSize && (
          <div className='flex items-center space-x-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(currentPage - 1)}
            >
              Previous
            </Button>
            <span className='text-sm text-muted-foreground'>
              Page {currentPage} of {totalPages}
            </span>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage(currentPage + 1)}
            >
              Next
            </Button>
          </div>
        )}
      </div>

      {/* Error State */}
      {error && (
        <Card className='mb-6 border-red-200 bg-red-50'>
          <CardContent className='p-4'>
            <p className='text-red-600'>
              Error loading leads. Please try again.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Loading State */}
      {isLoading && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {Array.from({ length: 6 }).map((_, index) => (
            <Card key={index} className='animate-pulse'>
              <CardContent className='p-4'>
                <div className='space-y-3'>
                  <div className='h-4 bg-muted rounded w-3/4'></div>
                  <div className='h-3 bg-muted rounded w-1/2'></div>
                  <div className='h-3 bg-muted rounded w-2/3'></div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Lead Grid */}
      {!isLoading && leads.length > 0 && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {leads.map((lead) => (
            <div key={lead.id} className='relative'>
              <EntityCard
                type='lead'
                entity={lead}
                onClick={() => {
                  console.log('View lead:', lead.id);
                }}
                onEdit={() => handleEditLead(lead)}
                onDelete={() => handleDeleteLead(lead.id)}
                className='h-full'
              />
              {/* Quick Convert Button for Qualified Leads */}
              {lead.status === 'QUALIFIED' && (
                <div className='absolute top-2 right-2'>
                  <Button
                    size='sm'
                    variant='outline'
                    className='bg-green-50 border-green-200 text-green-700 hover:bg-green-100'
                    onClick={(e) => {
                      e.stopPropagation();
                      handleConvertLead(lead.id);
                    }}
                  >
                    Convert
                  </Button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && leads.length === 0 && !error && (
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
            <h3 className='text-lg font-semibold mb-2'>No leads found</h3>
            <p className='text-muted-foreground mb-4'>
              {searchQuery || statusFilter || sourceFilter
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first lead.'}
            </p>
            <Button onClick={() => setShowCreateForm(true)}>
              Add First Lead
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default LeadListPage;
