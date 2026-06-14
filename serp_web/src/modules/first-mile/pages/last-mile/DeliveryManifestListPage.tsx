/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Delivery Manifest List Page
 */

'use client';

import React, { useMemo, useState } from 'react';
import {
  Truck,
  Plus,
  Calendar,
  Package,
  CheckCircle2,
  XCircle,
  Clock,
  Banknote,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import { TmsCombobox } from '../../components/TmsCombobox';
import { useGetPostOfficesQuery } from '../../api/firstMileApi';
import { useGetDeliveryManifestsQuery } from '../../api/lastMileApi';
import type {
  DeliveryManifestResponse,
  DeliveryManifestStatus,
} from '../../types';
import { DeliveryManifestFormDialog } from './components/DeliveryManifestFormDialog';
import { DeliveryManifestDetailDialog } from './components/DeliveryManifestDetailDialog';

const STATUS_CONFIG: Record<
  DeliveryManifestStatus,
  { label: string; color: string; icon: React.ElementType }
> = {
  CREATED: {
    label: 'Created',
    color: 'bg-slate-100 text-slate-700',
    icon: Clock,
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: 'bg-blue-100 text-blue-700',
    icon: Truck,
  },
  COMPLETED: {
    label: 'Completed',
    color: 'bg-green-100 text-green-700',
    icon: CheckCircle2,
  },
  CANCELLED: {
    label: 'Cancelled',
    color: 'bg-red-100 text-red-700',
    icon: XCircle,
  },
};

type DeliveryManifestAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_SCOPED'
  | 'COURIER_SELF'
  | 'NO_ACCESS';

const resolveDeliveryManifestAccessScope = (
  roles: string[]
): DeliveryManifestAccessScope => {
  if (roles.includes('TMS_ADMIN')) return 'ADMIN_ALL';
  if (roles.includes('TMS_POSTOFFICER_MANAGER')) return 'MANAGER_SCOPED';
  if (roles.includes('TMS_POSTOFFICER')) return 'COURIER_SELF';
  return 'NO_ACCESS';
};

const getScopeLabel = (scope: DeliveryManifestAccessScope): string => {
  if (scope === 'ADMIN_ALL') return 'TMS admin';
  if (scope === 'MANAGER_SCOPED') return 'Post office manager';
  if (scope === 'COURIER_SELF') return 'Courier';
  return 'No access';
};

const getScopeDescription = (scope: DeliveryManifestAccessScope): string => {
  if (scope === 'COURIER_SELF') {
    return 'View assigned delivery routes and complete customer delivery check-ins.';
  }

  if (scope === 'ADMIN_ALL' || scope === 'MANAGER_SCOPED') {
    return 'Create and monitor delivery manifests for last-mile courier routes.';
  }

  return 'Your account does not have permission to access delivery manifests.';
};

export const DeliveryManifestListPage: React.FC = () => {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const accessScope = useMemo(
    () => resolveDeliveryManifestAccessScope(roles),
    [roles]
  );
  const canAccess = accessScope !== 'NO_ACCESS';
  const isCourierScope = accessScope === 'COURIER_SELF';
  const canManageManifests =
    accessScope === 'ADMIN_ALL' || accessScope === 'MANAGER_SCOPED';

  const [postOfficeCode, setPostOfficeCode] = useState('');
  const [statusFilter, setStatusFilter] = useState<DeliveryManifestStatus | ''>(
    ''
  );
  const [dateFilter, setDateFilter] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [selectedManifest, setSelectedManifest] =
    useState<DeliveryManifestResponse | null>(null);

  const { data: postOfficesData, isLoading: isLoadingPostOffices } =
    useGetPostOfficesQuery(
      { page: 0, size: 200, status: 'ACTIVE' },
      { skip: !canManageManifests }
    );

  const postOfficeOptions = useMemo(
    () =>
      (postOfficesData?.items ?? []).map((postOffice) => ({
        value: postOffice.code,
        label: `${postOffice.code} - ${postOffice.name}`,
      })),
    [postOfficesData]
  );

  const selectedPostOffice = useMemo(
    () =>
      (postOfficesData?.items ?? []).find(
        (postOffice) => postOffice.code === postOfficeCode
      ),
    [postOfficeCode, postOfficesData]
  );

  const {
    data: manifests,
    isLoading,
    error: manifestsError,
    refetch,
  } = useGetDeliveryManifestsQuery(
    {
      ...(postOfficeCode ? { postOfficeCode } : {}),
      ...(statusFilter ? { status: statusFilter } : {}),
      ...(dateFilter ? { date: dateFilter } : {}),
    },
    { skip: !canAccess || (!isCourierScope && !postOfficeCode) }
  );

  React.useEffect(() => {
    if (!manifestsError) return;

    notification.error('Failed to load delivery manifests.', {
      description: getErrorMessage(manifestsError),
    });
  }, [manifestsError, notification]);

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Delivery Manifests
          </h1>
          <p className='text-muted-foreground mt-1'>
            Manage delivery manifests for last-mile courier routes.
          </p>
        </div>
        {canManageManifests ? (
          <Button onClick={() => setShowForm(true)} disabled={!postOfficeCode}>
            <Plus className='h-4 w-4 mr-2' />
            New Manifest
          </Button>
        ) : null}
      </div>

      <Card className='p-4'>
        <div className='flex items-center justify-between gap-3'>
          <div>
            <div className='text-sm font-medium'>Access Scope</div>
            <p className='text-sm text-muted-foreground'>
              {getScopeDescription(accessScope)}
            </p>
          </div>
          <Badge variant={canAccess ? 'default' : 'secondary'}>
            {getScopeLabel(accessScope)}
          </Badge>
        </div>
      </Card>

      {!canAccess ? (
        <Card className='p-12 text-center text-muted-foreground'>
          <Truck className='h-12 w-12 mx-auto mb-3 opacity-50' />
          <p className='font-medium'>Access denied</p>
          <p className='text-sm mt-1'>
            Your account does not have permission to access delivery manifests.
          </p>
        </Card>
      ) : (
        <>
          {/* Filters */}
          <Card className='p-4'>
            <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
              {isCourierScope ? (
                <div className='rounded-md border bg-muted/30 p-3 text-sm text-muted-foreground md:col-span-2'>
                  You are viewing delivery routes assigned to your courier
                  profile.
                </div>
              ) : (
                <div className='space-y-1'>
                  <label className='text-xs font-medium text-muted-foreground'>
                    Post Office
                  </label>
                  <TmsCombobox
                    id='delivery-manifest-post-office'
                    value={postOfficeCode}
                    onValueChange={setPostOfficeCode}
                    options={postOfficeOptions}
                    placeholder='Select post office'
                    emptyText='No post offices found'
                    disabled={isLoadingPostOffices}
                    loading={isLoadingPostOffices}
                  />
                </div>
              )}
              <div className='space-y-1'>
                <label className='text-xs font-medium text-muted-foreground'>
                  Status
                </label>
                <select
                  className='w-full h-9 rounded-md border border-input bg-transparent px-3 py-1 text-sm'
                  value={statusFilter}
                  onChange={(e) =>
                    setStatusFilter(
                      e.target.value as DeliveryManifestStatus | ''
                    )
                  }
                >
                  <option value=''>All Statuses</option>
                  <option value='CREATED'>Created</option>
                  <option value='IN_PROGRESS'>In Progress</option>
                  <option value='COMPLETED'>Completed</option>
                </select>
              </div>
              <div className='space-y-1'>
                <label className='text-xs font-medium text-muted-foreground'>
                  Planned Date
                </label>
                <Input
                  type='date'
                  value={dateFilter}
                  onChange={(e) => setDateFilter(e.target.value)}
                />
              </div>
              <div className='flex items-end'>
                <Button
                  variant='outline'
                  onClick={() => refetch()}
                  className='w-full'
                  disabled={!isCourierScope && !postOfficeCode}
                >
                  Search
                </Button>
              </div>
            </div>
          </Card>

          {/* Results */}
          {isLoading ? (
            <div className='text-center py-12 text-muted-foreground'>
              Loading manifests...
            </div>
          ) : !isCourierScope && !postOfficeCode ? (
            <Card className='p-12 text-center text-muted-foreground'>
              <Truck className='h-12 w-12 mx-auto mb-3 opacity-50' />
              <p className='font-medium'>Select a post office to begin</p>
              <p className='text-sm mt-1'>
                You will see all delivery manifests created for that office.
              </p>
            </Card>
          ) : !manifests?.length ? (
            <Card className='p-12 text-center text-muted-foreground'>
              <Package className='h-12 w-12 mx-auto mb-3 opacity-50' />
              <p className='font-medium'>No manifests found</p>
              <p className='text-sm mt-1'>
                {isCourierScope
                  ? 'No assigned delivery routes match the current filters.'
                  : 'Create a new manifest to assign orders to a courier.'}
              </p>
            </Card>
          ) : (
            <div className='grid gap-4'>
              {manifests.map((manifest) => (
                <ManifestCard
                  key={manifest.id}
                  manifest={manifest}
                  onClick={() => setSelectedManifest(manifest)}
                />
              ))}
            </div>
          )}

          {/* Form Dialog */}
          {showForm && (
            <DeliveryManifestFormDialog
              postOfficeCode={postOfficeCode}
              postOfficeId={selectedPostOffice?.id}
              onClose={() => setShowForm(false)}
              onCreated={() => {
                setShowForm(false);
                refetch();
              }}
            />
          )}

          {/* Detail Dialog */}
          {selectedManifest && (
            <DeliveryManifestDetailDialog
              manifest={selectedManifest}
              canOperate={isCourierScope}
              onClose={() => setSelectedManifest(null)}
              onUpdated={() => {
                setSelectedManifest(null);
                refetch();
              }}
            />
          )}
        </>
      )}
    </div>
  );
};

