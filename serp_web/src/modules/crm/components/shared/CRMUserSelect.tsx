/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM shared user selector component
 */

'use client';

import React from 'react';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import { useAppSelector } from '@/shared/hooks';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';

interface CRMUserSelectProps {
  value?: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  className?: string;
  placeholder?: string;
  id?: string;
  fallbackUserName?: string;
}

export const CRMUserSelect: React.FC<CRMUserSelectProps> = ({
  value = '',
  onChange,
  disabled = false,
  className,
  placeholder = 'Unassigned',
  id,
  fallbackUserName,
}) => {
  const organizationId = useAppSelector(selectOrganizationId);
  const { data: myModules } = useGetMyModulesQuery(undefined, {
    skip: !organizationId,
  });
  const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

  const { data: orgUsersResponse, isLoading } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100,
      status: 'ACTIVE',
      moduleId: crmModuleId,
    },
    { skip: !organizationId }
  );

  const orgUsers = orgUsersResponse?.data?.items ?? [];
  const valString = value?.trim() ?? '';

  const formatOrgUserLabel = (u: (typeof orgUsers)[number]) => {
    const name = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
    return name || u.email || `User #${u.id}`;
  };

  const showOrphanAssignee =
    Boolean(valString) && !orgUsers.some((u) => String(u.id) === valString);
  const orphanAssigneeLabel = fallbackUserName || `User #${valString}`;

  return (
    <div className='w-full'>
      <Select
        disabled={disabled || isLoading || !organizationId}
        value={valString ? valString : '_none'}
        onValueChange={(v) => onChange(v === '_none' ? '' : v)}
      >
        <SelectTrigger id={id} className={className}>
          <SelectValue
            placeholder={isLoading ? 'Loading users…' : placeholder}
          />
        </SelectTrigger>
        <SelectContent position='popper' className='max-h-60'>
          <SelectItem value='_none'>{placeholder}</SelectItem>
          {showOrphanAssignee && (
            <SelectItem value={valString}>{orphanAssigneeLabel}</SelectItem>
          )}
          {orgUsers.map((u) => (
            <SelectItem key={u.id} value={String(u.id)}>
              {formatOrgUserLabel(u)}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      {!organizationId && (
        <p className='text-[10px] text-muted-foreground mt-1'>
          Your profile does not include an organization yet; assignees cannot be
          loaded.
        </p>
      )}
    </div>
  );
};

export default CRMUserSelect;
