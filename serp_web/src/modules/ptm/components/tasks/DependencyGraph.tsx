/**
 * PTM v2 - Dependency Graph Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Network visualization of task dependencies
 */

'use client';

import { useCallback, useMemo, useEffect } from 'react';
import ReactFlow, {
  Node,
  Edge,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  MarkerType,
  Position,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { Card } from '@/shared/components/ui/card';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';

import { useGetTasksQuery } from '../../api';
import type { Task, TaskDependency } from '../../types';
import { getMockTaskDependencies } from '../../mocks/mockData';

interface DependencyGraphProps {
  projectId?: number;
  className?: string;
}

// Custom node component for tasks
function TaskNode({ data }: { data: Task }) {
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH':
        return 'border-red-500 bg-red-50 dark:bg-red-950/30 dark:text-red-100';
      case 'MEDIUM':
        return 'border-yellow-500 bg-yellow-50 dark:bg-yellow-950/30 dark:text-yellow-100';
      case 'LOW':
        return 'border-blue-500 bg-blue-50 dark:bg-blue-950/30 dark:text-blue-100';
      default:
        return 'border-gray-500 bg-gray-50 dark:bg-gray-800 dark:text-gray-100';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DONE':
        return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-300';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300';
      case 'TODO':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700/60 dark:text-gray-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700/60 dark:text-gray-300';
    }
  };

  return (
    <Card
      className={cn(
        'p-3 min-w-[200px] max-w-[250px] border-2',
        getPriorityColor(data.priority)
      )}
    >
      <div className='space-y-2'>
        <div className='font-medium text-sm truncate'>{data.title}</div>
        <div className='flex items-center gap-2 flex-wrap'>
          <Badge
            variant='outline'
            className={cn('text-xs', getStatusColor(data.status))}
          >
            {data.status}
          </Badge>
          <Badge variant='outline' className='text-xs'>
            {data.priority}
          </Badge>
        </div>
        {data.deadlineMs && (
          <div className='text-xs text-muted-foreground'>
            Due: {new Date(data.deadlineMs).toLocaleDateString()}
          </div>
        )}
      </div>
    </Card>
  );
}

const nodeTypes = {
  taskNode: TaskNode,
};

export function DependencyGraph({
  projectId,
  className,
}: DependencyGraphProps) {
  const { data: paginatedData } = useGetTasksQuery(
    projectId ? { projectId } : {}
  );
  const allTasks = paginatedData?.data?.items || [];

  // Get all dependencies for the project/tasks
  // In production, this would be a single API call: GET /api/v1/dependencies?projectId=X
  const allDependencies = useMemo(() => {
    const taskIdSet = new Set(allTasks.map((t) => t.id));
    const allMockDependencies = getMockTaskDependencies();

    const filtered = allMockDependencies.filter(
      (dep: TaskDependency) =>
        taskIdSet.has(dep.taskId) && taskIdSet.has(dep.dependsOnTaskId)
    );

    return filtered;
  }, [allTasks]);

  // Convert tasks to nodes
  const initialNodes: Node[] = useMemo(() => {
    return allTasks.map((task, index) => ({
      id: task.id.toString(),
      type: 'taskNode',
      position: {
        x: (index % 4) * 300,
        y: Math.floor(index / 4) * 150,
      },
      data: task,
      sourcePosition: Position.Right,
      targetPosition: Position.Left,
    }));
  }, [allTasks]);

  // Convert dependencies to edges
  const initialEdges: Edge[] = useMemo(() => {
    return allDependencies.map((dep: TaskDependency) => ({
      id: `e${dep.taskId}-${dep.dependsOnTaskId}`,
      source: dep.dependsOnTaskId.toString(),
      target: dep.taskId.toString(),
      type: 'smoothstep',
      animated: true,
      markerEnd: {
        type: MarkerType.ArrowClosed,
        width: 20,
        height: 20,
      },
      label:
        dep.dependencyType === 'FINISH_TO_START' ? 'FS' : dep.dependencyType,
      style: { stroke: '#64748b', strokeWidth: 2 },
      labelStyle: { fill: '#64748b', fontWeight: 600 },
      labelBgStyle: { fill: 'white', fillOpacity: 0.8 },
    }));
  }, [allDependencies]);

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  // Auto-layout algorithm (simple dagre-like layout)
  const autoLayout = useCallback(() => {
    const levelMap = new Map<string, number>();
    const visited = new Set<string>();

    // Calculate levels using BFS
    const calculateLevel = (nodeId: string, level: number = 0) => {
      if (visited.has(nodeId)) return;
      visited.add(nodeId);

      const currentLevel = levelMap.get(nodeId) || 0;
      levelMap.set(nodeId, Math.max(currentLevel, level));

      // Find all nodes that depend on this node
      const dependentEdges = initialEdges.filter((e) => e.source === nodeId);
      dependentEdges.forEach((edge) => {
        calculateLevel(edge.target, level + 1);
      });
    };

    // Start from root nodes (nodes with no dependencies)
    const rootNodes = initialNodes.filter(
      (node) => !initialEdges.some((edge) => edge.target === node.id)
    );

    rootNodes.forEach((node) => calculateLevel(node.id));

    // Position nodes based on levels
    const levelCounts = new Map<number, number>();
    const layoutedNodes = initialNodes.map((node) => {
      const level = levelMap.get(node.id) || 0;
      const countInLevel = levelCounts.get(level) || 0;
      levelCounts.set(level, countInLevel + 1);

      return {
        ...node,
        position: {
          x: level * 300,
          y: countInLevel * 150,
        },
      };
    });

    setNodes(layoutedNodes);
  }, [initialNodes, initialEdges, setNodes]);

  // Sync edges when initialEdges changes
  useEffect(() => {
    setEdges(initialEdges);
  }, [initialEdges, setEdges]);

  // Sync nodes when initialNodes changes
  useEffect(() => {
    if (initialNodes.length > 0) {
      autoLayout();
    }
  }, [initialNodes.length, autoLayout]);

  if (allTasks.length === 0) {
    return (
      <Card className='p-12 text-center'>
        <p className='text-muted-foreground'>No tasks to visualize</p>
        <p className='text-sm text-muted-foreground mt-2'>
          Create tasks and add dependencies to see the graph
        </p>
      </Card>
    );
  }

  return (
    <div className={cn('h-[600px] w-full border rounded-lg', className)}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        nodeTypes={nodeTypes}
        fitView
        attributionPosition='bottom-left'
      >
        <Background />
        <Controls />
        <MiniMap
          nodeColor={(node) => {
            const task = node.data as Task;
            switch (task.status) {
              case 'DONE':
                return '#10b981';
              case 'IN_PROGRESS':
                return '#3b82f6';
              default:
                return '#94a3b8';
            }
          }}
        />
      </ReactFlow>
    </div>
  );
}
