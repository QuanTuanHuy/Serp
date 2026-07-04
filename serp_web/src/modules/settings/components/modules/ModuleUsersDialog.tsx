/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Redesigned Module Access Management Dialog
 */

'use client';

import React, { useMemo, useState, useEffect } from 'react';
import { useDebounce } from '@/shared/hooks';
import { useNotification } from '@/shared/hooks/use-notification';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Badge,
  Checkbox,
  Input,
  Table,
  TableHeader,
  TableBody,
  TableHead,
  TableRow,
  TableCell,
  Popover,
  PopoverTrigger,
  PopoverContent,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from '@/shared/components/ui';
import { Separator } from '@/shared/components/ui/separator';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
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
import {
  Users,
  Building2,
  Info,
  Search,
  Loader2,
  Trash2,
  CheckCircle2,
  X,
  AlertTriangle,
} from 'lucide-react';
import { useSettingsModules } from '../../hooks/useModules';
import { useSettingsUsers } from '../../hooks/useUsers';
import type {
  AccessibleModule,
  ModuleRole,
} from '../../types/module-access.types';
import { getInitials } from '@/shared/utils';

export interface ModuleUsersDialogProps {
  open: boolean;
  module?: AccessibleModule;
  onOpenChange: (v: boolean) => void;
}

