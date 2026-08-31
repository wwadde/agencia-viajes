import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  afterNextRender,
  computed,
  effect,
  inject,
  input,
  output,
  PLATFORM_ID,
  viewChild,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import type { LatLngExpression, Map, Marker, Polyline } from 'leaflet';

export interface MapLocation {
  latitude: number;
  longitude: number;
  label?: string;
}

@Component({
  selector: 'app-leaflet-map',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './leaflet-map.component.html',
  styleUrl: './leaflet-map.component.css',
})
export class LeafletMapComponent implements OnDestroy {
  readonly latitude = input<number | null>(null);
  readonly longitude = input<number | null>(null);
  readonly interactive = input(false);
  readonly navigable = input(true);
  readonly popupText = input('');
  readonly locations = input<MapLocation[]>([]);
  readonly locationSelected = output<MapLocation>();

  private readonly mapElement = viewChild.required<ElementRef<HTMLDivElement>>('mapElement');
  private readonly platformId = inject(PLATFORM_ID);
  private map: Map | null = null;
  private markers: Marker[] = [];
  private routeLine: Polyline | null = null;
  private leaflet: typeof import('leaflet') | null = null;
  private destroyed = false;

  readonly hasCoordinates = computed(() =>
    this.isValidLatitude(this.latitude()) && this.isValidLongitude(this.longitude()),
  );

  constructor() {
    afterNextRender(() => {
      if (isPlatformBrowser(this.platformId)) {
        void this.initializeMap();
      }
    });

    effect(() => {
      const latitude = this.latitude();
      const longitude = this.longitude();
      const popupText = this.popupText();
      const locations = this.locations();

      if (this.map) {
        this.renderLocations(locations.length > 0 ? locations : this.singleLocation(latitude, longitude, popupText));
      }
    });
  }

  ngOnDestroy() {
    this.destroyed = true;
    this.map?.remove();
    this.map = null;
    this.markers = [];
    this.routeLine = null;
    this.leaflet = null;
  }

  private async initializeMap() {
    if (this.map || this.destroyed) return;

    const leafletModule = await import('leaflet');
    if (this.destroyed) return;

    this.leaflet = leafletModule;
    const initialLocations = this.locations().length > 0
      ? this.locations()
      : this.singleLocation(this.latitude(), this.longitude(), this.popupText());
    const initialLocation = initialLocations.length > 0
      ? [initialLocations[0].latitude, initialLocations[0].longitude] as LatLngExpression
      : [4.711, -74.072] as LatLngExpression;

    this.map = leafletModule.map(this.mapElement().nativeElement, {
      center: initialLocation,
      zoom: initialLocations.length > 0 ? 12 : 5,
      scrollWheelZoom: this.navigable(),
      dragging: this.navigable(),
      doubleClickZoom: this.navigable(),
      touchZoom: this.navigable(),
      boxZoom: this.navigable(),
      keyboard: this.navigable(),
      zoomControl: true,
    });

    leafletModule
      .tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      })
      .addTo(this.map);

    if (this.interactive()) {
      this.map.on('click', event => {
        this.locationSelected.emit({
          latitude: event.latlng.lat,
          longitude: event.latlng.lng,
        });
      });
    }

    if (initialLocations.length > 0) {
      this.renderLocations(initialLocations);
    }

    setTimeout(() => this.map?.invalidateSize(), 0);
  }

  private renderLocations(locations: MapLocation[]) {
    if (!this.map || !this.leaflet) return;

    this.markers.forEach(marker => marker.remove());
    this.routeLine?.remove();
    this.markers = [];
    this.routeLine = null;

    const validLocations = locations.filter(location =>
      this.isValidLatitude(location.latitude) && this.isValidLongitude(location.longitude));
    validLocations.forEach((location, index) => {
      const marker = this.leaflet!
        .marker([location.latitude, location.longitude], {
          icon: this.leaflet!.icon({
            iconUrl: '/marker.svg',
            iconSize: [42, 42],
            iconAnchor: [21, 42],
            popupAnchor: [0, -38],
          }),
          draggable: false,
        })
        .addTo(this.map!);
      const label = location.label || `Parada ${index + 1}`;
      marker.bindPopup(`<strong>${index + 1}. ${label}</strong>`);
      this.markers.push(marker);
    });

    if (validLocations.length > 1) {
      this.routeLine = this.leaflet.polyline(
        validLocations.map(location => [location.latitude, location.longitude] as LatLngExpression),
        { color: '#0f766e', weight: 4, opacity: 0.85, dashArray: '9 8' },
      ).addTo(this.map);
    }

    if (validLocations.length === 1) {
      this.map.setView([validLocations[0].latitude, validLocations[0].longitude], this.map.getZoom() < 8 ? 12 : this.map.getZoom());
    } else if (validLocations.length > 1) {
      const bounds = validLocations.map(location => [location.latitude, location.longitude] as [number, number]);
      this.map.fitBounds(bounds, { padding: [36, 36] });
    }
  }

  private singleLocation(latitude: number | null, longitude: number | null, label: string): MapLocation[] {
    return this.isValidLatitude(latitude) && this.isValidLongitude(longitude)
      ? [{ latitude, longitude, label }]
      : [];
  }

  private isValidLatitude(value: number | null): value is number {
    return value !== null && Number.isFinite(value) && value >= -90 && value <= 90;
  }

  private isValidLongitude(value: number | null): value is number {
    return value !== null && Number.isFinite(value) && value >= -180 && value <= 180;
  }
}
