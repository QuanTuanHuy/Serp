/**
 * TaskDetailTabs Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Tabs for task details and activity history
 */

'use client';

import { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Badge } from '@/shared/components/ui/badge';
import { TaskDetailContent } from './TaskDetailContent';
import { SubtasksTab } from './SubtasksTab';
import { TaskActivityTimeline } from '../TaskActivityTimeline';
import { useGetEntityActivitiesQuery } from '../../../api';
import type { Task } from '../../../types';

interface TaskDetailTabsProps {
  taskId: number;
  task: Task;
}

export function TaskDetailTabs({ taskId, task }: TaskDetailTabsProps) {
  const searchParams = useSearchParams();
  const [activeTab, setActiveTab] = useState('details');

  // URL params for navigation
  const openDetail = searchParams.get('openDetail') === 'true';
  const highlightActivityId = searchParams.get('highlightActivity');

  const { data: activities = [] } = useGetEntityActivitiesQuery(
    {
      entityType: 'task',
      entityId: taskId,
    },
    {
      skip: !taskId,
    }
  );

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

  return (
    <Tabs value={activeTab} onValueChange={setActiveTab}>
      <TabsList>
        <TabsTrigger value='details'>Details</TabsTrigger>
        <TabsTrigger value='subtasks'>
          Subtasks
          {task.subTasks && task.subTasks.length > 0 && (
            <Badge variant='secondary' className='ml-2'>
              {task.subTasks.length}
            </Badge>
          )}
        </TabsTrigger>
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
        <TaskDetailContent task={task} />
      </TabsContent>

      <TabsContent value='subtasks' className='mt-6'>
        <SubtasksTab task={task} />
      </TabsContent>

      <TabsContent value='activity'>
        <TaskActivityTimeline
          activities={activities}
          highlightId={
            highlightActivityId ? parseInt(highlightActivityId, 10) : undefined
          }
        />
      </TabsContent>
    </Tabs>
  );
}
