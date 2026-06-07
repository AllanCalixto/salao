package br.com.calixto.salao.controller;

import br.com.calixto.salao.dto.request.DisponibilidadeProfissionalRequest;
import br.com.calixto.salao.dto.response.DisponibilidadeProfissionalResponse;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.model.ProfissionalDisponibilidade;
import br.com.calixto.salao.repository.ProfissionalDisponibilidadeRepository;
import br.com.calixto.salao.repository.ProfissionalRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profissionais/{profissionalId}/disponibilidade")
@SecurityRequirement(name = "bearer-key")
public class DisponibilidadeProfissionalController {

    private final ProfissionalDisponibilidadeRepository disponibilidadeRepository;
    private final ProfissionalRepository profissionalRepository;

    private static final String[] DIAS_SEMANA = {
        "DOMINGO", "SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO"
    };

    public DisponibilidadeProfissionalController(
            ProfissionalDisponibilidadeRepository disponibilidadeRepository,
            ProfissionalRepository profissionalRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @GetMapping
    public ResponseEntity<List<DisponibilidadeProfissionalResponse>> listar(
            @PathVariable Integer profissionalId) {
        profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        List<DisponibilidadeProfissionalResponse> response = disponibilidadeRepository
                .findByProfissionalIdOrderByDiaSemana(profissionalId)
                .stream()
                .map(d -> new DisponibilidadeProfissionalResponse(
                        d.getId(), d.getDiaSemana(),
                        DIAS_SEMANA[d.getDiaSemana()],
                        d.getHoraInicio(), d.getHoraFim(), d.getAtivo()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{diaSemana}")
    @Transactional
    public ResponseEntity<DisponibilidadeProfissionalResponse> atualizar(
            @PathVariable Integer profissionalId,
            @PathVariable Integer diaSemana,
            @RequestBody @Valid DisponibilidadeProfissionalRequest request) {

        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        ProfissionalDisponibilidade disp = disponibilidadeRepository
                .findByProfissionalIdOrderByDiaSemana(profissionalId)
                .stream()
                .filter(d -> d.getDiaSemana().equals(diaSemana))
                .findFirst()
                .orElseGet(() -> {
                    ProfissionalDisponibilidade nova = new ProfissionalDisponibilidade();
                    nova.setProfissional(profissional);
                    nova.setDiaSemana(diaSemana);
                    return nova;
                });

        disp.setHoraInicio(request.horaInicio());
        disp.setHoraFim(request.horaFim());
        disp.setAtivo(request.ativo());

        ProfissionalDisponibilidade salvo = disponibilidadeRepository.save(disp);

        return ResponseEntity.ok(new DisponibilidadeProfissionalResponse(
                salvo.getId(), salvo.getDiaSemana(),
                DIAS_SEMANA[salvo.getDiaSemana()],
                salvo.getHoraInicio(), salvo.getHoraFim(), salvo.getAtivo()));
    }
}