// Sales CustomerListPage Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Button,
  Card,
  CardContent,
  Input,
  Badge,
} from '@/shared/components/ui';
import {
  Search,
  Plus,
  SlidersHorizontal,
  Users,
  X,
  ChevronLeft,
  ChevronRight,
  Mail,
  Phone,
  Edit,
  Trash2,
  ExternalLink,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetCustomersQuery,
  useDeleteCustomerMutation,
} from '../../api/salesApi';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { setCustomerPagination, setCustomerFilters } from '../../store';
import {
  selectCustomerPagination,
  selectCustomerFilters,
} from '../../store/selectors';
import type { Customer, CustomerStatus } from '../../types';

interface CustomerListPageProps {
  className?: string;
}

// Enhanced StatsCard component with gradients
const StatsCard = ({
  title,
  value,
  icon: Icon,
  variant = 'default',
}: {
  title: string;
  value: number | string;
  icon: any;
  variant?: 'default' | 'primary' | 'success' | 'warning';
}) => {
  const variantStyles = {
    default: {
      card: 'bg-card hover:bg-card/90 border-border',
      icon: 'bg-muted text-muted-foreground',
      iconRing: '',
    },
    primary: {
      card: 'bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/30 dark:to-blue-900/20 border-blue-200/50 dark:border-blue-800/30',
      icon: 'bg-blue-500 text-white shadow-blue-500/25',
      iconRing: 'ring-4 ring-blue-500/10',
    },
    success: {
      card: 'bg-gradient-to-br from-emerald-50 to-emerald-100/50 dark:from-emerald-950/30 dark:to-emerald-900/20 border-emerald-200/50 dark:border-emerald-800/30',
      icon: 'bg-emerald-500 text-white shadow-emerald-500/25',
      iconRing: 'ring-4 ring-emerald-500/10',
    },
    warning: {
      card: 'bg-gradient-to-br from-amber-50 to-amber-100/50 dark:from-amber-950/30 dark:to-amber-900/20 border-amber-200/50 dark:border-amber-800/30',
      icon: 'bg-amber-500 text-white shadow-amber-500/25',
      iconRing: 'ring-4 ring-amber-500/10',
    },
  };

  const styles = variantStyles[variant];

  return (
    <Card
      className={cn(
        'group relative overflow-hidden p-5 shadow-sm transition-all duration-200 hover:shadow-md cursor-pointer',
        styles.card
      )}
    >
      {/* Background Pattern */}
      <div className='absolute inset-0 opacity-5 dark:opacity-10'>
        <svg
          className='absolute -right-8 -top-8 h-32 w-32 text-current'
          viewBox='0 0 100 100'
        >
          <circle
            cx='50'
            cy='50'
            r='40'
            fill='currentColor'
            fillOpacity='0.3'
          />
        </svg>
      </div>

      <div className='relative flex items-start justify-between gap-4'>
        <div className='space-y-1 min-w-0 flex-1'>
          <p className='text-sm font-medium text-muted-foreground truncate'>
            {title}
          </p>
          <p className='text-2xl font-bold tracking-tight truncate'>{value}</p>
        </div>

        <div
          className={cn(
            'flex h-12 w-12 shrink-0 items-center justify-center rounded-xl shadow-lg transition-transform duration-200 group-hover:scale-105',
            styles.icon,
            styles.iconRing
          )}
        >
          <Icon className='h-6 w-6' />
        </div>
      </div>
    </Card>
  );
};

