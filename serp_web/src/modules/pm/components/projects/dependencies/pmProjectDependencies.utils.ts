/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM dependency view helpers
 */

import { MarkerType, Position, type Edge, type Node } from 'reactflow';
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
      sourcePosition: Position.Right,
      targetPosition: Position.Left,
      style: {
        width: 220,
        borderRadius: 8,
        border: node.hasCycle
          ? '1px solid var(--destructive)'
          : node.outsideFilter
            ? '1px dashed var(--muted-foreground)'
            : '1px solid var(--border)',
        background: node.hasCycle
          ? 'color-mix(in oklch, var(--destructive) 10%, var(--background))'
          : node.outsideFilter
            ? 'var(--muted)'
            : 'var(--background)',
        color: 'var(--foreground)',
        fontSize: 12,
      },
    };
  });

  const flowEdges: Edge[] = edges.map((edge) => {
    const stroke = edge.cycle
      ? 'var(--destructive)'
      : edge.relatedLink
        ? 'var(--muted-foreground)'
        : 'var(--primary)';

    return {
      id: String(edge.linkId),
      source: String(edge.predecessorId),
      target: String(edge.successorId),
      type: 'smoothstep',
      label: getEdgeLabel(edge),
      animated: edge.cycle,
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color: stroke,
        width: 20,
        height: 20,
      },
      style: {
        stroke,
        strokeWidth: 2,
        strokeDasharray: edge.relatedLink ? '5 5' : undefined,
      },
      labelStyle: {
        fill: 'var(--foreground)',
        fontSize: 11,
        fontWeight: 600,
      },
      labelBgStyle: {
        fill: 'var(--background)',
        fillOpacity: 0.85,
      },
    };
  });

  return { nodes: flowNodes, edges: flowEdges };
}
