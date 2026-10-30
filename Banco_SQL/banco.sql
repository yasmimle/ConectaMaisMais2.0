-- ============================================================
-- BANCO DE DADOS COMPLETO - Conecta++ (WEB + APP MOBILE)
--
-- PARTE 1: banco do site (igual ao script original do web)
-- PARTE 2: tabelas e regras do app mobile
--
-- ATENÇÃO: este script APAGA o banco dbSistemaDoacao (se ele já
-- existir) e cria tudo de novo, já com os dados iniciais.
-- Tudo que estiver cadastrado nele será perdido.
--
-- Como usar: abra no SQL Server Management Studio (SSMS),
-- conecte no servidor (ex.: localhost\SQLEXPRESS) e aperte F5.
-- ============================================================

USE MASTER;
GO

IF EXISTS (
    SELECT *
    FROM SYS.DATABASES
    WHERE NAME = 'dbSistemaDoacao'
)
BEGIN
    ALTER DATABASE dbSistemaDoacao
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

    DROP DATABASE dbSistemaDoacao;
END
GO

CREATE DATABASE dbSistemaDoacao;
GO

USE dbSistemaDoacao;
GO


-- ############################################################
-- PARTE 1 - WEB
-- ############################################################

-- ============================================================
-- TABELAS
-- ============================================================

CREATE TABLE ONG (

    ID_ONG INT IDENTITY(1,1) PRIMARY KEY,

    DSC_NOME VARCHAR(255) NOT NULL,

    NUM_TELEFONE VARCHAR(15) NOT NULL,

    DSC_EMAIL VARCHAR(100) NOT NULL,

    DSC_ENDERECO VARCHAR(150) NOT NULL,

    NUM_CNPJ CHAR(14) NOT NULL

);
GO


CREATE TABLE ADMIN (

    ID_ADMIN INT IDENTITY(1,1) PRIMARY KEY,

    ID_ONG INT NOT NULL,

    DSC_NOME VARCHAR(100) NOT NULL,

    DSC_EMAIL VARCHAR(100) NOT NULL,

    DSC_SENHA VARCHAR(255) NOT NULL,

    -- celular do administrador ("Editar perfil" do site)
    NUM_TELEFONE VARCHAR(15) NULL,

    CONSTRAINT FK_ADMIN_ONG
    FOREIGN KEY (ID_ONG)
    REFERENCES ONG(ID_ONG)

);
GO


CREATE TABLE ATENDENTE (

    ID_ATENDENTE INT IDENTITY(1,1) PRIMARY KEY,

    ID_ONG INT NOT NULL,

    DSC_NOME VARCHAR(100) NOT NULL,

    DSC_EMAIL VARCHAR(100) NOT NULL,

    DSC_SENHA VARCHAR(255) NOT NULL,

    CONSTRAINT FK_ATENDENTE_ONG
    FOREIGN KEY (ID_ONG)
    REFERENCES ONG(ID_ONG)

);
GO


CREATE TABLE UF (

    ID_UF INT IDENTITY(1,1) PRIMARY KEY,

    SIGLA_UF CHAR(2) NOT NULL

);
GO


CREATE TABLE USUARIO (

    ID_USUARIO INT IDENTITY(1,1) PRIMARY KEY,

    ID_ADMIN INT NOT NULL,

    ID_ATENDENTE INT NOT NULL,

    ID_UF INT NOT NULL,

    DSC_NOME VARCHAR(100) NOT NULL,

    NUM_CPF CHAR(11) NOT NULL,

    NUM_TELEFONE VARCHAR(15) NOT NULL,

    DSC_EMAIL VARCHAR(100) NULL,

    DSC_ENDERECO VARCHAR(150) NOT NULL,

    DSC_CIDADE VARCHAR(50) NOT NULL,

    DATA_NASCIMENTO DATE NOT NULL,

    CONSTRAINT FK_USUARIO_ADMIN
    FOREIGN KEY (ID_ADMIN)
    REFERENCES ADMIN(ID_ADMIN),

    CONSTRAINT FK_USUARIO_ATENDENTE
    FOREIGN KEY (ID_ATENDENTE)
    REFERENCES ATENDENTE(ID_ATENDENTE),

    CONSTRAINT FK_USUARIO_UF
    FOREIGN KEY (ID_UF)
    REFERENCES UF(ID_UF)

);
GO