export function ModuleUsersDialog({
  open,
  module,
  onOpenChange,
}: ModuleUsersDialogProps) {
  const {
    assign,
    revoke,
    bulkAssign,
    bulkRevoke,
    assignStatus,
    revokeStatus,
    bulkAssignStatus,
    bulkRevokeStatus,
    useModuleRoles,
    useModuleUsers,
  } = useSettingsModules({ skipQuery: true });

  const moduleId = module?.moduleId as number | undefined;
  const { data: rolesData, isLoading: isLoadingRoles } = useModuleRoles(moduleId);
  const roles = (rolesData || []) as ModuleRole[];

  // Organization Users listing state
  const {
    users: orgUsers,
    pagination: orgPagination,
    isLoading: isLoadingOrgUsers,
    setSearch: setOrgSearch,
    setPage: setOrgPage,
    organizationId,
  } = useSettingsUsers();

  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearch = useDebounce(searchQuery, 300);

  // Tab filter: 'ALL' or 'GRANTED'
  const [filterTab, setFilterTab] = useState<'ALL' | 'GRANTED'>('ALL');

  // Multi-selection state
  const [selectedUserIds, setSelectedUserIds] = useState<Set<number>>(new Set());

  // Single role assign state for popovers
  const [assigningUserId, setAssigningUserId] = useState<number | null>(null);
  const [singleRoleId, setSingleRoleId] = useState<string>('');

  // Bulk role assign state
  const [bulkRoleId, setBulkRoleId] = useState<string>('');
  const [bulkAssignPopoverOpen, setBulkAssignPopoverOpen] = useState(false);

  // Confirm dialogs
  const [userToRevoke, setUserToRevoke] = useState<{ id: number; name: string } | null>(null);
  const [confirmBulkRevokeOpen, setConfirmBulkRevokeOpen] = useState(false);

  // Load all users who currently have access (large page size) to build the access Set
  const {
    data: allActiveUsersResponse,
    isLoading: isLoadingActiveUsers,
    refetch: refetchActiveUsers,
  } = useModuleUsers(moduleId, {
    page: 0,
    pageSize: 100,
  });

  const activeModuleUsers = useMemo(
    () => allActiveUsersResponse?.data?.items || [],
    [allActiveUsersResponse]
  );

  const hasAccessSet = useMemo(
    () => new Set(activeModuleUsers.map((u) => u.id)),
    [activeModuleUsers]
  );

  const moduleRoleNamesSet = useMemo(
    () => new Set(roles.map((r) => r.name)),
    [roles]
  );

  // Sync search and tab filter with useSettingsUsers hook
  useEffect(() => {
    if (open && organizationId) {
      setOrgSearch(debouncedSearch || undefined);
      setOrgPage(0);
    }
  }, [debouncedSearch, open, organizationId, setOrgSearch, setOrgPage]);

  const displayedUsers = useMemo(() => {
    if (filterTab === 'GRANTED') {
      return activeModuleUsers.filter((u) => {
        if (!debouncedSearch) return true;
        const q = debouncedSearch.toLowerCase();
        return (
          u.firstName?.toLowerCase().includes(q) ||
          u.lastName?.toLowerCase().includes(q) ||
          u.email?.toLowerCase().includes(q)
        );
      });
    }
    return orgUsers;
  }, [filterTab, orgUsers, activeModuleUsers, debouncedSearch]);

  // Reset selected state when Dialog closes
  useEffect(() => {
    if (!open) {
      setSelectedUserIds(new Set());
      setSearchQuery('');
      setFilterTab('ALL');
    }
  }, [open]);

  // Checkbox handlers
  const handleToggleUser = (userId: number) => {
    const next = new Set(selectedUserIds);
    if (next.has(userId)) {
      next.delete(userId);
    } else {
      next.add(userId);
    }
    setSelectedUserIds(next);
  };

  const isAllPageSelected = useMemo(() => {
    if (displayedUsers.length === 0) return false;
    return displayedUsers.every((u) => selectedUserIds.has(u.id));
  }, [displayedUsers, selectedUserIds]);

  const handleToggleAllPage = () => {
    const next = new Set(selectedUserIds);
    if (isAllPageSelected) {
      displayedUsers.forEach((u) => next.delete(u.id));
    } else {
      displayedUsers.forEach((u) => next.add(u.id));
    }
    setSelectedUserIds(next);
  };

  // Operations
  const handleSingleAssign = async (userId: number) => {
    if (!moduleId) return;
    const roleId = singleRoleId ? Number(singleRoleId) : undefined;
    try {
      await assign(moduleId, userId, roleId);
      refetchActiveUsers();
      setAssigningUserId(null);
      setSingleRoleId('');
    } catch (e) {
      // hook notifies error
    }
  };

  const handleSingleRevoke = async () => {
    if (!moduleId || !userToRevoke) return;
    try {
      await revoke(moduleId, userToRevoke.id);
      refetchActiveUsers();
      setUserToRevoke(null);
    } catch (e) {}
  };

  const handleBulkAssign = async () => {
    if (!moduleId || selectedUserIds.size === 0) return;
    const roleId = bulkRoleId ? Number(bulkRoleId) : undefined;
    try {
      await bulkAssign(moduleId, Array.from(selectedUserIds), roleId);
      refetchActiveUsers();
      setSelectedUserIds(new Set());
      setBulkRoleId('');
      setBulkAssignPopoverOpen(false);
    } catch (e) {}
  };

  const handleBulkRevoke = async () => {
    if (!moduleId || selectedUserIds.size === 0) return;
    try {
      await bulkRevoke(moduleId, Array.from(selectedUserIds));
      refetchActiveUsers();
      setSelectedUserIds(new Set());
      setConfirmBulkRevokeOpen(false);
    } catch (e) {}
  };

  const handleClearSelection = () => {
    setSelectedUserIds(new Set());
  };

  const isLoading = isLoadingOrgUsers || isLoadingActiveUsers || isLoadingRoles;
  const isMutating = assignStatus.isLoading || revokeStatus.isLoading || bulkAssignStatus.isLoading || bulkRevokeStatus.isLoading;

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className='!max-w-6xl h-[90vh] flex flex-col p-0 overflow-hidden'>
          <DialogHeader className='px-6 pt-6 pb-4 shrink-0 bg-background'>
            <div className='flex items-center justify-between'>
              <div className='space-y-1.5'>
                <DialogTitle className='flex items-center gap-2 text-xl font-bold'>
                  <Building2 className='h-6 w-6 text-purple-600' />
                  Module Access Management
                </DialogTitle>
                <DialogDescription asChild>
                  <div className='flex items-center gap-2 mt-1'>
                    <span className='font-semibold text-foreground text-sm'>
                      {module?.moduleName || 'Module'}
                    </span>
                    <Badge variant='secondary' className='text-xs bg-purple-50 text-purple-700 dark:bg-purple-950 dark:text-purple-300'>
                      {module?.moduleCode}
                    </Badge>
                    {organizationId && (
                      <span className='text-xs text-muted-foreground border-l pl-2 border-border'>
                        Org ID: {organizationId}
                      </span>
                    )}
                  </div>
                </DialogDescription>
              </div>
            </div>

            {/* Info stats card */}
            <div className='mt-4 p-4 border rounded-xl bg-purple-50/30 dark:bg-purple-950/10 border-purple-100 dark:border-purple-900/30 flex items-start gap-3'>
              <Info className='h-5 w-5 text-purple-600 shrink-0 mt-0.5' />
              <div className='space-y-1'>
                <p className='text-sm font-semibold text-purple-950 dark:text-purple-200'>
                  Active Subscribed Seats: {hasAccessSet.size} user{hasAccessSet.size !== 1 ? 's' : ''} assigned
                </p>
                <p className='text-xs text-muted-foreground'>
                  {module?.moduleDescription || 'Configure which organization members have permission to use this module.'}
                </p>
              </div>
            </div>
          </DialogHeader>

          {/* Table Search & Tab filters */}
          <div className='px-6 py-2 shrink-0 flex flex-col sm:flex-row gap-3 sm:items-center sm:justify-between border-b bg-background'>
            <div className='relative flex-1 max-w-sm'>
              <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
              <Input
                placeholder='Search by name or email...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='pl-9 h-9'
              />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery('')}
                  className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
                >
                  <X className='h-4 w-4' />
                </button>
              )}
            </div>

            {/* Segmented Filter Tab */}
            <div className='flex border rounded-lg p-0.5 bg-muted self-start sm:self-auto shrink-0'>
              <button
                onClick={() => setFilterTab('ALL')}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-all ${
                  filterTab === 'ALL'
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                All Members
              </button>
              <button
                onClick={() => setFilterTab('GRANTED')}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-all ${
                  filterTab === 'GRANTED'
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                Members with Access ({hasAccessSet.size})
              </button>
            </div>
          </div>

          {/* Unified Table view */}
          <div className='flex-1 overflow-hidden relative min-h-0 bg-background'>
            <ScrollArea className='h-full'>
              {isLoading ? (
                <div className='flex flex-col items-center justify-center h-64 gap-2'>
                  <Loader2 className='h-8 w-8 animate-spin text-purple-600' />
                  <span className='text-sm text-muted-foreground'>Loading members list...</span>
                </div>
              ) : displayedUsers.length === 0 ? (
                <div className='flex flex-col items-center justify-center h-64 text-center p-6'>
                  <Users className='h-12 w-12 text-muted-foreground mb-3' />
                  <h3 className='text-base font-semibold'>No members found</h3>
                  <p className='text-sm text-muted-foreground max-w-sm mt-1'>
                    Try modifying your search queries or change filter tabs.
                  </p>
                </div>
              ) : (
                <div className='px-6 pb-24'>
                  <Table>
                    <TableHeader className='sticky top-0 bg-background z-10'>
                      <TableRow>
                        <TableHead className='w-[50px]'>
                          <Checkbox
                            checked={isAllPageSelected}
                            onCheckedChange={handleToggleAllPage}
                            aria-label='Select all on page'
                          />
                        </TableHead>
                        <TableHead>Member</TableHead>
                        <TableHead>Access Status</TableHead>
                        <TableHead>Module Role</TableHead>
                        <TableHead>Org Status</TableHead>
                        <TableHead className='text-right'>Action</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {displayedUsers.map((user) => {
                        const hasAccess = hasAccessSet.has(user.id);
                        const isSelected = selectedUserIds.has(user.id);
                        
                        // Extract roles associated with this module
                        const matchedRoles =
                          user.roles?.filter((roleName) =>
                            moduleRoleNamesSet.has(roleName)
                          ) || [];

                        return (
                          <TableRow
                            key={user.id}
                            className={`hover:bg-muted/50 transition-colors ${
                              isSelected ? 'bg-purple-50/20 dark:bg-purple-950/5' : ''
                            }`}
                          >
                            <TableCell>
                              <Checkbox
                                checked={isSelected}
                                onCheckedChange={() => handleToggleUser(user.id)}
                                aria-label={`Select ${user.firstName}`}
                              />
                            </TableCell>
                            <TableCell>
                              <div className='flex items-center gap-3'>
                                <Avatar className='h-8 w-8'>
                                  <AvatarFallback className='text-xs'>
                                    {getInitials({
                                      firstName: user.firstName,
                                      lastName: user.lastName,
                                      email: user.email,
                                    })}
                                  </AvatarFallback>
                                </Avatar>
                                <div className='flex flex-col min-w-0'>
                                  <span className='text-sm font-semibold truncate'>
                                    {user.firstName} {user.lastName}
                                  </span>
                                  <span className='text-xs text-muted-foreground truncate'>
                                    {user.email}
                                  </span>
                                </div>
                              </div>
                            </TableCell>
                            <TableCell>
                              {hasAccess ? (
                                <Badge className='bg-green-100 text-green-700 hover:bg-green-100 dark:bg-green-900/30 dark:text-green-300 border-none font-medium text-xs px-2 py-0.5'>
                                  Active Access
                                </Badge>
                              ) : (
                                <Badge variant='secondary' className='text-xs font-medium text-muted-foreground bg-muted border-none px-2 py-0.5'>
                                  No Access
                                </Badge>
                              )}
                            </TableCell>
                            <TableCell>
                              {hasAccess && matchedRoles.length > 0 ? (
                                <div className='flex gap-1 flex-wrap'>
                                  {matchedRoles.map((roleName) => (
                                    <Badge
                                      key={roleName}
                                      variant='outline'
                                      className='border-purple-200 bg-purple-50/50 text-purple-700 dark:border-purple-800 dark:bg-purple-950/20 dark:text-purple-300 text-[10px] px-1.5 py-0 font-normal'
                                    >
                                      {roleName.replace('ROLE_', '').replace('_', ' ')}
                                    </Badge>
                                  ))}
                                </div>
                              ) : (
                                <span className='text-xs text-muted-foreground'>—</span>
                              )}
                            </TableCell>
                            <TableCell>
                              <Badge
                                variant={user.status === 'ACTIVE' ? 'default' : 'secondary'}
                                className='text-[10px] px-1.5 py-0 font-normal'
                              >
                                {user.status || 'ACTIVE'}
                              </Badge>
                            </TableCell>
                            <TableCell className='text-right'>
                              {hasAccess ? (
                                <Button
                                  variant='ghost'
                                  size='sm'
                                  disabled={isMutating}
                                  onClick={() =>
                                    setUserToRevoke({
                                      id: user.id,
                                      name: `${user.firstName} ${user.lastName}`,
                                    })
                                  }
                                  className='h-8 text-xs text-destructive hover:text-destructive hover:bg-destructive/10'
                                >
                                  <Trash2 className='h-3.5 w-3.5 mr-1.5' />
                                  Revoke
                                </Button>
                              ) : (
                                <Popover
                                  open={assigningUserId === user.id}
                                  onOpenChange={(op) => {
                                    if (op) {
                                      setAssigningUserId(user.id);
                                      setSingleRoleId('');
                                    } else {
                                      setAssigningUserId(null);
                                    }
                                  }}
                                >
                                  <PopoverTrigger asChild>
                                    <Button
                                      size='sm'
                                      variant='outline'
                                      disabled={isMutating}
                                      className='h-8 text-xs border-purple-200 text-purple-700 hover:bg-purple-50 dark:border-purple-900 dark:text-purple-300 dark:hover:bg-purple-950/30'
                                    >
                                      Assign
                                    </Button>
                                  </PopoverTrigger>
                                  <PopoverContent className='w-64 p-3 z-50' align='end'>
                                    <h4 className='text-xs font-semibold mb-2 text-foreground'>Select Role (Optional)</h4>
                                    <div className='space-y-2'>
                                      <Select value={singleRoleId || 'default'} onValueChange={(val) => setSingleRoleId(val === 'default' ? '' : val)}>
                                        <SelectTrigger className='w-full h-8 text-xs bg-background border border-input'>
                                          <SelectValue placeholder='Select role' />
                                        </SelectTrigger>
                                        <SelectContent>
                                          <SelectItem value='default'>Default Permissions</SelectItem>
                                          {roles.map((r) => (
                                            <SelectItem key={r.id} value={String(r.id)}>
                                              {r.name}
                                            </SelectItem>
                                          ))}
                                        </SelectContent>
                                      </Select>
                                      <Button
                                        size='sm'
                                        className='w-full text-xs bg-purple-600 hover:bg-purple-700 text-white'
                                        onClick={() => handleSingleAssign(user.id)}
                                        disabled={assignStatus.isLoading}
                                      >
                                        {assignStatus.isLoading ? (
                                          <Loader2 className='h-3 w-3 animate-spin mr-1' />
                                        ) : null}
                                        Grant Access
                                      </Button>
                                    </div>
                                  </PopoverContent>
                                </Popover>
                              )}
                            </TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                </div>
              )}
            </ScrollArea>

            {/* Floating Bulk Action Bar */}
            {selectedUserIds.size > 0 && (
              <div className='absolute bottom-6 left-1/2 -translate-x-1/2 z-20 w-11/12 max-w-2xl bg-background/80 dark:bg-background/90 backdrop-blur-md border border-purple-200 dark:border-purple-800 shadow-xl rounded-full px-5 py-3 flex items-center justify-between transition-all duration-300 animate-in slide-in-from-bottom-5'>
                <div className='flex items-center gap-2'>
                  <CheckCircle2 className='h-5 w-5 text-purple-600 shrink-0' />
                  <span className='text-xs md:text-sm font-semibold'>
                    Selected <strong className='text-purple-600 font-bold'>{selectedUserIds.size}</strong> member{selectedUserIds.size !== 1 ? 's' : ''}
                  </span>
                </div>

                <div className='flex items-center gap-2'>
                  {/* Bulk Assign Popover */}
                  <Popover open={bulkAssignPopoverOpen} onOpenChange={setBulkAssignPopoverOpen}>
                    <PopoverTrigger asChild>
                      <Button
                        size='sm'
                        className='bg-purple-600 hover:bg-purple-700 text-white text-xs rounded-full h-8 px-4'
                        disabled={isMutating}
                      >
                        Assign Access
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className='w-64 p-3 z-50' align='end'>
                      <h4 className='text-xs font-semibold mb-2 text-foreground'>Select Role for Group</h4>
                      <div className='space-y-2'>
                        <Select value={bulkRoleId || 'default'} onValueChange={(val) => setBulkRoleId(val === 'default' ? '' : val)}>
                          <SelectTrigger className='w-full h-8 text-xs bg-background border border-input'>
                            <SelectValue placeholder='Select role' />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value='default'>Default Permissions</SelectItem>
                            {roles.map((r) => (
                              <SelectItem key={r.id} value={String(r.id)}>
                                {r.name}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <Button
                          size='sm'
                          className='w-full text-xs bg-purple-600 hover:bg-purple-700 text-white'
                          onClick={handleBulkAssign}
                          disabled={bulkAssignStatus.isLoading}
                        >
                          {bulkAssignStatus.isLoading ? (
                            <Loader2 className='h-3 w-3 animate-spin mr-1' />
                          ) : null}
                          Grant Bulk Access
                        </Button>
                      </div>
                    </PopoverContent>
                  </Popover>

                  {/* Bulk Revoke Action */}
                  <Button
                    size='sm'
                    variant='outline'
                    onClick={() => setConfirmBulkRevokeOpen(true)}
                    className='text-destructive border-destructive/20 hover:bg-destructive/10 text-xs rounded-full h-8 px-4'
                    disabled={isMutating}
                  >
                    Revoke Access
                  </Button>

                  <Separator orientation='vertical' className='h-6' />

                  <Button
                    variant='ghost'
                    size='icon'
                    onClick={handleClearSelection}
                    className='h-8 w-8 hover:bg-muted rounded-full shrink-0 text-muted-foreground hover:text-foreground'
                  >
                    <X className='h-4 w-4' />
                  </Button>
                </div>
              </div>
            )}
          </div>

          <Separator className='shrink-0' />
          
          {/* Footer with Pagination */}
          <div className='flex items-center justify-between px-6 py-4 shrink-0 bg-background border-t'>
            {filterTab === 'ALL' && orgPagination.totalPages > 1 ? (
              <div className='flex items-center justify-between w-full'>
                <span className='text-xs text-muted-foreground'>
                  Showing Page {orgPagination.currentPage + 1} of {orgPagination.totalPages || 1} ({orgPagination.totalItems} members total)
                </span>
                <div className='flex items-center gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={orgPagination.currentPage === 0 || isLoading}
                    onClick={() => setOrgPage(orgPagination.currentPage - 1)}
                  >
                    Previous
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={orgPagination.currentPage >= orgPagination.totalPages - 1 || isLoading}
                    onClick={() => setOrgPage(orgPagination.currentPage + 1)}
                  >
                    Next
                  </Button>
                </div>
              </div>
            ) : (
              <div className='flex items-center justify-between w-full'>
                <span className='text-xs text-muted-foreground'>
                  {displayedUsers.length} member{displayedUsers.length !== 1 ? 's' : ''} displayed
                </span>
                <Button variant='outline' size='sm' onClick={() => onOpenChange(false)}>
                  Close
                </Button>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* Single Revoke Confirmation Alert Dialog */}
      <AlertDialog
        open={!!userToRevoke}
        onOpenChange={(op) => !op && setUserToRevoke(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className='flex items-center gap-2 text-destructive'>
              <AlertTriangle className='h-5 w-5 shrink-0' />
              Revoke Module Access
            </AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to revoke module access for{' '}
              <strong className='font-semibold text-foreground'>
                {userToRevoke?.name}
              </strong>
              ?
              <br />
              <br />
              This user will no longer be able to open or use this module.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              className='bg-destructive text-white hover:bg-destructive/90'
              onClick={handleSingleRevoke}
            >
              Confirm Revoke
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Bulk Revoke Confirmation Alert Dialog */}
      <AlertDialog
        open={confirmBulkRevokeOpen}
        onOpenChange={setConfirmBulkRevokeOpen}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className='flex items-center gap-2 text-destructive'>
              <AlertTriangle className='h-5 w-5 shrink-0' />
              Bulk Revoke Module Access
            </AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to revoke module access for all{' '}
              <strong className='font-bold text-foreground'>
                {selectedUserIds.size}
              </strong>{' '}
              selected members?
              <br />
              <br />
              These members will lose access immediately.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              className='bg-destructive text-white hover:bg-destructive/90'
              onClick={handleBulkRevoke}
            >
              Confirm Bulk Revoke
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
