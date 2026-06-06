/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - System-wide roles management page
 */

'use client';

import React, { useState, useMemo, useCallback } from 'react';
import {
  useRoles,
  AdminActionMenu,
  AdminFilterChips,
  AdminFilterDialog,
  RoleFormDialog,
} from '@/modules/admin';
import type { Role, CreateRoleRequest } from '@/modules/admin';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import {
  Shield,
  Search,
  Eye,
  Edit,
  Trash2,
  Plus,
  Star,
  Building2,
  Package,
  Check,
  SlidersHorizontal,
} from 'lucide-react';
import { useModules } from '@/modules/admin/hooks/useModules';

export default function RolesPage() {
  const {
    filters,
    roles,
    pagination,
    isLoading,
    isFetching,
    error,
    handleSearch,
    handleFilterChange,
    handlePageChange,
    handleCreateRole: createRole,
    handleUpdateRole: updateRole,
    handleDeleteRole: deleteRole,
    isCreating,
    isUpdating,
  } = useRoles();

  const { modules } = useModules();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | undefined>(undefined);
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [selectedCriterion, setSelectedCriterion] = useState('scope');

  const handleOpenCreateDialog = () => {
    setSelectedRole(undefined);
    setDialogOpen(true);
  };

  const handleEditRole = (role: Role) => {
    setSelectedRole(role);
    setDialogOpen(true);
  };

  const handleDeleteRoleClick = async (roleId: number) => {
    if (confirm('Are you sure you want to delete this role?')) {
      try {
        await deleteRole(roleId);
        setDialogOpen(false);
      } catch (error) {
        // Error already handled by useRoles hook
      }
    }
  };

  const handleSubmitRole = async (data: CreateRoleRequest) => {
    try {
      if (selectedRole) {
        await updateRole(selectedRole.id, data);
      } else {
        await createRole(data);
      }
      setDialogOpen(false);
    } catch (error) {
      // Error already handled by useRoles hook
      throw error;
    }
  };

  const formatDate = useCallback((isoDate?: string) => {
    if (!isoDate) return 'N/A';
    return new Date(isoDate).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }, []);

  const getScopeBadgeColor = useCallback((scope: string) => {
    switch (scope) {
      case 'SYSTEM':
        return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
      case 'ORGANIZATION':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'MODULE':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'DEPARTMENT':
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    }
  }, []);

  const getRoleTypeLabel = useCallback((roleType: string) => {
    const labels: Record<string, string> = {
      OWNER: 'Owner',
      ADMIN: 'Admin',
      MANAGER: 'Manager',
      USER: 'User',
      VIEWER: 'Viewer',
      CUSTOM: 'Custom',
    };
    return labels[roleType] || roleType;
  }, []);

  const getModuleName = useCallback(
    (moduleId?: number) => {
      if (!moduleId) return null;
      const module = modules.find((m) => m.id === moduleId);
      return module ? module.moduleName : `ID: ${moduleId}`;
    },
    [modules]
  );

  const scopeOptions = [
    { value: 'SYSTEM', label: 'System' },
    { value: 'ORGANIZATION', label: 'Organization' },
    { value: 'MODULE', label: 'Module' },
    { value: 'DEPARTMENT', label: 'Department' },
  ];

  const roleTypeOptions = [
    { value: 'OWNER', label: 'Owner' },
    { value: 'ADMIN', label: 'Admin' },
    { value: 'MANAGER', label: 'Manager' },
    { value: 'USER', label: 'User' },
    { value: 'VIEWER', label: 'Viewer' },
    { value: 'CUSTOM', label: 'Custom' },
  ];

  const filterCriteria = [
    { id: 'scope', label: 'Scope', count: filters.scope ? 1 : 0 },
    { id: 'roleType', label: 'Role type', count: filters.roleType ? 1 : 0 },
  ];

  const filterChips = [
    filters.scope
      ? {
          id: 'scope',
          label: `Scope: ${
            scopeOptions.find((item) => item.value === filters.scope)?.label ??
            filters.scope
          }`,
          onRemove: () => handleFilterChange('scope', undefined),
        }
      : null,
    filters.roleType
      ? {
          id: 'roleType',
          label: `Role type: ${
            roleTypeOptions.find((item) => item.value === filters.roleType)
              ?.label ?? filters.roleType
          }`,
          onRemove: () => handleFilterChange('roleType', undefined),
        }
      : null,
  ].filter(Boolean) as Array<{
    id: string;
    label: string;
    onRemove: () => void;
  }>;

  const clearFilters = () => {
    handleFilterChange('scope', undefined);
    handleFilterChange('roleType', undefined);
  };

  // Define columns for DataTable
  const columns = useMemo<ColumnDef<Role>[]>(
    () => [
      {
        id: 'role',
        header: 'Role',
        accessor: 'name',
        defaultVisible: true,
        cell: ({ row }) => (
          <div className='flex items-center gap-3'>
            <div className='h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center'>
              <Shield className='h-5 w-5 text-primary' />
            </div>
            <div>
              <div className='flex items-center gap-2'>
                <p className='font-medium'>{row.name}</p>
                {row.isDefault && (
                  <Star className='h-4 w-4 text-yellow-500 fill-yellow-500' />
                )}
              </div>
              {row.description && (
                <p className='text-xs text-muted-foreground'>
                  {row.description}
                </p>
              )}
            </div>
          </div>
        ),
      },
      {
        id: 'scope',
        header: 'Scope',
        accessor: 'scope',
        defaultVisible: true,
        cell: ({ value }) => (
          <span
            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getScopeBadgeColor(value)}`}
          >
            {value}
          </span>
        ),
      },
      {
        id: 'roleType',
        header: 'Role Type',
        accessor: 'roleType',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm'>{getRoleTypeLabel(value)}</span>
        ),
      },
      {
        id: 'priority',
        header: 'Priority',
        accessor: 'priority',
        defaultVisible: true,
        align: 'center',
        cell: ({ value }) => (
          <span className='text-sm font-medium'>{value}</span>
        ),
      },
      {
        id: 'permissions',
        header: 'Permissions',
        accessor: 'permissions',
        defaultVisible: true,
        align: 'center',
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {value?.length || 0}
          </span>
        ),
      },
      {
        id: 'organization',
        header: 'Organization',
        accessor: 'organizationId',
        defaultVisible: false,
        cell: ({ value }) => (
          <div className='flex items-center gap-2'>
            {value ? (
              <>
                <Building2 className='h-4 w-4 text-muted-foreground' />
                <span className='text-sm'>ID: {value}</span>
              </>
            ) : (
              <span className='text-sm text-muted-foreground'>All</span>
            )}
          </div>
        ),
      },
      {
        id: 'module',
        header: 'Module',
        accessor: 'moduleId',
        defaultVisible: false,
        cell: ({ value }) => {
          const moduleName = getModuleName(value);
          return (
            <div className='flex items-center gap-2'>
              {moduleName ? (
                <>
                  <Package className='h-4 w-4 text-muted-foreground' />
                  <span className='text-sm'>{moduleName}</span>
                </>
              ) : (
                <span className='text-sm text-muted-foreground'>All</span>
              )}
            </div>
          );
        },
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
                label: 'Edit',
                onClick: () => handleEditRole(row),
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: 'Delete',
                onClick: () => handleDeleteRoleClick(row.id),
                icon: <Trash2 className='h-4 w-4' />,
                separator: true,
                variant: 'destructive',
              },
            ]}
          />
        ),
      },
    ],
    [
      modules,
      handleEditRole,
      handleDeleteRoleClick,
      getScopeBadgeColor,
      getRoleTypeLabel,
      getModuleName,
      formatDate,
    ]
  );

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Roles</h1>
          <p className='text-muted-foreground mt-2'>
            System-wide role management and permissions
          </p>
        </div>

        <div className='flex items-center gap-2'>
          <Button size='sm' onClick={handleOpenCreateDialog}>
            <Plus className='h-4 w-4' />
            Create
          </Button>
        </div>
      </div>

      <div className='space-y-3'>
        <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
          <div className='relative w-full md:max-w-md'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search by name, description...'
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

      {/* Table Card */}
      <DataTable
        columns={columns}
        data={roles}
        keyExtractor={(role) => String(role.id)}
        isLoading={isLoading}
        error={error}
        storageKey='admin-roles-columns'
        pagination={{
          currentPage: pagination.currentPage,
          totalPages: pagination.totalPages,
          totalItems: pagination.totalItems,
          onPageChange: handlePageChange,
          isFetching,
        }}
        loadingState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-muted-foreground'>Loading roles...</div>
          </div>
        }
        errorState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-destructive'>Failed to load roles</div>
          </div>
        }
      />

      {/* Role Form Dialog */}
      <RoleFormDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        role={selectedRole}
        onSubmit={handleSubmitRole}
        isLoading={isCreating || isUpdating}
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
        {selectedCriterion === 'scope' ? (
          <FilterPane title='Scope'>
            <FilterOption
              label='All scopes'
              selected={!filters.scope}
              onSelect={() => handleFilterChange('scope', undefined)}
            />
            {scopeOptions.map((option) => (
              <FilterOption
                key={option.value}
                label={option.label}
                selected={filters.scope === option.value}
                onSelect={() => handleFilterChange('scope', option.value)}
              />
            ))}
          </FilterPane>
        ) : null}

        {selectedCriterion === 'roleType' ? (
          <FilterPane title='Role type'>
            <FilterOption
              label='All role types'
              selected={!filters.roleType}
              onSelect={() => handleFilterChange('roleType', undefined)}
            />
            {roleTypeOptions.map((option) => (
              <FilterOption
                key={option.value}
                label={option.label}
                selected={filters.roleType === option.value}
                onSelect={() => handleFilterChange('roleType', option.value)}
              />
            ))}
          </FilterPane>
        ) : null}
      </AdminFilterDialog>
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
      <div className='space-y-1'>{children}</div>
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
      title={label}
      className={`flex w-full min-w-0 items-center justify-between gap-2 rounded-md px-3 py-2 text-left text-sm hover:bg-muted ${
        selected ? 'bg-muted font-medium' : ''
      }`}
    >
      <span className='min-w-0 flex-1 truncate'>{label}</span>
      {selected ? <Check className='h-4 w-4 shrink-0 text-primary' /> : null}
    </button>
  );
}
