// CustomerListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Card, CardContent, Input } from '@/shared/components/ui';
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
import { cn } from '@/shared/utils';
import { CustomerCard } from '../../components/cards';
import { CustomerForm } from '../../components/forms';
import { StatsCard } from '../../components/dashboard';
import { ExportDropdown } from '../../components/shared';
import { QuickAddCustomerDialog } from '../../components/dialogs';
import { CUSTOMER_EXPORT_COLUMNS } from '../../utils/export';
import { MOCK_CUSTOMERS } from '../../mocks';
import type { Customer, CustomerFilters, CustomerStatus } from '../../types';

interface CustomerListPageProps {
  className?: string;
}

export const CustomerListPage: React.FC<CustomerListPageProps> = ({
  className,
}) => {
  const router = useRouter();

  // State management
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<CustomerStatus | ''>('');
  const [typeFilter, setTypeFilter] = useState<'INDIVIDUAL' | 'COMPANY' | ''>(
    ''
  );
  const [sortBy, setSortBy] = useState<'name' | 'createdAt' | 'totalValue'>(
    'name'
  );
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [currentPage, setCurrentPage] = useState(1);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [showQuickAdd, setShowQuickAdd] = useState(false);

  const pageSize = 12;

  // Filter and sort mock data
  const filteredCustomers = useMemo(() => {
    let result = [...MOCK_CUSTOMERS];

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(
        (c) =>
          c.name.toLowerCase().includes(query) ||
          c.email.toLowerCase().includes(query) ||
          c.companyName?.toLowerCase().includes(query)
      );
    }

    // Apply status filter
    if (statusFilter) {
      result = result.filter((c) => c.status === statusFilter);
    }

    // Apply type filter
    if (typeFilter) {
      result = result.filter((c) => c.customerType === typeFilter);
    }

    // Apply sorting
    result.sort((a, b) => {
      let comparison = 0;
      if (sortBy === 'name') {
        comparison = a.name.localeCompare(b.name);
      } else if (sortBy === 'createdAt') {
        comparison =
          new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      } else if (sortBy === 'totalValue') {
        comparison = a.totalValue - b.totalValue;
      }
      return sortOrder === 'asc' ? comparison : -comparison;
    });

    return result;
  }, [searchQuery, statusFilter, typeFilter, sortBy, sortOrder]);

  // Pagination
  const total = filteredCustomers.length;
  const totalPages = Math.ceil(total / pageSize);
  const customers = filteredCustomers.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );
  const isLoading = false;
  const error = null;

  // Calculate stats
  const stats = useMemo(() => {
    return {
      total,
      active: MOCK_CUSTOMERS.filter((c) => c.status === 'ACTIVE').length,
      companies: MOCK_CUSTOMERS.filter((c) => c.customerType === 'COMPANY')
        .length,
      totalValue: MOCK_CUSTOMERS.reduce(
        (sum, c) => sum + (c.totalValue || 0),
        0
      ),
    };
  }, [total]);

  // Handle actions
  const handleCreateCustomer = async (data: any) => {
    console.log('Creating customer:', data);
    setShowCreateForm(false);
  };

  const handleQuickAddCustomer = async (data: any) => {
    console.log('Quick adding customer:', data);
    setShowQuickAdd(false);
  };

  const handleEditCustomer = (customer: Customer) => {
    setEditingCustomer(customer);
  };

  const handleUpdateCustomer = async (data: any) => {
    console.log('Updating customer:', data);
    setEditingCustomer(null);
  };

  const handleDeleteCustomer = async (customerId: string) => {
    console.log('Deleting customer:', customerId);
  };

  const handleViewCustomer = (customerId: string) => {
    router.push(`/crm/customers/${customerId}`);
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
        <CustomerForm
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
          <h1 className='text-2xl font-bold tracking-tight'>Customers</h1>
          <p className='text-muted-foreground'>
            Manage your customer relationships
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <ExportDropdown
            data={filteredCustomers}
            columns={CUSTOMER_EXPORT_COLUMNS}
            filename='customers'
            onExportComplete={(format, count) => {
              console.log(`Exported ${count} customers as ${format}`);
            }}
          />
          <Button
            variant='outline'
            onClick={() => setShowQuickAdd(true)}
            className='gap-2'
          >
            <Plus className='h-4 w-4' />
            Thêm nhanh
          </Button>
          <Button onClick={() => setShowCreateForm(true)} className='gap-2'>
            <Plus className='h-4 w-4' />
            Add Customer
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-4 gap-4'>
        <StatsCard
          title='Total Customers'
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
          value={`$${stats.totalValue.toLocaleString()}`}
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
            placeholder='Search customers by name, email, or company...'
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
                    setStatusFilter(e.target.value as CustomerStatus | '');
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Statuses</option>
                  <option value='ACTIVE'>Active</option>
                  <option value='INACTIVE'>Inactive</option>
                  <option value='POTENTIAL'>Potential</option>
                  <option value='BLOCKED'>Blocked</option>
                </select>
              </div>

              <div>
                <label className='text-sm font-medium mb-1.5 block'>Type</label>
                <select
                  value={typeFilter}
                  onChange={(e) => {
                    setTypeFilter(
                      e.target.value as 'INDIVIDUAL' | 'COMPANY' | ''
                    );
                    setCurrentPage(1);
                  }}
                  className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                >
                  <option value=''>All Types</option>
                  <option value='INDIVIDUAL'>Individual</option>
                  <option value='COMPANY'>Company</option>
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
              Error loading customers. Please try again.
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

      {/* Customer Grid/List */}
      {!isLoading && customers.length > 0 && (
        <div
          className={cn(
            'gap-4',
            viewMode === 'grid'
              ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
              : 'flex flex-col'
          )}
        >
          {customers.map((customer) => (
            <CustomerCard
              key={customer.id}
              customer={customer}
              variant={viewMode === 'list' ? 'compact' : 'default'}
              onClick={() => handleViewCustomer(customer.id)}
              onEdit={() => handleEditCustomer(customer)}
              onDelete={() => handleDeleteCustomer(customer.id)}
              onEmailClick={() => console.log('Email:', customer.email)}
              onCallClick={() => console.log('Call:', customer.phone)}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && customers.length === 0 && !error && (
        <Card>
          <CardContent className='py-16 text-center'>
            <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
              <Users className='w-10 h-10 text-muted-foreground' />
            </div>
            <h3 className='text-lg font-semibold mb-2'>No customers found</h3>
            <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
              {hasActiveFilters
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first customer.'}
            </p>
            {hasActiveFilters ? (
              <Button variant='outline' onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button onClick={() => setShowCreateForm(true)}>
                <Plus className='h-4 w-4 mr-2' />
                Add First Customer
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
            {Math.min(currentPage * pageSize, total)} of {total} customers
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
      <QuickAddCustomerDialog
        open={showQuickAdd}
        onOpenChange={setShowQuickAdd}
        onSubmit={handleQuickAddCustomer}
      />
    </div>
  );
};

export default CustomerListPage;
