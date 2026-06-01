/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project people page
 */

'use client';

import { useDeferredValue, useMemo, useState } from 'react';
import { MoreHorizontal, Plus, Search, Sparkles, Users } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  Input,
  Label,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { useAppSelector } from '@/shared/hooks';
import { useGetPmSkillsQuery, useGetPmUsersSkillsQuery } from '../api';
import {
  useGetPmProjectPeopleQuery,
  useGetPmProjectRolesQuery,
  useRemovePmProjectPersonMutation,
  useReplacePmProjectPersonRolesMutation,
} from '../api/projectApi';
import { PMUserSkillDialog } from '../components/skills';
import {
  buildSkillMap,
  getProficiencyLabel,
} from '../components/skills/skill-ui.utils';
import type {
  PMProjectPersonApi,
  PMSkillApi,
  PMUserSkillApi,
  PMUserSkillsByUserApi,
} from '../types/api';

interface PMProjectPeoplePageProps {
  projectId: string;
}

type PeopleDialogMode = 'add' | 'edit';

interface SkillBadgesProps {
  skills: PMUserSkillApi[];
  skillsById: Map<number, PMSkillApi>;
}

export function PMProjectPeoplePage({ projectId }: PMProjectPeoplePageProps) {
  const numericProjectId = Number(projectId);
  const organizationId = useAppSelector(selectOrganizationId);
  const [search, setSearch] = useState('');
  const [userSearch, setUserSearch] = useState('');
  const [dialogMode, setDialogMode] = useState<PeopleDialogMode>('add');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [removingPerson, setRemovingPerson] =
    useState<PMProjectPersonApi | null>(null);
  const [skillPerson, setSkillPerson] = useState<PMProjectPersonApi | null>(
    null
  );
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());
  const deferredUserSearch = useDeferredValue(userSearch.trim());
  const canSearchUsers = deferredUserSearch.length > 0;

  const peopleQuery = useGetPmProjectPeopleQuery(numericProjectId, {
    skip: !Number.isFinite(numericProjectId),
  });
  const rolesQuery = useGetPmProjectRolesQuery({ page: 0, pageSize: 100 });
  const usersQuery = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      search: deferredUserSearch || undefined,
      page: 0,
      pageSize: 50,
      status: 'ACTIVE',
      sortBy: 'firstName',
      sortDir: 'ASC',
    },
    {
      skip:
        !organizationId ||
        !dialogOpen ||
        dialogMode !== 'add' ||
        !canSearchUsers,
    }
  );
  const [replaceRoles, replaceState] = useReplacePmProjectPersonRolesMutation();
  const [removePerson, removeState] = useRemovePmProjectPersonMutation();

  const people = peopleQuery.data ?? [];
  const peopleUserIds = useMemo(
    () => people.map((person) => person.userId),
    [people]
  );
  const userSkillsQuery = useGetPmUsersSkillsQuery(peopleUserIds, {
    skip: peopleUserIds.length === 0,
  });
  const skillsQuery = useGetPmSkillsQuery(undefined, {
    skip: peopleUserIds.length === 0,
  });
  const roles = rolesQuery.data?.data.items ?? [];
  const skillsById = useMemo(
    () => buildSkillMap(skillsQuery.data),
    [skillsQuery.data]
  );
  const existingUserIds = useMemo(
    () => new Set(people.map((person) => person.userId)),
    [people]
  );
  const availableUsers = useMemo(
    () =>
      (usersQuery.data?.data.items ?? []).filter(
        (user) => !existingUserIds.has(Number(user.id))
      ),
    [existingUserIds, usersQuery.data]
  );
  const filteredPeople = useMemo(() => {
    if (!deferredSearch) {
      return people;
    }
    return people.filter((person) => {
      const text = [
        person.name,
        person.email,
        ...person.roles.map((role) => role.name),
        getPersonSkillSearchText(
          person.userId,
          userSkillsQuery.data,
          skillsById
        ),
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return text.includes(deferredSearch);
    });
  }, [deferredSearch, people, skillsById, userSkillsQuery.data]);

  const openAddDialog = () => {
    setDialogMode('add');
    setSelectedUserId(null);
    setSelectedRoleIds([]);
    setUserSearch('');
    setDialogOpen(true);
  };

  const openEditDialog = (person: PMProjectPersonApi) => {
    setDialogMode('edit');
    setSelectedUserId(person.userId);
    setSelectedRoleIds(person.roles.map((role) => role.id));
    setUserSearch('');
    setDialogOpen(true);
  };

  const toggleRole = (roleId: number) => {
    setSelectedRoleIds((current) =>
      current.includes(roleId)
        ? current.filter((id) => id !== roleId)
        : [...current, roleId]
    );
  };

  const handleSaveRoles = async () => {
    if (!selectedUserId || selectedRoleIds.length === 0) {
      toast.error('Select a user and at least one role.');
      return;
    }

    try {
      await replaceRoles({
        projectId: numericProjectId,
        userId: selectedUserId,
        body: { roleIds: selectedRoleIds },
      }).unwrap();
      toast.success(dialogMode === 'add' ? 'Person added.' : 'Roles updated.');
      setDialogOpen(false);
    } catch (error) {
      toast.error('Unable to save project person', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRemove = async () => {
    if (!removingPerson) {
      return;
    }
    try {
      await removePerson({
        projectId: numericProjectId,
        userId: removingPerson.userId,
      }).unwrap();
      toast.success('Person removed from project roles.');
      setRemovingPerson(null);
    } catch (error) {
      toast.error('Unable to remove project person', {
        description: getErrorMessage(error),
      });
    }
  };

  const selectedPerson = people.find(
    (person) => person.userId === selectedUserId
  );
  const isSaving = replaceState.isLoading;
  const isInitialLoading = peopleQuery.isLoading && people.length === 0;
  const isSkillsLoading =
    peopleUserIds.length > 0 &&
    (skillsQuery.isLoading || userSkillsQuery.isLoading);

  return (
    <div className='space-y-5'>
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>People</h1>
          <p className='text-sm text-muted-foreground'>
            Manage users and project role membership.
          </p>
        </div>
        <Button type='button' onClick={openAddDialog}>
          <Plus className='mr-2 h-4 w-4' />
          Add user
        </Button>
      </div>

      <Card className='shadow-sm'>
        <CardHeader className='gap-3 border-b md:flex-row md:items-center md:justify-between'>
          <CardTitle className='flex items-center gap-2 text-base'>
            <Users className='h-4 w-4' />
            Project people
          </CardTitle>
          <div className='relative w-full md:w-80'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder='Search people or roles...'
              className='pl-9'
            />
          </div>
        </CardHeader>
        <CardContent className='p-0'>
          {isInitialLoading ? (
            <div className='space-y-3 p-4'>
              {Array.from({ length: 4 }).map((_, index) => (
                <Skeleton key={index} className='h-12 w-full' />
              ))}
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Roles</TableHead>
                  <TableHead>Skills</TableHead>
                  <TableHead>Added</TableHead>
                  <TableHead className='w-12' />
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredPeople.map((person) => {
                  const userSkills = getUserSkillItems(
                    person.userId,
                    userSkillsQuery.data
                  );
                  return (
                    <TableRow key={person.userId}>
                      <TableCell>
                        <div className='flex items-center gap-3'>
                          <Avatar className='h-9 w-9'>
                            {person.avatarUrl ? (
                              <AvatarImage src={person.avatarUrl} alt='' />
                            ) : null}
                            <AvatarFallback>
                              {getInitials(person)}
                            </AvatarFallback>
                          </Avatar>
                          <div className='min-w-0'>
                            <div className='flex flex-wrap items-center gap-2'>
                              <span className='font-medium'>
                                {person.name || `User #${person.userId}`}
                              </span>
                              {person.projectLead ? (
                                <Badge variant='secondary'>Project lead</Badge>
                              ) : null}
                            </div>
                            <p className='truncate text-sm text-muted-foreground'>
                              {person.email || '-'}
                            </p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className='flex flex-wrap gap-1.5'>
                          {person.roles.length > 0 ? (
                            person.roles.map((role) => (
                              <Badge key={role.id} variant='outline'>
                                {role.name}
                              </Badge>
                            ))
                          ) : (
                            <span className='text-sm text-muted-foreground'>
                              No roles
                            </span>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        {isSkillsLoading ? (
                          <Skeleton className='h-6 w-40' />
                        ) : (
                          <SkillBadges
                            skills={userSkills}
                            skillsById={skillsById}
                          />
                        )}
                      </TableCell>
                      <TableCell className='text-sm text-muted-foreground'>
                        {formatTimestamp(person.addedAt)}
                      </TableCell>
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant='ghost' size='icon'>
                              <MoreHorizontal className='h-4 w-4' />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align='end'>
                            <DropdownMenuItem
                              onClick={() => setSkillPerson(person)}
                            >
                              Edit skills
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => openEditDialog(person)}
                            >
                              Edit roles
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              className='text-destructive'
                              onClick={() => setRemovingPerson(person)}
                            >
                              Remove from project
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  );
                })}
                {filteredPeople.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={5}
                      className='h-32 text-center text-sm text-muted-foreground'
                    >
                      No project people found.
                    </TableCell>
                  </TableRow>
                ) : null}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className='max-w-xl'>
          <DialogHeader>
            <DialogTitle>
              {dialogMode === 'add' ? 'Add user' : 'Edit roles'}
            </DialogTitle>
            <DialogDescription>
              {dialogMode === 'add'
                ? 'Search organization users and assign project roles.'
                : `Update roles for ${selectedPerson?.name || 'this user'}.`}
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            {dialogMode === 'add' ? (
              <div className='space-y-2'>
                <Label htmlFor='people-user-search'>User</Label>
                <Input
                  id='people-user-search'
                  value={userSearch}
                  onChange={(event) => setUserSearch(event.target.value)}
                  placeholder='Search organization users...'
                />
                <div className='max-h-48 overflow-y-auto rounded-md border'>
                  {!canSearchUsers ? (
                    <div className='px-3 py-6 text-center text-sm text-muted-foreground'>
                      Start typing to search organization users.
                    </div>
                  ) : null}
                  {canSearchUsers
                    ? availableUsers.map((user) => {
                        const label = getUserLabel(user);
                        return (
                          <button
                            key={user.id}
                            type='button'
                            className='flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-muted'
                            onClick={() => setSelectedUserId(Number(user.id))}
                          >
                            <span>
                              <span className='font-medium'>{label}</span>
                              <span className='block text-muted-foreground'>
                                {user.email || '-'}
                              </span>
                            </span>
                            {selectedUserId === Number(user.id) ? (
                              <Badge>Selected</Badge>
                            ) : null}
                          </button>
                        );
                      })
                    : null}
                  {canSearchUsers &&
                  !usersQuery.isFetching &&
                  availableUsers.length === 0 ? (
                    <div className='px-3 py-6 text-center text-sm text-muted-foreground'>
                      No users found.
                    </div>
                  ) : null}
                </div>
              </div>
            ) : null}

            <div className='space-y-2'>
              <Label>Roles</Label>
              <div className='grid max-h-56 gap-2 overflow-y-auto rounded-md border p-3'>
                {roles.map((role) => (
                  <label
                    key={role.id}
                    className='flex items-center gap-2 text-sm'
                  >
                    <Checkbox
                      checked={selectedRoleIds.includes(role.id)}
                      onCheckedChange={() => toggleRole(role.id)}
                    />
                    <span>{role.name}</span>
                    {role.isSystem ? (
                      <Badge variant='secondary'>System</Badge>
                    ) : null}
                  </label>
                ))}
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => setDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button type='button' onClick={handleSaveRoles} disabled={isSaving}>
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog
        open={!!removingPerson}
        onOpenChange={(open) => !open && setRemovingPerson(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Remove from project?</AlertDialogTitle>
            <AlertDialogDescription>
              This removes all project roles from{' '}
              {removingPerson?.name || 'this user'}. Project lead ownership is
              not changed.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleRemove}
              disabled={removeState.isLoading}
            >
              Remove
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <PMUserSkillDialog
        open={skillPerson !== null}
        userId={skillPerson?.userId}
        userName={skillPerson?.name}
        onOpenChange={(open) => !open && setSkillPerson(null)}
      />
    </div>
  );
}

function SkillBadges({ skills, skillsById }: SkillBadgesProps) {
  if (skills.length === 0) {
    return <span className='text-sm text-muted-foreground'>No skills</span>;
  }

  const visibleSkills = skills.slice(0, 3);
  const hiddenCount = skills.length - visibleSkills.length;

  return (
    <div className='flex max-w-md flex-wrap gap-1.5'>
      {visibleSkills.map((skill) => (
        <Badge key={skill.id} variant='outline'>
          {getSkillBadgeLabel(skill, skillsById)}
        </Badge>
      ))}
      {hiddenCount > 0 ? (
        <Badge variant='secondary'>+{hiddenCount}</Badge>
      ) : null}
    </div>
  );
}

function getInitials(person: PMProjectPersonApi) {
  const source = person.name || person.email || String(person.userId);
  return source
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}

function getUserSkillItems(
  userId: number,
  userSkillsByUser?: PMUserSkillsByUserApi
) {
  return userSkillsByUser?.[String(userId)] ?? [];
}

function getPersonSkillSearchText(
  userId: number,
  userSkillsByUser: PMUserSkillsByUserApi | undefined,
  skillsById: Map<number, PMSkillApi>
) {
  return getUserSkillItems(userId, userSkillsByUser)
    .map((skill) => {
      const catalogSkill = skillsById.get(skill.skillId);
      return [
        catalogSkill?.name,
        catalogSkill?.code,
        getProficiencyLabel(skill.proficiency),
      ]
        .filter(Boolean)
        .join(' ');
    })
    .join(' ');
}

function getSkillBadgeLabel(
  userSkill: PMUserSkillApi,
  skillsById: Map<number, PMSkillApi>
) {
  const skill = skillsById.get(userSkill.skillId);
  const name = skill?.code || skill?.name || `Skill #${userSkill.skillId}`;
  return `${name}: ${getProficiencyLabel(userSkill.proficiency)}`;
}

function formatTimestamp(value?: number | null) {
  if (!value) {
    return '-';
  }
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  }).format(new Date(value));
}

function getUserLabel(user: {
  id: string | number;
  firstName?: string;
  lastName?: string;
  email?: string;
}) {
  return (
    `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
    user.email ||
    `User #${user.id}`
  );
}