CREATE TABLE STATUS (

    ID_STATUS INT IDENTITY(1,1) PRIMARY KEY,

    DSC_STATUS VARCHAR(50) NOT NULL

);
GO


CREATE TABLE INTERESSE (

    ID_INTERESSE INT IDENTITY(1,1) PRIMARY KEY,

    ID_USUARIO INT NOT NULL,

    ID_STATUS INT NOT NULL,

    -- valores usados pelo sistema: doar, receber ou ambos
    DSC_TIPO VARCHAR(255) NOT NULL,

    DSC_DESCRICAO VARCHAR(255) NULL,

    DATA_REGISTRO DATE NOT NULL,

    CONSTRAINT FK_INTERESSE_USUARIO
    FOREIGN KEY (ID_USUARIO)
    REFERENCES USUARIO(ID_USUARIO),

    CONSTRAINT FK_INTERESSE_STATUS
    FOREIGN KEY (ID_STATUS)
    REFERENCES STATUS(ID_STATUS)

);
GO


-- ============================================================
-- DADOS INICIAIS DO WEB (o sistema precisa deles para funcionar)
-- ============================================================

-- ONG (o sistema sempre usa a ONG de ID 1) - mesmos dados do site
INSERT INTO ONG (DSC_NOME, NUM_TELEFONE, DSC_EMAIL, DSC_ENDERECO, NUM_CNPJ)
VALUES ('Conecta++', '11919100860', 'contato@ong.com',
        'Rua Gugu, São Paulo SP', '12345678000199');
GO

-- Estados: a ordem importa, o cadastro usa a numeração 1 a 27
-- (1 = AC ... 25 = SP ... 27 = TO), a mesma do menu do sistema
INSERT INTO UF (SIGLA_UF) VALUES
    ('AC'),
    ('AL'),
    ('AP'),
    ('AM'),
    ('BA'),
    ('CE'),
    ('DF'),
    ('ES'),
    ('GO'),
    ('MA'),
    ('MT'),
    ('MS'),
    ('MG'),
    ('PA'),
    ('PB'),
    ('PR'),
    ('PE'),
    ('PI'),
    ('RJ'),
    ('RN'),
    ('RS'),
    ('RO'),
    ('RR'),
    ('SC'),
    ('SP'),
    ('SE'),
    ('TO');
GO

-- Status do interesse
INSERT INTO STATUS (DSC_STATUS) VALUES
    ('Pendente'),
    ('Em andamento'),
    ('Concluído');
GO


-- ############################################################
-- PARTE 2 - APP MOBILE
-- ############################################################

-- ============================================================
-- PERFIL DE ACESSO (login por perfil)
-- ============================================================

CREATE TABLE PERFIL_ACESSO (

    ID_PERFIL INT IDENTITY(1,1) PRIMARY KEY,

    DSC_PERFIL VARCHAR(50) NOT NULL,

    DSC_PERMISSOES VARCHAR(500) NULL

);
GO

-- Perfis: a ordem importa (1 = Ajudante, 2 = Usuário), pois os
-- valores padrão de AJUDANTE e USUARIO usam esses números
INSERT INTO PERFIL_ACESSO (DSC_PERFIL, DSC_PERMISSOES) VALUES
    ('Ajudante', 'Consultar a própria agenda, gerenciar a própria disponibilidade e concluir ou cancelar atendimentos'),
    ('Usuário',  'Consultar e gerenciar os próprios dados e agendamentos');
GO


-- ============================================================
-- AJUSTES NA TABELA USUARIO (idoso) PARA O APP
-- ============================================================

-- perfil 2 = 'Usuário'. O padrão evita que o cadastro do site
-- precise informar o perfil.
ALTER TABLE USUARIO
ADD ID_PERFIL INT NOT NULL
    CONSTRAINT DF_USUARIO_PERFIL DEFAULT 2;
GO

ALTER TABLE USUARIO
ADD CONSTRAINT FK_USUARIO_PERFIL
    FOREIGN KEY (ID_PERFIL)
    REFERENCES PERFIL_ACESSO(ID_PERFIL);
GO

-- o app faz login só com o CPF, então ele não pode repetir
ALTER TABLE USUARIO
ADD CONSTRAINT UQ_USUARIO_NUM_CPF
    UNIQUE (NUM_CPF);
GO


-- ============================================================
-- TABELAS DO APP
-- ============================================================

