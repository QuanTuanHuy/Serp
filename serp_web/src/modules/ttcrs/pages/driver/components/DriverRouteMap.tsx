'use client';

import { useEffect, useRef, useState } from 'react';
import { Loader2, MapPin } from 'lucide-react';
import type { TransportPlanStopDetail } from '../../../types';

declare global {
  interface Window {
    L: any;
  }
}

const LEAFLET_CSS_ID = 'leaflet-css';
const LEAFLET_JS_ID = 'leaflet-js';
const DEFAULT_CENTER: [number, number] = [10.8231, 106.6297];
const osrmCache = new Map<string, [number, number][]>();

async function fetchOsrmRoute(
  coords: [number, number][],
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

interface DriverRouteMapProps {
  stops: TransportPlanStopDetail[];
  /** Highlight this stop as the "current target" (different colour) */
  highlightSeq?: number | null;
}

export function DriverRouteMap({ stops, highlightSeq }: DriverRouteMapProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<any>(null);
  const layerGroupRef = useRef<any>(null);
  const [leafletReady, setLeafletReady] = useState(false);
  const [roadCoords, setRoadCoords] = useState<[number, number][] | null>(null);

  const geocoded = stops
    .filter((s) => s.lat != null && s.lng != null)
    .sort((a, b) => a.sequence - b.sequence);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (window.L) { setLeafletReady(true); return; }
    if (!document.getElementById(LEAFLET_CSS_ID)) {
      const link = document.createElement('link');
      link.id = LEAFLET_CSS_ID; link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }
    if (!document.getElementById(LEAFLET_JS_ID)) {
      const script = document.createElement('script');
      script.id = LEAFLET_JS_ID;
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => setLeafletReady(true);
      document.head.appendChild(script);
    } else {
      const iv = setInterval(() => { if (window.L) { clearInterval(iv); setLeafletReady(true); } }, 100);
      return () => clearInterval(iv);
    }
  }, []);

  useEffect(() => {
    if (!leafletReady || !containerRef.current || mapRef.current) return;
    const L = window.L;
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    });
    const map = window.L.map(containerRef.current).setView(DEFAULT_CENTER, 10);
    window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);
    mapRef.current = map;
    return () => { map.remove(); mapRef.current = null; };
  }, [leafletReady]);

  // Fetch OSRM route
  useEffect(() => {
    const coords = geocoded.map((s) => [s.lat!, s.lng!] as [number, number]);
    fetchOsrmRoute(coords).then(setRoadCoords);
  }, [stops]);

  // Render markers + polyline
  useEffect(() => {
    if (!leafletReady || !mapRef.current) return;
    const L = window.L;
    const map = mapRef.current;

    if (layerGroupRef.current) layerGroupRef.current.remove();
    layerGroupRef.current = L.layerGroup().addTo(map);
    const group = layerGroupRef.current;

    if (roadCoords) {
      L.polyline(roadCoords, { color: '#3b82f6', weight: 4, opacity: 0.85 }).addTo(group);
    }

    // Group stops sharing the same coordinates into one marker
    const coordGroups = new Map<string, typeof geocoded>();
    geocoded.forEach((stop) => {
      const key = `${stop.lat},${stop.lng}`;
      if (!coordGroups.has(key)) coordGroups.set(key, []);
      coordGroups.get(key)!.push(stop);
    });

    const allCoords: [number, number][] = [];
    coordGroups.forEach((stopsAtPos) => {
      const pos: [number, number] = [stopsAtPos[0].lat!, stopsAtPos[0].lng!];
      allCoords.push(pos);

      const hasTarget = stopsAtPos.some((s) => s.sequence === highlightSeq);
      const allDepot = stopsAtPos.every(
        (s) => s.action === 'DEPOT_START' || s.action === 'DEPOT_END',
      );
      const color = hasTarget ? '#f97316' : allDepot ? '#6b7280' : '#3b82f6';

      // Collect all sequence numbers to display on the marker badge
      const seqNumbers = stopsAtPos.map((s) => s.sequence).join(',');
      const multi = stopsAtPos.length > 1;
      // Wider pill for multiple sequences, circle for single
      const width = multi ? Math.max(28, 14 + seqNumbers.length * 7) : 28;

      const svgIcon = L.divIcon({
        className: '',
        html: `<div style="
          min-width:${width}px;height:28px;border-radius:14px;
          padding:0 5px;
          background:${color};color:#fff;
          display:flex;align-items:center;justify-content:center;
          font-weight:700;font-size:11px;
          border:2px solid #fff;
          box-shadow:0 1px 4px rgba(0,0,0,0.4);
          white-space:nowrap;
        ">${seqNumbers}</div>`,
        iconSize: [width, 28],
        iconAnchor: [width / 2, 14],
      });

      const popupRows = stopsAtPos
        .map(
          (s) =>
            `<div style="margin-bottom:4px">
              <b>${s.sequence}. ${s.locationCode}</b><br>
              <span style="color:#888">${s.action.replace(/_/g, ' ')}</span>
              ${s.plannedArrivalTime ? `<br><span style="color:#555">ETA: ${s.plannedArrivalTime.replace('T', ' ').slice(0, 16)}</span>` : ''}
            </div>`,
        )
        .join('<hr style="margin:4px 0;border-color:#eee">');

      L.marker(pos, { icon: svgIcon })
        .bindPopup(`<div style="font-size:12px;line-height:1.6">${popupRows}</div>`)
        .addTo(group);
    });

    if (allCoords.length >= 2) {
      map.fitBounds(L.latLngBounds(allCoords), { padding: [30, 30], maxZoom: 14 });
    } else if (allCoords.length === 1) {
      map.setView(allCoords[0], 13);
    }
  }, [geocoded, roadCoords, leafletReady, highlightSeq]);

  const hasCoords = geocoded.length > 0;

  return (
    <div className="relative z-0 h-full w-full">
      {!leafletReady && (
        <div className="absolute inset-0 z-10 flex items-center justify-center gap-2 bg-muted rounded-lg">
          <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
          <span className="text-sm text-muted-foreground">Loading map…</span>
        </div>
      )}
      {leafletReady && !hasCoords && (
        <div className="pointer-events-none absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-muted/60 rounded-lg">
          <MapPin className="h-8 w-8 text-muted-foreground/50" />
          <span className="text-xs text-muted-foreground">
            No GPS coordinates on locations — add lat/lng to locations to see the map
          </span>
        </div>
      )}
      <div ref={containerRef} className="relative z-0 h-full w-full rounded-lg" />
    </div>
  );
}
