package br.com.calixto.salao.repository;

import br.com.calixto.salao.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {
    Optional<Servico> findByNomeIgnoreCase(String nome);
}