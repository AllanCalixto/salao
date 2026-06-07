package br.com.calixto.salao.mapper;

import br.com.calixto.salao.dto.response.AgendamentoRealizadoResponse;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import br.com.calixto.salao.model.Atendimento;
import org.springframework.stereotype.Component;

@Component
public class AtendimentoMapper {

    public AtendimentoResponse toDtoResponse(Atendimento atendimento){
        if (atendimento == null) {
            return null;
        }
        return new AtendimentoResponse(
                atendimento.getId(),
                atendimento.getCliente().getNome(),
                atendimento.getProfissional().getNome(),
                atendimento.getProfissional().getEspecialidade().name(),
                atendimento.getServicoEscolhido(),
                atendimento.getPreco(),
                atendimento.getDataAtendimento(),
                atendimento.getDataFim(),
                atendimento.getStatus()
        );
    }

    public AgendamentoRealizadoResponse toAgendamentoRealizadoResponse(Atendimento atendimento) {
        if (atendimento == null) return null;
        return new AgendamentoRealizadoResponse(
                atendimento.getId(),
                atendimento.getCliente().getNome(),
                atendimento.getProfissional().getNome(),
                atendimento.getServicoEscolhido(),
                atendimento.getDataAtendimento(),
                atendimento.getDataFim(),
                atendimento.getStatus()
        );
    }
}