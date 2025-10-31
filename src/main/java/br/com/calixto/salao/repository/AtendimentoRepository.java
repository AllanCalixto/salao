package br.com.calixto.salao.repository;

import br.com.calixto.salao.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Integer> {
}
