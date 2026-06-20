/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search page
 */

'use client';

import { useSearchParams } from 'next/navigation';

import { useGetPmGlobalSearchQuery } from '../api';
import { PMGlobalSearchResults } from '../components/search';

export function PMGlobalSearchPage() {
  const searchParams = useSearchParams();
  const query = searchParams.get('q')?.trim() ?? '';
  const shouldSearch = query.length >= 2;
  const { data, isFetching, isError } = useGetPmGlobalSearchQuery(
    { q: query, limit: 10 },
    { skip: !shouldSearch }
  );

  if (!shouldSearch) {
    return (
      <div className='mx-auto w-full max-w-4xl px-6 py-8'>
        <h1 className='text-2xl font-bold'>Search PM</h1>
        <p className='mt-2 text-sm text-muted-foreground'>
          Enter at least two characters from the PM search box.
        </p>
      </div>
    );
  }

  return (
    <div className='mx-auto w-full max-w-4xl px-6 py-8'>
      <div className='mb-6'>
        <h1 className='text-2xl font-bold'>Search results</h1>
        <p className='mt-2 text-sm text-muted-foreground'>
          Results for "{query}"
        </p>
      </div>

      {isFetching && (
        <div className='rounded-md border p-6 text-sm text-muted-foreground'>
          Searching...
        </div>
      )}

      {isError && !isFetching && (
        <div className='rounded-md border border-destructive/30 p-6 text-sm text-destructive'>
          Search failed. Refresh the page or try another query.
        </div>
      )}

      {!isFetching && !isError && (
        <PMGlobalSearchResults groups={data?.groups ?? []} />
      )}
    </div>
  );
}
