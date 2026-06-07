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
  UserX,
  Plus,
  SlidersHorizontal,
  X,
  CheckCircle,
} from 'lucide-react';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Combobox } from '@/shared/components/ui/combobox';
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
  SettingsFilterDialog,
  SettingsFilterChips,
} from '@/modules/settings';
import type { SettingsFilterChip } from '@/modules/settings';
import { useSettingsUsers } from '@/modules/settings/hooks/useUsers';
import { EditUserDialog } from '@/modules/settings/components/users/EditUserDialog';
import { CreateUserDialog } from '@/modules/settings/components/users/CreateUserDialog';
import { InviteUserDialog } from '@/modules/settings/components/users/InviteUserDialog';
import { UserDetailDialog } from '@/modules/settings/components/users/UserDetailDialog';
import { ConfirmStatusDialog } from '@/modules/settings/components/users/ConfirmStatusDialog';
import { ConfirmResetPasswordDialog } from '@/modules/settings/components/users/ConfirmResetPasswordDialog';
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
import { useGetDepartmentsQuery } from '@/modules/settings/services/departments/departmentsApi';

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

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  INVITED: 'Invited',
  SUSPENDED: 'Suspended',
};

const USER_TYPE_LABELS: Record<string, string> = {
  OWNER: 'Owner',
  ADMIN: 'Admin',
  EMPLOYEE: 'Employee',
  CONTRACTOR: 'Contractor',
  EXTERNAL: 'External',
  GUEST: 'Guest',
};

const USER_SORT_OPTIONS = [
  { value: 'id', label: 'ID' },
  { value: 'firstName', label: 'First name' },
  { value: 'email', label: 'Email' },
  { value: 'userType', label: 'User type' },
  { value: 'status', label: 'Status' },
  { value: 'lastLoginAt', label: 'Last login' },
  { value: 'createdAt', label: 'Created at' },
  { value: 'updatedAt', label: 'Updated at' },
] as const;

const SORT_DIRECTION_LABELS: Record<string, string> = {
  ASC: 'Ascending',
  DESC: 'Descending',
};

const USER_STATUS_OPTIONS: UserStatus[] = [
  'ACTIVE',
  'INACTIVE',
  'INVITED',
  'SUSPENDED',
];

