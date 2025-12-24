/*
Author: QuanTuanHuy
Description: Part of Serp Project - Contact Card Component for CRM
*/

'use client';

import {
  Card,
  CardContent,
  Button,
  Badge,
  Avatar,
  AvatarFallback,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import {
  Phone,
  Mail,
  MoreVertical,
  Edit,
  Trash2,
  Star,
  StarOff,
  Copy,
  ExternalLink,
  MessageSquare,
  Calendar,
  Building2,
} from 'lucide-react';
import { cn } from '@/shared/utils';

export interface Contact {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  jobTitle?: string;
  department?: string;
  isPrimary?: boolean;
  isFavorite?: boolean;
  notes?: string;
  linkedInUrl?: string;
  createdAt: string;
  updatedAt: string;
}

interface ContactCardProps {
  contact: Contact;
  onEdit?: (contact: Contact) => void;
  onDelete?: (contact: Contact) => void;
  onToggleFavorite?: (contact: Contact) => void;
  onSetPrimary?: (contact: Contact) => void;
  className?: string;
}

export const ContactCard: React.FC<ContactCardProps> = ({
  contact,
  onEdit,
  onDelete,
  onToggleFavorite,
  onSetPrimary,
  className,
}) => {
  const fullName = `${contact.firstName} ${contact.lastName}`;
  const initials = `${contact.firstName[0]}${contact.lastName[0]}`;

  const copyToClipboard = (text: string, type: string) => {
    navigator.clipboard.writeText(text);
    // Could add toast notification here
  };

  return (
    <Card className={cn('hover:shadow-md transition-shadow', className)}>
      <CardContent className='p-4'>
        <div className='flex items-start gap-3'>
          {/* Avatar */}
          <Avatar className='h-12 w-12'>
            <AvatarFallback className='bg-primary/10 text-primary font-medium'>
              {initials}
            </AvatarFallback>
          </Avatar>

          {/* Info */}
          <div className='flex-1 min-w-0'>
            <div className='flex items-center gap-2'>
              <h3 className='font-medium text-foreground truncate'>
                {fullName}
              </h3>
              {contact.isPrimary && (
                <Badge
                  variant='outline'
                  className='bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300 text-xs'
                >
                  Primary
                </Badge>
              )}
              {contact.isFavorite && (
                <Star className='h-4 w-4 text-yellow-500 fill-yellow-500' />
              )}
            </div>

            {contact.jobTitle && (
              <p className='text-sm text-muted-foreground truncate'>
                {contact.jobTitle}
                {contact.department && ` • ${contact.department}`}
              </p>
            )}

            <div className='flex flex-col gap-1 mt-2'>
              <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                <Mail className='h-3.5 w-3.5' />
                <a
                  href={`mailto:${contact.email}`}
                  className='hover:text-primary truncate'
                >
                  {contact.email}
                </a>
                <button
                  onClick={() => copyToClipboard(contact.email, 'Email')}
                  className='hover:text-primary'
                >
                  <Copy className='h-3 w-3' />
                </button>
              </div>

              {contact.phone && (
                <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                  <Phone className='h-3.5 w-3.5' />
                  <a
                    href={`tel:${contact.phone}`}
                    className='hover:text-primary'
                  >
                    {contact.phone}
                  </a>
                  <button
                    onClick={() => copyToClipboard(contact.phone!, 'Phone')}
                    className='hover:text-primary'
                  >
                    <Copy className='h-3 w-3' />
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Actions */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='ghost' size='icon' className='h-8 w-8'>
                <MoreVertical className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem onClick={() => onEdit?.(contact)}>
                <Edit className='h-4 w-4 mr-2' />
                Chỉnh sửa
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onToggleFavorite?.(contact)}>
                {contact.isFavorite ? (
                  <>
                    <StarOff className='h-4 w-4 mr-2' />
                    Bỏ yêu thích
                  </>
                ) : (
                  <>
                    <Star className='h-4 w-4 mr-2' />
                    Yêu thích
                  </>
                )}
              </DropdownMenuItem>
              {!contact.isPrimary && (
                <DropdownMenuItem onClick={() => onSetPrimary?.(contact)}>
                  <Building2 className='h-4 w-4 mr-2' />
                  Đặt làm liên hệ chính
                </DropdownMenuItem>
              )}
              {contact.linkedInUrl && (
                <DropdownMenuItem asChild>
                  <a
                    href={contact.linkedInUrl}
                    target='_blank'
                    rel='noopener noreferrer'
                  >
                    <ExternalLink className='h-4 w-4 mr-2' />
                    Xem LinkedIn
                  </a>
                </DropdownMenuItem>
              )}
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => onDelete?.(contact)}
                className='text-red-600 focus:text-red-600'
              >
                <Trash2 className='h-4 w-4 mr-2' />
                Xóa liên hệ
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* Quick Actions */}
        <div className='flex items-center gap-2 mt-3 pt-3 border-t'>
          <Button variant='outline' size='sm' className='flex-1' asChild>
            <a href={`mailto:${contact.email}`}>
              <Mail className='h-4 w-4 mr-2' />
              Email
            </a>
          </Button>
          {contact.phone && (
            <Button variant='outline' size='sm' className='flex-1' asChild>
              <a href={`tel:${contact.phone}`}>
                <Phone className='h-4 w-4 mr-2' />
                Gọi
              </a>
            </Button>
          )}
          <Button variant='outline' size='sm' className='flex-1'>
            <Calendar className='h-4 w-4 mr-2' />
            Lên lịch
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default ContactCard;
