// Base CRM Types (authors: QuanTuanHuy, Description: Part of Serp Project)

export type ActiveStatus = 'ACTIVE' | 'INACTIVE';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}
