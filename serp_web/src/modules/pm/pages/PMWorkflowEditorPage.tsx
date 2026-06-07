/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM workflow editor page
 */

'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  GitBranch,
  Plus,
  Send,
  Trash2,
} from 'lucide-react';
import ReactFlow, {
  Background,
  Controls,
  MarkerType,
  MiniMap,
  Position,
  type Edge,
  type Node,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { toast } from 'sonner';

import { getErrorMessage } from '@/lib/store/api';
import { Alert, AlertDescription } from '@/shared/components/ui/alert';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Checkbox } from '@/shared/components/ui/checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Skeleton } from '@/shared/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';

import {
  useAddPmWorkflowStepMutation,
  useAddPmWorkflowTransitionMutation,
  useCreatePmStatusMutation,
  useGetPmStatusCategoriesQuery,
  useGetPmStatusesQuery,
  useGetPmWorkflowEditorQuery,
  usePublishPmWorkflowMutation,
  useRemovePmWorkflowStepMutation,
  useRemovePmWorkflowTransitionMutation,
  useReorderPmWorkflowStepsMutation,
  useUpdatePmWorkflowMutation,
  useUpdatePmWorkflowTransitionMutation,
  useValidatePmWorkflowMutation,
} from '../api';
import type {
  PMStatusApi,
  PMStatusCategoryApi,
  PMWorkflowStepApi,
  PMWorkflowTransitionApi,
  PMWorkflowValidationApi,
} from '../types/api';

const ANY_STEP_VALUE = '__any__';
const STATUS_PICKER_PAGE_SIZE = 100;

