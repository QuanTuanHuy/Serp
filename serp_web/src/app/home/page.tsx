/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Home page for authenticated users
 */

'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useMemo } from 'react';
import { ModuleShowcase } from '@/modules/account/components/ModuleShowcase';
import { useAuth } from '@/modules/account/hooks/useAuth';
import { useModules } from '@/modules/account/hooks/useModules';
import { Header } from '@/shared/components/Header';
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/shared/components/ui/alert';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent } from '@/shared/components/ui/card';
import {
  ORGANIZATION_ADMIN_ROLES,
  SYSTEM_ADMIN_ROLES,
} from '@/shared/types/role.types';
import {
  AlertTriangle,
  ArrowRight,
  Compass,
  Loader2,
  type LucideIcon,
  Settings2,
  Shield,
  UserRound,
} from 'lucide-react';

const WORKSPACE_GUIDANCE = [
  {
    title: 'Open the module that matches the task',
    description:
      'Keep the first click intentional so users land in the right workspace instead of browsing multiple menus.',
  },
  {
    title: 'Keep profile and organization data updated',
    description:
      'Correct names, emails, and workspace settings make notifications and permissions easier to trust.',
  },
  {
    title: 'Separate admin work from daily operations',
    description:
      'Users should immediately know which tools are for execution and which ones are for configuration or governance.',
  },
] as const;

interface OverviewStat {
  label: string;
  value: string;
  detail: string;
}

interface QuickAction {
  title: string;
  description: string;
  href: string;
  actionLabel: string;
  icon: LucideIcon;
}

function hasAnyRole(userRoles: string[] | undefined, allowedRoles: string[]) {
  return allowedRoles.some((role) => userRoles?.includes(role));
}

function formatRoleLabel(role: string) {
  return role
    .toLowerCase()
    .split('_')
    .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1))
    .join(' ');
}

function getPreferredName({
  fullName,
  firstName,
  email,
}: {
  fullName?: string;
  firstName?: string;
  email?: string;
}) {
  if (firstName) return firstName;
  if (fullName) return fullName.split(' ')[0] || fullName;
  if (email) return email.split('@')[0];

  return 'there';
}

