import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-vital-sign',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [ngClass]="{'border-triage-critical/50 shadow-[0_0_12px_rgba(255,45,85,0.25)]': critical()}" 
         class="flex flex-col justify-between p-3 min-w-[100px] min-h-[96px] bg-black/30 border border-slate-600/30 rounded-xl transition-all duration-300">
      
      <!-- Label -->
      <div class="text-[10px] font-mono tracking-widest text-slate-400 uppercase">
        {{ label() }}
      </div>

      <!-- Value and Trend -->
      <div class="flex items-baseline justify-between mt-1">
        <span [ngClass]="critical() ? 'text-triage-critical font-bold animate-pulse-critical' : 'text-slate-100'" 
              class="text-2xl font-mono font-bold leading-none">
          {{ value() }}
        </span>
        @if (trend()) {
          <span [ngClass]="{
            'text-triage-critical': trend() === 'up' && critical(),
            'text-cyber-teal': trend() === 'stable' || (trend() === 'up' && !critical()),
            'text-blue-400': trend() === 'down'
          }" class="text-xs font-bold font-mono ml-1">
            @if (trend() === 'up') { ▲ }
            @else if (trend() === 'down') { ▼ }
            @else { ▬ }
          </span>
        }
      </div>

      <!-- Unit -->
      <div class="text-[10px] text-slate-400/80 mt-1 font-sans">
        {{ unit() }}
      </div>
    </div>
  `
})
export class VitalSign {
  label = input.required<string>();
  value = input.required<string | number>();
  unit = input.required<string>();
  critical = input<boolean>(false);
  trend = input<'up' | 'down' | 'stable' | null>(null);
}
