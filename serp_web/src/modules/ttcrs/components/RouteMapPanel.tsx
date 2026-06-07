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
const osrmCache = new Map<string, [number, number][]>();

async function fetchOsrmRoute(
  coords: [number, number][]
): Promise<[number, number][] | null> {
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
        L.polyline(roadCoords, {
          color: route.color,
          weight: 4,
          opacity: 0.85,
        }).addTo(group);
      }

      route.stops.forEach((stop) => {
        L.circleMarker([stop.lat, stop.lng], {
          radius: stop.isDepot ? 5 : 8,
          color: route.color,
          fillColor: stop.isDepot ? '#ffffff' : route.color,
          fillOpacity: stop.isDepot ? 0.9 : 1,
          weight: 2,
        })
          .bindPopup(
            `<div style="font-size:12px;line-height:1.5">
              <span style="font-family:monospace;font-weight:700">${stop.locationCode}</span><br>
              <span style="color:#888">${stop.action}</span>
              <br><span style="font-weight:600;color:${route.color}">${route.truckCode}</span>
            </div>`
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
