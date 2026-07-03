/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organizations management page
 */

'use client';

import React, { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  AdminActionMenu,
  AdminFilterChips,
  AdminFilterDialog,
  AdminStatusBadge,
  OrganizationDetailsDrawer,
  OrganizationStatusDialog,
  UserDialog,
  useOrganizations,
  FilterPane,
  FilterOption,
} from '@/modules/admin';
import type {
  Organization,
  OrganizationStatus,
  OrganizationType,
} from '@/modules/admin/types';
import { openCreateUserDialog } from '@/modules/admin/store';
import { formatAdminDate } from '@/modules/admin/utils/date';
import { Button, Input } from '@/shared/components/ui';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import { useAppDispatch } from '@/shared/hooks';
import {
  Ban,
  Building2,
  CheckCircle,
  Eye,
  Search,
  SlidersHorizontal,
} from 'lucide-react';

const statusOptions: Array<{ value: OrganizationStatus; label: string }> = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'TRIAL', label: 'Trial' },
  { value: 'SUSPENDED', label: 'Suspended' },
  { value: 'EXPIRED', label: 'Expired' },
  { value: 'CLOSED', label: 'Closed' },
];

const typeOptions: Array<{ value: OrganizationType; label: string }> = [
  { value: 'ENTERPRISE', label: 'Enterprise' },
  { value: 'SMB', label: 'SMB' },
  { value: 'STARTUP', label: 'Startup' },
  { value: 'PERSONAL', label: 'Personal' },
  { value: 'NON_PROFIT', label: 'Non-profit' },
  { value: 'GOVERNMENT', label: 'Government' },
];

export default function OrganizationsPage() {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [selectedCriterion, setSelectedCriterion] = useState('status');

  const {
    filters,
    organizations,
    pagination,
    isLoading,
    isFetching,
    error,
    ui,
    handleSearch,
    handleFilterChange,
    handlePageChange,
    openDetails,
    closeDetails,
    openStatus,
    closeStatus,
  } = useOrganizations();

  const selectedOrganization = useMemo(
    () =>
      organizations.find(
        (organization) => organization.id === ui.selectedOrganizationId
      ),
    [organizations, ui.selectedOrganizationId]
  );

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

  const columns = useMemo<ColumnDef<Organization>[]>(
    () => [
      {
        id: 'organization',
        header: 'Organization',
        accessor: 'name',
        defaultVisible: true,
        cell: ({ row }) => (
          <div className='flex items-center gap-3'>
            <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10'>
              <Building2 className='h-5 w-5 text-primary' />
            </div>
            <div className='min-w-0'>
              <p className='truncate font-medium'>{row.name}</p>
              <p className='text-xs text-muted-foreground'>{row.code}</p>
            </div>
          </div>
        ),
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'organizationType',
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
        id: 'employees',
        header: 'Employees',
        accessor: 'employeeCount',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{value || 'N/A'}</span>,
      },
      {
        id: 'created',
        header: 'Created',
        accessor: 'createdAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatAdminDate(value)}
          </span>
        ),
      },
      {
        id: 'subscription',
        header: 'Subscription',
        accessor: 'subscriptionExpiresAt',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {value ? `Expires ${formatAdminDate(value)}` : 'No subscription'}
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
                  onClick: () => openDetails(row.id),
                  icon: <Eye className='h-4 w-4' />,
                },
                {
                  label: targetStatus === 'SUSPENDED' ? 'Suspend' : 'Activate',
                  onClick: () => openStatus(row.id, row.status),
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
    [openDetails, openStatus]
  );

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Organizations</h1>
          <p className='mt-2 text-muted-foreground'>
            Manage organization lifecycle and linked users.
          </p>
        </div>
      </div>

      <div className='space-y-3'>
        <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
          <div className='relative w-full md:max-w-md'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search by name, code, email...'
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
        data={organizations}
        keyExtractor={(organization) => String(organization.id)}
        isLoading={isLoading}
        error={error}
        storageKey='admin-organizations-columns'
        pagination={{
          currentPage: pagination.currentPage,
          totalPages: pagination.totalPages,
          totalItems: pagination.totalItems,
          onPageChange: handlePageChange,
          isFetching,
        }}
        loadingState={
          <div className='flex h-64 items-center justify-center'>
            <div className='text-muted-foreground'>
              Loading organizations...
            </div>
          </div>
        }
        errorState={
          <div className='flex h-64 items-center justify-center'>
            <div className='text-destructive'>Failed to load organizations</div>
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

      <OrganizationDetailsDrawer
        open={ui.detailDrawerOpen}
        organizationId={ui.selectedOrganizationId}
        onOpenChange={(open) => {
          if (!open) closeDetails();
        }}
        onCreateUser={(organizationId) =>
          dispatch(openCreateUserDialog({ organizationId }))
        }
        onViewAllUsers={(organizationId) =>
          router.push(`/admin/users?organizationId=${organizationId}`)
        }
        onToggleStatus={openStatus}
      />

      <OrganizationStatusDialog
        open={ui.statusDialogOpen}
        organizationId={ui.selectedOrganizationId}
        organizationName={selectedOrganization?.name}
        currentStatus={ui.statusDialogStatus}
        onOpenChange={(open) => {
          if (!open) closeStatus();
        }}
      />

      <UserDialog />
    </div>
  );
}
