/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useState } from 'react';
import {
  Search,
  Grid3x3,
  List,
  ChevronLeft,
  ChevronRight,
  FolderKanban,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { ProjectCard } from './ProjectCard';
import { useProjects } from '../../hooks';
import type { ProjectStatus, ProjectPriority } from '../../types';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { cn } from '@/shared/utils';

interface ProjectGridProps {
  className?: string;
  onProjectClick?: (projectId: number) => void;
}

export function ProjectGrid({ className, onProjectClick }: ProjectGridProps) {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  const {
    projects,
    isLoading,
    searchQuery,
    setSearchQuery,
    statusFilter,
    setStatusFilter,
    priorityFilter,
    setPriorityFilter,
    currentPage,
    totalPages,
    handleNextPage,
    handlePreviousPage,
  } = useProjects();

  if (isLoading) {
    return (
      <div className={className}>
        <div className='flex items-center justify-between mb-6'>
          <Skeleton className='h-10 w-48' />
          <Skeleton className='h-10 w-32' />
        </div>
        <Skeleton className='h-12 w-full mb-4' />
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className='h-64 w-full' />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className={className}>
      {/* Compact Toolbar */}
      <div className='flex flex-col sm:flex-row sm:items-center gap-3 mb-6'>
        {/* Search */}
        <div className='relative flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder='Search projects...'
            className='pl-9 h-10'
          />
        </div>

        <div className='flex items-center gap-2 flex-wrap'>
          <Select
            value={statusFilter}
            onValueChange={(value) =>
              setStatusFilter(value as ProjectStatus | 'ALL')
            }
          >
            <SelectTrigger className='w-[130px] h-10'>
              <SelectValue placeholder='Status' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ALL'>All Status</SelectItem>
              <SelectItem value='NEW'>New</SelectItem>
              <SelectItem value='IN_PROGRESS'>In Progress</SelectItem>
              <SelectItem value='COMPLETED'>Completed</SelectItem>
              <SelectItem value='ON_HOLD'>On Hold</SelectItem>
              <SelectItem value='ARCHIVED'>Archived</SelectItem>
            </SelectContent>
          </Select>

          <Select
            value={priorityFilter}
            onValueChange={(value) =>
              setPriorityFilter(value as ProjectPriority | 'ALL')
            }
          >
            <SelectTrigger className='w-[130px] h-10'>
              <SelectValue placeholder='Priority' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ALL'>All Priority</SelectItem>
              <SelectItem value='HIGH'>High</SelectItem>
              <SelectItem value='MEDIUM'>Medium</SelectItem>
              <SelectItem value='LOW'>Low</SelectItem>
            </SelectContent>
          </Select>

          <div className='flex items-center gap-1 bg-muted p-1 rounded-lg'>
            <Button
              variant={viewMode === 'grid' ? 'default' : 'ghost'}
              size='sm'
              onClick={() => setViewMode('grid')}
              className='h-8 px-3'
            >
              <Grid3x3 className='h-4 w-4' />
            </Button>
            <Button
              variant={viewMode === 'list' ? 'default' : 'ghost'}
              size='sm'
              onClick={() => setViewMode('list')}
              className='h-8 px-3'
            >
              <List className='h-4 w-4' />
            </Button>
          </div>
        </div>
      </div>

      {projects.length > 0 && (
        <p className='text-sm text-muted-foreground mb-4'>
          Showing {projects.length}{' '}
          {projects.length === 1 ? 'project' : 'projects'}
        </p>
      )}

      {projects.length === 0 ? (
        <div className='text-center py-16 text-muted-foreground border-2 border-dashed rounded-lg'>
          <FolderKanban className='h-12 w-12 mx-auto mb-4 opacity-50' />
          <p className='text-lg font-medium'>No projects found</p>
          <p className='text-sm mt-1'>
            {searchQuery || statusFilter !== 'ALL' || priorityFilter !== 'ALL'
              ? 'Try adjusting your filters'
              : 'Create your first project to get started'}
          </p>
        </div>
      ) : (
        <>
          <div
            className={cn(
              viewMode === 'grid'
                ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'
                : 'flex flex-col gap-3'
            )}
          >
            {projects.map((project) => (
              <ProjectCard
                key={project.id}
                project={project}
                onClick={onProjectClick}
                viewMode={viewMode}
              />
            ))}
          </div>

          {totalPages > 1 && (
            <div className='flex items-center justify-between mt-6 pt-4 border-t'>
              <p className='text-sm text-muted-foreground'>
                Page {currentPage + 1} of {totalPages}
              </p>
              <div className='flex items-center gap-2'>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={handlePreviousPage}
                  disabled={currentPage === 0}
                >
                  <ChevronLeft className='h-4 w-4 mr-1' />
                  Previous
                </Button>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={handleNextPage}
                  disabled={currentPage >= totalPages - 1}
                >
                  Next
                  <ChevronRight className='h-4 w-4 ml-1' />
                </Button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
