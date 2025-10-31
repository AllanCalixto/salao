package br.com.calixto.salao.repository;

import br.com.calixto.salao.model.Profissional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Integer> {
    Optional<Object> findByNome(String nome);
}