CREATE TABLE AJUDANTE (

    ID_AJUDANTE INT IDENTITY(1,1) PRIMARY KEY,

    -- perfil 1 = 'Ajudante'
    ID_PERFIL INT NOT NULL
    CONSTRAINT DF_AJUDANTE_PERFIL DEFAULT 1,

    DSC_NOME VARCHAR(100) NOT NULL,

    -- CPF obrigatório, único e com exatamente 11 dígitos
    CPF VARCHAR(11) NOT NULL,

    FLG_ATIVO BIT NOT NULL
    CONSTRAINT DF_AJUDANTE_ATIVO DEFAULT 1,

    CONSTRAINT UQ_AJUDANTE_CPF
    UNIQUE (CPF),

    CONSTRAINT CK_AJUDANTE_CPF
    CHECK (LEN(CPF) = 11 AND CPF NOT LIKE '%[^0-9]%'),

    CONSTRAINT FK_AJUDANTE_PERFIL
    FOREIGN KEY (ID_PERFIL)
    REFERENCES PERFIL_ACESSO(ID_PERFIL)

);
GO


CREATE TABLE DISPONIBILIDADE_AJUDANTE (

    ID_DISPONIBILIDADE INT IDENTITY(1,1) PRIMARY KEY,

    ID_AJUDANTE INT NOT NULL,

    -- 1 = domingo ... 7 = sábado
    DIA_SEMANA TINYINT NOT NULL,

    HORA_INICIO TIME(0) NOT NULL,

    HORA_FIM TIME(0) NOT NULL,

    CONSTRAINT CK_DISP_DIA_SEMANA
    CHECK (DIA_SEMANA BETWEEN 1 AND 7),

    -- horário de atendimento: 07:00 às 20:00
    CONSTRAINT CK_DISP_HORARIO_ATENDIMENTO
    CHECK (HORA_INICIO >= '07:00' AND HORA_FIM <= '20:00'),

    CONSTRAINT FK_DISPONIBILIDADE_AJUDANTE
    FOREIGN KEY (ID_AJUDANTE)
    REFERENCES AJUDANTE(ID_AJUDANTE)

);
GO


CREATE TABLE AGENDAMENTO (

    ID_AGENDAMENTO INT IDENTITY(1,1) PRIMARY KEY,

    ID_USUARIO INT NOT NULL,

    -- o ajudante pode ser escolhido depois (a API escolhe)
    ID_AJUDANTE INT NULL,

    DATA_AGENDAMENTO DATE NOT NULL,

    HORA_AGENDAMENTO TIME(0) NOT NULL,

    DSC_TIPO VARCHAR(100) NOT NULL,

    -- de onde o idoso sai e para onde vai
    DSC_ORIGEM VARCHAR(250) NULL,

    DSC_LOCAL VARCHAR(250) NULL,

    DSC_OBSERVACAO VARCHAR(500) NULL,

    -- duração prevista em minutos
    DURACAO_MIN INT NULL,

    DISTANCIA_KM DECIMAL(8,2) NULL,

    DSC_STATUS VARCHAR(30) NOT NULL
    CONSTRAINT DF_AGENDAMENTO_STATUS DEFAULT 'Agendado',

    CONSTRAINT CK_AGENDAMENTO_DURACAO
    CHECK (DURACAO_MIN IS NULL OR DURACAO_MIN > 0),

    -- horário permitido: 07:00 às 20:00
    CONSTRAINT CK_AGENDAMENTO_HORARIO
    CHECK (HORA_AGENDAMENTO BETWEEN '07:00' AND '20:00'),

    -- distância máxima: 12 km
    CONSTRAINT CK_AGENDAMENTO_DISTANCIA_MAX
    CHECK (DISTANCIA_KM IS NULL OR DISTANCIA_KM <= 12),

    CONSTRAINT FK_AGENDAMENTO_USUARIO
    FOREIGN KEY (ID_USUARIO)
    REFERENCES USUARIO(ID_USUARIO),

    CONSTRAINT FK_AGENDAMENTO_AJUDANTE
    FOREIGN KEY (ID_AJUDANTE)
    REFERENCES AJUDANTE(ID_AJUDANTE)

);
GO


