/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Role Assignment Dialog for Menu Displays
 */

'use client';

import React, { useState, useMemo } from 'react';
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
import { Badge } from '@/shared/components/ui/badge';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Search, Loader2, ShieldCheck, X } from 'lucide-react';
import { useDebounce } from '@/shared/hooks/use-debounce';
import { useGetAllRolesQuery } from '../../services/adminApi';
import type { MenuDisplayDetail, Role } from '../../types';

interface RoleAssignmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  menuDisplay: MenuDisplayDetail | null;
  onUpdateRoles: (menuDisplayId: number, roleIds: number[]) => Promise<void>;
  isLoading?: boolean;
}

export const RoleAssignmentDialog: React.FC<RoleAssignmentDialogProps> = ({
  open,
  onOpenChange,
  menuDisplay,
  onUpdateRoles,
  isLoading = false,
}) => {
  const [searchInput, setSearchInput] = useState('');
  const debouncedSearch = useDebounce(searchInput, 300);
  const [selectedRoleIds, setSelectedRoleIds] = useState<Set<number>>(
    new Set()
  );
  const [processing, setProcessing] = useState(false);

  const { data: roles = [], isLoading: isLoadingRoles } = useGetAllRolesQuery();

  const assignedRoleIds = useMemo(() => {
    if (!menuDisplay?.assignedRoles) return new Set<number>();
    return new Set(menuDisplay.assignedRoles.map((r) => r.roleId));
  }, [menuDisplay]);

  const hasChanged = useMemo(() => {
    if (assignedRoleIds.size !== selectedRoleIds.size) return true;
    for (const id of selectedRoleIds) {
      if (!assignedRoleIds.has(id)) return true;
    }
    return false;
  }, [assignedRoleIds, selectedRoleIds]);

  const filteredRoles = useMemo(() => {
    if (!debouncedSearch) return roles;
    const searchLower = debouncedSearch.toLowerCase();
    return roles.filter(
      (role: Role) =>
        role.name?.toLowerCase().includes(searchLower) ||
        role.description?.toLowerCase().includes(searchLower)
    );
  }, [roles, debouncedSearch]);

  // Reset state when dialog opens/closes
  React.useEffect(() => {
    if (open) {
      if (menuDisplay?.assignedRoles) {
        setSelectedRoleIds(new Set(menuDisplay.assignedRoles.map((r) => r.roleId)));
      } else {
        setSelectedRoleIds(new Set());
      }
      setSearchInput('');
    }
  }, [open, menuDisplay]);

  const handleToggleRole = (roleId: number) => {
    setSelectedRoleIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(roleId)) {
        newSet.delete(roleId);
      } else {
        newSet.add(roleId);
      }
      return newSet;
    });
  };

  const handleApply = async () => {
    if (!menuDisplay?.id) return;

    setProcessing(true);
    try {
      await onUpdateRoles(menuDisplay.id, Array.from(selectedRoleIds));
      onOpenChange(false);
    } catch (error) {
      console.error('Role assignment error:', error);
    } finally {
      setProcessing(false);
    }
  };

  const getRoleStatus = (roleId: number): 'assigned' | 'unassigned' | 'new' | 'removed' => {
    const wasAssigned = assignedRoleIds.has(roleId);
    const isSelected = selectedRoleIds.has(roleId);

    if (wasAssigned && isSelected) return 'assigned';
    if (!wasAssigned && isSelected) return 'new';
    if (wasAssigned && !isSelected) return 'removed';
    return 'unassigned';
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!sm:max-w-2xl !max-w-4xl'>
        <DialogHeader>
          <DialogTitle>Assign Roles to Menu Display</DialogTitle>
          <DialogDescription>
            {menuDisplay && (
              <span>
                Manage role assignments for &ldquo;{menuDisplay.name}&rdquo;.
                Select roles to toggle their assignment.
              </span>
            )}
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          {/* Search Input */}
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
            <Input
              placeholder='Search roles by name, code, or description...'
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className='pl-10'
            />
          </div>

          {/* Selected Count */}
          {selectedRoleIds.size > 0 && (
            <div className='flex items-center justify-between px-3 py-2 bg-primary/10 rounded-md'>
              <span className='text-sm font-medium'>
                {selectedRoleIds.size} role
                {selectedRoleIds.size !== 1 ? 's' : ''} selected
              </span>
              <Button
                variant='ghost'
                size='sm'
                onClick={() => setSelectedRoleIds(new Set())}
              >
                Clear
              </Button>
            </div>
          )}

          {/* Roles List */}
          <ScrollArea className='h-[400px] rounded-md border'>
            {isLoadingRoles ? (
              <div className='flex items-center justify-center h-full'>
                <Loader2 className='h-6 w-6 animate-spin text-muted-foreground' />
              </div>
            ) : filteredRoles.length === 0 ? (
              <div className='flex flex-col items-center justify-center h-full text-center p-6'>
                <ShieldCheck className='h-12 w-12 text-muted-foreground mb-3' />
                <p className='text-sm font-medium'>No roles found</p>
                <p className='text-xs text-muted-foreground mt-1'>
                  {searchInput
                    ? 'Try adjusting your search criteria'
                    : 'No roles available'}
                </p>
              </div>
            ) : (
              <div className='p-4 space-y-2'>
                {filteredRoles.map((role: Role) => {
                  const status = getRoleStatus(role.id);
                  const isSelected = selectedRoleIds.has(role.id);

                  return (
                    <div
                      key={role.id}
                      className='flex items-start gap-3 p-3 rounded-md hover:bg-accent/50 transition-colors cursor-pointer'
                      onClick={() => handleToggleRole(role.id)}
                    >
                      <Checkbox
                        checked={isSelected}
                        onCheckedChange={() => handleToggleRole(role.id)}
                        className='mt-0.5'
                      />

                      <div className='flex-1 min-w-0'>
                        <div className='flex items-center gap-2 mb-1'>
                          <span className='font-medium text-sm'>
                            {role.name}
                          </span>
                          {status === 'assigned' && (
                            <Badge variant='secondary' className='text-xs'>
                              Assigned
                            </Badge>
                          )}
                          {status === 'new' && (
                            <Badge variant='default' className='text-xs bg-green-600 hover:bg-green-600 text-white'>
                              Will Assign
                            </Badge>
                          )}
                          {status === 'removed' && (
                            <Badge variant='destructive' className='text-xs'>
                              Will Remove
                            </Badge>
                          )}
                        </div>
                        <div className='flex items-center gap-2 text-xs text-muted-foreground'>
                          <span>{role.scope}</span>
                          {role.roleType && <span>• {role.roleType}</span>}
                        </div>
                        {role.description && (
                          <p className='text-xs text-muted-foreground mt-1 line-clamp-1'>
                            {role.description}
                          </p>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </ScrollArea>
        </div>

        <DialogFooter>
          <Button
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={processing || isLoading}
          >
            Cancel
          </Button>
          <Button
            onClick={handleApply}
            disabled={
              !hasChanged ||
              processing ||
              isLoading ||
              !menuDisplay
            }
          >
            {processing || isLoading ? (
              <>
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                Applying...
              </>
            ) : (
              `Apply Changes (${selectedRoleIds.size})`
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
