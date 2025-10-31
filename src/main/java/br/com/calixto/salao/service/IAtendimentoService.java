package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.AtendimentoRequest;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface IAtendimentoService {
    AtendimentoResponse salvar(AtendimentoRequest atendimentoRequest);
    List<AtendimentoResponse> listarTodos();

    AtendimentoResponse buscarPorId(Integer id);

    AtendimentoResponse atualizar(Integer id, @Valid AtendimentoRequest request);

    void deletar(Integer id);
}
