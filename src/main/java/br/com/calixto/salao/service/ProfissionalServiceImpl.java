package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.ProfissionalRequest;
import br.com.calixto.salao.dto.response.ProfissionalResponse;
import br.com.calixto.salao.exception.ProfissionalJaCadastradoException;
import br.com.calixto.salao.exception.ProfissionalNaoEncontradoException;
import br.com.calixto.salao.mapper.ProfissionalMapper;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.repository.ProfissionalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfissionalServiceImpl implements IProfissionalService{
    private final ProfissionalRepository profissionalRepository;
    private final ProfissionalMapper profissionalMapper;

    public ProfissionalServiceImpl(ProfissionalRepository profissionalRepository,
                                   ProfissionalMapper profissionalMapper) {
        this.profissionalRepository = profissionalRepository;
        this.profissionalMapper = profissionalMapper;
    }

    @Override
    public ProfissionalResponse salvar(ProfissionalRequest profissionalRequest) {
        boolean nomeJaExiste = profissionalRepository.findByNome(profissionalRequest.nome()).isPresent();

        if (nomeJaExiste) {
            throw new ProfissionalJaCadastradoException(
                    "Profissional com o nome: " + profissionalRequest.nome() + " já foi cadastrado!"
            );
        }

        Profissional profissional = profissionalMapper.toEntity(profissionalRequest);
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
    public ProfissionalResponse atualizar(Integer id, ProfissionalRequest profissionalRequest) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ProfissionalNaoEncontradoException(
                        "Profissional com ID " + id + " não encontrado."));

        profissional.setNome(profissionalRequest.nome());
        profissional.setEspecialidade(profissionalRequest.especialidade());
        profissional.setServicos(profissionalRequest.servicos());

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
