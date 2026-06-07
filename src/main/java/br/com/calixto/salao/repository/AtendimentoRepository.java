package br.com.calixto.salao.repository;

import br.com.calixto.salao.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Integer> {

    // Verifica se existe conflito de horário para o profissional (sobreposição de intervalos)
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Atendimento a
        WHERE a.profissional.id = :profissionalId
        AND a.status <> 'CANCELADO'
        AND a.dataAtendimento < :dataFim
        AND a.dataFim > :dataInicio
    """)
    boolean existsConflitoProfissional(
            @Param("profissionalId") Integer profissionalId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // Verifica se existe conflito de horário para o cliente (sobreposição de intervalos)
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Atendimento a
        WHERE a.cliente.id = :clienteId
        AND a.status <> 'CANCELADO'
        AND a.dataAtendimento < :dataFim
        AND a.dataFim > :dataInicio
    """)
    boolean existsConflitoCliente(
            @Param("clienteId") Integer clienteId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // Busca todos os atendimentos ativos do profissional em uma data específica
    @Query("""
        SELECT a FROM Atendimento a
        WHERE a.profissional.id = :profissionalId
        AND a.status <> 'CANCELADO'
        AND a.dataAtendimento >= :inicioDia
        AND a.dataAtendimento < :fimDia
        ORDER BY a.dataAtendimento
    """)
    List<Atendimento> findByProfissionalAndDataBetween(
            @Param("profissionalId") Integer profissionalId,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("fimDia") LocalDateTime fimDia
    );

    // Busca todos os atendimentos ativos do cliente em uma data específica
    @Query("""
        SELECT a FROM Atendimento a
        WHERE a.cliente.id = :clienteId
        AND a.status <> 'CANCELADO'
        AND a.dataAtendimento >= :inicioDia
        AND a.dataAtendimento < :fimDia
        ORDER BY a.dataAtendimento
    """)
    List<Atendimento> findByClienteAndDataBetween(
            @Param("clienteId") Integer clienteId,
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("fimDia") LocalDateTime fimDia
    );

    // Mantida para compatibilidade (agora verifica por telefone)
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Atendimento a
        JOIN a.cliente c
        WHERE c.telefone = :telefone
        AND a.status <> 'CANCELADO'
        AND a.dataAtendimento < :dataFim
        AND a.dataFim > :dataInicio
    """)
    boolean existsConflitoClienteTelefone(
            @Param("telefone") String telefone,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );
}