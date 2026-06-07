package br.com.calixto.salao.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "profissional_disponibilidade",
       uniqueConstraints = @UniqueConstraint(columnNames = {"profissional_id", "dia_semana"}))
public class ProfissionalDisponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana; // 0=DOMINGO, 1=SEGUNDA ... 6=SABADO

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    private Boolean ativo = true;

    public ProfissionalDisponibilidade() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}