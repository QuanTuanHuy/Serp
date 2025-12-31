/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import { ProjectFormDialog } from './ProjectFormDialog';
import type { Project } from '../../types';

interface EditProjectDialogProps {
  project: Project;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function EditProjectDialog({
  project,
  open,
  onOpenChange,
}: EditProjectDialogProps) {
  return (
    <ProjectFormDialog
      mode='edit'
      project={project}
      open={open}
      onOpenChange={onOpenChange}
    />
  );
}
