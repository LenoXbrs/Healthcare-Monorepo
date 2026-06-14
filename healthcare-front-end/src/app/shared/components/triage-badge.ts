import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-triage-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span [ngClass]="badgeClass" class="inline-flex items-center justify-center font-semibold rounded-full select-none text-center transition-all duration-300">
      {{ label }}
    </span>
  `
})
export class TriageBadge {
  priority = input<string | null | undefined>();
  size = input<'sm' | 'md' | 'lg'>('md');

  get label(): string {
    const p = this.priority();
    if (!p) return 'AGUARDANDO';
    switch (p.toUpperCase()) {
      case 'VERMELHO': return 'EMERGÊNCIA';
      case 'LARANJA': return 'MUITO URGENTE';
      case 'AMARELO': return 'URGENTE';
      case 'VERDE': return 'POUCO URGENTE';
      default: return p;
    }
  }

  get badgeClass(): string {
    const p = this.priority();
    const s = this.size();
    let sizeStyles = '';
    if (s === 'sm') sizeStyles = 'h-[20px] px-2 text-[10px]';
    else if (s === 'lg') sizeStyles = 'h-[40px] px-5 text-[14px]';
    else sizeStyles = 'h-[28px] px-3 text-[12px]';

    if (!p) {
      return `${sizeStyles} bg-triage-none/15 text-triage-none border border-triage-none/30 animate-glow-pulse`;
    }

    switch (p.toUpperCase()) {
      case 'VERMELHO':
        return `${sizeStyles} bg-triage-critical/15 text-triage-critical border border-triage-critical/40 animate-pulse-critical shadow-glow-critical`;
      case 'LARANJA':
        return `${sizeStyles} bg-triage-urgent/15 text-triage-urgent border border-triage-urgent/40 shadow-glow-urgent`;
      case 'AMARELO':
        return `${sizeStyles} bg-triage-semi/15 text-triage-semi border border-triage-semi/40`;
      case 'VERDE':
        return `${sizeStyles} bg-triage-low/15 text-triage-low border border-triage-low/40`;
      default:
        return `${sizeStyles} bg-triage-none/15 text-triage-none border border-triage-none/40`;
    }
  }
}
