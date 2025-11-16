/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Featured Apps Showcase Component
 */

'use client';

import { useRouter } from 'next/navigation';
import { ArrowRight } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { ModuleCard, useGetAllModulesQuery } from '@/modules/apps';

export function FeaturedAppsShowcase() {
  const router = useRouter();

  // Fetch modules and show only ACTIVE/BETA ones, limited to 8
  const { data: modules = [], isLoading } = useGetAllModulesQuery({
    isGlobal: true,
  });

  const featuredModules = modules
    .filter((m) => m.status === 'ACTIVE' || m.status === 'BETA')
    .sort((a, b) => (a.displayOrder || 999) - (b.displayOrder || 999))
    .slice(0, 8);

  if (isLoading) {
    return (
      <section className='py-16 w-full'>
        <div className='container mx-auto px-4'>
          <div className='text-center mb-12'>
            <h2 className='text-3xl font-bold mb-4'>Available Applications</h2>
            <p className='text-muted-foreground text-lg max-w-2xl mx-auto'>
              Comprehensive business management tools designed to streamline
              your operations and boost productivity across all departments.
            </p>
          </div>

          <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'>
            {[...Array(8)].map((_, i) => (
              <div key={i} className='animate-pulse bg-muted rounded-lg h-80' />
            ))}
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className='py-16 w-full'>
      <div className='container mx-auto px-4'>
        <div className='text-center mb-12'>
          <h2 className='text-3xl font-bold mb-4'>Available Applications</h2>
          <p className='text-muted-foreground text-lg max-w-2xl mx-auto'>
            Comprehensive business management tools designed to streamline your
            operations and boost productivity across all departments.
          </p>
        </div>

        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-12'>
          {featuredModules.map((module) => (
            <ModuleCard key={module.id} module={module} viewMode='grid' />
          ))}
        </div>

        <div className='text-center'>
          <Button
            variant='outline'
            size='lg'
            className='gap-2'
            onClick={() => router.push('/apps')}
          >
            View All Applications
            <ArrowRight className='w-4 h-4' />
          </Button>
        </div>
      </div>
    </section>
  );
}
