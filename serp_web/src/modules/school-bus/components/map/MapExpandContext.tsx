'use client';

import * as React from 'react';

interface MapExpandContextValue {
  /** True while the SchoolBusMapWorkspace is in fullscreen/expanded mode. */
  isExpanded: boolean;
  /**
   * Counter that bumps each time expanded ↔ collapsed.
   * Map clients use this as a trigger for `invalidateSize()`.
   */
  expandKey: number;
}

const MapExpandContext = React.createContext<MapExpandContextValue>({
  isExpanded: false,
  expandKey: 0,
});

/** Consume the nearest SchoolBusMapWorkspace expand state. */
export function useMapExpand(): MapExpandContextValue {
  return React.useContext(MapExpandContext);
}

export { MapExpandContext };
