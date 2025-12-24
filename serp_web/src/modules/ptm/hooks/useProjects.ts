/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useState, useMemo } from 'react';
import { useGetProjectsQuery } from '../services/projectApi';
import type { ProjectStatus, ProjectPriority, Project } from '../types';

interface UseProjectsOptions {
  pageSize?: number;
  initialStatus?: ProjectStatus | 'ALL';
  initialPriority?: ProjectPriority | 'ALL';
}

export function useProjects(options: UseProjectsOptions = {}) {
  const {
    pageSize = 12,
    initialStatus = 'ALL',
    initialPriority = 'ALL',
  } = options;

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<ProjectStatus | 'ALL'>(
    initialStatus
  );
  const [priorityFilter, setPriorityFilter] = useState<ProjectPriority | 'ALL'>(
    initialPriority
  );
  const [currentPage, setCurrentPage] = useState(0);

  const {
    data: response,
    isLoading,
    isFetching,
  } = useGetProjectsQuery({
    status: statusFilter !== 'ALL' ? statusFilter : undefined,
    priority: priorityFilter !== 'ALL' ? priorityFilter : undefined,
    page: currentPage,
    pageSize,
  });

  const projects = response?.data.items || [];
  const totalItems = response?.data.totalItems || 0;
  const totalPages = response?.data.totalPages || 0;

  const filteredProjects = useMemo(() => {
    if (!searchQuery) return projects;

    return projects.filter(
      (project) =>
        project.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        project.description?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [projects, searchQuery]);

  const handleStatusFilterChange = (status: ProjectStatus | 'ALL') => {
    setStatusFilter(status);
    setCurrentPage(0);
  };

  const handlePriorityFilterChange = (priority: ProjectPriority | 'ALL') => {
    setPriorityFilter(priority);
    setCurrentPage(0);
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage((prev) => prev + 1);
    }
  };

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      setCurrentPage((prev) => prev - 1);
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  return {
    projects: filteredProjects,
    allProjects: projects,
    isLoading,
    isFetching,
    searchQuery,
    setSearchQuery,
    statusFilter,
    setStatusFilter: handleStatusFilterChange,
    priorityFilter,
    setPriorityFilter: handlePriorityFilterChange,
    currentPage,
    totalPages,
    totalItems,
    pageSize,
    handleNextPage,
    handlePreviousPage,
    handlePageChange,
  };
}
