import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [RouterLink],
    template: `
  <div class="theme-shell min-h-screen text-[var(--color-ink)]">
    <!-- Navbar Superior Transparent -->
  <nav class="absolute top-0 w-full z-50 bg-[var(--color-cloud)]/70 backdrop-blur-xl border-b border-white/30">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-20">
          <div class="flex items-center gap-3">
            <div class="text-[var(--color-ink)]">
               <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                 <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
               </svg>
            </div>
            <span class="font-black text-2xl tracking-tight text-[var(--color-ink)] uppercase">Ventura Viajes</span>
          </div>
          
          <div class="flex items-center gap-4">
            <div class="hidden sm:flex flex-col items-end mr-2">
              <span class="text-sm font-semibold text-[var(--color-ink)] leading-tight">
                {{ auth.user()?.usuario }}
              </span>
              <span class="text-[10px] uppercase font-bold tracking-widest text-[var(--color-muted)]">
                Rol: {{ auth.roles() }}
              </span>
            </div>
            <button 
              (click)="onLogout()"
              [disabled]="auth.loading()"
              class="group relative inline-flex items-center justify-center p-0.5 mb-2 mr-2 overflow-hidden text-sm font-medium text-white rounded-xl bg-[var(--color-ink)] hover:bg-[var(--color-night)] focus:ring-4 focus:outline-none focus:ring-[var(--color-sea)]"
            >
              <span class="relative px-5 py-2.5 transition-all ease-in duration-75 bg-transparent rounded-[10px] group-hover:bg-opacity-0">
                Cerrar Sesión
              </span>
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Hero Header / Welcome -->
    <div class="relative pt-32 pb-20 lg:pt-48 lg:pb-32 overflow-hidden">
       <div class="absolute inset-0 z-0">
          <div class="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1436491865332-7a61a109cc05?ixlib=rb-4.0.3&auto=format&fit=crop&w=2000&q=80')] bg-cover bg-center opacity-20 mix-blend-screen mix-blend-luminosity grayscale"></div>
          <div class="absolute inset-0 bg-gradient-to-b from-[var(--color-night)]/20 via-[var(--color-night)]/65 to-[var(--color-night)]"></div>
          <div class="absolute inset-0 bg-gradient-to-r from-[var(--color-sea)]/20 to-[var(--color-rose)]/20"></div>
       </div>

       <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center mt-10">
          <span class="inline-block py-1 px-3 rounded-full bg-white/20 text-white border border-white/30 text-xs font-bold uppercase tracking-widest mb-6 backdrop-blur-md">
            {{ auth.roles() === 'ADMIN' ? 'Centro de Operaciones' : 'Portal del Aventurero' }}
          </span>
          <h1 class="text-5xl lg:text-7xl font-bold font-sans text-white tracking-tight mb-6" style="text-wrap: balance">
             Bienvenido de nuevo, <span class="bg-gradient-to-r from-[var(--color-sea)] to-[var(--color-rose)] text-transparent bg-clip-text">{{ auth.user()?.usuario }}</span>
          </h1>
          <p class="text-lg lg:text-xl text-white/85 max-w-2xl mx-auto leading-relaxed">
             {{ auth.roles() === 'ADMIN' ? 'Controla todo el inventario, revisa agendas próximas y gestiona de manera rápida las reservas de los clientes.' : 'La aventura aguarda. Continúa inspeccionando tus futuras reservas o planea hoy tu próxima escapada de mundo.' }}
          </p>
       </div>
    </div>

    <!-- Main Navigation/Dashboard Options -->
    <main class="relative z-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-16 pb-24">
       <!-- Grid Cards -->
       <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">

          <!-- User Info Widget -->
          <div class="bg-white/70 backdrop-blur-2xl p-8 rounded-[2rem] border border-white/80 shadow-2xl flex flex-col justify-between group hover:border-[var(--color-sea)] transition-colors">
            <div>
              <div class="w-12 h-12 bg-[var(--color-sea)]/70 rounded-2xl flex items-center justify-center mb-6 text-[var(--color-ink)] transition-colors">
                 <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
              </div>
              <h3 class="text-2xl font-bold text-[var(--color-ink)] mb-2">Tu Identidad</h3>
              <div class="space-y-4 mt-6">
                <div>
                  <p class="text-xs font-bold text-[var(--color-muted-soft)] uppercase tracking-widest">ID Cuenta</p>
                  <p class="text-lg text-[var(--color-ink)] font-mono mt-0.5">#{{ auth.user()?.id }}</p>
                </div>
                <div>
                  <p class="text-xs font-bold text-[var(--color-muted-soft)] uppercase tracking-widest">Correo Electrónico</p>
                  <p class="text-lg text-[var(--color-muted)] mt-0.5 truncate">{{ auth.user()?.correo }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- ADMIN WIDGETS -->
          @if (auth.roles() === 'ADMIN') {
             <div class="col-span-1 md:col-span-1 lg:col-span-2 relative overflow-hidden bg-gradient-to-br from-[var(--color-night)] to-[var(--color-ink)] p-8 rounded-[2rem] border border-[var(--color-sea-strong)]/40 shadow-[0_0_50px_-12px_rgba(82,100,104,0.3)] flex flex-col justify-center">
                
                <div class="absolute -right-20 -bottom-20 opacity-30 pointer-events-none rotate-12">
                   <svg width="300" height="300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="text-[var(--color-sea-strong)]"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>
                </div>

                <div class="relative z-10 h-full flex flex-col justify-center max-w-md">
                  <span class="inline-flex py-1 px-3 text-xs font-bold bg-[var(--color-sea)] text-[var(--color-ink)] rounded-full uppercase tracking-wider self-start mb-6 shadow-md shadow-[var(--color-sea)]/30">Área Restringida</span>
                  <h3 class="text-3xl font-extrabold text-white mb-4 leading-tight">Administración Global del Sistema</h3>
                  <p class="text-white/80 mb-8 font-medium">Gestiona desde una interfaz poderosa la vitrina de destinos, sube imágenes promocionales y supervisa los pagos.</p>
                  
                  <a routerLink="/admin" class="group flex items-center gap-3 w-fit bg-white text-[var(--color-ink)] px-8 py-4 rounded-xl font-bold text-lg hover:bg-[var(--color-cloud)] transition-all hover:scale-105 hover:shadow-2xl hover:shadow-[var(--color-sea-strong)]/30">
                     Acceder al Superpanel
                     <svg class="w-5 h-5 group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
                  </a>
                </div>
             </div>
          } 
          
          <!-- CLIENT WIDGETS -->
          @else {
             <div class="relative overflow-hidden bg-gradient-to-br from-[var(--color-night)] to-[var(--color-ink)] p-8 rounded-[2rem] border border-[var(--color-sea-strong)]/30 shadow-[0_0_40px_-15px_rgba(82,100,104,0.3)] flex flex-col justify-between group hover:border-[var(--color-sea-strong)]/55 transition-colors">
               <div class="relative z-10">
                 <div class="w-12 h-12 bg-[var(--color-sea)]/25 rounded-2xl flex items-center justify-center mb-6 text-[var(--color-sea)]">
                    <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
                 </div>
                 <h3 class="text-2xl font-bold text-white mb-3">Mis Reservaciones</h3>
                 <p class="text-white/80 text-sm">Realiza tus pagos, descarga tus tíquetes y revisa qué te depara el futuro este año.</p>
               </div>
               
               <a routerLink="/mis-reservas" class="mt-8 flex items-center justify-between bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white p-4 rounded-xl font-medium transition-colors shadow-lg relative z-10">
                 Ver mi portaequipaje
                 <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" /></svg>
               </a>
             </div>

             <div class="relative overflow-hidden bg-gradient-to-br from-[var(--color-ink)] to-[var(--color-night)] p-8 rounded-[2rem] border border-[var(--color-rose)]/35 shadow-xl flex flex-col justify-between group hover:border-[var(--color-rose)]/55 transition-colors">
               <div class="absolute -right-10 -bottom-10 opacity-10">
                 <svg class="w-64 h-64 text-[var(--color-rose)]" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
               </div>
               
               <div class="relative z-10">
                 <div class="w-12 h-12 bg-[var(--color-rose)]/25 rounded-2xl flex items-center justify-center mb-6 text-[var(--color-rose)]">
                    <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" /></svg>
                 </div>
                 <h3 class="text-2xl font-bold text-white mb-3">Inspírate</h3>
                 <p class="text-white/80 text-sm">Regresa a la pantalla principal y descubre destinos mágicos con los mejores precios garantizados.</p>
               </div>

               <a routerLink="/" class="mt-8 flex items-center justify-between border-2 border-[var(--color-sea)]/70 hover:bg-[var(--color-sea)] hover:border-[var(--color-sea-strong)] text-white p-4 rounded-xl font-medium transition-colors relative z-10 bg-[var(--color-ink)]">
                 Explorar Catálogo
                 <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
               </a>
             </div>
          }
       </div>
    </main>
    </div>
  `
})
export default class DashboardComponent {
    auth = inject(AuthService);
    private router = inject(Router);

    async onLogout() {
        await this.auth.logout();
        this.router.navigate(['/']);
    }
}

