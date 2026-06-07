CREATE TABLE profissional_disponibilidade (
    id INT AUTO_INCREMENT PRIMARY KEY,
    profissional_id INT NOT NULL,
    dia_semana TINYINT NOT NULL COMMENT '0=DOMINGO, 1=SEGUNDA, ... 6=SABADO',
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_disponibilidade_profissional FOREIGN KEY (profissional_id)
        REFERENCES profissionais(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT unique_profissional_dia UNIQUE (profissional_id, dia_semana)
);

-- Insere disponibilidade padrão para todos os profissionais existentes
-- Segunda a Sexta: 08:00-18:00, Sabado: 08:00-12:00
INSERT INTO profissional_disponibilidade (profissional_id, dia_semana, hora_inicio, hora_fim, ativo)
SELECT p.id, 1, '08:00', '18:00', true FROM profissionais p;

INSERT INTO profissional_disponibilidade (profissional_id, dia_semana, hora_inicio, hora_fim, ativo)
SELECT p.id, 2, '08:00', '18:00', true FROM profissionais p;

INSERT INTO profissional_disponibilidade (profissional_id, dia_semana, hora_inicio, hora_fim, ativo)
SELECT p.id, 3, '08:00', '18:00', true FROM profissionais p;

INSERT INTO profissional_disponibilidade (profissional_id, dia_semana, hora_inicio, hora_fim, ativo)
SELECT p.id, 4, '08:00', '18:00', true FROM profissionais p;

INSERT INTO profissional_disponibilidade (profissional_id, dia_semana, hora_inicio, hora_fim, ativo)
SELECT p.id, 5, '08:00', '18:00', true FROM profissionais p;

INSERT INTO profissional_disponibilidade (profissional_id, dia_semana, hora_inicio, hora_fim, ativo)
SELECT p.id, 6, '08:00', '12:00', true FROM profissionais p;