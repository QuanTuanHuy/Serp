'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User Detail dialog (read-only view with actions)
 */

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import { Separator } from '@/shared/components/ui/separator';
import { Skeleton } from '@/shared/components/ui/skeleton';
import {
  Mail,
  Phone,
  Globe,
  Calendar,
  Shield,
  Building2,
  Puzzle,
  Clock,
} from 'lucide-react';
import { SettingsStatusBadge } from '../shared/SettingsStatusBadge';
import { useGetUserDetailQuery } from '../../services/users/usersApi';
import { getInitials } from '@/shared/utils';

export interface UserDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  userId: number | null;
  organizationId: number | undefined;
}

const formatDate = (isoDate?: string) => {
  if (!isoDate) return 'N/A';
  return new Date(isoDate).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const InfoRow = ({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ElementType;
  label: string;
  value?: string | null;
}) => (
  <div className='flex items-center gap-3 py-1.5'>
    <Icon className='h-4 w-4 text-muted-foreground flex-shrink-0' />
    <span className='text-sm text-muted-foreground w-32 flex-shrink-0'>
      {label}
    </span>
    <span className='text-sm'>{value || 'N/A'}</span>
  </div>
);

export function UserDetailDialog({
  open,
  onOpenChange,
  userId,
  organizationId,
}: UserDetailDialogProps) {
  const { data: detailResponse, isLoading } = useGetUserDetailQuery(
    { organizationId: organizationId as number, userId: userId as number },
    { skip: !open || !userId || !organizationId }
  );

  const user = detailResponse?.data;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl max-h-[85vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>User Details</DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className='space-y-4'>
            <div className='flex items-center gap-4'>
              <Skeleton className='h-16 w-16 rounded-full' />
              <div className='space-y-2'>
                <Skeleton className='h-5 w-40' />
                <Skeleton className='h-4 w-56' />
              </div>
            </div>
            <Skeleton className='h-32 w-full' />
            <Skeleton className='h-24 w-full' />
          </div>
        ) : user ? (
          <div className='space-y-6'>
            {/* Header */}
            <div className='flex items-center gap-4'>
              <Avatar className='h-16 w-16'>
                {user.avatarUrl && (
                  <AvatarImage
                    src={user.avatarUrl}
                    alt={`${user.firstName} ${user.lastName}`}
                  />
                )}
                <AvatarFallback className='bg-primary/10 text-lg'>
                  {getInitials({
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                  })}
                </AvatarFallback>
              </Avatar>
              <div>
                <h3 className='text-lg font-semibold'>
                  {user.firstName} {user.lastName}
                </h3>
                <p className='text-sm text-muted-foreground'>{user.email}</p>
                <div className='flex items-center gap-2 mt-1'>
                  <SettingsStatusBadge status={user.status} />
                  <Badge variant='outline'>{user.userType}</Badge>
                </div>
              </div>
            </div>

            <Separator />

            {/* Basic Information */}
            <div>
              <h4 className='text-sm font-semibold mb-2'>Information</h4>
              <div className='space-y-0.5'>
                <InfoRow icon={Mail} label='Email' value={user.email} />
                <InfoRow icon={Phone} label='Phone' value={user.phoneNumber} />
                <InfoRow icon={Globe} label='Timezone' value={user.timezone} />
                <InfoRow
                  icon={Globe}
                  label='Language'
                  value={user.preferredLanguage}
                />
                <InfoRow
                  icon={Building2}
                  label='Organization'
                  value={user.organizationName}
                />
                <InfoRow
                  icon={Clock}
                  label='Last Login'
                  value={formatDate(user.lastLoginAt)}
                />
                <InfoRow
                  icon={Calendar}
                  label='Created'
                  value={formatDate(user.createdAt)}
                />
              </div>
            </div>

            {/* Roles */}
            {user.roles && user.roles.length > 0 && (
              <>
                <Separator />
                <div>
                  <h4 className='text-sm font-semibold mb-2 flex items-center gap-2'>
                    <Shield className='h-4 w-4' />
                    Roles ({user.roles.length})
                  </h4>
                  <div className='flex flex-wrap gap-2'>
                    {user.roles.map((role) => (
                      <Badge key={role.id} variant='secondary'>
                        {role.name}
                        {role.scope && (
                          <span className='ml-1 text-muted-foreground'>
                            ({role.scope})
                          </span>
                        )}
                      </Badge>
                    ))}
                  </div>
                </div>
              </>
            )}

            {/* Departments */}
            {user.departments && user.departments.length > 0 && (
              <>
                <Separator />
                <div>
                  <h4 className='text-sm font-semibold mb-2 flex items-center gap-2'>
                    <Building2 className='h-4 w-4' />
                    Departments ({user.departments.length})
                  </h4>
                  <div className='flex flex-wrap gap-2'>
                    {user.departments.map((dept) => (
                      <Badge
                        key={dept.id}
                        variant={dept.isPrimary ? 'default' : 'outline'}
                      >
                        {dept.name}
                        {dept.jobTitle && (
                          <span className='ml-1 text-muted-foreground'>
                            - {dept.jobTitle}
                          </span>
                        )}
                        {dept.isPrimary && (
                          <span className='ml-1 text-xs'>(Primary)</span>
                        )}
                      </Badge>
                    ))}
                  </div>
                </div>
              </>
            )}

            {/* Module Access */}
            {user.moduleAccesses && user.moduleAccesses.length > 0 && (
              <>
                <Separator />
                <div>
                  <h4 className='text-sm font-semibold mb-2 flex items-center gap-2'>
                    <Puzzle className='h-4 w-4' />
                    Module Access ({user.moduleAccesses.length})
                  </h4>
                  <div className='flex flex-wrap gap-2'>
                    {user.moduleAccesses.map((mod) => (
                      <Badge
                        key={mod.moduleId}
                        variant={mod.isActive ? 'secondary' : 'outline'}
                      >
                        {mod.moduleName || mod.moduleCode}
                        {!mod.isActive && (
                          <span className='ml-1 text-destructive'>
                            (Inactive)
                          </span>
                        )}
                      </Badge>
                    ))}
                  </div>
                </div>
              </>
            )}
          </div>
        ) : (
          <div className='text-center text-muted-foreground py-8'>
            User not found
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

export default UserDetailDialog;