// ─── Sub-component ────────────────────────────────────────────────────────

interface ManifestCardProps {
  manifest: DeliveryManifestResponse;
  onClick: () => void;
}

const ManifestCard: React.FC<ManifestCardProps> = ({ manifest, onClick }) => {
  const config = STATUS_CONFIG[manifest.status];
  const StatusIcon = config.icon;
  const progressPercent =
    manifest.totalOrders > 0
      ? Math.round(
          ((manifest.deliveredCount + manifest.failedCount) /
            manifest.totalOrders) *
            100
        )
      : 0;

  return (
    <Card
      className='p-4 cursor-pointer hover:shadow-md transition-shadow'
      onClick={onClick}
    >
      <div className='flex items-start justify-between'>
        <div className='space-y-2 min-w-0 flex-1'>
          <div className='flex items-center gap-3'>
            <span className='font-mono font-semibold text-sm'>
              {manifest.manifestCode}
            </span>
            <Badge className={`${config.color} text-xs`}>
              <StatusIcon className='h-3 w-3 mr-1' />
              {config.label}
            </Badge>
          </div>

          <div className='flex flex-wrap items-center gap-4 text-sm text-muted-foreground'>
            <span className='flex items-center gap-1'>
              <Calendar className='h-3.5 w-3.5' />
              {manifest.plannedDate}
            </span>
            <span className='flex items-center gap-1'>
              <Package className='h-3.5 w-3.5' />
              {manifest.deliveredCount}/{manifest.totalOrders} delivered
            </span>
            {manifest.courierName && (
              <span className='flex items-center gap-1'>
                <Truck className='h-3.5 w-3.5' />
                {manifest.courierName}
              </span>
            )}
          </div>

          {/* Progress bar */}
          <div className='flex items-center gap-2'>
            <div className='flex-1 h-2 bg-muted rounded-full overflow-hidden'>
              <div
                className='h-full bg-green-500 rounded-full transition-all'
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            <span className='text-xs text-muted-foreground'>
              {progressPercent}%
            </span>
          </div>
        </div>

        <div className='text-right flex-shrink-0 ml-4'>
          <div className='flex items-center gap-1 text-sm'>
            <Banknote className='h-4 w-4 text-green-600' />
            <span className='font-medium'>
              {manifest.collectedCodAmount.toLocaleString()}đ
            </span>
            <span className='text-muted-foreground'>
              / {manifest.totalCodAmount.toLocaleString()}đ
            </span>
          </div>
          {manifest.failedCount > 0 && (
            <span className='text-xs text-red-500 mt-1 block'>
              {manifest.failedCount} failed
            </span>
          )}
        </div>
      </div>
    </Card>
  );
};
