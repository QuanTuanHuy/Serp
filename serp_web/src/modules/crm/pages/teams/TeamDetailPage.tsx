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
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
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
  TeamMember,
  CreateTeamMemberRequest,
  UpdateTeamMemberRequest,
  TeamMemberRole,
  TeamMemberStatus,
} from '../../types';

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
  const availableTerritories = territoryOptionsData?.data?.data || [];
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
                  <CardContent className='flex items-center justify-between p-4'>
                    <div className='flex items-center gap-3'>
                      <div className='h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center'>
                        <span className='text-sm font-medium text-primary'>
                          {member.name
                            .split(' ')
                            .map((n) => n[0])
                            .join('')
                            .slice(0, 2)
                            .toUpperCase()}
                        </span>
                      </div>
                      <div>
                        <p className='font-medium'>{member.name}</p>
                        <p className='text-sm text-muted-foreground'>
                          {member.email}
                        </p>
                      </div>
                    </div>
                    <div className='flex items-center gap-3'>
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

      <AddMemberDialog
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
        <EditMemberDialog
          member={editingMember}
          open={!!editingMember}
          onOpenChange={(open) => !open && setEditingMember(null)}
          onSubmit={(data) => handleUpdateMember(editingMember.id, data)}
        />
      )}
    </div>
  );
};

interface AddMemberDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateTeamMemberRequest) => void;
  teamId: string;
  users: Array<{
    id: number;
    firstName?: string;
    lastName?: string;
    email: string;
  }>;
  isLoadingUsers: boolean;
}

const AddMemberDialog: React.FC<AddMemberDialogProps> = ({
  open,
  onOpenChange,
  onSubmit,
  teamId,
  users,
  isLoadingUsers,
}) => {
  const [userId, setUserId] = useState('');
  const [role, setRole] = useState<TeamMemberRole>('SALES_REP');

  const handleSubmit = () => {
    onSubmit({ teamId: Number(teamId), userId: Number(userId), role });
    setUserId('');
    setRole('SALES_REP');
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add Team Member</DialogTitle>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>User *</label>
            <Select value={userId} onValueChange={setUserId}>
              <SelectTrigger>
                <SelectValue
                  placeholder={
                    isLoadingUsers ? 'Loading users...' : 'Select user'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {users.map((user) => {
                  const fullName = [user.firstName, user.lastName]
                    .filter(Boolean)
                    .join(' ')
                    .trim();

                  return (
                    <SelectItem key={user.id} value={String(user.id)}>
                      {fullName || user.email} ({user.email})
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Role *</label>
            <Select
              value={role}
              onValueChange={(v) => setRole(v as TeamMemberRole)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='MANAGER'>Manager</SelectItem>
                <SelectItem value='SALES_REP'>Sales Rep</SelectItem>
                <SelectItem value='VIEWER'>Viewer</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={!userId}>
            Add Member
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
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

interface EditMemberDialogProps {
  member: TeamMember;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: UpdateTeamMemberRequest) => void;
}

const EditMemberDialog: React.FC<EditMemberDialogProps> = ({
  member,
  open,
  onOpenChange,
  onSubmit,
}) => {
  const [name, setName] = useState(member.name);
  const [email, setEmail] = useState(member.email || '');
  const [phone, setPhone] = useState(member.phone || '');
  const [role, setRole] = useState<TeamMemberRole>(member.role);
  const [status, setStatus] = useState<TeamMemberStatus>(member.status);

  const handleSubmit = () => {
    onSubmit({ name, email, phone, role, status });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Team Member</DialogTitle>
        </DialogHeader>
        <div className='space-y-4'>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Name *</label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Email</label>
            <Input value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Phone</label>
            <Input value={phone} onChange={(e) => setPhone(e.target.value)} />
          </div>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Role *</label>
            <Select
              value={role}
              onValueChange={(v) => setRole(v as TeamMemberRole)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='MANAGER'>Manager</SelectItem>
                <SelectItem value='SALES_REP'>Sales Rep</SelectItem>
                <SelectItem value='VIEWER'>Viewer</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-2'>
            <label className='text-sm font-medium'>Status *</label>
            <Select
              value={status}
              onValueChange={(v) => setStatus(v as TeamMemberStatus)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ACTIVE'>Active</SelectItem>
                <SelectItem value='INACTIVE'>Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleSubmit}>Update Member</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default TeamDetailPage;
