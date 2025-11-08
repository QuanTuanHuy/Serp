/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Enhanced Dialog
 */

'use client';

import React, { useMemo, useState, useEffect } from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Badge,
} from '@/shared/components/ui';
import { Separator } from '@/shared/components/ui/separator';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Users, Building2, Info, UserPlus, ListChecks } from 'lucide-react';
import { useSettingsModules } from '../../hooks/useModules';
import { useSettingsUsers } from '../../hooks/useUsers';
import type {
  AccessibleModule,
  ModuleRole,
} from '../../types/module-access.types';
import type { UserProfile } from '@/modules/admin/types';
import { AssignUserForm } from './AssignUserForm';
import { ModuleUsersList } from './ModuleUsersList';

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
    assignStatus,
    revokeStatus,
    useModuleRoles,
    useModuleUsers,
  } = useSettingsModules();
  const moduleId = module?.moduleId as number | undefined;
  const { data: rolesData, isLoading: isLoadingRoles } =
    useModuleRoles(moduleId);
  const roles = (rolesData || []) as ModuleRole[];
  const {
    data: moduleUsersData,
    isLoading: isLoadingModuleUsers,
    refetch: refetchModuleUsers,
  } = useModuleUsers(moduleId);
  const moduleUsers = (moduleUsersData || []) as UserProfile[];
  const { users, setSearch, organizationId } = useSettingsUsers();
  const [activeTab, setActiveTab] = useState<'assign' | 'manage'>('assign');

  useEffect(() => {
    if (!open) {
      setActiveTab('assign');
    }
  }, [open]);

  const handleAssign = async (userId: number, roleId?: number) => {
    if (!moduleId) return;
    await assign(moduleId, userId, roleId);
    refetchModuleUsers();
    setActiveTab('manage');
  };

  const handleRevoke = async (userId: number) => {
    if (!moduleId) return;
    await revoke(moduleId, userId);
    refetchModuleUsers();
  };

  const handleSearchChange = (search: string) => {
    setSearch(search || undefined);
  };

  const existingUserIds = useMemo(
    () => new Set((moduleUsers || []).map((u) => u.id)),
    [moduleUsers]
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-4xl h-[90vh] flex flex-col p-0'>
        <DialogHeader className='px-6 pt-6 pb-4 shrink-0'>
          <DialogTitle className='flex items-center gap-2'>
            <Building2 className='h-5 w-5 text-purple-600' />
            Module Access Management
          </DialogTitle>
          <DialogDescription className='space-y-1'>
            <div className='flex items-center gap-2'>
              <span className='font-semibold text-foreground'>
                {module?.moduleName || 'Module'}
              </span>
              <Badge variant='secondary' className='text-xs'>
                {module?.moduleCode}
              </Badge>
            </div>
            {organizationId && (
              <div className='text-xs text-muted-foreground'>
                Organization ID: {organizationId}
              </div>
            )}
          </DialogDescription>
        </DialogHeader>

        <div className='flex-1 overflow-y-auto px-6'>
          <div className='flex flex-col gap-4 pb-4'>
            {/* Module Info Banner */}
            <Card className='border-purple-200 dark:border-purple-800 bg-purple-50/50 dark:bg-purple-950/20'>
              <CardContent className='p-4'>
                <div className='flex items-start gap-3'>
                  <Info className='h-5 w-5 text-purple-600 mt-0.5' />
                  <div className='flex-1 space-y-1'>
                    <p className='text-sm font-medium'>
                      Current Access: {moduleUsers.length} user
                      {moduleUsers.length !== 1 ? 's' : ''}
                    </p>
                    <p className='text-xs text-muted-foreground'>
                      {module?.moduleDescription ||
                        'Manage which users can access this module'}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Tabs for Assign and Manage */}
            <Tabs
              value={activeTab}
              onValueChange={(v) => setActiveTab(v as any)}
            >
              <TabsList className='grid w-full grid-cols-2'>
                <TabsTrigger value='assign' className='flex items-center gap-2'>
                  <UserPlus className='h-4 w-4' />
                  Assign Users
                </TabsTrigger>
                <TabsTrigger value='manage' className='flex items-center gap-2'>
                  <ListChecks className='h-4 w-4' />
                  Manage Access ({moduleUsers.length})
                </TabsTrigger>
              </TabsList>

              <TabsContent value='assign' className='mt-4'>
                <Card>
                  <CardHeader>
                    <CardTitle className='text-lg'>Assign New User</CardTitle>
                    <CardDescription>
                      Grant module access to users from your organization
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <AssignUserForm
                      users={users || []}
                      roles={roles}
                      existingUserIds={existingUserIds}
                      isLoadingRoles={isLoadingRoles}
                      isAssigning={assignStatus.isLoading}
                      onAssign={handleAssign}
                      onSearchChange={handleSearchChange}
                    />
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value='manage' className='mt-4'>
                <Card>
                  <CardHeader>
                    <CardTitle className='text-lg flex items-center gap-2'>
                      <Users className='h-5 w-5' />
                      Users with Module Access
                    </CardTitle>
                    <CardDescription>
                      View and manage users who currently have access to this
                      module
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ModuleUsersList
                      users={moduleUsers}
                      isLoading={isLoadingModuleUsers}
                      isRevoking={revokeStatus.isLoading}
                      onRevoke={handleRevoke}
                    />
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>
        </div>

        <Separator className='shrink-0' />
        <div className='flex justify-end gap-2 px-6 py-4 shrink-0 bg-background'>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Close
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
