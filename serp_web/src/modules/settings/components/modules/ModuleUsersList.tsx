/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - List of users with module access
 */

'use client';

import { useState } from 'react';
import { Trash2, Loader2, Search, Shield, UserX } from 'lucide-react';
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
import { getInitials } from '@/shared/utils';

export interface ModuleUsersListProps {
  users: UserProfile[];
  isLoading: boolean;
  isRevoking: boolean;
  onRevoke: (userId: number) => void;
}

export function ModuleUsersList({
  users,
  isLoading,
  isRevoking,
  onRevoke,
}: ModuleUsersListProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [userToRevoke, setUserToRevoke] = useState<UserProfile | null>(null);

  const filteredUsers = users.filter((u) => {
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      u.firstName?.toLowerCase().includes(q) ||
      u.lastName?.toLowerCase().includes(q) ||
      u.email?.toLowerCase().includes(q)
    );
  });

  const handleRevokeClick = (user: UserProfile) => {
    setUserToRevoke(user);
  };

  const handleConfirmRevoke = () => {
    if (userToRevoke) {
      onRevoke(userToRevoke.id);
      setUserToRevoke(null);
    }
  };

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

  if (users.length === 0) {
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
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className='pl-9'
          />
        </div>

        {/* Stats */}
        <div className='flex items-center justify-between text-sm'>
          <span className='text-muted-foreground'>
            {filteredUsers.length === users.length ? (
              <>
                <span className='font-semibold text-foreground'>
                  {users.length}
                </span>{' '}
                {users.length === 1 ? 'user' : 'users'} with access
              </>
            ) : (
              <>
                Showing{' '}
                <span className='font-semibold text-foreground'>
                  {filteredUsers.length}
                </span>{' '}
                of {users.length}
              </>
            )}
          </span>
        </div>

        {/* Users List */}
        <ScrollArea className='h-[400px] pr-4'>
          {filteredUsers.length === 0 ? (
            <div className='flex items-center justify-center h-32 text-sm text-muted-foreground'>
              No users match your search
            </div>
          ) : (
            <div className='space-y-2'>
              {filteredUsers.map((user) => (
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
                      <div className='flex items-center gap-2'>
                        <span className='text-sm font-medium truncate'>
                          {user.firstName} {user.lastName}
                        </span>
                        {user.status && (
                          <Badge
                            variant={
                              user.status === 'ACTIVE' ? 'default' : 'secondary'
                            }
                            className='text-xs'
                          >
                            {user.status}
                          </Badge>
                        )}
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
              ))}
            </div>
          )}
        </ScrollArea>
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
