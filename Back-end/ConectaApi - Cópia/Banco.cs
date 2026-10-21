using System.Data.SqlClient;
using System.Security.Cryptography;
using System.Text;

// Classe responsável pela conexão e manipulação do banco de dados
public class Banco
{
    // String de conexão com o SQL Server
    private string conexao = @"Server=localhost\SQLEXPRESS;Database=dbSistemaDoacao;Trusted_Connection=True;";

    // Método responsável por abrir conexão com o banco
    public SqlConnection AbrirConexao()
    {
        SqlConnection conn = new SqlConnection(conexao);

        conn.Open();

        return conn;
    }

    // Método responsável por inserir usuários no banco
    public void InserirUsuario(Usuario usuario)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"INSERT INTO USUARIO
(ID_ADMIN, ID_ATENDENTE, ID_UF, DSC_NOME, NUM_CPF,
NUM_TELEFONE, DSC_EMAIL, DSC_ENDERECO,
DSC_CIDADE, DATA_NASCIMENTO)

OUTPUT INSERTED.ID_USUARIO

VALUES
(
6, 
1,
@iduf,
@nome, 
@cpf, 
@telefone,
@email,
@endereco, 
@cidade,
@dataNascimento
)";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@nome", usuario.Nome);
        cmd.Parameters.AddWithValue("@cpf", usuario.Cpf);
        cmd.Parameters.AddWithValue("@telefone", usuario.Telefone);
        cmd.Parameters.AddWithValue("@email", usuario.Email ?? "");
        cmd.Parameters.AddWithValue("@endereco", usuario.Endereco);
        cmd.Parameters.AddWithValue("@iduf", usuario.IdUf);
        cmd.Parameters.AddWithValue("@cidade", usuario.Cidade ?? "");
        cmd.Parameters.AddWithValue("@dataNascimento", usuario.DataNascimento);

        // Retorna automaticamente o ID gerado no banco
        usuario.IdUsuario = Convert.ToInt32(cmd.ExecuteScalar());

        conn.Close();
    }
    // Método responsável por inserir interesses no banco
    public void InserirInteresse(Interesse interesse)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"INSERT INTO INTERESSE
(ID_USUARIO, ID_STATUS, DSC_TIPO, DSC_DESCRICAO, DATA_REGISTRO)

