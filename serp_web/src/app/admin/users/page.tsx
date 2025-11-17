/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - System-wide users management page
 */

'use client';

import React, { useMemo, useState } from 'react';
import {
  useUsers,
  UserDialog,
  AdminStatusBadge,
  AdminActionMenu,
} from '@/modules/admin';
import type { UserProfile } from '@/modules/admin';
import { Combobox } from '@/shared/components/ui/combobox';
import { useGetOrganizationsQuery } from '@/modules/admin/services/organizations/organizationsApi';
import type { Organization } from '@/modules/admin/types';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import { DataTable } from '@/shared/components';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { getInitials } from '@/shared/utils';
import type { ColumnDef } from '@/shared/types';
import {
  Users,
  Search,
  Eye,
  Edit,
  Ban,
  CheckCircle,
  Mail,
  Building2,
} from 'lucide-react';

export default function UsersPage() {
  const {
    filters,
    users,
    pagination,
    isLoading,
    isFetching,
    error,
    handleSearch,
    handleFilterChange,
    handlePageChange,
    openCreate,
    openEdit,
  } = useUsers();

  const [orgSearch, setOrgSearch] = useState<string>('');
  const { data: orgsResponse, isFetching: isFetchingOrgs } =
    useGetOrganizationsQuery({
      page: 0,
      pageSize: 50,
      sortBy: 'name',
      sortDir: 'ASC',
      search: orgSearch || undefined,
    } as any);
  const organizations: Organization[] = useMemo(
    () => orgsResponse?.data.items || [],
    [orgsResponse]
  );

  const formatDate = (isoDate?: string) => {
    if (!isoDate) return 'Never';
    return new Date(isoDate).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  // Define columns for DataTable
  const columns = useMemo<ColumnDef<UserProfile>[]>(
    () => [
      {
        id: 'user',
        header: 'User',
        accessor: 'email',
        defaultVisible: true,
        cell: ({ row }) => (
          <div className='flex items-center gap-3'>
            <div className='h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center'>
              <Avatar className='h-10 w-10'>
                {row.avatarUrl ? (
                  <AvatarImage
                    src={row.avatarUrl}
                    alt={`${row.firstName || ''} ${row.lastName || ''}`}
                  />
                ) : null}
                <AvatarFallback className='bg-primary/10'>
                  {getInitials({
                    firstName: row.firstName,
                    lastName: row.lastName,
                    email: row.email,
                  }) || <Users className='h-5 w-5 text-primary' />}
                </AvatarFallback>
              </Avatar>
            </div>
            <div>
              <p className='font-medium'>
                {row.firstName} {row.lastName}
              </p>
              <div className='flex items-center gap-1 text-xs text-muted-foreground'>
                <Mail className='h-3 w-3' />
                {row.email}
              </div>
            </div>
          </div>
        ),
      },
      {
        id: 'organization',
        header: 'Organization',
        accessor: 'organizationName',
        defaultVisible: true,
        cell: ({ value }) => (
          <div className='flex items-center gap-2'>
            <Building2 className='h-4 w-4 text-muted-foreground' />
            <span className='text-sm'>{value || 'N/A'}</span>
          </div>
        ),
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'userType',
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
        id: 'lastLogin',
        header: 'Last Login',
        accessor: 'lastLoginAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatDate(value)}
          </span>
        ),
      },
      {
        id: 'created',
        header: 'Created',
        accessor: 'createdAt',
        defaultVisible: false,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatDate(value)}
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
                label: 'View Profile',
                onClick: () => console.log('View', row.id),
                icon: <Eye className='h-4 w-4' />,
              },
              {
                label: 'Edit',
                onClick: () => openEdit(row.id),
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
          <h1 className='text-3xl font-bold tracking-tight'>Users</h1>
          <p className='text-muted-foreground mt-2'>
            System-wide user management and monitoring
          </p>
        </div>

        <div className='flex items-center gap-2'>
          <Button
            variant='default'
            size='sm'
            disabled={!filters.organizationId}
            onClick={() => openCreate(filters.organizationId)}
          >
            Create User
          </Button>
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
                  placeholder='Search by name, email...'
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
                <option value='INACTIVE'>Inactive</option>
                <option value='PENDING'>Pending</option>
                <option value='SUSPENDED'>Suspended</option>
              </select>
            </div>

            {/* Organization Filter */}
            <div>
              <Combobox
                value={filters.organizationId}
                onChange={(val) =>
                  handleFilterChange(
                    'organizationId',
                    val !== undefined ? Number(val) : undefined
                  )
                }
                items={organizations.map((o) => ({
                  value: o.id,
                  label: o.name,
                }))}
                placeholder='All Organizations'
                loading={isFetchingOrgs}
                onSearch={(q) => setOrgSearch(q)}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Table Card */}
      <DataTable
        columns={columns}
        data={users}
        keyExtractor={(user) => String(user.id)}
        isLoading={isLoading}
        error={error}
        storageKey='admin-users-columns'
        pagination={{
          currentPage: pagination.currentPage,
          totalPages: pagination.totalPages,
          totalItems: pagination.totalItems,
          onPageChange: handlePageChange,
          isFetching,
        }}
        loadingState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-muted-foreground'>Loading users...</div>
          </div>
        }
        errorState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-destructive'>Failed to load users</div>
          </div>
        }
      />
      {/* Dialogs */}
      <UserDialog />
    </div>
  );
}
