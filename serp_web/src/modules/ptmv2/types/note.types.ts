/**
 * PTM v2 - Note Type Definitions
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Note domain types
 */

export interface Note {
  id: string;
  userId: string;
  tenantId: string;
  taskId?: string;
  projectId?: string;

  content: string; // Markdown formatted
  contentPlain: string; // Plain text for search
  attachments: NoteAttachment[];

  isPinned: boolean;
  activeStatus: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

export interface NoteAttachment {
  name: string;
  url: string;
  size: number;
  type: string;
}

export interface CreateNoteRequest {
  taskId?: string;
  projectId?: string;
  content: string;
  isPinned?: boolean;
}

export interface UpdateNoteRequest {
  id: string;
  content?: string;
  isPinned?: boolean;
}
