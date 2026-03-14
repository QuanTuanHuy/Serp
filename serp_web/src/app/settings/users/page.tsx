/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings users management page
 */

'use client';

import React, { useMemo, useState, useEffect, useCallback } from 'react';
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
  Eye,
  RotateCcw,
  Send,
  XCircle,
  KeyRound,
  Download,
  UserX,
  Plus,
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
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  SettingsStatsCard,
  SettingsActionMenu,
  SettingsStatusBadge,
} from '@/modules/settings';
import { useSettingsUsers } from '@/modules/settings/hooks/useUsers';
import { EditUserDialog } from '@/modules/settings/components/users/EditUserDialog';
import { CreateUserDialog } from '@/modules/settings/components/users/CreateUserDialog';
import { InviteUserDialog } from '@/modules/settings/components/users/InviteUserDialog';
import { UserDetailDialog } from '@/modules/settings/components/users/UserDetailDialog';
import { ConfirmStatusDialog } from '@/modules/settings/components/users/ConfirmStatusDialog';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import type { UserProfile, UserStatus } from '@/modules/admin/types';
import type { UserInvitation } from '@/modules/settings/types/user.types';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { useDebounce } from '@/shared/hooks';
import { getInitials } from '@/shared/utils';
import {
  useLazyExportUsersQuery,
  useGetOrganizationRolesQuery,
} from '@/modules/settings/services/users/usersApi';

// ==================== Sub-components ====================

const UserAvatarCell = ({ row }: { row: UserProfile }) => (
  <div className='flex items-center gap-3'>
    <Avatar className='h-9 w-9'>
      {row.avatarUrl && (
        <AvatarImage
          src={row.avatarUrl}
          alt={`${row.firstName || ''} ${row.lastName || ''}`}
        />
      )}
      <AvatarFallback className='bg-primary/10 text-xs'>
        {getInitials({
          firstName: row.firstName,
          lastName: row.lastName,
          email: row.email,
        }) || <UsersIcon className='h-4 w-4 text-primary' />}
      </AvatarFallback>
    </Avatar>
    <div className='min-w-0'>
      <p className='font-medium text-sm truncate'>
        {row.firstName} {row.lastName}
      </p>
      <div className='flex items-center gap-1 text-xs text-muted-foreground'>
        <Mail className='h-3 w-3 flex-shrink-0' />
        <span className='truncate'>{row.email}</span>
      </div>
    </div>
  </div>
);

