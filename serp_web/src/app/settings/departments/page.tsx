/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings departments management page
 */

'use client';

import React, { useMemo, useState, useEffect, useCallback } from 'react';
import {
  Layers,
  Plus,
  Search,
  Users,
  UserPlus,
  Eye,
  Edit,
  Trash2,
  X,
  SlidersHorizontal,
  CheckCircle,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Combobox } from '@/shared/components/ui/combobox';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import {
  SettingsStatsCard,
  SettingsActionMenu,
  SettingsStatusBadge,
  SettingsFilterDialog,
  SettingsFilterChips,
  CreateDepartmentDialog,
  UpdateDepartmentDialog,
  DeleteDepartmentDialog,
  DepartmentDetailDialog,
  useSettingsDepartments,
} from '@/modules/settings';
import type { SettingsFilterChip } from '@/modules/settings';
import { DepartmentOrgChart } from '@/modules/settings/components/departments/DepartmentOrgChart';
import { AddMemberDialog } from '@/modules/settings/components/departments/AddMemberDialog';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import type { Department, DepartmentTreeNode } from '@/modules/settings';
import { useDebounce } from '@/shared/hooks';

// ==================== Sub-components ====================

const DepartmentNameCell = ({ row }: { row: Department }) => (
  <div className='flex items-center gap-3'>
    <div className='h-9 w-9 rounded-lg bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center flex-shrink-0'>
      <Layers className='h-4 w-4 text-purple-600 dark:text-purple-400' />
    </div>
    <div className='min-w-0'>
      <p className='font-medium text-sm truncate'>{row.name}</p>
      {row.code && (
        <p className='text-xs text-muted-foreground font-mono'>{row.code}</p>
      )}
    </div>
  </div>
);

const ManagerCell = ({ row }: { row: Department }) => {
  if (!row.managerName) {
    return <span className='text-sm text-muted-foreground'>Unassigned</span>;
  }
  return (
    <div className='flex items-center gap-2'>
      <div className='h-6 w-6 rounded-full bg-purple-100 text-purple-700 flex items-center justify-center text-[10px] font-medium'>
        {row.managerName
          .split(' ')
          .map((n) => n[0])
          .join('')}
      </div>
      <span className='text-sm truncate'>{row.managerName}</span>
    </div>
  );
};

