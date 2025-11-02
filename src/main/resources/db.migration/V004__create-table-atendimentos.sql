CREATE TABLE atendimentos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    profissional_id INT NOT NULL,
    servico_escolhido VARCHAR(100) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    data_atendimento DATETIME NOT NULL,
    CONSTRAINT fk_atendimento_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_atendimento_profissional FOREIGN KEY (profissional_id)
        REFERENCES profissionais (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);