package br.com.calixto.salao.service;

import br.com.calixto.salao.dto.request.AgendamentoPublicoRequest;
import br.com.calixto.salao.dto.request.AtendimentoRequest;
import br.com.calixto.salao.dto.response.AgendamentoRealizadoResponse;
import br.com.calixto.salao.dto.response.AtendimentoResponse;
import br.com.calixto.salao.dto.response.DisponibilidadeResponse;
import br.com.calixto.salao.dto.response.HorarioDisponivel;
import br.com.calixto.salao.exception.AtendimentoException;
import br.com.calixto.salao.exception.ClienteNaoEncontradoException;
import br.com.calixto.salao.exception.ProfissionalNaoEncontradoException;
import br.com.calixto.salao.exception.ServicoInvalidoException;
import br.com.calixto.salao.mapper.AtendimentoMapper;
import br.com.calixto.salao.model.Atendimento;
import br.com.calixto.salao.model.Cliente;
import br.com.calixto.salao.model.Profissional;
import br.com.calixto.salao.model.ProfissionalDisponibilidade;
import br.com.calixto.salao.model.Servico;
import br.com.calixto.salao.repository.AtendimentoRepository;
import br.com.calixto.salao.repository.ClienteRepository;
import br.com.calixto.salao.repository.ProfissionalDisponibilidadeRepository;
import br.com.calixto.salao.repository.ProfissionalRepository;
import br.com.calixto.salao.repository.ServicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AtendimentoServiceImpl implements IAtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final ProfissionalDisponibilidadeRepository disponibilidadeRepository;
    private final AtendimentoMapper atendimentoMapper;

    private static final int INTERVALO_MINUTOS = 30;

    public AtendimentoServiceImpl(AtendimentoRepository atendimentoRepository,
                                  ClienteRepository clienteRepository,
                                  ProfissionalRepository profissionalRepository,
                                  ServicoRepository servicoRepository,
                                  ProfissionalDisponibilidadeRepository disponibilidadeRepository,
                                  AtendimentoMapper atendimentoMapper) {
        this.atendimentoRepository = atendimentoRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.servicoRepository = servicoRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.atendimentoMapper = atendimentoMapper;
    }

    @Override
    @Transactional
    public AtendimentoResponse salvar(AtendimentoRequest atendimentoRequest) {
        Cliente cliente = clienteRepository.findById(atendimentoRequest.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado!"));

        Profissional profissional = profissionalRepository.findById(atendimentoRequest.profissionalId())
                .orElseThrow(() -> new ProfissionalNaoEncontradoException("Profissional não foi encontrado"));

        if (!profissional.getServicos().contains(atendimentoRequest.servicoEscolhido().toUpperCase())) {
            throw new ServicoInvalidoException(
                    "O Serviço " + atendimentoRequest.servicoEscolhido() +
                            " não pertence a especialidade do profissional " + profissional.getNome());
        }

        LocalDateTime dataInicio = atendimentoRequest.dataAtendimento();
        LocalDateTime dataFim = atendimentoRequest.dataFim() != null ?
                atendimentoRequest.dataFim() : dataInicio.plusMinutes(30);

        validarDisponibilidade(profissional.getId(), cliente.getId(), null, dataInicio, dataFim);

        Atendimento atendimento = new Atendimento();
        atendimento.setCliente(cliente);
        atendimento.setProfissional(profissional);
        atendimento.setServicoEscolhido(atendimentoRequest.servicoEscolhido().toUpperCase());
        atendimento.setDataAtendimento(dataInicio);
        atendimento.setDataFim(dataFim);
        atendimento.setPreco(atendimentoRequest.preco());
        atendimento.setStatus("AGENDADO");

        try {
            Atendimento salvo = atendimentoRepository.save(atendimento);
            return atendimentoMapper.toDtoResponse(salvo);
        } catch (OptimisticLockingFailureException e) {
            throw new AtendimentoException("Conflito ao agendar. Tente novamente.");
        }
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

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado!"));

        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new ProfissionalNaoEncontradoException("Profissional não encontrado!"));

        if (!profissional.getServicos().contains(request.servicoEscolhido().toUpperCase())) {
            throw new ServicoInvalidoException("O Serviço " + request.servicoEscolhido() +
                    " não pertence à especialidade do profissional " + profissional.getNome());
        }

        LocalDateTime dataInicio = request.dataAtendimento();
        LocalDateTime dataFim = request.dataFim() != null ? request.dataFim() : dataInicio.plusMinutes(30);

        validarDisponibilidade(profissional.getId(), cliente.getId(), id, dataInicio, dataFim);

        atendimento.setCliente(cliente);
        atendimento.setProfissional(profissional);
        atendimento.setServicoEscolhido(request.servicoEscolhido().toUpperCase());
        atendimento.setDataAtendimento(dataInicio);
        atendimento.setDataFim(dataFim);
        atendimento.setPreco(request.preco());

        try {
            Atendimento atualizado = atendimentoRepository.save(atendimento);
            return atendimentoMapper.toDtoResponse(atualizado);
        } catch (OptimisticLockingFailureException e) {
            throw new AtendimentoException("Conflito ao atualizar. Tente novamente.");
        }
    }

    @Override
    @Transactional
    public void deletar(Integer id) {
        Atendimento atendimento = atendimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendimento não encontrado!"));
        atendimento.setStatus("CANCELADO");
        atendimentoRepository.save(atendimento);
    }

    // ========== MÉTODOS PÚBLICOS (sem autenticação) ==========

    public DisponibilidadeResponse consultarDisponibilidade(Integer profissionalId, String servicoNome, LocalDate data) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ProfissionalNaoEncontradoException("Profissional não encontrado"));

        if (servicoNome != null && !servicoNome.isEmpty()) {
            String servicoUpper = servicoNome.toUpperCase();
            if (profissional.getServicos().stream().noneMatch(s -> s.equalsIgnoreCase(servicoUpper))) {
                throw new ServicoInvalidoException("O profissional " + profissional.getNome() +
                        " não oferece o serviço " + servicoNome);
            }
        }

        int duracaoMinutos = 30;
        Optional<Servico> servicoOpt = servicoRepository.findByNomeIgnoreCase(servicoNome);
        if (servicoOpt.isPresent()) {
            duracaoMinutos = servicoOpt.get().getDuracaoMinutos();
        }

        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();

        List<Atendimento> agendamentos = atendimentoRepository.findByProfissionalAndDataBetween(
                profissionalId, inicioDia, fimDia);

        List<HorarioDisponivel> horariosDisponiveis = calcularHorariosDisponiveis(
                profissionalId, data, duracaoMinutos, agendamentos);

        String dataFormatada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        return new DisponibilidadeResponse(
                profissionalId, profissional.getNome(),
                servicoNome, duracaoMinutos, dataFormatada, horariosDisponiveis);
    }

    @Transactional
    public AgendamentoRealizadoResponse criarAgendamentoPublico(AgendamentoPublicoRequest request) {
        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new ProfissionalNaoEncontradoException("Profissional não encontrado"));

        String nomeServico = request.servicoNome() != null && !request.servicoNome().isBlank()
                ? request.servicoNome().toUpperCase() : "";

        if (!nomeServico.isEmpty() &&
            profissional.getServicos().stream().noneMatch(s -> s.equalsIgnoreCase(nomeServico))) {
            throw new ServicoInvalidoException(
                    "O profissional " + profissional.getNome() + " não oferece o serviço " + nomeServico);
        }

        Servico servico = servicoRepository.findById(request.servicoId()).orElse(null);
        if (servico == null && !nomeServico.isEmpty()) {
            servico = servicoRepository.findByNomeIgnoreCase(nomeServico).orElse(null);
        }
        if (servico == null) {
            servico = new Servico(nomeServico.isEmpty() ? "SERVIÇO" : nomeServico, "Serviço", 30, 0.0);
            servico = servicoRepository.save(servico);
        }

        LocalDateTime dataInicio = LocalDateTime.of(request.data(), request.horario());
        LocalDateTime dataFim = dataInicio.plusMinutes(servico.getDuracaoMinutos());

        // Valida se o horário está dentro da disponibilidade do profissional
        validarHorarioDisponivel(profissional.getId(), request.data(), request.horario(), dataFim.toLocalTime());

        Cliente cliente = clienteRepository.findByTelefone(request.telefoneCliente())
                .orElseGet(() -> {
                    Cliente novoCliente = new Cliente();
                    novoCliente.setNome(request.nomeCliente());
                    novoCliente.setTelefone(request.telefoneCliente());
                    return clienteRepository.save(novoCliente);
                });

        if (!cliente.getNome().equals(request.nomeCliente())) {
            cliente.setNome(request.nomeCliente());
            clienteRepository.save(cliente);
        }

        validarDisponibilidade(profissional.getId(), cliente.getId(), null, dataInicio, dataFim);

        Atendimento atendimento = new Atendimento();
        atendimento.setCliente(cliente);
        atendimento.setProfissional(profissional);
        atendimento.setServico(servico);
        atendimento.setServicoEscolhido(servico.getNome().toUpperCase());
        atendimento.setPreco(servico.getPreco());
        atendimento.setDataAtendimento(dataInicio);
        atendimento.setDataFim(dataFim);
        atendimento.setStatus("AGENDADO");

        try {
            Atendimento salvo = atendimentoRepository.save(atendimento);
            return atendimentoMapper.toAgendamentoRealizadoResponse(salvo);
        } catch (OptimisticLockingFailureException e) {
            throw new AtendimentoException("Conflito ao agendar. Outra reserva foi feita simultaneamente. Tente novamente.");
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validarDisponibilidade(Integer profissionalId, Integer clienteId,
                                        Integer atendimentoIgnorarId,
                                        LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (atendimentoRepository.existsConflitoProfissional(profissionalId, dataInicio, dataFim)) {
            throw new AtendimentoException("Este profissional já possui atendimento neste horário.");
        }
        if (atendimentoRepository.existsConflitoCliente(clienteId, dataInicio, dataFim)) {
            throw new AtendimentoException("Você já possui um agendamento conflitante.");
        }
    }

    /**
     * Valida se o horário está dentro da disponibilidade semanal configurada pelo profissional
     */
    private void validarHorarioDisponivel(Integer profissionalId, LocalDate data,
                                           LocalTime horarioInicio, LocalTime horarioFim) {
        DayOfWeek diaDaSemana = data.getDayOfWeek();
        int diaSemana = diaDaSemana.getValue() % 7;

        List<ProfissionalDisponibilidade> disponibilidades = disponibilidadeRepository
                .findByProfissionalIdAndAtivoTrue(profissionalId);

        Optional<ProfissionalDisponibilidade> disp = disponibilidades.stream()
                .filter(d -> d.getDiaSemana() == diaSemana)
                .findFirst();

        if (disp.isEmpty()) {
            throw new AtendimentoException("O profissional não atende neste dia da semana.");
        }

        ProfissionalDisponibilidade d = disp.get();
        if (horarioInicio.isBefore(d.getHoraInicio()) || horarioFim.isAfter(d.getHoraFim())) {
            throw new AtendimentoException("Horário fora do expediente do profissional (" +
                    d.getHoraInicio() + " às " + d.getHoraFim() + ")");
        }
    }

    /**
     * Calcula os horários disponíveis baseado na disponibilidade do profissional + agendamentos
     */
    private List<HorarioDisponivel> calcularHorariosDisponiveis(
            Integer profissionalId, LocalDate data, int duracaoMinutos, List<Atendimento> agendamentos) {

        // Busca disponibilidade do profissional para o dia da semana
        DayOfWeek diaDaSemana = data.getDayOfWeek();
        int diaSemana = diaDaSemana.getValue() % 7;

        List<ProfissionalDisponibilidade> disponibilidades = disponibilidadeRepository
                .findByProfissionalIdAndAtivoTrue(profissionalId);

        Optional<ProfissionalDisponibilidade> dispOpt = disponibilidades.stream()
                .filter(d -> d.getDiaSemana() == diaSemana)
                .findFirst();

        // Se não tem disponibilidade configurada, retorna vazio
        if (dispOpt.isEmpty()) {
            return new ArrayList<>();
        }

        ProfissionalDisponibilidade disp = dispOpt.get();
        LocalTime horarioInicio = disp.getHoraInicio();
        LocalTime horarioFim = disp.getHoraFim();
        LocalTime horarioLimite = horarioFim.minusMinutes(duracaoMinutos);

        List<HorarioDisponivel> horarios = new ArrayList<>();
        LocalTime horarioAtual = horarioInicio;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (!horarioAtual.isAfter(horarioLimite)) {
            LocalDateTime inicioSlot = LocalDateTime.of(data, horarioAtual);
            LocalDateTime fimSlot = inicioSlot.plusMinutes(duracaoMinutos);

            boolean disponivel = true;
            for (Atendimento att : agendamentos) {
                if (inicioSlot.isBefore(att.getDataFim()) && fimSlot.isAfter(att.getDataAtendimento())) {
                    disponivel = false;
                    break;
                }
            }

            if (disponivel) {
                horarios.add(new HorarioDisponivel(horarioAtual, horarioAtual.format(formatter)));
            }

            horarioAtual = horarioAtual.plusMinutes(INTERVALO_MINUTOS);
        }

        return horarios;
    }
}