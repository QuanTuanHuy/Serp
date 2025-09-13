/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication layout with login/register tabs
 */

'use client';

import React, { useState } from 'react';
import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';
import { Button } from '@/shared/components';

type AuthMode = 'login' | 'register';

interface AuthLayoutProps {
  defaultMode?: AuthMode;
  onAuthSuccess?: () => void;
}

export const AuthLayout: React.FC<AuthLayoutProps> = ({
  defaultMode = 'login',
  onAuthSuccess,
}) => {
  const [mode, setMode] = useState<AuthMode>(defaultMode);

  const handleAuthSuccess = () => {
    onAuthSuccess?.();
  };

  return (
    <div className='min-h-screen flex items-center justify-center bg-background p-4'>
      <div className='w-full max-w-md space-y-6'>
        {/* Tab Navigation */}
        <div className='flex rounded-lg bg-muted p-1'>
          <Button
            variant={mode === 'login' ? 'default' : 'ghost'}
            className='flex-1'
            onClick={() => setMode('login')}
          >
            Sign In
          </Button>
          <Button
            variant={mode === 'register' ? 'default' : 'ghost'}
            className='flex-1'
            onClick={() => setMode('register')}
          >
            Sign Up
          </Button>
        </div>

        {/* Auth Forms */}
        {mode === 'login' ? (
          <LoginForm
            onSuccess={handleAuthSuccess}
            onSwitchToRegister={() => setMode('register')}
          />
        ) : (
          <RegisterForm
            onSuccess={handleAuthSuccess}
            onSwitchToLogin={() => setMode('login')}
          />
        )}

        {/* Footer */}
        <div className='text-center text-sm text-muted-foreground'>
          <p>
            By continuing, you agree to SERP&apos;s{' '}
            <Button variant='link' className='p-0 h-auto text-sm'>
              Terms of Service
            </Button>{' '}
            and{' '}
            <Button variant='link' className='p-0 h-auto text-sm'>
              Privacy Policy
            </Button>
          </p>
        </div>
      </div>
    </div>
  );
};
