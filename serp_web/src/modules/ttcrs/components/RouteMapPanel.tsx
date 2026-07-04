'use client';

import { useEffect, useRef, useState } from 'react';
import { Loader2, MapPin } from 'lucide-react';

declare global {
  interface Window {
    L: any;
  }
}

export interface RouteStopPoint {
  locationCode: string;
  action: string;
  arrivalTime: string;
  isDepot: boolean;
  lat: number;
  lng: number;
}

export interface RoutePolylineData {
  truckCode: string;
  color: string;
  coords: [number, number][];
  stops: RouteStopPoint[];
}

const LEAFLET_CSS_ID = 'leaflet-css';
const LEAFLET_JS_ID = 'leaflet-js';
const DEFAULT_CENTER: [number, number] = [10.8231, 106.6297]; // Ho Chi Minh City
const DEFAULT_ZOOM = 10;
const OSRM_DEBOUNCE_MS = 200;
const OFFSET_INCREMENT = 0.008; // km offset per route (for parallel offset)
const osrmCache = new Map<string, [number, number][]>();

/**
 * Calculate bearing (angle) between two points in degrees [0-360]
 */
function calculateBearing(from: [number, number], to: [number, number]): number {
  const [lat1, lng1] = from;
  const [lat2, lng2] = to;
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const lat1Rad = lat1 * Math.PI / 180;
  const lat2Rad = lat2 * Math.PI / 180;

  const y = Math.sin(dLng) * Math.cos(lat2Rad);
  const x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng);
  const bearing = Math.atan2(y, x) * 180 / Math.PI;
  return (bearing + 360) % 360;
}

/**
 * Offset a point perpendicular to bearing by given distance (in km)
 * positive offset = right side of bearing, negative = left side
 */
function offsetPoint(point: [number, number], bearing: number, offsetKm: number): [number, number] {
  const [lat, lng] = point;
  const R = 6371; // Earth radius in km
  const dLat = (offsetKm / R) * 180 / Math.PI;
  const dLng = (offsetKm / (R * Math.cos(lat * Math.PI / 180))) * 180 / Math.PI;

  // Perpendicular bearing (90 degrees to the right of original bearing)
  const perpBearing = (bearing + 90) * Math.PI / 180;
  const offsetLat = lat + dLat * Math.cos(perpBearing);
  const offsetLng = lng + dLng * Math.sin(perpBearing);

  return [offsetLat, offsetLng];
}

/**
 * Apply parallel offset to entire polyline
 * Calculates perpendicular offset for each point along the route
 */
function applyOffsetToPolyline(coords: [number, number][], offsetKm: number): [number, number][] {
  if (coords.length < 2 || offsetKm === 0) return coords;

  const offsetCoords: [number, number][] = [];

  for (let i = 0; i < coords.length; i++) {
    let bearing: number;

    // Use bearing from next point if available, otherwise from previous
    if (i < coords.length - 1) {
      bearing = calculateBearing(coords[i], coords[i + 1]);
    } else {
      bearing = calculateBearing(coords[i - 1], coords[i]);
    }

    offsetCoords.push(offsetPoint(coords[i], bearing, offsetKm));
  }

  return offsetCoords;
}

async function fetchOsrmRoute(coords: [number, number][]): Promise<[number, number][] | null> {
  if (coords.length < 2) return null;

  const key = JSON.stringify(coords);
  if (osrmCache.has(key)) return osrmCache.get(key)!;

  const waypoints = coords.map(([lat, lng]) => `${lng},${lat}`).join(';');
  const url = `https://router.project-osrm.org/route/v1/driving/${waypoints}?overview=full&geometries=geojson`;

  try {
    const res = await fetch(url, { signal: AbortSignal.timeout(8000) });
    if (!res.ok) return null;
    const data = await res.json();
    if (data.code !== 'Ok' || !data.routes?.[0]) return null;

    const geometry: [number, number][] = (
      data.routes[0].geometry.coordinates as [number, number][]
    ).map(([lon, lat]) => [lat, lon]);

    osrmCache.set(key, geometry);
    return geometry;
  } catch {
    return null;
  }
}

interface RouteMapPanelProps {
  routes: RoutePolylineData[];
}

