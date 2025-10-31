package br.com.calixto.salao.exception;

import jakarta.validation.constraints.NotBlank;

public class ProfissionalJaCadastradoException extends RuntimeException {
    public ProfissionalJaCadastradoException(String message) {
        super(message);
    }
}
