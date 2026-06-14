import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-gradient-surface text-slate-100 flex flex-col font-sans">
      
      <!-- Top header -->
      <header class="h-16 bg-navy-deep/80 border-b border-slate-600/20 px-6 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <button (click)="goBack()" class="p-1.5 bg-black/10 hover:bg-slate-600/20 border border-slate-600/10 rounded-lg text-slate-400 hover:text-slate-100 transition-all">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <div>
            <h2 class="text-sm font-bold">Relatórios e Analytics</h2>
            <span class="text-[9px] font-mono text-slate-400 uppercase">Indicadores Clínicos de Produtividade</span>
          </div>
        </div>
        <div class="text-xs font-mono text-slate-400 bg-black/10 px-3 py-1 rounded-lg">
          Perfil: {{ currentUser()?.nome }}
        </div>
      </header>

      <div class="flex-1 flex justify-center p-8 overflow-y-auto">
        <div class="w-full max-w-5xl flex flex-col gap-6">
          
          <!-- Filtro de datas -->
          <div class="glass-card p-4 rounded-xl flex flex-wrap items-center justify-between gap-4">
            <div class="flex items-center gap-2">
              <span class="text-xs font-mono text-slate-400">Período:</span>
              <select class="form-input text-xs">
                <option>Últimas 24 Horas</option>
                <option selected>Últimos 7 Dias</option>
                <option>Últimos 30 Dias</option>
              </select>
            </div>
            <button class="btn-primary h-8 px-4 rounded-lg text-xs">
              Exportar CSV
            </button>
          </div>

          <!-- Grid de Gráficos -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            <!-- GRAFICO 1: Atendimentos por hora (Area Chart SVG) -->
            <div class="glass-card p-6 rounded-2xl flex flex-col gap-4">
              <div>
                <h3 class="text-sm font-bold text-slate-100">Atendimentos Diários</h3>
                <p class="text-xs text-slate-400">Gráfico de área mostrando a evolução de atendimentos na semana.</p>
              </div>

              <!-- Desenho do Gráfico com SVG -->
              <div class="bg-black/20 p-4 border border-slate-600/10 rounded-xl flex items-center justify-center">
                <svg viewBox="0 0 500 200" class="w-full h-[180px]">
                  <defs>
                    <linearGradient id="gradientArea" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stop-color="#00D4CC" stop-opacity="0.4" />
                      <stop offset="100%" stop-color="#00D4CC" stop-opacity="0.0" />
                    </linearGradient>
                  </defs>
                  <!-- Linha de fundo horizontal -->
                  <line x1="50" y1="40" x2="450" y2="40" stroke="rgba(255,255,255,0.05)" stroke-width="1" />
                  <line x1="50" y1="90" x2="450" y2="90" stroke="rgba(255,255,255,0.05)" stroke-width="1" />
                  <line x1="50" y1="140" x2="450" y2="140" stroke="rgba(255,255,255,0.05)" stroke-width="1" />
                  <line x1="50" y1="170" x2="450" y2="170" stroke="rgba(255,255,255,0.1)" stroke-width="1.5" />

                  <!-- Área preenchida -->
                  <path d="M 50 170 L 100 130 L 150 150 L 200 90 L 250 100 L 300 60 L 350 80 L 400 45 L 450 170 Z" fill="url(#gradientArea)" />
                  
                  <!-- Linha Principal -->
                  <path d="M 50 170 L 100 130 L 150 150 L 200 90 L 250 100 L 300 60 L 350 80 L 400 45" fill="none" stroke="#00D4CC" stroke-width="3" stroke-linecap="round" />

                  <!-- Pontos da linha -->
                  <circle cx="100" cy="130" r="4" fill="#00F5E4" />
                  <circle cx="150" cy="150" r="4" fill="#00F5E4" />
                  <circle cx="200" cy="90" r="4" fill="#00F5E4" />
                  <circle cx="250" cy="100" r="4" fill="#00F5E4" />
                  <circle cx="300" cy="60" r="4" fill="#00F5E4" />
                  <circle cx="350" cy="80" r="4" fill="#00F5E4" />
                  <circle cx="400" cy="45" r="4" fill="#00F5E4" />

                  <!-- X Labels -->
                  <text x="50" y="190" fill="rgba(255,255,255,0.4)" font-size="9" font-family="monospace" text-anchor="middle">SEG</text>
                  <text x="100" y="190" fill="rgba(255,255,255,0.4)" font-size="9" font-family="monospace" text-anchor="middle">TER</text>
                  <text x="150" y="190" fill="rgba(255,255,255,0.4)" font-size="9" font-family="monospace" text-anchor="middle">QUA</text>
                  <text x="200" y="190" fill="rgba(255,255,255,0.4)" font-size="9" font-family="monospace" text-anchor="middle">QUI</text>
                  <text x="250" y="190" fill="rgba(255,255,255,0.4)" font-size="9" font-family="monospace" text-anchor="middle">SEX</text>
                  <text x="300" y="190" fill="rgba(255,255,255,0.4)" font-size="9" font-family="monospace" text-anchor="middle">SAB</text>
                  <text x="350" y="190" fill="rgba(255,255,255,0.4)" font-size="9" font-family="monospace" text-anchor="middle">DOM</text>
                </svg>
              </div>
            </div>

            <!-- GRAFICO 2: Distribuição por nível (Donut Chart SVG) -->
            <div class="glass-card p-6 rounded-2xl flex flex-col gap-4">
              <div>
                <h3 class="text-sm font-bold text-slate-100">Distribuição Manchester</h3>
                <p class="text-xs text-slate-400">Percentual de pacientes triados por cor de prioridade.</p>
              </div>

              <div class="bg-black/20 p-4 border border-slate-600/10 rounded-xl flex items-center justify-around flex-wrap gap-4">
                <!-- Donut SVG -->
                <svg viewBox="0 0 100 100" class="w-[140px] h-[140px]">
                  <!-- Emergencia Vermelho: 15% (stroke-dasharray="15 85" stroke-dashoffset="25") -->
                  <circle cx="50" cy="50" r="40" fill="none" stroke="#FF2D55" stroke-width="12" stroke-dasharray="12 88" stroke-dashoffset="25" />
                  
                  <!-- Muito Urgente Laranja: 25% (stroke-dasharray="25 75" stroke-dashoffset="13") -->
                  <circle cx="50" cy="50" r="40" fill="none" stroke="#FF6B00" stroke-width="12" stroke-dasharray="20 80" stroke-dashoffset="1" />
                  
                  <!-- Urgente Amarelo: 35% -->
                  <circle cx="50" cy="50" r="40" fill="none" stroke="#FFD600" stroke-width="12" stroke-dasharray="33 67" stroke-dashoffset="81" />
                  
                  <!-- Pouco Urgente Verde: 25% -->
                  <circle cx="50" cy="50" r="40" fill="none" stroke="#00C853" stroke-width="12" stroke-dasharray="25 75" stroke-dashoffset="48" />

                  <!-- Furo do donut -->
                  <circle cx="50" cy="50" r="32" fill="#0D1B3E" />
                  <text x="50" y="55" text-anchor="middle" fill="#C8D4F0" font-size="10" font-weight="700">75 Pts</text>
                </svg>

                <!-- Legenda -->
                <div class="flex flex-col gap-1.5 text-[10px] font-mono">
                  <div class="flex items-center gap-2">
                    <span class="w-3 h-3 bg-triage-critical rounded"></span>
                    <span class="text-slate-300">Emergência: 12%</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="w-3 h-3 bg-triage-urgent rounded"></span>
                    <span class="text-slate-300">Muito Urgente: 20%</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="w-3 h-3 bg-triage-semi rounded"></span>
                    <span class="text-slate-300">Urgente: 33%</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="w-3 h-3 bg-triage-low rounded"></span>
                    <span class="text-slate-300">Pouco Urgente: 25%</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- GRAFICO 3: Tempo Médio de Espera (Horizontal Bar Chart) -->
            <div class="glass-card p-6 rounded-2xl flex flex-col gap-4 md:col-span-2">
              <div>
                <h3 class="text-sm font-bold text-slate-100">Tempo Médio de Espera (Minutos)</h3>
                <p class="text-xs text-slate-400">Tempo de fila decorrido até o atendimento médico finalizado por nível de risco.</p>
              </div>

              <div class="bg-black/20 p-4 border border-slate-600/10 rounded-xl flex flex-col gap-4">
                <!-- Vermelho -->
                <div class="flex flex-col gap-1">
                  <div class="flex justify-between text-[10px] font-mono text-slate-300">
                    <span>EMERGÊNCIA (Meta: 0 min)</span>
                    <span class="text-triage-critical font-bold">Real: 2 min</span>
                  </div>
                  <div class="h-2 w-full bg-slate-600/10 rounded-full overflow-hidden">
                    <div class="h-full bg-triage-critical rounded-full" style="width: 5%"></div>
                  </div>
                </div>

                <!-- Laranja -->
                <div class="flex flex-col gap-1">
                  <div class="flex justify-between text-[10px] font-mono text-slate-300">
                    <span>MUITO URGENTE (Meta: 10 min)</span>
                    <span class="text-triage-urgent font-bold">Real: 8 min</span>
                  </div>
                  <div class="h-2 w-full bg-slate-600/10 rounded-full overflow-hidden">
                    <div class="h-full bg-triage-urgent rounded-full" style="width: 40%"></div>
                  </div>
                </div>

                <!-- Amarelo -->
                <div class="flex flex-col gap-1">
                  <div class="flex justify-between text-[10px] font-mono text-slate-300">
                    <span>URGENTE (Meta: 30 min)</span>
                    <span class="text-triage-semi font-bold">Real: 24 min</span>
                  </div>
                  <div class="h-2 w-full bg-slate-600/10 rounded-full overflow-hidden">
                    <div class="h-full bg-triage-semi rounded-full" style="width: 80%"></div>
                  </div>
                </div>

                <!-- Verde -->
                <div class="flex flex-col gap-1">
                  <div class="flex justify-between text-[10px] font-mono text-slate-300">
                    <span>POUCO URGENTE (Meta: 60 min)</span>
                    <span class="text-triage-low font-bold">Real: 42 min</span>
                  </div>
                  <div class="h-2 w-full bg-slate-600/10 rounded-full overflow-hidden">
                    <div class="h-full bg-triage-low rounded-full" style="width: 70%"></div>
                  </div>
                </div>
              </div>
            </div>

          </div>

        </div>
      </div>
    </div>
  `
})
export class Reports {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}
