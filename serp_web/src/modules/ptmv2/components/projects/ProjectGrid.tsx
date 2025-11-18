/**
 * PTM v2 - Project Grid Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Grid layout for projects
 */

'use client';

import { useState, useMemo } from 'react';
import { Filter, Search } from 'lucide-react';
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
import { CreateProjectDialog } from './CreateProjectDialog';
import { useGetProjectsQuery } from '../../services/projectApi';
import type { ProjectStatus } from '../../types';
import { Skeleton } from '@/shared/components/ui/skeleton';

interface ProjectGridProps {
  className?: string;
  onProjectClick?: (projectId: string) => void;
}

export function ProjectGrid({ className, onProjectClick }: ProjectGridProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<ProjectStatus | 'ALL'>(
    'ALL'
  );
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);

  const { data: projects = [], isLoading } = useGetProjectsQuery({});

  // Filter projects
  const filteredProjects = useMemo(() => {
    let filtered = [...projects];

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (project) =>
          project.title.toLowerCase().includes(query) ||
          project.description?.toLowerCase().includes(query)
      );
    }

    // Status filter
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((project) => project.status === statusFilter);
    }

    // Favorites filter
    if (showFavoritesOnly) {
      filtered = filtered.filter((project) => project.isFavorite);
    }

    // Sort by favorites first, then by updated date
    filtered.sort((a, b) => {
      if (a.isFavorite && !b.isFavorite) return -1;
      if (!a.isFavorite && b.isFavorite) return 1;
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });

    return filtered;
  }, [projects, searchQuery, statusFilter, showFavoritesOnly]);

  if (isLoading) {
    return (
      <div className={className}>
        <div className='flex items-center justify-between mb-6'>
          <h2 className='text-2xl font-bold'>Projects</h2>
          <CreateProjectDialog />
        </div>{' '}
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
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <div>
          <h2 className='text-2xl font-bold'>Projects</h2>
          {filteredProjects.length > 0 && (
            <p className='text-sm text-muted-foreground mt-1'>
              {filteredProjects.length}{' '}
              {filteredProjects.length === 1 ? 'project' : 'projects'}
            </p>
          )}
        </div>
        <CreateProjectDialog />
      </div>

      {/* Filters */}
      <div className='space-y-3 mb-6'>
        {/* Search */}
        <div className='relative'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder='Search projects...'
            className='pl-9'
          />
        </div>

        {/* Filters Row */}
        <div className='flex flex-wrap items-center gap-2'>
          <Select
            value={statusFilter}
            onValueChange={(value) =>
              setStatusFilter(value as ProjectStatus | 'ALL')
            }
          >
            <SelectTrigger className='w-[140px]'>
              <SelectValue placeholder='Status' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ALL'>All Status</SelectItem>
              <SelectItem value='ACTIVE'>Active</SelectItem>
              <SelectItem value='COMPLETED'>Completed</SelectItem>
              <SelectItem value='ON_HOLD'>On Hold</SelectItem>
              <SelectItem value='ARCHIVED'>Archived</SelectItem>
            </SelectContent>
          </Select>

          <Button
            variant={showFavoritesOnly ? 'default' : 'outline'}
            size='sm'
            onClick={() => setShowFavoritesOnly(!showFavoritesOnly)}
          >
            <Filter className='mr-2 h-4 w-4' />
            Favorites Only
          </Button>
        </div>
      </div>

      {/* Project Grid */}
      {filteredProjects.length === 0 ? (
        <div className='text-center py-12 text-muted-foreground'>
          <p className='text-lg font-medium'>No projects found</p>
          <p className='text-sm mt-1'>
            {searchQuery || statusFilter !== 'ALL' || showFavoritesOnly
              ? 'Try adjusting your filters'
              : 'Create your first project to get started'}
          </p>
        </div>
      ) : (
        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
          {filteredProjects.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              onClick={onProjectClick}
            />
          ))}
        </div>
      )}
    </div>
  );
}