const formatDate = (timestamp?: number) => {
  if (!timestamp) return '-';
  return new Date(timestamp).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

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

export default function SettingsDepartmentsPage() {
  const [activeTab, setActiveTab] = useState('departments');
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [selectedCriterion, setSelectedCriterion] = useState('status');

  const {
    organizationId,
    isLoading,
    isFetching,
    error,
    departments,
    activeDepartments,
    totalPages,
    totalItems,
    currentPage,
    statistics,
    activeModules,
    managers,
    search,
    filters,
    setSearch,
    setPage,
    setActiveFilter,
    setParentFilter,
    setManagerFilter,
    clearFilters,
    create,
    update,
    remove,
    createStatus,
    updateStatus,
    deleteStatus,
    removeUserFromDept,
    assignUserToDept,
    bulkAssignUsersToDept,
    assignStatus,
    bulkAssignStatus,
    useDepartmentMembers,
    // Tree
    departmentTree,
    isLoadingTree,
  } = useSettingsDepartments();

  // Dialog states
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [updateDialogOpen, setUpdateDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedDepartment, setSelectedDepartment] =
    useState<Department | null>(null);

  // Pre-selected parent for "Add Sub-department" from org chart
  const [presetParentId, setPresetParentId] = useState<number | undefined>();

  // Add member dialog state
  const [addMemberDialogOpen, setAddMemberDialogOpen] = useState(false);
  const [memberTargetDepartment, setMemberTargetDepartment] =
    useState<Department | null>(null);

  // Search debounce
  const [searchInput, setSearchInput] = useState(search || '');
  const debouncedSearch = useDebounce(searchInput, 400);
  useEffect(() => {
    setSearch(debouncedSearch || '');
  }, [debouncedSearch, setSearch]);

  const clearAllFilters = useCallback(() => {
    setSearchInput('');
    clearFilters();
  }, [clearFilters]);

  const filterChips = useMemo<SettingsFilterChip[]>(() => {
    const chips: SettingsFilterChip[] = [];

    if (search) {
      chips.push({
        id: 'search',
        label: `Keyword: "${search}"`,
        onRemove: () => {
          setSearchInput('');
          setSearch('');
        },
      });
    }

    if (filters.isActive !== undefined) {
      chips.push({
        id: 'status',
        label: `Status: ${filters.isActive ? 'Active' : 'Inactive'}`,
        onRemove: () => setActiveFilter(undefined),
      });
    }

    if (filters.parentDepartmentId !== undefined) {
      const label =
        filters.parentDepartmentId === 0
          ? 'Top Level Only'
          : (activeDepartments.find((d) => d.id === filters.parentDepartmentId)
              ?.name ?? `Department #${filters.parentDepartmentId}`);
      chips.push({
        id: 'parent',
        label: `Parent: ${label}`,
        onRemove: () => setParentFilter(undefined),
      });
    }

    if (filters.managerId !== undefined) {
      const manager = managers.find((m) => m.id === filters.managerId);
      chips.push({
        id: 'manager',
        label: `Manager: ${manager?.name || `Manager #${filters.managerId}`}`,
        onRemove: () => setManagerFilter(undefined),
      });
    }

    return chips;
  }, [
    search,
    filters.isActive,
    filters.parentDepartmentId,
    filters.managerId,
    activeDepartments,
    managers,
    setSearch,
    setActiveFilter,
    setParentFilter,
    setManagerFilter,
  ]);

  const filterCriteria = useMemo(
    () => [
      {
        id: 'status',
        label: 'Status',
        count: filters.isActive !== undefined ? 1 : 0,
      },
      {
        id: 'parent',
        label: 'Parent department',
        count: filters.parentDepartmentId !== undefined ? 1 : 0,
      },
      {
        id: 'manager',
        label: 'Manager',
        count: filters.managerId !== undefined ? 1 : 0,
      },
    ],
    [filters.isActive, filters.parentDepartmentId, filters.managerId]
  );

  // Handlers
  const handleView = useCallback((dept: Department) => {
    setSelectedDepartment(dept);
    setDetailDialogOpen(true);
  }, []);

  const handleEdit = useCallback((dept: Department) => {
    setSelectedDepartment(dept);
    setUpdateDialogOpen(true);
  }, []);

  const handleDelete = useCallback((dept: Department) => {
    setSelectedDepartment(dept);
    setDeleteDialogOpen(true);
  }, []);

  const handlePageChange = useCallback(
    (newPage: number) => setPage(newPage),
    [setPage]
  );

  // Member management handlers
  const handleAddMembers = useCallback((dept: Department) => {
    setMemberTargetDepartment(dept);
    setAddMemberDialogOpen(true);
  }, []);

  const handleRemoveMember = useCallback(
    async (departmentId: number, userId: number) => {
      await removeUserFromDept(departmentId, userId);
    },
    [removeUserFromDept]
  );

  // Org chart handlers (convert tree node to Department for dialogs)
  const handleTreeView = useCallback(
    (node: DepartmentTreeNode) => {
      const dept = departments.find((d) => d.id === node.id);
      if (dept) handleView(dept);
    },
    [departments, handleView]
  );

  const handleTreeEdit = useCallback(
    (node: DepartmentTreeNode) => {
      const dept = departments.find((d) => d.id === node.id);
      if (dept) handleEdit(dept);
    },
    [departments, handleEdit]
  );

  const handleTreeAddChild = useCallback((parentNode: DepartmentTreeNode) => {
    setPresetParentId(parentNode.id);
    setCreateDialogOpen(true);
  }, []);

  const handleTreeDelete = useCallback(
    (node: DepartmentTreeNode) => {
      const dept = departments.find((d) => d.id === node.id);
      if (dept) handleDelete(dept);
    },
    [departments, handleDelete]
  );

  // ==================== Table Column Definitions ====================

  const departmentColumns = useMemo<ColumnDef<Department>[]>(
    () => [
      {
        id: 'department',
        header: 'Department',
        accessor: 'name',
        defaultVisible: true,
        cell: ({ row }) => <DepartmentNameCell row={row} />,
      },
      {
        id: 'parent',
        header: 'Parent',
        accessor: 'parentDepartmentName',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>{value || '-'}</span>
        ),
      },
      {
        id: 'manager',
        header: 'Manager',
        accessor: 'managerName',
        defaultVisible: true,
        cell: ({ row }) => <ManagerCell row={row} />,
      },
      {
        id: 'members',
        header: 'Members',
        accessor: 'memberCount',
        defaultVisible: true,
        align: 'center',
        cell: ({ value }) => (
          <div className='flex items-center justify-center gap-1'>
            <Users className='h-3.5 w-3.5 text-muted-foreground' />
            <span className='text-sm'>{value ?? 0}</span>
          </div>
        ),
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'isActive',
        defaultVisible: true,
        cell: ({ value }) => (
          <SettingsStatusBadge status={value ? 'ACTIVE' : 'INACTIVE'} />
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
        id: 'updated',
        header: 'Updated',
        accessor: 'updatedAt',
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
                onClick: () => handleView(row),
                icon: <Eye className='h-4 w-4' />,
              },
              {
                label: 'Edit Department',
                onClick: () => handleEdit(row),
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: row.managerId ? 'Change Manager' : 'Assign Manager',
                onClick: () => handleEdit(row),
                icon: <UserPlus className='h-4 w-4' />,
              },
              {
                label: 'Manage Members',
                onClick: () => handleView(row),
                icon: <Users className='h-4 w-4' />,
                separator: true,
              },
              {
                label: 'Delete',
                onClick: () => handleDelete(row),
                icon: <Trash2 className='h-4 w-4' />,
                variant: 'destructive',
                separator: true,
              },
            ]}
          />
        ),
      },
    ],
    [handleView, handleEdit, handleDelete]
  );

  if (!organizationId) {
    return (
      <div className='flex items-center justify-center h-96'>
        <p className='text-muted-foreground'>
          Please log in to view departments
        </p>
      </div>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>
            Department Management
          </h1>
          <p className='text-muted-foreground mt-1'>
            Organize your team into departments and manage hierarchy
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <Button
            onClick={() => {
              setPresetParentId(undefined);
              setCreateDialogOpen(true);
            }}
          >
            <Plus className='h-4 w-4 mr-2' />
            Create Department
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Departments'
          value={statistics?.totalDepartments ?? '-'}
          description='All departments'
          icon={<Layers className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='Total Members'
          value={statistics?.totalMembers ?? '-'}
          description='Across all departments'
          icon={<Users className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='Avg Team Size'
          value={statistics?.averageTeamSize ?? '-'}
          description='Members per department'
          icon={<Users className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='With Managers'
          value={statistics?.departmentsWithManagers ?? '-'}
          description='Departments assigned'
          icon={<UserPlus className='h-4 w-4' />}
        />
      </div>

      {/* Tabs: Departments / Organization Chart */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='departments'>
            Departments
            {totalItems > 0 && (
              <span className='ml-1.5 text-xs bg-muted px-1.5 py-0.5 rounded-full'>
                {totalItems}
              </span>
            )}
          </TabsTrigger>
          <TabsTrigger value='orgchart'>Organization Chart</TabsTrigger>
        </TabsList>

        {/* ==================== Departments Tab ==================== */}
        <TabsContent value='departments' className='space-y-4'>
          {/* Search + Filters */}
          <div className='space-y-3'>
            <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
              <div className='relative w-full md:max-w-md'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder='Search by name, code, or description...'
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
                      setSearch('');
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

          {/* Departments Table */}
          <DataTable
            columns={departmentColumns}
            data={departments}
            keyExtractor={(d) => String(d.id)}
            isLoading={isLoading}
            error={error}
            storageKey='settings-departments-columns'
            pagination={{
              currentPage,
              totalPages,
              totalItems,
              onPageChange: handlePageChange,
              isFetching,
            }}
          />
        </TabsContent>

        {/* ==================== Organization Chart Tab ==================== */}
        <TabsContent value='orgchart' className='space-y-4'>
          <div className='rounded-lg border bg-card p-4 md:p-6 shadow-sm'>
            <DepartmentOrgChart
              tree={departmentTree}
              isLoading={isLoadingTree}
              onView={handleTreeView}
              onEdit={handleTreeEdit}
              onAddChild={handleTreeAddChild}
              onDelete={handleTreeDelete}
            />
          </div>
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
              selected={filters.isActive === undefined}
              onSelect={() => setActiveFilter(undefined)}
            />
            <FilterOption
              label='Active'
              selected={filters.isActive === true}
              onSelect={() => setActiveFilter(true)}
            />
            <FilterOption
              label='Inactive'
              selected={filters.isActive === false}
              onSelect={() => setActiveFilter(false)}
            />
          </FilterPane>
        ) : null}

        {selectedCriterion === 'parent' ? (
          <FilterPane title='Parent department'>
            <Combobox
              value={
                filters.parentDepartmentId === 0
                  ? 0
                  : filters.parentDepartmentId
              }
              onChange={(value) =>
                setParentFilter(value !== undefined ? Number(value) : undefined)
              }
              items={[
                { value: 0, label: 'Top Level Only' },
                ...activeDepartments.map((dept) => ({
                  value: dept.id,
                  label: `Under ${dept.name}`,
                })),
              ]}
              placeholder='All departments'
            />
          </FilterPane>
        ) : null}

        {selectedCriterion === 'manager' ? (
          <FilterPane title='Manager'>
            <Combobox
              value={filters.managerId}
              onChange={(value) =>
                setManagerFilter(
                  value !== undefined ? Number(value) : undefined
                )
              }
              items={managers.map((mgr) => ({
                value: mgr.id,
                label: mgr.name,
              }))}
              placeholder='All managers'
            />
          </FilterPane>
        ) : null}
      </SettingsFilterDialog>

      <CreateDepartmentDialog
        open={createDialogOpen}
        onOpenChange={(open) => {
          setCreateDialogOpen(open);
          if (!open) setPresetParentId(undefined);
        }}
        onSubmit={create}
        isLoading={createStatus.isLoading}
        departments={activeDepartments}
        managers={managers}
        modules={activeModules}
      />

      <UpdateDepartmentDialog
        open={updateDialogOpen}
        onOpenChange={setUpdateDialogOpen}
        organizationId={organizationId}
        department={selectedDepartment}
        departments={activeDepartments}
        managers={managers}
      />

      <DeleteDepartmentDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        department={selectedDepartment}
        onConfirm={remove}
        isLoading={deleteStatus.isLoading}
      />

      <DepartmentDetailDialog
        open={detailDialogOpen}
        onOpenChange={setDetailDialogOpen}
        department={selectedDepartment}
        useDepartmentMembers={useDepartmentMembers}
        onAddMembers={handleAddMembers}
        onRemoveMember={handleRemoveMember}
      />

      <AddMemberDialog
        open={addMemberDialogOpen}
        onOpenChange={setAddMemberDialogOpen}
        department={memberTargetDepartment}
        useDepartmentMembers={useDepartmentMembers}
        managers={managers}
        onAssignUser={assignUserToDept}
        onBulkAssignUsers={bulkAssignUsersToDept}
        isAssigning={assignStatus.isLoading}
        isBulkAssigning={bulkAssignStatus.isLoading}
      />
    </div>
  );
}
