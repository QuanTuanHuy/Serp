/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings general section
 */

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';
import { PMProjectSettingsField } from './PMProjectSettingsField';

interface PMProjectSettingsGeneralSectionProps {
  project: PMProjectSettingsOverviewApi['project'];
}

export function PMProjectSettingsGeneralSection({
  project,
}: PMProjectSettingsGeneralSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>Project profile</CardTitle>
      </CardHeader>
      <CardContent className='grid gap-3 p-4 md:grid-cols-2'>
        <PMProjectSettingsField label='Name' value={project.name} />
        <PMProjectSettingsField label='Key' value={project.key} />
        <PMProjectSettingsField
          label='Lead'
          value={project.leadUserName || 'Unassigned'}
        />
        <PMProjectSettingsField
          label='Category'
          value={project.category?.name || 'No category'}
        />
        <PMProjectSettingsField
          label='Project type'
          value={project.projectTypeKey}
        />
        <PMProjectSettingsField
          label='State'
          value={project.isArchived ? 'Archived' : 'Active'}
        />
        <div className='md:col-span-2'>
          <PMProjectSettingsField
            label='Description'
            value={project.description || '-'}
          />
        </div>
      </CardContent>
    </Card>
  );
}
