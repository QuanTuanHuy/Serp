/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { useState } from 'react';
import Link from 'next/link';
import { AlertCircle, Package, History } from 'lucide-react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  ChangeStageDialog,
  ReopenOpportunityDialog,
  DeleteOpportunityDialog,
} from '../../components/dialogs';
import { RequestMeetingDialog } from '../../components/meeting-requests';
import {
  OpportunityHeader,
  OpportunityDealMetricsStrip,
  PipelineProgress,
  OpportunityOverviewTab,
  OpportunityActivitiesTab,
} from '../../components/opportunities';
import { formatCurrency } from '../../utils';
import {
  useOpportunityDetail,
  useOpportunityDialogs,
  useOpportunityActions,
} from '../../hooks';

interface OpportunityDetailPageProps {
  opportunityId: string;
  className?: string;
}

function parseOptionalActualValue(raw: string): number | undefined {
  const trimmed = raw.trim();
  if (!trimmed) return undefined;
  const n = Number(trimmed);
  return Number.isFinite(n) ? n : undefined;
}

export const OpportunityDetailPage: React.FC<OpportunityDetailPageProps> = ({
  opportunityId,
  className,
}) => {
  const [activeTab, setActiveTab] = useState('overview');
  const [meetingRequestOpen, setMeetingRequestOpen] = useState(false);

  const {
    opportunity,
    activities,
    isLoading,
    isActivitiesLoading,
    stageConfig,
    typeConfig,
    isClosed,
    canReopen,
    probability,
    estimatedValue,
    weightedValue,
    daysInPipeline,
    daysUntilClose,
  } = useOpportunityDetail(opportunityId);

  const {
    isStageDialogOpen,
    setIsStageDialogOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    isReopenDialogOpen,
    setIsReopenDialogOpen,
    selectedStage,
    setSelectedStage,
    stageNotes,
    setStageNotes,
    lostReason,
    setLostReason,
    wonActualValue,
    setWonActualValue,
    reopenReason,
    setReopenReason,
    reopenStage,
    setReopenStage,
    resetDialogStates,
  } = useOpportunityDialogs();

  const { handleStageChange, handleReopen, handleDelete } =
    useOpportunityActions(opportunityId);

  if (!isLoading && !opportunity) {
    return (
      <div className={cn('p-6', className)}>
        <Card>
          <CardContent className='py-16 text-center'>
            <AlertCircle className='mx-auto mb-4 h-12 w-12 text-muted-foreground' />
            <h2 className='mb-2 text-xl font-semibold'>
              Opportunity Not Found
            </h2>
            <p className='mb-4 text-muted-foreground'>
              The opportunity you're looking for doesn't exist or has been
              deleted.
            </p>
            <Button asChild>
              <Link href='/crm/opportunities'>Back to Opportunities</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!opportunity || !stageConfig || !typeConfig) {
    return null;
  }

  return (
    <div className={cn('space-y-6 p-6', className)}>
      <OpportunityHeader
        opportunity={opportunity}
        stageConfig={stageConfig}
        typeConfig={typeConfig}
        isClosed={isClosed}
        canReopen={canReopen}
        onBack={() => window.history.back()}
        onEdit={() =>
          (window.location.href = `/crm/opportunities/${opportunityId}/edit`)
        }
        onChangeStage={() => setIsStageDialogOpen(true)}
        onReopen={() => setIsReopenDialogOpen(true)}
        onDelete={() => setIsDeleteDialogOpen(true)}
        canRequestMeeting={Boolean(opportunity.accountId?.trim())}
        onRequestMeeting={() => setMeetingRequestOpen(true)}
      />

      {opportunity.accountId?.trim() && (
        <RequestMeetingDialog
          open={meetingRequestOpen}
          onOpenChange={setMeetingRequestOpen}
          accountId={opportunity.accountId.trim()}
          accountName={opportunity.customerName}
          opportunityId={opportunityId}
        />
      )}

      <div className='space-y-6 rounded-lg border bg-card p-4 md:p-5'>
        <div className='border-t pt-6'>
          <div className='mb-4 flex items-center justify-between'>
            <h3 className='font-semibold'>Pipeline Progress</h3>
            {!isClosed && (
              <Button
                variant='outline'
                size='sm'
                onClick={() => setIsStageDialogOpen(true)}
              >
                Change Stage
              </Button>
            )}
          </div>
          <PipelineProgress
            currentStage={opportunity.stage}
            probability={probability}
          />
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='activities'>Activities</TabsTrigger>
          <TabsTrigger value='products'>Products</TabsTrigger>
          <TabsTrigger value='timeline'>Timeline</TabsTrigger>
        </TabsList>

        <TabsContent value='overview' className='mt-6'>
          <OpportunityOverviewTab
            opportunity={opportunity}
            stageLabel={stageConfig.label}
            estimatedValue={estimatedValue}
            weightedValue={weightedValue}
            probability={probability}
            daysInPipeline={daysInPipeline}
            daysUntilClose={daysUntilClose}
            formatCurrency={formatCurrency}
          />
        </TabsContent>

        <TabsContent value='activities' className='mt-6'>
          <OpportunityActivitiesTab
            activities={activities}
            isLoading={isActivitiesLoading}
          />
        </TabsContent>

        <TabsContent value='products' className='mt-6'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Products</h3>
            </CardHeader>
            <CardContent className='text-sm text-muted-foreground'>
              <div className='flex items-start gap-3 rounded-lg border border-dashed p-4'>
                <Package className='mt-0.5 h-4 w-4' />
                <div>
                  <p className='font-medium text-foreground'>
                    Products are not integrated yet
                  </p>
                  <p>
                    This section is currently read-only and waiting for backend
                    product line support.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='timeline' className='mt-6'>
          <Card>
            <CardHeader>
              <h3 className='font-semibold'>Timeline</h3>
            </CardHeader>
            <CardContent className='text-sm text-muted-foreground'>
              <div className='flex items-start gap-3 rounded-lg border border-dashed p-4'>
                <History className='mt-0.5 h-4 w-4' />
                <div>
                  <p className='font-medium text-foreground'>
                    Timeline is not integrated yet
                  </p>
                  <p>
                    Opportunity change history is currently shown only through
                    the available activity feed.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <ChangeStageDialog
        open={isStageDialogOpen}
        onOpenChange={setIsStageDialogOpen}
        onSubmit={() => {
          const actualValue =
            selectedStage === 'CLOSED_WON'
              ? parseOptionalActualValue(wonActualValue)
              : undefined;
          handleStageChange(
            {
              stage: selectedStage as any,
              notes: stageNotes || undefined,
              actualValue,
              lossReason:
                selectedStage === 'CLOSED_LOST'
                  ? lostReason || undefined
                  : undefined,
            },
            'Update opportunity stage successfully',
            () => {
              setIsStageDialogOpen(false);
              resetDialogStates();
            }
          );
        }}
        selectedStage={selectedStage}
        onStageChange={setSelectedStage}
        stageNotes={stageNotes}
        onStageNotesChange={setStageNotes}
        lostReason={lostReason}
        onLostReasonChange={setLostReason}
        wonActualValue={wonActualValue}
        onWonActualValueChange={setWonActualValue}
      />

      <ReopenOpportunityDialog
        open={isReopenDialogOpen}
        onOpenChange={setIsReopenDialogOpen}
        onSubmit={() => {
          handleReopen(reopenStage, reopenReason, () => {
            setIsReopenDialogOpen(false);
            resetDialogStates();
          });
        }}
        reopenStage={reopenStage}
        onReopenStageChange={setReopenStage}
        reopenReason={reopenReason}
        onReopenReasonChange={setReopenReason}
      />

      <DeleteOpportunityDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={handleDelete}
      />
    </div>
  );
};

export default OpportunityDetailPage;
