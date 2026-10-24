using System.Data;
using System.Data.SqlClient;
using System.Globalization;
using System.Text;

// Regras do app (idoso e ajudante) que ficam na API e nao no banco.
public static class AgendamentosApi
{
    const int LIMITE_KM = 12;
    const int HORA_MIN = 7 * 60;   // 07:00
    const int HORA_MAX = 20 * 60;  // 20:00

    // ---------------------------------------------------------------
    // Funcoes de apoio
    // ---------------------------------------------------------------

    public static bool CpfValido(string cpf)
    {
        if (cpf == null || cpf.Length != 11) return false;
        if (cpf.Any(c => !char.IsDigit(c))) return false;
        if (cpf.Distinct().Count() == 1) return false;

        for (int t = 9; t <= 10; t++)
        {
            int soma = 0;
            for (int i = 0; i < t; i++)
                soma += (cpf[i] - '0') * (t + 1 - i);
            int dv = (soma * 10) % 11;
            if (dv == 10) dv = 0;
            if (dv != cpf[t] - '0') return false;
        }
        return true;
    }

    public static string SoNumeros(string texto)
    {
        return new string((texto ?? "").Where(char.IsDigit).ToArray());
    }

    static string SemAcento(string texto)
    {
        if (texto == null) return "";
        var normalizado = texto.Normalize(NormalizationForm.FormD);
        var sb = new StringBuilder();
        foreach (var c in normalizado)
            if (CharUnicodeInfo.GetUnicodeCategory(c) != UnicodeCategory.NonSpacingMark)
                sb.Append(c);
        return sb.ToString().ToLowerInvariant().Trim();
    }

    // Hospital 180, Mercado 120, Farmacia 90. Devolve 0 se o tipo nao for reconhecido.
    static int DuracaoPorTipo(string tipo)
    {
        var t = SemAcento(tipo);
        if (t.Contains("hospital") || t.Contains("medic") || t.Contains("consulta")) return 180;
        if (t.Contains("mercado") || t.Contains("compra")) return 120;
        if (t.Contains("farmacia")) return 90;
        return 0;
    }

    static double DistanciaEntrePontos(double lat1, double lon1, double lat2, double lon2)
    {
        const double R = 6371.0;
        double dLat = (lat2 - lat1) * Math.PI / 180.0;
        double dLon = (lon2 - lon1) * Math.PI / 180.0;
        double a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                   Math.Cos(lat1 * Math.PI / 180.0) * Math.Cos(lat2 * Math.PI / 180.0) *
                   Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
        return R * 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
    }

    static string Texto(object valor)
    {
        return valor == DBNull.Value ? "" : valor.ToString();
    }

    static object ValorOuNulo(string texto)
    {
        return string.IsNullOrWhiteSpace(texto) ? (object)DBNull.Value : texto.Trim();
    }

    static bool TentaData(string texto, out DateTime data)
    {
        return DateTime.TryParseExact(texto, "yyyy-MM-dd", CultureInfo.InvariantCulture,
                                      DateTimeStyles.None, out data);
    }

    static bool TentaHora(string texto, out TimeSpan hora)
    {
        return TimeSpan.TryParseExact(texto, @"hh\:mm", CultureInfo.InvariantCulture, out hora);
    }

    public static bool CpfEmAjudante(Banco banco, string cpf)
    {
        using var conn = banco.AbrirConexao();
        using var cmd = new SqlCommand("SELECT COUNT(*) FROM AJUDANTE WHERE CPF = @cpf", conn);
        cmd.Parameters.AddWithValue("@cpf", cpf);
        return (int)cmd.ExecuteScalar() > 0;
    }

    static bool CpfEmUsuario(SqlConnection conn, string cpf)
    {
        using var cmd = new SqlCommand("SELECT COUNT(*) FROM USUARIO WHERE NUM_CPF = @cpf", conn);
        cmd.Parameters.AddWithValue("@cpf", cpf);
        return (int)cmd.ExecuteScalar() > 0;
    }

