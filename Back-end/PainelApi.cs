using System.Data;
using System.Data.SqlClient;
using System.Globalization;

// Rotas do site (painel do admin e do atendente): usuarios, interesses,
// administradores, atendentes e dados da ONG.
public static class PainelApi
{
    // ---------------------------------------------------------------
    // Funcoes de apoio
    // ---------------------------------------------------------------

    static string Texto(object valor)
    {
        return valor == DBNull.Value ? "" : valor.ToString();
    }

    static object ValorOuNulo(string texto)
    {
        return string.IsNullOrWhiteSpace(texto) ? (object)DBNull.Value : texto.Trim();
    }

    static string SoNumeros(string texto)
    {
        return new string((texto ?? "").Where(char.IsDigit).ToArray());
    }

    static bool TemColuna(SqlDataReader reader, string nome)
    {
        for (int i = 0; i < reader.FieldCount; i++)
            if (string.Equals(reader.GetName(i), nome, StringComparison.OrdinalIgnoreCase))
                return true;
        return false;
    }

    static int Contar(SqlConnection conn, string sql, string nomeParametro, object valor)
    {
        using var cmd = new SqlCommand(sql, conn);
        cmd.Parameters.AddWithValue(nomeParametro, valor);
        return Convert.ToInt32(cmd.ExecuteScalar());
    }

    // ---------------------------------------------------------------
    // Administradores e atendentes (mesma logica, tabelas diferentes)
    // ---------------------------------------------------------------

