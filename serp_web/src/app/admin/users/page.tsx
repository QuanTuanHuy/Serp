/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - System-wide users management page
 */

'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import {
  AdminActionMenu,
  AdminFilterChips,
  AdminFilterDialog,
  AdminStatusBadge,
  UserAccessDialog,
  UserDetailsDrawer,
  UserDialog,
  UserStatusDialog,
  useUsers,
} from '@/modules/admin';
import type {
  DepartmentOption,
  Organization,
  Role,
  UserProfile,
  UserStatus,
  UserType,
} from '@/modules/admin/types';
import { useGetOrganizationsQuery } from '@/modules/admin/services/organizations/organizationsApi';
import { useGetDepartmentsQuery } from '@/modules/admin/services/departments/departmentsApi';
import { useGetAllRolesQuery } from '@/modules/admin/services/roles/rolesApi';
import { formatAdminDate } from '@/modules/admin/utils/date';
import { Combobox } from '@/shared/components/ui/combobox';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Button,
  Input,
} from '@/shared/components/ui';
import { DataTable } from '@/shared/components';
import { getInitials } from '@/shared/utils';
import type { ColumnDef } from '@/shared/types';
import {
  Ban,
  CheckCircle,
  Eye,
  Edit,
  Mail,
  Search,
  Shield,
  SlidersHorizontal,
  Users,
} from 'lucide-react';

const statusOptions: Array<{ value: UserStatus; label: string }> = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'INVITED', label: 'Invited' },
  { value: 'SUSPENDED', label: 'Suspended' },
  { value: 'DELETED', label: 'Deleted' },
];

