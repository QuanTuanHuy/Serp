/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Note type definition
 */

export interface Note {
  id: string;
  tenantId: string;
  entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
  entityId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface CreateNoteRequest {
  entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
  entityId: number;
  content: string;
}

export interface UpdateNoteRequest {
  content: string;
}
