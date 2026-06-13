package br.com.calixto.salao.repository;

import br.com.calixto.salao.model.ProfissionalDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProfissionalDisponibilidadeRepository extends JpaRepository<ProfissionalDisponibilidade, Integer> {
    List<ProfissionalDisponibilidade> findByProfissionalIdOrderByDiaSemana(Integer profissionalId);
    List<ProfissionalDisponibilidade> findByProfissionalIdAndAtivoTrue(Integer profissionalId);

    @Modifying
    @Query("DELETE FROM ProfissionalDisponibilidade pd WHERE pd.profissional.id = :profissionalId")
    void deleteByProfissionalId(Integer profissionalId);
}