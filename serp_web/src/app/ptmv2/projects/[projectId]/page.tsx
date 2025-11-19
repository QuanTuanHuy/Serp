/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useSearchParams, useRouter } from 'next/navigation';
import { use, useEffect, useState } from 'react';
import { ArrowLeft, Calendar, Clock, CheckCircle2 } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Badge } from '@/shared/components/ui/badge';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Progress } from '@/shared/components/ui/progress';
import { useGetProjectQuery } from '@/modules/ptmv2/services/projectApi';
import { useGetTasksQuery } from '@/modules/ptmv2/services/taskApi';
import { useGetEntityActivitiesQuery } from '@/modules/ptmv2/services/activityApi';
import { EditProjectDialog } from '@/modules/ptmv2/components/projects/EditProjectDialog';
import { ProjectActivityTimeline } from '@/modules/ptmv2/components/projects';
import { TaskList } from '@/modules/ptmv2/components/tasks/TaskList';
import { PriorityBadge } from '@/modules/ptmv2/components/shared/PriorityBadge';
import { DependencyGraph } from '@/modules/ptmv2/components/tasks/DependencyGraph';
import { GanttView } from '@/modules/ptmv2/components/schedule/GanttView';
import { Network, GanttChart } from 'lucide-react';

export default function ProjectDetailPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const unwrappedParams = use(params);
  const projectId = parseInt(unwrappedParams.projectId, 10);

  const [activeTab, setActiveTab] = useState('details');
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const highlightActivityId = searchParams.get('highlightActivity');
  const editParam = searchParams.get('editProject');
  const tab = searchParams.get('tab');

  const { data: project, isLoading } = useGetProjectQuery(projectId);
  const { data: projectTasks = [], isLoading: isLoadingTasks } =
    useGetTasksQuery({ projectId }, { skip: !projectId });
  const { data: activities = [] } = useGetEntityActivitiesQuery(
    {
      entityType: 'project',
      entityId: projectId,
    },
    {
      skip: !projectId,
    }
  );

  // Auto-open edit dialog from URL param
  useEffect(() => {
    if (editParam === projectId.toString()) {
      setIsEditDialogOpen(true);
    }
  }, [editParam, projectId]);

  // Auto-switch to activity tab if URL param present
  useEffect(() => {
    if (tab === 'activity' || highlightActivityId) {
      setActiveTab('activity');
    }
  }, [tab, highlightActivityId]);

  // Scroll to highlighted activity
  useEffect(() => {
    if (highlightActivityId && activeTab === 'activity') {
      setTimeout(() => {
        const element = document.getElementById(
          `activity-${highlightActivityId}`
        );
        element?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 300);
    }
  }, [highlightActivityId, activeTab]);

  if (isLoading) {
    return (
      <div className='container mx-auto py-6'>
        <div className='animate-pulse space-y-4'>
          <div className='h-8 w-48 bg-muted rounded' />
          <div className='h-96 bg-muted rounded' />
        </div>
      </div>
    );
  }

  if (!project) {
    return (
      <div className='container mx-auto py-6 text-center'>
        <p className='text-muted-foreground'>Project not found</p>
      </div>
    );
  }

  return (
    <div className='container mx-auto py-6 space-y-6'>
      {/* Header */}
      <div className='flex items-center gap-4'>
        <Button
          variant='ghost'
          size='sm'
          onClick={() => router.push('/ptmv2/projects')}
        >
          <ArrowLeft className='h-4 w-4 mr-2' />
          Back to Projects
        </Button>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='details'>Details</TabsTrigger>
          <TabsTrigger value='tasks'>
            Tasks
            {projectTasks.length > 0 && (
              <Badge variant='secondary' className='ml-2'>
                {projectTasks.length}
              </Badge>
            )}
          </TabsTrigger>
          <TabsTrigger value='dependencies' className='flex items-center gap-2'>
            <Network className='h-4 w-4' />
            Dependencies
          </TabsTrigger>
          <TabsTrigger value='timeline' className='flex items-center gap-2'>
            <GanttChart className='h-4 w-4' />
            Timeline
          </TabsTrigger>
          <TabsTrigger value='activity'>
            Activity
            {activities.length > 0 && (
              <Badge variant='secondary' className='ml-2'>
                {activities.length}
              </Badge>
            )}
          </TabsTrigger>
        </TabsList>

        <TabsContent value='details' className='space-y-6'>
          {/* Project Header */}
          <Card>
            <CardHeader>
              <div className='flex items-start justify-between gap-4'>
                <div className='flex-1 space-y-3'>
                  <CardTitle className='text-3xl'>{project?.title}</CardTitle>
                  {project?.description && (
                    <p className='text-muted-foreground text-lg'>
                      {project.description}
                    </p>
                  )}
                </div>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => setIsEditDialogOpen(true)}
                >
                  Edit Project
                </Button>
              </div>
            </CardHeader>
            <CardContent className='space-y-6'>
              {/* Status & Priority */}
              <div className='flex items-center gap-6'>
                <div className='space-y-1'>
                  <p className='text-xs text-muted-foreground font-medium'>
                    Status
                  </p>
                  <Badge variant='outline'>{project?.status ?? 'ACTIVE'}</Badge>
                </div>
                <div className='space-y-1'>
                  <p className='text-xs text-muted-foreground font-medium'>
                    Priority
                  </p>
                  <PriorityBadge priority={project?.priority ?? 'MEDIUM'} />
                </div>
              </div>

              {/* Progress */}
              <div className='space-y-3'>
                <div className='flex items-center justify-between'>
                  <p className='text-sm font-medium'>Progress</p>
                  <span className='text-sm text-muted-foreground'>
                    {project?.progressPercentage ?? 0}%
                  </span>
                </div>
                <Progress
                  value={project?.progressPercentage ?? 0}
                  className='h-2'
                />
                <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                  <CheckCircle2 className='h-4 w-4' />
                  <span>
                    {project?.completedTasks ?? 0} of {project?.totalTasks ?? 0}{' '}
                    tasks completed
                  </span>
                </div>
              </div>

              {/* Dates & Time */}
              <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
                {project?.startDateMs && (
                  <div className='space-y-2'>
                    <p className='text-xs text-muted-foreground font-medium'>
                      Start Date
                    </p>
                    <div className='flex items-center gap-2'>
                      <Calendar className='h-4 w-4 text-muted-foreground' />
                      <p className='text-sm font-semibold'>
                        {new Date(project.startDateMs).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                )}
                {project?.deadlineMs && (
                  <div className='space-y-2'>
                    <p className='text-xs text-muted-foreground font-medium'>
                      Deadline
                    </p>
                    <div className='flex items-center gap-2'>
                      <Calendar className='h-4 w-4 text-muted-foreground' />
                      <p className='text-sm font-semibold'>
                        {new Date(project.deadlineMs).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                )}
                {project?.estimatedHours && project.estimatedHours > 0 && (
                  <div className='space-y-2'>
                    <p className='text-xs text-muted-foreground font-medium'>
                      Estimated Time
                    </p>
                    <div className='flex items-center gap-2'>
                      <Clock className='h-4 w-4 text-muted-foreground' />
                      <p className='text-sm font-semibold'>
                        {project.estimatedHours}h
                      </p>
                    </div>
                  </div>
                )}
              </div>

              {/* Color */}
              {project?.color && (
                <div className='space-y-2'>
                  <p className='text-xs text-muted-foreground font-medium'>
                    Project Color
                  </p>
                  <div className='flex items-center gap-2'>
                    <div
                      className='w-6 h-6 rounded-md border'
                      style={{ backgroundColor: project.color }}
                    />
                    <span className='text-sm font-mono'>{project.color}</span>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='tasks' className='space-y-4'>
          {isLoadingTasks ? (
            <div className='text-center py-12'>
              <p className='text-muted-foreground'>Loading tasks...</p>
            </div>
          ) : projectTasks.length > 0 ? (
            <TaskList projectId={projectId} />
          ) : (
            <Card className='p-12 text-center'>
              <p className='text-muted-foreground'>
                No tasks in this project yet
              </p>
              <Button
                variant='outline'
                size='sm'
                className='mt-4'
                onClick={() => router.push(`/ptmv2/tasks?project=${projectId}`)}
              >
                Create Task
              </Button>
            </Card>
          )}
        </TabsContent>

        {/* Dependencies Tab */}
        <TabsContent value='dependencies'>
          <Card className='p-6'>
            <div className='space-y-4'>
              <div>
                <h3 className='text-lg font-semibold'>
                  Project Dependencies Graph
                </h3>
                <p className='text-sm text-muted-foreground'>
                  Visualize dependencies between tasks in this project
                </p>
              </div>
              {projectTasks.length > 0 ? (
                <div className='h-[600px] border rounded-lg overflow-hidden'>
                  <DependencyGraph projectId={projectId} />
                </div>
              ) : (
                <Card className='p-12 text-center border-dashed'>
                  <Network className='h-12 w-12 mx-auto mb-4 text-muted-foreground' />
                  <p className='text-muted-foreground'>
                    No tasks to visualize dependencies
                  </p>
                </Card>
              )}
            </div>
          </Card>
        </TabsContent>

        {/* Timeline Tab */}
        <TabsContent value='timeline'>
          <Card className='p-6'>
            <div className='space-y-4'>
              <div>
                <h3 className='text-lg font-semibold'>Project Timeline</h3>
                <p className='text-sm text-muted-foreground'>
                  View task schedules and dependencies on a Gantt chart
                </p>
              </div>
              {projectTasks.length > 0 ? (
                <div className='h-[600px] border rounded-lg overflow-hidden'>
                  <GanttView projectId={projectId} />
                </div>
              ) : (
                <Card className='p-12 text-center border-dashed'>
                  <GanttChart className='h-12 w-12 mx-auto mb-4 text-muted-foreground' />
                  <p className='text-muted-foreground'>
                    No tasks to display on timeline
                  </p>
                </Card>
              )}
            </div>
          </Card>
        </TabsContent>

        <TabsContent value='activity'>
          <ProjectActivityTimeline
            activities={activities}
            highlightId={
              highlightActivityId
                ? parseInt(highlightActivityId, 10)
                : undefined
            }
          />
        </TabsContent>
      </Tabs>

      {/* Edit Dialog */}
      {project && (
        <EditProjectDialog
          project={project}
          open={isEditDialogOpen}
          onOpenChange={setIsEditDialogOpen}
        />
      )}
    </div>
  );
}
