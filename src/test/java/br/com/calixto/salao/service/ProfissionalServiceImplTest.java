package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.ProfissionalRequest;
import br.com.calixto.salao.dto.response.ProfissionalResponse;
import br.com.calixto.salao.exception.ProfissionalJaCadastradoException;
import br.com.calixto.salao.exception.ProfissionalNaoEncontradoException;
import br.com.calixto.salao.mapper.ProfissionalMapper;
import br.com.calixto.salao.model.Especialidade;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.repository.ProfissionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceImplTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ProfissionalMapper profissionalMapper;

    @InjectMocks
    private ProfissionalServiceImpl profissionalService;

    private Profissional profissional;
    private ProfissionalRequest request;
    private ProfissionalResponse response;

    @BeforeEach
    void setUp() {
        profissional = new Profissional();
        profissional.setId(1);
        profissional.setNome("Joana Silva");
        profissional.setEspecialidade(Especialidade.MANICURE);
        profissional.setServicos(List.of("MÃO", "PÉ"));

        request = new ProfissionalRequest(
                "Joana Silva",
                Especialidade.MANICURE,
                List.of("MÃO", "PÉ"),
                null
        );

        response = new ProfissionalResponse(
                1,
                "Joana Silva",
                Especialidade.MANICURE,
                List.of("MÃO", "PÉ"),
                List.of()
        );
    }

    @Test
    void deveSalvarProfissionalComSucesso() {
        when(profissionalRepository.findByNome("Joana Silva")).thenReturn(Optional.empty());
        when(profissionalMapper.toEntity(request)).thenReturn(profissional);
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(profissional);
        when(profissionalMapper.toResponse(profissional)).thenReturn(response);

        ProfissionalResponse result = profissionalService.salvar(request);

        assertNotNull(result);
        assertEquals("Joana Silva", result.nome());
        assertEquals(Especialidade.MANICURE, result.especialidade());
        assertEquals(2, result.servicos().size());
        verify(profissionalRepository, times(1)).save(any(Profissional.class));
    }

    @Test
    void deveLancarExcecaoQuandoNomeJaExiste() {
        when(profissionalRepository.findByNome("Joana Silva")).thenReturn(Optional.of(profissional));

        assertThrows(ProfissionalJaCadastradoException.class, () -> profissionalService.salvar(request));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveListarTodosOsProfissionais() {
        Profissional profissional2 = new Profissional();
        profissional2.setId(2);
        profissional2.setNome("Maria Santos");
        profissional2.setEspecialidade(Especialidade.CABELEREIRA);
        profissional2.setServicos(List.of("CORTE", "ESCOVA"));

        ProfissionalResponse response2 = new ProfissionalResponse(
                2,
                "Maria Santos",
                Especialidade.CABELEREIRA,
                List.of("CORTE", "ESCOVA"),
                List.of()
        );

        when(profissionalRepository.findAll()).thenReturn(List.of(profissional, profissional2));
        when(profissionalMapper.toResponse(profissional)).thenReturn(response);
        when(profissionalMapper.toResponse(profissional2)).thenReturn(response2);

        var lista = profissionalService.listarTodos();

        assertEquals(2, lista.size());
        assertEquals("Joana Silva", lista.get(0).nome());
        assertEquals("Maria Santos", lista.get(1).nome());
        verify(profissionalRepository, times(1)).findAll();
    }

    @Test
    void deveBuscarProfissionalPorIdComSucesso() {
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(profissionalMapper.toResponse(profissional)).thenReturn(response);

        ProfissionalResponse result = profissionalService.buscarPorId(1);

        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals("Joana Silva", result.nome());
        assertEquals(Especialidade.MANICURE, result.especialidade());
        verify(profissionalRepository, times(1)).findById(1);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorIdInexistente() {
        when(profissionalRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ProfissionalNaoEncontradoException.class, () -> profissionalService.buscarPorId(999));
    }

    @Test
    void deveAtualizarProfissionalComSucesso() {
        ProfissionalRequest requestAtualizado = new ProfissionalRequest(
                "Joana Silva Atualizada",
                Especialidade.CABELEREIRA,
                List.of("CORTE", "ESCOVA", "COLORAÇÃO"),
                null
        );

        ProfissionalResponse responseAtualizado = new ProfissionalResponse(
                1,
                "Joana Silva Atualizada",
                Especialidade.CABELEREIRA,
                List.of("CORTE", "ESCOVA", "COLORAÇÃO"),
                List.of()
        );

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(profissional);
        when(profissionalMapper.toResponse(any(Profissional.class))).thenReturn(responseAtualizado);

        ProfissionalResponse result = profissionalService.atualizar(1, requestAtualizado);

        assertNotNull(result);
        assertEquals("Joana Silva Atualizada", result.nome());
        assertEquals(Especialidade.CABELEREIRA, result.especialidade());
        assertEquals(3, result.servicos().size());
        verify(profissionalRepository, times(1)).save(any(Profissional.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarProfissionalInexistente() {
        ProfissionalRequest requestAtualizado = new ProfissionalRequest(
                "Joana Silva Atualizada",
                Especialidade.CABELEREIRA,
                List.of("CORTE", "ESCOVA"),
                null
        );

        when(profissionalRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ProfissionalNaoEncontradoException.class, 
                () -> profissionalService.atualizar(999, requestAtualizado));
        verify(profissionalRepository, never()).save(any(Profissional.class));
    }

    @Test
    void deveDeletarProfissionalComSucesso() {
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));

        profissionalService.deletar(1);

        verify(profissionalRepository, times(1)).delete(profissional);
    }

    @Test
    void deveLancarExcecaoAoDeletarProfissionalInexistente() {
        when(profissionalRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ProfissionalNaoEncontradoException.class, () -> profissionalService.deletar(999));
        verify(profissionalRepository, never()).delete(any(Profissional.class));
    }
}


