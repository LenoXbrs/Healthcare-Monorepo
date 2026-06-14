import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex flex-col items-center justify-center min-h-screen bg-gradient-surface p-4 relative">
      <!-- Glow superior sutil -->
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-[500px] h-[300px] bg-cyber-teal/5 rounded-full blur-[120px] pointer-events-none"></div>

      <!-- Container principal -->
      <div class="w-full max-w-[440px] z-10">
        <!-- Logo / Título -->
        <div class="text-center mb-6">
          <div class="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-teal shadow-glow-teal mb-3">
            <!-- Icone Hospitalar -->
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-obsidian" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
              <path stroke-linecap="round" stroke-linejoin="round" d="M19 10.5V20a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-9.5m14 0V9a2 2 0 0 0-2-2h-2m4 3.5L12 3 3 10.5M3 10.5V9a2 2 0 0 1 2-2h2m0 0V5a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v2M7 7h10" />
            </svg>
          </div>
          <h2 class="text-2xl font-extrabold text-slate-100 tracking-wide">STH</h2>
          <p class="text-xs text-slate-400 font-mono tracking-widest uppercase">Sistema de Triagem Hospitalar</p>
        </div>

        <!-- Glass card de login -->
        <div class="glass-elevated p-8 rounded-2xl border border-slate-600/30">
          <h3 class="text-lg font-bold text-slate-100 mb-6 text-center">Acesso Restrito</h3>

          @if (errorMessage()) {
            <div class="p-3 mb-4 rounded-lg bg-triage-critical/10 border border-triage-critical/30 text-triage-critical text-xs flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>{{ errorMessage() }}</span>
            </div>
          }

          <form (ngSubmit)="onSubmit()" class="flex flex-col gap-4">
            <!-- Email -->
            <div class="flex flex-col gap-1.5">
              <label class="text-[10px] font-semibold tracking-wider text-slate-400 uppercase">E-mail Institucional</label>
              <input type="email" name="email" [(ngModel)]="email" required
                     placeholder="usuario@hospital.com"
                     class="form-input text-sm" />
            </div>

            <!-- Senha -->
            <div class="flex flex-col gap-1.5">
              <label class="text-[10px] font-semibold tracking-wider text-slate-400 uppercase">Senha</label>
              <input type="password" name="password" [(ngModel)]="senha" required
                     placeholder="••••••••"
                     class="form-input text-sm" />
            </div>

            <!-- Botão Submit -->
            <button type="submit" [disabled]="loading()"
                    class="btn-primary w-full h-11 rounded-lg text-sm mt-2 flex items-center justify-center gap-2">
              @if (loading()) {
                <span class="w-4 h-4 border-2 border-obsidian border-t-transparent rounded-full animate-spin"></span>
                <span>Autenticando...</span>
              } @else {
                <span>Entrar no Sistema</span>
              }
            </button>
          </form>
        </div>

        <!-- Seletor rápido de perfil (facilitar homologação e testes) -->
        <div class="mt-6">
          <p class="text-[10px] font-mono tracking-wider uppercase text-slate-500 text-center mb-3">Selecione um perfil para testes rápidos</p>
          <div class="grid grid-cols-2 gap-2">
            @for (profile of quickProfiles; track profile.name) {
              <button (click)="selectProfile(profile)"
                      class="flex flex-col items-start p-2.5 bg-navy-deep/40 hover:bg-navy-deep/80 border border-slate-600/10 hover:border-cyber-teal/30 rounded-xl transition-all duration-300 text-left">
                <span class="text-xs font-bold text-slate-200">{{ profile.name }}</span>
                <span class="text-[9px] font-mono text-slate-400 mt-0.5">{{ profile.email }}</span>
              </button>
            }
          </div>
        </div>

        <div class="text-center mt-8 text-[9px] font-mono text-slate-500">
          STH v1.0.0 · Ambiente Clínico Protegido
        </div>
      </div>
    </div>
  `
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  senha = '';
  loading = signal(false);
  errorMessage = signal('');

  quickProfiles = [
    { name: 'Recepcionista', email: 'recepcionista@healthcare.com', pass: 'recepcionista123' },
    { name: 'Enfermeiro(a)', email: 'enfermeiro@healthcare.com', pass: 'enfermeiro123' },
    { name: 'Médico Roberto', email: 'roberto@healthcare.com', pass: 'medico123' },
    { name: 'Administrador', email: 'admin@healthcare.com', pass: 'admin123' }
  ];

  selectProfile(p: typeof this.quickProfiles[0]) {
    this.email = p.email;
    this.senha = p.pass;
    this.errorMessage.set('');
  }

  onSubmit() {
    if (!this.email || !this.senha) {
      this.errorMessage.set('Por favor, preencha todos os campos.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.authService.login(this.email, this.senha).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set('Credenciais incorretas ou servidor indisponível.');
      }
    });
  }
}
