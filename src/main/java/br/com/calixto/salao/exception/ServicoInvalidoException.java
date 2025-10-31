package br.com.calixto.salao.exception;

import jakarta.validation.constraints.NotNull;

public class ServicoInvalidoException extends RuntimeException {
    public ServicoInvalidoException(String message) {
        super(message);
    }
}
