/*
Author: QuanTuanHuy
Description: Part of Serp Project - Note List Component for CRM
*/

'use client';

import { useState, useMemo } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Input,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import {
  Search,
  Plus,
  MessageSquare,
  SortAsc,
  SortDesc,
  Filter,
  AlertCircle,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { NoteCard, Note } from './NoteCard';
import { AddNoteDialog } from './AddNoteDialog';

interface NoteListProps {
  notes: Note[];
  title?: string;
  emptyMessage?: string;
  currentUserId?: string;
  onAddNote?: (content: string) => void;
  onEditNote?: (note: Note, newContent: string) => void;
  onDeleteNote?: (note: Note) => void;
  onPinNote?: (note: Note) => void;
  onReplyNote?: (note: Note) => void;
  className?: string;
}

export const NoteList: React.FC<NoteListProps> = ({
  notes,
  title = 'Ghi chú',
  emptyMessage = 'Chưa có ghi chú nào',
  currentUserId = 'current-user',
  onAddNote,
  onEditNote,
  onDeleteNote,
  onPinNote,
  onReplyNote,
  className,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [sortOrder, setSortOrder] = useState<'newest' | 'oldest'>('newest');
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [selectedNote, setSelectedNote] = useState<Note | null>(null);
  const [editContent, setEditContent] = useState('');

  // Filter notes based on search
  const filteredNotes = useMemo(() => {
    if (!searchQuery) return notes;

    const query = searchQuery.toLowerCase();
    return notes.filter(
      (note) =>
        note.content.toLowerCase().includes(query) ||
        note.createdBy.name.toLowerCase().includes(query)
    );
  }, [notes, searchQuery]);

  // Sort notes - pinned first, then by date
  const sortedNotes = useMemo(() => {
    return [...filteredNotes].sort((a, b) => {
      // Pinned notes first
      if (a.isPinned && !b.isPinned) return -1;
      if (!a.isPinned && b.isPinned) return 1;

      // Then by date
      const dateA = new Date(a.createdAt).getTime();
      const dateB = new Date(b.createdAt).getTime();

      return sortOrder === 'newest' ? dateB - dateA : dateA - dateB;
    });
  }, [filteredNotes, sortOrder]);

  const handleEdit = (note: Note) => {
    setSelectedNote(note);
    setEditContent(note.content);
    setShowEditDialog(true);
  };

  const handleDelete = (note: Note) => {
    setSelectedNote(note);
    setShowDeleteDialog(true);
  };

  const handleAddSubmit = (content: string) => {
    onAddNote?.(content);
    setShowAddDialog(false);
  };

  const handleEditSubmit = () => {
    if (selectedNote && editContent.trim()) {
      onEditNote?.(selectedNote, editContent);
    }
    setShowEditDialog(false);
    setSelectedNote(null);
    setEditContent('');
  };

  const handleConfirmDelete = () => {
    if (selectedNote) {
      onDeleteNote?.(selectedNote);
    }
    setShowDeleteDialog(false);
    setSelectedNote(null);
  };

  return (
    <Card className={className}>
      <CardHeader className='pb-4'>
        <div className='flex items-center justify-between'>
          <div className='flex items-center gap-2'>
            <MessageSquare className='h-5 w-5 text-muted-foreground' />
            <h3 className='text-lg font-semibold'>{title}</h3>
            <span className='text-sm text-muted-foreground'>
              ({notes.length})
            </span>
          </div>
          <div className='flex items-center gap-2'>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8'
              onClick={() =>
                setSortOrder(sortOrder === 'newest' ? 'oldest' : 'newest')
              }
              title={
                sortOrder === 'newest' ? 'Mới nhất trước' : 'Cũ nhất trước'
              }
            >
              {sortOrder === 'newest' ? (
                <SortDesc className='h-4 w-4' />
              ) : (
                <SortAsc className='h-4 w-4' />
              )}
            </Button>
            <Button size='sm' onClick={() => setShowAddDialog(true)}>
              <Plus className='h-4 w-4 mr-2' />
              Add Note
            </Button>
          </div>
        </div>

        {/* Search */}
        {notes.length > 3 && (
          <div className='relative mt-4'>
            <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
            <Input
              placeholder='Search notes...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-9'
            />
          </div>
        )}
      </CardHeader>

      <CardContent className='space-y-3'>
        {sortedNotes.length > 0 ? (
          sortedNotes.map((note) => (
            <NoteCard
              key={note.id}
              note={note}
              currentUserId={currentUserId}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onPin={onPinNote}
              onReply={onReplyNote}
            />
          ))
        ) : (
          <div className='text-center py-8'>
            <MessageSquare className='h-12 w-12 text-muted-foreground mx-auto mb-3' />
            <p className='text-muted-foreground mb-4'>
              {searchQuery ? 'No matching notes found' : emptyMessage}
            </p>
            {!searchQuery && (
              <Button onClick={() => setShowAddDialog(true)}>
                <Plus className='h-4 w-4 mr-2' />
                Add First Note
              </Button>
            )}
          </div>
        )}
      </CardContent>

      {/* Add Note Dialog */}
      <AddNoteDialog
        open={showAddDialog}
        onOpenChange={setShowAddDialog}
        onSubmit={handleAddSubmit}
      />

      {/* Edit Note Dialog */}
      <Dialog open={showEditDialog} onOpenChange={setShowEditDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Chỉnh sửa ghi chú</DialogTitle>
            <DialogDescription>Cập nhật nội dung ghi chú</DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              className='w-full min-h-[120px] p-3 border rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-primary bg-background text-foreground'
              placeholder='Nhập nội dung ghi chú...'
            />
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => {
                setShowEditDialog(false);
                setSelectedNote(null);
                setEditContent('');
              }}
            >
              Hủy
            </Button>
            <Button onClick={handleEditSubmit} disabled={!editContent.trim()}>
              Cập nhật
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className='flex items-center gap-2'>
              <AlertCircle className='h-5 w-5 text-red-500' />
              Xác nhận xóa ghi chú
            </DialogTitle>
            <DialogDescription>
              Bạn có chắc chắn muốn xóa ghi chú này? Hành động này không thể
              hoàn tác.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => {
                setShowDeleteDialog(false);
                setSelectedNote(null);
              }}
            >
              Hủy
            </Button>
            <Button variant='destructive' onClick={handleConfirmDelete}>
              Xóa ghi chú
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  );
};

export default NoteList;
