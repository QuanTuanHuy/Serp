/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Display Tree component
 */

'use client';

import React from 'react';
import * as LucideIcons from 'lucide-react';
import {
  ChevronRight,
  ChevronDown,
  MoreVertical,
  Eye,
  Edit,
  Trash2,
} from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui/alert-dialog';
import { cn } from '@/shared/utils';
import type { MenuDisplayTreeNode } from '../../types';

interface MenuDisplayTreeProps {
  nodes: MenuDisplayTreeNode[];
  expandedNodes: Set<number>;
  onToggle: (nodeId: number) => void;
  onEdit: (node: MenuDisplayTreeNode) => void;
  onDelete: (id: number, name: string) => void;
  level?: number;
}

const MenuTreeNode: React.FC<{
  node: MenuDisplayTreeNode;
  isExpanded: boolean;
  expandedNodes: Set<number>;
  onToggle: (nodeId: number) => void;
  onEdit: (node: MenuDisplayTreeNode) => void;
  onDelete: (id: number, name: string) => void;
  level: number;
}> = ({
  node,
  isExpanded,
  expandedNodes,
  onToggle,
  onEdit,
  onDelete,
  level,
}) => {
  const [showDeleteDialog, setShowDeleteDialog] = React.useState(false);
  const hasChildren = node.children && node.children.length > 0;

  // Get icon component
  const getIconComponent = (iconName?: string) => {
    if (!iconName) return null;
    const IconComponent = (LucideIcons as any)[iconName];
    return IconComponent || null;
  };

  const IconComponent = getIconComponent(node.icon);

  const handleDelete = () => {
    if (node.id) {
      onDelete(node.id, node.name);
      setShowDeleteDialog(false);
    }
  };

  return (
    <>
      <div
        className={cn(
          'group hover:bg-accent/50 rounded-md transition-colors',
          level === 0 && 'border-b'
        )}
      >
        <div
          className='flex items-center gap-2 py-2.5 px-3'
          style={{ paddingLeft: `${level * 1.5 + 0.75}rem` }}
        >
          {/* Expand/Collapse Button */}
          <button
            onClick={() => node.id && onToggle(node.id)}
            className={cn(
              'flex-shrink-0 h-5 w-5 flex items-center justify-center rounded hover:bg-accent transition-colors',
              !hasChildren && 'invisible'
            )}
          >
            {hasChildren &&
              (isExpanded ? (
                <ChevronDown className='h-4 w-4' />
              ) : (
                <ChevronRight className='h-4 w-4' />
              ))}
          </button>

          {/* Icon */}
          <div className='flex-shrink-0 h-8 w-8 flex items-center justify-center rounded-md bg-primary/10'>
            {IconComponent ? (
              <IconComponent className='h-4 w-4 text-primary' />
            ) : (
              <LucideIcons.Menu className='h-4 w-4 text-primary' />
            )}
          </div>

          {/* Menu Info */}
          <div className='flex-1 min-w-0'>
            <div className='flex items-center gap-2'>
              <span className='font-medium truncate'>{node.name}</span>
              {!node.isVisible && (
                <Badge variant='outline' className='text-xs'>
                  <Eye className='h-3 w-3 mr-1' />
                  Hidden
                </Badge>
              )}
              {hasChildren && (
                <Badge variant='secondary' className='text-xs'>
                  {node.children!.length}
                </Badge>
              )}
            </div>
            <div className='flex items-center gap-2 text-xs text-muted-foreground mt-0.5'>
              <code className='bg-muted px-1.5 py-0.5 rounded'>
                {node.path}
              </code>
              <span>•</span>
              <span>Order: {node.order}</span>
              {node.assignedRoles && node.assignedRoles.length > 0 && (
                <>
                  <span>•</span>
                  <span>
                    {node.assignedRoles.length} role
                    {node.assignedRoles.length !== 1 ? 's' : ''}
                  </span>
                </>
              )}
            </div>
          </div>

          {/* Module Badge */}
          {node.moduleName && (
            <Badge
              variant='outline'
              className='flex-shrink-0 text-xs hidden md:flex'
            >
              {node.moduleName}
            </Badge>
          )}

          {/* Actions */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant='ghost'
                size='icon'
                className='h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity'
              >
                <MoreVertical className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem
                onClick={() => console.log('View details', node)}
              >
                <Eye className='mr-2 h-4 w-4' />
                View Details
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onEdit(node)}>
                <Edit className='mr-2 h-4 w-4' />
                Edit
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => setShowDeleteDialog(true)}
                className='text-destructive focus:text-destructive'
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Children */}
      {hasChildren && isExpanded && (
        <div className='pl-0'>
          <MenuDisplayTree
            nodes={node.children!}
            expandedNodes={expandedNodes}
            onToggle={onToggle}
            onEdit={onEdit}
            onDelete={onDelete}
            level={level + 1}
          />
        </div>
      )}

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Menu Display</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &ldquo;{node.name}&rdquo;?
              {hasChildren && (
                <span className='text-destructive font-medium block mt-2'>
                  Warning: This menu has {node.children!.length} child menu
                  {node.children!.length !== 1 ? 's' : ''} that may be affected.
                </span>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className='bg-destructive hover:bg-destructive/90'
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
};

export const MenuDisplayTree: React.FC<MenuDisplayTreeProps> = ({
  nodes,
  expandedNodes,
  onToggle,
  onEdit,
  onDelete,
  level = 0,
}) => {
  if (nodes.length === 0) {
    return null;
  }

  return (
    <div className='space-y-0.5'>
      {nodes.map((node) => (
        <MenuTreeNode
          key={node.id}
          node={node}
          isExpanded={node.id ? expandedNodes.has(node.id) : false}
          expandedNodes={expandedNodes}
          onToggle={onToggle}
          onEdit={onEdit}
          onDelete={onDelete}
          level={level}
        />
      ))}
    </div>
  );
};