CREATE TABLE HISTORICO_AGENDAMENTO (

    ID_HISTORICO INT IDENTITY(1,1) PRIMARY KEY,

    ID_AGENDAMENTO INT NOT NULL,

    DSC_STATUS_ANTERIOR VARCHAR(30) NULL,

    DSC_STATUS_NOVO VARCHAR(30) NULL,

    DATA_ALTERACAO DATETIME NOT NULL
    CONSTRAINT DF_HISTORICO_DATA DEFAULT GETDATE(),

    CONSTRAINT FK_HISTORICO_AGENDAMENTO
    FOREIGN KEY (ID_AGENDAMENTO)
    REFERENCES AGENDAMENTO(ID_AGENDAMENTO)

);
GO


-- ============================================================
-- PROCEDURE E TRIGGER DO APP
-- ============================================================

-- Cria um agendamento. Os parâmetros de ajudante, distância e
-- duração são opcionais: a API calcula e escolhe o ajudante.
CREATE PROCEDURE dbo.sp_CriarAgendamento
    @ID_USUARIO INT,
    @DATA_AGENDAMENTO DATE,
    @HORA_AGENDAMENTO TIME(0),
    @DSC_TIPO VARCHAR(100),
    @DSC_LOCAL VARCHAR(250) = NULL,
    @DSC_OBSERVACAO VARCHAR(500) = NULL,
    @DSC_ORIGEM VARCHAR(250) = NULL,
    @DURACAO_MIN INT = NULL,
    @ID_AJUDANTE INT = NULL,
    @DISTANCIA_KM DECIMAL(8,2) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.USUARIO WHERE ID_USUARIO = @ID_USUARIO)
        THROW 52040, 'Usuário não encontrado.', 1;
    IF @ID_AJUDANTE IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.AJUDANTE WHERE ID_AJUDANTE = @ID_AJUDANTE AND FLG_ATIVO = 1)
        THROW 52041, 'Ajudante não encontrado ou inativo.', 1;
    IF @DURACAO_MIN IS NOT NULL AND @DURACAO_MIN <= 0
        THROW 52042, 'A duração deve ser maior que zero.', 1;
    IF @DISTANCIA_KM IS NOT NULL AND @DISTANCIA_KM > 12
        THROW 52043, 'A distância máxima permitida é 12 km.', 1;
    IF @HORA_AGENDAMENTO < '07:00' OR @HORA_AGENDAMENTO > '20:00'
        THROW 52044, 'O horário permitido é das 07:00 às 20:00.', 1;

    INSERT INTO dbo.AGENDAMENTO
        (ID_USUARIO, ID_AJUDANTE, DATA_AGENDAMENTO, HORA_AGENDAMENTO,
         DSC_TIPO, DSC_ORIGEM, DSC_LOCAL, DSC_OBSERVACAO, DURACAO_MIN, DISTANCIA_KM)
    VALUES
        (@ID_USUARIO, @ID_AJUDANTE, @DATA_AGENDAMENTO, @HORA_AGENDAMENTO,
         @DSC_TIPO, @DSC_ORIGEM, @DSC_LOCAL, @DSC_OBSERVACAO, @DURACAO_MIN, @DISTANCIA_KM);

    SELECT CAST(SCOPE_IDENTITY() AS INT) AS ID_AGENDAMENTO;
END;
GO


-- Registra no histórico toda mudança de status do agendamento.
CREATE TRIGGER dbo.TR_AGENDAMENTO_HISTORICO
ON dbo.AGENDAMENTO
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(DSC_STATUS) RETURN;

    INSERT INTO dbo.HISTORICO_AGENDAMENTO
        (ID_AGENDAMENTO, DSC_STATUS_ANTERIOR, DSC_STATUS_NOVO)
    SELECT I.ID_AGENDAMENTO, D.DSC_STATUS, I.DSC_STATUS
    FROM inserted I
    INNER JOIN deleted D ON D.ID_AGENDAMENTO = I.ID_AGENDAMENTO
    WHERE ISNULL(D.DSC_STATUS, '') <> ISNULL(I.DSC_STATUS, '');
END;
GO


-- ============================================================
-- CONFERÊNCIA: deve mostrar 1 ONG, 27 estados, 3 status e 2 perfis
-- ============================================================
SELECT COUNT(*) AS TOTAL_ONG FROM ONG;
SELECT COUNT(*) AS TOTAL_UF  FROM UF;
SELECT ID_STATUS, DSC_STATUS FROM STATUS ORDER BY ID_STATUS;
SELECT ID_PERFIL, DSC_PERFIL FROM PERFIL_ACESSO ORDER BY ID_PERFIL;
GO
