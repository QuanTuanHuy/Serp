/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import { useMemo } from 'react';
import { useGetProjectsQuery } from '../api';

export function useProjectStats() {
  const { data: response, isLoading } = useGetProjectsQuery({ pageSize: 100 });

  const projects = response?.data.items || [];

  const stats = useMemo(() => {
    const activeProjects = projects.filter(
      (p) => p.status === 'NEW' || p.status === 'IN_PROGRESS'
    ).length;

    const completedProjects = projects.filter(
      (p) => p.status === 'COMPLETED'
    ).length;

    const totalHours = projects.reduce(
      (sum, p) => sum + (p.estimatedHours || 0),
      0
    );

    const onHoldProjects = projects.filter(
      (p) => p.status === 'ON_HOLD'
    ).length;

    const archivedProjects = projects.filter(
      (p) => p.status === 'ARCHIVED'
    ).length;

    const highPriorityProjects = projects.filter(
      (p) => p.priority === 'HIGH'
    ).length;

    return {
      total: projects.length,
      active: activeProjects,
      completed: completedProjects,
      onHold: onHoldProjects,
      archived: archivedProjects,
      highPriority: highPriorityProjects,
      totalHours,
    };
  }, [projects]);

  return {
    stats,
    isLoading,
  };
}
