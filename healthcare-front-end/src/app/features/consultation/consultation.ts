import { Component, inject, signal, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TriageService, TriageDetail, Paciente } from '../../core/services/triage.service';
import { DiagnosisService, DiagnosticoResponse, MedicamentoPrescrito } from '../../core/services/diagnosis.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-consultation',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
            <h2 class="text-sm font-bold">Prontuário e Atendimento Médico</h2>
            <span class="text-[9px] font-mono text-slate-400 uppercase">Área de Prescrição Eletrônica</span>
          </div>
        </div>
        <div class="text-xs font-mono text-slate-400 bg-black/10 px-3 py-1 rounded-lg">
          Médico: {{ currentUser()?.nome }} (ID: {{ currentUser()?.usuarioId }})
        </div>
      </header>

      <div class="flex-1 flex justify-center p-8 overflow-y-auto">
        <div class="w-full max-w-4xl flex flex-col gap-6">

          @if (errorMsg()) {
            <div class="p-4 bg-triage-critical/10 border border-triage-critical/30 rounded-xl text-triage-critical text-sm font-mono flex items-center justify-between">
              <span>{{ errorMsg() }}</span>
              <button (click)="errorMsg.set('')" class="text-xs underline hover:text-white">Fechar</button>
            </div>
          }

          @if (loading()) {
            <div class="text-center py-20 text-slate-400">
              <span class="inline-block w-8 h-8 border-2 border-cyber-teal border-t-transparent rounded-full animate-spin mb-3"></span>
              <p>Iniciando prontuário médico...</p>
            </div>
          } @else if (diagnostico(); as diag) {
            <!-- Dados do Paciente e Triagem -->
            <div class="glass-card p-6 rounded-2xl flex flex-col sm:flex-row justify-between gap-4">
              <div>
                <span class="text-[9px] font-mono text-slate-400 uppercase tracking-widest">Paciente em Atendimento</span>
                <div class="flex flex-wrap items-center gap-3 mt-1">
                  <h3 class="text-lg font-bold text-slate-100 font-sans tracking-wide">{{ paciente()?.nome }}</h3>
                  @if (!isSigned()) {
                    <div class="flex items-center gap-1.5 bg-black/20 px-2.5 py-1 rounded-lg border border-slate-600/10">
                      <input type="text" [(ngModel)]="salaAtendimento" (change)="saveSala()" placeholder="Sala" class="bg-transparent text-xs font-mono text-cyber-teal w-20 focus:outline-none border-b border-slate-600/30 text-center" />
                      <button (click)="chamarPacienteAtual()" class="px-2.5 py-0.5 bg-cyber-teal/10 hover:bg-cyber-teal/20 text-cyber-teal border border-cyber-teal/30 rounded-md text-[10px] font-semibold flex items-center gap-1 transition-all">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                          <path stroke-linecap="round" stroke-linejoin="round" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                        </svg>
                        <span>{{ triage()?.chamado ? 'Rechamar' : 'Chamar no Mural' }}</span>
                      </button>
                    </div>
                  }
                </div>
                <p class="text-xs font-mono text-slate-400 mt-0.5">CPF: {{ paciente()?.cpf }} | Triagem ID: #{{ triage()?.id }}</p>
              </div>
              <div class="flex flex-col text-right sm:items-end font-mono">
                <span class="text-[9px] text-slate-400 uppercase tracking-widest">Classificação de Risco</span>
                <span [ngClass]="{
                  'text-triage-critical': triage()?.prioridade === 'VERMELHO',
                  'text-triage-urgent': triage()?.prioridade === 'LARANJA',
                  'text-triage-semi': triage()?.prioridade === 'AMARELO',
                  'text-triage-low': triage()?.prioridade === 'VERDE'
                }" class="text-sm font-bold mt-1">
                  {{ triage()?.prioridade || 'SEM CLASSIFICAÇÃO' }}
                </span>
                <span class="text-[10px] text-slate-400 mt-1">Queixa: {{ triage()?.queixaPrincipal }}</span>
              </div>
            </div>

            <!-- Sinais Vitais do Paciente -->
            @if (triage()?.sinaisVitais; as sv) {
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div class="p-3 bg-black/20 border border-slate-600/10 rounded-xl font-mono text-center">
                  <div class="text-[9px] text-slate-400 uppercase">Pressão Arterial</div>
                  <div class="text-lg font-bold mt-1 text-slate-100">{{ sv.pressaoSist }}/{{ sv.pressaoDiast }} mmHg</div>
                </div>
                <div class="p-3 bg-black/20 border border-slate-600/10 rounded-xl font-mono text-center">
                  <div class="text-[9px] text-slate-400 uppercase">Temperatura</div>
                  <div class="text-lg font-bold mt-1 text-slate-100">{{ sv.temperatura }} °C</div>
                </div>
                <div class="p-3 bg-black/20 border border-slate-600/10 rounded-xl font-mono text-center">
                  <div class="text-[9px] text-slate-400 uppercase">Frequência</div>
                  <div class="text-lg font-bold mt-1 text-slate-100">{{ sv.frequencia }} bpm</div>
                </div>
                <div class="p-3 bg-black/20 border border-slate-600/10 rounded-xl font-mono text-center">
                  <div class="text-[9px] text-slate-400 uppercase">Saturação</div>
                  <div class="text-lg font-bold mt-1 text-slate-100">{{ sv.saturacao }}%</div>
                </div>
              </div>
            }

            <!-- Avaliacao e Diagnostico -->
            <div class="glass-card p-6 rounded-2xl flex flex-col gap-4">
              <div>
                <h3 class="text-base font-bold text-slate-100">Avaliação Clínica & Diagnóstico</h3>
                <p class="text-xs text-slate-400 mt-1">Registros e conclusões da avaliação médica atual.</p>
              </div>

              <textarea [(ngModel)]="descricao" [disabled]="isSigned()"
                        rows="6" placeholder="Insira o diagnóstico clínico do paciente..."
                        class="form-input text-sm leading-relaxed resize-none w-full"></textarea>
            </div>

            <!-- Receituário / Medicamentos -->
            <div class="glass-card p-6 rounded-2xl flex flex-col gap-6">
              <div>
                <h3 class="text-base font-bold text-slate-100">Receita de Medicamentos</h3>
                <p class="text-xs text-slate-400 mt-1">Insira os medicamentos prescritos com suas respectivas dosagens e períodos.</p>
              </div>

              <!-- Listagem de Medicamentos Inseridos -->
              <div class="flex flex-col gap-2">
                @if (medicamentos.length === 0) {
                  <p class="text-xs text-slate-500 font-mono italic">Nenhum medicamento prescrito até o momento.</p>
                } @else {
                  <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr class="border-b border-slate-600/20 text-slate-400">
                          <th class="py-2">Medicamento</th>
                          <th class="py-2">Dosagem</th>
                          <th class="py-2">Frequência</th>
                          <th class="py-2">Duração</th>
                          @if (!isSigned()) { <th class="py-2 text-right">Ação</th> }
                        </tr>
                      </thead>
                      <tbody>
                        @for (med of medicamentos; track $index) {
                          <tr class="border-b border-slate-600/10 text-slate-200">
                            <td class="py-2.5 font-bold">{{ med.nome }}</td>
                            <td class="py-2.5">{{ med.dosagem }}</td>
                            <td class="py-2.5">{{ med.frequencia }}</td>
                            <td class="py-2.5">{{ med.prazoUso }}</td>
                            @if (!isSigned()) {
                              <td class="py-2.5 text-right">
                                <button (click)="removerMedicamento($index)" class="text-triage-critical hover:text-triage-critical/80 transition-all font-bold">
                                  Excluir
                                </button>
                              </td>
                            }
                          </tr>
                        }
                      </tbody>
                    </table>
                  </div>
                }
              </div>

              <!-- Subform Adicionar Medicamento (se não assinado) -->
              @if (!isSigned()) {
                <div class="p-4 bg-black/20 border border-slate-600/10 rounded-xl flex flex-col gap-4">
                  <span class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Novo Medicamento</span>
                  
                  <div class="grid grid-cols-1 sm:grid-cols-4 gap-3">
                    <input type="text" [(ngModel)]="novoMed.nome" placeholder="Medicamento" class="form-input text-xs" />
                    <input type="text" [(ngModel)]="novoMed.dosagem" placeholder="Dosagem" class="form-input text-xs" />
                    <input type="text" [(ngModel)]="novoMed.frequencia" placeholder="Frequência" class="form-input text-xs" />
                    <input type="text" [(ngModel)]="novoMed.prazoUso" placeholder="Duração/Prazo" class="form-input text-xs" />
                  </div>

                  <div class="flex justify-end">
                    <button (click)="adicionarMedicamento()" [disabled]="!novoMed.nome" class="btn-secondary h-8 px-4 rounded-lg text-xs font-bold">
                      + Adicionar à Receita
                    </button>
                  </div>
                </div>
              }
            </div>

            <!-- Certificação Digital (Exibe se Assinado) -->
            @if (isSigned()) {
              <div class="p-6 bg-triage-low/5 border border-triage-low/30 rounded-2xl flex flex-col gap-3">
                <div class="flex items-center gap-2 text-triage-low text-sm font-bold">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                  <span>Consulta Assinada Digitalmente com Validade Jurídica</span>
                </div>
                <div class="text-xs text-slate-300 font-mono bg-black/30 p-3 rounded-lg break-all">
                  <span class="font-bold text-slate-400 block mb-1">Hash SHA-256 de Autenticidade:</span>
                  {{ diag.assinaturaHash }}
                </div>
                @if (diag.assinaturaBase64) {
                  <div class="mt-2 flex flex-col gap-1.5">
                    <span class="text-[9px] font-bold text-slate-400 font-mono uppercase tracking-wider">Assinatura do Médico:</span>
                    <div class="bg-white p-2 rounded-xl inline-block w-40 h-16 border border-slate-300 overflow-hidden">
                      <img [src]="diag.assinaturaBase64" alt="Assinatura" class="w-full h-full object-contain" />
                    </div>
                  </div>
                }
                <p class="text-[10px] text-slate-400 mt-1">Assinado em: {{ diag.assinadoEm | date:'dd/MM/yyyy HH:mm:ss' }} pelo médico ID {{ diag.medicoId }}.</p>
              </div>
            }

            <!-- Painel Ações/Rodapé -->
            <div class="flex flex-wrap items-center justify-between gap-4">
              <button (click)="goBack()" class="btn-secondary h-11 px-6 rounded-lg text-sm">
                Voltar ao Painel
              </button>

              <div class="flex items-center gap-3">
                @if (saveSuccess()) {
                  <span class="text-xs text-triage-low font-mono mr-2">{{ saveSuccess() }}</span>
                }
                @if (errorMsg()) {
                  <span class="text-xs text-triage-critical font-mono mr-2">{{ errorMsg() }}</span>
                }

                @if (!isSigned()) {
                  <button (click)="salvarRascunho()" [disabled]="savingDraft()"
                          class="btn-secondary h-11 px-5 rounded-lg text-sm">
                    @if (savingDraft()) { Salvando... } @else { Salvar Rascunho }
                  </button>
                  <button (click)="abrirPainelAssinatura()" [disabled]="signing() || !descricao"
                          class="btn-primary h-11 px-6 rounded-lg text-sm flex items-center gap-2">
                    @if (signing()) {
                      <span class="w-4 h-4 border-2 border-obsidian border-t-transparent rounded-full animate-spin"></span>
                      <span>Assinando...</span>
                    } @else {
                      <span>Assinar & Concluir</span>
                    }
                  </button>
                } @else {
                  <button (click)="chamarProximo()" [disabled]="callingNext()"
                          class="btn-secondary h-11 px-5 rounded-lg text-sm border border-cyber-teal/30 text-cyber-teal hover:bg-cyber-teal/10 flex items-center gap-2 transition-all">
                    @if (callingNext()) {
                      <span class="w-4 h-4 border-2 border-cyber-teal border-t-transparent rounded-full animate-spin"></span>
                      <span>Buscando próximo...</span>
                    } @else {
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
                      </svg>
                      <span>Chamar Próximo Paciente</span>
                    }
                  </button>

                  <button (click)="baixarPDF(diag.id)" [disabled]="downloadingPdf()"
                          class="btn-primary h-11 px-6 rounded-lg text-sm flex items-center gap-2">
                    @if (downloadingPdf()) {
                      <span class="w-4 h-4 border-2 border-obsidian border-t-transparent rounded-full animate-spin"></span>
                      <span>Baixando PDF...</span>
                    } @else {
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                      <span>Baixar Receita (PDF)</span>
                    }
                  </button>
                }
              </div>
            </div>
          } @else {
            <div class="text-center py-20 text-slate-500 italic">
              Selecione um paciente classificado na fila do painel principal para iniciar a consulta médica.
            </div>
          }

          <!-- Signature Canvas Modal Overlay -->
          @if (showSignaturePad()) {
            <div class="fixed inset-0 z-[1000] flex items-center justify-center bg-black/60 backdrop-blur-sm">
              <div class="glass-card max-w-md w-full p-6 rounded-2xl flex flex-col gap-4 border border-slate-600/30">
                <div>
                  <h3 class="text-base font-bold text-slate-100">Assinatura Eletrônica</h3>
                  <p class="text-xs text-slate-400 mt-1">Desenhe sua assinatura na área abaixo com o mouse ou tela de toque.</p>
                </div>
                
                <!-- Canvas Area -->
                <div class="bg-white rounded-xl overflow-hidden border border-slate-300 h-44 relative">
                  <canvas #sigCanvas class="w-full h-full cursor-crosshair bg-white"
                          (mousedown)="startDrawing($event)" 
                          (mousemove)="draw($event)" 
                          (mouseup)="stopDrawing()" 
                          (mouseleave)="stopDrawing()"
                          (touchstart)="startDrawingTouch($event)"
                          (touchmove)="drawTouch($event)"
                          (touchend)="stopDrawing()"></canvas>
                </div>

                <div class="flex justify-between items-center mt-2">
                  <button (click)="clearCanvas()" class="btn-secondary px-4 py-2 text-xs rounded-lg">
                    Limpar Desenho
                  </button>
                  <div class="flex gap-2">
                    <button (click)="showSignaturePad.set(false)" class="btn-secondary px-4 py-2 text-xs rounded-lg">
                      Cancelar
                    </button>
                    <button (click)="confirmarAssinatura()" class="btn-primary px-4 py-2 text-xs rounded-lg">
                      Confirmar Assinatura
                    </button>
                  </div>
                </div>
              </div>
            </div>
          }

        </div>
      </div>
    </div>
  `
})
export class Consultation implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly triageService = inject(TriageService);
  private readonly diagnosisService = inject(DiagnosisService);
  private readonly authService = inject(AuthService);

  readonly currentUser = this.authService.currentUser;

  @ViewChild('sigCanvas') sigCanvas!: ElementRef<HTMLCanvasElement>;
  showSignaturePad = signal(false);
  private ctx!: CanvasRenderingContext2D;
  private drawing = false;

  // View States
  loading = signal(false);
  savingDraft = signal(false);
  signing = signal(false);
  downloadingPdf = signal(false);
  saveSuccess = signal('');
  errorMsg = signal('');

  // Domain Objects
  triageId = signal<number | null>(null);
  triage = signal<TriageDetail | null>(null);
  paciente = signal<Paciente | null>(null);
  diagnostico = signal<DiagnosticoResponse | null>(null);

  // Form States
  descricao = '';
  medicamentos: MedicamentoPrescrito[] = [];
  
  // Subform inputs
  novoMed: Omit<MedicamentoPrescrito, 'id'> = {
    nome: '',
    dosagem: '',
    frequencia: '',
    prazoUso: ''
  };

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['triagemId']) {
        const tId = Number(params['triagemId']);
        this.triageId.set(tId);
        this.iniciarProntuario(tId);
      }
    });
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  iniciarProntuario(tId: number) {
    this.loading.set(true);
    this.errorMsg.set('');
    const medicoId = this.currentUser()?.usuarioId || 3; // Default Roberto ID

    // 1. Iniciar ou reaver diagnóstico
    this.diagnosisService.iniciar(tId, medicoId).subscribe({
      next: (diag) => {
        this.diagnostico.set(diag);
        this.descricao = diag.descricao || '';
        this.medicamentos = diag.medicamentos || [];

        // 2. Buscar detalhes da triagem associada
        this.triageService.buscarTriagemPorId(tId).subscribe({
          next: (t) => {
            this.triage.set(t);
            
            // 3. Buscar detalhes do paciente cadastrado
            this.triageService.buscarPacientePorId(t.pacienteId).subscribe({
              next: (p) => {
                this.paciente.set(p);
                this.loading.set(false);
              },
              error: (err) => {
                console.error('Erro ao buscar paciente:', err);
                this.paciente.set({ id: t.pacienteId, nome: `Paciente (ID: ${t.pacienteId})`, cpf: 'N/A' });
                this.loading.set(false);
              }
            });
          },
          error: (err) => {
            console.error('Erro ao buscar triagem:', err);
            this.errorMsg.set('Erro ao carregar os dados da triagem.');
            this.loading.set(false);
          }
        });
      },
      error: (err) => {
        console.error('Erro ao iniciar diagnóstico:', err);
        this.loading.set(false);
        this.errorMsg.set('Erro ao carregar prontuário. Apenas médicos podem acessar.');
      }
    });
  }

  isSigned(): boolean {
    return this.diagnostico()?.status === 'ASSINADO';
  }

  adicionarMedicamento() {
    if (!this.novoMed.nome) return;
    this.medicamentos.push({ ...this.novoMed });
    // Reset inputs
    this.novoMed = { nome: '', dosagem: '', frequencia: '', prazoUso: '' };
  }

  removerMedicamento(index: number) {
    this.medicamentos.splice(index, 1);
  }

  salvarRascunho() {
    const diag = this.diagnostico();
    if (!diag) return;

    this.savingDraft.set(true);
    this.saveSuccess.set('');
    this.errorMsg.set('');

    this.diagnosisService.salvar(diag.id, diag.medicoId, {
      descricao: this.descricao,
      medicamentos: this.medicamentos
    }).subscribe({
      next: (res) => {
        this.diagnostico.set(res);
        this.savingDraft.set(false);
        this.saveSuccess.set('Rascunho salvo!');
        setTimeout(() => this.saveSuccess.set(''), 3000);
      },
      error: () => {
        this.savingDraft.set(false);
        this.errorMsg.set('Erro ao salvar rascunho.');
      }
    });
  }

  abrirPainelAssinatura() {
    if (!this.descricao.trim()) {
      this.errorMsg.set('Insira a avaliação clínica antes de assinar.');
      return;
    }
    this.showSignaturePad.set(true);
    setTimeout(() => this.initCanvas(), 100);
  }

  private initCanvas() {
    const canvas = this.sigCanvas.nativeElement;
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;
    this.ctx = canvas.getContext('2d')!;
    this.ctx.strokeStyle = '#0f172a'; // Navy dark ink
    this.ctx.lineWidth = 3;
    this.ctx.lineCap = 'round';
  }

  startDrawing(e: MouseEvent) {
    this.drawing = true;
    const canvas = this.sigCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    this.ctx.beginPath();
    this.ctx.moveTo(e.clientX - rect.left, e.clientY - rect.top);
  }

  draw(e: MouseEvent) {
    if (!this.drawing) return;
    const canvas = this.sigCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    this.ctx.lineTo(e.clientX - rect.left, e.clientY - rect.top);
    this.ctx.stroke();
  }

  startDrawingTouch(e: TouchEvent) {
    e.preventDefault();
    this.drawing = true;
    const canvas = this.sigCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const touch = e.touches[0];
    this.ctx.beginPath();
    this.ctx.moveTo(touch.clientX - rect.left, touch.clientY - rect.top);
  }

  drawTouch(e: TouchEvent) {
    e.preventDefault();
    if (!this.drawing) return;
    const canvas = this.sigCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const touch = e.touches[0];
    this.ctx.lineTo(touch.clientX - rect.left, touch.clientY - rect.top);
    this.ctx.stroke();
  }

  stopDrawing() {
    this.drawing = false;
  }

  clearCanvas() {
    const canvas = this.sigCanvas.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
  }

  confirmarAssinatura() {
    const canvas = this.sigCanvas.nativeElement;
    const base64Data = canvas.toDataURL('image/png');
    this.showSignaturePad.set(false);
    this.assinarDiagnostico(base64Data);
  }

  assinarDiagnostico(signatureData: string) {
    const diag = this.diagnostico();
    if (!diag) return;

    this.signing.set(true);
    this.errorMsg.set('');

    // First save the current state, then sign it
    this.diagnosisService.salvar(diag.id, diag.medicoId, {
      descricao: this.descricao,
      medicamentos: this.medicamentos
    }).subscribe({
      next: () => {
        this.diagnosisService.assinar(diag.id, diag.medicoId, signatureData).subscribe({
          next: (signedDiag) => {
            this.diagnostico.set(signedDiag);
            this.signing.set(false);
          },
          error: (err) => {
            this.signing.set(false);
            this.errorMsg.set('Erro ao assinar diagnóstico.');
          }
        });
      },
      error: () => {
        this.signing.set(false);
        this.errorMsg.set('Erro ao salvar prontuário antes da assinatura.');
      }
    });
  }

  baixarPDF(diagnosticoId: number) {
    this.downloadingPdf.set(true);
    this.diagnosisService.baixarPdf(diagnosticoId).subscribe({
      next: (blob) => {
        this.downloadingPdf.set(false);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `receituario-${diagnosticoId}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: () => {
        this.downloadingPdf.set(false);
        this.errorMsg.set('Erro ao gerar documento PDF.');
      }
    });
  }

  salaAtendimento = localStorage.getItem('medico_sala') || 'Consultório 1';
  callingNext = signal(false);

  saveSala() {
    localStorage.setItem('medico_sala', this.salaAtendimento);
  }

  chamarPacienteAtual() {
    const t = this.triage();
    if (!t) return;
    const sala = this.salaAtendimento;
    this.triageService.chamar(t.id, sala).subscribe({
      next: (res) => {
        this.triage.update(current => current ? { ...current, chamado: true, salaChamada: sala } : null);
        this.saveSuccess.set('Chamada enviada ao mural!');
        setTimeout(() => this.saveSuccess.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set('Erro ao enviar chamada ao mural.');
      }
    });
  }

  chamarProximo() {
    this.callingNext.set(true);
    this.errorMsg.set('');

    this.triageService.listarTriagens().subscribe({
      next: (data) => {
        const waitingForDoctor = data.filter(t => t.status === 'EM_ATENDIMENTO' && !t.chamado);
        
        if (waitingForDoctor.length === 0) {
          this.callingNext.set(false);
          this.errorMsg.set('Não há nenhum paciente aguardando atendimento na fila.');
          setTimeout(() => this.errorMsg.set(''), 4000);
          return;
        }

        const order: Record<string, number> = {
          'VERMELHO': 1,
          'LARANJA': 2,
          'AMARELO': 3,
          'VERDE': 4
        };

        const sorted = [...waitingForDoctor].sort((a, b) => {
          const aVal = order[a.prioridade || ''] || 5;
          const bVal = order[b.prioridade || ''] || 5;
          if (aVal !== bVal) return aVal - bVal;
          return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
        });

        const nextTriage = sorted[0];
        const sala = this.salaAtendimento;

        this.triageService.chamar(nextTriage.id, sala).subscribe({
          next: () => {
            this.callingNext.set(false);
            this.router.navigate(['/atendimento'], { queryParams: { triagemId: nextTriage.id } });
            this.iniciarProntuario(nextTriage.id);
          },
          error: (err) => {
            console.error('Erro ao chamar o próximo paciente:', err);
            this.callingNext.set(false);
            this.errorMsg.set('Erro ao chamar o próximo paciente na fila.');
          }
        });
      },
      error: (err) => {
        console.error('Erro ao listar triagens:', err);
        this.callingNext.set(false);
        this.errorMsg.set('Erro ao buscar a fila de pacientes.');
      }
    });
  }
}
