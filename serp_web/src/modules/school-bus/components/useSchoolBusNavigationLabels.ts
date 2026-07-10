'use client';

import { useCallback, useMemo } from 'react';
import { useModuleSidebar } from '@/shared/hooks';
import type { SidebarMenuItem } from '@/shared/hooks';
import { normalizePath } from '@/shared/utils';

const SCHOOL_BUS_MODULE_CODE = 'SCHOOLBUS';
const SCHOOL_BUS_MODULE_FALLBACK = 'Xe bus trường học';

const STATIC_PATH_LABELS: Record<string, string> = {
  '/school-bus/dashboard': 'Tổng quan',
  '/school-bus/schools': 'Trường học',
  '/school-bus/students': 'Học sinh',
  '/school-bus/parents': 'Phụ huynh',
  '/school-bus/fleet': 'Đội xe',
  '/school-bus/requests': 'Yêu cầu xe buýt',
  '/school-bus/subscriptions': 'Đăng ký dịch vụ',
  '/school-bus/dispatch': 'Điều phối',
  '/school-bus/dispatch/planning': 'Lập kế hoạch tuyến',
  '/school-bus/trips': 'Vận hành chuyến',
  '/school-bus/reports': 'Báo cáo',
};

const STATIC_SEGMENT_LABELS: Record<string, string> = {
  new: 'Tạo mới',
  edit: 'Chỉnh sửa',
  planning: 'Lập kế hoạch tuyến',
};

function buildLabelByPath(items: SidebarMenuItem[]): Map<string, string> {
  const labelByPath = new Map<string, string>();

  const walk = (menuItems: SidebarMenuItem[]) => {
    menuItems.forEach((item) => {
      labelByPath.set(normalizePath(item.href), item.name);

      if (item.children?.length) {
        walk(item.children);
      }
    });
  };

  walk(items);

  return labelByPath;
}

function formatSegmentLabel(segment: string): string {
  const decoded = decodeURIComponent(segment);
  const normalized = decoded.toLowerCase();

  if (STATIC_SEGMENT_LABELS[normalized]) {
    return STATIC_SEGMENT_LABELS[normalized];
  }

  if (/^\d+$/.test(decoded)) {
    return decoded;
  }

  return decoded
    .replace(/-/g, ' ')
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

export function useSchoolBusNavigationLabels() {
  const { menuItems, currentModule } = useModuleSidebar(SCHOOL_BUS_MODULE_CODE);

  const labelByPath = useMemo(() => buildLabelByPath(menuItems), [menuItems]);

  const getPathLabel = useCallback(
    (path?: string, fallback?: string) => {
      if (!path) {
        return fallback;
      }

      const normalizedPath = normalizePath(path.split('?')[0] || path);

      return (
        labelByPath.get(normalizedPath) ??
        STATIC_PATH_LABELS[normalizedPath] ??
        fallback
      );
    },
    [labelByPath]
  );

  const getSegmentLabel = useCallback(
    (segment: string, href: string) => {
      return getPathLabel(href) ?? formatSegmentLabel(segment);
    },
    [getPathLabel]
  );

  return {
    moduleLabel: currentModule?.moduleName || SCHOOL_BUS_MODULE_FALLBACK,
    getPathLabel,
    getSegmentLabel,
  };
}
