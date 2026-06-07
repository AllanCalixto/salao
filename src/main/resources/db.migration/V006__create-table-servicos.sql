CREATE TABLE servicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(255) NOT NULL,
    duracao_minutos INT NOT NULL,
    preco DECIMAL(10, 2) NOT NULL
);

INSERT INTO servicos (nome, descricao, duracao_minutos, preco) VALUES
('CORTE DE CABELO', 'Corte de cabelo masculino ou feminino', 30, 45.00),
('ESCOVA', 'Escova modeladora', 45, 60.00),
('HIDRATAÇÃO', 'Hidratação capilar', 40, 50.00),
('COLORAÇÃO', 'Coloração completa', 120, 120.00),
('MANICURE', 'Alongamento e pintura de unhas', 45, 35.00),
('PEDICURE', 'Cuidados com os pés', 40, 35.00),
('MANICURE E PEDICURE', 'Combo mãos e pés', 60, 60.00),
('DEPILAÇÃO', 'Depilação com cera', 30, 40.00),
('SOBRANCELHA', 'Design de sobrancelhas', 20, 25.00),
('LUZES', 'Luzes e mechas', 120, 150.00);