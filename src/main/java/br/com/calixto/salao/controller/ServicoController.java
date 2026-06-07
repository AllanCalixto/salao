package br.com.calixto.salao.controller;

import br.com.calixto.salao.dto.response.ServicoResponse;
import br.com.calixto.salao.model.Servico;
import br.com.calixto.salao.repository.ServicoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoRepository servicoRepository;

    public ServicoController(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listarTodos() {
        List<ServicoResponse> servicos = servicoRepository.findAll()
                .stream()
                .map(s -> new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(), s.getDuracaoMinutos(), s.getPreco()))
                .toList();
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Integer id) {
        Servico servico = servicoRepository.findById(id).orElse(null);
        if (servico == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ServicoResponse(servico.getId(), servico.getNome(),
                servico.getDescricao(), servico.getDuracaoMinutos(), servico.getPreco()));
    }
}