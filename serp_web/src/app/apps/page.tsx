/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Apps Catalog Page
 */

'use client';

import { useState } from 'react';
import { Header } from '@/shared/components';
import {
  AppsHeader,
  ModuleFilters,
  ModuleGrid,
  useGetAllModulesQuery,
  useModuleFilters,
  type ViewMode,
} from '@/modules/apps';

export default function AppsPage() {
  const [viewMode, setViewMode] = useState<ViewMode>('grid');

  const {
    data: modules = [],
    isLoading,
    isError,
  } = useGetAllModulesQuery({
    isGlobal: true,
  });

  const {
    filters,
    filteredModules,
    updateFilter,
    clearFilters,
    categories,
    hasActiveFilters,
  } = useModuleFilters(modules);

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='w-full'>
        {/* Hero Section with Search */}
        <section className='py-12 md:py-16 bg-gradient-to-br from-primary/10 via-background to-secondary/10'>
          <div className='container mx-auto px-4'>
            <AppsHeader
              searchValue={filters.search}
              onSearchChange={(value) => updateFilter('search', value)}
            />
          </div>
        </section>

        {/* Filters and Results */}
        <section className='py-8'>
          <div className='container mx-auto px-4'>
            {/* Error State */}
            {isError && (
              <div className='text-center py-16'>
                <div className='w-16 h-16 bg-destructive/10 rounded-full flex items-center justify-center mx-auto mb-4'>
                  <span className='text-3xl'>⚠️</span>
                </div>
                <h3 className='text-lg font-semibold mb-2 text-destructive'>
                  Failed to load applications
                </h3>
                <p className='text-muted-foreground text-sm'>
                  Please try again later or contact support if the problem
                  persists.
                </p>
              </div>
            )}

            {/* Filters Bar */}
            {!isError && (
              <ModuleFilters
                viewMode={viewMode}
                onViewModeChange={setViewMode}
                categories={categories}
                selectedCategory={filters.category}
                onCategoryChange={(value) => updateFilter('category', value)}
                selectedPricingModel={filters.pricingModel}
                onPricingModelChange={(value) =>
                  updateFilter('pricingModel', value)
                }
                selectedStatus={filters.status}
                onStatusChange={(value) => updateFilter('status', value)}
                hasActiveFilters={hasActiveFilters}
                onClearFilters={clearFilters}
                resultCount={filteredModules.length}
              />
            )}

            {/* Module Grid */}
            {!isError && (
              <div className='mt-6'>
                <ModuleGrid
                  modules={filteredModules}
                  viewMode={viewMode}
                  isLoading={isLoading}
                />
              </div>
            )}
          </div>
        </section>

        {/* CTA Section */}
        <section className='py-16 bg-muted/30'>
          <div className='container mx-auto px-4 text-center'>
            <h2 className='text-3xl font-bold mb-4'>Need a Custom Solution?</h2>
            <p className='text-muted-foreground text-lg mb-6 max-w-2xl mx-auto'>
              Can&apos;t find exactly what you&apos;re looking for? Contact our
              sales team to discuss custom module development and enterprise
              solutions.
            </p>
            <div className='flex flex-col sm:flex-row gap-4 justify-center'>
              <button className='bg-primary text-primary-foreground px-8 py-3 rounded-lg font-medium hover:bg-primary/90 transition-colors'>
                Contact Sales
              </button>
              <button
                className='border border-input bg-background px-8 py-3 rounded-lg font-medium hover:bg-accent transition-colors'
                onClick={() => (window.location.href = '/subscription')}
              >
                View Pricing Plans
              </button>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className='border-t py-12'>
        <div className='container mx-auto px-4'>
          <div className='grid grid-cols-1 md:grid-cols-4 gap-8'>
            <div>
              <div className='flex items-center space-x-2 mb-4'>
                <div className='h-8 w-8 bg-primary rounded-md flex items-center justify-center'>
                  <span className='text-primary-foreground font-bold text-sm'>
                    S
                  </span>
                </div>
                <span className='font-bold text-xl'>SERP</span>
              </div>
              <p className='text-muted-foreground text-sm'>
                Modern ERP solution for businesses of all sizes.
              </p>
            </div>

            <div>
              <h4 className='font-semibold mb-4'>Product</h4>
              <ul className='space-y-2 text-sm text-muted-foreground'>
                <li>
                  <a href='/apps' className='hover:text-foreground'>
                    Apps
                  </a>
                </li>
                <li>
                  <a href='/subscription' className='hover:text-foreground'>
                    Pricing
                  </a>
                </li>
                <li>
                  <a href='/features' className='hover:text-foreground'>
                    Features
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <h4 className='font-semibold mb-4'>Support</h4>
              <ul className='space-y-2 text-sm text-muted-foreground'>
                <li>
                  <a href='/help' className='hover:text-foreground'>
                    Help Center
                  </a>
                </li>
                <li>
                  <a href='/community' className='hover:text-foreground'>
                    Community
                  </a>
                </li>
                <li>
                  <a href='/contact' className='hover:text-foreground'>
                    Contact
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <h4 className='font-semibold mb-4'>Company</h4>
              <ul className='space-y-2 text-sm text-muted-foreground'>
                <li>
                  <a href='/about' className='hover:text-foreground'>
                    About
                  </a>
                </li>
                <li>
                  <a href='/careers' className='hover:text-foreground'>
                    Careers
                  </a>
                </li>
                <li>
                  <a href='/blog' className='hover:text-foreground'>
                    Blog
                  </a>
                </li>
              </ul>
            </div>
          </div>

          <div className='border-t mt-8 pt-8 text-center text-sm text-muted-foreground'>
            <p>&copy; 2025 SERP. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
