/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useSearchParams } from 'next/navigation';
import { use, useEffect, useState } from 'react';
import { ArrowLeft, Calendar, Clock } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { Button } from '@/shared/components/ui/button';
import { Card } from '@/shared/components/ui/card';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Badge } from '@/shared/components/ui/badge';
import { useGetTaskQuery } from '@/modules/ptm/services/taskApi';
import { useGetEntityActivitiesQuery } from '@/modules/ptm/services/activityApi';
import { TaskDetail } from '@/modules/ptm/components/tasks/TaskDetail';
import { TaskActivityTimeline } from '@/modules/ptm/components/tasks/TaskActivityTimeline';

export default function TaskDetailPage({
  params,
}: {
  params: Promise<{ taskId: string }>;
}) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const unwrappedParams = use(params);
  const taskId = parseInt(unwrappedParams.taskId, 10);

  const [activeTab, setActiveTab] = useState('details');
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const openDetail = searchParams.get('openDetail') === 'true';
  const highlightActivityId = searchParams.get('highlightActivity');
  const editParam = searchParams.get('editTask');

  const { data: task, isLoading } = useGetTaskQuery(taskId);
  const { data: activities = [] } = useGetEntityActivitiesQuery(
    {
      entityType: 'task',
      entityId: taskId,
    },
    {
      skip: !taskId,
    }
  );

  // Auto-open edit dialog from URL param
  useEffect(() => {
    if (editParam === taskId.toString()) {
      setIsEditDialogOpen(true);
    }
  }, [editParam, taskId]);

  // Auto-open activity tab if navigation param present
  useEffect(() => {
    if (highlightActivityId) {
      setActiveTab('activity');
    } else if (openDetail) {
      setActiveTab('details');
    }
  }, [openDetail, highlightActivityId]);

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

  if (!task) {
    return (
      <div className='container mx-auto py-6 text-center'>
        <p className='text-muted-foreground'>Task not found</p>
      </div>
    );
  }

  return (
    <div className='container mx-auto py-6 space-y-6'>
      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='details'>Details</TabsTrigger>
          <TabsTrigger value='activity'>
            Activity History
            {activities.length > 0 && (
              <Badge variant='secondary' className='ml-2'>
                {activities.length}
              </Badge>
            )}
          </TabsTrigger>
        </TabsList>

        <TabsContent value='details' className='mt-6'>
          {task && (
            <div className='space-y-6'>
              {/* Task Header */}
              <div className='space-y-4'>
                <div className='flex flex-col md:flex-row md:items-start md:justify-between gap-4'>
                  <div className='flex-1 space-y-3'>
                    <h1 className='text-3xl font-bold'>{task.title}</h1>
                    {task.description && (
                      <p className='text-muted-foreground text-lg'>
                        {task.description}
                      </p>
                    )}
                  </div>
                  <div className='flex items-center gap-2'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => router.push('/ptm/tasks')}
                    >
                      <ArrowLeft className='h-4 w-4 mr-2' />
                      Back
                    </Button>
                    <Button
                      variant='default'
                      size='sm'
                      onClick={() => setIsEditDialogOpen(true)}
                    >
                      Edit Task
                    </Button>
                  </div>
                </div>
              </div>

              {/* Task Meta */}
              <div className='grid grid-cols-2 md:grid-cols-4 gap-4'>
                <Card className='p-4'>
                  <div className='space-y-2'>
                    <p className='text-xs text-muted-foreground font-medium'>
                      Status
                    </p>
                    <Badge variant='outline' className='text-sm'>
                      {task.status}
                    </Badge>
                  </div>
                </Card>
                <Card className='p-4'>
                  <div className='space-y-2'>
                    <p className='text-xs text-muted-foreground font-medium'>
                      Priority
                    </p>
                    <Badge
                      className='text-sm'
                      variant={
                        task.priority === 'HIGH'
                          ? 'destructive'
                          : task.priority === 'MEDIUM'
                            ? 'default'
                            : 'secondary'
                      }
                    >
                      {task.priority}
                    </Badge>
                  </div>
                </Card>
                {task.estimatedDurationHours && (
                  <Card className='p-4'>
                    <div className='space-y-2'>
                      <p className='text-xs text-muted-foreground font-medium'>
                        Duration
                      </p>
                      <div className='flex items-center gap-2'>
                        <Clock className='h-4 w-4 text-muted-foreground' />
                        <p className='text-sm font-semibold'>
                          {task.estimatedDurationHours}h
                        </p>
                      </div>
                    </div>
                  </Card>
                )}
                {task.deadlineMs && (
                  <Card className='p-4'>
                    <div className='space-y-2'>
                      <p className='text-xs text-muted-foreground font-medium'>
                        Deadline
                      </p>
                      <div className='flex items-center gap-2'>
                        <Calendar className='h-4 w-4 text-muted-foreground' />
                        <p className='text-sm font-semibold'>
                          {new Date(task.deadlineMs).toLocaleDateString()}
                        </p>
                      </div>
                    </div>
                  </Card>
                )}
              </div>

              {/* Additional Info */}
              {task.projectId && (
                <div className='space-y-2'>
                  <p className='text-sm text-muted-foreground'>Project</p>
                  <Button
                    variant='link'
                    className='h-auto p-0'
                    onClick={() =>
                      router.push(`/ptm/projects/${task.projectId}`)
                    }
                  >
                    View Project
                  </Button>
                </div>
              )}
            </div>
          )}
        </TabsContent>

        <TabsContent value='activity'>
          <TaskActivityTimeline
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
      <TaskDetail
        taskId={taskId}
        open={isEditDialogOpen}
        onOpenChange={setIsEditDialogOpen}
      />
    </div>
  );
}
