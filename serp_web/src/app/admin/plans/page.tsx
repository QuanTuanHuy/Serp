/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription plans management page
 */

'use client';

import React from 'react';
import {
  useGetSubscriptionPlansQuery,
  useUpdateSubscriptionPlanMutation,
  AdminStatusBadge,
  AdminActionMenu,
  AdminStatsCard,
} from '@/modules/admin';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import {
  Package,
  Plus,
  Eye,
  Edit,
  Trash2,
  Users,
  CheckCircle,
  XCircle,
} from 'lucide-react';

export default function PlansPage() {
  const { data: plans, isLoading, error } = useGetSubscriptionPlansQuery();
  const [updatePlan] = useUpdateSubscriptionPlanMutation();

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

        <Button size='sm'>
          <Plus className='h-4 w-4 mr-2' />
          Create Plan
        </Button>
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

      {/* Plans Grid */}
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
                    {/* {plan.yearlySavings && (
                      <p className='text-xs text-green-600'>
                        Save {formatPrice(plan.yearlySavings)}
                      </p>
                    )} */}
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

                  {/* <div className='flex items-center justify-between text-sm'>
                    <span className='text-muted-foreground'>Modules</span>
                    <span className='font-medium'>{plan.moduleCount || 0}</span>
                  </div> */}

                  {plan.isCustom && (
                    <div className='flex items-center gap-1 text-xs text-amber-600'>
                      <Users className='h-3 w-3' />
                      Custom Plan
                    </div>
                  )}
                </div>

                {/* Actions */}
                <div className='flex gap-2 pt-2 border-t'>
                  <Button variant='outline' size='sm' className='flex-1'>
                    <Eye className='h-4 w-4 mr-2' />
                    View
                  </Button>

                  <Button variant='outline' size='sm' className='flex-1'>
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
                        onClick: () => console.log('Delete', plan.id),
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

      {/* Empty State */}
      {!isLoading && !error && plans?.length === 0 && (
        <Card>
          <CardContent className='flex flex-col items-center justify-center h-64 text-center'>
            <Package className='h-12 w-12 text-muted-foreground mb-4' />
            <h3 className='text-lg font-medium'>No subscription plans yet</h3>
            <p className='text-sm text-muted-foreground mt-1'>
              Create your first subscription plan to get started
            </p>
            <Button size='sm' className='mt-4'>
              <Plus className='h-4 w-4 mr-2' />
              Create Plan
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
