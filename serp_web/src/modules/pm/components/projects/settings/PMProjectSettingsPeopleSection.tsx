/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project settings people section
 */

import { Users } from 'lucide-react';

import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

import type { PMProjectSettingsOverviewApi } from '../../../types/api';
import { PMProjectSettingsField } from './PMProjectSettingsField';

interface PMProjectSettingsPeopleSectionProps {
  people: PMProjectSettingsOverviewApi['people'];
  onOpenPeople: () => void;
}

export function PMProjectSettingsPeopleSection({
  people,
  onOpenPeople,
}: PMProjectSettingsPeopleSectionProps) {
  return (
    <Card className='shadow-sm'>
      <CardHeader className='border-b'>
        <CardTitle className='text-base'>People & roles</CardTitle>
      </CardHeader>
      <CardContent className='space-y-3 p-4'>
        <PMProjectSettingsField
          label='Project lead'
          value={people.leadUserName || 'Unassigned'}
        />
        <div className='grid gap-3 sm:grid-cols-2'>
          <PMProjectSettingsField
            label='Members'
            value={String(people.memberCount)}
          />
          <PMProjectSettingsField
            label='Roles used'
            value={String(people.roleCount)}
          />
        </div>
        <Button
          type='button'
          variant='outline'
          className='w-full justify-start'
          onClick={onOpenPeople}
        >
          <Users className='mr-2 h-4 w-4' />
          Open people
        </Button>
      </CardContent>
    </Card>
  );
}
