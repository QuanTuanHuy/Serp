/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organizations management page
 */

'use client';

import React, { useMemo } from 'react';
import {
  useOrganizations,
  AdminStatusBadge,
  AdminActionMenu,
} from '@/modules/admin';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import { Building2, Search, Eye, Edit, Ban, CheckCircle } from 'lucide-react';
import { Organization as OrganizationType } from '@/modules/admin/types';

export default function OrganizationsPage() {
  const {
    filters,
    organizations,
    pagination,
    isLoading,
    isFetching,
    error,
    handleSearch,
    handleFilterChange,
    handlePageChange,
  } = useOrganizations();

  const formatDate = (isoDate?: string) => {
    if (!isoDate) return 'N/A';
    return new Date(isoDate).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  // Define columns for DataTable
  const columns = useMemo<ColumnDef<OrganizationType>[]>(
    () => [
      {
        id: 'organization',
        header: 'Organization',
        accessor: 'name',
        defaultVisible: true,
        cell: ({ row }) => (
          <div className='flex items-center gap-3'>
            <div className='h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center'>
              <Building2 className='h-5 w-5 text-primary' />
            </div>
            <div>
              <p className='font-medium'>{row.name}</p>
              <p className='text-xs text-muted-foreground'>{row.code}</p>
            </div>
          </div>
        ),
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'organizationType',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{value}</span>,
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'status',
        defaultVisible: true,
        cell: ({ value }) => <AdminStatusBadge status={value} />,
      },
      {
        id: 'employees',
        header: 'Employees',
        accessor: 'employeeCount',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{value || 'N/A'}</span>,
      },
      {
        id: 'email',
        header: 'Email',
        accessor: 'email',
        defaultVisible: false,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {value || 'N/A'}
          </span>
        ),
      },
      {
        id: 'phone',
        header: 'Phone',
        accessor: 'phoneNumber',
        defaultVisible: false,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {value || 'N/A'}
          </span>
        ),
      },
      {
        id: 'created',
        header: 'Created',
        accessor: 'createdAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatDate(value)}
          </span>
        ),
      },
      {
        id: 'subscription',
        header: 'Subscription',
        accessor: 'subscriptionExpiresAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {value ? `Expires ${formatDate(value)}` : 'No subscription'}
          </span>
        ),
      },
      {
        id: 'actions',
        header: 'Actions',
        accessor: 'id',
        align: 'right',
        defaultVisible: true,
        cell: ({ row }) => (
          <AdminActionMenu
            items={[
              {
                label: 'View Details',
                onClick: () => console.log('View', row.id),
                icon: <Eye className='h-4 w-4' />,
              },
              {
                label: 'Edit',
                onClick: () => console.log('Edit', row.id),
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: row.status === 'ACTIVE' ? 'Suspend' : 'Activate',
                onClick: () => console.log('Toggle status', row.id),
                icon:
                  row.status === 'ACTIVE' ? (
                    <Ban className='h-4 w-4' />
                  ) : (
                    <CheckCircle className='h-4 w-4' />
                  ),
                separator: true,
                variant: row.status === 'ACTIVE' ? 'destructive' : 'default',
              },
            ]}
          />
        ),
      },
    ],
    []
  );

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Organizations</h1>
          <p className='text-muted-foreground mt-2'>
            Manage and monitor all organizations in the system
          </p>
        </div>

        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm'>
            Export
          </Button>
        </div>
      </div>

      {/* Filters Card */}
      <Card>
        <CardHeader>
          <CardTitle className='text-base font-medium'>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid gap-4 md:grid-cols-4'>
            {/* Search */}
            <div className='md:col-span-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search by name, code, email...'
                  value={filters.search || ''}
                  onChange={(e) => handleSearch(e.target.value)}
                  className='pl-10'
                />
              </div>
            </div>

            {/* Status Filter */}
            <div>
              <select
                value={filters.status || ''}
                onChange={(e) =>
                  handleFilterChange('status', e.target.value || undefined)
                }
                className='w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>All Statuses</option>
                <option value='ACTIVE'>Active</option>
                <option value='TRIAL'>Trial</option>
                <option value='SUSPENDED'>Suspended</option>
                <option value='INACTIVE'>Inactive</option>
              </select>
            </div>

            {/* Type Filter */}
            <div>
              <select
                value={filters.type || ''}
                onChange={(e) =>
                  handleFilterChange('type', e.target.value || undefined)
                }
                className='w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>All Types</option>
                <option value='ENTERPRISE'>Enterprise</option>
                <option value='SMB'>SMB</option>
                <option value='STARTUP'>Startup</option>
                <option value='INDIVIDUAL'>Individual</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Table Card */}
      <DataTable
        columns={columns}
        data={organizations}
        keyExtractor={(org) => String(org.id)}
        isLoading={isLoading}
        error={error}
        storageKey='admin-organizations-columns'
        pagination={{
          currentPage: pagination.currentPage,
          totalPages: pagination.totalPages,
          totalItems: pagination.totalItems,
          onPageChange: handlePageChange,
          isFetching,
        }}
        loadingState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-muted-foreground'>
              Loading organizations...
            </div>
          </div>
        }
        errorState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-destructive'>Failed to load organizations</div>
          </div>
        }
      />
    </div>
  );
}
