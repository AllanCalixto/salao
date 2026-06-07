package br.com.calixto.salao.dto.response;

import java.time.LocalTime;

public record DisponibilidadeProfissionalResponse(
        Integer id,
        Integer diaSemana,
        String diaSemanaLabel,
        LocalTime horaInicio,
        LocalTime horaFim,
        Boolean ativo
) {}