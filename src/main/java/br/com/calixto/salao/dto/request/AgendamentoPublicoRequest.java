package br.com.calixto.salao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoPublicoRequest(
        @NotBlank String nomeCliente,
        @NotBlank String telefoneCliente,
        @NotNull Integer profissionalId,
        @NotNull Integer servicoId,
        @NotBlank String servicoNome,
        @NotNull LocalDate data,
        @NotNull LocalTime horario
) {}
