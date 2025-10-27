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
  Shield,
  LucideIcon,
} from 'lucide-react';

export type ModuleCode =
  | 'CRM'
  | 'SALES'
  | 'MARKETING'
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
  | 'ADMIN';

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

  // Sales & Marketing
  CRM: {
    icon: Users,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50 dark:bg-blue-950',
  },
  SALES: {
    icon: ShoppingCart,
    color: 'text-green-600',
    bgColor: 'bg-green-50 dark:bg-green-950',
  },
  MARKETING: {
    icon: Megaphone,
    color: 'text-purple-600',
    bgColor: 'bg-purple-50 dark:bg-purple-950',
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
  return MODULE_ICONS[moduleCode as ModuleCode] || null;
};

/**
 * Get module route path
 */
export const getModuleRoute = (moduleCode: string): string => {
  if (moduleCode === 'ADMIN') return '/admin';
  if (moduleCode === 'PTM') return '/ptm';
  if (moduleCode === 'CRM') return '/crm';
  // Add more routes as needed
  return `/${moduleCode.toLowerCase()}`;
};