export function PMWorkflowEditorPage({ workflowId }: { workflowId: number }) {
  const router = useRouter();
  const [tab, setTab] = useState('diagram');
  const [showLabels, setShowLabels] = useState(true);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [stepDialogOpen, setStepDialogOpen] = useState(false);
  const [transitionDialog, setTransitionDialog] =
    useState<TransitionDialogState>(null);
  const [validation, setValidation] = useState<PMWorkflowValidationApi | null>(
    null
  );

  const editorQuery = useGetPmWorkflowEditorQuery(workflowId);
  const statusesQuery = useGetPmStatusesQuery({
    page: 0,
    pageSize: STATUS_PICKER_PAGE_SIZE,
    sortBy: 'name',
    sortDirection: 'asc',
    isSystem: false,
  });
  const statusCategoriesQuery = useGetPmStatusCategoriesQuery({
    page: 0,
    pageSize: STATUS_PICKER_PAGE_SIZE,
    sortBy: 'name',
    isSystem: false,
    sortDirection: 'asc',
  });
  const [addStep, addStepState] = useAddPmWorkflowStepMutation();
  const [createStatus, createStatusState] = useCreatePmStatusMutation();
  const [removeStep, removeStepState] = useRemovePmWorkflowStepMutation();
  const [reorderSteps, reorderStepsState] = useReorderPmWorkflowStepsMutation();
  const [addTransition, addTransitionState] =
    useAddPmWorkflowTransitionMutation();
  const [updateTransition, updateTransitionState] =
    useUpdatePmWorkflowTransitionMutation();
  const [removeTransition, removeTransitionState] =
    useRemovePmWorkflowTransitionMutation();
  const [validateWorkflow, validateWorkflowState] =
    useValidatePmWorkflowMutation();
  const [publishWorkflow, publishWorkflowState] =
    usePublishPmWorkflowMutation();
  const [updateWorkflow, updateWorkflowState] = useUpdatePmWorkflowMutation();

  const editor = editorQuery.data;
  const workflow = editor?.workflow;
  const steps = editor?.steps ?? [];
  const transitions = editor?.transitions ?? [];
  const statuses = statusesQuery.data?.data.items ?? [];
  const statusCategories = statusCategoriesQuery.data?.data.items ?? [];
  const editable = Boolean(workflow && !workflow.readOnly);
  const isSaving =
    updateWorkflowState.isLoading ||
    createStatusState.isLoading ||
    addStepState.isLoading ||
    removeStepState.isLoading ||
    reorderStepsState.isLoading ||
    addTransitionState.isLoading ||
    updateTransitionState.isLoading ||
    removeTransitionState.isLoading ||
    validateWorkflowState.isLoading ||
    publishWorkflowState.isLoading;

  const stepById = useMemo(
    () => new Map(steps.map((step) => [step.id, step])),
    [steps]
  );
  const usedStatusIds = useMemo(
    () => new Set(steps.map((step) => step.statusId)),
    [steps]
  );
  const availableStatuses = useMemo(
    () => statuses.filter((status) => !usedStatusIds.has(status.id)),
    [statuses, usedStatusIds]
  );

  const graph = useMemo(
    () => buildWorkflowGraph(steps, transitions, showLabels),
    [showLabels, steps, transitions]
  );

  const ensureDraft = useCallback(async () => {
    if (!workflow || !editor) {
      return null;
    }
    if (!editable) {
      toast.error('This workflow is read-only.');
      return null;
    }
    if (workflow.draftVersionId) {
      return editor;
    }

    try {
      await updateWorkflow({
        id: workflowId,
        body: {
          name: workflow.name,
          description: workflow.description ?? null,
        },
      }).unwrap();
      const refreshed = await editorQuery.refetch().unwrap();
      setValidation(null);
      toast.success('Workflow draft created.');
      return refreshed;
    } catch (error) {
      toast.error('Unable to create workflow draft', {
        description: getErrorMessage(error),
      });
      return null;
    }
  }, [editable, editor, editorQuery, updateWorkflow, workflow, workflowId]);

  const handleValidate = useCallback(async () => {
    const draftEditor = await ensureDraft();
    if (!draftEditor) {
      return null;
    }

    try {
      const result = await validateWorkflow(workflowId).unwrap();
      setValidation(result);
      if (result.valid) {
        toast.success('Workflow validation passed.');
      } else {
        toast.error('Workflow validation failed.');
      }
      return result;
    } catch (error) {
      toast.error('Unable to validate workflow', {
        description: getErrorMessage(error),
      });
      return null;
    }
  }, [ensureDraft, validateWorkflow, workflowId]);

  const handlePublish = useCallback(async () => {
    const result = await handleValidate();
    if (!result?.valid) {
      return;
    }
    try {
      await publishWorkflow(workflowId).unwrap();
      toast.success('Workflow published.');
      setValidation(null);
    } catch (error) {
      toast.error('Unable to publish workflow', {
        description: getErrorMessage(error),
      });
    }
  }, [handleValidate, publishWorkflow, workflowId]);

  const handleOpenAddStepDialog = useCallback(async () => {
    const draftEditor = await ensureDraft();
    if (!draftEditor) {
      return;
    }
    setStepDialogOpen(true);
  }, [ensureDraft]);

  const handleOpenAddTransitionDialog = useCallback(async () => {
    const draftEditor = await ensureDraft();
    if (!draftEditor || draftEditor.steps.length === 0) {
      return;
    }
    setTransitionDialog({ mode: 'create' });
  }, [ensureDraft]);

  const handleAddStep = useCallback(
    async (values: AddStepValues) => {
      const draftEditor = await ensureDraft();
      if (!draftEditor) {
        return;
      }

      try {
        const statusId =
          values.mode === 'existing'
            ? values.statusId
            : (
                await createStatus({
                  statusKey: generateStatusKey(values.name),
                  name: values.name.trim(),
                  description: null,
                  iconUrl: null,
                  statusCategoryId: values.statusCategoryId,
                }).unwrap()
              ).id;

        await addStep({
          workflowId,
          body: {
            statusId,
            isInitial: values.isInitial,
            isTerminal: values.isTerminal,
          },
        }).unwrap();
        await Promise.all([
          editorQuery.refetch().unwrap(),
          statusesQuery.refetch().unwrap(),
        ]);
        setStepDialogOpen(false);
        setValidation(null);
        toast.success(
          values.mode === 'new' ? 'Status created and added.' : 'Status added.'
        );
      } catch (error) {
        toast.error('Unable to add status', {
          description: getErrorMessage(error),
        });
      }
    },
    [addStep, createStatus, editorQuery, ensureDraft, statusesQuery, workflowId]
  );

  const handleRemoveStep = useCallback(
    async (step: PMWorkflowStepApi) => {
      if (!window.confirm(`Remove ${step.name} from this workflow?`)) {
        return;
      }
      const draftEditor = await ensureDraft();
      const draftStep = draftEditor
        ? findMatchingStep(draftEditor.steps, step)
        : null;
      if (!draftStep) {
        toast.error('Unable to locate the workflow draft status.');
        return;
      }

      try {
        await removeStep({ workflowId, stepId: draftStep.id }).unwrap();
        await editorQuery.refetch().unwrap();
        setValidation(null);
        toast.success('Status removed from workflow.');
      } catch (error) {
        toast.error('Unable to remove status', {
          description: getErrorMessage(error),
        });
      }
    },
    [editorQuery, ensureDraft, removeStep, workflowId]
  );

  const handleMoveStep = useCallback(
    async (step: PMWorkflowStepApi, direction: 'up' | 'down') => {
      const draftEditor = await ensureDraft();
      if (!draftEditor) {
        return;
      }

      const draftSteps = draftEditor.steps;
      const draftStep = findMatchingStep(draftSteps, step);
      if (!draftStep) {
        toast.error('Unable to locate the workflow draft status.');
        return;
      }

      const index = draftSteps.findIndex(
        (candidate) => candidate.id === draftStep.id
      );
      const targetIndex = direction === 'up' ? index - 1 : index + 1;
      if (index < 0 || targetIndex < 0 || targetIndex >= draftSteps.length) {
        return;
      }
      const next = [...draftSteps];
      [next[index], next[targetIndex]] = [next[targetIndex], next[index]];
      try {
        await reorderSteps({
          workflowId,
          body: { stepIds: next.map((item) => item.id) },
        }).unwrap();
        await editorQuery.refetch().unwrap();
        toast.success('Status order updated.');
      } catch (error) {
        toast.error('Unable to reorder statuses', {
          description: getErrorMessage(error),
        });
      }
    },
    [editorQuery, ensureDraft, reorderSteps, workflowId]
  );

  const handleSaveTransition = useCallback(
    async (values: TransitionValues, item?: PMWorkflowTransitionApi) => {
      const draftEditor = await ensureDraft();
      if (!draftEditor) {
        return;
      }
      const draftItem = item
        ? findMatchingTransition(
            draftEditor.transitions,
            item,
            stepById,
            draftEditor.steps
          )
        : undefined;
      if (item && !draftItem) {
        toast.error('Unable to locate the workflow draft transition.');
        return;
      }

      try {
        if (draftItem) {
          await updateTransition({
            workflowId,
            transitionId: draftItem.id,
            body: {
              name: values.name,
              screenId: null,
              sequence: values.sequence,
            },
          }).unwrap();
          await editorQuery.refetch().unwrap();
          toast.success('Transition updated.');
        } else {
          await addTransition({
            workflowId,
            body: {
              name: values.name,
              fromStepId: values.fromStepId,
              toStepId: values.toStepId,
              screenId: null,
              sequence: values.sequence,
            },
          }).unwrap();
          await editorQuery.refetch().unwrap();
          toast.success('Transition added.');
        }
        setTransitionDialog(null);
        setValidation(null);
      } catch (error) {
        toast.error('Unable to save transition', {
          description: getErrorMessage(error),
        });
      }
    },
    [
      addTransition,
      editorQuery,
      ensureDraft,
      stepById,
      updateTransition,
      workflowId,
    ]
  );

  const handleRemoveTransition = useCallback(
    async (transition: PMWorkflowTransitionApi) => {
      if (!window.confirm(`Remove transition ${transition.name}?`)) {
        return;
      }
      const draftEditor = await ensureDraft();
      const draftTransition = draftEditor
        ? findMatchingTransition(
            draftEditor.transitions,
            transition,
            stepById,
            draftEditor.steps
          )
        : null;
      if (!draftTransition) {
        toast.error('Unable to locate the workflow draft transition.');
        return;
      }

      try {
        await removeTransition({
          workflowId,
          transitionId: draftTransition.id,
        }).unwrap();
        await editorQuery.refetch().unwrap();
        setValidation(null);
        toast.success('Transition removed.');
      } catch (error) {
        toast.error('Unable to remove transition', {
          description: getErrorMessage(error),
        });
      }
    },
    [editorQuery, ensureDraft, removeTransition, stepById, workflowId]
  );

  const handleEditTransition = useCallback(
    async (transition: PMWorkflowTransitionApi) => {
      const draftEditor = await ensureDraft();
      const draftTransition = draftEditor
        ? findMatchingTransition(
            draftEditor.transitions,
            transition,
            stepById,
            draftEditor.steps
          )
        : null;
      if (!draftTransition) {
        toast.error('Unable to locate the workflow draft transition.');
        return;
      }
      setTransitionDialog({ mode: 'edit', item: draftTransition });
    },
    [ensureDraft, stepById]
  );

  if (Number.isNaN(workflowId)) {
    return <EditorState message='Invalid workflow id.' />;
  }

  if (editorQuery.isLoading) {
    return <EditorSkeleton />;
  }

  if (editorQuery.error || !editor || !workflow) {
    return (
      <EditorState
        message={
          editorQuery.error
            ? getErrorMessage(editorQuery.error)
            : 'Workflow editor is unavailable.'
        }
      />
    );
  }

  return (
    <main className='flex min-h-screen flex-col bg-background text-foreground'>
      <header className='border-b px-4 py-3 lg:px-6'>
        <div className='flex flex-wrap items-center justify-between gap-3'>
          <div className='flex min-w-0 items-center gap-3'>
            <Button
              type='button'
              variant='ghost'
              size='icon'
              aria-label='Back to workflow settings'
              onClick={() => router.push('/pm/settings?section=workflows')}
            >
              <ArrowLeft className='h-4 w-4' />
            </Button>
            <div className='min-w-0'>
              <div className='flex flex-wrap items-center gap-2'>
                <h1 className='truncate text-xl font-semibold'>
                  {workflow.name}
                </h1>
                <Badge variant={editable ? 'default' : 'secondary'}>
                  {editor.versionState}
                </Badge>
                {!editable ? <Badge variant='outline'>Read-only</Badge> : null}
              </div>
              <p className='truncate text-sm text-muted-foreground'>
                {workflow.description || workflow.workflowKey}
              </p>
            </div>
          </div>
          <div className='flex flex-wrap items-center gap-2'>
            <Button
              type='button'
              variant='outline'
              onClick={handleValidate}
              disabled={!editable || isSaving}
            >
              <CheckCircle2 className='mr-2 h-4 w-4' />
              Validate
            </Button>
            <Button
              type='button'
              onClick={handlePublish}
              disabled={!editable || isSaving}
            >
              <Send className='mr-2 h-4 w-4' />
              Publish
            </Button>
          </div>
        </div>
      </header>

      <section className='border-b px-4 py-3 lg:px-6'>
        <div className='flex flex-wrap items-center justify-between gap-3'>
          <Tabs value={tab} onValueChange={setTab} className='w-auto'>
            <TabsList>
              <TabsTrigger value='diagram'>Diagram</TabsTrigger>
              <TabsTrigger value='text'>Text</TabsTrigger>
            </TabsList>
          </Tabs>
          <div className='flex flex-wrap items-center gap-2'>
            <Button
              type='button'
              variant='outline'
              onClick={() => setShowLabels((current) => !current)}
            >
              {showLabels ? 'Hide labels' : 'Show labels'}
            </Button>
            <Button
              type='button'
              variant='outline'
              onClick={handleOpenAddStepDialog}
              disabled={!editable || isSaving}
            >
              <Plus className='mr-2 h-4 w-4' />
              Add status
            </Button>
            <Button
              type='button'
              onClick={handleOpenAddTransitionDialog}
              disabled={!editable || steps.length === 0 || isSaving}
            >
              <GitBranch className='mr-2 h-4 w-4' />
              Add transition
            </Button>
          </div>
        </div>
      </section>

      {validation ? <ValidationPanel validation={validation} /> : null}

      <Tabs value={tab} onValueChange={setTab} className='min-h-0 flex-1'>
        <TabsContent value='diagram' className='m-0 h-[calc(100vh-12rem)]'>
          <div className='grid h-full min-h-[560px] grid-cols-1 lg:grid-cols-[minmax(0,1fr)_320px]'>
            <div className='min-h-0 border-b lg:border-b-0 lg:border-r'>
              <ReactFlow
                nodes={graph.nodes}
                edges={graph.edges}
                nodesDraggable={false}
                nodesConnectable={false}
                elementsSelectable
                fitView
                onNodeClick={(_event, node) => setSelectedId(node.id)}
                onEdgeClick={(_event, edge) => setSelectedId(edge.id)}
              >
                <Background gap={24} size={1} />
                <Controls />
                <MiniMap pannable zoomable />
              </ReactFlow>
            </div>
            <WorkflowInspector
              selectedId={selectedId}
              steps={steps}
              transitions={transitions}
              stepById={stepById}
              editable={editable}
              onEditTransition={handleEditTransition}
              onRemoveTransition={handleRemoveTransition}
            />
          </div>
        </TabsContent>
        <TabsContent value='text' className='m-0'>
          <WorkflowTextView
            editable={editable}
            steps={steps}
            transitions={transitions}
            stepById={stepById}
            onMoveStep={handleMoveStep}
            onRemoveStep={handleRemoveStep}
            onEditTransition={handleEditTransition}
            onRemoveTransition={handleRemoveTransition}
          />
        </TabsContent>
      </Tabs>

      <AddStepDialog
        open={stepDialogOpen}
        statuses={availableStatuses}
        statusCategories={statusCategories}
        isSubmitting={addStepState.isLoading || createStatusState.isLoading}
        onOpenChange={setStepDialogOpen}
        onSubmit={handleAddStep}
      />
      <TransitionDialog
        state={transitionDialog}
        steps={steps}
        isSubmitting={
          addTransitionState.isLoading || updateTransitionState.isLoading
        }
        onOpenChange={(open) => {
          if (!open) {
            setTransitionDialog(null);
          }
        }}
        onSubmit={handleSaveTransition}
      />
    </main>
  );
}

