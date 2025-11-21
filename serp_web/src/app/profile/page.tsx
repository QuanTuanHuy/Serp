/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User Profile View Page
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { useAuth } from '@/modules/account';
import { UserProfile } from '@/modules/account';
import { Button } from '@/shared/components/ui/button';
import { Edit } from 'lucide-react';

export default function ProfilePage() {
  const { user, isLoading, isAuthenticated } = useAuth();

  if (!isAuthenticated && isLoading) {
    return (
      <div className='container mx-auto py-8'>
        <div className='flex justify-center items-center min-h-[400px]'>
          <p className='text-muted-foreground'>Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return (
      <div className='container mx-auto py-8'>
        <div className='flex justify-center items-center min-h-[400px]'>
          <p className='text-muted-foreground'>
            Please log in to view your profile
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className='container mx-auto py-8 max-w-4xl'>
      <div className='flex justify-between items-center mb-6'>
        <h1 className='text-3xl font-bold'>My Profile</h1>
        <Link href='/profile/edit'>
          <Button>
            <Edit className='mr-2 h-4 w-4' />
            Edit Profile
          </Button>
        </Link>
      </div>
      <UserProfile showCard={true} />
    </div>
  );
}
