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

// Login do app (CPF). O app manda só números e espera "token" e "id" na resposta.
app.MapPost("/auth/login-cpf", (Banco banco, Dictionary<string, string> corpo) =>
{
    var cpf = new string((corpo["cpf"] ?? "").Where(char.IsDigit).ToArray());
    var usuario = banco.BuscarUsuarioPorCpf(cpf);

    if (usuario == null)
        return Results.Unauthorized();

    return Results.Ok(new { token = "token-teste", id = usuario.IdUsuario });
});

// Lista de usuários (para testar a ligação com o banco)
app.MapGet("/usuarios", (Banco banco) => banco.ListarUsuarios());

// Cadastro de usuário (idoso) vindo do site
app.MapPost("/usuarios", (Banco banco, NovoUsuario dados) =>
{
    var cpf = new string((dados.Cpf ?? "").Where(char.IsDigit).ToArray());

    if (banco.CpfExiste(cpf))
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

app.Run();
record NovoUsuario(string Nome, string Email, string Telefone, string Cpf,
                   string Endereco, string Cidade, string Estado, string DataNascimento);