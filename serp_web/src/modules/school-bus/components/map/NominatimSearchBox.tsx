'use client';

import * as React from 'react';
import { Loader2, Search } from 'lucide-react';
import { Button, Input } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useLazySearchMapLocationsQuery } from '../../api/schoolBusApi';
import { schoolBusUi } from '../../theme';
import type { SchoolBusMapLocation } from '../../types';

interface NominatimSearchBoxProps {
  placeholder?: string;
  onSelect: (location: SchoolBusMapLocation) => void;
}

export function NominatimSearchBox({
  placeholder = 'Tìm địa chỉ trên OpenStreetMap',
  onSelect,
}: NominatimSearchBoxProps) {
  const [query, setQuery] = React.useState('');
  const [searchLocations, { data, isFetching }] =
    useLazySearchMapLocationsQuery();

  const results = data?.data || [];

  const handleSearch = async () => {
    if (!query.trim()) {
      return;
    }
    await searchLocations(query.trim());
  };

  return (
    <div className='space-y-3'>
      <div className='flex gap-2'>
        <Input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              event.preventDefault();
              event.stopPropagation();
              void handleSearch();
            }
          }}
          placeholder={placeholder}
        />
        <Button
          type='button'
          variant='outline'
          className={schoolBusUi.outlineButton}
          disabled={isFetching}
          onClick={() => void handleSearch()}
        >
          {isFetching ? (
            <Loader2 className='h-4 w-4 animate-spin' />
          ) : (
            <Search className='h-4 w-4' />
          )}
          Search
        </Button>
      </div>

      {results.length > 0 ? (
        <div className='max-h-40 space-y-2 overflow-y-auto rounded-2xl border p-3'>
          {results.map((result, index) => (
            <button
              key={`${result.displayName}-${index}`}
              type='button'
              className={cn('w-full text-left', schoolBusUi.interactiveCard)}
              onClick={(event) => {
                event.preventDefault();
                event.stopPropagation();
                onSelect(result);
              }}
            >
              <p className='text-sm font-medium text-slate-950'>
                {result.displayName}
              </p>
              <p className='mt-1 text-xs text-slate-500'>
                {result.latitude}, {result.longitude}
              </p>
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}
