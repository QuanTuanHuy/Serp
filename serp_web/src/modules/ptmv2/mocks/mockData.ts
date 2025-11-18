/**
 * PTM v2 - Mock Data
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Mock data for development/testing
 */

import type {
  Task,
  Project,
  ScheduleEvent,
  SchedulePlan,
  FocusTimeBlock,
  TaskTemplate,
} from '../types';

// Mock Tasks - Using function to ensure mutable arrays
const _mockTasksData: Task[] = [
  {
    id: '1',
    userId: 'user-1',
    tenantId: 'tenant-1',
    projectId: 'proj-1',
    title: 'Review Pull Requests',
    description: 'Review and merge pending PRs for the authentication module',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    estimatedDurationHours: 2,
    actualDurationHours: 1.5,
    deadlineMs: Date.now() + 3 * 60 * 60 * 1000, // 3 hours from now
    isDeepWork: true,
    category: 'Development',
    tags: ['code-review', 'backend'],
    dependentTaskIds: [],
    source: 'manual',
    progressPercentage: 60,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '2',
    userId: 'user-1',
    tenantId: 'tenant-1',
    projectId: 'proj-1',
    title: 'Update Documentation',
    description: 'Update API documentation with new endpoints',
    priority: 'MEDIUM',
    status: 'TODO',
    estimatedDurationHours: 1.5,
    deadlineMs: Date.now() + 24 * 60 * 60 * 1000, // 1 day from now
    isDeepWork: false,
    category: 'Documentation',
    tags: ['docs', 'api'],
    dependentTaskIds: ['1'],
    source: 'manual',
    progressPercentage: 0,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '3',
    userId: 'user-1',
    tenantId: 'tenant-1',
    projectId: 'proj-2',
    title: 'Design System Components',
    description: 'Create reusable UI components for the design system',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    estimatedDurationHours: 4,
    actualDurationHours: 2,
    deadlineMs: Date.now() + 2 * 24 * 60 * 60 * 1000, // 2 days from now
    isDeepWork: true,
    category: 'Design',
    tags: ['ui', 'components', 'figma'],
    dependentTaskIds: [],
    source: 'manual',
    progressPercentage: 50,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '4',
    userId: 'user-1',
    tenantId: 'tenant-1',
    title: 'Team Standup Meeting',
    description: 'Daily standup with the development team',
    priority: 'LOW',
    status: 'DONE',
    estimatedDurationHours: 0.25,
    actualDurationHours: 0.25,
    completedAt: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
    isDeepWork: false,
    category: 'Meeting',
    tags: ['meeting', 'team'],
    dependentTaskIds: [],
    source: 'manual',
    progressPercentage: 100,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '5',
    userId: 'user-1',
    tenantId: 'tenant-1',
    projectId: 'proj-1',
    title: 'Fix Critical Bug in Payment Flow',
    description: 'Resolve the issue causing payment failures in production',
    priority: 'HIGH',
    status: 'TODO',
    estimatedDurationHours: 3,
    deadlineMs: Date.now() - 2 * 60 * 60 * 1000, // 2 hours ago (OVERDUE)
    isDeepWork: true,
    category: 'Bug Fix',
    tags: ['critical', 'payment', 'production'],
    dependentTaskIds: [],
    source: 'manual',
    progressPercentage: 0,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '6',
    userId: 'user-1',
    tenantId: 'tenant-1',
    projectId: 'proj-2',
    title: 'Client Presentation Prep',
    description: 'Prepare slides and demo for client meeting',
    priority: 'MEDIUM',
    status: 'IN_PROGRESS',
    estimatedDurationHours: 2,
    actualDurationHours: 1,
    deadlineMs: Date.now() + 4 * 60 * 60 * 1000, // 4 hours from now
    isDeepWork: false,
    category: 'Presentation',
    tags: ['client', 'presentation'],
    dependentTaskIds: [],
    source: 'manual',
    progressPercentage: 40,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
  },
];

export const mockTasks: Task[] = JSON.parse(JSON.stringify(_mockTasksData));

