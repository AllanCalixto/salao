package br.com.calixto.salao.dto.response;

public record ServicoResponse(
        Integer id,
        String nome,
        String descricao,
        Integer duracaoMinutos,
        Double preco
) {}