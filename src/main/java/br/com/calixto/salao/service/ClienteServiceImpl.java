package br.com.calixto.salao.service;

import java.util.List;

import br.com.calixto.salao.exception.ClienteJaCadastradoException;
import br.com.calixto.salao.exception.ClienteNaoEncontradoException;
import br.com.calixto.salao.mapper.ClienteMapper;
import br.com.calixto.salao.model.Cliente;
import br.com.calixto.salao.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import br.com.calixto.salao.dto.request.ClienteRequest;
import br.com.calixto.salao.dto.response.ClienteResponse;

@Service
public class ClienteServiceImpl implements IClienteService{

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper){
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Override
    public ClienteResponse salvar(ClienteRequest clienteRequest) {
        boolean nomeJaExiste = clienteRepository.findByNome(clienteRequest.nome()).isPresent();
        if (nomeJaExiste) {
            throw new ClienteJaCadastradoException(
                    "Cliente com o nome: " + clienteRequest.nome() + " já foi cadastrado!"
            );
        }
        Cliente cliente = clienteMapper.toEntity(clienteRequest);
        Cliente clienteSalvo = clienteRepository.save(cliente);
        return clienteMapper.toDtoResponse(clienteSalvo);
    }

	@Override
	public List<ClienteResponse> listarTodosClientes() {
		return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toDtoResponse)
                .toList();
	}

	@Override
	public ClienteResponse findByExistsNome(String nome) {
        Cliente cliente = clienteRepository.findByNome(nome).orElseThrow(()-> new ClienteNaoEncontradoException("Cliente com o nome: "+nome+ " não foi encontrado!"));
        return clienteMapper.toDtoResponse(cliente);
	}

	@Override
	public ClienteResponse findByExistsTelefone(String telefone) {
        Cliente cliente = clienteRepository.findByTelefone(telefone).orElseThrow(() -> new ClienteNaoEncontradoException("Cliente com o telefone: "+telefone+ " não foi encontrado!"));
        return clienteMapper.toDtoResponse(cliente);
	}

	@Override
	public ClienteResponse findById(Integer id) {
        var cliente = clienteRepository.findById(id).orElseThrow(() -> new ClienteNaoEncontradoException("Cliente com ID: "+id + "não foi encontrado!"));
        return clienteMapper.toDtoResponse(cliente);
	}

    @Override
    public ClienteResponse atualizar(Integer id, ClienteRequest clienteRequest) {
        Cliente clienteExistente = clienteRepository.findById(id).orElseThrow(() -> new ClienteNaoEncontradoException("Cliente com ID: "+id+ " não foi encontrado!"));
        var nomeJaExiste = clienteRepository.findByNome(clienteRequest.nome());
        if (!nomeJaExiste.isEmpty()){
            throw new ClienteJaCadastradoException("Já existe outro cliente com o nome: "+clienteRequest.nome());
        }
        clienteExistente.setNome(clienteRequest.nome());
        clienteExistente.setTelefone(clienteRequest.telefone());
        Cliente clienteAtualizado = clienteRepository.save(clienteExistente);
        return clienteMapper.toDtoResponse(clienteAtualizado);
    }
}
