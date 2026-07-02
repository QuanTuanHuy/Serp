/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Pricing administration tab
 */

'use client';

import React from 'react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  TableCell,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { CircleHelp } from 'lucide-react';
import {
  useListSurchargeRulesQuery,
  useListTariffsQuery,
  useListVasRulesQuery,
} from '../../../../api';
import type {
  BillingDeliveryService,
  SurchargeRuleAdminResponse,
  TariffAdminResponse,
  VasRuleAdminResponse,
} from '../../../../types';
import {
  useSurchargeRuleForm,
  useTariffRuleForm,
  useVasRuleForm,
} from '../hooks';
import {
  BillingPricingRulesTable,
  formatTariffMoney,
  getTariffRouteLabel,
} from './BillingPricingRulesTable';
import {
  SurchargeRuleFormDialog,
  type SurchargeRuleFormMode,
} from './SurchargeRuleFormDialog';
import {
  TariffRuleFormDialog,
  type TariffRuleFormMode,
} from './TariffRuleFormDialog';
import { VasRuleFormDialog, type VasRuleFormMode } from './VasRuleFormDialog';

export const BillingPricingAdminTab: React.FC = () => {
  const [activeTab, setActiveTab] = React.useState('tariff');
  const [tariffServiceFilter, setTariffServiceFilter] = React.useState<
    BillingDeliveryService | 'ALL'
  >('ALL');

  const [tariffDialogOpen, setTariffDialogOpen] = React.useState(false);
  const [tariffFormMode, setTariffFormMode] =
    React.useState<TariffRuleFormMode>('create');
  const [surchargeDialogOpen, setSurchargeDialogOpen] = React.useState(false);
  const [surchargeFormMode, setSurchargeFormMode] =
    React.useState<SurchargeRuleFormMode>('create');
  const [vasDialogOpen, setVasDialogOpen] = React.useState(false);
  const [vasFormMode, setVasFormMode] =
    React.useState<VasRuleFormMode>('create');

  const tariff = useTariffRuleForm();
  const surcharge = useSurchargeRuleForm();
  const vas = useVasRuleForm();

  const tariffQuery = useListTariffsQuery(
    tariffServiceFilter === 'ALL'
      ? undefined
      : { serviceCode: tariffServiceFilter }
  );
  const surchargeQuery = useListSurchargeRulesQuery();
  const vasQuery = useListVasRulesQuery();

  const filteredTariffs = React.useMemo(() => {
    const items = tariffQuery.data ?? [];
    if (tariffServiceFilter === 'ALL') {
      return items;
    }
    return items.filter((item) => item.serviceCode === tariffServiceFilter);
  }, [tariffQuery.data, tariffServiceFilter]);

  const openTariffCreate = () => {
    tariff.reset();
    setTariffFormMode('create');
    setTariffDialogOpen(true);
  };

  const openTariffEdit = (row: TariffAdminResponse) => {
    tariff.loadFromSaved(row);
    setTariffFormMode('edit');
    setTariffDialogOpen(true);
  };

  const handleTariffDialogChange = (open: boolean) => {
    if (!open && !tariff.isLoading) {
      setTariffDialogOpen(false);
      tariff.reset();
    }
  };

  const handleTariffSubmit = async (event: React.FormEvent) => {
    const saved = await tariff.submit(event);
    if (saved) {
      setTariffDialogOpen(false);
      tariff.reset();
    }
  };

  const openSurchargeCreate = () => {
    surcharge.reset();
    setSurchargeFormMode('create');
    setSurchargeDialogOpen(true);
  };

  const openSurchargeEdit = (row: SurchargeRuleAdminResponse) => {
    surcharge.loadFromSaved(row);
    setSurchargeFormMode('edit');
    setSurchargeDialogOpen(true);
  };

  const handleSurchargeDialogChange = (open: boolean) => {
    if (!open && !surcharge.isLoading) {
      setSurchargeDialogOpen(false);
      surcharge.reset();
    }
  };

  const handleSurchargeSubmit = async (event: React.FormEvent) => {
    const saved = await surcharge.submit(event);
    if (saved) {
      setSurchargeDialogOpen(false);
      surcharge.reset();
    }
  };

  const openVasCreate = () => {
    vas.reset();
    setVasFormMode('create');
    setVasDialogOpen(true);
  };

  const openVasEdit = (row: VasRuleAdminResponse) => {
    vas.loadFromSaved(row);
    setVasFormMode('edit');
    setVasDialogOpen(true);
  };

  const handleVasDialogChange = (open: boolean) => {
    if (!open && !vas.isLoading) {
      setVasDialogOpen(false);
      vas.reset();
    }
  };

  const handleVasSubmit = async (event: React.FormEvent) => {
    const saved = await vas.submit(event);
    if (saved) {
      setVasDialogOpen(false);
      vas.reset();
    }
  };

  return (
    <div className='space-y-6'>
      <Alert>
        <CircleHelp className='h-4 w-4' />
        <AlertTitle>How to use pricing administration</AlertTitle>
        <AlertDescription>
          View current rules from the billing service. Click Edit on a row or
          Add to open the form in a dialog, then save changes. Tariffs are keyed
          by service, route type, and effective date.
        </AlertDescription>
      </Alert>

      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList>
          <TabsTrigger value='tariff'>Tariff</TabsTrigger>
          <TabsTrigger value='surcharge'>Surcharge</TabsTrigger>
          <TabsTrigger value='vas'>VAS</TabsTrigger>
        </TabsList>

        <TabsContent value='tariff' className='space-y-6'>
          <BillingPricingRulesTable<TariffAdminResponse>
            title='Configured tariffs'
            description='Base freight ladder per delivery service and route type.'
            columns={[
              { key: 'service', label: 'Service' },
              { key: 'route', label: 'Route' },
              { key: 'base', label: 'Base (weight / price)' },
              { key: 'step', label: 'Step (weight / price)' },
              { key: 'dates', label: 'Effective' },
            ]}
            rows={filteredTariffs}
            isLoading={tariffQuery.isLoading}
            isError={tariffQuery.isError}
            emptyMessage='No tariffs found. Add a tariff rule or run DB migration seeds.'
            getRowKey={(row) => row.id}
            onEdit={openTariffEdit}
            onCreate={openTariffCreate}
            createLabel='Add tariff'
            serviceFilter={{
              value: tariffServiceFilter,
              onChange: setTariffServiceFilter,
            }}
            renderCells={(row) => (
              <>
                <TableCell className='font-medium'>{row.serviceCode}</TableCell>
                <TableCell>{getTariffRouteLabel(row.routeTypeCode)}</TableCell>
                <TableCell>
                  {row.baseWeight}g / {formatTariffMoney(row.basePrice)}
                </TableCell>
                <TableCell>
                  {row.stepWeight}g / {formatTariffMoney(row.stepPrice)}
                </TableCell>
                <TableCell>
                  {row.effectiveDate}
                  {row.expirationDate ? ` → ${row.expirationDate}` : ''}
                </TableCell>
              </>
            )}
          />
        </TabsContent>

        <TabsContent value='surcharge' className='space-y-6'>
          <BillingPricingRulesTable<SurchargeRuleAdminResponse>
            title='Configured surcharge rules'
            description='Optional fees such as remote-area handling.'
            columns={[
              { key: 'code', label: 'Code' },
              { key: 'name', label: 'Name' },
              { key: 'type', label: 'Calculation' },
              { key: 'amount', label: 'Amount config' },
            ]}
            rows={surchargeQuery.data ?? []}
            isLoading={surchargeQuery.isLoading}
            isError={surchargeQuery.isError}
            emptyMessage='No surcharge rules found.'
            getRowKey={(row) => row.id}
            onEdit={openSurchargeEdit}
            onCreate={openSurchargeCreate}
            createLabel='Add surcharge'
            renderCells={(row) => (
              <>
                <TableCell className='font-medium'>{row.code}</TableCell>
                <TableCell>{row.name}</TableCell>
                <TableCell>{row.calculationType}</TableCell>
                <TableCell>
                  {row.calculationType === 'STEP_WEIGHT'
                    ? `${row.baseWeight ?? '-'}g / ${formatTariffMoney(row.basePrice)} + ${row.stepWeight ?? '-'}g / ${formatTariffMoney(row.stepPrice)}`
                    : row.fixedAmount != null
                      ? formatTariffMoney(row.fixedAmount)
                      : row.ratePercent != null
                        ? `${row.ratePercent}%`
                        : '—'}
                </TableCell>
              </>
            )}
          />
        </TabsContent>

        <TabsContent value='vas' className='space-y-6'>
          <BillingPricingRulesTable<VasRuleAdminResponse>
            title='Configured VAS rules'
            description='Value-added services such as COD.'
            columns={[
              { key: 'code', label: 'Code' },
              { key: 'name', label: 'Name' },
              { key: 'type', label: 'Calculation' },
              { key: 'amount', label: 'Rate / amount' },
            ]}
            rows={vasQuery.data ?? []}
            isLoading={vasQuery.isLoading}
            isError={vasQuery.isError}
            emptyMessage='No VAS rules found.'
            getRowKey={(row) => row.id}
            onEdit={openVasEdit}
            onCreate={openVasCreate}
            createLabel='Add VAS rule'
            renderCells={(row) => (
              <>
                <TableCell className='font-medium'>{row.code}</TableCell>
                <TableCell>{row.name}</TableCell>
                <TableCell>{row.calculationType}</TableCell>
                <TableCell>
                  {row.ratePercent != null
                    ? `${row.ratePercent}% (min ${formatTariffMoney(row.minAmount)})`
                    : row.fixedAmount != null
                      ? formatTariffMoney(row.fixedAmount)
                      : '—'}
                </TableCell>
              </>
            )}
          />
        </TabsContent>
      </Tabs>

      <TariffRuleFormDialog
        open={tariffDialogOpen}
        mode={tariffFormMode}
        form={tariff.form}
        onFormChange={tariff.setForm}
        isLoading={tariff.isLoading}
        onOpenChange={handleTariffDialogChange}
        onSubmit={handleTariffSubmit}
      />

      <SurchargeRuleFormDialog
        open={surchargeDialogOpen}
        mode={surchargeFormMode}
        form={surcharge.form}
        onFormChange={surcharge.setForm}
        calculationTypeHelper={surcharge.calculationTypeHelper}
        isLoading={surcharge.isLoading}
        onOpenChange={handleSurchargeDialogChange}
        onSubmit={handleSurchargeSubmit}
      />

      <VasRuleFormDialog
        open={vasDialogOpen}
        mode={vasFormMode}
        form={vas.form}
        onFormChange={vas.setForm}
        calculationTypeHelper={vas.calculationTypeHelper}
        isLoading={vas.isLoading}
        onOpenChange={handleVasDialogChange}
        onSubmit={handleVasSubmit}
      />
    </div>
  );
};
