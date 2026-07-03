/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - List of users with module access
 */

'use client';

import { useState, useMemo } from 'react';
import { Trash2, Loader2, Search, UserX } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import type { UserProfile } from '@/modules/admin/types';
import type { ModuleRole } from '../../types/module-access.types';
import { getInitials } from '@/shared/utils';

export interface ModuleUsersListProps {
  users: UserProfile[];
  isLoading: boolean;
  isRevoking: boolean;
  onRevoke: (userId: number) => void;
  roles: ModuleRole[];
  search: string;
  onSearchChange: (search: string) => void;
  pagination: {
    currentPage: number;
    totalPages: number;
    totalItems: number;
  };
  onPageChange: (page: number) => void;
}

export function ModuleUsersList({
  users,
  isLoading,
  isRevoking,
  onRevoke,
  roles,
  search,
  onSearchChange,
  pagination,
  onPageChange,
}: ModuleUsersListProps) {
  const [userToRevoke, setUserToRevoke] = useState<UserProfile | null>(null);

  const handleRevokeClick = (user: UserProfile) => {
    setUserToRevoke(user);
  };

  const handleConfirmRevoke = () => {
    if (userToRevoke) {
      onRevoke(userToRevoke.id);
      setUserToRevoke(null);
    }
  };

  // Create an O(1) set of module role names for optimized lookup
  const moduleRoleNamesSet = useMemo(
    () => new Set(roles.map((r) => r.name)),
    [roles]
  );

  if (isLoading) {
    return (
      <div className='flex items-center justify-center h-64'>
        <div className='flex items-center gap-2 text-muted-foreground text-sm'>
          <Loader2 className='h-5 w-5 animate-spin' />
          <span>Loading users...</span>
        </div>
      </div>
    );
  }

  if (users.length === 0 && !search) {
    return (
      <div className='flex flex-col items-center justify-center h-64 text-center'>
        <UserX className='h-12 w-12 text-muted-foreground mb-3' />
        <h3 className='text-lg font-semibold mb-1'>No users assigned yet</h3>
        <p className='text-sm text-muted-foreground max-w-md'>
          Start by assigning users from your organization to grant them access
          to this module.
        </p>
      </div>
    );
  }

  return (
    <>
      <div className='space-y-4'>
        {/* Search Bar */}
        <div className='relative'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search users...'
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            className='pl-9'
          />
        </div>

        {/* Stats */}
        <div className='flex items-center justify-between text-sm'>
          <span className='text-muted-foreground'>
            <span className='font-semibold text-foreground'>
              {pagination.totalItems}
            </span>{' '}
            {pagination.totalItems === 1 ? 'user' : 'users'} with access
          </span>
        </div>

        {/* Users List */}
        <ScrollArea className='h-[400px] pr-4'>
          {users.length === 0 ? (
            <div className='flex items-center justify-center h-32 text-sm text-muted-foreground'>
              No users match your search
            </div>
          ) : (
            <div className='space-y-2'>
              {users.map((user) => {
                // Filter module specific roles
                const matchedRoles =
                  user.roles?.filter((roleName) =>
                    moduleRoleNamesSet.has(roleName)
                  ) || [];

                return (
                  <div
                    key={user.id}
                    className='flex items-center justify-between p-3 rounded-lg border bg-card hover:bg-muted/50 transition-colors'
                  >
                    <div className='flex items-center gap-3 flex-1 min-w-0'>
                      <Avatar className='h-10 w-10'>
                        <AvatarFallback>
                          {getInitials({
                            firstName: user.firstName,
                            lastName: user.lastName,
                            email: user.email,
                          })}
                        </AvatarFallback>
                      </Avatar>
                      <div className='flex flex-col min-w-0 flex-1'>
                        <div className='flex items-center gap-2 flex-wrap'>
                          <span className='text-sm font-medium truncate'>
                            {user.firstName} {user.lastName}
                          </span>
                          {/* Render status */}
                          {user.status && (
                            <Badge
                              variant={
                                user.status === 'ACTIVE'
                                  ? 'default'
                                  : 'secondary'
                              }
                              className='text-[10px] px-1.5 py-0'
                            >
                              {user.status}
                            </Badge>
                          )}
                          {/* Render roles */}
                          {matchedRoles.map((roleName) => (
                            <Badge
                              key={roleName}
                              variant='secondary'
                              className='bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800 text-[10px] px-1.5 py-0'
                            >
                              {roleName.replace('ROLE_', '').replace('_', ' ')}
                            </Badge>
                          ))}
                        </div>
                        <span className='text-xs text-muted-foreground truncate'>
                          {user.email}
                        </span>
                      </div>
                    </div>
                    <Button
                      size='sm'
                      variant='ghost'
                      onClick={() => handleRevokeClick(user)}
                      disabled={isRevoking}
                      className='text-destructive hover:text-destructive hover:bg-destructive/10'
                    >
                      <Trash2 className='h-4 w-4 mr-2' />
                      Revoke
                    </Button>
                  </div>
                );
              })}
            </div>
          )}
        </ScrollArea>

        {/* Pagination Footer */}
        {pagination.totalPages > 1 && (
          <div className='flex items-center justify-between border-t pt-4 mt-4'>
            <span className='text-xs text-muted-foreground'>
              Showing Page {pagination.currentPage + 1} of{' '}
              {pagination.totalPages || 1} ({pagination.totalItems} users total)
            </span>
            <div className='flex items-center gap-2'>
              <Button
                variant='outline'
                size='sm'
                disabled={pagination.currentPage === 0}
                onClick={() => onPageChange(pagination.currentPage - 1)}
              >
                Previous
              </Button>
              <Button
                variant='outline'
                size='sm'
                disabled={pagination.currentPage >= pagination.totalPages - 1}
                onClick={() => onPageChange(pagination.currentPage + 1)}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Revoke Confirmation Dialog */}
      <AlertDialog
        open={!!userToRevoke}
        onOpenChange={(open: boolean) => !open && setUserToRevoke(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Revoke Module Access</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to revoke module access for{' '}
              <span className='font-semibold'>
                {userToRevoke?.firstName} {userToRevoke?.lastName}
              </span>
              ?
              <br />
              <br />
              This user will no longer be able to access this module until
              reassigned.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmRevoke}
              className='bg-destructive hover:bg-destructive/90'
            >
              {isRevoking ? (
                <>
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  Revoking...
                </>
              ) : (
                'Revoke Access'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
