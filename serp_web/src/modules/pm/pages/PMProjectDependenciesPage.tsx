/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project dependencies page
 */

'use client';

import { useDeferredValue, useEffect, useMemo, useState } from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Card,
  CardContent,
} from '@/shared/components/ui';
import { useGetPmWorkItemDependenciesQuery } from '../api';
import { PMWorkItemDetailDialog } from '../components/work-items/detail';
import { PMProjectDependenciesFilters } from '../components/projects/dependencies/PMProjectDependenciesFilters';
import { PMProjectDependenciesGraph } from '../components/projects/dependencies/PMProjectDependenciesGraph';
import { PMProjectDependenciesSummary } from '../components/projects/dependencies/PMProjectDependenciesSummary';
import { PMProjectDependenciesTable } from '../components/projects/dependencies/PMProjectDependenciesTable';
import { PMProjectDependenciesToolbar } from '../components/projects/dependencies/PMProjectDependenciesToolbar';
import {
  getDependencyFilterCount,
  parseBooleanToggle,
  parseDepth,
  parseNumberList,
  parseOptionalNumber,
} from '../components/projects/dependencies/pmProjectDependencies.utils';

interface PMProjectDependenciesPageProps {
  projectId: string;
}

export function PMProjectDependenciesPage({
  projectId,
}: PMProjectDependenciesPageProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const searchParamString = searchParams.toString();
  const numericProjectId = Number(projectId);

  const [keyword, setKeyword] = useState(searchParams.get('q') ?? '');
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [selectedWorkItemId, setSelectedWorkItemId] = useState<number>();
  const [workItemDetailOpen, setWorkItemDetailOpen] = useState(false);
  const [selectedWorkItemIds, setSelectedWorkItemIds] = useState<number[]>([]);

  const deferredKeyword = useDeferredValue(keyword.trim());

  const parentId = parseOptionalNumber(searchParams.get('parentId'));
  const assigneeIds = parseNumberList(searchParams.get('assigneeIds'));
  const issueTypeIds = parseNumberList(searchParams.get('issueTypeIds'));
  const statusIds = parseNumberList(searchParams.get('statusIds'));
  const priorityIds = parseNumberList(searchParams.get('priorityIds'));
  const componentIds = parseNumberList(searchParams.get('componentIds'));
  const depth = parseDepth(searchParams.get('depth'));
  const includeOutside = parseBooleanToggle(
    searchParams.get('includeOutside'),
    true
  );
  const includeRelatedLinks = parseBooleanToggle(
    searchParams.get('includeRelatedLinks'),
    false
  );

  const activeFilterCount = useMemo(
    () =>
      getDependencyFilterCount({
        parentId,
        assigneeIds,
        issueTypeIds,
        statusIds,
        priorityIds,
        componentIds,
      }),
    [assigneeIds, componentIds, issueTypeIds, parentId, priorityIds, statusIds]
  );

  useEffect(() => {
    setKeyword(searchParams.get('q') ?? '');
  }, [searchParamString]);

  useEffect(() => {
    if (deferredKeyword === (searchParams.get('q') ?? '')) return;

    const nextParams = new URLSearchParams(searchParams.toString());
    if (deferredKeyword) {
      nextParams.set('q', deferredKeyword);
    } else {
      nextParams.delete('q');
    }

    const queryString = nextParams.toString();
    router.replace(queryString ? `${pathname}?${queryString}` : pathname, {
      scroll: false,
    });
  }, [deferredKeyword, pathname, router, searchParams]);

  const query = useGetPmWorkItemDependenciesQuery(
    {
      projectId: numericProjectId,
      params: {
        keyword: deferredKeyword || undefined,
        parentId,
        assigneeIds,
        issueTypeIds,
        statusIds,
        priorityIds,
        componentIds,
        depth,
        includeOutside,
        includeRelatedLinks,
        page: 0,
        pageSize: 100,
      },
    },
    { skip: !Number.isFinite(numericProjectId) }
  );

  const updateUrl = (updates: Record<string, string | undefined>) => {
    const nextParams = new URLSearchParams(searchParams.toString());
    Object.entries(updates).forEach(([key, value]) => {
      if (value) {
        nextParams.set(key, value);
      } else {
        nextParams.delete(key);
      }
    });
    const queryString = nextParams.toString();
    router.replace(queryString ? `${pathname}?${queryString}` : pathname, {
      scroll: false,
    });
  };

  const updateFilter = (updates: Record<string, string | undefined>) => {
    updateUrl({ ...updates });
  };

  const clearFilters = () => {
    updateFilter({
      parentId: undefined,
      assigneeIds: undefined,
      issueTypeIds: undefined,
      statusIds: undefined,
      priorityIds: undefined,
      componentIds: undefined,
    });
  };

  const optimizeSelected = () => {
    if (!selectedWorkItemIds.length) return;
    router.push(
      `/pm/projects/${projectId}/optimization?selected=${selectedWorkItemIds.join(',')}`
    );
  };

  const toggleSelectedWorkItem = (workItemId: number) => {
    setSelectedWorkItemIds((current) =>
      current.includes(workItemId)
        ? current.filter((value) => value !== workItemId)
        : [...current, workItemId]
    );
  };

  const openWorkItem = (workItemId: number) => {
    setSelectedWorkItemId(workItemId);
    setWorkItemDetailOpen(true);
  };

  if (!Number.isFinite(numericProjectId)) {
    return (
      <Card className='shadow-sm'>
        <CardContent className='p-6 text-sm text-muted-foreground'>
          Invalid project id.
        </CardContent>
      </Card>
    );
  }

  const nodes = query.data?.nodes || [];
  const edges = query.data?.edges || [];

  return (
    <div className='space-y-4'>
      <div className='space-y-2'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Dependencies</h1>
          <p className='text-sm text-muted-foreground'>
            Inspect dependency chains, outside blockers, and optimization scope.
          </p>
        </div>
      </div>

      <PMProjectDependenciesToolbar
        keyword={keyword}
        activeFilterCount={activeFilterCount}
        depth={depth}
        includeOutside={includeOutside}
        includeRelatedLinks={includeRelatedLinks}
        selectedCount={selectedWorkItemIds.length}
        isRefreshing={query.isFetching}
        onKeywordChange={setKeyword}
        onDepthChange={(value) => updateFilter({ depth: String(value) })}
        onIncludeOutsideChange={(value) =>
          updateFilter({ includeOutside: String(value) })
        }
        onIncludeRelatedLinksChange={(value) =>
          updateFilter({ includeRelatedLinks: String(value) })
        }
        onFilterClick={() => setFiltersOpen(true)}
        onRefresh={() => query.refetch()}
        onOptimizeSelected={optimizeSelected}
      />

      {query.error ? (
        <Alert variant='destructive'>
          <AlertTitle>Dependencies unavailable</AlertTitle>
          <AlertDescription>{getErrorMessage(query.error)}</AlertDescription>
        </Alert>
      ) : null}

      <PMProjectDependenciesSummary
        summary={query.data?.summary}
        isLoading={query.isLoading}
      />

      <div className='grid gap-4 xl:grid-cols-[minmax(0,1fr)_420px]'>
        <PMProjectDependenciesGraph
          nodes={nodes}
          edges={edges}
          onOpenWorkItem={openWorkItem}
        />
        <div className='space-y-4'>
          <PMProjectDependenciesTable
            nodes={nodes}
            edges={edges}
            selectedIds={selectedWorkItemIds}
            onToggleSelectWorkItem={toggleSelectedWorkItem}
            onOpenWorkItem={openWorkItem}
          />
        </div>
      </div>

      <PMProjectDependenciesFilters
        projectId={numericProjectId}
        open={filtersOpen}
        parentId={parentId}
        assigneeIds={assigneeIds}
        issueTypeIds={issueTypeIds}
        statusIds={statusIds}
        priorityIds={priorityIds}
        componentIds={componentIds}
        onOpenChange={setFiltersOpen}
        onUpdate={updateFilter}
        onClear={clearFilters}
      />

      <PMWorkItemDetailDialog
        projectId={numericProjectId}
        workItemId={selectedWorkItemId}
        open={workItemDetailOpen}
        onOpenChange={(open) => {
          if (!open) {
            setWorkItemDetailOpen(false);
            setSelectedWorkItemId(undefined);
          }
        }}
      />
    </div>
  );
}
