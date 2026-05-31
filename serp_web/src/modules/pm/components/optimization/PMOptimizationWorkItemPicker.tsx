/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization work item picker
 */

'use client';

import { CheckSquare, Filter, Search } from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Input,
  ScrollArea,
} from '@/shared/components/ui';
import type { PMWorkItemSearchApi } from '../../types/api';

type PMOptimizationWorkItemPickerProps = {
  keyword: string;
  onKeywordChange: (value: string) => void;
  selectedIds: number[];
  items: PMWorkItemSearchApi[];
  totalItems?: number;
  isLoading?: boolean;
  onToggleSelected: (workItemId: number) => void;
  onSelectVisible: () => void;
  onClearSelected: () => void;
};

export function PMOptimizationWorkItemPicker({
  keyword,
  onKeywordChange,
  selectedIds,
  items,
  totalItems,
  isLoading,
  onToggleSelected,
  onSelectVisible,
  onClearSelected,
}: PMOptimizationWorkItemPickerProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <div className='flex flex-col gap-3'>
          <div className='flex items-start justify-between gap-3'>
            <div>
              <CardTitle className='text-base'>Work items</CardTitle>
              <p className='text-sm text-muted-foreground'>
                Search and select the items to include in the run.
              </p>
            </div>
            <Badge variant='secondary' className='h-6 px-2'>
              {selectedIds.length} selected
            </Badge>
          </div>

          <div className='flex flex-col gap-2 lg:flex-row lg:items-center'>
            <div className='relative flex-1'>
              <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                value={keyword}
                onChange={(event) => onKeywordChange(event.target.value)}
                placeholder='Search work items'
                className='pl-9'
              />
            </div>
            <div className='flex items-center gap-2'>
              <Button type='button' variant='outline' onClick={onSelectVisible}>
                <CheckSquare className='mr-2 h-4 w-4' />
                Select visible
              </Button>
              <Button type='button' variant='ghost' onClick={onClearSelected}>
                Clear selected
              </Button>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent className='p-0'>
        <div className='flex items-center justify-between border-b px-4 py-3 text-sm text-muted-foreground'>
          <div className='flex items-center gap-2'>
            <Filter className='h-4 w-4' />
            {totalItems ?? items.length} items
          </div>
          <span>{isLoading ? 'Loading...' : 'Ready'}</span>
        </div>

        <ScrollArea className='h-[560px]'>
          <div className='divide-y'>
            {items.length ? (
              items.map((item) => (
                <button
                  key={item.id}
                  type='button'
                  className='flex w-full items-start gap-3 px-4 py-3 text-left hover:bg-muted/40'
                  onClick={() => onToggleSelected(item.id)}
                >
                  <Checkbox
                    checked={selectedIds.includes(item.id)}
                    className='mt-1'
                    onClick={(event) => event.stopPropagation()}
                    onCheckedChange={() => onToggleSelected(item.id)}
                  />
                  <div className='min-w-0 flex-1'>
                    <div className='flex items-center gap-2'>
                      <span className='text-xs font-semibold text-primary'>
                        {item.key}
                      </span>
                      <Badge variant='secondary' className='h-5 px-1.5'>
                        {item.issueTypeName || 'Work item'}
                      </Badge>
                    </div>
                    <p className='mt-1 line-clamp-2 text-sm font-medium'>
                      {item.summary}
                    </p>
                    <div className='mt-2 flex flex-wrap gap-3 text-xs text-muted-foreground'>
                      <span>{item.assigneeName || 'Unassigned'}</span>
                      <span>{item.priorityName || 'No priority'}</span>
                      <span>{item.statusName || 'No status'}</span>
                    </div>
                  </div>
                </button>
              ))
            ) : (
              <div className='px-4 py-10 text-sm text-muted-foreground'>
                {isLoading ? 'Loading work items...' : 'No work items found.'}
              </div>
            )}
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  );
}
