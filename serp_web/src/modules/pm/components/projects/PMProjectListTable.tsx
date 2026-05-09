/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list table
 */

'use client';

import {
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
  totalCount: number;
  onOpen: (projectId: string) => void;
  onEdit: (projectId: string) => void;
}

export function PMProjectListTable({
  projects,
  totalCount,
  onOpen,
  onEdit,
}: PMProjectListTableProps) {
  return (
    <Card className='overflow-hidden rounded-2xl shadow-sm'>
      <CardContent className='p-0'>
        <Table>
          <TableHeader className='bg-muted/40'>
            <TableRow className='hover:bg-muted/40'>
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
                Template
              </TableHead>
              <TableHead className='hidden h-12 px-4 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground xl:table-cell'>
                Category & Status
              </TableHead>
              <TableHead className='h-12 px-4 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground'>
                Activity
              </TableHead>
              <TableHead className='hidden h-12 px-4 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground lg:table-cell'>
                Updated
              </TableHead>
              <TableHead className='h-12 px-6 text-right text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground'>
                Actions
              </TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {projects.length > 0 ? (
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
                <TableCell colSpan={8} className='px-6 py-16 text-center'>
                  <div className='space-y-2'>
                    <p className='text-base font-semibold text-foreground'>
                      No projects match the current filters
                    </p>
                    <p className='text-sm text-muted-foreground'>
                      Try adjusting your search, status, or template filters.
                    </p>
                  </div>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>

        <div className='border-t bg-background px-6 py-4 text-sm text-muted-foreground'>
          {projects.length === 0
            ? `No results from ${totalCount} software projects`
            : `Showing ${projects.length} of ${totalCount} software projects`}
        </div>
      </CardContent>
    </Card>
  );
}
