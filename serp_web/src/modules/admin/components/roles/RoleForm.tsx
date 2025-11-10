/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Role Form Component
 */

'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Button,
  Input,
  Label,
  Card,
  CardContent,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { Loader2 } from 'lucide-react';
import type { Role, RoleScope, RoleType } from '../../types';
import {
  useGetModulesQuery,
  useGetAllRolesQuery,
} from '../../services/adminApi';

// Validation schema
const roleFormSchema = z.object({
  name: z
    .string()
    .min(1, 'Role name is required')
    .max(100, 'Role name must not exceed 100 characters'),
  description: z.string().optional(),
  scope: z.enum(['SYSTEM', 'ORGANIZATION', 'MODULE', 'DEPARTMENT']),
  roleType: z.enum(['OWNER', 'ADMIN', 'MANAGER', 'USER', 'VIEWER', 'CUSTOM']),
  priority: z.string().min(1, 'Priority is required'),
  isRealmRole: z.boolean(),
  keycloakClientId: z.string().optional(),
  isDefault: z.boolean(),
  organizationId: z.string().optional(),
  moduleId: z.string().optional(),
  departmentId: z.string().optional(),
  parentRoleId: z.string().optional(),
});

type RoleFormData = z.infer<typeof roleFormSchema>;

interface RoleFormProps {
  role?: Role;
  onSubmit: (data: any) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const RoleForm: React.FC<RoleFormProps> = ({
  role,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!role;

  const { data: modules = [] } = useGetModulesQuery();
  const { data: allRoles = [] } = useGetAllRolesQuery();

  const availableParentRoles: Role[] = isEditing
    ? allRoles.filter((r) => r.id !== role?.id)
    : allRoles;

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RoleFormData>({
    resolver: zodResolver(roleFormSchema),
    defaultValues: role
      ? {
          name: role.name,
          description: role.description || '',
          scope: role.scope,
          roleType: role.roleType,
          priority: String(role.priority),
          isRealmRole: role.isRealmRole,
          keycloakClientId: role.keycloakClientId || '',
          isDefault: role.isDefault,
          organizationId: role.organizationId
            ? String(role.organizationId)
            : '',
          moduleId: role.moduleId ? String(role.moduleId) : '',
          departmentId: role.departmentId ? String(role.departmentId) : '',
          parentRoleId: role.parentRoleId ? String(role.parentRoleId) : '',
        }
      : {
          name: '',
          description: '',
          scope: 'ORGANIZATION' as RoleScope,
          roleType: 'USER' as RoleType,
          priority: '100',
          isRealmRole: false,
          keycloakClientId: '',
          isDefault: false,
          organizationId: '',
          moduleId: '',
          departmentId: '',
          parentRoleId: '',
        },
  });

  const scope = watch('scope');
  const isRealmRole = watch('isRealmRole');

  const handleFormSubmit = handleSubmit(async (data) => {
    try {
      const transformedData = {
        name: data.name,
        description: data.description || undefined,
        scope: data.scope,
        roleType: data.roleType,
        priority: Number(data.priority) || 100,
        isRealmRole: data.isRealmRole,
        keycloakClientId: data.keycloakClientId || undefined,
        isDefault: data.isDefault,
        organizationId:
          data.organizationId && data.organizationId !== ''
            ? Number(data.organizationId)
            : undefined,
        moduleId:
          data.moduleId && data.moduleId !== ''
            ? Number(data.moduleId)
            : undefined,
        departmentId:
          data.departmentId && data.departmentId !== ''
            ? Number(data.departmentId)
            : undefined,
        parentRoleId:
          data.parentRoleId && data.parentRoleId !== ''
            ? Number(data.parentRoleId)
            : undefined,
      };

      await onSubmit(transformedData);
    } catch (error) {
      console.error('Form submission error:', error);
    }
  });

  return (
    <form onSubmit={handleFormSubmit} className={cn('space-y-6', className)}>
      {/* Basic Information */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Basic Information</h3>

          <div className='space-y-2'>
            <Label htmlFor='name'>
              Role Name <span className='text-destructive'>*</span>
            </Label>
            <Input
              id='name'
              placeholder='e.g. Sales Manager'
              {...register('name')}
              disabled={isLoading || isSubmitting}
              className={cn(errors.name && 'border-destructive')}
            />
            {errors.name && (
              <p className='text-sm text-destructive'>{errors.name.message}</p>
            )}
          </div>

          <div className='space-y-2'>
            <Label htmlFor='description'>Description</Label>
            <textarea
              id='description'
              placeholder='Describe the role responsibilities and permissions...'
              {...register('description')}
              disabled={isLoading || isSubmitting}
              className={cn(
                'flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
                errors.description && 'border-destructive'
              )}
              rows={3}
            />
            {errors.description && (
              <p className='text-sm text-destructive'>
                {errors.description.message}
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Role Configuration */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Role Configuration</h3>

          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='scope'>
                Scope <span className='text-destructive'>*</span>
              </Label>
              <select
                id='scope'
                {...register('scope')}
                disabled={isLoading || isSubmitting || isEditing}
                className={cn(
                  'w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm',
                  errors.scope && 'border-destructive'
                )}
              >
                <option value='SYSTEM'>System</option>
                <option value='ORGANIZATION'>Organization</option>
                <option value='MODULE'>Module</option>
                <option value='DEPARTMENT'>Department</option>
              </select>
              {errors.scope && (
                <p className='text-sm text-destructive'>
                  {errors.scope.message}
                </p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='roleType'>
                Role Type <span className='text-destructive'>*</span>
              </Label>
              <select
                id='roleType'
                {...register('roleType')}
                disabled={isLoading || isSubmitting}
                className={cn(
                  'w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm',
                  errors.roleType && 'border-destructive'
                )}
              >
                <option value='OWNER'>Owner (Full Control)</option>
                <option value='ADMIN'>Admin (Almost Full Control)</option>
                <option value='MANAGER'>Manager (Department/Module)</option>
                <option value='USER'>User (Regular User)</option>
                <option value='VIEWER'>Viewer (Read Only)</option>
                <option value='CUSTOM'>Custom</option>
              </select>
              {errors.roleType && (
                <p className='text-sm text-destructive'>
                  {errors.roleType.message}
                </p>
              )}
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='priority'>
              Priority <span className='text-destructive'>*</span>
            </Label>
            <Input
              id='priority'
              type='number'
              min='1'
              max='1000'
              placeholder='100'
              {...register('priority')}
              disabled={isLoading || isSubmitting}
              className={cn(errors.priority && 'border-destructive')}
            />
            <p className='text-xs text-muted-foreground'>
              Lower number = higher priority (1-1000)
            </p>
            {errors.priority && (
              <p className='text-sm text-destructive'>
                {errors.priority.message}
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Scope Context */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Scope Context</h3>
          <p className='text-sm text-muted-foreground mb-4'>
            Set the context IDs based on the selected scope
          </p>

          <div className='grid gap-4 md:grid-cols-2'>
            {(scope === 'ORGANIZATION' || scope === 'DEPARTMENT') && (
              <div className='space-y-2'>
                <Label htmlFor='organizationId'>Organization ID</Label>
                <Input
                  id='organizationId'
                  type='number'
                  min='1'
                  placeholder='Leave empty for all organizations'
                  {...register('organizationId')}
                  disabled={isLoading || isSubmitting}
                  className={cn(errors.organizationId && 'border-destructive')}
                />
                <p className='text-xs text-muted-foreground'>
                  {scope === 'ORGANIZATION'
                    ? 'Required for organization-scoped roles'
                    : 'Optional'}
                </p>
                {errors.organizationId && (
                  <p className='text-sm text-destructive'>
                    {errors.organizationId.message}
                  </p>
                )}
              </div>
            )}

            {scope === 'MODULE' && (
              <div className='space-y-2'>
                <Label htmlFor='moduleId'>Module</Label>
                <select
                  id='moduleId'
                  {...register('moduleId')}
                  disabled={isLoading || isSubmitting}
                  className={cn(
                    'w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm',
                    errors.moduleId && 'border-destructive'
                  )}
                >
                  <option value=''>Select a module (optional)</option>
                  {modules.map((module) => (
                    <option key={module.id} value={module.id}>
                      {module.moduleName} ({module.code})
                    </option>
                  ))}
                </select>
                <p className='text-xs text-muted-foreground'>
                  Select module for module-scoped roles
                </p>
                {errors.moduleId && (
                  <p className='text-sm text-destructive'>
                    {errors.moduleId.message}
                  </p>
                )}
              </div>
            )}

            {scope === 'DEPARTMENT' && (
              <div className='space-y-2'>
                <Label htmlFor='departmentId'>Department ID</Label>
                <Input
                  id='departmentId'
                  type='number'
                  min='1'
                  placeholder='Leave empty for all departments'
                  {...register('departmentId')}
                  disabled={isLoading || isSubmitting}
                  className={cn(errors.departmentId && 'border-destructive')}
                />
                <p className='text-xs text-muted-foreground'>
                  Required for department-scoped roles
                </p>
                {errors.departmentId && (
                  <p className='text-sm text-destructive'>
                    {errors.departmentId.message}
                  </p>
                )}
              </div>
            )}

            <div className='space-y-2'>
              <Label htmlFor='parentRoleId'>Parent Role</Label>
              <select
                id='parentRoleId'
                {...register('parentRoleId')}
                disabled={isLoading || isSubmitting}
                className={cn(
                  'w-full h-10 rounded-md border border-input bg-background px-3 py-2 text-sm',
                  errors.parentRoleId && 'border-destructive'
                )}
              >
                <option value=''>No parent role (optional)</option>
                {availableParentRoles.map((parentRole) => (
                  <option key={parentRole.id} value={parentRole.id}>
                    {parentRole.name} ({parentRole.roleType})
                  </option>
                ))}
              </select>
              <p className='text-xs text-muted-foreground'>
                Inherit permissions from parent role
              </p>
              {errors.parentRoleId && (
                <p className='text-sm text-destructive'>
                  {errors.parentRoleId.message}
                </p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Keycloak Settings */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Keycloak Integration</h3>

          <div className='flex items-center justify-between rounded-lg border p-4'>
            <div className='space-y-0.5'>
              <Label htmlFor='isRealmRole' className='text-base'>
                Realm Role
              </Label>
              <p className='text-sm text-muted-foreground'>
                Create as Keycloak realm role (vs client role)
              </p>
            </div>
            <input
              type='checkbox'
              id='isRealmRole'
              {...register('isRealmRole')}
              disabled={isLoading || isSubmitting}
              className='h-4 w-4 rounded border-gray-300 text-primary focus:ring-2 focus:ring-primary'
            />
          </div>

          {!isRealmRole && (
            <div className='space-y-2'>
              <Label htmlFor='keycloakClientId'>Keycloak Client ID</Label>
              <Input
                id='keycloakClientId'
                placeholder='e.g. serp-client'
                {...register('keycloakClientId')}
                disabled={isLoading || isSubmitting}
                className={cn(errors.keycloakClientId && 'border-destructive')}
              />
              <p className='text-xs text-muted-foreground'>
                Required for client roles
              </p>
              {errors.keycloakClientId && (
                <p className='text-sm text-destructive'>
                  {errors.keycloakClientId.message}
                </p>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Additional Settings */}
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold mb-4'>Additional Settings</h3>

          <div className='flex items-center justify-between rounded-lg border p-4'>
            <div className='space-y-0.5'>
              <Label htmlFor='isDefault' className='text-base'>
                Default Role
              </Label>
              <p className='text-sm text-muted-foreground'>
                Auto-assign to new users in this scope
              </p>
            </div>
            <input
              type='checkbox'
              id='isDefault'
              {...register('isDefault')}
              disabled={isLoading || isSubmitting}
              className='h-4 w-4 rounded border-gray-300 text-primary focus:ring-2 focus:ring-primary'
            />
          </div>
        </CardContent>
      </Card>

      {/* Submit Buttons */}
      <div className='flex justify-end gap-2'>
        {onCancel && (
          <Button
            type='button'
            variant='outline'
            onClick={onCancel}
            disabled={isLoading || isSubmitting}
          >
            Cancel
          </Button>
        )}
        <Button type='submit' disabled={isLoading || isSubmitting}>
          {(isLoading || isSubmitting) && (
            <Loader2 className='mr-2 h-4 w-4 animate-spin' />
          )}
          {isEditing ? 'Update Role' : 'Create Role'}
        </Button>
      </div>
    </form>
  );
};
