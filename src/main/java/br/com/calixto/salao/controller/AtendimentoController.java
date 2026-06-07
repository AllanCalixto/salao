package br.com.calixto.salao.controller;

import br.com.calixto.salao.dto.request.AgendamentoPublicoRequest;
import br.com.calixto.salao.dto.request.AtendimentoRequest;
import br.com.calixto.salao.dto.response.AgendamentoRealizadoResponse;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import br.com.calixto.salao.dto.response.DisponibilidadeResponse;
import br.com.calixto.salao.service.IAtendimentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping
public class AtendimentoController {

    private final IAtendimentoService atendimentoService;

    public AtendimentoController(IAtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    // ========== ENDPOINTS PÚBLICOS (sem autenticação) ==========

    @GetMapping("/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> consultarDisponibilidade(
            @RequestParam Integer profissionalId,
            @RequestParam(required = false) String servico,
            @RequestParam String data) {
        LocalDate dataLocal = LocalDate.parse(data);
        DisponibilidadeResponse response = atendimentoService.consultarDisponibilidade(profissionalId, servico, dataLocal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/agendamento/publico")
    @Transactional
    public ResponseEntity<AgendamentoRealizadoResponse> criarAgendamentoPublico(
            @RequestBody @Valid AgendamentoPublicoRequest request,
            UriComponentsBuilder uriBuilder) {
        AgendamentoRealizadoResponse response = atendimentoService.criarAgendamentoPublico(request);
        var uri = uriBuilder.path("/agendamento/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    // ========== ENDPOINTS ADMIN (autenticados) ==========

    @PostMapping("/atendimentos")
    @Transactional
    @SecurityRequirement(name = "bearer-key")
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

    @GetMapping("/atendimentos")
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<List<AtendimentoResponse>> listarTodos() {
        List<AtendimentoResponse> atendimentos = atendimentoService.listarTodos();
        return ResponseEntity.ok(atendimentos);
    }

    @GetMapping("/atendimentos/{id}")
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<AtendimentoResponse> buscarPorId(@PathVariable Integer id) {
        AtendimentoResponse atendimento = atendimentoService.buscarPorId(id);
        return ResponseEntity.ok(atendimento);
    }

    @PutMapping("/atendimentos/{id}")
    @Transactional
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<AtendimentoResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody @Valid AtendimentoRequest request
    ) {
        AtendimentoResponse atualizado = atendimentoService.atualizar(id, request);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/atendimentos/{id}")
    @Transactional
    @SecurityRequirement(name = "bearer-key")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        atendimentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}