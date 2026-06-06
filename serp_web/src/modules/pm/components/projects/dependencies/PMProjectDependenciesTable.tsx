/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM dependency table
 */

'use client';

import { ExternalLink } from 'lucide-react';
import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { formatDate } from '../../work-items/workItemView.utils';
import type {
  PMWorkItemDependencyEdgeApi,
  PMWorkItemDependencyNodeApi,
} from '../../../types/api';
import { getEdgeLabel } from './pmProjectDependencies.utils';

interface PMProjectDependenciesTableProps {
  nodes: PMWorkItemDependencyNodeApi[];
  edges: PMWorkItemDependencyEdgeApi[];
  selectedIds: number[];
  onToggleSelectWorkItem: (workItemId: number) => void;
  onOpenWorkItem: (workItemId: number) => void;
}

export function PMProjectDependenciesTable({
  nodes,
  edges,
  selectedIds,
  onToggleSelectWorkItem,
  onOpenWorkItem,
}: PMProjectDependenciesTableProps) {
  const nodeMap = new Map(nodes.map((node) => [node.id, node]));

  return (
    <Card className='shadow-sm'>
      <CardHeader>
        <CardTitle className='text-base'>Edges</CardTitle>
      </CardHeader>
      <CardContent>
        <div className='overflow-x-auto'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Predecessor</TableHead>
                <TableHead>Relation</TableHead>
                <TableHead>Successor</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Assignee</TableHead>
                <TableHead>Dates</TableHead>
                <TableHead className='text-right'>Flags</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {edges.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    className='py-10 text-center text-sm text-muted-foreground'
                  >
                    No dependency edges match the current filters.
                  </TableCell>
                </TableRow>
              ) : null}
              {edges.map((edge) => {
                const predecessor = nodeMap.get(edge.predecessorId);
                const successor = nodeMap.get(edge.successorId);
                return (
                  <TableRow key={edge.linkId}>
                    <TableCell>
                      <div className='flex items-start gap-2'>
                        <Checkbox
                          checked={selectedIds.includes(edge.predecessorId)}
                          onCheckedChange={() =>
                            onToggleSelectWorkItem(edge.predecessorId)
                          }
                        />
                        <button
                          type='button'
                          className='flex items-center gap-2 font-medium hover:underline'
                          onClick={() => onOpenWorkItem(edge.predecessorId)}
                        >
                          <span>{predecessor?.key ?? edge.predecessorId}</span>
                          <ExternalLink className='h-3.5 w-3.5 text-muted-foreground' />
                        </button>
                      </div>
                      <p className='text-xs text-muted-foreground'>
                        {predecessor?.summary}
                      </p>
                    </TableCell>
                    <TableCell>{getEdgeLabel(edge)}</TableCell>
                    <TableCell>
                      <div className='flex items-start gap-2'>
                        <Checkbox
                          checked={selectedIds.includes(edge.successorId)}
                          onCheckedChange={() =>
                            onToggleSelectWorkItem(edge.successorId)
                          }
                        />
                        <button
                          type='button'
                          className='flex items-center gap-2 font-medium hover:underline'
                          onClick={() => onOpenWorkItem(edge.successorId)}
                        >
                          <span>{successor?.key ?? edge.successorId}</span>
                          <ExternalLink className='h-3.5 w-3.5 text-muted-foreground' />
                        </button>
                      </div>
                      <p className='text-xs text-muted-foreground'>
                        {successor?.summary}
                      </p>
                    </TableCell>
                    <TableCell>{successor?.statusName ?? '-'}</TableCell>
                    <TableCell>{successor?.assigneeName ?? '-'}</TableCell>
                    <TableCell>
                      <div className='space-y-1 text-xs text-muted-foreground'>
                        <p>
                          Due: {formatDate(successor?.dueDate, 'No due date')}
                        </p>
                        <p>
                          Plan: {formatDate(successor?.plannedStart, 'No plan')}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex flex-wrap justify-end gap-1'>
                        {edge.cycle ? (
                          <Badge variant='destructive'>Cycle</Badge>
                        ) : null}
                        {edge.outsideFilter ? (
                          <Badge variant='secondary'>Outside</Badge>
                        ) : null}
                        {edge.externalProject ? (
                          <Badge variant='outline'>External</Badge>
                        ) : null}
                        {edge.relatedLink ? (
                          <Badge variant='secondary'>Related</Badge>
                        ) : null}
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
}
