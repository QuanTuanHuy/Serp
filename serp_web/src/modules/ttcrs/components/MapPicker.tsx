'use client';

import React, { useEffect, useRef, useState } from 'react';
import { Loader2, MapPin } from 'lucide-react';

// Extend Window to hold the dynamically loaded Leaflet global
declare global {
  interface Window {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    L: any;
  }
}

interface MapPickerProps {
  /** Currently selected latitude (null = nothing selected yet) */
  lat: number | null;
  /** Currently selected longitude (null = nothing selected yet) */
  lng: number | null;
  /** Called when the user clicks the map to pick a new coordinate */
  onPick: (lat: number, lng: number) => void;
  /** When true the map is view-only; clicks don't move the pin */
  readonly?: boolean;
}

const LEAFLET_CSS_ID = 'leaflet-css';
const LEAFLET_JS_ID = 'leaflet-js';
// Default centre: Vietnam
const DEFAULT_CENTER: [number, number] = [16.0, 106.0];
const DEFAULT_ZOOM = 6;

/**
 * An interactive OpenStreetMap tile map rendered via Leaflet (CDN).
 * The user clicks anywhere on the map to pick a coordinate; a pin is placed
 * at the selected spot and `onPick` is called with the rounded lat/lng.
 *
 * No npm package installation is required — Leaflet is loaded from
 * https://unpkg.com/leaflet@1.9.4 the first time this component mounts.
 */
export function MapPicker({ lat, lng, onPick, readonly }: MapPickerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const mapRef = useRef<any>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const markerRef = useRef<any>(null);
  // Stable ref so the Leaflet click handler always calls the latest `onPick`
  const onPickRef = useRef(onPick);
  // Capture the initial lat/lng at mount time so Step 2 can place the pin
  const initialLatRef = useRef(lat);
  const initialLngRef = useRef(lng);
  const [leafletReady, setLeafletReady] = useState(false);

  // Keep the stable ref up-to-date
  useEffect(() => {
    onPickRef.current = onPick;
  }, [onPick]);

  // ── Step 1: load Leaflet CSS + JS from CDN ──────────────────────────────
  useEffect(() => {
    if (typeof window === 'undefined') return;

    // Already loaded in a previous mount
    if (window.L) {
      setLeafletReady(true);
      return;
    }

    // Inject CSS once
    if (!document.getElementById(LEAFLET_CSS_ID)) {
      const link = document.createElement('link');
      link.id = LEAFLET_CSS_ID;
      link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }

    // Inject JS once, then signal ready
    if (!document.getElementById(LEAFLET_JS_ID)) {
      const script = document.createElement('script');
      script.id = LEAFLET_JS_ID;
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => setLeafletReady(true);
      script.onerror = () =>
        console.error('[MapPicker] Failed to load Leaflet from CDN');
      document.head.appendChild(script);
    } else {
      // Script tag exists but may still be loading — poll until ready
      const interval = setInterval(() => {
        if (window.L) {
          clearInterval(interval);
          setLeafletReady(true);
        }
      }, 100);
      return () => clearInterval(interval);
    }
  }, []);

  // ── Step 2: initialise Leaflet map after CDN is ready ───────────────────
  useEffect(() => {
    if (!leafletReady || !containerRef.current || mapRef.current) return;

    const L = window.L;

    // Fix default icon path broken by webpack asset hashing
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl:
        'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl:
        'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    });

    const initLat = initialLatRef.current;
    const initLng = initialLngRef.current;
    const hasInitialPin = initLat !== null && initLng !== null;

    const map = L.map(containerRef.current).setView(
      hasInitialPin ? [initLat, initLng] : DEFAULT_CENTER,
      hasInitialPin ? 13 : DEFAULT_ZOOM,
    );

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '© <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap</a> contributors',
      maxZoom: 19,
    }).addTo(map);

    // Place the initial marker if coordinates were provided on mount
    if (hasInitialPin) {
      markerRef.current = L.marker([initLat, initLng]).addTo(map);
    }

    if (!readonly) {
      map.on('click', (e: any) => {
        const pickedLat = parseFloat(e.latlng.lat.toFixed(6));
        const pickedLng = parseFloat(e.latlng.lng.toFixed(6));

        if (markerRef.current) {
          markerRef.current.setLatLng([pickedLat, pickedLng]);
        } else {
          markerRef.current = L.marker([pickedLat, pickedLng]).addTo(map);
        }

        onPickRef.current(pickedLat, pickedLng);
      });
    }

    mapRef.current = map;

    return () => {
      map.remove();
      mapRef.current = null;
      markerRef.current = null;
    };
    // Only run once after Leaflet is ready
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [leafletReady]);

  // ── Step 3: sync external lat/lng prop → marker on the map ─────────────
  useEffect(() => {
    if (!mapRef.current || !window.L || lat === null || lng === null) return;

    const L = window.L;

    if (markerRef.current) {
      markerRef.current.setLatLng([lat, lng]);
    } else {
      markerRef.current = L.marker([lat, lng]).addTo(mapRef.current);
    }

    mapRef.current.setView([lat, lng], 13);
  }, [lat, lng]);

  return (
    <div className='relative w-full overflow-hidden rounded-lg border border-border'>
      {/* Loading overlay */}
      {!leafletReady && (
        <div className='absolute inset-0 z-10 flex items-center justify-center gap-2 bg-muted'>
          <Loader2 className='h-5 w-5 animate-spin text-muted-foreground' />
          <span className='text-sm text-muted-foreground'>Loading map…</span>
        </div>
      )}

      {/* Instruction banner */}
      {leafletReady && lat === null && (
        <div className='pointer-events-none absolute bottom-2 left-1/2 z-10 -translate-x-1/2 rounded-full bg-background/90 px-3 py-1 text-xs text-muted-foreground shadow'>
          <MapPin className='mr-1 inline h-3 w-3' />
          Click on the map to place a pin
        </div>
      )}

      {/* Map container — Leaflet renders inside this div */}
      <div ref={containerRef} className='h-64 w-full' />
    </div>
  );
}
