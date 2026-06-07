package br.com.calixto.salao.dto.response;

import java.time.LocalTime;

public record HorarioDisponivel(
        LocalTime horario,
        String horarioFormatado
) {}