function buildWorkflowGraph(
  steps: PMWorkflowStepApi[],
  transitions: PMWorkflowTransitionApi[],
  showLabels: boolean
): { nodes: Node[]; edges: Edge[] } {
  const hasAnyStart = transitions.some((transition) => !transition.fromStepId);
  const nodes: Node[] = steps.map((step, index) => ({
    id: `step-${step.id}`,
    type: 'default',
    position: { x: 240 * index + 80, y: step.isInitial ? 150 : 220 },
    sourcePosition: Position.Right,
    targetPosition: Position.Left,
    data: {
      label: (
        <div className='min-w-36 text-center'>
          <div className='font-semibold'>{step.name}</div>
          <div className='mt-1 text-[10px] uppercase text-muted-foreground'>
            {step.isInitial ? 'Initial' : step.isTerminal ? 'Terminal' : 'Step'}
          </div>
        </div>
      ),
    },
    style: {
      borderColor: step.isTerminal ? 'hsl(var(--success))' : undefined,
      borderWidth: step.isInitial || step.isTerminal ? 2 : 1,
    },
  }));

  if (hasAnyStart) {
    nodes.unshift({
      id: 'step-any',
      type: 'input',
      position: { x: 0, y: 90 },
      sourcePosition: Position.Right,
      data: { label: 'Any status' },
    });
  }

  const edges: Edge[] = transitions.map((transition) => ({
    id: `transition-${transition.id}`,
    source: transition.fromStepId
      ? `step-${transition.fromStepId}`
      : 'step-any',
    target: `step-${transition.toStepId}`,
    label: showLabels ? transition.name : undefined,
    markerEnd: { type: MarkerType.ArrowClosed },
    animated: !transition.fromStepId,
  }));

  return { nodes, edges };
}

