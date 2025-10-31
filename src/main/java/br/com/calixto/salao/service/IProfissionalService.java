package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.ProfissionalRequest;
import br.com.calixto.salao.dto.response.ProfissionalResponse;

import java.util.List;

public interface IProfissionalService {
    ProfissionalResponse salvar( ProfissionalRequest profissionalRequest);

    List<ProfissionalResponse> listarTodos();

    ProfissionalResponse buscarPorId(Integer id);

    ProfissionalResponse atualizar(Integer id, ProfissionalRequest profissionalRequest);

    void deletar(Integer id);
}
