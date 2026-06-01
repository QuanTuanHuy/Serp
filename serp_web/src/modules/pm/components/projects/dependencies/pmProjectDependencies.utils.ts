/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM dependency view helpers
 */

import { MarkerType, type Edge, type Node } from 'reactflow';
import {
  countActiveFilters,
  formatDate,
  parseNumberList,
} from '../../work-items/workItemView.utils';
import type {
  PMWorkItemDependencyEdgeApi,
  PMWorkItemDependencyNodeApi,
} from '../../../types/api';

export { formatDate, parseNumberList };

export function serializeNumberList(values: number[]): string | undefined {
  return values.length ? values.join(',') : undefined;
}

export function parseBooleanToggle(
  value: string | null,
  defaultValue: boolean
): boolean {
  if (value === 'true') return true;
  if (value === 'false') return false;
  return defaultValue;
}

export function parseDepth(value: string | null): number {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 2;
  return Math.min(Math.max(Math.trunc(parsed), 1), 5);
}

export function parseOptionalNumber(value: string | null): number | undefined {
  if (!value) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

export function getDependencyFilterCount(filters: {
  parentId?: number;
  assigneeIds: number[];
  issueTypeIds: number[];
  statusIds: number[];
  priorityIds: number[];
  componentIds: number[];
}): number {
  return countActiveFilters([
    filters.parentId,
    filters.assigneeIds.length,
    filters.issueTypeIds.length,
    filters.statusIds.length,
    filters.priorityIds.length,
    filters.componentIds.length,
  ]);
}

export function getEdgeLabel(edge: PMWorkItemDependencyEdgeApi): string {
  if (edge.relatedLink) {
    return edge.linkTypeName || 'Related';
  }
  if (edge.dependencyBehavior === 'SOURCE_DEPENDS_ON_TARGET') {
    return edge.linkTypeName || 'Depends on';
  }
  return edge.linkTypeName || 'Blocks';
}

export function buildDependencyFlowElements(
  nodes: PMWorkItemDependencyNodeApi[],
  edges: PMWorkItemDependencyEdgeApi[]
): {
  nodes: Node[];
  edges: Edge[];
} {
  const columns = Math.max(1, Math.ceil(Math.sqrt(nodes.length || 1)));
  const flowNodes: Node[] = nodes.map((node, index) => {
    const column = index % columns;
    const row = Math.floor(index / columns);
    return {
      id: String(node.id),
      position: {
        x: column * 260,
        y: row * 150,
      },
      data: {
        label: `${node.key} - ${node.summary}`,
      },
      style: {
        width: 220,
        borderRadius: 8,
        border: node.hasCycle
          ? '1px solid hsl(var(--destructive))'
          : node.outsideFilter
            ? '1px dashed hsl(var(--muted-foreground))'
            : '1px solid hsl(var(--border))',
        background: node.hasCycle
          ? 'hsl(var(--destructive) / 0.08)'
          : node.outsideFilter
            ? 'hsl(var(--muted) / 0.65)'
            : 'hsl(var(--background))',
        color: 'hsl(var(--foreground))',
        fontSize: 12,
      },
    };
  });

  const flowEdges: Edge[] = edges.map((edge) => ({
    id: String(edge.linkId),
    source: String(edge.predecessorId),
    target: String(edge.successorId),
    label: getEdgeLabel(edge),
    animated: edge.cycle,
    markerEnd: {
      type: MarkerType.ArrowClosed,
    },
    style: {
      stroke: edge.cycle
        ? 'hsl(var(--destructive))'
        : edge.relatedLink
          ? 'hsl(var(--muted-foreground))'
          : 'hsl(var(--primary))',
      strokeDasharray: edge.relatedLink ? '5 5' : undefined,
    },
    labelStyle: {
      fill: 'hsl(var(--foreground))',
      fontSize: 11,
    },
  }));

  return { nodes: flowNodes, edges: flowEdges };
}
