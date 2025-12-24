/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import { ProjectFormDialog } from './ProjectFormDialog';

interface CreateProjectDialogProps {
  trigger?: React.ReactNode;
}

export function CreateProjectDialog({ trigger }: CreateProjectDialogProps) {
  return <ProjectFormDialog mode='create' trigger={trigger} />;
}
