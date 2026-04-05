import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="theme-shell flex h-screen text-[var(--color-ink)] overflow-hidden font-sans">
      @if (isSidebarOpen()) {
        <button
          type="button"
          class="fixed inset-0 z-40 bg-black/45 backdrop-blur-[1px] lg:hidden"
          (click)="closeSidebar()"
          aria-label="Cerrar menú lateral">
        </button>
      }
      
      <!-- Sidebar -->
      <aside
        id="admin-sidebar"
        class="fixed inset-y-0 left-0 z-50 w-72 flex-shrink-0 border-r border-white/60 bg-[var(--color-cloud)]/80 flex flex-col backdrop-blur-sm transition-transform duration-300 lg:static lg:z-auto lg:translate-x-0"
        [class.translate-x-0]="isSidebarOpen()"
        [class.-translate-x-full]="!isSidebarOpen()">
        <!-- Brand -->
        <div class="h-16 flex items-center px-6 border-b border-white/70">
          <div class="flex items-center gap-3 flex-1 min-w-0">
            <div class="w-8 h-8 rounded-lg bg-[var(--color-ink)] flex items-center justify-center shadow-lg">
              <span class="text-[var(--color-night)] font-bold text-lg leading-none">A</span>
            </div>
            <span class="font-bold text-xl tracking-tight text-[var(--color-ink)]">Agencia<span class="text-[var(--color-muted)]">Admin</span></span>
          </div>
          <button
            type="button"
            class="lg:hidden p-2 rounded-lg text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors"
            (click)="closeSidebar()"
            aria-label="Cerrar menú lateral">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Navigation -->
        <nav class="flex-1 overflow-y-auto py-6 px-4 space-y-1">
          <p class="px-2 text-xs font-semibold text-[var(--color-muted)] uppercase tracking-wider mb-3">General</p>
          
           <a routerLink="/admin" (click)="closeSidebar()" routerLinkActive="bg-[var(--color-sea)] text-[var(--color-ink)] border-[var(--color-sea-strong)]" [routerLinkActiveOptions]="{exact: true}"
             class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors border border-transparent">
            <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
            </svg>
            Dashboard
          </a>

          <div class="pt-4 pb-2">
            <p class="px-2 text-xs font-semibold text-[var(--color-muted)] uppercase tracking-wider mb-2">Contenido</p>
          </div>

           <a routerLink="/admin/destinos" (click)="closeSidebar()" routerLinkActive="bg-[var(--color-sea)] text-[var(--color-ink)] border-[var(--color-sea-strong)]"
             class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors border border-transparent">
            <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Destinos
          </a>

           <a routerLink="/admin/paquetes" (click)="closeSidebar()" routerLinkActive="bg-[var(--color-sea)] text-[var(--color-ink)] border-[var(--color-sea-strong)]"
             class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors border border-transparent">
            <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Paquetes Turísticos
          </a>

           <a routerLink="/admin/imagenes" (click)="closeSidebar()" routerLinkActive="bg-[var(--color-sea)] text-[var(--color-ink)] border-[var(--color-sea-strong)]"
             class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors border border-transparent">
            <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            Biblioteca de Imágenes
          </a>

          <div class="pt-4 pb-2">
            <p class="px-2 text-xs font-semibold text-[var(--color-muted)] uppercase tracking-wider mb-2">Operaciones</p>
          </div>

           <a routerLink="/admin/agendas" (click)="closeSidebar()" routerLinkActive="bg-[var(--color-sea)] text-[var(--color-ink)] border-[var(--color-sea-strong)]"
             class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors border border-transparent">
            <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            Agendas & Salidas
          </a>

           <a routerLink="/admin/reservas" (click)="closeSidebar()" routerLinkActive="bg-[var(--color-sea)] text-[var(--color-ink)] border-[var(--color-sea-strong)]"
             class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors border border-transparent">
            <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
            Reservas
          </a>

           <a routerLink="/admin/contenidos" (click)="closeSidebar()" routerLinkActive="bg-[var(--color-sea)] text-[var(--color-ink)] border-[var(--color-sea-strong)]"
             class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-white/70 transition-colors border border-transparent">
             <svg class="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            Contenidos
          </a>
        </nav>

        <!-- User bottom -->
        <div class="p-4 border-t border-white/70">
          <div class="flex items-center gap-3 p-3 rounded-xl bg-white/80">
            <div class="h-10 w-10 rounded-lg bg-[var(--color-sea)] flex items-center justify-center flex-shrink-0">
               <span class="text-[var(--color-ink)] font-medium">AD</span>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-[var(--color-ink)] truncate">{{ auth.user()?.usuario || 'Admin' }}</p>
              <p class="text-xs text-[var(--color-muted)] truncate">{{ auth.roles() }}</p>
            </div>
            <button (click)="logout()" class="p-2 text-[var(--color-muted)] hover:text-[var(--color-ink)] rounded-lg hover:bg-[var(--color-sea)] transition-colors" title="Cerrar sesión">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </div>
      </aside>

      <!-- Main Content Container -->
      <main class="flex-1 flex flex-col min-w-0 relative">
        
        <!-- Header -->
        <header class="h-16 flex items-center gap-3 px-4 sm:px-6 lg:px-8 border-b border-white/60 bg-[var(--color-cloud)]/55 backdrop-blur-xl sticky top-0 z-30">
          <button
            type="button"
            class="lg:hidden inline-flex items-center justify-center p-2 rounded-lg border border-white/60 bg-white/70 text-[var(--color-ink)] hover:bg-white transition-colors"
            (click)="openSidebar()"
            [attr.aria-expanded]="isSidebarOpen()"
            aria-controls="admin-sidebar"
            aria-label="Abrir menú lateral">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <div class="flex-1">
            <a routerLink="/" class="text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              Volver al sitio público
            </a>
          </div>
        </header>

        <!-- Scrollable Content -->
        <div class="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <div class="max-w-7xl mx-auto">
            <router-outlet></router-outlet>
          </div>
        </div>
      </main>

    </div>
  `,
  host: {
    'class': 'block h-full'
  }
})
export default class AdminLayoutComponent {
  auth = inject(AuthService);
  router = inject(Router);
  isSidebarOpen = signal(false);

  openSidebar() {
    this.isSidebarOpen.set(true);
  }

  closeSidebar() {
    this.isSidebarOpen.set(false);
  }

  logout() {
    this.auth.logout().then(() => {
      this.closeSidebar();
      this.router.navigate(['/login']);
    });
  }
}

