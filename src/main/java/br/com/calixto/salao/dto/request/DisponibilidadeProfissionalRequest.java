package br.com.calixto.salao.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record DisponibilidadeProfissionalRequest(
        @NotNull Integer profissionalId,
        @NotNull Integer diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFim,
        @NotNull Boolean ativo
) {}