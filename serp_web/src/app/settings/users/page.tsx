/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings users management page
 */

'use client';

import React, { useMemo, useState, useEffect } from 'react';
import {
  UserPlus,
  Search,
  Mail,
  Edit,
  Ban,
  CheckCircle2,
  Clock,
  Shield,
  Users as UsersIcon,
  UserIcon,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import {
  SettingsStatsCard,
  SettingsActionMenu,
  SettingsStatusBadge,
} from '@/modules/settings';
import { useSettingsUsers } from '@/modules/settings/hooks/useUsers';
import { SettingsUserDialog } from '@/modules/settings/components';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import type { UserProfile } from '@/modules/admin/types';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { useDebounce } from '@/shared/hooks';
import { getInitials } from '@/shared/utils';

export default function SettingsUsersPage() {
  const [openDialog, setOpenDialog] = useState(false);
  const [editUserId, setEditUserId] = useState<number | null>(null);

  const {
    filters,
    users,
    pagination,
    isLoading,
    isFetching,
    error,
    setSearch,
    setStatus,
    handlePageChange,
  } = useSettingsUsers();

  const [searchInput, setSearchInput] = useState<string>(filters.search || '');
  const debouncedSearch = useDebounce(searchInput, 400);
  useEffect(() => {
    setSearch(debouncedSearch || undefined);
  }, [debouncedSearch, setSearch]);

  const formatDate = (isoDate?: string) => {
    if (!isoDate) return 'Never';
    return new Date(isoDate).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

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
                  }) || <UsersIcon className='h-5 w-5 text-primary' />}
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
        cell: ({ value }) => <SettingsStatusBadge status={value} />,
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
          <SettingsActionMenu
            items={[
              {
                label: 'Edit User',
                onClick: () => {
                  setEditUserId(row.id as number);
                  setOpenDialog(true);
                },
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: 'Inactivate User',
                onClick: () => console.log('Inactivate', row.id),
                icon: <Ban className='h-4 w-4' />,
                variant: 'destructive',
                separator: true,
              },
            ]}
          />
        ),
      },
    ],
    []
  );

  // Status rendering handled by SettingsStatusBadge in table columns

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>User Management</h1>
          <p className='text-muted-foreground mt-2'>
            Manage organization members, roles, and permissions
          </p>
        </div>
        <Button
          className='bg-purple-600 hover:bg-purple-700'
          onClick={() => {
            setEditUserId(null);
            setOpenDialog(true);
          }}
        >
          <UserPlus className='h-4 w-4 mr-2' />
          Create User
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Users'
          value={45}
          description='Organization members'
          icon={<UserIcon className='h-4 w-4' />}
          trend={{ value: 12.5, label: 'vs last month' }}
        />

        <SettingsStatsCard
          title='Active Users'
          value={42}
          description='Currently active'
          icon={<CheckCircle2 className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Pending Invites'
          value={3}
          description='Awaiting acceptance'
          icon={<Clock className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Admin Users'
          value={5}
          description='With admin privileges'
          icon={<Shield className='h-4 w-4' />}
        />
      </div>

      {/* Filters and Search */}
      <Card>
        <CardHeader>
          <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
            <div>
              <CardTitle>All Users</CardTitle>
              <CardDescription>
                View and manage organization members
              </CardDescription>
            </div>
            {/* Optional actions can go here */}
          </div>
        </CardHeader>
        <CardContent>
          <div className='grid gap-4 md:grid-cols-4'>
            {/* Search */}
            <div className='md:col-span-2'>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search by name, email...'
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  className='pl-10'
                />
              </div>
            </div>

            {/* Status Filter */}
            <div>
              <select
                value={
                  filters.status === undefined
                    ? ''
                    : filters.status === 'all'
                      ? ''
                      : (filters.status as string)
                }
                onChange={(e) => setStatus((e.target.value || 'all') as any)}
                className='w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>All Statuses</option>
                <option value='ACTIVE'>Active</option>
                <option value='INACTIVE'>Inactive</option>
                <option value='PENDING'>Pending</option>
                <option value='SUSPENDED'>Suspended</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      <DataTable
        columns={columns}
        data={users}
        keyExtractor={(u) => String(u.id)}
        isLoading={isLoading}
        error={error}
        storageKey='settings-users-columns'
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

      {/* Create/Edit User Dialog */}
      <SettingsUserDialog
        open={openDialog}
        mode={editUserId ? 'edit' : 'create'}
        initialUser={useMemo(
          () => users.find((u) => u.id === editUserId) || undefined,
          [users, editUserId]
        )}
        onOpenChange={(o) => {
          if (!o) {
            setOpenDialog(false);
            setEditUserId(null);
          } else {
            setOpenDialog(true);
          }
        }}
      />
    </div>
  );
}
