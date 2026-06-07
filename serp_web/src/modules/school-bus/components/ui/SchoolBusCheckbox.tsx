'use client';

import * as React from 'react';
import * as CheckboxPrimitive from '@radix-ui/react-checkbox';
import { CheckIcon } from 'lucide-react';
import { cn } from '@/shared/utils';

export type SchoolBusCheckboxProps = React.ComponentPropsWithoutRef<typeof CheckboxPrimitive.Root>;

export const SchoolBusCheckbox = React.forwardRef<
  React.ElementRef<typeof CheckboxPrimitive.Root>,
  SchoolBusCheckboxProps
>(({ className, ...props }, ref) => {
  return (
    <CheckboxPrimitive.Root
      ref={ref}
      className={cn(
        'peer h-4 w-4 shrink-0 rounded border border-slate-300 shadow-sm transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#C81E3A] focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
        'data-[state=checked]:bg-[#C81E3A] data-[state=checked]:border-[#C81E3A] data-[state=checked]:text-white',
        'hover:border-[#C81E3A]/70',
        className
      )}
      {...props}
    >
      <CheckboxPrimitive.Indicator className='flex items-center justify-center text-current'>
        <CheckIcon className='h-3.5 w-3.5 stroke-[3]' />
      </CheckboxPrimitive.Indicator>
    </CheckboxPrimitive.Root>
  );
});

SchoolBusCheckbox.displayName = 'SchoolBusCheckbox';
