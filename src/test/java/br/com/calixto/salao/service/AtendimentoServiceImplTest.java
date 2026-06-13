package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.AgendamentoPublicoRequest;
import br.com.calixto.salao.dto.request.AtendimentoRequest;
import br.com.calixto.salao.dto.response.AgendamentoRealizadoResponse;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import br.com.calixto.salao.dto.response.DisponibilidadeResponse;
import br.com.calixto.salao.exception.AtendimentoException;
import br.com.calixto.salao.exception.ClienteNaoEncontradoException;
import br.com.calixto.salao.exception.ProfissionalNaoEncontradoException;
import br.com.calixto.salao.exception.ServicoInvalidoException;
import br.com.calixto.salao.mapper.AtendimentoMapper;
import br.com.calixto.salao.model.Atendimento;
import br.com.calixto.salao.model.Cliente;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.model.Servico;
import br.com.calixto.salao.model.ProfissionalDisponibilidade;
import br.com.calixto.salao.repository.AtendimentoRepository;
import br.com.calixto.salao.repository.ClienteRepository;
import br.com.calixto.salao.repository.ProfissionalDisponibilidadeRepository;
import br.com.calixto.salao.repository.ProfissionalRepository;
import br.com.calixto.salao.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private ServicoRepository servicoRepository;

    @Mock
    private ProfissionalDisponibilidadeRepository disponibilidadeRepository;

    @Mock
    private AtendimentoMapper atendimentoMapper;

    @InjectMocks
    private AtendimentoServiceImpl atendimentoService;

    private Cliente cliente;
    private Profissional profissional;
    private Servico servico;
    private Atendimento atendimento;
    private AtendimentoRequest request;
    private AtendimentoResponse response;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome("Luisa");
        cliente.setTelefone("11999999999");

        profissional = new Profissional();
        profissional.setId(1);
        profissional.setNome("Joana");
        profissional.setServicos(List.of("MÃO", "PÉ", "CORTE DE CABELO"));

        servico = new Servico();
        servico.setId(1);
        servico.setNome("CORTE DE CABELO");
        servico.setDuracaoMinutos(30);

        LocalDateTime dataInicio = LocalDateTime.of(2025, 11, 2, 14, 0);
        LocalDateTime dataFim = dataInicio.plusMinutes(30);

        atendimento = new Atendimento();
        atendimento.setId(1);
        atendimento.setCliente(cliente);
        atendimento.setProfissional(profissional);
        atendimento.setServicoEscolhido("CORTE DE CABELO");
        atendimento.setServico(servico);
        atendimento.setDataAtendimento(dataInicio);
        atendimento.setDataFim(dataFim);
        atendimento.setPreco(45.0);
        atendimento.setStatus("AGENDADO");

        request = new AtendimentoRequest(
                cliente.getId(),
                profissional.getId(),
                "CORTE DE CABELO",
                45.0,
                dataInicio,
                dataFim
        );

        response = new AtendimentoResponse(
                1, "Luisa", "Joana", "CABELEREIRA",
                "CORTE DE CABELO", 45.0, dataInicio, dataFim, "AGENDADO"
        );
    }

    // ========== TESTES DE SALVAR (admin) ==========

    @Test
    void deveSalvarAtendimentoComSucesso() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.existsConflitoProfissionalIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.existsConflitoClienteIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);
        when(atendimentoMapper.toDtoResponse(any(Atendimento.class))).thenReturn(response);

        AtendimentoResponse result = atendimentoService.salvar(request);

        assertNotNull(result);
        assertEquals("Luisa", result.clienteNome());
        assertEquals("Joana", result.profissionalNome());
        assertEquals("AGENDADO", result.status());
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
                cliente.getId(), profissional.getId(), "SOBRANCELHA", 25.0,
                LocalDateTime.of(2025, 11, 2, 14, 0),
                LocalDateTime.of(2025, 11, 2, 14, 20)
        );

        assertThrows(ServicoInvalidoException.class, () -> atendimentoService.salvar(requestInvalido));
    }

    @Test
    void deveLancarExcecaoQuandoClienteJaTemConflito() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.existsConflitoProfissionalIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.existsConflitoClienteIgnorando(anyInt(), any(), any(), any())).thenReturn(true);

        assertThrows(AtendimentoException.class, () -> atendimentoService.salvar(request));
    }

    @Test
    void deveLancarExcecaoQuandoProfissionalJaTemConflito() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.existsConflitoProfissionalIgnorando(anyInt(), any(), any(), any())).thenReturn(true);

        assertThrows(AtendimentoException.class, () -> atendimentoService.salvar(request));
    }

    // ========== TESTES DE LISTAR ==========

    @Test
    void deveListarTodosOsAtendimentos() {
        when(atendimentoRepository.findAll()).thenReturn(List.of(atendimento));
        when(atendimentoMapper.toDtoResponse(atendimento)).thenReturn(response);

        var lista = atendimentoService.listarTodos();

        assertEquals(1, lista.size());
        assertEquals("Luisa", lista.get(0).clienteNome());
        verify(atendimentoRepository, times(1)).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaAtendimentos() {
        when(atendimentoRepository.findAll()).thenReturn(List.of());

        var lista = atendimentoService.listarTodos();

        assertTrue(lista.isEmpty());
    }

    // ========== TESTES DE BUSCAR POR ID ==========

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

    // ========== TESTES DE ATUALIZAR ==========

    @Test
    void deveAtualizarAtendimentoComSucesso() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.of(atendimento));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(atendimentoRepository.existsConflitoProfissionalIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.existsConflitoClienteIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
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

    // ========== TESTES DE DELETAR ==========

    @Test
    void deveDeletarAtendimentoComSucesso() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.of(atendimento));
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);

        atendimentoService.deletar(1);

        assertEquals("CANCELADO", atendimento.getStatus());
        verify(atendimentoRepository, times(1)).save(atendimento);
    }

    @Test
    void deveLancarExcecaoAoDeletarInexistente() {
        when(atendimentoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> atendimentoService.deletar(1));
    }

    // ========== TESTES DE DISPONIBILIDADE PÚBLICA ==========

    private ProfissionalDisponibilidade criarDisponibilidade(Integer profissionalId, int diaSemana,
                                                              LocalTime inicio, LocalTime fim) {
        ProfissionalDisponibilidade disp = new ProfissionalDisponibilidade();
        disp.setId(1);
        disp.setProfissional(profissional);
        disp.setDiaSemana(diaSemana);
        disp.setHoraInicio(inicio);
        disp.setHoraFim(fim);
        disp.setAtivo(true);
        return disp;
    }

    @Test
    void deveConsultarDisponibilidadeComSucesso() {
        LocalDate data = LocalDate.of(2025, 11, 5);
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();

        // 2025-11-05 é quarta-feira (DayOfWeek.WEDNESDAY = 3, nosso diaSemana = 3)
        ProfissionalDisponibilidade disp = criarDisponibilidade(1, 3, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findByNomeIgnoreCase("CORTE DE CABELO")).thenReturn(Optional.of(servico));
        when(disponibilidadeRepository.findByProfissionalIdAndAtivoTrue(1)).thenReturn(List.of(disp));
        when(atendimentoRepository.findByProfissionalAndDataBetween(1, inicioDia, fimDia)).thenReturn(List.of());

        DisponibilidadeResponse result = atendimentoService.consultarDisponibilidade(1, "CORTE DE CABELO", data);

        assertNotNull(result);
        assertEquals(1, result.profissionalId());
        assertEquals("Joana", result.profissionalNome());
        assertEquals("CORTE DE CABELO", result.servico());
        assertFalse(result.horariosDisponiveis().isEmpty());
    }

    @Test
    void deveConsultarDisponibilidadeComAgendamentosConflitantes() {
        LocalDate data = LocalDate.of(2025, 11, 5);
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();

        ProfissionalDisponibilidade disp = criarDisponibilidade(1, 3, LocalTime.of(8, 0), LocalTime.of(18, 0));

        // Cria um atendimento existente que ocupa 10:00-10:30
        Atendimento existente = new Atendimento();
        existente.setId(2);
        existente.setProfissional(profissional);
        existente.setDataAtendimento(LocalDateTime.of(2025, 11, 5, 10, 0));
        existente.setDataFim(LocalDateTime.of(2025, 11, 5, 10, 30));
        existente.setStatus("AGENDADO");

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findByNomeIgnoreCase("CORTE DE CABELO")).thenReturn(Optional.of(servico));
        when(disponibilidadeRepository.findByProfissionalIdAndAtivoTrue(1)).thenReturn(List.of(disp));
        when(atendimentoRepository.findByProfissionalAndDataBetween(1, inicioDia, fimDia))
                .thenReturn(List.of(existente));

        DisponibilidadeResponse result = atendimentoService.consultarDisponibilidade(1, "CORTE DE CABELO", data);

        assertNotNull(result);
        // Verifica que 10:00 não está disponível (ocupado)
        boolean horario1000Disponivel = result.horariosDisponiveis().stream()
                .anyMatch(h -> h.horario().equals(LocalTime.of(10, 0)));
        assertFalse(horario1000Disponivel, "Horário 10:00 não deveria estar disponível");
    }

    @Test
    void deveLancarExcecaoAoConsultarDisponibilidadeProfissionalInexistente() {
        when(profissionalRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ProfissionalNaoEncontradoException.class,
                () -> atendimentoService.consultarDisponibilidade(999, "CORTE", LocalDate.now()));
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoPertenceAoProfissionalNaDisponibilidade() {
        profissional.setServicos(List.of("MÃO", "PÉ"));
        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));

        assertThrows(ServicoInvalidoException.class,
                () -> atendimentoService.consultarDisponibilidade(1, "CORTE DE CABELO", LocalDate.now()));
    }

    // ========== TESTES DE AGENDAMENTO PÚBLICO ==========

    @Test
    void deveCriarAgendamentoPublicoComSucesso() {
        AgendamentoPublicoRequest request = new AgendamentoPublicoRequest(
                "Luisa", "11999999999", 1, 1, "CORTE DE CABELO",
                LocalDate.of(2025, 11, 10), LocalTime.of(14, 0)
        );

        // 2025-11-10 é segunda-feira (diaSemana=1)
        ProfissionalDisponibilidade disp = criarDisponibilidade(1, 1, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(disponibilidadeRepository.findByProfissionalIdAndAtivoTrue(1)).thenReturn(List.of(disp));
        when(clienteRepository.findByTelefone("11999999999")).thenReturn(Optional.of(cliente));
        when(atendimentoRepository.existsConflitoProfissionalIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.existsConflitoClienteIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);

        AgendamentoRealizadoResponse expectedResponse = new AgendamentoRealizadoResponse(
                1, "Luisa", "Joana", "CORTE DE CABELO",
                LocalDateTime.of(2025, 11, 10, 14, 0),
                LocalDateTime.of(2025, 11, 10, 14, 30),
                "AGENDADO"
        );
        when(atendimentoMapper.toAgendamentoRealizadoResponse(any(Atendimento.class))).thenReturn(expectedResponse);

        AgendamentoRealizadoResponse result = atendimentoService.criarAgendamentoPublico(request);

        assertNotNull(result);
        assertEquals("Luisa", result.nomeCliente());
        assertEquals("CORTE DE CABELO", result.servicoNome());
        verify(atendimentoRepository, times(1)).save(any(Atendimento.class));
    }

    @Test
    void deveCriarClienteQuandoNaoExisteNoAgendamentoPublico() {
        AgendamentoPublicoRequest request = new AgendamentoPublicoRequest(
                "Novo Cliente", "11988888888", 1, 1, "CORTE DE CABELO",
                LocalDate.of(2025, 11, 10), LocalTime.of(14, 0)
        );

        ProfissionalDisponibilidade disp = criarDisponibilidade(1, 1, LocalTime.of(8, 0), LocalTime.of(18, 0));

        Cliente novoCliente = new Cliente();
        novoCliente.setId(2);
        novoCliente.setNome("Novo Cliente");
        novoCliente.setTelefone("11988888888");

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(disponibilidadeRepository.findByProfissionalIdAndAtivoTrue(1)).thenReturn(List.of(disp));
        when(clienteRepository.findByTelefone("11988888888")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(novoCliente);
        when(atendimentoRepository.existsConflitoProfissionalIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.existsConflitoClienteIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);

        AgendamentoRealizadoResponse expectedResponse = new AgendamentoRealizadoResponse(
                1, "Novo Cliente", "Joana", "CORTE DE CABELO",
                LocalDateTime.of(2025, 11, 10, 14, 0),
                LocalDateTime.of(2025, 11, 10, 14, 30),
                "AGENDADO"
        );
        when(atendimentoMapper.toAgendamentoRealizadoResponse(any(Atendimento.class))).thenReturn(expectedResponse);

        AgendamentoRealizadoResponse result = atendimentoService.criarAgendamentoPublico(request);

        assertNotNull(result);
        assertEquals("Novo Cliente", result.nomeCliente());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void deveCriarServicoGenericoQuandoNaoExisteNoCatalogo() {
        profissional.setServicos(List.of("SERVIÇO NOVO"));
        AgendamentoPublicoRequest request = new AgendamentoPublicoRequest(
                "Luisa", "11999999999", 1, 999, "SERVIÇO NOVO",
                LocalDate.of(2025, 11, 10), LocalTime.of(14, 0)
        );

        Servico novoServico = new Servico("SERVIÇO NOVO", "Serviço", 30, 0.0);
        novoServico.setId(99);

        ProfissionalDisponibilidade disp = criarDisponibilidade(1, 1, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findById(999)).thenReturn(Optional.empty());
        when(servicoRepository.findByNomeIgnoreCase("SERVIÇO NOVO")).thenReturn(Optional.empty());
        when(servicoRepository.save(any(Servico.class))).thenReturn(novoServico);
        when(disponibilidadeRepository.findByProfissionalIdAndAtivoTrue(1)).thenReturn(List.of(disp));
        when(clienteRepository.findByTelefone("11999999999")).thenReturn(Optional.of(cliente));
        when(atendimentoRepository.existsConflitoProfissionalIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.existsConflitoClienteIgnorando(anyInt(), any(), any(), any())).thenReturn(false);
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);

        AgendamentoRealizadoResponse expectedResponse = new AgendamentoRealizadoResponse(
                1, "Luisa", "Joana", "SERVIÇO NOVO",
                LocalDateTime.of(2025, 11, 10, 14, 0),
                LocalDateTime.of(2025, 11, 10, 14, 30),
                "AGENDADO"
        );
        when(atendimentoMapper.toAgendamentoRealizadoResponse(any(Atendimento.class))).thenReturn(expectedResponse);

        AgendamentoRealizadoResponse result = atendimentoService.criarAgendamentoPublico(request);

        assertNotNull(result);
        assertEquals("SERVIÇO NOVO", result.servicoNome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    void deveLancarExcecaoQuandoProfissionalNaoOfereceServico() {
        AgendamentoPublicoRequest request = new AgendamentoPublicoRequest(
                "Luisa", "11999999999", 1, 2, "SOBRANCELHA",
                LocalDate.of(2025, 11, 10), LocalTime.of(14, 0)
        );

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));

        assertThrows(ServicoInvalidoException.class,
                () -> atendimentoService.criarAgendamentoPublico(request));
    }

    @Test
    void deveLancarExcecaoQuandoHorarioForaDoExpediente() {
        AgendamentoPublicoRequest request = new AgendamentoPublicoRequest(
                "Luisa", "11999999999", 1, 1, "CORTE DE CABELO",
                LocalDate.of(2025, 11, 10), LocalTime.of(7, 0)
        );

        ProfissionalDisponibilidade disp = criarDisponibilidade(1, 1, LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(profissionalRepository.findById(1)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(disponibilidadeRepository.findByProfissionalIdAndAtivoTrue(1)).thenReturn(List.of(disp));

        assertThrows(AtendimentoException.class,
                () -> atendimentoService.criarAgendamentoPublico(request));
    }
}