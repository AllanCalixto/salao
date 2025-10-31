package br.com.calixto.salao.controller;

import br.com.calixto.salao.dto.request.ProfissionalRequest;
import br.com.calixto.salao.dto.response.ProfissionalResponse;
import br.com.calixto.salao.service.IProfissionalService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final IProfissionalService iProfissionalService;

    public ProfissionalController(IProfissionalService iProfissionalService) {
        this.iProfissionalService = iProfissionalService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProfissionalResponse> cadastrar(
            @RequestBody @Valid ProfissionalRequest profissionalRequest,
            UriComponentsBuilder uriBuilder) {

        ProfissionalResponse profissional = iProfissionalService.salvar(profissionalRequest);
        var uri = uriBuilder.path("/profissionais/{id}")
                .buildAndExpand(profissional.id())
                .toUri();

        return ResponseEntity.created(uri).body(profissional);
    }

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listarTodos() {
        List<ProfissionalResponse> profissionais = iProfissionalService.listarTodos();
        return ResponseEntity.ok(profissionais);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> buscarPorId(@PathVariable Integer id) {
        ProfissionalResponse profissional = iProfissionalService.buscarPorId(id);
        return ResponseEntity.ok(profissional);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ProfissionalResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid ProfissionalRequest profissionalRequest) {

        ProfissionalResponse atualizado = iProfissionalService.atualizar(id, profissionalRequest);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        iProfissionalService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

