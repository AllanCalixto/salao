-- Atualiza durações conforme solicitado
UPDATE servicos SET duracao_minutos = 45 WHERE nome = 'MANICURE' AND duracao_minutos <> 45;
UPDATE servicos SET duracao_minutos = 45 WHERE nome = 'PEDICURE' AND duracao_minutos <> 45;
UPDATE servicos SET duracao_minutos = 90 WHERE nome = 'MANICURE E PEDICURE' AND duracao_minutos <> 90;