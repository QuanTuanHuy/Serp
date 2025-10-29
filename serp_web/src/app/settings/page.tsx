/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings general page (Organization profile)
 */

'use client';

import React, { useState } from 'react';
import { SettingsStatsCard } from '@/modules/settings/components/shared/SettingsStatsCard';
import {
  Building2,
  Users,
  Calendar,
  Crown,
  Settings as SettingsIcon,
  Save,
  Upload,
  Globe,
  Mail,
  Phone,
  MapPin,
  Palette,
  Clock,
  DollarSign,
  Languages,
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
import { Label } from '@/shared/components/ui/label';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Separator } from '@/shared/components/ui/separator';
import { Textarea } from '@/shared/components/ui/textarea';

export default function SettingsGeneralPage() {
  const [activeTab, setActiveTab] = useState('profile');

  // Mock data - will be replaced with actual API calls
  const organizationData = {
    name: 'Acme Corporation',
    slug: 'acme-corp',
    email: 'contact@acme.com',
    phone: '+1 (555) 123-4567',
    website: 'https://acme.com',
    address: '123 Business Street',
    city: 'San Francisco',
    state: 'CA',
    country: 'United States',
    zipCode: '94102',
    taxId: 'TAX-123456',
    industry: 'Technology',
    companySize: '51-200',
    description: 'Leading technology solutions provider',
    logoUrl: '',
    createdAt: '2024-01-15',
  };

  return (
    <div className='space-y-6'>
      {/* Page Header */}
      <div>
        <h1 className='text-3xl font-bold tracking-tight'>General Settings</h1>
        <p className='text-muted-foreground mt-2'>
          Manage your organization profile, branding, and preferences
        </p>
      </div>

      {/* Stats Grid */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Users'
          value={45}
          description='Active members'
          icon={<Users className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Departments'
          value={8}
          description='Active departments'
          icon={<Building2 className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Subscription'
          value='Professional'
          description='Active plan'
          icon={<Crown className='h-4 w-4' />}
        />

        <SettingsStatsCard
          title='Member Since'
          value='Jan 2024'
          description='Organization age'
          icon={<Calendar className='h-4 w-4' />}
        />
      </div>

      {/* Tabs for different settings sections */}
      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-6'
      >
        <TabsList className='grid w-full grid-cols-3 lg:w-auto'>
          <TabsTrigger value='profile'>
            <Building2 className='h-4 w-4 mr-2' />
            Profile
          </TabsTrigger>
          <TabsTrigger value='branding'>
            <Palette className='h-4 w-4 mr-2' />
            Branding
          </TabsTrigger>
          <TabsTrigger value='preferences'>
            <SettingsIcon className='h-4 w-4 mr-2' />
            Preferences
          </TabsTrigger>
        </TabsList>

        {/* Profile Tab */}
        <TabsContent value='profile' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Organization Profile</CardTitle>
              <CardDescription>
                Update your organization's basic information and contact details
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-6'>
              {/* Logo Upload */}
              <div className='space-y-2'>
                <Label>Organization Logo</Label>
                <div className='flex items-center gap-4'>
                  <div className='h-20 w-20 rounded-lg border-2 border-dashed border-muted-foreground/25 flex items-center justify-center bg-muted/50'>
                    <Building2 className='h-8 w-8 text-muted-foreground' />
                  </div>
                  <div className='space-y-2'>
                    <Button variant='outline' size='sm'>
                      <Upload className='h-4 w-4 mr-2' />
                      Upload Logo
                    </Button>
                    <p className='text-xs text-muted-foreground'>
                      Recommended: 400x400px, PNG or JPG
                    </p>
                  </div>
                </div>
              </div>

              <Separator />

              {/* Basic Information */}
              <div className='grid gap-4 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='name'>Organization Name *</Label>
                  <Input
                    id='name'
                    defaultValue={organizationData.name}
                    placeholder='Enter organization name'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='slug'>Slug</Label>
                  <Input
                    id='slug'
                    defaultValue={organizationData.slug}
                    placeholder='organization-slug'
                    disabled
                  />
                  <p className='text-xs text-muted-foreground'>
                    URL-friendly identifier (cannot be changed)
                  </p>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='email'>
                    <Mail className='h-4 w-4 inline mr-1' />
                    Email *
                  </Label>
                  <Input
                    id='email'
                    type='email'
                    defaultValue={organizationData.email}
                    placeholder='contact@example.com'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='phone'>
                    <Phone className='h-4 w-4 inline mr-1' />
                    Phone
                  </Label>
                  <Input
                    id='phone'
                    defaultValue={organizationData.phone}
                    placeholder='+1 (555) 000-0000'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='website'>
                    <Globe className='h-4 w-4 inline mr-1' />
                    Website
                  </Label>
                  <Input
                    id='website'
                    defaultValue={organizationData.website}
                    placeholder='https://example.com'
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='industry'>Industry</Label>
                  <Select defaultValue={organizationData.industry}>
                    <SelectTrigger>
                      <SelectValue placeholder='Select industry' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='Technology'>Technology</SelectItem>
                      <SelectItem value='Finance'>Finance</SelectItem>
                      <SelectItem value='Healthcare'>Healthcare</SelectItem>
                      <SelectItem value='Education'>Education</SelectItem>
                      <SelectItem value='Retail'>Retail</SelectItem>
                      <SelectItem value='Manufacturing'>
                        Manufacturing
                      </SelectItem>
                      <SelectItem value='Other'>Other</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='companySize'>Company Size</Label>
                  <Select defaultValue={organizationData.companySize}>
                    <SelectTrigger>
                      <SelectValue placeholder='Select size' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='1-10'>1-10 employees</SelectItem>
                      <SelectItem value='11-50'>11-50 employees</SelectItem>
                      <SelectItem value='51-200'>51-200 employees</SelectItem>
                      <SelectItem value='201-500'>201-500 employees</SelectItem>
                      <SelectItem value='501-1000'>
                        501-1000 employees
                      </SelectItem>
                      <SelectItem value='1001+'>1001+ employees</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='taxId'>Tax ID</Label>
                  <Input
                    id='taxId'
                    defaultValue={organizationData.taxId}
                    placeholder='Tax identification number'
                  />
                </div>
              </div>

              <Separator />

              {/* Address Information */}
              <div>
                <h3 className='text-lg font-semibold mb-4'>
                  <MapPin className='h-5 w-5 inline mr-2' />
                  Address Information
                </h3>
                <div className='grid gap-4 md:grid-cols-2'>
                  <div className='space-y-2 md:col-span-2'>
                    <Label htmlFor='address'>Street Address</Label>
                    <Input
                      id='address'
                      defaultValue={organizationData.address}
                      placeholder='123 Main Street'
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='city'>City</Label>
                    <Input
                      id='city'
                      defaultValue={organizationData.city}
                      placeholder='San Francisco'
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='state'>State/Province</Label>
                    <Input
                      id='state'
                      defaultValue={organizationData.state}
                      placeholder='CA'
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='country'>Country</Label>
                    <Select defaultValue={organizationData.country}>
                      <SelectTrigger>
                        <SelectValue placeholder='Select country' />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value='United States'>
                          United States
                        </SelectItem>
                        <SelectItem value='Canada'>Canada</SelectItem>
                        <SelectItem value='United Kingdom'>
                          United Kingdom
                        </SelectItem>
                        <SelectItem value='Germany'>Germany</SelectItem>
                        <SelectItem value='France'>France</SelectItem>
                        <SelectItem value='Japan'>Japan</SelectItem>
                        <SelectItem value='Other'>Other</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='zipCode'>ZIP/Postal Code</Label>
                    <Input
                      id='zipCode'
                      defaultValue={organizationData.zipCode}
                      placeholder='94102'
                    />
                  </div>
                </div>
              </div>

              <Separator />

              {/* Description */}
              <div className='space-y-2'>
                <Label htmlFor='description'>Description</Label>
                <Textarea
                  id='description'
                  defaultValue={organizationData.description}
                  placeholder='Brief description of your organization'
                  rows={4}
                />
                <p className='text-xs text-muted-foreground'>
                  Brief description about your organization (optional)
                </p>
              </div>

              {/* Action Buttons */}
              <div className='flex justify-end gap-2 pt-4'>
                <Button variant='outline'>Cancel</Button>
                <Button className='bg-purple-600 hover:bg-purple-700'>
                  <Save className='h-4 w-4 mr-2' />
                  Save Changes
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Branding Tab */}
        <TabsContent value='branding' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Branding & Appearance</CardTitle>
              <CardDescription>
                Customize the look and feel of your organization's interface
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-6'>
              <div className='grid gap-6 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='primaryColor'>Primary Color</Label>
                  <div className='flex gap-2'>
                    <Input
                      id='primaryColor'
                      type='color'
                      defaultValue='#7c3aed'
                      className='h-10 w-20'
                    />
                    <Input defaultValue='#7c3aed' placeholder='#7c3aed' />
                  </div>
                  <p className='text-xs text-muted-foreground'>
                    Main brand color for buttons, links, etc.
                  </p>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='secondaryColor'>Secondary Color</Label>
                  <div className='flex gap-2'>
                    <Input
                      id='secondaryColor'
                      type='color'
                      defaultValue='#a78bfa'
                      className='h-10 w-20'
                    />
                    <Input defaultValue='#a78bfa' placeholder='#a78bfa' />
                  </div>
                  <p className='text-xs text-muted-foreground'>
                    Accent color for highlights and badges
                  </p>
                </div>
              </div>

              <Separator />

              <div className='space-y-4'>
                <h3 className='text-sm font-semibold'>Logo & Icons</h3>
                <div className='grid gap-4 md:grid-cols-2'>
                  <div className='space-y-2'>
                    <Label>Main Logo</Label>
                    <div className='border-2 border-dashed rounded-lg p-8 text-center'>
                      <Upload className='h-8 w-8 mx-auto text-muted-foreground mb-2' />
                      <p className='text-sm text-muted-foreground mb-2'>
                        Upload main logo
                      </p>
                      <Button variant='outline' size='sm'>
                        Choose File
                      </Button>
                    </div>
                  </div>

                  <div className='space-y-2'>
                    <Label>Favicon</Label>
                    <div className='border-2 border-dashed rounded-lg p-8 text-center'>
                      <Upload className='h-8 w-8 mx-auto text-muted-foreground mb-2' />
                      <p className='text-sm text-muted-foreground mb-2'>
                        Upload favicon (32x32)
                      </p>
                      <Button variant='outline' size='sm'>
                        Choose File
                      </Button>
                    </div>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className='flex justify-end gap-2 pt-4'>
                <Button variant='outline'>Reset to Default</Button>
                <Button className='bg-purple-600 hover:bg-purple-700'>
                  <Save className='h-4 w-4 mr-2' />
                  Save Branding
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Preferences Tab */}
        <TabsContent value='preferences' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Organization Preferences</CardTitle>
              <CardDescription>
                Configure regional and localization settings
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-6'>
              <div className='grid gap-4 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='timezone'>
                    <Clock className='h-4 w-4 inline mr-1' />
                    Timezone
                  </Label>
                  <Select defaultValue='America/Los_Angeles'>
                    <SelectTrigger>
                      <SelectValue placeholder='Select timezone' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='America/Los_Angeles'>
                        Pacific Time (PT)
                      </SelectItem>
                      <SelectItem value='America/Denver'>
                        Mountain Time (MT)
                      </SelectItem>
                      <SelectItem value='America/Chicago'>
                        Central Time (CT)
                      </SelectItem>
                      <SelectItem value='America/New_York'>
                        Eastern Time (ET)
                      </SelectItem>
                      <SelectItem value='Europe/London'>
                        London (GMT)
                      </SelectItem>
                      <SelectItem value='Europe/Paris'>Paris (CET)</SelectItem>
                      <SelectItem value='Asia/Tokyo'>Tokyo (JST)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='dateFormat'>Date Format</Label>
                  <Select defaultValue='MM/DD/YYYY'>
                    <SelectTrigger>
                      <SelectValue placeholder='Select date format' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='MM/DD/YYYY'>MM/DD/YYYY</SelectItem>
                      <SelectItem value='DD/MM/YYYY'>DD/MM/YYYY</SelectItem>
                      <SelectItem value='YYYY-MM-DD'>YYYY-MM-DD</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='timeFormat'>Time Format</Label>
                  <Select defaultValue='12h'>
                    <SelectTrigger>
                      <SelectValue placeholder='Select time format' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='12h'>12-hour (2:30 PM)</SelectItem>
                      <SelectItem value='24h'>24-hour (14:30)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='weekStartsOn'>Week Starts On</Label>
                  <Select defaultValue='sunday'>
                    <SelectTrigger>
                      <SelectValue placeholder='Select day' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='sunday'>Sunday</SelectItem>
                      <SelectItem value='monday'>Monday</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='currency'>
                    <DollarSign className='h-4 w-4 inline mr-1' />
                    Currency
                  </Label>
                  <Select defaultValue='USD'>
                    <SelectTrigger>
                      <SelectValue placeholder='Select currency' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='USD'>USD ($)</SelectItem>
                      <SelectItem value='EUR'>EUR (€)</SelectItem>
                      <SelectItem value='GBP'>GBP (£)</SelectItem>
                      <SelectItem value='JPY'>JPY (¥)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='language'>
                    <Languages className='h-4 w-4 inline mr-1' />
                    Language
                  </Label>
                  <Select defaultValue='en'>
                    <SelectTrigger>
                      <SelectValue placeholder='Select language' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='en'>English</SelectItem>
                      <SelectItem value='es'>Español</SelectItem>
                      <SelectItem value='fr'>Français</SelectItem>
                      <SelectItem value='de'>Deutsch</SelectItem>
                      <SelectItem value='ja'>日本語</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Action Buttons */}
              <div className='flex justify-end gap-2 pt-4'>
                <Button variant='outline'>Reset to Default</Button>
                <Button className='bg-purple-600 hover:bg-purple-700'>
                  <Save className='h-4 w-4 mr-2' />
                  Save Preferences
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
