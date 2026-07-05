/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modules management page
 */

'use client';

import React, { useMemo, useState, useEffect } from 'react';
import {
  AdminFilterChips,
  AdminFilterDialog,
  AdminStatusBadge,
  AdminActionMenu,
  AdminStatsCard,
  FilterPane,
  FilterOption,
} from '@/modules/admin';
import type { Module } from '@/modules/admin';
import { useModules } from '@/modules/admin/hooks/useModules';
import { ModuleFormDialog } from '@/modules/admin/components/modules/ModuleFormDialog';
import { Card, Button, Input } from '@/shared/components';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import { useDebounce } from '@/shared/hooks';
import { AdminConfirmStatusDialog } from '@/modules/admin/components/shared/AdminConfirmStatusDialog';
import {
  Puzzle,
  Plus,
  Edit,
  Power,
  PowerOff,
  CheckCircle,
  Search,
  SlidersHorizontal,
  XCircle,
  LayoutGrid,
  List,
  Layers,
  Coins,
  ShieldCheck,
} from 'lucide-react';

const statusOptions = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'BETA', label: 'Beta' },
  { value: 'DEPRECATED', label: 'Deprecated' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'DISABLED', label: 'Disabled' },
];

const typeOptions = [
  { value: 'SYSTEM', label: 'System' },
  { value: 'CUSTOM', label: 'Custom' },
];

