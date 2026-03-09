/*
Author: QuanTuanHuy
Description: Part of Serp Project - Reusable User Search Component with support for single/multi select
*/

'use client';

import * as React from 'react';
import { Check, ChevronsUpDown, Loader2, X } from 'lucide-react';
import { cn, getAvatarColor } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/shared/components/ui/command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { useGetUsersQuery } from '../api/users.api';
import type { UserInfo } from '../types';

export interface UserSearchProps {
  /**
   * Selected user IDs
   */
  selectedUserIds: string[];
  /**
   * Callback when selection changes
   */
  onSelectionChange: (userIds: string[]) => void;
  /**
   * Mode: 'single' for one user (e.g. DM), 'multi' for multiple (e.g. Group)
   * @default 'multi'
   */
  mode?: 'single' | 'multi';
  /**
   * Placeholder text
   */
  placeholder?: string;
  /**
   * IDs of users to exclude (e.g. current user, existing members)
   */
  excludedUserIds?: string[];
  /**
   * Custom trigger element (optional)
   */
  trigger?: React.ReactNode;
}

export function UserSearch({
  selectedUserIds,
  onSelectionChange,
  mode = 'multi',
  placeholder = 'Search users...',
  excludedUserIds = [],
  trigger,
}: UserSearchProps) {
  const [open, setOpen] = React.useState(false);
  const [searchQuery, setSearchQuery] = React.useState('');

  // Debounce search query manually since we don't have useDebounce hook verified yet
  // actually RTK Query has debounce built-in usually, or we can rely on local state debounce
  // For now, let's just pass query directly, usually API handles it fast enough or we add a small delay

  const { data: usersResponse, isLoading } = useGetUsersQuery(
    { query: searchQuery },
    { skip: !open } // Only fetch when open
  );

  const users = usersResponse?.data || [];

  // Filter out excluded users
  const filteredUsers = React.useMemo(() => {
    return users.filter((user) => !excludedUserIds.includes(user.id));
  }, [users, excludedUserIds]);

  // Selected users objects (for display)
  // In a real app we might need to fetch these if not in the current search results
  // For now we assume they are either in results or we handle them gracefully
  const selectedUsers = React.useMemo(() => {
    return filteredUsers.filter((user) => selectedUserIds.includes(user.id));
  }, [filteredUsers, selectedUserIds]);

  const handleSelect = (userId: string) => {
    if (mode === 'single') {
      onSelectionChange([userId]);
      setOpen(false);
    } else {
      const newSelected = selectedUserIds.includes(userId)
        ? selectedUserIds.filter((id) => id !== userId)
        : [...selectedUserIds, userId];
      onSelectionChange(newSelected);
    }
  };

  const removeUser = (e: React.MouseEvent, userId: string) => {
    e.stopPropagation();
    onSelectionChange(selectedUserIds.filter((id) => id !== userId));
  };

  const getUserInitials = (name: string) => {
    return name
      .split(' ')
      .map((word) => word[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        {trigger ? (
          trigger
        ) : (
          <Button
            variant='outline'
            role='combobox'
            aria-expanded={open}
            className='w-full justify-between min-h-[44px] h-auto'
          >
            {selectedUserIds.length > 0 ? (
              <div className='flex flex-wrap gap-1 items-center'>
                {mode === 'single' ? (
                  // Single select display
                  <span className='truncate'>
                    {users.find((u) => u.id === selectedUserIds[0])?.name ||
                      selectedUserIds[0]}
                  </span>
                ) : (
                  // Multi select display (badges)
                  <>
                    {selectedUserIds.length > 2 ? (
                      <Badge variant='secondary' className='mr-1'>
                        {selectedUserIds.length} selected
                      </Badge>
                    ) : (
                      selectedUserIds.map((id) => {
                        const user = users.find((u) => u.id === id);
                        return (
                          <Badge key={id} variant='secondary' className='mr-1'>
                            {user?.name || id}
                            <div
                              className='ml-1 ring-offset-background rounded-full outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 cursor-pointer'
                              onMouseDown={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                              }}
                              onClick={(e) => removeUser(e, id)}
                            >
                              <X className='h-3 w-3 text-muted-foreground hover:text-foreground' />
                            </div>
                          </Badge>
                        );
                      })
                    )}
                  </>
                )}
              </div>
            ) : (
              <span className='text-muted-foreground'>{placeholder}</span>
            )}
            <ChevronsUpDown className='ml-2 h-4 w-4 shrink-0 opacity-50' />
          </Button>
        )}
      </PopoverTrigger>
      <PopoverContent className='p-0 w-[400px]' align='start'>
        <Command shouldFilter={false}>
          {/* We handle filtering via API and manual exclude */}
          <CommandInput
            placeholder='Search by name or email...'
            value={searchQuery}
            onValueChange={setSearchQuery}
          />
          <CommandList>
            {isLoading ? (
              <div className='py-6 flex justify-center items-center text-sm text-muted-foreground'>
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                Searching...
              </div>
            ) : (
              <CommandEmpty>No users found.</CommandEmpty>
            )}

            <CommandGroup heading='Users'>
              {filteredUsers.map((user) => (
                <CommandItem
                  key={user.id}
                  value={user.name} // Used for internal command filtering if enabled
                  onSelect={() => handleSelect(user.id)}
                  className='cursor-pointer'
                >
                  <div className='flex items-center gap-3 w-full'>
                    <Avatar className='h-8 w-8'>
                      {user.avatarUrl && (
                        <AvatarImage src={user.avatarUrl} alt={user.name} />
                      )}
                      <AvatarFallback
                        className={cn(
                          'text-xs text-white font-medium bg-gradient-to-br',
                          getAvatarColor(user.name)
                        )}
                      >
                        {getUserInitials(user.name)}
                      </AvatarFallback>
                    </Avatar>
                    <div className='flex flex-col flex-1 min-w-0'>
                      <span className='font-medium truncate'>{user.name}</span>
                      <span className='text-xs text-muted-foreground truncate'>
                        {user.email}
                      </span>
                    </div>
                    {selectedUserIds.includes(user.id) && (
                      <Check className='h-4 w-4 text-violet-600' />
                    )}
                  </div>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