function HomePageLoadingState() {
  return (
    <div className='min-h-screen bg-background'>
      <div className='flex min-h-screen items-center justify-center px-4'>
        <div className='flex max-w-sm flex-col items-center gap-4 text-center'>
          <div className='rounded-full border border-primary/15 bg-primary/10 p-4'>
            <Loader2 className='h-8 w-8 animate-spin text-primary' />
          </div>
          <div className='space-y-2'>
            <h1 className='text-xl font-semibold tracking-tight'>
              Preparing your workspace
            </h1>
            <p className='text-sm leading-6 text-muted-foreground'>
              We are checking your session and loading the modules available to
              you.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

function QuickActionCard({ item }: { item: QuickAction }) {
  const Icon = item.icon;

  return (
    <Link
      href={item.href}
      className='group block rounded-2xl border border-border/60 bg-background/80 p-5 transition-all duration-200 hover:-translate-y-1 hover:border-primary/35 hover:shadow-lg'
    >
      <div className='flex items-start gap-4'>
        <div className='rounded-2xl bg-primary/10 p-3 text-primary'>
          <Icon className='h-5 w-5' />
        </div>
        <div className='min-w-0 flex-1'>
          <h3 className='font-semibold tracking-tight'>{item.title}</h3>
          <p className='mt-2 text-sm leading-6 text-muted-foreground'>
            {item.description}
          </p>
          <div className='mt-4 flex items-center gap-2 text-sm font-medium text-primary'>
            {item.actionLabel}
            <ArrowRight className='h-4 w-4 transition-transform group-hover:translate-x-1' />
          </div>
        </div>
      </div>
    </Link>
  );
}

function WorkspaceGuidanceItem({
  item,
  index,
}: {
  item: (typeof WORKSPACE_GUIDANCE)[number];
  index: number;
}) {
  return (
    <div className='flex items-start gap-4'>
      <div className='flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-semibold text-primary'>
        {index + 1}
      </div>
      <div>
        <h3 className='font-medium tracking-tight'>{item.title}</h3>
        <p className='mt-1 text-sm leading-6 text-muted-foreground'>
          {item.description}
        </p>
      </div>
    </div>
  );
}

export default function HomePage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();
  const {
    modules,
    isLoading: modulesLoading,
    error: modulesError,
    hasModules,
  } = useModules();

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.replace('/auth');
    }
  }, [authLoading, isAuthenticated, router]);

  const roleFlags = useMemo(
    () => ({
      isSystemAdmin: hasAnyRole(user?.roles, SYSTEM_ADMIN_ROLES),
      isOrgAdmin: hasAnyRole(user?.roles, ORGANIZATION_ADMIN_ROLES),
    }),
    [user?.roles]
  );

  const overview = useMemo(() => {
    const activeModules = modules.filter((module) => module.isActive).length;
    const adminModules = modules.filter((module) => module.isAdmin).length;
    const firstAvailableModule =
      modules.find((module) => module.isActive) ?? modules[0] ?? null;
    const primaryRole = user?.roles?.[0];

    return {
      availableModules: modules.length,
      activeModules,
      adminModules,
      firstAvailableModule,
      greetingName: getPreferredName({
        fullName: user?.fullName,
        firstName: user?.firstName,
        email: user?.email,
      }),
      organizationName: user?.organizationName,
      roleLabel: primaryRole
        ? formatRoleLabel(primaryRole)
        : 'Workspace member',
      roleCount: user?.roles?.length ?? 0,
    };
  }, [modules, user]);

  const quickActions = useMemo<QuickAction[]>(() => {
    const items: QuickAction[] = [
      {
        title: overview.firstAvailableModule
          ? `Open ${overview.firstAvailableModule.name}`
          : 'Explore applications',
        description: overview.firstAvailableModule
          ? 'Jump straight into the most relevant workspace available to you right now.'
          : 'Browse the catalog and discover which tools can be enabled next.',
        href: overview.firstAvailableModule?.href ?? '/apps',
        actionLabel: overview.firstAvailableModule
          ? 'Continue now'
          : 'Browse apps',
        icon: Compass,
      },
      {
        title: 'Review your profile',
        description:
          'Update personal details, preferences, and account information in one place.',
        href: '/profile',
        actionLabel: 'View profile',
        icon: UserRound,
      },
    ];

    if (roleFlags.isOrgAdmin) {
      items.push({
        title: 'Manage organization settings',
        description:
          'Adjust workspace configuration, structure, and organization-level preferences.',
        href: '/settings',
        actionLabel: 'Open settings',
        icon: Settings2,
      });
    }

    if (roleFlags.isSystemAdmin) {
      items.push({
        title: 'Open system administration',
        description:
          'Review global modules, roles, and platform-wide control surfaces.',
        href: '/admin',
        actionLabel: 'Open admin',
        icon: Shield,
      });
    }

    return items;
  }, [
    overview.firstAvailableModule,
    roleFlags.isOrgAdmin,
    roleFlags.isSystemAdmin,
  ]);

  const isPreparingWorkspace = authLoading && !isAuthenticated;

  if (isPreparingWorkspace) {
    return <HomePageLoadingState />;
  }

  if (!isAuthenticated) {
    return <div className='min-h-screen bg-background' />;
  }

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='relative overflow-hidden'>
        <div className='absolute inset-x-0 top-0 -z-10 h-[28rem] bg-gradient-to-b from-primary/10 via-background to-background' />
        <div className='absolute left-0 top-24 -z-10 h-64 w-64 rounded-full bg-primary/10 blur-3xl' />
        <div className='absolute right-0 top-16 -z-10 h-72 w-72 rounded-full bg-sky-500/10 blur-3xl' />

        <div className='container mx-auto px-4 py-8 md:py-10'>
          <section>
            <Card className='overflow-hidden border-border/60 bg-background/90 shadow-lg'>
              <CardContent className='p-6 md:p-8'>
                <div className='max-w-3xl'>
                  <p className='text-sm font-medium text-primary'>
                    Welcome back
                  </p>
                  <h1 className='mt-2 text-3xl font-semibold tracking-tight sm:text-4xl'>
                    Hi {overview.greetingName}, your modules are now the center
                    of the workspace.
                  </h1>
                </div>

                <div className='mt-8 flex flex-col gap-3 sm:flex-row'>
                  <Button asChild size='lg' className='gap-2'>
                    <Link href={overview.firstAvailableModule?.href ?? '/apps'}>
                      {overview.firstAvailableModule
                        ? `Open ${overview.firstAvailableModule.name}`
                        : 'Explore applications'}
                      <ArrowRight className='h-4 w-4' />
                    </Link>
                  </Button>
                  <Button asChild variant='outline' size='lg'>
                    <Link href='/apps'>Browse all apps</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          </section>

          {modulesError ? (
            <Alert className='mt-6 border-destructive/30 bg-destructive/5'>
              <AlertTriangle className='h-4 w-4' />
              <AlertTitle>
                Unable to refresh part of your module data
              </AlertTitle>
              <AlertDescription>
                Core workspace access is still visible, but some synced modules
                could not be refreshed right now. Please try again shortly.
              </AlertDescription>
            </Alert>
          ) : null}

          <section className='mt-8'>
            <ModuleShowcase
              modules={modules}
              isLoading={modulesLoading}
              className='min-h-[480px]'
            />
          </section>

          {/* <section className='mt-8 grid gap-6 lg:grid-cols-[minmax(0,1.15fr)_minmax(0,0.85fr)]'>
            <Card className='border-border/60 bg-background/90 shadow-sm'>
              <CardContent className='p-6'>
                <p className='text-sm font-medium text-primary'>Next actions</p>
                <h2 className='mt-2 text-2xl font-semibold tracking-tight'>
                  Common destinations for this role
                </h2>
                <p className='mt-2 text-sm leading-6 text-muted-foreground'>
                  Home now focuses on modules first, while the actions below
                  keep supporting tasks one click away.
                </p>

                <div className='mt-6 grid gap-4 md:grid-cols-2'>
                  {quickActions.map((item) => (
                    <QuickActionCard key={item.title} item={item} />
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card className='border-border/60 bg-background/85 shadow-sm'>
              <CardContent className='p-6'>
                <p className='text-sm font-medium text-primary'>UX notes</p>
                <h2 className='mt-2 text-2xl font-semibold tracking-tight'>
                  What this home page now optimizes for
                </h2>
                <p className='mt-2 text-sm leading-6 text-muted-foreground'>
                  The layout is designed so the important workspace entry points
                  stay obvious even when the user has multiple roles.
                </p>

                <div className='mt-6 space-y-4'>
                  {WORKSPACE_GUIDANCE.map((item, index) => (
                    <WorkspaceGuidanceItem
                      key={item.title}
                      item={item}
                      index={index}
                    />
                  ))}
                </div>

                {!hasModules && !modulesLoading ? (
                  <div className='mt-6 rounded-2xl border border-dashed border-primary/35 bg-primary/5 p-5'>
                    <h3 className='font-semibold tracking-tight'>
                      Need more tools?
                    </h3>
                    <p className='mt-2 text-sm leading-6 text-muted-foreground'>
                      Compare plans or browse the app catalog to unlock more
                      workflows for the organization.
                    </p>
                    <div className='mt-4 flex flex-col gap-3 sm:flex-row'>
                      <Button asChild>
                        <Link href='/subscription'>View pricing plans</Link>
                      </Button>
                      <Button asChild variant='outline'>
                        <Link href='/apps'>Browse app catalog</Link>
                      </Button>
                    </div>
                  </div>
                ) : null}
              </CardContent>
            </Card>
          </section> */}
        </div>
      </main>
    </div>
  );
}