    static void MapConta(WebApplication app, string rota, string tabela, bool temTelefone, string nomeSingular)
    {
        // Lista (nunca devolve a senha)
        app.MapGet(rota, (Banco banco) =>
        {
            var lista = new List<object>();
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand("SELECT * FROM " + tabela + " ORDER BY DSC_NOME", conn);
            using var reader = cmd.ExecuteReader();
            bool colunaTelefone = TemColuna(reader, "NUM_TELEFONE");
            while (reader.Read())
            {
                lista.Add(new
                {
                    nome = Texto(reader["DSC_NOME"]),
                    email = Texto(reader["DSC_EMAIL"]),
                    telefone = colunaTelefone ? Texto(reader["NUM_TELEFONE"]) : ""
                });
            }
            return Results.Ok(lista);
        });

        // Cria
        app.MapPost(rota, (Banco banco, NovaConta dados) =>
        {
            var nome = (dados.Nome ?? "").Trim();
            var email = (dados.Email ?? "").Trim();
            var senha = dados.Senha ?? "";

            if (nome == "" || !email.Contains("@") || senha == "")
                return Results.BadRequest(new { erro = "Preencha nome, e-mail e senha." });

            using var conn = banco.AbrirConexao();

            if (Contar(conn, "SELECT COUNT(*) FROM " + tabela + " WHERE DSC_EMAIL = @e", "@e", email) > 0)
                return Results.Conflict(new { erro = "Email já cadastrado." });

            using var cmd = new SqlCommand(
                "INSERT INTO " + tabela + " (ID_ONG, DSC_NOME, DSC_EMAIL, DSC_SENHA) VALUES (1, @nome, @email, @senha)", conn);
            cmd.Parameters.AddWithValue("@nome", nome);
            cmd.Parameters.AddWithValue("@email", email);
            cmd.Parameters.AddWithValue("@senha", BCrypt.Net.BCrypt.HashPassword(senha));
            cmd.ExecuteNonQuery();

            return Results.Ok(new { nome, email });
        });

        // Edita (so muda o que vier preenchido). A pessoa e achada pelo e-mail atual: ?email=...
        app.MapPut(rota, (Banco banco, string email, EdicaoConta dados) =>
        {
            if (string.IsNullOrWhiteSpace(email))
                return Results.BadRequest(new { erro = "Informe o e-mail atual (?email=...)." });

            using var conn = banco.AbrirConexao();

            if (Contar(conn, "SELECT COUNT(*) FROM " + tabela + " WHERE DSC_EMAIL = @e", "@e", email) == 0)
                return Results.NotFound(new { erro = nomeSingular + " não encontrado." });

            var novoEmail = (dados.Email ?? "").Trim();
            if (novoEmail != "" &&
                !string.Equals(novoEmail, email, StringComparison.OrdinalIgnoreCase) &&
                Contar(conn, "SELECT COUNT(*) FROM " + tabela + " WHERE DSC_EMAIL = @e", "@e", novoEmail) > 0)
                return Results.Conflict(new { erro = "Email já cadastrado." });

            var sets = new List<string>();
            using var cmd = new SqlCommand();
            cmd.Connection = conn;

            if (!string.IsNullOrWhiteSpace(dados.Nome))
            {
                sets.Add("DSC_NOME = @nome");
                cmd.Parameters.AddWithValue("@nome", dados.Nome.Trim());
            }
            if (novoEmail != "")
            {
                sets.Add("DSC_EMAIL = @novoEmail");
                cmd.Parameters.AddWithValue("@novoEmail", novoEmail);
            }
            if (!string.IsNullOrEmpty(dados.Senha))
            {
                sets.Add("DSC_SENHA = @senha");
                cmd.Parameters.AddWithValue("@senha", BCrypt.Net.BCrypt.HashPassword(dados.Senha));
            }
            if (temTelefone && !string.IsNullOrWhiteSpace(dados.Telefone))
            {
                sets.Add("NUM_TELEFONE = @tel");
                cmd.Parameters.AddWithValue("@tel", SoNumeros(dados.Telefone));
            }

            if (sets.Count == 0)
                return Results.BadRequest(new { erro = "Nada para alterar." });

            cmd.CommandText = "UPDATE " + tabela + " SET " + string.Join(", ", sets) + " WHERE DSC_EMAIL = @emailAtual";
            cmd.Parameters.AddWithValue("@emailAtual", email);

            try
            {
                cmd.ExecuteNonQuery();
            }
            catch (SqlException ex)
            {
                return Results.BadRequest(new { erro = ex.Message });
            }

            return Results.Ok(new { email = novoEmail != "" ? novoEmail : email });
        });

        // Remove: ?email=...
        app.MapDelete(rota, (Banco banco, string email) =>
        {
            using var conn = banco.AbrirConexao();

            if (tabela == "ADMIN" && Contar(conn, "SELECT COUNT(*) FROM ADMIN WHERE 1 = @x", "@x", 1) <= 1)
                return Results.Conflict(new { erro = "Precisa existir pelo menos um administrador." });

            try
            {
                using var cmd = new SqlCommand("DELETE FROM " + tabela + " WHERE DSC_EMAIL = @e", conn);
                cmd.Parameters.AddWithValue("@e", email);
                if (cmd.ExecuteNonQuery() == 0)
                    return Results.NotFound(new { erro = nomeSingular + " não encontrado." });
            }
            catch (SqlException)
            {
                return Results.Conflict(new { erro = nomeSingular + " está vinculado a usuários e não pode ser removido." });
            }

            return Results.Ok();
        });
    }

    // ---------------------------------------------------------------
    // Rotas
    // ---------------------------------------------------------------

