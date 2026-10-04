using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Runtime.ConstrainedExecution;
using BCrypt.Net;

// Classe principal do sistema
class Program
{
    // Instância da classe Banco para acesso ao banco de dados
    static Banco banco = new Banco();

    // Armazena o usuário logado no sistema
    static string usuarioLogado = "";

    // Lista de usuários cadastrados
    static List<Usuario> usuarios = new List<Usuario>();

    // Lista de interesses cadastrados
    static List<Interesse> interesses = new List<Interesse>();

    // Método principal do sistema
    static void Main()
    {
        // Abre conexão com o banco de dados
        banco.AbrirConexao();

        Console.WriteLine("Conexão com banco realizada com sucesso!");

        // Cria o administrador padrão (admin@conecta.com / 1234)
        // somente se ainda não existir nenhum administrador
        try
        {
            banco.GarantirAdminPadrao();
        }
        catch (Exception erro)
        {
            Console.WriteLine("Não foi possível criar o administrador padrão: " + erro.Message);
            Console.WriteLine("Rode o script banco_completo.sql no SQL Server (ele cria o banco e os dados iniciais).");
        }

        int opcao = 0;

        // Menu principal do sistema
        while (opcao != 5)
        {
            Console.WriteLine("\n=== Sistema de Apoio ao Idoso ===");

            Console.WriteLine("1 - Cadastro de Usuário");
            Console.WriteLine("2 - Login  do Sistema");
            Console.WriteLine("3 - Lembrete de Saúde");
            Console.WriteLine("4 - Prevenção de Golpes");
            Console.WriteLine("5 - Sair");

            Console.Write("Escolha uma opção: ");

            // Validação para aceitar apenas números
            if (!int.TryParse(Console.ReadLine(), out opcao))
            {
                Console.WriteLine("Digite apenas números.");
                continue;
            }

            switch (opcao)
            {
                case 1:
                    CadastrarUsuario();
                    break;

                case 2:
                    LoginSistema();
                    break;

                case 3:
                    LembreteSaude();
                    break;

                case 4:
                    PrevençãodeGolpes();
                    break;

                case 5:
                    Console.WriteLine("Sistema encerrado.");
                    break;

                default:
                    Console.WriteLine("Opção inválida!");
                    break;
            }
        }
    }
    // Método responsável pelo cadastro de usuários
    static void CadastrarUsuario()
    {
        // O banco vincula cada usuário a um administrador e a um atendente
        if (!banco.ExisteAdminEAtendente())
        {
            Console.WriteLine("\nAntes de cadastrar usuários é preciso ter pelo menos 1 administrador e 1 atendente.");
            Console.WriteLine("Entre como administrador e use a opção 16 (Cadastrar atendente).");
            return;
        }

        Usuario usuario = new Usuario();

        bool nomeValido = false;

        // Validação do nome
        while (!nomeValido)
        {
            Console.Write("Nome completo: ");

            usuario.Nome = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(usuario.Nome) &&
                usuario.Nome.All(c => char.IsLetter(c) || char.IsWhiteSpace(c)))
            {
                nomeValido = true;
            }
            else
            {
                Console.WriteLine("Nome inválido.");
            }
        }

        bool cpfValido = false;

        // Validação do CPF
        while (!cpfValido)
        {
            Console.Write("CPF (somente números): ");

            usuario.Cpf = Console.ReadLine();

            if (usuario.Cpf.Length == 11 &&
                 usuario.Cpf.All(char.IsDigit) &&
                 ValidarCpf(usuario.Cpf))
            {
                // Verifica se o CPF já existe
                if (banco.CpfExiste(usuario.Cpf))
                {
                    Console.WriteLine("CPF já cadastrado.");
                }
                else
                {
                    cpfValido = true;
                }
            }
            else
            {
                Console.WriteLine("CPF inválido. Digite 11 números.");
            }
        }

        bool telefoneValido = false;

        // Validação do telefone
        while (!telefoneValido)
        {
            Console.Write("Telefone (somente números): ");

            usuario.Telefone = Console.ReadLine();

            if (usuario.Telefone.Length == 11 &&
                usuario.Telefone.All(char.IsDigit))
            {
                telefoneValido = true;
            }
            else
            {
                Console.WriteLine("Telefone inválido. Digite DDD + número com 11 dígitos.");
            }
        }

        bool emailValido = false;

        // Validação do email
        while (!emailValido)
        {
            Console.Write("Email (opcional): ");

            usuario.Email = Console.ReadLine();

            if (string.IsNullOrWhiteSpace(usuario.Email) ||
                usuario.Email.Contains("@"))
            {
                emailValido = true;
            }
            else
            {
                Console.WriteLine("Email inválido.");
            }
        }

        string cep = "";

        bool cepValido = false;

        // Validação do CEP
        while (!cepValido)
        {
            Console.Write("CEP (somente números): ");

            cep = Console.ReadLine();

            if (cep.Length == 8 &&
                cep.All(char.IsDigit))
            {
                cepValido = true;
            }
            else
            {
                Console.WriteLine("CEP inválido. Digite 8 números.");
            }
        }

        string numero = "";

        bool numeroValido = false;

        // Validação do número da residência
        while (!numeroValido)
        {
            Console.Write("Número da casa: ");

            numero = Console.ReadLine();

            if (numero.Length > 0 &&
                numero.All(c => char.IsLetterOrDigit(c)))
            {
                numeroValido = true;
            }
            else
            {
                Console.WriteLine("Digite apenas números e letras.");
            }
        }

        string rua = "";

        bool ruaValida = false;

