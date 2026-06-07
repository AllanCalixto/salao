package br.com.calixto.salao.dto.response;

import java.util.List;

public record DisponibilidadeResponse(
        Integer profissionalId,
        String profissionalNome,
        String servico,
        Integer duracaoMinutos,
        String data,
        List<HorarioDisponivel> horariosDisponiveis
) {}