/*
Author: QuanTuanHuy
Description: Part of Serp Project - Search dialog for channel messages
*/

'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { BarChart3 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { SearchBar } from './SearchBar';
import { SearchResults } from './SearchResults';
import { useSearchMessagesQuery } from '../api/discussApi';
import type { SearchFilters, Message } from '../types';

// =============================================================================
// Types
// =============================================================================

interface SearchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  channelId: string;
  onResultClick?: (channelId: string, messageId: string) => void;
}

// =============================================================================
// Constants
// =============================================================================

const PAGE_SIZE = 15;

// =============================================================================
// Component
// =============================================================================

export function SearchDialog({
  open,
  onOpenChange,
  channelId,
  onResultClick,
}: SearchDialogProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState<SearchFilters>({});
  const [page, setPage] = useState(1);
  const [allResults, setAllResults] = useState<Message[]>([]);

  // Track previous data to avoid duplicate appends (rerender-dependencies)
  const prevDataRef = useRef<typeof data>(undefined);

  // RTK Query - search messages
  const { data, isLoading, isFetching } = useSearchMessagesQuery(
    {
      channelId,
      query: searchQuery,
      pagination: { page, limit: PAGE_SIZE },
    },
    {
      skip: !searchQuery.trim() || !open,
    }
  );

  // Accumulate results for pagination (only when data actually changes)
  useEffect(() => {
    if (!data?.data?.items || data === prevDataRef.current) return;
    prevDataRef.current = data;

    const newItems = data.data.items;
    if (page === 1) {
      setAllResults(newItems);
    } else {
      setAllResults((prev) => [...prev, ...newItems]);
    }
  }, [data, page]);

  // Reset pagination when query changes
  useEffect(() => {
    setPage(1);
    setAllResults([]);
    prevDataRef.current = undefined;
  }, [searchQuery]);

  const handleLoadMore = useCallback(() => {
    if (data?.data?.hasNext && !isFetching) {
      setPage((p) => p + 1);
    }
  }, [data?.data?.hasNext, isFetching]);

  const handleResultClick = useCallback(
    (channelId: string, messageId: string) => {
      onResultClick?.(channelId, messageId);
      onOpenChange(false);
    },
    [onResultClick, onOpenChange]
  );

  const handleClose = useCallback(() => {
    onOpenChange(false);
    // Reset state after close animation
    setTimeout(() => {
      setSearchQuery('');
      setFilters({});
      setPage(1);
      setAllResults([]);
      prevDataRef.current = undefined;
    }, 200);
  }, [onOpenChange]);

  const hasMore = data?.data?.hasNext && !isFetching;
  const totalResults = data?.data?.totalItems ?? 0;

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='max-w-2xl h-[75vh] p-0 gap-0 flex flex-col overflow-hidden'>
        {/* Header with search bar */}
        <DialogHeader className='p-5 pb-3 border-b flex-shrink-0'>
          <DialogTitle className='text-lg font-semibold text-foreground mb-3'>
            Search Messages
          </DialogTitle>

          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            filters={filters}
            onFiltersChange={setFilters}
            placeholder='Search messages in this channel...'
          />

          {/* Search stats */}
          {searchQuery.trim() && !isLoading && totalResults > 0 && (
            <div className='flex items-center gap-2 mt-3 text-xs text-muted-foreground'>
              <BarChart3 className='h-3.5 w-3.5' />
              <span>
                {totalResults} {totalResults === 1 ? 'result' : 'results'} found
              </span>
            </div>
          )}
        </DialogHeader>

        {/* Results area */}
        <div className='flex-1 overflow-hidden'>
          <SearchResults
            results={allResults}
            isLoading={isLoading && page === 1}
            isFetchingMore={isFetching && page > 1}
            hasMore={hasMore}
            onLoadMore={handleLoadMore}
            onResultClick={handleResultClick}
            searchQuery={searchQuery}
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
