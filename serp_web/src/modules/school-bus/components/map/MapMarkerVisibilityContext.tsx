'use client';

import * as React from 'react';
import type { MarkerKind } from './mapIcons';

interface MapMarkerVisibilityContextValue {
  /** Set of currently visible marker kinds */
  visibleKinds: Set<MarkerKind>;
  /** Toggle a specific marker kind on/off */
  toggleKind: (kind: MarkerKind) => void;
  /** Check if a marker kind is visible */
  isVisible: (kind: MarkerKind) => boolean;
}

const ALL_KINDS: MarkerKind[] = [
  'school', 'depot', 'pickup', 'dropoff', 'start', 'end', 'bus', 'student',
];

const MapMarkerVisibilityContext = React.createContext<MapMarkerVisibilityContextValue>({
  visibleKinds: new Set(ALL_KINDS),
  toggleKind: () => {},
  isVisible: () => true,
});

export function MapMarkerVisibilityProvider({ children }: { children: React.ReactNode }) {
  const [visibleKinds, setVisibleKinds] = React.useState<Set<MarkerKind>>(
    () => new Set(ALL_KINDS)
  );

  const toggleKind = React.useCallback((kind: MarkerKind) => {
    setVisibleKinds((prev) => {
      const next = new Set(prev);
      if (next.has(kind)) {
        next.delete(kind);
      } else {
        next.add(kind);
      }
      return next;
    });
  }, []);

  const isVisible = React.useCallback(
    (kind: MarkerKind) => visibleKinds.has(kind),
    [visibleKinds]
  );

  const value = React.useMemo(
    () => ({ visibleKinds, toggleKind, isVisible }),
    [visibleKinds, toggleKind, isVisible]
  );

  return (
    <MapMarkerVisibilityContext.Provider value={value}>
      {children}
    </MapMarkerVisibilityContext.Provider>
  );
}

export function useMapMarkerVisibility() {
  return React.useContext(MapMarkerVisibilityContext);
}
