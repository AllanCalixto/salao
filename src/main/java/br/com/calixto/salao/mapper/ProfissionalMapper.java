package br.com.calixto.salao.mapper;

import br.com.calixto.salao.dto.request.ProfissionalRequest;
import br.com.calixto.salao.dto.response.DisponibilidadeProfissionalResponse;
import br.com.calixto.salao.dto.response.ProfissionalResponse;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.model.ProfissionalDisponibilidade;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProfissionalMapper {

    private static final String[] DIAS_SEMANA = {
            "DOMINGO", "SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO"
    };

    public Profissional toEntity(ProfissionalRequest profissionalRequest) {
        if (profissionalRequest == null) {
            return null;
        }

        Profissional profissional = new Profissional();
        profissional.setNome(profissionalRequest.nome());
        profissional.setEspecialidade(profissionalRequest.especialidade());
        profissional.setServicos(profissionalRequest.servicos());
        return profissional;
    }

    public ProfissionalResponse toResponse(Profissional profissional) {
        if (profissional == null) {
            return null;
        }

        List<DisponibilidadeProfissionalResponse> dispResponse = Collections.emptyList();
        if (profissional.getDisponibilidades() != null) {
            dispResponse = profissional.getDisponibilidades()
                    .stream()
                    .filter(d -> d.getAtivo())
                    .map(d -> new DisponibilidadeProfissionalResponse(
                            d.getId(),
                            d.getDiaSemana(),
                            DIAS_SEMANA[d.getDiaSemana()],
                            d.getHoraInicio(),
                            d.getHoraFim(),
                            d.getAtivo()))
                    .toList();
        }

        return new ProfissionalResponse(
                profissional.getId(),
                profissional.getNome(),
                profissional.getEspecialidade(),
                profissional.getServicos(),
                dispResponse
        );
    }

    public ProfissionalRequest toDtoRequest(Profissional profissional) {
        if (profissional == null) {
            return null;
        }

        return new ProfissionalRequest(
                profissional.getNome(),
                profissional.getEspecialidade(),
                profissional.getServicos(),
                new ArrayList<>()
        );
    }
}