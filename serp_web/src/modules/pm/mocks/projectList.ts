/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM mock project list data
 */

import type { PMProjectListItem } from '../types/project-list.types';

export const PM_PROJECT_LIST_MOCKS: PMProjectListItem[] = [
  {
    id: 'pm-101',
    name: 'Digital Onboarding Hub',
    key: 'DOH',
    description: 'Unified onboarding journeys for retail and SME customers.',
    templateType: 'SCRUM',
    category: 'Customer Experience',
    status: 'ACTIVE',
    lead: {
      id: 'lead-01',
      name: 'Trang Nguyen',
    },
    openItemsCount: 24,
    updatedAt: '2026-05-09T08:15:00.000Z',
    createdAt: '2026-01-18T03:00:00.000Z',
  },
  {
    id: 'pm-102',
    name: 'Core Payment Platform',
    key: 'CPP',
    description:
      'Service modernization for transaction routing and settlement.',
    templateType: 'KANBAN',
    category: 'Core Platform',
    status: 'ACTIVE',
    lead: {
      id: 'lead-02',
      name: 'Minh Tran',
    },
    openItemsCount: 17,
    updatedAt: '2026-05-08T14:40:00.000Z',
    createdAt: '2025-11-06T02:30:00.000Z',
  },
  {
    id: 'pm-103',
    name: 'Merchant Insights Workspace',
    key: 'MIW',
    description: 'Operational dashboards and alerting for merchant analytics.',
    templateType: 'BLANK',
    category: 'Data Products',
    status: 'ACTIVE',
    lead: {
      id: 'lead-03',
      name: 'An Le',
    },
    openItemsCount: 9,
    updatedAt: '2026-05-07T03:25:00.000Z',
    createdAt: '2026-02-24T09:00:00.000Z',
  },
  {
    id: 'pm-104',
    name: 'Risk Rules Engine',
    key: 'RRE',
    description: 'Configurable rules execution for fraud and policy screening.',
    templateType: 'KANBAN',
    category: 'Risk Technology',
    status: 'ACTIVE',
    lead: {
      id: 'lead-04',
      name: 'Hieu Pham',
    },
    openItemsCount: 42,
    updatedAt: '2026-05-06T10:05:00.000Z',
    createdAt: '2025-10-11T05:45:00.000Z',
  },
  {
    id: 'pm-105',
    name: 'Mobile Wallet Revamp',
    key: 'MWR',
    description:
      'Sprint-based upgrade of wallet journeys, growth hooks, and UX.',
    templateType: 'SCRUM',
    category: 'Digital Channels',
    status: 'COMPLETED',
    lead: {
      id: 'lead-05',
      name: 'Linh Do',
    },
    openItemsCount: 0,
    updatedAt: '2026-04-28T16:20:00.000Z',
    createdAt: '2025-08-01T01:15:00.000Z',
  },
  {
    id: 'pm-106',
    name: 'Identity Access Services',
    key: 'IAS',
    description: 'Internal auth and entitlement tooling for engineering teams.',
    templateType: 'BLANK',
    category: 'Internal Enablement',
    status: 'ARCHIVED',
    lead: {
      id: 'lead-06',
      name: 'Bao Hoang',
    },
    openItemsCount: 3,
    updatedAt: '2026-03-11T07:10:00.000Z',
    createdAt: '2025-05-09T04:10:00.000Z',
  },
];
