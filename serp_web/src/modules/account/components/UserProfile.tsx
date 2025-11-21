/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Display user profile and logout functionality
 */

'use client';

import React from 'react';
import { useAuth } from '../hooks/useAuth';
import {
  Card,
  CardContent,
  Button,
  Avatar,
  AvatarImage,
  AvatarFallback,
  Badge,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components';
import { formatDateTime } from '@/shared';
import {
  Mail,
  Phone,
  Globe,
  Clock,
  Building,
  Shield,
  Calendar,
  LogOut,
} from 'lucide-react';
import { useUser } from '../hooks';

interface UserProfileProps {
  showCard?: boolean;
}

export const UserProfile: React.FC<UserProfileProps> = ({
  showCard = true,
}) => {
  const { logout, isLoading } = useAuth();
  const { user, getInitials } = useUser();

  if (!user) {
    return null;
  }

  const handleLogout = () => {
    logout();
  };

  const content = (
    <div className='space-y-8'>
      {/* Header Section with Gradient Background */}
      <div className='relative mb-16'>
        <div className='h-32 w-full rounded-t-lg bg-gradient-to-r from-blue-500 to-purple-600'></div>
        <div className='absolute -bottom-12 left-8 flex items-end space-x-4'>
          <Avatar className='h-24 w-24 border-4 border-background shadow-lg'>
            <AvatarImage src={user.avatarUrl} alt={user.fullName} />
            <AvatarFallback className='text-2xl font-bold bg-primary text-primary-foreground'>
              {getInitials()}
            </AvatarFallback>
          </Avatar>
          <div className='mb-2 space-y-1'>
            <h2 className='text-2xl font-bold text-foreground'>
              {user.fullName}
            </h2>
            <div className='flex items-center space-x-2'>
              <Badge variant={user.isActive ? 'default' : 'destructive'}>
                {user.isActive ? 'Active' : 'Inactive'}
              </Badge>
              {user.roles.slice(0, 2).map((role) => (
                <Badge key={role} variant='secondary'>
                  {role}
                </Badge>
              ))}
              {user.roles.length > 2 && (
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Badge
                        variant='outline'
                        className='cursor-help hover:bg-accent'
                      >
                        +{user.roles.length - 2} more
                      </Badge>
                    </TooltipTrigger>
                    <TooltipContent>
                      <div className='flex flex-col gap-1'>
                        {user.roles.slice(2).map((role) => (
                          <span key={role} className='text-xs'>
                            {role}
                          </span>
                        ))}
                      </div>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              )}
            </div>
          </div>
        </div>
        <div className='absolute -bottom-12 right-8 mb-2'>
          <Button
            variant='destructive'
            size='sm'
            onClick={handleLogout}
            disabled={isLoading}
          >
            <LogOut className='mr-2 h-4 w-4' />
            {isLoading ? 'Logging out...' : 'Logout'}
          </Button>
        </div>
      </div>

      {/* Content Grid */}
      <div className='grid grid-cols-1 md:grid-cols-2 gap-8 px-2'>
        {/* Contact Information */}
        <div className='space-y-4'>
          <h3 className='text-lg font-semibold border-b pb-2'>
            Contact Information
          </h3>
          <div className='space-y-3'>
            <div className='flex items-center text-sm'>
              <Mail className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Email:</span>
              <span className='font-medium'>{user.email}</span>
            </div>
            <div className='flex items-center text-sm'>
              <Phone className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Phone:</span>
              <span className='font-medium'>
                {user.phoneNumber || 'Not provided'}
              </span>
            </div>
          </div>
        </div>

        {/* Account Details */}
        <div className='space-y-4'>
          <h3 className='text-lg font-semibold border-b pb-2'>
            Account Details
          </h3>
          <div className='space-y-3'>
            <div className='flex items-center text-sm'>
              <Building className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Organization:</span>
              <span className='font-medium'>
                {user.organizationName || 'N/A'}
              </span>
            </div>
            <div className='flex items-center text-sm'>
              <Shield className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Org ID:</span>
              <span className='font-medium font-mono text-xs'>
                {user.organizationId || 'N/A'}
              </span>
            </div>
          </div>
        </div>

        {/* Preferences */}
        <div className='space-y-4'>
          <h3 className='text-lg font-semibold border-b pb-2'>Preferences</h3>
          <div className='space-y-3'>
            <div className='flex items-center text-sm'>
              <Globe className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Language:</span>
              <span className='font-medium'>
                {user.preferredLanguage || 'Default'}
              </span>
            </div>
            <div className='flex items-center text-sm'>
              <Clock className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Timezone:</span>
              <span className='font-medium'>{user.timezone || 'Default'}</span>
            </div>
          </div>
        </div>

        {/* System Info */}
        <div className='space-y-4'>
          <h3 className='text-lg font-semibold border-b pb-2'>
            System Information
          </h3>
          <div className='space-y-3'>
            <div className='flex items-center text-sm'>
              <Calendar className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Joined:</span>
              <span className='font-medium'>
                {formatDateTime(user.createdAt)}
              </span>
            </div>
            <div className='flex items-center text-sm'>
              <Clock className='mr-3 h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground w-24'>Last Updated:</span>
              <span className='font-medium'>
                {formatDateTime(user.updatedAt)}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  if (showCard) {
    return (
      <Card className='overflow-hidden'>
        <CardContent className='p-0 pb-8'>{content}</CardContent>
      </Card>
    );
  }

  return content;
};

/**
 * Compact user profile for header/navbar
 */
export const UserProfileCompact: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();

  if (!isAuthenticated || !user) {
    return null;
  }

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  };

  return (
    <div className='flex items-center space-x-3'>
      <div className='text-right hidden md:block'>
        <p className='text-sm font-medium leading-none'>{user.fullName}</p>
        <p className='text-xs text-muted-foreground mt-1'>
          {user.roles[0] || 'User'}
        </p>
      </div>
      <Avatar className='h-8 w-8 cursor-pointer' onClick={() => logout()}>
        <AvatarImage src={user.avatarUrl} alt={user.fullName} />
        <AvatarFallback className='text-xs bg-primary text-primary-foreground'>
          {getInitials(user.fullName)}
        </AvatarFallback>
      </Avatar>
    </div>
  );
};
