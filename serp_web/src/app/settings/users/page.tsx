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
  Filter,
  X,
  ChevronDown,
  ChevronUp,
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
import { Label } from '@/shared/components/ui/label';
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
import { Badge } from '@/shared/components/ui/badge';

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

  // Collapsible advanced filters
  const [showFilters, setShowFilters] = useState(false);
  const hasAdvancedFilters =
    filters.roleId !== undefined || filters.departmentId !== undefined;
  const clearAllFilters = useCallback(() => {
    setSearchInput('');
    setSearch(undefined);
    setStatus('all' as any);
    setUserType(undefined);
    setRoleId(undefined);
    setDepartmentId(undefined);
    setSortBy(undefined);
    setSortDir(undefined);
  }, [
    setSearch,
    setStatus,
    setUserType,
    setRoleId,
    setDepartmentId,
    setSortBy,
    setSortDir,
  ]);

  const clearAdvancedFilters = useCallback(() => {
    setRoleId(undefined);
    setDepartmentId(undefined);
  }, [setRoleId, setDepartmentId]);

  const selectedRoleName = useMemo(() => {
    if (filters.roleId === undefined) return undefined;
    return (
      availableRoles.find((role) => role.id === filters.roleId)?.name ||
      `Role #${filters.roleId}`
    );
  }, [availableRoles, filters.roleId]);

  const selectedDepartmentName = useMemo(() => {
    if (filters.departmentId === undefined) return undefined;
    return (
      availableDepartments.find(
        (department) => department.id === filters.departmentId
      )?.name || `Department #${filters.departmentId}`
    );
  }, [availableDepartments, filters.departmentId]);

  const activeFilterCount = useMemo(() => {
    let count = 0;
    if (filters.search) count += 1;
    if (filters.status && filters.status !== 'all') count += 1;
    if (filters.userType) count += 1;
    if (filters.roleId !== undefined) count += 1;
    if (filters.departmentId !== undefined) count += 1;
    if (filters.sortBy || filters.sortDir) count += 1;
    return count;
  }, [
    filters.search,
    filters.status,
    filters.userType,
    filters.roleId,
    filters.departmentId,
    filters.sortBy,
    filters.sortDir,
  ]);

  const activeFilterBadges = useMemo(() => {
    const badges: string[] = [];

    if (filters.search) {
      badges.push(`Keyword: \"${filters.search}\"`);
    }

    if (filters.status && filters.status !== 'all') {
      const status = String(filters.status);
      badges.push(`Status: ${STATUS_LABELS[status] || status}`);
    }

    if (filters.userType) {
      const userType = String(filters.userType);
      badges.push(`Type: ${USER_TYPE_LABELS[userType] || userType}`);
    }

    if (selectedRoleName) {
      badges.push(`Role: ${selectedRoleName}`);
    }

    if (selectedDepartmentName) {
      badges.push(`Department: ${selectedDepartmentName}`);
    }

    if (filters.sortBy || filters.sortDir) {
      const currentSortBy = filters.sortBy || 'id';
      const currentSortDir = filters.sortDir || 'DESC';
      const sortByLabel =
        USER_SORT_OPTIONS.find((option) => option.value === currentSortBy)
          ?.label || currentSortBy;
      const sortDirLabel =
        SORT_DIRECTION_LABELS[currentSortDir] || currentSortDir;
      badges.push(`Sort: ${sortByLabel} (${sortDirLabel})`);
    }

    return badges;
  }, [
    filters.search,
    filters.status,
    filters.userType,
    filters.sortBy,
    filters.sortDir,
    selectedRoleName,
    selectedDepartmentName,
  ]);

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
          <Card className='border-border/70 shadow-sm'>
            <CardContent className='p-4 md:p-5'>
              <div className='space-y-4'>
                <div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
                  <div>
                    <h2 className='text-sm font-semibold'>Search & Filters</h2>
                    <p className='text-xs text-muted-foreground mt-1'>
                      Find users faster by keyword, status, type, role, and
                      department.
                    </p>
                  </div>

                  <div className='flex flex-wrap items-center gap-2'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setShowFilters((prev) => !prev)}
                      className='gap-2'
                    >
                      <Filter className='h-4 w-4' />
                      {showFilters ? 'Hide advanced' : 'Show advanced'}
                      {hasAdvancedFilters && (
                        <Badge
                          variant='secondary'
                          className='px-1.5 py-0 text-xs'
                        >
                          {(filters.roleId !== undefined ? 1 : 0) +
                            (filters.departmentId !== undefined ? 1 : 0)}
                        </Badge>
                      )}
                      {showFilters ? (
                        <ChevronUp className='h-4 w-4 text-muted-foreground' />
                      ) : (
                        <ChevronDown className='h-4 w-4 text-muted-foreground' />
                      )}
                    </Button>

                    {activeFilterCount > 0 && (
                      <Button
                        variant='ghost'
                        size='sm'
                        onClick={clearAllFilters}
                        className='gap-1.5 text-muted-foreground hover:text-foreground'
                      >
                        <X className='h-3.5 w-3.5' />
                        Reset all
                      </Button>
                    )}
                  </div>
                </div>

                <div className='grid gap-3 md:grid-cols-12'>
                  <div className='md:col-span-6 lg:col-span-4'>
                    <Label
                      htmlFor='users-search'
                      className='mb-2 text-xs font-medium text-muted-foreground'
                    >
                      Search users
                    </Label>
                    <div className='relative'>
                      <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                      <Input
                        id='users-search'
                        placeholder='Name or email...'
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                        className='h-10 pl-10 pr-10'
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
                  </div>

                  <div className='md:col-span-3 lg:col-span-2'>
                    <Label className='mb-2 text-xs font-medium text-muted-foreground'>
                      Status
                    </Label>
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
                      <SelectTrigger className='h-10'>
                        <SelectValue placeholder='All statuses' />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value='all'>All Statuses</SelectItem>
                        <SelectItem value='ACTIVE'>Active</SelectItem>
                        <SelectItem value='INACTIVE'>Inactive</SelectItem>
                        <SelectItem value='INVITED'>Invited</SelectItem>
                        <SelectItem value='SUSPENDED'>Suspended</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className='md:col-span-3 lg:col-span-2'>
                    <Label className='mb-2 text-xs font-medium text-muted-foreground'>
                      User type
                    </Label>
                    <Select
                      value={filters.userType || 'all'}
                      onValueChange={(v) =>
                        setUserType(v === 'all' ? undefined : v)
                      }
                    >
                      <SelectTrigger className='h-10'>
                        <SelectValue placeholder='All types' />
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

                  <div className='md:col-span-6 lg:col-span-2'>
                    <Label className='mb-2 text-xs font-medium text-muted-foreground'>
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

                  <div className='md:col-span-6 lg:col-span-2'>
                    <Label className='mb-2 text-xs font-medium text-muted-foreground'>
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

                {activeFilterBadges.length > 0 && (
                  <div className='flex flex-wrap items-center gap-2 rounded-md border border-dashed bg-muted/30 p-2.5'>
                    {activeFilterBadges.map((badge) => (
                      <Badge
                        key={badge}
                        variant='secondary'
                        className='font-normal'
                      >
                        {badge}
                      </Badge>
                    ))}
                  </div>
                )}

                {showFilters && (
                  <div className='rounded-lg border bg-muted/40 p-3 md:p-4'>
                    <div className='grid gap-3 md:grid-cols-2'>
                      <div className='space-y-2'>
                        <Label
                          htmlFor='users-role-filter'
                          className='text-xs font-medium text-muted-foreground'
                        >
                          Role
                        </Label>
                        <Select
                          value={
                            filters.roleId !== undefined
                              ? String(filters.roleId)
                              : 'all'
                          }
                          onValueChange={(v) =>
                            setRoleId(v === 'all' ? undefined : Number(v))
                          }
                        >
                          <SelectTrigger
                            id='users-role-filter'
                            className='h-10'
                          >
                            <SelectValue placeholder='All roles' />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value='all'>All Roles</SelectItem>
                            {availableRoles.map((role) => (
                              <SelectItem key={role.id} value={String(role.id)}>
                                {role.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div className='space-y-2'>
                        <Label
                          htmlFor='users-department-filter'
                          className='text-xs font-medium text-muted-foreground'
                        >
                          Department
                        </Label>
                        <Select
                          value={
                            filters.departmentId !== undefined
                              ? String(filters.departmentId)
                              : 'all'
                          }
                          onValueChange={(v) =>
                            setDepartmentId(v === 'all' ? undefined : Number(v))
                          }
                        >
                          <SelectTrigger
                            id='users-department-filter'
                            className='h-10'
                          >
                            <SelectValue placeholder='All departments' />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value='all'>All Departments</SelectItem>
                            {availableDepartments.map((department) => (
                              <SelectItem
                                key={department.id}
                                value={String(department.id)}
                              >
                                {department.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>
                    </div>

                    {hasAdvancedFilters && (
                      <div className='mt-3 flex justify-end'>
                        <Button
                          variant='ghost'
                          size='sm'
                          onClick={clearAdvancedFilters}
                          className='gap-1 text-muted-foreground hover:text-foreground'
                        >
                          <X className='h-3.5 w-3.5' />
                          Clear advanced
                        </Button>
                      </div>
                    )}
                  </div>
                )}
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
