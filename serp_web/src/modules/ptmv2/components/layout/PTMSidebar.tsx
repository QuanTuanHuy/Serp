/**
 * PTM v2 - Sidebar Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Navigation sidebar
 */

'use client';

import { useSelector, useDispatch } from 'react-redux';
import { useRouter, usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  CheckSquare,
  FolderKanban,
  Calendar,
  BarChart3,
  Settings,
  ChevronLeft,
  ChevronRight,
  Plus,
  Lock,
  AlertTriangle,
} from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { cn } from '@/shared/utils';
import {
  setActiveView,
  collapseSidebar,
  toggleQuickAdd,
} from '../../store/uiSlice';
import { LAYOUT_CONSTANTS } from '../../constants/colors';
import type { PTMState } from '../../store';

const navItems = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    icon: LayoutDashboard,
    href: '/ptmv2',
  },
  { id: 'tasks', label: 'Tasks', icon: CheckSquare, href: '/ptmv2/tasks' },
  {
    id: 'projects',
    label: 'Projects',
    icon: FolderKanban,
    href: '/ptmv2/projects',
  },
  {
    id: 'schedule',
    label: 'Schedule',
    icon: Calendar,
    href: '/ptmv2/schedule',
  },
  {
    id: 'activity',
    label: 'Activity',
    icon: CheckSquare,
    href: '/ptmv2/activity',
  },
  {
    id: 'analytics',
    label: 'Analytics',
    icon: BarChart3,
    href: '/ptmv2/analytics',
  },
  {
    id: 'deadline_risks',
    label: 'Deadline Risks',
    icon: AlertTriangle,
    href: '/ptmv2/deadline-risks',
  },
  {
    id: 'settings',
    label: 'Settings',
    icon: Settings,
    href: '/ptmv2/settings',
  },
] as const;

export function PTMSidebar() {
  const router = useRouter();
  const pathname = usePathname();
  const dispatch = useDispatch();
  const { sidebarCollapsed } = useSelector(
    (state: { ptm: PTMState }) => state.ptm.ui
  );

  const handleToggleCollapse = () => {
    dispatch(collapseSidebar(!sidebarCollapsed));
  };

  const handleNavClick = (item: (typeof navItems)[number]) => {
    // cast to any because `setActiveView` expects a narrower union of ids
    dispatch(setActiveView(item.id as any));
    router.push(item.href);
  };

  const handleQuickAdd = () => {
    dispatch(toggleQuickAdd());
  };

  return (
    <aside
      className={cn(
        'flex flex-col border-r bg-card transition-all duration-300',
        sidebarCollapsed ? 'w-16' : 'w-60'
      )}
    >
      {/* Logo */}
      <div className='h-16 flex items-center justify-between px-4 border-b'>
        {!sidebarCollapsed && (
          <div className='flex items-center gap-2'>
            <div className='w-8 h-8 rounded-lg bg-gradient-to-br from-purple-500 to-blue-500 flex items-center justify-center'>
              <span className='text-white font-bold text-sm'>PTM</span>
            </div>
            <span className='font-semibold text-lg'>Task Manager</span>
          </div>
        )}

        <Button
          variant='ghost'
          size='icon'
          onClick={handleToggleCollapse}
          className='h-8 w-8'
        >
          {sidebarCollapsed ? (
            <ChevronRight className='h-4 w-4' />
          ) : (
            <ChevronLeft className='h-4 w-4' />
          )}
        </Button>
      </div>

      {/* Navigation */}
      <nav className='flex-1 px-3 py-2 space-y-1'>
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;

          return (
            <Button
              key={item.id}
              variant={isActive ? 'secondary' : 'ghost'}
              className={cn(
                'w-full justify-start gap-3',
                sidebarCollapsed && 'justify-center px-2',
                isActive && 'bg-primary/10 text-primary hover:bg-primary/20'
              )}
              size={sidebarCollapsed ? 'icon' : 'default'}
              onClick={() => handleNavClick(item)}
            >
              <Icon className='h-5 w-5' />
              {!sidebarCollapsed && <span>{item.label}</span>}
            </Button>
          );
        })}
      </nav>

      {/* Settings */}
      <div className='p-3 border-t'>
        <Button
          variant='ghost'
          className={cn(
            'w-full justify-start gap-3',
            sidebarCollapsed && 'justify-center px-2'
          )}
          size={sidebarCollapsed ? 'icon' : 'default'}
        >
          <Settings className='h-5 w-5' />
          {!sidebarCollapsed && <span>Settings</span>}
        </Button>
      </div>
    </aside>
  );
}
