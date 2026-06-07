/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item link actions
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { Loader2, Plus, X } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import {
  Button,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Combobox } from '@/shared/components/ui/combobox';
import {
  useCreatePmWorkItemLinkMutation,
  useGetPmIssueLinkTypesQuery,
  useSearchPmWorkItemsQuery,
} from '../../../api/workItemApi';
import type { PMIssueLinkTypeApi } from '../../../types/api';
import { mapWorkItemToComboboxItem } from '../createWorkItemForm';

interface PMWorkItemLinkActionsProps {
  projectId: number;
  workItemId?: number;
}

export function PMWorkItemLinkActions({
  projectId,
  workItemId,
}: PMWorkItemLinkActionsProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [targetSearch, setTargetSearch] = useState('');
  const [targetId, setTargetId] = useState<string>('');
  const [linkTypeId, setLinkTypeId] = useState<string>('');
  const [createWorkItemLink, createState] = useCreatePmWorkItemLinkMutation();

  const { data: linkTypeResponse, isFetching: isLinkTypeLoading } =
    useGetPmIssueLinkTypesQuery(
      {
        page: 0,
        pageSize: 100,
        sortBy: 'name',
        sortDirection: 'asc',
      },
      { skip: !isOpen }
    );

  const { data: searchResponse, isFetching: isTargetLoading } =
    useSearchPmWorkItemsQuery(
      {
        projectId,
        params: {
          keyword: targetSearch.trim() || undefined,
          page: 0,
          pageSize: 20,
          sortField: 'updatedAt',
          sortDirection: 'DESC',
          enriched: true,
        },
      },
      { skip: !isOpen || !workItemId }
    );

  const targetOptions = useMemo(
    () =>
      (searchResponse?.data.items || [])
        .filter((item) => item.id !== workItemId)
        .map(mapWorkItemToComboboxItem),
    [searchResponse, workItemId]
  );

  const linkTypeOptions = useMemo(
    () =>
      (linkTypeResponse?.data.items || []).map((item: PMIssueLinkTypeApi) => ({
        value: String(item.id),
        label: item.outwardDescription
          ? `${item.name} - ${item.outwardDescription}`
          : item.name,
      })),
    [linkTypeResponse]
  );

  useEffect(() => {
    if (!isOpen) {
      setTargetSearch('');
      setTargetId('');
      setLinkTypeId('');
    }
  }, [isOpen]);

  const handleSubmit = async () => {
    if (!workItemId || !targetId || !linkTypeId) {
      return;
    }

    try {
      await createWorkItemLink({
        projectId,
        workItemId,
        body: {
          targetId: Number(targetId),
          linkTypeId: Number(linkTypeId),
        },
      }).unwrap();
      toast.success('Link created.');
      setIsOpen(false);
    } catch (error) {
      toast.error('Failed to create link', {
        description: getErrorMessage(error),
      });
    }
  };

  if (!workItemId) {
    return null;
  }

  return (
    <div className='space-y-3'>
      <div className='flex items-center justify-end gap-2'>
        <Button
          type='button'
          variant='outline'
          size='sm'
          className='gap-2'
          onClick={() => setIsOpen((current) => !current)}
        >
          {isOpen ? <X className='h-4 w-4' /> : <Plus className='h-4 w-4' />}
          {isOpen ? 'Close' : 'Add link'}
        </Button>
        {createState.isLoading ? (
          <Loader2 className='h-4 w-4 animate-spin text-muted-foreground' />
        ) : null}
      </div>

      {isOpen ? (
        <div className='grid gap-3 rounded-md border bg-muted/20 p-3 md:grid-cols-[minmax(0,1fr)_220px_auto]'>
          <div className='min-w-0'>
            <Combobox
              value={targetId || undefined}
              onChange={(value) => setTargetId(value ? String(value) : '')}
              items={targetOptions}
              placeholder='Search target work item'
              emptyText='No work items found'
              loading={isTargetLoading}
              onSearch={setTargetSearch}
            />
          </div>
          <div className='min-w-0'>
            <Select
              value={linkTypeId || undefined}
              onValueChange={setLinkTypeId}
              disabled={isLinkTypeLoading}
            >
              <SelectTrigger>
                <SelectValue placeholder='Link type' />
              </SelectTrigger>
              <SelectContent>
                {linkTypeOptions.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className='flex items-center gap-2'>
            <Button
              type='button'
              onClick={handleSubmit}
              disabled={!targetId || !linkTypeId || createState.isLoading}
            >
              Create
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