const FilterPane = ({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) => (
  <div className='flex min-h-0 flex-1 flex-col p-4'>
    <div className='mb-3'>
      <h3 className='text-sm font-semibold'>{title}</h3>
      <p className='text-sm text-muted-foreground'>Select one value.</p>
    </div>
    <div className='space-y-1'>{children}</div>
  </div>
);

const FilterOption = ({
  label,
  selected,
  onSelect,
}: {
  label: string;
  selected: boolean;
  onSelect: () => void;
}) => (
  <button
    type='button'
    onClick={onSelect}
    className={`flex w-full items-center justify-between rounded-md px-3 py-2 text-left text-sm hover:bg-muted ${
      selected ? 'bg-muted font-medium' : ''
    }`}
  >
    {label}
    {selected ? <CheckCircle className='h-4 w-4 text-primary' /> : null}
  </button>
);

// ==================== Main Page ====================

export default function SettingsUsersPage() {
  const [activeTab, setActiveTab] = useState('users');
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [openInviteDialog, setOpenInviteDialog] = useState(false);
  const [openDetailDialog, setOpenDetailDialog] = useState(false);
  const [openStatusDialog, setOpenStatusDialog] = useState(false);
  const [openResetPasswordDialog, setOpenResetPasswordDialog] = useState(false);
  const [editUserId, setEditUserId] = useState<number | null>(null);
  const [detailUserId, setDetailUserId] = useState<number | null>(null);
  const [statusTarget, setStatusTarget] = useState<{
    userId: number;
    name: string;
    status: string;
  } | null>(null);
  const [resetPasswordTarget, setResetPasswordTarget] = useState<{
    userId: number;
    name: string;
    email: string;
  } | null>(null);

  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [selectedCriterion, setSelectedCriterion] = useState('status');

  const {
    organizationId,
    filters,
    users,
    stats,
    pagination,
    isLoading,
    isFetching,
    error,
    setSearch,
    setStatus,
    setUserType,
    setRoleId,
    setDepartmentId,
    setSortBy,
    setSortDir,
    handlePageChange,
    // Actions
    create,
    createStatus,
    updateStatus,
    resetPassword,
    resetPasswordStatus,
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

  const { data: departmentsResponse } = useGetDepartmentsQuery(
    { organizationId: organizationId as number, pageSize: 100 },
    { skip: !organizationId }
  );
  const availableDepartments = departmentsResponse?.data.items ?? [];

  // Search debounce
  const [searchInput, setSearchInput] = useState<string>(filters.search || '');
  const debouncedSearch = useDebounce(searchInput, 400);
  useEffect(() => {
    setSearch(debouncedSearch || undefined);
  }, [debouncedSearch, setSearch]);

  const clearAllFilters = useCallback(() => {
    setStatus('all' as any);
    setUserType(undefined);
    setRoleId(undefined);
    setDepartmentId(undefined);
    setSortBy(undefined);
    setSortDir(undefined);
  }, [
    setStatus,
    setUserType,
    setRoleId,
    setDepartmentId,
    setSortBy,
    setSortDir,
  ]);

  const filterChips = useMemo<SettingsFilterChip[]>(() => {
    const chips: SettingsFilterChip[] = [];

    if (filters.status && filters.status !== 'all') {
      const status = String(filters.status);
      chips.push({
        id: 'status',
        label: `Status: ${STATUS_LABELS[status] || status}`,
        onRemove: () => setStatus('all' as any),
      });
    }

    if (filters.userType) {
      const userType = String(filters.userType);
      chips.push({
        id: 'type',
        label: `Type: ${USER_TYPE_LABELS[userType] || userType}`,
        onRemove: () => setUserType(undefined),
      });
    }

    if (filters.roleId !== undefined) {
      const role = availableRoles.find((r) => r.id === filters.roleId);
      chips.push({
        id: 'role',
        label: `Role: ${role?.name || `Role #${filters.roleId}`}`,
        onRemove: () => setRoleId(undefined),
      });
    }

    if (filters.departmentId !== undefined) {
      const dept = availableDepartments.find(
        (department) => department.id === filters.departmentId
      );
      chips.push({
        id: 'department',
        label: `Department: ${dept?.name || `Department #${filters.departmentId}`}`,
        onRemove: () => setDepartmentId(undefined),
      });
    }

    if (filters.sortBy || filters.sortDir) {
      const sortBy = filters.sortBy || 'id';
      const sortDir = filters.sortDir || 'DESC';
      const sortByLabel =
        USER_SORT_OPTIONS.find((option) => option.value === sortBy)?.label ||
        sortBy;
      const sortDirLabel = SORT_DIRECTION_LABELS[sortDir] || sortDir;
      chips.push({
        id: 'sort',
        label: `Sort: ${sortByLabel} (${sortDirLabel})`,
        onRemove: () => {
          setSortBy(undefined);
          setSortDir(undefined);
        },
      });
    }

    return chips;
  }, [
    filters.status,
    filters.userType,
    filters.roleId,
    filters.departmentId,
    filters.sortBy,
    filters.sortDir,
    availableRoles,
    availableDepartments,
    setStatus,
    setUserType,
    setRoleId,
    setDepartmentId,
    setSortBy,
    setSortDir,
  ]);

  const filterCriteria = useMemo(
    () => [
      {
        id: 'status',
        label: 'Status',
        count: filters.status && filters.status !== 'all' ? 1 : 0,
      },
      { id: 'type', label: 'Type', count: filters.userType ? 1 : 0 },
      {
        id: 'role',
        label: 'Role',
        count: filters.roleId !== undefined ? 1 : 0,
      },
      {
        id: 'department',
        label: 'Department',
        count: filters.departmentId !== undefined ? 1 : 0,
      },
      {
        id: 'sort',
        label: 'Sort',
        count: filters.sortBy || filters.sortDir ? 1 : 0,
      },
    ],
    [
      filters.status,
      filters.userType,
      filters.roleId,
      filters.departmentId,
      filters.sortBy,
      filters.sortDir,
    ]
  );

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

  const handleResetPassword = useCallback((user: UserProfile) => {
    setResetPasswordTarget({
      userId: user.id,
      name:
        `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.email,
      email: user.email,
    });
    setOpenResetPasswordDialog(true);
  }, []);

  const confirmResetPassword = useCallback(async () => {
    if (!resetPasswordTarget) return;
    try {
      await resetPassword(resetPasswordTarget.userId);
      setOpenResetPasswordDialog(false);
      setResetPasswordTarget(null);
    } catch {
      // Error handled by hook
    }
  }, [resetPassword, resetPasswordTarget]);

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
                onClick: () => handleResetPassword(row),
                icon: <KeyRound className='h-4 w-4' />,
                className: 'text-amber-600 focus:text-amber-700',
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
    [handleResetPassword, handleStatusChange]
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
          {/* <Button variant='outline' size='sm' onClick={handleExport}>
            <Download className='h-4 w-4 mr-2' />
            Export
          </Button> */}
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
          {/* Search + Filters */}
          <div className='space-y-3'>
            <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
              <div className='relative w-full md:max-w-md'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search by name or email...'
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                  className='pl-10 pr-10'
                />
                {searchInput && (
                  <Button
                    type='button'
                    variant='ghost'
                    size='icon'
                    onClick={() => {
                      setSearchInput('');
                      setSearch(undefined);
                    }}
                    className='absolute right-1 top-1/2 h-8 w-8 -translate-y-1/2 text-muted-foreground hover:text-foreground'
                    aria-label='Clear search'
                  >
                    <X className='h-4 w-4' />
                  </Button>
                )}
              </div>
              <Button
                type='button'
                variant='outline'
                onClick={() => setFilterDialogOpen(true)}
              >
                <SlidersHorizontal className='h-4 w-4' />
                Filters
              </Button>
            </div>
            <SettingsFilterChips
              chips={filterChips}
              onClearAll={clearAllFilters}
            />
          </div>

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

      <SettingsFilterDialog
        open={filterDialogOpen}
        title='Filters'
        description='Pick a filter group, then select a value.'
        criteria={filterCriteria}
        selectedCriterion={selectedCriterion}
        onSelectCriterion={setSelectedCriterion}
        onOpenChange={setFilterDialogOpen}
        onClear={clearAllFilters}
      >
        {selectedCriterion === 'status' ? (
          <FilterPane title='Status'>
            <FilterOption
              label='All statuses'
              selected={!filters.status || filters.status === 'all'}
              onSelect={() => setStatus('all' as any)}
            />
            {USER_STATUS_OPTIONS.map((status) => (
              <FilterOption
                key={status}
                label={STATUS_LABELS[status] || status}
                selected={filters.status === status}
                onSelect={() => setStatus(status)}
              />
            ))}
          </FilterPane>
        ) : null}

        {selectedCriterion === 'type' ? (
          <FilterPane title='Type'>
            <FilterOption
              label='All types'
              selected={!filters.userType}
              onSelect={() => setUserType(undefined)}
            />
            {Object.entries(USER_TYPE_LABELS).map(([value, label]) => (
              <FilterOption
                key={value}
                label={label}
                selected={filters.userType === value}
                onSelect={() => setUserType(value)}
              />
            ))}
          </FilterPane>
        ) : null}

        {selectedCriterion === 'role' ? (
          <FilterPane title='Role'>
            <Combobox
              value={filters.roleId}
              onChange={(value) =>
                setRoleId(value !== undefined ? Number(value) : undefined)
              }
              items={availableRoles.map((role) => ({
                value: role.id,
                label: role.name,
              }))}
              placeholder='All roles'
            />
          </FilterPane>
        ) : null}

        {selectedCriterion === 'department' ? (
          <FilterPane title='Department'>
            <Combobox
              value={filters.departmentId}
              onChange={(value) =>
                setDepartmentId(value !== undefined ? Number(value) : undefined)
              }
              items={availableDepartments.map((dept) => ({
                value: dept.id,
                label: dept.name,
              }))}
              placeholder='All departments'
            />
          </FilterPane>
        ) : null}

        {selectedCriterion === 'sort' ? (
          <FilterPane title='Sort'>
            <div className='space-y-3'>
              <div className='space-y-2'>
                <Label className='text-xs font-medium text-muted-foreground'>
                  Sort by
                </Label>
                <Select
                  value={filters.sortBy || 'default'}
                  onValueChange={(v) =>
                    setSortBy(v === 'default' ? undefined : v)
                  }
                >
                  <SelectTrigger className='h-10'>
                    <SelectValue placeholder='Default sort' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='default'>Default</SelectItem>
                    {USER_SORT_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label className='text-xs font-medium text-muted-foreground'>
                  Direction
                </Label>
                <Select
                  value={filters.sortDir || 'default'}
                  onValueChange={(v) =>
                    setSortDir(
                      v === 'default' ? undefined : (v as 'ASC' | 'DESC')
                    )
                  }
                >
                  <SelectTrigger className='h-10'>
                    <SelectValue placeholder='Default direction' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='default'>Default</SelectItem>
                    <SelectItem value='ASC'>Ascending</SelectItem>
                    <SelectItem value='DESC'>Descending</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </FilterPane>
        ) : null}
      </SettingsFilterDialog>

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

      <ConfirmResetPasswordDialog
        open={openResetPasswordDialog}
        onOpenChange={(open) => {
          setOpenResetPasswordDialog(open);
          if (!open) setResetPasswordTarget(null);
        }}
        userName={resetPasswordTarget?.name || ''}
        userEmail={resetPasswordTarget?.email || ''}
        onConfirm={confirmResetPassword}
        isSubmitting={resetPasswordStatus.isLoading}
      />
    </div>
  );
}
