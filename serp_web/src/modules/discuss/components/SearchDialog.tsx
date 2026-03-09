/*
Author: QuanTuanHuy
Description: Part of Serp Project - Full-screen search dialog with Cmd+K shortcut
*/

'use client';

import { useState, useEffect, useCallback } from 'react';
import { X, TrendingUp, BarChart3 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { SearchBar } from './SearchBar';
import { SearchResults } from './SearchResults';
import { useSearchMessagesQuery } from '../api/discussApi';
import type { SearchFilters, GroupedSearchResults } from '../types';

interface SearchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  channelId: string; // Required for backend API
  onResultClick?: (channelId: string, messageId: string) => void;
}

const MAX_RECENT_SEARCHES = 10;
const RECENT_SEARCHES_KEY = 'discuss_recent_searches';

export function SearchDialog({
  open,
  onOpenChange,
  channelId,
  onResultClick,
}: SearchDialogProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [filters, setFilters] = useState<SearchFilters>({});
  const [page, setPage] = useState(1);
  const [recentSearches, setRecentSearches] = useState<string[]>([]);
  const [allResults, setAllResults] = useState<any[]>([]); // Messages instead of GroupedSearchResults

  // Load recent searches from localStorage
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem(RECENT_SEARCHES_KEY);
      if (saved) {
        try {
          setRecentSearches(JSON.parse(saved));
        } catch {
          // Ignore parse errors
        }
      }
    }
  }, []);

  // Save recent searches to localStorage
  const saveRecentSearch = useCallback((query: string) => {
    if (!query.trim()) return;

    setRecentSearches((prev) => {
      const updated = [query, ...prev.filter((q) => q !== query)].slice(
        0,
        MAX_RECENT_SEARCHES
      );
      if (typeof window !== 'undefined') {
        localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
      }
      return updated;
    });
  }, []);

  const clearRecentSearches = useCallback(() => {
    setRecentSearches([]);
    if (typeof window !== 'undefined') {
      localStorage.removeItem(RECENT_SEARCHES_KEY);
    }
  }, []);

  // RTK Query for search (channel-specific)
  const { data, isLoading, isFetching } = useSearchMessagesQuery(
    {
      channelId,
      query: searchQuery,
      pagination: { page, limit: 10 },
    },
    {
      skip: !searchQuery.trim(),
    }
  );

  // Accumulate results for infinite scroll
  useEffect(() => {
    if (data?.data?.items) {
      if (page === 1) {
        setAllResults(data.data.items);
      } else {
        setAllResults((prev) => [...prev, ...data.data.items]);
      }
    }
  }, [data, page]);

  // Reset pagination when query or filters change
  useEffect(() => {
    setPage(1);
    setAllResults([]);
  }, [searchQuery, filters]);

  const handleSearch = useCallback(
    (query: string, searchFilters: SearchFilters) => {
      if (query.trim()) {
        saveRecentSearch(query);
      }
    },
    [saveRecentSearch]
  );

  const handleLoadMore = useCallback(() => {
    if (data?.data?.hasNext) {
      setPage((p) => p + 1);
    }
  }, [data, page]);

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
    }, 200);
  }, [onOpenChange]);

  // Global keyboard shortcut: Cmd+K / Ctrl+K
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        onOpenChange(!open);
      }
      // Escape to close
      if (e.key === 'Escape' && open) {
        handleClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [open, onOpenChange, handleClose]);

  const hasMore = data?.data?.hasNext && !isFetching;

  const totalResults = data?.data?.totalItems || 0;
  const searchTime = isLoading ? 0 : 0.3; // Mock search time

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-5xl h-[85vh] p-0 gap-0 bg-slate-900 border-white/10'>
        {/* Header with search bar */}
        <DialogHeader className='p-6 pb-4 border-b border-white/10'>
          <div className='flex items-center justify-between mb-4'>
            <DialogTitle className='text-2xl font-bold bg-gradient-to-r from-violet-400 to-fuchsia-400 bg-clip-text text-transparent'>
              Search Messages
            </DialogTitle>
            <button
              onClick={handleClose}
              className='rounded-full p-2 hover:bg-white/10 transition-colors'
            >
              <X className='h-5 w-5' />
            </button>
          </div>

          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            onSearch={handleSearch}
            filters={filters}
            onFiltersChange={setFilters}
            recentSearches={recentSearches}
            onSelectRecent={setSearchQuery}
            onClearRecent={clearRecentSearches}
            placeholder='Search across all channels...'
          />

          {/* Search analytics */}
          {searchQuery && !isLoading && (
            <div className='flex items-center gap-6 mt-4 text-sm text-slate-400'>
              <div className='flex items-center gap-2'>
                <BarChart3 className='h-4 w-4' />
                <span>
                  {totalResults} {totalResults === 1 ? 'result' : 'results'}
                </span>
              </div>
              <div className='flex items-center gap-2'>
                <TrendingUp className='h-4 w-4' />
                <span>{searchTime.toFixed(2)}s search time</span>
              </div>
            </div>
          )}
        </DialogHeader>

        {/* Results area */}
        <div className='flex-1 overflow-hidden'>
          <SearchResults
            results={allResults}
            isLoading={isLoading && page === 1}
            hasMore={hasMore}
            onLoadMore={handleLoadMore}
            onResultClick={handleResultClick}
            searchQuery={searchQuery}
          />
        </div>

        {/* Footer gradient */}
        <div className='h-2 bg-gradient-to-r from-violet-600/20 via-fuchsia-600/20 to-violet-600/20' />
      </DialogContent>
    </Dialog>
  );
}
