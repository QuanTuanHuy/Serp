/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Display Details View Dialog
 */

'use client';

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Badge } from '@/shared/components/ui/badge';
import { Separator } from '@/shared/components/ui/separator';
import {
  Menu,
  FolderTree,
  Hash,
  Eye,
  EyeOff,
  Calendar,
  Shield,
} from 'lucide-react';
import type { MenuDisplayDetail } from '../../types';

interface MenuDisplayDetailsDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  menuDisplay: MenuDisplayDetail | null;
}

export const MenuDisplayDetailsDialog: React.FC<
  MenuDisplayDetailsDialogProps
> = ({ open, onOpenChange, menuDisplay }) => {
  if (!menuDisplay) return null;

  const formatDate = (timestamp: string | null | undefined) => {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-2xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle className='flex items-center gap-2'>
            <Menu className='h-5 w-5' />
            Menu Display Details
          </DialogTitle>
        </DialogHeader>

        <div className='space-y-6'>
          {/* Basic Information */}
          <div>
            <h3 className='text-sm font-semibold mb-3 flex items-center gap-2'>
              <Hash className='h-4 w-4' />
              Basic Information
            </h3>
            <div className='space-y-3'>
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>Name:</span>
                <span className='col-span-2 text-sm font-medium'>
                  {menuDisplay.name}
                </span>
              </div>
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>Path:</span>
                <span className='col-span-2 text-sm font-mono bg-muted px-2 py-1 rounded'>
                  {menuDisplay.path}
                </span>
              </div>
              {menuDisplay.icon && (
                <div className='grid grid-cols-3 gap-2'>
                  <span className='text-sm text-muted-foreground'>Icon:</span>
                  <span className='col-span-2 text-sm font-mono bg-muted px-2 py-1 rounded'>
                    {menuDisplay.icon}
                  </span>
                </div>
              )}
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>
                  Display Order:
                </span>
                <span className='col-span-2 text-sm'>{menuDisplay.order}</span>
              </div>
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>
                  Visibility:
                </span>
                <span className='col-span-2'>
                  <Badge
                    variant={menuDisplay.isVisible ? 'default' : 'secondary'}
                    className='gap-1'
                  >
                    {menuDisplay.isVisible ? (
                      <Eye className='h-3 w-3' />
                    ) : (
                      <EyeOff className='h-3 w-3' />
                    )}
                    {menuDisplay.isVisible ? 'Visible' : 'Hidden'}
                  </Badge>
                </span>
              </div>
            </div>
          </div>

          <Separator />

          {/* Hierarchy Information */}
          <div>
            <h3 className='text-sm font-semibold mb-3 flex items-center gap-2'>
              <FolderTree className='h-4 w-4' />
              Hierarchy
            </h3>
            <div className='space-y-3'>
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>Module:</span>
                <span className='col-span-2'>
                  <Badge variant='outline'>
                    {menuDisplay.moduleCode ||
                      menuDisplay.moduleName ||
                      `Module #${menuDisplay.moduleId}`}
                  </Badge>
                </span>
              </div>
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>Parent:</span>
                <span className='col-span-2 text-sm'>
                  {menuDisplay.parentId ? (
                    <span className='text-muted-foreground'>
                      Parent ID: {menuDisplay.parentId}
                    </span>
                  ) : (
                    <span className='text-muted-foreground'>
                      Root level (no parent)
                    </span>
                  )}
                </span>
              </div>
            </div>
          </div>

          <Separator />

          {/* Assigned Roles */}
          <div>
            <h3 className='text-sm font-semibold mb-3 flex items-center gap-2'>
              <Shield className='h-4 w-4' />
              Assigned Roles ({menuDisplay.assignedRoles?.length || 0})
            </h3>
            {menuDisplay.assignedRoles &&
            menuDisplay.assignedRoles.length > 0 ? (
              <div className='flex flex-wrap gap-2'>
                {menuDisplay.assignedRoles.map((roleAssignment) => (
                  <Badge key={roleAssignment.roleId} variant='secondary'>
                    {roleAssignment.roleName ||
                      `Role #${roleAssignment.roleId}`}
                  </Badge>
                ))}
              </div>
            ) : (
              <p className='text-sm text-muted-foreground'>
                No roles assigned. This menu may not be visible to any users.
              </p>
            )}
          </div>

          <Separator />

          {/* Timestamps */}
          <div>
            <h3 className='text-sm font-semibold mb-3 flex items-center gap-2'>
              <Calendar className='h-4 w-4' />
              Timestamps
            </h3>
            <div className='space-y-3'>
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>Created:</span>
                <span className='col-span-2 text-sm'>
                  {formatDate(menuDisplay.createdAt)}
                </span>
              </div>
              <div className='grid grid-cols-3 gap-2'>
                <span className='text-sm text-muted-foreground'>
                  Last Updated:
                </span>
                <span className='col-span-2 text-sm'>
                  {formatDate(menuDisplay.updatedAt)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
