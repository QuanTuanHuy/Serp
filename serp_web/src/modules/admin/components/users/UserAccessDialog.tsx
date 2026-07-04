'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User access dialog
 */

import { useEffect, useMemo, useState } from 'react';
import {
  Badge,
  Button,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  ScrollArea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Separator,
  Skeleton,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Switch,
} from '@/shared/components/ui';
import {
  useGetUserDetailQuery,
  useGetOrganizationRolesQuery,
  useUpdateUserRolesMutation,
  useUpdateUserTypeMutation,
} from '@/modules/admin/services/users/usersApi';
import {
  useGetAccessibleModulesForOrganizationQuery,
  useAssignUserToModuleMutation,
  useRevokeUserAccessToModuleMutation,
  useGetModuleRolesQuery,
} from '@/modules/settings/services/modules/modulesApi';
import type { Role, UserType } from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';
import { AdminStatusBadge } from '@/modules/admin/components/shared/AdminStatusBadge';
import { Puzzle, Shield, Loader2 } from 'lucide-react';
import { MODULE_ICONS } from '@/shared/constants/moduleIcons';

interface UserAccessDialogProps {
  open: boolean;
  organizationId?: number;
  userId?: number;
  onOpenChange: (open: boolean) => void;
}

const userTypeOptions: Array<{ value: UserType; label: string }> = [
  { value: 'OWNER', label: 'Owner' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'CONTRACTOR', label: 'Contractor' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GUEST', label: 'Guest' },
];

interface ModuleAccessRowProps {
  module: any;
  isEnabled: boolean;
  onToggle: (moduleId: number) => void;
  selectedRoleId?: number;
  onRoleChange: (moduleId: number, roleId: number) => void;
  userRoles: number[];
}

function ModuleAccessRow({
  module,
  isEnabled,
  onToggle,
  selectedRoleId,
  onRoleChange,
  userRoles,
}: ModuleAccessRowProps) {
  const moduleId = module.moduleId;
  const { data: rolesResponse, isLoading: isLoadingRoles } =
    useGetModuleRolesQuery(moduleId ?? 0, { skip: !isEnabled || !moduleId });
  const roles = rolesResponse ?? [];

  // Automatically select the active role if the user already has one of the module's roles
  useEffect(() => {
    if (isEnabled && roles.length > 0 && !selectedRoleId) {
      const matchingRole = userRoles.find((roleId) =>
        roles.some((r) => r.id === roleId)
      );
      if (matchingRole) {
        onRoleChange(moduleId, matchingRole);
      } else {
        const defaultRole = roles.find((r) => r.isDefault) || roles[0];
        if (defaultRole) {
          onRoleChange(moduleId, defaultRole.id);
        }
      }
    }
  }, [isEnabled, roles, userRoles, selectedRoleId, onRoleChange, moduleId]);

  const moduleIconConfig =
    MODULE_ICONS[module.moduleCode as keyof typeof MODULE_ICONS];
  const IconComponent = moduleIconConfig?.icon || Puzzle;
  const iconColor = moduleIconConfig?.color || 'text-gray-600';
  const iconBg = moduleIconConfig?.bgColor || 'bg-gray-50';

  return (
    <div className='flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-lg border bg-card hover:bg-muted/10 transition-colors'>
      <div className='flex items-center gap-3 min-w-0 w-full sm:w-auto'>
        <div
          className={`h-10 w-10 rounded-lg flex items-center justify-center flex-shrink-0 ${iconBg} ${iconColor}`}
        >
          <IconComponent className='h-5 w-5' />
        </div>
        <div className='min-w-0 flex-1'>
          <div className='flex items-center gap-2'>
            <span className='font-semibold text-sm truncate'>
              {module.moduleName || module.moduleCode}
            </span>
            <Badge
              variant='outline'
              className='text-[10px] uppercase font-mono'
            >
              {module.moduleCode}
            </Badge>
          </div>
          <p className='text-xs text-muted-foreground truncate mt-0.5'>
            {module.moduleDescription || 'No description provided.'}
          </p>
        </div>
      </div>

      <div className='flex items-center justify-between sm:justify-end gap-3 w-full sm:w-auto shrink-0 border-t pt-3 sm:border-t-0 sm:pt-0'>
        {isEnabled && (
          <div className='flex-1 sm:w-48 sm:flex-initial'>
            <Select
              value={selectedRoleId ? String(selectedRoleId) : ''}
              onValueChange={(val) => onRoleChange(moduleId, Number(val))}
              disabled={isLoadingRoles || roles.length === 0}
            >
              <SelectTrigger className='h-8 text-xs'>
                <SelectValue
                  placeholder={
                    isLoadingRoles ? 'Loading roles...' : 'Select role'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {roles.map((r) => (
                  <SelectItem
                    key={r.id}
                    value={String(r.id)}
                    className='text-xs'
                  >
                    <div className='flex items-center gap-1.5'>
                      <Shield className='h-3 w-3 text-purple-500' />
                      <span>{r.name}</span>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}

        <Switch
          checked={isEnabled}
          onCheckedChange={() => onToggle(moduleId)}
          className='data-[state=checked]:bg-purple-600 ml-auto sm:ml-0'
        />
      </div>
    </div>
  );
}

export function UserAccessDialog({
  open,
  organizationId,
  userId,
  onOpenChange,
}: UserAccessDialogProps) {
  const { success, error: showError } = useNotification();
  const { data: detailResponse, isLoading: isLoadingDetail } =
    useGetUserDetailQuery(
      { organizationId: organizationId ?? 0, userId: userId ?? 0 },
      { skip: !open || !organizationId || !userId }
    );
  const { data: rolesResponse, isLoading: isLoadingRoles } =
    useGetOrganizationRolesQuery(organizationId ?? 0, {
      skip: !open || !organizationId,
    });

  const [updateUserRoles, updateRolesState] = useUpdateUserRolesMutation();
  const [updateUserType, updateTypeState] = useUpdateUserTypeMutation();

  const { data: orgModulesResponse, isLoading: isLoadingOrgModules } =
    useGetAccessibleModulesForOrganizationQuery(organizationId ?? 0, {
      skip: !open || !organizationId,
    });
  const orgModules = orgModulesResponse ?? [];

  const [assignUserToModule, assignState] = useAssignUserToModuleMutation();
  const [revokeUserAccessToModule, revokeState] =
    useRevokeUserAccessToModuleMutation();

  const user = detailResponse?.data;
  const availableRoles = (rolesResponse?.data ?? []) as Role[];

  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [selectedUserType, setSelectedUserType] =
    useState<UserType>('EMPLOYEE');

  const [selectedModuleIds, setSelectedModuleIds] = useState<Set<number>>(
    new Set()
  );
  const [selectedModuleRoles, setSelectedModuleRoles] = useState<
    Map<number, number>
  >(new Map());
  const [activeTab, setActiveTab] = useState<'roles' | 'modules'>('roles');

  useEffect(() => {
    if (user) {
      setSelectedUserType(user.userType);
      setSelectedRoleIds(user.roles.map((role) => role.id));

      const activeIds = new Set<number>();
      const rolesMap = new Map<number, number>();

      user.moduleAccesses.forEach((ma) => {
        if (ma.isActive) {
          activeIds.add(ma.moduleId);
        }
      });
      setSelectedModuleIds(activeIds);
      setSelectedModuleRoles(rolesMap);
    }
  }, [user]);

  const toggleRole = (roleId: number) => {
    setSelectedRoleIds((current) =>
      current.includes(roleId)
        ? current.filter((id) => id !== roleId)
        : [...current, roleId]
    );
  };

  const handleSave = async () => {
    if (!organizationId || !userId || !user) {
      return;
    }

    try {
      const savePromises: Promise<any>[] = [];

      // 1. User Type update
      if (selectedUserType !== user.userType) {
        savePromises.push(
          updateUserType({
            organizationId,
            userId,
            body: { userType: selectedUserType },
          }).unwrap()
        );
      }

      // 2. Roles update
      const originalRoleIds = user.roles.map((role) => role.id).sort();
      const nextRoleIds = [...selectedRoleIds].sort();
      const rolesChanged =
        originalRoleIds.length !== nextRoleIds.length ||
        originalRoleIds.some((roleId, index) => roleId !== nextRoleIds[index]);

      if (rolesChanged) {
        savePromises.push(
          updateUserRoles({
            organizationId,
            userId,
            body: { roleIds: selectedRoleIds },
          }).unwrap()
        );
      }

      // 3. Module Access diff detection
      const originalActiveModuleAccesses = user.moduleAccesses.filter(
        (ma) => ma.isActive
      );
      const originalActiveModuleIds = originalActiveModuleAccesses.map(
        (ma) => ma.moduleId
      );

      const modulesToRevoke = originalActiveModuleIds.filter(
        (id) => !selectedModuleIds.has(id)
      );
      const modulesToAssign = Array.from(selectedModuleIds).filter(
        (id) => !originalActiveModuleIds.includes(id)
      );

      // Module role change detection
      const modulesWithRoleChanged: number[] = [];
      Array.from(selectedModuleIds).forEach((moduleId) => {
        if (originalActiveModuleIds.includes(moduleId)) {
          const newRoleId = selectedModuleRoles.get(moduleId);
          if (newRoleId && !user.roles.some((r) => r.id === newRoleId)) {
            modulesWithRoleChanged.push(moduleId);
          }
        }
      });

      // 4. Populate revoke promises
      modulesToRevoke.forEach((moduleId) => {
        savePromises.push(
          revokeUserAccessToModule({
            organizationId,
            moduleId,
            userId,
          }).unwrap()
        );
      });

      // 5. Populate assign promises
      modulesToAssign.forEach((moduleId) => {
        const roleId = selectedModuleRoles.get(moduleId);
        savePromises.push(
          assignUserToModule({
            organizationId,
            moduleId,
            userId,
            roleId,
          }).unwrap()
        );
      });

      // 6. Populate role change promises (revoke then assign sequentially)
      modulesWithRoleChanged.forEach((moduleId) => {
        const roleId = selectedModuleRoles.get(moduleId);
        const changePromise = revokeUserAccessToModule({
          organizationId,
          moduleId,
          userId,
        })
          .unwrap()
          .then(() =>
            assignUserToModule({
              organizationId,
              moduleId,
              userId,
              roleId,
            }).unwrap()
          );
        savePromises.push(changePromise);
      });

      // Execute all mutations in parallel
      await Promise.all(savePromises);

      success('User access updated successfully.');
      onOpenChange(false);
    } catch (error) {
      showError(getErrorMessage(error));
    }
  };

  const isLoading = isLoadingDetail || isLoadingRoles;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[85vh] w-[95vw] sm:max-w-4xl overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Manage access</DialogTitle>
          <DialogDescription>
            Update user type and role assignments for the selected organization.
          </DialogDescription>
        </DialogHeader>

        {isLoading || !user ? (
          <div className='space-y-4'>
            <Skeleton className='h-16 w-full' />
            <Skeleton className='h-72 w-full' />
          </div>
        ) : (
          <div className='space-y-5'>
            <section className='flex items-center justify-between gap-4 rounded-lg border p-4'>
              <div className='min-w-0'>
                <p className='truncate text-sm font-medium'>
                  {user.firstName} {user.lastName}
                </p>
                <p className='truncate text-sm text-muted-foreground'>
                  {user.email}
                </p>
              </div>
              <div className='flex items-center gap-2'>
                <AdminStatusBadge status={user.status} />
                <Badge variant='outline'>{user.userType}</Badge>
              </div>
            </section>

            <Tabs
              value={activeTab}
              onValueChange={(val) => setActiveTab(val as any)}
              className='w-full'
            >
              <TabsList className='grid w-full grid-cols-2 mb-4'>
                <TabsTrigger value='roles'>General Roles</TabsTrigger>
                <TabsTrigger value='modules'>Module Access</TabsTrigger>
              </TabsList>

              <TabsContent
                value='roles'
                className='space-y-5 focus-visible:ring-0 focus-visible:ring-offset-0 mt-0'
              >
                <section className='space-y-3'>
                  <div className='grid gap-2 sm:max-w-xs'>
                    <Label>User type</Label>
                    <Select
                      value={selectedUserType}
                      onValueChange={(value) =>
                        setSelectedUserType(value as UserType)
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {userTypeOptions.map((item) => (
                          <SelectItem key={item.value} value={item.value}>
                            {item.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </section>

                <Separator />

                <section className='space-y-3'>
                  <div>
                    <h3 className='text-sm font-semibold'>Roles</h3>
                    <p className='text-sm text-muted-foreground'>
                      Choose the roles assigned to this user.
                    </p>
                  </div>
                  <ScrollArea className='h-64 rounded-md border'>
                    <div className='space-y-1 p-2'>
                      {availableRoles.length > 0 ? (
                        availableRoles.map((role) => (
                          <label
                            key={role.id}
                            className='flex items-start gap-2 rounded-md px-2 py-2 text-sm hover:bg-muted/50'
                          >
                            <Checkbox
                              checked={selectedRoleIds.includes(role.id)}
                              onCheckedChange={() => toggleRole(role.id)}
                              className='mt-0.5'
                            />
                            <div className='min-w-0 flex-1'>
                              <div className='flex items-center gap-2'>
                                <span className='font-medium'>{role.name}</span>
                                <Badge
                                  variant='outline'
                                  className='text-[10px]'
                                >
                                  {role.scope}
                                </Badge>
                              </div>
                              {role.description ? (
                                <p className='truncate text-xs text-muted-foreground'>
                                  {role.description}
                                </p>
                              ) : null}
                            </div>
                          </label>
                        ))
                      ) : (
                        <p className='p-3 text-sm text-muted-foreground'>
                          No roles available for this organization.
                        </p>
                      )}
                    </div>
                  </ScrollArea>
                </section>

                <section className='space-y-3'>
                  <div>
                    <h3 className='text-sm font-semibold'>Departments</h3>
                    <p className='text-sm text-muted-foreground'>
                      Current department membership is shown in the details
                      drawer.
                    </p>
                  </div>
                  <div className='flex flex-wrap gap-2'>
                    {user.departments.length > 0 ? (
                      user.departments.map((department) => (
                        <Badge key={department.id} variant='secondary'>
                          {department.name || `Department #${department.id}`}
                        </Badge>
                      ))
                    ) : (
                      <p className='text-sm text-muted-foreground'>
                        No department membership.
                      </p>
                    )}
                  </div>
                </section>
              </TabsContent>

              <TabsContent
                value='modules'
                className='space-y-4 focus-visible:ring-0 focus-visible:ring-offset-0 mt-0'
              >
                <div>
                  <h3 className='text-sm font-semibold'>Module Access</h3>
                  <p className='text-sm text-muted-foreground'>
                    Configure which modules this user can access and their
                    specific roles.
                  </p>
                </div>

                {isLoadingOrgModules ? (
                  <div className='space-y-3'>
                    <Skeleton className='h-16 w-full' />
                    <Skeleton className='h-16 w-full' />
                  </div>
                ) : orgModules.filter((m) => m.isActive).length > 0 ? (
                  <div className='space-y-2 max-h-[380px] overflow-y-auto pr-1'>
                    {orgModules
                      .filter((m) => m.isActive)
                      .map((module) => (
                        <ModuleAccessRow
                          key={module.moduleId ?? module.moduleCode}
                          module={module}
                          isEnabled={selectedModuleIds.has(
                            module.moduleId ?? 0
                          )}
                          onToggle={(id) => {
                            setSelectedModuleIds((prev) => {
                              const next = new Set(prev);
                              if (next.has(id)) {
                                next.delete(id);
                              } else {
                                next.add(id);
                              }
                              return next;
                            });
                          }}
                          selectedRoleId={selectedModuleRoles.get(
                            module.moduleId ?? 0
                          )}
                          onRoleChange={(moduleId, roleId) => {
                            setSelectedModuleRoles((prev) => {
                              const next = new Map(prev);
                              next.set(moduleId, roleId);
                              return next;
                            });
                          }}
                          userRoles={selectedRoleIds}
                        />
                      ))}
                  </div>
                ) : (
                  <p className='text-sm text-muted-foreground py-4 text-center'>
                    No active modules in this organization.
                  </p>
                )}
              </TabsContent>
            </Tabs>
          </div>
        )}

        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button
            type='button'
            onClick={handleSave}
            disabled={
              updateRolesState.isLoading ||
              updateTypeState.isLoading ||
              assignState.isLoading ||
              revokeState.isLoading
            }
          >
            {updateRolesState.isLoading ||
            updateTypeState.isLoading ||
            assignState.isLoading ||
            revokeState.isLoading ? (
              <>
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                Saving...
              </>
            ) : (
              'Save'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
