/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM mock project detail data
 */

import type { PMProjectDetail } from '../types/project-detail.types';
import { PM_PROJECT_LIST_MOCKS } from './projectList';

const PROJECT_DETAIL_OVERRIDES: Record<
  string,
  Pick<PMProjectDetail, 'visibility' | 'startDate' | 'targetDate'>
> = {
  'pm-101': {
    visibility: 'TEAM',
    startDate: '2026-01-20',
    targetDate: '2026-09-30',
  },
  'pm-102': {
    visibility: 'ORGANIZATION',
    startDate: '2025-11-10',
    targetDate: '2026-12-31',
  },
  'pm-103': {
    visibility: 'TEAM',
    startDate: '2026-02-24',
    targetDate: '2026-08-15',
  },
  'pm-104': {
    visibility: 'PRIVATE',
    startDate: '2025-10-14',
    targetDate: '2026-10-30',
  },
  'pm-105': {
    visibility: 'ORGANIZATION',
    startDate: '2025-08-04',
    targetDate: '2026-04-30',
  },
  'pm-106': {
    visibility: 'PRIVATE',
    startDate: '2025-05-12',
    targetDate: '2025-12-31',
  },
};

export const PM_PROJECT_DETAIL_MOCKS: PMProjectDetail[] =
  PM_PROJECT_LIST_MOCKS.map((project) => ({
    ...project,
    ...(PROJECT_DETAIL_OVERRIDES[project.id] || {
      visibility: 'TEAM',
      startDate: project.createdAt.slice(0, 10),
      targetDate: project.updatedAt.slice(0, 10),
    }),
  }));

export function getPMProjectDetailMockById(projectId: string) {
  return (
    PM_PROJECT_DETAIL_MOCKS.find((project) => project.id === projectId) || null
  );
}
