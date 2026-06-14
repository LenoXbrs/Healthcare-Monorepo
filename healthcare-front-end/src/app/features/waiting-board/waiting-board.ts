import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TriageService, TriageItem } from '../../core/services/triage.service';
import { TriageBadge } from '../../shared/components/triage-badge';
import { Router } from '@angular/router';

@Component({
  selector: 'app-waiting-board',
  standalone: true,
  imports: [CommonModule, TriageBadge],
  template: `
    <div class="min-h-screen bg-obsidian text-slate-100 flex flex-col font-sans select-none overflow-hidden p-6 relative">
      <!-- Glow ambient background -->
      <div class="absolute -top-[200px] -left-[200px] w-[600px] h-[600px] bg-cyber-teal/5 rounded-full blur-[150px] pointer-events-none"></div>
      <div class="absolute -bottom-[200px] -right-[200px] w-[600px] h-[600px] bg-triage-critical/5 rounded-full blur-[150px] pointer-events-none"></div>

      <!-- Topbar do Painel -->
      <header class="h-16 flex items-center justify-between border-b border-slate-600/20 pb-4 mb-6 z-10">
        <div class="flex items-center gap-3">
          <button (click)="goBack()" class="p-1.5 bg-black/20 hover:bg-slate-600/20 border border-slate-600/10 rounded-lg text-slate-400 hover:text-slate-100 transition-all">
            Voltar
          </button>
          <div>
            <h1 class="text-base font-extrabold tracking-wider text-slate-200">PAINEL DE ATENDIMENTO</h1>
            <p class="text-[9px] font-mono text-slate-400 uppercase tracking-widest">Hospital de Triagem e Emergência</p>
          </div>
        </div>
        
        <div class="text-2xl font-mono font-bold text-cyber-teal bg-black/40 border border-slate-600/15 px-5 py-1.5 rounded-xl">
          {{ currentTime() }}
        </div>
      </header>

      <!-- Grid Principal: Chamados vs Fila Completa -->
      <div class="flex-1 grid grid-cols-1 lg:grid-cols-12 gap-8 z-10 overflow-hidden">
        
        <!-- COLUNA ESQUERDA: Últimas senhas chamadas (Grande Destaque) -->
        <div class="lg:col-span-5 flex flex-col gap-6 overflow-hidden">
          <h2 class="text-sm font-mono text-slate-400 uppercase tracking-widest">Últimas Chamadas</h2>
          
          <div class="flex-1 flex flex-col gap-4">
            <!-- Último chamado (Gigante) -->
            <div class="p-6 bg-navy-deep/80 border-2 border-cyber-teal rounded-2xl shadow-glow-teal flex flex-col items-center justify-center text-center flex-1 min-h-[200px]">
              <span class="text-xs font-mono text-cyber-teal uppercase tracking-widest">PACIENTE CHAMADO</span>
              <h3 class="text-4xl font-extrabold text-slate-100 mt-2 font-sans tracking-wide">
                {{ lastCalled()?.nome || 'Aguardando Chamada...' }}
              </h3>
              <div class="mt-4 px-6 py-2 bg-cyber-teal text-obsidian font-mono font-extrabold text-lg rounded-xl">
                {{ lastCalled()?.local || 'SALA DE ATENDIMENTO' }}
              </div>
            </div>

            <!-- Chamadas Anteriores (Histórico) -->
            <div class="flex flex-col gap-2">
              @for (c of prevCalls; track c.id) {
                <div class="p-3 bg-navy-deep/40 border border-slate-600/10 rounded-xl flex items-center justify-between">
                  <div>
                    <h4 class="text-sm font-bold text-slate-200">{{ c.nome }}</h4>
                    <span class="text-[10px] font-mono text-slate-400">Classificação: {{ c.prioridade }}</span>
                  </div>
                  <div class="px-3 py-1 bg-black/30 border border-slate-600/15 font-mono text-xs font-bold text-slate-300 rounded-lg">
                    {{ c.local }}
                  </div>
                </div>
              }
            </div>
          </div>
        </div>

        <!-- COLUNA DIREITA: Pacientes em Espera (Sem dados sensíveis) -->
        <div class="lg:col-span-7 flex flex-col gap-4 overflow-hidden">
          <h2 class="text-sm font-mono text-slate-400 uppercase tracking-widest">Fila de Espera por Risco</h2>
          
          <div class="flex-1 overflow-y-auto flex flex-col gap-3 pr-2">
            @if (waitingQueue().length === 0) {
              <div class="h-full flex items-center justify-center text-slate-500 font-mono italic text-sm border border-slate-600/10 rounded-2xl bg-navy-deep/20">
                Fila de espera vazia.
              </div>
            } @else {
              @for (item of waitingQueue(); track item.id) {
                <div class="relative p-3.5 bg-navy-deep/60 border border-slate-600/10 rounded-xl flex items-center justify-between overflow-hidden">
                  <!-- Borda esquerda semântica -->
                  <div [ngClass]="{
                    'bg-triage-critical': item.prioridade === 'VERMELHO',
                    'bg-triage-urgent': item.prioridade === 'LARANJA',
                    'bg-triage-semi': item.prioridade === 'AMARELO',
                    'bg-triage-low': item.prioridade === 'VERDE'
                  }" class="absolute left-0 top-0 bottom-0 w-1"></div>

                  <div class="flex items-center gap-4 pl-2">
                    <!-- Nome anonimizado (proteção a dados sensíveis) -->
                    <span class="text-sm font-bold text-slate-200 font-mono">
                      {{ getInitials(item.pacienteId) }}
                    </span>
                    <app-triage-badge [priority]="item.prioridade" size="sm"></app-triage-badge>
                  </div>

                  <div class="text-right font-mono text-xs text-slate-400">
                    Entrada: {{ formatTime(item.createdAt) }}
                  </div>
                </div>
              }
            }
          </div>
        </div>

      </div>

      <!-- Rodapé Institucional -->
      <footer class="h-10 border-t border-slate-600/20 mt-6 pt-4 flex items-center justify-between text-[10px] font-mono text-slate-500 z-10">
        <div>Painel Informativo · Atualizações em Tempo Real</div>
        <div>Apenas as iniciais dos nomes são expostas para privacidade dos pacientes</div>
      </footer>
    </div>
  `
})
export class WaitingBoard implements OnInit, OnDestroy {
  private readonly triageService = inject(TriageService);
  private readonly router = inject(Router);

