package br.com.calixto.salao.dto.response;

import java.time.LocalDateTime;

public record AgendamentoRealizadoResponse(
        Integer id,
        String nomeCliente,
        String profissionalNome,
        String servicoNome,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String status
) {}