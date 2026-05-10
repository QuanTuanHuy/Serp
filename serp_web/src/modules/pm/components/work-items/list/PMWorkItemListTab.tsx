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
import {
  AlertCircle,
  LayoutList,
  ListChecks,
  RefreshCw,
  Search,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Card,
  CardContent,
  Input,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetPmWorkItemByIdQuery,
  useSearchPmWorkItemsQuery,
} from '../../../api';
import { PMWorkItemListFilters } from './PMWorkItemListFilters';
import {
  PMWorkItemCompactList,
  PMWorkItemDetailPanel,
  PMWorkItemListTable,
} from './PMWorkItemListViews';
import {
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

  return (
    <div className='space-y-4'>
      <Card className='shadow-sm'>
        <CardContent className='p-4'>
          <div className='flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'>
            <div>
              <p className='text-sm font-medium text-muted-foreground'>
                Project work items
              </p>
              <h1 className='mt-1 text-2xl font-semibold tracking-tight'>
                List
              </h1>
            </div>
            <div className='flex flex-col gap-2 sm:flex-row sm:items-center'>
              <div className='relative sm:w-80'>
                <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  placeholder='Search key or summary'
                  className='pl-9'
                />
              </div>
              <div className='inline-flex rounded-md border bg-background p-1'>
                <Button
                  type='button'
                  variant={view === 'list' ? 'secondary' : 'ghost'}
                  size='sm'
                  onClick={() => setView('list')}
                >
                  <ListChecks className='mr-2 h-4 w-4' />
                  List view
                </Button>
                <Button
                  type='button'
                  variant={view === 'detail' ? 'secondary' : 'ghost'}
                  size='sm'
                  onClick={() => setView('detail')}
                >
                  <LayoutList className='mr-2 h-4 w-4' />
                  Detail view
                </Button>
              </div>
              <Button
                type='button'
                variant='outline'
                onClick={() => searchQuery.refetch()}
                disabled={searchQuery.isFetching}
              >
                <RefreshCw
                  className={cn(
                    'mr-2 h-4 w-4',
                    searchQuery.isFetching && 'animate-spin'
                  )}
                />
                Refresh
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

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
        parentId={parentId}
        assigneeIds={assigneeIds}
        issueTypeIds={issueTypeIds}
        statusIds={statusIds}
        priorityIds={priorityIds}
        reporterIds={reporterIds}
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
      dueDate={data?.dueDate ?? fallbackItem?.dueDate}
      updatedAt={data?.updatedAt ?? fallbackItem?.updatedAt}
    />
  );
}
