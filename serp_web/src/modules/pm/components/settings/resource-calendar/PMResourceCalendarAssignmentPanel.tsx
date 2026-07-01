/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar assignments
 */

import { FormEvent, useMemo, useState } from 'react';
import { Search, Trash2, Users } from 'lucide-react';
import { toast } from 'sonner';

import { Button } from '@/shared/components/ui/button';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

import { PMDatePicker } from '../../shared';
import { toLocalDateInputValue } from '../../../utils/date';
import type { UserProfile } from '@/modules/admin/types';
import type {
  PMReplaceResourceCalendarAssignmentsRequest,
  PMResourceCalendarAssignmentApi,
  PMResourceCalendarProfileApi,
} from '../../../types/api';

export function PMResourceCalendarAssignmentPanel({
  profiles,
  assignments,
  isSubmitting,
  onSubmit,
  userMap = new Map(),
  users = [],
}: {
  profiles: PMResourceCalendarProfileApi[];
  assignments: PMResourceCalendarAssignmentApi[];
  selectedProfileId: number | null;
  isSubmitting: boolean;
  onProfileChange: (profileId: number | null) => void;
  onSubmit: (
    profileId: number,
    body: PMReplaceResourceCalendarAssignmentsRequest
  ) => Promise<void>;
  userMap?: Map<number, UserProfile>;
  users?: UserProfile[];
}) {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterProfileId, setFilterProfileId] = useState<string>('all');
  const [selectedUserIds, setSelectedUserIds] = useState<number[]>([]);
  const [bulkProfileId, setBulkProfileId] = useState<string>('');
  const [bulkEffectiveFrom, setBulkEffectiveFrom] = useState(
    toLocalDateInputValue(new Date())
  );
  const [bulkEffectiveTo, setBulkEffectiveTo] = useState('');

  // 1. Gather all users (loaded from org + currently assigned users in calendar)
  const allUsersMapped = useMemo(() => {
    const listMap = new Map<number, { id: number; name: string; email: string }>();

    // Load from fetched users
    users.forEach((u) => {
      const fullName = `${u.firstName || ''} ${u.lastName || ''}`.trim();
      listMap.set(u.id, {
        id: u.id,
        name: fullName || u.email || `User #${u.id}`,
        email: u.email || '',
      });
    });

    // Load from assignments to prevent missing users
    assignments.forEach((a) => {
      if (!listMap.has(a.userId)) {
        const u = userMap.get(a.userId);
        const fullName = u ? `${u.firstName || ''} ${u.lastName || ''}`.trim() : '';
        listMap.set(a.userId, {
          id: a.userId,
          name: fullName || u?.email || `User #${a.userId}`,
          email: u?.email || '',
        });
      }
    });

    return Array.from(listMap.values());
  }, [users, assignments, userMap]);

  // 2. Filter users by search term and calendar profile
  const filteredUsers = useMemo(() => {
    return allUsersMapped.filter((user) => {
      // Search text filter
      const matchesSearch =
        user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase());

      if (!matchesSearch) return false;

      // Profile filter
      const userAssignment = assignments.find((a) => a.userId === user.id);
      if (filterProfileId === 'all') {
        return true;
      }
      if (filterProfileId === 'unassigned') {
        return !userAssignment;
      }
      return userAssignment?.profileId === Number(filterProfileId);
    });
  }, [allUsersMapped, assignments, searchTerm, filterProfileId]);

  // 3. Checkbox Handlers
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedUserIds(filteredUsers.map((u) => u.id));
    } else {
      setSelectedUserIds([]);
    }
  };

  const handleSelectUser = (userId: number, checked: boolean) => {
    setSelectedUserIds((current) =>
      checked ? [...current, userId] : current.filter((id) => id !== userId)
    );
  };

  // 4. Bulk assignment/removal handlers
  const handleBulkAssign = async () => {
    if (!bulkProfileId) {
      toast.error('Please select a calendar profile.');
      return;
    }
    if (selectedUserIds.length === 0) {
      toast.error('Please select at least one user.');
      return;
    }
    if (!bulkEffectiveFrom) {
      toast.error('Please select an effective start date.');
      return;
    }

    const targetProfileId = Number(bulkProfileId);

    try {
      // Find all profiles that currently have assignments for the selected users
      const profilesToUpdate = new Set<number>();
      selectedUserIds.forEach((userId) => {
        const existing = assignments.find((a) => a.userId === userId);
        if (existing && existing.profileId !== targetProfileId) {
          profilesToUpdate.add(existing.profileId);
        }
      });

      // A. Remove users from their old profiles
      for (const oldProfileId of Array.from(profilesToUpdate)) {
        const remaining = assignments
          .filter((a) => a.profileId === oldProfileId && !selectedUserIds.includes(a.userId))
          .map((a) => ({
            userId: a.userId,
            effectiveFrom: a.effectiveFrom,
            effectiveTo: a.effectiveTo || null,
          }));
        await onSubmit(oldProfileId, { assignments: remaining });
      }

      // B. Add users to the new profile
      const newAssignmentsForTarget = [
        ...assignments
          .filter((a) => a.profileId === targetProfileId && !selectedUserIds.includes(a.userId))
          .map((a) => ({
            userId: a.userId,
            effectiveFrom: a.effectiveFrom,
            effectiveTo: a.effectiveTo || null,
          })),
        ...selectedUserIds.map((userId) => ({
          userId,
          effectiveFrom: bulkEffectiveFrom,
          effectiveTo: bulkEffectiveTo.trim() || null,
        })),
      ];

      await onSubmit(targetProfileId, { assignments: newAssignmentsForTarget });
      toast.success(`Assigned ${selectedUserIds.length} user(s) successfully.`);
      setSelectedUserIds([]);
    } catch (err) {
      // error handled in parent
    }
  };

  const handleBulkRemove = async () => {
    if (selectedUserIds.length === 0) return;

    try {
      const profilesToUpdate = new Set<number>();
      selectedUserIds.forEach((userId) => {
        const existing = assignments.find((a) => a.userId === userId);
        if (existing) {
          profilesToUpdate.add(existing.profileId);
        }
      });

      for (const profileId of Array.from(profilesToUpdate)) {
        const remaining = assignments
          .filter((a) => a.profileId === profileId && !selectedUserIds.includes(a.userId))
          .map((a) => ({
            userId: a.userId,
            effectiveFrom: a.effectiveFrom,
            effectiveTo: a.effectiveTo || null,
          }));
        await onSubmit(profileId, { assignments: remaining });
      }

      toast.success(`Removed assignments for ${selectedUserIds.length} user(s).`);
      setSelectedUserIds([]);
    } catch (err) {
      // error handled in parent
    }
  };

  const getUserInitials = (name: string) => {
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return `${parts[0]?.[0] || ''}${parts[parts.length - 1]?.[0] || ''}`.toUpperCase();
    }
    return (name[0] || '?').toUpperCase();
  };

  const allSelected =
    filteredUsers.length > 0 && selectedUserIds.length === filteredUsers.length;

  return (
    <Card className='border-border/60 bg-background/90 shadow-sm col-span-1'>
      <CardHeader className='border-b py-4 space-y-3'>
        <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
          <CardTitle className='text-sm flex items-center gap-2'>
            <Users className='h-4 w-4 text-muted-foreground' />
            Resource Assignments Directory
          </CardTitle>
        </div>

        {/* Filter controls */}
        <div className='grid gap-3 sm:grid-cols-2'>
          <div className='relative'>
            <Search className='absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground' />
            <Input
              placeholder='Search users by name or email...'
              className='pl-8 h-9'
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <Select value={filterProfileId} onValueChange={setFilterProfileId}>
            <SelectTrigger className='h-9'>
              <SelectValue placeholder='Filter by Profile' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='all'>All Users</SelectItem>
              <SelectItem value='unassigned'>Unassigned Users</SelectItem>
              {profiles.map((p) => (
                <SelectItem key={p.id} value={String(p.id)}>
                  Profile: {p.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Floating Bulk Action Bar */}
        {selectedUserIds.length > 0 && (
          <div className='flex flex-col gap-3 rounded-md bg-primary/5 border border-primary/20 p-3 text-sm animate-in fade-in slide-in-from-top-2 duration-200'>
            <div className='flex items-center justify-between'>
              <span className='font-semibold text-primary'>
                {selectedUserIds.length} user(s) selected
              </span>
              <Button
                type='button'
                variant='ghost'
                size='sm'
                onClick={handleBulkRemove}
                className='text-destructive hover:text-destructive hover:bg-destructive/10 h-8 px-2'
              >
                <Trash2 className='mr-1.5 h-3.5 w-3.5' />
                Remove assignments
              </Button>
            </div>
            <div className='grid gap-2 items-end md:grid-cols-[1.5fr_1fr_1fr_auto]'>
              <div className='space-y-1.5'>
                <Label className='text-xs'>Target Profile</Label>
                <Select value={bulkProfileId} onValueChange={setBulkProfileId}>
                  <SelectTrigger className='h-8'>
                    <SelectValue placeholder='Select profile' />
                  </SelectTrigger>
                  <SelectContent>
                    {profiles.map((p) => (
                      <SelectItem key={p.id} value={String(p.id)}>
                        {p.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-1.5'>
                <Label className='text-xs'>Effective from</Label>
                <PMDatePicker
                  value={bulkEffectiveFrom}
                  onChange={(date) => setBulkEffectiveFrom(toLocalDateInputValue(date))}
                />
              </div>
              <div className='space-y-1.5'>
                <Label className='text-xs'>Effective to</Label>
                <PMDatePicker
                  value={bulkEffectiveTo}
                  onChange={(date) => setBulkEffectiveTo(toLocalDateInputValue(date))}
                  placeholder='Optional'
                />
              </div>
              <Button
                type='button'
                size='sm'
                className='h-8'
                disabled={isSubmitting}
                onClick={handleBulkAssign}
              >
                Assign Profile
              </Button>
            </div>
          </div>
        )}
      </CardHeader>

      <CardContent className='p-0'>
        {filteredUsers.length === 0 ? (
          <div className='p-6 text-sm text-center text-muted-foreground'>
            No matching resources found.
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className='w-[40px] pl-4'>
                  <Checkbox checked={allSelected} onCheckedChange={handleSelectAll} />
                </TableHead>
                <TableHead>User</TableHead>
                <TableHead>Current Calendar Profile</TableHead>
                <TableHead>Effective Period</TableHead>
                <TableHead className='text-right pr-4'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.map((user) => {
                const assignment = assignments.find((a) => a.userId === user.id);
                const assignedProfile = profiles.find(
                  (p) => p.id === assignment?.profileId
                );

                return (
                  <TableRow key={user.id}>
                    <TableCell className='pl-4'>
                      <Checkbox
                        checked={selectedUserIds.includes(user.id)}
                        onCheckedChange={(checked) => handleSelectUser(user.id, !!checked)}
                      />
                    </TableCell>
                    <TableCell>
                      <div className='flex items-center gap-3.5'>
                        <div className='h-9 w-9 rounded-full bg-primary/10 border border-primary/20 text-primary flex items-center justify-center font-medium text-xs shadow-inner'>
                          {getUserInitials(user.name)}
                        </div>
                        <div className='flex flex-col'>
                          <span className='font-semibold text-sm leading-tight text-foreground'>
                            {user.name}
                          </span>
                          {user.email && (
                            <span className='text-xs text-muted-foreground mt-0.5'>
                              {user.email}
                            </span>
                          )}
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      {assignedProfile ? (
                        <Badge
                          variant='outline'
                          className='bg-primary/5 text-primary border-primary/20 font-medium px-2 py-0.5 rounded-full'
                        >
                          {assignedProfile.name}
                        </Badge>
                      ) : (
                        <Badge
                          variant='secondary'
                          className='bg-muted text-muted-foreground border-border/40 font-normal px-2 py-0.5 rounded-full'
                        >
                          Unassigned
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell className='text-sm text-muted-foreground'>
                      {assignment ? (
                        <span>
                          {assignment.effectiveFrom}{' '}
                          {assignment.effectiveTo ? `to ${assignment.effectiveTo}` : 'onwards'}
                        </span>
                      ) : (
                        '-'
                      )}
                    </TableCell>
                    <TableCell className='text-right pr-4'>
                      {assignment && (
                        <Button
                          type='button'
                          variant='ghost'
                          size='icon'
                          className='h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10 rounded-full'
                          title='Remove Calendar Assignment'
                          onClick={async () => {
                            setSelectedUserIds([user.id]);
                            // Wait briefly, then trigger remove
                            setTimeout(() => handleBulkRemove(), 50);
                          }}
                        >
                          <Trash2 className='h-4 w-4' />
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}
