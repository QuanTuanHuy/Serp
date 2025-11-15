/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Department Detail Dialog Component
 */

'use client';

import React from 'react';
import {
  Users,
  Mail,
  Phone,
  Briefcase,
  X,
  UserPlus,
  Loader2,
} from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Separator } from '@/shared/components/ui/separator';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { isSuccessResponse } from '@/lib/store/api/utils';
import type { Department } from '../../types';

interface DepartmentDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  department: Department | null;
  useDepartmentMembers: (departmentId?: number) => any;
  onAddMembers?: (department: Department) => void;
}

export const DepartmentDetailDialog: React.FC<DepartmentDetailDialogProps> = ({
  open,
  onOpenChange,
  department,
  useDepartmentMembers,
  onAddMembers,
}) => {
  const { data: membersData, isLoading: membersLoading } = useDepartmentMembers(
    department?.id
  );

  const members =
    membersData && isSuccessResponse(membersData) ? membersData.data : [];

  if (!department) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[700px] max-h-[85vh] !max-w-4xl'>
        <DialogHeader>
          <div className='flex items-start justify-between'>
            <div className='flex-1'>
              <DialogTitle className='text-2xl'>{department.name}</DialogTitle>
              <DialogDescription className='mt-2'>
                {department.description || 'No description provided'}
              </DialogDescription>
            </div>
            <Badge variant={department.isActive ? 'default' : 'secondary'}>
              {department.isActive ? 'Active' : 'Inactive'}
            </Badge>
          </div>
        </DialogHeader>

        <div className='space-y-6'>
          {/* Department Info */}
          <div className='grid grid-cols-2 gap-4'>
            {department.code && (
              <div>
                <p className='text-sm text-muted-foreground'>Code</p>
                <p className='text-sm font-medium'>{department.code}</p>
              </div>
            )}
            {department.parentDepartmentName && (
              <div>
                <p className='text-sm text-muted-foreground'>
                  Parent Department
                </p>
                <p className='text-sm font-medium'>
                  {department.parentDepartmentName}
                </p>
              </div>
            )}
            {department.managerName && (
              <div>
                <p className='text-sm text-muted-foreground'>Manager</p>
                <p className='text-sm font-medium'>{department.managerName}</p>
              </div>
            )}
            <div>
              <p className='text-sm text-muted-foreground'>Total Members</p>
              <p className='text-sm font-medium'>
                {department.memberCount || 0}
              </p>
            </div>
          </div>

          <Separator />

          {/* Members List */}
          <div>
            <div className='flex items-center justify-between mb-4'>
              <h3 className='text-lg font-semibold flex items-center gap-2'>
                <Users className='h-5 w-5' />
                Team Members ({members.length})
              </h3>
              {onAddMembers && (
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => onAddMembers(department)}
                >
                  <UserPlus className='h-4 w-4 mr-2' />
                  Add Members
                </Button>
              )}
            </div>

            {membersLoading ? (
              <div className='flex items-center justify-center py-8'>
                <Loader2 className='h-6 w-6 animate-spin text-muted-foreground' />
              </div>
            ) : members.length === 0 ? (
              <div className='text-center py-8'>
                <Users className='h-12 w-12 mx-auto text-muted-foreground mb-3' />
                <p className='text-sm text-muted-foreground'>
                  No members in this department yet
                </p>
                {onAddMembers && (
                  <Button
                    variant='outline'
                    size='sm'
                    className='mt-4'
                    onClick={() => onAddMembers(department)}
                  >
                    <UserPlus className='h-4 w-4 mr-2' />
                    Add First Member
                  </Button>
                )}
              </div>
            ) : (
              <ScrollArea className='h-[300px] pr-4'>
                <div className='space-y-3'>
                  {members.map((member: any) => (
                    <div
                      key={member.userId}
                      className='flex items-start gap-3 p-3 rounded-lg border hover:bg-accent/50 transition-colors'
                    >
                      <Avatar className='h-10 w-10'>
                        <AvatarFallback className='bg-purple-100 text-purple-700'>
                          {member.userName
                            .split(' ')
                            .map((n: any) => n[0])
                            .join('')
                            .toUpperCase()}
                        </AvatarFallback>
                      </Avatar>
                      <div className='flex-1 min-w-0'>
                        <div className='flex items-center gap-2'>
                          <p className='text-sm font-medium truncate'>
                            {member.userName}
                          </p>
                          {member.isPrimary && (
                            <Badge
                              variant='secondary'
                              className='text-xs h-5 px-1.5'
                            >
                              Primary
                            </Badge>
                          )}
                          {!member.isActive && (
                            <Badge
                              variant='outline'
                              className='text-xs h-5 px-1.5'
                            >
                              Inactive
                            </Badge>
                          )}
                        </div>
                        {member.jobTitle && (
                          <div className='flex items-center gap-1.5 mt-1'>
                            <Briefcase className='h-3 w-3 text-muted-foreground' />
                            <p className='text-xs text-muted-foreground'>
                              {member.jobTitle}
                            </p>
                          </div>
                        )}
                        <div className='flex items-center gap-3 mt-1.5'>
                          <div className='flex items-center gap-1.5'>
                            <Mail className='h-3 w-3 text-muted-foreground' />
                            <p className='text-xs text-muted-foreground truncate'>
                              {member.email}
                            </p>
                          </div>
                          {member.phoneNumber && (
                            <div className='flex items-center gap-1.5'>
                              <Phone className='h-3 w-3 text-muted-foreground' />
                              <p className='text-xs text-muted-foreground'>
                                {member.phoneNumber}
                              </p>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
