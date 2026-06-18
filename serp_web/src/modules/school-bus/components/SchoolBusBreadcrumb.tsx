'use client';

import type { ReactNode } from 'react';
import Link from 'next/link';
import { ChevronRight, Home } from 'lucide-react';
import { cn } from '@/shared/utils';

export interface BreadcrumbItem {
  /** Display text for this crumb. */
  label: string;
  /** If provided, clicking navigates to this URL via Next.js router. */
  href?: string;
  /** Optional icon rendered before the label (only meaningful on first item). */
  icon?: ReactNode;
  /** If provided, clicking scrolls the element with this id into view. */
  sectionId?: string;
  /** Marks this crumb as the current page — non-clickable, visually distinct. */
  current?: boolean;
}

interface SchoolBusBreadcrumbProps {
  items: BreadcrumbItem[];
  className?: string;
}

/**
 * SchoolBusBreadcrumb
 *
 * A proper navigation breadcrumb component with:
 * - Home icon on first item automatically
 * - Chevron separators with good spacing
 * - Hover color matching theme primary (slate)
 * - Current item is bolder/darker, non-clickable
 * - Truncation for long labels
 */
export function SchoolBusBreadcrumb({
  items,
  className,
}: SchoolBusBreadcrumbProps) {
  const handleSectionScroll = (sectionId: string) => {
    const el = document.getElementById(sectionId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <nav
      aria-label='Breadcrumb'
      className={cn(
        'flex h-6 items-center gap-0 flex-wrap overflow-hidden',
        className
      )}
    >
      {items.map((item, index) => {
        const isFirst = index === 0;
        const isLast = index === items.length - 1;

        // Label + icon content
        const labelNode = (
          <span
            className={cn(
              'inline-flex items-center gap-1.5 max-w-[200px] text-[13px] leading-none select-none',
              item.current
                ? 'cursor-default font-semibold text-foreground'
                : 'cursor-pointer font-medium text-muted-foreground transition-colors duration-150 hover:text-foreground active:text-foreground'
            )}
          >
            {item.icon ? (
              <span className='shrink-0 flex items-center'>{item.icon}</span>
            ) : isFirst ? (
              <Home
                className={cn(
                  'h-[14px] w-[14px] shrink-0',
                  item.current
                    ? 'text-foreground'
                    : 'text-muted-foreground group-hover:text-foreground'
                )}
                aria-hidden='true'
              />
            ) : null}
            <span className='truncate'>{item.label}</span>
          </span>
        );

        // Choose appropriate wrapper
        let crumb: ReactNode;
        if (item.current) {
          crumb = <span aria-current='page'>{labelNode}</span>;
        } else if (item.sectionId) {
          crumb = (
            <button
              type='button'
              className='group rounded-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1'
              onClick={() => handleSectionScroll(item.sectionId!)}
            >
              {labelNode}
            </button>
          );
        } else if (item.href) {
          crumb = (
            <Link
              href={item.href}
              className='group rounded-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1'
            >
              {labelNode}
            </Link>
          );
        } else {
          crumb = labelNode;
        }

        return (
          <span key={index} className='flex items-center'>
            {crumb}
            {!isLast && (
              <ChevronRight
                className='mx-1.5 h-3 w-3 shrink-0 text-border'
                aria-hidden='true'
              />
            )}
          </span>
        );
      })}
    </nav>
  );
}
