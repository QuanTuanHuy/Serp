/**
 * Apps Showcase Component
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

'use client';

import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Button,
} from '@/shared/components';

interface AppItem {
  id: string;
  name: string;
  description: string;
  icon: string;
  route: string;
  color: string;
  isAvailable: boolean;
}

const apps: AppItem[] = [
  {
    id: 'crm',
    name: 'CRM',
    description:
      'Customer Relationship Management - Manage leads, customers, and sales pipeline',
    icon: '👥',
    route: '/crm',
    color: 'bg-blue-500',
    isAvailable: true,
  },
  {
    id: 'accounting',
    name: 'Accounting',
    description: 'Financial management, invoicing, and accounting operations',
    icon: '💰',
    route: '/accounting',
    color: 'bg-green-500',
    isAvailable: false,
  },
  {
    id: 'inventory',
    name: 'Inventory',
    description: 'Warehouse management, stock tracking, and inventory control',
    icon: '📦',
    route: '/inventory',
    color: 'bg-orange-500',
    isAvailable: false,
  },
  {
    id: 'hr',
    name: 'Human Resources',
    description: 'Employee management, payroll, and HR operations',
    icon: '👨‍💼',
    route: '/hr',
    color: 'bg-purple-500',
    isAvailable: false,
  },
  {
    id: 'sales',
    name: 'Sales',
    description: 'Sales management, quotations, and order processing',
    icon: '📈',
    route: '/sales',
    color: 'bg-red-500',
    isAvailable: false,
  },
  {
    id: 'project',
    name: 'Project Management',
    description: 'Project planning, task management, and collaboration tools',
    icon: '📋',
    route: '/project',
    color: 'bg-indigo-500',
    isAvailable: false,
  },
  {
    id: 'ptm',
    name: 'Personal Task Manager',
    description: 'Personal productivity, task scheduling, and time management',
    icon: '✅',
    route: '/ptm',
    color: 'bg-cyan-500',
    isAvailable: true,
  },
  {
    id: 'analytics',
    name: 'Analytics',
    description: 'Business intelligence, reporting, and data analytics',
    icon: '📊',
    route: '/analytics',
    color: 'bg-pink-500',
    isAvailable: false,
  },
];

export function AppsShowcase() {
  const router = useRouter();

  const handleAppClick = (app: AppItem) => {
    if (app.isAvailable) {
      router.push(app.route);
    }
  };

  return (
    <section className='py-12 w-full'>
      <div className='container mx-auto px-4'>
        <div className='text-center mb-12'>
          <h2 className='text-3xl font-bold mb-4'>Available Applications</h2>
          <p className='text-muted-foreground text-lg max-w-2xl mx-auto'>
            Comprehensive business management tools designed to streamline your
            operations and boost productivity across all departments.
          </p>
        </div>

        <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'>
          {apps.map((app) => (
            <Card
              key={app.id}
              className={`relative overflow-hidden transition-all duration-300 hover:shadow-lg ${
                app.isAvailable
                  ? 'cursor-pointer hover:scale-105'
                  : 'opacity-60 cursor-not-allowed'
              }`}
              onClick={() => handleAppClick(app)}
            >
              {!app.isAvailable && (
                <div className='absolute top-2 right-2 bg-muted text-muted-foreground text-xs px-2 py-1 rounded-full'>
                  Coming Soon
                </div>
              )}

              <CardHeader className='pb-4'>
                <div className='flex items-center space-x-3'>
                  <div
                    className={`w-12 h-12 ${app.color} rounded-lg flex items-center justify-center text-white text-2xl`}
                  >
                    {app.icon}
                  </div>
                  <div className='flex-1'>
                    <CardTitle className='text-lg'>{app.name}</CardTitle>
                  </div>
                </div>
              </CardHeader>

              <CardContent className='pt-0'>
                <CardDescription className='text-sm leading-relaxed mb-4'>
                  {app.description}
                </CardDescription>

                <Button
                  variant={app.isAvailable ? 'default' : 'secondary'}
                  size='sm'
                  className='w-full'
                  disabled={!app.isAvailable}
                >
                  {app.isAvailable ? 'Open App' : 'Coming Soon'}
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className='text-center mt-12'>
          <p className='text-muted-foreground mb-4'>
            Need a custom solution for your business?
          </p>
          <Button variant='outline' size='lg'>
            Contact Sales
          </Button>
        </div>
      </div>
    </section>
  );
}
