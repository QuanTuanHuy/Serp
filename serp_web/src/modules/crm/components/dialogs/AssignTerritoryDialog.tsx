'use client';

import { useState } from 'react';
import { getErrorMessage } from '@/lib/store/api';
import {
  Button,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Input,
  Badge,
} from '@/shared/components/ui';
import { toast } from 'sonner';
import { Search, MapPin, Check } from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetTerritoriesQuery,
  useAssignTerritoriesMutation,
} from '../../api/crmApi';
import type { Territory } from '../../types';

interface AssignTerritoryDialogProps {
  teamId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  currentTerritoryCodes?: string[];
}

export const AssignTerritoryDialog: React.FC<AssignTerritoryDialogProps> = ({
  teamId,
  open,
  onOpenChange,
  currentTerritoryCodes = [],
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCodes, setSelectedCodes] = useState<string[]>(
    currentTerritoryCodes
  );

  const [assignTerritories, { isLoading: isAssigning }] =
    useAssignTerritoriesMutation();

  const { data, isLoading } = useGetTerritoriesQuery({
    filters: { keyword: searchQuery || undefined },
    pagination: { page: 1, limit: 50 },
  });

  const territories = data?.data || [];

  const toggleTerritory = (code: string) => {
    setSelectedCodes((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code]
    );
  };

  const handleSubmit = async () => {
    try {
      await assignTerritories({
        teamId,
        data: { territoryCodes: selectedCodes },
      }).unwrap();
      toast.success('Territories assigned successfully');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to assign territories', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Assign Territories</DialogTitle>
        </DialogHeader>

        <div className='space-y-4'>
          <div className='relative'>
            <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground' />
            <Input
              placeholder='Search territories...'
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className='pl-10'
            />
          </div>

          <div className='text-sm text-muted-foreground'>
            Selected: {selectedCodes.length} territories
          </div>

          {isLoading ? (
            <div className='space-y-2'>
              {Array.from({ length: 5 }).map((_, i) => (
                <div
                  key={i}
                  className='h-12 bg-muted rounded-lg animate-pulse'
                />
              ))}
            </div>
          ) : (
            <div className='max-h-80 overflow-y-auto space-y-2'>
              {territories.length === 0 ? (
                <div className='text-center py-8 text-muted-foreground'>
                  No territories found
                </div>
              ) : (
                territories.map((territory) => (
                  <TerritoryItem
                    key={territory.territoryCode}
                    territory={territory}
                    selected={selectedCodes.includes(territory.territoryCode)}
                    onToggle={() => toggleTerritory(territory.territoryCode)}
                  />
                ))
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={isAssigning}>
            {isAssigning ? 'Assigning...' : 'Assign Territories'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

interface TerritoryItemProps {
  territory: Territory;
  selected: boolean;
  onToggle: () => void;
}

const TerritoryItem: React.FC<TerritoryItemProps> = ({
  territory,
  selected,
  onToggle,
}) => {
  return (
    <div
      className={cn(
        'flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-colors',
        selected ? 'border-primary bg-primary/5' : 'hover:bg-muted/50'
      )}
      onClick={onToggle}
    >
      <div className='flex items-center gap-3'>
        <div className='h-8 w-8 rounded-full bg-amber-500/10 flex items-center justify-center'>
          <MapPin className='h-4 w-4 text-amber-600' />
        </div>
        <div>
          <p className='font-medium text-sm'>{territory.territoryName}</p>
          <p className='text-xs text-muted-foreground'>
            {territory.territoryCode}
          </p>
        </div>
      </div>
      <div className='flex items-center gap-2'>
        <Badge variant='outline' className='text-xs'>
          {territory.countryCode}
        </Badge>
        {selected && (
          <div className='h-5 w-5 rounded-full bg-primary flex items-center justify-center'>
            <Check className='h-3 w-3 text-primary-foreground' />
          </div>
        )}
      </div>
    </div>
  );
};

export default AssignTerritoryDialog;
