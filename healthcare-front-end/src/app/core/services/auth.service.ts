import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

export interface UserSession {
  token: string;
  usuarioId: number;
  nome: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiPrefix = 'http://localhost:30080/usuarios';

  readonly currentUser = signal<UserSession | null>(null);

  constructor() {
    const token = localStorage.getItem('token');
    const usuarioIdStr = localStorage.getItem('usuarioId');
    const nome = localStorage.getItem('nome');
    const role = localStorage.getItem('role');

    if (token && usuarioIdStr && nome && role) {
      this.currentUser.set({
        token,
        usuarioId: Number(usuarioIdStr),
        nome,
        role
      });
    }
  }

  login(email: string, senha: string) {
    return this.http.post<UserSession>(`${this.apiPrefix}/login`, { email, senha }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('usuarioId', String(res.usuarioId));
        localStorage.setItem('nome', res.nome);
        localStorage.setItem('role', res.role);
        this.currentUser.set(res);
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('nome');
    localStorage.removeItem('role');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  hasRole(allowedRoles: string[]): boolean {
    const user = this.currentUser();
    return !!user && allowedRoles.includes(user.role);
  }

  buscarUsuarioPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiPrefix}/${id}`);
  }
}
