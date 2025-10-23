/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modules management page
 */

'use client';

import React, { useMemo } from 'react';
import {
  useGetModulesQuery,
  useUpdateModuleMutation,
  AdminStatusBadge,
  AdminActionMenu,
  AdminStatsCard,
} from '@/modules/admin';
import type { Module } from '@/modules/admin';
import { Card, Button } from '@/shared/components';
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
  XCircle,
} from 'lucide-react';

export default function ModulesPage() {
  const { data: modules, isLoading, error } = useGetModulesQuery();
  const [updateModule] = useUpdateModuleMutation();

  const handleToggleStatus = async (
    moduleId: string,
    currentStatus: string
  ) => {
    try {
      const newStatus = currentStatus === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
      await updateModule({
        id: moduleId,
        data: { status: newStatus },
      }).unwrap();
    } catch (error) {
      console.error('Failed to update module:', error);
    }
  };

  // Calculate stats
  const stats = {
    total: modules?.length || 0,
    enabled: modules?.filter((m) => m.status === 'ACTIVE').length || 0,
    disabled: modules?.filter((m) => m.status === 'DISABLED').length || 0,
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
                onClick: () => console.log('Edit', row.id),
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
    [handleToggleStatus]
  );

  return (
    <div className='space-y-4 sm:space-y-6 px-4 sm:px-6 lg:px-8'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl sm:text-3xl font-bold tracking-tight'>
            Modules
          </h1>
          <p className='text-muted-foreground mt-1 sm:mt-2'>
            Manage system modules and features
          </p>
        </div>

        <Button size='sm' className='self-start sm:self-auto'>
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
            <Button size='sm' className='mt-4'>
              <Plus className='h-4 w-4 mr-2' />
              Create Module
            </Button>
          </div>
        }
      />
    </div>
  );
}
