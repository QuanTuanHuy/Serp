// Team & Team Member Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type { BaseEntity } from './base';
import type { CrmDayOfWeek } from './customer';

export type TeamStatus = 'ACTIVE' | 'INACTIVE';
export type TeamMemberRole = 'MANAGER' | 'SALES_REP' | 'VIEWER';
export type TeamMemberStatus = 'ACTIVE' | 'INACTIVE';

/** Matches CRM `ExperienceLevel` enum (scheduling / matching). */
export type ExperienceLevel = 'JUNIOR' | 'MID' | 'SENIOR' | 'EXPERT';

/** CRM `WorkingHoursRequest` / `WorkingHoursResponse` shape. */
export interface WorkingHoursItem {
  dayOfWeek: CrmDayOfWeek;
  workingDay: boolean;
  startMinute?: number;
  endMinute?: number;
}

export interface Team extends BaseEntity {
  name: string;
  description?: string;
  managerUserId?: number;
  notes?: string;
  status: TeamStatus;
  manager?: TeamMember;
  memberCount?: number;
}

export interface TeamMember extends BaseEntity {
  name?: string;
  email?: string;
  phone?: string;
  teamId: string;
  userId?: number;
  role: TeamMemberRole;
  status: TeamMemberStatus;
  skills?: string[];
  languages?: string[];
  experienceLevel?: ExperienceLevel;
  capacity?: number;
  maxMeetings?: number;
  workingHours?: WorkingHoursItem[];
}

// Filter types
export interface TeamFilters {
  status?: TeamStatus;
}

// Request types
export interface CreateTeamRequest {
  name: string;
  description?: string;
  managerUserId: number;
  notes?: string;
}

export interface UpdateTeamRequest {
  name?: string;
  description?: string;
  managerUserId?: number;
  notes?: string;
  status?: TeamStatus;
}

export interface CreateTeamMemberRequest {
  teamId: number;
  userId: number;
  role: TeamMemberRole;
  skills?: string[];
  languages?: string[];
  experienceLevel?: ExperienceLevel;
  capacity?: number;
  maxMeetings?: number;
  workingHours?: WorkingHoursItem[];
}

export interface UpdateTeamMemberRequest {
  name?: string;
  email?: string;
  phone?: string;
  role?: TeamMemberRole;
  status?: TeamMemberStatus;
  skills?: string[];
  languages?: string[];
  experienceLevel?: ExperienceLevel;
  capacity?: number;
  maxMeetings?: number;
  workingHours?: WorkingHoursItem[];
}

export interface ChangeManagerRequest {
  newManagerUserId: number;
  previousManagerRole: TeamMemberRole;
}
