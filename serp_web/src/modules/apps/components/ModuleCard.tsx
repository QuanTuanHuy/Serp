/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module Card Component
 */

'use client';

import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
  Button,
  Badge,
} from '@/shared/components/ui';
import { getModuleIcon, getModuleRoute } from '@/shared/constants/moduleIcons';
import { CheckCircle2 } from 'lucide-react';
import type { Module } from '../types';
import { useAuth } from '@/modules/account';

interface ModuleCardProps {
  module: Module;
  viewMode?: 'grid' | 'list';
  onDetailsClick?: (module: Module) => void;
}

export function ModuleCard({
  module,
  viewMode = 'grid',
  onDetailsClick,
}: ModuleCardProps) {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const iconConfig = getModuleIcon(module.code);

  const isAvailable = module.status === 'ACTIVE';
  const isBeta = module.status === 'BETA';
  const isComingSoon = module.status === 'DISABLED' || !isAvailable;

  const handleCardClick = () => {
    if (onDetailsClick) {
      onDetailsClick(module);
    }
  };

  const handleActionClick = (e: React.MouseEvent) => {
    e.stopPropagation();

    if (!isAvailable && !isBeta) {
      return;
    }

    if (!isAuthenticated) {
      router.push(
        `/auth?redirect=${encodeURIComponent(getModuleRoute(module.code))}`
      );
      return;
    }

    router.push(getModuleRoute(module.code));
  };

  const IconComponent = iconConfig?.icon;

  if (viewMode === 'list') {
    return (
      <Card
        className={`relative overflow-hidden transition-all duration-300 hover:shadow-md ${
          isAvailable || isBeta
            ? 'cursor-pointer'
            : 'opacity-60 cursor-not-allowed'
        }`}
        onClick={handleCardClick}
      >
        <div className='flex items-center p-6 gap-6'>
          {/* Icon */}
          <div
            className={`w-16 h-16 rounded-lg flex items-center justify-center flex-shrink-0 ${
              iconConfig?.bgColor || 'bg-gray-100'
            }`}
          >
            {IconComponent ? (
              <IconComponent
                className={`w-8 h-8 ${iconConfig?.color || 'text-gray-600'}`}
              />
            ) : (
              <span className='text-2xl'>{module.icon || '📦'}</span>
            )}
          </div>

          {/* Content */}
          <div className='flex-1 min-w-0'>
            <div className='flex items-start justify-between gap-4 mb-2'>
              <div>
                <CardTitle className='text-lg mb-1'>
                  {module.moduleName}
                </CardTitle>
                <div className='flex items-center gap-2 flex-wrap'>
                  {module.category && (
                    <Badge variant='outline' className='text-xs'>
                      {module.category}
                    </Badge>
                  )}
                  {module.isFree ? (
                    <Badge variant='secondary' className='text-xs'>
                      Free
                    </Badge>
                  ) : (
                    <Badge variant='default' className='text-xs'>
                      {module.pricingModel === 'PER_USER'
                        ? 'Per User'
                        : module.pricingModel === 'FIXED'
                          ? 'Fixed Price'
                          : 'Premium'}
                    </Badge>
                  )}
                  {isBeta && (
                    <Badge
                      variant='outline'
                      className='text-xs border-yellow-500 text-yellow-600'
                    >
                      Beta
                    </Badge>
                  )}
                  {isComingSoon && (
                    <Badge variant='outline' className='text-xs'>
                      Coming Soon
                    </Badge>
                  )}
                </div>
              </div>
              <Button
                variant={isAvailable || isBeta ? 'default' : 'secondary'}
                size='sm'
                disabled={isComingSoon}
                onClick={handleActionClick}
              >
                {isComingSoon
                  ? 'Coming Soon'
                  : isAuthenticated
                    ? 'Open App'
                    : 'Get Started'}
              </Button>
            </div>
            <CardDescription className='text-sm line-clamp-2'>
              {module.description || 'No description available'}
            </CardDescription>
          </div>
        </div>
      </Card>
    );
  }

  return (
    <Card
      className={`relative overflow-hidden transition-all duration-300 hover:shadow-lg h-full flex flex-col ${
        isAvailable || isBeta
          ? 'cursor-pointer hover:scale-[1.02]'
          : 'opacity-60 cursor-not-allowed'
      }`}
      onClick={handleCardClick}
    >
      {/* Status badges */}
      <div className='absolute top-3 right-3 flex gap-2 z-10'>
        {isBeta && (
          <Badge
            variant='outline'
            className='text-xs border-yellow-500 text-yellow-600 bg-yellow-50'
          >
            Beta
          </Badge>
        )}
        {isComingSoon && (
          <Badge variant='outline' className='text-xs bg-background'>
            Coming Soon
          </Badge>
        )}
      </div>

      <CardHeader className='pb-4'>
        <div className='flex items-start gap-3'>
          <div
            className={`w-14 h-14 rounded-lg flex items-center justify-center flex-shrink-0 ${
              iconConfig?.bgColor || 'bg-gray-100'
            }`}
          >
            {IconComponent ? (
              <IconComponent
                className={`w-7 h-7 ${iconConfig?.color || 'text-gray-600'}`}
              />
            ) : (
              <span className='text-2xl'>{module.icon || '📦'}</span>
            )}
          </div>
          <div className='flex-1 min-w-0'>
            <CardTitle className='text-lg leading-tight line-clamp-2'>
              {module.moduleName}
            </CardTitle>
            {module.category && (
              <Badge variant='outline' className='text-xs mt-2'>
                {module.category}
              </Badge>
            )}
          </div>
        </div>
      </CardHeader>

      <CardContent className='pt-0 flex-1 flex flex-col'>
        <CardDescription className='text-sm leading-relaxed mb-4 line-clamp-3 flex-1'>
          {module.description ||
            'Powerful tools to streamline your business operations and boost productivity.'}
        </CardDescription>

        {/* Key features placeholder */}
        <div className='space-y-1.5 mb-4'>
          <div className='flex items-center gap-2 text-xs text-muted-foreground'>
            <CheckCircle2 className='w-3.5 h-3.5 text-green-600' />
            <span>Easy to use interface</span>
          </div>
          <div className='flex items-center gap-2 text-xs text-muted-foreground'>
            <CheckCircle2 className='w-3.5 h-3.5 text-green-600' />
            <span>Real-time collaboration</span>
          </div>
          <div className='flex items-center gap-2 text-xs text-muted-foreground'>
            <CheckCircle2 className='w-3.5 h-3.5 text-green-600' />
            <span>Advanced analytics</span>
          </div>
        </div>

        {/* Pricing badge */}
        <div className='flex items-center gap-2 mb-3'>
          {module.isFree ? (
            <Badge variant='secondary' className='text-xs'>
              Free Forever
            </Badge>
          ) : (
            <Badge variant='default' className='text-xs'>
              {module.pricingModel === 'PER_USER'
                ? 'Per User Pricing'
                : module.pricingModel === 'FIXED'
                  ? 'Fixed Price'
                  : 'Premium Plan'}
            </Badge>
          )}
        </div>
      </CardContent>

      <CardFooter className='pt-0'>
        <Button
          variant={isAvailable || isBeta ? 'default' : 'secondary'}
          size='sm'
          className='w-full'
          disabled={isComingSoon}
          onClick={handleActionClick}
        >
          {isComingSoon
            ? 'Coming Soon'
            : isAuthenticated
              ? 'Open App'
              : 'Get Started'}
        </Button>
      </CardFooter>
    </Card>
  );
}
