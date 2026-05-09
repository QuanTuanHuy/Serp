/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM projects enterprise list page
 */

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { FolderKanban, Plus } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { PM_PROJECT_LIST_MOCKS } from '../mocks/projectList';
import type {
  PMProjectListItem,
  PMProjectSort,
} from '../types/project-list.types';
import {
  PMProjectListToolbar,
  type PMProjectStatusFilter,
  type PMProjectTemplateFilter,
} from '../components/projects/PMProjectListToolbar';
import { PMProjectListTable } from '../components/projects/PMProjectListTable';

function matchesProjectSearch(project: PMProjectListItem, query: string) {
  const normalizedQuery = query.trim().toLowerCase();

  if (!normalizedQuery) {
    return true;
  }

  return [
    project.name,
    project.key,
    project.description,
    project.category,
    project.lead.name,
  ].some((value) => value.toLowerCase().includes(normalizedQuery));
}

function sortProjects(projects: PMProjectListItem[], sortBy: PMProjectSort) {
  const items = [...projects];

  if (sortBy === 'name') {
    return items.sort((left, right) => left.name.localeCompare(right.name));
  }

  if (sortBy === 'createdDate') {
    return items.sort(
      (left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
    );
  }

  return items.sort(
    (left, right) =>
      new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime()
  );
}

export function PMProjectsPage() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [templateFilter, setTemplateFilter] =
    useState<PMProjectTemplateFilter>('ALL');
  const [statusFilter, setStatusFilter] =
    useState<PMProjectStatusFilter>('ALL');
  const [sortBy, setSortBy] = useState<PMProjectSort>('recentlyUpdated');

  const filteredProjects = useMemo(() => {
    const items = PM_PROJECT_LIST_MOCKS.filter((project) => {
      const matchesSearch = matchesProjectSearch(project, searchQuery);
      const matchesTemplate =
        templateFilter === 'ALL' || project.templateType === templateFilter;
      const matchesStatus =
        statusFilter === 'ALL' || project.status === statusFilter;

      return matchesSearch && matchesTemplate && matchesStatus;
    });

    return sortProjects(items, sortBy);
  }, [searchQuery, sortBy, statusFilter, templateFilter]);

  const hasActiveFilters =
    searchQuery.trim().length > 0 ||
    templateFilter !== 'ALL' ||
    statusFilter !== 'ALL';

  const activeProjectsCount = useMemo(
    () =>
      PM_PROJECT_LIST_MOCKS.filter((project) => project.status === 'ACTIVE')
        .length,
    []
  );

  const clearFilters = () => {
    setSearchQuery('');
    setTemplateFilter('ALL');
    setStatusFilter('ALL');
    setSortBy('recentlyUpdated');
  };

  const handleOpenProject = (projectId: string) => {
    router.push(`/pm/projects/${projectId}/summary`);
  };

  const handleEditProject = (projectId: string) => {
    router.push(`/pm/projects/${projectId}/edit`);
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-3'>
          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <FolderKanban className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>Projects</h1>
              <p className='text-sm text-muted-foreground'>
                Manage company software projects across teams, workflows, and
                delivery lanes.
              </p>
            </div>
          </div>

          <div className='flex flex-wrap items-center gap-3 text-sm text-muted-foreground'>
            <span>
              <span className='font-semibold text-foreground'>
                {PM_PROJECT_LIST_MOCKS.length}
              </span>{' '}
              total projects
            </span>
            <span className='hidden h-1 w-1 rounded-full bg-border sm:inline-block' />
            <span>
              <span className='font-semibold text-foreground'>
                {activeProjectsCount}
              </span>{' '}
              active projects
            </span>
          </div>
        </div>

        <Button
          type='button'
          size='lg'
          className='shadow-sm'
          onClick={() => router.push('/pm/projects/create')}
        >
          <Plus className='mr-2 h-4 w-4' />
          Create project
        </Button>
      </div>

      <PMProjectListToolbar
        searchQuery={searchQuery}
        onSearchQueryChange={setSearchQuery}
        templateFilter={templateFilter}
        onTemplateFilterChange={setTemplateFilter}
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        sortBy={sortBy}
        onSortByChange={setSortBy}
        hasActiveFilters={hasActiveFilters}
        onClearFilters={clearFilters}
        resultCount={filteredProjects.length}
        totalCount={PM_PROJECT_LIST_MOCKS.length}
      />

      <PMProjectListTable
        projects={filteredProjects}
        totalCount={PM_PROJECT_LIST_MOCKS.length}
        onOpen={handleOpenProject}
        onEdit={handleEditProject}
      />
    </div>
  );
}