VALUES
(@idUsuario, 1, @tipo, @descricao, @dataRegistro)";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@idUsuario", interesse.IdUsuario);
        cmd.Parameters.AddWithValue("@tipo", interesse.Tipo);
        cmd.Parameters.AddWithValue("@descricao", interesse.Descricao);
        cmd.Parameters.AddWithValue("@status", interesse.IdStatus);
        cmd.Parameters.AddWithValue("@dataRegistro", interesse.DataRegistro);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por atualizar dados do usuário
    public void AtualizarUsuario(Usuario usuario)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"UPDATE USUARIO
               SET NUM_TELEFONE = @telefone,
                   DSC_EMAIL = @email,
                   DSC_ENDERECO = @endereco
               WHERE NUM_CPF = @cpf";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@telefone", usuario.Telefone);
        cmd.Parameters.AddWithValue("@email", usuario.Email);
        cmd.Parameters.AddWithValue("@endereco", usuario.Endereco);
        cmd.Parameters.AddWithValue("@cpf", usuario.Cpf);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por buscar usuário pelo CPF
    public Usuario BuscarUsuarioPorCpf(string cpf)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM USUARIO WHERE NUM_CPF = @cpf";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@cpf", cpf);

        SqlDataReader reader = cmd.ExecuteReader();

        Usuario usuario = null;

        if (reader.Read())
        {
            usuario = new Usuario();

            usuario.IdUsuario = Convert.ToInt32(reader["ID_USUARIO"]);
            usuario.Nome = reader["DSC_NOME"].ToString();
            usuario.Cpf = reader["NUM_CPF"].ToString();
            usuario.Telefone = reader["NUM_TELEFONE"].ToString();
            usuario.Email = reader["DSC_EMAIL"].ToString();
            usuario.Endereco = reader["DSC_ENDERECO"].ToString();
            usuario.Cidade = reader["DSC_CIDADE"].ToString();
            usuario.IdUf = Convert.ToInt32(reader["ID_UF"]);
            usuario.DataNascimento = Convert.ToDateTime(reader["DATA_NASCIMENTO"]);
        }

        conn.Close();

        return usuario;
    }

    // Método responsável por listar todos os usuários cadastrados
    public List<Usuario> ListarUsuarios()
    {
        List<Usuario> usuarios = new List<Usuario>();

        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM USUARIO";

        SqlCommand cmd = new SqlCommand(sql, conn);

        SqlDataReader reader = cmd.ExecuteReader();

        while (reader.Read())
        {
            Usuario usuario = new Usuario();

            usuario.IdUsuario = Convert.ToInt32(reader["ID_USUARIO"]);
            usuario.Nome = reader["DSC_NOME"].ToString();
            usuario.Cpf = reader["NUM_CPF"].ToString();
            usuario.Telefone = reader["NUM_TELEFONE"].ToString();
            usuario.Email = reader["DSC_EMAIL"].ToString();
            usuario.Endereco = reader["DSC_ENDERECO"].ToString();
            usuario.Cidade = reader["DSC_CIDADE"].ToString();
            usuario.IdUf = Convert.ToInt32(reader["ID_UF"]);
            usuario.DataNascimento = Convert.ToDateTime(reader["DATA_NASCIMENTO"]);

            usuarios.Add(usuario);
        }

        conn.Close();

        return usuarios;
    }

    // Método responsável por listar todos os interesses cadastrados
    public List<Interesse> ListarInteresses()
    {
        List<Interesse> interesses = new List<Interesse>();

        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM INTERESSE";

        SqlCommand cmd = new SqlCommand(sql, conn);

        SqlDataReader reader = cmd.ExecuteReader();

        while (reader.Read())
        {
            Interesse interesse = new Interesse();

            interesse.IdInteresse = Convert.ToInt32(reader["ID_INTERESSE"]);
            interesse.IdUsuario = Convert.ToInt32(reader["ID_USUARIO"]);
            interesse.Tipo = reader["DSC_TIPO"].ToString();
            interesse.Descricao = reader["DSC_DESCRICAO"].ToString();
            interesse.IdStatus = Convert.ToInt32(reader["ID_STATUS"]);
            interesse.DataRegistro = Convert.ToDateTime(reader["DATA_REGISTRO"]);

            interesses.Add(interesse);
        }

        conn.Close();

        return interesses;
    }

    // Método responsável por buscar interesses de um usuário específico
    public List<Interesse> BuscarInteressesPorUsuario(int idUsuario)
    {
        List<Interesse> interesses = new List<Interesse>();

        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM INTERESSE WHERE ID_USUARIO = @idUsuario";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@idUsuario", idUsuario);

        SqlDataReader reader = cmd.ExecuteReader();

        while (reader.Read())
        {
            Interesse interesse = new Interesse();

            interesse.IdInteresse = Convert.ToInt32(reader["ID_INTERESSE"]);
            interesse.IdUsuario = Convert.ToInt32(reader["ID_USUARIO"]);
            interesse.Tipo = reader["DSC_TIPO"].ToString();
            interesse.Descricao = reader["DSC_DESCRICAO"].ToString();
            interesse.IdStatus = Convert.ToInt32(reader["ID_STATUS"]);
            interesse.DataRegistro = Convert.ToDateTime(reader["DATA_REGISTRO"]);

            interesses.Add(interesse);
        }

        conn.Close();

        return interesses;
    }

    // Método responsável por atualizar o status do interesse
    public void AtualizarStatusInteresse(int idInteresse, int novoStatus)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"UPDATE INTERESSE
               SET ID_STATUS = @status
               WHERE ID_INTERESSE = @id";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@status", novoStatus);
        cmd.Parameters.AddWithValue("@id", idInteresse);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por cadastrar administradores
    public void InserirAdmin(string nome, string email, string senha)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"INSERT INTO ADMIN
               (ID_ONG, DSC_NOME, DSC_EMAIL, DSC_SENHA)

               VALUES
               (1, @nome, @email, @senha)";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@nome", nome);
        cmd.Parameters.AddWithValue("@email", email);

        // Criptografia da senha utilizando BCrypt
        cmd.Parameters.AddWithValue("@senha", BCrypt.Net.BCrypt.HashPassword(senha));

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por atualizar interesses cadastrados
    public void AtualizarInteresse(Interesse interesse)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"UPDATE INTERESSE
               SET DSC_TIPO = @tipo,
                   DSC_DESCRICAO = @descricao,
                   ID_STATUS = @status
               WHERE ID_INTERESSE = @id";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@tipo", interesse.Tipo);
        cmd.Parameters.AddWithValue("@descricao", interesse.Descricao);
        cmd.Parameters.AddWithValue("@status", interesse.IdStatus);
        cmd.Parameters.AddWithValue("@id", interesse.IdInteresse);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por buscar interesse pelo ID
    public Interesse BuscarInteressePorId(int id)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM INTERESSE WHERE ID_INTERESSE = @id";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", id);

        SqlDataReader reader = cmd.ExecuteReader();

        Interesse interesse = null;

        if (reader.Read())
        {
            interesse = new Interesse();

            interesse.IdInteresse = Convert.ToInt32(reader["ID_INTERESSE"]);
            interesse.IdUsuario = Convert.ToInt32(reader["ID_USUARIO"]);
            interesse.Tipo = reader["DSC_TIPO"].ToString();
            interesse.Descricao = reader["DSC_DESCRICAO"].ToString();
            interesse.IdStatus = Convert.ToInt32(reader["ID_STATUS"]);

            interesse.DataRegistro =
                Convert.ToDateTime(reader["DATA_REGISTRO"]);
        }

        conn.Close();

        return interesse;
    }

    // Método responsável por remover interesses do banco
    public void RemoverInteresse(int id)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "DELETE FROM INTERESSE WHERE ID_INTERESSE = @id";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", id);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por remover administradores
    public void RemoverAdministrador(string email)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "DELETE FROM ADMIN WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por consultar os dados da ONG
    public void ConsultarOng()
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM ONG WHERE ID_ONG = 1";

        SqlCommand cmd = new SqlCommand(sql, conn);

        SqlDataReader reader = cmd.ExecuteReader();

        if (reader.Read())
        {
            Console.WriteLine("\n=== DADOS DA ONG ===");

            Console.WriteLine($"Nome: {reader["DSC_NOME"]}");
            Console.WriteLine($"Telefone: {reader["NUM_TELEFONE"]}");
            Console.WriteLine($"Email: {reader["DSC_EMAIL"]}");
            Console.WriteLine($"Endereço: {reader["DSC_ENDERECO"]}");
            Console.WriteLine($"CNPJ: {reader["NUM_CNPJ"]}");
        }

        conn.Close();
    }

    // Método responsável por listar interesses junto com os usuários
    public List<string> ListarInteressesComUsuario()
    {
        List<string> lista = new List<string>();

        SqlConnection conn = AbrirConexao();

        // Consulta utilizando INNER JOIN entre as tabelas
        string sql = @"
SELECT
    I.ID_INTERESSE,
    U.DSC_NOME,
    I.DSC_DESCRICAO,
    S.DSC_STATUS,
    I.DATA_REGISTRO

FROM INTERESSE I

INNER JOIN USUARIO U
ON I.ID_USUARIO = U.ID_USUARIO

INNER JOIN STATUS S
ON I.ID_STATUS = S.ID_STATUS

ORDER BY I.DATA_REGISTRO DESC";

        SqlCommand cmd = new SqlCommand(sql, conn);

        SqlDataReader reader = cmd.ExecuteReader();

        while (reader.Read())
        {
            string texto =
                $"Usuário: {reader["DSC_NOME"]} | " +
                $"Descrição: {reader["DSC_DESCRICAO"]} | " +
                $"Status: {reader["DSC_STATUS"]} | " +
                $"Data: {Convert.ToDateTime(reader["DATA_REGISTRO"]):dd/MM/yyyy}";

            lista.Add(texto);
        }

        conn.Close();

        return lista;
    }

    // Método responsável por verificar se o email do administrador já existe
    public bool EmailAdminExiste(string email)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT COUNT(*) FROM ADMIN WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        int quantidade = Convert.ToInt32(cmd.ExecuteScalar());

        conn.Close();

        return quantidade > 0;
    }

    // Método responsável pelo login do administrador
    public bool LoginAdmin(string email, string senha)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT DSC_SENHA FROM ADMIN WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        object resultado = cmd.ExecuteScalar();

        conn.Close();

        if (resultado == null)
        {
            return false;
        }

        string senhaBanco = resultado.ToString();

        // Verificação da senha criptografada utilizando BCrypt
        return BCrypt.Net.BCrypt.Verify(senha, senhaBanco);
    }

    // Método responsável pelo login do atendente
    public bool LoginAtendente(string email, string senha)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT DSC_SENHA FROM ATENDENTE WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        object resultado = cmd.ExecuteScalar();

        conn.Close();

        if (resultado == null)
        {
            return false;
        }

        string senhaBanco = resultado.ToString();

        // Verificação da senha criptografada utilizando BCrypt
        return BCrypt.Net.BCrypt.Verify(senha, senhaBanco);
    }

    // Método responsável por atualizar os dados da ONG
    public void AtualizarOng(string telefone, string email, string endereco)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"