// Enhanced CustomerCard component with gradients and animations
const CustomerCard = ({
  customer,
  onClick,
  onEdit,
  onDelete,
}: {
  customer: Customer;
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
}) => {
  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const statusStyles = {
    ACTIVE: {
      bg: 'bg-emerald-100 dark:bg-emerald-900/30',
      text: 'text-emerald-700 dark:text-emerald-400',
      dot: 'bg-emerald-500',
    },
    INACTIVE: {
      bg: 'bg-slate-100 dark:bg-slate-800/50',
      text: 'text-slate-600 dark:text-slate-400',
      dot: 'bg-slate-400',
    },
  };

  const status =
    statusStyles[customer.statusId as keyof typeof statusStyles] ||
    statusStyles.ACTIVE;

  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer'
      )}
      onClick={onClick}
    >
      {/* Header with gradient accent */}
      <div className='relative h-2 bg-gradient-to-r from-primary/60 via-primary/40 to-primary/20' />

      <CardContent className='p-5'>
        {/* Top Row - Avatar & Actions */}
        <div className='flex items-start justify-between mb-4'>
          <div className='flex items-center gap-3'>
            <div className='relative'>
              <div className='flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary font-semibold text-lg shadow-sm'>
                {getInitials(customer.name)}
              </div>
              <div
                className={cn(
                  'absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-full border-2 border-card',
                  status.dot
                )}
              />
            </div>
            <div className='min-w-0'>
              <h3 className='font-semibold text-foreground truncate'>
                {customer.name}
              </h3>
              <Badge
                variant='secondary'
                className={cn('gap-1 mt-1', status.bg, status.text)}
              >
                <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
                {customer.statusId}
              </Badge>
            </div>
          </div>

          <div className='flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8 hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-600 dark:text-blue-400'
              onClick={(e) => {
                e.stopPropagation();
                onEdit?.();
              }}
              title='Edit'
            >
              <Edit className='h-4 w-4' />
            </Button>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8 hover:bg-rose-100 dark:hover:bg-rose-900/30 text-rose-600 dark:text-rose-400'
              onClick={(e) => {
                e.stopPropagation();
                onDelete?.();
              }}
              title='Delete'
            >
              <Trash2 className='h-4 w-4' />
            </Button>
          </div>
        </div>

        {/* Contact Info */}
        <div className='space-y-2 mb-4'>
          {customer.email && (
            <div className='flex items-center gap-2 text-sm'>
              <Mail className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                {customer.email}
              </span>
            </div>
          )}
          {customer.phone && (
            <div className='flex items-center gap-2 text-sm'>
              <Phone className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>{customer.phone}</span>
            </div>
          )}
        </div>

        {/* Footer - Quick Actions */}
        <div className='flex items-center justify-end gap-1 pt-3 border-t'>
          <Button
            variant='ghost'
            size='icon'
            className='h-9 w-9 hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-600 dark:text-blue-400'
            onClick={(e) => {
              e.stopPropagation();
              // Email action
            }}
            title='Send Email'
          >
            <Mail className='h-4 w-4' />
          </Button>
          {customer.phone && (
            <Button
              variant='ghost'
              size='icon'
              className='h-9 w-9 hover:bg-emerald-100 dark:hover:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400'
              onClick={(e) => {
                e.stopPropagation();
                // Call action
              }}
              title='Call'
            >
              <Phone className='h-4 w-4' />
            </Button>
          )}
          <Button
            variant='ghost'
            size='icon'
            className='h-9 w-9'
            onClick={(e) => {
              e.stopPropagation();
              onClick?.();
            }}
            title='View Details'
          >
            <ExternalLink className='h-4 w-4' />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export const CustomerListPage: React.FC<CustomerListPageProps> = ({
  className,
}) => {
  const router = useRouter();
  const dispatch = useAppDispatch();

  const pagination = useAppSelector(selectCustomerPagination);
  const filters = useAppSelector(selectCustomerFilters);

  const { data, isLoading, error } = useGetCustomersQuery({
    filters,
    pagination,
  });

  const [deleteCustomer] = useDeleteCustomerMutation();

  // Local state
  const [searchQuery, setSearchQuery] = useState(filters.query || '');
  const [statusFilter, setStatusFilter] = useState<CustomerStatus | ''>('');
  const [showFilters, setShowFilters] = useState(false);

  const customers = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage || 0;

  // Handle actions
  const handleSearch = () => {
    dispatch(setCustomerFilters({ ...filters, query: searchQuery }));
    dispatch(setCustomerPagination({ ...pagination, page: 0 }));
  };

  const handlePageChange = (newPage: number) => {
    dispatch(setCustomerPagination({ ...pagination, page: newPage }));
  };

  const handleViewCustomer = (customerId: string) => {
    router.push(`/sales/customers/${customerId}`);
  };

  const handleEditCustomer = (customerId: string) => {
    router.push(`/sales/customers/${customerId}/edit`);
  };

  const handleDeleteCustomer = async (customerId: string) => {
    if (confirm('Are you sure you want to delete this customer?')) {
      try {
        await deleteCustomer(customerId).unwrap();
      } catch (error) {
        console.error('Failed to delete customer:', error);
      }
    }
  };

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('');
    dispatch(setCustomerFilters({}));
    dispatch(setCustomerPagination({ ...pagination, page: 0 }));
  };

  const hasActiveFilters = searchQuery || statusFilter;

  // Calculate stats
  const stats = useMemo(() => {
    return {
      total: totalItems,
      active: customers.filter((c: Customer) => c.statusId === 'ACTIVE').length,
    };
  }, [totalItems, customers]);

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
          <Button
            onClick={() => router.push('/sales/customers/new')}
            className='gap-2'
          >
            <Plus className='h-4 w-4' />
            Add Customer
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-2 sm:grid-cols-3 gap-4'>
        <StatsCard title='Total Customers' value={stats.total} icon={Users} />
        <StatsCard title='Active' value={stats.active} icon={Users} />
        <StatsCard
          title='Inactive'
          value={stats.total - stats.active}
          icon={Users}
        />
      </div>

      {/* Search & Filters Bar */}
      <div className='flex flex-col sm:flex-row gap-3'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search customers by name, email...'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            className='pl-10 pr-10'
          />
          {searchQuery && (
            <button
              onClick={() => {
                setSearchQuery('');
                handleSearch();
              }}
              className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
            >
              <X className='h-4 w-4' />
            </button>
          )}
        </div>

        <Button onClick={handleSearch} variant='secondary'>
          <Search className='h-4 w-4 mr-2' />
          Search
        </Button>

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
      </div>

      {/* Expanded Filters */}
      {showFilters && (
        <Card>
          <CardContent className='p-4'>
            <div className='grid grid-cols-1 sm:grid-cols-2 gap-4'>
              <div>
                <label className='text-sm font-medium mb-1.5 block'>
                  Status
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => {
                    const value = e.target.value as CustomerStatus | '';
                    setStatusFilter(value);
                    dispatch(
                      setCustomerFilters({
                        ...filters,
                        statusId: value || undefined,
                      })
                    );
                  }}
                  className='w-full px-3 py-2 border rounded-lg bg-background'
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
                  {totalItems} results found
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
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
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
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Customer Grid */}
      {!isLoading && customers.length > 0 && (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {customers.map((customer: Customer) => (
            <CustomerCard
              key={customer.id}
              customer={customer}
              onClick={() => handleViewCustomer(customer.id)}
              onEdit={() => handleEditCustomer(customer.id)}
              onDelete={() => handleDeleteCustomer(customer.id)}
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
              <Button onClick={() => router.push('/sales/customers/new')}>
                <Plus className='h-4 w-4 mr-2' />
                Add First Customer
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pagination */}
      {totalItems > (pagination.size || 10) && (
        <div className='flex items-center justify-between pt-4'>
          <p className='text-sm text-muted-foreground'>
            Showing {currentPage * (pagination.size || 10) + 1} to{' '}
            {Math.min((currentPage + 1) * (pagination.size || 10), totalItems)}{' '}
            of {totalItems} customers
          </p>
          <div className='flex items-center gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage === 0}
              onClick={() => handlePageChange(currentPage - 1)}
            >
              <ChevronLeft className='h-4 w-4' />
              Previous
            </Button>
            <div className='flex items-center gap-1'>
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                const pageNum = i;
                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={cn(
                      'h-8 w-8 rounded-md text-sm font-medium transition-colors',
                      currentPage === pageNum
                        ? 'bg-primary text-primary-foreground'
                        : 'hover:bg-muted'
                    )}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
            </div>
            <Button
              variant='outline'
              size='sm'
              disabled={currentPage >= totalPages - 1}
              onClick={() => handlePageChange(currentPage + 1)}
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

export default CustomerListPage;
