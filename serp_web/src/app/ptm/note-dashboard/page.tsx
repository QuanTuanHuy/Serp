/**
 * PTM Note Dashboard Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Personal Notes and Documentation
 */

'use client';

import React, { useState } from 'react';
import {
  FileText,
  Plus,
  Search,
  Filter,
  BookOpen,
  Star,
  Clock,
  Tag,
} from 'lucide-react';
import { Button, Card, Input } from '@/shared/components';

const NoteDashboard: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');

  const notes = [
    {
      id: 1,
      title: 'API Design Patterns',
      content: 'Collection of REST API design patterns and best practices...',
      tags: ['API', 'Design', 'Backend'],
      lastModified: '2 hours ago',
      favorite: true,
      category: 'Development',
    },
    {
      id: 2,
      title: 'Meeting Notes - Sprint Planning',
      content: 'Sprint planning session notes for the upcoming iteration...',
      tags: ['Meeting', 'Sprint', 'Planning'],
      lastModified: '1 day ago',
      favorite: false,
      category: 'Meetings',
    },
    {
      id: 3,
      title: 'Code Review Checklist',
      content:
        'Comprehensive checklist for conducting thorough code reviews...',
      tags: ['Code Review', 'Quality', 'Checklist'],
      lastModified: '3 days ago',
      favorite: true,
      category: 'Development',
    },
    {
      id: 4,
      title: 'Project Requirements',
      content: 'Detailed requirements for the SERP project implementation...',
      tags: ['Requirements', 'SERP', 'Project'],
      lastModified: '1 week ago',
      favorite: false,
      category: 'Projects',
    },
    {
      id: 5,
      title: 'Learning Resources',
      content: 'Curated list of learning resources for React and Next.js...',
      tags: ['Learning', 'React', 'Next.js'],
      lastModified: '2 weeks ago',
      favorite: true,
      category: 'Learning',
    },
  ];

  const categories = ['All', 'Development', 'Meetings', 'Projects', 'Learning'];

  const filteredNotes = notes.filter(
    (note) =>
      note.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      note.content.toLowerCase().includes(searchQuery.toLowerCase()) ||
      note.tags.some((tag) =>
        tag.toLowerCase().includes(searchQuery.toLowerCase())
      )
  );

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <FileText className='h-6 w-6' />
          <h1 className='text-2xl font-bold'>Note Dashboard</h1>
        </div>
        <Button>
          <Plus className='mr-2 h-4 w-4' />
          New Note
        </Button>
      </div>

      {/* Search and Filter */}
      <Card className='p-6'>
        <div className='flex flex-col gap-4 md:flex-row'>
          <div className='relative flex-1'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              placeholder='Search notes, tags, content...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-10'
            />
          </div>
          <div className='flex gap-2'>
            <Button variant='outline' size='sm'>
              <Filter className='mr-2 h-4 w-4' />
              Filter
            </Button>
            <Button variant='outline' size='sm'>
              <Tag className='mr-2 h-4 w-4' />
              Tags
            </Button>
          </div>
        </div>
      </Card>

      {/* Categories */}
      <div className='flex gap-2 overflow-x-auto'>
        {categories.map((category) => (
          <Button
            key={category}
            variant={category === 'All' ? 'default' : 'outline'}
            size='sm'
            className='whitespace-nowrap'
          >
            {category}
          </Button>
        ))}
      </div>

      {/* Notes Grid */}
      <div className='grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3'>
        {filteredNotes.map((note) => (
          <Card key={note.id} className='p-6 hover:shadow-md transition-shadow'>
            <div className='space-y-4'>
              {/* Note Header */}
              <div className='flex items-start justify-between'>
                <div className='flex-1'>
                  <h3 className='font-semibold line-clamp-2'>{note.title}</h3>
                  <p className='text-sm text-muted-foreground mt-1'>
                    {note.category}
                  </p>
                </div>
                {note.favorite && (
                  <Star className='h-4 w-4 text-yellow-500 fill-current' />
                )}
              </div>

              {/* Note Content Preview */}
              <p className='text-sm text-muted-foreground line-clamp-3'>
                {note.content}
              </p>

              {/* Tags */}
              <div className='flex flex-wrap gap-1'>
                {note.tags.slice(0, 3).map((tag) => (
                  <span
                    key={tag}
                    className='rounded-full bg-muted px-2 py-1 text-xs'
                  >
                    {tag}
                  </span>
                ))}
                {note.tags.length > 3 && (
                  <span className='rounded-full bg-muted px-2 py-1 text-xs'>
                    +{note.tags.length - 3}
                  </span>
                )}
              </div>

              {/* Note Footer */}
              <div className='flex items-center justify-between text-xs text-muted-foreground'>
                <div className='flex items-center gap-1'>
                  <Clock className='h-3 w-3' />
                  <span>{note.lastModified}</span>
                </div>
                <div className='flex gap-2'>
                  <Button size='sm' variant='ghost'>
                    Edit
                  </Button>
                  <Button size='sm' variant='ghost'>
                    Share
                  </Button>
                </div>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Quick Stats */}
      <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-blue-600'>{notes.length}</div>
          <div className='text-sm text-muted-foreground'>Total Notes</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-yellow-600'>
            {notes.filter((n) => n.favorite).length}
          </div>
          <div className='text-sm text-muted-foreground'>Favorites</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-green-600'>
            {categories.length - 1}
          </div>
          <div className='text-sm text-muted-foreground'>Categories</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-purple-600'>
            {notes.reduce((acc, note) => acc + note.tags.length, 0)}
          </div>
          <div className='text-sm text-muted-foreground'>Total Tags</div>
        </Card>
      </div>

      {/* Quick Actions */}
      <Card className='p-6'>
        <h3 className='mb-4 text-lg font-semibold'>Quick Actions</h3>
        <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
          <Button variant='outline' className='justify-start'>
            <Plus className='mr-2 h-4 w-4' />
            Create Note
          </Button>
          <Button variant='outline' className='justify-start'>
            <BookOpen className='mr-2 h-4 w-4' />
            Templates
          </Button>
          <Button variant='outline' className='justify-start'>
            <Tag className='mr-2 h-4 w-4' />
            Manage Tags
          </Button>
          <Button variant='outline' className='justify-start'>
            <Search className='mr-2 h-4 w-4' />
            Advanced Search
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default NoteDashboard;
