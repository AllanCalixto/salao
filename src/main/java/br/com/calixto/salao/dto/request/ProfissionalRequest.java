package br.com.calixto.salao.dto.request;

import br.com.calixto.salao.model.Especialidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProfissionalRequest(
        @NotBlank(message = "O nome do profissional é obrigatório.")
        String nome,

        @NotNull(message = "A especialidade é obrigatória.")
        Especialidade especialidade,

        @NotEmpty(message = "O profissional deve ter pelo menos um serviço.")
        List<String> servicos

) {
}
