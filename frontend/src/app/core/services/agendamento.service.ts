import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  DisponibilidadeResponse,
  DisponibilidadeProfissionalResponse,
  AgendamentoPublicoRequest,
  AgendamentoRealizadoResponse,
  ProfissionalResponse,
  ServicoResponse,
} from '../models';

@Injectable({
  providedIn: 'root',
})
export class AgendamentoService {
  private readonly http = inject(HttpClient);

  listarProfissionais(): Observable<ProfissionalResponse[]> {
    return this.http.get<ProfissionalResponse[]>('/profissionais');
  }

  listarServicos(): Observable<ServicoResponse[]> {
    return this.http.get<ServicoResponse[]>('/servicos');
  }

  consultarDisponibilidade(
    profissionalId: number,
    servico: string,
    data: string
  ): Observable<DisponibilidadeResponse> {
    return this.http.get<DisponibilidadeResponse>(
      `/disponibilidade?profissionalId=${profissionalId}&servico=${encodeURIComponent(servico)}&data=${data}`
    );
  }

  criarAgendamento(
    request: AgendamentoPublicoRequest
  ): Observable<AgendamentoRealizadoResponse> {
    return this.http.post<AgendamentoRealizadoResponse>(
      '/agendamento/publico',
      request
    );
  }

  listarDisponibilidadeProfissional(
    profissionalId: number
  ): Observable<DisponibilidadeProfissionalResponse[]> {
    return this.http.get<DisponibilidadeProfissionalResponse[]>(
      `/profissionais/${profissionalId}/disponibilidade`
    );
  }
}
