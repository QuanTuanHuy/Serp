/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS order product type list page
 */

'use client';

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
} from '@/shared/components/ui';
import { Loader2, RefreshCw, Search } from 'lucide-react';
import { useGetProductTypesQuery } from '../../api';

const PAGE_SIZE = 20;

export const ProductTypeListPage: React.FC = () => {
  const [page, setPage] = React.useState(0);
  const [keywordInput, setKeywordInput] = React.useState('');
  const [keyword, setKeyword] = React.useState<string | undefined>(undefined);

  const { data, isLoading, isFetching, refetch } = useGetProductTypesQuery({
    page,
    size: PAGE_SIZE,
    keyword,
  });

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim() || undefined);
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Product Types</h1>
        <p className='text-muted-foreground'>
          Configure product classification used in TMS order imports.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Search</CardTitle>
          <CardDescription>
            Filter product types by code or name.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSearch} className='flex gap-2'>
            <div className='relative flex-1'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                className='pl-10'
                value={keywordInput}
                onChange={(event) => setKeywordInput(event.target.value)}
                placeholder='Search product type...'
              />
            </div>
            <Button type='submit'>Apply</Button>
            <Button
              type='button'
              variant='outline'
              onClick={() => refetch()}
              disabled={isFetching}
            >
              <RefreshCw className='h-4 w-4 mr-2' />
              Refresh
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className='flex items-center gap-2 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Loading product types...
            </div>
          ) : data && data.items.length > 0 ? (
            <div className='space-y-3'>
              {data.items.map((item) => (
                <div
                  key={item.id}
                  className='rounded-lg border p-3 flex items-start justify-between'
                >
                  <div>
                    <p className='font-medium'>
                      {item.code} - {item.name}
                    </p>
                    <p className='text-xs text-muted-foreground mt-1'>
                      Active: {item.isActive ? 'Yes' : 'No'}
                    </p>
                  </div>
                </div>
              ))}

              <div className='flex items-center justify-between pt-2'>
                <Button
                  variant='outline'
                  onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                  disabled={!data.hasPrevious || isFetching}
                >
                  Previous
                </Button>
                <span className='text-sm text-muted-foreground'>
                  Page {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
                </span>
                <Button
                  variant='outline'
                  onClick={() => setPage((prev) => prev + 1)}
                  disabled={!data.hasNext || isFetching}
                >
                  Next
                </Button>
              </div>
            </div>
          ) : (
            <p className='text-muted-foreground'>No product types found.</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
