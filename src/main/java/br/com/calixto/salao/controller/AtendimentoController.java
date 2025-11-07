package br.com.calixto.salao.controller;

import br.com.calixto.salao.dto.request.AtendimentoRequest;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import br.com.calixto.salao.service.IAtendimentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/atendimentos")
@SecurityRequirement(name = "bearer-key")
public class AtendimentoController {

    private final IAtendimentoService atendimentoService;

    public AtendimentoController(IAtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    // Criar um atendimento
    @PostMapping
    @Transactional
    public ResponseEntity<AtendimentoResponse> criarAtendimento(
            @RequestBody @Valid AtendimentoRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        AtendimentoResponse atendimentoSalvo = atendimentoService.salvar(request);
        var uri = uriBuilder.path("/atendimentos/{id}")
                .buildAndExpand(atendimentoSalvo.id())
                .toUri();
        return ResponseEntity.created(uri).body(atendimentoSalvo);
    }

    // Listar todos os atendimentos
    @GetMapping
    public ResponseEntity<List<AtendimentoResponse>> listarTodos() {
        List<AtendimentoResponse> atendimentos = atendimentoService.listarTodos();
        return ResponseEntity.ok(atendimentos);
    }

    // Buscar atendimento por ID
    @GetMapping("/{id}")
    public ResponseEntity<AtendimentoResponse> buscarPorId(@PathVariable Integer id) {
        AtendimentoResponse atendimento = atendimentoService.buscarPorId(id);
        return ResponseEntity.ok(atendimento);
    }

    // Atualizar atendimento
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<AtendimentoResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid AtendimentoRequest request
    ) {
        AtendimentoResponse atualizado = atendimentoService.atualizar(id, request);
        return ResponseEntity.ok(atualizado);
    }

    // Deletar atendimento
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        atendimentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}