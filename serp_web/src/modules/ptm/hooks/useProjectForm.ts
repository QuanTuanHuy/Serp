/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useState, useCallback, useEffect } from 'react';
import {
  useCreateProjectMutation,
  useUpdateProjectMutation,
} from '../services/projectApi';
import type { ProjectPriority, ProjectStatus, Project } from '../types';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib';

interface UseProjectFormOptions {
  mode: 'create' | 'edit';
  project?: Project;
  onSuccess?: () => void;
}

const PROJECT_COLORS = [
  { value: '#3b82f6', label: 'Blue' },
  { value: '#8b5cf6', label: 'Purple' },
  { value: '#ec4899', label: 'Pink' },
  { value: '#f59e0b', label: 'Amber' },
  { value: '#10b981', label: 'Green' },
  { value: '#ef4444', label: 'Red' },
  { value: '#6366f1', label: 'Indigo' },
  { value: '#14b8a6', label: 'Teal' },
];

export function useProjectForm({
  mode,
  project,
  onSuccess,
}: UseProjectFormOptions) {
  const [title, setTitle] = useState(project?.title || '');
  const [description, setDescription] = useState(project?.description || '');
  const [priority, setPriority] = useState<ProjectPriority>(
    project?.priority || 'MEDIUM'
  );
  const [status, setStatus] = useState<ProjectStatus>(project?.status || 'NEW');
  const [color, setColor] = useState(project?.color || PROJECT_COLORS[0].value);
  const [deadline, setDeadline] = useState(
    project?.deadlineMs
      ? new Date(project.deadlineMs).toISOString().split('T')[0]
      : ''
  );
  const [isCalendarOpen, setIsCalendarOpen] = useState(false);

  const [createProject, { isLoading: isCreating }] = useCreateProjectMutation();
  const [updateProject, { isLoading: isUpdating }] = useUpdateProjectMutation();

  const isLoading = isCreating || isUpdating;

  useEffect(() => {
    if (mode === 'edit' && project) {
      setTitle(project.title);
      setDescription(project.description || '');
      setPriority(project.priority);
      setStatus(project.status);
      setColor(project.color || PROJECT_COLORS[0].value);
      setDeadline(
        project.deadlineMs
          ? new Date(project.deadlineMs).toISOString().split('T')[0]
          : ''
      );
      setIsCalendarOpen(false);
    }
  }, [mode, project]);

  const resetForm = useCallback(() => {
    setTitle('');
    setDescription('');
    setPriority('MEDIUM');
    setStatus('NEW');
    setColor(PROJECT_COLORS[0].value);
    setDeadline('');
    setIsCalendarOpen(false);
  }, []);

  const handleSubmit = useCallback(
    async (e?: React.FormEvent) => {
      e?.preventDefault();

      if (!title.trim()) {
        toast.error('Project title is required');
        return false;
      }

      try {
        const payload = {
          title: title.trim(),
          description: description.trim() || undefined,
          priority,
          color,
          deadlineMs: deadline ? new Date(deadline).getTime() : undefined,
        };

        if (mode === 'create') {
          await createProject(payload).unwrap();
          toast.success('Project created successfully');
          resetForm();
        } else if (mode === 'edit' && project) {
          await updateProject({
            id: project.id,
            ...payload,
            status,
          }).unwrap();
          toast.success('Project updated successfully');
        }

        onSuccess?.();
        return true;
      } catch (error) {
        toast.error(
          getErrorMessage(error) || 'An error occurred. Please try again.'
        );
        return false;
      }
    },
    [
      title,
      description,
      priority,
      status,
      color,
      deadline,
      mode,
      project,
      createProject,
      updateProject,
      resetForm,
      onSuccess,
    ]
  );

  return {
    title,
    setTitle,
    description,
    setDescription,
    priority,
    setPriority,
    status,
    setStatus,
    color,
    setColor,
    deadline,
    setDeadline,
    isCalendarOpen,
    setIsCalendarOpen,
    isLoading,
    handleSubmit,
    resetForm,
    colors: PROJECT_COLORS,
  };
}
