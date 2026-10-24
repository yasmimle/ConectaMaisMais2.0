var builder = WebApplication.CreateBuilder(args);

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddSingleton<Banco>();
builder.Services.AddCors(o => o.AddPolicy("front", p =>
    p.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod()));

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();
app.UseCors("front");

// Lista de usuários (para testar a ligação com o banco)
app.MapGet("/usuarios", (Banco banco) => banco.ListarUsuarios());

// Cadastro de usuário (idoso) vindo do site
app.MapPost("/usuarios", (Banco banco, NovoUsuario dados) =>
{
    var cpf = new string((dados.Cpf ?? "").Where(char.IsDigit).ToArray());

    // O mesmo CPF não pode existir em USUARIO e em AJUDANTE
    if (banco.CpfExiste(cpf) || AgendamentosApi.CpfEmAjudante(banco, cpf))
        return Results.Conflict(new { erro = "CPF já cadastrado." });

    // Ordem das siglas no banco (ID_UF = posição + 1)
    string[] ufs = { "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
                     "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO" };
    int idUf = Array.IndexOf(ufs, (dados.Estado ?? "").ToUpper()) + 1;
    if (idUf == 0)
        return Results.BadRequest(new { erro = "Estado inválido." });

    var usuario = new Usuario
    {
        Nome = dados.Nome,
        Email = dados.Email,
        Telefone = new string((dados.Telefone ?? "").Where(char.IsDigit).ToArray()),
        Cpf = cpf,
        Endereco = dados.Endereco,
        Cidade = dados.Cidade,
        DataNascimento = DateTime.Parse(dados.DataNascimento),
        IdUf = idUf
    };

    banco.InserirUsuario(usuario);
    return Results.Ok(new { id = usuario.IdUsuario });
});

// Cria o admin padrão (admin@conecta.com / 1234) se ainda não existir nenhum
try { app.Services.GetRequiredService<Banco>().GarantirAdminPadrao(); } catch { }

// Login do painel (admin ou atendente)
app.MapPost("/auth/login-admin", (Banco banco, LoginDados dados) =>
{
    var email = (dados.Email ?? "").Trim().ToLower();
    var senha = dados.Senha ?? "";

    if (banco.LoginAdmin(email, senha))
    {
        var a = banco.BuscarAdminPorEmail(email);
        return Results.Ok(new { tipo = "administrador", nome = a.Nome, email = a.Email });
    }

    if (banco.LoginAtendente(email, senha))
    {
        var t = banco.BuscarAtendentePorEmail(email);
        return Results.Ok(new { tipo = "atendente", nome = t.Nome, email = t.Email });
    }

    return Results.Unauthorized();
});

// Remover usuário (e os interesses dele)
app.MapDelete("/usuarios/{id}", (Banco banco, int id) =>
{
    banco.RemoverUsuario(id);
    return Results.Ok();
});

// Rotas do app: login por CPF, agendamentos, ajudantes e contato da ONG (arquivo Agendamentos.cs)
app.MapAgendamentos();

// Rotas do site: interesses, administradores, atendentes, ONG e editar usuário (arquivo PainelApi.cs)
app.MapPainel();

app.Run();

record NovoUsuario(string Nome, string Email, string Telefone, string Cpf,
                   string Endereco, string Cidade, string Estado, string DataNascimento);

record LoginDados(string Email, string Senha);
