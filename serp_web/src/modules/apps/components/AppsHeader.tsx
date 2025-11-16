/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Apps Header Component
 */

'use client';

import { Search } from 'lucide-react';
import { Input } from '@/shared/components/ui';

interface AppsHeaderProps {
  searchValue: string;
  onSearchChange: (value: string) => void;
}

export function AppsHeader({ searchValue, onSearchChange }: AppsHeaderProps) {
  return (
    <div className='space-y-6'>
      <div className='text-center'>
        <h1 className='text-4xl md:text-5xl font-bold mb-4'>
          Explore Our Applications
        </h1>
        <p className='text-xl text-muted-foreground max-w-3xl mx-auto'>
          Discover powerful tools to transform your business operations. From
          CRM to accounting, find the perfect solution for your needs.
        </p>
      </div>

      {/* Search Bar */}
      <div className='max-w-2xl mx-auto'>
        <div className='relative'>
          <Search className='absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-5 h-5' />
          <Input
            type='text'
            placeholder='Search applications by name, category, or description...'
            value={searchValue}
            onChange={(e) => onSearchChange(e.target.value)}
            className='pl-10 h-12 text-base'
          />
        </div>
      </div>
    </div>
  );
}
