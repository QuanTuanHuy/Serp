/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list tab
 */

'use client';

import {
  startTransition,
  useCallback,
  useDeferredValue,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { AlertCircle } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Card,
  CardContent,
} from '@/shared/components/ui';
import {
  useGetPmWorkItemByIdQuery,
  useSearchPmWorkItemsQuery,
} from '../../../api';
import { PMWorkItemCommandBar } from './PMWorkItemCommandBar';
import { PMWorkItemListFilters } from './PMWorkItemListFilters';
import {
  PMWorkItemCompactList,
  PMWorkItemDetailPanel,
  PMWorkItemListTable,
} from './PMWorkItemListViews';
import {
  getActiveFilterCount,
  parseIssueId,
  parseNumberList,
  parseViewMode,
  type WorkItemListViewMode,
} from './pmWorkItemList.utils';

interface PMWorkItemListTabProps {
  projectId: number;
}

export function PMWorkItemListTab({ projectId }: PMWorkItemListTabProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const view = parseViewMode(searchParams.get('view'));
  const selectedIssueId = parseIssueId(searchParams.get('issueId'));
  const parentId = parseIssueId(searchParams.get('parentId'));
  const assigneeIds = parseNumberList(searchParams.get('assigneeIds'));
  const issueTypeIds = parseNumberList(searchParams.get('issueTypeIds'));
  const statusIds = parseNumberList(searchParams.get('statusIds'));
  const priorityIds = parseNumberList(searchParams.get('priorityIds'));
  const reporterIds = parseNumberList(searchParams.get('reporterIds'));
  const [keyword, setKeyword] = useState(searchParams.get('q') ?? '');
  const [filtersOpen, setFiltersOpen] = useState(false);
  const deferredKeyword = useDeferredValue(keyword.trim());

  const searchQuery = useSearchPmWorkItemsQuery({
    projectId,
    params: {
      keyword: deferredKeyword || undefined,
      parentId,
      assigneeIds,
      issueTypeIds,
      statusIds,
      priorityIds,
      reporterIds,
      enriched: true,
      page: 0,
      pageSize: 50,
      sortField: 'rank',
      sortDirection: 'ASC',
    },
  });

  const items = searchQuery.data?.data.items ?? [];
  const totalItems = searchQuery.data?.data.totalItems ?? 0;

  const selectedItem = useMemo(
    () => items.find((item) => item.id === selectedIssueId),
    [items, selectedIssueId]
  );

  const updateUrl = useCallback(
    (updates: Record<string, string | undefined>) => {
      const nextParams = new URLSearchParams(searchParams.toString());
      for (const [key, value] of Object.entries(updates)) {
        if (value) {
          nextParams.set(key, value);
        } else {
          nextParams.delete(key);
        }
      }
      const queryString = nextParams.toString();
      startTransition(() => {
        router.replace(queryString ? `${pathname}?${queryString}` : pathname, {
          scroll: false,
        });
      });
    },
    [pathname, router, searchParams]
  );

  useEffect(() => {
    if (deferredKeyword === (searchParams.get('q') ?? '')) return;
    updateUrl({ q: deferredKeyword || undefined });
  }, [deferredKeyword, searchParams, updateUrl]);

  useEffect(() => {
    if (view !== 'detail' || selectedIssueId || !items[0]) return;
    updateUrl({ issueId: String(items[0].id) });
  }, [items, selectedIssueId, updateUrl, view]);

  const selectIssue = (issueId: number) => {
    updateUrl({
      view: view === 'detail' ? 'detail' : undefined,
      issueId: String(issueId),
    });
  };

  const setView = (nextView: WorkItemListViewMode) => {
    updateUrl({
      view: nextView === 'detail' ? 'detail' : undefined,
      issueId:
        nextView === 'detail'
          ? String(selectedIssueId ?? items[0]?.id ?? '') || undefined
          : undefined,
    });
  };

  const updateFilter = (updates: Record<string, string | undefined>) => {
    updateUrl({ ...updates, issueId: undefined });
  };

  const clearFilters = () => {
    updateFilter({
      parentId: undefined,
      assigneeIds: undefined,
      issueTypeIds: undefined,
      statusIds: undefined,
      priorityIds: undefined,
      reporterIds: undefined,
    });
  };

  const activeFilterCount = getActiveFilterCount({
    parentId,
    assigneeIds,
    issueTypeIds,
    statusIds,
    priorityIds,
    reporterIds,
  });

  const activeFilterChips = [
    parentId ? { key: 'parentId', label: 'Parent' } : null,
    assigneeIds.length
      ? { key: 'assigneeIds', label: `Assignee: ${assigneeIds.length}` }
      : null,
    issueTypeIds.length
      ? { key: 'issueTypeIds', label: `Work type: ${issueTypeIds.length}` }
      : null,
    statusIds.length
      ? { key: 'statusIds', label: `Status: ${statusIds.length}` }
      : null,
    priorityIds.length
      ? { key: 'priorityIds', label: `Priority: ${priorityIds.length}` }
      : null,
    reporterIds.length
      ? { key: 'reporterIds', label: `Reporter: ${reporterIds.length}` }
      : null,
  ].filter(Boolean) as Array<{ key: string; label: string }>;

  const removeFilter = (key: string) => {
    updateFilter({ [key]: undefined });
  };

  return (
    <div className='space-y-4'>
      <PMWorkItemCommandBar
        keyword={keyword}
        view={view}
        activeFilterCount={activeFilterCount}
        isRefreshing={searchQuery.isFetching}
        onKeywordChange={setKeyword}
        onViewChange={setView}
        onRefresh={() => searchQuery.refetch()}
        onFilterClick={() => setFiltersOpen(true)}
      />

      {activeFilterChips.length > 0 ? (
        <div className='flex flex-wrap items-center gap-2'>
          {activeFilterChips.map((chip) => (
            <Badge
              key={chip.key}
              variant='secondary'
              className='inline-flex items-center gap-2 px-2 py-1'
            >
              <span>{chip.label}</span>
              <button
                type='button'
                className='text-xs text-muted-foreground hover:text-foreground'
                onClick={() => removeFilter(chip.key)}
              >
                x
              </button>
            </Badge>
          ))}
          <Button
            type='button'
            variant='ghost'
            size='sm'
            onClick={clearFilters}
          >
            Clear all
          </Button>
        </div>
      ) : null}

      {searchQuery.error ? (
        <Alert variant='destructive'>
          <AlertCircle className='h-4 w-4' />
          <AlertTitle>Work items unavailable</AlertTitle>
          <AlertDescription>
            {getErrorMessage(searchQuery.error)}
          </AlertDescription>
        </Alert>
      ) : null}

      <PMWorkItemListFilters
        projectId={projectId}
        open={filtersOpen}
        parentId={parentId}
        assigneeIds={assigneeIds}
        issueTypeIds={issueTypeIds}
        statusIds={statusIds}
        priorityIds={priorityIds}
        reporterIds={reporterIds}
        onOpenChange={setFiltersOpen}
        onUpdate={updateFilter}
        onClear={clearFilters}
      />

      {view === 'detail' ? (
        <div className='grid gap-4 xl:grid-cols-[440px_minmax(0,1fr)]'>
          <PMWorkItemCompactList
            items={items}
            loading={searchQuery.isLoading}
            selectedIssueId={selectedIssueId}
            totalItems={totalItems}
            onSelect={selectIssue}
          />
          <PMWorkItemDetailPanelContainer
            projectId={projectId}
            workItemId={selectedIssueId}
            fallbackItem={selectedItem}
          />
        </div>
      ) : (
        <PMWorkItemListTable
          items={items}
          loading={searchQuery.isLoading}
          selectedIssueId={selectedIssueId}
          totalItems={totalItems}
          onSelect={selectIssue}
        />
      )}
    </div>
  );
}

