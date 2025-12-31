/**
 * PTM v2 - Task Detail Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task detail slide-over panel
 */

'use client';

import { useState } from 'react';
import {
  Calendar,
  Clock,
  Tag,
  Trash2,
  Copy,
  ExternalLink,
  Edit,
  Save,
  X,
  StickyNote,
  Plus,
  Link as LinkIcon,
  AlertCircle,
} from 'lucide-react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui/sheet';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Textarea } from '@/shared/components/ui/textarea';
import { Label } from '@/shared/components/ui/label';
import { Separator } from '@/shared/components/ui/separator';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Card } from '@/shared/components/ui/card';
import { StatusBadge } from '../shared/StatusBadge';
import { PriorityBadge } from '../shared/PriorityBadge';
import { SubtaskList } from './SubtaskList';
import { DependencyList } from './DependencyList';
import { NoteCard } from '../notes/NoteCard';
import { NoteEditorNovel } from '../notes/NoteEditorNovel';
import { EditTaskDialog } from './dialogs/EditTaskDialog';
import { DeleteTaskDialog } from './dialogs/DeleteTaskDialog';
import { isMac as checkIsMac } from '@/shared/utils';
import {
  useTaskDetail,
  useNoteOperations,
  useTaskEditForm,
  useKeyboardShortcuts,
  useTaskDialogs,
} from '../../hooks';
import { toNumericId } from '../../utils';
import { toast } from 'sonner';

interface TaskDetailProps {
  taskId: number | string | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onOpenFullView?: (taskId: number) => void;
}

