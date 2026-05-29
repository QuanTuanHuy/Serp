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
} from '../../../api';
import type {
  BillingDeliveryService,
  SurchargeRuleAdminResponse,
  TariffAdminResponse,
  VasRuleAdminResponse,
} from '../../../types';
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
import { SurchargeRuleFormCard } from './SurchargeRuleFormCard';
import { TariffRuleFormCard } from './TariffRuleFormCard';
import { VasRuleFormCard } from './VasRuleFormCard';

export const BillingPricingAdminTab: React.FC = () => {
  const [activeTab, setActiveTab] = React.useState('tariff');
  const [tariffServiceFilter, setTariffServiceFilter] = React.useState<
    BillingDeliveryService | 'ALL'
  >('ALL');

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

  return (
    <div className='space-y-6'>
      <Alert>
        <CircleHelp className='h-4 w-4' />
        <AlertTitle>How to use pricing administration</AlertTitle>
        <AlertDescription>
          View current rules from the billing service, click Edit to load a row
          into the form, then save changes. Tariffs are keyed by service, route
          type, and effective date.
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
            emptyMessage='No tariffs found. Save a tariff rule below or run DB migration seeds.'
            getRowKey={(row) => row.id}
            onEdit={tariff.loadFromSaved}
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

          <TariffRuleFormCard
            form={tariff.form}
            onFormChange={tariff.setForm}
            lastSaved={tariff.lastSaved}
            isLoading={tariff.isLoading}
            onSubmit={tariff.submit}
            onReset={tariff.reset}
          />
        </TabsContent>

        <TabsContent value='surcharge' className='space-y-6'>
          <BillingPricingRulesTable<SurchargeRuleAdminResponse>
            title='Configured surcharge rules'
            description='Optional fees such as remote area or special cargo handling.'
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
            onEdit={surcharge.loadFromSaved}
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

          <SurchargeRuleFormCard
            form={surcharge.form}
            onFormChange={surcharge.setForm}
            calculationTypeHelper={surcharge.calculationTypeHelper}
            lastSaved={surcharge.lastSaved}
            isLoading={surcharge.isLoading}
            onSubmit={surcharge.submit}
            onReset={surcharge.reset}
          />
        </TabsContent>

        <TabsContent value='vas' className='space-y-6'>
          <BillingPricingRulesTable<VasRuleAdminResponse>
            title='Configured VAS rules'
            description='Value-added services such as COD and insurance.'
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
            onEdit={vas.loadFromSaved}
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

          <VasRuleFormCard
            form={vas.form}
            onFormChange={vas.setForm}
            calculationTypeHelper={vas.calculationTypeHelper}
            lastSaved={vas.lastSaved}
            isLoading={vas.isLoading}
            onSubmit={vas.submit}
            onReset={vas.reset}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
};
