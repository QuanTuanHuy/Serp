'use client';

import { useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import { useGetMyOrganizationQuery } from '@/modules/settings/services/organizations/organizationsApi';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Badge,
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
  Textarea,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/components/ui';
import { toast } from 'sonner';
import {
  ArrowLeft,
  Edit,
  Trash2,
  Users,
  MapPin,
  UserCircle,
  Plus,
} from 'lucide-react';
import {
  AddTeamMemberDialog,
  EditTeamMemberDialog,
} from '../../components/teams';
import {
  useGetTeamQuery,
  useGetTeamMembersQuery,
  useGetTeamTerritoriesQuery,
  useDeleteTeamMutation,
  useAddTeamMemberMutation,
  useUpdateTeamMemberMutation,
  useRemoveTeamMemberMutation,
  useAssignTerritoriesMutation,
  useGetTerritoriesQuery,
} from '../../api/crmApi';
import type {
  ExperienceLevel,
  TeamMember,
  CreateTeamMemberRequest,
  UpdateTeamMemberRequest,
} from '../../types';

const EXPERIENCE_LABEL: Record<ExperienceLevel, string> = {
  JUNIOR: 'Junior',
  MID: 'Mid',
  SENIOR: 'Senior',
  EXPERT: 'Expert',
};

function memberDisplayName(member: TeamMember): string {
  const n = member.name?.trim();
  if (n) return n;
  const e = member.email?.trim();
  if (e) return e;
  return 'Member';
}

function memberInitials(member: TeamMember): string {
  const base = memberDisplayName(member);
  return (
    base
      .split(/\s+/)
      .filter(Boolean)
      .map((p) => p[0])
      .join('')
      .slice(0, 2)
      .toUpperCase() || '?'
  );
}

export const TeamDetailPage: React.FC = () => {
  const router = useRouter();
  const params = useParams();
  const teamId = params.id as string;

  const [activeTab, setActiveTab] = useState('overview');
  const [showAddMember, setShowAddMember] = useState(false);
  const [showAssignTerritory, setShowAssignTerritory] = useState(false);
  const [editingMember, setEditingMember] = useState<TeamMember | null>(null);
  const { data: organization } = useGetMyOrganizationQuery();
  const organizationId = organization?.id;

  const { data: teamData, isLoading: isLoadingTeam } = useGetTeamQuery(teamId);
  const { data: membersData, isLoading: isLoadingMembers } =
    useGetTeamMembersQuery({ teamId, page: 1, size: 50 });
  const { data: territoriesData, isLoading: isLoadingTerritories } =
    useGetTeamTerritoriesQuery(teamId);
  const { data: usersData, isLoading: isLoadingUsers } =
    useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
      },
      { skip: !organizationId }
    );
  const { data: territoryOptionsData, isLoading: isLoadingTerritoryOptions } =
    useGetTerritoriesQuery({
      filters: {
        active: true,
      },
      pagination: {
        page: 1,
        limit: 100,
      },
    });

  const [deleteTeam] = useDeleteTeamMutation();
  const [addTeamMember] = useAddTeamMemberMutation();
  const [updateTeamMember] = useUpdateTeamMemberMutation();
  const [removeTeamMember] = useRemoveTeamMemberMutation();
  const [assignTerritories] = useAssignTerritoriesMutation();

  const team = teamData?.data;
  const members = membersData?.data?.items || [];
  const territories = territoriesData?.data?.territories || [];
  const users = usersData?.data.items || [];
  const availableTerritories = territoryOptionsData?.data || [];
  const managerName = team?.manager?.name || undefined;

  const handleDeleteTeam = async () => {
    if (!confirm('Are you sure you want to delete this team?')) return;
    try {
      await deleteTeam(teamId).unwrap();
      toast.success('Team deleted successfully');
      router.push('/crm/teams');
    } catch (error) {
      toast.error('Failed to delete team', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleAddMember = async (data: CreateTeamMemberRequest) => {
    try {
      await addTeamMember({ teamId, data }).unwrap();
      toast.success('Member added successfully');
      setShowAddMember(false);
    } catch (error) {
      toast.error('Failed to add member', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleUpdateMember = async (
    memberId: string,
    data: UpdateTeamMemberRequest
  ) => {
    try {
      await updateTeamMember({ teamId, memberId, data }).unwrap();
      toast.success('Member updated successfully');
      setEditingMember(null);
    } catch (error) {
      toast.error('Failed to update member', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRemoveMember = async (memberId: string) => {
    if (!confirm('Are you sure you want to remove this member?')) return;
    try {
      await removeTeamMember({ teamId, memberId }).unwrap();
      toast.success('Member removed successfully');
    } catch (error) {
      toast.error('Failed to remove member', {
        description: getErrorMessage(error),
      });
    }
  };

  if (isLoadingTeam) {
    return (
      <div className='flex items-center justify-center h-64'>
        <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-primary' />
      </div>
    );
  }

  if (!team) {
    return (
      <Card>
        <CardContent className='py-16 text-center'>
          <h3 className='text-lg font-semibold mb-2'>Team not found</h3>
          <Button onClick={() => router.push('/crm/teams')}>
            Back to Teams
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-4'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/crm/teams')}
          >
            <ArrowLeft className='h-5 w-5' />
          </Button>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>{team.name}</h1>
            <p className='text-muted-foreground'>Team details and management</p>
          </div>
        </div>
        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            onClick={() => router.push(`/crm/teams/${teamId}/edit`)}
          >
            <Edit className='h-4 w-4 mr-2' />
            Edit
          </Button>
          <Button variant='destructive' onClick={handleDeleteTeam}>
            <Trash2 className='h-4 w-4 mr-2' />
            Delete
          </Button>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='members'>Members ({members.length})</TabsTrigger>
          <TabsTrigger value='territories'>
            Territories ({territories.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value='overview' className='space-y-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            <Card>
              <CardHeader>
                <CardTitle className='text-lg'>Team Information</CardTitle>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='flex items-center gap-2'>
                  <span className='text-muted-foreground'>Status:</span>
                  <Badge
                    variant={team.status === 'ACTIVE' ? 'default' : 'secondary'}
                  >
                    {team.status}
                  </Badge>
                </div>
                {managerName && (
                  <div className='flex items-center gap-2'>
                    <UserCircle className='h-4 w-4 text-muted-foreground' />
                    <span className='text-muted-foreground'>Manager:</span>
                    <span className='font-medium'>{managerName}</span>
                  </div>
                )}
                {team.description && (
                  <div>
                    <span className='text-muted-foreground text-sm'>
                      Description:
                    </span>
                    <p className='mt-1'>{team.description}</p>
                  </div>
                )}
                {team.notes && (
                  <div>
                    <span className='text-muted-foreground text-sm'>
                      Notes:
                    </span>
                    <p className='mt-1 text-sm'>{team.notes}</p>
                  </div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className='text-lg'>Statistics</CardTitle>
              </CardHeader>
              <CardContent className='space-y-4'>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2'>
                    <Users className='h-5 w-5 text-muted-foreground' />
                    <span>Members</span>
                  </div>
                  <span className='font-bold text-lg'>
                    {team.memberCount ?? members.length}
                  </span>
                </div>
                <div className='flex items-center justify-between'>
                  <div className='flex items-center gap-2'>
                    <MapPin className='h-5 w-5 text-muted-foreground' />
                    <span>Territories</span>
                  </div>
                  <span className='font-bold text-lg'>
                    {territories.length}
                  </span>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value='members' className='space-y-4'>
          <div className='flex justify-between items-center'>
            <h3 className='text-lg font-semibold'>Team Members</h3>
            <Button onClick={() => setShowAddMember(true)}>
              <Plus className='h-4 w-4 mr-2' />
              Add Member
            </Button>
          </div>

          {isLoadingMembers ? (
            <div className='animate-pulse space-y-2'>
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className='h-16 bg-muted rounded-lg' />
              ))}
            </div>
          ) : members.length === 0 ? (
            <Card>
              <CardContent className='py-8 text-center'>
                <Users className='h-12 w-12 mx-auto text-muted-foreground mb-4' />
                <p className='text-muted-foreground'>No members yet</p>
              </CardContent>
            </Card>
          ) : (
            <div className='space-y-2'>
              {members.map((member) => (
                <Card key={member.id}>
                  <CardContent className='flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between'>
                    <div className='flex min-w-0 flex-1 items-start gap-3'>
                      <div className='flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10'>
                        <span className='text-sm font-medium text-primary'>
                          {memberInitials(member)}
                        </span>
                      </div>
                      <div className='min-w-0 flex-1'>
                        <p className='font-medium'>
                          {memberDisplayName(member)}
                        </p>
                        {member.email ? (
                          <p className='truncate text-sm text-muted-foreground'>
                            {member.email}
                          </p>
                        ) : null}
                        <div className='mt-1 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-muted-foreground'>
                          {member.experienceLevel ? (
                            <Badge variant='secondary' className='font-normal'>
                              {EXPERIENCE_LABEL[member.experienceLevel]}
                            </Badge>
                          ) : null}
                          {member.capacity != null ||
                          member.maxMeetings != null ? (
                            <span>
                              {member.capacity != null
                                ? `${member.capacity}% load`
                                : ''}
                              {member.capacity != null &&
                              member.maxMeetings != null
                                ? ' · '
                                : ''}
                              {member.maxMeetings != null
                                ? `${member.maxMeetings} mtgs`
                                : ''}
                            </span>
                          ) : null}
                          {member.skills?.length || member.languages?.length ? (
                            <span>
                              {member.skills?.length
                                ? `${member.skills.length} skills`
                                : ''}
                              {member.skills?.length && member.languages?.length
                                ? ' · '
                                : ''}
                              {member.languages?.length
                                ? `${member.languages.length} langs`
                                : ''}
                            </span>
                          ) : null}
                        </div>
                      </div>
                    </div>
                    <div className='flex shrink-0 flex-wrap items-center gap-2 sm:gap-3'>
                      <Badge variant='outline'>{member.role}</Badge>
                      <Badge
                        variant={
                          member.status === 'ACTIVE' ? 'default' : 'secondary'
                        }
                      >
                        {member.status}
                      </Badge>
                      <Button
                        variant='ghost'
                        size='sm'
                        onClick={() => setEditingMember(member)}
                      >
                        <Edit className='h-4 w-4' />
                      </Button>
                      <Button
                        variant='ghost'
                        size='sm'
                        onClick={() => handleRemoveMember(member.id)}
                      >
                        <Trash2 className='h-4 w-4 text-destructive' />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value='territories' className='space-y-4'>
          <div className='flex justify-between items-center'>
            <h3 className='text-lg font-semibold'>Assigned Territories</h3>
            <Button onClick={() => setShowAssignTerritory(true)}>
              <Plus className='h-4 w-4 mr-2' />
              Assign Territory
            </Button>
          </div>

          {isLoadingTerritories ? (
            <div className='animate-pulse space-y-2'>
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className='h-16 bg-muted rounded-lg' />
              ))}
            </div>
          ) : territories.length === 0 ? (
            <Card>
              <CardContent className='py-8 text-center'>
                <MapPin className='h-12 w-12 mx-auto text-muted-foreground mb-4' />
                <p className='text-muted-foreground'>No territories assigned</p>
              </CardContent>
            </Card>
          ) : (
            <div className='space-y-2'>
              {territories.map((tt) => (
                <Card key={tt.territoryCode}>
                  <CardContent className='flex items-center justify-between p-4'>
                    <div className='flex items-center gap-3'>
                      <div className='h-10 w-10 rounded-full bg-amber-500/10 flex items-center justify-center'>
                        <MapPin className='h-5 w-5 text-amber-600' />
                      </div>
                      <div>
                        <p className='font-medium'>
                          {tt.territoryName || tt.territoryCode}
                        </p>
                        <p className='text-sm text-muted-foreground'>
                          {tt.territoryCode}
                        </p>
                      </div>
                    </div>
                    <Badge variant={tt.active ? 'default' : 'secondary'}>
                      {tt.active ? 'Active' : 'Inactive'}
                    </Badge>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>

      <AddTeamMemberDialog
        open={showAddMember}
        onOpenChange={setShowAddMember}
        onSubmit={handleAddMember}
        teamId={teamId}
        users={users}
        isLoadingUsers={isLoadingUsers}
      />

      <AssignTerritoryDialog
        open={showAssignTerritory}
        onOpenChange={setShowAssignTerritory}
        onSubmit={async (territoryCodes) => {
          try {
            await assignTerritories({
              teamId,
              data: { territoryCodes },
            }).unwrap();
            toast.success('Territories assigned successfully');
            setShowAssignTerritory(false);
          } catch (error) {
            toast.error('Failed to assign territories', {
              description: getErrorMessage(error),
            });
          }
        }}
        territories={availableTerritories}
        isLoadingTerritories={isLoadingTerritoryOptions}
      />

      {editingMember && (
        <EditTeamMemberDialog
          member={editingMember}
          open={!!editingMember}
          onOpenChange={(open) => !open && setEditingMember(null)}
          onSubmit={(data) => handleUpdateMember(editingMember.id, data)}
        />
      )}
    </div>
  );
};

interface AssignTerritoryDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (territoryCodes: string[]) => void;
  territories: Array<{
    territoryCode: string;
    territoryName: string;
  }>;
  isLoadingTerritories: boolean;
}

const AssignTerritoryDialog: React.FC<AssignTerritoryDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  territories,
  isLoadingTerritories,
}) => {
  const [territoryCodes, setTerritoryCodes] = useState('');

  const handleSubmit = () => {
    const selectedCodes = territoryCodes
      .split(',')
      .map((code) => code.trim())
      .filter(Boolean);

    onSubmit(selectedCodes);
    setTerritoryCodes('');
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Assign Territories</DialogTitle>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Available territories</label>
            <div className='max-h-48 overflow-y-auto rounded-md border p-3 text-sm'>
              {isLoadingTerritories ? (
                <p className='text-muted-foreground'>Loading territories...</p>
              ) : territories.length === 0 ? (
                <p className='text-muted-foreground'>
                  No active territories available.
                </p>
              ) : (
                territories.map((territory) => (
                  <p key={territory.territoryCode}>
                    {territory.territoryName} ({territory.territoryCode})
                  </p>
                ))
              )}
            </div>
          </div>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Territory codes *</label>
            <Textarea
              value={territoryCodes}
              onChange={(e) => setTerritoryCodes(e.target.value)}
              placeholder='Enter comma-separated territory codes'
              rows={4}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={!territoryCodes.trim()}>
            Assign
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default TeamDetailPage;
