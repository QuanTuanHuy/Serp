/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Icon component mapper utility
 */

import * as LucideIcons from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

/**
 * Map icon name string to Lucide React icon component
 *
 * @param iconName - Name of the Lucide icon (e.g., "LayoutDashboard", "Users")
 * @returns Lucide icon component or fallback Menu icon
 */
export const getIconComponent = (iconName?: string): LucideIcon => {
  if (!iconName) {
    return LucideIcons.Menu;
  }
  const IconComponent = (LucideIcons as any)[iconName];
  return IconComponent || LucideIcons.Menu;
};
