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
  Building2,
  Filter,
  Eye,
  Edit,
  Trash2,
  X,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
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
  CreateDepartmentDialog,
  UpdateDepartmentDialog,
  DeleteDepartmentDialog,
  DepartmentDetailDialog,
  useSettingsDepartments,
} from '@/modules/settings';
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
      <Avatar className='h-6 w-6'>
        <AvatarFallback className='text-[10px] bg-purple-100 text-purple-700'>
          {row.managerName
            .split(' ')
            .map((n) => n[0])
            .join('')}
        </AvatarFallback>
      </Avatar>
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

// ==================== Main Page ====================

export default function SettingsDepartmentsPage() {
  const [activeTab, setActiveTab] = useState('departments');

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
    // Filter helpers
    activeFilterCount,
    activeFilterBadges,
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

  // Advanced filters UI
  const [showFilters, setShowFilters] = useState(false);
  const hasAdvancedFilters = filters.managerId !== undefined;

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

  const clearAdvancedFilters = useCallback(() => {
    setManagerFilter(undefined);
  }, [setManagerFilter]);

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
          {/* Filters */}
          <Card className='border-border/70 shadow-sm'>
            <CardContent className='p-4 md:p-5'>
              <div className='space-y-4'>
                <div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
                  <div>
                    <h2 className='text-sm font-semibold'>Search & Filters</h2>
                    <p className='text-xs text-muted-foreground mt-1'>
                      Find departments by name, status, hierarchy, or manager.
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
                          {filters.managerId !== undefined ? 1 : 0}
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

                {/* Main filters row */}
                <div className='grid gap-3 md:grid-cols-12'>
                  <div className='md:col-span-5 lg:col-span-6'>
                    <Label
                      htmlFor='dept-search'
                      className='mb-2 text-xs font-medium text-muted-foreground'
                    >
                      Search departments
                    </Label>
                    <div className='relative'>
                      <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                      <Input
                        id='dept-search'
                        placeholder='Name, code, or description...'
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
                            setSearch('');
                          }}
                          className='absolute right-1 top-1/2 h-8 w-8 -translate-y-1/2 text-muted-foreground hover:text-foreground'
                          aria-label='Clear search'
                        >
                          <X className='h-4 w-4' />
                        </Button>
                      )}
                    </div>
                  </div>

                  <div className='md:col-span-3 lg:col-span-3'>
                    <Label className='mb-2 text-xs font-medium text-muted-foreground'>
                      Status
                    </Label>
                    <Select
                      value={
                        filters.isActive === undefined
                          ? 'all'
                          : filters.isActive
                            ? 'active'
                            : 'inactive'
                      }
                      onValueChange={(value) =>
                        setActiveFilter(
                          value === 'all' ? undefined : value === 'active'
                        )
                      }
                    >
                      <SelectTrigger className='h-10'>
                        <SelectValue placeholder='All statuses' />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value='all'>All Statuses</SelectItem>
                        <SelectItem value='active'>Active</SelectItem>
                        <SelectItem value='inactive'>Inactive</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className='md:col-span-4 lg:col-span-3'>
                    <Label className='mb-2 text-xs font-medium text-muted-foreground'>
                      Parent department
                    </Label>
                    <Select
                      value={
                        filters.parentDepartmentId !== undefined
                          ? String(filters.parentDepartmentId)
                          : 'all'
                      }
                      onValueChange={(value) =>
                        setParentFilter(
                          value === 'all' ? undefined : Number(value)
                        )
                      }
                    >
                      <SelectTrigger className='h-10'>
                        <SelectValue placeholder='All departments' />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value='all'>All Departments</SelectItem>
                        <SelectItem value='0'>Top Level Only</SelectItem>
                        {activeDepartments.map((dept) => (
                          <SelectItem key={dept.id} value={String(dept.id)}>
                            Under {dept.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                {/* Active filter badges */}
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

                {/* Advanced filters */}
                {showFilters && (
                  <div className='rounded-lg border bg-muted/40 p-3 md:p-4'>
                    <div className='grid gap-3 md:grid-cols-2'>
                      <div className='space-y-2'>
                        <Label
                          htmlFor='dept-manager-filter'
                          className='text-xs font-medium text-muted-foreground'
                        >
                          Manager
                        </Label>
                        <Select
                          value={
                            filters.managerId !== undefined
                              ? String(filters.managerId)
                              : 'all'
                          }
                          onValueChange={(v) =>
                            setManagerFilter(
                              v === 'all' ? undefined : Number(v)
                            )
                          }
                        >
                          <SelectTrigger
                            id='dept-manager-filter'
                            className='h-10'
                          >
                            <SelectValue placeholder='All managers' />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value='all'>All Managers</SelectItem>
                            {managers.map((mgr) => (
                              <SelectItem key={mgr.id} value={String(mgr.id)}>
                                {mgr.name}
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
          <Card>
            <CardContent className='p-4 md:p-6'>
              <DepartmentOrgChart
                tree={departmentTree}
                isLoading={isLoadingTree}
                onView={handleTreeView}
                onEdit={handleTreeEdit}
                onAddChild={handleTreeAddChild}
                onDelete={handleTreeDelete}
              />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* ==================== Dialogs ==================== */}

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