    public static void MapPainel(this WebApplication app)
    {
        MapConta(app, "/administradores", "ADMIN", true, "Administrador");
        MapConta(app, "/atendentes", "ATENDENTE", false, "Atendente");

        // Editar usuario (telefone, e-mail, endereco)
        app.MapPut("/usuarios/{id}", (Banco banco, int id, EdicaoUsuario dados) =>
        {
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand(@"
                UPDATE USUARIO
                SET NUM_TELEFONE = ISNULL(@tel, NUM_TELEFONE),
                    DSC_EMAIL    = ISNULL(@email, DSC_EMAIL),
                    DSC_ENDERECO = ISNULL(@end, DSC_ENDERECO)
                WHERE ID_USUARIO = @id", conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.AddWithValue("@tel",
                string.IsNullOrWhiteSpace(dados.Telefone) ? (object)DBNull.Value : SoNumeros(dados.Telefone));
            cmd.Parameters.AddWithValue("@email", ValorOuNulo(dados.Email));
            cmd.Parameters.AddWithValue("@end", ValorOuNulo(dados.Endereco));

            return cmd.ExecuteNonQuery() > 0
                ? Results.Ok()
                : Results.NotFound(new { erro = "Usuário não encontrado." });
        });

        // Lista de interesses (com o nome do usuario e o nome do status)
        app.MapGet("/interesses", (Banco banco) =>
        {
            var lista = new List<object>();
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand(@"
                SELECT I.ID_INTERESSE, I.ID_USUARIO, U.DSC_NOME, I.DSC_TIPO, I.DSC_DESCRICAO,
                       S.DSC_STATUS, I.DATA_REGISTRO
                FROM INTERESSE I
                INNER JOIN USUARIO U ON U.ID_USUARIO = I.ID_USUARIO
                INNER JOIN STATUS S ON S.ID_STATUS = I.ID_STATUS
                ORDER BY I.DATA_REGISTRO DESC, I.ID_INTERESSE DESC", conn);
            using var reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                lista.Add(new
                {
                    id = Convert.ToInt32(reader["ID_INTERESSE"]),
                    idUsuario = Convert.ToInt32(reader["ID_USUARIO"]),
                    nome = Texto(reader["DSC_NOME"]),
                    tipo = Texto(reader["DSC_TIPO"]),
                    necessidade = Texto(reader["DSC_DESCRICAO"]),
                    status = Texto(reader["DSC_STATUS"]),
                    dataSolicitacao = Convert.ToDateTime(reader["DATA_REGISTRO"]).ToString("dd/MM/yyyy")
                });
            }
            return Results.Ok(lista);
        });

        // Novo interesse (o status comeca como Pendente)
        app.MapPost("/interesses", (Banco banco, NovoInteresse dados) =>
        {
            var tipo = (dados.Tipo ?? "").Trim().ToLowerInvariant();
            if (tipo != "doar" && tipo != "receber" && tipo != "ambos")
                return Results.BadRequest(new { erro = "Tipo inválido. Use doar, receber ou ambos." });

            using var conn = banco.AbrirConexao();

            if (Contar(conn, "SELECT COUNT(*) FROM USUARIO WHERE ID_USUARIO = @i", "@i", dados.IdUsuario) == 0)
                return Results.NotFound(new { erro = "Usuário não encontrado." });

            using var cmd = new SqlCommand(@"
                INSERT INTO INTERESSE (ID_USUARIO, ID_STATUS, DSC_TIPO, DSC_DESCRICAO, DATA_REGISTRO)
                VALUES (@u, (SELECT TOP 1 ID_STATUS FROM STATUS WHERE DSC_STATUS COLLATE Latin1_General_CI_AI = 'Pendente'),
                        @tipo, @desc, CAST(GETDATE() AS DATE));
                SELECT CAST(SCOPE_IDENTITY() AS INT);", conn);
            cmd.Parameters.AddWithValue("@u", dados.IdUsuario);
            cmd.Parameters.AddWithValue("@tipo", tipo);
            cmd.Parameters.AddWithValue("@desc", ValorOuNulo(dados.Descricao));

            try
            {
                return Results.Ok(new { id = Convert.ToInt32(cmd.ExecuteScalar()) });
            }
            catch (SqlException ex)
            {
                return Results.BadRequest(new { erro = ex.Message });
            }
        });

        // Editar interesse: so muda o que vier preenchido (tipo, descricao, status)
        app.MapPut("/interesses/{id}", (Banco banco, int id, EdicaoInteresse dados) =>
        {
            using var conn = banco.AbrirConexao();

            object idStatus = DBNull.Value;
            if (!string.IsNullOrWhiteSpace(dados.Status))
            {
                using var buscaStatus = new SqlCommand(
                    "SELECT TOP 1 ID_STATUS FROM STATUS WHERE DSC_STATUS COLLATE Latin1_General_CI_AI = @s", conn);
                buscaStatus.Parameters.AddWithValue("@s", dados.Status.Trim());
                var achado = buscaStatus.ExecuteScalar();
                if (achado == null)
                    return Results.BadRequest(new { erro = "Status inválido. Use Pendente, Em andamento ou Concluído." });
                idStatus = Convert.ToInt32(achado);
            }

            object tipo = DBNull.Value;
            if (!string.IsNullOrWhiteSpace(dados.Tipo))
            {
                var t = dados.Tipo.Trim().ToLowerInvariant();
                if (t != "doar" && t != "receber" && t != "ambos")
                    return Results.BadRequest(new { erro = "Tipo inválido. Use doar, receber ou ambos." });
                tipo = t;
            }

            using var cmd = new SqlCommand(@"
                UPDATE INTERESSE
                SET ID_STATUS     = ISNULL(@status, ID_STATUS),
                    DSC_TIPO      = ISNULL(@tipo, DSC_TIPO),
                    DSC_DESCRICAO = ISNULL(@desc, DSC_DESCRICAO)
                WHERE ID_INTERESSE = @id", conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.AddWithValue("@status", idStatus);
            cmd.Parameters.AddWithValue("@tipo", tipo);
            cmd.Parameters.AddWithValue("@desc", ValorOuNulo(dados.Descricao));

            return cmd.ExecuteNonQuery() > 0
                ? Results.Ok()
                : Results.NotFound(new { erro = "Interesse não encontrado." });
        });

        // Remover interesse
        app.MapDelete("/interesses/{id}", (Banco banco, int id) =>
        {
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand("DELETE FROM INTERESSE WHERE ID_INTERESSE = @id", conn);
            cmd.Parameters.AddWithValue("@id", id);
            return cmd.ExecuteNonQuery() > 0
                ? Results.Ok()
                : Results.NotFound(new { erro = "Interesse não encontrado." });
        });

        // Dados da ONG
        app.MapGet("/ong", (Banco banco) =>
        {
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand("SELECT TOP 1 * FROM ONG WHERE ID_ONG = 1", conn);
            using var reader = cmd.ExecuteReader();
            if (!reader.Read())
                return Results.NotFound(new { erro = "ONG não encontrada." });

            return Results.Ok(new
            {
                nome = Texto(reader["DSC_NOME"]),
                cnpj = Texto(reader["NUM_CNPJ"]),
                telefone = Texto(reader["NUM_TELEFONE"]),
                email = Texto(reader["DSC_EMAIL"]),
                endereco = Texto(reader["DSC_ENDERECO"])
            });
        });

        app.MapPut("/ong", (Banco banco, EdicaoOng dados) =>
        {
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand(@"
                UPDATE ONG
                SET NUM_TELEFONE = ISNULL(@tel, NUM_TELEFONE),
                    DSC_EMAIL    = ISNULL(@email, DSC_EMAIL),
                    DSC_ENDERECO = ISNULL(@end, DSC_ENDERECO)
                WHERE ID_ONG = 1", conn);
            cmd.Parameters.AddWithValue("@tel",
                string.IsNullOrWhiteSpace(dados.Telefone) ? (object)DBNull.Value : SoNumeros(dados.Telefone));
            cmd.Parameters.AddWithValue("@email", ValorOuNulo(dados.Email));
            cmd.Parameters.AddWithValue("@end", ValorOuNulo(dados.Endereco));

            try
            {
                cmd.ExecuteNonQuery();
            }
            catch (SqlException ex)
            {
                return Results.BadRequest(new { erro = ex.Message });
            }
            return Results.Ok();
        });
    }
}

public record NovaConta(string Nome, string Email, string Senha);
public record EdicaoConta(string Nome, string Email, string Senha, string Telefone);
public record EdicaoUsuario(string Telefone, string Email, string Endereco);
public record NovoInteresse(int IdUsuario, string Tipo, string Descricao);
public record EdicaoInteresse(string Tipo, string Descricao, string Status);
public record EdicaoOng(string Telefone, string Email, string Endereco);
