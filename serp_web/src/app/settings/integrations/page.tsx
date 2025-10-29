/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings integrations page
 */

'use client';

import React, { useState } from 'react';
import {
  Plug,
  Plus,
  Settings,
  Trash2,
  Check,
  AlertCircle,
  ExternalLink,
  Mail,
  Calendar,
  Users,
  FileText,
  MessageSquare,
  Cloud,
  Database,
  Workflow,
  BarChart,
  Lock,
  Zap,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import { Switch } from '@/shared/components/ui/switch';
import { Label } from '@/shared/components/ui/label';
import { SettingsStatsCard, SettingsActionMenu } from '@/modules/settings';
import { Separator } from '@/shared/components/ui/separator';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';

interface Integration {
  id: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  category:
    | 'communication'
    | 'productivity'
    | 'storage'
    | 'analytics'
    | 'automation';
  status?: 'connected' | 'disconnected' | 'error';
  lastSync?: string;
  isPopular?: boolean;
  isPremium?: boolean;
}

export default function SettingsIntegrationsPage() {
  const [activeTab, setActiveTab] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  // Available integrations
  const availableIntegrations: Integration[] = [
    {
      id: 'gmail',
      name: 'Gmail',
      description: 'Sync emails and automate email workflows',
      icon: <Mail className='h-5 w-5' />,
      category: 'communication',
      isPopular: true,
    },
    {
      id: 'google-calendar',
      name: 'Google Calendar',
      description: 'Sync events and manage schedules',
      icon: <Calendar className='h-5 w-5' />,
      category: 'productivity',
      isPopular: true,
    },
    {
      id: 'slack',
      name: 'Slack',
      description: 'Send notifications and integrate team communication',
      icon: <MessageSquare className='h-5 w-5' />,
      category: 'communication',
      isPopular: true,
    },
    {
      id: 'teams',
      name: 'Microsoft Teams',
      description: 'Collaborate with team members in real-time',
      icon: <Users className='h-5 w-5' />,
      category: 'communication',
    },
    {
      id: 'dropbox',
      name: 'Dropbox',
      description: 'Store and share files securely',
      icon: <Cloud className='h-5 w-5' />,
      category: 'storage',
    },
    {
      id: 'google-drive',
      name: 'Google Drive',
      description: 'Access and manage your Google Drive files',
      icon: <FileText className='h-5 w-5' />,
      category: 'storage',
      isPopular: true,
    },
    {
      id: 'salesforce',
      name: 'Salesforce',
      description: 'Sync CRM data and automate sales workflows',
      icon: <Database className='h-5 w-5' />,
      category: 'productivity',
      isPremium: true,
    },
    {
      id: 'hubspot',
      name: 'HubSpot',
      description: 'Integrate marketing and sales automation',
      icon: <BarChart className='h-5 w-5' />,
      category: 'analytics',
      isPremium: true,
    },
    {
      id: 'zapier',
      name: 'Zapier',
      description: 'Connect with 5000+ apps and automate workflows',
      icon: <Zap className='h-5 w-5' />,
      category: 'automation',
      isPopular: true,
    },
    {
      id: 'make',
      name: 'Make (Integromat)',
      description: 'Build complex automation scenarios',
      icon: <Workflow className='h-5 w-5' />,
      category: 'automation',
    },
  ];

  // Connected integrations
  const connectedIntegrations: Integration[] = [
    {
      id: 'slack',
      name: 'Slack',
      description: 'Send notifications and integrate team communication',
      icon: <MessageSquare className='h-5 w-5' />,
      category: 'communication',
      status: 'connected',
      lastSync: '2024-03-20T16:30:00Z',
    },
    {
      id: 'google-drive',
      name: 'Google Drive',
      description: 'Access and manage your Google Drive files',
      icon: <FileText className='h-5 w-5' />,
      category: 'storage',
      status: 'connected',
      lastSync: '2024-03-20T15:45:00Z',
    },
    {
      id: 'zapier',
      name: 'Zapier',
      description: 'Connect with 5000+ apps and automate workflows',
      icon: <Zap className='h-5 w-5' />,
      category: 'automation',
      status: 'error',
      lastSync: '2024-03-18T10:00:00Z',
    },
  ];

  const categories = [
    {
      value: 'all',
      label: 'All Integrations',
      icon: <Plug className='h-4 w-4' />,
    },
    {
      value: 'communication',
      label: 'Communication',
      icon: <MessageSquare className='h-4 w-4' />,
    },
    {
      value: 'productivity',
      label: 'Productivity',
      icon: <Calendar className='h-4 w-4' />,
    },
    { value: 'storage', label: 'Storage', icon: <Cloud className='h-4 w-4' /> },
    {
      value: 'analytics',
      label: 'Analytics',
      icon: <BarChart className='h-4 w-4' />,
    },
    {
      value: 'automation',
      label: 'Automation',
      icon: <Workflow className='h-4 w-4' />,
    },
  ];

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'connected':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
      case 'error':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  const getStatusIcon = (status?: string) => {
    switch (status) {
      case 'connected':
        return <Check className='h-3 w-3' />;
      case 'error':
        return <AlertCircle className='h-3 w-3' />;
      default:
        return null;
    }
  };

  const filteredIntegrations = availableIntegrations.filter((integration) => {
    const matchesSearch =
      integration.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      integration.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory =
      activeTab === 'all' || integration.category === activeTab;
    return matchesSearch && matchesCategory;
  });

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Integrations</h1>
          <p className='text-muted-foreground mt-2'>
            Connect third-party services to extend functionality
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Connected'
          value={
            connectedIntegrations.filter((i) => i.status === 'connected').length
          }
          description='Active integrations'
          icon={<Check className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Available'
          value={availableIntegrations.length}
          description='Total integrations'
          icon={<Plug className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Errors'
          value={
            connectedIntegrations.filter((i) => i.status === 'error').length
          }
          description='Need attention'
          icon={<AlertCircle className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Premium'
          value={availableIntegrations.filter((i) => i.isPremium).length}
          description='Enterprise features'
          icon={<Lock className='h-4 w-4' />}
        />
      </div>

      {/* Connected Integrations */}
      {connectedIntegrations.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Connected Integrations</CardTitle>
            <CardDescription>Manage your active integrations</CardDescription>
          </CardHeader>
          <CardContent>
            <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
              {connectedIntegrations.map((integration) => (
                <Card
                  key={integration.id}
                  className='hover:shadow-md transition-shadow'
                >
                  <CardHeader className='pb-3'>
                    <div className='flex items-start justify-between'>
                      <div className='flex items-center gap-3'>
                        <div className='h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center'>
                          {integration.icon}
                        </div>
                        <div>
                          <CardTitle className='text-base flex items-center gap-2'>
                            {integration.name}
                          </CardTitle>
                        </div>
                      </div>
                      <SettingsActionMenu
                        items={[
                          {
                            label: 'Configure',
                            onClick: () =>
                              console.log('Configure', integration.id),
                            icon: <Settings className='h-4 w-4' />,
                          },
                          {
                            label: 'Disconnect',
                            onClick: () =>
                              console.log('Disconnect', integration.id),
                            icon: <Trash2 className='h-4 w-4' />,
                            variant: 'destructive',
                            separator: true,
                          },
                        ]}
                      />
                    </div>
                  </CardHeader>
                  <CardContent className='space-y-3'>
                    <p className='text-sm text-muted-foreground'>
                      {integration.description}
                    </p>
                    <div className='flex items-center justify-between'>
                      <Badge
                        className={`text-xs ${getStatusColor(integration.status)}`}
                      >
                        {getStatusIcon(integration.status)}
                        <span className='ml-1 capitalize'>
                          {integration.status}
                        </span>
                      </Badge>
                      {integration.lastSync && (
                        <span className='text-xs text-muted-foreground'>
                          {formatDate(integration.lastSync)}
                        </span>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Available Integrations */}
      <Card>
        <CardHeader>
          <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
            <div>
              <CardTitle>Available Integrations</CardTitle>
              <CardDescription>
                Browse and connect new integrations
              </CardDescription>
            </div>
            <div className='flex gap-2'>
              <Input
                placeholder='Search integrations...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='max-w-xs'
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {/* Category Tabs */}
          <Tabs
            value={activeTab}
            onValueChange={setActiveTab}
            className='space-y-6'
          >
            <TabsList className='grid w-full grid-cols-3 lg:grid-cols-6'>
              {categories.map((category) => (
                <TabsTrigger key={category.value} value={category.value}>
                  {category.icon}
                  <span className='ml-2 hidden md:inline'>
                    {category.label}
                  </span>
                </TabsTrigger>
              ))}
            </TabsList>

            {/* Integration Grid */}
            <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
              {filteredIntegrations.map((integration) => {
                const isConnected = connectedIntegrations.some(
                  (c) => c.id === integration.id
                );

                return (
                  <Card
                    key={integration.id}
                    className='hover:shadow-md transition-shadow'
                  >
                    <CardHeader className='pb-3'>
                      <div className='flex items-start justify-between'>
                        <div className='flex items-center gap-3'>
                          <div className='h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center'>
                            {integration.icon}
                          </div>
                          <div className='flex-1'>
                            <CardTitle className='text-base flex items-center gap-2 flex-wrap'>
                              {integration.name}
                              {integration.isPopular && (
                                <Badge variant='secondary' className='text-xs'>
                                  Popular
                                </Badge>
                              )}
                              {integration.isPremium && (
                                <Badge className='text-xs bg-purple-600'>
                                  <Lock className='h-3 w-3 mr-1' />
                                  Premium
                                </Badge>
                              )}
                            </CardTitle>
                          </div>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent className='space-y-3'>
                      <p className='text-sm text-muted-foreground'>
                        {integration.description}
                      </p>
                      <div className='flex items-center justify-between'>
                        <Badge variant='outline' className='text-xs capitalize'>
                          {integration.category}
                        </Badge>
                        {isConnected ? (
                          <Button
                            variant='outline'
                            size='sm'
                            className='border-green-600 text-green-600 hover:bg-green-50 dark:hover:bg-green-950'
                          >
                            <Check className='h-4 w-4 mr-1' />
                            Connected
                          </Button>
                        ) : (
                          <Button
                            size='sm'
                            className='bg-purple-600 hover:bg-purple-700'
                            disabled={integration.isPremium}
                          >
                            <Plus className='h-4 w-4 mr-1' />
                            Connect
                          </Button>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>

            {filteredIntegrations.length === 0 && (
              <div className='text-center py-12'>
                <Plug className='h-12 w-12 text-muted-foreground mx-auto mb-4' />
                <h3 className='text-lg font-semibold mb-2'>
                  No integrations found
                </h3>
                <p className='text-muted-foreground'>
                  Try adjusting your search or filters
                </p>
              </div>
            )}
          </Tabs>
        </CardContent>
      </Card>

      {/* Custom Integration */}
      <Card>
        <CardHeader>
          <CardTitle>Custom Integration</CardTitle>
          <CardDescription>
            Don't see what you need? Build your own integration using our API
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className='flex flex-col md:flex-row items-start md:items-center justify-between gap-4'>
            <div className='flex items-start gap-4'>
              <div className='h-12 w-12 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center'>
                <Settings className='h-6 w-6 text-purple-600 dark:text-purple-400' />
              </div>
              <div>
                <h3 className='font-semibold mb-1'>API Documentation</h3>
                <p className='text-sm text-muted-foreground'>
                  Learn how to integrate custom applications with our API
                </p>
              </div>
            </div>
            <Button variant='outline'>
              View Docs
              <ExternalLink className='h-4 w-4 ml-2' />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
