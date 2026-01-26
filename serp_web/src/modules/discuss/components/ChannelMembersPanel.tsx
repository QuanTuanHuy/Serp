/*
Author: QuanTuanHuy
Description: Part of Serp Project - Channel Members Panel component
*/

'use client';

import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  ScrollArea,
  Button,
} from '@/shared/components/ui';
import {
  useGetChannelMembersQuery,
  useRemoveChannelMemberMutation,
  useAddChannelMemberMutation,
} from '../api/discussApi';
import {
  Crown,
  Shield,
  User,
  UserMinus,
  Loader2,
  AlertCircle,
  CircleDot,
  UserPlus,
  X,
} from 'lucide-react';
import { cn, getAvatarColor } from '@/shared/utils';
import { UserSearch } from './UserSearch';
import type { ChannelMember } from '../types';

interface ChannelMembersPanelProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  channelId: string;
  channelName: string;
  currentUserId: string;
}

const getRoleIcon = (role: string) => {
  switch (role) {
    case 'OWNER':
      return <Crown className='h-4 w-4 text-amber-500' />;
    case 'ADMIN':
      return <Shield className='h-4 w-4 text-blue-500' />;
    default:
      return <User className='h-4 w-4 text-slate-500' />;
  }
};

const getRoleBadge = (role: string) => {
  const styles = {
    OWNER:
      'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    ADMIN: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
    MEMBER: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-400',
  };

  return (
    <Badge
      variant='outline'
      className={cn(
        'text-xs',
        styles[role as keyof typeof styles] || styles.MEMBER
      )}
    >
      {role.toLowerCase()}
    </Badge>
  );
};

