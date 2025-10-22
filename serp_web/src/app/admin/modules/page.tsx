/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modules management page
 */

'use client';

import React from 'react';
import {
  useGetModulesQuery,
  useUpdateModuleMutation,
  AdminStatusBadge,
  AdminActionMenu,
  AdminStatsCard,
} from '@/modules/admin';
import { Card, CardContent, Button } from '@/shared/components';
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

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Modules</h1>
          <p className='text-muted-foreground mt-2'>
            Manage system modules and features
          </p>
        </div>

        <Button size='sm'>
          <Plus className='h-4 w-4 mr-2' />
          Create Module
        </Button>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-3'>
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
      <Card>
        <CardContent className='p-0'>
          {/* Loading State */}
          {isLoading && (
            <div className='flex items-center justify-center h-64'>
              <div className='text-muted-foreground'>Loading modules...</div>
            </div>
          )}

          {/* Error State */}
          {error && (
            <div className='flex items-center justify-center h-64'>
              <div className='text-destructive'>Failed to load modules</div>
            </div>
          )}

          {/* Table */}
          {!isLoading && !error && modules && (
            <div className='overflow-x-auto'>
              <table className='w-full'>
                <thead className='border-b bg-muted/50'>
                  <tr>
                    <th className='px-4 py-3 text-left text-sm font-medium'>
                      Module
                    </th>
                    <th className='px-4 py-3 text-left text-sm font-medium'>
                      Code
                    </th>
                    <th className='px-4 py-3 text-left text-sm font-medium'>
                      Status
                    </th>
                    <th className='px-4 py-3 text-left text-sm font-medium'>
                      Icon
                    </th>
                    <th className='px-4 py-3 text-left text-sm font-medium'>
                      Order
                    </th>
                    <th className='px-4 py-3 text-right text-sm font-medium'>
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className='divide-y'>
                  {modules.map((module) => (
                    <tr
                      key={module.id}
                      className='hover:bg-muted/50 transition-colors'
                    >
                      <td className='px-4 py-3'>
                        <div className='flex items-center gap-3'>
                          <div className='h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center'>
                            <Puzzle className='h-5 w-5 text-primary' />
                          </div>
                          <div>
                            <p className='font-medium'>{module.moduleName}</p>
                            {module.description && (
                              <p className='text-xs text-muted-foreground max-w-md truncate'>
                                {module.description}
                              </p>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className='px-4 py-3'>
                        <code className='text-xs bg-muted px-2 py-1 rounded'>
                          {module.code}
                        </code>
                      </td>
                      <td className='px-4 py-3'>
                        <AdminStatusBadge status={module.status} />
                      </td>
                      <td className='px-4 py-3'>
                        <span className='text-sm'>{module.icon || 'N/A'}</span>
                      </td>
                      <td className='px-4 py-3'>
                        <span className='text-sm'>{module.displayOrder}</span>
                      </td>
                      <td className='px-4 py-3 text-right'>
                        <AdminActionMenu
                          items={[
                            {
                              label: 'View Details',
                              onClick: () => console.log('View', module.id),
                              icon: <Eye className='h-4 w-4' />,
                            },
                            {
                              label: 'Edit',
                              onClick: () => console.log('Edit', module.id),
                              icon: <Edit className='h-4 w-4' />,
                            },
                            {
                              label:
                                module.status === 'ACTIVE'
                                  ? 'Disable'
                                  : 'Enable',
                              onClick: () =>
                                handleToggleStatus(
                                  String(module.id),
                                  module.status
                                ),
                              icon:
                                module.status === 'ACTIVE' ? (
                                  <PowerOff className='h-4 w-4' />
                                ) : (
                                  <Power className='h-4 w-4' />
                                ),
                              separator: true,
                              variant:
                                module.status === 'ACTIVE'
                                  ? 'destructive'
                                  : 'default',
                            },
                          ]}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Empty State */}
          {!isLoading && !error && modules?.length === 0 && (
            <div className='flex flex-col items-center justify-center h-64 text-center'>
              <Puzzle className='h-12 w-12 text-muted-foreground mb-4' />
              <h3 className='text-lg font-medium'>No modules yet</h3>
              <p className='text-sm text-muted-foreground mt-1'>
                Create your first module to get started
              </p>
              <Button size='sm' className='mt-4'>
                <Plus className='h-4 w-4 mr-2' />
                Create Module
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
