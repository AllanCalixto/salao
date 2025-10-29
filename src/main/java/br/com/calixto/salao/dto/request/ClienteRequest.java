package br.com.calixto.salao.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank
        String nome,
        @NotBlank
        String telefone) {

}
