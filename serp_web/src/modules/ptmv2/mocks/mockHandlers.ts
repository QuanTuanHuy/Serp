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
  mockNotes,
  getMockActivityEvents,
  delay,
} from './mockData';
import type {
  Task,
  Project,
  ScheduleEvent,
  Note,
  ActivityEvent,
  ActivityFeedResponse,
  ActivityFeedFilters,
} from '../types';

// Enable/disable mock mode (set to false when API is ready)
export const USE_MOCK_DATA = true;

// Helper to create mutable copies
const deepClone = <T>(arr: T[]): T[] => JSON.parse(JSON.stringify(arr));

let tasksStore = deepClone(mockTasks);
let projectsStore = deepClone(mockProjects);
let eventsStore = deepClone(mockScheduleEvents);
let notesStore = deepClone(mockNotes);
let activitiesStore = getMockActivityEvents();

// Helper to extract plain text from Tiptap JSON
const extractPlainText = (content: string): string => {
  try {
    const json = JSON.parse(content);
    const getText = (node: any): string => {
      if (node.type === 'text') {
        return node.text || '';
      }
      if (node.content && Array.isArray(node.content)) {
        return node.content.map(getText).join(' ');
      }
      return '';
    };
    const plainText = getText(json).trim();
    // Return first 200 characters for preview
    return plainText.length > 200
      ? plainText.substring(0, 200) + '...'
      : plainText;
  } catch {
    // If not JSON, return as-is (markdown or plain text)
    const cleaned = content.replace(/[#*`_\[\]()]/g, '');
    return cleaned.length > 200 ? cleaned.substring(0, 200) + '...' : cleaned;
  }
};

export const mockApiHandlers = {
  // Task handlers
  tasks: {
    getAll: async (params?: {
      status?: string;
      projectId?: number | string;
    }) => {
      await delay();
      let filtered = tasksStore;

      if (params?.status) {
        filtered = filtered.filter((t) => t.status === params.status);
      }
      if (params?.projectId) {
        filtered = filtered.filter((t) => t.projectId == params.projectId);
      }

      return filtered;
    },

    getById: async (id: number | string) => {
      await delay();
      const task = tasksStore.find((t) => t.id == id);
      if (!task) throw new Error('Task not found');
      return task;
    },

    create: async (data: Partial<Task>) => {
      await delay();
      const newTask: Task = {
        id: Date.now(),
        userId: 1,
        tenantId: 1,
        title: data.title || 'New Task',
        description: data.description,
        priority: data.priority || 'MEDIUM',
        status: 'TODO',
        estimatedDurationHours: data.estimatedDurationHours || 1,
        isDeepWork: data.isDeepWork || false,
        category: data.category,
        tags: data.tags || [],
        dependentTaskIds: [],
        repeatConfig: data.repeatConfig,
        source: 'manual',
        progressPercentage: 0,
        activeStatus: 'ACTIVE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        projectId: data.projectId,
        deadlineMs: data.deadlineMs,
        parentTaskId: data.parentTaskId,
      };

      tasksStore = [...tasksStore, newTask];
      return newTask;
    },

    update: async (id: number | string, data: Partial<Task>) => {
      await delay();
      const index = tasksStore.findIndex((t) => t.id == id);
      if (index === -1) throw new Error('Task not found');

      const updatedTask = {
        ...tasksStore[index],
        ...data,
        updatedAt: new Date().toISOString(),
      };

      tasksStore = tasksStore.map((t, i) => (i === index ? updatedTask : t));

      return updatedTask;
    },

    delete: async (id: number | string) => {
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
      templateId: number | string,
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

    // Subtask and dependency handlers
    getSubtasks: async (parentTaskId: number | string) => {
      await delay();
      return tasksStore.filter((t) => t.parentTaskId == parentTaskId);
    },

    getTaskTree: async (rootTaskId: number | string) => {
      await delay();
      const buildTree = (taskId: number | string): any => {
        const task = tasksStore.find((t) => t.id == taskId);
        if (!task) return null;

        const subtasks = tasksStore
          .filter((t) => t.parentTaskId == taskId)
          .map((subtask) => buildTree(subtask.id));

        return {
          ...task,
          subtasks,
          depth: task.parentTaskId ? 1 : 0, // Simple depth calculation
          hasSubtasks: subtasks.length > 0,
          completedSubtasksCount: subtasks.filter(
            (st: any) => st.status === 'COMPLETED'
          ).length,
          totalSubtasksCount: subtasks.length,
        };
      };

      return buildTree(rootTaskId);
    },

    getDependencies: async (taskId: number | string) => {
      await delay();
      const task = tasksStore.find((t) => t.id == taskId);
      if (!task) throw new Error('Task not found');

      const dependencies = task.dependentTaskIds
        .map((depId) => {
          const depTask = tasksStore.find((t) => t.id == depId);
          return depTask
            ? {
                id: `${taskId}-${depId}`,
                dependentTaskId: taskId,
                dependencyTaskId: depId,
                dependencyType: 'FINISH_TO_START' as const,
                createdAt: new Date().toISOString(),
                dependencyTask: depTask,
              }
            : null;
        })
        .filter(Boolean);

      return dependencies;
    },

    addDependency: async (data: {
      dependentTaskId: number;
      dependencyTaskId: number;
      dependencyType?: string;
    }) => {
      await delay();
      const {
        dependentTaskId,
        dependencyTaskId,
        dependencyType = 'FINISH_TO_START',
      } = data;

      // Validate tasks exist
      const dependentTask = tasksStore.find((t) => t.id == dependentTaskId);
      const dependencyTask = tasksStore.find((t) => t.id == dependencyTaskId);
      if (!dependentTask || !dependencyTask) {
        throw new Error('One or both tasks not found');
      }

      // Check for circular dependency (simple check)
      if (dependentTask.dependentTaskIds.includes(dependencyTaskId)) {
        throw new Error('Circular dependency detected');
      }

      // Update dependent task
      const updatedTask = {
        ...dependentTask,
        dependentTaskIds: [...dependentTask.dependentTaskIds, dependencyTaskId],
        updatedAt: new Date().toISOString(),
      };

      tasksStore = tasksStore.map((t) =>
        t.id == dependentTaskId ? updatedTask : t
      );

      return {
        id: `${dependentTaskId}-${dependencyTaskId}`,
        dependentTaskId,
        dependencyTaskId,
        dependencyType,
        createdAt: new Date().toISOString(),
        dependencyTask,
      };
    },

    removeDependency: async (
      dependentTaskId: number,
      dependencyTaskId: number
    ) => {
      await delay();
      const task = tasksStore.find((t) => t.id == dependentTaskId);
      if (!task) throw new Error('Task not found');

      const updatedTask = {
        ...task,
        dependentTaskIds: task.dependentTaskIds.filter(
          (id) => id !== dependencyTaskId
        ),
        updatedAt: new Date().toISOString(),
      };

      tasksStore = tasksStore.map((t) =>
        t.id == dependentTaskId ? updatedTask : t
      );
    },

    validateDependency: async (
      dependentTaskId: number,
      dependencyTaskId: number
    ) => {
      await delay();

      // Check if tasks exist
      const dependentTask = tasksStore.find((t) => t.id == dependentTaskId);
      const dependencyTask = tasksStore.find((t) => t.id == dependencyTaskId);
      if (!dependentTask || !dependencyTask) {
        return {
          isValid: false,
          reason: 'One or both tasks not found',
        };
      }

      // Check for self-dependency
      if (dependentTaskId === dependencyTaskId) {
        return {
          isValid: false,
          reason: 'Task cannot depend on itself',
        };
      }

      // Check for circular dependency (simple check - would need graph traversal for full validation)
      if (dependentTask.dependentTaskIds.includes(dependencyTaskId)) {
        return {
          isValid: false,
          reason: 'Circular dependency detected',
        };
      }

      // Check if dependency task is a subtask of dependent task (would create hierarchy issues)
      const isDependencySubtask = (
        taskId: number,
        potentialParentId: number
      ): boolean => {
        const task = tasksStore.find((t) => t.id == taskId);
        if (!task || !task.parentTaskId) return false;
        if (task.parentTaskId == potentialParentId) return true;
        return isDependencySubtask(task.parentTaskId, potentialParentId);
      };

      if (isDependencySubtask(dependencyTaskId, dependentTaskId)) {
        return {
          isValid: false,
          reason: 'Cannot create dependency with subtask',
        };
      }

      return {
        isValid: true,
        reason: 'Valid dependency',
      };
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

    getById: async (id: number | string) => {
      await delay();
      const project = projectsStore.find((p) => p.id == id);
      if (!project) throw new Error('Project not found');
      return project;
    },

    create: async (data: Partial<Project>) => {
      await delay();
      const newProject: Project = {
        id: Date.now(),
        userId: 1,
        tenantId: 1,
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

      projectsStore = [...projectsStore, newProject];
      return newProject;
    },

    update: async (id: number | string, data: Partial<Project>) => {
      await delay();
      const index = projectsStore.findIndex((p) => p.id == id);
      if (index === -1) throw new Error('Project not found');

      const updatedProject = {
        ...projectsStore[index],
        ...data,
        updatedAt: new Date().toISOString(),
      };

      projectsStore = projectsStore.map((p, i) =>
        i === index ? updatedProject : p
      );

      return updatedProject;
    },

    delete: async (id: number | string) => {
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

    createEvent: async (data: Partial<ScheduleEvent>) => {
      await delay();
      const startMin = data.startMin || 480;
      const endMin = data.endMin || 540;
      const durationMin = endMin - startMin;

      const newEvent: ScheduleEvent = {
        id: Date.now(),
        schedulePlanId: mockSchedulePlan.id,
        scheduleTaskId: data.scheduleTaskId || Date.now(),
        dateMs: data.dateMs || Date.now(),
        startMin,
        endMin,
        durationMin,
        status: 'scheduled',
        taskPart: 1,
        totalParts: 1,
        utility: 75,
        utilityBreakdown: {
          priorityScore: 20,
          deadlineScore: 25,
          contextSwitchPenalty: -5,
          focusTimeBonus: data.isDeepWork ? 10 : 0,
          totalUtility: 75,
          reason: 'Manually scheduled by user',
        },
        isManualOverride: true,
        title: data.title || 'Untitled Event',
        priority: data.priority,
        isDeepWork: data.isDeepWork,
        projectColor: data.projectColor,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      eventsStore = [...eventsStore, newEvent];
      return newEvent;
    },

    updateEvent: async (id: number, data: Partial<ScheduleEvent>) => {
      await delay();
      const index = eventsStore.findIndex((e) => e.id == id);
      if (index === -1) throw new Error('Event not found');

      const updatedEvent = {
        ...eventsStore[index],
        ...data,
        updatedAt: new Date().toISOString(),
      };

      eventsStore = eventsStore.map((e, i) => (i === index ? updatedEvent : e));

      return updatedEvent;
    },

    deleteEvent: async (id: number) => {
      await delay();
      const index = eventsStore.findIndex((e) => e.id == id);
      if (index === -1) throw new Error('Event not found');
      eventsStore.splice(index, 1);
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
        userId: 1,
        tenantId: 1,
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

  // Note handlers
  notes: {
    getByTask: async (taskId: number) => {
      await delay();
      return notesStore.filter((n) => n.taskId === taskId);
    },

    getByProject: async (projectId: number) => {
      await delay();
      return notesStore.filter((n) => n.projectId === projectId);
    },

    getById: async (id: number | string) => {
      await delay();
      const note = notesStore.find((n) => n.id == id);
      if (!note) throw new Error('Note not found');
      return note;
    },

    create: async (data: Partial<Note>) => {
      await delay();
      const newNote: Note = {
        id: Date.now(),
        userId: 1,
        tenantId: 1,
        content: data.content || '',
        contentPlain: extractPlainText(data.content || ''),
        attachments: data.attachments || [],
        isPinned: data.isPinned || false,
        activeStatus: 'ACTIVE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        taskId: data.taskId,
        projectId: data.projectId,
      };

      notesStore = [...notesStore, newNote];
      return newNote;
    },

    update: async (id: number | string, data: Partial<Note>) => {
      await delay();
      const index = notesStore.findIndex((n) => n.id == id);
      if (index === -1) throw new Error('Note not found');

      const updatedNote = {
        ...notesStore[index],
        ...data,
        contentPlain: data.content
          ? extractPlainText(data.content)
          : notesStore[index].contentPlain,
        updatedAt: new Date().toISOString(),
      };

      notesStore = notesStore.map((n, i) => (i === index ? updatedNote : n));

      return updatedNote;
    },

    delete: async (id: number | string) => {
      await delay();
      notesStore = notesStore.filter((n) => n.id !== id);
    },
  },

  // Activity handlers
  activities: {
    getFeed: async (filters: ActivityFeedFilters) => {
      await delay();
      let filtered = activitiesStore;

      // Filter by types
      if (filters.types && filters.types.length > 0) {
        filtered = filtered.filter((a) => filters.types!.includes(a.eventType));
      }

      // Filter by entity type
      if (filters.entity) {
        filtered = filtered.filter((a) => a.entityType === filters.entity);
      }

      // Filter by date range
      if (filters.fromDateMs) {
        filtered = filtered.filter((a) => a.createdAt >= filters.fromDateMs!);
      }
      if (filters.toDateMs) {
        filtered = filtered.filter((a) => a.createdAt <= filters.toDateMs!);
      }

      // Sort by newest first
      filtered.sort((a, b) => b.createdAt - a.createdAt);

      // Pagination
      const page = filters.page ?? 0;
      const size = filters.size ?? 20;
      const start = page * size;
      const end = start + size;
      const paginatedActivities = filtered.slice(start, end);

      const response: ActivityFeedResponse = {
        activities: paginatedActivities,
        totalCount: filtered.length,
        currentPage: page,
        pageSize: size,
        hasMore: end < filtered.length,
      };

      return response;
    },

    getByEntity: async (entityType: string, entityId: number) => {
      await delay();
      return activitiesStore.filter(
        (a) => a.entityType === entityType && a.entityId === entityId
      );
    },

    getStats: async () => {
      await delay();
      const now = Date.now();
      const oneDayAgo = now - 24 * 60 * 60 * 1000;
      const oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000;

      const todayCount = activitiesStore.filter(
        (a) => a.createdAt >= oneDayAgo
      ).length;
      const weekCount = activitiesStore.filter(
        (a) => a.createdAt >= oneWeekAgo
      ).length;

      return {
        todayCount,
        weekCount,
        averagePerDay: Math.round(weekCount / 7),
        mostActiveHour: '14:00',
      };
    },
  },
};

// Reset mock data (useful for testing)
export const resetMockData = () => {
  tasksStore = deepClone(mockTasks);
  projectsStore = deepClone(mockProjects);
  eventsStore = deepClone(mockScheduleEvents);
  notesStore = deepClone(mockNotes);
  activitiesStore = getMockActivityEvents();
};
