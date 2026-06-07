package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.ClienteRequest;
import br.com.calixto.salao.dto.response.ClienteResponse;
import br.com.calixto.salao.exception.ClienteJaCadastradoException;
import br.com.calixto.salao.exception.ClienteNaoEncontradoException;
import br.com.calixto.salao.mapper.ClienteMapper;
import br.com.calixto.salao.model.Cliente;
import br.com.calixto.salao.repository.ClienteRepository;
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
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private ClienteRequest request;
    private ClienteResponse response;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome("Maria Silva");
        cliente.setTelefone("11999999999");

        request = new ClienteRequest("Maria Silva", "11999999999");

        response = new ClienteResponse(1, "Maria Silva", "11999999999");
    }

    @Test
    void deveSalvarClienteComSucesso() {
        when(clienteRepository.findByNome("Maria Silva")).thenReturn(Optional.empty());
        when(clienteMapper.toEntity(request)).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDtoResponse(cliente)).thenReturn(response);

        ClienteResponse result = clienteService.salvar(request);

        assertNotNull(result);
        assertEquals("Maria Silva", result.nome());
        assertEquals("11999999999", result.telefone());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoQuandoNomeJaExiste() {
        when(clienteRepository.findByNome("Maria Silva")).thenReturn(Optional.of(cliente));

        assertThrows(ClienteJaCadastradoException.class, () -> clienteService.salvar(request));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveListarTodosOsClientes() {
        Cliente cliente2 = new Cliente();
        cliente2.setId(2);
        cliente2.setNome("João Santos");
        cliente2.setTelefone("11888888888");

        ClienteResponse response2 = new ClienteResponse(2, "João Santos", "11888888888");

        when(clienteRepository.findAll()).thenReturn(List.of(cliente, cliente2));
        when(clienteMapper.toDtoResponse(cliente)).thenReturn(response);
        when(clienteMapper.toDtoResponse(cliente2)).thenReturn(response2);

        var lista = clienteService.listarTodosClientes();

        assertEquals(2, lista.size());
        assertEquals("Maria Silva", lista.get(0).nome());
        assertEquals("João Santos", lista.get(1).nome());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    void deveBuscarClientePorNomeComSucesso() {
        when(clienteRepository.findByNome("Maria Silva")).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDtoResponse(cliente)).thenReturn(response);

        ClienteResponse result = clienteService.findByExistsNome("Maria Silva");

        assertNotNull(result);
        assertEquals("Maria Silva", result.nome());
        verify(clienteRepository, times(1)).findByNome("Maria Silva");
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorNomeInexistente() {
        when(clienteRepository.findByNome("Cliente Inexistente")).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, 
                () -> clienteService.findByExistsNome("Cliente Inexistente"));
    }

    @Test
    void deveBuscarClientePorTelefoneComSucesso() {
        when(clienteRepository.findByTelefone("11999999999")).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDtoResponse(cliente)).thenReturn(response);

        ClienteResponse result = clienteService.findByExistsTelefone("11999999999");

        assertNotNull(result);
        assertEquals("11999999999", result.telefone());
        verify(clienteRepository, times(1)).findByTelefone("11999999999");
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorTelefoneInexistente() {
        when(clienteRepository.findByTelefone("11000000000")).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, 
                () -> clienteService.findByExistsTelefone("11000000000"));
    }

    @Test
    void deveBuscarClientePorIdComSucesso() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDtoResponse(cliente)).thenReturn(response);

        ClienteResponse result = clienteService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals("Maria Silva", result.nome());
        verify(clienteRepository, times(1)).findById(1);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorIdInexistente() {
        when(clienteRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> clienteService.findById(999));
    }

    @Test
    void deveAtualizarClienteComSucesso() {
        ClienteRequest requestAtualizado = new ClienteRequest("Maria Silva Atualizada", "11988888888");
        ClienteResponse responseAtualizado = new ClienteResponse(1, "Maria Silva Atualizada", "11988888888");

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByNome("Maria Silva Atualizada")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toDtoResponse(any(Cliente.class))).thenReturn(responseAtualizado);

        ClienteResponse result = clienteService.atualizar(1, requestAtualizado);

        assertNotNull(result);
        assertEquals("Maria Silva Atualizada", result.nome());
        assertEquals("11988888888", result.telefone());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        ClienteRequest requestAtualizado = new ClienteRequest("Maria Silva Atualizada", "11988888888");

        when(clienteRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, 
                () -> clienteService.atualizar(999, requestAtualizado));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComNomeJaExistente() {
        Cliente outroCliente = new Cliente();
        outroCliente.setId(2);
        outroCliente.setNome("João Santos");

        ClienteRequest requestAtualizado = new ClienteRequest("João Santos", "11988888888");

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findByNome("João Santos")).thenReturn(Optional.of(outroCliente));

        assertThrows(ClienteJaCadastradoException.class, 
                () -> clienteService.atualizar(1, requestAtualizado));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveDeletarClienteComSucesso() {
        when(clienteRepository.existsById(1)).thenReturn(true);

        clienteService.deletar(1);

        verify(clienteRepository, times(1)).existsById(1);
        verify(clienteRepository, times(1)).deleteById(1);
    }

    @Test
    void deveLancarExcecaoAoDeletarClienteInexistente() {
        when(clienteRepository.existsById(999)).thenReturn(false);

        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> clienteService.deletar(999)
        );

        assertEquals("Cliente 999 não foi encontrado!", exception.getMessage());

        verify(clienteRepository, times(1)).existsById(999);
        verify(clienteRepository, never()).deleteById(anyInt());
    }
}

