/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM projects enterprise list page
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { FolderKanban, Plus } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { useDebounce } from '@/shared/hooks/use-debounce';
import { Button } from '@/shared/components/ui';
import {
  useGetPmProjectsQuery,
  useGetProjectCategoriesQuery,
} from '../api/projectApi';
import type { PMProjectSummaryApi } from '../types/api';
import type {
  PMProjectListItem,
  PMProjectSort,
  PMProjectViewMode,
} from '../types/project-list.types';
import {
  type PMProjectCategoryFilter,
  PMProjectListToolbar,
  type PMProjectStatusFilter,
} from '../components/projects/PMProjectListToolbar';
import { PMProjectListFilters } from '../components/projects/PMProjectListFilters';
import { PMProjectListGrid } from '../components/projects/PMProjectListGrid';
import { PMProjectListTable } from '../components/projects/PMProjectListTable';

const PAGE_SIZE = 6;

function mapSortToApi(sortBy: PMProjectSort) {
  if (sortBy === 'name') {
    return { sortBy: 'name', sortDirection: 'asc' as const };
  }

  if (sortBy === 'createdDate') {
    return { sortBy: 'createdAt', sortDirection: 'desc' as const };
  }

  return { sortBy: 'updatedAt', sortDirection: 'desc' as const };
}

function mapStatusToArchived(statusFilter: PMProjectStatusFilter) {
  if (statusFilter === 'ACTIVE') {
    return false;
  }

  if (statusFilter === 'ARCHIVED') {
    return true;
  }

  return undefined;
}

function mapProjectSummaryToListItem(
  project: PMProjectSummaryApi
): PMProjectListItem {
  const fallbackTimestamp = new Date().toISOString();

  return {
    id: String(project.id),
    name: project.name,
    key: project.key,
    description: project.description?.trim() || 'No description provided.',
    category: project.categoryName?.trim() || 'Uncategorized',
    status: project.isArchived ? 'ARCHIVED' : 'ACTIVE',
    lead: {
      id:
        typeof project.leadUserId === 'number'
          ? String(project.leadUserId)
          : '',
      name:
        project.leadUserName?.trim() ||
        (typeof project.leadUserId === 'number'
          ? `User #${project.leadUserId}`
          : 'Unassigned'),
    },
    updatedAt: project.updatedAt || project.createdAt || fallbackTimestamp,
    createdAt: project.createdAt || project.updatedAt || fallbackTimestamp,
  };
}