const getUserInitials = (name: string) => {
  return name
    .split(' ')
    .map((word) => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

export const ChannelMembersPanel: React.FC<ChannelMembersPanelProps> = ({
  open,
  onOpenChange,
  channelId,
  channelName,
  currentUserId,
}) => {
  const [isAdding, setIsAdding] = useState(false);
  const [selectedUsersToAdd, setSelectedUsersToAdd] = useState<string[]>([]);

  const {
    data: membersResponse,
    isLoading,
    isError,
    error,
  } = useGetChannelMembersQuery(channelId, {
    skip: !open,
  });

  const [removeMember, { isLoading: isRemoving }] =
    useRemoveChannelMemberMutation();
  const [addMember, { isLoading: isAddingMember }] =
    useAddChannelMemberMutation();

  const members = membersResponse?.data || [];
  const memberIds = members.map((m) => m.userId);

  const handleRemoveMember = async (userId: string, userName: string) => {
    if (
      !confirm(`Are you sure you want to remove ${userName} from this channel?`)
    ) {
      return;
    }

    try {
      await removeMember({ channelId, userId }).unwrap();
    } catch (error: any) {
      console.error('Failed to remove member:', error);
      alert(error?.data?.message || 'Failed to remove member.');
    }
  };

  const handleAddMembers = async () => {
    if (selectedUsersToAdd.length === 0) return;

    try {
      // Add members sequentially or parallel
      await Promise.all(
        selectedUsersToAdd.map((userId) =>
          addMember({ channelId, userId }).unwrap()
        )
      );

      setIsAdding(false);
      setSelectedUsersToAdd([]);
      alert('Members added successfully');
    } catch (error: any) {
      console.error('Failed to add members:', error);
      alert(error?.data?.message || 'Failed to add some members.');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl max-h-[80vh] overflow-hidden flex flex-col'>
        <DialogHeader className='flex-shrink-0'>
          <div className='flex items-center justify-between pr-8'>
            <div>
              <DialogTitle className='text-xl font-bold'>
                Channel Members
              </DialogTitle>
              <DialogDescription>
                Members in{' '}
                <span className='font-semibold text-violet-600 dark:text-violet-400'>
                  {channelName}
                </span>
              </DialogDescription>
            </div>

            {!isAdding && (
              <Button
                variant='outline'
                size='sm'
                onClick={() => setIsAdding(true)}
                className='gap-2'
              >
                <UserPlus className='h-4 w-4' />
                Add People
              </Button>
            )}
          </div>
        </DialogHeader>

        {isAdding && (
          <div className='p-4 bg-slate-50 dark:bg-slate-900/50 rounded-lg border border-slate-200 dark:border-slate-800 mt-4 flex flex-col gap-3'>
            <div className='flex items-center justify-between'>
              <span className='text-sm font-medium'>Add new members</span>
              <Button
                variant='ghost'
                size='sm'
                className='h-6 w-6 p-0'
                onClick={() => setIsAdding(false)}
              >
                <X className='h-4 w-4' />
              </Button>
            </div>
            <UserSearch
              selectedUserIds={selectedUsersToAdd}
              onSelectionChange={setSelectedUsersToAdd}
              excludedUserIds={memberIds} // Exclude existing members
              placeholder='Search users to add...'
            />
            <div className='flex justify-end'>
              <Button
                size='sm'
                onClick={handleAddMembers}
                disabled={selectedUsersToAdd.length === 0 || isAddingMember}
                className='bg-violet-600 hover:bg-violet-700'
              >
                {isAddingMember ? (
                  <Loader2 className='h-3 w-3 animate-spin mr-2' />
                ) : null}
                Add Selected
              </Button>
            </div>
          </div>
        )}

        <div className='flex-1 overflow-hidden mt-4'>
          {isLoading ? (
            <div className='flex flex-col items-center justify-center py-12 gap-3'>
              <Loader2 className='h-8 w-8 text-violet-500 animate-spin' />
              <p className='text-sm text-slate-500'>Loading members...</p>
            </div>
          ) : isError ? (
            <div className='flex flex-col items-center justify-center py-12 gap-3'>
              <div className='h-12 w-12 rounded-full bg-rose-100 flex items-center justify-center'>
                <AlertCircle className='h-6 w-6 text-rose-500' />
              </div>
              <p className='text-sm font-semibold'>Failed to load members</p>
            </div>
          ) : members.length === 0 ? (
            <div className='flex flex-col items-center justify-center py-12 gap-3'>
              <User className='h-12 w-12 text-slate-300' />
              <p className='text-sm text-slate-500'>No members found</p>
            </div>
          ) : (
            <ScrollArea className='flex-1'>
              <div className='space-y-1 pr-4'>
                <div className='px-3 py-2 mb-3'>
                  <p className='text-xs font-semibold text-slate-500 uppercase tracking-wide'>
                    {members.length}{' '}
                    {members.length === 1 ? 'Member' : 'Members'}
                  </p>
                </div>

                {members.map((member: ChannelMember) => {
                  const isCurrentUser = member.userId === currentUserId;
                  const canRemove = !isCurrentUser && member.role !== 'OWNER';
                  const userName = member.user?.name || `User ${member.userId}`;
                  const userAvatar = member.user?.avatarUrl;

                  return (
                    <div
                      key={member.id}
                      className={cn(
                        'flex items-center gap-3 px-3 py-3 rounded-lg transition-colors',
                        'hover:bg-slate-50 dark:hover:bg-slate-800/50',
                        isCurrentUser && 'bg-violet-50 dark:bg-violet-900/10'
                      )}
                    >
                      <div className='relative flex-shrink-0'>
                        <Avatar className='h-10 w-10'>
                          {userAvatar && (
                            <AvatarImage src={userAvatar} alt={userName} />
                          )}
                          <AvatarFallback
                            className={cn(
                              'text-sm font-semibold text-white bg-gradient-to-br',
                              getAvatarColor(userName)
                            )}
                          >
                            {getUserInitials(userName)}
                          </AvatarFallback>
                        </Avatar>
                        {member.isOnline && (
                          <div className='absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 bg-white dark:bg-slate-900 rounded-full flex items-center justify-center'>
                            <CircleDot className='h-2.5 w-2.5 text-emerald-500 fill-emerald-500' />
                          </div>
                        )}
                      </div>

                      <div className='flex-1 min-w-0'>
                        <div className='flex items-center gap-2'>
                          <p className='text-sm font-semibold truncate'>
                            {userName}
                            {isCurrentUser && (
                              <span className='ml-1.5 text-xs text-violet-600 font-normal'>
                                (You)
                              </span>
                            )}
                          </p>
                          {getRoleIcon(member.role)}
                        </div>
                        <div className='flex items-center gap-2 mt-0.5'>
                          {getRoleBadge(member.role)}
                          {member.user?.email && (
                            <p className='text-xs text-slate-500 truncate'>
                              {member.user.email}
                            </p>
                          )}
                        </div>
                      </div>

                      {canRemove && (
                        <Button
                          variant='ghost'
                          size='sm'
                          onClick={() =>
                            handleRemoveMember(member.userId, userName)
                          }
                          disabled={isRemoving}
                          className='text-rose-600 hover:text-rose-700 hover:bg-rose-50'
                        >
                          <UserMinus className='h-4 w-4' />
                        </Button>
                      )}
                    </div>
                  );
                })}
              </div>
            </ScrollArea>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};
