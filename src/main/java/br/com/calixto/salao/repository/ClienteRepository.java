package br.com.calixto.salao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.calixto.salao.model.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer>{
    Optional<Cliente> findByNome(String nome);

    Optional<Cliente> findByTelefone(String telefone);
}
