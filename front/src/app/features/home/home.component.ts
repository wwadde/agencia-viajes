import { ChangeDetectionStrategy, Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { DatePipe, NgOptimizedImage } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { SeoService } from '../../core/services/seo.service';
import { Router } from '@angular/router';
import { ImagenUrlPipe } from '../../core/pipes/imagen-url.pipe';
import { ImagenMobileSrcsetPipe } from '../../core/pipes/imagen-mobile-srcset.pipe';
import { HomeService } from '../../core/services/home.service';

@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, NgOptimizedImage, ImagenUrlPipe, ImagenMobileSrcsetPipe, DatePipe],
  template: `
    <div class="theme-landing min-h-screen text-stone-900 flex flex-col">
      <!-- Navbar -->
      <nav class="border-b border-white/30 bg-[var(--color-cloud)]/75 backdrop-blur-xl sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex justify-between items-center h-16">
            <div class="flex items-center gap-3">
              <span class="font-bold text-xl tracking-tight text-[var(--color-ink)] hidden sm:block">Agencia Viajes</span>
            </div>

            <div class="flex items-center gap-4">
              <a routerLink="/foro" class="px-4 py-2 text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors">
                Foro
              </a>
              @if (auth.isAuthenticated()) {
                <span class="text-sm text-[var(--color-muted)] hidden sm:block">
                  Hola, <strong class="text-[var(--color-ink)]">{{ auth.user()?.usuario }}</strong>
                </span>
                @if (auth.roles() === 'ADMIN') {
                  <a routerLink="/admin" class="px-4 py-2 text-sm font-medium text-[var(--color-ink)] hover:text-[var(--color-night)] transition-colors">
                    Admin
                  </a>
                }
                @if (auth.roles() === 'CLIENTE') {
                  <a routerLink="/mis-reservas" class="px-4 py-2 text-sm font-medium text-[var(--color-ink)] hover:text-[var(--color-night)] transition-colors">
                    Mis reservas
                  </a>
                }
                <button (click)="onLogout()" class="px-4 py-2 text-sm font-medium text-white bg-[var(--color-ink)] hover:bg-[var(--color-night)] rounded-lg transition-all border border-transparent shadow-md hover:-translate-y-0.5">
                  Cerrar sesión
                </button>
              } @else {
                <a routerLink="/login" class="px-4 py-2 text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors">
                  Iniciar sesión
                </a>
                <a routerLink="/register" class="px-4 py-2 text-sm font-semibold text-[var(--color-ink)] bg-[var(--color-rose)]/55 hover:bg-[var(--color-rose)]/80 rounded-lg transition-all border border-[var(--color-rose)]/70 hover:-translate-y-0.5">
                  Registrarse
                </a>
              }
            </div>
          </div>
        </div>
      </nav>

      <!-- Hero Section -->
      <header class="relative overflow-hidden min-h-[calc(100svh-4rem)] border-b border-white/30 hero-glow">
        @if (currentLandingSlide(); as slide) {
          <picture>
            <source
              media="(max-width: 767px)"
              [attr.srcset]="currentLandingMobileSrcset()"
              sizes="100vw"
              type="image/webp"
            />
            <img
              [ngSrc]="slide.url | imagenUrl"
              [alt]="slide.nombre"
              fill
              priority
              sizes="100vw"
              class="absolute inset-0 h-full w-full object-cover scale-[1.03] transition-opacity duration-700"
            />
          </picture>
        } @else {
          <div
            aria-hidden="true"
            class="absolute inset-0 bg-[linear-gradient(135deg,#5c7b80_0%,#3a5a61_48%,#1f2a2d_100%)]"
          ></div>
        }
        <div class="absolute inset-0 bg-gradient-to-r from-[var(--color-night)]/78 via-[var(--color-night)]/62 to-[var(--color-ink)]/30"></div>
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_18%_20%,rgba(226,218,221,0.32),transparent_44%),radial-gradient(circle_at_82%_18%,rgba(218,226,223,0.35),transparent_38%)]"></div>

        <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 min-h-[calc(100svh-4rem)] flex items-center">
          <div class="max-w-2xl text-balance animate-fade-up">
            <span class="inline-flex px-3 py-1 mb-6 text-xs font-semibold bg-white/20 text-white rounded-full border border-white/35 backdrop-blur-md tracking-[0.14em] uppercase">
              Agencia boutique de experiencias
            </span>
            <h1 class="text-4xl sm:text-6xl lg:text-7xl font-extrabold text-white tracking-tight mb-6 leading-[1.02]">
              Viaja con estilo,
              <span class="text-[var(--color-sea)]">vive historias</span>
              que se quedan para siempre.
            </h1>
            <p class="text-lg sm:text-xl text-white/85 max-w-xl mb-10 leading-relaxed animate-fade-up-delay">
              Diseñamos rutas curadas para quienes quieren algo más que un destino: quieren un viaje inolvidable, cómodo y auténtico.
            </p>
            <a href="#paquetes" class="inline-flex px-8 py-4 text-base font-semibold text-[var(--color-night)] bg-[var(--color-sea)] hover:bg-[var(--color-sea-strong)] rounded-2xl transition-all shadow-xl hover:shadow-2xl hover:-translate-y-0.5 animate-fade-up-delay-2">
              Descubrir Paquetes
            </a>
          </div>

          <div class="hidden lg:flex ml-auto w-[320px] rounded-[1.8rem] border border-white/25 bg-white/12 backdrop-blur-md p-6 text-white shadow-2xl animate-float-soft">
            <div>
              <p class="text-xs uppercase tracking-[0.2em] text-white/75">Siguiente salida destacada</p>
              <p class="mt-4 text-2xl font-bold leading-tight">Cartagena + Islas del Rosario</p>
              <p class="mt-2 text-sm text-white/80">7 días de mar, ciudad histórica y gastronomía local.</p>
              <a href="#paquetes" class="mt-5 inline-flex text-sm font-semibold text-[var(--color-sea)] hover:text-[var(--color-sea-strong)] transition-colors">Ver itinerario</a>
            </div>
          </div>
        </div>

        @if (homeService.landingSlides().length > 1) {
          <div class="absolute bottom-6 left-1/2 -translate-x-1/2 z-20 flex items-center gap-2 rounded-full bg-black/25 backdrop-blur-sm border border-white/20 px-3 py-2">
            @for (slide of homeService.landingSlides(); track slide.referencia) {
              <button
                type="button"
                [attr.aria-label]="'Ir a imagen ' + ($index + 1)"
                [class.bg-white]="activeLandingSlideIndex() === $index"
                [class.bg-white/45]="activeLandingSlideIndex() !== $index"
                class="h-2.5 w-2.5 rounded-full transition-colors"
                (click)="goToLandingSlide($index)"
              ></button>
            }
          </div>
        }
      </header>

      <!-- Main Content: Paquetes -->
      <main id="paquetes" class="scroll-mt-20 flex-grow max-w-7xl mx-auto w-full py-16 px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between mb-10 animate-fade-up">
          <h2 class="text-2xl sm:text-3xl font-bold text-[var(--color-ink)] tracking-tight leading-none">Paquetes Populares</h2>
        </div>

        @if (homeService.loading()) {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            @for (i of [1,2,3]; track i) {
              <div class="bg-white/60 border border-white/70 rounded-2xl overflow-hidden animate-pulse">
                <div class="h-48 bg-white/70 w-full"></div>
                <div class="p-6">
                  <div class="h-5 bg-white/80 rounded-md w-3/4 mb-4"></div>
                  <div class="h-4 bg-white/80 rounded-md w-full mb-2"></div>
                  <div class="h-4 bg-white/80 rounded-md w-2/3 mb-6"></div>
                  <div class="flex justify-between items-center">
                    <div class="h-6 bg-white/80 rounded-md w-1/3"></div>
                    <div class="h-8 bg-white/80 rounded-md w-1/4"></div>
                  </div>
                </div>
              </div>
            }
          </div>
        } @else if (homeService.error()) {
          <div class="bg-red-100 border border-red-300 rounded-2xl p-8 text-center">
            <svg class="w-12 h-12 text-red-600 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <h3 class="text-lg font-medium text-[var(--color-ink)] mb-2">Error de conexión</h3>
            <p class="text-[var(--color-muted)]">{{ homeService.error() }}</p>
            <button (click)="reloadHomeData()" class="mt-6 px-4 py-2 text-sm font-medium text-white bg-[var(--color-ink)] hover:bg-[var(--color-night)] rounded-lg transition-colors border border-transparent">
              Reintentar
            </button>
          </div>
        } @else if (homeService.paquetes().length === 0) {
          <div class="bg-white/70 border border-white/80 rounded-2xl p-12 text-center text-[var(--color-muted)]">
            No hay paquetes disponibles en este momento.
          </div>
        } @else {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            @for (paquete of homeService.paquetes(); track paquete.id) {
              <article class="group bg-white/65 border border-white/80 hover:border-[var(--color-sea)]/70 rounded-3xl overflow-hidden transition-all duration-300 hover:shadow-2xl hover:shadow-[var(--color-sea)]/20 flex flex-col animate-fade-up">
                <a
                  [routerLink]="['/paquetes', paquete.slug]"
                  [attr.aria-label]="'Ver detalle de ' + paquete.nombre"
                  class="relative h-56 overflow-hidden bg-[var(--color-cloud)] block"
                >
                  @if (paquete.imagenPrincipal) {
                    <picture>
                      <source
                        media="(max-width: 767px)"
                        [attr.srcset]="paquete.imagenPrincipal | imagenMobileSrcset"
                        sizes="100vw"
                        type="image/webp"
                      />
                      <img
                        [ngSrc]="paquete.imagenPrincipal | imagenUrl"
                        [alt]="paquete.nombre"
                        fill
                        [attr.loading]="$index === 0 ? null : 'lazy'"
                        sizes="(max-width: 767px) 100vw, (max-width: 1023px) 50vw, 33vw"
                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      />
                    </picture>
                  } @else {
                    <div class="w-full h-full flex items-center justify-center text-[var(--color-muted)]">
                      <svg class="w-12 h-12 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                  }
                  <div class="absolute top-4 right-4">
                    <span class="px-3 py-1 text-xs font-bold uppercase tracking-wider bg-[var(--color-night)]/60 backdrop-blur-md text-white rounded-full border border-white/20">
                      {{ paquete.duracionDias }} días
                    </span>
                  </div>
                </a>

                <div class="p-6 flex-grow flex flex-col">
                  <div class="flex items-start justify-between mb-2">
                    <h3 class="text-xl font-bold text-[var(--color-ink)] leading-tight">{{ paquete.nombre }}</h3>
                  </div>
                  <div class="flex items-center text-[var(--color-muted)] text-sm mb-4">
                    <svg class="w-4 h-4 mr-1 text-[var(--color-ink)]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    {{ paquete.destino }}
                  </div>

                  <p class="text-[var(--color-muted)] text-sm line-clamp-2 mb-6 flex-grow">
                    {{ paquete.descripcion }}
                  </p>

                  <div class="mt-auto pt-5 border-t border-white/80 flex items-center justify-between">
                    <span class="text-sm text-[var(--color-muted)]">{{ paquete.destino }}</span>
                    <a routerLink="/paquetes/{{ paquete.slug }}" class="px-4 py-2 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white text-sm font-medium rounded-xl transition-all hover:-translate-y-0.5">
                      Ver detalle
                    </a>
                  </div>
                </div>
              </article>
            }
          </div>
        }

        <section class="mt-16">
          <div class="flex items-center justify-between mb-8 animate-fade-up">
            <div>
              <h2 class="text-2xl sm:text-3xl font-bold text-[var(--color-ink)] tracking-tight leading-none">Últimas publicaciones del foro</h2>
              <p class="text-sm text-[var(--color-muted)] mt-2">Inspírate con historias y consejos recién publicados.</p>
            </div>
            <a routerLink="/foro" class="px-4 py-2 rounded-xl border border-[var(--color-sea-strong)]/45 text-sm font-medium text-[var(--color-ink)] hover:bg-white/70 transition-colors">Ver foro</a>
          </div>

          @if (homeService.foroPosts().length === 0) {
            <div class="rounded-2xl border border-white/80 bg-white/70 p-6 text-sm text-[var(--color-muted)]">Aún no hay publicaciones recientes en el foro.</div>
          } @else {
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              @for (post of homeService.foroPosts(); track post.id) {
                <article class="group bg-white/65 border border-white/80 hover:border-[var(--color-sea)]/70 rounded-3xl overflow-hidden transition-all duration-300 hover:shadow-2xl hover:shadow-[var(--color-sea)]/20 flex flex-col animate-fade-up">
                  <a
                    [routerLink]="['/foro', post.slug]"
                    [attr.aria-label]="'Leer publicación ' + post.titulo"
                    class="relative h-52 overflow-hidden bg-[var(--color-cloud)] block"
                  >
                    @if (post.imagenPortada) {
                      <img
                        [src]="post.imagenPortada | imagenUrl"
                        [alt]="post.titulo"
                        loading="lazy"
                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      />
                    } @else {
                      <div class="w-full h-full flex items-center justify-center text-[var(--color-muted-soft)] text-sm">Sin portada</div>
                    }
                  </a>

                  <div class="p-6 flex flex-col flex-grow">
                    <div class="text-xs uppercase tracking-[0.15em] font-semibold text-[var(--color-muted-soft)] mb-3">
                      {{ post.fechaPublicacion | date:'dd MMM yyyy' }} · {{ post.totalComentarios }} comentarios
                    </div>
                    <h3 class="text-xl font-bold text-[var(--color-ink)] leading-tight">{{ post.titulo }}</h3>
                    <p class="text-[var(--color-muted)] text-sm line-clamp-3 mt-3 flex-grow">{{ post.resumen }}</p>
                    <a [routerLink]="['/foro', post.slug]" class="mt-5 inline-flex items-center gap-2 text-sm font-semibold text-[var(--color-ink)] hover:text-[var(--color-night)] transition-colors">
                      Leer publicación
                      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                      </svg>
                    </a>
                  </div>
                </article>
              }
            </div>
          }
        </section>
      </main>

            <!-- Attention Hours -->
      <section class="relative border-b border-white/20 bg-[linear-gradient(120deg,rgba(218,226,223,0.82),rgba(226,218,221,0.9))]">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 sm:py-16">
          <div class="grid grid-cols-1 lg:grid-cols-[1.1fr_1.2fr] gap-8 lg:gap-12 items-start">
            <div class="animate-fade-up">
              <p class="text-xs uppercase tracking-[0.2em] text-[var(--color-muted)] font-semibold">Horarios de atención</p>
              <h2 class="mt-3 text-3xl sm:text-4xl font-bold text-[var(--color-ink)] leading-tight">Estamos listos para planear tu próxima aventura</h2>
              <p class="mt-4 text-[var(--color-muted)] leading-relaxed max-w-lg">
                Te acompañamos desde la elección del destino hasta el regreso. Nuestro equipo responde por WhatsApp y en oficina en estos horarios.
              </p>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <article class="rounded-2xl bg-white/65 border border-white/80 p-5 shadow-sm animate-fade-up-delay">
                <h3 class="text-sm font-semibold uppercase tracking-[0.16em] text-[var(--color-muted)]">Lunes a Viernes</h3>
                <p class="mt-2 text-2xl font-bold text-[var(--color-ink)]">8:00 AM - 6:30 PM</p>
                <p class="mt-1 text-sm text-[var(--color-muted)]">Asesoría completa y cotizaciones.</p>
              </article>
              <article class="rounded-2xl bg-white/65 border border-white/80 p-5 shadow-sm animate-fade-up-delay-2">
                <h3 class="text-sm font-semibold uppercase tracking-[0.16em] text-[var(--color-muted)]">Sábados</h3>
                <p class="mt-2 text-2xl font-bold text-[var(--color-ink)]">9:00 AM - 2:00 PM</p>
                <p class="mt-1 text-sm text-[var(--color-muted)]">Atención para reservas y pagos.</p>
              </article>
              <article class="rounded-2xl bg-white/65 border border-white/80 p-5 shadow-sm sm:col-span-2 animate-fade-up-delay-2">
                <h3 class="text-sm font-semibold uppercase tracking-[0.16em] text-[var(--color-muted)]">Domingos y festivos</h3>
                <p class="mt-2 text-xl font-bold text-[var(--color-ink)]">Canal digital</p>
                <p class="mt-1 text-sm text-[var(--color-muted)]">Respuestas por WhatsApp para urgencias de viaje.</p>
              </article>
            </div>
          </div>
        </div>
      </section>

      <!-- Floating WhatsApp -->
      <a
        href="https://wa.me/573115553225?text=Hola%2C%20quiero%20informaci%C3%B3n%20sobre%20paquetes%20tur%C3%ADsticos"
        target="_blank"
        rel="noopener noreferrer"
        aria-label="Escribir por WhatsApp"
        class="fixed bottom-6 right-6 z-[90] w-14 h-14 rounded-full bg-[#25D366] hover:bg-[#22c35c] text-white shadow-xl hover:shadow-2xl flex items-center justify-center transition-all hover:-translate-y-1 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#25D366]"
      >
        <svg class="w-7 h-7" viewBox="0 0 32 32" fill="currentColor" aria-hidden="true">
          <path d="M19.11 17.56c-.27-.14-1.57-.78-1.82-.87-.24-.09-.42-.14-.6.14-.18.27-.69.87-.85 1.05-.16.18-.32.2-.59.07-.27-.14-1.15-.42-2.19-1.33-.81-.72-1.35-1.61-1.5-1.88-.16-.27-.02-.42.11-.55.12-.12.27-.31.41-.47.14-.16.18-.27.27-.45.09-.18.05-.34-.02-.48-.07-.14-.6-1.44-.82-1.98-.22-.52-.44-.45-.6-.46h-.51c-.18 0-.47.07-.72.34-.24.27-.94.92-.94 2.23 0 1.31.96 2.58 1.09 2.76.14.18 1.89 2.88 4.58 4.04.64.27 1.14.43 1.53.55.64.2 1.22.17 1.68.1.51-.08 1.57-.64 1.79-1.26.22-.62.22-1.15.15-1.26-.07-.11-.24-.18-.51-.32z" />
          <path d="M16 3C8.83 3 3 8.83 3 16c0 2.29.6 4.53 1.74 6.5L3 29l6.69-1.73A12.96 12.96 0 0 0 16 29c7.17 0 13-5.83 13-13S23.17 3 16 3zm0 23.65c-2.04 0-4.03-.56-5.76-1.62l-.41-.24-3.97 1.03 1.06-3.87-.27-.4A10.62 10.62 0 0 1 5.35 16C5.35 10.11 10.11 5.35 16 5.35S26.65 10.11 26.65 16 21.89 26.65 16 26.65z" />
        </svg>
      </a>

      <!-- Footer -->
      <footer class="border-t border-white/30 bg-[var(--color-cloud)] py-12 mt-auto">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-[var(--color-muted)] text-sm">
          <p>&copy; 2026 Agencia Viajes. Todos los derechos reservados.</p>
        </div>
      </footer>
    </div>
  `
})
export default class HomeComponent implements OnInit, OnDestroy {
  auth = inject(AuthService);
  homeService = inject(HomeService);
  seo = inject(SeoService);
  router = inject(Router);

