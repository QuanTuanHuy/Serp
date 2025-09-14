/**
 * PTM Projects Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Project Management Dashboard
 */

'use client';

import React from 'react';
import { Button, Card } from '@/shared/components';
import {
  FolderKanban,
  Plus,
  Users,
  Calendar,
  MoreHorizontal,
} from 'lucide-react';

const Projects: React.FC = () => {
  const projects = [
    {
      id: 1,
      name: 'SERP Development',
      description: 'Smart ERP system development project',
      progress: 75,
      members: 5,
      dueDate: '2024-12-31',
      status: 'In Progress',
    },
    {
      id: 2,
      name: 'Marketing Campaign',
      description: 'Q4 marketing campaign planning and execution',
      progress: 45,
      members: 3,
      dueDate: '2024-11-15',
      status: 'In Progress',
    },
    {
      id: 3,
      name: 'Mobile App',
      description: 'Cross-platform mobile application development',
      progress: 90,
      members: 4,
      dueDate: '2024-10-30',
      status: 'Nearly Complete',
    },
  ];

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <FolderKanban className='h-6 w-6' />
          <h1 className='text-2xl font-bold'>Project Management</h1>
        </div>
        <Button>
          <Plus className='mr-2 h-4 w-4' />
          New Project
        </Button>
      </div>

      {/* Projects Grid */}
      <div className='grid grid-cols-1 gap-4 lg:grid-cols-2 xl:grid-cols-3'>
        {projects.map((project) => (
          <Card key={project.id} className='p-6'>
            <div className='space-y-4'>
              {/* Project Header */}
              <div className='flex items-start justify-between'>
                <div>
                  <h3 className='font-semibold'>{project.name}</h3>
                  <p className='text-sm text-muted-foreground'>
                    {project.description}
                  </p>
                </div>
                <Button variant='ghost' size='sm'>
                  <MoreHorizontal className='h-4 w-4' />
                </Button>
              </div>

              {/* Progress Bar */}
              <div className='space-y-2'>
                <div className='flex justify-between text-sm'>
                  <span>Progress</span>
                  <span>{project.progress}%</span>
                </div>
                <div className='h-2 rounded-full bg-muted'>
                  <div
                    className='h-2 rounded-full bg-primary'
                    style={{ width: `${project.progress}%` }}
                  />
                </div>
              </div>

              {/* Project Info */}
              <div className='flex items-center justify-between text-sm text-muted-foreground'>
                <div className='flex items-center gap-1'>
                  <Users className='h-4 w-4' />
                  <span>{project.members} members</span>
                </div>
                <div className='flex items-center gap-1'>
                  <Calendar className='h-4 w-4' />
                  <span>{project.dueDate}</span>
                </div>
              </div>

              {/* Status Badge */}
              <div className='flex justify-end'>
                <span
                  className={`rounded-full px-2 py-1 text-xs font-medium ${
                    project.status === 'In Progress'
                      ? 'bg-blue-100 text-blue-700'
                      : 'bg-green-100 text-green-700'
                  }`}
                >
                  {project.status}
                </span>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card className='p-6'>
        <h3 className='mb-4 text-lg font-semibold'>Quick Actions</h3>
        <div className='grid grid-cols-1 gap-4 md:grid-cols-3'>
          <Button variant='outline' className='justify-start'>
            <Plus className='mr-2 h-4 w-4' />
            Create New Project
          </Button>
          <Button variant='outline' className='justify-start'>
            <Users className='mr-2 h-4 w-4' />
            Manage Team
          </Button>
          <Button variant='outline' className='justify-start'>
            <Calendar className='mr-2 h-4 w-4' />
            Project Timeline
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default Projects;
