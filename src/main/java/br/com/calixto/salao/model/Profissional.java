package br.com.calixto.salao.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especialidade especialidade;

    // Lista de serviços de acordo com a especialidade
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "profissional_servicos",
            joinColumns = @JoinColumn(name = "profissional_id")
    )
    @Column(name = "servico")
    private List<String> servicos;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }

    public List<String> getServicos() {
        return servicos;
    }

    public void setServicos(List<String> servicos) {
        this.servicos = servicos;
    }
}