    // Procura um ajudante livre: tem disponibilidade cobrindo o atendimento inteiro
    // e nenhum outro agendamento (nao cancelado) que se sobreponha.
    // Entre os livres, escolhe quem tem menos atendimentos no dia.
    static int? EscolherAjudante(SqlConnection conn, DateTime data, TimeSpan inicio, int duracaoMin)
    {
        var fim = inicio.Add(TimeSpan.FromMinutes(duracaoMin));

        using var cmd = new SqlCommand(@"
            SELECT TOP 1 A.ID_AJUDANTE
            FROM AJUDANTE A
            WHERE A.FLG_ATIVO = 1
              AND EXISTS (SELECT 1 FROM DISPONIBILIDADE_AJUDANTE D
                          WHERE D.ID_AJUDANTE = A.ID_AJUDANTE
                            AND D.DATA_DISPONIBILIDADE = @data
                            AND D.FLG_DISPONIVEL = 1
                            AND D.HORA_INICIO <= @inicio
                            AND D.HORA_FIM >= @fim)
              AND NOT EXISTS (SELECT 1 FROM AGENDAMENTO G
                              WHERE G.ID_AJUDANTE = A.ID_AJUDANTE
                                AND G.DATA_AGENDAMENTO = @data
                                AND G.DSC_STATUS <> 'Cancelado'
                                AND G.HORA_AGENDAMENTO < @fim
                                AND DATEADD(MINUTE, ISNULL(G.DURACAO_MIN, 60), G.HORA_AGENDAMENTO) > @inicio)
            ORDER BY (SELECT COUNT(*) FROM AGENDAMENTO X
                      WHERE X.ID_AJUDANTE = A.ID_AJUDANTE
                        AND X.DATA_AGENDAMENTO = @data
                        AND X.DSC_STATUS <> 'Cancelado'),
                     A.ID_AJUDANTE", conn);

        cmd.Parameters.Add("@data", SqlDbType.Date).Value = data.Date;
        cmd.Parameters.Add("@inicio", SqlDbType.Time).Value = inicio;
        cmd.Parameters.Add("@fim", SqlDbType.Time).Value = fim;

        var resultado = cmd.ExecuteScalar();
        if (resultado == null || resultado == DBNull.Value) return null;
        return Convert.ToInt32(resultado);
    }

    static IResult MudarStatus(Banco banco, int id, string novoStatus)
    {
        using var conn = banco.AbrirConexao();

        // So muda se o atendimento ainda esta "Agendado".
        var sql = @"UPDATE AGENDAMENTO
                    SET DSC_STATUS = @status" +
                  (novoStatus == "Cancelado" ? ", DATA_CANCELAMENTO = SYSDATETIME()" : "") + @"
                    WHERE ID_AGENDAMENTO = @id AND DSC_STATUS = 'Agendado'";

        using var cmd = new SqlCommand(sql, conn);
        cmd.Parameters.AddWithValue("@status", novoStatus);
        cmd.Parameters.AddWithValue("@id", id);

        if (cmd.ExecuteNonQuery() > 0)
            return Results.Ok(new { id, status = novoStatus });

        using var existe = new SqlCommand("SELECT COUNT(*) FROM AGENDAMENTO WHERE ID_AGENDAMENTO = @id", conn);
        existe.Parameters.AddWithValue("@id", id);
        if ((int)existe.ExecuteScalar() == 0)
            return Results.NotFound(new { erro = "Agendamento nao encontrado." });

        return Results.Conflict(new { erro = "Esse agendamento ja foi concluido ou cancelado." });
    }

    // ---------------------------------------------------------------
    // Rotas
    // ---------------------------------------------------------------

    public static void MapAgendamentos(this WebApplication app)
    {
        // Telefone e WhatsApp da ONG (ficam no appsettings.json)
        app.MapGet("/ong/contato", (IConfiguration config) =>
            Results.Ok(new
            {
                telefone = config["Ong:Telefone"],
                whatsapp = config["Ong:WhatsApp"]
            }));

        // Login do app: so CPF. Devolve tipo (idoso ou ajudante), id e token.
        app.MapPost("/auth/login-cpf", (Banco banco, Dictionary<string, string> corpo) =>
        {
            corpo.TryGetValue("cpf", out var cpfDigitado);
            var cpf = SoNumeros(cpfDigitado);

            if (!CpfValido(cpf))
                return Results.BadRequest(new { erro = "CPF invalido." });

            using var conn = banco.AbrirConexao();

            using (var cmd = new SqlCommand("SELECT ID_USUARIO FROM USUARIO WHERE NUM_CPF = @cpf", conn))
            {
                cmd.Parameters.AddWithValue("@cpf", cpf);
                var id = cmd.ExecuteScalar();
                if (id != null)
                    return Results.Ok(new { token = "token-teste", id = Convert.ToInt32(id), tipo = "idoso" });
            }

            using (var cmd = new SqlCommand("SELECT ID_AJUDANTE FROM AJUDANTE WHERE CPF = @cpf AND FLG_ATIVO = 1", conn))
            {
                cmd.Parameters.AddWithValue("@cpf", cpf);
                var id = cmd.ExecuteScalar();
                if (id != null)
                    return Results.Ok(new { token = "token-teste", id = Convert.ToInt32(id), tipo = "ajudante" });
            }

            return Results.Unauthorized();
        });

        // Cadastro de ajudante (CPF nao pode existir em USUARIO nem em AJUDANTE)
        app.MapPost("/ajudantes", (Banco banco, NovoAjudante dados) =>
        {
            var cpf = SoNumeros(dados.Cpf);
            if (!CpfValido(cpf))
                return Results.BadRequest(new { erro = "CPF invalido." });
            if (string.IsNullOrWhiteSpace(dados.Nome))
                return Results.BadRequest(new { erro = "Informe o nome." });

            using var conn = banco.AbrirConexao();

            if (CpfEmUsuario(conn, cpf) || CpfEmAjudante(banco, cpf))
                return Results.Conflict(new { erro = "CPF ja cadastrado." });

            using var cmd = new SqlCommand(@"
                INSERT INTO AJUDANTE (ID_PERFIL, NOME_AJUDANTE, CPF, TELEFONE, EMAIL, DSC_ENDERECO)
                VALUES ((SELECT TOP 1 ID_PERFIL FROM PERFIL_ACESSO WHERE DSC_PERFIL = 'Ajudante'),
                        @nome, @cpf, @tel, @email, @end);
                SELECT CAST(SCOPE_IDENTITY() AS INT);", conn);
            cmd.Parameters.AddWithValue("@nome", dados.Nome.Trim());
            cmd.Parameters.AddWithValue("@cpf", cpf);
            cmd.Parameters.AddWithValue("@tel", ValorOuNulo(SoNumeros(dados.Telefone)));
            cmd.Parameters.AddWithValue("@email", ValorOuNulo(dados.Email));
            cmd.Parameters.AddWithValue("@end", ValorOuNulo(dados.Endereco));

            var id = (int)cmd.ExecuteScalar();
            return Results.Ok(new { id });
        });

        // Novo agendamento: calcula duracao e distancia, escolhe o ajudante e grava.
        app.MapPost("/agendamentos", (Banco banco, NovoAgendamento dados) =>
        {
            if (!TentaData(dados.Data, out var data))
                return Results.BadRequest(new { erro = "Data invalida. Use yyyy-MM-dd." });
            if (!TentaHora(dados.Hora, out var hora))
                return Results.BadRequest(new { erro = "Hora invalida. Use HH:mm." });
            if (hora.TotalMinutes < HORA_MIN || hora.TotalMinutes > HORA_MAX)
                return Results.BadRequest(new { erro = "O horario precisa ser entre 07:00 e 20:00." });

            int duracao = DuracaoPorTipo(dados.Tipo);
            if (duracao == 0)
                return Results.BadRequest(new { erro = "Tipo invalido. Use Hospital, Mercado ou Farmacia." });

            if (hora.TotalMinutes + duracao > HORA_MAX)
                return Results.BadRequest(new { erro = "Esse atendimento termina depois das 20:00." });

            // Distancia: pelas coordenadas (se o app mandar) ou pelo valor informado.
            decimal? distancia = null;
            if (dados.LatOrigem.HasValue && dados.LonOrigem.HasValue &&
                dados.LatDestino.HasValue && dados.LonDestino.HasValue)
            {
                distancia = Math.Round((decimal)DistanciaEntrePontos(
                    dados.LatOrigem.Value, dados.LonOrigem.Value,
                    dados.LatDestino.Value, dados.LonDestino.Value), 2);
            }
            else if (dados.DistanciaKm.HasValue)
            {
                distancia = Math.Round(dados.DistanciaKm.Value, 2);
            }

            if (distancia.HasValue && (distancia.Value < 0 || distancia.Value > LIMITE_KM))
                return Results.BadRequest(new { erro = "O destino esta alem do limite de 12 km.", distanciaKm = distancia });

            using var conn = banco.AbrirConexao();

            using (var existe = new SqlCommand("SELECT COUNT(*) FROM USUARIO WHERE ID_USUARIO = @id", conn))
            {
                existe.Parameters.AddWithValue("@id", dados.IdUsuario);
                if ((int)existe.ExecuteScalar() == 0)
                    return Results.NotFound(new { erro = "Usuario nao encontrado." });
            }

            var idAjudante = EscolherAjudante(conn, data, hora, duracao);
            if (idAjudante == null)
                return Results.Conflict(new { erro = "Nenhum ajudante disponivel nesse dia e horario." });

            try
            {
                using var cmd = new SqlCommand("sp_CriarAgendamento", conn);
                cmd.CommandType = CommandType.StoredProcedure;
                cmd.Parameters.AddWithValue("@ID_USUARIO", dados.IdUsuario);
                cmd.Parameters.Add("@DATA_AGENDAMENTO", SqlDbType.Date).Value = data.Date;
                cmd.Parameters.Add("@HORA_AGENDAMENTO", SqlDbType.Time).Value = hora;
                cmd.Parameters.AddWithValue("@DSC_TIPO", dados.Tipo.Trim());
                cmd.Parameters.AddWithValue("@DSC_LOCAL", ValorOuNulo(dados.Destino));
                cmd.Parameters.AddWithValue("@DSC_OBSERVACAO", ValorOuNulo(dados.Observacao));
                cmd.Parameters.AddWithValue("@DSC_ORIGEM", ValorOuNulo(dados.Origem));
                cmd.Parameters.AddWithValue("@DURACAO_MIN", duracao);
                cmd.Parameters.AddWithValue("@ID_AJUDANTE", idAjudante.Value);
                cmd.Parameters.AddWithValue("@DISTANCIA_KM", distancia.HasValue ? (object)distancia.Value : DBNull.Value);

                var id = Convert.ToInt32(cmd.ExecuteScalar());
                return Results.Ok(new { id, idAjudante = idAjudante.Value, duracaoMin = duracao, distanciaKm = distancia });
            }
            catch (SqlException ex)
            {
                return Results.BadRequest(new { erro = ex.Message });
            }
        });

        // Horarios livres de um dia (para o calendario do idoso: horario ocupado some)
        app.MapGet("/agendamentos/horarios-livres", (Banco banco, string data, string tipo) =>
        {
            if (!TentaData(data, out var dia))
                return Results.BadRequest(new { erro = "Data invalida. Use yyyy-MM-dd." });

            int duracao = DuracaoPorTipo(tipo);
            if (duracao == 0)
                return Results.BadRequest(new { erro = "Tipo invalido. Use Hospital, Mercado ou Farmacia." });

            var livres = new List<string>();
            using var conn = banco.AbrirConexao();

            for (int minutos = HORA_MIN; minutos + duracao <= HORA_MAX; minutos += 30)
            {
                var inicio = TimeSpan.FromMinutes(minutos);
                if (EscolherAjudante(conn, dia, inicio, duracao) != null)
                    livres.Add(inicio.ToString(@"hh\:mm"));
            }
            return Results.Ok(livres);
        });

        // Historico do idoso (formato que o app Kotlin espera)
        app.MapGet("/agendamentos/usuario/{id}", (Banco banco, int id) =>
        {
            var lista = new List<object>();
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand(@"
                SELECT ID_AGENDAMENTO, DATA_AGENDAMENTO, HORA_AGENDAMENTO, DSC_TIPO, DSC_ORIGEM,
                       DSC_LOCAL, DSC_STATUS, DURACAO_MIN, DISTANCIA_KM
                FROM AGENDAMENTO
                WHERE ID_USUARIO = @id
                ORDER BY DATA_AGENDAMENTO DESC, HORA_AGENDAMENTO DESC", conn);
            cmd.Parameters.AddWithValue("@id", id);

            using var reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                var tipo = Texto(reader["DSC_TIPO"]);
                var destino = Texto(reader["DSC_LOCAL"]);
                lista.Add(new
                {
                    id = Convert.ToInt32(reader["ID_AGENDAMENTO"]),
                    status = Texto(reader["DSC_STATUS"]),
                    data = Convert.ToDateTime(reader["DATA_AGENDAMENTO"]).ToString("dd/MM/yyyy"),
                    hora = ((TimeSpan)reader["HORA_AGENDAMENTO"]).ToString(@"hh\:mm"),
                    descricao = string.IsNullOrEmpty(destino) ? tipo : tipo + " - " + destino,
                    tipo,
                    origem = Texto(reader["DSC_ORIGEM"]),
                    destino
                });
            }
            return Results.Ok(lista);
        });

        // Atendimentos de um ajudante (dia opcional: ?data=yyyy-MM-dd)
        app.MapGet("/agendamentos/ajudante/{id}", (Banco banco, int id, string data) =>
        {
            DateTime dia = DateTime.Today;
            if (!string.IsNullOrEmpty(data) && !TentaData(data, out dia))
                return Results.BadRequest(new { erro = "Data invalida. Use yyyy-MM-dd." });

            var lista = new List<object>();
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand(@"
                SELECT G.ID_AGENDAMENTO, G.DATA_AGENDAMENTO, G.HORA_AGENDAMENTO, G.DSC_TIPO,
                       G.DSC_ORIGEM, G.DSC_LOCAL, G.DSC_OBSERVACAO, G.DSC_STATUS, G.DURACAO_MIN,
                       G.DISTANCIA_KM, U.DSC_NOME, U.NUM_TELEFONE
                FROM AGENDAMENTO G
                INNER JOIN USUARIO U ON U.ID_USUARIO = G.ID_USUARIO
                WHERE G.ID_AJUDANTE = @id AND G.DATA_AGENDAMENTO = @data
                ORDER BY G.HORA_AGENDAMENTO", conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.Add("@data", SqlDbType.Date).Value = dia.Date;

            using var reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                lista.Add(new
                {
                    id = Convert.ToInt32(reader["ID_AGENDAMENTO"]),
                    data = Convert.ToDateTime(reader["DATA_AGENDAMENTO"]).ToString("dd/MM/yyyy"),
                    hora = ((TimeSpan)reader["HORA_AGENDAMENTO"]).ToString(@"hh\:mm"),
                    tipo = Texto(reader["DSC_TIPO"]),
                    origem = Texto(reader["DSC_ORIGEM"]),
                    destino = Texto(reader["DSC_LOCAL"]),
                    observacao = Texto(reader["DSC_OBSERVACAO"]),
                    status = Texto(reader["DSC_STATUS"]),
                    duracaoMin = reader["DURACAO_MIN"] == DBNull.Value ? (int?)null : Convert.ToInt32(reader["DURACAO_MIN"]),
                    distanciaKm = reader["DISTANCIA_KM"] == DBNull.Value ? (decimal?)null : Convert.ToDecimal(reader["DISTANCIA_KM"]),
                    nomeIdoso = Texto(reader["DSC_NOME"]),
                    telefoneIdoso = Texto(reader["NUM_TELEFONE"])
                });
            }
            return Results.Ok(lista);
        });

        // Ajudante conclui ou cancela. O historico de status e gravado pela trigger.
        app.MapPut("/agendamentos/{id}/concluir", (Banco banco, int id) => MudarStatus(banco, id, "Concluído"));
        app.MapPut("/agendamentos/{id}/cancelar", (Banco banco, int id) => MudarStatus(banco, id, "Cancelado"));

        // Disponibilidade do ajudante
        app.MapPost("/ajudantes/{id}/disponibilidade", (Banco banco, int id, NovaDisponibilidade dados) =>
        {
            if (!TentaData(dados.Data, out var data))
                return Results.BadRequest(new { erro = "Data invalida. Use yyyy-MM-dd." });
            if (!TentaHora(dados.HoraInicio, out var inicio) || !TentaHora(dados.HoraFim, out var fim))
                return Results.BadRequest(new { erro = "Hora invalida. Use HH:mm." });
            if (inicio.TotalMinutes < HORA_MIN || fim.TotalMinutes > HORA_MAX)
                return Results.BadRequest(new { erro = "A disponibilidade precisa ficar entre 07:00 e 20:00." });
            if (fim <= inicio)
                return Results.BadRequest(new { erro = "A hora final precisa ser depois da inicial." });

            using var conn = banco.AbrirConexao();

            using (var existe = new SqlCommand("SELECT COUNT(*) FROM AJUDANTE WHERE ID_AJUDANTE = @id AND FLG_ATIVO = 1", conn))
            {
                existe.Parameters.AddWithValue("@id", id);
                if ((int)existe.ExecuteScalar() == 0)
                    return Results.NotFound(new { erro = "Ajudante nao encontrado." });
            }

            using (var conflito = new SqlCommand(@"
                SELECT COUNT(*) FROM DISPONIBILIDADE_AJUDANTE
                WHERE ID_AJUDANTE = @id AND DATA_DISPONIBILIDADE = @data AND FLG_DISPONIVEL = 1
                  AND HORA_INICIO < @fim AND HORA_FIM > @inicio", conn))
            {
                conflito.Parameters.AddWithValue("@id", id);
                conflito.Parameters.Add("@data", SqlDbType.Date).Value = data.Date;
                conflito.Parameters.Add("@inicio", SqlDbType.Time).Value = inicio;
                conflito.Parameters.Add("@fim", SqlDbType.Time).Value = fim;
                if ((int)conflito.ExecuteScalar() > 0)
                    return Results.Conflict(new { erro = "Esse horario se sobrepoe a outra disponibilidade sua." });
            }

            using var cmd = new SqlCommand(@"
                INSERT INTO DISPONIBILIDADE_AJUDANTE (ID_AJUDANTE, DATA_DISPONIBILIDADE, HORA_INICIO, HORA_FIM, DSC_OBSERVACAO)
                VALUES (@id, @data, @inicio, @fim, @obs);
                SELECT CAST(SCOPE_IDENTITY() AS INT);", conn);
            cmd.Parameters.AddWithValue("@id", id);
            cmd.Parameters.Add("@data", SqlDbType.Date).Value = data.Date;
            cmd.Parameters.Add("@inicio", SqlDbType.Time).Value = inicio;
            cmd.Parameters.Add("@fim", SqlDbType.Time).Value = fim;
            cmd.Parameters.AddWithValue("@obs", ValorOuNulo(dados.Observacao));

            return Results.Ok(new { id = (int)cmd.ExecuteScalar() });
        });

        app.MapGet("/ajudantes/{id}/disponibilidade", (Banco banco, int id, string data) =>
        {
            var lista = new List<object>();
            using var conn = banco.AbrirConexao();

            var sql = @"SELECT ID_DISPONIBILIDADE, DATA_DISPONIBILIDADE, HORA_INICIO, HORA_FIM, DSC_OBSERVACAO
                        FROM DISPONIBILIDADE_AJUDANTE
                        WHERE ID_AJUDANTE = @id AND FLG_DISPONIVEL = 1";
            DateTime dia = DateTime.MinValue;
            bool filtraDia = !string.IsNullOrEmpty(data);
            if (filtraDia)
            {
                if (!TentaData(data, out dia))
                    return Results.BadRequest(new { erro = "Data invalida. Use yyyy-MM-dd." });
                sql += " AND DATA_DISPONIBILIDADE = @data";
            }
            else
            {
                sql += " AND DATA_DISPONIBILIDADE >= CAST(GETDATE() AS DATE)";
            }
            sql += " ORDER BY DATA_DISPONIBILIDADE, HORA_INICIO";

            using var cmd = new SqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@id", id);
            if (filtraDia) cmd.Parameters.Add("@data", SqlDbType.Date).Value = dia.Date;

            using var reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                lista.Add(new
                {
                    id = Convert.ToInt32(reader["ID_DISPONIBILIDADE"]),
                    data = Convert.ToDateTime(reader["DATA_DISPONIBILIDADE"]).ToString("yyyy-MM-dd"),
                    horaInicio = ((TimeSpan)reader["HORA_INICIO"]).ToString(@"hh\:mm"),
                    horaFim = ((TimeSpan)reader["HORA_FIM"]).ToString(@"hh\:mm"),
                    observacao = Texto(reader["DSC_OBSERVACAO"])
                });
            }
            return Results.Ok(lista);
        });

        // Tira uma disponibilidade (marca como indisponivel, nao apaga a linha)
        app.MapDelete("/ajudantes/disponibilidade/{idDisponibilidade}", (Banco banco, int idDisponibilidade) =>
        {
            using var conn = banco.AbrirConexao();
            using var cmd = new SqlCommand(
                "UPDATE DISPONIBILIDADE_AJUDANTE SET FLG_DISPONIVEL = 0 WHERE ID_DISPONIBILIDADE = @id", conn);
            cmd.Parameters.AddWithValue("@id", idDisponibilidade);
            return cmd.ExecuteNonQuery() > 0 ? Results.Ok() : Results.NotFound();
        });
    }
}

public record NovoAjudante(string Nome, string Cpf, string Telefone, string Email, string Endereco);

public record NovoAgendamento(int IdUsuario, string Data, string Hora, string Tipo,
                              string Origem, string Destino, string Observacao,
                              decimal? DistanciaKm,
                              double? LatOrigem, double? LonOrigem,
                              double? LatDestino, double? LonDestino);

public record NovaDisponibilidade(string Data, string HoraInicio, string HoraFim, string Observacao);
