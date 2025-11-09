/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscriptions management page
 */

'use client';

import React, { useMemo, useState } from 'react';
import {
  useSubscriptions,
  AdminActionMenu,
  AdminStatusBadge,
} from '@/modules/admin';
import { SubscriptionDetailsDialog } from '@/modules/admin/components/subscriptions';
import { Combobox } from '@/shared/components/ui/combobox';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { DataTable } from '@/shared/components';
import type { ColumnDef } from '@/shared/types';
import {
  CheckCircle,
  Ban,
  Building2,
  TimerOff,
  Eye,
  DollarSign,
  Users,
} from 'lucide-react';
import type { Organization } from '@/modules/admin/types';
import { useGetOrganizationsQuery } from '@/modules/admin/services/organizations/organizationsApi';

export default function SubscriptionsPage() {
  const {
    filters,
    subscriptions,
    plans,
    pagination,
    isLoading,
    isFetching,
    error,
    isRejecting,
    handleFilterChange,
    handlePageChange,
    handleActivate,
    handleReject,
    handleExpire,
    detailsOpen,
    selectedSubscription,
    selectedPlan,
    openDetailsDialog,
    setDetailsOpen,
    formatDate,
    formatPrice,
  } = useSubscriptions();

  // Reject dialog state
  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectId, setRejectId] = useState<number | null>(null);

  // Fetch organizations for filters
  const [orgSearch, setOrgSearch] = useState<string>('');
  const { data: orgsResponse, isFetching: isFetchingOrgs } =
    useGetOrganizationsQuery({
      page: 0,
      pageSize: 5,
      sortBy: 'name',
      sortDir: 'ASC',
      search: orgSearch || undefined,
    } as any);
  const organizations: Organization[] = useMemo(
    () => orgsResponse?.data.items || [],
    [orgsResponse]
  );

  // Client-side filter for plan
  const subscriptionsToShow = useMemo(() => {
    if (!filters.planId) return subscriptions;
    return subscriptions.filter(
      (s) => String(s.subscriptionPlanId) === String(filters.planId)
    );
  }, [subscriptions, filters.planId]);

  const columns = useMemo<ColumnDef<any>[]>(
    () => [
      {
        id: 'organization',
        header: 'Organization',
        accessor: 'organizationName',
        defaultVisible: true,
        cell: ({ row }) => (
          <div className='flex items-center gap-3'>
            <div className='h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center'>
              <Building2 className='h-5 w-5 text-primary' />
            </div>
            <div>
              <p className='font-medium'>{row.organizationName || 'N/A'}</p>
              <p className='text-xs text-muted-foreground'>
                ID: {row.organizationId}
              </p>
            </div>
          </div>
        ),
      },
      {
        id: 'plan',
        header: 'Plan',
        accessor: 'planName',
        defaultVisible: true,
        cell: ({ row }) => {
          const plan = plans.find((p) => p.id === row.subscriptionPlanId);
          return (
            <div>
              <p className='font-medium'>{row.planName || 'N/A'}</p>
              {plan && (
                <div className='flex items-center gap-2 mt-1'>
                  <span className='text-xs text-muted-foreground flex items-center gap-1'>
                    <DollarSign className='h-3 w-3' />
                    {row.billingCycle === 'MONTHLY'
                      ? formatPrice(plan.monthlyPrice)
                      : formatPrice(plan.yearlyPrice)}
                  </span>
                  {plan.maxUsers && (
                    <span className='text-xs text-muted-foreground flex items-center gap-1'>
                      <Users className='h-3 w-3' />
                      {plan.maxUsers} users
                    </span>
                  )}
                </div>
              )}
            </div>
          );
        },
      },
      {
        id: 'status',
        header: 'Status',
        accessor: 'status',
        defaultVisible: true,
        cell: ({ value }) => <AdminStatusBadge status={value} />,
      },
      {
        id: 'billing',
        header: 'Billing',
        accessor: 'billingCycle',
        defaultVisible: true,
        cell: ({ value }) => <span className='text-sm'>{value || 'N/A'}</span>,
      },
      {
        id: 'start',
        header: 'Start Date',
        accessor: 'startDate',
        defaultVisible: true,
        cell: ({ value }) => (
          <span className='text-sm text-muted-foreground'>
            {formatDate(value)}
          </span>
        ),
      },
      {
        id: 'end',
        header: 'End Date',
        accessor: 'endDate',
        defaultVisible: true,
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
        cell: ({ row }) => {
          const items = [
            {
              label: 'View Details',
              onClick: () => openDetailsDialog(row),
              icon: <Eye className='h-4 w-4' />,
            },
          ] as any[];

          if (row.status !== 'ACTIVE') {
            items.push({
              label: 'Activate',
              onClick: () => handleActivate(row.id),
              icon: <CheckCircle className='h-4 w-4' />,
              separator: true,
            });
          }
          if (row.status === 'PENDING') {
            items.push({
              label: 'Reject',
              onClick: () => {
                setRejectId(row.id);
                setRejectReason('');
                setRejectOpen(true);
              },
              icon: <Ban className='h-4 w-4' />,
              variant: 'destructive',
            });
          }
          if (row.status === 'PENDING_UPGRADE') {
            items.push({
              label: 'Reject Upgrade',
              onClick: () => {
                setRejectId(row.id);
                setRejectReason('');
                setRejectOpen(true);
              },
              icon: <Ban className='h-4 w-4' />,
              variant: 'destructive',
            });
          }
          if (row.status === 'ACTIVE' || row.status === 'TRIAL') {
            items.push({
              label: 'Expire',
              onClick: () => handleExpire(row.id),
              icon: <TimerOff className='h-4 w-4' />,
              variant: 'destructive',
            });
          }
          return <AdminActionMenu items={items} />;
        },
      },
    ],
    [handleActivate, handleReject, plans, openDetailsDialog]
  );

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Subscriptions</h1>
          <p className='text-muted-foreground mt-2'>
            View and manage organization subscriptions
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm'>
            Export
          </Button>
        </div>
      </div>

      {/* Filters Card */}
      <Card>
        <CardHeader>
          <CardTitle className='text-base font-medium'>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid gap-4 md:grid-cols-5'>
            {/* Status */}
            <div>
              <select
                value={filters.status || ''}
                onChange={(e) =>
                  handleFilterChange('status', e.target.value || undefined)
                }
                className='w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>All Statuses</option>
                <option value='PENDING'>Pending</option>
                <option value='PENDING_UPGRADE'>Pending Upgrade</option>
                <option value='ACTIVE'>Active</option>
                <option value='TRIAL'>Trial</option>
                <option value='EXPIRED'>Expired</option>
                <option value='CANCELLED'>Cancelled</option>
                <option value='PAYMENT_FAILED'>Payment Failed</option>
                <option value='GRACE_PERIOD'>Grace Period</option>
              </select>
            </div>

            {/* Billing Cycle */}
            <div>
              <select
                value={filters.billingCycle || ''}
                onChange={(e) =>
                  handleFilterChange(
                    'billingCycle',
                    e.target.value || undefined
                  )
                }
                className='w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm'
              >
                <option value=''>All Billing</option>
                <option value='MONTHLY'>Monthly</option>
                <option value='YEARLY'>Yearly</option>
                <option value='TRIAL'>Trial</option>
              </select>
            </div>

            {/* Organization */}
            <div>
              <Combobox
                value={filters.organizationId}
                onChange={(val) =>
                  handleFilterChange(
                    'organizationId',
                    val !== undefined ? Number(val) : undefined
                  )
                }
                items={organizations.map((o) => ({
                  value: o.id,
                  label: o.name,
                }))}
                placeholder='All Organizations'
                loading={isFetchingOrgs}
                onSearch={(q) => setOrgSearch(q)}
              />
            </div>

            {/* Plan */}
            <div>
              <Combobox
                value={filters.planId}
                onChange={(val) =>
                  handleFilterChange(
                    'planId',
                    val !== undefined ? Number(val) : undefined
                  )
                }
                items={plans.map((p) => ({ value: p.id, label: p.planName }))}
                placeholder='All Plans'
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Table */}
      <DataTable
        columns={columns}
        data={subscriptionsToShow}
        keyExtractor={(s) => String(s.id)}
        isLoading={isLoading}
        error={error as any}
        storageKey='admin-subscriptions-columns'
        pagination={{
          currentPage: pagination.currentPage,
          totalPages: pagination.totalPages,
          totalItems: pagination.totalItems,
          onPageChange: handlePageChange,
          isFetching: isFetching,
        }}
        loadingState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-muted-foreground'>
              Loading subscriptions...
            </div>
          </div>
        }
        errorState={
          <div className='flex items-center justify-center h-64'>
            <div className='text-destructive'>Failed to load subscriptions</div>
          </div>
        }
      />

      {/* Reject Subscription Dialog */}
      <Dialog open={rejectOpen} onOpenChange={setRejectOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reject subscription</DialogTitle>
            <DialogDescription>
              Please provide a reason for rejecting this subscription. This
              action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Reason</label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              rows={4}
              placeholder='Enter rejection reason...'
              className='w-full rounded-md border border-input bg-background px-3 py-2 text-sm'
            />
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setRejectOpen(false)}
              disabled={isRejecting}
            >
              Cancel
            </Button>
            <Button
              variant='destructive'
              disabled={!rejectReason.trim() || !rejectId || isRejecting}
              onClick={async () => {
                if (!rejectId) return;
                const ok = await handleReject(rejectId, rejectReason.trim());
                if (ok) {
                  setRejectOpen(false);
                  setRejectReason('');
                  setRejectId(null);
                }
              }}
            >
              {isRejecting ? 'Rejecting...' : 'Reject'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Subscription Details Dialog */}
      <SubscriptionDetailsDialog
        open={detailsOpen}
        onOpenChange={setDetailsOpen}
        subscription={selectedSubscription}
        plan={selectedPlan}
      />
    </div>
  );
}
