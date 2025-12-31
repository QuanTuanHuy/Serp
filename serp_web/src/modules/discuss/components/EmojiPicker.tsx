/*
Author: QuanTuanHuy
Description: Part of Serp Project - Emoji Picker Component
*/

'use client';

import { useState } from 'react';
import { Smile } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import { ScrollArea } from '@/shared/components/ui/scroll-area';

interface EmojiPickerProps {
  onEmojiSelect: (emoji: string) => void;
  triggerClassName?: string;
}

// Curated emoji categories with distinctive selection
const EMOJI_CATEGORIES = [
  {
    name: 'Frequently Used',
    emojis: ['👍', '❤️', '😊', '😂', '🎉', '🔥', '✨', '👏', '💯', '🚀'],
  },
  {
    name: 'Smileys & Emotion',
    emojis: [
      '😀',
      '😃',
      '😄',
      '😁',
      '😆',
      '😅',
      '🤣',
      '😂',
      '🙂',
      '🙃',
      '😉',
      '😊',
      '😇',
      '🥰',
      '😍',
      '🤩',
      '😘',
      '😗',
      '😚',
      '😙',
      '😋',
      '😛',
      '😜',
      '🤪',
      '😝',
      '🤑',
      '🤗',
      '🤭',
      '🤫',
      '🤔',
      '🤐',
      '🤨',
      '😐',
      '😑',
      '😶',
      '😏',
      '😒',
      '🙄',
      '😬',
      '🤥',
      '😌',
      '😔',
      '😪',
      '🤤',
      '😴',
      '😷',
      '🤒',
      '🤕',
      '🤢',
      '🤮',
      '🤧',
      '🥵',
      '🥶',
      '😵',
      '🤯',
      '🤠',
      '🥳',
      '😎',
      '🤓',
      '🧐',
    ],
  },
  {
    name: 'Gestures & People',
    emojis: [
      '👋',
      '🤚',
      '🖐️',
      '✋',
      '🖖',
      '👌',
      '🤏',
      '✌️',
      '🤞',
      '🤟',
      '🤘',
      '🤙',
      '👈',
      '👉',
      '👆',
      '🖕',
      '👇',
      '☝️',
      '👍',
      '👎',
      '✊',
      '👊',
      '🤛',
      '🤜',
      '👏',
      '🙌',
      '👐',
      '🤲',
      '🤝',
      '🙏',
      '💪',
      '🦾',
      '🦿',
      '🦵',
      '🦶',
      '👂',
      '🦻',
      '👃',
      '🧠',
      '🫀',
    ],
  },
  {
    name: 'Objects & Symbols',
    emojis: [
      '💼',
      '📁',
      '📂',
      '🗂️',
      '📅',
      '📆',
      '🗒️',
      '📝',
      '📊',
      '📈',
      '📉',
      '📌',
      '📍',
      '✅',
      '❌',
      '⭐',
      '🌟',
      '💫',
      '✨',
      '⚡',
      '🔥',
      '💥',
      '💯',
      '🎯',
      '🎉',
      '🎊',
      '🎈',
      '🎁',
      '🏆',
      '🥇',
      '🚀',
      '💡',
      '🔔',
      '⏰',
      '⌛',
      '⏳',
      '📢',
      '📣',
      '🎵',
      '🎶',
    ],
  },
  {
    name: 'Nature & Food',
    emojis: [
      '🌱',
      '🌿',
      '🍀',
      '🌺',
      '🌸',
      '🌼',
      '🌻',
      '🌞',
      '🌝',
      '⭐',
      '🌈',
      '☀️',
      '⛅',
      '☁️',
      '🌤️',
      '⛈️',
      '🌧️',
      '💧',
      '💦',
      '🌊',
      '🍎',
      '🍊',
      '🍋',
      '🍌',
      '🍉',
      '🍇',
      '🍓',
      '🍒',
      '🍑',
      '🥝',
      '🥑',
      '🍅',
      '🥕',
      '🌽',
      '🥒',
      '🥬',
      '🥦',
      '🍞',
      '🥖',
      '🧀',
      '☕',
      '🍵',
      '🥤',
      '🍷',
      '🍺',
      '🍻',
      '🥂',
      '🍰',
      '🎂',
      '🍪',
    ],
  },
  {
    name: 'Activities & Travel',
    emojis: [
      '⚽',
      '🏀',
      '🏈',
      '⚾',
      '🥎',
      '🎾',
      '🏐',
      '🏉',
      '🥏',
      '🎱',
      '🏓',
      '🏸',
      '🏒',
      '🏑',
      '🥍',
      '🏏',
      '⛳',
      '🏹',
      '🎣',
      '🥊',
      '🎮',
      '🎯',
      '🎲',
      '🎰',
      '🎪',
      '🎭',
      '🎨',
      '🎬',
      '🎤',
      '🎧',
      '🚗',
      '🚕',
      '🚙',
      '🚌',
      '🚎',
      '🏎️',
      '🚓',
      '🚑',
      '🚒',
      '🚐',
      '✈️',
      '🛫',
      '🛬',
      '🚁',
      '🚂',
      '🚆',
      '🚇',
      '🚊',
      '🚝',
      '🚄',
    ],
  },
];

