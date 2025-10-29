package br.com.calixto.salao.mapper;

import br.com.calixto.salao.dto.request.ClienteRequest;
import br.com.calixto.salao.dto.response.ClienteResponse;
import br.com.calixto.salao.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    public Cliente toEntity(ClienteRequest clienteRequest){
        if (clienteRequest == null) {
            return null;
        }
        Cliente cliente = new Cliente();
        cliente.setNome(clienteRequest.nome());
        cliente.setTelefone(clienteRequest.telefone());
        return cliente;
    }

    public ClienteRequest toDtoRequest(Cliente cliente){
        if (cliente == null){
            return null;
        }
        return new ClienteRequest(cliente.getNome(), cliente.getTelefone());
    }

    public ClienteResponse toDtoResponse(Cliente cliente){
        if (cliente == null){
            return null;
        }
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getTelefone());
    }
}
