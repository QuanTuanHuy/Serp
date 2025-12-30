/*
Author: QuanTuanHuy
Description: Part of Serp Project - Reaction Picker for Messages
*/

'use client';

import { useState } from 'react';
import { Plus } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';

interface ReactionPickerProps {
  onReactionSelect: (emoji: string) => void;
  existingReactions?: string[];
}

// Quick reactions for fast access
const QUICK_REACTIONS = [
  '👍',
  '❤️',
  '😂',
  '🎉',
  '😮',
  '😢',
  '🔥',
  '👏',
  '✨',
  '💯',
];

export function ReactionPicker({
  onReactionSelect,
  existingReactions = [],
}: ReactionPickerProps) {
  const [open, setOpen] = useState(false);

  const handleReactionClick = (emoji: string) => {
    onReactionSelect(emoji);
    setOpen(false);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant='ghost'
          size='icon'
          className='h-7 w-7 rounded-full hover:bg-violet-100 dark:hover:bg-violet-900/30'
          aria-label='Add reaction'
        >
          <Plus className='h-3.5 w-3.5 text-slate-600 dark:text-slate-400' />
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className='w-auto p-2 border-2 border-violet-200 dark:border-violet-800 shadow-xl bg-white dark:bg-slate-900'
        align='start'
        sideOffset={4}
      >
        <div className='grid grid-cols-5 gap-1'>
          {QUICK_REACTIONS.map((emoji) => (
            <button
              key={emoji}
              onClick={() => handleReactionClick(emoji)}
              disabled={existingReactions.includes(emoji)}
              className={`
                w-10 h-10 flex items-center justify-center text-xl
                rounded-lg transition-all duration-150
                ${
                  existingReactions.includes(emoji)
                    ? 'opacity-40 cursor-not-allowed'
                    : 'hover:bg-gradient-to-br hover:from-violet-100 hover:to-fuchsia-100 dark:hover:from-violet-900/50 dark:hover:to-fuchsia-900/50 hover:scale-125 active:scale-110'
                }
              `}
              title={emoji}
            >
              {emoji}
            </button>
          ))}
        </div>
      </PopoverContent>
    </Popover>
  );
}
