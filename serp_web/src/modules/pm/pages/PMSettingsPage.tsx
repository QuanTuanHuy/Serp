/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings shell page
 */

'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  FolderKanban,
  ListFilter,
  Settings2,
  Tag,
  Workflow,
} from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  ScrollArea,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetPmIssueTypesQuery,
  useGetPmPrioritiesQuery,
  useGetPmProjectsQuery,
  useGetPmStatusesQuery,
} from '../api';
import type {
  PMIssueTypeApi,
  PMPriorityApi,
  PMProjectSummaryApi,
  PMStatusApi,
} from '../types/api';

type PMSettingsScope = 'work-items' | 'projects';
type PMSettingsSection = 'issue-types' | 'statuses' | 'priorities' | 'projects';

type SettingsMenuItem = {
  key: PMSettingsSection;
  title: string;
  description: string;
  icon: typeof Settings2;
  group: string;
};

const WORK_ITEM_ITEMS: SettingsMenuItem[] = [
  {
    key: 'issue-types',
    title: 'Work types',
    description: 'Issue type catalog used by project teams.',
    icon: Tag,
    group: 'Work types',
  },
  {
    key: 'statuses',
    title: 'Statuses',
    description: 'Status catalog and status category mapping.',
    icon: Workflow,
    group: 'Workflows',
  },
  {
    key: 'priorities',
    title: 'Priorities',
    description: 'Priority catalog and sort order.',
    icon: ListFilter,
    group: 'Priorities',
  },
];

const PROJECT_ITEMS: SettingsMenuItem[] = [
  {
    key: 'projects',
    title: 'Project directory',
    description: 'Open project-level settings for each project.',
    icon: FolderKanban,
    group: 'Projects',
  },
];

function formatCount(value?: number | null) {
  return typeof value === 'number' ? value.toString() : '-';
}

function countBy<T>(items: T[], predicate: (item: T) => boolean) {
  return items.filter(predicate).length;
}

