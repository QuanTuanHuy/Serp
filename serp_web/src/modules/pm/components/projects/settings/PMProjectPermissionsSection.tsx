/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project permissions settings section
 */

'use client';

import { useMemo } from 'react';
import { RotateCcw, Save, ShieldCheck } from 'lucide-react';

import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';

import type {
  PMProjectPermissionDefinitionApi,
  PMProjectPermissionGrantApi,
} from '../../../types/api';

export type PMEditablePermissionGranteeType = 'PROJECT_LEAD' | 'PROJECT_ROLE';

interface PMProjectPermissionsSectionProps {
  permissions: PMProjectPermissionDefinitionApi[];
  grants: PMProjectPermissionGrantApi[];
  roles: Array<{ id: number; name: string; isSystem: boolean }>;
  schemeName?: string;
  isLoading: boolean;
  errorMessage?: string;
  isSaving: boolean;
  isDirty: boolean;
  onToggleGrant: (
    permissionKey: string,
    granteeType: PMEditablePermissionGranteeType,
    granteeRef?: string | null
  ) => void;
  onReset: () => void;
  onSave: () => void;
}

export function PMProjectPermissionsSection({
  permissions,
  grants,
  roles,
  schemeName,
  isLoading,
  errorMessage,
  isSaving,
  isDirty,
  onToggleGrant,
  onReset,
  onSave,
}: PMProjectPermissionsSectionProps) {
  const permissionGroups = useMemo(
    () => groupPermissionsByCategory(permissions),
    [permissions]
  );
  const unmanagedGrants = useMemo(
    () =>
      grants.filter(
        (grant) =>
          grant.granteeType !== 'PROJECT_LEAD' &&
          grant.granteeType !== 'PROJECT_ROLE'
      ),
    [grants]
  );

  return (
    <Card className='shadow-sm'>
      <CardHeader className='gap-3 border-b md:flex-row md:items-center md:justify-between'>
        <div className='min-w-0'>
          <CardTitle className='flex items-center gap-2 text-base'>
            <ShieldCheck className='h-4 w-4' />
            Project permissions
          </CardTitle>
          <p className='mt-1 truncate text-sm text-muted-foreground'>
            {schemeName || 'Permission scheme'}
          </p>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={onReset}
            disabled={!isDirty || isSaving}
          >
            <RotateCcw className='mr-2 h-4 w-4' />
            Reset
          </Button>
          <Button
            type='button'
            size='sm'
            onClick={onSave}
            disabled={!isDirty || isSaving}
          >
            <Save className='mr-2 h-4 w-4' />
            Save
          </Button>
        </div>
      </CardHeader>
      <CardContent className='p-0'>
        {isLoading ? (
          <div className='space-y-3 p-4'>
            {Array.from({ length: 5 }).map((_, index) => (
              <Skeleton key={index} className='h-12 w-full' />
            ))}
          </div>
        ) : errorMessage ? (
          <div className='p-6 text-sm text-muted-foreground'>
            Project permissions unavailable: {errorMessage}
          </div>
        ) : (
          <div className='overflow-x-auto'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className='min-w-72'>Permission</TableHead>
                  <TableHead className='w-32 text-center'>
                    Project lead
                  </TableHead>
                  {roles.map((role) => (
                    <TableHead key={role.id} className='w-36 text-center'>
                      <span className='line-clamp-2'>{role.name}</span>
                    </TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {permissionGroups.map((group) => (
                  <PermissionGroupRows
                    key={group.category}
                    category={group.category}
                    permissions={group.permissions}
                    grants={grants}
                    roles={roles}
                    onToggleGrant={onToggleGrant}
                  />
                ))}
                {permissionGroups.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={roles.length + 2}
                      className='h-24 text-center text-sm text-muted-foreground'
                    >
                      No permission definitions found.
                    </TableCell>
                  </TableRow>
                ) : null}
              </TableBody>
            </Table>
          </div>
        )}
        {unmanagedGrants.length > 0 ? (
          <div className='border-t p-4'>
            <p className='text-xs font-semibold uppercase text-muted-foreground'>
              Other grants
            </p>
            <div className='mt-2 flex flex-wrap gap-2'>
              {unmanagedGrants.map((grant) => (
                <Badge key={grantKey(grant)} variant='outline'>
                  {grant.permissionKey}: {grant.granteeType}
                  {grant.granteeRef ? ` ${grant.granteeRef}` : ''}
                </Badge>
              ))}
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function PermissionGroupRows({
  category,
  permissions,
  grants,
  roles,
  onToggleGrant,
}: {
  category: string;
  permissions: PMProjectPermissionDefinitionApi[];
  grants: PMProjectPermissionGrantApi[];
  roles: Array<{ id: number; name: string; isSystem: boolean }>;
  onToggleGrant: PMProjectPermissionsSectionProps['onToggleGrant'];
}) {
  return (
    <>
      <TableRow className='bg-muted/50 hover:bg-muted/50'>
        <TableCell
          colSpan={roles.length + 2}
          className='py-2 text-xs font-semibold uppercase text-muted-foreground'
        >
          {formatPermissionCategory(category)}
        </TableCell>
      </TableRow>
      {permissions.map((permission) => (
        <TableRow key={permission.permissionKey}>
          <TableCell>
            <div className='space-y-1'>
              <p className='font-medium'>{permission.name}</p>
              <p className='text-xs text-muted-foreground'>
                {permission.description || permission.permissionKey}
              </p>
            </div>
          </TableCell>
          <TableCell className='text-center'>
            <Checkbox
              checked={grants.some((grant) =>
                isGrantMatch(
                  grant,
                  permission.permissionKey,
                  'PROJECT_LEAD',
                  null
                )
              )}
              onCheckedChange={() =>
                onToggleGrant(permission.permissionKey, 'PROJECT_LEAD', null)
              }
              aria-label={`${permission.name} for project lead`}
            />
          </TableCell>
          {roles.map((role) => (
            <TableCell key={role.id} className='text-center'>
              <Checkbox
                checked={grants.some((grant) =>
                  isGrantMatch(
                    grant,
                    permission.permissionKey,
                    'PROJECT_ROLE',
                    role.name
                  )
                )}
                onCheckedChange={() =>
                  onToggleGrant(
                    permission.permissionKey,
                    'PROJECT_ROLE',
                    role.name
                  )
                }
                aria-label={`${permission.name} for ${role.name}`}
              />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );
}

function groupPermissionsByCategory(
  permissions: PMProjectPermissionDefinitionApi[]
) {
  const groups = new Map<string, PMProjectPermissionDefinitionApi[]>();
  for (const permission of permissions) {
    const category = permission.category || 'OTHER';
    groups.set(category, [...(groups.get(category) ?? []), permission]);
  }
  return Array.from(groups.entries()).map(([category, items]) => ({
    category,
    permissions: items,
  }));
}

export function isGrantMatch(
  grant: PMProjectPermissionGrantApi,
  permissionKey: string,
  granteeType: string,
  granteeRef?: string | null
) {
  return (
    grant.permissionKey === permissionKey &&
    grant.granteeType === granteeType &&
    normalizeGrantRef(grant.granteeRef) === normalizeGrantRef(granteeRef)
  );
}

export function buildGrantStateKey(grants: PMProjectPermissionGrantApi[]) {
  return grants.map(grantKey).sort().join('|');
}

function grantKey(grant: PMProjectPermissionGrantApi) {
  return [
    grant.permissionKey,
    grant.granteeType,
    normalizeGrantRef(grant.granteeRef) ?? '',
    grant.customFieldId ?? '',
  ].join(':');
}

function normalizeGrantRef(value?: string | null) {
  const normalized = value?.trim();
  return normalized ? normalized.toLowerCase() : null;
}

function formatPermissionCategory(category: string) {
  return category.toLowerCase().replaceAll('_', ' ');
}
