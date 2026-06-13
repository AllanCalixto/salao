export interface LoginRequest {
  login: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
}

export interface ClienteRequest {
  nome: string;
  telefone: string;
}

export interface ClienteResponse {
  id: number;
  nome: string;
  telefone: string;
}

export enum Especialidade {
  MANICURE = 'MANICURE',
  CABELEREIRA = 'CABELEREIRA',
  DEPILADORA = 'DEPILADORA',
}

export const ESPECIALIDADE_LABELS: Record<Especialidade, string> = {
  [Especialidade.MANICURE]: 'Manicure',
  [Especialidade.CABELEREIRA]: 'Cabeleireira',
  [Especialidade.DEPILADORA]: 'Depiladora',
};

export interface DisponibilidadeItemRequest {
  diaSemana: number;
  horaInicio: string;
  horaFim: string;
}

export interface ProfissionalRequest {
  nome: string;
  especialidade: Especialidade;
  servicos: string[];
  disponibilidade?: DisponibilidadeItemRequest[];
}

export interface ProfissionalResponse {
  id: number;
  nome: string;
  especialidade: Especialidade;
  servicos: string[];
  disponibilidade?: DisponibilidadeProfissionalResponse[];
}

export interface AtendimentoRequest {
  clienteId: number;
  profissionalId: number;
  servicoEscolhido: string;
  preco: number;
  dataAtendimento: string;
  dataFim?: string;
}

export interface AtendimentoResponse {
  id: number;
  clienteNome: string;
  profissionalNome: string;
  especialidade: string;
  servicoEscolhido: string;
  preco: number;
  dataAtendimento: string;
  dataFim: string;
  status: string;
}

export interface DisponibilidadeResponse {
  profissionalId: number;
  profissionalNome: string;
  servico: string;
  duracaoMinutos: number;
  data: string;
  horariosDisponiveis: HorarioDisponivel[];
}

export interface HorarioDisponivel {
  horario: string;
  horarioFormatado: string;
}

export interface AgendamentoPublicoRequest {
  nomeCliente: string;
  telefoneCliente: string;
  profissionalId: number;
  servicoId: number;
  servicoNome: string;
  data: string;
  horario: string;
}

export interface AgendamentoRealizadoResponse {
  id: number;
  nomeCliente: string;
  profissionalNome: string;
  servicoNome: string;
  dataInicio: string;
  dataFim: string;
  status: string;
}

export interface ServicoResponse {
  id: number;
  nome: string;
  descricao: string;
  duracaoMinutos: number;
  preco: number;
}

export interface Pageable {
  page: number;
  size: number;
  sort?: string;
}

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp?: string;
}

export interface DisponibilidadeProfissionalResponse {
  id: number;
  diaSemana: number;
  diaSemanaLabel: string;
  horaInicio: string;
  horaFim: string;
  ativo: boolean;
}
