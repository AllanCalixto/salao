package br.com.calixto.salao.dto.response;

import java.time.LocalDateTime;

public record AtendimentoResponse(
        Integer id,
        String clienteNome,
        String profissionalNome,
        String especialidade,
        String servicoEscolhido,
        Double preco,
        LocalDateTime dataAtendimento
) {}
