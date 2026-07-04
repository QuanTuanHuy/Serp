'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User details drawer
 */

import type React from 'react';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  Button,
  ScrollArea,
  Separator,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  Skeleton,
} from '@/shared/components/ui';
import { Building2, Mail, UserCircle2 } from 'lucide-react';
import { useGetUserDetailQuery } from '@/modules/admin/services/users/usersApi';
import { AdminStatusBadge } from '@/modules/admin/components/shared/AdminStatusBadge';
import { formatAdminDate } from '@/modules/admin/utils/date';
import { getInitials } from '@/shared/utils';
import type { UserStatus } from '@/modules/admin/types';

interface UserDetailsDrawerProps {
  open: boolean;
  organizationId?: number;
  userId?: number;
  onOpenChange: (open: boolean) => void;
  onEdit: (userId: number, organizationId?: number) => void;
  onAccess: (userId: number, organizationId?: number) => void;
  onToggleStatus: (
    userId: number,
    organizationId: number | undefined,
    status: UserStatus
  ) => void;
}

export function UserDetailsDrawer({
  open,
  organizationId,
  userId,
  onOpenChange,
  onEdit,
  onAccess,
  onToggleStatus,
}: UserDetailsDrawerProps) {
  const { data, isLoading } = useGetUserDetailQuery(
    { organizationId: organizationId ?? 0, userId: userId ?? 0 },
    { skip: !open || !organizationId || !userId }
  );

  const user = data?.data;

  const targetStatus =
    user?.status === 'SUSPENDED' ? 'ACTIVE' : ('SUSPENDED' as UserStatus);

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className='w-full p-0 sm:max-w-2xl flex flex-col h-full overflow-hidden'>
        <SheetHeader className='border-b px-6 py-4 flex-shrink-0'>
          <SheetTitle className='text-lg font-semibold'>
            User details
          </SheetTitle>
          <SheetDescription>
            View profile, access, and activity data for the selected user.
          </SheetDescription>
        </SheetHeader>

        {isLoading || !user ? (
          <div className='flex-1 space-y-4 px-6 py-5 overflow-y-auto'>
            <Skeleton className='h-20 w-full' />
            <Skeleton className='h-44 w-full' />
            <Skeleton className='h-36 w-full' />
          </div>
        ) : (
          <>
            <div className='flex-1 overflow-y-auto px-6 py-5 space-y-6'>
              <section className='flex items-start justify-between gap-4'>
                <div className='flex items-center gap-3'>
                  <Avatar className='h-12 w-12'>
                    {user.avatarUrl ? (
                      <AvatarImage
                        src={user.avatarUrl}
                        alt={`${user.firstName ?? ''} ${user.lastName ?? ''}`}
                      />
                    ) : null}
                    <AvatarFallback className='bg-primary/10 text-sm'>
                      {getInitials({
                        firstName: user.firstName,
                        lastName: user.lastName,
                        email: user.email,
                      })}
                    </AvatarFallback>
                  </Avatar>
                  <div className='min-w-0'>
                    <h2 className='truncate text-xl font-semibold'>
                      {user.firstName} {user.lastName}
                    </h2>
                    <p className='truncate text-sm text-muted-foreground'>
                      {user.email}
                    </p>
                    <div className='mt-2 flex flex-wrap items-center gap-2'>
                      <AdminStatusBadge status={user.status} />
                      <Badge variant='outline'>{user.userType}</Badge>
                    </div>
                  </div>
                </div>
              </section>

              <Separator />

              <section className='grid gap-3 sm:grid-cols-2'>
                <InfoTile
                  icon={<Mail className='h-4 w-4' />}
                  label='Email'
                  value={user.email}
                />
                <InfoTile
                  icon={<Building2 className='h-4 w-4' />}
                  label='Organization'
                  value={user.organizationName}
                />
                <InfoTile
                  icon={<UserCircle2 className='h-4 w-4' />}
                  label='Phone'
                  value={user.phoneNumber}
                />
                <InfoTile
                  icon={<UserCircle2 className='h-4 w-4' />}
                  label='Timezone'
                  value={user.timezone}
                />
                <InfoTile
                  icon={<UserCircle2 className='h-4 w-4' />}
                  label='Last login'
                  value={formatAdminDate(user.lastLoginAt, 'Never')}
                />
                <InfoTile
                  icon={<UserCircle2 className='h-4 w-4' />}
                  label='Created'
                  value={formatAdminDate(user.createdAt)}
                />
              </section>

              <Separator />

              <section className='space-y-3'>
                <h3 className='text-sm font-semibold'>Roles</h3>
                <div className='flex flex-wrap gap-2'>
                  {user.roles.length > 0 ? (
                    user.roles.map((role) => (
                      <Badge key={role.id} variant='outline'>
                        {role.name}
                      </Badge>
                    ))
                  ) : (
                    <p className='text-sm text-muted-foreground'>
                      No roles assigned.
                    </p>
                  )}
                </div>
              </section>

              <section className='space-y-3'>
                <h3 className='text-sm font-semibold'>Departments</h3>
                <div className='flex flex-wrap gap-2'>
                  {user.departments.length > 0 ? (
                    user.departments.map((department) => (
                      <Badge key={department.id} variant='secondary'>
                        {department.name || `Department #${department.id}`}
                        {department.isPrimary ? ' • Primary' : ''}
                      </Badge>
                    ))
                  ) : (
                    <p className='text-sm text-muted-foreground'>
                      No department assignment.
                    </p>
                  )}
                </div>
              </section>

              <Separator />

              <section className='space-y-3'>
                <h3 className='text-sm font-semibold'>Module access</h3>
                <ScrollArea className='max-h-56 rounded-md border'>
                  <div className='divide-y'>
                    {user.moduleAccesses.length > 0 ? (
                      user.moduleAccesses.map((module) => (
                        <div
                          key={`${module.moduleId}-${module.moduleCode}`}
                          className='flex items-center justify-between gap-3 p-3'
                        >
                          <div className='min-w-0'>
                            <p className='text-sm font-medium'>
                              {module.moduleName || module.moduleCode}
                            </p>
                            <p className='text-xs text-muted-foreground'>
                              {module.moduleCode}
                            </p>
                          </div>
                          <Badge
                            variant={module.isActive ? 'default' : 'secondary'}
                          >
                            {module.isActive ? 'Enabled' : 'Disabled'}
                          </Badge>
                        </div>
                      ))
                    ) : (
                      <div className='p-3 text-sm text-muted-foreground'>
                        No module access records.
                      </div>
                    )}
                  </div>
                </ScrollArea>
              </section>
            </div>

            {/* Sticky Action Footer */}
            <div className='border-t px-6 py-4 flex items-center justify-end gap-2 bg-background flex-shrink-0'>
              <Button
                type='button'
                variant='outline'
                onClick={() => onEdit(user.id, organizationId)}
              >
                Edit user
              </Button>
              <Button
                type='button'
                variant='secondary'
                onClick={() => onAccess(user.id, organizationId)}
              >
                Access
              </Button>
              <Button
                type='button'
                variant={
                  targetStatus === 'SUSPENDED' ? 'destructive' : 'default'
                }
                onClick={() =>
                  onToggleStatus(user.id, organizationId, user.status)
                }
              >
                {targetStatus === 'SUSPENDED' ? 'Suspend' : 'Activate'}
              </Button>
            </div>
          </>
        )}
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
  value?: string | null;
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
