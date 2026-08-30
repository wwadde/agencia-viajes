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
import type { LatLngExpression, Map, Marker } from 'leaflet';

export interface MapLocation {
  latitude: number;
  longitude: number;
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
  readonly locationSelected = output<MapLocation>();

  private readonly mapElement = viewChild.required<ElementRef<HTMLDivElement>>('mapElement');
  private readonly platformId = inject(PLATFORM_ID);
  private map: Map | null = null;
  private marker: Marker | null = null;
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

      if (this.map && this.isValidLatitude(latitude) && this.isValidLongitude(longitude)) {
        this.setLocation(latitude, longitude, popupText);
      }
    });
  }

  ngOnDestroy() {
    this.destroyed = true;
    this.map?.remove();
    this.map = null;
    this.marker = null;
    this.leaflet = null;
  }

  private async initializeMap() {
    if (this.map || this.destroyed) return;

    const leafletModule = await import('leaflet');
    if (this.destroyed) return;

    this.leaflet = leafletModule;
    const initialLocation = this.hasCoordinates()
      ? [this.latitude()!, this.longitude()!] as LatLngExpression
      : [4.711, -74.072] as LatLngExpression;

    this.map = leafletModule.map(this.mapElement().nativeElement, {
      center: initialLocation,
      zoom: this.hasCoordinates() ? 12 : 5,
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

    if (this.hasCoordinates()) {
      this.setLocation(this.latitude()!, this.longitude()!, this.popupText());
    }

    setTimeout(() => this.map?.invalidateSize(), 0);
  }

  private setLocation(latitude: number, longitude: number, popupText: string) {
    if (!this.map || !this.leaflet) return;

    const location: LatLngExpression = [latitude, longitude];
    if (!this.marker) {
      this.marker = this.leaflet
        .marker(location, {
          icon: this.leaflet.icon({
            iconUrl: '/marker.svg',
            iconSize: [42, 42],
            iconAnchor: [21, 42],
            popupAnchor: [0, -38],
          }),
          draggable: false,
        })
        .addTo(this.map);
    } else {
      this.marker.setLatLng(location);
    }

    this.map.setView(location, this.map.getZoom() < 8 ? 12 : this.map.getZoom());

    if (popupText) {
      this.marker.bindPopup(popupText);
    } else {
      this.marker.closePopup();
    }
  }

  private isValidLatitude(value: number | null): value is number {
    return value !== null && Number.isFinite(value) && value >= -90 && value <= 90;
  }

  private isValidLongitude(value: number | null): value is number {
    return value !== null && Number.isFinite(value) && value >= -180 && value <= 180;
  }
}
