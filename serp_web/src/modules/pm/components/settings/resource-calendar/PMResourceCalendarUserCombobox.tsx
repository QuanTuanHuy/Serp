/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar user search
 */

'use client';

import { useDeferredValue, useMemo, useState } from 'react';

import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import { Combobox, type ComboboxItem } from '@/shared/components/ui/combobox';
import { useAppSelector } from '@/shared/hooks';

import type { UserProfile } from '@/modules/admin/types';

export function PMResourceCalendarUserCombobox({
  value,
  onChange,
  disabled,
  placeholder = 'Search users...',
  className,
}: {
  value?: number | null;
  onChange: (value: number | null) => void;
  disabled?: boolean;
  placeholder?: string;
  className?: string;
}) {
  const organizationId = useAppSelector(selectOrganizationId);
  const { data: myModules } = useGetMyModulesQuery(undefined, {
    skip: !organizationId,
  });
  const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

  const [search, setSearch] = useState('');
  const deferredSearch = useDeferredValue(search.trim());
  const canSearch = deferredSearch.length > 0;

  const usersQuery = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      search: deferredSearch || undefined,
      page: 0,
      pageSize: 50,
      status: 'ACTIVE',
      sortBy: 'firstName',
      sortDir: 'ASC',
      moduleId: pmModuleId,
    },
    {
      skip: !organizationId || !canSearch,
    }
  );

  const items = useMemo<ComboboxItem[]>(() => {
    const mappedItems = (usersQuery.data?.data.items ?? []).map((user) => ({
      value: user.id,
      label: getUserLabel(user),
    }));

    if (
      value &&
      !mappedItems.some((item) => Number(item.value) === Number(value))
    ) {
      return [{ value, label: `User #${value}` }, ...mappedItems];
    }

    return mappedItems;
  }, [usersQuery.data, value]);

  return (
    <Combobox
      value={value ?? undefined}
      onChange={(nextValue) =>
        onChange(nextValue === undefined ? null : Number(nextValue))
      }
      items={items}
      placeholder={
        organizationId ? placeholder : 'Organization context is unavailable'
      }
      emptyText={
        !organizationId
          ? 'Organization context is unavailable.'
          : canSearch
            ? 'No users found.'
            : 'Start typing to search users.'
      }
      loading={usersQuery.isFetching}
      disabled={disabled || !organizationId}
      clearable
      onSearch={setSearch}
      className={className}
    />
  );
}

function getUserLabel(user: UserProfile): string {
  const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
  if (fullName && user.email) {
    return `${fullName} (${user.email})`;
  }

  return fullName || user.email || `User #${user.id}`;
}