export default function ModulesPage() {
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [selectedCriterion, setSelectedCriterion] = useState('status');

  const {
    modules,
    stats,
    pageInfo,
    isLoading,
    error,
    openCreateDialog,
    openEditDialog,
    toggleStatus,
    isDialogOpen,
    selectedModule,
    isCreating,
    submitModule,
    closeDialog,
    filters,
    view,
    setPage,
    setViewMode,
    handleSearch,
    handleFilterChange,
  } = useModules();

  const [searchInput, setSearchInput] = useState(filters.search || '');
  const debouncedSearch = useDebounce(searchInput, 400);

  const [confirmOpen, setConfirmOpen] = useState(false);
  const [selectedModuleForToggle, setSelectedModuleForToggle] = useState<{ id: string; status: string } | null>(null);

  useEffect(() => {
    setSearchInput(filters.search || '');
  }, [filters.search]);

  useEffect(() => {
    if (debouncedSearch !== (filters.search || '')) {
      handleSearch(debouncedSearch);
    }
  }, [debouncedSearch, handleSearch, filters.search]);

  const handleToggleStatus = async (
    moduleId: string,
    currentStatus: string
  ) => {
    if (currentStatus === 'ACTIVE') {
      setSelectedModuleForToggle({ id: moduleId, status: currentStatus });
      setConfirmOpen(true);
    } else {
      await toggleStatus(moduleId, currentStatus);
    }
  };

  const handleConfirmToggle = async () => {
    if (selectedModuleForToggle) {
      await toggleStatus(selectedModuleForToggle.id, selectedModuleForToggle.status);
      setConfirmOpen(false);
      setSelectedModuleForToggle(null);
    }
  };

  const filterCriteria = [
    { id: 'status', label: 'Status', count: filters.status ? 1 : 0 },
    { id: 'type', label: 'Type', count: filters.type ? 1 : 0 },
  ];

  const filterChips = [
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
    filters.type
      ? {
          id: 'type',
          label: `Type: ${
            typeOptions.find((item) => item.value === filters.type)?.label ??
            filters.type
          }`,
          onRemove: () => handleFilterChange('type', undefined),
        }
      : null,
  ].filter(Boolean) as Array<{
    id: string;
    label: string;
    onRemove: () => void;
  }>;

  const clearFilters = () => {
    handleFilterChange('status', undefined);
    handleFilterChange('type', undefined);
  };

  // Define columns for DataTable
  const columns = useMemo<ColumnDef<Module>[]>(
    () => [
      {
        id: 'module',
        header: 'Module',
        accessor: 'moduleName',
        defaultVisible: true,
        cell: ({ row }) => (
          <div className='flex items-center gap-2 sm:gap-3'>
            <div className='h-8 w-8 sm:h-10 sm:w-10 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0'>
              <Puzzle className='h-4 w-4 sm:h-5 sm:w-5 text-primary' />
            </div>
            <div className='min-w-0 flex-1'>
              <p className='font-medium truncate'>{row.moduleName}</p>
              {row.description && (
                <p className='text-xs text-muted-foreground max-w-xs sm:max-w-md truncate'>
                  {row.description}
                </p>
              )}
            </div>
          </div>
        ),
      },
      {
        id: 'code',
        header: 'Code',
        accessor: 'code',
        defaultVisible: true,
        cell: ({ value }) => (
          <code className='text-xs bg-muted px-2 py-1 rounded'>{value}</code>
        ),
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'status',
        defaultVisible: true,
        cell: ({ value }) => <AdminStatusBadge status={value} />,
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'moduleType',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm font-medium'>{value}</span>
        ),
      },
      {
        id: 'pricing',
        header: 'Pricing',
        accessor: 'pricingModel',
        defaultVisible: true,
        cell: ({ row }) => (
          <span className='text-sm'>
            {row.isFree ? 'FREE' : row.pricingModel}
          </span>
        ),
      },
      {
        id: 'visibility',
        header: 'Visibility',
        accessor: 'isGlobal',
        defaultVisible: true,
        cell: ({ row }) => (
          <span className='text-sm'>
            {row.isGlobal ? 'Global' : `Org #${row.organizationId ?? '-'}`}
          </span>
        ),
      },
      {
        id: 'order',
        header: 'Order',
        accessor: 'displayOrder',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{value}</span>,
      },
      {
        id: 'actions',
        header: 'Actions',
        accessor: 'id',
        align: 'right',
        defaultVisible: true,
        cell: ({ row }) => (
          <div className='flex items-center justify-end gap-2'>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => handleToggleStatus(String(row.id), row.status)}
              title={row.status === 'ACTIVE' ? 'Disable Module' : 'Enable Module'}
              className='h-8 w-8 p-0 text-muted-foreground hover:text-foreground'
            >
              {row.status === 'ACTIVE' ? (
                <PowerOff className='h-4 w-4 text-destructive' />
              ) : (
                <Power className='h-4 w-4 text-success' />
              )}
            </Button>
            <AdminActionMenu
              items={[
                {
                  label: 'Edit',
                  onClick: () => openEditDialog(row as unknown as Module),
                  icon: <Edit className='h-4 w-4' />,
                },
              ]}
            />
          </div>
        ),
      },
    ],
    [openEditDialog]
  );

  return (
    <div className='space-y-4 sm:space-y-6 px-4 sm:px-6 lg:px-8'>
      {/* Page Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-2xl sm:text-3xl font-bold tracking-tight'>
            Modules
          </h1>
          <p className='text-muted-foreground mt-1 sm:mt-2'>
            Manage system modules and features
          </p>
        </div>
        <Button size='sm' onClick={openCreateDialog}>
          <Plus className='h-4 w-4 mr-2' />
          Create Module
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3'>
        <AdminStatsCard
          title='Total Modules'
          value={stats.total}
          icon={<Puzzle className='h-4 w-4' />}
        />

        <AdminStatsCard
          title='Enabled'
          value={stats.enabled}
          icon={<CheckCircle className='h-4 w-4' />}
        />

        <AdminStatsCard
          title='Disabled'
          value={stats.disabled}
          icon={<XCircle className='h-4 w-4' />}
        />
      </div>

      <div className='space-y-3'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div className='relative w-full sm:max-w-md'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search by name, code, description...'
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              className='pl-10'
            />
          </div>
          <div className='flex items-center gap-2 self-end sm:self-auto'>
            <Button
              type='button'
              variant='outline'
              onClick={() => setFilterDialogOpen(true)}
            >
              <SlidersHorizontal className='h-4 w-4' />
              Filters
            </Button>
            <div className='flex items-center border border-input bg-background rounded-md p-1 h-10'>
              <Button
                variant={view === 'grid' ? 'secondary' : 'ghost'}
                size='sm'
                onClick={() => setViewMode('grid')}
                className='h-8 px-2.5 rounded-sm'
                title='Grid View'
              >
                <LayoutGrid className='h-4 w-4' />
              </Button>
              <Button
                variant={view === 'list' ? 'secondary' : 'ghost'}
                size='sm'
                onClick={() => setViewMode('list')}
                className='h-8 px-2.5 rounded-sm'
                title='List View'
              >
                <List className='h-4 w-4' />
              </Button>
            </div>
          </div>
        </div>

        <AdminFilterChips chips={filterChips} onClearAll={clearFilters} />
      </div>

      {/* Modules Main Content */}
      {isLoading ? (
        <div className='flex items-center justify-center h-64 border border-dashed rounded-lg bg-card'>
          <div className='flex flex-col items-center gap-2'>
            <div className='h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent' />
            <div className='text-sm text-muted-foreground'>Loading modules...</div>
          </div>
        </div>
      ) : error ? (
        <div className='flex items-center justify-center h-64 border border-dashed border-destructive/50 rounded-lg bg-destructive/5'>
          <div className='text-center p-6'>
            <XCircle className='h-10 w-10 text-destructive mx-auto mb-2' />
            <h3 className='font-semibold text-destructive'>Failed to load modules</h3>
            <p className='text-sm text-muted-foreground mt-1'>Please try refreshing the page.</p>
          </div>
        </div>
      ) : modules.length === 0 ? (
        <div className='flex flex-col items-center justify-center text-center px-4 py-12 border border-dashed rounded-lg bg-card'>
          <Puzzle className='h-12 w-12 text-muted-foreground mb-4' />
          <h3 className='text-lg font-medium'>No modules yet</h3>
          <p className='text-sm text-muted-foreground mt-1 max-w-sm'>
            Create your first module or adjust your filters to get started.
          </p>
          <Button size='sm' className='mt-4' onClick={openCreateDialog}>
            <Plus className='h-4 w-4 mr-2' />
            Create Module
          </Button>
        </div>
      ) : view === 'grid' ? (
        /* Grid Layout (App Store style) */
        <div className='space-y-6'>
          <div className='grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3'>
            {modules.map((module) => (
              <Card
                key={module.id}
                className='flex flex-col justify-between overflow-hidden group hover:shadow-md transition-all duration-300 border border-border/60 hover:border-border'
              >
                <div className='p-5 space-y-4'>
                  <div className='flex items-start justify-between gap-3'>
                    <div className='flex items-center gap-3'>
                      <div className='h-12 w-12 rounded-xl bg-gradient-to-br from-primary/5 to-primary/20 flex items-center justify-center flex-shrink-0 text-primary border border-primary/10 group-hover:scale-105 transition-transform duration-300'>
                        <Puzzle className='h-6 w-6' />
                      </div>
                      <div className='min-w-0'>
                        <h3 className='font-semibold text-foreground text-base tracking-tight truncate group-hover:text-primary transition-colors'>
                          {module.moduleName}
                        </h3>
                        <code className='text-[10px] uppercase font-mono bg-muted px-1.5 py-0.5 rounded text-muted-foreground'>
                          {module.code}
                        </code>
                      </div>
                    </div>
                    <div className='flex-shrink-0'>
                      <AdminStatusBadge status={module.status} />
                    </div>
                  </div>

                  <p className='text-sm text-muted-foreground line-clamp-3 min-h-[60px] leading-relaxed'>
                    {module.description || 'No description provided.'}
                  </p>

                  <div className='flex flex-wrap items-center gap-2 pt-1'>
                    <span className='inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-full bg-secondary text-secondary-foreground'>
                      <Layers className='h-3 w-3' />
                      {module.moduleType}
                    </span>
                    <span className='inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-full bg-primary/5 text-primary'>
                      <Coins className='h-3 w-3' />
                      {module.isFree ? 'FREE' : module.pricingModel}
                    </span>
                    <span className='inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-full bg-accent/10 text-accent-foreground'>
                      <ShieldCheck className='h-3 w-3' />
                      {module.isGlobal ? 'Global' : 'Custom'}
                    </span>
                  </div>
                </div>

                <div className='px-5 py-3 border-t bg-muted/30 flex items-center justify-between'>
                  <span className='text-xs text-muted-foreground'>
                    Display order: <span className='font-semibold'>{module.displayOrder}</span>
                  </span>
                  <div className='flex items-center gap-1.5'>
                    <Button
                      variant='ghost'
                      size='sm'
                      onClick={() => handleToggleStatus(String(module.id), module.status)}
                      title={module.status === 'ACTIVE' ? 'Disable Module' : 'Enable Module'}
                      className='h-8 w-8 p-0 hover:bg-muted text-muted-foreground hover:text-foreground'
                    >
                      {module.status === 'ACTIVE' ? (
                        <PowerOff className='h-4 w-4 text-destructive' />
                      ) : (
                        <Power className='h-4 w-4 text-success' />
                      )}
                    </Button>
                    <Button
                      variant='ghost'
                      size='sm'
                      onClick={() => openEditDialog(module)}
                      className='h-8 w-8 p-0 hover:bg-muted text-muted-foreground hover:text-foreground'
                    >
                      <Edit className='h-4 w-4' />
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {/* Pagination Controls */}
          {pageInfo.totalPages > 1 && (
            <div className='flex flex-col sm:flex-row items-center justify-between gap-4 border-t border-border pt-6 mt-4'>
              <div className='text-sm text-muted-foreground'>
                Showing{' '}
                <span className='font-medium'>
                  {pageInfo.number * pageInfo.size + 1}
                </span>{' '}
                to{' '}
                <span className='font-medium'>
                  {Math.min((pageInfo.number + 1) * pageInfo.size, pageInfo.totalElements)}
                </span>{' '}
                of <span className='font-medium'>{pageInfo.totalElements}</span> modules
              </div>
              <div className='flex items-center gap-1.5'>
                <Button
                  variant='outline'
                  size='sm'
                  disabled={pageInfo.number === 0}
                  onClick={() => setPage(pageInfo.number)}
                  className='h-8 px-3'
                >
                  Previous
                </Button>
                {Array.from({ length: pageInfo.totalPages }, (_, i) => i + 1).map((p) => (
                  <Button
                    key={p}
                    variant={p === pageInfo.number + 1 ? 'default' : 'outline'}
                    size='sm'
                    onClick={() => setPage(p)}
                    className='h-8 w-8 p-0'
                  >
                    {p}
                  </Button>
                ))}
                <Button
                  variant='outline'
                  size='sm'
                  disabled={pageInfo.number + 1 >= pageInfo.totalPages}
                  onClick={() => setPage(pageInfo.number + 2)}
                  className='h-8 px-3'
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </div>
      ) : (
        /* List Layout (Table style) */
        <div className='space-y-4'>
          <DataTable
            columns={columns}
            data={modules}
            keyExtractor={(module) => String(module.id)}
            isLoading={isLoading}
            error={error}
            storageKey='admin-modules-columns'
            emptyState={
              <div className='flex flex-col items-center justify-center text-center px-4 py-8'>
                <Puzzle className='h-8 w-8 text-muted-foreground mb-4' />
                <h3 className='font-medium'>No modules match the filter</h3>
              </div>
            }
          />

          {/* Pagination Controls */}
          {pageInfo.totalPages > 1 && (
            <div className='flex flex-col sm:flex-row items-center justify-between gap-4 border-t border-border pt-4 mt-2'>
              <div className='text-sm text-muted-foreground'>
                Showing{' '}
                <span className='font-medium'>
                  {pageInfo.number * pageInfo.size + 1}
                </span>{' '}
                to{' '}
                <span className='font-medium'>
                  {Math.min((pageInfo.number + 1) * pageInfo.size, pageInfo.totalElements)}
                </span>{' '}
                of <span className='font-medium'>{pageInfo.totalElements}</span> modules
              </div>
              <div className='flex items-center gap-1.5'>
                <Button
                  variant='outline'
                  size='sm'
                  disabled={pageInfo.number === 0}
                  onClick={() => setPage(pageInfo.number)}
                  className='h-8 px-3'
                >
                  Previous
                </Button>
                {Array.from({ length: pageInfo.totalPages }, (_, i) => i + 1).map((p) => (
                  <Button
                    key={p}
                    variant={p === pageInfo.number + 1 ? 'default' : 'outline'}
                    size='sm'
                    onClick={() => setPage(p)}
                    className='h-8 w-8 p-0'
                  >
                    {p}
                  </Button>
                ))}
                <Button
                  variant='outline'
                  size='sm'
                  disabled={pageInfo.number + 1 >= pageInfo.totalPages}
                  onClick={() => setPage(pageInfo.number + 2)}
                  className='h-8 px-3'
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Create/Edit Module Dialog */}
      <ModuleFormDialog
        open={isDialogOpen}
        onOpenChange={(open) => {
          if (!open) closeDialog();
        }}
        module={selectedModule}
        onSubmit={submitModule}
        isLoading={isCreating}
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

        {selectedCriterion === 'type' ? (
          <FilterPane title='Type'>
            <FilterOption
              label='All types'
              selected={!filters.type}
              onSelect={() => handleFilterChange('type', undefined)}
            />
            {typeOptions.map((option) => (
              <FilterOption
                key={option.value}
                label={option.label}
                selected={filters.type === option.value}
                onSelect={() => handleFilterChange('type', option.value)}
              />
            ))}
          </FilterPane>
        ) : null}
      </AdminFilterDialog>

      <AdminConfirmStatusDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        onConfirm={handleConfirmToggle}
        title="Disable Module?"
        description="Are you sure you want to disable this module? Users will lose access to all its features and associated roles."
        impactText="This action will take effect immediately."
        confirmLabel="Disable"
        confirmVariant="destructive"
      />
    </div>
  );
}

