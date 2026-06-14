import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TriageBadge } from './triage-badge';

@Component({
  selector: 'app-patient-card',
  standalone: true,
  imports: [CommonModule, TriageBadge],
  template: `
    <div [ngClass]="cardClass" 
         (click)="cardClick.emit()"
         class="relative flex flex-col md:flex-row md:items-center justify-between p-4 glass-card rounded-xl transition-all duration-300 cursor-pointer overflow-hidden">
      
      <!-- Borda esquerda semântica -->
      <div [ngClass]="borderClass" class="absolute left-0 top-0 bottom-0 w-1"></div>

      <!-- Info Paciente -->
      <div class="flex flex-col gap-1 pl-2">
        <div class="flex items-center gap-2">
          <span class="text-xs font-mono text-slate-400">#{{ triageId() }}</span>
          <app-triage-badge [priority]="priority()" size="sm"></app-triage-badge>
        </div>
        
        <h4 class="text-base font-bold text-slate-100 font-sans tracking-wide">
          {{ patientName() }}
        </h4>
        
        @if (queixa()) {
          <p class="text-xs text-slate-400 font-sans line-clamp-1 max-w-md">
            <span class="font-semibold">Queixa:</span> {{ queixa() }}
          </p>
        }
      </div>

      <!-- Meta info: tempo de espera / status -->
      <div class="flex flex-col items-end gap-1 mt-2 md:mt-0 font-mono text-right">
        <div class="text-[10px] text-slate-400 uppercase tracking-widest">
          {{ status() }}
        </div>
        
        @if (status() === 'AGUARDANDO') {
          <div [ngClass]="waitClass" class="text-lg font-bold">
            {{ formatWaitTime() }}
          </div>
        } @else {
          <div class="text-xs text-slate-200">
            {{ formatEntryTime() }}
          </div>
        }
      </div>
    </div>
  `
})
export class PatientCard {
  triageId = input.required<number>();
  patientName = input.required<string>();
  priority = input<string | null | undefined>();
  status = input.required<string>(); // AGUARDANDO, EM_ATENDIMENTO, FINALIZADO
  createdAt = input.required<string>();
  queixa = input<string>('');
  selected = input<boolean>(false);

  cardClick = output<void>();

  get cardClass(): string {
    let classes = 'glass-hover';
    if (this.selected()) {
      classes += ' border-cyber-teal bg-cyber-teal/5 shadow-glow-teal';
    }
    const p = this.priority();
    if (p && p.toUpperCase() === 'VERMELHO') {
      classes += ' border-triage-critical/30';
    }
    return classes;
  }

  get borderClass(): string {
    const p = this.priority();
    if (!p) return 'bg-triage-none';
    switch (p.toUpperCase()) {
      case 'VERMELHO': return 'bg-triage-critical';
      case 'LARANJA': return 'bg-triage-urgent';
      case 'AMARELO': return 'bg-triage-semi';
      case 'VERDE': return 'bg-triage-low';
      default: return 'bg-triage-none';
    }
  }

  get waitClass(): string {
    const min = this.calculateWaitMinutes();
    const p = this.priority();
    if (!p) return 'text-slate-100';

    if (p.toUpperCase() === 'VERMELHO') return 'text-triage-critical animate-pulse-critical';
    if (p.toUpperCase() === 'LARANJA' && min > 10) return 'text-triage-urgent';
    if (p.toUpperCase() === 'AMARELO' && min > 30) return 'text-triage-semi';
    if (p.toUpperCase() === 'VERDE' && min > 60) return 'text-triage-low';
    
    return 'text-slate-200';
  }

  calculateWaitMinutes(): number {
    const created = new Date(this.createdAt());
    const diff = new Date().getTime() - created.getTime();
    return Math.max(0, Math.floor(diff / 60000));
  }

  formatWaitTime(): string {
    const min = this.calculateWaitMinutes();
    if (min < 60) return `${min}m`;
    const hrs = Math.floor(min / 60);
    const remainingMins = min % 60;
    return `${hrs}h ${remainingMins}m`;
  }

  formatEntryTime(): string {
    const date = new Date(this.createdAt());
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }
}