  activeLandingSlideIndex = signal(0);

  currentLandingSlide = computed(() => {
    const slides = this.homeService.landingSlides();
    if (slides.length === 0) {
      return null;
    }

    const index = this.activeLandingSlideIndex() % slides.length;
    return slides[index] ?? null;
  });

  currentLandingMobileSrcset = computed(() => {
    const slide = this.currentLandingSlide();
    if (!slide) {
      return '';
    }

    return `${slide.thumbnailSmallUrl} 480w, ${slide.thumbnailMediumUrl} 960w`;
  });

  private carouselIntervalId: ReturnType<typeof setInterval> | null = null;

  async ngOnInit() {
    this.seo.setHomeDefaults();
    await this.reloadHomeData();
  }

  ngOnDestroy() {
    this.stopCarousel();
  }

  async reloadHomeData() {
    await this.homeService.loadHomeData();
    this.activeLandingSlideIndex.set(0);
    this.startCarousel();
  }

  goToLandingSlide(index: number) {
    this.activeLandingSlideIndex.set(index);
    this.startCarousel();
  }

  private startCarousel() {
    this.stopCarousel();

    if (this.homeService.landingSlides().length < 2) {
      return;
    }

    this.carouselIntervalId = setInterval(() => {
      const totalSlides = this.homeService.landingSlides().length;
      if (totalSlides < 2) {
        return;
      }

      this.activeLandingSlideIndex.update((current) => (current + 1) % totalSlides);
    }, 7000);
  }

  private stopCarousel() {
    if (!this.carouselIntervalId) {
      return;
    }

    clearInterval(this.carouselIntervalId);
    this.carouselIntervalId = null;
  }

  async onLogout() {
    await this.auth.logout();
    this.router.navigateByUrl('/');
  }
}

