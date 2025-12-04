/*
Author: QuanTuanHuy
Description: Part of Serp Project - Contact List Component for CRM
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
import { Search, Plus, Users, Grid, List, AlertCircle } from 'lucide-react';
import { cn } from '@/shared/utils';
import { ContactCard, Contact } from './ContactCard';
import { ContactForm, ContactFormData } from './ContactForm';

interface ContactListProps {
  contacts: Contact[];
  title?: string;
  emptyMessage?: string;
  onAddContact?: (data: ContactFormData) => void;
  onEditContact?: (contact: Contact, data: ContactFormData) => void;
  onDeleteContact?: (contact: Contact) => void;
  onToggleFavorite?: (contact: Contact) => void;
  onSetPrimary?: (contact: Contact) => void;
  className?: string;
}

export const ContactList: React.FC<ContactListProps> = ({
  contacts,
  title = 'Danh bạ',
  emptyMessage = 'Chưa có liên hệ nào',
  onAddContact,
  onEditContact,
  onDeleteContact,
  onToggleFavorite,
  onSetPrimary,
  className,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null);

  // Filter contacts based on search
  const filteredContacts = useMemo(() => {
    if (!searchQuery) return contacts;

    const query = searchQuery.toLowerCase();
    return contacts.filter(
      (contact) =>
        contact.firstName.toLowerCase().includes(query) ||
        contact.lastName.toLowerCase().includes(query) ||
        contact.email.toLowerCase().includes(query) ||
        contact.phone?.toLowerCase().includes(query) ||
        contact.jobTitle?.toLowerCase().includes(query)
    );
  }, [contacts, searchQuery]);

  // Sort contacts - favorites first, then primary, then alphabetically
  const sortedContacts = useMemo(() => {
    return [...filteredContacts].sort((a, b) => {
      if (a.isFavorite && !b.isFavorite) return -1;
      if (!a.isFavorite && b.isFavorite) return 1;
      if (a.isPrimary && !b.isPrimary) return -1;
      if (!a.isPrimary && b.isPrimary) return 1;
      return a.firstName.localeCompare(b.firstName);
    });
  }, [filteredContacts]);

  const handleEdit = (contact: Contact) => {
    setSelectedContact(contact);
    setShowEditDialog(true);
  };

  const handleDelete = (contact: Contact) => {
    setSelectedContact(contact);
    setShowDeleteDialog(true);
  };

  const handleAddSubmit = (data: ContactFormData) => {
    onAddContact?.(data);
    setShowAddDialog(false);
  };

  const handleEditSubmit = (data: ContactFormData) => {
    if (selectedContact) {
      onEditContact?.(selectedContact, data);
    }
    setShowEditDialog(false);
    setSelectedContact(null);
  };

  const handleConfirmDelete = () => {
    if (selectedContact) {
      onDeleteContact?.(selectedContact);
    }
    setShowDeleteDialog(false);
    setSelectedContact(null);
  };

  return (
    <Card className={className}>
      <CardHeader className='pb-4'>
        <div className='flex items-center justify-between'>
          <div className='flex items-center gap-2'>
            <Users className='h-5 w-5 text-muted-foreground' />
            <h3 className='text-lg font-semibold'>{title}</h3>
            <span className='text-sm text-muted-foreground'>
              ({contacts.length})
            </span>
          </div>
          <div className='flex items-center gap-2'>
            <div className='flex items-center border rounded-lg p-0.5'>
              <Button
                variant={viewMode === 'grid' ? 'secondary' : 'ghost'}
                size='icon'
                className='h-7 w-7'
                onClick={() => setViewMode('grid')}
              >
                <Grid className='h-4 w-4' />
              </Button>
              <Button
                variant={viewMode === 'list' ? 'secondary' : 'ghost'}
                size='icon'
                className='h-7 w-7'
                onClick={() => setViewMode('list')}
              >
                <List className='h-4 w-4' />
              </Button>
            </div>
            <Button size='sm' onClick={() => setShowAddDialog(true)}>
              <Plus className='h-4 w-4 mr-2' />
              Thêm liên hệ
            </Button>
          </div>
        </div>

        {/* Search */}
        <div className='relative mt-4'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
          <Input
            placeholder='Tìm kiếm liên hệ...'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className='pl-9'
          />
        </div>
      </CardHeader>

      <CardContent>
        {sortedContacts.length > 0 ? (
          <div
            className={cn(
              viewMode === 'grid'
                ? 'grid grid-cols-1 md:grid-cols-2 gap-4'
                : 'space-y-3'
            )}
          >
            {sortedContacts.map((contact) => (
              <ContactCard
                key={contact.id}
                contact={contact}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onToggleFavorite={onToggleFavorite}
                onSetPrimary={onSetPrimary}
              />
            ))}
          </div>
        ) : (
          <div className='text-center py-8'>
            <Users className='h-12 w-12 text-muted-foreground mx-auto mb-3' />
            <p className='text-muted-foreground mb-4'>
              {searchQuery ? 'Không tìm thấy liên hệ phù hợp' : emptyMessage}
            </p>
            {!searchQuery && (
              <Button onClick={() => setShowAddDialog(true)}>
                <Plus className='h-4 w-4 mr-2' />
                Thêm liên hệ đầu tiên
              </Button>
            )}
          </div>
        )}
      </CardContent>

      {/* Add Contact Dialog */}
      <Dialog open={showAddDialog} onOpenChange={setShowAddDialog}>
        <DialogContent className='max-w-lg'>
          <DialogHeader>
            <DialogTitle>Thêm liên hệ mới</DialogTitle>
            <DialogDescription>Nhập thông tin liên hệ mới</DialogDescription>
          </DialogHeader>
          <ContactForm
            onSubmit={handleAddSubmit}
            onCancel={() => setShowAddDialog(false)}
          />
        </DialogContent>
      </Dialog>

      {/* Edit Contact Dialog */}
      <Dialog open={showEditDialog} onOpenChange={setShowEditDialog}>
        <DialogContent className='max-w-lg'>
          <DialogHeader>
            <DialogTitle>Chỉnh sửa liên hệ</DialogTitle>
            <DialogDescription>Cập nhật thông tin liên hệ</DialogDescription>
          </DialogHeader>
          {selectedContact && (
            <ContactForm
              initialData={{
                firstName: selectedContact.firstName,
                lastName: selectedContact.lastName,
                email: selectedContact.email,
                phone: selectedContact.phone,
                jobTitle: selectedContact.jobTitle,
                department: selectedContact.department,
                isPrimary: selectedContact.isPrimary,
                linkedInUrl: selectedContact.linkedInUrl,
                notes: selectedContact.notes,
              }}
              onSubmit={handleEditSubmit}
              onCancel={() => {
                setShowEditDialog(false);
                setSelectedContact(null);
              }}
              isEditing
            />
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className='flex items-center gap-2'>
              <AlertCircle className='h-5 w-5 text-red-500' />
              Xác nhận xóa liên hệ
            </DialogTitle>
            <DialogDescription>
              Bạn có chắc chắn muốn xóa liên hệ{' '}
              <strong>
                {selectedContact?.firstName} {selectedContact?.lastName}
              </strong>
              ? Hành động này không thể hoàn tác.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => {
                setShowDeleteDialog(false);
                setSelectedContact(null);
              }}
            >
              Hủy
            </Button>
            <Button variant='destructive' onClick={handleConfirmDelete}>
              Xóa liên hệ
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  );
};

export default ContactList;
