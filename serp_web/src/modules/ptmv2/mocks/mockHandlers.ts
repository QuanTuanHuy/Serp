/**
 * PTM v2 - Mock API Handlers
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Mock API responses for development
 */

import {
  mockTasks,
  mockProjects,
  mockScheduleEvents,
  mockSchedulePlan,
  mockFocusTimeBlocks,
  mockTaskTemplates,
  delay,
} from './mockData';
import type { Task, Project, ScheduleEvent } from '../types';

// Enable/disable mock mode (set to false when API is ready)
export const USE_MOCK_DATA = true;

let tasksStore = [...mockTasks];
let projectsStore = [...mockProjects];
let eventsStore = [...mockScheduleEvents];

export const mockApiHandlers = {
  // Task handlers
  tasks: {
    getAll: async (params?: { status?: string; projectId?: string }) => {
      await delay();
      let filtered = tasksStore;

      if (params?.status) {
        filtered = filtered.filter((t) => t.status === params.status);
      }
      if (params?.projectId) {
        filtered = filtered.filter((t) => t.projectId === params.projectId);
      }

      return filtered;
    },

    getById: async (id: string) => {
      await delay();
      const task = tasksStore.find((t) => t.id === id);
      if (!task) throw new Error('Task not found');
      return task;
    },

    create: async (data: Partial<Task>) => {
      await delay();
      const newTask: Task = {
        id: `task-${Date.now()}`,
        userId: 'user-1',
        tenantId: 'tenant-1',
        title: data.title || 'New Task',
        description: data.description,
        priority: data.priority || 'MEDIUM',
        status: 'TODO',
        estimatedDurationHours: data.estimatedDurationHours || 1,
        isDeepWork: data.isDeepWork || false,
        category: data.category,
        tags: data.tags || [],
        dependentTaskIds: [],
        source: 'manual',
        progressPercentage: 0,
        activeStatus: 'ACTIVE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        projectId: data.projectId,
        deadlineMs: data.deadlineMs,
      };

      tasksStore.push(newTask);
      return newTask;
    },

    update: async (id: string, data: Partial<Task>) => {
      await delay();
      const index = tasksStore.findIndex((t) => t.id === id);
      if (index === -1) throw new Error('Task not found');

      tasksStore[index] = {
        ...tasksStore[index],
        ...data,
        updatedAt: new Date().toISOString(),
      };

      return tasksStore[index];
    },

    delete: async (id: string) => {
      await delay();
      tasksStore = tasksStore.filter((t) => t.id !== id);
    },

    quickAdd: async (data: { title: string }) => {
      await delay();
      return mockApiHandlers.tasks.create({
        title: data.title,
        priority: 'MEDIUM',
        estimatedDurationHours: 1,
      });
    },

    getTemplates: async () => {
      await delay();
      return mockTaskTemplates;
    },

    createFromTemplate: async (
      templateId: string,
      variables?: Record<string, string>
    ) => {
      await delay();
      const template = mockTaskTemplates.find((t) => t.id === templateId);
      if (!template) throw new Error('Template not found');

      let title = template.titleTemplate;
      let description = template.descriptionTemplate;

      if (variables) {
        Object.entries(variables).forEach(([key, value]) => {
          title = title.replace(`{{${key}}}`, value);
          if (description) {
            description = description.replace(`{{${key}}}`, value);
          }
        });
      }

      return mockApiHandlers.tasks.create({
        title,
        description,
        priority: template.priority,
        estimatedDurationHours: template.estimatedDurationHours,
        category: template.category,
        tags: template.tags,
        isDeepWork: template.isDeepWork,
      });
    },
  },

  // Project handlers
  projects: {
    getAll: async (params?: { status?: string }) => {
      await delay();
      let filtered = projectsStore;

      if (params?.status) {
        filtered = filtered.filter((p) => p.status === params.status);
      }

      return filtered;
    },

    getById: async (id: string) => {
      await delay();
      const project = projectsStore.find((p) => p.id === id);
      if (!project) throw new Error('Project not found');
      return project;
    },

    create: async (data: Partial<Project>) => {
      await delay();
      const newProject: Project = {
        id: `proj-${Date.now()}`,
        userId: 'user-1',
        tenantId: 'tenant-1',
        title: data.title || 'New Project',
        description: data.description,
        status: 'ACTIVE',
        priority: data.priority || 'MEDIUM',
        progressPercentage: 0,
        color: data.color || '#3B82F6',
        isFavorite: false,
        totalTasks: 0,
        completedTasks: 0,
        estimatedHours: 0,
        actualHours: 0,
        activeStatus: 'ACTIVE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        startDateMs: data.startDateMs,
        deadlineMs: data.deadlineMs,
        icon: data.icon,
      };

      projectsStore.push(newProject);
      return newProject;
    },

    update: async (id: string, data: Partial<Project>) => {
      await delay();
      const index = projectsStore.findIndex((p) => p.id === id);
      if (index === -1) throw new Error('Project not found');

      projectsStore[index] = {
        ...projectsStore[index],
        ...data,
        updatedAt: new Date().toISOString(),
      };

      return projectsStore[index];
    },

    delete: async (id: string) => {
      await delay();
      projectsStore = projectsStore.filter((p) => p.id !== id);
    },
  },

  // Schedule handlers
  schedule: {
    getPlans: async () => {
      await delay();
      return [mockSchedulePlan];
    },

    getActivePlan: async () => {
      await delay();
      return mockSchedulePlan;
    },

    createPlan: async (data: any) => {
      await delay();
      return {
        ...mockSchedulePlan,
        id: `plan-${Date.now()}`,
        ...data,
      };
    },

    getEvents: async (params: { startDateMs: number; endDateMs: number }) => {
      await delay();
      return eventsStore.filter(
        (e) => e.dateMs >= params.startDateMs && e.dateMs < params.endDateMs
      );
    },

    updateEvent: async (id: string, data: Partial<ScheduleEvent>) => {
      await delay();
      const index = eventsStore.findIndex((e) => e.id === id);
      if (index === -1) throw new Error('Event not found');

      eventsStore[index] = {
        ...eventsStore[index],
        ...data,
        updatedAt: new Date().toISOString(),
      };

      return eventsStore[index];
    },

    triggerOptimization: async (data: any) => {
      await delay(1000); // Simulate longer processing
      return { jobId: `job-${Date.now()}` };
    },

    getFocusBlocks: async () => {
      await delay();
      return mockFocusTimeBlocks;
    },

    createFocusBlock: async (data: any) => {
      await delay();
      return {
        id: `focus-${Date.now()}`,
        userId: 'user-1',
        tenantId: 'tenant-1',
        ...data,
        isEnabled: true,
        activeStatus: 'ACTIVE' as const,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
    },

    updateFocusBlock: async (id: string, data: any) => {
      await delay();
      return {
        id,
        ...data,
        updatedAt: new Date().toISOString(),
      };
    },

    deleteFocusBlock: async (id: string) => {
      await delay();
    },
  },
};

// Reset mock data (useful for testing)
export const resetMockData = () => {
  tasksStore = [...mockTasks];
  projectsStore = [...mockProjects];
  eventsStore = [...mockScheduleEvents];
};
