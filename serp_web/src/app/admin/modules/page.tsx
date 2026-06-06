/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modules management page
 */

'use client';

import React, { useMemo, useState } from 'react';
import {
  AdminFilterChips,
  AdminFilterDialog,
  AdminStatusBadge,
  AdminActionMenu,
  AdminStatsCard,
} from '@/modules/admin';
import type { Module } from '@/modules/admin';
import { useModules } from '@/modules/admin/hooks/useModules';
import { ModuleFormDialog } from '@/modules/admin/components/modules/ModuleFormDialog';
import { Card, Button, Input } from '@/shared/components';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import {
  Puzzle,
  Plus,
  Eye,
  Edit,
  Power,
  PowerOff,
  CheckCircle,
  Search,
  SlidersHorizontal,
  XCircle,
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
    handleSearch,
    handleFilterChange,
  } = useModules();

  const handleToggleStatus = async (
    moduleId: string,
    currentStatus: string
  ) => {
    await toggleStatus(moduleId, currentStatus);
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
            statusOptions.find((item) => item.value === filters.status)?.label ??
            filters.status
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
        id: 'icon',
        header: 'Icon',
        accessor: 'icon',
        defaultVisible: false,
        cell: ({ value }) => <span className='text-sm'>{value || 'N/A'}</span>,
      },
      {
        id: 'order',
        header: 'Order',
        accessor: 'displayOrder',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{value}</span>,
      },
      {
        id: 'category',
        header: 'Category',
        accessor: 'category',
        defaultVisible: false,
        cell: ({ value }) => <span className='text-sm'>{value || 'N/A'}</span>,
      },
      {
        id: 'version',
        header: 'Version',
        accessor: 'version',
        defaultVisible: false,
        cell: ({ value }) => <span className='text-sm'>{value || '—'}</span>,
      },
      {
        id: 'keycloak',
        header: 'Keycloak Client',
        accessor: 'keycloakClientId',
        defaultVisible: false,
        cell: ({ value }) => <span className='text-xs'>{value || '—'}</span>,
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
                label: 'View Details',
                onClick: () => console.log('View', row.id),
                icon: <Eye className='h-4 w-4' />,
              },
              {
                label: 'Edit',
                onClick: () => openEditDialog(row as unknown as Module),
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: row.status === 'ACTIVE' ? 'Disable' : 'Enable',
                onClick: () => handleToggleStatus(String(row.id), row.status),
                icon:
                  row.status === 'ACTIVE' ? (
                    <PowerOff className='h-4 w-4' />
                  ) : (
                    <Power className='h-4 w-4' />
                  ),
                separator: true,
                variant: row.status === 'ACTIVE' ? 'destructive' : 'default',
              },
            ]}
          />
        ),
      },
    ],
    [handleToggleStatus, openEditDialog]
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
        <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
          <div className='relative w-full md:max-w-md'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search by name, code, description...'
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

      {/* Modules Table */}
      <DataTable
        columns={columns}
        data={modules || []}
        keyExtractor={(module) => String(module.id)}
        isLoading={isLoading}
        error={error}
        storageKey='admin-modules-columns'
        loadingState={
          <div className='flex items-center justify-center h-32 sm:h-64'>
            <div className='text-muted-foreground'>Loading modules...</div>
          </div>
        }
        errorState={
          <div className='flex items-center justify-center h-32 sm:h-64'>
            <div className='text-destructive'>Failed to load modules</div>
          </div>
        }
        emptyState={
          <div className='flex flex-col items-center justify-center text-center px-4 py-8 sm:py-12'>
            <Puzzle className='h-8 w-8 sm:h-12 sm:w-12 text-muted-foreground mb-4' />
            <h3 className='text-base sm:text-lg font-medium'>No modules yet</h3>
            <p className='text-sm text-muted-foreground mt-1 max-w-sm'>
              Create your first module to get started
            </p>
            <Button size='sm' className='mt-4' onClick={openCreateDialog}>
              <Plus className='h-4 w-4 mr-2' />
              Create Module
            </Button>
          </div>
        }
      />

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
      {selected ? <CheckCircle className='h-4 w-4 shrink-0 text-primary' /> : null}
    </button>
  );
}
