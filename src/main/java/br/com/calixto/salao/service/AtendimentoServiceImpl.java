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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtendimentoServiceImpl implements IAtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AtendimentoMapper atendimentoMapper;

    public AtendimentoServiceImpl(AtendimentoRepository atendimentoRepository, ClienteRepository clienteRepository, ProfissionalRepository profissionalRepository, AtendimentoMapper atendimentoMapper) {
        this.atendimentoRepository = atendimentoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.atendimentoMapper = atendimentoMapper;
    }

    @Override
    @Transactional
    public AtendimentoResponse salvar(AtendimentoRequest atendimentoRequest) {
        Cliente cliente = clienteRepository.findById(atendimentoRequest.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado!"));

        Profissional profissional = profissionalRepository.findById(atendimentoRequest.profissionalId())
                .orElseThrow(() -> new ProfissionalNaoEncontradoException("Profissional não foi encontrado"));


        // Valida se o serviço deve pertencer ao profissional
        if (!profissional.getServicos().contains(atendimentoRequest.servicoEscolhido().toUpperCase())) {
            throw new ServicoInvalidoException(
                    "O Serviço " + atendimentoRequest.servicoEscolhido() +
                            " não pertence a especialidade do profissional " + profissional.getNome());
        }

        // Valida se o cliente já tem atendimento no mesmo dia e horário
        boolean clienteOcupado = atendimentoRepository.existsAtendimentoClienteMesmoHorario(cliente.getId(), atendimentoRequest.dataAtendimento());

        if (clienteOcupado) {
            throw new AtendimentoException("O cliente " + cliente.getNome() + " já possui um agendamento para " + atendimentoRequest.dataAtendimento());
        }

        // Valida se o profissional já tem atendimento no mesmo dia e horário
        boolean profissionalOcupado = atendimentoRepository.existsAtendimentoProfissionalMesmoHorario(profissional.getId(), atendimentoRequest.dataAtendimento());

        if (profissionalOcupado) {
            throw new AtendimentoException("O profissional "+profissional.getNome()+ " já possui um agendamento para "+ atendimentoRequest.dataAtendimento());
        }

        Atendimento atendimento = new Atendimento();
        atendimento.setCliente(cliente);
        atendimento.setProfissional(profissional);
        atendimento.setServicoEscolhido(atendimentoRequest.servicoEscolhido().toUpperCase());
        atendimento.setDataAtendimento(atendimentoRequest.dataAtendimento());
        atendimento.setPreco(atendimentoRequest.preco());

        Atendimento salvo = atendimentoRepository.save(atendimento);
        return atendimentoMapper.toDtoResponse(salvo);
    }

    @Override
    public List<AtendimentoResponse> listarTodos() {
        return atendimentoRepository.findAll()
                .stream()
                .map(atendimentoMapper::toDtoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AtendimentoResponse buscarPorId(Integer id) {
        Atendimento atendimento = atendimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendimento não encontrado!"));
        return atendimentoMapper.toDtoResponse(atendimento);
    }

    @Override
    public AtendimentoResponse atualizar(Integer id, AtendimentoRequest request) {

        Atendimento atendimento = atendimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendimento não encontrado!"));

        // Buscar cliente e profissional
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado!"));

        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new ProfissionalNaoEncontradoException("Profissional não encontrado!"));


        if (!profissional.getServicos().contains(request.servicoEscolhido().toUpperCase())) {
            throw new ServicoInvalidoException("O Serviço " + request.servicoEscolhido() +
                    " não pertence à especialidade do profissional " + profissional.getNome());
        }

        atendimento.setCliente(cliente);
        atendimento.setProfissional(profissional);
        atendimento.setServicoEscolhido(request.servicoEscolhido().toUpperCase());
        atendimento.setDataAtendimento(request.dataAtendimento());
        atendimento.setPreco(request.preco());


        Atendimento atualizado = atendimentoRepository.save(atendimento);
        return atendimentoMapper.toDtoResponse(atualizado);
    }

    @Override
    @Transactional
    public void deletar(Integer id) {
        Atendimento atendimento = atendimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendimento não encontrado!"));
        atendimentoRepository.delete(atendimento);
    }
}
