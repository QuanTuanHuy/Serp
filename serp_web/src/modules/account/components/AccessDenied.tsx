/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Component to display access denied messages with various customization options
 */

'use client';

import React from 'react';
import {
  ShieldX,
  Lock,
  Users,
  Key,
  AlertTriangle,
  ArrowLeft,
  Home,
} from 'lucide-react';
import { Button } from '../../../shared/components';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../../../shared/components';
import { cn } from '../../../shared/utils';

export type AccessDeniedReason =
  | 'authentication'
  | 'authorization'
  | 'role'
  | 'permission'
  | 'organization'
  | 'feature'
  | 'maintenance'
  | 'custom';

export interface AccessDeniedProps {
  /** The reason for access denial */
  reason?: AccessDeniedReason;

  /** Custom title for the access denied message */
  title?: string;

  /** Custom description for the access denied message */
  description?: string;

  /** Additional details to show */
  details?: string;

  /** Required roles that user lacks */
  requiredRoles?: string[];

  /** Required permissions that user lacks */
  requiredPermissions?: string[];

  /** Show a back button */
  showBackButton?: boolean;

  /** Show a home button */
  showHomeButton?: boolean;

  /** Show contact support button */
  showContactSupport?: boolean;

  /** Custom action buttons */
  actions?: React.ReactNode;

  /** Custom icon */
  icon?: React.ReactNode;

  /** Additional CSS classes */
  className?: string;

  /** Variant styling */
  variant?: 'default' | 'minimal' | 'detailed';

  /** Size variant */
  size?: 'sm' | 'md' | 'lg';

  /** Callback when back button is clicked */
  onBack?: () => void;

  /** Callback when home button is clicked */
  onHome?: () => void;

  /** Callback when contact support is clicked */
  onContactSupport?: () => void;
}

const reasonConfig: Record<
  AccessDeniedReason,
  {
    icon: React.ComponentType<{ className?: string }>;
    title: string;
    description: string;
    color: string;
  }
> = {
  authentication: {
    icon: Lock,
    title: 'Authentication Required',
    description: 'You need to log in to access this page.',
    color: 'text-amber-600',
  },
  authorization: {
    icon: ShieldX,
    title: 'Access Denied',
    description: "You don't have permission to access this resource.",
    color: 'text-red-600',
  },
  role: {
    icon: Users,
    title: 'Insufficient Role',
    description: "Your current role doesn't allow access to this feature.",
    color: 'text-orange-600',
  },
  permission: {
    icon: Key,
    title: 'Missing Permissions',
    description: 'You lack the required permissions for this action.',
    color: 'text-red-600',
  },
  organization: {
    icon: ShieldX,
    title: 'Organization Access',
    description: 'This resource is not available for your organization.',
    color: 'text-purple-600',
  },
  feature: {
    icon: Lock,
    title: 'Feature Unavailable',
    description: 'This feature is not enabled for your account.',
    color: 'text-blue-600',
  },
  maintenance: {
    icon: AlertTriangle,
    title: 'Under Maintenance',
    description: 'This feature is temporarily unavailable due to maintenance.',
    color: 'text-yellow-600',
  },
  custom: {
    icon: ShieldX,
    title: 'Access Restricted',
    description: 'Access to this resource is restricted.',
    color: 'text-gray-600',
  },
};