// Mock Projects
const _mockProjectsData: Project[] = [
  {
    id: 'proj-1',
    userId: 'user-1',
    tenantId: 'tenant-1',
    title: 'Backend Refactoring',
    description: 'Refactor backend services for better performance',
    status: 'ACTIVE',
    priority: 'HIGH',
    startDateMs: Date.now() - 14 * 24 * 60 * 60 * 1000,
    deadlineMs: Date.now() + 14 * 24 * 60 * 60 * 1000,
    progressPercentage: 65,
    color: '#3B82F6',
    icon: '🚀',
    isFavorite: true,
    totalTasks: 12,
    completedTasks: 8,
    estimatedHours: 48,
    actualHours: 32,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 14 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'proj-2',
    userId: 'user-1',
    tenantId: 'tenant-1',
    title: 'Design System v2',
    description: 'Build comprehensive design system for all products',
    status: 'ACTIVE',
    priority: 'MEDIUM',
    startDateMs: Date.now() - 7 * 24 * 60 * 60 * 1000,
    deadlineMs: Date.now() + 21 * 24 * 60 * 60 * 1000,
    progressPercentage: 30,
    color: '#8B5CF6',
    icon: '🎨',
    isFavorite: false,
    totalTasks: 20,
    completedTasks: 6,
    estimatedHours: 80,
    actualHours: 24,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  },
];

export const mockProjects: Project[] = JSON.parse(
  JSON.stringify(_mockProjectsData)
);

// Mock Schedule Events for Today
const today = new Date();
today.setHours(0, 0, 0, 0);

export const mockScheduleEvents: ScheduleEvent[] = [
  {
    id: 'evt-1',
    schedulePlanId: 'plan-1',
    scheduleTaskId: '1',
    dateMs: today.getTime(),
    startMin: 540, // 9:00 AM
    endMin: 600, // 10:00 AM
    durationMin: 60,
    status: 'completed',
    taskPart: 1,
    totalParts: 2,
    utility: 85.5,
    utilityBreakdown: {
      priorityScore: 30,
      deadlineScore: 25,
      contextSwitchPenalty: -5,
      focusTimeBonus: 35.5,
      totalUtility: 85.5,
      reason: 'High-priority deep work during peak focus time',
    },
    isManualOverride: false,
    title: 'Review Pull Requests',
    priority: 'HIGH',
    isDeepWork: true,
    projectColor: '#3B82F6',
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'evt-2',
    schedulePlanId: 'plan-1',
    scheduleTaskId: '3',
    dateMs: today.getTime(),
    startMin: 660, // 11:00 AM
    endMin: 780, // 1:00 PM
    durationMin: 120,
    status: 'scheduled',
    taskPart: 1,
    totalParts: 2,
    utility: 78.2,
    utilityBreakdown: {
      priorityScore: 30,
      deadlineScore: 20,
      contextSwitchPenalty: -8,
      focusTimeBonus: 36.2,
      totalUtility: 78.2,
      reason: 'Deep work session with minimal context switching',
    },
    isManualOverride: false,
    title: 'Design System Components',
    priority: 'HIGH',
    isDeepWork: true,
    projectColor: '#8B5CF6',
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'evt-3',
    schedulePlanId: 'plan-1',
    scheduleTaskId: '6',
    dateMs: today.getTime(),
    startMin: 840, // 2:00 PM
    endMin: 900, // 3:00 PM
    durationMin: 60,
    status: 'scheduled',
    taskPart: 1,
    totalParts: 2,
    utility: 65.0,
    utilityBreakdown: {
      priorityScore: 20,
      deadlineScore: 25,
      contextSwitchPenalty: -5,
      focusTimeBonus: 25,
      totalUtility: 65.0,
      reason: 'Medium priority task in afternoon slot',
    },
    isManualOverride: false,
    title: 'Client Presentation Prep',
    priority: 'MEDIUM',
    isDeepWork: false,
    projectColor: '#8B5CF6',
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'evt-4',
    schedulePlanId: 'plan-1',
    scheduleTaskId: '2',
    dateMs: today.getTime(),
    startMin: 960, // 4:00 PM
    endMin: 1050, // 5:30 PM
    durationMin: 90,
    status: 'scheduled',
    taskPart: 1,
    totalParts: 1,
    utility: 55.0,
    utilityBreakdown: {
      priorityScore: 15,
      deadlineScore: 20,
      contextSwitchPenalty: -3,
      focusTimeBonus: 23,
      totalUtility: 55.0,
      reason: 'Documentation task in late afternoon',
    },
    isManualOverride: false,
    title: 'Update Documentation',
    priority: 'MEDIUM',
    isDeepWork: false,
    projectColor: '#3B82F6',
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
  },
];

