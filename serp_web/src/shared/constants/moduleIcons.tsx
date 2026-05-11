/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module icon mapping
 */

import {
  Users,
  ShoppingCart,
  Megaphone,
  CheckSquare,
  Briefcase,
  Calculator,
  FileText,
  UserCog,
  UserPlus,
  Warehouse,
  Settings,
  Headphones,
  MapPin,
  Bus,
  Shield,
  LucideIcon,
  MessageSquare,
} from 'lucide-react';
import { toCanonicalModuleCode } from '@/shared/utils';

export type ModuleCode =
  | 'CRM'
  | 'PURCHASE'
  | 'SALES'
  | 'LOGISTICS'
  | 'LOGISTICS2'
  | 'TMS'
  | 'FIRST_MILE'
  | 'SCHOOLBUS'
  | 'MARKETING'
  | 'PM'
  | 'PTM'
  | 'PROJECT'
  | 'ACCOUNTING'
  | 'INVOICING'
  | 'HR'
  | 'RECRUITMENT'
  | 'INVENTORY'
  | 'MANUFACTURING'
  | 'HELPDESK'
  | 'FIELD_SERVICE'
  | 'DISCUSSION'
  | 'ADMIN'
  | 'SETTINGS'
  | 'TTCRS';

export interface ModuleIconConfig {
  icon: LucideIcon;
  color: string;
  bgColor: string;
}

/**
 * Module icon mapping for all SERP modules
 * Maps module code to Lucide React icon and color scheme
 */
export const MODULE_ICONS: Record<ModuleCode, ModuleIconConfig> = {
  // Admin (Special)
  ADMIN: {
    icon: Shield,
    color: 'text-red-600',
    bgColor: 'bg-red-50 dark:bg-red-950',
  },

  // Settings (Special)
  SETTINGS: {
    icon: Settings,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50 dark:bg-purple-950',
  },

  // Communication
  DISCUSSION: {
    icon: MessageSquare,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
  },

  // Sales & Marketing
  SALES: {
    icon: ShoppingCart,
    color: 'text-green-600',
    bgColor: 'bg-green-50 dark:bg-green-950',
  },
  CRM: {
    icon: Users,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
  },
  MARKETING: {
    icon: Megaphone,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50 dark:bg-purple-950',
  },
  PM: {
    icon: Briefcase,
    color: 'text-indigo-600',
    bgColor: 'bg-indigo-50 dark:bg-indigo-950',
  },

  // Productivity
  PTM: {
    icon: CheckSquare,
    color: 'text-orange-600',
    bgColor: 'bg-orange-50 dark:bg-orange-950',
  },
  PROJECT: {
    icon: Briefcase,
    color: 'text-indigo-600',
    bgColor: 'bg-indigo-50 dark:bg-indigo-950',
  },

  // Warehousing & Logistics
  PURCHASE: {
    icon: ShoppingCart,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
  },
  LOGISTICS: {
    icon: MapPin,
    color: 'text-green-600',
    bgColor: 'bg-green-50 dark:bg-green-950',
  },
  LOGISTICS2: {
    icon: MapPin,
    color: 'text-emerald-600',
    bgColor: 'bg-emerald-50 dark:bg-emerald-950',
  },
  TMS: {
    icon: MapPin,
    color: 'text-cyan-600',
    bgColor: 'bg-cyan-50 dark:bg-cyan-950',
  },
  FIRST_MILE: {
    icon: MapPin,
    color: 'text-cyan-600',
    bgColor: 'bg-cyan-50 dark:bg-cyan-950',
  },
  TTCRS: {
    icon: MapPin,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
  },
  SCHOOLBUS: {
    icon: Bus,
    color: 'text-rose-600',
    bgColor: 'bg-rose-50 dark:bg-rose-950',
  },

  // Finance
  ACCOUNTING: {
    icon: Calculator,
    color: 'text-teal-600',
    bgColor: 'bg-teal-50 dark:bg-teal-950',
  },
  INVOICING: {
    icon: FileText,
    color: 'text-cyan-600',
    bgColor: 'bg-cyan-50 dark:bg-cyan-950',
  },

  // Human Resources
  HR: {
    icon: UserCog,
    color: 'text-pink-600',
    bgColor: 'bg-pink-50 dark:bg-pink-950',
  },
  RECRUITMENT: {
    icon: UserPlus,
    color: 'text-rose-600',
    bgColor: 'bg-rose-50 dark:bg-rose-950',
  },

  // Operations
  INVENTORY: {
    icon: Warehouse,
    color: 'text-amber-600',
    bgColor: 'bg-amber-50 dark:bg-amber-950',
  },
  MANUFACTURING: {
    icon: Settings,
    color: 'text-gray-600',
    bgColor: 'bg-gray-50 dark:bg-gray-950',
  },

  // Support & Service
  HELPDESK: {
    icon: Headphones,
    color: 'text-violet-600',
    bgColor: 'bg-violet-50 dark:bg-violet-950',
  },
  FIELD_SERVICE: {
    icon: MapPin,
    color: 'text-emerald-600',
    bgColor: 'bg-emerald-50 dark:bg-emerald-950',
  },
};

/**
 * Get icon configuration for a module
 */
export const getModuleIcon = (moduleCode: string): ModuleIconConfig | null => {
  const normalizedCode = moduleCode.replace(/-/g, '_').toUpperCase();

  if (toCanonicalModuleCode(normalizedCode) === 'TMS') {
    return MODULE_ICONS.TMS;
  }

  if (normalizedCode in MODULE_ICONS) {
    return MODULE_ICONS[normalizedCode as ModuleCode];
  }

  return null;
};

/**
 * Get module route path
 */
export const getModuleRoute = (moduleCode: string, roles?: string[]): string => {
  const canonicalCode = toCanonicalModuleCode(moduleCode);

  if (canonicalCode === 'ADMIN') return '/admin';
  if (canonicalCode === 'SETTINGS') return '/settings';
  if (canonicalCode === 'DISCUSSION') return '/discuss';
  if (canonicalCode === 'PTM') return '/ptm/dashboard';
  if (canonicalCode === 'CRM') return '/crm/dashboard';
  if (canonicalCode === 'TMS') return '/first-mile';
  if (canonicalCode === 'TTCRS') {
    if (roles?.includes('TTCRS_CUSTOMER'))   return '/ttcrs/customer/requests';
    if (roles?.includes('TTCRS_DRIVER'))     return '/ttcrs/driver/routes';
    if (roles?.includes('TTCRS_DISPATCHER')) return '/ttcrs/dispatcher/requests';
    return '/ttcrs/dispatcher/requests';
  }
  if (canonicalCode === 'SCHOOLBUS') {
    return '/school-bus';
  }

  const normalizedCode = moduleCode.replace(/-/g, '_').toLowerCase();
  return `/${normalizedCode.replace(/_/g, '-')}`;
};

export const getModuleRootRoute = (moduleCode: string): string => {
  const canonicalCode = toCanonicalModuleCode(moduleCode);
  if (canonicalCode === 'ADMIN') return '/admin';
  if (canonicalCode === 'SETTINGS') return '/settings';
  if (canonicalCode === 'PTM') return '/ptm';
  if (canonicalCode === 'CRM') return '/crm';
  if (canonicalCode === 'SCHOOLBUS') return '/school-bus';
  return `/${moduleCode.toLowerCase()}`;
};
