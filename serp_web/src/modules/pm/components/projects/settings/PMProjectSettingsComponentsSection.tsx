/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings components section
 */

import { FolderKanban } from 'lucide-react';

import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';

interface PMProjectSettingsComponentsSectionProps {
  components: PMProjectSettingsOverviewApi['components'];
  onOpenComponents: () => void;
}

export function PMProjectSettingsComponentsSection({
  components,
  onOpenComponents,
}: PMProjectSettingsComponentsSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Components</CardTitle>
      </CardHeader>
      <CardContent className='p-0'>
        <div className='flex items-center justify-between border-b px-4 py-3 text-sm text-muted-foreground'>
          <span>{components.totalCount} total</span>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={onOpenComponents}
          >
            <FolderKanban className='mr-2 h-4 w-4' />
            Open components
          </Button>
        </div>
        {components.preview.length > 0 ? (
          <div className='divide-y'>
            {components.preview.map((component) => (
              <div
                key={component.id}
                className='grid gap-3 px-4 py-3 md:grid-cols-[minmax(0,1fr)_120px_160px]'
              >
                <div className='min-w-0'>
                  <p className='font-medium'>{component.name}</p>
                  <p className='truncate text-sm text-muted-foreground'>
                    {component.description || '-'}
                  </p>
                </div>
                <div className='text-sm text-muted-foreground'>
                  {component.issueCount} issues
                </div>
                <div className='text-sm text-muted-foreground'>
                  {component.assigneeType}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className='px-4 py-6 text-sm text-muted-foreground'>
            No components.
          </div>
        )}
      </CardContent>
    </Card>
  );
}
