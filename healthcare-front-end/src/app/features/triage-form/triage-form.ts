import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TriageService, Paciente, SinaisVitais } from '../../core/services/triage.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-triage-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-gradient-surface text-slate-100 flex flex-col font-sans">
      
      <!-- Top Header -->
      <header class="h-16 bg-navy-deep/80 border-b border-slate-600/20 px-6 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <button (click)="goBack()" class="p-1.5 bg-black/10 hover:bg-slate-600/20 border border-slate-600/10 rounded-lg text-slate-400 hover:text-slate-100 transition-all">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <div>
            <h2 class="text-sm font-bold">Atendimento e Triagem Clinica</h2>
            <span class="text-[9px] font-mono text-slate-400 uppercase">Fluxo Clínico Integrado</span>
          </div>
        </div>
        <div class="text-xs font-mono text-slate-400 bg-black/10 px-3 py-1 rounded-lg">
          Perfil: {{ currentUser()?.nome }} ({{ currentUser()?.role }})
        </div>
      </header>

      <div class="flex-1 flex justify-center p-8 overflow-y-auto">
        <div class="w-full max-w-3xl flex flex-col gap-6">
          
          <!-- Tab selector: Check-in vs Triage Form -->
          @if (!triageId()) {
            <div class="flex border-b border-slate-600/20 mb-2">
              <button (click)="activeTab.set('checkin')" 
                      [ngClass]="{'border-cyber-teal text-cyber-teal': activeTab() === 'checkin', 'border-transparent text-slate-400': activeTab() !== 'checkin'}"
                      class="px-6 py-3 border-b-2 font-bold text-sm tracking-wide transition-all">
                Check-in de Paciente
              </button>
              <button (click)="activeTab.set('novo-paciente')" 
                      [ngClass]="{'border-cyber-teal text-cyber-teal': activeTab() === 'novo-paciente', 'border-transparent text-slate-400': activeTab() !== 'novo-paciente'}"
                      class="px-6 py-3 border-b-2 font-bold text-sm tracking-wide transition-all">
                Cadastrar Novo Paciente
              </button>
            </div>
          }

          <!-- TAB: CHECK-IN -->
          @if (activeTab() === 'checkin' && !triageId()) {
            <div class="glass-card p-6 rounded-2xl flex flex-col gap-6">
              <div>
                <h3 class="text-base font-bold text-slate-100 font-sans tracking-wide">Buscar Paciente Cadastrado</h3>
                <p class="text-xs text-slate-400 mt-1">Busque o paciente pelo CPF para registrar a entrada no hospital.</p>
              </div>

              @if (checkinError()) {
                <div class="p-3 rounded-lg bg-triage-critical/10 border border-triage-critical/30 text-triage-critical text-xs">
                  {{ checkinError() }}
                </div>
              }
              @if (checkinSuccess()) {
                <div class="p-3 rounded-lg bg-triage-low/10 border border-triage-low/30 text-triage-low text-xs">
                  {{ checkinSuccess() }}
                </div>
              }

              <div class="flex flex-col sm:flex-row gap-3">
                <input type="text" [(ngModel)]="searchCpf" placeholder="Digite o CPF do paciente (apenas números)" 
                       class="form-input flex-1 text-sm font-mono" />
                <button (click)="buscarPaciente()" [disabled]="searchingPaciente()" 
                        class="btn-secondary h-11 px-5 rounded-lg text-sm flex items-center justify-center gap-2">
                  @if (searchingPaciente()) {
                    <span class="w-4 h-4 border-2 border-cyber-teal border-t-transparent rounded-full animate-spin"></span>
                  }
                  <span>Buscar</span>
                </button>
              </div>

              <!-- Resultado da busca -->
              @if (foundPaciente(); as p) {
                <div class="p-4 bg-navy-deep/40 border border-slate-600/20 rounded-xl flex items-center justify-between">
                  <div>
                    <h4 class="text-sm font-bold text-slate-200">{{ p.nome }}</h4>
                    <p class="text-xs font-mono text-slate-400 mt-0.5">CPF: {{ p.cpf }}</p>
                  </div>
                  <button (click)="confirmCheckin(p.id)" [disabled]="submittingCheckin()"
                          class="btn-primary px-4 py-2 text-xs rounded-lg">
                    Realizar Check-in na Fila
                  </button>
                </div>
              }
            </div>
          }

          <!-- TAB: NOVO PACIENTE -->
          @if (activeTab() === 'novo-paciente' && !triageId()) {
            <div class="glass-card p-6 rounded-2xl flex flex-col gap-6">
              <div>
                <h3 class="text-base font-bold text-slate-100 font-sans tracking-wide">Cadastro de Paciente</h3>
                <p class="text-xs text-slate-400 mt-1">Insira os dados cadastrais do paciente para registrá-lo no hospital.</p>
              </div>

              @if (cadastroError()) {
                <div class="p-3 rounded-lg bg-triage-critical/10 border border-triage-critical/30 text-triage-critical text-xs">
                  {{ cadastroError() }}
                </div>
              }

              <form (ngSubmit)="cadastrarPaciente()" class="flex flex-col gap-4">
                <div class="flex flex-col gap-1.5">
                  <label class="text-[10px] font-semibold tracking-wider text-slate-400 uppercase">Nome Completo</label>
                  <input type="text" name="nome" [(ngModel)]="novoNome" required placeholder="Nome do paciente" class="form-input text-sm" />
                </div>

                <div class="flex flex-col gap-1.5">
                  <label class="text-[10px] font-semibold tracking-wider text-slate-400 uppercase">CPF (Apenas números)</label>
                  <input type="text" name="cpf" [(ngModel)]="novoCpf" required placeholder="Ex: 12345678909" class="form-input text-sm font-mono" />
                </div>

                <button type="submit" [disabled]="submittingCadastro()" class="btn-primary w-full h-11 rounded-lg text-sm mt-2">
                  Salvar Paciente & Fazer Check-in
                </button>
              </form>
            </div>
          }

          <!-- TRIAGE CLASSIFICATION STEP WIZARD -->
          @if (triageId()) {
            <!-- Banner com o nome do paciente -->
            <div class="glass-card p-4 rounded-xl flex items-center justify-between border-l-4 border-l-cyber-teal mb-4 bg-navy-deep/60">
              <div class="flex flex-col">
                <span class="text-[9px] font-mono text-slate-400 uppercase tracking-widest">Paciente em Triagem</span>
                <h4 class="text-sm font-bold text-slate-100 font-sans mt-0.5">{{ pacienteNome() }}</h4>
              </div>
              <div class="text-right">
                <span class="text-[9px] font-mono text-slate-400 block uppercase tracking-widest">Triagem ID</span>
                <span class="text-xs font-mono text-cyber-teal font-bold">#{{ triageId() }}</span>
              </div>
            </div>

            <div class="glass-card p-6 rounded-2xl">
              <!-- Steps Indicator -->
              <div class="flex items-center justify-between mb-8 border-b border-slate-600/10 pb-4">
                @for (stepNum of [1, 2, 3, 4]; track stepNum) {
                  <div class="flex items-center gap-2">
                    <div [ngClass]="{
                      'bg-gradient-teal text-obsidian shadow-glow-teal font-bold': activeStep() === stepNum,
                      'bg-slate-600/30 text-slate-400': activeStep() < stepNum,
                      'bg-cyber-teal/20 text-cyber-teal border border-cyber-teal/30': activeStep() > stepNum
                    }" class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-mono transition-all">
                      {{ stepNum }}
                    </div>
                    <span [ngClass]="activeStep() === stepNum ? 'text-slate-200' : 'text-slate-500'" class="text-xs font-medium hidden sm:inline">
                      {{ stepNames[stepNum - 1] }}
                    </span>
                    @if (stepNum < 4) {
                      <div class="w-8 sm:w-16 h-0.5 bg-slate-600/20"></div>
                    }
                  </div>
                }
              </div>

              <!-- STEP 1: QUEIXA PRINCIPAL -->
              @if (activeStep() === 1) {
                <div class="flex flex-col gap-5 animate-fade-in-up">
                  <div>
                    <h3 class="text-base font-bold text-slate-100">Ficha de Queixa Principal</h3>
                    <p class="text-xs text-slate-400 mt-1">Descreva em detalhes a reclamação de saúde trazida pelo paciente.</p>
                  </div>

                  <div class="flex flex-col gap-2">
                    <label class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Queixa Principal (Obrigatório)</label>
                    <textarea [(ngModel)]="queixa" rows="5" placeholder="Descreva os sintomas relatados..." class="form-input text-sm leading-relaxed resize-none"></textarea>
                  </div>

                  <!-- Quick symptoms tag selectors -->
                  <div>
                    <span class="text-[10px] font-mono text-slate-400 uppercase tracking-widest block mb-2">Sintomas Comuns</span>
                    <div class="flex flex-wrap gap-2">
                      @for (symptom of commonSymptoms; track symptom) {
                        <button (click)="addSymptomTag(symptom)" 
                                class="px-3 py-1.5 bg-black/20 hover:bg-cyber-teal/10 hover:text-cyber-teal border border-slate-600/10 rounded-lg text-xs transition-all">
                          + {{ symptom }}
                        </button>
                      }
                    </div>
                  </div>

                  <div class="flex justify-end mt-4">
                    <button (click)="nextStep()" [disabled]="!queixa.trim()" class="btn-primary px-6 py-2.5 rounded-lg text-sm">
                      Próximo Passo
                    </button>
                  </div>
                </div>
              }

              <!-- STEP 2: SINAIS VITAIS -->
              @if (activeStep() === 2) {
                <div class="flex flex-col gap-5 animate-fade-in-up">
                  <div>
                    <h3 class="text-base font-bold text-slate-100">Sinais Vitais</h3>
                    <p class="text-xs text-slate-400 mt-1">Colete e registre os dados fisiológicos do paciente.</p>
                  </div>

                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <!-- PA Sistolica -->
                    <div class="flex flex-col gap-1.5">
                      <label class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Pressão Sistólica (mmHg)</label>
                      <input type="number" [(ngModel)]="sv.pressaoSist" placeholder="Ex: 120" class="form-input text-sm font-mono" />
                    </div>

                    <!-- PA Diastolica -->
                    <div class="flex flex-col gap-1.5">
                      <label class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Pressão Diastólica (mmHg)</label>
                      <input type="number" [(ngModel)]="sv.pressaoDiast" placeholder="Ex: 80" class="form-input text-sm font-mono" />
                    </div>

                    <!-- Temperatura -->
                    <div class="flex flex-col gap-1.5">
                      <label class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Temperatura corporal (°C)</label>
                      <input type="number" step="0.1" [(ngModel)]="sv.temperatura" placeholder="Ex: 36.5" class="form-input text-sm font-mono" />
                    </div>

                    <!-- Frequencia Cardiaca -->
                    <div class="flex flex-col gap-1.5">
                      <label class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Frequência Cardíaca (bpm)</label>
                      <input type="number" [(ngModel)]="sv.frequencia" placeholder="Ex: 75" class="form-input text-sm font-mono" />
                    </div>

                    <!-- Saturacao -->
                    <div class="flex flex-col gap-1.5">
                      <label class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Saturação de Oxigênio SpO2 (%)</label>
                      <input type="number" [(ngModel)]="sv.saturacao" placeholder="Ex: 98" class="form-input text-sm font-mono" />
                    </div>
                  </div>

                  <div class="flex justify-between mt-4">
                    <button (click)="prevStep()" class="btn-secondary px-6 py-2.5 rounded-lg text-sm">Voltar</button>
                    <button (click)="nextStep()" class="btn-primary px-6 py-2.5 rounded-lg text-sm">Próximo Passo</button>
                  </div>
                </div>
              }

              <!-- STEP 3: PROTOCOLO MANCHESTER -->
              @if (activeStep() === 3) {
                <div class="flex flex-col gap-5 animate-fade-in-up">
                  <div>
                    <h3 class="text-base font-bold text-slate-100">Classificação de Risco Manchester</h3>
                    <p class="text-xs text-slate-400 mt-1">Algoritmo de triagem baseado nas respostas das perguntas de triagem clínica.</p>
                  </div>

                  <!-- Seletor Rápido se quiser forçar -->
                  <div class="p-4 bg-navy-deep/40 border border-slate-600/10 rounded-xl flex flex-col gap-3">
                    <span class="text-xs font-mono text-slate-400">Classificação Recomendada:</span>
                    <div class="flex items-center gap-3">
                      <div [ngClass]="{
                        'bg-triage-critical/15 text-triage-critical border border-triage-critical/30 shadow-glow-critical animate-pulse-critical': prioridade() === 'VERMELHO',
                        'bg-triage-urgent/15 text-triage-urgent border border-triage-urgent/30': prioridade() === 'LARANJA',
                        'bg-triage-semi/15 text-triage-semi border border-triage-semi/30': prioridade() === 'AMARELO',
                        'bg-triage-low/15 text-triage-low border border-triage-low/30': prioridade() === 'VERDE',
                        'bg-triage-none/15 text-triage-none border border-triage-none/30': !prioridade()
                      }" class="h-10 px-6 rounded-lg flex items-center font-bold text-sm tracking-wide">
                        {{ getPrioridadeLabel() }}
                      </div>
                    </div>
                  </div>

                  <!-- Perguntas Manchester -->
                  <div class="flex flex-col gap-4">
                    <span class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Árvore de Decisão de Risco</span>
                    
                    @for (q of questions; track q.id) {
                      <div class="p-3 bg-black/20 border border-slate-600/10 rounded-xl flex items-center justify-between gap-4">
                        <span class="text-xs text-slate-200 leading-relaxed">{{ q.text }}</span>
                        <div class="flex gap-2 shrink-0">
                          <button (click)="answerQuestion(q.id, true)" 
                                  [ngClass]="q.answer === true ? 'bg-triage-critical text-white' : 'bg-black/30 border border-slate-600/20 text-slate-300 hover:bg-slate-600/10'"
                                  class="px-3 py-1 rounded-md text-xs font-bold transition-all">
                            Sim
                          </button>
                          <button (click)="answerQuestion(q.id, false)" 
                                  [ngClass]="q.answer === false ? 'bg-triage-low text-white' : 'bg-black/30 border border-slate-600/20 text-slate-300 hover:bg-slate-600/10'"
                                  class="px-3 py-1 rounded-md text-xs font-bold transition-all">
                            Não
                          </button>
                        </div>
                      </div>
                    }
                  </div>

                  <div class="flex justify-between mt-4">
                    <button (click)="prevStep()" class="btn-secondary px-6 py-2.5 rounded-lg text-sm">Voltar</button>
                    <button (click)="nextStep()" class="btn-primary px-6 py-2.5 rounded-lg text-sm">Revisão Final</button>
                  </div>
                </div>
              }

              <!-- STEP 4: CONFIRMAÇÃO -->
              @if (activeStep() === 4) {
                <div class="flex flex-col gap-5 animate-fade-in-up">
                  <div>
                    <h3 class="text-base font-bold text-slate-100">Resumo da Classificação</h3>
                    <p class="text-xs text-slate-400 mt-1">Confirme as informações clínicas coletadas antes de encaminhar o paciente para a fila do médico.</p>
                  </div>

                  @if (submitError()) {
                    <div class="p-3 rounded-lg bg-triage-critical/10 border border-triage-critical/30 text-triage-critical text-xs">
                      {{ submitError() }}
                    </div>
                  }

                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="p-4 bg-black/20 border border-slate-600/10 rounded-xl">
                      <span class="text-[9px] font-mono text-slate-400 uppercase tracking-widest">Resumo Clínico</span>
                      <p class="text-sm text-slate-200 mt-1 font-sans whitespace-pre-line">{{ queixa }}</p>
                    </div>

                    <div class="p-4 bg-black/20 border border-slate-600/10 rounded-xl flex flex-col gap-2">
                      <span class="text-[9px] font-mono text-slate-400 uppercase tracking-widest block">Classificação de Risco</span>
                      <div class="font-bold text-sm text-slate-100 mt-1">
                        {{ getPrioridadeLabel() }}
                      </div>
                      <div class="text-[10px] font-mono text-slate-400 mt-2">
                        PA: {{ sv.pressaoSist }}/{{ sv.pressaoDiast }} mmHg | Temp: {{ sv.temperatura }} °C | Sat: {{ sv.saturacao }}%
                      </div>
                    </div>
                  </div>

                  <div class="flex justify-between mt-4">
                    <button (click)="prevStep()" class="btn-secondary px-6 py-2.5 rounded-lg text-sm">Voltar</button>
                    <button (click)="submitTriage()" [disabled]="submittingTriage()" class="btn-primary px-8 py-2.5 rounded-lg text-sm flex items-center justify-center gap-2">
                      @if (submittingTriage()) {
                        <span class="w-4 h-4 border-2 border-obsidian border-t-transparent rounded-full animate-spin"></span>
                      }
                      <span>Confirmar e Atribuir ao Médico</span>
                    </button>
                  </div>
                </div>
              }
            </div>
          }
          
        </div>
      </div>
    </div>
  `
})
export class TriageForm implements OnInit {
  private readonly triageService = inject(TriageService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly currentUser = this.authService.currentUser;

  // Flow State
  activeTab = signal<'checkin' | 'novo-paciente'>('checkin');
  activeStep = signal(1);
  triageId = signal<number | null>(null);
  pacienteId = signal<number | null>(null);
  pacienteNome = signal('Buscando paciente...');

  // Tab Check-in States
  searchCpf = '';
  searchingPaciente = signal(false);
  foundPaciente = signal<Paciente | null>(null);
  checkinError = signal('');
  checkinSuccess = signal('');
  submittingCheckin = signal(false);

  // Tab Cadastro States
  novoNome = '';
  novoCpf = '';
  cadastroError = signal('');
  submittingCadastro = signal(false);

  // Triage Wizard States
  queixa = '';
  sv: SinaisVitais = {
    pressaoSist: 120,
    pressaoDiast: 80,
    temperatura: 36.5,
    frequencia: 75,
    saturacao: 98
  };
  prioridade = signal<string>('VERDE');
  submittingTriage = signal(false);
  submitError = signal('');

  stepNames = ['Queixa Principal', 'Sinais Vitais', 'Triagem Manchester', 'Resumo'];
  commonSymptoms = ['Febre alta', 'Falta de ar', 'Dor de cabeça aguda', 'Dor no peito / torácica', 'Náusea / Vômito', 'Tontura intensa', 'Palpitação'];

  questions = [
    { id: 1, text: 'Risco de morte iminente, inconsciência, parada cardíaca ou trauma craniano grave?', answer: null as boolean | null },
    { id: 2, text: 'Dor torácica súbita e aguda, suspeita de AVC, convulsão ativa ou sangramento maciço?', answer: null as boolean | null },
    { id: 3, text: 'Dor severa moderada, vômitos incoercíveis, febre alta persistente ou confusão mental leve?', answer: null as boolean | null },
    { id: 4, text: 'Feridas simples, dor leve crônica, febre baixa ou resfriado comum?', answer: null as boolean | null }
  ];

  ngOnInit() {
    // Check if queryParam ?triagemId=X is provided (nurse taking a ticket to classify)
    this.route.queryParams.subscribe(params => {
      if (params['triagemId']) {
        const id = Number(params['triagemId']);
        this.triageId.set(id);
        
        // Load details to get queixa if pre-filled, or initialize empty
        this.triageService.buscarTriagemPorId(id).subscribe(t => {
          this.pacienteId.set(t.pacienteId);
          this.queixa = t.queixaPrincipal || '';
          if (t.sinaisVitais) {
            this.sv = {
              pressaoSist: t.sinaisVitais.pressaoSist,
              pressaoDiast: t.sinaisVitais.pressaoDiast,
              temperatura: t.sinaisVitais.temperatura,
              frequencia: t.sinaisVitais.frequencia,
              saturacao: t.sinaisVitais.saturacao
            };
          }

          // Buscar o nome do paciente
          this.triageService.buscarPacientePorId(t.pacienteId).subscribe({
            next: (p) => {
              this.pacienteNome.set(p.nome);
            },
            error: () => {
              this.pacienteNome.set(`Paciente (ID: ${t.pacienteId})`);
            }
          });
        });
      }
    });
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  // Checkin Operations
  buscarPaciente() {
    if (!this.searchCpf) {
      this.checkinError.set('Informe o CPF para realizar a busca.');
      return;
    }
    this.checkinError.set('');
    this.checkinSuccess.set('');
    this.foundPaciente.set(null);
    this.searchingPaciente.set(true);

    const cleanCpf = this.searchCpf.replace(/\D/g, '');

    this.triageService.buscarPacientePorCpf(cleanCpf).subscribe({
      next: (pac) => {
        this.foundPaciente.set(pac);
        this.searchingPaciente.set(false);
      },
      error: () => {
        this.searchingPaciente.set(false);
        this.checkinError.set('Paciente não cadastrado com este CPF.');
      }
    });
  }

  confirmCheckin(pacienteId: number) {
    this.submittingCheckin.set(true);
    this.checkinError.set('');
    this.triageService.checkIn(pacienteId).subscribe({
      next: (item) => {
        this.submittingCheckin.set(false);
        this.checkinSuccess.set(`Check-in realizado com sucesso! Triagem #${item.id} inserida na fila.`);
        this.foundPaciente.set(null);
        this.searchCpf = '';
        setTimeout(() => this.router.navigate(['/dashboard']), 2000);
      },
      error: (err) => {
        this.submittingCheckin.set(false);
        this.checkinError.set('Erro ao efetuar check-in. Talvez o paciente já possua uma triagem ativa.');
      }
    });
  }

  cadastrarPaciente() {
    if (!this.novoNome || !this.novoCpf) {
      this.cadastroError.set('Preencha os campos obrigatórios.');
      return;
    }
    this.cadastroError.set('');
    this.submittingCadastro.set(true);

    const cleanCpf = this.novoCpf.replace(/\D/g, '');
    if (cleanCpf.length !== 11) {
      this.submittingCadastro.set(false);
      this.cadastroError.set('CPF inválido. Deve conter exatamente 11 dígitos numéricos.');
      return;
    }

    this.triageService.criarPaciente({ nome: this.novoNome, cpf: cleanCpf }).subscribe({
      next: (pac) => {
        // A triagem é criada de forma assíncrona em segundo plano pelo RabbitMQ (PacienteCriadoEvent).
        // Redirecionamos o usuário diretamente para o painel principal.
        this.submittingCadastro.set(false);
        this.novoNome = '';
        this.novoCpf = '';
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.submittingCadastro.set(false);
        this.cadastroError.set('Erro ao cadastrar paciente. Verifique se o CPF já existe.');
      }
    });
  }

  // Wizard Navigation
  nextStep() {
    if (this.activeStep() < 4) {
      this.activeStep.update(s => s + 1);
    }
  }

  prevStep() {
    if (this.activeStep() > 1) {
      this.activeStep.update(s => s - 1);
    }
  }

  addSymptomTag(tag: string) {
    if (this.queixa) {
      this.queixa += `, ${tag.toLowerCase()}`;
    } else {
      this.queixa = tag;
    }
  }

  answerQuestion(id: number, answer: boolean) {
    const q = this.questions.find(item => item.id === id);
    if (q) {
      q.answer = answer;
    }
    this.evaluateManchester();
  }

  evaluateManchester() {
    // If Question 1 is YES -> VERMELHO (Nível 1 - Emergência)
    if (this.questions[0].answer === true) {
      this.prioridade.set('VERMELHO');
      return;
    }
    // If Question 2 is YES -> LARANJA (Nível 2 - Muito Urgente)
    if (this.questions[1].answer === true) {
      this.prioridade.set('LARANJA');
      return;
    }
    // If Question 3 is YES -> AMARELO (Nível 3 - Urgente)
    if (this.questions[2].answer === true) {
      this.prioridade.set('AMARELO');
      return;
    }
    // If Question 4 is YES -> VERDE (Nível 4 - Pouco Urgente)
    if (this.questions[3].answer === true) {
      this.prioridade.set('VERDE');
      return;
    }
    this.prioridade.set('VERDE'); // Default green for standard
  }

  getPrioridadeLabel(): string {
    const p = this.prioridade();
    switch (p) {
      case 'VERMELHO': return 'EMERGÊNCIA (Vermelho)';
      case 'LARANJA': return 'MUITO URGENTE (Laranja)';
      case 'AMARELO': return 'URGENTE (Amarelo)';
      case 'VERDE': return 'POUCO URGENTE (Verde)';
      default: return 'AGUARDANDO AVALIAÇÃO';
    }
  }

  submitTriage() {
    const id = this.triageId();
    const pacId = this.pacienteId();
    if (!id || !pacId) return;

    this.submittingTriage.set(true);
    this.submitError.set('');

    const enfId = this.currentUser()?.usuarioId || 1;

    // 1. Abrir triagem (associa enfermeiro e queixa)
    this.triageService.abrir(pacId, enfId, this.queixa).subscribe({
      next: () => {
        // 2. Classificar triagem (sinais vitais + prioridade)
        this.triageService.classificar(id, {
          medicoId: null, // Let load balancing auto-route
          prioridade: this.prioridade(),
          sinaisVitais: this.sv
        }).subscribe({
          next: () => {
            this.submittingTriage.set(false);
            this.router.navigate(['/dashboard']);
          },
          error: () => {
            this.submittingTriage.set(false);
            this.submitError.set('Erro ao classificar triagem. Verifique os dados de sinais vitais.');
          }
        });
      },
      error: () => {
        this.submittingTriage.set(false);
        this.submitError.set('Erro ao abrir prontuário de triagem.');
      }
    });
  }
}
