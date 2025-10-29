package br.com.calixto.salao.exception;

public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(String message) {
        super(message);
    }
}