// Mock Schedule Plan
export const mockSchedulePlan: SchedulePlan = {
  id: 'plan-1',
  userId: 'user-1',
  tenantId: 'tenant-1',
  startDateMs: today.getTime(),
  endDateMs: today.getTime() + 7 * 24 * 60 * 60 * 1000,
  status: 'active',
  algorithmType: 'hybrid',
  totalUtility: 283.7,
  tasksScheduled: 4,
  tasksUnscheduled: 2,
  version: 1,
  activeStatus: 'ACTIVE',
  createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
  updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
};

// Mock Focus Time Blocks
export const mockFocusTimeBlocks: FocusTimeBlock[] = [
  {
    id: 'focus-1',
    userId: 'user-1',
    tenantId: 'tenant-1',
    blockName: 'Morning Deep Work',
    dayOfWeek: 1, // Monday
    startMin: 540, // 9:00 AM
    endMin: 720, // 12:00 PM
    allowMeetings: false,
    allowRegularTasks: false,
    flexibilityLevel: 20,
    isEnabled: true,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'focus-2',
    userId: 'user-1',
    tenantId: 'tenant-1',
    blockName: 'Afternoon Focus',
    dayOfWeek: 3, // Wednesday
    startMin: 840, // 2:00 PM
    endMin: 1020, // 5:00 PM
    allowMeetings: false,
    allowRegularTasks: true,
    flexibilityLevel: 40,
    isEnabled: true,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
  },
];

// Mock Task Templates
export const mockTaskTemplates: TaskTemplate[] = [
  {
    id: 'tpl-1',
    userId: 'user-1',
    tenantId: 'tenant-1',
    templateName: 'Code Review',
    titleTemplate: 'Review PR: {{pr_number}}',
    descriptionTemplate:
      'Review and provide feedback on pull request #{{pr_number}}',
    estimatedDurationHours: 1,
    priority: 'MEDIUM',
    category: 'Development',
    tags: ['code-review'],
    isDeepWork: true,
    isFavorite: true,
    usageCount: 45,
    lastUsedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'tpl-2',
    userId: 'user-1',
    tenantId: 'tenant-1',
    templateName: 'Weekly Planning',
    titleTemplate: 'Plan Week of {{week_date}}',
    descriptionTemplate: 'Review goals and plan tasks for the upcoming week',
    estimatedDurationHours: 2,
    priority: 'HIGH',
    category: 'Planning',
    tags: ['planning', 'weekly'],
    isDeepWork: false,
    isFavorite: true,
    usageCount: 12,
    lastUsedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 60 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
  },
];

// Helper to simulate API delay
export const delay = (ms: number = 300) =>
  new Promise((resolve) => setTimeout(resolve, ms));

// Mock Notes
export const mockNotes: import('../types').Note[] = [
  {
    id: 'note-1',
    userId: 'user-1',
    tenantId: 'tenant-1',
    taskId: '1',
    content:
      '# Review Notes\n\nKey points to check:\n- Authentication logic\n- Error handling\n- Test coverage\n\n**Priority areas:**\n- Security validations\n- Input sanitization',
    contentPlain:
      'Review Notes Key points to check: Authentication logic Error handling Test coverage Priority areas: Security validations Input sanitization',
    attachments: [],
    isPinned: true,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'note-2',
    userId: 'user-1',
    tenantId: 'tenant-1',
    projectId: 'proj-1',
    content:
      '## Project Setup\n\nInitial configurations done:\n- Database schema\n- API endpoints\n- Authentication flow\n\nNext steps:\n- Add unit tests\n- Setup CI/CD',
    contentPlain:
      'Project Setup Initial configurations done: Database schema API endpoints Authentication flow Next steps: Add unit tests Setup CI/CD',
    attachments: [],
    isPinned: false,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'note-3',
    userId: 'user-1',
    tenantId: 'tenant-1',
    taskId: '3',
    content:
      '### Design Tokens\n\nColors: `#3B82F6`, `#10B981`, `#F59E0B`\n\nTypography:\n- Heading: Inter Bold\n- Body: Inter Regular\n\nSpacing: 4px, 8px, 16px, 24px, 32px',
    contentPlain:
      'Design Tokens Colors: #3B82F6, #10B981, #F59E0B Typography: Heading: Inter Bold Body: Inter Regular Spacing: 4px, 8px, 16px, 24px, 32px',
    attachments: [
      {
        name: 'design-specs.pdf',
        url: 'https://example.com/files/design-specs.pdf',
        size: 2048000,
        type: 'application/pdf',
      },
    ],
    isPinned: false,
    activeStatus: 'ACTIVE',
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  },
];