export function RouteMapPanel({ routes }: RouteMapPanelProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<any>(null);
  const routeGroupsRef = useRef<any[]>([]);
  const [leafletReady, setLeafletReady] = useState(false);

  const [roadGeometries, setRoadGeometries] = useState<
    Map<number, [number, number][]>
  >(new Map());
  const fetchDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    if (window.L) {
      setLeafletReady(true);
      return;
    }

    if (!document.getElementById(LEAFLET_CSS_ID)) {
      const link = document.createElement('link');
      link.id = LEAFLET_CSS_ID;
      link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }

    if (!document.getElementById(LEAFLET_JS_ID)) {
      const script = document.createElement('script');
      script.id = LEAFLET_JS_ID;
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => setLeafletReady(true);
      script.onerror = () =>
        console.error('[RouteMapPanel] Failed to load Leaflet');
      document.head.appendChild(script);
    } else {
      const interval = setInterval(() => {
        if (window.L) {
          clearInterval(interval);
          setLeafletReady(true);
        }
      }, 100);
      return () => clearInterval(interval);
    }
  }, []);

  useEffect(() => {
    if (!leafletReady || !containerRef.current || mapRef.current) return;

    const L = window.L;

    delete (L.Icon.Default.prototype as Record<string, unknown>)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl:
        'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl:
        'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    });

    const map = L.map(containerRef.current).setView(
      DEFAULT_CENTER,
      DEFAULT_ZOOM
    );
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '© <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);

    mapRef.current = map;

    return () => {
      map.remove();
      mapRef.current = null;
      routeGroupsRef.current = [];
    };
  }, [leafletReady]);

  useEffect(() => {
    if (fetchDebounceRef.current) clearTimeout(fetchDebounceRef.current);

    fetchDebounceRef.current = setTimeout(async () => {
      const entries = await Promise.all(
        routes.map(async (route, index) => {
          const geo = await fetchOsrmRoute(route.coords);
          return [index, geo] as const;
        })
      );

      setRoadGeometries(
        new Map(
          entries
            .filter(([, geo]) => geo !== null)
            .map(([index, geo]) => [index, geo as [number, number][]])
        )
      );
    }, OSRM_DEBOUNCE_MS);

    return () => {
      if (fetchDebounceRef.current) clearTimeout(fetchDebounceRef.current);
    };
  }, [routes]);

  useEffect(() => {
    if (!leafletReady || !mapRef.current) return;

    const L = window.L;
    const map = mapRef.current;

    routeGroupsRef.current.forEach((g) => g.remove());
    routeGroupsRef.current = [];

    const allCoords: [number, number][] = [];

    routes.forEach((route, index) => {
      if (route.coords.length === 0) return;

      const group = L.layerGroup().addTo(map);

      const roadCoords = roadGeometries.get(index);
      if (roadCoords) {
        // Calculate offset to separate overlapping routes
        // Even index: positive offset (right side), Odd: negative (left side)
        // Multiply by (Math.floor(index / 2) + 1) to spread further for more routes
        const offsetMultiplier = Math.floor(index / 2) + 1;
        const offsetAmount = index % 2 === 0
          ? offsetMultiplier * OFFSET_INCREMENT
          : -offsetMultiplier * OFFSET_INCREMENT;

        const offsetRoadCoords = applyOffsetToPolyline(roadCoords, offsetAmount);

        L.polyline(offsetRoadCoords, {
          color:   route.color,
          weight:  4,
          opacity: 0.85,
        }).addTo(group);
      }

      // Group stops by locationCode so overlapping markers show combined sequences e.g. "1,4"
      const byLocation = new Map<
        string,
        { stop: (typeof route.stops)[0]; seqs: number[] }
      >();
      route.stops.forEach((stop, stopIdx) => {
        const key = stop.locationCode;
        const existing = byLocation.get(key);
        if (existing) {
          existing.seqs.push(stopIdx + 1);
        } else {
          byLocation.set(key, { stop, seqs: [stopIdx + 1] });
        }
      });

      byLocation.forEach(({ stop, seqs }) => {
        const label = seqs.join(',');
        const isDepot = stop.isDepot;
        const multiVisit = seqs.length > 1;
        // Wider pill for multi-sequence labels
        const width = multiVisit ? Math.max(32, label.length * 8 + 16) : (isDepot ? 22 : 26);
        const height = isDepot ? 22 : 26;
        const bg = isDepot ? '#ffffff' : route.color;
        const fg = isDepot ? route.color : '#ffffff';
        const border = isDepot ? `2px solid ${route.color}` : '2px solid #fff';
        const borderRadius = multiVisit ? '12px' : '50%';
        const icon = L.divIcon({
          className: '',
          html: `<div style="min-width:${width}px;height:${height}px;padding:0 4px;border-radius:${borderRadius};background:${bg};color:${fg};display:flex;align-items:center;justify-content:center;font-weight:700;font-size:10px;border:${border};box-shadow:0 1px 4px rgba(0,0,0,0.35);white-space:nowrap">${label}</div>`,
          iconSize: [width, height],
          iconAnchor: [width / 2, height / 2],
        });
        const actions = seqs
          .map((s, i) => `${s}: ${route.stops[s - 1]?.action ?? ''}`)
          .join('<br>');

        // Apply offset to marker position (same logic as polyline offset)
        const offsetMultiplier = Math.floor(index / 2) + 1;
        const offsetAmount = index % 2 === 0
          ? offsetMultiplier * OFFSET_INCREMENT
          : -offsetMultiplier * OFFSET_INCREMENT;

        const [offsetLat, offsetLng] = offsetPoint([stop.lat, stop.lng], 45, offsetAmount);

        L.marker([offsetLat, offsetLng], { icon })
          .bindPopup(
            `<div style="font-size:12px;line-height:1.6">
              <span style="font-family:monospace;font-weight:700">${stop.locationCode}</span><br>
              <span style="color:#888">${actions}</span><br>
              <span style="font-weight:600;color:${route.color}">${route.truckCode}</span>
            </div>`,
          )
          .addTo(group);
      });

      routeGroupsRef.current.push(group);
      allCoords.push(...route.coords);
    });

    if (allCoords.length >= 2) {
      map.fitBounds(L.latLngBounds(allCoords), {
        padding: [30, 30],
        maxZoom: 14,
      });
    } else if (allCoords.length === 1) {
      map.setView(allCoords[0], 13);
    }
  }, [routes, roadGeometries, leafletReady]);

  return (
    <div className='relative h-full w-full'>
      {!leafletReady && (
        <div className='absolute inset-0 z-10 flex items-center justify-center gap-2 bg-muted rounded-lg'>
          <Loader2 className='h-5 w-5 animate-spin text-muted-foreground' />
          <span className='text-sm text-muted-foreground'>Loading map…</span>
        </div>
      )}

      {leafletReady && routes.every((r) => r.coords.length === 0) && (
        <div className='pointer-events-none absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-muted/60 rounded-lg'>
          <MapPin className='h-8 w-8 text-muted-foreground/50' />
          <span className='text-xs text-muted-foreground'>
            No geocoded locations — add GPS coordinates to your locations to see
            routes here
          </span>
        </div>
      )}

      <div ref={containerRef} className='h-full w-full rounded-lg' />
    </div>
  );
}