const userTypeOptions: Array<{ value: UserType; label: string }> = [
  { value: 'OWNER', label: 'Owner' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'CONTRACTOR', label: 'Contractor' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GUEST', label: 'Guest' },
];

export default function UsersPage() {
  const searchParams = useSearchParams();
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [selectedCriterion, setSelectedCriterion] = useState('organization');
  const [organizationSearch, setOrganizationSearch] = useState('');

  const {
    filters,
    users,
    pagination,
    isLoading,
    isFetching,
    error,
    ui,
    handleSearch,
    handleFilterChange,
    handlePageChange,
    openCreate,
    openEdit,
    openDetails,
    closeDetails,
    openAccess,
    closeAccess,
    openStatus,
    closeStatus,
  } = useUsers();

  useEffect(() => {
    const organizationIdParam = searchParams.get('organizationId');
    if (!organizationIdParam) {
      return;
    }

    const organizationId = Number(organizationIdParam);
    if (
      Number.isFinite(organizationId) &&
      filters.organizationId !== organizationId
    ) {
      handleFilterChange('organizationId', organizationId);
    }
  }, [filters.organizationId, handleFilterChange, searchParams]);

  const { data: orgsResponse, isFetching: isFetchingOrganizations } =
    useGetOrganizationsQuery({
      page: 0,
      pageSize: 50,
      sortBy: 'name',
      sortDir: 'ASC',
      search: organizationSearch || undefined,
    });
  const { data: roles = [] } = useGetAllRolesQuery();
  const { data: departmentsResponse, isFetching: isFetchingDepartments } =
    useGetDepartmentsQuery(
      { organizationId: filters.organizationId ?? 0 },
      { skip: !filters.organizationId }
    );

  const organizations: Organization[] = orgsResponse?.data.items ?? [];
  const departments: DepartmentOption[] = departmentsResponse?.data.items ?? [];
  const selectedUser = useMemo(
    () => users.find((user) => user.id === ui.selectedUserId),
    [users, ui.selectedUserId]
  );

  const filterCriteria = [
    {
      id: 'organization',
      label: 'Organization',
      count: filters.organizationId ? 1 : 0,
    },
    { id: 'status', label: 'Status', count: filters.status ? 1 : 0 },
    { id: 'userType', label: 'User type', count: filters.userType ? 1 : 0 },
    { id: 'role', label: 'Role', count: filters.roleId ? 1 : 0 },
    {
      id: 'department',
      label: 'Department',
      count: filters.departmentId ? 1 : 0,
    },
  ];

  const organizationLabel =
    organizations.find(
      (organization) => organization.id === filters.organizationId
    )?.name ??
    (filters.organizationId ? `Organization #${filters.organizationId}` : '');
  const roleLabel =
    roles.find((role) => role.id === filters.roleId)?.name ??
    (filters.roleId ? `Role #${filters.roleId}` : '');
  const departmentLabel =
    departments.find((department) => department.id === filters.departmentId)
      ?.name ??
    (filters.departmentId ? `Department #${filters.departmentId}` : '');

  const filterChips = [
    filters.organizationId
      ? {
          id: 'organization',
          label: `Organization: ${organizationLabel}`,
          onRemove: () => {
            handleFilterChange('organizationId', undefined);
            handleFilterChange('departmentId', undefined);
          },
        }
      : null,
    filters.status
      ? {
          id: 'status',
          label: `Status: ${
            statusOptions.find((item) => item.value === filters.status)
              ?.label ?? filters.status
          }`,
          onRemove: () => handleFilterChange('status', undefined),
        }
      : null,
    filters.userType
      ? {
          id: 'userType',
          label: `Type: ${
            userTypeOptions.find((item) => item.value === filters.userType)
              ?.label ?? filters.userType
          }`,
          onRemove: () => handleFilterChange('userType', undefined),
        }
      : null,
    filters.roleId
      ? {
          id: 'role',
          label: `Role: ${roleLabel}`,
          onRemove: () => handleFilterChange('roleId', undefined),
        }
      : null,
    filters.departmentId
      ? {
          id: 'department',
          label: `Department: ${departmentLabel}`,
          onRemove: () => handleFilterChange('departmentId', undefined),
        }
      : null,
  ].filter(Boolean) as Array<{
    id: string;
    label: string;
    onRemove: () => void;
  }>;

  const clearFilters = () => {
    handleFilterChange('organizationId', undefined);
    handleFilterChange('status', undefined);
    handleFilterChange('userType', undefined);
    handleFilterChange('roleId', undefined);
    handleFilterChange('departmentId', undefined);
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
            <div className='min-w-0'>
              <p className='truncate font-medium'>
                {row.firstName} {row.lastName}
              </p>
              <div className='flex items-center gap-1 text-xs text-muted-foreground'>
                <Mail className='h-3 w-3' />
                <span className='truncate'>{row.email}</span>
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
          <span className='text-sm'>{value ? String(value) : 'N/A'}</span>
        ),
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'userType',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{String(value)}</span>,
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'status',
        defaultVisible: true,
        cell: ({ value }) => <AdminStatusBadge status={String(value)} />,
      },
      {
        id: 'lastLogin',
        header: 'Last login',
        accessor: 'lastLoginAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatAdminDate(value, 'Never')}
          </span>
        ),
      },
      {
        id: 'actions',
        header: 'Actions',
        accessor: 'id',
        align: 'right',
        defaultVisible: true,
        cell: ({ row }) => {
          const targetStatus =
            row.status === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED';

          return (
            <AdminActionMenu
              items={[
                {
                  label: 'View details',
                  onClick: () => openDetails(row.id, row.organizationId),
                  icon: <Eye className='h-4 w-4' />,
                },
                {
                  label: 'Edit profile',
                  onClick: () => openEdit(row.id),
                  icon: <Edit className='h-4 w-4' />,
                },
                {
                  label: 'Manage access',
                  onClick: () => openAccess(row.id, row.organizationId),
                  icon: <Shield className='h-4 w-4' />,
                },
                {
                  label: targetStatus === 'SUSPENDED' ? 'Suspend' : 'Activate',
                  onClick: () =>
                    openStatus(row.id, row.organizationId, row.status),
                  icon:
                    targetStatus === 'SUSPENDED' ? (
                      <Ban className='h-4 w-4' />
                    ) : (
                      <CheckCircle className='h-4 w-4' />
                    ),
                  separator: true,
                  variant:
                    targetStatus === 'SUSPENDED' ? 'destructive' : 'default',
                },
              ]}
            />
          );
        },
      },
    ],
    [openAccess, openDetails, openEdit, openStatus]
  );

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Users</h1>
          <p className='mt-2 text-muted-foreground'>
            Manage users, access, and organization membership.
          </p>
        </div>
        <Button
          type='button'
          onClick={() => openCreate(filters.organizationId)}
        >
          Create user
        </Button>
      </div>

      <div className='space-y-3'>
        <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
          <div className='relative w-full md:max-w-md'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search by name or email...'
              value={filters.search || ''}
              onChange={(event) => handleSearch(event.target.value)}
              className='pl-10'
            />
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

        <AdminFilterChips chips={filterChips} onClearAll={clearFilters} />
      </div>

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
          <div className='flex h-64 items-center justify-center'>
            <div className='text-muted-foreground'>Loading users...</div>
          </div>
        }
        errorState={
          <div className='flex h-64 items-center justify-center'>
            <div className='text-destructive'>Failed to load users</div>
          </div>
        }
      />

      <AdminFilterDialog
        open={filterDialogOpen}
        title='Filters'
        description='Pick a filter group, then select a value.'
        criteria={filterCriteria}
        selectedCriterion={selectedCriterion}
        onSelectCriterion={setSelectedCriterion}
        onOpenChange={setFilterDialogOpen}
        onClear={clearFilters}
      >
        {selectedCriterion === 'organization' ? (
          <FilterPane title='Organization'>
            <Combobox
              value={filters.organizationId}
              onChange={(value) => {
                handleFilterChange(
                  'organizationId',
                  value !== undefined ? Number(value) : undefined
                );
                handleFilterChange('departmentId', undefined);
              }}
              items={organizations.map((organization) => ({
                value: organization.id,
                label: organization.name,
              }))}
              placeholder='All organizations'
              loading={isFetchingOrganizations}
              onSearch={setOrganizationSearch}
            />
          </FilterPane>
        ) : null}

        {selectedCriterion === 'status' ? (
          <FilterPane title='Status'>
            <FilterOption
              label='All statuses'
              selected={!filters.status}
              onSelect={() => handleFilterChange('status', undefined)}
            />
            {statusOptions.map((option) => (
              <FilterOption
                key={option.value}
                label={option.label}
                selected={filters.status === option.value}
                onSelect={() => handleFilterChange('status', option.value)}
              />
            ))}
          </FilterPane>
        ) : null}

        {selectedCriterion === 'userType' ? (
          <FilterPane title='User type'>
            <FilterOption
              label='All user types'
              selected={!filters.userType}
              onSelect={() => handleFilterChange('userType', undefined)}
            />
            {userTypeOptions.map((option) => (
              <FilterOption
                key={option.value}
                label={option.label}
                selected={filters.userType === option.value}
                onSelect={() => handleFilterChange('userType', option.value)}
              />
            ))}
          </FilterPane>
        ) : null}

        {selectedCriterion === 'role' ? (
          <FilterPane title='Role'>
            <FilterOption
              label='All roles'
              selected={!filters.roleId}
              onSelect={() => handleFilterChange('roleId', undefined)}
            />
            {roles.map((role: Role) => (
              <FilterOption
                key={role.id}
                label={role.name}
                selected={filters.roleId === role.id}
                onSelect={() => handleFilterChange('roleId', role.id)}
              />
            ))}
          </FilterPane>
        ) : null}

        {selectedCriterion === 'department' ? (
          <FilterPane title='Department'>
            {!filters.organizationId ? (
              <p className='text-sm text-muted-foreground'>
                Select an organization first.
              </p>
            ) : (
              <>
                <FilterOption
                  label='All departments'
                  selected={!filters.departmentId}
                  onSelect={() => handleFilterChange('departmentId', undefined)}
                />
                {isFetchingDepartments ? (
                  <p className='px-3 py-2 text-sm text-muted-foreground'>
                    Loading departments...
                  </p>
                ) : null}
                {departments.map((department) => (
                  <FilterOption
                    key={department.id}
                    label={department.name}
                    selected={filters.departmentId === department.id}
                    onSelect={() =>
                      handleFilterChange('departmentId', department.id)
                    }
                  />
                ))}
              </>
            )}
          </FilterPane>
        ) : null}
      </AdminFilterDialog>

      <UserDetailsDrawer
        open={ui.detailDrawerOpen}
        organizationId={ui.selectedOrganizationId}
        userId={ui.selectedUserId}
        onOpenChange={(open) => {
          if (!open) closeDetails();
        }}
        onEdit={(userId) => openEdit(userId)}
        onAccess={openAccess}
        onToggleStatus={openStatus}
      />

      <UserAccessDialog
        open={ui.accessDialogOpen}
        organizationId={ui.selectedOrganizationId}
        userId={ui.selectedUserId}
        onOpenChange={(open) => {
          if (!open) closeAccess();
        }}
      />

      <UserStatusDialog
        open={ui.statusDialogOpen}
        organizationId={ui.selectedOrganizationId}
        userId={ui.selectedUserId}
        userName={
          selectedUser
            ? `${selectedUser.firstName ?? ''} ${selectedUser.lastName ?? ''}`.trim() ||
              selectedUser.email
            : undefined
        }
        currentStatus={ui.selectedStatus ?? selectedUser?.status}
        onOpenChange={(open) => {
          if (!open) closeStatus();
        }}
      />

      <UserDialog />
    </div>
  );
}

function FilterPane({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className='flex min-h-0 flex-1 flex-col p-4'>
      <div className='mb-3'>
        <h3 className='text-sm font-semibold'>{title}</h3>
        <p className='text-sm text-muted-foreground'>Select one value.</p>
      </div>
      <div className='space-y-2'>{children}</div>
    </div>
  );
}

function FilterOption({
  label,
  selected,
  onSelect,
}: {
  label: string;
  selected: boolean;
  onSelect: () => void;
}) {
  return (
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
}
