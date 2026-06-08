/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar profile table
 */

import { memo } from 'react';
import { Edit, MoreHorizontal, Trash2 } from 'lucide-react';

import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

import type { PMResourceCalendarProfileApi } from '../../../types/api';

export const PMResourceCalendarProfileTable = memo(
  function PMResourceCalendarProfileTable({
    profiles,
    onEdit,
    onDelete,
  }: {
    profiles: PMResourceCalendarProfileApi[];
    onEdit: (profile: PMResourceCalendarProfileApi) => void;
    onDelete: (profile: PMResourceCalendarProfileApi) => void;
  }) {
    if (profiles.length === 0) {
      return (
        <div className='p-6 text-sm text-muted-foreground'>
          No resource calendar profiles configured.
        </div>
      );
    }

    return (
      <div className='overflow-x-auto'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Timezone</TableHead>
              <TableHead>Assignments</TableHead>
              <TableHead>Weekly blocks</TableHead>
              <TableHead className='text-right'>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {profiles.map((profile) => (
              <TableRow key={profile.id}>
                <TableCell className='min-w-[260px] align-top'>
                  <div className='flex flex-col gap-1'>
                    <div className='flex flex-wrap items-center gap-2'>
                      <span className='font-medium'>{profile.name}</span>
                      {profile.isDefault ? (
                        <Badge variant='outline' className='text-[10px]'>
                          DEFAULT
                        </Badge>
                      ) : null}
                    </div>
                    <span className='text-xs text-muted-foreground'>
                      {profile.description || 'No description'}
                    </span>
                  </div>
                </TableCell>
                <TableCell className='align-top'>{profile.timezone}</TableCell>
                <TableCell className='align-top'>
                  {profile.assignmentCount}
                </TableCell>
                <TableCell className='align-top'>
                  {profile.blocks.length}
                </TableCell>
                <TableCell className='text-right align-top'>
                  <ProfileActionMenu
                    label={profile.name}
                    onEdit={() => onEdit(profile)}
                    onDelete={() => onDelete(profile)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    );
  }
);

function ProfileActionMenu({
  label,
  onEdit,
  onDelete,
}: {
  label: string;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='ghost' size='icon' aria-label={`Actions for ${label}`}>
          <MoreHorizontal className='h-4 w-4' />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end'>
        <DropdownMenuItem onClick={onEdit}>
          <Edit className='mr-2 h-4 w-4' />
          Edit
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={onDelete} className='text-destructive'>
          <Trash2 className='mr-2 h-4 w-4' />
          Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
