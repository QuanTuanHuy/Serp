/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project list view model types
 */

export type PMProjectTemplateType = 'BLANK' | 'KANBAN' | 'SCRUM';

export type PMProjectStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';

export type PMProjectSort = 'recentlyUpdated' | 'name' | 'createdDate';

export type PMProjectViewMode = 'list' | 'grid';

export interface PMProjectLead {
  id: string;
  name: string;
  avatarUrl?: string;
}

export interface PMProjectListItem {
  id: string;
  name: string;
  key: string;
  description: string;
  templateType?: PMProjectTemplateType;
  category: string;
  status: PMProjectStatus;
  lead: PMProjectLead;
  updatedAt: string;
  createdAt: string;
}
