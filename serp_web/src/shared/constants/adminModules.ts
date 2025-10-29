/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module icon mapping
 */

import { ModuleDisplayItem } from '@/modules/account';

export const DEFAULT_ADMIN_MODULES: ModuleDisplayItem[] = [
  {
    code: 'ADMIN',
    name: 'Admin',
    description: 'System Administration',
    href: '/admin',
    isActive: true,
    isAdmin: true,
  },
  {
    code: 'SETTINGS',
    name: 'Settings',
    description: 'Organization Settings',
    href: '/settings',
    isActive: true,
    isAdmin: true,
  },
  {
    code: 'PTM',
    name: 'PTM',
    description: 'Project and Task Management',
    href: '/ptm',
    isActive: true,
    isAdmin: true,
  },
  {
    code: 'CRM',
    name: 'CRM',
    description: 'Customer Relationship Management',
    href: '/crm',
    isActive: true,
    isAdmin: true,
  },
];

export const DEFAULT_ORG_ADMIN_MODULES: ModuleDisplayItem[] = [
  {
    code: 'SETTINGS',
    name: 'Settings',
    description: 'Organization Settings',
    href: '/settings',
    isActive: true,
    isAdmin: true,
  },
];