export function TaskDetail({
  taskId,
  open,
  onOpenChange,
  onOpenFullView,
}: TaskDetailProps) {
  const [activeTab, setActiveTab] = useState<
    'details' | 'dependencies' | 'notes'
  >('details');
  const [showNoteEditor, setShowNoteEditor] = useState(false);
  const { editDialog: subtaskEditDialog, deleteDialog: subtaskDeleteDialog } =
    useTaskDialogs();

  const isMac = checkIsMac();
  const modKey = isMac ? '⌘' : 'Ctrl';

  // Convert taskId to number for API calls
  const numericTaskId = toNumericId(taskId);

  // Custom hooks for task operations
  const {
    task,
    isLoading,
    deleteDialogOpen,
    setDeleteDialogOpen,
    handleUpdate,
    openDeleteDialog,
    handleCopyLink,
  } = useTaskDetail({
    taskId: numericTaskId,
    onDeleteSuccess: () => onOpenChange(false),
  });

  // Custom hooks for note operations
  const {
    notes,
    handleCreate,
    handleUpdate: handleUpdateNote,
    handleDelete: handleDeleteNote,
  } = useNoteOperations({ taskId: numericTaskId });

  // Custom hook for edit form state
  const {
    isEditing,
    editForm,
    startEdit,
    cancelEdit,
    setEditForm,
    setIsEditing,
  } = useTaskEditForm(task);

  // Save handler
  const handleSave = async () => {
    try {
      await handleUpdate(editForm);
      setIsEditing(false);
    } catch (error) {
      // Error already handled in hook
    }
  };

  // Note creation handler
  const handleCreateNote = async (content: string, isPinned: boolean) => {
    const success = await handleCreate(content, isPinned);
    if (success) {
      setShowNoteEditor(false);
    }
  };

  // Note update handler - convert ID to number
  const handleUpdateNoteWithId = async (
    noteId: number | string,
    content: string,
    isPinned: boolean
  ) => {
    const numericNoteId = toNumericId(noteId);
    if (numericNoteId) {
      await handleUpdateNote(numericNoteId, content, isPinned);
    }
  };

  // Note delete handler - convert ID to number
  const handleDeleteNoteWithId = async (noteId: number | string) => {
    const numericNoteId = toNumericId(noteId);
    if (numericNoteId) {
      await handleDeleteNote(numericNoteId);
    }
  };

  // Keyboard shortcuts
  useKeyboardShortcuts(
    [
      {
        key: 'Escape',
        handler: () => onOpenChange(false),
        description: 'Close panel',
      },
      {
        key: 'Enter',
        shift: true,
        handler: () => onOpenFullView?.(numericTaskId!),
        description: 'Open in full view',
      },
      {
        key: 'e',
        mod: true,
        handler: () => (isEditing ? handleSave() : startEdit()),
        description: 'Edit task / Save changes',
      },
      {
        key: 'd',
        mod: true,
        handler: openDeleteDialog,
        description: 'Delete task',
      },
      {
        key: 'l',
        mod: true,
        handler: handleCopyLink,
        description: 'Copy link to clipboard',
      },
      {
        key: '1',
        mod: true,
        handler: () => setActiveTab('details'),
        description: 'Switch to Details tab',
      },
      {
        key: '2',
        mod: true,
        handler: () => setActiveTab('dependencies'),
        description: 'Switch to Dependencies tab',
      },
      {
        key: '3',
        mod: true,
        handler: () => setActiveTab('notes'),
        description: 'Switch to Notes tab',
      },
    ],
    { enabled: open && !isEditing }
  );

  if (!task && !isLoading) {
    return null;
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className='w-full sm:max-w-2xl overflow-y-auto p-6'>
        {isLoading ? (
          <>
            <SheetTitle className='sr-only'>Loading task...</SheetTitle>
            <div className='space-y-4'>
              <div className='h-8 w-3/4 bg-muted animate-pulse rounded' />
              <div className='h-4 w-1/2 bg-muted animate-pulse rounded' />
            </div>
          </>
        ) : task ? (
          <>
            <SheetHeader>
              <div className='space-y-3'>
                {isEditing ? (
                  <>
                    <SheetTitle className='sr-only'>{task.title}</SheetTitle>
                    <Input
                      value={editForm.title}
                      onChange={(e) =>
                        setEditForm({ ...editForm, title: e.target.value })
                      }
                      className='text-2xl font-bold'
                    />
                  </>
                ) : (
                  <>
                    <div className='flex items-start justify-between gap-2'>
                      <SheetTitle className='text-2xl flex-1'>
                        {task.title}
                      </SheetTitle>
                      <div className='flex items-center gap-1'>
                        {/* Open in full view button */}
                        {onOpenFullView && (
                          <Button
                            variant='ghost'
                            size='icon'
                            onClick={() => onOpenFullView(numericTaskId!)}
                            title='Open in full view (Shift+Enter)'
                            className='h-8 w-8'
                          >
                            <ExternalLink className='h-4 w-4' />
                          </Button>
                        )}

                        {/* Copy link button */}
                        <Button
                          variant='ghost'
                          size='icon'
                          onClick={handleCopyLink}
                          title={`Copy link (${modKey}+L)`}
                          className='h-8 w-8'
                        >
                          <Copy className='h-4 w-4' />
                        </Button>

                        {/* Edit button */}
                        <Button
                          variant='ghost'
                          size='icon'
                          onClick={startEdit}
                          title={`Edit (${modKey}+E)`}
                          className='h-8 w-8'
                        >
                          <Edit className='h-4 w-4' />
                        </Button>

                        {/* Delete button */}
                        <Button
                          variant='ghost'
                          size='icon'
                          onClick={openDeleteDialog}
                          title={`Delete (${modKey}+D)`}
                          className='h-8 w-8 text-red-600 hover:text-red-700'
                        >
                          <Trash2 className='h-4 w-4' />
                        </Button>
                      </div>
                    </div>

                    {/* Keyboard shortcuts hint */}
                    <div className='hidden md:flex items-center gap-3 text-xs text-muted-foreground flex-wrap'>
                      {onOpenFullView && (
                        <>
                          <div className='flex items-center gap-1'>
                            <kbd className='px-1.5 py-0.5 bg-muted rounded border'>
                              Shift+↵
                            </kbd>
                            <span>Full view</span>
                          </div>
                          <span>•</span>
                        </>
                      )}
                      <div className='flex items-center gap-1'>
                        <kbd className='px-1.5 py-0.5 bg-muted rounded border'>
                          {modKey}+L
                        </kbd>
                        <span>Copy link</span>
                      </div>
                      <span>•</span>
                      <div className='flex items-center gap-1'>
                        <kbd className='px-1.5 py-0.5 bg-muted rounded border'>
                          {modKey}+1/2/3
                        </kbd>
                        <span>Tabs</span>
                      </div>
                    </div>
                  </>
                )}

                <div className='flex items-center gap-2'>
                  <StatusBadge status={task.status} />
                  <PriorityBadge priority={task.priority} />
                </div>
              </div>
            </SheetHeader>

            <div className='space-y-6 mt-6'>
              {/* Tabs for Details and Notes */}
              <Tabs
                value={activeTab}
                onValueChange={(v) =>
                  setActiveTab(v as 'details' | 'dependencies' | 'notes')
                }
              >
                <TabsList className='grid w-full grid-cols-3'>
                  <TabsTrigger value='details'>Details</TabsTrigger>
                  <TabsTrigger
                    value='dependencies'
                    className='flex items-center gap-2'
                  >
                    <LinkIcon className='h-4 w-4' />
                    Dependencies
                    {task.isBlocked && (
                      <AlertCircle className='h-3 w-3 text-red-600' />
                    )}
                  </TabsTrigger>
                  <TabsTrigger
                    value='notes'
                    className='flex items-center gap-2'
                  >
                    <StickyNote className='h-4 w-4' />
                    Notes {notes.length > 0 && `(${notes.length})`}
                  </TabsTrigger>
                </TabsList>

                {/* Details Tab */}
                <TabsContent value='details' className='space-y-6 mt-4'>
                  {/* Description */}
                  <div className='space-y-2'>
                    <Label>Description</Label>
                    {isEditing ? (
                      <Textarea
                        value={editForm.description || ''}
                        onChange={(e) =>
                          setEditForm({
                            ...editForm,
                            description: e.target.value,
                          })
                        }
                        rows={4}
                      />
                    ) : (
                      <p className='text-sm text-muted-foreground'>
                        {task.description || 'No description provided'}
                      </p>
                    )}
                  </div>

                  {/* Subtasks Section */}
                  <div className='p-4 bg-muted/30 rounded-lg'>
                    <SubtaskList
                      parentTaskId={task.id}
                      allowNesting={true}
                      showFullDetails={true}
                      onEditOpen={subtaskEditDialog.openEdit}
                      onDeleteOpen={subtaskDeleteDialog.openDelete}
                      onTaskClick={(subtaskId) => {
                        // Open subtask in new detail sheet (future: nested sheets)
                        toast.info(
                          'Click to open subtask details (coming soon)'
                        );
                      }}
                    />
                  </div>

                  <Separator />

                  {/* Details Grid */}
                  <div className='grid grid-cols-2 gap-4'>
                    {/* Duration */}
                    <div className='space-y-1'>
                      <Label className='text-muted-foreground'>Duration</Label>
                      {isEditing ? (
                        <Input
                          type='number'
                          step='15'
                          placeholder='Minutes'
                          value={editForm.estimatedDurationMin || ''}
                          onChange={(e) =>
                            setEditForm({
                              ...editForm,
                              estimatedDurationMin: parseFloat(e.target.value),
                            })
                          }
                        />
                      ) : (
                        <div className='flex items-center gap-2 text-sm'>
                          <Clock className='h-4 w-4' />
                          <span>
                            {task.estimatedDurationMin
                              ? `${Math.floor(task.estimatedDurationMin / 60)}h ${task.estimatedDurationMin % 60}m estimated`
                              : 'Not set'}
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Deadline */}
                    {task.deadlineMs && (
                      <div className='space-y-1'>
                        <Label className='text-muted-foreground'>
                          Deadline
                        </Label>
                        <div className='flex items-center gap-2 text-sm'>
                          <Calendar className='h-4 w-4' />
                          <span>
                            {new Date(task.deadlineMs).toLocaleDateString()}
                          </span>
                        </div>
                      </div>
                    )}

                    {/* Created */}
                    <div className='space-y-1'>
                      <Label className='text-muted-foreground'>Created</Label>
                      <p className='text-sm'>
                        {new Date(task.createdAt).toLocaleDateString()}
                      </p>
                    </div>

                    {/* Updated */}
                    <div className='space-y-1'>
                      <Label className='text-muted-foreground'>Updated</Label>
                      <p className='text-sm'>
                        {new Date(task.updatedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>

                  {/* Tags */}
                  {task.tags && task.tags.length > 0 && (
                    <div className='space-y-2'>
                      <Label className='flex items-center gap-2'>
                        <Tag className='h-4 w-4' />
                        Tags
                      </Label>
                      <div className='flex flex-wrap gap-2'>
                        {task.tags.map((tag: string, index: number) => (
                          <span
                            key={index}
                            className='px-3 py-1 bg-muted rounded-full text-sm'
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </TabsContent>

                {/* Dependencies Tab */}
                <TabsContent value='dependencies' className='mt-4'>
                  <DependencyList taskId={task.id} />
                </TabsContent>

                {/* Notes Tab */}
                <TabsContent value='notes' className='space-y-4 mt-4'>
                  <div className='flex items-center justify-between'>
                    <h3 className='text-lg font-semibold'>Task Notes</h3>
                    <Button
                      size='sm'
                      onClick={() => setShowNoteEditor(!showNoteEditor)}
                      variant={showNoteEditor ? 'outline' : 'default'}
                    >
                      {showNoteEditor ? (
                        <>
                          <X className='h-4 w-4 mr-2' />
                          Cancel
                        </>
                      ) : (
                        <>
                          <Plus className='h-4 w-4 mr-2' />
                          Add Note
                        </>
                      )}
                    </Button>
                  </div>

                  {showNoteEditor && (
                    <NoteEditorNovel
                      onSave={handleCreateNote}
                      onCancel={() => setShowNoteEditor(false)}
                      placeholder='Add notes, ideas, or important information about this task...'
                    />
                  )}

                  {notes.length === 0 ? (
                    <Card className='p-8 text-center border-dashed'>
                      <div className='flex flex-col items-center gap-2 text-muted-foreground'>
                        <StickyNote className='h-12 w-12' />
                        <p className='text-lg font-medium'>No notes yet</p>
                        <p className='text-sm'>
                          Add notes to capture important information about this
                          task
                        </p>
                      </div>
                    </Card>
                  ) : (
                    <div className='space-y-3'>
                      {notes.map((note) => (
                        <NoteCard key={note.id} note={note} />
                      ))}
                    </div>
                  )}
                </TabsContent>
              </Tabs>

              <Separator />

              {/* Actions */}
              <div className='flex items-center justify-between pt-2'>
                {isEditing ? (
                  <div className='flex gap-2 w-full'>
                    <Button
                      variant='outline'
                      onClick={cancelEdit}
                      className='flex-1'
                    >
                      <X className='mr-2 h-4 w-4' />
                      Cancel
                    </Button>
                    <Button onClick={handleSave} className='flex-1'>
                      <Save className='mr-2 h-4 w-4' />
                      Save Changes
                    </Button>
                  </div>
                ) : null}
              </div>
            </div>
          </>
        ) : null}
      </SheetContent>

      {/* Delete Confirmation Dialog */}
      <DeleteTaskDialog
        task={task || null}
        taskId={null}
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        onSuccess={() => onOpenChange(false)}
      />

      {/* Subtask Edit/Delete Dialogs */}
      <EditTaskDialog
        taskId={subtaskEditDialog.taskId}
        open={subtaskEditDialog.open}
        onOpenChange={subtaskEditDialog.onOpenChange}
      />
      <DeleteTaskDialog
        taskId={subtaskDeleteDialog.taskId}
        taskTitle={subtaskDeleteDialog.taskTitle}
        open={subtaskDeleteDialog.open}
        onOpenChange={subtaskDeleteDialog.onOpenChange}
      />
    </Sheet>
  );
}
