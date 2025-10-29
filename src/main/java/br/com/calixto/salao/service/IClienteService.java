package br.com.calixto.salao.service;

import java.util.List;

import br.com.calixto.salao.dto.request.ClienteRequest;
import br.com.calixto.salao.dto.response.ClienteResponse;

public interface IClienteService {
	ClienteResponse salvar(ClienteRequest clienteRequest);
	List<ClienteResponse> listarTodosClientes();
	ClienteResponse findByExistsNome(String nome);
	ClienteResponse findByExistsTelefone(String telefone);
	ClienteResponse findById(Integer id);
	ClienteResponse atualizar(Integer id, ClienteRequest clienteRequest);
	

}
