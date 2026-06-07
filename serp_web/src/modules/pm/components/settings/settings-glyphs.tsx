/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings glyphs
 */

import {
  Bug,
  CheckSquare,
  ChevronDown,
  ChevronUp,
  ChevronsDown,
  ChevronsUp,
  Copy,
  Minus,
  Zap,
} from 'lucide-react';

export function WorkTypeGlyph({
  workType,
}: {
  workType: { typeKey?: string; hierarchyLevel?: number | null };
}) {
  const key = workType.typeKey?.toLowerCase() || '';
  if (key.includes('bug')) {
    return <Bug className='h-4 w-4 text-red-500' />;
  }
  if (key.includes('epic') || workType.hierarchyLevel === 2) {
    return <Zap className='h-4 w-4 text-purple-500' />;
  }
  if (key.includes('story')) {
    return <CheckSquare className='h-4 w-4 text-blue-500' />;
  }
  return <Copy className='h-4 w-4 text-sky-500' />;
}

export function PriorityGlyph({
  priority,
}: {
  priority: {
    priorityKey?: string;
    name?: string;
    iconUrl?: string | null;
    color?: string | null;
  };
}) {
  if (priority.iconUrl) {
    return (
      <span
        aria-hidden='true'
        className='h-4 w-4 shrink-0 bg-contain bg-center bg-no-repeat'
        style={{ backgroundImage: `url(${priority.iconUrl})` }}
      />
    );
  }

  const value =
    `${priority.priorityKey ?? ''} ${priority.name ?? ''}`.toLowerCase();
  const style = priority.color ? { color: priority.color } : undefined;
  if (value.includes('highest')) {
    return <ChevronsUp className='h-4 w-4' style={style} />;
  }
  if (value.includes('high')) {
    return <ChevronUp className='h-4 w-4' style={style} />;
  }
  if (value.includes('lowest')) {
    return <ChevronsDown className='h-4 w-4' style={style} />;
  }
  if (value.includes('low')) {
    return <ChevronDown className='h-4 w-4' style={style} />;
  }
  return <Minus className='h-4 w-4' style={style} />;
}
