/**
 * PTM v2 - Projects Page (Optimized)
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Clean, focused project management
 */

'use client';

import { useState } from 'react';
import {
  ProjectGrid,
  ProjectDetailView,
  CreateProjectDialog,
} from '@/modules/ptm';
import { Card } from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { FolderKanban, Plus, CheckCircle2, Clock } from 'lucide-react';
import { useGetProjectsQuery } from '@/modules/ptm/services/projectApi';

export default function ProjectsPage() {
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(
    null
  );

  const { data: projects = [] } = useGetProjectsQuery({});

  // Calculate stats from real data
  const activeProjects = projects.filter((p) => p.status === 'ACTIVE').length;
  const completedProjects = projects.filter(
    (p) => p.status === 'COMPLETED'
  ).length;
  const totalHours = projects.reduce(
    (sum, p) => sum + (p.estimatedHours || 0),
    0
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
          <p className='text-muted-foreground text-sm'>
            Organize your work into focused projects
          </p>
        </div>
        <CreateProjectDialog
          trigger={
            <Button
              size='lg'
              className='shadow-md hover:shadow-lg transition-shadow'
            >
              <Plus className='h-4 w-4 mr-2' />
              New Project
            </Button>
          }
        />
      </div>

      {/* Mini Stats - 3 cards only */}
      <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
        <Card className='p-5 hover:shadow-md transition-shadow'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground font-medium'>
                Active Projects
              </p>
              <p className='text-3xl font-bold mt-1'>{activeProjects}</p>
            </div>
            <div className='p-3 rounded-full bg-primary/10'>
              <FolderKanban className='h-6 w-6 text-primary' />
            </div>
          </div>
        </Card>

        <Card className='p-5 hover:shadow-md transition-shadow'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground font-medium'>
                Completed
              </p>
              <p className='text-3xl font-bold mt-1'>{completedProjects}</p>
            </div>
            <div className='p-3 rounded-full bg-green-500/10'>
              <CheckCircle2 className='h-6 w-6 text-green-600 dark:text-green-400' />
            </div>
          </div>
        </Card>

        <Card className='p-5 hover:shadow-md transition-shadow'>
          <div className='flex items-center justify-between'>
            <div>
              <p className='text-sm text-muted-foreground font-medium'>
                Total Hours
              </p>
              <p className='text-3xl font-bold mt-1'>{totalHours}h</p>
            </div>
            <div className='p-3 rounded-full bg-blue-500/10'>
              <Clock className='h-6 w-6 text-blue-600 dark:text-blue-400' />
            </div>
          </div>
        </Card>
      </div>

      {/* Project Grid with integrated filters */}
      <ProjectGrid onProjectClick={(id) => setSelectedProjectId(id)} />
    </div>
  );
}