UPDATE ONG
SET
    NUM_TELEFONE = ISNULL(@telefone, NUM_TELEFONE),
    DSC_EMAIL = ISNULL(@email, DSC_EMAIL),
    DSC_ENDERECO = ISNULL(@endereco, DSC_ENDERECO)
WHERE ID_ONG = 1";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@telefone",
            (object?)telefone ?? DBNull.Value);

        cmd.Parameters.AddWithValue("@email",
            (object?)email ?? DBNull.Value);

        cmd.Parameters.AddWithValue("@endereco",
            (object?)endereco ?? DBNull.Value);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por verificar se o CPF já existe no banco
    public bool CpfExiste(string cpf)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT COUNT(*) FROM USUARIO WHERE NUM_CPF = @cpf";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@cpf", cpf);

        int quantidade = Convert.ToInt32(cmd.ExecuteScalar());

        conn.Close();

        return quantidade > 0;
    }

    // Método responsável por buscar o nome do status
    public string BuscarNomeStatus(int idStatus)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT DSC_STATUS FROM STATUS WHERE ID_STATUS = @id";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", idStatus);

        object resultado = cmd.ExecuteScalar();

        conn.Close();

        // Verifica se o status existe no banco
        if (resultado == null)
        {
            return "Status não encontrado";
        }

        return resultado.ToString();
    }

    // Método responsável por buscar a sigla do estado (UF)
    public string BuscarSiglaUf(int idUf)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT SIGLA_UF FROM UF WHERE ID_UF = @id";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", idUf);

        object resultado = cmd.ExecuteScalar();

        conn.Close();

        // Verifica se a UF existe no banco
        if (resultado == null)
        {
            return "UF não encontrada";
        }

        return resultado.ToString();
    }
    // Método responsável por cadastrar atendentes
    public void InserirAtendente(string nome, string email, string senha)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"INSERT INTO ATENDENTE
           (ID_ONG, DSC_NOME, DSC_EMAIL, DSC_SENHA)

           VALUES
           (1, @nome, @email, @senha)";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@nome", nome);
        cmd.Parameters.AddWithValue("@email", email);

        // Criptografia da senha utilizando BCrypt
        cmd.Parameters.AddWithValue("@senha", BCrypt.Net.BCrypt.HashPassword(senha));

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por remover atendentes
    public void RemoverAtendente(string email)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "DELETE FROM ATENDENTE WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por verificar se o email do atendente já existe
    public bool EmailAtendenteExiste(string email)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT COUNT(*) FROM ATENDENTE WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        int quantidade = Convert.ToInt32(cmd.ExecuteScalar());

        conn.Close();

        return quantidade > 0;
    }

    // Método responsável por editar dados do atendente
    public void EditarAtendente(
    string emailAtual,
    string novoEmail,
    string novaSenha)
    {
        SqlConnection conn = AbrirConexao();

        string sql;

        SqlCommand cmd;

        // Mantém a senha atual caso o usuário pressione ENTER
        if (string.IsNullOrWhiteSpace(novaSenha))
        {
            sql = @"
    UPDATE ATENDENTE
    SET DSC_EMAIL = @novoEmail
    WHERE DSC_EMAIL = @emailAtual";

            cmd = new SqlCommand(sql, conn);

            cmd.Parameters.AddWithValue("@novoEmail", novoEmail);
            cmd.Parameters.AddWithValue("@emailAtual", emailAtual);
        }
        else
        {
            sql = @"
    UPDATE ATENDENTE
    SET DSC_EMAIL = @novoEmail,
        DSC_SENHA = @novaSenha
    WHERE DSC_EMAIL = @emailAtual";

            cmd = new SqlCommand(sql, conn);

            cmd.Parameters.AddWithValue("@novoEmail", novoEmail);

            // Criptografia da nova senha
            string senhaCriptografada =
                BCrypt.Net.BCrypt.HashPassword(novaSenha);

            cmd.Parameters.AddWithValue("@novaSenha", senhaCriptografada);

            cmd.Parameters.AddWithValue("@emailAtual", emailAtual);
        }

        cmd.ExecuteNonQuery();

        conn.Close();
    }
    // Método responsável por buscar usuário pelo ID
    public Usuario BuscarUsuarioPorId(int id)
    {
        Usuario usuario = null;

        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM USUARIO WHERE ID_USUARIO = @id";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@id", id);

        SqlDataReader reader = cmd.ExecuteReader();

        if (reader.Read())
        {
            usuario = new Usuario();

            usuario.IdUsuario = Convert.ToInt32(reader["ID_USUARIO"]);
            usuario.Nome = reader["DSC_NOME"].ToString();
        }

        conn.Close();

        return usuario;
    }

    // Método responsável por buscar usuário pelo nome
    public Usuario BuscarUsuarioPorNome(string nome)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM USUARIO WHERE DSC_NOME = @nome";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@nome", nome);

        SqlDataReader reader = cmd.ExecuteReader();

        Usuario usuario = null;

        if (reader.Read())
        {
            usuario = new Usuario();

            usuario.IdUsuario = Convert.ToInt32(reader["ID_USUARIO"]);
            usuario.Nome = reader["DSC_NOME"].ToString();

            usuario.Cpf = reader["NUM_CPF"].ToString();
            usuario.Telefone = reader["NUM_TELEFONE"].ToString();
            usuario.Email = reader["DSC_EMAIL"].ToString();
            usuario.Endereco = reader["DSC_ENDERECO"].ToString();
        }

        conn.Close();

        return usuario;
    }

    // Método responsável por atualizar senha do administrador
    public void AtualizarSenhaAdmin(string email, string novaSenha)
    {
        SqlConnection conn = AbrirConexao();

        string sql = @"
                 UPDATE ADMIN
                 SET DSC_SENHA = @senha
                 WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        // Criptografia da nova senha
        cmd.Parameters.AddWithValue("@senha", BCrypt.Net.BCrypt.HashPassword(novaSenha));

        cmd.Parameters.AddWithValue("@email", email);

        cmd.ExecuteNonQuery();

        conn.Close();
    }

    // Método responsável por buscar administrador pelo email
    public Admin BuscarAdminPorEmail(string email)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM ADMIN WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        SqlDataReader reader = cmd.ExecuteReader();

        if (reader.Read())
        {
            Admin admin = new Admin();

            admin.Email = reader["DSC_EMAIL"].ToString();

            conn.Close();

            return admin;
        }

        conn.Close();

        return null;
    }

    // Método responsável por buscar atendente pelo email
    public Atendente BuscarAtendentePorEmail(string email)
    {
        SqlConnection conn = AbrirConexao();

        string sql = "SELECT * FROM ATENDENTE WHERE DSC_EMAIL = @email";

        SqlCommand cmd = new SqlCommand(sql, conn);

        cmd.Parameters.AddWithValue("@email", email);

        SqlDataReader reader = cmd.ExecuteReader();

        if (reader.Read())
        {
            Atendente atendente = new Atendente();

            atendente.IdAtendente =
                Convert.ToInt32(reader["ID_ATENDENTE"]);

            atendente.Nome =
                reader["DSC_NOME"].ToString();

            atendente.Email =
                reader["DSC_EMAIL"].ToString();

            atendente.Senha =
                reader["DSC_SENHA"].ToString();

            conn.Close();

            return atendente;
        }

        conn.Close();

        return null;
    }

    // Método responsável por editar administrador
    public void EditarAdministrador(
        string emailAtual,
        string novoEmail,
        string novaSenha)
    {
        SqlConnection conn = AbrirConexao();

        string sql;

        SqlCommand cmd;

        // Mantém a senha atual caso o usuário pressione ENTER
        if (string.IsNullOrWhiteSpace(novaSenha))
        {
            sql = @"
        UPDATE ADMIN
        SET DSC_EMAIL = @novoEmail
        WHERE DSC_EMAIL = @emailAtual";

            cmd = new SqlCommand(sql, conn);

            cmd.Parameters.AddWithValue("@novoEmail", novoEmail);
            cmd.Parameters.AddWithValue("@emailAtual", emailAtual);
        }
        else
        {
            sql = @"
        UPDATE ADMIN
        SET DSC_EMAIL = @novoEmail,
            DSC_SENHA = @senha
        WHERE DSC_EMAIL = @emailAtual";

            cmd = new SqlCommand(sql, conn);

            // Criptografia da nova senha
            string senhaCriptografada =
                BCrypt.Net.BCrypt.HashPassword(novaSenha);

            cmd.Parameters.AddWithValue("@novoEmail", novoEmail);
            cmd.Parameters.AddWithValue("@senha", senhaCriptografada);
            cmd.Parameters.AddWithValue("@emailAtual", emailAtual);
        }

        cmd.ExecuteNonQuery();

        conn.Close();
    }
}