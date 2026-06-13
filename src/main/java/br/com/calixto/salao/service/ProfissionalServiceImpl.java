package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.ProfissionalRequest;
import br.com.calixto.salao.dto.response.ProfissionalResponse;
import br.com.calixto.salao.exception.ProfissionalJaCadastradoException;
import br.com.calixto.salao.exception.ProfissionalNaoEncontradoException;
import br.com.calixto.salao.mapper.ProfissionalMapper;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.model.ProfissionalDisponibilidade;
import br.com.calixto.salao.repository.ProfissionalDisponibilidadeRepository;
import br.com.calixto.salao.repository.ProfissionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfissionalServiceImpl implements IProfissionalService{
    private final ProfissionalRepository profissionalRepository;
    private final ProfissionalDisponibilidadeRepository profissionalDisponibilidadeRepository;
    private final ProfissionalMapper profissionalMapper;

    public ProfissionalServiceImpl(ProfissionalRepository profissionalRepository,
                                   ProfissionalDisponibilidadeRepository profissionalDisponibilidadeRepository,
                                   ProfissionalMapper profissionalMapper) {
        this.profissionalRepository = profissionalRepository;
        this.profissionalDisponibilidadeRepository = profissionalDisponibilidadeRepository;
        this.profissionalMapper = profissionalMapper;
    }

    @Override
    @Transactional
    public ProfissionalResponse salvar(ProfissionalRequest profissionalRequest) {
        boolean nomeJaExiste = profissionalRepository.findByNome(profissionalRequest.nome()).isPresent();

        if (nomeJaExiste) {
            throw new ProfissionalJaCadastradoException(
                    "Profissional com o nome: " + profissionalRequest.nome() + " já foi cadastrado!"
            );
        }

        Profissional profissional = profissionalMapper.toEntity(profissionalRequest);

        if (profissionalRequest.disponibilidade() != null && !profissionalRequest.disponibilidade().isEmpty()) {
            List<ProfissionalDisponibilidade> disponibilidades = profissionalRequest.disponibilidade()
                    .stream()
                    .map(d -> {
                        ProfissionalDisponibilidade disp = new ProfissionalDisponibilidade();
                        disp.setProfissional(profissional);
                        disp.setDiaSemana(d.diaSemana());
                        disp.setHoraInicio(d.horaInicio());
                        disp.setHoraFim(d.horaFim());
                        disp.setAtivo(true);
                        return disp;
                    })
                    .toList();
            profissional.setDisponibilidades(disponibilidades);
        }

        Profissional profissionalSalvo = profissionalRepository.save(profissional);
        return profissionalMapper.toResponse(profissionalSalvo);
    }

    @Override
    public List<ProfissionalResponse> listarTodos() {
        return profissionalRepository.findAll()
                .stream()
                .map(profissionalMapper::toResponse)
                .toList();
    }

    @Override
    public ProfissionalResponse buscarPorId(Integer id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ProfissionalNaoEncontradoException(
                        "Profissional com ID " + id + " não encontrado."));
        return profissionalMapper.toResponse(profissional);
    }

    @Override
    @Transactional
    public ProfissionalResponse atualizar(Integer id, ProfissionalRequest profissionalRequest) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ProfissionalNaoEncontradoException(
                        "Profissional com ID " + id + " não encontrado."));

        profissional.setNome(profissionalRequest.nome());
        profissional.setEspecialidade(profissionalRequest.especialidade());
        profissional.setServicos(profissionalRequest.servicos());

        if (profissionalRequest.disponibilidade() != null) {
            // Deleta explicitamente as disponibilidades existentes no banco
            // para evitar violação de unique constraint (profissional_id, dia_semana)
            profissionalDisponibilidadeRepository.deleteByProfissionalId(id);
            profissionalDisponibilidadeRepository.flush();

            List<ProfissionalDisponibilidade> disponibilidades = profissionalRequest.disponibilidade()
                    .stream()
                    .map(d -> {
                        ProfissionalDisponibilidade disp = new ProfissionalDisponibilidade();
                        disp.setProfissional(profissional);
                        disp.setDiaSemana(d.diaSemana());
                        disp.setHoraInicio(d.horaInicio());
                        disp.setHoraFim(d.horaFim());
                        disp.setAtivo(true);
                        return disp;
                    })
                    .toList();
            profissional.setDisponibilidades(disponibilidades);
        }

        Profissional atualizado = profissionalRepository.save(profissional);
        return profissionalMapper.toResponse(atualizado);
    }

    @Override
    public void deletar(Integer id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ProfissionalNaoEncontradoException(
                        "Profissional com ID " + id + " não encontrado."));
        profissionalRepository.delete(profissional);
    }
}