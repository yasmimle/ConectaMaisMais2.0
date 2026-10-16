/* ============================================================
   CONECTA++ - BANCO COMPLEMENTAR - 4º SEMESTRE
   Base: dbSistemaDoacao (banco do 3º semestre)

   IMPORTANTE:
   - Este script NÃO apaga o banco existente.
   - Ele acrescenta as estruturas da melhoria do 4º semestre.
   - Execute no banco dbSistemaDoacao.
   ============================================================ */

USE dbSistemaDoacao;
GO

/* ============================================================
   1. TABELA DE NECESSIDADES
   Registra as necessidades informadas pelos usuários idosos.
   ============================================================ */

IF OBJECT_ID('dbo.NECESSIDADE', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.NECESSIDADE (
        ID_NECESSIDADE INT IDENTITY(1,1) PRIMARY KEY,
        ID_USUARIO INT NOT NULL,
        ID_STATUS INT NOT NULL,
        DSC_TIPO VARCHAR(100) NOT NULL,
        DSC_DESCRICAO VARCHAR(500) NOT NULL,
        DSC_PRIORIDADE VARCHAR(20) NOT NULL
            CONSTRAINT CK_NECESSIDADE_PRIORIDADE
            CHECK (DSC_PRIORIDADE IN ('Baixa','Média','Alta','Urgente')),
        DATA_REGISTRO DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        DATA_ATUALIZACAO DATETIME2 NULL,

        CONSTRAINT FK_NECESSIDADE_USUARIO
            FOREIGN KEY (ID_USUARIO) REFERENCES dbo.USUARIO(ID_USUARIO),
        CONSTRAINT FK_NECESSIDADE_STATUS
            FOREIGN KEY (ID_STATUS) REFERENCES dbo.STATUS(ID_STATUS)
    );
END;
GO

/* ============================================================
   2. TABELA DE AGENDAMENTOS
   Permite registrar consultas e outros compromissos do usuário.
   ============================================================ */

IF OBJECT_ID('dbo.AGENDAMENTO', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.AGENDAMENTO (
        ID_AGENDAMENTO INT IDENTITY(1,1) PRIMARY KEY,
        ID_USUARIO INT NOT NULL,
        DATA_AGENDAMENTO DATE NOT NULL,
        HORA_AGENDAMENTO TIME(0) NOT NULL,
        DSC_TIPO VARCHAR(100) NOT NULL,
        DSC_LOCAL VARCHAR(150) NULL,
        DSC_OBSERVACAO VARCHAR(500) NULL,
        DSC_STATUS VARCHAR(30) NOT NULL DEFAULT 'Agendado'
            CONSTRAINT CK_AGENDAMENTO_STATUS
            CHECK (DSC_STATUS IN ('Agendado','Concluído','Cancelado')),
        DATA_CADASTRO DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

        CONSTRAINT FK_AGENDAMENTO_USUARIO
            FOREIGN KEY (ID_USUARIO) REFERENCES dbo.USUARIO(ID_USUARIO)
    );
END;
GO

/* ============================================================
   3. TABELA DE LEMBRETES
   Lembretes podem estar associados a um agendamento.
   ============================================================ */

IF OBJECT_ID('dbo.LEMBRETE', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.LEMBRETE (
        ID_LEMBRETE INT IDENTITY(1,1) PRIMARY KEY,
        ID_USUARIO INT NOT NULL,
        ID_AGENDAMENTO INT NULL,
        DSC_TITULO VARCHAR(150) NOT NULL,
        DSC_DESCRICAO VARCHAR(500) NULL,
        DATA_LEMBRETE DATE NOT NULL,
        HORA_LEMBRETE TIME(0) NULL,
        DSC_STATUS VARCHAR(20) NOT NULL DEFAULT 'Pendente'
            CONSTRAINT CK_LEMBRETE_STATUS
            CHECK (DSC_STATUS IN ('Pendente','Enviado','Concluído')),
        DATA_CADASTRO DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

        CONSTRAINT FK_LEMBRETE_USUARIO
            FOREIGN KEY (ID_USUARIO) REFERENCES dbo.USUARIO(ID_USUARIO),
        CONSTRAINT FK_LEMBRETE_AGENDAMENTO
            FOREIGN KEY (ID_AGENDAMENTO) REFERENCES dbo.AGENDAMENTO(ID_AGENDAMENTO)
    );
END;
GO

/* ============================================================
   4. TABELA DE CONTEÚDOS
   Conteúdos informativos sobre saúde e prevenção de golpes.
   ============================================================ */

IF OBJECT_ID('dbo.CONTEUDO', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.CONTEUDO (
        ID_CONTEUDO INT IDENTITY(1,1) PRIMARY KEY,
        ID_ADMIN INT NOT NULL,
        DSC_TITULO VARCHAR(200) NOT NULL,
        DSC_CATEGORIA VARCHAR(50) NOT NULL
            CONSTRAINT CK_CONTEUDO_CATEGORIA
            CHECK (DSC_CATEGORIA IN ('Saúde','Prevenção de golpes','Informação')),
        DSC_RESUMO VARCHAR(500) NULL,
        DSC_CONTEUDO VARCHAR(MAX) NOT NULL,
        DATA_PUBLICACAO DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        FLG_ATIVO BIT NOT NULL DEFAULT 1,

        CONSTRAINT FK_CONTEUDO_ADMIN
            FOREIGN KEY (ID_ADMIN) REFERENCES dbo.ADMIN(ID_ADMIN)
    );
END;
GO

/* ============================================================
   5. TABELA DE NOTIFICAÇÕES
   Centraliza avisos destinados ao usuário.
   ============================================================ */

IF OBJECT_ID('dbo.NOTIFICACAO', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.NOTIFICACAO (
        ID_NOTIFICACAO INT IDENTITY(1,1) PRIMARY KEY,
        ID_USUARIO INT NOT NULL,
        ID_AGENDAMENTO INT NULL,
        ID_LEMBRETE INT NULL,
        DSC_TITULO VARCHAR(150) NOT NULL,
        DSC_MENSAGEM VARCHAR(500) NOT NULL,
        DATA_NOTIFICACAO DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        FLG_LIDA BIT NOT NULL DEFAULT 0,

        CONSTRAINT FK_NOTIFICACAO_USUARIO
            FOREIGN KEY (ID_USUARIO) REFERENCES dbo.USUARIO(ID_USUARIO),
        CONSTRAINT FK_NOTIFICACAO_AGENDAMENTO
            FOREIGN KEY (ID_AGENDAMENTO) REFERENCES dbo.AGENDAMENTO(ID_AGENDAMENTO),
        CONSTRAINT FK_NOTIFICACAO_LEMBRETE
            FOREIGN KEY (ID_LEMBRETE) REFERENCES dbo.LEMBRETE(ID_LEMBRETE)
    );
END;
GO

/* ============================================================
   6. TABELA DE PREFERÊNCIAS DE ACESSIBILIDADE
   Guarda preferências de uso do aplicativo pelo usuário.
   ============================================================ */

IF OBJECT_ID('dbo.PREFERENCIA_ACESSIBILIDADE', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.PREFERENCIA_ACESSIBILIDADE (
        ID_PREFERENCIA INT IDENTITY(1,1) PRIMARY KEY,
        ID_USUARIO INT NOT NULL UNIQUE,
        TAMANHO_FONTE VARCHAR(20) NOT NULL DEFAULT 'Médio'
            CONSTRAINT CK_ACESSIBILIDADE_FONTE
            CHECK (TAMANHO_FONTE IN ('Pequeno','Médio','Grande','Muito grande')),
        FLG_ALTO_CONTRASTE BIT NOT NULL DEFAULT 0,
        FLG_LEITURA_VOZ BIT NOT NULL DEFAULT 0,
        FLG_NOTIFICACAO_SONORA BIT NOT NULL DEFAULT 1,

        CONSTRAINT FK_ACESSIBILIDADE_USUARIO
            FOREIGN KEY (ID_USUARIO) REFERENCES dbo.USUARIO(ID_USUARIO)
    );
END;
GO

/* ============================================================
   7. ÍNDICES PARA CONSULTAS FREQUENTES
   ============================================================ */

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_NECESSIDADE_USUARIO')
    CREATE INDEX IX_NECESSIDADE_USUARIO ON dbo.NECESSIDADE(ID_USUARIO);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_AGENDAMENTO_USUARIO_DATA')
    CREATE INDEX IX_AGENDAMENTO_USUARIO_DATA ON dbo.AGENDAMENTO(ID_USUARIO, DATA_AGENDAMENTO);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_LEMBRETE_USUARIO_DATA')
    CREATE INDEX IX_LEMBRETE_USUARIO_DATA ON dbo.LEMBRETE(ID_USUARIO, DATA_LEMBRETE);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_NOTIFICACAO_USUARIO')
    CREATE INDEX IX_NOTIFICACAO_USUARIO ON dbo.NOTIFICACAO(ID_USUARIO, FLG_LIDA);
GO

/* ============================================================
   8. PROCEDURE - CADASTRAR NECESSIDADE
   ============================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_CadastrarNecessidade
    @ID_USUARIO INT,
    @DSC_TIPO VARCHAR(100),
    @DSC_DESCRICAO VARCHAR(500),
    @DSC_PRIORIDADE VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.USUARIO WHERE ID_USUARIO = @ID_USUARIO)
        THROW 50001, 'Usuário não encontrado.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.STATUS WHERE ID_STATUS = 1)
        THROW 50002, 'Status Pendente não encontrado.', 1;

    INSERT INTO dbo.NECESSIDADE
        (ID_USUARIO, ID_STATUS, DSC_TIPO, DSC_DESCRICAO, DSC_PRIORIDADE)
    VALUES
        (@ID_USUARIO, 1, @DSC_TIPO, @DSC_DESCRICAO, @DSC_PRIORIDADE);

    SELECT CAST(SCOPE_IDENTITY() AS INT) AS ID_NECESSIDADE;
END;
GO

/* ============================================================
   9. PROCEDURE - CRIAR AGENDAMENTO
   ============================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_CriarAgendamento
    @ID_USUARIO INT,
    @DATA_AGENDAMENTO DATE,
    @HORA_AGENDAMENTO TIME(0),
    @DSC_TIPO VARCHAR(100),
    @DSC_LOCAL VARCHAR(150) = NULL,
    @DSC_OBSERVACAO VARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.USUARIO WHERE ID_USUARIO = @ID_USUARIO)
        THROW 50003, 'Usuário não encontrado.', 1;

    INSERT INTO dbo.AGENDAMENTO
        (ID_USUARIO, DATA_AGENDAMENTO, HORA_AGENDAMENTO, DSC_TIPO, DSC_LOCAL, DSC_OBSERVACAO)
    VALUES
        (@ID_USUARIO, @DATA_AGENDAMENTO, @HORA_AGENDAMENTO, @DSC_TIPO, @DSC_LOCAL, @DSC_OBSERVACAO);

    SELECT CAST(SCOPE_IDENTITY() AS INT) AS ID_AGENDAMENTO;
END;
GO

/* ============================================================
   10. PROCEDURE - CONSULTAR NECESSIDADES DO USUÁRIO
   ============================================================ */

CREATE OR ALTER PROCEDURE dbo.sp_ConsultarNecessidadesUsuario
    @ID_USUARIO INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        N.ID_NECESSIDADE,
        N.DSC_TIPO,
        N.DSC_DESCRICAO,
        N.DSC_PRIORIDADE,
        S.DSC_STATUS,
        N.DATA_REGISTRO,
        N.DATA_ATUALIZACAO
    FROM dbo.NECESSIDADE AS N
    INNER JOIN dbo.STATUS AS S
        ON S.ID_STATUS = N.ID_STATUS
    WHERE N.ID_USUARIO = @ID_USUARIO
    ORDER BY N.DATA_REGISTRO DESC;
END;
GO

/* ============================================================
   11. TRIGGER - ATUALIZA DATA DE ALTERAÇÃO DA NECESSIDADE
   ============================================================ */

CREATE OR ALTER TRIGGER dbo.TR_NECESSIDADE_ATUALIZACAO
ON dbo.NECESSIDADE
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF TRIGGER_NESTLEVEL() > 1
        RETURN;

    UPDATE N
       SET DATA_ATUALIZACAO = SYSDATETIME()
    FROM dbo.NECESSIDADE AS N
    INNER JOIN inserted AS I
        ON I.ID_NECESSIDADE = N.ID_NECESSIDADE;
END;
GO

/* ============================================================
   12. TRIGGER - CRIA NOTIFICAÇÃO QUANDO UM AGENDAMENTO É CRIADO
   ============================================================ */

CREATE OR ALTER TRIGGER dbo.TR_AGENDAMENTO_NOTIFICACAO
ON dbo.AGENDAMENTO
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.NOTIFICACAO
        (ID_USUARIO, ID_AGENDAMENTO, DSC_TITULO, DSC_MENSAGEM)
    SELECT
        I.ID_USUARIO,
        I.ID_AGENDAMENTO,
        'Novo agendamento',
        'Seu agendamento de ' + I.DSC_TIPO + ' foi registrado para ' +
        CONVERT(VARCHAR(10), I.DATA_AGENDAMENTO, 103) + '.'
    FROM inserted AS I;
END;
GO

/* ============================================================
   13. TRIGGER - CRIA NOTIFICAÇÃO QUANDO UM LEMBRETE É CADASTRADO
   ============================================================ */

CREATE OR ALTER TRIGGER dbo.TR_LEMBRETE_NOTIFICACAO
ON dbo.LEMBRETE
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.NOTIFICACAO
        (ID_USUARIO, ID_LEMBRETE, DSC_TITULO, DSC_MENSAGEM)
    SELECT
        I.ID_USUARIO,
        I.ID_LEMBRETE,
        I.DSC_TITULO,
        ISNULL(I.DSC_DESCRICAO, 'Novo lembrete cadastrado.')
    FROM inserted AS I;
END;
GO

/* ============================================================
   14. DADOS DE EXEMPLO
   Inseridos somente se houver usuário/admin para referência.
   ============================================================ */

DECLARE @ID_ADMIN INT = (SELECT MIN(ID_ADMIN) FROM dbo.ADMIN);
DECLARE @ID_USUARIO INT = (SELECT MIN(ID_USUARIO) FROM dbo.USUARIO);

IF @ID_ADMIN IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.CONTEUDO)
BEGIN
    INSERT INTO dbo.CONTEUDO
        (ID_ADMIN, DSC_TITULO, DSC_CATEGORIA, DSC_RESUMO, DSC_CONTEUDO)
    VALUES
        (@ID_ADMIN,
         'Cuidados com a saúde na terceira idade',
         'Saúde',
         'Informações gerais para o cuidado e acompanhamento da saúde.',
         'Conteúdo informativo sobre cuidados, acompanhamento médico e hábitos saudáveis.');

    INSERT INTO dbo.CONTEUDO
        (ID_ADMIN, DSC_TITULO, DSC_CATEGORIA, DSC_RESUMO, DSC_CONTEUDO)
    VALUES
        (@ID_ADMIN,
         'Como evitar golpes',
         'Prevenção de golpes',
         'Orientações para reconhecer situações suspeitas.',
         'Nunca compartilhe senhas ou códigos de confirmação e desconfie de solicitações inesperadas.');
END;

IF @ID_USUARIO IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.PREFERENCIA_ACESSIBILIDADE WHERE ID_USUARIO = @ID_USUARIO)
        INSERT INTO dbo.PREFERENCIA_ACESSIBILIDADE (ID_USUARIO)
        VALUES (@ID_USUARIO);
END;
GO

/* ============================================================
   15. CONSULTAS DE CONFERÊNCIA
   ============================================================ */

SELECT 'NECESSIDADE' AS TABELA, COUNT(*) AS QUANTIDADE FROM dbo.NECESSIDADE
UNION ALL
SELECT 'AGENDAMENTO', COUNT(*) FROM dbo.AGENDAMENTO
UNION ALL
SELECT 'LEMBRETE', COUNT(*) FROM dbo.LEMBRETE
UNION ALL
SELECT 'CONTEUDO', COUNT(*) FROM dbo.CONTEUDO
UNION ALL
SELECT 'NOTIFICACAO', COUNT(*) FROM dbo.NOTIFICACAO
UNION ALL
SELECT 'PREFERENCIA_ACESSIBILIDADE', COUNT(*) FROM dbo.PREFERENCIA_ACESSIBILIDADE;
GO

SELECT name AS OBJETO, type_desc AS TIPO
FROM sys.objects
WHERE name IN (
    'sp_CadastrarNecessidade',
    'sp_CriarAgendamento',
    'sp_ConsultarNecessidadesUsuario',
    'TR_NECESSIDADE_ATUALIZACAO',
    'TR_AGENDAMENTO_NOTIFICACAO',
    'TR_LEMBRETE_NOTIFICACAO'
)
ORDER BY type_desc, name;
GO
