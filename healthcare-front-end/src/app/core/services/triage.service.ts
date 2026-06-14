import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface SinaisVitais {
  pressaoSist: number;
  pressaoDiast: number;
  temperatura: number;
  frequencia: number;
  saturacao: number;
  coletadoEm?: string;
}

export interface TriageItem {
  id: number;
  pacienteId: number;
  status: string;
  prioridade?: string;
  createdAt: string;
  chamado?: boolean;
  salaChamada?: string;
  chamadoEm?: string;
}

export interface TriageDetail {
  id: number;
  pacienteId: number;
  enfermeiroId?: number;
  medicoId?: number;
  status: string;
  prioridade?: string;
  queixaPrincipal?: string;
  createdAt: string;
  classificadoEm?: string;
  sinaisVitais?: SinaisVitais;
  chamado?: boolean;
  salaChamada?: string;
  chamadoEm?: string;
}

export interface Paciente {
  id: number;
  nome: string;
  cpf: string;
  telefone?: string;
  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TriageService {
  private readonly http = inject(HttpClient);
  private readonly gatewayUrl = 'http://localhost:30080';

  // Patient endpoints
  buscarPacientePorId(id: number): Observable<Paciente> {
    return this.http.get<Paciente>(`${this.gatewayUrl}/pacientes/${id}`);
  }

  buscarPacientePorCpf(cpf: string): Observable<Paciente> {
    return this.http.get<Paciente>(`${this.gatewayUrl}/pacientes/cpf/${cpf}`);
  }

  criarPaciente(paciente: Omit<Paciente, 'id'>): Observable<Paciente> {
    return this.http.post<Paciente>(`${this.gatewayUrl}/pacientes`, paciente);
  }

  // Triage endpoints
  listarTriagens(): Observable<TriageItem[]> {
    return this.http.get<any[]>(`${this.gatewayUrl}/triagem`).pipe(
      map(items => items.map(item => ({
        ...item,
        createdAt: item.criadoEm || item.createdAt
      })))
    );
  }

  buscarTriagemPorId(id: number): Observable<TriageDetail> {
    return this.http.get<any>(`${this.gatewayUrl}/triagem/${id}`).pipe(
      map(detail => ({
        ...detail,
        createdAt: detail.criadoEm || detail.createdAt
      }))
    );
  }

  checkIn(pacienteId: number): Observable<TriageItem> {
    return this.http.post<any>(`${this.gatewayUrl}/triagem/check-in?pacienteId=${pacienteId}`, {}).pipe(
      map(item => ({
        ...item,
        createdAt: item.criadoEm || item.createdAt
      }))
    );
  }

  abrir(pacienteId: number, enfermeiroId: number, queixaPrincipal: string): Observable<any> {
    return this.http.post<any>(`${this.gatewayUrl}/triagem`, {
      pacienteId,
      enfermeiroId,
      queixaPrincipal
    });
  }

  classificar(id: number, payload: {
    medicoId?: number | null;
    prioridade: string;
    sinaisVitais: SinaisVitais;
  }): Observable<any> {
    return this.http.post<any>(`${this.gatewayUrl}/triagem/${id}/classificar`, payload);
  }

  finalizar(id: number, observacoes: string): Observable<any> {
    return this.http.put<any>(`${this.gatewayUrl}/triagem/${id}/finalizar`, { observacoes });
  }

  chamar(id: number, sala: string): Observable<TriageDetail> {
    return this.http.post<TriageDetail>(`${this.gatewayUrl}/triagem/${id}/chamar?sala=${encodeURIComponent(sala)}`, {});
  }
}
