'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organization users preview list
 */

import { ChevronRight, Users } from 'lucide-react';
import { useGetUsersQuery } from '@/modules/admin/services/users/usersApi';
import { AdminStatusBadge } from '@/modules/admin/components/shared/AdminStatusBadge';
import { Button, ScrollArea, Skeleton } from '@/shared/components/ui';
import { cn } from '@/shared/utils';

interface OrganizationUsersPreviewProps {
  organizationId: number;
  onViewAllUsers: () => void;
  onCreateUser: () => void;
}

export function OrganizationUsersPreview({
  organizationId,
  onViewAllUsers,
  onCreateUser,
}: OrganizationUsersPreviewProps) {
  const { data, isLoading } = useGetUsersQuery(
    {
      organizationId,
      page: 0,
      pageSize: 5,
      sortBy: 'updatedAt',
      sortDir: 'DESC',
    },
    { skip: !organizationId }
  );

  const users = data?.data.items ?? [];

  return (
    <section className='space-y-3'>
      <div className='flex items-center justify-between gap-3'>
        <div>
          <h3 className='text-sm font-semibold'>Users</h3>
          <p className='text-sm text-muted-foreground'>
            Recent users in this organization.
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={onCreateUser}
          >
            Create user
          </Button>
          <Button
            type='button'
            variant='ghost'
            size='sm'
            onClick={onViewAllUsers}
          >
            View all
            <ChevronRight className='h-4 w-4' />
          </Button>
        </div>
      </div>

      <ScrollArea className='max-h-64 rounded-md border'>
        <div className='divide-y'>
          {isLoading ? (
            Array.from({ length: 3 }).map((_, index) => (
              <div
                key={`org-user-skeleton-${index}`}
                className='flex items-center justify-between gap-3 p-3'
              >
                <div className='flex items-center gap-3'>
                  <Skeleton className='h-9 w-9 rounded-full' />
                  <div className='space-y-2'>
                    <Skeleton className='h-4 w-32' />
                    <Skeleton className='h-3 w-48' />
                  </div>
                </div>
                <Skeleton className='h-6 w-20 rounded-full' />
              </div>
            ))
          ) : users.length > 0 ? (
            users.map((user) => (
              <div
                key={user.id}
                className={cn(
                  'flex items-center justify-between gap-3 p-3',
                  'hover:bg-muted/30'
                )}
              >
                <div className='min-w-0'>
                  <p className='flex items-center gap-2 text-sm font-medium'>
                    <Users className='h-4 w-4 text-muted-foreground' />
                    <span className='truncate'>
                      {user.firstName} {user.lastName}
                    </span>
                  </p>
                  <p className='truncate text-xs text-muted-foreground'>
                    {user.email}
                  </p>
                </div>
                <div className='flex items-center gap-2'>
                  <AdminStatusBadge status={user.status} />
                  <span className='text-xs text-muted-foreground'>
                    {user.userType}
                  </span>
                </div>
              </div>
            ))
          ) : (
            <div className='p-3 text-sm text-muted-foreground'>
              No users found.
            </div>
          )}
        </div>
      </ScrollArea>
    </section>
  );
}
