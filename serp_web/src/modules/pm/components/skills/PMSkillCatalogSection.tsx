/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM skill catalog settings section
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import { Archive, Edit, Loader2, Plus, Search } from 'lucide-react';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';

import {
  useArchivePmSkillMutation,
  useCreatePmSkillMutation,
  useGetPmSkillsQuery,
  useUpdatePmSkillMutation,
} from '../../api';
import type { PMSkillApi } from '../../types/api';
import { normalizeOptionalDescription } from './skill-ui.utils';

type SkillDialogState =
  | { mode: 'create'; skill?: undefined }
  | { mode: 'edit'; skill: PMSkillApi };

export function PMSkillCatalogSection() {
  const [search, setSearch] = useState('');
  const [dialogState, setDialogState] = useState<SkillDialogState | null>(null);
  const [archiveTarget, setArchiveTarget] = useState<PMSkillApi | null>(null);

  const skillsQuery = useGetPmSkillsQuery();
  const [createSkill, createState] = useCreatePmSkillMutation();
  const [updateSkill, updateState] = useUpdatePmSkillMutation();
  const [archiveSkill, archiveState] = useArchivePmSkillMutation();

  const filteredSkills = useMemo(() => {
    const needle = search.trim().toLowerCase();
    const skills = skillsQuery.data ?? [];
    if (!needle) {
      return skills;
    }
    return skills.filter((skill) =>
      [skill.code, skill.name, skill.description]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(needle)
    );
  }, [search, skillsQuery.data]);

  const handleSubmit = async (values: {
    code: string;
    name: string;
    description: string;
  }) => {
    const body = {
      code: values.code.trim(),
      name: values.name.trim(),
      description: normalizeOptionalDescription(values.description),
    };

    try {
      if (dialogState?.mode === 'edit') {
        await updateSkill({
          skillId: dialogState.skill.id,
          body,
        }).unwrap();
        toast.success('Skill updated.');
      } else {
        await createSkill(body).unwrap();
        toast.success('Skill created.');
      }
      setDialogState(null);
    } catch (error) {
      toast.error('Unable to save skill', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleArchive = async () => {
    if (!archiveTarget) {
      return;
    }
    try {
      await archiveSkill(archiveTarget.id).unwrap();
      toast.success('Skill archived.');
      setArchiveTarget(null);
    } catch (error) {
      toast.error('Unable to archive skill', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-4'>
      <div className='flex flex-col gap-3 border-b border-border/60 pb-4 md:flex-row md:items-start md:justify-between'>
        <div className='space-y-1'>
          <h1 className='text-2xl font-semibold tracking-tight'>Skills</h1>
          <p className='text-sm text-muted-foreground'>
            Manage the tenant skill catalog used by user profiles and work item
            requirements.
          </p>
        </div>
        <Button
          type='button'
          onClick={() => setDialogState({ mode: 'create' })}
        >
          <Plus className='mr-2 h-4 w-4' />
          Add skill
        </Button>
      </div>

      <Card className='border-border/60 bg-background/90 shadow-sm'>
        <CardHeader className='flex flex-col gap-3 border-b py-4 md:flex-row md:items-center md:justify-between'>
          <CardTitle className='text-base'>Skill catalog</CardTitle>
          <div className='relative w-full md:max-w-sm'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder='Filter skills'
              className='pl-9'
            />
          </div>
        </CardHeader>
        <CardContent className='p-0'>
          {skillsQuery.isLoading ? (
            <div className='space-y-3 p-4'>
              {Array.from({ length: 5 }).map((_, index) => (
                <Skeleton key={index} className='h-12 rounded-md' />
              ))}
            </div>
          ) : skillsQuery.error ? (
            <div className='p-6 text-sm text-muted-foreground'>
              Unable to load skills: {getErrorMessage(skillsQuery.error)}
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Skill</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='w-28 text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredSkills.map((skill) => (
                  <TableRow key={skill.id}>
                    <TableCell>
                      <div className='space-y-1'>
                        <div className='font-medium'>{skill.name}</div>
                        <div className='text-xs text-muted-foreground'>
                          {skill.code}
                        </div>
                      </div>
                    </TableCell>
                    <TableCell className='max-w-xl text-sm text-muted-foreground'>
                      {skill.description || '-'}
                    </TableCell>
                    <TableCell>
                      <Badge variant={skill.active ? 'secondary' : 'outline'}>
                        {skill.active === false ? 'Archived' : 'Active'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className='flex justify-end gap-1'>
                        <Button
                          type='button'
                          variant='ghost'
                          size='icon'
                          onClick={() =>
                            setDialogState({ mode: 'edit', skill })
                          }
                        >
                          <Edit className='h-4 w-4' />
                        </Button>
                        <Button
                          type='button'
                          variant='ghost'
                          size='icon'
                          onClick={() => setArchiveTarget(skill)}
                        >
                          <Archive className='h-4 w-4' />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
                {filteredSkills.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={4}
                      className='h-32 text-center text-sm text-muted-foreground'
                    >
                      No skills found.
                    </TableCell>
                  </TableRow>
                ) : null}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <SkillCatalogDialog
        state={dialogState}
        isSubmitting={createState.isLoading || updateState.isLoading}
        onOpenChange={(open) => !open && setDialogState(null)}
        onSubmit={handleSubmit}
      />
      <ConfirmDialog
        open={archiveTarget !== null}
        onOpenChange={(open) => !open && setArchiveTarget(null)}
        title='Archive skill'
        description={
          archiveTarget
            ? `Archive "${archiveTarget.name}"? Existing references remain in history, but the skill is no longer selectable.`
            : undefined
        }
        confirmText='Archive'
        variant='destructive'
        isLoading={archiveState.isLoading}
        onConfirm={handleArchive}
      />
    </div>
  );
}

function SkillCatalogDialog({
  state,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: SkillDialogState | null;
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: {
    code: string;
    name: string;
    description: string;
  }) => void;
}) {
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  useEffect(() => {
    if (!state) {
      return;
    }
    setCode(state.mode === 'edit' ? state.skill.code : '');
    setName(state.mode === 'edit' ? state.skill.name : '');
    setDescription(state.mode === 'edit' ? state.skill.description || '' : '');
  }, [state]);

  const canSubmit = code.trim().length > 0 && name.trim().length > 0;

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-lg'>
        <DialogHeader>
          <DialogTitle>
            {state?.mode === 'edit' ? 'Edit skill' : 'Add skill'}
          </DialogTitle>
        </DialogHeader>
        <div className='space-y-4'>
          <label className='space-y-2'>
            <Label>Code</Label>
            <Input
              value={code}
              onChange={(event) => setCode(event.target.value)}
            />
          </label>
          <label className='space-y-2'>
            <Label>Name</Label>
            <Input
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
          </label>
          <label className='space-y-2'>
            <Label>Description</Label>
            <Textarea
              rows={4}
              value={description}
              onChange={(event) => setDescription(event.target.value)}
            />
          </label>
        </div>
        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button
            type='button'
            disabled={!canSubmit || isSubmitting}
            onClick={() => onSubmit({ code, name, description })}
          >
            {isSubmitting ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : null}
            Save
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
