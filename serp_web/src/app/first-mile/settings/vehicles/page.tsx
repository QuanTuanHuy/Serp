/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS vehicle settings route
 */

import {
  VehicleSettingsPage,
  type VehicleSettingsScope,
} from '@/modules/first-mile';

type VehicleSettingsSearchParams = Record<
  string,
  string | string[] | undefined
>;

interface VehicleSettingsRoutePageProps {
  searchParams: Promise<VehicleSettingsSearchParams>;
}

function resolveInitialScope(
  searchParams: VehicleSettingsSearchParams
): VehicleSettingsScope {
  const scope = searchParams.scope;
  const value = Array.isArray(scope) ? scope[0] : scope;

  return value === 'second-mile' ? 'second-mile' : 'first-mile';
}

export default async function FirstMileVehicleSettingsRoutePage({
  searchParams,
}: VehicleSettingsRoutePageProps) {
  const initialScope = resolveInitialScope(await searchParams);

  return <VehicleSettingsPage initialScope={initialScope} />;
}
