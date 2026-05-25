/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project — edit CRM team member dialog
 */

'use client';

import { useEffect, useState } from 'react';
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
  ExperienceLevel,
  TeamMember,
  TeamMemberRole,
  TeamMemberStatus,
  UpdateTeamMemberRequest,
  WorkingHoursItem,
} from '../../types';
import {
  formatCommaSeparatedList,
  normalizeWorkingHoursWeek,
  parseCommaSeparatedList,
  toPersistableWorkingHours,
} from '../../utils/working-hours-time';
import { WorkingHoursEditor } from './WorkingHoursEditor';

export interface EditTeamMemberDialogProps {
  member: TeamMember;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: UpdateTeamMemberRequest) => void;
}

const EXPERIENCE_OPTIONS: { value: ExperienceLevel; label: string }[] = [
  { value: 'JUNIOR', label: 'Junior' },
  { value: 'MID', label: 'Mid' },
  { value: 'SENIOR', label: 'Senior' },
  { value: 'EXPERT', label: 'Expert' },
];

export function EditTeamMemberDialog({
  member,
  open,
  onOpenChange,
  onSubmit,
}: EditTeamMemberDialogProps) {
  const [name, setName] = useState(member.name ?? '');
  const [email, setEmail] = useState(member.email ?? '');
  const [phone, setPhone] = useState(member.phone ?? '');
  const [role, setRole] = useState<TeamMemberRole>(member.role);
  const [status, setStatus] = useState<TeamMemberStatus>(member.status);
  const [experienceLevel, setExperienceLevel] = useState<ExperienceLevel>(
    member.experienceLevel ?? 'MID'
  );
  const [capacity, setCapacity] = useState(
    member.capacity != null ? String(member.capacity) : ''
  );
  const [maxMeetings, setMaxMeetings] = useState(
    member.maxMeetings != null ? String(member.maxMeetings) : ''
  );
  const [skillsText, setSkillsText] = useState(
    formatCommaSeparatedList(member.skills)
  );
  const [languagesText, setLanguagesText] = useState(
    formatCommaSeparatedList(member.languages)
  );
  const [workingHours, setWorkingHours] = useState<WorkingHoursItem[]>(() =>
    normalizeWorkingHoursWeek(member.workingHours)
  );

  useEffect(() => {
    setName(member.name ?? '');
    setEmail(member.email ?? '');
    setPhone(member.phone ?? '');
    setRole(member.role);
    setStatus(member.status);
    setExperienceLevel(member.experienceLevel ?? 'MID');
    setCapacity(member.capacity != null ? String(member.capacity) : '');
    setMaxMeetings(
      member.maxMeetings != null ? String(member.maxMeetings) : ''
    );
    setSkillsText(formatCommaSeparatedList(member.skills));
    setLanguagesText(formatCommaSeparatedList(member.languages));
    setWorkingHours(normalizeWorkingHoursWeek(member.workingHours));
  }, [member]);

  const handleSubmit = () => {
    const payload: UpdateTeamMemberRequest = {
      name: name.trim() || undefined,
      email: email.trim() || undefined,
      phone: phone.trim() || undefined,
      role,
      status,
      experienceLevel,
      skills: parseCommaSeparatedList(skillsText),
      languages: parseCommaSeparatedList(languagesText),
    };

    const cap = capacity.trim() ? Number.parseInt(capacity, 10) : NaN;
    if (!Number.isNaN(cap)) {
      payload.capacity = cap;
    }

    const maxM = maxMeetings.trim() ? Number.parseInt(maxMeetings, 10) : NaN;
    if (!Number.isNaN(maxM)) {
      payload.maxMeetings = maxM;
    }

    const wh = toPersistableWorkingHours(workingHours);
    payload.workingHours = wh ?? [];

    onSubmit(payload);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-lg'>
        <DialogHeader>
          <DialogTitle>Edit Team Member</DialogTitle>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <Label>Name</Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className='space-y-2'>
            <Label>Email</Label>
            <Input value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className='space-y-2'>
            <Label>Phone</Label>
            <Input value={phone} onChange={(e) => setPhone(e.target.value)} />
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
            <Label>Status *</Label>
            <Select
              value={status}
              onValueChange={(v) => setStatus(v as TeamMemberStatus)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ACTIVE'>Active</SelectItem>
                <SelectItem value='INACTIVE'>Inactive</SelectItem>
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
          <Button onClick={handleSubmit}>Update Member</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
