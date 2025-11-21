/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User Profile Edit Page
 */

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/modules/account';
import { EditProfileForm, ChangePasswordForm } from '@/modules/account';
import { Button } from '@/shared/components/ui/button';
import { ArrowLeft } from 'lucide-react';

export default function EditProfilePage() {
  const router = useRouter();
  const { user, isLoading, isAuthenticated } = useAuth();

  if (isLoading) {
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
            Please log in to edit your profile
          </p>
        </div>
      </div>
    );
  }

  const handleSuccess = () => {
    router.push('/profile');
  };

  const handleCancel = () => {
    router.push('/profile');
  };

  return (
    <div className='container mx-auto py-8 max-w-4xl'>
      <div className='mb-6'>
        <Button variant='ghost' onClick={() => router.back()} className='mb-4'>
          <ArrowLeft className='mr-2 h-4 w-4' />
          Back
        </Button>
        <h1 className='text-3xl font-bold'>Edit Profile</h1>
      </div>

      <div className='space-y-6'>
        {/* Basic Information Section */}
        <section>
          <EditProfileForm
            user={user}
            onSuccess={handleSuccess}
            onCancel={handleCancel}
          />
        </section>

        {/* Security Section */}
        <section>
          <ChangePasswordForm userId={user.id} onSuccess={handleSuccess} />
        </section>
      </div>
    </div>
  );
}
