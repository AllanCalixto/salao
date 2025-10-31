package br.com.calixto.salao.dto.response;

import br.com.calixto.salao.model.Especialidade;

import java.util.List;

public record ProfissionalResponse(
        Integer id,
        String nome,
        Especialidade especialidade,
        List<String> servicos
) {
}
