/**
 * PTM v2 - Header Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Top header with search
 */

'use client';

import { useSelector, useDispatch } from 'react-redux';
import { Search, Bell, User, Command } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { openCommandPalette } from '../../store/uiSlice';
import { cn } from '@/shared/utils';
import type { PTMState } from '../../store';

export function PTMHeader() {
  const dispatch = useDispatch();
  const { activeView } = useSelector(
    (state: { ptm: PTMState }) => state.ptm.ui
  );

  const viewTitles = {
    dashboard: 'Dashboard',
    tasks: 'Tasks',
    projects: 'Projects',
    schedule: 'Schedule',
    analytics: 'Analytics',
  };

  const handleOpenCommandPalette = () => {
    dispatch(openCommandPalette());
  };

  return (
    <header className='h-16 border-b bg-card flex items-center justify-between px-6'>
      {/* Left: Title & Search */}
      <div className='flex items-center gap-4 flex-1'>
        <h1 className='text-xl font-semibold'>{viewTitles[activeView]}</h1>

        <div className='relative max-w-md flex-1'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Search tasks, projects...'
            className='pl-9 pr-20'
            onClick={handleOpenCommandPalette}
            readOnly
          />
          <div className='absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1'>
            <kbd className='px-2 py-1 text-xs bg-muted rounded'>
              <Command className='h-3 w-3 inline' />K
            </kbd>
          </div>
        </div>
      </div>

      {/* Right: Actions */}
      <div className='flex items-center gap-2'>
        {/* Notifications */}
        <Button
          variant='ghost'
          size='icon'
          className='relative'
          aria-label='Notifications'
        >
          <Bell className='h-5 w-5' />
          <span className='absolute top-1 right-1 h-2 w-2 bg-red-500 rounded-full' />
        </Button>

        {/* User Menu */}
        <Button variant='ghost' size='icon' aria-label='User menu'>
          <User className='h-5 w-5' />
        </Button>
      </div>
    </header>
  );
}