const formatDate = (isoDate?: string) => {
  if (!isoDate) return 'Never';
  return new Date(isoDate).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

// ==================== Main Page ====================

export default function SettingsUsersPage() {
  const [activeTab, setActiveTab] = useState('users');
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [openInviteDialog, setOpenInviteDialog] = useState(false);
  const [openDetailDialog, setOpenDetailDialog] = useState(false);
  const [openStatusDialog, setOpenStatusDialog] = useState(false);
  const [editUserId, setEditUserId] = useState<number | null>(null);
  const [detailUserId, setDetailUserId] = useState<number | null>(null);
  const [statusTarget, setStatusTarget] = useState<{
    userId: number;
    name: string;
    status: string;
  } | null>(null);

  const {
    organizationId,
    filters,
    users,
    stats,
    pagination,
    isLoading,
    isLoadingStats,
    isFetching,
    error,
    setSearch,
    setStatus,
    setUserType,
    handlePageChange,
    // Actions
    create,
    createStatus,
    updateStatus,
    resetPassword,
    inviteUser,
    // Invitations
    invitations,
    invitationPagination,
    isLoadingInvitations,
    isFetchingInvitations,
    invitationPage,
    setInvitationPage,
    invitationStatus: invFilterStatus,
    setInvitationStatus,
    cancelInvitation,
    resendInvitation,
  } = useSettingsUsers();

  const [triggerExport] = useLazyExportUsersQuery();
  const { data: rolesResponse } = useGetOrganizationRolesQuery(
    { organizationId: organizationId as number },
    { skip: !organizationId }
  );
  const availableRoles = rolesResponse?.data ?? [];

  // Search debounce
  const [searchInput, setSearchInput] = useState<string>(filters.search || '');
  const debouncedSearch = useDebounce(searchInput, 400);
  useEffect(() => {
    setSearch(debouncedSearch || undefined);
  }, [debouncedSearch, setSearch]);

  // Status change handlers
  const handleStatusChange = useCallback(
    (user: UserProfile, targetStatus: string) => {
      setStatusTarget({
        userId: user.id,
        name:
          `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.email,
        status: targetStatus,
      });
      setOpenStatusDialog(true);
    },
    []
  );

  const confirmStatusChange = useCallback(async () => {
    if (!statusTarget) return;
    try {
      await updateStatus(statusTarget.userId, statusTarget.status);
      setOpenStatusDialog(false);
      setStatusTarget(null);
    } catch {
      // Error handled by hook
    }
  }, [statusTarget, updateStatus]);

  const handleExport = useCallback(async () => {
    if (!organizationId) return;
    try {
      const result = await triggerExport({ organizationId }).unwrap();
      const url = URL.createObjectURL(result);
      const a = document.createElement('a');
      a.href = url;
      a.download = `users-export-${Date.now()}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      // Error handled by RTK Query
    }
  }, [organizationId, triggerExport]);

  // ==================== User Table Columns ====================

  const userColumns = useMemo<ColumnDef<UserProfile>[]>(
    () => [
      {
        id: 'user',
        header: 'User',
        accessor: 'email',
        defaultVisible: true,
        cell: ({ row }) => <UserAvatarCell row={row} />,
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'userType',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm font-medium'>{value}</span>
        ),
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'status',
        defaultVisible: true,
        cell: ({ value }) => <SettingsStatusBadge status={value} />,
      },
      {
        id: 'department',
        header: 'Department',
        accessor: 'primaryDepartmentName',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>{value || '-'}</span>
        ),
      },
      {
        id: 'modules',
        header: 'Modules',
        accessor: 'moduleAccessCount',
        defaultVisible: true,
        align: 'center',
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>{value ?? 0}</span>
        ),
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
        header: '',
        accessor: 'id',
        align: 'right',
        defaultVisible: true,
        cell: ({ row }) => (
          <SettingsActionMenu
            items={[
              {
                label: 'View Details',
                onClick: () => {
                  setDetailUserId(row.id);
                  setOpenDetailDialog(true);
                },
                icon: <Eye className='h-4 w-4' />,
              },
              {
                label: 'Edit User',
                onClick: () => {
                  setEditUserId(row.id);
                  setOpenEditDialog(true);
                },
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: 'Reset Password',
                onClick: () => resetPassword(row.id),
                icon: <KeyRound className='h-4 w-4' />,
                separator: true,
              },
              ...(row.status !== 'ACTIVE'
                ? [
                    {
                      label: 'Activate',
                      onClick: () => handleStatusChange(row, 'ACTIVE'),
                      icon: <CheckCircle2 className='h-4 w-4' />,
                    },
                  ]
                : []),
              ...(row.status === 'ACTIVE'
                ? [
                    {
                      label: 'Suspend',
                      onClick: () => handleStatusChange(row, 'SUSPENDED'),
                      icon: <Ban className='h-4 w-4' />,
                      variant: 'destructive' as const,
                      separator: true,
                    },
                    {
                      label: 'Deactivate',
                      onClick: () => handleStatusChange(row, 'INACTIVE'),
                      icon: <UserX className='h-4 w-4' />,
                      variant: 'destructive' as const,
                    },
                  ]
                : []),
            ]}
          />
        ),
      },
    ],
    [handleStatusChange, resetPassword]
  );

  // ==================== Invitation Table Columns ====================

  const invitationColumns = useMemo<ColumnDef<UserInvitation>[]>(
    () => [
      {
        id: 'invitee',
        header: 'Invitee',
        accessor: 'email',
        defaultVisible: true,
        cell: ({ row }) => (
          <div>
            <p className='font-medium text-sm'>
              {row.firstName} {row.lastName}
            </p>
            <p className='text-xs text-muted-foreground'>{row.email}</p>
          </div>
        ),
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'userType',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{value || '-'}</span>,
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'status',
        defaultVisible: true,
        cell: ({ value }) => <SettingsStatusBadge status={value} />,
      },
      {
        id: 'invitedAt',
        header: 'Invited',
        accessor: 'invitedAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatDate(value)}
          </span>
        ),
      },
      {
        id: 'expiresAt',
        header: 'Expires',
        accessor: 'expiresAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatDate(value)}
          </span>
        ),
      },
      {
        id: 'actions',
        header: '',
        accessor: 'id',
        align: 'right',
        defaultVisible: true,
        cell: ({ row }) => (
          <SettingsActionMenu
            items={[
              ...(row.status === 'PENDING'
                ? [
                    {
                      label: 'Resend',
                      onClick: () => resendInvitation(row.id),
                      icon: <Send className='h-4 w-4' />,
                    },
                    {
                      label: 'Cancel',
                      onClick: () => cancelInvitation(row.id),
                      icon: <XCircle className='h-4 w-4' />,
                      variant: 'destructive' as const,
                      separator: true,
                    },
                  ]
                : []),
              ...(row.status === 'EXPIRED'
                ? [
                    {
                      label: 'Resend',
                      onClick: () => resendInvitation(row.id),
                      icon: <RotateCcw className='h-4 w-4' />,
                    },
                  ]
                : []),
            ]}
          />
        ),
      },
    ],
    [cancelInvitation, resendInvitation]
  );

  // ==================== Stats trend calculation ====================

  const userGrowth = useMemo(() => {
    if (!stats || !stats.newUsersLastMonth) return 0;
    if (stats.newUsersLastMonth === 0)
      return stats.newUsersThisMonth > 0 ? 100 : 0;
    return Math.round(
      ((stats.newUsersThisMonth - stats.newUsersLastMonth) /
        stats.newUsersLastMonth) *
        100
    );
  }, [stats]);

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>User Management</h1>
          <p className='text-muted-foreground mt-1'>
            Manage organization members, roles, and permissions
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm' onClick={handleExport}>
            <Download className='h-4 w-4 mr-2' />
            Export
          </Button>
          <Button variant='outline' onClick={() => setOpenCreateDialog(true)}>
            <Plus className='h-4 w-4 mr-2' />
            Create User
          </Button>
          <Button onClick={() => setOpenInviteDialog(true)}>
            <UserPlus className='h-4 w-4 mr-2' />
            Invite User
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Users'
          value={stats?.totalUsers ?? '-'}
          description='Organization members'
          icon={<UserIcon className='h-4 w-4' />}
          trend={
            stats ? { value: userGrowth, label: 'vs last month' } : undefined
          }
        />
        <SettingsStatsCard
          title='Active Users'
          value={stats?.activeUsers ?? '-'}
          description='Currently active'
          icon={<CheckCircle2 className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='Pending Invites'
          value={stats?.invitedUsers ?? '-'}
          description='Awaiting acceptance'
          icon={<Clock className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='Admin Users'
          value={stats?.adminUsers ?? '-'}
          description='With admin privileges'
          icon={<Shield className='h-4 w-4' />}
        />
      </div>

      {/* Tabs: Users / Invitations */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='users'>
            Users
            {pagination.totalItems > 0 && (
              <span className='ml-1.5 text-xs bg-muted px-1.5 py-0.5 rounded-full'>
                {pagination.totalItems}
              </span>
            )}
          </TabsTrigger>
          <TabsTrigger value='invitations'>
            Invitations
            {invitationPagination.totalItems > 0 && (
              <span className='ml-1.5 text-xs bg-muted px-1.5 py-0.5 rounded-full'>
                {invitationPagination.totalItems}
              </span>
            )}
          </TabsTrigger>
        </TabsList>

        {/* ==================== Users Tab ==================== */}
        <TabsContent value='users' className='space-y-4'>
          {/* Filters */}
          <Card>
            <CardContent className='pt-6'>
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
                <Select
                  value={
                    filters.status === 'all'
                      ? 'all'
                      : (filters.status as string) || 'all'
                  }
                  onValueChange={(v) =>
                    setStatus(v === 'all' ? 'all' : (v as UserStatus))
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder='All Statuses' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='all'>All Statuses</SelectItem>
                    <SelectItem value='ACTIVE'>Active</SelectItem>
                    <SelectItem value='INACTIVE'>Inactive</SelectItem>
                    <SelectItem value='INVITED'>Invited</SelectItem>
                    <SelectItem value='SUSPENDED'>Suspended</SelectItem>
                  </SelectContent>
                </Select>

                {/* User Type Filter */}
                <Select
                  value={filters.userType || 'all'}
                  onValueChange={(v) =>
                    setUserType(v === 'all' ? undefined : v)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder='All Types' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='all'>All Types</SelectItem>
                    <SelectItem value='OWNER'>Owner</SelectItem>
                    <SelectItem value='ADMIN'>Admin</SelectItem>
                    <SelectItem value='EMPLOYEE'>Employee</SelectItem>
                    <SelectItem value='CONTRACTOR'>Contractor</SelectItem>
                    <SelectItem value='EXTERNAL'>External</SelectItem>
                    <SelectItem value='GUEST'>Guest</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </CardContent>
          </Card>

          {/* Users Table */}
          <DataTable
            columns={userColumns}
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
          />
        </TabsContent>

        {/* ==================== Invitations Tab ==================== */}
        <TabsContent value='invitations' className='space-y-4'>
          {/* Invitation Filters */}
          <Card>
            <CardContent className='pt-6'>
              <div className='flex items-center gap-4'>
                <Select
                  value={invFilterStatus || 'all'}
                  onValueChange={(v) =>
                    setInvitationStatus(v === 'all' ? undefined : v)
                  }
                >
                  <SelectTrigger className='w-48'>
                    <SelectValue placeholder='All Statuses' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='all'>All Statuses</SelectItem>
                    <SelectItem value='PENDING'>Pending</SelectItem>
                    <SelectItem value='ACCEPTED'>Accepted</SelectItem>
                    <SelectItem value='EXPIRED'>Expired</SelectItem>
                    <SelectItem value='CANCELLED'>Cancelled</SelectItem>
                  </SelectContent>
                </Select>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => setOpenInviteDialog(true)}
                >
                  <UserPlus className='h-4 w-4 mr-2' />
                  New Invitation
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Invitations Table */}
          <DataTable
            columns={invitationColumns}
            data={invitations}
            keyExtractor={(inv) => String(inv.id)}
            isLoading={isLoadingInvitations}
            storageKey='settings-invitations-columns'
            pagination={{
              currentPage: invitationPagination.currentPage,
              totalPages: invitationPagination.totalPages,
              totalItems: invitationPagination.totalItems,
              onPageChange: setInvitationPage,
              isFetching: isFetchingInvitations,
            }}
          />
        </TabsContent>
      </Tabs>

      {/* ==================== Dialogs ==================== */}

      {/* Edit User Dialog */}
      <EditUserDialog
        open={openEditDialog}
        onOpenChange={(o) => {
          if (!o) {
            setOpenEditDialog(false);
            setEditUserId(null);
          } else {
            setOpenEditDialog(true);
          }
        }}
        userId={editUserId}
        organizationId={organizationId}
      />

      {/* Create User Dialog */}
      <CreateUserDialog
        open={openCreateDialog}
        onOpenChange={setOpenCreateDialog}
        onSubmit={create}
        isSubmitting={createStatus.isLoading}
        availableRoles={availableRoles}
      />

      {/* Invite User Dialog */}
      <InviteUserDialog
        open={openInviteDialog}
        onOpenChange={setOpenInviteDialog}
        onSubmit={inviteUser}
      />

      {/* User Detail Dialog */}
      <UserDetailDialog
        open={openDetailDialog}
        onOpenChange={(o) => {
          setOpenDetailDialog(o);
          if (!o) setDetailUserId(null);
        }}
        userId={detailUserId}
        organizationId={organizationId}
      />

      {/* Confirm Status Dialog */}
      <ConfirmStatusDialog
        open={openStatusDialog}
        onOpenChange={(o) => {
          setOpenStatusDialog(o);
          if (!o) setStatusTarget(null);
        }}
        userName={statusTarget?.name || ''}
        targetStatus={statusTarget?.status || 'ACTIVE'}
        onConfirm={confirmStatusChange}
      />
    </div>
  );
}
