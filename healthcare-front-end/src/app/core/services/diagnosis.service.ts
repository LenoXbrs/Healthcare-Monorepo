import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MedicamentoPrescrito {
  id?: number;
  nome: string;
  dosagem: string;
  frequencia: string;
  prazoUso: string;
}

export interface DiagnosticoResponse {
  id: number;
  triagemId: number;
  medicoId: number;
  descricao?: string;
  status: string; // EM_ANDAMENTO, ASSINADO
  assinaturaHash?: string;
  assinaturaBase64?: string;
  assinadoEm?: string;
  medicamentos?: MedicamentoPrescrito[];
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class DiagnosisService {
  private readonly http = inject(HttpClient);
  private readonly gatewayUrl = 'http://localhost:30080/diagnosticos';

  iniciar(triagemId: number, medicoId: number): Observable<DiagnosticoResponse> {
    return this.http.post<DiagnosticoResponse>(`${this.gatewayUrl}?triagemId=${triagemId}&medicoId=${medicoId}`, {});
  }

  salvar(id: number, medicoId: number, payload: {
    descricao: string;
    medicamentos: Omit<MedicamentoPrescrito, 'id'>[];
  }): Observable<DiagnosticoResponse> {
    return this.http.put<DiagnosticoResponse>(`${this.gatewayUrl}/${id}?medicoId=${medicoId}`, payload);
  }

  assinar(id: number, medicoId: number, assinaturaBase64: string): Observable<DiagnosticoResponse> {
    return this.http.post<DiagnosticoResponse>(`${this.gatewayUrl}/${id}/assinar?medicoId=${medicoId}`, { assinaturaBase64 });
  }

  baixarPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.gatewayUrl}/${id}/pdf`, { responseType: 'blob' });
  }
}
