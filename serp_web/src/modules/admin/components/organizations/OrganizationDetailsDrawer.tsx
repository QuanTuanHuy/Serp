'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organization details drawer
 */

import type React from 'react';
import {
  Building2,
  Mail,
  MapPin,
  Phone,
  Globe,
  Users,
  ShieldAlert,
} from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  Badge,
  Button,
  Separator,
  Skeleton,
} from '@/shared/components/ui';
import { AdminStatusBadge } from '@/modules/admin/components/shared/AdminStatusBadge';
import {
  useGetOrganizationByIdQuery,
  useGetOrganizationUserStatsQuery,
} from '@/modules/admin/services/organizations/organizationsApi';
import { formatAdminDate } from '@/modules/admin/utils/date';
import { OrganizationUsersPreview } from './OrganizationUsersPreview';
import type { OrganizationStatus } from '@/modules/admin/types';

interface OrganizationDetailsDrawerProps {
  open: boolean;
  organizationId?: number;
  onOpenChange: (open: boolean) => void;
  onCreateUser: (organizationId: number) => void;
  onViewAllUsers: (organizationId: number) => void;
  onToggleStatus: (organizationId: number, status: OrganizationStatus) => void;
}

export function OrganizationDetailsDrawer({
  open,
  organizationId,
  onOpenChange,
  onCreateUser,
  onViewAllUsers,
  onToggleStatus,
}: OrganizationDetailsDrawerProps) {
  const { data: organization, isLoading } = useGetOrganizationByIdQuery(
    String(organizationId ?? ''),
    { skip: !open || !organizationId }
  );
  const { data: statsResponse, isLoading: isLoadingStats } =
    useGetOrganizationUserStatsQuery(organizationId ?? 0, {
      skip: !open || !organizationId,
    });

  const stats = statsResponse?.data;

  const targetStatus =
    organization?.status === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED';

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className='w-full overflow-y-auto p-0 sm:max-w-2xl'>
        <SheetHeader className='border-b px-6 py-4'>
          <SheetTitle className='text-lg font-semibold'>
            Organization details
          </SheetTitle>
          <SheetDescription>
            Review organization profile, lifecycle status, and linked users.
          </SheetDescription>
        </SheetHeader>

        <div className='space-y-6 px-6 py-5'>
          {isLoading || !organization ? (
            <div className='space-y-4'>
              <Skeleton className='h-16 w-full' />
              <Skeleton className='h-40 w-full' />
              <Skeleton className='h-56 w-full' />
            </div>
          ) : (
            <>
              <section className='space-y-4'>
                <div className='flex items-start justify-between gap-4'>
                  <div className='space-y-2'>
                    <div className='flex items-center gap-3'>
                      <div className='flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10'>
                        <Building2 className='h-6 w-6 text-primary' />
                      </div>
                      <div className='min-w-0'>
                        <h2 className='truncate text-xl font-semibold'>
                          {organization.name}
                        </h2>
                        <p className='text-sm text-muted-foreground'>
                          {organization.code}
                        </p>
                      </div>
                    </div>
                    <div className='flex flex-wrap items-center gap-2'>
                      <AdminStatusBadge status={organization.status} />
                      <Badge variant='outline'>
                        {organization.organizationType}
                      </Badge>
                    </div>
                  </div>
                  <div className='flex flex-col items-end gap-2'>
                    <Button
                      type='button'
                      variant='outline'
                      onClick={() => onCreateUser(organization.id)}
                    >
                      Create user
                    </Button>
                    <Button
                      type='button'
                      variant='secondary'
                      onClick={() =>
                        onToggleStatus(organization.id, organization.status)
                      }
                    >
                      {targetStatus === 'SUSPENDED' ? 'Suspend' : 'Activate'}
                    </Button>
                  </div>
                </div>

                {organization.description ? (
                  <p className='text-sm text-muted-foreground'>
                    {organization.description}
                  </p>
                ) : null}
              </section>

              <Separator />

              <section className='grid gap-3 sm:grid-cols-2'>
                <InfoTile
                  icon={<Mail className='h-4 w-4' />}
                  label='Email'
                  value={organization.email}
                />
                <InfoTile
                  icon={<Phone className='h-4 w-4' />}
                  label='Phone'
                  value={organization.phoneNumber}
                />
                <InfoTile
                  icon={<MapPin className='h-4 w-4' />}
                  label='Address'
                  value={organization.address}
                />
                <InfoTile
                  icon={<Globe className='h-4 w-4' />}
                  label='Website'
                  value={organization.website}
                />
              </section>

              <Separator />

              <section className='grid gap-3 sm:grid-cols-3'>
                <StatTile
                  icon={<Users className='h-4 w-4' />}
                  label='Users'
                  value={
                    isLoadingStats ? '...' : String(stats?.totalUsers ?? 0)
                  }
                  helper='Total accounts'
                />
                <StatTile
                  icon={<ShieldAlert className='h-4 w-4' />}
                  label='Suspended'
                  value={
                    isLoadingStats ? '...' : String(stats?.suspendedUsers ?? 0)
                  }
                  helper='Blocked users'
                />
                <StatTile
                  icon={<Users className='h-4 w-4' />}
                  label='Active'
                  value={
                    isLoadingStats ? '...' : String(stats?.activeUsers ?? 0)
                  }
                  helper='Enabled users'
                />
              </section>

              <Separator />

              <section className='grid gap-3 sm:grid-cols-2'>
                <InfoTile
                  icon={<Building2 className='h-4 w-4' />}
                  label='Created'
                  value={formatAdminDate(organization.createdAt)}
                />
                <InfoTile
                  icon={<Building2 className='h-4 w-4' />}
                  label='Updated'
                  value={formatAdminDate(organization.updatedAt)}
                />
                <InfoTile
                  icon={<Building2 className='h-4 w-4' />}
                  label='Billing cycle'
                  value={organization.currentBillingCycle}
                />
                <InfoTile
                  icon={<Building2 className='h-4 w-4' />}
                  label='Currency / Language'
                  value={[organization.currency, organization.language]
                    .filter(Boolean)
                    .join(' / ')}
                />
              </section>

              <Separator />

              <OrganizationUsersPreview
                organizationId={organization.id}
                onCreateUser={() => onCreateUser(organization.id)}
                onViewAllUsers={() => onViewAllUsers(organization.id)}
              />
            </>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}

function InfoTile({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value?: string | number | null;
}) {
  return (
    <div className='rounded-lg border p-3'>
      <div className='flex items-center gap-2 text-xs font-medium text-muted-foreground'>
        {icon}
        {label}
      </div>
      <p className='mt-2 truncate text-sm font-medium'>{value || 'N/A'}</p>
    </div>
  );
}

function StatTile({
  icon,
  label,
  value,
  helper,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  helper: string;
}) {
  return (
    <div className='rounded-lg border p-3'>
      <div className='flex items-center gap-2 text-xs font-medium text-muted-foreground'>
        {icon}
        {label}
      </div>
      <p className='mt-2 text-2xl font-semibold'>{value}</p>
      <p className='text-xs text-muted-foreground'>{helper}</p>
    </div>
  );
}