function WorkflowInspector({
  selectedId,
  steps,
  transitions,
  stepById,
  editable,
  onEditTransition,
  onRemoveTransition,
}: {
  selectedId: string | null;
  steps: PMWorkflowStepApi[];
  transitions: PMWorkflowTransitionApi[];
  stepById: Map<number, PMWorkflowStepApi>;
  editable: boolean;
  onEditTransition: (transition: PMWorkflowTransitionApi) => void;
  onRemoveTransition: (transition: PMWorkflowTransitionApi) => void;
}) {
  const selectedStep = selectedId?.startsWith('step-')
    ? steps.find((step) => `step-${step.id}` === selectedId)
    : undefined;
  const selectedTransition = selectedId?.startsWith('transition-')
    ? transitions.find(
        (transition) => `transition-${transition.id}` === selectedId
      )
    : undefined;

  return (
    <aside className='min-h-0 overflow-y-auto p-4'>
      <div className='space-y-5'>
        <div>
          <h2 className='text-sm font-semibold'>Workflow summary</h2>
          <div className='mt-3 grid grid-cols-2 gap-2 text-sm'>
            <Metric label='Statuses' value={steps.length} />
            <Metric label='Transitions' value={transitions.length} />
          </div>
        </div>
        <div className='rounded-md border p-3'>
          {selectedStep ? (
            <div className='space-y-2'>
              <h3 className='font-medium'>{selectedStep.name}</h3>
              <p className='text-sm text-muted-foreground'>
                Status ID {selectedStep.statusId}, order{' '}
                {selectedStep.stepOrder ?? '-'}
              </p>
              <Badge variant='outline'>
                {selectedStep.isInitial
                  ? 'Initial'
                  : selectedStep.isTerminal
                    ? 'Terminal'
                    : 'Workflow status'}
              </Badge>
            </div>
          ) : selectedTransition ? (
            <div className='space-y-2'>
              <h3 className='font-medium'>{selectedTransition.name}</h3>
              <p className='text-sm text-muted-foreground'>
                {formatStepName(stepById, selectedTransition.fromStepId)}{' '}
                <ArrowRight className='inline h-3.5 w-3.5' />{' '}
                {formatStepName(stepById, selectedTransition.toStepId)}
              </p>
              <Badge variant='outline'>
                Sequence {selectedTransition.sequence ?? '-'}
              </Badge>
              <div className='flex gap-2 pt-2'>
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  disabled={!editable}
                  onClick={() => onEditTransition(selectedTransition)}
                >
                  Edit
                </Button>
                <Button
                  type='button'
                  variant='destructive'
                  size='sm'
                  disabled={!editable}
                  onClick={() => onRemoveTransition(selectedTransition)}
                >
                  Remove
                </Button>
              </div>
            </div>
          ) : (
            <p className='text-sm text-muted-foreground'>
              Select a status or transition in the diagram.
            </p>
          )}
        </div>
      </div>
    </aside>
  );
}

