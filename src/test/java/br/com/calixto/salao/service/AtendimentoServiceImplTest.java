package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.AtendimentoRequest;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import br.com.calixto.salao.exception.AtendimentoException;
import br.com.calixto.salao.exception.ClienteNaoEncontradoException;
import br.com.calixto.salao.exception.ProfissionalNaoEncontradoException;
import br.com.calixto.salao.exception.ServicoInvalidoException;
import br.com.calixto.salao.mapper.AtendimentoMapper;
import br.com.calixto.salao.model.Atendimento;
import br.com.calixto.salao.model.Cliente;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.repository.AtendimentoRepository;
import br.com.calixto.salao.repository.ClienteRepository;
import br.com.calixto.salao.repository.ProfissionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceImplTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private AtendimentoMapper atendimentoMapper;

    @InjectMocks
    private AtendimentoServiceImpl atendimentoService;

    private Cliente cliente;
    private Profissional profissional;
    private Atendimento atendimento;
    private AtendimentoRequest request;
    private AtendimentoResponse response;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome("Luisa");

        profissional = new Profissional();
        profissional.setId(1);
        profissional.setNome("Joana");
        profissional.setServicos(List.of("MÃO", "PÉ"));

        atendimento = new Atendimento();
        atendimento.setId(1);
        atendimento.setCliente(cliente);
        atendimento.setProfissional(profissional);
        atendimento.setServicoEscolhido("PÉ");
        atendimento.setDataAtendimento(LocalDateTime.of(2025, 11, 2, 14, 0));
        atendimento.setPreco(100.0);

        request = new AtendimentoRequest(
                cliente.getId(),
                profissional.getId(),
                "PÉ",
                100.0,
                atendimento.getDataAtendimento()
        );

        response = new AtendimentoResponse(
                1, "Luisa", "Joana", "MANICURE", "PÉ", 100.0, atendimento.getDataAtendimento()
        );
    }

    // =====================================================
    // SALVAR ATENDIMENTO
    // =====================================================

    @Test
    void deveSalvarAtendimentoComSucesso() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.existsAtendimentoClienteMesmoHorario(1, request.dataAtendimento())).thenReturn(false);
        when(atendimentoRepository.existsAtendimentoProfissionalMesmoHorario(1, request.dataAtendimento())).thenReturn(false);
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);
        when(atendimentoMapper.toDtoResponse(any(Atendimento.class))).thenReturn(response);

        AtendimentoResponse result = atendimentoService.salvar(request);

        assertNotNull(result);
        assertEquals("Luisa", result.clienteNome());
        assertEquals("Joana", result.profissionalNome());
        verify(atendimentoRepository, times(1)).save(any(Atendimento.class));
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        when(clienteRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> atendimentoService.salvar(request));
    }

    @Test
    void deveLancarExcecaoQuandoProfissionalNaoEncontrado() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ProfissionalNaoEncontradoException.class, () -> atendimentoService.salvar(request));
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoPertenceAoProfissional() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));

        AtendimentoRequest requestInvalido = new AtendimentoRequest(
                cliente.getId(), profissional.getId(), "SOBRANCELHA", 100.0, request.dataAtendimento()
        );

        assertThrows(ServicoInvalidoException.class, () -> atendimentoService.salvar(requestInvalido));
    }

    @Test
    void deveLancarExcecaoQuandoClienteJaTemAtendimentoMesmoHorario() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.existsAtendimentoClienteMesmoHorario(1, request.dataAtendimento())).thenReturn(true);

        assertThrows(AtendimentoException.class, () -> atendimentoService.salvar(request));
    }

    @Test
    void deveLancarExcecaoQuandoProfissionalJaTemAtendimentoMesmoHorario() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.existsAtendimentoClienteMesmoHorario(1, request.dataAtendimento())).thenReturn(false);
        when(atendimentoRepository.existsAtendimentoProfissionalMesmoHorario(1, request.dataAtendimento())).thenReturn(true);

        assertThrows(AtendimentoException.class, () -> atendimentoService.salvar(request));
    }

    // =====================================================
    // LISTAR TODOS
    // =====================================================

    @Test
    void deveListarTodosOsAtendimentos() {
        when(atendimentoRepository.findAll()).thenReturn(List.of(atendimento));
        when(atendimentoMapper.toDtoResponse(atendimento)).thenReturn(response);

        var lista = atendimentoService.listarTodos();

        assertEquals(1, lista.size());
        assertEquals("Luisa", lista.get(0).clienteNome());
        verify(atendimentoRepository, times(1)).findAll();
    }

    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    @Test
    void deveBuscarAtendimentoPorId() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.of(atendimento));
        when(atendimentoMapper.toDtoResponse(atendimento)).thenReturn(response);

        AtendimentoResponse result = atendimentoService.buscarPorId(1);

        assertNotNull(result);
        assertEquals("Joana", result.profissionalNome());
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorIdInexistente() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> atendimentoService.buscarPorId(1));
    }

    // =====================================================
    // ATUALIZAR
    // =====================================================

    @Test
    void deveAtualizarAtendimentoComSucesso() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.of(atendimento));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);
        when(atendimentoMapper.toDtoResponse(any(Atendimento.class))).thenReturn(response);

        AtendimentoResponse result = atendimentoService.atualizar(1, request);

        assertEquals("Luisa", result.clienteNome());
        verify(atendimentoRepository, times(1)).save(any(Atendimento.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComProfissionalInvalido() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.of(atendimento));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ProfissionalNaoEncontradoException.class, () -> atendimentoService.atualizar(1, request));
    }

    // =====================================================
    // DELETAR
    // =====================================================

    @Test
    void deveDeletarAtendimentoComSucesso() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.of(atendimento));

        atendimentoService.deletar(1);

        verify(atendimentoRepository, times(1)).delete(atendimento);
    }

    @Test
    void deveLancarExcecaoAoDeletarInexistente() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> atendimentoService.deletar(1));
    }
}
