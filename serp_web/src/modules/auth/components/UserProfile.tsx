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
  CardDescription,
  CardHeader,
  CardTitle,
  Button,
} from '@/shared/components';

interface UserProfileProps {
  showCard?: boolean;
}

export const UserProfile: React.FC<UserProfileProps> = ({
  showCard = true,
}) => {
  const { user, logout, isAuthenticated, isLoading } = useAuth();

  if (!isAuthenticated || !user) {
    return null;
  }

  const handleLogout = () => {
    logout();
  };

  const content = (
    <div className='space-y-4'>
      <div className='space-y-2'>
        <div className='flex justify-between items-start'>
          <div className='space-y-1'>
            <h3 className='font-semibold'>{user.fullName}</h3>
            <p className='text-sm text-muted-foreground'>{user.email}</p>
            <p className='text-xs text-muted-foreground'>Role: {user.role}</p>
            {user.organizationId && (
              <p className='text-xs text-muted-foreground'>
                Org ID: {user.organizationId}
              </p>
            )}
          </div>
          <div className='flex items-center space-x-1'>
            <div
              className={`w-2 h-2 rounded-full ${
                user.isActive ? 'bg-green-500' : 'bg-red-500'
              }`}
            />
            <span className='text-xs text-muted-foreground'>
              {user.isActive ? 'Active' : 'Inactive'}
            </span>
          </div>
        </div>
      </div>

      <div className='flex gap-2'>
        <Button
          variant='outline'
          size='sm'
          onClick={handleLogout}
          disabled={isLoading}
        >
          {isLoading ? 'Logging out...' : 'Logout'}
        </Button>
      </div>
    </div>
  );

  if (showCard) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>User Profile</CardTitle>
          <CardDescription>Your account information</CardDescription>
        </CardHeader>
        <CardContent>{content}</CardContent>
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

  return (
    <div className='flex items-center space-x-3'>
      <div className='text-right'>
        <p className='text-sm font-medium'>{user.fullName}</p>
        <p className='text-xs text-muted-foreground'>{user.role}</p>
      </div>
      <Button variant='ghost' size='sm' onClick={() => logout()}>
        Logout
      </Button>
    </div>
  );
};
