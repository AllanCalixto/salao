package br.com.calixto.salao.repository;

import br.com.calixto.salao.model.ProfissionalDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalDisponibilidadeRepository extends JpaRepository<ProfissionalDisponibilidade, Integer> {
    List<ProfissionalDisponibilidade> findByProfissionalIdOrderByDiaSemana(Integer profissionalId);
    List<ProfissionalDisponibilidade> findByProfissionalIdAndAtivoTrue(Integer profissionalId);
}