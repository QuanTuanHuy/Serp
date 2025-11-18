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
      {/* Header with Gradient Background */}
      <div className='relative overflow-hidden rounded-lg bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 p-8'>
        <div className='absolute inset-0 bg-grid-white/10' />
        <div className='relative z-10 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between'>
          <div className='text-white'>
            <div className='flex items-center gap-2 mb-2'>
              <FolderKanban className='h-8 w-8' />
              <h1 className='text-3xl font-bold'>Projects</h1>
            </div>
            <p className='text-white/90 text-sm'>
              Organize and track your projects from start to finish
            </p>
          </div>
          <CreateProjectDialog
            trigger={
              <Button className='bg-white text-indigo-600 hover:bg-white/90 shadow-lg'>
                <Plus className='h-4 w-4 mr-2' />
                New Project
              </Button>
            }
          />
        </div>
      </div>

      {/* Stats Overview */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <Card className='p-4 bg-gradient-to-br from-indigo-50 to-indigo-100 dark:from-indigo-950/20 dark:to-indigo-900/20 border-indigo-200 dark:border-indigo-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Active Projects</p>
              <p className='text-2xl font-bold text-indigo-600 dark:text-indigo-400'>
                8
              </p>
            </div>
            <div className='p-3 rounded-full bg-indigo-500/10'>
              <FolderKanban className='h-5 w-5 text-indigo-600 dark:text-indigo-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-green-50 to-green-100 dark:from-green-950/20 dark:to-green-900/20 border-green-200 dark:border-green-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Completed</p>
              <p className='text-2xl font-bold text-green-600 dark:text-green-400'>
                12
              </p>
            </div>
            <div className='p-3 rounded-full bg-green-500/10'>
              <CheckCircle2 className='h-5 w-5 text-green-600 dark:text-green-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-950/20 dark:to-purple-900/20 border-purple-200 dark:border-purple-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Total Tasks</p>
              <p className='text-2xl font-bold text-purple-600 dark:text-purple-400'>
                142
              </p>
            </div>
            <div className='p-3 rounded-full bg-purple-500/10'>
              <Target className='h-5 w-5 text-purple-600 dark:text-purple-400' />
            </div>
          </div>
        </Card>

        <Card className='p-4 bg-gradient-to-br from-amber-50 to-amber-100 dark:from-amber-950/20 dark:to-amber-900/20 border-amber-200 dark:border-amber-800'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground'>Success Rate</p>
              <p className='text-2xl font-bold text-amber-600 dark:text-amber-400'>
                92%
              </p>
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
      <Card className='p-4 bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-950/10 dark:to-purple-950/10 border-indigo-200 dark:border-indigo-800'>
        <div className='flex items-start gap-3'>
          <Sparkles className='h-5 w-5 text-indigo-600 dark:text-indigo-400 mt-0.5 flex-shrink-0' />
          <div className='space-y-1'>
            <p className='font-semibold text-sm text-indigo-900 dark:text-indigo-100'>
              💡 Project Insights
            </p>
            <p className='text-sm text-indigo-700 dark:text-indigo-300'>
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
