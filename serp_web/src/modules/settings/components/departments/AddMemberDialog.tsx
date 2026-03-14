/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Add Member to Department Dialog
 */

'use client';

import React, { useState, useMemo, useCallback } from 'react';
import {
  UserPlus,
  Users,
  Loader2,
  X,
  Check,
  Search,
  Briefcase,
  Mail,
} from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Badge } from '@/shared/components/ui/badge';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Separator } from '@/shared/components/ui/separator';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/shared/components/ui/command';
import { Combobox } from '@/shared/components/ui/combobox';
import { cn } from '@/shared/utils';
import { isSuccessResponse } from '@/lib/store/api/utils';
import type {
  Department,
  DepartmentMember,
  AssignUserToDepartmentRequest,
  BulkAssignUsersToDepartmentRequest,
} from '../../types';

interface AddMemberDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  department: Department | null;
  useDepartmentMembers: (departmentId?: number) => any;
  managers: Array<{ id: number; name: string; email?: string }>;
  onAssignUser: (
    departmentId: number,
    request: AssignUserToDepartmentRequest
  ) => Promise<void>;
  onBulkAssignUsers: (
    departmentId: number,
    request: BulkAssignUsersToDepartmentRequest
  ) => Promise<void>;
  isAssigning: boolean;
  isBulkAssigning: boolean;
}

function getInitials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

