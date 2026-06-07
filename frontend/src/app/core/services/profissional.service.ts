import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProfissionalRequest, ProfissionalResponse } from '../models';

@Injectable({
  providedIn: 'root',
})
export class ProfissionalService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/profissionais';

  listarTodos(): Observable<ProfissionalResponse[]> {
    return this.http.get<ProfissionalResponse[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<ProfissionalResponse> {
    return this.http.get<ProfissionalResponse>(`${this.apiUrl}/${id}`);
  }

  cadastrar(request: ProfissionalRequest): Observable<ProfissionalResponse> {
    return this.http.post<ProfissionalResponse>(this.apiUrl, request);
  }

  atualizar(id: number, request: ProfissionalRequest): Observable<ProfissionalResponse> {
    return this.http.put<ProfissionalResponse>(`${this.apiUrl}/${id}`, request);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}