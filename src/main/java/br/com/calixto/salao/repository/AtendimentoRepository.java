package br.com.calixto.salao.repository;

import br.com.calixto.salao.model.Atendimento;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Integer> {

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Atendimento a
        WHERE a.cliente.id = :clienteId
        AND a.dataAtendimento = :dataAtendimento
    """)
    boolean existsAtendimentoClienteMesmoHorario(Integer clienteId, LocalDateTime dataAtendimento);


    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Atendimento a
            WHERE a.profissional.id = :profissionalId
            AND a.dataAtendimento = :dataAtendimento
            """)
    boolean existsAtendimentoProfissionalMesmoHorario(Integer profissionalId,  LocalDateTime dataAtendimento);
}