export const AddMemberDialog: React.FC<AddMemberDialogProps> = ({
  open,
  onOpenChange,
  department,
  useDepartmentMembers,
  managers,
  onAssignUser,
  onBulkAssignUsers,
  isAssigning,
  isBulkAssigning,
}) => {
  const [mode, setMode] = useState<'single' | 'bulk'>('single');

  // Single mode
  const [selectedUserId, setSelectedUserId] = useState<number | undefined>();
  const [jobTitle, setJobTitle] = useState('');
  const [isPrimary, setIsPrimary] = useState(false);

  // Bulk mode
  const [selectedUserIds, setSelectedUserIds] = useState<number[]>([]);
  const [bulkJobTitle, setBulkJobTitle] = useState('');
  const [bulkPickerOpen, setBulkPickerOpen] = useState(false);
  const [bulkSearchQuery, setBulkSearchQuery] = useState('');

  // Fetch existing members to filter them out
  const { data: membersData } = useDepartmentMembers(
    open ? department?.id : undefined
  );
  const existingMemberIds = useMemo(() => {
    if (!membersData || !isSuccessResponse(membersData))
      return new Set<number>();
    return new Set(
      (membersData.data as DepartmentMember[]).map((m) => m.userId)
    );
  }, [membersData]);

  // Filtered user lists
  const availableUsers = useMemo(
    () => managers.filter((u) => !existingMemberIds.has(u.id)),
    [managers, existingMemberIds]
  );

  const comboboxItems = useMemo(
    () =>
      availableUsers.map((u) => ({
        value: u.id,
        label: u.email ? `${u.name} (${u.email})` : u.name,
      })),
    [availableUsers]
  );

  const bulkFilteredUsers = useMemo(() => {
    if (!bulkSearchQuery) return availableUsers;
    const q = bulkSearchQuery.toLowerCase();
    return availableUsers.filter(
      (u) =>
        u.name.toLowerCase().includes(q) ||
        (u.email && u.email.toLowerCase().includes(q))
    );
  }, [availableUsers, bulkSearchQuery]);

  const selectedUser = useMemo(
    () => managers.find((u) => u.id === selectedUserId),
    [managers, selectedUserId]
  );

  const selectedBulkUsers = useMemo(
    () => managers.filter((u) => selectedUserIds.includes(u.id)),
    [managers, selectedUserIds]
  );

  const resetForm = useCallback(() => {
    setSelectedUserId(undefined);
    setJobTitle('');
    setIsPrimary(false);
    setSelectedUserIds([]);
    setBulkJobTitle('');
    setBulkSearchQuery('');
    setMode('single');
  }, []);

  const handleClose = useCallback(() => {
    resetForm();
    onOpenChange(false);
  }, [resetForm, onOpenChange]);

  const handleSingleSubmit = useCallback(async () => {
    if (!department || !selectedUserId) return;
    try {
      const request: AssignUserToDepartmentRequest = {
        userId: selectedUserId,
        jobTitle: jobTitle.trim() || undefined,
        isPrimary: isPrimary || undefined,
      };
      await onAssignUser(department.id, request);
      resetForm();
    } catch {
      // Error handled in hook
    }
  }, [
    department,
    selectedUserId,
    jobTitle,
    isPrimary,
    onAssignUser,
    resetForm,
  ]);

  const handleBulkSubmit = useCallback(async () => {
    if (!department || selectedUserIds.length === 0) return;
    try {
      const request: BulkAssignUsersToDepartmentRequest = {
        userIds: selectedUserIds,
        jobTitle: bulkJobTitle.trim() || undefined,
      };
      await onBulkAssignUsers(department.id, request);
      resetForm();
    } catch {
      // Error handled in hook
    }
  }, [department, selectedUserIds, bulkJobTitle, onBulkAssignUsers, resetForm]);

  const toggleBulkUser = useCallback((userId: number) => {
    setSelectedUserIds((prev) =>
      prev.includes(userId)
        ? prev.filter((id) => id !== userId)
        : [...prev, userId]
    );
  }, []);

  const removeBulkUser = useCallback((userId: number) => {
    setSelectedUserIds((prev) => prev.filter((id) => id !== userId));
  }, []);

  if (!department) return null;

  const isLoading = isAssigning || isBulkAssigning;

  return (
    <Dialog open={open} onOpenChange={(o) => !o && handleClose()}>
      <DialogContent className='sm:max-w-[560px] max-h-[85vh]'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <div className='h-8 w-8 rounded-lg bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center'>
              <UserPlus className='h-4 w-4 text-purple-600 dark:text-purple-400' />
            </div>
            Add Members
          </DialogTitle>
          <DialogDescription>
            Add team members to{' '}
            <span className='font-medium'>{department.name}</span>
          </DialogDescription>
        </DialogHeader>

        <Tabs
          value={mode}
          onValueChange={(v) => setMode(v as 'single' | 'bulk')}
        >
          <TabsList className='w-full'>
            <TabsTrigger value='single' className='flex-1'>
              <UserPlus className='h-3.5 w-3.5 mr-1.5' />
              Single Member
            </TabsTrigger>
            <TabsTrigger value='bulk' className='flex-1'>
              <Users className='h-3.5 w-3.5 mr-1.5' />
              Multiple Members
            </TabsTrigger>
          </TabsList>

          {/* ===== Single Member Mode ===== */}
          <TabsContent value='single' className='space-y-4 mt-4'>
            {/* User Picker */}
            <div className='space-y-2'>
              <Label>
                Select Member <span className='text-red-500'>*</span>
              </Label>
              <Combobox
                value={selectedUserId}
                onChange={(v) =>
                  setSelectedUserId(v !== undefined ? Number(v) : undefined)
                }
                items={comboboxItems}
                placeholder='Search by name or email...'
                emptyText={
                  availableUsers.length === 0
                    ? 'All users are already members'
                    : 'No matching users'
                }
                clearable
              />
            </div>

            {/* Job Title */}
            <div className='space-y-2'>
              <Label htmlFor='single-job-title'>Job Title</Label>
              <Input
                id='single-job-title'
                placeholder='e.g., Software Engineer, Team Lead'
                value={jobTitle}
                onChange={(e) => setJobTitle(e.target.value)}
              />
            </div>

            {/* Primary Department */}
            <div className='flex items-center space-x-3'>
              <Checkbox
                id='single-is-primary'
                checked={isPrimary}
                onCheckedChange={(c) => setIsPrimary(c === true)}
              />
              <div>
                <Label
                  htmlFor='single-is-primary'
                  className='cursor-pointer text-sm font-medium'
                >
                  Set as primary department
                </Label>
                <p className='text-xs text-muted-foreground'>
                  This will be the user&apos;s main department
                </p>
              </div>
            </div>

            {/* Preview */}
            {selectedUser && (
              <>
                <Separator />
                <div className='rounded-lg border bg-muted/30 p-3'>
                  <p className='text-xs font-medium text-muted-foreground mb-2'>
                    Assignment Preview
                  </p>
                  <div className='flex items-center gap-3'>
                    <Avatar className='h-9 w-9'>
                      <AvatarFallback className='bg-purple-100 text-purple-700 text-xs'>
                        {getInitials(selectedUser.name)}
                      </AvatarFallback>
                    </Avatar>
                    <div className='flex-1 min-w-0'>
                      <p className='text-sm font-medium truncate'>
                        {selectedUser.name}
                      </p>
                      {selectedUser.email && (
                        <div className='flex items-center gap-1'>
                          <Mail className='h-3 w-3 text-muted-foreground' />
                          <p className='text-xs text-muted-foreground truncate'>
                            {selectedUser.email}
                          </p>
                        </div>
                      )}
                    </div>
                    <div className='flex flex-col items-end gap-1'>
                      {jobTitle.trim() && (
                        <Badge variant='outline' className='text-xs'>
                          <Briefcase className='h-3 w-3 mr-1' />
                          {jobTitle.trim()}
                        </Badge>
                      )}
                      {isPrimary && (
                        <Badge variant='secondary' className='text-xs'>
                          Primary
                        </Badge>
                      )}
                    </div>
                  </div>
                </div>
              </>
            )}
          </TabsContent>

          {/* ===== Bulk Mode ===== */}
          <TabsContent value='bulk' className='space-y-4 mt-4'>
            {/* Multi-Select User Picker */}
            <div className='space-y-2'>
              <Label>
                Select Members <span className='text-red-500'>*</span>
              </Label>
              <Popover open={bulkPickerOpen} onOpenChange={setBulkPickerOpen}>
                <PopoverTrigger asChild>
                  <Button
                    type='button'
                    variant='outline'
                    role='combobox'
                    className='w-full justify-between h-auto min-h-10'
                  >
                    {selectedUserIds.length === 0 ? (
                      <span className='text-muted-foreground'>
                        Search and select users...
                      </span>
                    ) : (
                      <span className='text-sm'>
                        {selectedUserIds.length} user
                        {selectedUserIds.length > 1 ? 's' : ''} selected
                      </span>
                    )}
                    <Search className='ml-2 h-4 w-4 shrink-0 opacity-50' />
                  </Button>
                </PopoverTrigger>
                <PopoverContent
                  className='w-[var(--radix-popover-trigger-width)] p-0'
                  align='start'
                >
                  <Command shouldFilter={false}>
                    <CommandInput
                      placeholder='Search by name or email...'
                      value={bulkSearchQuery}
                      onValueChange={setBulkSearchQuery}
                    />
                    <CommandList>
                      <CommandEmpty>
                        {availableUsers.length === 0
                          ? 'All users are already members'
                          : 'No matching users'}
                      </CommandEmpty>
                      <CommandGroup>
                        {bulkFilteredUsers.map((user) => {
                          const isSelected = selectedUserIds.includes(user.id);
                          return (
                            <CommandItem
                              key={user.id}
                              value={String(user.id)}
                              onSelect={() => toggleBulkUser(user.id)}
                            >
                              <div
                                className={cn(
                                  'mr-2 flex h-4 w-4 items-center justify-center rounded-sm border',
                                  isSelected
                                    ? 'bg-purple-600 border-purple-600 text-white'
                                    : 'border-muted-foreground/30'
                                )}
                              >
                                {isSelected && <Check className='h-3 w-3' />}
                              </div>
                              <Avatar className='h-6 w-6 mr-2'>
                                <AvatarFallback className='text-[9px] bg-purple-100 text-purple-700'>
                                  {getInitials(user.name)}
                                </AvatarFallback>
                              </Avatar>
                              <div className='flex-1 min-w-0'>
                                <p className='text-sm truncate'>{user.name}</p>
                                {user.email && (
                                  <p className='text-xs text-muted-foreground truncate'>
                                    {user.email}
                                  </p>
                                )}
                              </div>
                            </CommandItem>
                          );
                        })}
                      </CommandGroup>
                    </CommandList>
                  </Command>
                </PopoverContent>
              </Popover>
            </div>

            {/* Selected Users Pills */}
            {selectedBulkUsers.length > 0 && (
              <div className='space-y-2'>
                <p className='text-xs font-medium text-muted-foreground'>
                  Selected ({selectedBulkUsers.length})
                </p>
                <ScrollArea className='max-h-[120px]'>
                  <div className='flex flex-wrap gap-1.5'>
                    {selectedBulkUsers.map((user) => (
                      <Badge
                        key={user.id}
                        variant='secondary'
                        className='pl-1.5 pr-1 py-1 gap-1.5'
                      >
                        <Avatar className='h-4 w-4'>
                          <AvatarFallback className='text-[7px] bg-purple-100 text-purple-700'>
                            {getInitials(user.name)}
                          </AvatarFallback>
                        </Avatar>
                        <span className='text-xs'>{user.name}</span>
                        <button
                          type='button'
                          onClick={() => removeBulkUser(user.id)}
                          className='ml-0.5 rounded-full hover:bg-muted-foreground/20 p-0.5'
                        >
                          <X className='h-3 w-3' />
                        </button>
                      </Badge>
                    ))}
                  </div>
                </ScrollArea>
              </div>
            )}

            {/* Shared Job Title */}
            <div className='space-y-2'>
              <Label htmlFor='bulk-job-title'>
                Job Title{' '}
                <span className='text-xs text-muted-foreground font-normal'>
                  (applied to all)
                </span>
              </Label>
              <Input
                id='bulk-job-title'
                placeholder='e.g., Software Engineer'
                value={bulkJobTitle}
                onChange={(e) => setBulkJobTitle(e.target.value)}
              />
            </div>
          </TabsContent>
        </Tabs>

        <DialogFooter className='gap-2 sm:gap-0'>
          <Button
            type='button'
            variant='outline'
            onClick={handleClose}
            disabled={isLoading}
          >
            Cancel
          </Button>
          {mode === 'single' ? (
            <Button
              className='bg-purple-600 hover:bg-purple-700'
              onClick={handleSingleSubmit}
              disabled={isLoading || !selectedUserId}
            >
              {isAssigning && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              Assign Member
            </Button>
          ) : (
            <Button
              className='bg-purple-600 hover:bg-purple-700'
              onClick={handleBulkSubmit}
              disabled={isLoading || selectedUserIds.length === 0}
            >
              {isBulkAssigning && (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              )}
              Assign{' '}
              {selectedUserIds.length > 0
                ? `${selectedUserIds.length} Member${selectedUserIds.length > 1 ? 's' : ''}`
                : 'Members'}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
