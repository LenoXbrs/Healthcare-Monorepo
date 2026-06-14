import { Component, inject, signal, OnInit, OnDestroy, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { TriageService, TriageItem, TriageDetail, Paciente } from '../../core/services/triage.service';
import { PatientCard } from '../../shared/components/patient-card';
import { TriageBadge } from '../../shared/components/triage-badge';
import { VitalSign } from '../../shared/components/vital-sign';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, PatientCard, TriageBadge, VitalSign],
  template: `
    <div class="min-h-screen bg-gradient-surface text-slate-100 flex flex-col font-sans">
      
      <!-- TOPBAR (Zona A) -->
      <header class="h-16 fixed top-0 left-0 right-0 z-[100] bg-navy-deep/80 backdrop-blur-md border-b border-slate-600/20 px-6 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-gradient-teal flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-obsidian" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
              <path stroke-linecap="round" stroke-linejoin="round" d="M19 10.5V20a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-9.5m14 0V9a2 2 0 0 0-2-2h-2m4 3.5L12 3 3 10.5M3 10.5V9a2 2 0 0 1 2-2h2m0 0V5a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v2M7 7h10" />
            </svg>
          </div>
          <div>
            <h1 class="text-sm font-bold tracking-wide">STH</h1>
            <span class="text-[9px] font-mono text-slate-400 uppercase tracking-widest">Triagem Hospitalar</span>
          </div>
        </div>

        <!-- Clock & Info -->
        <div class="flex items-center gap-6">
          <div class="text-sm font-mono text-cyber-teal font-semibold bg-black/20 px-3 py-1.5 rounded-lg border border-slate-600/10">
            {{ currentTime() }}
          </div>
          <div class="flex items-center gap-3">
            <div class="text-right hidden sm:block">
              <div class="text-xs font-bold text-slate-200">{{ currentUser()?.nome }}</div>
              <div class="text-[9px] font-mono text-slate-400 uppercase tracking-widest">{{ currentUser()?.role }}</div>
            </div>
            <button (click)="logout()" class="p-1.5 bg-black/20 hover:bg-triage-critical/10 hover:text-triage-critical border border-slate-600/10 hover:border-triage-critical/30 rounded-lg transition-all duration-300">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </div>
      </header>

      <div class="flex flex-1 pt-16">
        
        <!-- SIDEBAR (Zona B) -->
        <aside class="w-16 hover:w-56 bg-gradient-sidebar border-r border-slate-600/20 fixed top-16 bottom-0 left-0 z-50 transition-all duration-300 overflow-hidden flex flex-col group justify-between py-4">
          <nav class="flex flex-col gap-2 px-3">
            <!-- Dashboard Link -->
            <a routerLink="/dashboard" routerLinkActive="bg-cyber-teal/10 text-cyber-teal border border-cyber-teal/30"
               class="flex items-center gap-4 p-2.5 rounded-xl transition-all duration-200 text-slate-400 hover:text-slate-100 hover:bg-slate-600/10">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2H6a2 2 0 01-2-2v-4zM14 16a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2v-4z" />
              </svg>
              <span class="opacity-0 group-hover:opacity-100 transition-opacity duration-200 font-medium text-sm">Dashboard</span>
            </a>

            <!-- Check-in / Triagem -->
            @if (canAccessTriage()) {
              <a routerLink="/triagem"
                 class="flex items-center gap-4 p-2.5 rounded-xl transition-all duration-200 text-slate-400 hover:text-slate-100 hover:bg-slate-600/10">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5.586a1 1 0 0 1 .707.293l5.414 5.414a1 1 0 0 1 .293.707V19a2 2 0 0 1-2 2z" />
                </svg>
                <span class="opacity-0 group-hover:opacity-100 transition-opacity duration-200 font-medium text-sm">Fazer Triagem</span>
              </a>
            }

            <!-- Atendimento Médico -->
            @if (isMedico()) {
              <a routerLink="/atendimento"
                 class="flex items-center gap-4 p-2.5 rounded-xl transition-all duration-200 text-slate-400 hover:text-slate-100 hover:bg-slate-600/10">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                </svg>
                <span class="opacity-0 group-hover:opacity-100 transition-opacity duration-200 font-medium text-sm">Atendimento</span>
              </a>
            }

            <!-- Mural Sala de Espera -->
            <a routerLink="/mural"
               class="flex items-center gap-4 p-2.5 rounded-xl transition-all duration-200 text-slate-400 hover:text-slate-100 hover:bg-slate-600/10">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
              <span class="opacity-0 group-hover:opacity-100 transition-opacity duration-200 font-medium text-sm">Mural Público</span>
            </a>

            <!-- Relatórios -->
            <a routerLink="/relatorios"
               class="flex items-center gap-4 p-2.5 rounded-xl transition-all duration-200 text-slate-400 hover:text-slate-100 hover:bg-slate-600/10">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              <span class="opacity-0 group-hover:opacity-100 transition-opacity duration-200 font-medium text-sm">Relatórios</span>
            </a>
          </nav>
          <div class="px-4 py-2 text-center text-[8px] font-mono text-slate-600 uppercase tracking-widest hidden group-hover:block transition-all duration-300">
            Hospital STH v1.0
          </div>
        </aside>

        <!-- CONTENT AREA (Zona C) -->
        <main class="flex-1 ml-16 p-8 overflow-y-auto">
          <!-- Titulo -->
          <div class="mb-6">
            <h2 class="text-xl font-bold font-sans tracking-wide">Painel de Triagem Hospitalar</h2>
            <p class="text-xs text-slate-400">Gerenciamento de fila clínica e tempo de espera do paciente em tempo real.</p>
          </div>

          <!-- KPI Cards Grid -->
          <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
            <!-- Total na fila -->
            <div class="p-4 bg-navy-deep/60 border border-slate-600/20 rounded-xl relative overflow-hidden">
              <div class="text-[10px] font-mono tracking-wider text-slate-400 uppercase">Aguardando Triagem</div>
              <div class="text-4xl font-bold font-mono mt-2 text-cyber-teal">{{ totalAguardando() }}</div>
              <div class="absolute bottom-0 right-0 p-3 opacity-10">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
            </div>
            <!-- Em Atendimento -->
            <div class="p-4 bg-navy-deep/60 border border-slate-600/20 rounded-xl relative overflow-hidden">
              <div class="text-[10px] font-mono tracking-wider text-slate-400 uppercase">Em Atendimento</div>
              <div class="text-4xl font-bold font-mono mt-2 text-electric-blue">{{ totalAtendimento() }}</div>
              <div class="absolute bottom-0 right-0 p-3 opacity-10">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                </svg>
              </div>
            </div>
            <!-- Média de Espera -->
            <div class="p-4 bg-navy-deep/60 border border-slate-600/20 rounded-xl relative overflow-hidden">
              <div class="text-[10px] font-mono tracking-wider text-slate-400 uppercase">Média de Espera</div>
              <div class="text-4xl font-bold font-mono mt-2 text-triage-urgent">{{ avgWaitMinutes() }}m</div>
              <div class="absolute bottom-0 right-0 p-3 opacity-10">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <!-- Ocupação -->
            <div class="p-4 bg-navy-deep/60 border border-slate-600/20 rounded-xl relative overflow-hidden">
              <div class="text-[10px] font-mono tracking-wider text-slate-400 uppercase">Capacidade Operacional</div>
              <div class="text-4xl font-bold font-mono mt-2 text-triage-low">{{ occupancyRate() }}%</div>
              <div class="absolute bottom-0 right-0 p-3 opacity-10">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
            </div>
          </div>

          <!-- Filtros da Fila -->
          <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div class="flex items-center gap-2">
              <span class="text-xs font-mono text-slate-400 uppercase">Filtrar por Status:</span>
              <div class="inline-flex rounded-lg p-0.5 bg-black/20 border border-slate-600/10">
                <button (click)="setStatusFilter('TODOS')" 
                        [ngClass]="{'bg-slate-600/30 text-slate-100': statusFilter() === 'TODOS'}"
                        class="px-3 py-1 rounded-md text-xs font-semibold text-slate-400 hover:text-slate-200 transition-all">
                  Todos
                </button>
                <button (click)="setStatusFilter('AGUARDANDO')" 
                        [ngClass]="{'bg-slate-600/30 text-slate-100': statusFilter() === 'AGUARDANDO'}"
                        class="px-3 py-1 rounded-md text-xs font-semibold text-slate-400 hover:text-slate-200 transition-all">
                  Em Espera
                </button>
                <button (click)="setStatusFilter('EM_ATENDIMENTO')" 
                        [ngClass]="{'bg-slate-600/30 text-slate-100': statusFilter() === 'EM_ATENDIMENTO'}"
                        class="px-3 py-1 rounded-md text-xs font-semibold text-slate-400 hover:text-slate-200 transition-all">
                  Atendimento
                </button>
                <button (click)="setStatusFilter('FINALIZADO')" 
                        [ngClass]="{'bg-slate-600/30 text-slate-100': statusFilter() === 'FINALIZADO'}"
                        class="px-3 py-1 rounded-md text-xs font-semibold text-slate-400 hover:text-slate-200 transition-all">
                  Finalizados
                </button>
              </div>
            </div>

            <!-- Botão Atualizar -->
            <button (click)="loadTriagens()" class="btn-secondary h-8 px-3 rounded-lg text-xs flex items-center gap-1.5">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 1121.21 8H18.2" />
              </svg>
              <span>Recarregar Fila</span>
            </button>
          </div>

          <!-- Pacientes Wait list -->
          <div class="flex flex-col gap-3">
            @if (loadingTriagens()) {
              <div class="text-center py-12 text-slate-400 text-sm">
                <span class="inline-block w-6 h-6 border-2 border-cyber-teal border-t-transparent rounded-full animate-spin mb-2"></span>
                <p>Carregando pacientes da fila...</p>
              </div>
            } @else if (filteredTriagens().length === 0) {
              <div class="text-center py-12 glass-card rounded-xl border border-slate-600/20 text-slate-400 text-sm">
                Nenhum paciente encontrado na fila com o filtro selecionado.
              </div>
            } @else {
              @for (triagem of filteredTriagens(); track triagem.id) {
                <app-patient-card 
                  [triageId]="triagem.id"
                  [patientName]="patientsCache()[triagem.pacienteId] || 'Buscando paciente...'"
                  [priority]="triagem.prioridade"
                  [status]="triagem.status"
                  [createdAt]="triagem.createdAt"
                  [selected]="selectedTriageId() === triagem.id"
                  (cardClick)="selectTriage(triagem.id)"
                ></app-patient-card>
              }
            }
          </div>
        </main>

        <!-- SIDEBAR DETALHES (Zona D - Slide-in) -->
        <aside [ngClass]="selectedTriageId() ? 'translate-x-0' : 'translate-x-full'" 
               class="w-[380px] bg-navy-deep border-l border-slate-600/20 fixed top-16 bottom-0 right-0 z-50 transition-transform duration-300 shadow-2xl flex flex-col justify-between">
          
          @if (loadingDetails()) {
            <div class="flex-1 flex flex-col items-center justify-center text-slate-400 text-sm">
              <span class="w-6 h-6 border-2 border-cyber-teal border-t-transparent rounded-full animate-spin mb-2"></span>
              Carregando ficha clínica...
            </div>
          } @else if (details(); as dt) {
            <!-- Header Ficha -->
            <div class="p-6 border-b border-slate-600/20 relative">
              <button (click)="closeDetails()" class="absolute top-4 right-4 p-1 bg-black/10 hover:bg-slate-600/20 border border-slate-600/10 rounded-lg text-slate-400 hover:text-slate-100 transition-all">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>

              <span class="text-[10px] font-mono text-slate-400">FICHA TÉCNICA #{{ dt.id }}</span>
              <h3 class="text-lg font-bold text-slate-100 mt-1 font-sans tracking-wide">
                {{ selectedPatient()?.nome }}
              </h3>
              <p class="text-xs font-mono text-slate-400 mt-0.5">CPF: {{ selectedPatient()?.cpf }}</p>
              
              <div class="mt-3">
                <app-triage-badge [priority]="dt.prioridade" size="md"></app-triage-badge>
              </div>
            </div>

            <!-- Conteúdo Ficha -->
            <div class="flex-1 overflow-y-auto p-6 flex flex-col gap-6">
              <!-- Dados Triagem -->
              <div>
                <h4 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Queixa Principal</h4>
                <div class="p-3 bg-black/20 border border-slate-600/10 rounded-lg text-sm text-slate-200 whitespace-pre-line leading-relaxed">
                  {{ dt.queixaPrincipal || 'Nenhuma queixa registrada' }}
                </div>
              </div>

              <!-- Sinais Vitais Grid -->
              @if (dt.sinaisVitais; as sv) {
                <div>
                  <h4 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">Sinais Vitais Coletados</h4>
                  <div class="grid grid-cols-2 gap-3">
                    <app-vital-sign label="Pressão" [value]="sv.pressaoSist + '/' + sv.pressaoDiast" unit="mmHg" [critical]="isPressureCritical(sv)"></app-vital-sign>
                    <app-vital-sign label="Temperatura" [value]="sv.temperatura" unit="°C" [critical]="sv.temperatura > 38" [trend]="sv.temperatura > 38 ? 'up' : 'stable'"></app-vital-sign>
                    <app-vital-sign label="Pulso / Freq" [value]="sv.frequencia" unit="bpm" [critical]="sv.frequencia > 100 || sv.frequencia < 50" [trend]="sv.frequencia > 100 ? 'up' : null"></app-vital-sign>
                    <app-vital-sign label="Saturação O2" [value]="sv.saturacao" unit="%" [critical]="sv.saturacao < 93" [trend]="sv.saturacao < 93 ? 'down' : 'stable'"></app-vital-sign>
                  </div>
                </div>
              }

              <!-- Informações Técnicas -->
              <div class="flex flex-col gap-2 p-3 bg-black/10 border border-slate-600/5 rounded-lg text-xs font-mono text-slate-400">
                <div>Profissional de Triagem: {{ enfermeiroNome() }}</div>
                <div>Médico Responsável: {{ medicoNome() }}</div>
                <div>Criado em: {{ dt.createdAt | date:'dd/MM/yyyy HH:mm' }}</div>
                @if (dt.classificadoEm) {
                  <div>Classificado em: {{ dt.classificadoEm | date:'dd/MM/yyyy HH:mm' }}</div>
                }
              </div>
            </div>

            <!-- Rodapé Ações Ficha -->
            <div class="p-6 border-t border-slate-600/20 bg-black/20">
              <!-- Se médico logado e triage em andamento/classificada -->
              @if (isMedico() && dt.status === 'EM_ATENDIMENTO') {
                <div class="flex flex-col gap-2">
                  <div class="flex items-center gap-2 mb-1.5">
                    <label class="text-[10px] font-mono text-slate-400 uppercase tracking-wider">Sala de Atendimento:</label>
                    <input type="text" [(ngModel)]="salaAtendimento" class="form-input text-xs h-7 py-1 px-2.5 w-32 ml-auto text-right bg-black/30 border-slate-600/20 rounded-md" />
                  </div>

                  <button (click)="chamarPaciente(dt.id)" class="btn-secondary w-full h-11 rounded-lg text-sm flex items-center justify-center gap-2 border border-cyber-teal/40 text-cyber-teal hover:bg-cyber-teal/10 transition-all">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                    </svg>
                    <span>{{ dt.chamado ? 'Chamar Novamente' : 'Chamar Paciente' }}</span>
                  </button>

                  <button (click)="iniciarAtendimentoMedico(dt.id)" class="btn-primary w-full h-11 rounded-lg text-sm flex items-center justify-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                    </svg>
                    <span>Iniciar Atendimento Clínico</span>
                  </button>
                </div>
              } @else if (dt.status === 'AGUARDANDO' && canAccessTriage()) {
                <button (click)="iniciarClassificacao(dt.id)" class="btn-primary w-full h-11 rounded-lg text-sm flex items-center justify-center gap-2">
                  <span>Classificar Paciente</span>
                </button>
              } @else {
                <div class="text-center text-xs text-slate-500 font-mono italic">
                  Prontuário sob custódia clínica
                </div>
              }
            </div>
          } @else {
            <div class="flex-1 flex items-center justify-center text-slate-500 text-sm italic">
              Selecione um paciente para ver a ficha técnica.
            </div>
          }
        </aside>

      </div>
    </div>
  `
})
export class Dashboard implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly triageService = inject(TriageService);
  private readonly router = inject(Router);

  // States
  readonly currentTime = signal('');
  readonly currentUser = this.authService.currentUser;
  
  readonly triagens = signal<TriageItem[]>([]);
  readonly patientsCache = signal<Record<number, string>>({}); // cache patientId -> name
  readonly loadingTriagens = signal(false);
  readonly statusFilter = signal<'TODOS' | 'AGUARDANDO' | 'EM_ATENDIMENTO' | 'FINALIZADO'>('TODOS');

  // Selected details
  readonly selectedTriageId = signal<number | null>(null);
  readonly details = signal<TriageDetail | null>(null);
  readonly selectedPatient = signal<Paciente | null>(null);
  readonly loadingDetails = signal(false);
  readonly enfermeiroNome = signal('N/A');
  readonly medicoNome = signal('N/A');

  // KPI calculations
  readonly totalAguardando = signal(0);
  readonly totalAtendimento = signal(0);
  readonly avgWaitMinutes = signal(0);
  readonly occupancyRate = signal(0);

  private clockInterval: any;
  private pollingInterval: any;

  constructor() {
    // Redireciona se não logado
    effect(() => {
      if (!this.currentUser()) {
        this.router.navigate(['/login']);
      }
    });
  }

  ngOnInit() {
    this.updateClock();
    this.clockInterval = setInterval(() => this.updateClock(), 1000);

    this.loadTriagens();
    // Poll queue every 15s to represent real-time dashboard
    this.pollingInterval = setInterval(() => this.loadTriagens(), 15000);
  }

  ngOnDestroy() {
    if (this.clockInterval) clearInterval(this.clockInterval);
    if (this.pollingInterval) clearInterval(this.pollingInterval);
  }

  updateClock() {
    const now = new Date();
    this.currentTime.set(now.toLocaleTimeString('pt-BR'));
  }

  logout() {
    this.authService.logout();
  }

  // Permissions helpers
  isMedico(): boolean {
    return this.authService.hasRole(['MEDICO']);
  }

  canAccessTriage(): boolean {
    return this.authService.hasRole(['ENFERMEIRO', 'RECEPCIONISTA', 'ADMIN']);
  }

  setStatusFilter(filter: 'TODOS' | 'AGUARDANDO' | 'EM_ATENDIMENTO' | 'FINALIZADO') {
    this.statusFilter.set(filter);
  }

  filteredTriagens() {
    const list = this.triagens();
    const filter = this.statusFilter();
    
    // Sort logic: VERMELHO first, then LARANJA, AMARELO, VERDE, then null (Aguardando)
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
      // Secondary sort: oldest first for wait times
      return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
    });

    if (filter === 'TODOS') return sortedList;
    return sortedList.filter(t => t.status === filter);
  }

  loadTriagens() {
    if (this.triagens().length === 0) {
      this.loadingTriagens.set(true);
    }
    
    this.triageService.listarTriagens().subscribe({
      next: (data) => {
        this.triagens.set(data);
        this.loadingTriagens.set(false);
        this.calculateKpis(data);

        // Fetch names for all patients in queue
        data.forEach(t => {
          if (!this.patientsCache()[t.pacienteId]) {
            this.triageService.buscarPacientePorId(t.pacienteId).subscribe({
              next: (p) => {
                this.patientsCache.update(c => ({ ...c, [t.pacienteId]: p.nome }));
              },
              error: () => {
                this.patientsCache.update(c => ({ ...c, [t.pacienteId]: `Paciente (ID: ${t.pacienteId})` }));
              }
            });
          }
        });
      },
      error: () => {
        this.loadingTriagens.set(false);
      }
    });
  }

  calculateKpis(data: TriageItem[]) {
    const aguardando = data.filter(t => t.status === 'AGUARDANDO');
    const atendimento = data.filter(t => t.status === 'EM_ATENDIMENTO');
    this.totalAguardando.set(aguardando.length);
    this.totalAtendimento.set(atendimento.length);

    // Calculate wait average
    if (aguardando.length > 0) {
      const totalMinutes = aguardando.reduce((acc, t) => {
        const created = new Date(t.createdAt).getTime();
        const diff = new Date().getTime() - created;
        return acc + Math.floor(diff / 60000);
      }, 0);
      this.avgWaitMinutes.set(Math.max(0, Math.floor(totalMinutes / aguardando.length)));
    } else {
      this.avgWaitMinutes.set(0);
    }

    // Capacity percentage
    const maxCapacity = 50;
    const currentActive = data.filter(t => t.status !== 'FINALIZADO').length;
    this.occupancyRate.set(Math.min(100, Math.floor((currentActive / maxCapacity) * 100)));
  }

  getPatientName(pacienteId: number): string {
    return this.patientsCache()[pacienteId] || 'Buscando paciente...';
  }

  selectTriage(id: number) {
    this.selectedTriageId.set(id);
    this.loadingDetails.set(true);
    this.details.set(null);
    this.selectedPatient.set(null);
    this.enfermeiroNome.set('N/A');
    this.medicoNome.set('N/A');

    this.triageService.buscarTriagemPorId(id).subscribe({
      next: (tDetails) => {
        this.details.set(tDetails);
        this.loadingDetails.set(false);

        // Get patient profile
        this.triageService.buscarPacientePorId(tDetails.pacienteId).subscribe({
          next: (p) => {
            this.selectedPatient.set(p);
          },
          error: () => {
            this.selectedPatient.set({
              id: tDetails.pacienteId,
              nome: `Paciente (ID: ${tDetails.pacienteId})`,
              cpf: 'Não disponível'
            });
          }
        });

        // Resolve names of professionals from user service if they exist
        if (tDetails.enfermeiroId) {
          this.authService.buscarUsuarioPorId(tDetails.enfermeiroId).subscribe({
            next: (u) => {
              this.enfermeiroNome.set(u.nome);
            },
            error: () => {
              this.enfermeiroNome.set(`Enfermeiro (ID: ${tDetails.enfermeiroId})`);
            }
          });
        }
        if (tDetails.medicoId) {
          this.authService.buscarUsuarioPorId(tDetails.medicoId).subscribe({
            next: (u) => {
              this.medicoNome.set(u.nome);
            },
            error: () => {
              this.medicoNome.set(`Médico (ID: ${tDetails.medicoId})`);
            }
          });
        }
      },
      error: () => {
        this.loadingDetails.set(false);
      }
    });
  }

  closeDetails() {
    this.selectedTriageId.set(null);
    this.details.set(null);
    this.selectedPatient.set(null);
  }

  isPressureCritical(sv: any): boolean {
    return sv.pressaoSist > 140 || sv.pressaoSist < 90 || sv.pressaoDiast > 90 || sv.pressaoDiast < 50;
  }

  iniciarAtendimentoMedico(triagemId: number) {
    this.router.navigate(['/atendimento'], { queryParams: { triagemId } });
  }

  iniciarClassificacao(triagemId: number) {
    // For nurses to go to classification form
    this.router.navigate(['/triagem'], { queryParams: { triagemId } });
  }

  salaAtendimento = localStorage.getItem('medico_sala') || 'Consultório 1';

  chamarPaciente(triageId: number) {
    localStorage.setItem('medico_sala', this.salaAtendimento);
    this.triageService.chamar(triageId, this.salaAtendimento).subscribe({
      next: () => {
        this.loadTriagens();
        this.selectTriage(triageId);
      },
      error: (err) => console.error('Erro ao chamar paciente:', err)
    });
  }
}
