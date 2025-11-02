CREATE TABLE profissional_servicos (
    profissional_id INT NOT NULL,
    servico VARCHAR(100) NOT NULL,
    PRIMARY KEY (profissional_id, servico),
    FOREIGN KEY (profissional_id) REFERENCES profissionais (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);