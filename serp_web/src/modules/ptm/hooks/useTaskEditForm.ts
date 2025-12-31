/**
 * PTM - Task Edit Form Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Manage task edit form state
 */

'use client';

import { useState } from 'react';
import type { Task } from '../types';

export function useTaskEditForm(task: Task | null | undefined) {
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState<Partial<Task>>({});

  /**
   * Start editing - populate form with current task data
   */
  const startEdit = () => {
    if (!task) return;

    setEditForm({
      title: task.title,
      description: task.description,
      priority: task.priority,
      estimatedDurationMin: task.estimatedDurationMin,
    });
    setIsEditing(true);
  };

  /**
   * Cancel editing - reset form and exit edit mode
   */
  const cancelEdit = () => {
    setEditForm({});
    setIsEditing(false);
  };

  /**
   * Update a single field in edit form
   */
  const updateField = <K extends keyof Task>(field: K, value: Task[K]) => {
    setEditForm((prev) => ({ ...prev, [field]: value }));
  };

  return {
    isEditing,
    editForm,
    startEdit,
    cancelEdit,
    updateField,
    setEditForm,
    setIsEditing,
  };
}
