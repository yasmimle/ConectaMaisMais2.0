-- ============================================================
-- BANCO DE DADOS COMPLETO - Sistema de Apoio ao Idoso (Conecta++)
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
-- DADOS INICIAIS (o sistema precisa deles para funcionar)
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


-- ============================================================
-- CONFERÊNCIA: deve mostrar 1 ONG, 27 estados e 3 status
-- ============================================================
SELECT COUNT(*) AS TOTAL_ONG FROM ONG;
SELECT COUNT(*) AS TOTAL_UF  FROM UF;
SELECT ID_STATUS, DSC_STATUS FROM STATUS ORDER BY ID_STATUS;
GO
