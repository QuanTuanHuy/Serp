/**
 * PTM - Tasks Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Centralized task filtering, sorting, and pagination
 */

'use client';

import { useMemo, useState } from 'react';
import { useGetTasksQuery } from '../api';
import type { Task, TaskStatus, TaskPriority } from '../types';

export interface UseTasksOptions {
  projectId?: number | string;
  parentTaskId?: number | string;
  status?: TaskStatus;
  priority?: TaskPriority;
  category?: string;
  tags?: string[];
  isDeepWork?: boolean;
  isMeeting?: boolean;
  isRecurring?: boolean;
  deadlineFrom?: number;
  deadlineTo?: number;
  initialPageSize?: number;
  initialSortBy?: 'deadline' | 'priority' | 'created' | 'title' | 'id';
  initialSortOrder?: 'ASC' | 'DESC';
}

export interface UseTasksResult {
  // Data
  tasks: Task[];
  totalItems: number;
  totalPages: number;
  isLoading: boolean;

  // Filters
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  statusFilter: TaskStatus | 'ALL';
  setStatusFilter: (status: TaskStatus | 'ALL') => void;
  priorityFilter: TaskPriority | 'ALL';
  setPriorityFilter: (priority: TaskPriority | 'ALL') => void;
  categoryFilter: string | 'ALL';
  setCategoryFilter: (category: string | 'ALL') => void;

  // Sort
  sortBy: 'deadline' | 'priority' | 'created' | 'title' | 'id';
  setSortBy: (
    sort: 'deadline' | 'priority' | 'created' | 'title' | 'id'
  ) => void;
  sortOrder: 'ASC' | 'DESC';
  setSortOrder: (order: 'ASC' | 'DESC') => void;

  // Pagination
  page: number;
  setPage: (page: number) => void;
  pageSize: number;
  setPageSize: (size: number) => void;

  // Actions
  clearFilters: () => void;
  refetch: () => void;
}

const SORT_FIELD_MAP: Record<string, string> = {
  deadline: 'deadline_ms',
  priority: 'priority',
  created: 'created_at',
  title: 'title',
  id: 'id',
};

export function useTasks(options: UseTasksOptions = {}): UseTasksResult {
  const {
    projectId,
    parentTaskId,
    status: initialStatus,
    priority: initialPriority,
    category: initialCategory,
    tags,
    isDeepWork,
    isMeeting,
    isRecurring,
    deadlineFrom,
    deadlineTo,
    initialPageSize = 20,
    initialSortBy = 'created',
    initialSortOrder = 'DESC',
  } = options;

  // Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<TaskStatus | 'ALL'>(
    initialStatus || 'ALL'
  );
  const [priorityFilter, setPriorityFilter] = useState<TaskPriority | 'ALL'>(
    initialPriority || 'ALL'
  );
  const [categoryFilter, setCategoryFilter] = useState<string | 'ALL'>(
    initialCategory || 'ALL'
  );

  // Sort state
  const [sortBy, setSortBy] = useState<
    'deadline' | 'priority' | 'created' | 'title' | 'id'
  >(initialSortBy);
  const [sortOrder, setSortOrder] = useState<'ASC' | 'DESC'>(initialSortOrder);

  // Pagination state
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(initialPageSize);

  // API call with server-side filters and sort
  const {
    data: paginatedData,
    isLoading,
    refetch,
  } = useGetTasksQuery({
    projectId: projectId ? Number(projectId) : undefined,
    parentTaskId: parentTaskId ? Number(parentTaskId) : undefined,
    status: statusFilter !== 'ALL' ? statusFilter : undefined,
    priority: priorityFilter !== 'ALL' ? priorityFilter : undefined,
    category: categoryFilter !== 'ALL' ? categoryFilter : undefined,
    tags,
    isDeepWork,
    isMeeting,
    isRecurring,
    deadlineFrom,
    deadlineTo,
    page,
    pageSize,
    sortBy: SORT_FIELD_MAP[sortBy],
    sortOrder,
  });

  const apiTasks = paginatedData?.data?.items || [];
  const totalItems = paginatedData?.data?.totalItems || 0;
  const totalPages = paginatedData?.data?.totalPages || 0;

  // Client-side filtering for search (API doesn't support search yet)
  const tasks = useMemo(() => {
    let filtered = [...apiTasks];

    // Search filter (client-side)
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (task) =>
          task.title.toLowerCase().includes(query) ||
          task.description?.toLowerCase().includes(query) ||
          task.tags?.some((tag: string) => tag.toLowerCase().includes(query))
      );
    }

    return filtered;
  }, [apiTasks, searchQuery]);

  const clearFilters = () => {
    setSearchQuery('');
    setStatusFilter('ALL');
    setPriorityFilter('ALL');
    setCategoryFilter('ALL');
    setPage(0);
  };

  return {
    // Data
    tasks,
    totalItems,
    totalPages,
    isLoading,

    // Filters
    searchQuery,
    setSearchQuery,
    statusFilter,
    setStatusFilter,
    priorityFilter,
    setPriorityFilter,
    categoryFilter,
    setCategoryFilter,

    // Sort
    sortBy,
    setSortBy,
    sortOrder,
    setSortOrder,

    // Pagination
    page,
    setPage,
    pageSize,
    setPageSize,

    // Actions
    clearFilters,
    refetch,
  };
}