        // Validação da rua
        while (!ruaValida)
        {
            Console.Write("Rua: ");

            rua = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(rua))
            {
                ruaValida = true;
            }
            else
            {
                Console.WriteLine("Rua inválida.");
            }
        }

        string bairro = "";

        bool bairroValido = false;

        // Validação do bairro
        while (!bairroValido)
        {
            Console.Write("Bairro: ");

            bairro = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(bairro) &&
                bairro.All(c => char.IsLetter(c) || char.IsWhiteSpace(c)))
            {
                bairroValido = true;
            }
            else
            {
                Console.WriteLine("Bairro inválido.");
            }
        }

        string cidade = "";

        bool cidadeValida = false;

        // Validação da cidade
        while (!cidadeValida)
        {
            Console.Write("Cidade: ");

            cidade = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(cidade) &&
                cidade.All(c => char.IsLetter(c) || char.IsWhiteSpace(c)))
            {
                cidadeValida = true;
            }
            else
            {
                Console.WriteLine("Cidade inválida.");
            }
        }

        // Lista de estados disponíveis
        Console.WriteLine("\nEstados disponíveis:");

        Console.WriteLine("1 - AC");
        Console.WriteLine("2 - AL");
        Console.WriteLine("3 - AP");
        Console.WriteLine("4 - AM");
        Console.WriteLine("5 - BA");
        Console.WriteLine("6 - CE");
        Console.WriteLine("7 - DF");
        Console.WriteLine("8 - ES");
        Console.WriteLine("9 - GO");
        Console.WriteLine("10 - MA");
        Console.WriteLine("11 - MT");
        Console.WriteLine("12 - MS");
        Console.WriteLine("13 - MG");
        Console.WriteLine("14 - PA");
        Console.WriteLine("15 - PB");
        Console.WriteLine("16 - PR");
        Console.WriteLine("17 - PE");
        Console.WriteLine("18 - PI");
        Console.WriteLine("19 - RJ");
        Console.WriteLine("20 - RN");
        Console.WriteLine("21 - RS");
        Console.WriteLine("22 - RO");
        Console.WriteLine("23 - RR");
        Console.WriteLine("24 - SC");
        Console.WriteLine("25 - SP");
        Console.WriteLine("26 - SE");
        Console.WriteLine("27 - TO");

        int idUf = 0;

        bool ufValida = false;

        // Validação da UF
        while (!ufValida)
        {
            Console.Write("Escolha o estado: ");

            if (!int.TryParse(Console.ReadLine(), out idUf))
            {
                Console.WriteLine("Digite apenas números.");
            }
            else if (idUf < 1 || idUf > 27)
            {
                Console.WriteLine("Escolha um estado entre 1 e 27.");
            }
            else
            {
                ufValida = true;
            }
        }

        usuario.IdUf = idUf;

        usuario.Cidade = cidade.Trim();

        // Montagem do endereço completo
        usuario.Endereco =
        $"CEP: {cep}, Nº: {numero}, Rua: {rua}, Bairro: {bairro}, Cidade: {cidade}, Estado: {banco.BuscarSiglaUf(usuario.IdUf)}";

        // A coluna DSC_ENDERECO do banco aceita no máximo 150 caracteres
        if (usuario.Endereco.Length > 150 || cidade.Trim().Length > 50)
        {
            Console.WriteLine("Endereço muito longo para o cadastro (máximo de 150 caracteres no total, cidade até 50).");
            Console.WriteLine("Cadastro cancelado. Tente novamente com a rua ou o bairro abreviados.");
            return;
        }

        bool dataValida = false;

        // Validação da data de nascimento
        while (!dataValida)
        {
            Console.Write("Data de nascimento (DDMMAAAA): ");

            string dataTexto = Console.ReadLine();

            if (dataTexto.Length == 8 &&
                dataTexto.All(char.IsDigit))
            {
                dataTexto = dataTexto.Insert(2, "/");
                dataTexto = dataTexto.Insert(5, "/");

                DateTime dataConvertida;

                if (DateTime.TryParse(dataTexto, out dataConvertida))
                {
                    usuario.DataNascimento = dataConvertida;

                    dataValida = true;
                }
                else
                {
                    Console.WriteLine("Data inválida.");
                }
            }
            else
            {
                Console.WriteLine("Digite 8 números.");
            }
        }

        usuario.DataCadastro = DateTime.Now;

        // Escolha do tipo de participação
        Console.WriteLine("Tipo de participação:");
        Console.WriteLine("1 - Quero doar");
        Console.WriteLine("2 - Quero receber doação");
        Console.WriteLine("3 - Ambos");

        bool tipoValido = false;

        // Repete até escolher uma opção válida (como no front)
        while (!tipoValido)
        {
            Console.Write("Escolha o tipo: ");

            string tipo = Console.ReadLine();

            switch (tipo)
            {
                case "1":
                    usuario.TipoParticipacao = "doar";
                    tipoValido = true;
                    break;

                case "2":
                    usuario.TipoParticipacao = "receber";
                    tipoValido = true;
                    break;

                case "3":
                    usuario.TipoParticipacao = "ambos";
                    tipoValido = true;
                    break;

                default:
                    Console.WriteLine("Opção inválida. Digite 1, 2 ou 3.");
                    break;
            }
        }

        // Criação do interesse do usuário
        Interesse interesse = new Interesse();

        interesse.IdUsuario = usuario.IdUsuario;

        // Descrição obrigatória (igual ao front)
        string descricao = "";

        while (string.IsNullOrWhiteSpace(descricao))
        {
            Console.Write("Descreva sua necessidade ou doação: ");

            descricao = Console.ReadLine();

            if (string.IsNullOrWhiteSpace(descricao))
            {
                Console.WriteLine("Escreva sua necessidade ou doação.");
            }
            else if (descricao.Trim().Length > 255)
            {
                Console.WriteLine("Descrição muito longa (máximo de 255 caracteres).");
                descricao = "";
            }
        }

        interesse.Descricao = descricao.Trim();

        // Todo interesse novo começa como "Pendente"
        interesse.IdStatus = banco.BuscarIdStatusPorNome("Pendente");

        interesse.Tipo = usuario.TipoParticipacao;

        interesse.DataRegistro = DateTime.Now;

        usuarios.Add(usuario);

        interesses.Add(interesse);

        // Cadastro do usuário no banco
        banco.InserirUsuario(usuario);

        interesse.IdUsuario = usuario.IdUsuario;

        // Cadastro do interesse no banco
        banco.InserirInteresse(interesse);

        Console.WriteLine("\nCadastro realizado com sucesso!");

        Console.WriteLine("O Sistema de Apoio ao Idoso agradece seu cadastro.");

        Console.WriteLine("A ONG entrará em contato com você via WhatsApp para mais informações.");
    }
    // Método responsável por listar usuários cadastrados
    static void ListarUsuarios()
    {
        List<Usuario> usuarios = banco.ListarUsuarios();

        Console.WriteLine("\n=== LISTA DE USUÁRIOS ===");

        foreach (Usuario usuario in usuarios)
        {
            Console.WriteLine($"Nome: {usuario.Nome}");
            Console.WriteLine($"CPF: {usuario.Cpf}");

            // Formatação do telefone
            if (!string.IsNullOrWhiteSpace(usuario.Telefone))
            {
                // Considera só os números (o front pode guardar com máscara)
                string numeros = new string(usuario.Telefone.Where(char.IsDigit).ToArray());

                if (numeros.Length == 11)
                {
                    Console.WriteLine($"Telefone: {Convert.ToUInt64(numeros).ToString(@"\(00\) 00000\-0000")}");
                }
                else
                {
                    Console.WriteLine($"Telefone: {usuario.Telefone}");
                }
            }

            Console.WriteLine($"Email: {usuario.Email}");
            Console.WriteLine($"Endereço: {usuario.Endereco}");
            Console.WriteLine($"Nascimento: {usuario.DataNascimento.ToShortDateString()}");

            Console.WriteLine("----------------------------");
        }
    }

    // Método responsável por buscar usuário pelo CPF
    static void BuscarUsuarioCpf()
    {
        List<Usuario> usuarios = banco.ListarUsuarios();

        Console.WriteLine("\n=== LISTA DE USUÁRIOS ===");

        foreach (Usuario usuarioLista in usuarios)
        {
            Console.WriteLine($"Nome: {usuarioLista.Nome}");
            Console.WriteLine($"CPF: {usuarioLista.Cpf}");
            Console.WriteLine($"Telefone: {usuarioLista.Telefone}");
            Console.WriteLine($"Email: {usuarioLista.Email}");
            Console.WriteLine($"Endereço: {usuarioLista.Endereco}");
            Console.WriteLine($"Estado: {banco.BuscarSiglaUf(usuarioLista.IdUf)}");

            Console.WriteLine("----------------------------");
        }

        Console.Write("\nDigite um CPF para buscar ou aperte ENTER para voltar: ");

        string cpf = Console.ReadLine();

        // Volta ao menu caso o usuário pressione ENTER
        if (string.IsNullOrWhiteSpace(cpf))
        {
            return;
        }

        // Validação do CPF
        while (cpf.Length != 11 ||
       !cpf.All(char.IsDigit) ||
       !ValidarCpf(cpf))
        {
            Console.WriteLine("CPF inválido.");

            Console.Write("Digite um CPF válido: ");

            cpf = Console.ReadLine();
        }

        Usuario usuario = banco.BuscarUsuarioPorCpf(cpf);

        if (usuario != null)
        {
            Console.WriteLine("\n=== USUÁRIO ENCONTRADO ===");

            Console.WriteLine($"Nome: {usuario.Nome}");

            // Formatação do CPF e do telefone (considera só os números,
            // pra não dar erro se algum registro vier com máscara)
            string cpfNumeros = new string((usuario.Cpf ?? "").Where(char.IsDigit).ToArray());
            string telNumeros = new string((usuario.Telefone ?? "").Where(char.IsDigit).ToArray());

            if (cpfNumeros.Length == 11)
            {
                Console.WriteLine($"CPF: {Convert.ToUInt64(cpfNumeros).ToString(@"000\.000\.000\-00")}");
            }
            else
            {
                Console.WriteLine($"CPF: {usuario.Cpf}");
            }

            if (telNumeros.Length == 11)
            {
                Console.WriteLine($"Telefone: {Convert.ToUInt64(telNumeros).ToString(@"\(00\) 00000\-0000")}");
            }
            else
            {
                Console.WriteLine($"Telefone: {usuario.Telefone}");
            }

            Console.WriteLine($"Email: {usuario.Email}");
            Console.WriteLine($"Endereço: {usuario.Endereco}");
            Console.WriteLine($"Estado: {banco.BuscarSiglaUf(usuario.IdUf)}");
        }
        else
        {
            Console.WriteLine("Usuário não encontrado.");
        }
    }

    // Método responsável por buscar interesses de um usuário
    static void BuscarInteressesUsuario()
    {
        Console.Write("\nDigite o nome do usuário: ");

        string nome = Console.ReadLine();

        Usuario usuarioEncontrado = banco.BuscarUsuarioPorNome(nome);

        if (usuarioEncontrado == null)
        {
            Console.WriteLine("Usuário não encontrado.");
            return;
        }

        List<Interesse> interessesUsuario =
            banco.BuscarInteressesPorUsuario(usuarioEncontrado.IdUsuario);

        Console.WriteLine($"\n=== INTERESSES DE {usuarioEncontrado.Nome} ===");

        foreach (Interesse interesse in interessesUsuario)
        {
            Console.WriteLine($"Usuário: {usuarioEncontrado.Nome}");
            Console.WriteLine($"Descrição: {interesse.Descricao}");
            Console.WriteLine($"Status: {banco.BuscarNomeStatus(interesse.IdStatus)}");
            Console.WriteLine($"Data: {interesse.DataRegistro:dd/MM/yyyy}");

            Console.WriteLine("----------------------------");
        }
    }

    // Método responsável por listar usuários e seus interesses
    static void UsuariosEInteresses()
    {
        List<Usuario> listaUsuarios = banco.ListarUsuarios();

        List<Interesse> listaInteresses = banco.ListarInteresses();

        Console.WriteLine("\n=== USUÁRIOS E INTERESSES ===");

        foreach (Usuario usuario in listaUsuarios)
        {
            Console.WriteLine($"\nNome: {usuario.Nome}");

            bool possuiInteresse = false;

            foreach (Interesse interesse in listaInteresses)
            {
                if (interesse.IdUsuario == usuario.IdUsuario)
                {
                    Console.WriteLine($"Tipo: {interesse.Tipo}");
                    Console.WriteLine($"Descrição: {interesse.Descricao}");
                    Console.WriteLine($"Status: {banco.BuscarNomeStatus(interesse.IdStatus)}");
                    Console.WriteLine($"Data: {interesse.DataRegistro:dd/MM/yyyy}");

                    Console.WriteLine("----------------------------");

                    possuiInteresse = true;
                }
            }

            if (!possuiInteresse)
            {
                Console.WriteLine("Nenhum interesse encontrado.");
            }
        }
    }

    // Método responsável por editar status dos interesses
    static void EditarStatusInteresse()
    {
        List<Interesse> interesses = banco.ListarInteresses();

        Console.WriteLine("\n=== EDITAR STATUS ===");

        List<string> lista = banco.ListarInteressesComUsuario();

        foreach (string item in lista)
        {
            Console.WriteLine(item);
        }

        Console.Write("Digite o nome do usuário: ");

        string nome = Console.ReadLine();

        // Validação do nome
        while (string.IsNullOrWhiteSpace(nome) || nome.Any(char.IsDigit))
        {
            Console.WriteLine("Nome inválido. Digite apenas letras.");

            Console.Write("Digite o nome do usuário: ");

            nome = Console.ReadLine();
        }

        Console.WriteLine("\n1 - Pendente");
        Console.WriteLine("2 - Em andamento");
        Console.WriteLine("3 - Concluído");

        Console.Write("Digite o número do novo status: ");

        string opcao = Console.ReadLine();

        string nomeStatus;

        switch (opcao)
        {
            case "1":
                nomeStatus = "Pendente";
                break;

            case "2":
                nomeStatus = "Em andamento";
                break;

            case "3":
                nomeStatus = "Concluído";
                break;

            default:
                Console.WriteLine("Opção inválida.");
                return;
        }

        // Descobre o id do status pelo nome (não depende de número fixo)
        int novoStatus = banco.BuscarIdStatusPorNome(nomeStatus);

        if (novoStatus == 0)
        {
            Console.WriteLine($"O status \"{nomeStatus}\" não existe no banco. Rode o script banco_completo.sql.");
            return;
        }

        Usuario usuario = banco.BuscarUsuarioPorNome(nome);

        if (usuario == null)
        {
            Console.WriteLine("Usuário não encontrado.");
            return;
        }

        List<Interesse> listaInteresses =
            banco.BuscarInteressesPorUsuario(usuario.IdUsuario);

        foreach (Interesse interesse in listaInteresses)
        {
            banco.AtualizarStatusInteresse(interesse.IdInteresse, novoStatus);
        }

        Console.WriteLine("Status atualizado com sucesso!");
    }

    // Método responsável por remover interesses
    static void RemoverInteresse()
    {
        Console.WriteLine("\n=== REMOVER INTERESSE ===");

        List<Interesse> interesses = banco.ListarInteresses();

        List<string> lista = banco.ListarInteressesComUsuario();

        foreach (string item in lista)
        {
            Console.WriteLine(item);
        }

        Console.Write("Digite o nome do usuário: ");

        string nome = Console.ReadLine();

        Usuario usuario = banco.BuscarUsuarioPorNome(nome);

        if (usuario == null)
        {
            Console.WriteLine("Usuário não encontrado.");
            return;
        }

        List<Interesse> listainteresses =
            banco.BuscarInteressesPorUsuario(usuario.IdUsuario);

        if (listainteresses.Count == 0)
        {
            Console.WriteLine("Nenhum interesse encontrado.");
            return;
        }

        Interesse interesse = listainteresses.Last();

        banco.RemoverInteresse(interesse.IdInteresse);

        Console.WriteLine("Interesse removido com sucesso!");
    }

    // Método responsável por editar interesses
    static void EditarInteresse()
    {
        List<string> lista = banco.ListarInteressesComUsuario();

        Console.WriteLine("\n=== INTERESSES CADASTRADOS ===");

        foreach (string item in lista)
        {
            Console.WriteLine(item);
        }

        Console.Write("Digite o nome do usuário: ");

        string nome = Console.ReadLine();

        Usuario usuario = banco.BuscarUsuarioPorNome(nome);

        if (usuario == null)
        {
            Console.WriteLine("Usuário não encontrado.");
            return;
        }

        List<Interesse> interesses =
            banco.BuscarInteressesPorUsuario(usuario.IdUsuario);

        if (interesses.Count == 0)
        {
            Console.WriteLine("Esse usuário não possui interesses.");
            return;
        }

        Interesse interesse = interesses.Last();

        Console.WriteLine("\n=== EDITAR INTERESSE ===");

        Console.WriteLine($"Tipo atual: {interesse.Tipo}");

        Console.Write("Novo tipo - doar, receber ou ambos (ENTER para manter): ");

        string novoTipo = Console.ReadLine();

        // Mantém o valor atual caso o usuário pressione ENTER
        if (!string.IsNullOrWhiteSpace(novoTipo))
        {
            novoTipo = novoTipo.Trim().ToLower();

            // Só aceita os mesmos tipos do front
            if (novoTipo != "doar" && novoTipo != "receber" && novoTipo != "ambos")
            {
                Console.WriteLine("Tipo inválido. Use: doar, receber ou ambos.");
                return;
            }

            interesse.Tipo = novoTipo;
        }

        Console.WriteLine($"Descrição atual: {interesse.Descricao}");

        Console.Write("Nova descrição (ENTER para manter): ");

        string novaDescricao = Console.ReadLine();

        // Mantém a descrição atual caso o usuário pressione ENTER
        if (!string.IsNullOrWhiteSpace(novaDescricao))
        {
            interesse.Descricao = novaDescricao;
        }

        Console.WriteLine($"Status atual: {banco.BuscarNomeStatus(interesse.IdStatus)}");

        Console.Write("Novo status (pendente, em andamento ou concluido) ou ENTER para manter: ");

        string opcao = Console.ReadLine().ToLower();

        if (!string.IsNullOrWhiteSpace(opcao))
        {
            string nomeStatus;

            switch (opcao.Trim())
            {
                case "pendente":
                    nomeStatus = "Pendente";
                    break;

                case "em andamento":
                    nomeStatus = "Em andamento";
                    break;

                case "concluido":
                case "concluído":
                    nomeStatus = "Concluído";
                    break;

                default:
                    Console.WriteLine("Status inválido.");
                    return;
            }

            // Descobre o id do status pelo nome (não depende de número fixo)
            int novoStatus = banco.BuscarIdStatusPorNome(nomeStatus);

            if (novoStatus == 0)
            {
                Console.WriteLine($"O status \"{nomeStatus}\" não existe no banco. Rode o script banco_completo.sql.");
                return;
            }

            interesse.IdStatus = novoStatus;
        }

        banco.AtualizarInteresse(interesse);

        Console.WriteLine("Interesse atualizado com sucesso!");
    }

    // Método responsável por remover administradores
    static void RemoverAdministrador()
    {
        Console.WriteLine("\n=== REMOVER ADMINISTRADOR ===");

        Console.Write("Digite o email do administrador: ");

        string email = Console.ReadLine();

        // Verifica se o email foi digitado
        if (string.IsNullOrWhiteSpace(email))
        {
            Console.WriteLine("Email não pode ficar vazio.");
            return;
        }

        Admin admin = banco.BuscarAdminPorEmail(email);

        if (admin == null)
        {
            Console.WriteLine("Administrador não encontrado.");
            return;
        }

        banco.RemoverAdministrador(email);

        Console.WriteLine("Administrador removido com sucesso!");
    }

    // Método responsável pelo login do sistema
    static void LoginSistema()
    {
        Console.WriteLine("\n=== LOGIN DO SISTEMA ===");

        Console.WriteLine("1 - Administrador");
        Console.WriteLine("2 - Atendente");
        Console.WriteLine("3 - Esqueci minha senha");

        Console.Write("Escolha o perfil: ");

        string perfil = Console.ReadLine();

        // Recuperação de senha do atendente
        if (perfil == "3")
        {
            EsqueciSenhaAtendente();
            return;
        }

        Console.Write("Email: ");

        string email = Console.ReadLine();

        Console.Write("Senha: ");

        string senha = LerSenha();

        // Login do administrador
        if (perfil == "1")
        {
            bool loginValido = banco.LoginAdmin(email, senha);

            if (loginValido)
            {
                Console.WriteLine("Login de administrador realizado!");

                usuarioLogado = email;

                MenuAdmin();
            }
            else
            {
                Console.WriteLine("Email ou senha inválidos.");
            }
        }

        // Login do atendente
        else if (perfil == "2")
        {
            bool loginValido = banco.LoginAtendente(email, senha);

            if (loginValido)
            {
                Console.WriteLine("Login de atendente realizado!");

                usuarioLogado = email;

                MenuAtendente();
            }
            else
            {
                Console.WriteLine("Email ou senha inválidos.");
            }
        }
        else
        {
            Console.WriteLine("Perfil inválido.");
        }
    }
    // Método responsável pelo login administrativo
    static void Login()
    {
        Console.WriteLine("\n=== LOGIN ADMINISTRATIVO ===");

        Console.WriteLine("1 - Fazer login");
        Console.WriteLine("2 - Esqueci minha senha");

        Console.Write("Escolha uma opção: ");

        int opcaoLogin;

        // Validação para aceitar apenas números
        if (!int.TryParse(Console.ReadLine(), out opcaoLogin))
        {
            Console.WriteLine("Digite apenas números.");
            return;
        }

        // Recuperação de senha
        if (opcaoLogin == 2)
        {
            EsqueciSenha();
            return;
        }

        Console.Write("Email do administrador: ");

        string email = Console.ReadLine();

        Console.Write("Senha: ");

        string senha = LerSenha();

        bool loginValido = banco.LoginAdmin(email, senha);

        bool loginAtendente = banco.LoginAtendente(email, senha);

        // Login administrador
        if (loginValido)
        {
            Console.WriteLine("Login realizado com sucesso!");

            MenuAdmin();
        }

        // Login atendente
        else if (loginAtendente)
        {
            Console.WriteLine("Login realizado com sucesso!");

            MenuAtendente();
        }
        else
        {
            Console.WriteLine("Email ou senha inválidos.");

            Login();
        }
    }

    // Método responsável pela recuperação de senha
    static void EsqueciSenha()
    {
        Console.WriteLine("\n=== RECUPERAÇÃO DE SENHA ===");

        Console.Write("Digite o email da ONG: ");

        string email = Console.ReadLine();

        bool existe = banco.EmailAdminExiste(email);

        if (existe)
        {
            Console.WriteLine("Um link de recuperação foi enviado para o email da ONG.");
        }
        else
        {
            Console.WriteLine("Email não encontrado");
        }
    }

    // Método responsável pelo cadastro de administradores
    static void CadastrarAdministrador()
    {
        Console.WriteLine("\n=== CADASTRO ADMINISTRADOR ===");

        string nome = "";

        bool nomeValido = false;

        // Validação do nome
        while (!nomeValido)
        {
            Console.Write("Nome completo: ");

            nome = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(nome) &&
                nome.All(c => char.IsLetter(c) || char.IsWhiteSpace(c)))
            {
                nomeValido = true;
            }
            else
            {
                Console.WriteLine("Nome inválido.");
            }
        }

        string email = "";

        bool emailValido = false;

        // Validação do email
        while (!emailValido)
        {
            Console.Write("Email: ");

            email = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(email) &&
    email.Contains("@") &&
    email.Contains("."))
            {
                // Verifica se o email já existe
                if (banco.EmailAdminExiste(email))
                {
                    Console.WriteLine("Email já cadastrado.");
                    continue;
                }

                emailValido = true;
            }
            else
            {
                Console.WriteLine("Email inválido.");
            }
        }

        Console.Write("Senha: ");

        string senha = LerSenha();

        if (string.IsNullOrWhiteSpace(senha))
        {
            Console.WriteLine("Senha inválida.");
            return;
        }

        banco.InserirAdmin(nome, email, senha);

        Console.WriteLine("Administrador cadastrado com sucesso!");
    }

    // Método responsável pelo cadastro de atendentes
    static void CadastrarAtendente()
    {
        Console.WriteLine("\n=== CADASTRO ATENDENTE ===");

        string nome = "";

        bool nomeValido = false;

        // Validação do nome
        while (!nomeValido)
        {
            Console.Write("Nome completo: ");

            nome = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(nome) &&
                nome.All(c => char.IsLetter(c) || char.IsWhiteSpace(c)))
            {
                nomeValido = true;
            }
            else
            {
                Console.WriteLine("Nome inválido. Não use números.");
            }
        }

        string email = "";

        bool emailValido = false;

        // Validação do email
        while (!emailValido)
        {
            Console.Write("Email: ");

            email = Console.ReadLine();

            if (!string.IsNullOrWhiteSpace(email) &&
     email.Contains("@") &&
     email.Contains("."))
            {
                // Verifica se o email já existe
                if (banco.EmailAtendenteExiste(email))
                {
                    Console.WriteLine("Email já cadastrado.");
                    continue;
                }

                emailValido = true;
            }
            else
            {
                Console.WriteLine("Email inválido.");
            }
        }

        Console.Write("Senha: ");

        string senha = LerSenha();

        if (string.IsNullOrWhiteSpace(senha))
        {
            Console.WriteLine("Senha inválida.");
            return;
        }

        banco.InserirAtendente(nome, email, senha);

        Console.WriteLine("Atendente cadastrado com sucesso!");
    }

    // Método responsável pelo menu administrativo
    static void MenuAdmin()
    {
        int opcaoAdmin = 0;

        // Loop do menu administrador
        while (opcaoAdmin != 20)
        {
            Console.WriteLine("\n=== MENU ADMINISTRATIVO ===");

            Console.WriteLine("1 - Listar usuários");
            Console.WriteLine("2 - Situação do interesse");
            Console.WriteLine("3 - Buscar interesses por usuário");
            Console.WriteLine("4 - Listar interesses");
            Console.WriteLine("5 - Editar status do interesse");
            Console.WriteLine("6 - Editar usuario");
            Console.WriteLine("7 - Editar interesse");
            Console.WriteLine("8 - Editar atendente");
            Console.WriteLine("9 - Editar administrador");
            Console.WriteLine("10 - Remover interesse");
            Console.WriteLine("11 - Remover administrador");
            Console.WriteLine("12 - Remover atendente");
            Console.WriteLine("13 - Consultar dados da ONG");
            Console.WriteLine("14 - Editar perfil da ONG");
            Console.WriteLine("15 - Cadastrar administrador");
            Console.WriteLine("16 - Cadastrar atendente");
            Console.WriteLine("17 - Remover usuário");
            Console.WriteLine("18 - Listar administradores");
            Console.WriteLine("19 - Listar atendentes");
            Console.WriteLine("20 - Sair");

            Console.Write("Escolha uma opção: ");

            // Validação para aceitar apenas números
            if (!int.TryParse(Console.ReadLine(), out opcaoAdmin))
            {
                Console.WriteLine("Digite apenas números.");
                continue;
            }

            switch (opcaoAdmin)
            {
                case 1:
                    ListarUsuarios();
                    break;

                case 2:
                    SituacaoInteresse();
                    break;

                case 3:
                    BuscarInteressesUsuario();
                    break;

                case 4:
                    UsuariosEInteresses();
                    break;

                case 5:
                    EditarStatusInteresse();
                    break;

                case 6:
                    EditarUsuario();
                    break;

                case 7:
                    EditarInteresse();
                    break;

                case 8:
                    EditarAtendente();
                    break;

                case 9:
                    EditarAdministrador();
                    break;

                case 10:
                    RemoverInteresse();
                    break;

                case 11:
                    RemoverAdministrador();
                    break;

                case 12:
                    RemoverAtendente();
                    break;

                case 13:
                    ConsultarOng();
                    break;

                case 14:
                    EditarPerfilOng();
                    break;

                case 15:
                    CadastrarAdministrador();
                    break;

                case 16:
                    CadastrarAtendente();
                    break;

                case 17:
                    RemoverUsuario();
                    break;

                case 18:
                    ListarAdministradores();
                    break;

                case 19:
                    ListarAtendentes();
                    break;

                case 20:
                    Console.WriteLine("Saindo da área administrativa.");
                    break;

                default:
                    Console.WriteLine("Opção inválida!");
                    break;
            }
        }
    }
    // Método responsável pelo menu do atendente
    static void MenuAtendente()
    {
        int opcao = 0;

        // Loop do menu atendente
        while (opcao != 10)
        {
            Console.WriteLine("\n=== MENU ATENDENTE ===");

            Console.WriteLine("1 - Listar usuários");
            Console.WriteLine("2 - Listar interesses");
            Console.WriteLine("3 - Situação do interesse");
            Console.WriteLine("4 - Editar status do interesse");
            Console.WriteLine("5 - Editar usuário");
            Console.WriteLine("6 - Buscar interesse por usuário");
            Console.WriteLine("7 - Editar interesse");
            Console.WriteLine("8 - Remover interesse");
            Console.WriteLine("9 - Consultar dados da ONG");
            Console.WriteLine("10 - Sair");

            Console.Write("Escolha uma opção: ");

            // Validação para aceitar apenas números
            if (!int.TryParse(Console.ReadLine(), out opcao))
            {
                Console.WriteLine("Digite apenas números.");
                continue;
            }

            switch (opcao)
            {
                case 1:
                    ListarUsuarios();
                    break;

                case 2:
                    UsuariosEInteresses();
                    break;

                case 3:
                    SituacaoInteresse();
                    break;

                case 4:
                    EditarStatusInteresse();
                    break;

                case 5:
                    EditarUsuario();
                    break;

                case 6:
                    BuscarInteressesUsuario();
                    break;

                case 7:
                    EditarInteresse();
                    break;

                case 8:
                    RemoverInteresse();
                    break;

                case 9:
                    ConsultarOng();
                    break;

                case 10:
                    Console.WriteLine("Saindo...");
                    break;

                default:
                    Console.WriteLine("Opção inválida!");
                    break;
            }
        }
    }
    // Método responsável por consultar dados da ONG
    static void ConsultarOng()
    {
        banco.ConsultarOng();
    }

    // Método responsável por editar perfil da ONG
    static void EditarPerfilOng()
    {
        Console.WriteLine("\n=== EDITAR PERFIL DA ONG ===");

        string telefone = "";

        bool telefoneValido = false;

        // Validação do telefone
        while (!telefoneValido)
        {
            Console.Write("Novo telefone (ENTER para manter): ");

            telefone = Console.ReadLine();

            if (string.IsNullOrWhiteSpace(telefone))
            {
                telefone = null;

                telefoneValido = true;
            }
            else if (telefone.Length == 11 &&
                     telefone.All(char.IsDigit))
            {
                telefoneValido = true;
            }
            else
            {
                Console.WriteLine("Telefone inválido.");
            }
        }

        string email = "";

        bool emailValido = false;

        // Validação do email
        while (!emailValido)
        {
            Console.Write("Novo email (ENTER para manter): ");

            email = Console.ReadLine();

            if (string.IsNullOrWhiteSpace(email))
            {
                email = null;

                emailValido = true;
            }
            else if (email.Contains("@") &&
                     email.Contains("."))
            {
                emailValido = true;
            }
            else
            {
                Console.WriteLine("Email inválido.");
            }
        }

        string endereco = "";

        bool enderecoValido = false;

        // Validação do endereço
        while (!enderecoValido)
        {
            Console.Write("Novo endereço (ENTER para manter): ");

            endereco = Console.ReadLine();

            if (string.IsNullOrWhiteSpace(endereco))
            {
                endereco = null;

                enderecoValido = true;
            }
            else if (endereco.Length >= 5 &&
                     endereco.Any(char.IsLetter))
            {
                enderecoValido = true;
            }
            else
            {
                Console.WriteLine("Endereço inválido.");
            }
        }

        banco.AtualizarOng(telefone, email, endereco);

        Console.WriteLine("Perfil da ONG atualizado com sucesso!");
    }

    // Método responsável pelos lembretes de saúde
    static void LembreteSaude()
    {
        Console.WriteLine("\n=== LEMBRETES DE SAÚDE ===");

        Console.WriteLine("• Tome os medicamentos corretamente");
        Console.WriteLine("• Beba bastante água");
        Console.WriteLine("• Faça exames regularmente");
        Console.WriteLine("• Mantenha alimentação saudável");
    }

    // Método responsável pelos alertas de golpes
    static void PrevençãodeGolpes()
    {
        Console.WriteLine("\n=== PREVENÇÃO DE GOLPES ===");

        Console.WriteLine("• Nunca compartilhe senhas");
        Console.WriteLine("• Desconfie de ligações bancárias");
        Console.WriteLine("• Não clique em links desconhecidos");
        Console.WriteLine("• Confirme informações com familiares");
    }

    // Método responsável por editar usuários
    static void EditarUsuario()
    {
        Console.WriteLine("\n=== USUÁRIOS CADASTRADOS ===");

        List<Usuario> usuarios = banco.ListarUsuarios();

        foreach (Usuario item in usuarios)
        {
            Console.WriteLine($"Nome: {item.Nome} | CPF: {item.Cpf}");
        }

        Console.Write("Digite o nome do usuário que deseja editar: ");

        string nome = Console.ReadLine();

        Usuario usuario = banco.BuscarUsuarioPorNome(nome);

        if (usuario == null)
        {
            Console.WriteLine("Usuário não encontrado.");
            return;
        }

        if (usuario.Telefone == null)
        {
            usuario.Telefone = "";
        }

        if (usuario.Email == null)
        {
            usuario.Email = "";
        }

        if (usuario.Endereco == null)
        {
            usuario.Endereco = "";
        }

        Console.WriteLine("\n=== DADOS ATUAIS ===");

        Console.WriteLine($"Telefone atual: {usuario.Telefone}");

        bool telefoneValido = false;

        // Validação do telefone
        while (!telefoneValido)
        {
            Console.Write("Novo telefone (ENTER para manter): ");

            string novoTelefone = Console.ReadLine();

            // Mantém o valor atual caso o usuário pressione ENTER
            if (string.IsNullOrWhiteSpace(novoTelefone))
            {
                telefoneValido = true;
            }
            else if (novoTelefone.Length == 11 &&
                     novoTelefone.All(char.IsDigit))
            {
                usuario.Telefone = novoTelefone;

                telefoneValido = true;
            }
            else
            {
                Console.WriteLine("Telefone inválido.");
            }
        }

        Console.WriteLine($"\nEmail atual: {usuario.Email}");

        bool emailValido = false;

        // Validação do email
        while (!emailValido)
        {
            Console.Write("Novo email (ENTER para manter): ");

            string novoEmail = Console.ReadLine();

            // Mantém o valor atual caso o usuário pressione ENTER
            if (string.IsNullOrWhiteSpace(novoEmail))
            {
                emailValido = true;
            }
            else if (novoEmail.Contains("@") &&
                     novoEmail.Contains("."))
            {
                usuario.Email = novoEmail;

                emailValido = true;
            }
            else
            {
                Console.WriteLine("Email inválido.");
            }
        }

        Console.WriteLine($"\nEndereço atual: {usuario.Endereco}");

        Console.Write("Novo endereço (ENTER para manter): ");

        string novoEndereco = Console.ReadLine();

        // Mantém o endereço atual caso o usuário pressione ENTER
        if (!string.IsNullOrWhiteSpace(novoEndereco))
        {
            usuario.Endereco = novoEndereco;
        }

        banco.AtualizarUsuario(usuario);

        Console.WriteLine("\nUsuário atualizado com sucesso!");
    }
    // Método responsável por editar atendentes
    static void EditarAtendente()
    {
        Console.WriteLine("\n=== EDITAR ATENDENTE ===");

        Console.Write("Digite o email do atendente: ");

        string emailAtual = Console.ReadLine();

        Atendente atendente = banco.BuscarAtendentePorEmail(emailAtual);

        if (atendente == null)
        {
            Console.WriteLine("Atendente não encontrado.");
            return;
        }

        string novoEmail = "";

        bool emailValido = false;

        // Validação do email
        while (!emailValido)
        {
            Console.Write("Novo email (ENTER para manter): ");

            novoEmail = Console.ReadLine();

            // Mantém o email atual caso o usuário pressione ENTER
            if (string.IsNullOrWhiteSpace(novoEmail))
            {
                novoEmail = atendente.Email;

                emailValido = true;
            }
            else if (novoEmail.Contains("@") &&
                     novoEmail.Contains("."))
            {
                // Verifica se o email já existe
                if (banco.EmailAtendenteExiste(novoEmail) &&
                    novoEmail != emailAtual)
                {
                    Console.WriteLine("Email já cadastrado.");
                }
                else
                {
                    emailValido = true;
                }
            }
            else
            {
                Console.WriteLine("Email inválido.");
            }
        }

        Console.Write("Nova senha (ENTER para manter): ");

        string novaSenha = Console.ReadLine();

        banco.EditarAtendente(emailAtual, novoEmail, novaSenha);

        Console.WriteLine("Atendente atualizado com sucesso!");
    }

    // Método responsável por mostrar situação dos interesses
    static void SituacaoInteresse()
    {
        Console.WriteLine("\n=== SITUAÇÃO DOS INTERESSES ===");

        List<string> lista = banco.ListarInteressesComUsuario();

        foreach (string item in lista)
        {
            Console.WriteLine(item);
        }
    }

    // Método responsável por validar CPF
    static bool ValidarCpf(string cpf)
    {
        if (cpf.Length != 11)
            return false;

        // Verifica se todos os números são iguais
        if (cpf.All(c => c == cpf[0]))
            return false;

        int soma = 0;

        // Primeiro dígito verificador
        for (int i = 0; i < 9; i++)
        {
            soma += (cpf[i] - '0') * (10 - i);
        }

        int resto = (soma * 10) % 11;

        if (resto == 10)
            resto = 0;

        if (resto != (cpf[9] - '0'))
            return false;

        soma = 0;

        // Segundo dígito verificador
        for (int i = 0; i < 10; i++)
        {
            soma += (cpf[i] - '0') * (11 - i);
        }

        resto = (soma * 10) % 11;

        if (resto == 10)
            resto = 0;

        return resto == (cpf[10] - '0');
    }

    // Método responsável por ocultar senha digitada
    static string LerSenha()
    {
        string senha = "";

        ConsoleKeyInfo tecla;

        do
        {
            tecla = Console.ReadKey(true);

            // Adiciona caracteres da senha
            if (tecla.Key != ConsoleKey.Backspace &&
                tecla.Key != ConsoleKey.Enter)
            {
                senha += tecla.KeyChar;

                Console.Write("*");
            }

            // Remove caracteres ao apertar Backspace
            else if (tecla.Key == ConsoleKey.Backspace &&
                     senha.Length > 0)
            {
                senha = senha.Substring(0, senha.Length - 1);

                Console.Write("\b \b");
            }

        } while (tecla.Key != ConsoleKey.Enter);

        Console.WriteLine();

        return senha;
    }

    // Método responsável por remover atendentes
    static void RemoverAtendente()
    {
        Console.WriteLine("\n=== REMOVER ATENDENTE ===");

        Console.Write("Digite o email do atendente: ");

        string email = Console.ReadLine();

        bool existe = banco.EmailAtendenteExiste(email);

        if (existe)
        {
            banco.RemoverAtendente(email);

            Console.WriteLine("Atendente removido com sucesso!");
        }
        else
        {
            Console.WriteLine("Email não encontrado.");
        }
    }

    // Método responsável pela recuperação de senha do atendente
    static void EsqueciSenhaAtendente()
    {
        Console.WriteLine("\n=== RECUPERAÇÃO DE SENHA ATENDENTE ===");

        Console.Write("Digite o email do atendente: ");

        string email = Console.ReadLine();

        bool existe = banco.EmailAtendenteExiste(email);

        if (existe)
        {
            Console.WriteLine("Link de recuperação enviado!");
        }
        else
        {
            Console.WriteLine("Email não encontrado.");
        }
    }

    // Método responsável pela recuperação de senha do administrador
    static void EsqueciSenhaAdministrador()
    {
        Console.WriteLine("\n=== RECUPERAR SENHA ADMINISTRADOR ===");

        Console.Write("Digite o email do administrador: ");

        string email = Console.ReadLine();

        Admin admin = banco.BuscarAdminPorEmail(email);

        if (admin == null)
        {
            Console.WriteLine("Administrador não encontrado.");
            return;
        }

        Console.Write("Digite a nova senha: ");

        string novaSenha = Console.ReadLine();

        banco.AtualizarSenhaAdmin(email, novaSenha);

        Console.WriteLine("Senha alterada com sucesso!");
    }

    // Método responsável por editar administradores
    // Método responsável por remover usuários (somente administrador)
    static void RemoverUsuario()
    {
        Console.WriteLine("\n=== REMOVER USUÁRIO ===");

        List<Usuario> lista = banco.ListarUsuarios();

        foreach (Usuario item in lista)
        {
            Console.WriteLine($"Nome: {item.Nome} | CPF: {item.Cpf}");
        }

        Console.Write("Digite o CPF do usuário que deseja remover: ");

        string cpf = Console.ReadLine();

        // Aceita CPF com ponto e traço, valida só os números
        cpf = new string((cpf ?? "").Where(char.IsDigit).ToArray());

        Usuario usuario = banco.BuscarUsuarioPorCpf(cpf);

        if (usuario == null)
        {
            Console.WriteLine("Usuário não encontrado.");
            return;
        }

        Console.Write($"Tem certeza que deseja remover {usuario.Nome}? Os interesses dele também serão apagados. (S/N): ");

        string confirmacao = Console.ReadLine();

        if (confirmacao == null || confirmacao.Trim().ToUpper() != "S")
        {
            Console.WriteLine("Remoção cancelada.");
            return;
        }

        banco.RemoverUsuario(usuario.IdUsuario);

        Console.WriteLine("Usuário removido com sucesso!");
    }

    // Método responsável por listar administradores
    static void ListarAdministradores()
    {
        Console.WriteLine("\n=== ADMINISTRADORES ===");

        List<Admin> administradores = banco.ListarAdministradores();

        foreach (Admin admin in administradores)
        {
            Console.WriteLine($"Nome: {admin.Nome} | Email: {admin.Email}");
        }

        if (administradores.Count == 0)
        {
            Console.WriteLine("Nenhum administrador cadastrado.");
        }
    }

    // Método responsável por listar atendentes
    static void ListarAtendentes()
    {
        Console.WriteLine("\n=== ATENDENTES ===");

        List<Atendente> atendentes = banco.ListarAtendentes();

        foreach (Atendente atendente in atendentes)
        {
            Console.WriteLine($"Nome: {atendente.Nome} | Email: {atendente.Email}");
        }

        if (atendentes.Count == 0)
        {
            Console.WriteLine("Nenhum atendente cadastrado.");
        }
    }

    static void EditarAdministrador()
    {
        Console.WriteLine("\n=== EDITAR ADMINISTRADOR ===");

        Console.Write("Digite o email do administrador: ");

        string emailAtual = Console.ReadLine();

        Admin admin = banco.BuscarAdminPorEmail(emailAtual);

        if (admin == null)
        {
            Console.WriteLine("Administrador não encontrado.");
            return;
        }

        string novoEmail = "";

        bool emailValido = false;

        // Validação do email
        while (!emailValido)
        {
            Console.Write("Novo email (ENTER para manter): ");

            novoEmail = Console.ReadLine();

            // Mantém o email atual caso o usuário pressione ENTER
            if (string.IsNullOrWhiteSpace(novoEmail))
            {
                novoEmail = admin.Email;

                emailValido = true;
            }
            else if (novoEmail.Contains("@") &&
                     novoEmail.Contains("."))
            {
                // Verifica se o email já existe
                if (banco.EmailAdminExiste(novoEmail) &&
                    novoEmail != emailAtual)
                {
                    Console.WriteLine("Email já cadastrado.");
                }
                else
                {
                    emailValido = true;
                }
            }
            else
            {
                Console.WriteLine("Email inválido.");
            }
        }

        Console.Write("Nova senha (ENTER para manter): ");

        string novaSenha = Console.ReadLine();

        banco.EditarAdministrador(
            emailAtual,
            novoEmail,
            novaSenha);

        // Nome (mesma regra do cadastro: só letras e espaços)
        bool nomeValido = false;

        while (!nomeValido)
        {
            Console.Write("Novo nome (ENTER para manter): ");

            string novoNome = Console.ReadLine();

            if (string.IsNullOrWhiteSpace(novoNome))
            {
                nomeValido = true;
            }
            else if (novoNome.All(c => char.IsLetter(c) || char.IsWhiteSpace(c)))
            {
                banco.AtualizarNomeAdministrador(novoEmail, novoNome.Trim());
                nomeValido = true;
            }
            else
            {
                Console.WriteLine("Nome inválido.");
            }
        }

        // Celular (DDD + número = 11 dígitos)
        bool celularValido = false;

        while (!celularValido)
        {
            Console.Write("Novo celular (ENTER para manter): ");

            string novoCelular = Console.ReadLine();

            if (string.IsNullOrWhiteSpace(novoCelular))
            {
                celularValido = true;
            }
            else if (novoCelular.Length == 11 &&
                     novoCelular.All(char.IsDigit))
            {
                try
                {
                    banco.AtualizarTelefoneAdministrador(novoEmail, novoCelular);
                }
                catch (Exception erro)
                {
                    Console.WriteLine("Não foi possível salvar o celular: " + erro.Message);
                    Console.WriteLine("Rode o script banco_completo.sql (ele cria essa coluna).");
                }

                celularValido = true;
            }
            else
            {
                Console.WriteLine("Telefone inválido.");
            }
        }

        Console.WriteLine("Administrador atualizado com sucesso!");
    }
}