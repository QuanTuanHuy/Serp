/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM dependency graph
 */

'use client';

import { useMemo } from 'react';
import ReactFlow, { Background, Controls, MiniMap, type Node } from 'reactflow';
import 'reactflow/dist/style.css';
import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import type {
  PMWorkItemDependencyEdgeApi,
  PMWorkItemDependencyNodeApi,
} from '../../../types/api';
import { buildDependencyFlowElements } from './pmProjectDependencies.utils';

interface PMProjectDependenciesGraphProps {
  nodes: PMWorkItemDependencyNodeApi[];
  edges: PMWorkItemDependencyEdgeApi[];
  onOpenWorkItem: (workItemId: number) => void;
}

export function PMProjectDependenciesGraph({
  nodes,
  edges,
  onOpenWorkItem,
}: PMProjectDependenciesGraphProps) {
  const { nodes: flowNodes, edges: flowEdges } = useMemo(
    () => buildDependencyFlowElements(nodes, edges),
    [edges, nodes]
  );

  const handleNodeClick = (_event: unknown, node: Node) => {
    const workItemId = Number(node.id);
    if (Number.isFinite(workItemId)) {
      onOpenWorkItem(workItemId);
    }
  };

  return (
    <Card className='shadow-sm'>
      <CardHeader className='flex flex-row items-center justify-between gap-3'>
        <CardTitle className='text-base'>Graph</CardTitle>
        <div className='flex flex-wrap items-center gap-2 text-xs text-muted-foreground'>
          <Badge variant='secondary'>Cycles highlighted</Badge>
          <Badge variant='secondary'>Outside nodes dimmed</Badge>
          <Badge variant='secondary'>Related links dashed</Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div className='h-[640px] overflow-hidden rounded-md border bg-muted/10'>
          <ReactFlow
            nodes={flowNodes}
            edges={flowEdges}
            fitView
            nodesDraggable={false}
            nodesConnectable={false}
            elementsSelectable={false}
            onNodeClick={handleNodeClick}
          >
            <Background gap={16} />
            <Controls />
            <MiniMap zoomable pannable />
          </ReactFlow>
        </div>
      </CardContent>
    </Card>
  );
}