  currentTime = signal('');
  waitingQueue = signal<TriageItem[]>([]);
  patientsCache = signal<Record<number, string>>({}); // patientId -> initials

  // Real calling state
  prevCalls: Array<{ id: number; nome: string; local: string; prioridade: string }> = [];
  lastCalled = signal<{ nome: string; local: string; prioridade: string } | null>(null);

  private clockInterval: any;
  private pollingInterval: any;
  private lastAnnouncedId: number | null = null;

  ngOnInit() {
    this.updateClock();
    this.clockInterval = setInterval(() => this.updateClock(), 1000);

    this.loadQueue();
    this.pollingInterval = setInterval(() => this.loadQueue(), 10000); // 10s updates
  }

  ngOnDestroy() {
    if (this.clockInterval) clearInterval(this.clockInterval);
    if (this.pollingInterval) clearInterval(this.pollingInterval);
  }

  updateClock() {
    const now = new Date();
    this.currentTime.set(now.toLocaleTimeString('pt-BR'));
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  loadQueue() {
    this.triageService.listarTriagens().subscribe(data => {
      // 1. Fila de Espera por Risco:
      // Pacientes aguardando triagem (status AGUARDANDO) ou em atendimento (classificados) mas ainda não chamados
      const list = data.filter(t => t.status === 'AGUARDANDO' || (t.status === 'EM_ATENDIMENTO' && !t.chamado));
      
      const order: Record<string, number> = {
        'VERMELHO': 1,
        'LARANJA': 2,
        'AMARELO': 3,
        'VERDE': 4
      };

      const sortedList = [...list].sort((a, b) => {
        const aVal = order[a.prioridade || ''] || 5;
        const bVal = order[b.prioridade || ''] || 5;
        if (aVal !== bVal) return aVal - bVal;
        return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      });

      this.waitingQueue.set(sortedList);

      // 2. Últimas Chamadas:
      // Filtrar apenas chamadas ativas com chamado = true
      const calledList = data
        .filter(t => t.chamado && t.chamadoEm)
        .sort((a, b) => new Date(b.chamadoEm!).getTime() - new Date(a.chamadoEm!).getTime());

      // Cache patient names and compute initials
      const allActive = [...sortedList, ...calledList];
      allActive.forEach(t => {
        if (!this.patientsCache()[t.pacienteId]) {
          this.triageService.buscarPacientePorId(t.pacienteId).subscribe(p => {
            const initials = this.computeInitials(p.nome);
            this.patientsCache.update(c => ({ ...c, [t.pacienteId]: initials }));
          });
        }
      });

      // Update call signals and lists
      if (calledList.length > 0) {
        const latest = calledList[0];
        
        // Trigger announcement if it is a new call
        if (latest.id !== this.lastAnnouncedId) {
          this.lastAnnouncedId = latest.id;
          this.playChimeAndSpeak(latest.pacienteId, latest.salaChamada || 'Consultório');
        }

        this.lastCalled.set({
          nome: this.getInitials(latest.pacienteId),
          local: latest.salaChamada || 'SALA DE ATENDIMENTO',
          prioridade: latest.prioridade || 'VERDE'
        });

        this.prevCalls = calledList.slice(1, 4).map(t => ({
          id: t.id,
          nome: this.getInitials(t.pacienteId),
          local: t.salaChamada || 'SALA DE ATENDIMENTO',
          prioridade: t.prioridade || 'VERDE'
        }));
      } else {
        this.lastCalled.set(null);
        this.prevCalls = [];
      }
    });
  }

  playChimeAndSpeak(pacienteId: number, sala: string) {
    // 1. Play chime using AudioContext
    try {
      const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const playTone = (freq: number, start: number, duration: number) => {
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(freq, start);
        gain.gain.setValueAtTime(0.1, start);
        gain.gain.exponentialRampToValueAtTime(0.0001, start + duration);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start(start);
        osc.stop(start + duration);
      };
      
      const now = audioCtx.currentTime;
      playTone(523.25, now, 0.2); // C5
      playTone(659.25, now + 0.15, 0.2); // E5
      playTone(783.99, now + 0.3, 0.4); // G5
    } catch (e) {
      console.warn('Could not play chime:', e);
    }

    // 2. Speak announcement
    setTimeout(() => {
      try {
        this.triageService.buscarPacientePorId(pacienteId).subscribe(p => {
          const initials = this.computeInitials(p.nome);
          const speakText = `Atenção. Paciente ${initials.replace(/\./g, '')}, favor dirigir-se ao ${sala}`;
          const utterance = new SpeechSynthesisUtterance(speakText);
          utterance.lang = 'pt-BR';
          utterance.rate = 0.95;
          window.speechSynthesis.speak(utterance);
        });
      } catch (e) {
        console.warn('Speech synthesis error:', e);
      }
    }, 800);
  }

  computeInitials(name: string): string {
    if (!name) return 'P. C.';
    const parts = name.trim().toUpperCase().split(/\s+/);
    if (parts.length === 1) return parts[0];
    return parts.map(p => p[0] + '.').join(' ');
  }

  getInitials(pacienteId: number): string {
    return this.patientsCache()[pacienteId] || 'P. C.';
  }

  formatTime(timeStr: string): string {
    const d = new Date(timeStr);
    return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }
}
