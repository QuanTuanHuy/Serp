// CustomerListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  Input,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useGetCustomersQuery } from '../../api/crmApi';
import {
  EntityCard,
  SearchInput,
  StatusBadge,
  ActionMenu,
} from '../../components/shared';
import { CustomerForm } from '../../components/forms';
import type { Customer, CustomerFilters, CustomerStatus } from '../../types';

interface CustomerListPageProps {
  className?: string;
}

export const CustomerListPage: React.FC<CustomerListPageProps> = ({
  className,
}) => {
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

  const pageSize = 12;

  // Build filters
  const filters: CustomerFilters = useMemo(
    () => ({
      search: searchQuery || undefined,
      status: statusFilter ? [statusFilter] : undefined,
      type: typeFilter ? [typeFilter] : undefined,
    }),
    [searchQuery, statusFilter, typeFilter]
  );

  // Fetch customers
  const {
    data: customersResponse,
    isLoading,
    error,
  } = useGetCustomersQuery({
    filters,
    pagination: { page: currentPage, limit: pageSize, sortBy, sortOrder },
  });

  const customers = customersResponse?.data?.data || [];
  const totalPages = customersResponse?.data?.pagination?.totalPages || 0;
  const total = customersResponse?.data?.pagination?.total || 0;

  // Handle actions
  const handleCreateCustomer = async (data: any) => {
    // Implementation will use createCustomerMutation
    console.log('Creating customer:', data);
    setShowCreateForm(false);
  };

  const handleEditCustomer = (customer: Customer) => {
    setEditingCustomer(customer);
  };

  const handleUpdateCustomer = async (data: any) => {
    // Implementation will use updateCustomerMutation
    console.log('Updating customer:', data);
    setEditingCustomer(null);
  };

  const handleDeleteCustomer = async (customerId: string) => {
    // Implementation will use deleteCustomerMutation
    console.log('Deleting customer:', customerId);
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(1);
  };

  const handleSort = (field: typeof sortBy) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('asc');
    }
    setCurrentPage(1);
  };

  // Show create/edit form
  if (showCreateForm || editingCustomer) {
    return (
      <div className={cn('p-6', className)}>
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
    <div className={cn('p-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <div>
          <h1 className='text-2xl font-bold'>Customers</h1>
          <p className='text-muted-foreground'>
            Manage your customer relationships
          </p>
        </div>
        <Button onClick={() => setShowCreateForm(true)}>Add Customer</Button>
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
                placeholder='Search customers...'
                onSearch={handleSearch}
                value={searchQuery}
              />
            </div>

            <div>
              <select
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value as CustomerStatus | '');
                  setCurrentPage(1);
                }}
                className='w-full px-3 py-2 border border-border rounded-md bg-background focus:outline-none focus:ring-2 focus:ring-ring'
              >
                <option value=''>All Statuses</option>
                <option value='ACTIVE'>Active</option>
                <option value='INACTIVE'>Inactive</option>
                <option value='POTENTIAL'>Potential</option>
                <option value='BLOCKED'>Blocked</option>
              </select>
            </div>

            <div>
              <select
                value={typeFilter}
                onChange={(e) => {
                  setTypeFilter(
                    e.target.value as 'INDIVIDUAL' | 'COMPANY' | ''
                  );
                  setCurrentPage(1);
                }}
                className='w-full px-3 py-2 border border-border rounded-md bg-background focus:outline-none focus:ring-2 focus:ring-ring'
              >
                <option value=''>All Types</option>
                <option value='INDIVIDUAL'>Individual</option>
                <option value='COMPANY'>Company</option>
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
                <option value='name-asc'>Name A-Z</option>
                <option value='name-desc'>Name Z-A</option>
                <option value='createdAt-desc'>Newest First</option>
                <option value='createdAt-asc'>Oldest First</option>
                <option value='totalValue-desc'>Highest Value</option>
                <option value='totalValue-asc'>Lowest Value</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Results Summary */}
      <div className='flex items-center justify-between mb-4'>
        <p className='text-sm text-muted-foreground'>
          {isLoading ? 'Loading...' : `${total} customers found`}
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
            <span className='text-sm text-gray-600'>
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
              Error loading customers. Please try again.
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
                  <div className='h-4 bg-gray-200 rounded w-3/4'></div>
                  <div className='h-3 bg-gray-200 rounded w-1/2'></div>
                  <div className='h-3 bg-gray-200 rounded w-2/3'></div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Customer Grid */}
      {!isLoading && customers.length > 0 && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {customers.map((customer) => (
            <EntityCard
              key={customer.id}
              type='customer'
              entity={customer}
              onClick={() => {
                // Navigate to customer detail page
                console.log('View customer:', customer.id);
              }}
              onEdit={() => handleEditCustomer(customer)}
              onDelete={() => handleDeleteCustomer(customer.id)}
              className='h-full'
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && customers.length === 0 && !error && (
        <Card>
          <CardContent className='p-12 text-center'>
            <div className='mx-auto w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4'>
              <svg
                className='w-12 h-12 text-gray-400'
                fill='none'
                stroke='currentColor'
                viewBox='0 0 24 24'
              >
                <path
                  strokeLinecap='round'
                  strokeLinejoin='round'
                  strokeWidth='2'
                  d='M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z'
                />
              </svg>
            </div>
            <h3 className='text-lg font-semibold text-gray-900 mb-2'>
              No customers found
            </h3>
            <p className='text-gray-600 mb-4'>
              {searchQuery || statusFilter || typeFilter
                ? 'Try adjusting your filters to see more results.'
                : 'Get started by adding your first customer.'}
            </p>
            <Button onClick={() => setShowCreateForm(true)}>
              Add First Customer
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default CustomerListPage;
