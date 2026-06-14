/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin global search results page
 */

'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { AlertTriangle, ArrowRight, RefreshCw, Search } from 'lucide-react';

import { useGetAdminGlobalSearchQuery } from '@/modules/admin';
import type { AdminGlobalSearchGroup } from '@/modules/admin';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';

const GROUP_VIEW_ALL_HREF: Record<string, string> = {
  ORGANIZATION: '/admin/organizations',
  USER: '/admin/users',
  ROLE: '/admin/roles',
  MODULE: '/admin/modules',
};

const buildSearchHref = (baseHref: string, query: string) =>
  `${baseHref}?search=${encodeURIComponent(query)}`;

function SearchSection({
  group,
  query,
}: {
  group: AdminGlobalSearchGroup;
  query: string;
}) {
  const viewAllHref = buildSearchHref(GROUP_VIEW_ALL_HREF[group.type], query);

  return (
    <Card>
      <CardHeader className='flex flex-row items-center justify-between gap-3 pb-3'>
        <div>
          <CardTitle className='text-base'>{group.title}</CardTitle>
          <p className='text-sm text-muted-foreground'>
            {group.total} matching result{group.total === 1 ? '' : 's'}
          </p>
        </div>
        <Button asChild variant='ghost' size='sm' className='shrink-0 gap-2'>
          <Link href={viewAllHref}>
            View all
            <ArrowRight className='h-4 w-4' />
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        {group.items.length > 0 ? (
          <div className='space-y-2'>
            {group.items.map((item) => (
              <Link
                key={`${group.type}-${item.id}`}
                href={item.url}
                className='block rounded-md border px-3 py-2 transition-colors hover:bg-muted'
              >
                <p className='truncate text-sm font-medium'>{item.title}</p>
                {item.subtitle && (
                  <p className='mt-1 truncate text-xs text-muted-foreground'>
                    {item.subtitle}
                  </p>
                )}
              </Link>
            ))}
          </div>
        ) : (
          <p className='rounded-md border border-dashed px-3 py-6 text-center text-sm text-muted-foreground'>
            No {group.title.toLowerCase()} matched this search.
          </p>
        )}
      </CardContent>
    </Card>
  );
}

function LoadingState() {
  return (
    <div className='space-y-6'>
      <div className='space-y-2'>
        <Skeleton className='h-8 w-72' />
        <Skeleton className='h-4 w-96 max-w-full' />
      </div>
      <div className='grid gap-4 xl:grid-cols-2'>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className='h-48 rounded-lg' />
        ))}
      </div>
    </div>
  );
}

export default function AdminSearchPage() {
  const searchParams = useSearchParams();
  const query = (searchParams.get('q') || '').trim();

  const { data, isFetching, isLoading, isError, refetch } =
    useGetAdminGlobalSearchQuery(
      { q: query, limit: 5 },
      { skip: query.length === 0 }
    );

  if (!query) {
    return (
      <Card className='max-w-xl'>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <Search className='h-5 w-5 text-muted-foreground' />
            Search admin resources
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-sm text-muted-foreground'>
            Enter a query in the admin header to search organizations, users,
            roles, and modules.
          </p>
        </CardContent>
      </Card>
    );
  }

  if (isLoading || isFetching) {
    return <LoadingState />;
  }

  if (isError) {
    return (
      <Card className='max-w-xl'>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <AlertTriangle className='h-5 w-5 text-destructive' />
            Search unavailable
          </CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <p className='text-sm text-muted-foreground'>
            The account service did not return search results for "{query}".
          </p>
          <Button onClick={() => refetch()} className='gap-2'>
            <RefreshCw className='h-4 w-4' />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-2xl font-semibold tracking-tight'>
          Search results
        </h1>
        <p className='text-sm text-muted-foreground'>
          Results for "{data?.query ?? query}" across admin account resources.
        </p>
      </div>

      <div className='grid gap-4 xl:grid-cols-2'>
        {(data?.groups ?? []).map((group) => (
          <SearchSection key={group.type} group={group} query={query} />
        ))}
      </div>
    </div>
  );
}
