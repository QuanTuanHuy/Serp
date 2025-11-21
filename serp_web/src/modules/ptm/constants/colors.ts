/**
 * PTM v2 - Color Palette Constants
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM v2 color system
 */

export const PTM_COLORS = {
  // Priority colors
  priority: {
    low: {
      bg: 'bg-blue-50 dark:bg-blue-950/30',
      text: 'text-blue-700 dark:text-blue-400',
      border: 'border-blue-200 dark:border-blue-800',
      ring: 'ring-blue-500/20',
      dot: 'bg-blue-500',
    },
    medium: {
      bg: 'bg-amber-50 dark:bg-amber-950/30',
      text: 'text-amber-700 dark:text-amber-400',
      border: 'border-amber-200 dark:border-amber-800',
      ring: 'ring-amber-500/20',
      dot: 'bg-amber-500',
    },
    high: {
      bg: 'bg-red-50 dark:bg-red-950/30',
      text: 'text-red-700 dark:text-red-400',
      border: 'border-red-200 dark:border-red-800',
      ring: 'ring-red-500/20',
      dot: 'bg-red-500',
    },
  },

  // Status colors
  status: {
    todo: {
      bg: 'bg-gray-50 dark:bg-gray-900',
      text: 'text-gray-600 dark:text-gray-400',
      dot: 'bg-gray-400',
      border: 'border-gray-200 dark:border-gray-800',
    },
    in_progress: {
      bg: 'bg-blue-50 dark:bg-blue-950/30',
      text: 'text-blue-700 dark:text-blue-400',
      dot: 'bg-blue-500',
      border: 'border-blue-200 dark:border-blue-800',
    },
    done: {
      bg: 'bg-green-50 dark:bg-green-950/30',
      text: 'text-green-700 dark:text-green-400',
      dot: 'bg-green-500',
      border: 'border-green-200 dark:border-green-800',
    },
    cancelled: {
      bg: 'bg-gray-50 dark:bg-gray-900',
      text: 'text-gray-400 dark:text-gray-600',
      dot: 'bg-gray-300',
      border: 'border-gray-200 dark:border-gray-800',
    },
  },

  // Project colors (user customizable)
  projects: [
    { hex: '#3B82F6', name: 'Blue' },
    { hex: '#8B5CF6', name: 'Purple' },
    { hex: '#EC4899', name: 'Pink' },
    { hex: '#F59E0B', name: 'Amber' },
    { hex: '#10B981', name: 'Green' },
    { hex: '#06B6D4', name: 'Cyan' },
    { hex: '#F97316', name: 'Orange' },
    { hex: '#6366F1', name: 'Indigo' },
  ],

  // Focus time
  focusTime: {
    bg: 'bg-purple-50 dark:bg-purple-950/20',
    border: 'border-purple-200 dark:border-purple-800',
    text: 'text-purple-700 dark:text-purple-400',
    overlay: 'bg-purple-500/10',
    dot: 'bg-purple-500',
  },

  // Deadline risk
  deadlineRisk: {
    low: 'text-green-600 dark:text-green-400',
    medium: 'text-amber-600 dark:text-amber-400',
    high: 'text-orange-600 dark:text-orange-400',
    critical: 'text-red-600 dark:text-red-400',
  },
};

export const LAYOUT_CONSTANTS = {
  sidebar: {
    width: '240px',
    collapsedWidth: '64px',
  },
  header: {
    height: '64px',
  },
  content: {
    maxWidth: '1440px',
    padding: '24px',
  },
  card: {
    borderRadius: '12px',
    padding: '16px',
  },
};
