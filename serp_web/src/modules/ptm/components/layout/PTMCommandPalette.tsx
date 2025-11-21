/**
 * PTM v2 - Command Palette Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Cmd+K quick actions
 */

'use client';

import { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
  Plus,
  Search,
  Calendar,
  CheckSquare,
  FolderKanban,
  Settings,
  Sparkles,
} from 'lucide-react';
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from '@/shared/components/ui/command';
import {
  closeCommandPalette,
  setActiveView,
  toggleQuickAdd,
} from '../../store/uiSlice';
import type { PTMState } from '../../store';

export function PTMCommandPalette() {
  const dispatch = useDispatch();
  const { commandPaletteOpen } = useSelector(
    (state: { ptm: PTMState }) => state.ptm.ui
  );

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        dispatch(closeCommandPalette());
        setTimeout(() => {
          dispatch(closeCommandPalette());
        }, 0);
      }
    };

    document.addEventListener('keydown', down);
    return () => document.removeEventListener('keydown', down);
  }, [dispatch]);

  const handleClose = () => {
    dispatch(closeCommandPalette());
  };

  const handleQuickAdd = () => {
    handleClose();
    dispatch(toggleQuickAdd());
  };

  const handleNavigate = (
    view: 'dashboard' | 'tasks' | 'projects' | 'schedule' | 'analytics'
  ) => {
    handleClose();
    dispatch(setActiveView(view));
  };

  return (
    <CommandDialog open={commandPaletteOpen} onOpenChange={handleClose}>
      <CommandInput placeholder='Type a command or search...' />
      <CommandList>
        <CommandEmpty>No results found.</CommandEmpty>

        <CommandGroup heading='Quick Actions'>
          <CommandItem onSelect={handleQuickAdd}>
            <Plus className='mr-2 h-4 w-4' />
            <span>Quick Add Task</span>
            <kbd className='ml-auto'>⌘N</kbd>
          </CommandItem>
          <CommandItem>
            <Sparkles className='mr-2 h-4 w-4' />
            <span>Optimize Schedule</span>
            <kbd className='ml-auto'>⌘⇧O</kbd>
          </CommandItem>
        </CommandGroup>

        <CommandSeparator />

        <CommandGroup heading='Navigation'>
          <CommandItem onSelect={() => handleNavigate('dashboard')}>
            <Calendar className='mr-2 h-4 w-4' />
            <span>Dashboard</span>
          </CommandItem>
          <CommandItem onSelect={() => handleNavigate('tasks')}>
            <CheckSquare className='mr-2 h-4 w-4' />
            <span>Tasks</span>
          </CommandItem>
          <CommandItem onSelect={() => handleNavigate('projects')}>
            <FolderKanban className='mr-2 h-4 w-4' />
            <span>Projects</span>
          </CommandItem>
          <CommandItem onSelect={() => handleNavigate('schedule')}>
            <Calendar className='mr-2 h-4 w-4' />
            <span>Schedule</span>
          </CommandItem>
        </CommandGroup>

        <CommandSeparator />

        <CommandGroup heading='Settings'>
          <CommandItem>
            <Settings className='mr-2 h-4 w-4' />
            <span>Preferences</span>
          </CommandItem>
        </CommandGroup>
      </CommandList>
    </CommandDialog>
  );
}
