package br.com.calixto.salao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(ClienteJaCadastradoException.class)
    public ResponseEntity<String> handleClienteJaCadastradoException(ClienteJaCadastradoException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<String> handleClienteNaoEncontradoException(ClienteNaoEncontradoException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
