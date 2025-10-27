/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Home page for authenticated users
 */

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { Header } from '@/shared/components';
import { ModuleShowcase } from '@/modules/account/components';
import { useModules, useAuth } from '@/modules/account/hooks';
import { Loader2 } from 'lucide-react';

export default function HomePage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const { modules, isLoading: modulesLoading } = useModules();

  // Redirect to login if not authenticated
  React.useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/auth/login');
    }
  }, [isAuthenticated, authLoading, router]);

  // Show loading state
  if (authLoading || !isAuthenticated) {
    return (
      <div className='min-h-screen flex items-center justify-center bg-background'>
        <Loader2 className='w-8 h-8 animate-spin text-primary' />
      </div>
    );
  }

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='container mx-auto px-4 py-8'>
        {/* Welcome Section */}
        <section className='mb-8'>
          <h1 className='text-4xl font-bold mb-2'>Welcome to SERP</h1>
          <p className='text-muted-foreground text-lg'>
            Select a module to get started
          </p>
        </section>

        {/* Module Showcase */}
        <section>
          <ModuleShowcase modules={modules} isLoading={modulesLoading} />
        </section>
      </main>
    </div>
  );
}
