package br.com.calixto.salao.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AtendimentoRequest(
        @NotNull Integer clienteId,
        @NotNull Integer profissionalId,
        @NotNull String servicoEscolhido,
        @NotNull Double preco,
        @NotNull LocalDateTime dataAtendimento
) {}