export function EmojiPicker({
  onEmojiSelect,
  triggerClassName,
}: EmojiPickerProps) {
  const [open, setOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(0);

  const handleEmojiClick = (emoji: string) => {
    onEmojiSelect(emoji);
    setOpen(false);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type='button'
          variant='ghost'
          size='icon'
          className={triggerClassName}
          aria-label='Insert emoji'
        >
          <Smile className='h-5 w-5' />
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className='w-80 p-0 border-2 border-violet-200 dark:border-violet-800 shadow-2xl bg-white dark:bg-slate-900'
        align='end'
        sideOffset={8}
      >
        <div className='flex flex-col h-96'>
          {/* Category Tabs */}
          <div className='flex gap-1 p-2 border-b-2 border-violet-100 dark:border-violet-800 bg-gradient-to-r from-violet-50 to-fuchsia-50 dark:from-violet-900/30 dark:to-fuchsia-900/30'>
            {EMOJI_CATEGORIES.map((category, index) => (
              <button
                key={category.name}
                onClick={() => setSelectedCategory(index)}
                className={`
                  px-3 py-1.5 text-xs font-semibold rounded-lg transition-all duration-200
                  ${
                    selectedCategory === index
                      ? 'bg-gradient-to-r from-violet-500 to-fuchsia-600 text-white shadow-lg'
                      : 'text-gray-600 dark:text-gray-400 hover:bg-white dark:hover:bg-slate-800 hover:text-violet-600 dark:hover:text-violet-400'
                  }
                `}
                title={category.name}
              >
                {category.name === 'Frequently Used' && '⚡'}
                {category.name === 'Smileys & Emotion' && '😊'}
                {category.name === 'Gestures & People' && '👋'}
                {category.name === 'Objects & Symbols' && '💼'}
                {category.name === 'Nature & Food' && '🌿'}
                {category.name === 'Activities & Travel' && '⚽'}
              </button>
            ))}
          </div>

          {/* Category Name */}
          <div className='px-4 py-2 bg-white dark:bg-slate-900 border-b border-gray-100 dark:border-slate-800'>
            <h3 className='text-sm font-bold text-gray-700 dark:text-gray-300'>
              {EMOJI_CATEGORIES[selectedCategory].name}
            </h3>
          </div>

          {/* Emoji Grid */}
          <ScrollArea className='flex-1 p-2'>
            <div className='grid grid-cols-8 gap-1'>
              {EMOJI_CATEGORIES[selectedCategory].emojis.map((emoji, index) => (
                <button
                  key={`${emoji}-${index}`}
                  onClick={() => handleEmojiClick(emoji)}
                  className='
                    w-9 h-9 flex items-center justify-center text-2xl
                    rounded-lg transition-all duration-150
                    hover:bg-gradient-to-br hover:from-violet-100 hover:to-fuchsia-100
                    dark:hover:from-violet-900/50 dark:hover:to-fuchsia-900/50
                    hover:scale-125 hover:shadow-md
                    active:scale-110
                  '
                  title={emoji}
                >
                  {emoji}
                </button>
              ))}
            </div>
          </ScrollArea>

          {/* Footer */}
          <div className='px-4 py-2 border-t-2 border-violet-100 dark:border-violet-800 bg-gradient-to-r from-violet-50 to-fuchsia-50 dark:from-violet-900/30 dark:to-fuchsia-900/30'>
            <p className='text-xs text-gray-500 dark:text-gray-400 text-center font-medium'>
              Click an emoji to insert
            </p>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
}
