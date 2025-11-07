package br.com.calixto.salao.controller;

import br.com.calixto.salao.exception.ClienteJaCadastradoException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.calixto.salao.dto.request.ClienteRequest;
import br.com.calixto.salao.dto.response.ClienteResponse;
import br.com.calixto.salao.service.IClienteService;
import jakarta.transaction.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@SecurityRequirement(name = "bearer-key")
public class ClienteController {
	
	private final IClienteService iClienteService;
	
	public ClienteController(IClienteService iClienteService) {
		this.iClienteService = iClienteService;
	}
	
	@PostMapping
	@Transactional
	public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest clienteRequest, UriComponentsBuilder uriComponentsBuilder) {
		var cliente = iClienteService.salvar(clienteRequest);
        var uri = uriComponentsBuilder.path("/clientes/{id}").buildAndExpand(cliente.id()).toUri();
        return ResponseEntity.created(uri).body(cliente);
	}

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos(){
        return ResponseEntity.ok(iClienteService.listarTodosClientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> findById(@PathVariable Integer id){
        return ResponseEntity.ok(iClienteService.findById(id));
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<ClienteResponse> buscarPorNome(String nome){
        var cliente = iClienteService.findByExistsNome(nome);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/telefone/{telefone}")
    public ResponseEntity<ClienteResponse> buscarPortelefone(@PathVariable String telefone){
        var cliente = iClienteService.findByExistsTelefone(telefone);
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Integer id, @RequestBody @Valid ClienteRequest clienteRequest){
        var cliente = iClienteService.atualizar(id, clienteRequest);
        return ResponseEntity.ok(cliente);
    }
}
