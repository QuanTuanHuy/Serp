/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings schemes section
 */

import Link from 'next/link';

import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';

interface PMProjectSettingsSchemesSectionProps {
  schemes: PMProjectSettingsOverviewApi['schemes'];
}

export function PMProjectSettingsSchemesSection({
  schemes,
}: PMProjectSettingsSchemesSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Configuration schemes</CardTitle>
      </CardHeader>
      <CardContent className='space-y-2 p-4'>
        {schemes.map((scheme) => (
          <div key={scheme.type} className='rounded-md border p-3'>
            <div className='flex items-start justify-between gap-3'>
              <div className='min-w-0'>
                <p className='text-sm font-medium'>{scheme.label}</p>
                <p className='truncate text-sm text-muted-foreground'>
                  {scheme.schemeName ||
                    (typeof scheme.schemeId === 'number'
                      ? `#${scheme.schemeId}`
                      : '-')}
                </p>
              </div>
              {scheme.globalSection ? (
                <Button asChild variant='ghost' size='sm'>
                  <Link href={`/pm/settings?section=${scheme.globalSection}`}>
                    Open
                  </Link>
                </Button>
              ) : (
                <Badge variant='secondary'>Read-only</Badge>
              )}
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
