package br.com.calixto.salao.dto.response;

public record LoginResponse(String token, Long expiresIn) {
}
