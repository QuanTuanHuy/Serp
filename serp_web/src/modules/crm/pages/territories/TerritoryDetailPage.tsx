'use client';

import { useRouter, useParams } from 'next/navigation';
import { getErrorMessage } from '@/lib/store/api';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Badge,
} from '@/shared/components/ui';
import { toast } from 'sonner';
import { ArrowLeft, Edit, MapPin, Building, Users } from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetTerritoryQuery,
  useGetTerritoryOwnerQuery,
  useUpdateTerritoryMutation,
  useActivateTerritoryMutation,
  useDeactivateTerritoryMutation,
} from '../../api/crmApi';

export const TerritoryDetailPage: React.FC = () => {
  const router = useRouter();
  const params = useParams();
  const territoryCode = params.code as string;

  const { data: territoryData, isLoading: isLoadingTerritory } =
    useGetTerritoryQuery(territoryCode);
  const { data: ownerData } = useGetTerritoryOwnerQuery(territoryCode);

  const [activateTerritory] = useActivateTerritoryMutation();
  const [deactivateTerritory] = useDeactivateTerritoryMutation();

  const territory = territoryData?.data;
  const ownerTeam = ownerData?.data;

  const handleActivate = async () => {
    try {
      await activateTerritory(territoryCode).unwrap();
      toast.success('Territory activated successfully');
    } catch (error) {
      toast.error('Failed to activate territory', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDeactivate = async () => {
    try {
      await deactivateTerritory(territoryCode).unwrap();
      toast.success('Territory deactivated successfully');
    } catch (error) {
      toast.error('Failed to deactivate territory', {
        description: getErrorMessage(error),
      });
    }
  };

  if (isLoadingTerritory) {
    return (
      <div className='flex items-center justify-center h-64'>
        <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-primary' />
      </div>
    );
  }

  if (!territory) {
    return (
      <Card>
        <CardContent className='py-16 text-center'>
          <h3 className='text-lg font-semibold mb-2'>Territory not found</h3>
          <Button onClick={() => router.push('/crm/territories')}>
            Back to Territories
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
            onClick={() => router.push('/crm/territories')}
          >
            <ArrowLeft className='h-5 w-5' />
          </Button>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>
              {territory.territoryName}
            </h1>
            <p className='text-muted-foreground'>{territory.territoryCode}</p>
          </div>
        </div>
        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            onClick={() =>
              router.push(`/crm/territories/${territoryCode}/edit`)
            }
          >
            <Edit className='h-4 w-4 mr-2' />
            Edit
          </Button>
          {territory.active ? (
            <Button variant='outline' onClick={handleDeactivate}>
              Deactivate
            </Button>
          ) : (
            <Button onClick={handleActivate}>Activate</Button>
          )}
        </div>
      </div>

      <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
        <Card>
          <CardHeader>
            <CardTitle className='text-lg'>Territory Information</CardTitle>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='flex items-center gap-2'>
              <span className='text-muted-foreground'>Status:</span>
              <Badge variant={territory.active ? 'default' : 'secondary'}>
                {territory.active ? 'Active' : 'Inactive'}
              </Badge>
            </div>
            <div className='flex items-center gap-2'>
              <MapPin className='h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground'>Code:</span>
              <span className='font-medium'>{territory.territoryCode}</span>
            </div>
            <div className='flex items-center gap-2'>
              <Building className='h-4 w-4 text-muted-foreground' />
              <span className='text-muted-foreground'>Level:</span>
              <Badge variant='outline'>{territory.territoryLevel}</Badge>
            </div>
            <div className='flex items-center gap-2'>
              <span className='text-muted-foreground'>Country:</span>
              <span className='font-medium'>{territory.countryCode}</span>
            </div>
            {territory.parentTerritoryCode && (
              <div className='flex items-center gap-2'>
                <span className='text-muted-foreground'>Parent:</span>
                <span className='font-medium'>
                  {territory.parentTerritoryCode}
                </span>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className='text-lg'>Assigned Team</CardTitle>
          </CardHeader>
          <CardContent>
            {ownerTeam ? (
              <div className='space-y-4'>
                <div className='flex items-center gap-3'>
                  <div className='h-10 w-10 rounded-full bg-blue-500/10 flex items-center justify-center'>
                    <Users className='h-5 w-5 text-blue-600' />
                  </div>
                  <div>
                    <p className='font-medium'>{ownerTeam.name}</p>
                    <p className='text-sm text-muted-foreground'>
                      {ownerTeam.memberCount ?? 0} members
                    </p>
                  </div>
                </div>
                <Button
                  variant='outline'
                  size='sm'
                  onClick={() => router.push(`/crm/teams/${ownerTeam.id}`)}
                >
                  View Team
                </Button>
              </div>
            ) : (
              <div className='text-center py-4'>
                <Users className='h-12 w-12 mx-auto text-muted-foreground mb-2' />
                <p className='text-muted-foreground'>No team assigned</p>
                <p className='text-sm text-muted-foreground mt-1'>
                  Assign this territory to a team for lead routing
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default TerritoryDetailPage;
