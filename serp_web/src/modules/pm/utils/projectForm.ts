/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project form shared helpers
 */

import type {
  PMProjectBlueprintOption,
  PMProjectTemplateDefinition,
  PMProjectVisibilityOption,
} from '../types/project-create.types';
import type { PMProjectTemplateType } from '../types/project-list.types';

export const PM_PROJECT_TEMPLATE_OPTIONS: PMProjectTemplateDefinition[] = [
  {
    type: 'BLANK',
    title: 'Blank software project',
    description:
      'Start with a clean software project and configure the workflow for your team.',
    usageHint: 'Best when the team wants manual setup and minimal defaults.',
    boardBehavior: 'Basic board with minimal default statuses.',
    workflowSummary: 'Clean starting point with no sprint-specific structure.',
    presetSummary: [
      'Basic software workflow',
      'Minimal default statuses',
      'No sprint-specific structure',
    ],
  },
  {
    type: 'KANBAN',
    title: 'Software delivery - Kanban',
    description:
      'Use a continuous flow board for product maintenance, platform work, and ongoing delivery.',
    usageHint:
      'Best for teams that work with continuous prioritization and flow.',
    boardBehavior: 'Board-centric workflow tuned for steady item movement.',
    workflowSummary:
      'Continuous delivery setup with statuses aligned to kanban movement.',
    presetSummary: [
      'Board-centric workflow',
      'Continuous delivery setup',
      'Statuses aligned with kanban movement',
    ],
  },
  {
    type: 'SCRUM',
    title: 'Software delivery - Scrum',
    description:
      'Use sprint-based planning for backlog management, iteration execution, and release cadence.',
    usageHint:
      'Best for sprint planning, backlog ceremonies, and cadence-based delivery.',
    boardBehavior:
      'Sprint-ready board with backlog expectations and planning rhythm.',
    workflowSummary:
      'Agile delivery defaults for backlog, sprint planning, and execution.',
    presetSummary: [
      'Sprint-ready workflow',
      'Backlog and board expectations',
      'Agile cadence defaults',
    ],
  },
];

export const PM_PROJECT_VISIBILITY_OPTIONS: PMProjectVisibilityOption[] = [
  {
    value: 'PRIVATE',
    label: 'Private',
    description: 'Only invited members can access the project workspace.',
  },
  {
    value: 'TEAM',
    label: 'Team',
    description: 'Visible to the owning team and invited collaborators.',
  },
  {
    value: 'ORGANIZATION',
    label: 'Organization',
    description:
      'Visible across the organization for shared delivery tracking.',
  },
];

export function normalizePMProjectKey(value: string) {
  const normalized = value
    .toUpperCase()
    .replace(/[^A-Z0-9]/g, '')
    .slice(0, 10);

  if (!normalized) {
    return '';
  }

  return /^[A-Z]/.test(normalized) ? normalized : `P${normalized}`.slice(0, 10);
}

export function generatePMProjectKey(projectName: string) {
  const words = projectName
    .toUpperCase()
    .replace(/[^A-Z0-9\s]/g, ' ')
    .split(/\s+/)
    .filter(Boolean);

  if (words.length === 0) {
    return '';
  }

  const candidate =
    words.length === 1
      ? words[0]
      : words
          .slice(0, 10)
          .map((word) => word[0])
          .join('');

  return normalizePMProjectKey(candidate);
}

export function getPMProjectTemplateDefinition(
  templateType?: PMProjectTemplateType | null
) {
  return (
    PM_PROJECT_TEMPLATE_OPTIONS.find(
      (template) => template.type === templateType
    ) || null
  );
}

export function matchPMProjectTemplateType(
  blueprintName: string
): PMProjectTemplateType | null {
  const normalizedName = blueprintName.trim().toLowerCase();

  if (normalizedName.includes('kanban')) {
    return 'KANBAN';
  }

  if (normalizedName.includes('scrum')) {
    return 'SCRUM';
  }

  if (normalizedName.includes('blank')) {
    return 'BLANK';
  }

  return null;
}

export function filterAvailablePMProjectTemplates(
  blueprints: Array<{
    id: number;
    name: string;
    avatarUrl?: string | null;
  }>
): PMProjectBlueprintOption[] {
  const blueprintByTemplateType = new Map<
    PMProjectTemplateType,
    PMProjectBlueprintOption
  >();

  blueprints.forEach((blueprint) => {
    const templateType = matchPMProjectTemplateType(blueprint.name);

    if (!templateType || blueprintByTemplateType.has(templateType)) {
      return;
    }

    const definition = getPMProjectTemplateDefinition(templateType);

    if (!definition) {
      return;
    }

    blueprintByTemplateType.set(templateType, {
      ...definition,
      blueprintId: blueprint.id,
      blueprintName: blueprint.name,
      avatarUrl: blueprint.avatarUrl || undefined,
    });
  });

  return PM_PROJECT_TEMPLATE_OPTIONS.filter((template) =>
    blueprintByTemplateType.has(template.type)
  ).map((template) => blueprintByTemplateType.get(template.type)!);
}
