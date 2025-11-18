/**
 * PTM v2 - Projects Page (Modern Design)
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Modern project management interface
 */

'use client';

import { useState } from 'react';
import {
  ProjectGrid,
  ProjectDetailView,
  CreateProjectDialog,
} from '@/modules/ptmv2';
import { Card } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import {
  FolderKanban,
  Plus,
  Target,
  CheckCircle2,
  TrendingUp,
  Filter,
  Star,
  Calendar,
  Sparkles,
  Grid3x3,
  List,
} from 'lucide-react';

export default function ProjectsPage() {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(
    null
  );

  // If project is selected, show detail view
  if (selectedProjectId) {
    return (
      <ProjectDetailView
        projectId={selectedProjectId}
        onClose={() => setSelectedProjectId(null)}
      />
    );
  }

  return (
    <div className='space-y-6'>
      {/* Clean Modern Header */}
      <div className='flex items-center justify-between'>
        <div>
          <div className='flex items-center gap-3 mb-2'>
            <div className='p-2 bg-primary/10 rounded-lg'>
              <FolderKanban className='h-6 w-6 text-primary' />
            </div>
            <h1 className='text-3xl font-bold tracking-tight'>Projects</h1>
          </div>
          <p className='text-muted-foreground'>
            Organize and track your projects from start to finish
          </p>
        </div>
        <CreateProjectDialog
          trigger={
            <Button size='lg'>
              <Plus className='h-4 w-4 mr-2' />
              New Project
            </Button>
          }
        />
      </div>

      {/* Stats Overview */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Active Projects</p>
              <p className='text-2xl font-bold'>8</p>
            </div>
            <div className='p-3 rounded-full bg-primary/10'>
              <FolderKanban className='h-5 w-5 text-primary' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Completed</p>
              <p className='text-2xl font-bold'>12</p>
            </div>
            <div className='p-3 rounded-full bg-green-500/10'>
              <CheckCircle2 className='h-5 w-5 text-green-600 dark:text-green-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Total Tasks</p>
              <p className='text-2xl font-bold'>142</p>
            </div>
            <div className='p-3 rounded-full bg-blue-500/10'>
              <Target className='h-5 w-5 text-blue-600 dark:text-blue-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Success Rate</p>
              <p className='text-2xl font-bold'>92%</p>
            </div>
            <div className='p-3 rounded-full bg-amber-500/10'>
              <TrendingUp className='h-5 w-5 text-amber-600 dark:text-amber-400' />
            </div>
          </div>
        </Card>
      </div>

      {/* Quick Filters */}
      <div className='flex flex-wrap items-center gap-2'>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          All Projects
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          ⭐ Favorites
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          🔴 High Priority
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          ⏰ Due Soon
        </Badge>
        <Badge
          variant='outline'
          className='cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors'
        >
          🚀 In Progress
        </Badge>
      </div>

      {/* Toolbar */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Button variant='outline' size='sm'>
            <Filter className='h-4 w-4 mr-2' />
            Filter
          </Button>
          <Button variant='outline' size='sm'>
            <Star className='h-4 w-4 mr-2' />
            Show Favorites
          </Button>
          <Button variant='outline' size='sm'>
            <Calendar className='h-4 w-4 mr-2' />
            Sort by Deadline
          </Button>
          <Button variant='outline' size='sm'>
            <Sparkles className='h-4 w-4 mr-2' />
            AI Prioritize
          </Button>
        </div>

        <div className='flex items-center gap-1 bg-muted p-1 rounded-lg'>
          <Button
            variant={viewMode === 'grid' ? 'default' : 'ghost'}
            size='sm'
            onClick={() => setViewMode('grid')}
            className='h-8'
          >
            <Grid3x3 className='h-4 w-4' />
          </Button>
          <Button
            variant={viewMode === 'list' ? 'default' : 'ghost'}
            size='sm'
            onClick={() => setViewMode('list')}
            className='h-8'
          >
            <List className='h-4 w-4' />
          </Button>
        </div>
      </div>

      {/* Project Grid */}
      <ProjectGrid onProjectClick={(id) => setSelectedProjectId(id)} />

      {/* AI Insights */}
      <Card className='p-4 border-primary/20'>
        <div className='flex items-start gap-3'>
          <Sparkles className='h-5 w-5 text-primary mt-0.5 flex-shrink-0' />
          <div className='space-y-1'>
            <p className='font-semibold text-sm'>💡 Project Insights</p>
            <p className='text-sm text-muted-foreground'>
              "Website Redesign" is 85% complete with 3 tasks remaining.
              Consider scheduling a final review meeting for next week to ensure
              on-time delivery.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}