export function PMProjectsPage() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] =
    useState<PMProjectCategoryFilter>('ALL');
  const [statusFilter, setStatusFilter] =
    useState<PMProjectStatusFilter>('ALL');
  const [sortBy, setSortBy] = useState<PMProjectSort>('recentlyUpdated');
  const [viewMode, setViewMode] = useState<PMProjectViewMode>('grid');
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const debouncedSearchQuery = useDebounce(searchQuery, 400);
  const sortConfig = useMemo(() => mapSortToApi(sortBy), [sortBy]);
  const archivedFilter = useMemo(
    () => mapStatusToArchived(statusFilter),
    [statusFilter]
  );

  const { data: categoriesResponse, isLoading: isCategoryLoading } =
    useGetProjectCategoriesQuery({ page: 0, pageSize: 100 });
  const {
    data: projectsResponse,
    isLoading: isProjectsLoading,
    isFetching: isProjectsFetching,
    error: projectsError,
  } = useGetPmProjectsQuery({
    page: currentPage - 1,
    pageSize: PAGE_SIZE,
    projectTypeKey: 'software',
    search: debouncedSearchQuery.trim() || undefined,
    categoryId: categoryFilter === 'ALL' ? undefined : Number(categoryFilter),
    archived: archivedFilter,
    sortBy: sortConfig.sortBy,
    sortDirection: sortConfig.sortDirection,
  });

  const categoryOptions = useMemo(
    () =>
      [...(categoriesResponse?.data.items || [])]
        .sort((left, right) => left.name.localeCompare(right.name))
        .map((category) => ({
          id: String(category.id),
          name: category.name,
        })),
    [categoriesResponse]
  );

  const filteredProjects = useMemo(
    () => (projectsResponse?.data.items || []).map(mapProjectSummaryToListItem),
    [projectsResponse]
  );

  const totalPages = Math.max(1, projectsResponse?.data.totalPages || 1);
  const totalFilteredCount = projectsResponse?.data.totalItems || 0;
  const totalProjectsCount = totalFilteredCount;

  useEffect(() => {
    setCurrentPage(1);
  }, [debouncedSearchQuery, categoryFilter, statusFilter, sortBy]);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const hasActiveFilters =
    searchQuery.trim().length > 0 ||
    categoryFilter !== 'ALL' ||
    statusFilter !== 'ALL';
  const activeFilterCount =
    (categoryFilter === 'ALL' ? 0 : 1) + (statusFilter === 'ALL' ? 0 : 1);

  const tableEmptyTitle = projectsError
    ? 'Unable to load projects'
    : 'No projects match the current filters';
  const tableEmptyDescription = projectsError
    ? getErrorMessage(projectsError)
    : 'Try adjusting your search, status, or category filters.';

  const clearFilters = () => {
    setSearchQuery('');
    setCategoryFilter('ALL');
    setStatusFilter('ALL');
    setSortBy('recentlyUpdated');
    setCurrentPage(1);
  };

  const handleOpenProject = (projectId: string) => {
    router.push(`/pm/projects/${projectId}/summary`);
  };

  const handleEditProject = (projectId: string) => {
    router.push(`/pm/projects/${projectId}/edit`);
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between border-b border-border/40 pb-5'>
        <div className='space-y-1.5'>
          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-tr from-primary/15 to-violet-500/15 border border-primary/20 text-primary shadow-sm shadow-primary/5'>
              <FolderKanban className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-extrabold tracking-tight bg-gradient-to-r from-foreground via-foreground/90 to-muted-foreground bg-clip-text text-transparent'>
                Projects
              </h1>
              <p className='text-xs text-muted-foreground mt-0.5'>
                Manage company software projects across teams, workflows, and
                delivery lanes.
              </p>
            </div>
          </div>
        </div>

        <Button
          type='button'
          size='lg'
          className='shadow-md hover:shadow-primary/15 transition-all hover:scale-[1.02] active:scale-[0.98] duration-200 bg-primary hover:bg-primary/95 text-primary-foreground font-semibold rounded-xl px-5 h-11'
          onClick={() => router.push('/pm/projects/create')}
        >
          <Plus className='mr-2 h-4.5 w-4.5 stroke-[2.5]' />
          Create project
        </Button>
      </div>

      <PMProjectListToolbar
        searchQuery={searchQuery}
        onSearchQueryChange={setSearchQuery}
        categoryFilter={categoryFilter}
        categoryOptions={categoryOptions}
        statusFilter={statusFilter}
        sortBy={sortBy}
        onSortByChange={setSortBy}
        viewMode={viewMode}
        onViewModeChange={setViewMode}
        activeFilterCount={activeFilterCount}
        onOpenFilters={() => setFiltersOpen(true)}
        hasActiveFilters={hasActiveFilters}
        onClearFilters={clearFilters}
        resultCount={totalFilteredCount}
        totalCount={totalProjectsCount}
      />

      <PMProjectListFilters
        open={filtersOpen}
        categoryFilter={categoryFilter}
        categoryOptions={categoryOptions}
        statusFilter={statusFilter}
        onOpenChange={setFiltersOpen}
        onCategoryFilterChange={setCategoryFilter}
        onStatusFilterChange={setStatusFilter}
        onClear={clearFilters}
      />

      {viewMode === 'list' ? (
        <PMProjectListTable
          projects={filteredProjects}
          isLoading={
            isProjectsLoading || isProjectsFetching || isCategoryLoading
          }
          currentPage={currentPage}
          pageSize={PAGE_SIZE}
          totalCount={totalProjectsCount}
          totalFilteredCount={totalFilteredCount}
          totalPages={totalPages}
          emptyTitle={tableEmptyTitle}
          emptyDescription={tableEmptyDescription}
          onPageChange={setCurrentPage}
          onOpen={handleOpenProject}
          onEdit={handleEditProject}
        />
      ) : (
        <PMProjectListGrid
          projects={filteredProjects}
          isLoading={
            isProjectsLoading || isProjectsFetching || isCategoryLoading
          }
          currentPage={currentPage}
          pageSize={PAGE_SIZE}
          totalCount={totalProjectsCount}
          totalFilteredCount={totalFilteredCount}
          totalPages={totalPages}
          emptyTitle={tableEmptyTitle}
          emptyDescription={tableEmptyDescription}
          onPageChange={setCurrentPage}
          onOpen={handleOpenProject}
          onEdit={handleEditProject}
        />
      )}
    </div>
  );
}
