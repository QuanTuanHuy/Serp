/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings general page
 */

'use client';

import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import {
  Building2,
  Calendar,
  Clock,
  Crown,
  DollarSign,
  Globe,
  Languages,
  Mail,
  MapPin,
  Palette,
  Phone,
  Save,
  Settings as SettingsIcon,
  Users,
} from 'lucide-react';

import { getErrorMessage } from '@/lib/store/api';
import { SettingsStatsCard } from '@/modules/settings/components/shared/SettingsStatsCard';
import {
  useGetOrganizationSettingsQuery,
  useUpdateOrganizationSettingsMutation,
} from '@/modules/settings/services/settingsApi';
import type {
  DateFormat,
  TimeFormat,
  UpdateOrganizationSettingsRequest,
  WeekDay,
} from '@/modules/settings/types/general.types';
import { Alert, AlertDescription } from '@/shared/components/ui/alert';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Separator } from '@/shared/components/ui/separator';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Textarea } from '@/shared/components/ui/textarea';

const DEFAULT_FORM: UpdateOrganizationSettingsRequest = {
  name: '',
  email: '',
  phoneNumber: '',
  website: '',
  address: '',
  city: '',
  state: '',
  country: '',
  zipCode: '',
  taxId: '',
  industry: '',
  employeeCount: null,
  description: '',
  logoUrl: '',
  faviconUrl: '',
  primaryColor: '#7c3aed',
  secondaryColor: '#a78bfa',
  timezone: 'Asia/Ho_Chi_Minh',
  dateFormat: 'DD/MM/YYYY',
  timeFormat: '24h',
  weekStartsOn: 'monday',
  currency: 'VND',
  language: 'vi',
};

const INDUSTRIES = [
  'Technology',
  'Finance',
  'Healthcare',
  'Education',
  'Retail',
  'Manufacturing',
  'Other',
];

const COUNTRIES = [
  'Vietnam',
  'United States',
  'Canada',
  'United Kingdom',
  'Germany',
  'France',
  'Japan',
  'Other',
];

const TIMEZONES = [
  'Asia/Ho_Chi_Minh',
  'America/Los_Angeles',
  'America/New_York',
  'Europe/London',
  'Europe/Paris',
  'Asia/Tokyo',
];

const CURRENCIES = ['VND', 'USD', 'EUR', 'GBP', 'JPY'];

const LANGUAGES = [
  { value: 'vi', label: 'Vietnamese' },
  { value: 'en', label: 'English' },
  { value: 'fr', label: 'French' },
  { value: 'de', label: 'German' },
  { value: 'ja', label: 'Japanese' },
];

const toInputValue = (value: string | number | null | undefined) =>
  value == null ? '' : String(value);

const formatMemberSince = (createdAt?: number | null) => {
  if (!createdAt) {
    return 'N/A';
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    year: 'numeric',
  }).format(new Date(createdAt));
};

