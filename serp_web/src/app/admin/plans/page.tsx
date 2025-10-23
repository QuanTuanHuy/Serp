/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription plans management page
 */

'use client';

import React, { useState, useMemo } from 'react';
import {
  useGetSubscriptionPlansQuery,
  useUpdateSubscriptionPlanMutation,
  useCreateSubscriptionPlanMutation,
  useDeleteSubscriptionPlanMutation,
  AdminStatusBadge,
  AdminActionMenu,
  AdminStatsCard,
} from '@/modules/admin';
import type { SubscriptionPlan } from '@/modules/admin';
import { PlanFormDialog } from '@/modules/admin/components/plans';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import {
  Package,
  Plus,
  Eye,
  Edit,
  Trash2,
  Users,
  CheckCircle,
  XCircle,
  LayoutGrid,
  List,
} from 'lucide-react';

export default function PlansPage() {
  const { data: plans, isLoading, error } = useGetSubscriptionPlansQuery();
  const [updatePlan] = useUpdateSubscriptionPlanMutation();
  const [createPlan, { isLoading: isCreating }] =
    useCreateSubscriptionPlanMutation();
  const [deletePlan] = useDeleteSubscriptionPlanMutation();
  const [viewMode, setViewMode] = useState<'grid' | 'table'>('grid');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState<
    SubscriptionPlan | undefined
  >();

  const handleToggleActive = async (planId: string, isActive: boolean) => {
    try {
      await updatePlan({
        id: planId,
        data: { isActive: !isActive },
      }).unwrap();
    } catch (error) {
      console.error('Failed to update plan:', error);
    }
  };

  const handleCreatePlan = () => {
    setSelectedPlan(undefined);
    setIsDialogOpen(true);
  };

  const handleEditPlan = (plan: SubscriptionPlan) => {
    setSelectedPlan(plan);
    setIsDialogOpen(true);
  };

  const handleSubmitPlan = async (data: any) => {
    try {
      if (selectedPlan) {
        await updatePlan({
          id: String(selectedPlan.id),
          data,
        }).unwrap();
      } else {
        await createPlan(data).unwrap();
      }
      setIsDialogOpen(false);
      setSelectedPlan(undefined);
    } catch (error) {
      console.error('Failed to save plan:', error);
      throw error;
    }
  };

  const handleDeletePlan = async (planId: string) => {
    if (!confirm('Are you sure you want to delete this plan?')) {
      return;
    }

    try {
      await deletePlan(planId).unwrap();
    } catch (error) {
      console.error('Failed to delete plan:', error);
    }
  };

  const formatPrice = (price?: number) => {
    if (!price) return '$0';
    return `$${price.toFixed(2)}`;
  };

  // Calculate stats
  const stats = {
    total: plans?.length || 0,
    active: plans?.filter((p) => p.isActive).length || 0,
    custom: plans?.filter((p) => p.isCustom).length || 0,
  };

  // Define columns for DataTable
  const columns = useMemo<ColumnDef<SubscriptionPlan>[]>(
    () => [
      {
        id: 'plan',
        header: 'Plan',
        accessor: 'planName',
        defaultVisible: true,
        cell: ({ row }) => (
          <div>
            <p className='font-medium'>{row.planName}</p>
            <p className='text-xs text-muted-foreground'>{row.planCode}</p>
            {row.description && (
              <p className='text-xs text-muted-foreground mt-1 max-w-md'>
                {row.description}
              </p>
            )}
          </div>
        ),
      },
      {
        id: 'monthlyPrice',
        header: 'Monthly Price',
        accessor: 'monthlyPrice',
        defaultVisible: true,
        align: 'right',
        cell: ({ value }) => (
          <span className='font-semibold text-primary'>
            {formatPrice(value)}
          </span>
        ),
      },
      {
        id: 'yearlyPrice',
        header: 'Yearly Price',
        accessor: 'yearlyPrice',
        defaultVisible: true,
        align: 'right',
        cell: ({ value }) => (
          <span className='font-semibold text-primary'>
            {formatPrice(value)}
          </span>
        ),
      },
      {
        id: 'maxUsers',
        header: 'Max Users',
        accessor: 'maxUsers',
        defaultVisible: true,
        align: 'center',
        cell: ({ value }) => (
          <span className='text-sm'>{value || 'Unlimited'}</span>
        ),
      },
      {
        id: 'trialDays',
        header: 'Trial Days',
        accessor: 'trialDays',
        defaultVisible: true,
        align: 'center',
        cell: ({ value }) => <span className='text-sm'>{value || 0}</span>,
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'isActive',
        defaultVisible: true,
        cell: ({ value }) => (
          <AdminStatusBadge status={value ? 'ACTIVE' : 'INACTIVE'} />
        ),
      },
      {
        id: 'type',
        header: 'Type',
        accessor: 'isCustom',
        defaultVisible: false,
        cell: ({ value }) => (
          <span className='text-sm'>
            {value ? (
              <span className='flex items-center gap-1 text-amber-600'>
                <Users className='h-3 w-3' />
                Custom
              </span>
            ) : (
              'Standard'
            )}
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
                onClick: () => handleEditPlan(row),
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: row.isActive ? 'Deactivate' : 'Activate',
                onClick: () => handleToggleActive(String(row.id), row.isActive),
                icon: row.isActive ? (
                  <XCircle className='h-4 w-4' />
                ) : (
                  <CheckCircle className='h-4 w-4' />
                ),
                separator: true,
              },
              {
                label: 'Delete',
                onClick: () => handleDeletePlan(String(row.id)),
                icon: <Trash2 className='h-4 w-4' />,
                variant: 'destructive',
              },
            ]}
          />
        ),
      },
    ],
    [handleToggleActive, handleEditPlan, handleDeletePlan]
  );

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>
            Subscription Plans
          </h1>
          <p className='text-muted-foreground mt-2'>
            Configure and manage subscription plans for organizations
          </p>
        </div>

        <div className='flex items-center gap-2'>
          {/* View Mode Toggle */}
          <div className='flex items-center border rounded-lg'>
            <Button
              variant={viewMode === 'grid' ? 'default' : 'ghost'}
              size='sm'
              onClick={() => setViewMode('grid')}
              className='rounded-r-none'
            >
              <LayoutGrid className='h-4 w-4' />
            </Button>
            <Button
              variant={viewMode === 'table' ? 'default' : 'ghost'}
              size='sm'
              onClick={() => setViewMode('table')}
              className='rounded-l-none'
            >
              <List className='h-4 w-4' />
            </Button>
          </div>

          <Button size='sm' onClick={handleCreatePlan}>
            <Plus className='h-4 w-4 mr-2' />
            Create Plan
          </Button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-3'>
        <AdminStatsCard
          title='Total Plans'
          value={stats.total}
          icon={<Package className='h-4 w-4' />}
        />

        <AdminStatsCard
          title='Active Plans'
          value={stats.active}
          description={`${stats.total - stats.active} inactive`}
          icon={<CheckCircle className='h-4 w-4' />}
        />

        <AdminStatsCard
          title='Custom Plans'
          value={stats.custom}
          description={`${stats.total - stats.custom} standard`}
          icon={<Users className='h-4 w-4' />}
        />
      </div>

      {/* Plans Grid View */}
      {viewMode === 'grid' && (
        <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
          {/* Loading State */}
          {isLoading && (
            <div className='col-span-full'>
              <Card>
                <CardContent className='flex items-center justify-center h-64'>
                  <div className='text-muted-foreground'>Loading plans...</div>
                </CardContent>
              </Card>
            </div>
          )}

          {/* Error State */}
          {error && (
            <div className='col-span-full'>
              <Card>
                <CardContent className='flex items-center justify-center h-64'>
                  <div className='text-destructive'>Failed to load plans</div>
                </CardContent>
              </Card>
            </div>
          )}

          {/* Plans Cards */}
          {!isLoading &&
            !error &&
            plans?.map((plan) => (
              <Card key={plan.id} className='relative'>
                {/* Active/Inactive Badge */}
                <div className='absolute top-4 right-4'>
                  <AdminStatusBadge
                    status={plan.isActive ? 'ACTIVE' : 'INACTIVE'}
                  />
                </div>

                <CardHeader>
                  <div className='flex items-start justify-between'>
                    <div>
                      <CardTitle className='text-xl'>{plan.planName}</CardTitle>
                      <p className='text-xs text-muted-foreground mt-1'>
                        {plan.planCode}
                      </p>
                    </div>
                  </div>

                  {plan.description && (
                    <p className='text-sm text-muted-foreground mt-2'>
                      {plan.description}
                    </p>
                  )}
                </CardHeader>

                <CardContent className='space-y-4'>
                  {/* Pricing */}
                  <div className='grid grid-cols-2 gap-4'>
                    <div className='space-y-1'>
                      <p className='text-xs text-muted-foreground'>Monthly</p>
                      <p className='text-2xl font-bold text-primary'>
                        {formatPrice(plan.monthlyPrice)}
                      </p>
                    </div>

                    <div className='space-y-1'>
                      <p className='text-xs text-muted-foreground'>Yearly</p>
                      <p className='text-2xl font-bold text-primary'>
                        {formatPrice(plan.yearlyPrice)}
                      </p>
                    </div>
                  </div>

                  {/* Features */}
                  <div className='space-y-2 pt-2 border-t'>
                    <div className='flex items-center justify-between text-sm'>
                      <span className='text-muted-foreground'>Max Users</span>
                      <span className='font-medium'>
                        {plan.maxUsers || 'Unlimited'}
                      </span>
                    </div>

                    <div className='flex items-center justify-between text-sm'>
                      <span className='text-muted-foreground'>Trial Days</span>
                      <span className='font-medium'>{plan.trialDays || 0}</span>
                    </div>

                    {plan.isCustom && (
                      <div className='flex items-center gap-1 text-xs text-amber-600'>
                        <Users className='h-3 w-3' />
                        Custom Plan
                      </div>
                    )}
                  </div>

                  {/* Actions */}
                  <div className='flex gap-2 pt-2 border-t'>
                    <Button
                      variant='outline'
                      size='sm'
                      className='flex-1'
                      onClick={() => handleEditPlan(plan)}
                    >
                      <Edit className='h-4 w-4 mr-2' />
                      Edit
                    </Button>

                    <AdminActionMenu
                      items={[
                        {
                          label: plan.isActive ? 'Deactivate' : 'Activate',
                          onClick: () =>
                            handleToggleActive(String(plan.id), plan.isActive),
                          icon: plan.isActive ? (
                            <XCircle className='h-4 w-4' />
                          ) : (
                            <CheckCircle className='h-4 w-4' />
                          ),
                        },
                        {
                          label: 'Delete',
                          onClick: () => handleDeletePlan(String(plan.id)),
                          icon: <Trash2 className='h-4 w-4' />,
                          variant: 'destructive',
                          separator: true,
                        },
                      ]}
                    />
                  </div>
                </CardContent>
              </Card>
            ))}
        </div>
      )}

      {/* Plans Table View */}
      {viewMode === 'table' && (
        <DataTable
          columns={columns}
          data={plans || []}
          keyExtractor={(plan) => String(plan.id)}
          isLoading={isLoading}
          error={error}
          storageKey='admin-plans-columns'
          loadingState={
            <div className='flex items-center justify-center h-64'>
              <div className='text-muted-foreground'>Loading plans...</div>
            </div>
          }
          errorState={
            <div className='flex items-center justify-center h-64'>
              <div className='text-destructive'>Failed to load plans</div>
            </div>
          }
          emptyState={
            <div className='flex flex-col items-center justify-center text-center'>
              <Package className='h-12 w-12 text-muted-foreground mb-4' />
              <h3 className='text-lg font-medium'>No subscription plans yet</h3>
              <p className='text-sm text-muted-foreground mt-1'>
                Create your first subscription plan to get started
              </p>
              <Button size='sm' className='mt-4' onClick={handleCreatePlan}>
                <Plus className='h-4 w-4 mr-2' />
                Create Plan
              </Button>
            </div>
          }
        />
      )}

      {/* Empty State for Grid View */}
      {viewMode === 'grid' && !isLoading && !error && plans?.length === 0 && (
        <Card>
          <CardContent className='flex flex-col items-center justify-center h-64 text-center'>
            <Package className='h-12 w-12 text-muted-foreground mb-4' />
            <h3 className='text-lg font-medium'>No subscription plans yet</h3>
            <p className='text-sm text-muted-foreground mt-1'>
              Create your first subscription plan to get started
            </p>
            <Button size='sm' className='mt-4' onClick={handleCreatePlan}>
              <Plus className='h-4 w-4 mr-2' />
              Create Plan
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Plan Form Dialog */}
      <PlanFormDialog
        open={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        plan={selectedPlan}
        onSubmit={handleSubmitPlan}
        isLoading={isCreating}
      />
    </div>
  );
}
