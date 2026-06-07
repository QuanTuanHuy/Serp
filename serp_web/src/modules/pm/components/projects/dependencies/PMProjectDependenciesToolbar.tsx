/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM dependency toolbar
 */

'use client';

import { Filter, PlayCircle, RefreshCw, Search } from 'lucide-react';
import {
  Badge,
  Button,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Switch,
} from '@/shared/components/ui';

interface PMProjectDependenciesToolbarProps {
  keyword: string;
  activeFilterCount: number;
  depth: number;
  includeOutside: boolean;
  includeRelatedLinks: boolean;
  selectedCount: number;
  isRefreshing: boolean;
  onKeywordChange: (value: string) => void;
  onDepthChange: (value: number) => void;
  onIncludeOutsideChange: (value: boolean) => void;
  onIncludeRelatedLinksChange: (value: boolean) => void;
  onFilterClick: () => void;
  onRefresh: () => void;
  onOptimizeSelected: () => void;
}

export function PMProjectDependenciesToolbar({
  keyword,
  activeFilterCount,
  depth,
  includeOutside,
  includeRelatedLinks,
  selectedCount,
  isRefreshing,
  onKeywordChange,
  onDepthChange,
  onIncludeOutsideChange,
  onIncludeRelatedLinksChange,
  onFilterClick,
  onRefresh,
  onOptimizeSelected,
}: PMProjectDependenciesToolbarProps) {
  return (
    <div className='flex flex-col gap-3 rounded-lg border bg-card p-4 shadow-sm xl:flex-row xl:items-center xl:justify-between'>
      <div className='flex min-w-0 flex-1 flex-wrap items-center gap-2'>
        <div className='relative min-w-64 flex-1 xl:max-w-md'>
          <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
          <Input
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            placeholder='Search dependencies'
            className='pl-9'
          />
        </div>
        <Button
          type='button'
          variant='outline'
          size='sm'
          className='gap-2'
          onClick={onFilterClick}
        >
          <Filter className='h-4 w-4' />
          Filters
          {activeFilterCount > 0 ? (
            <Badge variant='secondary'>{activeFilterCount}</Badge>
          ) : null}
        </Button>
        <Button
          type='button'
          variant='outline'
          size='icon'
          onClick={onRefresh}
          disabled={isRefreshing}
        >
          <RefreshCw
            className={isRefreshing ? 'h-4 w-4 animate-spin' : 'h-4 w-4'}
          />
        </Button>
      </div>

      <div className='flex flex-wrap items-center gap-4'>
        <label className='flex items-center gap-2 text-sm'>
          <Switch
            checked={includeOutside}
            onCheckedChange={onIncludeOutsideChange}
          />
          <span>Show outside</span>
        </label>
        <label className='flex items-center gap-2 text-sm'>
          <Switch
            checked={includeRelatedLinks}
            onCheckedChange={onIncludeRelatedLinksChange}
          />
          <span>Related links</span>
        </label>
        <div className='flex items-center gap-2'>
          <Label className='text-sm'>Depth</Label>
          <Select
            value={String(depth)}
            onValueChange={(value) => onDepthChange(Number(value))}
          >
            <SelectTrigger className='h-9 w-20'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {[1, 2, 3, 4, 5].map((value) => (
                <SelectItem key={value} value={String(value)}>
                  {value}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <Button
          type='button'
          size='sm'
          className='gap-2'
          disabled={selectedCount === 0}
          onClick={onOptimizeSelected}
        >
          <PlayCircle className='h-4 w-4' />
          Optimize
          {selectedCount > 0 ? (
            <Badge variant='secondary'>{selectedCount}</Badge>
          ) : null}
        </Button>
      </div>
    </div>
  );
}
