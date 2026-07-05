// CustomerListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

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
  Users,
  Building2,
  ChevronLeft,
  ChevronRight,
  X,
} from 'lucide-react';
import { cn, getVisiblePages } from '@/shared/utils';
import { AccountCard } from '../../components/cards';
import { AccountForm } from '../../components/forms';
import { formatCurrency } from '../../utils';
import { StatsCard } from '../../components/dashboard';
import { ExportDropdown } from '../../components/shared';
import { QuickAddAccountDialog } from '../../components/dialogs';
import { CUSTOMER_EXPORT_COLUMNS } from '../../utils/export';
import {
  useCreateAccountMutation,
  useDeleteAccountMutation,
  useGetAccountsQuery,
  useUpdateAccountMutation,
} from '../../api/crmApi';
import type {
  Account,
  AccountStatus,
  AccountTier,
  CreateAccountRequest,
  UpdateAccountRequest,
} from '../../types';

interface CustomerListPageProps {
  className?: string;
}

export const CustomerListPage: React.FC<CustomerListPageProps> = ({
  className,
}) => {
  const router = useRouter();

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<AccountStatus | ''>('');
  const [typeFilter, setTypeFilter] = useState<'PROSPECT' | 'CUSTOMER' | ''>(
    ''
  );
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'totalValue'>(
    'name'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [currentPage, setCurrentPage] = useState(1);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Account | null>(null);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const debouncedSearchQuery = useDebounce(searchQuery, 400);

  const pageSize = 12;
  const [createAccount] = useCreateAccountMutation();
  const [updateAccount] = useUpdateAccountMutation();
  const [deleteAccount] = useDeleteAccountMutation();
  const { data, isLoading, error } = useGetAccountsQuery({
    filters: {
      search: debouncedSearchQuery || undefined,
      status: statusFilter ? [statusFilter] : undefined,
      type: typeFilter ? [typeFilter] : undefined,
    },
    pagination: {
      page: currentPage,
      limit: pageSize,
      sortBy,
      sortOrder,
    },
  });

  const accounts = data?.data?.data || [];
  const total = data?.data?.pagination?.total || 0;
  const totalPages = data?.data?.pagination?.totalPages || 1;

  // Calculate stats
  const stats = useMemo(() => {
    return {
      total,
      active: accounts.filter((c) => c.status === 'ACTIVE').length,
      companies: accounts.filter((c) => c.customerType === 'CUSTOMER').length,
      totalValue: accounts.reduce((sum, c) => sum + (c.totalValue || 0), 0),
    };
  }, [accounts, total]);

  // Handle actions
  const handleCreateCustomer = async (
    data: CreateAccountRequest | Partial<CreateAccountRequest>
  ) => {
    try {
      await createAccount(data as CreateAccountRequest).unwrap();
      toast.success('Create account successfully');
      setShowCreateForm(false);
    } catch (error) {
      toast.error('Failed to create account', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleQuickAddCustomer = async (data: {
    name: string;
    email: string;
    phone?: string;
    tier?: AccountTier;
    companySize?: string;
    customerType: 'PROSPECT' | 'CUSTOMER';
    status: 'ACTIVE' | 'INACTIVE';
    address?: string;
    city?: string;
    state?: string;
    zipCode?: string;
    country?: string;
    website?: string;
    notes?: string;
  }) => {
    try {
      await createAccount({
        isActive: data.status === 'ACTIVE',
        name: data.name,
        email: data.email,
        phone: data.phone,
        tier: data.tier,
        companySize: data.companySize,
        notes: data.notes,
        address: data.address || '',
        city: data.city || '',
        state: data.state || '',
        zipCode: data.zipCode || '',
        country: data.country || '',
        website: data.website || '',
        customerType: data.customerType,
        status: data.status,
        paymentTerms: '',
        creditLimit: undefined,
        tags: [],
        customFields: {},
      } as CreateAccountRequest).unwrap();
      toast.success('Create account successfully');
      setShowQuickAdd(false);
    } catch (error) {
      toast.error('Failed to create account', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleEditCustomer = (customer: Account) => {
    setEditingCustomer(customer);
  };

  const handleUpdateCustomer = async (
    data: CreateAccountRequest | UpdateAccountRequest
  ) => {
    if (!editingCustomer) return;
    try {
      await updateAccount({ id: editingCustomer.id, data }).unwrap();
      toast.success('Update account successfully');
      setEditingCustomer(null);
    } catch (error) {
      toast.error('Failed to update account', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeleteCustomer = async (customerId: string) => {
    try {
      await deleteAccount(customerId).unwrap();
      toast.success('Delete account successfully');
    } catch (error) {
      toast.error('Failed to delete account', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleViewCustomer = (customerId: string) => {
    router.push(`/crm/accounts/${customerId}`);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    setTypeFilter('');
    setCurrentPage(1);
  };

  const hasActiveFilters = searchQuery || statusFilter || typeFilter;

  // Show create/edit form
  if (showCreateForm || editingCustomer) {
    return (
      <div className={cn('', className)}>
        <AccountForm
          customer={editingCustomer || undefined}
          onSubmit={
            editingCustomer ? handleUpdateCustomer : handleCreateCustomer
          }
          onCancel={() => {
            setShowCreateForm(false);
            setEditingCustomer(null);
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
          <h1 className='text-2xl font-bold tracking-tight'>Accounts</h1>
          <p className='text-muted-foreground'>
            Manage your account relationships
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <ExportDropdown
            data={accounts}
            columns={CUSTOMER_EXPORT_COLUMNS}
            filename='accounts'
            onExportComplete={(format, count) => {
              console.log(`Exported ${count} accounts as ${format}`);
            }}
          />
          <Button onClick={() => setShowQuickAdd(true)} className='gap-2'>
            <Plus className='h-4 w-4' />
            Add Account
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Total Accounts'
          value={stats.total}
          icon={Users}
          variant='primary'
        />
        <StatsCard
          title='Active'
          value={stats.active}
          icon={Users}
          variant='success'
        />
        <StatsCard
          title='Companies'
          value={stats.companies}
          icon={Building2}
          variant='default'
        />
        <StatsCard
          title='Total Value'
          value={formatCurrency(stats.totalValue)}
          icon={Building2}
          variant='warning'
        />
      </div>

      {/* Search & Filters Bar */}
      <div className='flex flex-col sm:flex-row gap-3'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search accounts by name, email, or company...'
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
                    setStatusFilter(e.target.value as AccountStatus | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Statuses</option>
                  <option value='ACTIVE'>Active</option>
                  <option value='INACTIVE'>Inactive</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>Type</label>
                <select
                  value={typeFilter}
                  onChange={(e) => {
                    setTypeFilter(
                      e.target.value as 'PROSPECT' | 'CUSTOMER' | ''
                    );
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Types</option>
                  <option value='PROSPECT'>Prospect</option>
                  <option value='CUSTOMER'>Customer</option>
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
                  <option value='name-asc'>Name A-Z</option>
                  <option value='name-desc'>Name Z-A</option>
                  <option value='createdAt-desc'>Newest First</option>
                  <option value='createdAt-asc'>Oldest First</option>
                  <option value='totalValue-desc'>Highest Value</option>
                  <option value='totalValue-asc'>Lowest Value</option>
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
              Error loading accounts. Please try again.
            </p>
          </CardContent>
        </Card>
      )}

      {/* Loading State */}
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

      {/* Account Grid/List */}
      {!isLoading && accounts.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {accounts.map((customer) => (
            <AccountCard
              key={customer.id}
              customer={customer}
              variant={viewMode === 'list' ? 'compact' : 'default'}
              onClick={() => handleViewCustomer(customer.id)}
              onEdit={() => handleEditCustomer(customer)}
              onDelete={() => handleDeleteCustomer(customer.id)}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && accounts.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Users className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>No accounts found</h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first account.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => setShowCreateForm(true)}>
                <Plus className='h-4 w-4 mr-2' />
                Add First Account
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pagination */}
      {total > pageSize && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {(currentPage - 1) * pageSize + 1} to{' '}
            {Math.min(currentPage * pageSize, total)} of {total} accounts
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
              {getVisiblePages(currentPage, totalPages).map((pageNum) => (
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
              ))}
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
      <QuickAddAccountDialog
        open={showQuickAdd}
        onOpenChange={setShowQuickAdd}
        onSubmit={handleQuickAddCustomer}
      />
    </div>
  );
};

export default CustomerListPage;
