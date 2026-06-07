package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.AgendamentoPublicoRequest;
import br.com.calixto.salao.dto.request.AtendimentoRequest;
import br.com.calixto.salao.dto.response.AgendamentoRealizadoResponse;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import br.com.calixto.salao.dto.response.DisponibilidadeResponse;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public interface IAtendimentoService {
    AtendimentoResponse salvar(AtendimentoRequest atendimentoRequest);
    List<AtendimentoResponse> listarTodos();
    AtendimentoResponse buscarPorId(Integer id);
    AtendimentoResponse atualizar(Integer id, @Valid AtendimentoRequest request);
    void deletar(Integer id);

    // Métodos públicos (sem autenticação)
    DisponibilidadeResponse consultarDisponibilidade(Integer profissionalId, String servicoNome, LocalDate data);
    AgendamentoRealizadoResponse criarAgendamentoPublico(AgendamentoPublicoRequest request);
}