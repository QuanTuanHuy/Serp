/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list table
 */

'use client';

import { ChevronLeft, ChevronRight } from 'lucide-react';
import {
  Button,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import type { PMProjectListItem } from '../../types/project-list.types';
import { PMProjectListRow } from './PMProjectListRow';

interface PMProjectListTableProps {
  projects: PMProjectListItem[];
  isLoading: boolean;
  currentPage: number;
  pageSize: number;
  totalCount: number;
  totalFilteredCount: number;
  totalPages: number;
  emptyTitle?: string;
  emptyDescription?: string;
  onPageChange: (page: number) => void;
  onOpen: (projectId: string) => void;
  onEdit: (projectId: string) => void;
}

export function PMProjectListTable({
  projects,
  isLoading,
  currentPage,
  pageSize,
  totalCount,
  totalFilteredCount,
  totalPages,
  emptyTitle = 'No projects match the current filters',
  emptyDescription = 'Try adjusting your search, status, or category filters.',
  onPageChange,
  onOpen,
  onEdit,
}: PMProjectListTableProps) {
  const rangeStart =
    projects.length === 0 ? 0 : (currentPage - 1) * pageSize + 1;
  const rangeEnd = projects.length === 0 ? 0 : rangeStart + projects.length - 1;

  return (
    <Card className='overflow-hidden rounded-2xl border border-border/80 bg-card/40 backdrop-blur-md shadow-sm'>
      <CardContent className='p-0'>
        <Table>
          <TableHeader className='bg-muted/30 border-b border-border/60'>
            <TableRow className='hover:bg-transparent border-b border-border/60'>
              <TableHead className='h-12 px-6 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground'>
                Project Name
              </TableHead>
              <TableHead className='hidden h-12 px-4 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground md:table-cell'>
                Key
              </TableHead>
              <TableHead className='hidden h-12 px-4 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground lg:table-cell'>
                Lead
              </TableHead>
              <TableHead className='hidden h-12 px-4 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground xl:table-cell'>
                Category & Status
              </TableHead>
              <TableHead className='h-12 px-4 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground'>
                Updated
              </TableHead>
              <TableHead className='h-12 px-6 text-right text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground'>
                Actions
              </TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className='px-6 py-16 text-center'>
                  <div className='space-y-2'>
                    <p className='text-base font-semibold text-foreground'>
                      Loading projects...
                    </p>
                    <p className='text-sm text-muted-foreground'>
                      Fetching the latest project list from PM Core.
                    </p>
                  </div>
                </TableCell>
              </TableRow>
            ) : projects.length > 0 ? (
              projects.map((project) => (
                <PMProjectListRow
                  key={project.id}
                  project={project}
                  onOpen={onOpen}
                  onEdit={onEdit}
                />
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className='px-6 py-16 text-center'>
                  <div className='space-y-2'>
                    <p className='text-base font-semibold text-foreground'>
                      {emptyTitle}
                    </p>
                    <p className='text-sm text-muted-foreground'>
                      {emptyDescription}
                    </p>
                  </div>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>

        <div className='flex flex-col gap-3 border-t border-border/60 bg-muted/20 px-6 py-4 text-sm text-muted-foreground lg:flex-row lg:items-center lg:justify-between'>
          <div>
            {isLoading
              ? 'Loading project results'
              : projects.length === 0
                ? `No results from ${totalCount} software projects`
                : `Showing ${rangeStart}-${rangeEnd} of ${totalFilteredCount} matching projects`}
          </div>

          <div className='flex items-center justify-between gap-3 lg:justify-end'>
            <span className='text-xs uppercase tracking-[0.18em] text-muted-foreground'>
              Page {currentPage} of {Math.max(totalPages, 1)}
            </span>
            <div className='flex items-center gap-2'>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage <= 1}
                className='h-8 bg-background/50 border-border/60 hover:bg-muted/70 text-xs font-semibold rounded-lg'
              >
                <ChevronLeft className='mr-1 h-3.5 w-3.5' />
                Previous
              </Button>
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage >= totalPages}
                className='h-8 bg-background/50 border-border/60 hover:bg-muted/70 text-xs font-semibold rounded-lg'
              >
                Next
                <ChevronRight className='ml-1 h-3.5 w-3.5' />
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
