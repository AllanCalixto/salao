import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AtendimentoRequest, AtendimentoResponse } from '../models';

@Injectable({
  providedIn: 'root',
})
export class AtendimentoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/atendimentos';

  listarTodos(): Observable<AtendimentoResponse[]> {
    return this.http.get<AtendimentoResponse[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<AtendimentoResponse> {
    return this.http.get<AtendimentoResponse>(`${this.apiUrl}/${id}`);
  }

  cadastrar(request: AtendimentoRequest): Observable<AtendimentoResponse> {
    return this.http.post<AtendimentoResponse>(this.apiUrl, request);
  }

  atualizar(id: number, request: AtendimentoRequest): Observable<AtendimentoResponse> {
    return this.http.put<AtendimentoResponse>(`${this.apiUrl}/${id}`, request);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}