function WorkflowTextView({
  editable,
  steps,
  transitions,
  stepById,
  onMoveStep,
  onRemoveStep,
  onEditTransition,
  onRemoveTransition,
}: {
  editable: boolean;
  steps: PMWorkflowStepApi[];
  transitions: PMWorkflowTransitionApi[];
  stepById: Map<number, PMWorkflowStepApi>;
  onMoveStep: (step: PMWorkflowStepApi, direction: 'up' | 'down') => void;
  onRemoveStep: (step: PMWorkflowStepApi) => void;
  onEditTransition: (transition: PMWorkflowTransitionApi) => void;
  onRemoveTransition: (transition: PMWorkflowTransitionApi) => void;
}) {
  return (
    <div className='space-y-8 p-4 lg:p-6'>
      <section>
        <h2 className='text-lg font-semibold'>Statuses</h2>
        <div className='mt-3 overflow-x-auto'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className='w-20'>Order</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Flags</TableHead>
                <TableHead>Status ID</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {steps.map((step, index) => (
                <TableRow key={step.id}>
                  <TableCell>{step.stepOrder ?? index + 1}</TableCell>
                  <TableCell className='font-medium'>{step.name}</TableCell>
                  <TableCell>
                    <div className='flex flex-wrap gap-2'>
                      {step.isInitial ? (
                        <Badge variant='outline'>Initial</Badge>
                      ) : null}
                      {step.isTerminal ? (
                        <Badge variant='outline'>Terminal</Badge>
                      ) : null}
                    </div>
                  </TableCell>
                  <TableCell>{step.statusId}</TableCell>
                  <TableCell className='space-x-1 text-right'>
                    <Button
                      type='button'
                      variant='ghost'
                      size='sm'
                      disabled={!editable || index === 0}
                      onClick={() => onMoveStep(step, 'up')}
                    >
                      Up
                    </Button>
                    <Button
                      type='button'
                      variant='ghost'
                      size='sm'
                      disabled={!editable || index === steps.length - 1}
                      onClick={() => onMoveStep(step, 'down')}
                    >
                      Down
                    </Button>
                    <Button
                      type='button'
                      variant='ghost'
                      size='icon'
                      disabled={!editable}
                      aria-label={`Remove ${step.name}`}
                      onClick={() => onRemoveStep(step)}
                    >
                      <Trash2 className='h-4 w-4' />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </section>

      <section>
        <h2 className='text-lg font-semibold'>Transitions</h2>
        <div className='mt-3 overflow-x-auto'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>From</TableHead>
                <TableHead>To</TableHead>
                <TableHead>Sequence</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {transitions.map((transition) => (
                <TableRow key={transition.id}>
                  <TableCell className='font-medium'>
                    {transition.name}
                  </TableCell>
                  <TableCell>
                    {formatStepName(stepById, transition.fromStepId)}
                  </TableCell>
                  <TableCell>
                    {formatStepName(stepById, transition.toStepId)}
                  </TableCell>
                  <TableCell>{transition.sequence ?? '-'}</TableCell>
                  <TableCell className='space-x-1 text-right'>
                    <Button
                      type='button'
                      variant='ghost'
                      size='sm'
                      disabled={!editable}
                      onClick={() => onEditTransition(transition)}
                    >
                      Edit
                    </Button>
                    <Button
                      type='button'
                      variant='ghost'
                      size='icon'
                      disabled={!editable}
                      aria-label={`Remove ${transition.name}`}
                      onClick={() => onRemoveTransition(transition)}
                    >
                      <Trash2 className='h-4 w-4' />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </section>
    </div>
  );
}

function AddStepDialog({
  open,
  statuses,
  statusCategories,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  open: boolean;
  statuses: PMStatusApi[];
  statusCategories: PMStatusCategoryApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: AddStepValues) => void;
}) {
  const [mode, setMode] = useState<'existing' | 'new'>('existing');
  const [statusId, setStatusId] = useState('');
  const [name, setName] = useState('');
  const [statusCategoryId, setStatusCategoryId] = useState('');
  const [isInitial, setIsInitial] = useState(false);
  const [isTerminal, setIsTerminal] = useState(false);

  useEffect(() => {
    if (!open) {
      setMode('existing');
      setStatusId('');
      setName('');
      setStatusCategoryId('');
      setIsInitial(false);
      setIsTerminal(false);
      return;
    }

    if (mode === 'existing' && statuses.length === 1) {
      setStatusId(String(statuses[0].id));
    }

    if (
      mode === 'existing' &&
      statusId &&
      statuses.every((status) => String(status.id) !== statusId)
    ) {
      setStatusId('');
    }

    if (mode === 'new' && statusCategories.length === 1) {
      setStatusCategoryId(String(statusCategories[0].id));
    }

    if (
      mode === 'new' &&
      statusCategoryId &&
      statusCategories.every(
        (category) => String(category.id) !== statusCategoryId
      )
    ) {
      setStatusCategoryId('');
    }
  }, [mode, open, statusCategoryId, statusCategories, statusId, statuses]);

  const canSubmit =
    mode === 'existing'
      ? Boolean(statusId)
      : Boolean(name.trim()) && Boolean(statusCategoryId);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add status</DialogTitle>
          <DialogDescription>
            Choose an existing status or create a new one for this workflow
            draft.
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='grid grid-cols-2 gap-2 rounded-md bg-muted p-1'>
            <Button
              type='button'
              variant={mode === 'existing' ? 'secondary' : 'ghost'}
              onClick={() => setMode('existing')}
            >
              Existing
            </Button>
            <Button
              type='button'
              variant={mode === 'new' ? 'secondary' : 'ghost'}
              onClick={() => setMode('new')}
            >
              New status
            </Button>
          </div>

          {mode === 'existing' ? (
            <div className='space-y-2'>
              <Label>Status</Label>
              <Select value={statusId} onValueChange={setStatusId}>
                <SelectTrigger className='w-full'>
                  <SelectValue placeholder='Select status' />
                </SelectTrigger>
                <SelectContent>
                  {statuses.map((status) => (
                    <SelectItem key={status.id} value={String(status.id)}>
                      {status.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          ) : (
            <>
              <div className='space-y-2'>
                <Label>Name</Label>
                <Input
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  placeholder='In QA Review'
                />
              </div>
              <div className='space-y-2'>
                <Label>Category</Label>
                <Select
                  value={statusCategoryId}
                  onValueChange={setStatusCategoryId}
                >
                  <SelectTrigger className='w-full'>
                    <SelectValue placeholder='Select category' />
                  </SelectTrigger>
                  <SelectContent>
                    {statusCategories.map((category) => (
                      <SelectItem key={category.id} value={String(category.id)}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </>
          )}
          <label className='flex items-center gap-2 text-sm'>
            <Checkbox
              checked={isInitial}
              onCheckedChange={(checked) => setIsInitial(checked === true)}
            />
            Initial status
          </label>
          <label className='flex items-center gap-2 text-sm'>
            <Checkbox
              checked={isTerminal}
              onCheckedChange={(checked) => setIsTerminal(checked === true)}
            />
            Terminal status
          </label>
        </div>
        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button
            type='button'
            disabled={!canSubmit || isSubmitting}
            onClick={() => {
              if (mode === 'existing') {
                onSubmit({
                  mode: 'existing',
                  statusId: Number(statusId),
                  isInitial,
                  isTerminal,
                });
                return;
              }

              onSubmit({
                mode: 'new',
                name,
                statusCategoryId: Number(statusCategoryId),
                isInitial,
                isTerminal,
              });
            }}
          >
            {mode === 'new' ? 'Create and add' : 'Add status'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function TransitionDialog({
  state,
  steps,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: TransitionDialogState;
  steps: PMWorkflowStepApi[];
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: TransitionValues, item?: PMWorkflowTransitionApi) => void;
}) {
  const item = state?.mode === 'edit' ? state.item : undefined;
  const [name, setName] = useState(item?.name ?? '');
  const [fromStepId, setFromStepId] = useState(
    item?.fromStepId ? String(item.fromStepId) : ANY_STEP_VALUE
  );
  const [toStepId, setToStepId] = useState(
    item?.toStepId ? String(item.toStepId) : ''
  );
  const [sequence, setSequence] = useState(
    item?.sequence ? String(item.sequence) : ''
  );

  const open = state !== null;
  const editMode = state?.mode === 'edit';

  useEffect(() => {
    setName(item?.name ?? '');
    setFromStepId(item?.fromStepId ? String(item.fromStepId) : ANY_STEP_VALUE);
    setToStepId(item?.toStepId ? String(item.toStepId) : '');
    setSequence(item?.sequence ? String(item.sequence) : '');
  }, [item]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {editMode ? 'Edit transition' : 'Add transition'}
          </DialogTitle>
          <DialogDescription>
            Define how work items move between workflow statuses.
          </DialogDescription>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='transition-name'>Name</Label>
            <Input
              id='transition-name'
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder='Start progress'
            />
          </div>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label>From</Label>
              <Select
                value={fromStepId}
                onValueChange={setFromStepId}
                disabled={editMode}
              >
                <SelectTrigger className='w-full'>
                  <SelectValue placeholder='Any status' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ANY_STEP_VALUE}>Any status</SelectItem>
                  {steps.map((step) => (
                    <SelectItem key={step.id} value={String(step.id)}>
                      {step.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label>To</Label>
              <Select
                value={toStepId}
                onValueChange={setToStepId}
                disabled={editMode}
              >
                <SelectTrigger className='w-full'>
                  <SelectValue placeholder='Select target' />
                </SelectTrigger>
                <SelectContent>
                  {steps.map((step) => (
                    <SelectItem key={step.id} value={String(step.id)}>
                      {step.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className='space-y-2'>
            <Label htmlFor='transition-sequence'>Sequence</Label>
            <Input
              id='transition-sequence'
              type='number'
              min={0}
              value={sequence}
              onChange={(event) => setSequence(event.target.value)}
            />
          </div>
        </div>
        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button
            type='button'
            disabled={!name.trim() || !toStepId || isSubmitting}
            onClick={() =>
              onSubmit(
                {
                  name: name.trim(),
                  fromStepId:
                    fromStepId === ANY_STEP_VALUE ? null : Number(fromStepId),
                  toStepId: Number(toStepId),
                  sequence: sequence ? Number(sequence) : null,
                },
                item
              )
            }
          >
            Save transition
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function ValidationPanel({
  validation,
}: {
  validation: PMWorkflowValidationApi;
}) {
  const findings = [...validation.errors, ...validation.warnings];
  return (
    <div className='border-b p-4 lg:px-6'>
      <Alert variant={validation.valid ? 'default' : 'destructive'}>
        <AlertDescription>
          <div className='font-medium'>
            {validation.valid
              ? 'Workflow is valid.'
              : 'Workflow has validation findings.'}
          </div>
          {findings.length > 0 ? (
            <ul className='mt-2 list-disc space-y-1 pl-5'>
              {findings.map((finding) => (
                <li key={`${finding.ruleKey}-${finding.message}`}>
                  {finding.message}
                </li>
              ))}
            </ul>
          ) : null}
        </AlertDescription>
      </Alert>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <div className='rounded-md border bg-muted/20 p-3'>
      <div className='text-xs uppercase text-muted-foreground'>{label}</div>
      <div className='mt-1 text-2xl font-semibold'>{value}</div>
    </div>
  );
}

function EditorSkeleton() {
  return (
    <main className='space-y-4 p-6'>
      <Skeleton className='h-10 w-72' />
      <Skeleton className='h-12 w-full' />
      <Skeleton className='h-[560px] w-full' />
    </main>
  );
}

function EditorState({ message }: { message: string }) {
  return (
    <main className='p-6'>
      <Alert variant='destructive'>
        <AlertDescription>{message}</AlertDescription>
      </Alert>
    </main>
  );
}

function formatStepName(
  stepById: Map<number, PMWorkflowStepApi>,
  stepId?: number | null
) {
  if (!stepId) {
    return 'Any status';
  }
  return stepById.get(stepId)?.name ?? `Status ${stepId}`;
}

function findMatchingStep(
  steps: PMWorkflowStepApi[],
  source: PMWorkflowStepApi
) {
  return (
    steps.find((step) => step.id === source.id) ??
    steps.find((step) => step.statusId === source.statusId) ??
    null
  );
}

function findMatchingTransition(
  transitions: PMWorkflowTransitionApi[],
  source: PMWorkflowTransitionApi,
  sourceStepById: Map<number, PMWorkflowStepApi>,
  targetSteps: PMWorkflowStepApi[]
) {
  const targetStepById = new Map(targetSteps.map((step) => [step.id, step]));
  const sourceFromStatusId = source.fromStepId
    ? sourceStepById.get(source.fromStepId)?.statusId
    : null;
  const sourceToStatusId = sourceStepById.get(source.toStepId)?.statusId;

  return (
    transitions.find((transition) => transition.id === source.id) ??
    transitions.find((transition) => {
      const targetFromStatusId = transition.fromStepId
        ? targetStepById.get(transition.fromStepId)?.statusId
        : null;
      const targetToStatusId = targetStepById.get(
        transition.toStepId
      )?.statusId;

      return (
        transition.name === source.name &&
        transition.sequence === source.sequence &&
        targetFromStatusId === sourceFromStatusId &&
        targetToStatusId === sourceToStatusId
      );
    }) ??
    null
  );
}

function generateStatusKey(name: string): string {
  return name
    .trim()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toUpperCase();
}

type AddStepValues =
  | {
      mode: 'existing';
      statusId: number;
      isInitial: boolean;
      isTerminal: boolean;
    }
  | {
      mode: 'new';
      name: string;
      statusCategoryId: number;
      isInitial: boolean;
      isTerminal: boolean;
    };

type TransitionValues = {
  name: string;
  fromStepId?: number | null;
  toStepId: number;
  sequence?: number | null;
};

type TransitionDialogState =
  | null
  | { mode: 'create' }
  | { mode: 'edit'; item: PMWorkflowTransitionApi };
