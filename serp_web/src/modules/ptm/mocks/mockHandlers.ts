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
  getMockTaskDependencies,
  withTaskDefaults,
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
  TaskDependency,
  CreateDependencyRequest,
  DependencyValidationResult,
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
let dependenciesStore = getMockTaskDependencies();

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
      const newTask = withTaskDefaults({
        id: Date.now(),
        userId: 1,
        tenantId: 1,
        title: data.title || 'New Task',
        priority: data.priority || 'MEDIUM',
        status: 'TODO',
        ...data,
      });

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
        estimatedDurationMin: 60, // 1 hour in minutes
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
        estimatedDurationMin: template.estimatedDurationMin,
        category: template.category,
        tags: template.tags,
        isDeepWork: template.isDeepWork,
      });
    },

    // Phase 1: Subtask handlers
    getSubtasks: async (parentTaskId: number | string) => {
      await delay();
      return tasksStore.filter((t) => t.parentTaskId == parentTaskId);
    },

    getTaskTree: async (rootTaskId: number | string) => {
      await delay();
      const task = tasksStore.find((t) => t.id == rootTaskId);
      if (!task) throw new Error('Task not found');

      // Get direct children
      const children = tasksStore.filter((t) => t.parentTaskId == rootTaskId);

      // Get all descendants recursively
      const descendants: Task[] = [];
      const getDescendants = (parentId: number | string) => {
        const kids = tasksStore.filter((t) => t.parentTaskId == parentId);
        descendants.push(...kids);
        kids.forEach((kid) => getDescendants(kid.id));
      };
      getDescendants(rootTaskId);

      // Get ancestors
      const ancestors: Task[] = [];
      let currentParentId = task.parentTaskId;
      while (currentParentId) {
        const parent = tasksStore.find((t) => t.id === currentParentId);
        if (parent) {
          ancestors.unshift(parent);
          currentParentId = parent.parentTaskId;
        } else {
          break;
        }
      }

      return {
        task,
        children,
        descendants,
        ancestors,
      };
    },

    // Phase 2: Dependency handlers
    getDependencies: async (taskId: number | string) => {
      await delay();
      return dependenciesStore.filter((d) => d.taskId == taskId);
    },

    addDependency: async (request: CreateDependencyRequest) => {
      await delay();
      const {
        taskId,
        dependsOnTaskId,
        dependencyType = 'FINISH_TO_START',
        lagDays,
      } = request;

      // Validate tasks exist
      const task = tasksStore.find((t) => t.id == taskId);
      const dependsOnTask = tasksStore.find((t) => t.id == dependsOnTaskId);
      if (!task || !dependsOnTask) {
        throw new Error('One or both tasks not found');
      }

      // Validate no circular dependency
      const validation =
        await mockApiHandlers.tasks.validateDependency(request);
      if (!validation.isValid) {
        throw new Error(validation.errors[0] || 'Invalid dependency');
      }

      // Create new dependency
      const newDependency: TaskDependency = {
        id: Date.now(),
        taskId,
        dependsOnTaskId,
        dependencyType,
        lagDays,
        createdAt: Date.now(),
      };

      dependenciesStore = [...dependenciesStore, newDependency];

      // Update task's dependentTaskIds array
      const updatedTask = {
        ...task,
        dependentTaskIds: [...task.dependentTaskIds, dependsOnTaskId],
        updatedAt: new Date().toISOString(),
      };
      tasksStore = tasksStore.map((t) => (t.id == taskId ? updatedTask : t));

      return newDependency;
    },

    removeDependency: async (
      taskId: number | string,
      dependencyId: number | string
    ) => {
      await delay();
      const dependency = dependenciesStore.find(
        (d) => d.id == dependencyId && d.taskId == taskId
      );
      if (!dependency) throw new Error('Dependency not found');

      // Remove from dependencies store
      dependenciesStore = dependenciesStore.filter(
        (d) => d.id !== dependencyId
      );

      // Update task's dependentTaskIds array
      const task = tasksStore.find((t) => t.id == taskId);
      if (task) {
        const updatedTask = {
          ...task,
          dependentTaskIds: task.dependentTaskIds.filter(
            (id) => id !== dependency.dependsOnTaskId
          ),
          updatedAt: new Date().toISOString(),
        };
        tasksStore = tasksStore.map((t) => (t.id == taskId ? updatedTask : t));
      }
    },

    validateDependency: async (
      request: CreateDependencyRequest
    ): Promise<DependencyValidationResult> => {
      await delay();
      const { taskId, dependsOnTaskId } = request;
      const errors: string[] = [];
      const warnings: string[] = [];

      // Check if tasks exist
      const task = tasksStore.find((t) => t.id == taskId);
      const dependsOnTask = tasksStore.find((t) => t.id == dependsOnTaskId);
      if (!task) {
        errors.push('Task not found');
        return { isValid: false, errors, warnings, wouldCreateCycle: false };
      }
      if (!dependsOnTask) {
        errors.push('Dependency task not found');
        return { isValid: false, errors, warnings, wouldCreateCycle: false };
      }

      // Check for self-dependency
      if (taskId === dependsOnTaskId) {
        errors.push('Task cannot depend on itself');
        return { isValid: false, errors, warnings, wouldCreateCycle: true };
      }

      // Check for existing dependency
      const existingDep = dependenciesStore.find(
        (d) => d.taskId == taskId && d.dependsOnTaskId == dependsOnTaskId
      );
      if (existingDep) {
        errors.push('Dependency already exists');
        return { isValid: false, errors, warnings, wouldCreateCycle: false };
      }

      // Check for circular dependency using graph traversal
      const wouldCreateCycle = (
        startId: number | string,
        targetId: number | string
      ): boolean => {
        const visited = new Set<number | string>();
        const stack = [startId];

        while (stack.length > 0) {
          const currentId = stack.pop()!;
          if (visited.has(currentId)) continue;
          visited.add(currentId);

          if (currentId == targetId) return true;

          // Get all dependencies of current task
          const currentDeps = dependenciesStore
            .filter((d) => d.dependsOnTaskId == currentId)
            .map((d) => d.taskId);
          stack.push(...currentDeps);
        }

        return false;
      };

      if (wouldCreateCycle(dependsOnTaskId, taskId)) {
        errors.push('This would create a circular dependency');
        return { isValid: false, errors, warnings, wouldCreateCycle: true };
      }

      // Check if dependency task is a subtask of dependent task
      const isDescendant = (
        potentialDescendant: number | string,
        ancestorId: number | string
      ): boolean => {
        const desc = tasksStore.find((t) => t.id == potentialDescendant);
        if (!desc || !desc.parentTaskId) return false;
        if (desc.parentTaskId == ancestorId) return true;
        return isDescendant(desc.parentTaskId, ancestorId);
      };

      if (isDescendant(dependsOnTaskId, taskId)) {
        errors.push('Cannot create dependency with own subtask');
        return { isValid: false, errors, warnings, wouldCreateCycle: false };
      }

      // Check if tasks are in different projects
      if (
        task.projectId &&
        dependsOnTask.projectId &&
        task.projectId !== dependsOnTask.projectId
      ) {
        warnings.push('Tasks are in different projects');
      }

      return {
        isValid: true,
        errors,
        warnings,
        wouldCreateCycle: false,
      };
    },

    getBlockedBy: async (taskId: number | string) => {
      await delay();
      const dependencies = dependenciesStore.filter((d) => d.taskId == taskId);
      const blockedByTasks = dependencies
        .map((d) => tasksStore.find((t) => t.id == d.dependsOnTaskId))
        .filter(Boolean) as Task[];
      return blockedByTasks;
    },

    getBlocking: async (taskId: number | string) => {
      await delay();
      const blockingDependencies = dependenciesStore.filter(
        (d) => d.dependsOnTaskId == taskId
      );
      const blockingTasks = blockingDependencies
        .map((d) => tasksStore.find((t) => t.id == d.taskId))
        .filter(Boolean) as Task[];
      return blockingTasks;
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
        status: 'NEW',
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
