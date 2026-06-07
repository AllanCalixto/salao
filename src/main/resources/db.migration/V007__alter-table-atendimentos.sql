ALTER TABLE atendimentos
    ADD COLUMN data_fim DATETIME NULL AFTER data_atendimento,
    ADD COLUMN servico_id INT NULL AFTER data_fim,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO' AFTER servico_id,
    ADD COLUMN versao INT NULL DEFAULT 0 AFTER status,
    ADD CONSTRAINT fk_atendimento_servico FOREIGN KEY (servico_id) REFERENCES servicos(id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Atualiza registros existentes: data_fim = data_atendimento + 30 min para registros sem data_fim
UPDATE atendimentos SET data_fim = DATE_ADD(data_atendimento, INTERVAL 30 MINUTE) WHERE data_fim IS NULL;

-- Agora marca data_fim como NOT NULL
ALTER TABLE atendimentos MODIFY COLUMN data_fim DATETIME NOT NULL;