interface PMWorkItemDetailPanelContainerProps {
  projectId: number;
  workItemId?: number;
  fallbackItem?: {
    key?: string;
    summary?: string;
    description?: string | null;
    statusName?: string | null;
    issueTypeName?: string | null;
    priorityName?: string | null;
    assigneeName?: string | null;
    reporterName?: string | null;
    createdAt?: number | null;
    dueDate?: number | null;
    updatedAt?: number | null;
  };
}

function PMWorkItemDetailPanelContainer({
  projectId,
  workItemId,
  fallbackItem,
}: PMWorkItemDetailPanelContainerProps) {
  const { data, error, isFetching, isLoading } = useGetPmWorkItemByIdQuery(
    { projectId, workItemId: workItemId ?? 0 },
    { skip: !workItemId }
  );

  if (!workItemId) {
    return (
      <Card className='min-h-96 shadow-sm'>
        <CardContent className='flex h-96 items-center justify-center text-sm text-muted-foreground'>
          Select work item to view details.
        </CardContent>
      </Card>
    );
  }

  return (
    <PMWorkItemDetailPanel
      loading={isLoading}
      errorMessage={error ? getErrorMessage(error) : undefined}
      isFetching={isFetching}
      title={data?.summary ?? fallbackItem?.summary ?? 'Work item'}
      keyLabel={data?.key ?? fallbackItem?.key ?? `#${workItemId}`}
      description={
        data?.description ||
        fallbackItem?.description ||
        'No description provided.'
      }
      statusName={data?.status?.name ?? fallbackItem?.statusName ?? undefined}
      issueTypeName={
        data?.issueType?.name ?? fallbackItem?.issueTypeName ?? undefined
      }
      priorityName={
        data?.priority?.name ?? fallbackItem?.priorityName ?? undefined
      }
      assigneeName={
        data?.assignee?.displayName ?? fallbackItem?.assigneeName ?? undefined
      }
      reporterName={
        data?.reporter?.displayName ?? fallbackItem?.reporterName ?? undefined
      }
      createdAt={data?.createdAt ?? fallbackItem?.createdAt}
      dueDate={data?.dueDate ?? fallbackItem?.dueDate}
      updatedAt={data?.updatedAt ?? fallbackItem?.updatedAt}
    />
  );
}
