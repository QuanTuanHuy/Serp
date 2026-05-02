/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project — add CRM team member dialog
 */

'use client';

import { useState } from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import type {
  CreateTeamMemberRequest,
  ExperienceLevel,
  TeamMemberRole,
} from '../../types';
import {
  createEmptyWorkingHoursWeek,
  parseCommaSeparatedList,
  toPersistableWorkingHours,
} from '../../utils/working-hours-time';
import { WorkingHoursEditor } from './WorkingHoursEditor';

export interface AddTeamMemberDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateTeamMemberRequest) => void;
  teamId: string;
  users: Array<{
    id: number;
    firstName?: string;
    lastName?: string;
    email: string;
  }>;
  isLoadingUsers: boolean;
}

const EXPERIENCE_OPTIONS: { value: ExperienceLevel; label: string }[] = [
  { value: 'JUNIOR', label: 'Junior' },
  { value: 'MID', label: 'Mid' },
  { value: 'SENIOR', label: 'Senior' },
  { value: 'EXPERT', label: 'Expert' },
];

export function AddTeamMemberDialog({
  open,
  onOpenChange,
  onSubmit,
  teamId,
  users,
  isLoadingUsers,
}: AddTeamMemberDialogProps) {
  const [userId, setUserId] = useState('');
  const [role, setRole] = useState<TeamMemberRole>('SALES_REP');
  const [experienceLevel, setExperienceLevel] =
    useState<ExperienceLevel>('MID');
  const [capacity, setCapacity] = useState('');
  const [maxMeetings, setMaxMeetings] = useState('');
  const [skillsText, setSkillsText] = useState('');
  const [languagesText, setLanguagesText] = useState('');
  const [workingHours, setWorkingHours] = useState(() =>
    createEmptyWorkingHoursWeek()
  );

  const resetForm = () => {
    setUserId('');
    setRole('SALES_REP');
    setExperienceLevel('MID');
    setCapacity('');
    setMaxMeetings('');
    setSkillsText('');
    setLanguagesText('');
    setWorkingHours(createEmptyWorkingHoursWeek());
  };

  const handleSubmit = () => {
    const payload: CreateTeamMemberRequest = {
      teamId: Number(teamId),
      userId: Number(userId),
      role,
      experienceLevel,
    };

    const cap = capacity.trim() ? Number.parseInt(capacity, 10) : NaN;
    if (!Number.isNaN(cap)) {
      payload.capacity = cap;
    }

    const maxM = maxMeetings.trim() ? Number.parseInt(maxMeetings, 10) : NaN;
    if (!Number.isNaN(maxM)) {
      payload.maxMeetings = maxM;
    }

    const skills = parseCommaSeparatedList(skillsText);
    if (skills.length) {
      payload.skills = skills;
    }

    const languages = parseCommaSeparatedList(languagesText);
    if (languages.length) {
      payload.languages = languages;
    }

    const wh = toPersistableWorkingHours(workingHours);
    if (wh) {
      payload.workingHours = wh;
    }

    onSubmit(payload);
    resetForm();
  };

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!next) resetForm();
        onOpenChange(next);
      }}
    >
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-lg'>
        <DialogHeader>
          <DialogTitle>Add Team Member</DialogTitle>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <Label>User *</Label>
            <Select value={userId} onValueChange={setUserId}>
              <SelectTrigger>
                <SelectValue
                  placeholder={
                    isLoadingUsers ? 'Loading users...' : 'Select user'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {users.map((user) => {
                  const fullName = [user.firstName, user.lastName]
                    .filter(Boolean)
                    .join(' ')
                    .trim();

                  return (
                    <SelectItem key={user.id} value={String(user.id)}>
                      {fullName || user.email} ({user.email})
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-2'>
            <Label>Role *</Label>
            <Select
              value={role}
              onValueChange={(v) => setRole(v as TeamMemberRole)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='MANAGER'>Manager</SelectItem>
                <SelectItem value='SALES_REP'>Sales Rep</SelectItem>
                <SelectItem value='VIEWER'>Viewer</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-2'>
            <Label>Experience level</Label>
            <Select
              value={experienceLevel}
              onValueChange={(v) => setExperienceLevel(v as ExperienceLevel)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {EXPERIENCE_OPTIONS.map((o) => (
                  <SelectItem key={o.value} value={o.value}>
                    {o.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className='grid grid-cols-1 gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label>Capacity % (1–100)</Label>
              <Input
                type='number'
                min={1}
                max={100}
                placeholder='Optional'
                value={capacity}
                onChange={(e) => setCapacity(e.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label>Max meetings (1–50)</Label>
              <Input
                type='number'
                min={1}
                max={50}
                placeholder='Optional'
                value={maxMeetings}
                onChange={(e) => setMaxMeetings(e.target.value)}
              />
            </div>
          </div>
          <div className='space-y-2'>
            <Label>Skills</Label>
            <Textarea
              placeholder='Comma or newline separated'
              rows={2}
              value={skillsText}
              onChange={(e) => setSkillsText(e.target.value)}
            />
          </div>
          <div className='space-y-2'>
            <Label>Languages</Label>
            <Textarea
              placeholder='Comma or newline separated'
              rows={2}
              value={languagesText}
              onChange={(e) => setLanguagesText(e.target.value)}
            />
          </div>
          <WorkingHoursEditor value={workingHours} onChange={setWorkingHours} />
        </div>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={!userId}>
            Add Member
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
