'use client';

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  ThemeToggle,
  Input,
} from '@/shared/components';
import { ReduxExample } from '@/shared/components/ReduxExample';
import { useNotification } from '@/shared/hooks';
import { UserProfile, ProtectedRoute, useAuth } from '@/modules/auth';

export default function Home() {
  const notification = useNotification();
  const { isAuthenticated, user } = useAuth();

  const handleSuccessNotification = () => {
    notification.success('Operation completed successfully!', {
      description: 'Your data has been saved to the database.',
      duration: 3000,
    });
  };

  const handleErrorNotification = () => {
    notification.error('Something went wrong!', {
      description: 'Please try again later or contact support.',
      duration: 5000,
    });
  };

  const handleWarningNotification = () => {
    notification.warning('Please confirm your action', {
      description: 'This action cannot be undone.',
      duration: 6000,
    });
  };

  const handleInfoNotification = () => {
    notification.info('System Information', {
      description: 'Your system is running optimally.',
      duration: 5000,
    });
  };

  return (
    <ProtectedRoute permissions={'test'}>
      <div className='container mx-auto p-8 space-y-8'>
        <div className='flex justify-between items-center'>
          <h1 className='text-4xl font-bold'>
            SERP - Enterprise Resource Planning
          </h1>
          <div className='flex items-center gap-4'>
            {user && (
              <div className='text-right'>
                <p className='text-sm font-medium'>
                  Welcome, {user.firstName}!
                </p>
                <p className='text-xs text-muted-foreground'>{user.email}</p>
              </div>
            )}
            <ThemeToggle />
          </div>
        </div>

        {/* User Profile - only show if authenticated */}
        {isAuthenticated && <UserProfile />}

        <Card className='max-w-2xl'>
          <CardHeader>
            <CardTitle>Welcome to SERP Web</CardTitle>
            <CardDescription>
              Modern ERP system with modular architecture
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='flex gap-2'>
              <Input placeholder='Search...' className='flex-1' />
              <Button>Search</Button>
            </div>

            <div className='grid grid-cols-2 gap-2'>
              <Button onClick={handleSuccessNotification} variant='default'>
                ✅ Success
              </Button>
              <Button onClick={handleErrorNotification} variant='destructive'>
                ❌ Error
              </Button>
              <Button onClick={handleWarningNotification} variant='outline'>
                ⚠️ Warning
              </Button>
              <Button onClick={handleInfoNotification} variant='secondary'>
                ℹ️ Info
              </Button>
            </div>

            <div className='flex gap-2'>
              <Button variant='outline'>CRM Module</Button>
              <Button variant='outline'>Accounting</Button>
              <Button variant='outline'>Inventory</Button>
            </div>
          </CardContent>
        </Card>

        {/* Redux Store Status */}
        <ReduxExample />
      </div>
    </ProtectedRoute>
  );
}