export function PMSettingsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [search, setSearch] = useState('');
  const scope = (searchParams.get('scope') as PMSettingsScope) || 'work-items';
  const section =
    (searchParams.get('section') as PMSettingsSection) ||
    (scope === 'projects' ? 'projects' : 'issue-types');

  const issueTypesQuery = useGetPmIssueTypesQuery({
    page: 0,
    pageSize: 100,
    sortBy: 'name',
    sortDirection: 'asc',
  });
  const prioritiesQuery = useGetPmPrioritiesQuery({
    page: 0,
    pageSize: 100,
    sortBy: 'sequence',
    sortDirection: 'asc',
  });
  const statusesQuery = useGetPmStatusesQuery({
    page: 0,
    pageSize: 100,
    sortBy: 'name',
    sortDirection: 'asc',
  });
  const projectsQuery = useGetPmProjectsQuery({
    page: 0,
    pageSize: 100,
    sortBy: 'updatedAt',
    sortDirection: 'desc',
  });

  const currentMenu = scope === 'projects' ? PROJECT_ITEMS : WORK_ITEM_ITEMS;
  const activeSection = currentMenu.some((item) => item.key === section)
    ? section
    : currentMenu[0].key;

  const updateUrl = (updates: Partial<Record<'scope' | 'section', string>>) => {
    const nextParams = new URLSearchParams(searchParams.toString());
    Object.entries(updates).forEach(([key, value]) => {
      if (value) {
        nextParams.set(key, value);
      } else {
        nextParams.delete(key);
      }
    });
    const query = nextParams.toString();
    router.replace(query ? `/pm/settings?${query}` : '/pm/settings', {
      scroll: false,
    });
  };

  const filteredMenu = useMemo(() => {
    const needle = search.trim().toLowerCase();
    if (!needle) return currentMenu;
    return currentMenu.filter((item) =>
      `${item.title} ${item.description} ${item.group}`
        .toLowerCase()
        .includes(needle)
    );
  }, [currentMenu, search]);

  const workItemsStats = useMemo(() => {
    const issueTypes = issueTypesQuery.data?.data.items ?? [];
    const priorities = prioritiesQuery.data?.data.items ?? [];
    const statuses = statusesQuery.data?.data.items ?? [];

    return {
      issueTypesTotal:
        issueTypesQuery.data?.data.totalItems ?? issueTypes.length,
      issueTypesSystem: countBy(issueTypes, (item) => item.isSystem),
      prioritiesTotal:
        prioritiesQuery.data?.data.totalItems ?? priorities.length,
      statusesTotal: statusesQuery.data?.data.totalItems ?? statuses.length,
      statusesSystem: countBy(statuses, (item) => item.isSystem),
    };
  }, [
    issueTypesQuery.data?.data.items,
    issueTypesQuery.data?.data.totalItems,
    prioritiesQuery.data?.data.items,
    prioritiesQuery.data?.data.totalItems,
    statusesQuery.data?.data.items,
    statusesQuery.data?.data.totalItems,
  ]);

  const projectStats = useMemo(() => {
    const projects = projectsQuery.data?.data.items ?? [];
    return {
      total: projectsQuery.data?.data.totalItems ?? projects.length,
      active: countBy(projects, (item) => !item.isArchived),
      archived: countBy(projects, (item) => item.isArchived),
    };
  }, [projectsQuery.data?.data.items, projectsQuery.data?.data.totalItems]);

  const sectionContent = (() => {
    if (scope === 'projects') {
      return {
        title: 'Project directory',
        description: 'Open a project-specific settings page from here.',
        loading: projectsQuery.isLoading,
        items: projectsQuery.data?.data.items ?? [],
        total: projectsQuery.data?.data.totalItems ?? 0,
        headers: ['Name', 'Key', 'Lead', 'Category', 'State', 'Action'],
        renderRow: (item: PMProjectSummaryApi) => (
          <>
            <TableCell className='font-medium'>{item.name}</TableCell>
            <TableCell>{item.key}</TableCell>
            <TableCell>
              {item.leadUserName ||
                (item.leadUserId ? `User #${item.leadUserId}` : '-')}
            </TableCell>
            <TableCell>{item.categoryName || '-'}</TableCell>
            <TableCell>
              <Badge variant={item.isArchived ? 'secondary' : 'outline'}>
                {item.isArchived ? 'Archived' : 'Active'}
              </Badge>
            </TableCell>
            <TableCell className='text-right'>
              <Button type='button' variant='outline' size='sm' asChild>
                <Link href={`/pm/projects/${item.id}/settings`}>
                  Open settings
                </Link>
              </Button>
            </TableCell>
          </>
        ),
      };
    }

    if (activeSection === 'priorities') {
      const items = prioritiesQuery.data?.data.items ?? [];
      return {
        title: 'Priorities',
        description:
          'Manage the priority catalog used in project boards and lists.',
        loading: prioritiesQuery.isLoading,
        items,
        total: prioritiesQuery.data?.data.totalItems ?? 0,
        headers: ['Name', 'Key', 'Color', 'Rank', 'Type', 'Action'],
        renderRow: (item: PMPriorityApi) => (
          <>
            <TableCell className='font-medium'>{item.name}</TableCell>
            <TableCell>{item.priorityKey}</TableCell>
            <TableCell>{item.color || '-'}</TableCell>
            <TableCell>{formatCount(item.sequence)}</TableCell>
            <TableCell>{item.isSystem ? 'System' : 'Custom'}</TableCell>
            <TableCell />
          </>
        ),
      };
    }

    if (activeSection === 'statuses') {
      const items = statusesQuery.data?.data.items ?? [];
      return {
        title: 'Statuses',
        description: 'Manage status labels and status category mapping.',
        loading: statusesQuery.isLoading,
        items,
        total: statusesQuery.data?.data.totalItems ?? 0,
        headers: ['Name', 'Key', 'Category', 'Status', 'Type', 'Action'],
        renderRow: (item: PMStatusApi) => (
          <>
            <TableCell className='font-medium'>{item.name}</TableCell>
            <TableCell>{item.statusKey}</TableCell>
            <TableCell>{formatCount(item.statusCategoryId)}</TableCell>
            <TableCell>{item.readOnly ? 'Read only' : 'Editable'}</TableCell>
            <TableCell>{item.isSystem ? 'System' : 'Custom'}</TableCell>
            <TableCell />
          </>
        ),
      };
    }

    const items = issueTypesQuery.data?.data.items ?? [];
    return {
      title: 'Work types',
      description:
        'Manage issue types used by projects and work item creation.',
      loading: issueTypesQuery.isLoading,
      items,
      total: issueTypesQuery.data?.data.totalItems ?? 0,
      headers: ['Name', 'Key', 'Hierarchy', 'Status', 'Type', 'Action'],
      renderRow: (item: PMIssueTypeApi) => (
        <>
          <TableCell className='font-medium'>{item.name}</TableCell>
          <TableCell>{item.typeKey}</TableCell>
          <TableCell>{formatCount(item.hierarchyLevel)}</TableCell>
          <TableCell>{item.readOnly ? 'Read only' : 'Editable'}</TableCell>
          <TableCell>{item.isSystem ? 'System' : 'Custom'}</TableCell>
          <TableCell />
        </>
      ),
    };
  })();

  return (
    <div className='grid gap-6 lg:grid-cols-[300px_minmax(0,1fr)]'>
      <aside className='space-y-4'>
        <div className='flex items-center gap-2'>
          <Button
            type='button'
            variant={scope === 'work-items' ? 'default' : 'outline'}
            className='justify-start gap-2'
            onClick={() =>
              updateUrl({
                scope: 'work-items',
                section: 'issue-types',
              })
            }
          >
            <Tag className='h-4 w-4' />
            Work items
          </Button>
          <Button
            type='button'
            variant={scope === 'projects' ? 'default' : 'outline'}
            className='justify-start gap-2'
            onClick={() =>
              updateUrl({
                scope: 'projects',
                section: 'projects',
              })
            }
          >
            <FolderKanban className='h-4 w-4' />
            Projects
          </Button>
        </div>

        <Card className='border-border/60 bg-background/90 shadow-sm'>
          <CardHeader className='border-b py-4'>
            <CardTitle className='text-sm'>Switch settings</CardTitle>
          </CardHeader>
          <CardContent className='p-2'>
            <ScrollArea className='h-[calc(100vh-14rem)] pr-2'>
              <div className='space-y-4 p-1'>
                <div className='relative'>
                  <Input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder='Search settings'
                    className='pl-3'
                  />
                </div>

                {filteredMenu.map((item) => {
                  const Icon = item.icon;
                  const active = item.key === activeSection;
                  return (
                    <div key={item.key} className='space-y-2'>
                      <p className='px-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
                        {item.group}
                      </p>
                      <button
                        type='button'
                        onClick={() => updateUrl({ section: item.key })}
                        className={cn(
                          'flex w-full items-start gap-3 rounded-md px-3 py-2 text-left transition-colors hover:bg-muted',
                          active && 'bg-primary/10 text-primary'
                        )}
                      >
                        <Icon className='mt-0.5 h-4 w-4 shrink-0' />
                        <span className='min-w-0'>
                          <span className='block text-sm font-medium'>
                            {item.title}
                          </span>
                          <span className='block text-xs text-muted-foreground'>
                            {item.description}
                          </span>
                        </span>
                      </button>
                    </div>
                  );
                })}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </aside>

      <main className='space-y-4'>
        <Card className='border-border/60 bg-background/90 shadow-sm'>
          <CardHeader className='flex flex-row items-center justify-between gap-3 border-b py-4'>
            <div className='space-y-1'>
              <CardTitle className='text-base'>
                {sectionContent.title}
              </CardTitle>
              <p className='text-sm text-muted-foreground'>
                {sectionContent.description}
              </p>
            </div>
            <Badge variant='secondary'>
              {scope === 'projects'
                ? `${projectStats.total} projects`
                : `${sectionContent.total} records`}
            </Badge>
          </CardHeader>

          <CardContent className='space-y-4 p-4'>
            {scope === 'work-items' ? (
              <div className='grid gap-4 md:grid-cols-3'>
                <MiniStat
                  title='Work types'
                  value={workItemsStats.issueTypesTotal}
                  detail={`${workItemsStats.issueTypesSystem} system`}
                />
                <MiniStat
                  title='Statuses'
                  value={workItemsStats.statusesTotal}
                  detail={`${workItemsStats.statusesSystem} system`}
                />
                <MiniStat
                  title='Priorities'
                  value={workItemsStats.prioritiesTotal}
                  detail='Catalog entries'
                />
              </div>
            ) : (
              <div className='grid gap-4 md:grid-cols-3'>
                <MiniStat
                  title='Projects'
                  value={projectStats.total}
                  detail='Total projects'
                />
                <MiniStat
                  title='Active'
                  value={projectStats.active}
                  detail='Open projects'
                />
                <MiniStat
                  title='Archived'
                  value={projectStats.archived}
                  detail='Closed projects'
                />
              </div>
            )}

            {sectionContent.loading ? (
              <div className='space-y-3'>
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className='h-10 rounded-md' />
                ))}
              </div>
            ) : sectionContent.items.length === 0 ? (
              <div className='rounded-md border border-dashed p-6 text-sm text-muted-foreground'>
                No records available.
              </div>
            ) : (
              <div className='overflow-hidden rounded-lg border'>
                <Table>
                  <TableHeader>
                    <TableRow>
                      {sectionContent.headers.map((header) => (
                        <TableHead
                          key={header}
                          className={
                            header === 'Action' ? 'text-right' : undefined
                          }
                        >
                          {header}
                        </TableHead>
                      ))}
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {sectionContent.items.map((item) => (
                      <TableRow key={item.id}>
                        {sectionContent.renderRow(item as never)}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  );
}

function MiniStat({
  title,
  value,
  detail,
}: {
  title: string;
  value: number;
  detail: string;
}) {
  return (
    <div className='rounded-md border bg-muted/20 px-3 py-3'>
      <p className='text-xs font-medium uppercase tracking-wide text-muted-foreground'>
        {title}
      </p>
      <div className='mt-1 text-2xl font-semibold'>{value}</div>
      <p className='mt-1 text-sm text-muted-foreground'>{detail}</p>
    </div>
  );
}
