/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings security page
 */

'use client';

import React, { useState } from 'react';
import {
  Shield,
  Lock,
  Key,
  Activity,
  AlertCircle,
  CheckCircle2,
  Copy,
  Eye,
  EyeOff,
  Plus,
  Trash2,
  Clock,
  Globe,
  Terminal,
  UserCheck,
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
import {
  SettingsStatsCard,
  SettingsActionMenu,
  SettingsStatusBadge,
} from '@/modules/settings';
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

export default function SettingsSecurityPage() {
  const [activeTab, setActiveTab] = useState('general');
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(true);
  const [twoFactorEnforced, setTwoFactorEnforced] = useState(false);
  const [showApiKey, setShowApiKey] = useState<{ [key: string]: boolean }>({});

  // Mock data
  const securitySettings = {
    twoFactorEnabled: true,
    twoFactorEnforced: false,
    passwordPolicy: {
      minLength: 12,
      requireUppercase: true,
      requireLowercase: true,
      requireNumbers: true,
      requireSpecialChars: true,
      expiryDays: 90,
      preventReuse: 5,
    },
    sessionTimeout: 60,
    ipWhitelist: ['192.168.1.0/24', '10.0.0.0/8'],
    allowedDomains: ['acme.com', 'acme.io'],
  };

  const apiKeys = [
    {
      id: '1',
      name: 'Production API',
      key: '',
      prefix: 'sk_live',
      permissions: ['read', 'write'],
      status: 'ACTIVE',
      lastUsedAt: '2024-03-20T15:30:00Z',
      createdAt: '2024-01-15T10:00:00Z',
    },
    {
      id: '2',
      name: 'Development API',
      key: '',
      prefix: 'sk_test',
      permissions: ['read'],
      status: 'ACTIVE',
      lastUsedAt: '2024-03-19T14:20:00Z',
      createdAt: '2024-02-01T10:00:00Z',
    },
    {
      id: '3',
      name: 'Legacy Integration',
      key: '',
      prefix: 'sk_live',
      permissions: ['read', 'write'],
      status: 'INACTIVE',
      lastUsedAt: '2024-02-10T11:00:00Z',
      createdAt: '2023-12-01T10:00:00Z',
    },
  ];

  const auditLogs = [
    {
      id: '1',
      userEmail: 'john.doe@acme.com',
      userName: 'John Doe',
      action: 'User Created',
      resourceType: 'USER',
      resourceId: 'user_123',
      details: 'Created new user: jane.smith@acme.com',
      ipAddress: '192.168.1.100',
      timestamp: '2024-03-20T16:30:00Z',
    },
    {
      id: '2',
      userEmail: 'admin@acme.com',
      userName: 'Admin User',
      action: 'Role Updated',
      resourceType: 'ROLE',
      resourceId: 'role_456',
      details: 'Updated permissions for Project Manager role',
      ipAddress: '192.168.1.50',
      timestamp: '2024-03-20T15:45:00Z',
    },
    {
      id: '3',
      userEmail: 'john.doe@acme.com',
      userName: 'John Doe',
      action: 'API Key Generated',
      resourceType: 'API_KEY',
      resourceId: 'key_789',
      details: 'Generated new API key: Production API',
      ipAddress: '192.168.1.100',
      timestamp: '2024-03-20T14:20:00Z',
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

  const toggleApiKeyVisibility = (keyId: string) => {
    setShowApiKey((prev) => ({ ...prev, [keyId]: !prev[keyId] }));
  };

  const maskApiKey = (key: string) => {
    return `${key.substring(0, 12)}...${key.substring(key.length - 4)}`;
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-3xl font-bold tracking-tight'>Security</h1>
          <p className='text-muted-foreground mt-2'>
            Manage security settings, audit logs, and API access
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='2FA Status'
          value={twoFactorEnabled ? 'Enabled' : 'Disabled'}
          description='Two-factor authentication'
          icon={<Shield className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Active API Keys'
          value={apiKeys.filter((k) => k.status === 'ACTIVE').length}
          description='Currently in use'
          icon={<Key className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Session Timeout'
          value={`${securitySettings.sessionTimeout}m`}
          description='Inactivity timeout'
          icon={<Clock className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Audit Events'
          value='1.2K'
          description='Last 30 days'
          icon={<Activity className='h-4 w-4' />}
        />
      </div>

      {/* Tabs */}
      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-6'
      >
        <TabsList className='grid w-full grid-cols-3 lg:w-auto'>
          <TabsTrigger value='general'>
            <Shield className='h-4 w-4 mr-2' />
            General
          </TabsTrigger>
          <TabsTrigger value='api-keys'>
            <Key className='h-4 w-4 mr-2' />
            API Keys
          </TabsTrigger>
          <TabsTrigger value='audit-logs'>
            <Activity className='h-4 w-4 mr-2' />
            Audit Logs
          </TabsTrigger>
        </TabsList>

        {/* General Security Tab */}
        <TabsContent value='general' className='space-y-6'>
          {/* Two-Factor Authentication */}
          <Card>
            <CardHeader>
              <CardTitle>Two-Factor Authentication</CardTitle>
              <CardDescription>
                Add an extra layer of security to your organization
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='flex items-center justify-between'>
                <div className='space-y-0.5'>
                  <Label className='text-sm font-medium'>
                    Enable 2FA for organization
                  </Label>
                  <p className='text-xs text-muted-foreground'>
                    Allow users to enable two-factor authentication
                  </p>
                </div>
                <Switch
                  checked={twoFactorEnabled}
                  onCheckedChange={setTwoFactorEnabled}
                  className='data-[state=checked]:bg-purple-600'
                />
              </div>

              {twoFactorEnabled && (
                <>
                  <Separator />
                  <div className='flex items-center justify-between'>
                    <div className='space-y-0.5'>
                      <Label className='text-sm font-medium flex items-center gap-2'>
                        Enforce 2FA for all users
                        <Badge variant='secondary' className='text-xs'>
                          Recommended
                        </Badge>
                      </Label>
                      <p className='text-xs text-muted-foreground'>
                        Require all users to enable 2FA for enhanced security
                      </p>
                    </div>
                    <Switch
                      checked={twoFactorEnforced}
                      onCheckedChange={setTwoFactorEnforced}
                      className='data-[state=checked]:bg-purple-600'
                    />
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          {/* Password Policy */}
          <Card>
            <CardHeader>
              <CardTitle>Password Policy</CardTitle>
              <CardDescription>
                Configure password requirements for all users
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='minLength'>Minimum Length</Label>
                  <Input
                    id='minLength'
                    type='number'
                    defaultValue={securitySettings.passwordPolicy.minLength}
                    min={8}
                    max={32}
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='expiryDays'>Password Expiry (days)</Label>
                  <Input
                    id='expiryDays'
                    type='number'
                    defaultValue={securitySettings.passwordPolicy.expiryDays}
                    min={0}
                  />
                </div>
              </div>

              <Separator />

              <div className='space-y-3'>
                <Label>Password Requirements</Label>
                <div className='grid gap-3 md:grid-cols-2'>
                  <div className='flex items-center space-x-2'>
                    <CheckCircle2 className='h-4 w-4 text-green-600' />
                    <span className='text-sm'>Require uppercase letters</span>
                  </div>
                  <div className='flex items-center space-x-2'>
                    <CheckCircle2 className='h-4 w-4 text-green-600' />
                    <span className='text-sm'>Require lowercase letters</span>
                  </div>
                  <div className='flex items-center space-x-2'>
                    <CheckCircle2 className='h-4 w-4 text-green-600' />
                    <span className='text-sm'>Require numbers</span>
                  </div>
                  <div className='flex items-center space-x-2'>
                    <CheckCircle2 className='h-4 w-4 text-green-600' />
                    <span className='text-sm'>Require special characters</span>
                  </div>
                </div>
              </div>

              <Separator />

              <div className='flex justify-end'>
                <Button className='bg-purple-600 hover:bg-purple-700'>
                  Save Policy
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Access Control */}
          <Card>
            <CardHeader>
              <CardTitle>Access Control</CardTitle>
              <CardDescription>
                Configure IP whitelist and domain restrictions
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='space-y-2'>
                <Label htmlFor='sessionTimeout'>
                  Session Timeout (minutes)
                </Label>
                <Input
                  id='sessionTimeout'
                  type='number'
                  defaultValue={securitySettings.sessionTimeout}
                  min={5}
                  max={480}
                />
                <p className='text-xs text-muted-foreground'>
                  Automatically log out users after period of inactivity
                </p>
              </div>

              <Separator />

              <div className='space-y-2'>
                <Label className='flex items-center gap-2'>
                  <Globe className='h-4 w-4 text-muted-foreground' />
                  IP Whitelist
                </Label>
                <div className='flex flex-wrap gap-2'>
                  {securitySettings.ipWhitelist.map((ip, index) => (
                    <Badge
                      key={index}
                      variant='secondary'
                      className='px-3 py-1'
                    >
                      {ip}
                      <button className='ml-2 hover:text-destructive'>
                        <Trash2 className='h-3 w-3' />
                      </button>
                    </Badge>
                  ))}
                  <Button variant='outline' size='sm'>
                    <Plus className='h-4 w-4 mr-1' />
                    Add IP
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* API Keys Tab */}
        <TabsContent value='api-keys' className='space-y-6'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div>
                  <CardTitle>API Keys</CardTitle>
                  <CardDescription>
                    Manage API keys for programmatic access
                  </CardDescription>
                </div>
                <Button className='bg-purple-600 hover:bg-purple-700'>
                  <Plus className='h-4 w-4 mr-2' />
                  Generate Key
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className='space-y-3'>
                {apiKeys.map((apiKey) => (
                  <Card
                    key={apiKey.id}
                    className='hover:shadow-md transition-shadow'
                  >
                    <CardHeader className='pb-3'>
                      <div className='flex items-start justify-between'>
                        <div className='flex items-center gap-3'>
                          <div className='h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center'>
                            <Key className='h-5 w-5 text-purple-600 dark:text-purple-400' />
                          </div>
                          <div>
                            <CardTitle className='text-base flex items-center gap-2'>
                              {apiKey.name}
                              <SettingsStatusBadge status={apiKey.status} />
                            </CardTitle>
                            <p className='text-xs text-muted-foreground'>
                              Last used: {formatDate(apiKey.lastUsedAt)}
                            </p>
                          </div>
                        </div>
                        <SettingsActionMenu
                          items={[
                            {
                              label: 'Copy Key',
                              onClick: () => console.log('Copy', apiKey.id),
                              icon: <Copy className='h-4 w-4' />,
                            },
                            {
                              label: 'Regenerate',
                              onClick: () =>
                                console.log('Regenerate', apiKey.id),
                              icon: <Key className='h-4 w-4' />,
                            },
                            {
                              label: 'Delete',
                              onClick: () => console.log('Delete', apiKey.id),
                              icon: <Trash2 className='h-4 w-4' />,
                              variant: 'destructive',
                              separator: true,
                            },
                          ]}
                        />
                      </div>
                    </CardHeader>
                    <CardContent className='space-y-3'>
                      <div className='flex items-center gap-2'>
                        <Terminal className='h-4 w-4 text-muted-foreground' />
                        <code className='flex-1 bg-muted px-3 py-2 rounded text-sm font-mono'>
                          {showApiKey[apiKey.id]
                            ? apiKey.key
                            : maskApiKey(apiKey.key)}
                        </code>
                        <Button
                          variant='ghost'
                          size='sm'
                          onClick={() => toggleApiKeyVisibility(apiKey.id)}
                        >
                          {showApiKey[apiKey.id] ? (
                            <EyeOff className='h-4 w-4' />
                          ) : (
                            <Eye className='h-4 w-4' />
                          )}
                        </Button>
                      </div>

                      <div className='flex items-center gap-4 text-xs text-muted-foreground'>
                        <div className='flex items-center gap-1'>
                          <Shield className='h-3 w-3' />
                          <span>
                            {apiKey.permissions.join(', ')} permissions
                          </span>
                        </div>
                        <div className='flex items-center gap-1'>
                          <Clock className='h-3 w-3' />
                          <span>Created {formatDate(apiKey.createdAt)}</span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Audit Logs Tab */}
        <TabsContent value='audit-logs' className='space-y-6'>
          <Card>
            <CardHeader>
              <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
                <div>
                  <CardTitle>Audit Logs</CardTitle>
                  <CardDescription>
                    Track all security-related activities
                  </CardDescription>
                </div>
                <div className='flex gap-2'>
                  <Select defaultValue='all'>
                    <SelectTrigger className='w-[150px]'>
                      <SelectValue placeholder='Action' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='all'>All Actions</SelectItem>
                      <SelectItem value='user_created'>User Created</SelectItem>
                      <SelectItem value='role_updated'>Role Updated</SelectItem>
                      <SelectItem value='api_key_generated'>
                        API Key Generated
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className='space-y-3'>
                {auditLogs.map((log) => (
                  <div
                    key={log.id}
                    className='flex items-start gap-4 p-4 rounded-lg border hover:bg-accent transition-colors'
                  >
                    <div className='h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900 flex items-center justify-center flex-shrink-0'>
                      <UserCheck className='h-5 w-5 text-purple-600 dark:text-purple-400' />
                    </div>
                    <div className='flex-1 space-y-1'>
                      <div className='flex items-center gap-2'>
                        <p className='font-medium text-sm'>{log.action}</p>
                        <Badge variant='outline' className='text-xs'>
                          {log.resourceType}
                        </Badge>
                      </div>
                      <p className='text-sm text-muted-foreground'>
                        {log.details}
                      </p>
                      <div className='flex items-center gap-4 text-xs text-muted-foreground'>
                        <span>{log.userName}</span>
                        <span>•</span>
                        <span>{log.ipAddress}</span>
                        <span>•</span>
                        <span>{formatDate(log.timestamp)}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
