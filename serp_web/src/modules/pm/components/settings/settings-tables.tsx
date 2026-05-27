/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings tables
 */

import { memo } from 'react';
import {
  ChevronDown,
  ChevronUp,
  Edit,
  MoreHorizontal,
  Trash2,
} from 'lucide-react';

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

import type {
  PMPrioritySchemeSettingsApi,
  PMPrioritySettingsApi,
  PMWorkTypeSchemeSettingsApi,
  PMWorkTypeSettingsApi,
} from '../../types/api';
import { PriorityGlyph, WorkTypeGlyph } from './settings-glyphs';
import { formatCount } from './settings-page.types';

export const WorkTypesTable = memo(function WorkTypesTable({
  workTypes,
  onEdit,
  onDelete,
}: {
  workTypes: PMWorkTypeSettingsApi[];
  onEdit: (item: PMWorkTypeSettingsApi) => void;
  onDelete: (item: PMWorkTypeSettingsApi) => void;
}) {
  if (workTypes.length === 0) {
    return <EmptyState message='No work types match the current filter.' />;
  }

  return (
    <div className='overflow-x-auto'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Hierarchy level</TableHead>
            <TableHead>Related schemes</TableHead>
            <TableHead className='text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {workTypes.map((workType) => (
            <TableRow key={workType.id}>
              <TableCell className='min-w-[280px]'>
                <div className='flex items-start gap-3'>
                  <WorkTypeGlyph workType={workType} />
                  <div>
                    <div className='font-medium'>{workType.name}</div>
                    <p className='text-xs text-muted-foreground'>
                      {workType.description || workType.typeKey}
                    </p>
                  </div>
                </div>
              </TableCell>
              <TableCell>{workType.hierarchyLevel}</TableCell>
              <TableCell>
                <RelatedSchemeList schemes={workType.relatedSchemes} />
              </TableCell>
              <TableCell className='text-right'>
                <RowActionMenu
                  label={workType.name}
                  readOnly={workType.readOnly}
                  onEdit={() => onEdit(workType)}
                  onDelete={() => onDelete(workType)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
});

export const WorkTypeSchemesTable = memo(function WorkTypeSchemesTable({
  schemes,
  onEdit,
  onDelete,
}: {
  schemes: PMWorkTypeSchemeSettingsApi[];
  onEdit: (item: PMWorkTypeSchemeSettingsApi) => void;
  onDelete: (item: PMWorkTypeSchemeSettingsApi) => void;
}) {
  if (schemes.length === 0) {
    return (
      <EmptyState message='No work type schemes match the current filter.' />
    );
  }

  return (
    <div className='overflow-x-auto'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Options</TableHead>
            <TableHead>Spaces</TableHead>
            <TableHead className='text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {schemes.map((scheme) => (
            <TableRow key={scheme.id}>
              <TableCell className='min-w-[320px] align-top'>
                <div className='font-medium'>{scheme.name}</div>
                <p className='text-xs text-muted-foreground'>
                  {scheme.description || 'No description'}
                </p>
              </TableCell>
              <TableCell className='align-top'>
                <div className='space-y-1'>
                  {scheme.workTypes.map((workType) => (
                    <div key={workType.id} className='flex items-center gap-2'>
                      <WorkTypeGlyph workType={workType} />
                      <span>{workType.name}</span>
                      {workType.isDefault ? (
                        <Badge variant='outline' className='text-[10px]'>
                          DEFAULT
                        </Badge>
                      ) : null}
                    </div>
                  ))}
                </div>
              </TableCell>
              <TableCell className='align-top'>
                {scheme.spaces.length > 0 ? (
                  <div className='space-y-1'>
                    {scheme.spaces.map((space) => (
                      <div key={space.id} className='text-sm'>
                        {space.name}
                      </div>
                    ))}
                  </div>
                ) : (
                  <span className='text-sm text-muted-foreground'>-</span>
                )}
              </TableCell>
              <TableCell className='text-right align-top'>
                <RowActionMenu
                  label={scheme.name}
                  readOnly={scheme.readOnly}
                  onEdit={() => onEdit(scheme)}
                  onDelete={() => onDelete(scheme)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
});

export const PrioritiesTable = memo(function PrioritiesTable({
  priorities,
  onMove,
  onEdit,
  onDelete,
}: {
  priorities: PMPrioritySettingsApi[];
  onMove: (item: PMPrioritySettingsApi, direction: 'up' | 'down') => void;
  onEdit: (item: PMPrioritySettingsApi) => void;
  onDelete: (item: PMPrioritySettingsApi) => void;
}) {
  if (priorities.length === 0) {
    return <EmptyState message='No priorities match the current filter.' />;
  }

  return (
    <div className='overflow-x-auto'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className='w-24'>Order</TableHead>
            <TableHead>Icon and name</TableHead>
            <TableHead>Description</TableHead>
            <TableHead>Color</TableHead>
            <TableHead>Schemes</TableHead>
            <TableHead className='text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {priorities.map((priority, index) => (
            <TableRow key={priority.id}>
              <TableCell>{priority.sequence ?? index + 1}</TableCell>
              <TableCell className='min-w-[220px]'>
                <div className='flex items-center gap-3'>
                  <PriorityGlyph priority={priority} />
                  <span className='font-medium'>{priority.name}</span>
                </div>
              </TableCell>
              <TableCell className='max-w-sm text-muted-foreground'>
                {priority.description || '-'}
              </TableCell>
              <TableCell>
                <ColorSwatch color={priority.color} />
              </TableCell>
              <TableCell>
                <RelatedSchemeList schemes={priority.relatedSchemes} />
              </TableCell>
              <TableCell className='text-right'>
                <PriorityRowActionMenu
                  label={priority.name}
                  readOnly={priority.readOnly}
                  isFirst={index === 0}
                  isLast={index === priorities.length - 1}
                  onMoveUp={() => onMove(priority, 'up')}
                  onMoveDown={() => onMove(priority, 'down')}
                  onEdit={() => onEdit(priority)}
                  onDelete={() => onDelete(priority)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
});

export const PrioritySchemesTable = memo(function PrioritySchemesTable({
  schemes,
  onEdit,
  onDelete,
}: {
  schemes: PMPrioritySchemeSettingsApi[];
  onEdit: (item: PMPrioritySchemeSettingsApi) => void;
  onDelete: (item: PMPrioritySchemeSettingsApi) => void;
}) {
  if (schemes.length === 0) {
    return (
      <EmptyState message='No priority schemes match the current filter.' />
    );
  }

  return (
    <div className='overflow-x-auto'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Priorities</TableHead>
            <TableHead>Number of priorities</TableHead>
            <TableHead>Spaces</TableHead>
            <TableHead className='text-right'>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {schemes.map((scheme) => (
            <TableRow key={scheme.id}>
              <TableCell className='min-w-[320px] align-top'>
                <div className='font-medium'>{scheme.name}</div>
                <p className='text-xs text-muted-foreground'>
                  {scheme.description || 'No description'}
                </p>
              </TableCell>
              <TableCell className='align-top'>
                <div className='space-y-1'>
                  {scheme.priorities.map((priority) => (
                    <div key={priority.id} className='flex items-center gap-2'>
                      <PriorityGlyph priority={priority} />
                      <span>{priority.name}</span>
                      {priority.isDefault ? (
                        <Badge variant='outline' className='text-[10px]'>
                          DEFAULT
                        </Badge>
                      ) : null}
                    </div>
                  ))}
                </div>
              </TableCell>
              <TableCell className='align-top'>
                {formatCount(scheme.priorities.length)}
              </TableCell>
              <TableCell className='align-top'>
                {scheme.spaces.length > 0 ? (
                  <div className='space-y-1'>
                    {scheme.spaces.map((space) => (
                      <div key={space.id} className='text-sm'>
                        {space.name}
                      </div>
                    ))}
                  </div>
                ) : (
                  <span className='text-sm text-muted-foreground'>-</span>
                )}
              </TableCell>
              <TableCell className='text-right align-top'>
                <RowActionMenu
                  label={scheme.name}
                  readOnly={scheme.readOnly}
                  onEdit={() => onEdit(scheme)}
                  onDelete={() => onDelete(scheme)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
});

export function MiniStat({ title, value }: { title: string; value: number }) {
  return (
    <div className='rounded-md border bg-muted/20 px-3 py-3'>
      <p className='text-xs font-medium uppercase text-muted-foreground'>
        {title}
      </p>
      <div className='mt-1 text-2xl font-semibold'>{value}</div>
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return <div className='p-6 text-sm text-muted-foreground'>{message}</div>;
}

function RelatedSchemeList({
  schemes,
}: {
  schemes: Array<{ id: number; name: string }>;
}) {
  if (schemes.length === 0) {
    return <span className='text-sm text-muted-foreground'>-</span>;
  }

  return (
    <ul className='space-y-1 text-sm text-primary'>
      {schemes.map((scheme) => (
        <li key={scheme.id}>{scheme.name}</li>
      ))}
    </ul>
  );
}

function RowActionMenu({
  label,
  readOnly,
  onEdit,
  onDelete,
}: {
  label: string;
  readOnly: boolean;
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
        <DropdownMenuItem onClick={onEdit} disabled={readOnly}>
          <Edit className='mr-2 h-4 w-4' />
          Edit
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={onDelete}
          disabled={readOnly}
          className='text-destructive'
        >
          <Trash2 className='mr-2 h-4 w-4' />
          Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

function PriorityRowActionMenu({
  label,
  readOnly,
  isFirst,
  isLast,
  onMoveUp,
  onMoveDown,
  onEdit,
  onDelete,
}: {
  label: string;
  readOnly: boolean;
  isFirst: boolean;
  isLast: boolean;
  onMoveUp: () => void;
  onMoveDown: () => void;
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
        <DropdownMenuItem onClick={onMoveUp} disabled={readOnly || isFirst}>
          <ChevronUp className='mr-2 h-4 w-4' />
          Move up
        </DropdownMenuItem>
        <DropdownMenuItem onClick={onMoveDown} disabled={readOnly || isLast}>
          <ChevronDown className='mr-2 h-4 w-4' />
          Move down
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={onEdit} disabled={readOnly}>
          <Edit className='mr-2 h-4 w-4' />
          Edit
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={onDelete}
          disabled={readOnly}
          className='text-destructive'
        >
          <Trash2 className='mr-2 h-4 w-4' />
          Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

function ColorSwatch({ color }: { color?: string | null }) {
  return (
    <span className='flex items-center gap-2 text-sm'>
      <span
        className='h-5 w-5 rounded-md border'
        style={{ backgroundColor: color || 'transparent' }}
      />
      {color || '-'}
    </span>
  );
}