export default function SettingsGeneralPage() {
  const [activeTab, setActiveTab] = useState('profile');
  const [form, setForm] =
    useState<UpdateOrganizationSettingsRequest>(DEFAULT_FORM);
  const { data, error, isLoading, isFetching } =
    useGetOrganizationSettingsQuery();
  const [updateSettings, { isLoading: isSaving }] =
    useUpdateOrganizationSettingsMutation();

  useEffect(() => {
    if (!data) {
      return;
    }

    setForm({
      name: data.name ?? '',
      email: data.email ?? '',
      phoneNumber: data.phoneNumber ?? '',
      website: data.website ?? '',
      address: data.address ?? '',
      city: data.city ?? '',
      state: data.state ?? '',
      country: data.country ?? '',
      zipCode: data.zipCode ?? '',
      taxId: data.taxId ?? '',
      industry: data.industry ?? '',
      employeeCount: data.employeeCount ?? null,
      description: data.description ?? '',
      logoUrl: data.logoUrl ?? '',
      faviconUrl: data.faviconUrl ?? '',
      primaryColor: data.primaryColor ?? '#7c3aed',
      secondaryColor: data.secondaryColor ?? '#a78bfa',
      timezone: data.timezone ?? 'Asia/Ho_Chi_Minh',
      dateFormat: data.dateFormat ?? 'DD/MM/YYYY',
      timeFormat: data.timeFormat ?? '24h',
      weekStartsOn: data.weekStartsOn ?? 'monday',
      currency: data.currency ?? 'VND',
      language: data.language ?? 'vi',
    });
  }, [data]);

  const updateField = <K extends keyof UpdateOrganizationSettingsRequest>(
    key: K,
    value: UpdateOrganizationSettingsRequest[K]
  ) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const handleSave = async () => {
    try {
      await updateSettings(form).unwrap();
      toast.success('Organization settings saved.');
    } catch (saveError) {
      toast.error('Failed to save organization settings', {
        description: getErrorMessage(saveError),
      });
    }
  };

  if (isLoading) {
    return <div className='text-muted-foreground'>Loading settings...</div>;
  }

  if (error) {
    return (
      <Alert variant='destructive'>
        <AlertDescription>{getErrorMessage(error)}</AlertDescription>
      </Alert>
    );
  }

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-3xl font-bold tracking-tight'>General Settings</h1>
        <p className='text-muted-foreground mt-2'>
          Manage your organization profile, branding, and preferences
        </p>
      </div>

      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-4'>
        <SettingsStatsCard
          title='Total Users'
          value={data?.summary?.totalUsers ?? 'N/A'}
          description='Active members'
          icon={<Users className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='Departments'
          value={data?.summary?.totalDepartments ?? 'N/A'}
          description='Active departments'
          icon={<Building2 className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='Subscription'
          value={data?.summary?.subscriptionPlan ?? 'N/A'}
          description='Active plan'
          icon={<Crown className='h-4 w-4' />}
        />
        <SettingsStatsCard
          title='Member Since'
          value={formatMemberSince(data?.createdAt)}
          description='Organization age'
          icon={<Calendar className='h-4 w-4' />}
        />
      </div>

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

        <TabsContent value='profile' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Organization Profile</CardTitle>
              <CardDescription>
                Update basic information and contact details
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-6'>
              <div className='grid gap-4 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='name'>Organization Name *</Label>
                  <Input
                    id='name'
                    value={toInputValue(form.name)}
                    onChange={(event) =>
                      updateField('name', event.target.value)
                    }
                    placeholder='Enter organization name'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='code'>Code</Label>
                  <Input id='code' value={data?.code ?? ''} disabled />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='email'>
                    <Mail className='h-4 w-4 inline mr-1' />
                    Email
                  </Label>
                  <Input
                    id='email'
                    type='email'
                    value={toInputValue(form.email)}
                    onChange={(event) =>
                      updateField('email', event.target.value)
                    }
                    placeholder='contact@example.com'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='phoneNumber'>
                    <Phone className='h-4 w-4 inline mr-1' />
                    Phone
                  </Label>
                  <Input
                    id='phoneNumber'
                    value={toInputValue(form.phoneNumber)}
                    onChange={(event) =>
                      updateField('phoneNumber', event.target.value)
                    }
                    placeholder='+84 000 000 000'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='website'>
                    <Globe className='h-4 w-4 inline mr-1' />
                    Website
                  </Label>
                  <Input
                    id='website'
                    value={toInputValue(form.website)}
                    onChange={(event) =>
                      updateField('website', event.target.value)
                    }
                    placeholder='https://example.com'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='industry'>Industry</Label>
                  <Select
                    value={toInputValue(form.industry)}
                    onValueChange={(value) => updateField('industry', value)}
                  >
                    <SelectTrigger id='industry'>
                      <SelectValue placeholder='Select industry' />
                    </SelectTrigger>
                    <SelectContent>
                      {INDUSTRIES.map((industry) => (
                        <SelectItem key={industry} value={industry}>
                          {industry}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='employeeCount'>Employee Count</Label>
                  <Input
                    id='employeeCount'
                    type='number'
                    min={0}
                    value={toInputValue(form.employeeCount)}
                    onChange={(event) =>
                      updateField(
                        'employeeCount',
                        event.target.value ? Number(event.target.value) : null
                      )
                    }
                    placeholder='120'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='taxId'>Tax ID</Label>
                  <Input
                    id='taxId'
                    value={toInputValue(form.taxId)}
                    onChange={(event) =>
                      updateField('taxId', event.target.value)
                    }
                    placeholder='Tax identification number'
                  />
                </div>
              </div>

              <Separator />

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
                      value={toInputValue(form.address)}
                      onChange={(event) =>
                        updateField('address', event.target.value)
                      }
                      placeholder='123 Main Street'
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='city'>City</Label>
                    <Input
                      id='city'
                      value={toInputValue(form.city)}
                      onChange={(event) =>
                        updateField('city', event.target.value)
                      }
                      placeholder='Hanoi'
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='state'>State/Province</Label>
                    <Input
                      id='state'
                      value={toInputValue(form.state)}
                      onChange={(event) =>
                        updateField('state', event.target.value)
                      }
                      placeholder='HN'
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='country'>Country</Label>
                    <Select
                      value={toInputValue(form.country)}
                      onValueChange={(value) => updateField('country', value)}
                    >
                      <SelectTrigger id='country'>
                        <SelectValue placeholder='Select country' />
                      </SelectTrigger>
                      <SelectContent>
                        {COUNTRIES.map((country) => (
                          <SelectItem key={country} value={country}>
                            {country}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='zipCode'>ZIP/Postal Code</Label>
                    <Input
                      id='zipCode'
                      value={toInputValue(form.zipCode)}
                      onChange={(event) =>
                        updateField('zipCode', event.target.value)
                      }
                      placeholder='100000'
                    />
                  </div>
                </div>
              </div>

              <Separator />

              <div className='space-y-2'>
                <Label htmlFor='description'>Description</Label>
                <Textarea
                  id='description'
                  value={toInputValue(form.description)}
                  onChange={(event) =>
                    updateField('description', event.target.value)
                  }
                  placeholder='Brief description of your organization'
                  rows={4}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='branding' className='space-y-6'>
          <Card>
            <CardHeader>
              <CardTitle>Branding & Appearance</CardTitle>
              <CardDescription>
                Customize organization branding shown across the app
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-6'>
              <div className='grid gap-4 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='logoUrl'>Logo URL</Label>
                  <Input
                    id='logoUrl'
                    value={toInputValue(form.logoUrl)}
                    onChange={(event) =>
                      updateField('logoUrl', event.target.value)
                    }
                    placeholder='https://example.com/logo.png'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='faviconUrl'>Favicon URL</Label>
                  <Input
                    id='faviconUrl'
                    value={toInputValue(form.faviconUrl)}
                    onChange={(event) =>
                      updateField('faviconUrl', event.target.value)
                    }
                    placeholder='https://example.com/favicon.ico'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='primaryColor'>Primary Color</Label>
                  <div className='flex gap-2'>
                    <Input
                      id='primaryColor'
                      type='color'
                      value={toInputValue(form.primaryColor) || '#7c3aed'}
                      onChange={(event) =>
                        updateField('primaryColor', event.target.value)
                      }
                      className='h-10 w-20'
                    />
                    <Input
                      value={toInputValue(form.primaryColor)}
                      onChange={(event) =>
                        updateField('primaryColor', event.target.value)
                      }
                      placeholder='#7c3aed'
                    />
                  </div>
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='secondaryColor'>Secondary Color</Label>
                  <div className='flex gap-2'>
                    <Input
                      id='secondaryColor'
                      type='color'
                      value={toInputValue(form.secondaryColor) || '#a78bfa'}
                      onChange={(event) =>
                        updateField('secondaryColor', event.target.value)
                      }
                      className='h-10 w-20'
                    />
                    <Input
                      value={toInputValue(form.secondaryColor)}
                      onChange={(event) =>
                        updateField('secondaryColor', event.target.value)
                      }
                      placeholder='#a78bfa'
                    />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

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
                  <Select
                    value={toInputValue(form.timezone)}
                    onValueChange={(value) => updateField('timezone', value)}
                  >
                    <SelectTrigger id='timezone'>
                      <SelectValue placeholder='Select timezone' />
                    </SelectTrigger>
                    <SelectContent>
                      {TIMEZONES.map((timezone) => (
                        <SelectItem key={timezone} value={timezone}>
                          {timezone}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='dateFormat'>Date Format</Label>
                  <Select
                    value={(form.dateFormat as DateFormat) ?? 'DD/MM/YYYY'}
                    onValueChange={(value: DateFormat) =>
                      updateField('dateFormat', value)
                    }
                  >
                    <SelectTrigger id='dateFormat'>
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
                  <Select
                    value={(form.timeFormat as TimeFormat) ?? '24h'}
                    onValueChange={(value: TimeFormat) =>
                      updateField('timeFormat', value)
                    }
                  >
                    <SelectTrigger id='timeFormat'>
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
                  <Select
                    value={(form.weekStartsOn as WeekDay) ?? 'monday'}
                    onValueChange={(value: WeekDay) =>
                      updateField('weekStartsOn', value)
                    }
                  >
                    <SelectTrigger id='weekStartsOn'>
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
                  <Select
                    value={toInputValue(form.currency)}
                    onValueChange={(value) => updateField('currency', value)}
                  >
                    <SelectTrigger id='currency'>
                      <SelectValue placeholder='Select currency' />
                    </SelectTrigger>
                    <SelectContent>
                      {CURRENCIES.map((currency) => (
                        <SelectItem key={currency} value={currency}>
                          {currency}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='language'>
                    <Languages className='h-4 w-4 inline mr-1' />
                    Language
                  </Label>
                  <Select
                    value={toInputValue(form.language)}
                    onValueChange={(value) => updateField('language', value)}
                  >
                    <SelectTrigger id='language'>
                      <SelectValue placeholder='Select language' />
                    </SelectTrigger>
                    <SelectContent>
                      {LANGUAGES.map((language) => (
                        <SelectItem key={language.value} value={language.value}>
                          {language.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <div className='flex justify-end gap-2'>
        <Button onClick={handleSave} disabled={isSaving || isFetching}>
          <Save className='h-4 w-4 mr-2' />
          {isSaving ? 'Saving...' : 'Save Changes'}
        </Button>
      </div>
    </div>
  );
}
