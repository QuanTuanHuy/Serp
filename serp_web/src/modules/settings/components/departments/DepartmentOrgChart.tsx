/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Department organization chart tree view
 */

'use client';

import React, { useState } from 'react';
import {
  ChevronRight,
  ChevronDown,
  Users,
  Building2,
  Edit,
  Plus,
  Trash2,
  Eye,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { SettingsActionMenu } from '../shared/SettingsActionMenu';
import type { DepartmentTreeNode } from '../../types/department.types';

interface DepartmentOrgChartProps {
  tree: DepartmentTreeNode[];
  isLoading?: boolean;
  onView?: (node: DepartmentTreeNode) => void;
  onEdit?: (node: DepartmentTreeNode) => void;
  onAddChild?: (parentNode: DepartmentTreeNode) => void;
  onDelete?: (node: DepartmentTreeNode) => void;
}

interface TreeNodeProps {
  node: DepartmentTreeNode;
  depth: number;
  onView?: (node: DepartmentTreeNode) => void;
  onEdit?: (node: DepartmentTreeNode) => void;
  onAddChild?: (parentNode: DepartmentTreeNode) => void;
  onDelete?: (node: DepartmentTreeNode) => void;
}

function TreeNode({
  node,
  depth,
  onView,
  onEdit,
  onAddChild,
  onDelete,
}: TreeNodeProps) {
  const [expanded, setExpanded] = useState(depth < 2);
  const hasChildren = node.children && node.children.length > 0;

  return (
    <div>
      <div
        className={cn(
          'group flex items-center gap-2 rounded-lg px-3 py-2.5 transition-colors hover:bg-muted/50',
          depth === 0 && 'bg-muted/30'
        )}
        style={{ paddingLeft: `${depth * 24 + 12}px` }}
      >
        {/* Expand/Collapse */}
        <Button
          variant='ghost'
          size='icon'
          className={cn('h-6 w-6 flex-shrink-0', !hasChildren && 'invisible')}
          onClick={() => setExpanded(!expanded)}
        >
          {expanded ? (
            <ChevronDown className='h-4 w-4' />
          ) : (
            <ChevronRight className='h-4 w-4' />
          )}
        </Button>

        {/* Department Icon */}
        <div
          className={cn(
            'h-9 w-9 rounded-lg flex items-center justify-center flex-shrink-0',
            node.isActive ? 'bg-purple-100 dark:bg-purple-900/50' : 'bg-muted'
          )}
        >
          <Building2
            className={cn(
              'h-4 w-4',
              node.isActive
                ? 'text-purple-600 dark:text-purple-400'
                : 'text-muted-foreground'
            )}
          />
        </div>

        {/* Department Info */}
        <div
          className='flex-1 min-w-0 cursor-pointer'
          onClick={() => onView?.(node)}
        >
          <div className='flex items-center gap-2'>
            <span className='font-medium text-sm truncate'>{node.name}</span>
            <Badge
              variant='outline'
              className='text-[10px] px-1.5 py-0 font-mono'
            >
              {node.code}
            </Badge>
            {!node.isActive && (
              <Badge variant='destructive' className='text-[10px] px-1.5 py-0'>
                Inactive
              </Badge>
            )}
          </div>
          <div className='flex items-center gap-3 text-xs text-muted-foreground mt-0.5'>
            {node.managerName && (
              <div className='flex items-center gap-1'>
                <Avatar className='h-4 w-4'>
                  <AvatarFallback className='text-[8px] bg-purple-100 text-purple-700'>
                    {node.managerName
                      .split(' ')
                      .map((n) => n[0])
                      .join('')}
                  </AvatarFallback>
                </Avatar>
                <span className='truncate'>{node.managerName}</span>
              </div>
            )}
            <div className='flex items-center gap-1'>
              <Users className='h-3 w-3' />
              <span>
                {node.memberCount}{' '}
                {node.memberCount === 1 ? 'member' : 'members'}
              </span>
            </div>
            {hasChildren && (
              <span className='text-muted-foreground/60'>
                {node.children.length} sub-dept
                {node.children.length !== 1 ? 's' : ''}
              </span>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className='opacity-0 group-hover:opacity-100 transition-opacity'>
          <SettingsActionMenu
            items={[
              {
                label: 'View Details',
                onClick: () => onView?.(node),
                icon: <Eye className='h-4 w-4' />,
              },
              {
                label: 'Edit',
                onClick: () => onEdit?.(node),
                icon: <Edit className='h-4 w-4' />,
              },
              {
                label: 'Add Sub-department',
                onClick: () => onAddChild?.(node),
                icon: <Plus className='h-4 w-4' />,
                separator: true,
              },
              {
                label: 'Delete',
                onClick: () => onDelete?.(node),
                icon: <Trash2 className='h-4 w-4' />,
                variant: 'destructive',
                separator: true,
              },
            ]}
          />
        </div>
      </div>

      {/* Children */}
      {hasChildren && expanded && (
        <div className='relative'>
          <div
            className='absolute top-0 bottom-0 border-l border-border/50'
            style={{ left: `${depth * 24 + 24}px` }}
          />
          {node.children.map((child) => (
            <TreeNode
              key={child.id}
              node={child}
              depth={depth + 1}
              onView={onView}
              onEdit={onEdit}
              onAddChild={onAddChild}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export function DepartmentOrgChart({
  tree,
  isLoading,
  onView,
  onEdit,
  onAddChild,
  onDelete,
}: DepartmentOrgChartProps) {
  if (isLoading) {
    return (
      <div className='space-y-3 p-4'>
        {[1, 2, 3].map((i) => (
          <div key={i} className='flex items-center gap-3 animate-pulse'>
            <div className='h-9 w-9 rounded-lg bg-muted' />
            <div className='flex-1 space-y-2'>
              <div className='h-4 w-48 rounded bg-muted' />
              <div className='h-3 w-32 rounded bg-muted' />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (!tree || tree.length === 0) {
    return (
      <div className='text-center py-12'>
        <Building2 className='h-12 w-12 mx-auto text-muted-foreground mb-4' />
        <h3 className='text-lg font-semibold mb-2'>No departments yet</h3>
        <p className='text-muted-foreground'>
          Create your first department to see the organization chart
        </p>
      </div>
    );
  }

  return (
    <div className='space-y-0.5'>
      {tree.map((node) => (
        <TreeNode
          key={node.id}
          node={node}
          depth={0}
          onView={onView}
          onEdit={onEdit}
          onAddChild={onAddChild}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
}
