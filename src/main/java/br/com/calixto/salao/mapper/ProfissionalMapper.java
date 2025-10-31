package br.com.calixto.salao.mapper;

import br.com.calixto.salao.dto.request.ProfissionalRequest;
import br.com.calixto.salao.dto.response.ProfissionalResponse;
import br.com.calixto.salao.model.Profissional;
import org.springframework.stereotype.Component;

@Component
public class ProfissionalMapper {
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

        return new ProfissionalResponse(
                profissional.getId(),
                profissional.getNome(),
                profissional.getEspecialidade(),
                profissional.getServicos()
        );

    }

    public ProfissionalRequest toDtoRequest(Profissional profissional) {
        if (profissional == null) {
            return null;
        }

        return new ProfissionalRequest(
                profissional.getNome(),
                profissional.getEspecialidade(),
                profissional.getServicos()
        );
    }
}