export const AccessDenied: React.FC<AccessDeniedProps> = ({
  reason = 'authorization',
  title,
  description,
  details,
  requiredRoles,
  requiredPermissions,
  showBackButton = true,
  showHomeButton = false,
  showContactSupport = false,
  actions,
  icon,
  className,
  variant = 'default',
  size = 'md',
  onBack,
  onHome,
  onContactSupport,
}) => {
  const config = reasonConfig[reason];
  const IconComponent = icon ? () => icon : config.icon;

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (typeof window !== 'undefined') {
      window.history.back();
    }
  };

  const handleHome = () => {
    if (onHome) {
      onHome();
    } else if (typeof window !== 'undefined') {
      window.location.href = '/';
    }
  };

  const handleContactSupport = () => {
    if (onContactSupport) {
      onContactSupport();
    } else {
      // Default contact support action
      const email = 'support@serp.com';
      const subject = encodeURIComponent('Access Denied - Support Request');
      const body = encodeURIComponent(
        `I'm experiencing an access denied issue.\n\nReason: ${reason}\nPage: ${window.location.href}\nTimestamp: ${new Date().toISOString()}`
      );
      window.location.href = `mailto:${email}?subject=${subject}&body=${body}`;
    }
  };

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
  };

  if (variant === 'minimal') {
    return (
      <div
        className={cn(
          'flex flex-col items-center justify-center text-center space-y-4 p-8',
          sizeClasses[size],
          'mx-auto',
          className
        )}
      >
        <IconComponent className={cn('h-12 w-12', config.color)} />
        <div className='space-y-2'>
          <h2 className='text-lg font-semibold text-foreground'>
            {title || config.title}
          </h2>
          <p className='text-sm text-muted-foreground'>
            {description || config.description}
          </p>
        </div>

        {(showBackButton || showHomeButton || actions) && (
          <div className='flex flex-wrap gap-2 justify-center'>
            {showBackButton && (
              <Button
                variant='outline'
                size='sm'
                onClick={handleBack}
                className='gap-2'
              >
                <ArrowLeft className='h-4 w-4' />
                Go Back
              </Button>
            )}
            {showHomeButton && (
              <Button
                variant='outline'
                size='sm'
                onClick={handleHome}
                className='gap-2'
              >
                <Home className='h-4 w-4' />
                Home
              </Button>
            )}
            {actions}
          </div>
        )}
      </div>
    );
  }

  return (
    <div
      className={cn(
        'flex items-center justify-center min-h-[400px] p-4',
        className
      )}
    >
      <Card className={cn('w-full', sizeClasses[size])}>
        <CardHeader className='text-center space-y-4'>
          <div className='mx-auto'>
            <IconComponent className={cn('h-16 w-16', config.color)} />
          </div>
          <div className='space-y-2'>
            <CardTitle className='text-xl'>{title || config.title}</CardTitle>
            <CardDescription className='text-base'>
              {description || config.description}
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent className='space-y-6'>
          {details && (
            <div className='p-4 bg-muted rounded-lg'>
              <p className='text-sm text-muted-foreground'>{details}</p>
            </div>
          )}

          {variant === 'detailed' && (
            <div className='space-y-4'>
              {requiredRoles && requiredRoles.length > 0 && (
                <div className='space-y-2'>
                  <h4 className='text-sm font-medium text-foreground'>
                    Required Roles:
                  </h4>
                  <div className='flex flex-wrap gap-1'>
                    {requiredRoles.map((role) => (
                      <span
                        key={role}
                        className='inline-flex items-center px-2 py-1 rounded-md bg-secondary text-secondary-foreground text-xs'
                      >
                        {role}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {requiredPermissions && requiredPermissions.length > 0 && (
                <div className='space-y-2'>
                  <h4 className='text-sm font-medium text-foreground'>
                    Required Permissions:
                  </h4>
                  <div className='flex flex-wrap gap-1'>
                    {requiredPermissions.map((permission) => (
                      <span
                        key={permission}
                        className='inline-flex items-center px-2 py-1 rounded-md bg-secondary text-secondary-foreground text-xs'
                      >
                        {permission}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          <div className='flex flex-col sm:flex-row gap-3'>
            {showBackButton && (
              <Button
                variant='outline'
                onClick={handleBack}
                className='gap-2 flex-1'
              >
                <ArrowLeft className='h-4 w-4' />
                Go Back
              </Button>
            )}

            {showHomeButton && (
              <Button
                variant='outline'
                onClick={handleHome}
                className='gap-2 flex-1'
              >
                <Home className='h-4 w-4' />
                Home
              </Button>
            )}

            {showContactSupport && (
              <Button
                variant='default'
                onClick={handleContactSupport}
                className='gap-2 flex-1'
              >
                Contact Support
              </Button>
            )}

            {actions}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

// Convenience components for specific use cases
export const AuthenticationRequired: React.FC<
  Omit<AccessDeniedProps, 'reason'>
> = (props) => <AccessDenied reason='authentication' {...props} />;

export const InsufficientPermissions: React.FC<
  Omit<AccessDeniedProps, 'reason'>
> = (props) => <AccessDenied reason='permission' {...props} />;

export const InsufficientRole: React.FC<Omit<AccessDeniedProps, 'reason'>> = (
  props
) => <AccessDenied reason='role' {...props} />;

export const OrganizationAccessDenied: React.FC<
  Omit<AccessDeniedProps, 'reason'>
> = (props) => <AccessDenied reason='organization' {...props} />;

export const FeatureUnavailable: React.FC<Omit<AccessDeniedProps, 'reason'>> = (
  props
) => <AccessDenied reason='feature' {...props} />;

export const MaintenanceMode: React.FC<Omit<AccessDeniedProps, 'reason'>> = (
  props
) => <AccessDenied reason='maintenance' {...props} />;
